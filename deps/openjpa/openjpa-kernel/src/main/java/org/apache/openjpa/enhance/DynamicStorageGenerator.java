/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.enhance;

import java.lang.reflect.Constructor;
import java.security.AccessController;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.util.InternalException;
import serp.bytecode.BCClass;
import serp.bytecode.BCClassLoader;
import serp.bytecode.BCField;
import serp.bytecode.BCMethod;
import serp.bytecode.Code;
import serp.bytecode.Constants;
import serp.bytecode.Instruction;
import serp.bytecode.JumpInstruction;
import serp.bytecode.LoadInstruction;
import serp.bytecode.Project;
import serp.bytecode.TableSwitchInstruction;

/**
 * Factory for creating new {@link DynamicStorage} classes. Can be
 * extended to decorate/modify the generated instances behavior.
 *
 * @author Steve Kim
 * @nojavadoc
 * @since 0.3.2.0
 */
public class DynamicStorageGenerator {

    // prefix for generic generated classes.
    private static final String PREFIX = "openjpastorage$";

    /**
     * Constant to throw an exception on invalid index passed to type set/get
     * methods
     */
    protected static final int POLICY_EXCEPTION = 0;

    /**
     * Constant to not generate type set/get methods.
     */
    protected static final int POLICY_EMPTY = 1;

    /**
     * Constant to be as silent as possible during invalid index passed
     * to set/get type methods. On getting an Object, for example,
     * null will be returned.
     * However, on primitive gets, an exception will be thrown.
     */
    protected static final int POLICY_SILENT = 2;

    // wrappers for primitive types
    private static final Class[][] WRAPPERS = new Class[][]{
        { boolean.class, Boolean.class },
        { byte.class, Byte.class },
        { char.class, Character.class },
        { int.class, Integer.class },
        { short.class, Short.class },
        { long.class, Long.class },
        { float.class, Float.class },
        { double.class, Double.class },
    };

    // primitive types
    private static final int[] TYPES = new int[]{
        JavaTypes.BOOLEAN,
        JavaTypes.BYTE,
        JavaTypes.CHAR,
        JavaTypes.INT,
        JavaTypes.SHORT,
        JavaTypes.LONG,
        JavaTypes.FLOAT,
        JavaTypes.DOUBLE,
        JavaTypes.OBJECT
    };

    // the project/classloader for the classes.
    private final Project _project = new Project();
    private final BCClassLoader _loader =
        AccessController.doPrivileged(J2DoPrivHelper.newBCClassLoaderAction(
            _project, AccessController.doPrivileged(J2DoPrivHelper
                .getClassLoaderAction(DynamicStorage.class))));

    /**
     * Generate a generic {@link DynamicStorage} instance with the given
     * array of {@link JavaTypes} constants and the given object as
     * the user key for generation.
     */
    public DynamicStorage generateStorage(int[] types, Object obj) {
        if (obj == null)
            return null;

        String name = getClassName(obj);
        BCClass bc = _project.loadClass(name);
        declareClasses(bc);
        bc.addDefaultConstructor().makePublic();

        int objectCount = declareFields(types, bc);
        addFactoryMethod(bc);
        addFieldCount(bc, types, objectCount);
        addSetMethods(bc, types, objectCount);
        addGetMethods(bc, types);
        addInitialize(bc, objectCount);
        decorate(obj, bc, types);
        return createFactory(bc);
    }

    /**
     * Return a class name to use for the given user key. By default,
     * returns the stringified key prefixed by PREFIX.
     */
    protected String getClassName(Object obj) {
        return PREFIX + obj.toString();
    }

    /**
     * Return the default field ACCESS constant for generated fields from
     * {@link Constants}.
     */
    protected int getFieldAccess() {
        return Constants.ACCESS_PRIVATE;
    }

    /**
     * Return the name for the generated field at the given index. Returns
     * <code>"field" + i</code> by default.
     */
    protected String getFieldName(int index) {
        return "field" + index;
    }

    /**
     * Return the policy constant for how to create type methods.
     */
    protected int getCreateFieldMethods(int type) {
        return POLICY_EXCEPTION;
    }

    /**
     * Decorate the generated class.
     */
    protected void decorate(Object obj, BCClass cls, int[] types) {
    }

    /**
     * Create a stub factory instance for the given class.
     */
    protected DynamicStorage createFactory(BCClass bc) {
        try {
            Class cls = Class.forName(bc.getName(), false, _loader);
            Constructor cons = cls.getConstructor((Class[]) null);
            DynamicStorage data = (DynamicStorage) cons.newInstance
                ((Object[]) null);
            _project.clear(); // remove old refs
            return data;
        } catch (Throwable t) {
            throw new InternalException("cons-access", t).setFatal(true);
        }
    }

    /**
     * Add interface or superclass declarations to the generated class.
     */
    protected void declareClasses(BCClass bc) {
        bc.declareInterface(DynamicStorage.class);
    }

    /**
     * Implement the newInstance method.
     */
    private void addFactoryMethod(BCClass bc) {
        BCMethod method = bc.declareMethod("newInstance",
            DynamicStorage.class, null);
        Code code = method.getCode(true);
        code.anew().setType(bc);
        code.dup();
        code.invokespecial().setMethod(bc.getName(), "<init>", "void", null);
        code.areturn();
        code.calculateMaxLocals();
        code.calculateMaxStack();
    }

    /**
     * Implement getFieldCount/getObjectCount.
     */
    private void addFieldCount(BCClass bc, int[] types, int objectCount) {
        BCMethod method = bc.declareMethod("getFieldCount", int.class, null);
        Code code = method.getCode(true);
        code.constant().setValue(types.length);
        code.ireturn();
        code.calculateMaxLocals();
        code.calculateMaxStack();

        method = bc.declareMethod("getObjectCount", int.class, null);
        code = method.getCode(true);
        code.constant().setValue(objectCount);
        code.ireturn();
        code.calculateMaxLocals();
        code.calculateMaxStack();
    }

    /**
     * Implement initialize.
     */
    private void addInitialize(BCClass bc, int objectCount) {
        BCMethod meth = bc.declareMethod("initialize", void.class, null);
        Code code = meth.getCode(true);
        JumpInstruction ifins = null;
        if (objectCount > 0) {
            // if (objects == null)
            // 		objects = new Object[objectCount];
            code.aload().setThis();
            code.getfield().setField("objects", Object[].class);
            ifins = code.ifnonnull();
            code.aload().setThis();
            code.constant().setValue(objectCount);
            code.anewarray().setType(Object.class);
            code.putfield().setField("objects", Object[].class);
        }
        Instruction ins = code.vreturn();
        if (ifins != null)
            ifins.setTarget(ins);
        code.calculateMaxLocals();
        code.calculateMaxStack();
    }

    /**
     * Declare the primitive fields and the object field.
     */
    private int declareFields(int[] types, BCClass bc) {
        bc.declareField("objects", Object[].class).makePrivate();

        int objectCount = 0;
        Class type;
        for (int i = 0; i < types.length; i++) {
            type = forType(types[i]);
            if (type == Object.class)
                objectCount++;
            else {
                BCField field = bc.declareField(getFieldName(i), type);
                field.setAccessFlags(getFieldAccess());
            }
        }
        return objectCount;
    }

    /**
     * Add all the typed set by index method.
     */
    private void addSetMethods(BCClass bc, int[] types, int totalObjects) {
        for (int i = 0; i < TYPES.length; i++)
            addSetMethod(TYPES[i], bc, types, totalObjects);
    }

    /**
     * Add the typed set by index method.
     */
    private void addSetMethod(int typeCode, BCClass bc, int[] types,
        int totalObjects) {
        int handle = getCreateFieldMethods(typeCode);
        if (handle == POLICY_EMPTY)
            return;
        Class type = forType(typeCode);
        // public void set<Type> (int field, <type> val)
        String name = Object.class.equals(type) ? "Object" :
            StringUtils.capitalize(type.getName());
        name = "set" + name;
        BCMethod method = bc.declareMethod(name, void.class,
            new Class[]{ int.class, type });
        method.makePublic();
        Code code = method.getCode(true);
        // switch (field)
        code.aload().setParam(0);
        TableSwitchInstruction tabins = code.tableswitch();
        tabins.setLow(0);
        tabins.setHigh(types.length - 1);
        Instruction defaultIns;
        if (handle == POLICY_SILENT)
            defaultIns = code.vreturn();
        else
            defaultIns = throwException
                (code, IllegalArgumentException.class);
        tabins.setDefaultTarget(defaultIns);
        int objectCount = 0;
        for (int i = 0; i < types.length; i++) {
            // default: throw new IllegalArgumentException
            if (!isCompatible(types[i], typeCode)) {
                tabins.addTarget(tabins.getDefaultTarget());
                continue;
            }

            tabins.addTarget(code.aload().setThis());
            if (typeCode >= JavaTypes.OBJECT) {
                // if (objects == null)
                // 		objects = new Object[totalObjects];
                code.aload().setThis();
                code.getfield().setField("objects", Object[].class);
                JumpInstruction ifins = code.ifnonnull();
                code.aload().setThis();
                code.constant().setValue(totalObjects);
                code.anewarray().setType(Object.class);
                code.putfield().setField("objects", Object[].class);

                // objects[objectCount] = val;
                ifins.setTarget(code.aload().setThis());
                code.getfield().setField("objects", Object[].class);
                code.constant().setValue(objectCount);
                code.aload().setParam(1);
                code.aastore();
                objectCount++;
            } else {
                // case i: fieldi = val;
                LoadInstruction load = code.xload();
                load.setType(type);
                load.setParam(1);
                code.putfield().setField("field" + i, type);
            }
            // return
            code.vreturn();
        }
        code.calculateMaxLocals();
        code.calculateMaxStack();
    }

    /**
     * Add all typed get by index method for the given fields.
     */
    private void addGetMethods(BCClass bc, int[] types) {
        for (int i = 0; i < TYPES.length; i++)
            addGetMethod(TYPES[i], bc, types);
    }

    /**
     * Add typed get by index method.
     */
    private void addGetMethod(int typeCode, BCClass bc, int[] types) {
        int handle = getCreateFieldMethods(typeCode);
        if (handle == POLICY_EMPTY)
            return;
        Class type = forType(typeCode);
        // public <type> get<Type>Field (int field)
        String name = Object.class.equals(type) ? "Object" :
            StringUtils.capitalize(type.getName());
        name = "get" + name;
        BCMethod method = bc.declareMethod(name, type,
            new Class[]{ int.class });
        method.makePublic();
        Code code = method.getCode(true);
        // switch (field)
        code.aload().setParam(0);
        TableSwitchInstruction tabins = code.tableswitch();
        tabins.setLow(0);
        tabins.setHigh(types.length - 1);
        Instruction defaultIns = null;
        if (typeCode == JavaTypes.OBJECT && handle == POLICY_SILENT) {
            defaultIns = code.constant().setNull();
            code.areturn();
        } else
            defaultIns = throwException
                (code, IllegalArgumentException.class);
        tabins.setDefaultTarget(defaultIns);
        int objectCount = 0;
        for (int i = 0; i < types.length; i++) {
            // default: throw new IllegalArgumentException
            if (!isCompatible(types[i], typeCode)) {
                tabins.addTarget(tabins.getDefaultTarget());
                continue;
            }

            tabins.addTarget(code.aload().setThis());
            if (typeCode >= JavaTypes.OBJECT) {
                // if (objects == null)
                // 		return null;
                // return objects[objectCount];
                code.aload().setThis();
                code.getfield().setField("objects", Object[].class);
                JumpInstruction ifins = code.ifnonnull();
                code.constant().setNull();
                code.areturn();
                ifins.setTarget(code.aload().setThis());
                code.getfield().setField("objects", Object[].class);
                code.constant().setValue(objectCount);
                code.aaload();
                code.areturn();
                objectCount++;
            } else {
                // case i: return fieldi;
                code.getfield().setField("field" + i, type);
                code.xreturn().setType(type);
            }
        }
        code.calculateMaxLocals();
        code.calculateMaxStack();
    }

    /////////////
    // Utilities
    /////////////

    /**
     * Clear code associated with the given method signature, and return
     * the empty code. Will return null if the method should be empty.
     */
    protected Code replaceMethod(BCClass bc, String name, Class retType,
        Class[] args, boolean remove) {
        bc.removeDeclaredMethod(name, args);
        BCMethod meth = bc.declareMethod(name, retType, args);
        Code code = meth.getCode(true);
        if (!remove)
            return code;
        code.xreturn().setType(retType);
        code.calculateMaxStack();
        code.calculateMaxLocals();
        return null;
    }

    /**
     * Add a bean field of the given name and type.
     */
    protected BCField addBeanField(BCClass bc, String name, Class type) {
        if (name == null)
            throw new IllegalArgumentException("name == null");

        // private <type> <name>
        BCField field = bc.declareField(name, type);
        field.setAccessFlags(getFieldAccess());
        name = StringUtils.capitalize(name);

        // getter
        String prefix = (type == boolean.class) ? "is" : "get";
        BCMethod method = bc.declareMethod(prefix + name, type, null);
        method.makePublic();
        Code code = method.getCode(true);
        code.aload().setThis();
        code.getfield().setField(field);
        code.xreturn().setType(type);
        code.calculateMaxStack();
        code.calculateMaxLocals();

        // setter
        method = bc.declareMethod("set" + name, void.class,
            new Class[]{ type });
        method.makePublic();
        code = method.getCode(true);
        code.aload().setThis();
        code.xload().setParam(0).setType(type);
        code.putfield().setField(field);
        code.vreturn();
        code.calculateMaxStack();
        code.calculateMaxLocals();
        return field;
    }

    /**
     * Return true if the given field type and storage type are compatible.
     */
    protected boolean isCompatible(int fieldType, int storageType) {
        if (storageType == JavaTypes.OBJECT)
            return fieldType >= JavaTypes.OBJECT;
        return fieldType == storageType;
    }

    /**
     * Throw an exception of the given type.
     */
    protected Instruction throwException(Code code, Class type) {
        Instruction ins = code.anew().setType(type);
        code.dup();
        code.invokespecial().setMethod(type, "<init>", void.class, null);
        code.athrow();
        return ins;
    }

    /**
     * Return the proper type for the given {@link JavaTypes} constant.
     */
    protected Class forType(int type) {
        switch (type) {
            case JavaTypes.BOOLEAN:
                return boolean.class;
            case JavaTypes.BYTE:
                return byte.class;
            case JavaTypes.CHAR:
                return char.class;
            case JavaTypes.INT:
                return int.class;
            case JavaTypes.SHORT:
                return short.class;
            case JavaTypes.LONG:
                return long.class;
            case JavaTypes.FLOAT:
                return float.class;
            case JavaTypes.DOUBLE:
                return double.class;
        }
        return Object.class;
    }

    /**
     * get the wrapper for the given {@link JavaTypes} constant.
     */
    protected Class getWrapper(int type) {
        return getWrapper(forType(type));
    }

    /**
     * Get the wrapper for the given type.
     */
    protected Class getWrapper(Class c) {
        for (int i = 0; i < WRAPPERS.length; i++) {
            if (WRAPPERS[i][0].equals (c))
				return WRAPPERS[i][1];
		}
		return c;
	}
}

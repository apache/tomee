/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.core.cmp.cmp2;

import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.xbean.asm8.ClassWriter;
import org.apache.xbean.asm8.FieldVisitor;
import org.apache.xbean.asm8.Label;
import org.apache.xbean.asm8.MethodVisitor;
import org.apache.xbean.asm8.Opcodes;
import org.apache.xbean.asm8.Type;

import javax.ejb.EntityContext;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Code generate for CMP level 2 beans.  This will
 * generate the concrete class used to instantiate
 * the bean instance.
 */
public class Cmp2Generator implements Opcodes {
    private static final String UNKNOWN_PK_NAME = "OpenEJB_pk";
    private static final Type UNKNOWN_PK_TYPE = Type.getType(Long.class);

    private final String implClassName;
    private final String beanClassName;
    private final ClassWriter cw;
    private final Map<String, CmpField> cmpFields = new LinkedHashMap<>();
    private final Collection<CmrField> cmrFields = new ArrayList<>();
    private final CmpField pkField;
    private final Class primKeyClass;
    private final List<Method> selectMethods = new ArrayList<>();
    private final Class beanClass;
    private final PostCreateGenerator postCreateGenerator;
    private static final String DELETED = "openejb_deleted";

    /**
     * Constructor for a Cmp2Generator.  This validates the
     * initial EJB state information and prepares for the
     * code generation process.
     *
     * @param cmpImplClass The name of the implementation class we're generating.
     * @param beanClass    The bean implementation class that is our starting
     *                     point for code generation.
     * @param pkField      The name of the primary key field (optional if the
     *                     primary key class is given).
     * @param primKeyClass The optional primary key class for complex primary
     *                     keys.
     * @param cmpFields    The list of fields that are managed using cmp.
     */
    public Cmp2Generator(final String cmpImplClass, final Class beanClass, final String pkField, final Class<?> primKeyClass, final String[] cmpFields) {

        this.beanClass = beanClass;
        beanClassName = Type.getInternalName(beanClass);
        implClassName = cmpImplClass.replace('.', '/');

        if (pkField == null && primKeyClass == null) {
            throw new NullPointerException("Both pkField and primKeyClass are null for bean " + beanClassName);
        }

        this.primKeyClass = primKeyClass;


        // for each of the defined cmp fields, we need to locate the getter method 
        // for the name and retrieve the type.  This a) verifies that we have at least 
        // a getter defined and b) that we know the field return type.  The created CmpField 
        // list will feed into the generation process. 
        for (final String cmpFieldName : cmpFields) {
            final Method getter = getterMethod(cmpFieldName);
            if (getter == null) {
                throw new IllegalArgumentException("No such property " + cmpFieldName + " defined on bean class " + beanClassName);
            }
            // if this is an abstract method, then it's one we have to generate 
            if (Modifier.isAbstract(getter.getModifiers())) {

                final Type type = Type.getType(getter.getReturnType());
                final CmpField cmpField = new CmpField(cmpFieldName, type, getter);
                this.cmpFields.put(cmpFieldName, cmpField);
            } else {
                // the getter is non-abstract.  We only allow this if the class that 
                // defines the getter also has a private field with a matching type.  
                try {
                    final Field field = getter.getDeclaringClass().getDeclaredField(cmpFieldName);
                    // if this is a private field, then just continue.  We don't need to generate 
                    // any code for this 
                    if (Modifier.isPrivate(field.getModifiers())) {
                        continue;
                    }
                } catch (final NoSuchFieldException e) {
                    // no-op
                }
                throw new IllegalArgumentException("No such property " + cmpFieldName + " defined on bean class " + beanClassName);
            }
        }

        // if a pkField is defined, it MUST be a CMP field.  Make sure it really exists 
        // in the list we constructed above. 
        if (pkField != null) {
            this.pkField = this.cmpFields.get(pkField);
            if (this.pkField == null) {
                throw new IllegalArgumentException("No such property " + pkField + " defined on bean class " + beanClassName);
            }
        } else {
            this.pkField = null;
        }

        // build up the list of ejbSelectxxxx methods.  These will be generated automatically. 
        for (final Method method : beanClass.getMethods()) {
            if (Modifier.isAbstract(method.getModifiers()) && method.getName().startsWith("ejbSelect")) {
                addSelectMethod(method);
            }
        }

        // The class writer will be used for all generator activies, while the 
        // postCreateGenerator will be used to add the ejbPostCreatexxxx methods as a 
        // last step. 
        cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        postCreateGenerator = new PostCreateGenerator(beanClass, cw);
    }

    /**
     * Add a field to the list of fields defined as CMR
     * fields.  Note that if the field is also defined
     * as a CMP field, it will be removed from the normal
     * CMP list.
     *
     * @param cmrField The new CMR field definition pulled from the
     *                 EJB metadata.
     */
    public void addCmrField(final CmrField cmrField) {
        if (cmpFields.get(cmrField.getName()) != null) {
            cmpFields.remove(cmrField.getName());
        }
        cmrFields.add(cmrField);
    }

    /**
     * Add a method to the list of ejbSelect methods that
     * need to be processed.
     *
     * @param selectMethod The method that needs to be processed.
     */
    public void addSelectMethod(final Method selectMethod) {
        selectMethods.add(selectMethod);
    }

    /**
     * Perform the generation step for a CMP Entity Bean.
     * This uses the accumulated meta data and the
     * base bean class to generate a subclass with
     * the automatically generated bits of OpenEJB infrastructure
     * hooks.
     *
     * @return The class file byte array to be used for defining this
     * class.
     */
    public byte[] generate() {
        // generate the class as super class of the base bean class.  This class will also implment 
        // EntityBean and Cmp2Entity. 
        cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, implClassName, null, beanClassName, new String[]{"org/apache/openejb/core/cmp/cmp2/Cmp2Entity", "javax/ejb/EntityBean"});

        // public static Object deploymentInfo;
        {
            final FieldVisitor fv = cw.visitField(ACC_PUBLIC + ACC_STATIC, "deploymentInfo", "Ljava/lang/Object;", null, null);
            fv.visitEnd();
        }

        // private transient boolean deleted;
        {
            final FieldVisitor fv = cw.visitField(ACC_PRIVATE + ACC_TRANSIENT + ACC_VOLATILE, DELETED, "Z", null, null);
            fv.visitEnd();
        }

        if (Object.class.equals(primKeyClass)) {
            final FieldVisitor fv = cw.visitField(ACC_PRIVATE, UNKNOWN_PK_NAME, UNKNOWN_PK_TYPE.getDescriptor(), null, null);
            fv.visitEnd();
        }

        // Generate the set of cmp fields as private attributes. 
        // private ${cmpField.type} ${cmpField.name};
        for (final CmpField cmpField : cmpFields.values()) {
            createField(cmpField);
        }

        // and create the corresponding CMR fields as well. 
        for (final CmrField cmrField : cmrFields) {
            createCmrFields(cmrField);
        }

        createConstructor();

        // now for each of the CMP fields, generate the getter and setter methods 
        // from the abstract methods the bean author should have provided. 
        for (final CmpField cmpField : cmpFields.values()) {
            // public ${cmpField.type} get${cmpField.name}() {
            //     return this.${cmpField.name};
            // }
            createGetter(cmpField);

            // public void setId(${cmpField.type} ${cmpField.name}) {
            //    this.${cmpField.name} = ${cmpField.name};
            // }
            createSetter(cmpField);
        }

        // and repeat this for the cmr fields. 
        for (final CmrField cmrField : cmrFields) {
            createCmrGetter(cmrField);
            createCmrSetter(cmrField);
        }


        createSimplePrimaryKeyGetter();

        // add the set of OpenEJB container management methods. 
        createOpenEJB_isDeleted();
        createOpenEJB_deleted();
        createOpenEJB_addCmr();
        createOpenEJB_removeCmr();

        // generate the select methods 
        for (final Method selectMethod : selectMethods) {
            createSelectMethod(selectMethod);
        }


        // now automatically generate any of the ejb* methods.  According to the 
        // spec, the bean author should be responsble for these, but since these 
        // are frequently just nop stubs, we'll take responsibility for creating 
        // empty ones in the generated superclass. 
        if (!hasMethod(beanClass, "ejbActivate")) {
            createEjbActivate();
        }
        if (!hasMethod(beanClass, "ejbPassivate")) {
            createEjbPassivate();
        }
        if (!hasMethod(beanClass, "ejbLoad")) {
            createEjbLoad();
        }
        if (!hasMethod(beanClass, "ejbStore")) {
            createEjbStore();
        }
        if (!hasMethod(beanClass, "ejbRemove")) {
            createEjbRemove();
        }
        if (!hasMethod(beanClass, "setEntityContext", EntityContext.class)) {
            createSetEntityContext();
        }
        if (!hasMethod(beanClass, "unsetEntityContext")) {
            createUnsetEntityContext();
        }

        // add on any post-create methods that might be required. 
        postCreateGenerator.generate();

        cw.visitEnd();
        // the class, in theory, is now complete.  Return in byte[] form so this can 
        // be defined in the appropriate classloader instance. 
        return cw.toByteArray();
    }

    private boolean hasMethod(final Class beanClass, final String name, final Class... args) {
        try {
            final Method method = beanClass.getMethod(name, args);
            return !Modifier.isAbstract(method.getModifiers());
        } catch (final NoSuchMethodException e) {
            return false;
        }
    }

    private void createConstructor() {
        final MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, beanClassName, "<init>", "()V", false);

        for (final CmrField cmrField : cmrFields) {
            initCmrFields(mv, cmrField);
        }

        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void createOpenEJB_isDeleted() {
        final MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "OpenEJB_isDeleted", "()Z", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, implClassName, DELETED, "Z");
        mv.visitInsn(IRETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void createOpenEJB_deleted() {
        final MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "OpenEJB_deleted", "()V", null, null);
        mv.visitCode();

        /* if (deleted) return; */
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, implClassName, DELETED, "Z");
        final Label notDeleted = new Label();
        mv.visitJumpInsn(IFEQ, notDeleted);
        mv.visitInsn(RETURN);
        mv.visitLabel(notDeleted);

        // deleted = true;
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ICONST_1);
        mv.visitFieldInsn(PUTFIELD, implClassName, DELETED, "Z");

        for (final CmrField cmrField : cmrFields) {
            // ${cmrField.accessor}.delete(${cmrField.name});
            createOpenEJB_deleted(mv, cmrField);
        }

        // return;
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void createOpenEJB_addCmr() {
        final MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "OpenEJB_addCmr", "(Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/Object;", null, null);
        mv.visitCode();

        // if (deleted) return null;
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, implClassName, DELETED, "Z");
        final Label notDeleted = new Label();
        mv.visitJumpInsn(IFEQ, notDeleted);
        mv.visitInsn(ACONST_NULL);
        mv.visitInsn(ARETURN);
        mv.visitLabel(notDeleted);

        for (final CmrField cmrField : cmrFields) {
            // if ("${cmrField.name}".equals(name)) {
            //     ${cmrField.name}.add((${cmrField.type})value);
            //     return null;
            // }
            //
            // OR
            //
            // if ("${cmrField.name}".equals(name)) {
            //     Object oldValue = ${cmrField.name};
            //     ${cmrField.name} = (${cmrField.type}) bean;
            //     return oldValue;
            // }
            createOpenEJB_addCmr(mv, cmrField);
        }

        // throw new IllegalArgumentException("Unknown cmr field " + name + " on entity bean of type " + getClass().getName());
        mv.visitTypeInsn(NEW, "java/lang/IllegalArgumentException");
        mv.visitInsn(DUP);
        mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
        mv.visitLdcInsn("Unknown cmr field ");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        mv.visitLdcInsn(" on entity bean of type ");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getName", "()Ljava/lang/String;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/IllegalArgumentException", "<init>", "(Ljava/lang/String;)V", false);
        mv.visitInsn(ATHROW);

        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void createOpenEJB_removeCmr() {
        final MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "OpenEJB_removeCmr", "(Ljava/lang/String;Ljava/lang/Object;)V", null, null);
        mv.visitCode();

        // if (deleted) return;
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, implClassName, DELETED, "Z");
        final Label notDeleted = new Label();
        mv.visitJumpInsn(IFEQ, notDeleted);
        mv.visitInsn(RETURN);
        mv.visitLabel(notDeleted);

        for (final CmrField cmrField : cmrFields) {
            // if ("${cmrField.name}".equals(name)) {
            //     ${cmrField.name}.remove(value);
            //     return;
            // }
            //
            // OR
            //
            // if ("${cmrField.name}".equals(name)) {
            //     ${cmrField.name} = null;
            //     return;
            // }
            createOpenEJB_removeCmr(mv, cmrField);
        }

        // throw new IllegalArgumentException("Unknown cmr field " + name + " on entity bean of type " + getClass().getName());
        mv.visitTypeInsn(NEW, "java/lang/IllegalArgumentException");
        mv.visitInsn(DUP);
        mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
        mv.visitLdcInsn("Unknown cmr field ");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        mv.visitLdcInsn(" on entity bean of type ");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getName", "()Ljava/lang/String;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/IllegalArgumentException", "<init>", "(Ljava/lang/String;)V", false);
        mv.visitInsn(ATHROW);

        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    /**
     * Add a CMP field to the class.  The field is created
     * with private scope.
     *
     * @param cmpField The Cmp field defined in the metadata.
     */
    private void createField(final CmpField cmpField) {
        final FieldVisitor fv = cw.visitField(ACC_PRIVATE,
            cmpField.getName(),
            cmpField.getDescriptor(),
            null,
            null);
        fv.visitEnd();
    }


    /**
     * Generate a concrete getter field for a CMP field.
     * At this point, we're just generating a simple
     * accessor for the field, given the type.  The
     * JPA engine when it makes this implementation class
     * a managed class define whatever additional logic
     * might be required.
     *
     * @param cmpField The CMP field backing this getter method.
     */
    private void createGetter(final CmpField cmpField) {
        final String methodName = cmpField.getGetterMethod().getName();
        final MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, methodName, "()" + cmpField.getDescriptor(), null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, implClassName, cmpField.getName(), cmpField.getDescriptor());
        mv.visitInsn(cmpField.getType().getOpcode(IRETURN));
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    /**
     * Generate the getter name for a CMR property.
     *
     * @param propertyName The name of the CMR property.
     * @return The string name of the getter method for the
     * property.
     */
    private static String getterName(final String propertyName) {
        return "get" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
    }


    /**
     * Get the getter method for this CMP field.  This
     * will be either get<Name> or is<Name> depending on
     * what abstract method is defined on the base bean
     * class.
     *
     * @param propertyName The name of the CMP field.
     * @return The name to be used for generating this method.
     */
    private Method getterMethod(final String propertyName) {
        String getterName = "get" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
        try {
            // check to see if we have the getter as an abstract class.  This might be an "is" method. 
            return beanClass.getMethod(getterName, new Class[0]);
        } catch (final NoSuchMethodException e) {
            // no-op
        }

        // we're just going to assume this is the valid name.  Other validation should already have been 
        // performed prior to this. 
        getterName = "is" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
        try {
            // check to see if we have the getter as an abstract class.  This might be an "is" method. 
            return beanClass.getMethod(getterName, new Class[0]);
        } catch (final NoSuchMethodException e) {
            // no-op
        }
        return null;
    }


    /**
     * Generate a concrete setter field for a CMP field.
     * At this point, we're just generating a simple
     * accessor for the field, given the type.  The
     * JPA engine when it makes this implementation class
     * a managed class define whatever additional logic
     * might be required.
     *
     * @param cmpField The CMP field backing this setter method.
     */
    private void createSetter(final CmpField cmpField) {
        final String methodName = setterName(cmpField.getName());
        final MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, methodName, "(" + cmpField.getDescriptor() + ")V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(cmpField.getType().getOpcode(ILOAD), 1);
        mv.visitFieldInsn(PUTFIELD, implClassName, cmpField.getName(), cmpField.getDescriptor());
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private String setterName(final String propertyName) {
        return "set" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
    }

    /**
     * Create a simple internal method for obtaining the
     * primary key.  There are 2 possibilities for handling
     * the primary key here:
     *
     * 1)  There is a defined primary key field.  The
     * contents of that field are returned.
     *
     * 2)  The primary key is provided by the container.
     * This is a long value stored in a private, generated
     * field.  This field is returned as a generated
     * wrappered Long.
     *
     * 3)  A primary key class has been provided.  An instance
     * of this class is instantiated, and code is generated
     * that will copy all of the CMP fields from the EJB
     * into the primary key instance.
     */
    private void createSimplePrimaryKeyGetter() {
        final MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "OpenEJB_getPrimaryKey", "()Ljava/lang/Object;", null, null);
        mv.visitCode();

        // the primary key is identifed as a field.  We just return that value directly. 
        if (pkField != null) {
            // push the pk field
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, implClassName, pkField.getName(), pkField.getDescriptor());

            // return the pk field (from the stack)
            mv.visitInsn(pkField.getType().getOpcode(IRETURN));
        } else if (Object.class.equals(primKeyClass)) {
            // this is a container-generated primary key.  It's a long value stored in 
            // a generated field.  We return that value, wrappered in a Long instance. 

            // push the pk field
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, implClassName, UNKNOWN_PK_NAME, UNKNOWN_PK_TYPE.getDescriptor());

            // return the pk field (from the stack)
            mv.visitInsn(UNKNOWN_PK_TYPE.getOpcode(IRETURN));
        } else {
            // We have a primary key class defined.  For every field that matches one of the 
            // defined CMP fields, we generate code to copy that value into the corresponding 
            // field of the primary key class. 
            final String pkImplName = primKeyClass.getName().replace('.', '/');

            // new Pk();
            mv.visitTypeInsn(NEW, pkImplName);
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, pkImplName, "<init>", "()V", false);
            mv.visitVarInsn(ASTORE, 1);
            mv.visitVarInsn(ALOAD, 1);

            // copy each field from the ejb to the pk class
            for (final Field field : primKeyClass.getFields()) {
                final CmpField cmpField = cmpFields.get(field.getName());

                // only process the cmp fields
                if (cmpField == null) {
                    continue;
                }

                // verify types match... this should have been caught by the verifier, but
                // check again since generated code is so hard to debug
                if (!cmpField.getType().getClassName().equals(field.getType().getName())) {
                    throw new IllegalArgumentException("Primary key " + cmpField.getName() + " is type " + cmpField.getType().getClassName() + " but CMP field is type " + field.getType().getName());
                }

                // push the value from the cmp bean
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, implClassName, cmpField.getName(), cmpField.getDescriptor());
                // set matching field in the pk class to the value on the stack
                mv.visitFieldInsn(PUTFIELD, pkImplName, cmpField.getName(), cmpField.getDescriptor());
                mv.visitVarInsn(ALOAD, 1);
            }

            // return the Pk();
            mv.visitInsn(ARETURN);
        }

        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    /**
     * Create the CMR fields defined for this object.  This
     * creates a pair of fields for each CMR field.  The
     * first field is the real field containing the object
     * data.  The second field will be an accessor object
     * that's instantiated when the fields are first
     * initialized.  The accessor field gets created with
     * the same name and "Cmr" concatenated to the end
     * of the field name.
     *
     * @param cmrField The CMR field descriptor.
     */
    private void createCmrFields(final CmrField cmrField) {
        FieldVisitor fv = cw.visitField(ACC_PRIVATE,
            cmrField.getName(),
            cmrField.getDescriptor(),
            cmrField.getGenericSignature(),
            null);
        fv.visitEnd();

        fv = cw.visitField(ACC_PRIVATE + ACC_TRANSIENT,
            cmrField.getName() + "Cmr",
            cmrField.getAccessorDescriptor(),
            cmrField.getAccessorGenericSignature(),
            null);
        fv.visitEnd();
    }

    /**
     * Initialize the CMR fields associated with a CMR
     * definition.  This initializes two fields per CMR
     * defined field:  1)  The CMR field itself (which might
     * be initialized to an instance of a defined type) and 2)
     * the appropriate CMD accessor that handles the
     * different types of relationship.
     *
     * @param mv       The method context we're initializing in.
     * @param cmrField The CMR field to process.
     */
    private void initCmrFields(final MethodVisitor mv, final CmrField cmrField) {
        // this.${cmrField.name} = new ${cmrField.initialValueType}();
        final Type initialValueType = cmrField.getInitialValueType();
        if (initialValueType != null) {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitTypeInsn(NEW, initialValueType.getInternalName());
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, initialValueType.getInternalName(), "<init>", "()V", false);
            mv.visitFieldInsn(PUTFIELD, implClassName, cmrField.getName(), cmrField.getDescriptor());
        }

        // this.${cmrField.name}Cmr = new ${cmrField.accessorType}<${cmrField.type}, ${cmrField.proxyType}>(this,
        //         ${cmrField.name},
        //         ${cmrField.type},
        //         ${cmrField.relatedName});
        mv.visitVarInsn(ALOAD, 0);
        mv.visitTypeInsn(NEW, cmrField.getAccessorInternalName());
        mv.visitInsn(DUP);

        // arg0: EntityBean source = this
        mv.visitVarInsn(ALOAD, 0);

        // arg1: String sourceProperty - "b"
        mv.visitLdcInsn(cmrField.getName());

        // arg2: Class<Bean> relatedType = BBean_BBean
        mv.visitLdcInsn(cmrField.getType());

        // arg3: String relatedProperty
        if (cmrField.getRelatedName() != null) {
            mv.visitLdcInsn(cmrField.getRelatedName());
        } else {
            mv.visitInsn(ACONST_NULL);
        }

        // invoke
        mv.visitMethodInsn(INVOKESPECIAL,
            cmrField.getAccessorInternalName(),
            "<init>",
            "(Ljavax/ejb/EntityBean;Ljava/lang/String;Ljava/lang/Class;Ljava/lang/String;)V", false);

        // bCmr = result
        mv.visitFieldInsn(PUTFIELD,
            implClassName,
            cmrField.getName() + "Cmr",
            cmrField.getAccessorDescriptor());
    }

    /**
     * Create a getter method for the CMR field.  This
     * will used the accessor object initialized into the
     * name + "Cmr" field that was already generated and
     * initialized at object creation.  The accessor
     * object manages the object relationship.
     *
     * @param cmrField The field we're generating.
     */
    private void createCmrGetter(final CmrField cmrField) {
        // a synthentic method essentially means this is a relationship with 
        // no back reference.  We don't generate a getter method for this 
        if (cmrField.isSynthetic()) {
            return;
        }

        final String methodName = getterName(cmrField.getName());
        final MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, methodName, "()" + cmrField.getProxyDescriptor(), null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, implClassName, cmrField.getName() + "Cmr", cmrField.getAccessorDescriptor());
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, implClassName, cmrField.getName(), cmrField.getDescriptor());

        // return this.${cmrField.name}Cmr.get(this.${cmdField.name});  
        // this takes the value stored in the CMR field (which might be a single value or 
        // a Set or Collection), and hands it to the appropriate accessor.
        mv.visitMethodInsn(INVOKEVIRTUAL, cmrField.getAccessorInternalName(), "get", cmrField.getCmrStyle().getGetterDescriptor(), false);
        // if the style is a single value, then we're going to need to cast this 
        // to the target class before returning.  
        if (cmrField.getCmrStyle() == CmrStyle.SINGLE) {
            mv.visitTypeInsn(CHECKCAST, cmrField.getProxyType().getInternalName());
        }
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }


    /**
     * Generate a setter method for a CMR field.  The
     * setter method will delegate the setting responsibility
     * to the accessor object store the associated Cmr field.
     *
     * @param cmrField The field we're generating the setter for.
     */
    private void createCmrSetter(final CmrField cmrField) {
        // a synthentic method essentially means this is a relationship with 
        // no back reference.  We don't generate a getter method for this 
        if (cmrField.isSynthetic()) {
            return;
        }

        final String methodName = setterName(cmrField.getName());
        final MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, methodName, "(" + cmrField.getProxyDescriptor() + ")V", null, null);
        mv.visitCode();
        // if this is a Many relationship, the CMR field contains a Set value.  The accessor 
        // will process the elements in the Set, removing any existing ones, and then populating 
        // the Set with the new values from the new value source. 
        if (cmrField.getCmrStyle() != CmrStyle.SINGLE) {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, implClassName, cmrField.getName() + "Cmr", cmrField.getAccessorDescriptor());
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, implClassName, cmrField.getName(), cmrField.getDescriptor());
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEVIRTUAL, cmrField.getAccessorInternalName(), "set", cmrField.getCmrStyle().getSetterDescriptor(), false);
            mv.visitInsn(RETURN);
        } else {
            // this is a single value.  We pass the existing value and the old value to 
            // the accessor, then must cast the accessor return value to the target type 
            // so we can store it in the real CMR field. 
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, implClassName, cmrField.getName() + "Cmr", cmrField.getAccessorDescriptor());
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, implClassName, cmrField.getName(), cmrField.getDescriptor());
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEVIRTUAL, cmrField.getAccessorInternalName(), "set", cmrField.getCmrStyle().getSetterDescriptor(), false);
            mv.visitTypeInsn(CHECKCAST, cmrField.getType().getInternalName());
            mv.visitFieldInsn(PUTFIELD, implClassName, cmrField.getName(), cmrField.getDescriptor());
            mv.visitInsn(RETURN);
        }
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    /**
     * Generate the OpenEJB_deleted() logic for a
     * CMR field.  This handles the cascade from the referencing
     * object to the CMR fields for the object.  This method generates the
     * CMR logic inline inside the object OpenEJB_deleted() method.
     *
     * @param mv       The method context we're operating within.
     * @param cmrField The CMD field containing the deleted value.
     */
    private void createOpenEJB_deleted(final MethodVisitor mv, final CmrField cmrField) {
        // this.${cmrField.name}Cmr.deleted(this.${cmrField.name}); 
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, implClassName, cmrField.getName() + "Cmr", cmrField.getAccessorDescriptor());
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, implClassName, cmrField.getName(), cmrField.getDescriptor());
        mv.visitMethodInsn(INVOKEVIRTUAL, cmrField.getAccessorInternalName(), "deleted", cmrField.getCmrStyle().getDeletedDescriptor(), false);
    }

    /**
     * Generate the OpenEJB_addCmr logic for an individual
     * CMR field.  Each CMR field has a test against the
     * property name, which is passed to the wrappering
     * addCmr method.  This results in a series of
     * if blocks for each defined CMD property.
     *
     * @param mv       The method we're generating within.
     * @param cmrField The CMR field definition.
     */
    private void createOpenEJB_addCmr(final MethodVisitor mv, final CmrField cmrField) {
        // if (${cmrField.name}.equals(arg1))
        mv.visitLdcInsn(cmrField.getName());
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
        // if not equal jump to end
        final Label end = new Label();
        mv.visitJumpInsn(IFEQ, end);

        // collection style relationship.  Generate the code to add this object to the 
        // collection already anchored in the CMR field.  
        if (cmrField.getCmrStyle() != CmrStyle.SINGLE) {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, implClassName, cmrField.getName(), cmrField.getDescriptor());
            mv.visitVarInsn(ASTORE, 3);
            mv.visitVarInsn(ALOAD, 3);
            final Label fieldNotNull = new Label();
            mv.visitJumpInsn(IFNONNULL, fieldNotNull);
            // lazy creation of the collection type if not already created. 
            mv.visitTypeInsn(NEW, cmrField.getInitialValueType().getInternalName());
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, cmrField.getInitialValueType().getInternalName(), "<init>", "()V", false);
            mv.visitVarInsn(ASTORE, 3);
            mv.visitLabel(fieldNotNull);

            // ${cmrField.name}.add(arg2)
            mv.visitVarInsn(ALOAD, 3);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKEINTERFACE,
                cmrField.getCmrStyle().getCollectionType().getInternalName(),
                "add",
                "(Ljava/lang/Object;)Z", true);
            mv.visitInsn(POP);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 3);
            // unconditionally set the CMR field to the collection.  This is either the 
            // original one on entry, or a new one for first access. 
            mv.visitFieldInsn(PUTFIELD, implClassName, cmrField.getName(), cmrField.getDescriptor());

            // return null;
            mv.visitInsn(ACONST_NULL);
            mv.visitInsn(ARETURN);
        } else {
            // push: this.${cmrField.name};
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, implClassName, cmrField.getName(), cmrField.getDescriptor());

            // this.${cmrField.name} = (${cmrField.type}) bean;
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitTypeInsn(CHECKCAST, cmrField.getType().getInternalName());
            mv.visitFieldInsn(PUTFIELD, implClassName, cmrField.getName(), cmrField.getDescriptor());

            // return pushed value above
            mv.visitInsn(ARETURN);
        }

        // end of if statement
        mv.visitLabel(end);
    }

//    private void createPrintln(MethodVisitor mv, String message) {
//        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
//        mv.visitLdcInsn(message);
//        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
//    }

//    private void createPrintField(MethodVisitor mv, String fieldName, String descriptor) {
//        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
//        mv.visitLdcInsn(fieldName + "=");
//        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "print", "(Ljava/lang/String;)V");
//
//        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
//        mv.visitVarInsn(ALOAD, 0);
//        mv.visitFieldInsn(GETFIELD, implClassName, fieldName, descriptor);
//        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V");
//    }


    /**
     * Emit the remove logic for an individual CMR field.
     * Like the addCmr logic, each field is guarded by an
     * if test on the property name.
     *
     * @param mv
     * @param cmrField
     */
    private void createOpenEJB_removeCmr(final MethodVisitor mv, final CmrField cmrField) {
        // if (${cmrField.name}.equals(arg1))
        mv.visitLdcInsn(cmrField.getName());
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
        // if not equal jump to end
        final Label end = new Label();
        mv.visitJumpInsn(IFEQ, end);

        // collection valued CMR field.  Remove the object from the collection. 
        if (cmrField.getCmrStyle() != CmrStyle.SINGLE) {
            // ${cmrField.name}.remove(arg2)
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, implClassName, cmrField.getName(), cmrField.getDescriptor());
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKEINTERFACE,
                cmrField.getCmrStyle().getCollectionType().getInternalName(),
                "remove",
                "(Ljava/lang/Object;)Z", true);
            mv.visitInsn(POP);

            // return;
            mv.visitInsn(RETURN);
        } else {
            // single valued, so just null out the field. 

            // this.${cmrField.name} = null;
            mv.visitVarInsn(ALOAD, 0);
            mv.visitInsn(ACONST_NULL);
            mv.visitFieldInsn(PUTFIELD, implClassName, cmrField.getName(), cmrField.getDescriptor());

            // return;
            mv.visitInsn(RETURN);
        }

        // end of if statement
        mv.visitLabel(end);
    }

    /**
     * Generate a concrete implementation of an abstract
     * ejbSelectxxxx method.
     *
     * @param selectMethod The abstract definition for the method we're generating.
     */
    private void createSelectMethod(final Method selectMethod) {
        final Class<?> returnType = selectMethod.getReturnType();

        final Method executeMethod = EjbSelect.getSelectMethod(returnType);

        final MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, selectMethod.getName(), Type.getMethodDescriptor(selectMethod), null, getExceptionTypes(selectMethod));
        mv.visitCode();

        // push deploymentInfo
        mv.visitFieldInsn(GETSTATIC, implClassName, "deploymentInfo", "Ljava/lang/Object;");

        // push method signature
        mv.visitLdcInsn(getSelectMethodSignature(selectMethod));

        // push return type, but only if the executeMethod is not going to be for void or 
        // one of the primitives. 
        if (!returnType.isPrimitive()) {
            mv.visitLdcInsn(returnType.getName());
        }

        // new Object[]
        mv.visitIntInsn(BIPUSH, selectMethod.getParameterTypes().length);
        mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");

        // object[i] = arg${i}
        int i = 0;
        for (final Class<?> parameterType : selectMethod.getParameterTypes()) {
            // push arguement i on stack
            mv.visitInsn(DUP);
            bipush(mv, i);
            mv.visitVarInsn(Type.getType(parameterType).getOpcode(ILOAD), i + 1);

            // convert argument on stack to an Object
            Convert.toObjectFrom(mv, parameterType);

            // store it into the array
            mv.visitInsn(AASTORE);

            if (long.class.equals(parameterType) || double.class.equals(parameterType)) {
                // longs and doubles are double wide
                i = i + 2;
            } else {
                i++;
            }
        }

        // EjbSelect.execute_xxxx(deploymentInfo, signature, [returnType.] args[]);
        mv.visitMethodInsn(INVOKESTATIC,
            Type.getInternalName(executeMethod.getDeclaringClass()),
            executeMethod.getName(),
            Type.getMethodDescriptor(executeMethod), false);

        // if this is a void type, we just return.  Otherwise, the return type 
        // needs to match the type returned from the method call 
        if (!Void.TYPE.equals(returnType)) {
            // if this is a non-primitive return type, then the returned 
            // object will need to be cast to the appropriate return type for 
            // the verifier.  The primitive types all have the proper type on the 
            // stack already 
            if (!returnType.isPrimitive()) {
                // convert return type
                Convert.fromObjectTo(mv, returnType);
            }

            // And generate the appropriate return for the type 
            mv.visitInsn(Type.getReturnType(selectMethod).getOpcode(IRETURN));
        } else {
            // void return is just a RETURN. 
            mv.visitInsn(RETURN);
        }

        // close method
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private String getSelectMethodSignature(final Method selectMethod) {
        final StringBuilder signature = new StringBuilder();
        signature.append(selectMethod.getName());
        if (selectMethod.getParameterTypes().length > 0) {
            signature.append('(');
            boolean first = true;
            for (final Class<?> parameterType : selectMethod.getParameterTypes()) {
                if (!first) {
                    signature.append(',');
                }
                signature.append(parameterType.getCanonicalName());
                first = false;
            }
            signature.append(')');
        }
        return signature.toString();
    }

    private static String[] getExceptionTypes(final Method method) {
        final List<String> types = new ArrayList<>(method.getExceptionTypes().length);
        for (final Class<?> exceptionType : method.getExceptionTypes()) {
            types.add(Type.getInternalName(exceptionType));
        }
        return types.toArray(new String[types.size()]);
    }

    private static void bipush(final MethodVisitor mv, final int i) {
        switch (i) {
            case -1:
                mv.visitInsn(ICONST_M1);
                break;
            case 0:
                mv.visitInsn(ICONST_0);
                break;
            case 1:
                mv.visitInsn(ICONST_1);
                break;
            case 2:
                mv.visitInsn(ICONST_2);
                break;
            case 3:
                mv.visitInsn(ICONST_3);
                break;
            case 4:
                mv.visitInsn(ICONST_4);
                break;
            case 5:
                mv.visitInsn(ICONST_5);
                break;
            default:
                mv.visitIntInsn(BIPUSH, i);
        }

    }


    /**
     * Helper class to handle common type conversions
     * in generated code.
     */
    private static final class Convert {
        /**
         * Generate code to performing boxing of primitive types
         * into a wrapper class instance.
         *
         * @param mv   The method currently being emitted.
         * @param from The class we're converting from.
         */
        public static void toObjectFrom(final MethodVisitor mv, final Class from) {
            // we only handler boxing for the primitive types. 
            if (from.isPrimitive()) {
                final Convert conversion = getConversion(from);
                // the only conversion that will be trouble here is void.  
                if (conversion == null) {
                    throw new NullPointerException("conversion is null " + from.getName() + " " + from.isPrimitive());
                }
                conversion.primitiveToObject(mv);
            }
        }

        /**
         * Handle a conversion from one object type to another.  If
         * There are 3 possible conversions:
         *
         * 1)  The to class is Object.  This can be handled
         * without conversion.  This option is a NOP.
         * 2)  The to class is a reference type (non-primitive).  This conversion
         * is a cast operation (which might fail at run time).
         * 3)  The to class is a primitive type.  This is
         * an unboxing operation.
         *
         * @param mv The method currently being constructed.
         * @param to The target class for the conversion.
         */
        public static void fromObjectTo(final MethodVisitor mv, final Class to) {
            if (to.equals(Object.class)) { //NOPMD
                // direct assignment will work
            } else if (!to.isPrimitive()) {
                mv.visitTypeInsn(CHECKCAST, Type.getInternalName(to));
            } else {
                final Convert conversion = getConversion(to);
                {
                    if (conversion == null) {
                        throw new NullPointerException("unsupported conversion for EJB select return type " + to.getName());
                    }
                }
                conversion.objectToPrimitive(mv);
            }
        }

        private static final Map<Class, Convert> conversionsByPrimitive = new HashMap<Class, Convert>();

        public static Convert getConversion(final Class primitive) {
            if (!primitive.isPrimitive()) {
                throw new IllegalArgumentException(primitive.getName() + " is not a primitive class");
            }
            return conversionsByPrimitive.get(primitive);
        }

        public static final Convert BOOLEAN = new Convert(boolean.class, Boolean.class, "booleanValue");
        public static final Convert CHAR = new Convert(char.class, Character.class, "charValue");
        public static final Convert BYTE = new Convert(byte.class, Byte.class, "byteValue");
        public static final Convert SHORT = new Convert(short.class, Short.class, "shortValue");
        public static final Convert INT = new Convert(int.class, Integer.class, "intValue");
        public static final Convert LONG = new Convert(long.class, Long.class, "longValue");
        public static final Convert FLOAT = new Convert(float.class, Float.class, "floatValue");
        public static final Convert DOUBLE = new Convert(double.class, Double.class, "doubleValue");

        private final Type objectType;
        private final Method toPrimitive;
        private final Method toObject;

        private Convert(final Class primitiveClass, final Class objectClass, final String toPrimitiveMethodName) {
            objectType = Type.getType(objectClass);

            try {
                toObject = objectClass.getMethod("valueOf", primitiveClass);
                toPrimitive = objectClass.getMethod(toPrimitiveMethodName);
            } catch (final NoSuchMethodException e) {
                throw new OpenEJBRuntimeException(e);
            }

            conversionsByPrimitive.put(primitiveClass, this);
        }

        public void primitiveToObject(final MethodVisitor mv) {
            mv.visitMethodInsn(INVOKESTATIC, objectType.getInternalName(), toObject.getName(), Type.getMethodDescriptor(toObject), false);
        }

        public void objectToPrimitive(final MethodVisitor mv) {
            mv.visitTypeInsn(CHECKCAST, objectType.getInternalName());
            mv.visitMethodInsn(INVOKEVIRTUAL, objectType.getInternalName(), toPrimitive.getName(), Type.getMethodDescriptor(toPrimitive), false);
        }

    }

    public void createEjbActivate() {
        final MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "ejbActivate", "()V", null, null);
        mv.visitCode();
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 1);
        mv.visitEnd();
    }

    public void createEjbLoad() {
        final MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "ejbLoad", "()V", null, null);
        mv.visitCode();
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 1);
        mv.visitEnd();
    }

    public void createEjbPassivate() {
        final MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "ejbPassivate", "()V", null, null);
        mv.visitCode();
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 1);
        mv.visitEnd();
    }

    public void createEjbRemove() {
        final MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "ejbRemove", "()V", null, null);
        mv.visitCode();
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 1);
        mv.visitEnd();
    }

    public void createEjbStore() {
        final MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "ejbStore", "()V", null, null);
        mv.visitCode();
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 1);
        mv.visitEnd();
    }

    public void createSetEntityContext() {
        final MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "setEntityContext", "(Ljavax/ejb/EntityContext;)V", null, null);
        mv.visitCode();
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 2);
        mv.visitEnd();
    }

    public void createUnsetEntityContext() {
        final MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "unsetEntityContext", "()V", null, null);
        mv.visitCode();
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 1);
        mv.visitEnd();
    }
}

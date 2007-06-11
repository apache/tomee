/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.core.cmp.cmp2;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collection;

public class Cmp2Generator implements Opcodes {
    private static final String UNKNOWN_PK_NAME = "OpenEJB_pk";
    private static final Type UNKNOWN_PK_TYPE = Type.getType(Long.class);
    private static final Method EJB_SELECT_EXECUTE;
    static {
        try {
            EJB_SELECT_EXECUTE = EjbSelect.class.getMethod("execute", Object.class, String.class, String.class, Object[].class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private final String implClassName;
    private final String beanClassName;
    private final ClassWriter cw;
    private final Map<String, CmpField> cmpFields = new LinkedHashMap<String, CmpField>();
    private final Collection<CmrField> cmrFields = new ArrayList<CmrField>();
    private final CmpField pkField;
    private final Class primKeyClass;
    private final List<Method> selectMethods = new ArrayList<Method>();

    public Cmp2Generator(String cmpImplClass, Class beanClass, String pkField, Class<?> primKeyClass, String[] cmrFields) {
        if (pkField == null && primKeyClass == null) throw new NullPointerException("Both pkField and primKeyClass are null");
        beanClassName = Type.getInternalName(beanClass);
        implClassName = cmpImplClass.replace('.', '/');
        this.primKeyClass = primKeyClass;

        for (String cmpFieldName : cmrFields) {
            String getterName = getterName(cmpFieldName);
            try {
                Method getter = beanClass.getMethod(getterName);
                Type type = Type.getType(getter.getReturnType());
                CmpField cmpField = new CmpField(cmpFieldName, type);
                cmpFields.put(cmpFieldName, cmpField);
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("No such property " + cmpFieldName + " defined on bean class " + beanClassName, e);
            }
        }

        if (pkField != null) {
            this.pkField = cmpFields.get(pkField);
            if (this.pkField == null) {
                throw new IllegalArgumentException("No such property " + pkField + " defined on bean class " + beanClassName);
            }
        } else {
            this.pkField = null;
        }

        for (Method method : beanClass.getMethods()) {
            if (Modifier.isAbstract(method.getModifiers()) && method.getName().startsWith("ejbSelect")) {
                addSelectMethod(method);
            }
        }

        cw = new ClassWriter(true);
    }

    public void addCmrField(CmrField cmrField) {
        cmrFields.add(cmrField);
    }

    public void addSelectMethod(Method selectMethod) {
        selectMethods.add(selectMethod);
    }

    public byte[] generate() {
        cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, implClassName, null, beanClassName, new String[]{"org/apache/openejb/core/cmp/cmp2/Cmp2Entity"});

        // public static Object deploymentInfo;
        {
            FieldVisitor fv = cw.visitField(ACC_PUBLIC + ACC_STATIC, "deploymentInfo", "Ljava/lang/Object;", null, null);
            fv.visitEnd();
        }

        // private transient boolean deleted;
        {
            FieldVisitor fv = cw.visitField(ACC_PRIVATE + ACC_TRANSIENT, "deleted", "Z", null, null);
            fv.visitEnd();
        }

        if (Object.class.equals(primKeyClass)) {
            FieldVisitor fv = cw.visitField(ACC_PRIVATE, UNKNOWN_PK_NAME, UNKNOWN_PK_TYPE.getDescriptor(), null, null);
            fv.visitEnd();
        }

        // private ${cmpField.type} ${cmpField.name};
        for (CmpField cmpField : cmpFields.values()) {
            createField(cmpField);
        }

        for (CmrField cmrField : cmrFields) {
            createCmrFields(cmrField);
        }

        createConstructor();

        for (CmpField cmpField : cmpFields.values()) {
            // public ${cmpField.type} get${cmpField.name}() {
            //     return this.${cmpField.name};
            // }
            createGetter(cmpField);

            // public void setId(${cmpField.type} ${cmpField.name}) {
            //    this.${cmpField.name} = ${cmpField.name};
            // }
            createSetter(cmpField);
        }

        for (CmrField cmrField : cmrFields) {
            createCmrGetter(cmrField);
            createCmrSetter(cmrField);
        }

        createSimplePrimaryKeyGetter();

        createOpenEJB_isDeleted();

        createOpenEJB_deleted();

        createOpenEJB_addCmr();

        createOpenEJB_removeCmr();

        for (Method selectMethod : selectMethods) {
            createSelectMethod(selectMethod);
        }

        cw.visitEnd();

        return cw.toByteArray();
    }

    private void createConstructor() {
        MethodVisitor mv = mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, beanClassName, "<init>", "()V");

        for (CmrField cmrField : cmrFields) {
            initCmrFields(mv, cmrField);
        }

        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void createOpenEJB_isDeleted() {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "OpenEJB_isDeleted", "()Z", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, implClassName, "deleted", "Z");
        mv.visitInsn(IRETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void createOpenEJB_deleted() {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "OpenEJB_deleted", "()V", null, null);
        mv.visitCode();

        // if (deleted) return;
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, implClassName, "deleted", "Z");
        Label notDeleted = new Label();
        mv.visitJumpInsn(IFEQ, notDeleted);
        mv.visitInsn(RETURN);
        mv.visitLabel(notDeleted);

        // deleted = true;
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ICONST_1);
        mv.visitFieldInsn(PUTFIELD, implClassName, "deleted", "Z");

        for (CmrField cmrField : cmrFields) {
            // ${cmrField.accessor}.delete(${cmrField.name});
            createOpenEJB_deleted(mv, cmrField);
        }

        // return;
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void createOpenEJB_addCmr() {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "OpenEJB_addCmr", "(Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/Object;", null, null);
        mv.visitCode();

        // if (deleted) return null;
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, implClassName, "deleted", "Z");
        Label notDeleted = new Label();
        mv.visitJumpInsn(IFEQ, notDeleted);
        mv.visitInsn(ACONST_NULL);
        mv.visitInsn(ARETURN);
        mv.visitLabel(notDeleted);

        for (CmrField cmrField : cmrFields) {
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
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V");
        mv.visitLdcInsn("Unknown cmr field ");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
        mv.visitLdcInsn(" on entity bean of type ");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getName", "()Ljava/lang/String;");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;");
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/IllegalArgumentException", "<init>", "(Ljava/lang/String;)V");
        mv.visitInsn(ATHROW);

        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void createOpenEJB_removeCmr() {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "OpenEJB_removeCmr", "(Ljava/lang/String;Ljava/lang/Object;)V", null, null);
        mv.visitCode();

        // if (deleted) return;
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, implClassName, "deleted", "Z");
        Label notDeleted = new Label();
        mv.visitJumpInsn(IFEQ, notDeleted);
        mv.visitInsn(RETURN);
        mv.visitLabel(notDeleted);

        for (CmrField cmrField : cmrFields) {
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
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V");
        mv.visitLdcInsn("Unknown cmr field ");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
        mv.visitLdcInsn(" on entity bean of type ");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getName", "()Ljava/lang/String;");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;");
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/IllegalArgumentException", "<init>", "(Ljava/lang/String;)V");
        mv.visitInsn(ATHROW);

        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void createField(CmpField cmpField) {
        FieldVisitor fv = cw.visitField(ACC_PRIVATE,
                cmpField.getName(),
                cmpField.getDescriptor(),
                null,
                null);
        fv.visitEnd();
    }

    private void createGetter(CmpField cmpField) {
        String methodName = getterName(cmpField.getName());
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, methodName, "()" + cmpField.getDescriptor(), null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, implClassName, cmpField.getName(), cmpField.getDescriptor());
        mv.visitInsn(cmpField.getType().getOpcode(IRETURN));
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private static String getterName(String propertyName) {
        return "get" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
    }

    private void createSetter(CmpField cmpField) {
        String methodName = setterName(cmpField.getName());
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, methodName, "(" + cmpField.getDescriptor() + ")V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(cmpField.getType().getOpcode(ILOAD), 1);
        mv.visitFieldInsn(PUTFIELD, implClassName, cmpField.getName(), cmpField.getDescriptor());
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private String setterName(String propertyName) {
        return "set" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
    }

    private void createSimplePrimaryKeyGetter() {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "OpenEJB_getPrimaryKey", "()Ljava/lang/Object;", null, null);
        mv.visitCode();
        if (pkField != null) {
            // push the pk field
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, implClassName, pkField.getName(), pkField.getDescriptor());

            // return the pk field (from the stack)
            mv.visitInsn(pkField.getType().getOpcode(IRETURN));
        } else if (Object.class.equals(primKeyClass)) {
            // push the pk field
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, implClassName, UNKNOWN_PK_NAME, UNKNOWN_PK_TYPE.getDescriptor());

            // return the pk field (from the stack)
            mv.visitInsn(UNKNOWN_PK_TYPE.getOpcode(IRETURN));
        } else {
            String pkImplName = primKeyClass.getName().replace('.', '/');

            // new Pk();
            mv.visitTypeInsn(NEW, pkImplName);
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, pkImplName, "<init>", "()V");
            mv.visitVarInsn(ASTORE, 1);
            mv.visitVarInsn(ALOAD, 1);

            // copy each field from the ejb to the pk class
            for (Field field : primKeyClass.getFields()) {
                CmpField cmpField = cmpFields.get(field.getName());

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

    private void createCmrFields(CmrField cmrField) {
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

    private void initCmrFields(MethodVisitor mv, CmrField cmrField) {
        // this.${cmrField.name} = new ${cmrField.initialValueType}();
        Type initialValueType = cmrField.getInitialValueType();
        if (initialValueType != null) {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitTypeInsn(NEW, initialValueType.getInternalName());
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, initialValueType.getInternalName(), "<init>", "()V");
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
                "(Ljavax/ejb/EntityBean;Ljava/lang/String;Ljava/lang/Class;Ljava/lang/String;)V");

        // bCmr = result
        mv.visitFieldInsn(PUTFIELD,
                implClassName,
                cmrField.getName() + "Cmr",
                cmrField.getAccessorDescriptor());
    }

    private void createCmrGetter(CmrField cmrField) {
        if (cmrField.isSynthetic()) return;

        String methodName = getterName(cmrField.getName());
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, methodName, "()" + cmrField.getProxyDescriptor(), null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, implClassName, cmrField.getName() + "Cmr", cmrField.getAccessorDescriptor());
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, implClassName, cmrField.getName(), cmrField.getDescriptor());
        mv.visitMethodInsn(INVOKEVIRTUAL, cmrField.getAccessorInternalName(), "get", cmrField.getCmrStyle().getGetterDescriptor());
        if (cmrField.getCmrStyle() == CmrStyle.SINGLE) {
            mv.visitTypeInsn(CHECKCAST, cmrField.getProxyType().getInternalName());
        }
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void createCmrSetter(CmrField cmrField) {
        if (cmrField.isSynthetic()) return;

        String methodName = setterName(cmrField.getName());
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, methodName, "(" + cmrField.getProxyDescriptor() + ")V", null, null);
        mv.visitCode();
        if (cmrField.getCmrStyle() != CmrStyle.SINGLE) {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, implClassName, cmrField.getName() + "Cmr", cmrField.getAccessorDescriptor());
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, implClassName, cmrField.getName(), cmrField.getDescriptor());
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEVIRTUAL, cmrField.getAccessorInternalName(), "set", cmrField.getCmrStyle().getSetterDescriptor());
            mv.visitInsn(RETURN);
        } else {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, implClassName, cmrField.getName() + "Cmr", cmrField.getAccessorDescriptor());
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, implClassName, cmrField.getName(), cmrField.getDescriptor());
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEVIRTUAL, cmrField.getAccessorInternalName(), "set", cmrField.getCmrStyle().getSetterDescriptor());
            mv.visitTypeInsn(CHECKCAST, cmrField.getType().getInternalName());
            mv.visitFieldInsn(PUTFIELD, implClassName, cmrField.getName(), cmrField.getDescriptor());
            mv.visitInsn(RETURN);
        }
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void createOpenEJB_deleted(MethodVisitor mv, CmrField cmrField) {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, implClassName, cmrField.getName() + "Cmr", cmrField.getAccessorDescriptor());
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, implClassName, cmrField.getName(), cmrField.getDescriptor());
        mv.visitMethodInsn(INVOKEVIRTUAL, cmrField.getAccessorInternalName(), "deleted", cmrField.getCmrStyle().getDeletedDescriptor());
    }

    private void createOpenEJB_addCmr(MethodVisitor mv, CmrField cmrField) {
        // if (${cmrField.name}.equals(arg1))
        mv.visitLdcInsn(cmrField.getName());
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z");
        // if not equal jump to end
        Label end = new Label();
        mv.visitJumpInsn(IFEQ, end);

        if (cmrField.getCmrStyle() != CmrStyle.SINGLE) {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, implClassName, cmrField.getName(), cmrField.getDescriptor());
            mv.visitVarInsn(ASTORE, 3);
            mv.visitVarInsn(ALOAD, 3);
            Label fieldNotNull = new Label();
            mv.visitJumpInsn(IFNONNULL, fieldNotNull);
            mv.visitTypeInsn(NEW, cmrField.getInitialValueType().getInternalName());
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, cmrField.getInitialValueType().getInternalName(), "<init>", "()V");
            mv.visitVarInsn(ASTORE, 3);
            mv.visitLabel(fieldNotNull);

            // ${cmrField.name}.add(arg2)
            mv.visitVarInsn(ALOAD, 3);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKEINTERFACE,
                    cmrField.getCmrStyle().getCollectionType().getInternalName(),
                    "add",
                    "(Ljava/lang/Object;)Z");
            mv.visitInsn(POP);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 3);
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

    private void createOpenEJB_removeCmr(MethodVisitor mv, CmrField cmrField) {
        // if (${cmrField.name}.equals(arg1))
        mv.visitLdcInsn(cmrField.getName());
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z");
        // if not equal jump to end
        Label end = new Label();
        mv.visitJumpInsn(IFEQ, end);

        if (cmrField.getCmrStyle() != CmrStyle.SINGLE) {
            // ${cmrField.name}.remove(arg2)
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, implClassName, cmrField.getName(), cmrField.getDescriptor());
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKEINTERFACE,
                    cmrField.getCmrStyle().getCollectionType().getInternalName(),
                    "remove",
                    "(Ljava/lang/Object;)Z");
            mv.visitInsn(POP);

            // return;
            mv.visitInsn(RETURN);
        } else {
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

    private void createSelectMethod(Method selectMethod) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, selectMethod.getName(), Type.getMethodDescriptor(selectMethod), null, getExceptionTypes(selectMethod));
        mv.visitCode();

        // push deploymentInfo
        mv.visitFieldInsn(GETSTATIC, implClassName, "deploymentInfo", "Ljava/lang/Object;");

        // push method signature
        mv.visitLdcInsn(getSelectMethodSignature(selectMethod));

        // push return type
        mv.visitLdcInsn(selectMethod.getReturnType().getName());

        // new Object[]
        mv.visitIntInsn(BIPUSH, selectMethod.getParameterTypes().length);
        mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");

        // object[i] = arg${i}
        int i = 0;
        for (Class<?> parameterType : selectMethod.getParameterTypes()) {
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

        // EjbSelect.execute(deploymentInfo, signature, args[]);
        mv.visitMethodInsn(INVOKESTATIC,
                Type.getInternalName(EJB_SELECT_EXECUTE.getDeclaringClass()),
                EJB_SELECT_EXECUTE.getName(),
                Type.getMethodDescriptor(EJB_SELECT_EXECUTE));

        // convert return type
        Convert.fromObjectTo(mv, selectMethod.getReturnType());

        // return
        mv.visitInsn(Type.getReturnType(selectMethod).getOpcode(IRETURN));

        // close method
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private String getSelectMethodSignature(Method selectMethod) {
        StringBuilder signature = new StringBuilder();
        signature.append(selectMethod.getName());
        if (selectMethod.getParameterTypes().length > 0) {
            signature.append('(');
            boolean first = true;
            for (Class<?> parameterType : selectMethod.getParameterTypes()) {
                if (!first) signature.append(',');
                signature.append(parameterType.getCanonicalName());
                first = false;
            }
            signature.append(')');
        }
        return signature.toString();
    }

    private static String[] getExceptionTypes(Method method) {
        List<String> types = new ArrayList<String>(method.getExceptionTypes().length);
        for (Class<?> exceptionType : method.getExceptionTypes()) {
            types.add(Type.getInternalName(exceptionType));
        }
        return types.toArray(new String[types.size()]);
    }

    private static void bipush(MethodVisitor mv, int i) {
        switch(i) {
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
    private static class Convert {
        public static void toObjectFrom(MethodVisitor mv, Class from) {
            if (from.isPrimitive()) {
                Convert conversion = getConversion(from);
                if (conversion == null) throw new NullPointerException("conversion is null " + from.getName() + " " + from.isPrimitive());
                conversion.primitiveToObject(mv);
            }
        }

        public static void fromObjectTo(MethodVisitor mv, Class to) {
            if (to.equals(Object.class)) {
                // direct assignment will work
            } else if (!to.isPrimitive()) {
                mv.visitTypeInsn(CHECKCAST, Type.getInternalName(to));
            } else {
                Convert conversion = getConversion(to);
                conversion.objectToPrimitive(mv);
            }
        }

        private static Map<Class, Convert> conversionsByPrimitive = new HashMap<Class, Convert>();

        public static Convert getConversion(Class primitive) {
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

        private Type objectType;
        private final Method toPrimitive;
        private final Method toObject;

        private Convert(Class primitiveClass, Class objectClass, String toPrimitiveMethodName) {
            objectType = Type.getType(objectClass);

            try {
                toObject = objectClass.getMethod("valueOf", primitiveClass);
                toPrimitive = objectClass.getMethod(toPrimitiveMethodName);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }

            conversionsByPrimitive.put(primitiveClass, this);
        }

        public void primitiveToObject(MethodVisitor mv) {
            mv.visitMethodInsn(INVOKESTATIC, objectType.getInternalName(), toObject.getName(), Type.getMethodDescriptor(toObject));
        }

        public void objectToPrimitive(MethodVisitor mv) {
            mv.visitTypeInsn(CHECKCAST, objectType.getInternalName());
            mv.visitMethodInsn(INVOKEVIRTUAL, objectType.getInternalName(), toPrimitive.getName(), Type.getMethodDescriptor(toPrimitive));
        }

    }
}

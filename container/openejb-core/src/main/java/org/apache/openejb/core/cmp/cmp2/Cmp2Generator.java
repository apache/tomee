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

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class Cmp2Generator implements Opcodes {
    private String implClassName;
    private String beanClassName;
    private ClassWriter cw;
    private final Map<String, CmpField> cmpFields = new LinkedHashMap<String, CmpField>();
    private final Map<String, CmrField> cmrFields = new LinkedHashMap<String, CmrField>();
    private CmpField pkField;

    public Cmp2Generator(Class beanClass, String pkField, String[] cmrFields) {
        beanClassName = Type.getInternalName(beanClass);
        implClassName = beanClassName + "_JPA";

        for (String cmpFieldName : cmrFields) {
            String getterName = getterName(cmpFieldName);
            try {
                Method getter = beanClass.getMethod(getterName);
                Type type = Type.getType(getter.getReturnType());
                CmpField cmpField = new CmpField(cmpFieldName, type);
                cmpFields.put(cmpFieldName, cmpField);
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("No such property " + cmpFieldName + " defined on bean class " + beanClassName);
            }
        }

        this.pkField = cmpFields.get(pkField);
        // todo warn about unsupported complex primary key
        if (pkField != null && this.pkField == null) {
            throw new IllegalArgumentException("No such property " + pkField + " defined on bean class " + beanClassName);
        }
        cw = new ClassWriter(true);
    }

    public void addCmrField(CmrField cmrField) {
        cmrFields.put(cmrField.getName(), cmrField);
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

        // private ${cmpField.type} ${cmpField.name};
        for (CmpField cmpField : cmpFields.values()) {
            createField(cmpField);
        }

        for (CmrField cmrField : cmrFields.values()) {
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

        for (CmrField cmrField : cmrFields.values()) {
            createCmrGetter(cmrField);
            createCmrSetter(cmrField);
        }

        createSimplePrimaryKeyGetter(pkField);

        createOpenEJB_deleted();

        createOpenEJB_addCmr();

        createOpenEJB_removeCmr();

        cw.visitEnd();

        return cw.toByteArray();
    }

    private void createConstructor() {
        MethodVisitor mv = mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, beanClassName, "<init>", "()V");

        for (CmrField cmrField : cmrFields.values()) {
            initCmrFields(mv, cmrField);
        }

        mv.visitInsn(RETURN);
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

        for (CmrField cmrField : cmrFields.values()) {
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

        for (CmrField cmrField : cmrFields.values()) {
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

        for (CmrField cmrField : cmrFields.values()) {
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

    private void createSimplePrimaryKeyGetter(CmpField pkField) {
        // todo complex pk
        if (pkField == null) return;

        String descriptor = pkField.getType().getDescriptor();

        String methodName = "OpenEJB_getPrimaryKey";
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, methodName, "()Ljava/lang/Object;", null, null);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, implClassName, pkField.getName(), descriptor);
        mv.visitInsn(pkField.getType().getOpcode(IRETURN));
        mv.visitMaxs(0, 0);
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

        // arg2: Class<Bean> relatedType = BBean_JPA
        mv.visitLdcInsn(cmrField.getType());

        // arg3: String relatedProperty
        mv.visitLdcInsn(cmrField.getRelatedName());

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
            // ${cmrField.name}.add(arg2)
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, implClassName, cmrField.getName(), cmrField.getDescriptor());
            mv.visitVarInsn(ALOAD, 2);
            mv.visitTypeInsn(CHECKCAST, cmrField.getType().getInternalName());
            mv.visitMethodInsn(INVOKEINTERFACE,
                    cmrField.getCmrStyle().getCollectionType().getInternalName(),
                    "add",
                    "(Ljava/lang/Object;)Z");
            mv.visitInsn(POP);

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
}

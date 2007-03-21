/**
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
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.FieldVisitor;

public class Cmp1Generator implements Opcodes {
    private String implClassName;
    private String beanClassName;
    private ClassWriter cw;
    private boolean unknownPk;

    public Cmp1Generator(String cmpImplClass, Class beanClass) {
        beanClassName = Type.getInternalName(beanClass);
        implClassName = cmpImplClass.replace('.', '/');

        cw = new ClassWriter(true);
    }

    public byte[] generate() {
        cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, implClassName, null, beanClassName, new String[0]);

        // if we have an unknown pk, we need to add a field for the pk
        if (unknownPk) {
            // public Long OpenEJB_pk;
            FieldVisitor fv = cw.visitField(ACC_PUBLIC, "OpenEJB_pk", "Ljava/lang/Long;", null, null);
            fv.visitEnd();
        }

        createConstructor();

        cw.visitEnd();

        return cw.toByteArray();
    }

    private void createConstructor() {
        MethodVisitor mv = mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, beanClassName, "<init>", "()V");

        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    public boolean isUnknownPk() {
        return unknownPk;
    }

    public void setUnknownPk(boolean unknownPk) {
        this.unknownPk = unknownPk;
    }
}

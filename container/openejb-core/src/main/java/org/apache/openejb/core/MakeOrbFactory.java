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
package org.apache.openejb.core;

import org.apache.xbean.asm7.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.apache.xbean.asm7.Opcodes.*;


public class MakeOrbFactory {

    public static final String ORB_FACTORY = "org.apache.openejb.core.OrbFactory";

    public static void main(String[] args) throws Exception {
        final File file = new File(args[0]);

        createOrbFactory(file);
    }

    private static void createOrbFactory(final File baseDir) throws Exception{

        final String factory = ORB_FACTORY;

        final String classFilePath = factory.replace('.', '/');

        final String sourceFileName = factory.substring(factory.lastIndexOf('.') + 1) + ".java";

        ClassWriter cw = new ClassWriter(0);
        MethodVisitor mv;

        cw.visitSource(sourceFileName, null);

        cw.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC, classFilePath, null, "java/lang/Object", null);

        {
            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            mv.visitInsn(RETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "create", "()Lorg/omg/CORBA/ORB;", null, null);
            mv.visitCode();
            final Label l0 = new Label();
            final Label l1 = new Label();
            final Label l2 = new Label();
            final Label l3 = new Label();
            final Label l4 = new Label();
            {
                mv.visitLabel(l0);
                mv.visitMethodInsn(INVOKESTATIC, "org/apache/openejb/loader/SystemInstance", "get", "()Lorg/apache/openejb/loader/SystemInstance;", false);
                mv.visitLdcInsn(Type.getType("Lorg/omg/CORBA/ORB;"));
                mv.visitMethodInsn(INVOKEVIRTUAL, "org/apache/openejb/loader/SystemInstance", "getComponent", "(Ljava/lang/Class;)Ljava/lang/Object;", false);
                mv.visitTypeInsn(CHECKCAST, "Lorg/omg/CORBA/ORB;");
                mv.visitVarInsn(ASTORE, 0);
            }
            {
                mv.visitLabel(l1);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitJumpInsn(IFNONNULL, l2);
            }
            {
                mv.visitLabel(l3);
                mv.visitMethodInsn(INVOKESTATIC, "org/omg/CORBA/ORB", "init", "()Lorg/omg/CORBA/ORB;", false);
                mv.visitVarInsn(ASTORE, 0);
            }
            {
                mv.visitLabel(l4);
                mv.visitMethodInsn(INVOKESTATIC, "org/apache/openejb/loader/SystemInstance", "get", "()Lorg/apache/openejb/loader/SystemInstance;", false);
                mv.visitLdcInsn(Type.getType("Lorg/omg/CORBA/ORB;"));
                mv.visitVarInsn(ALOAD, 0);
                mv.visitMethodInsn(INVOKEVIRTUAL, "org/apache/openejb/loader/SystemInstance", "setComponent", "(Ljava/lang/Class;Ljava/lang/Object;)Ljava/lang/Object;", false);
            }
            {
                mv.visitLabel(l2);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitInsn(ARETURN);
                mv.visitMaxs(3, 2);
            }
            mv.visitEnd();
        }

        cw.visitEnd();

        write(baseDir, cw, classFilePath);
    }

    private static void write(final File originalFile, final ClassWriter cw, final String originalClassFileName) throws IOException {
        final String classFileName = "classes/" + originalClassFileName + ".class";
        File file = originalFile;
        for (final String part : classFileName.split("/")) {
            file = new File(file, part);
        }
        file.getParentFile().mkdirs();
        try (final FileOutputStream out = new FileOutputStream(file)) {
            out.write(cw.toByteArray());
        }
    }
}

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
package org.apache.openejb.client.corba;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.sun.org.apache.xpath.internal.compiler.OpCodes;
import org.apache.xbean.asm7.*;

import static org.apache.xbean.asm7.Opcodes.*;

public class MakeCorbas {

    public static final String CORBAS = "org.apache.openejb.client.corba.Corbas";


    public static void main(String[] args) throws Exception {
        final File file = new File(args[0]);

        createOrbFactory(file);
    }

    private static void createOrbFactory(final File baseDir) throws Exception {

        final String factory = CORBAS;

        final String classFilePath = factory.replace('.', '/');

        final String sourceFileName = factory.substring(factory.lastIndexOf('.') + 1) + ".java";

        ClassWriter cw = new ClassWriter(0);
        MethodVisitor mv;

        cw.visitSource(sourceFileName, null);

        cw.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC, classFilePath, null, "java/lang/Object", null);

        /* public static Object toStub(final Object obj) throws IOException */
        {
            mv = cw.visitMethod(ACC_PRIVATE, "<init>", "()V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            mv.visitInsn(RETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(Opcodes.ACC_PUBLIC + ACC_STATIC, "toStub", "(Ljava/lang/Object;)Ljava/lang/Object;", null, new String[]{"java/io/IOException"});
            mv.visitCode();

            final Label l0 = new Label();
            final Label l1 = new Label();
            final Label l2 = new Label();
            final Label l3 = new Label();

            {
                mv.visitLabel(l0);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitTypeInsn(CHECKCAST, "Ljava/rmi/Remote;");
                mv.visitMethodInsn(INVOKESTATIC, "javax/rmi/CORBA/Util", "getTie", "(Ljava/rmi/Remote;)Ljavax/rmi/CORBA/Tie;", false);
                mv.visitVarInsn(ASTORE, 1);
            }
            {
                mv.visitLabel(l1);
                mv.visitVarInsn(ALOAD, 1);
                mv.visitJumpInsn(IFNONNULL, l2);
            }
            {
                mv.visitLabel(l3);
                mv.visitTypeInsn(Opcodes.NEW, "Ljava/io/IOException;");
                mv.visitInsn(DUP);
                mv.visitTypeInsn(Opcodes.NEW, "Ljava/lang/StringBuilder;");
                mv.visitInsn(DUP);
                mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
                mv.visitLdcInsn("Unable to serialize PortableRemoteObject; object has not been exported: ");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
                mv.visitMethodInsn(INVOKESPECIAL, "java/io/IOException.", "<init>", "(Ljava/lang/String;)V", false);
                mv.visitInsn(ATHROW);

            }
            {
                mv.visitLabel(l2);
                mv.visitMethodInsn(INVOKESTATIC, "org/apache/openejb/client/corba/Corbas", "getORB", "()Lorg/omg/CORBA/ORB;", false);
                mv.visitVarInsn(ASTORE, 2);

                mv.visitVarInsn(ALOAD, 1);
                mv.visitVarInsn(ALOAD, 2);
                mv.visitMethodInsn(INVOKEINTERFACE, "javax/rmi/CORBA/Tie", "orb", "(Lorg/omg/CORBA/ORB;)V", false);

                mv.visitVarInsn(ALOAD, 0);
                mv.visitTypeInsn(CHECKCAST, "Ljava/rmi/Remote;");
                mv.visitMethodInsn(INVOKESTATIC, "javax/rmi/PortableRemoteObject", "toStub", "(Ljava/rmi/Remote;)Ljava/rmi/Remote;", false);

                mv.visitInsn(ARETURN);
                mv.visitMaxs(4, 3);
            }
            mv.visitEnd();
        }

        /* private static ORB getORB() throws IOException  */
        {
            mv = cw.visitMethod(ACC_PRIVATE + ACC_STATIC, "getORB", "()Lorg/omg/CORBA/ORB;", null, new String[]{"java/io/IOException"});
            mv.visitCode();

            final Label l0 = new Label();
            final Label l1 = new Label();
            final Label l2 = new Label();
            final Label l3 = new Label();
            final Label l4 = new Label();
            final Label l5 = new Label();
            final Label l6 = new Label();
            final Label l7 = new Label();
            mv.visitTryCatchBlock(l0, l1, l2, "java/lang/Throwable");
            mv.visitTryCatchBlock(l3, l4, l5, "java/lang/Throwable");

            {
                mv.visitLabel(l0);
                mv.visitLdcInsn(Type.getType("Lorg/omg/CORBA/ORB;"));
                mv.visitTypeInsn(Opcodes.NEW, "Ljavax/naming/InitialContext;");
                mv.visitInsn(DUP);
                mv.visitMethodInsn(INVOKESPECIAL, "javax/naming/InitialContext", "<init>", "()V", false);
                mv.visitLdcInsn("java:comp/ORB");
                mv.visitMethodInsn(INVOKEVIRTUAL, "javax/naming/InitialContext", "lookup", "(Ljava/lang/String;)Ljava/lang/Object;", false);
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "cast", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
                mv.visitTypeInsn(CHECKCAST, "Lorg/omg/CORBA/ORB;");
            }
            {
                mv.visitLabel(l1);
                mv.visitInsn(ARETURN);
            }
            {
                mv.visitLabel(l2);
                mv.visitVarInsn(ASTORE, 0);
            }
            {
                mv.visitLabel(l3);
                mv.visitMethodInsn(INVOKESTATIC, "org/omg/CORBA/ORB", "init", "()Lorg/omg/CORBA/ORB;", false);
            }
            {
                mv.visitLabel(l4);
                mv.visitInsn(ARETURN);
            }
            {
                mv.visitLabel(l5);

                mv.visitVarInsn(ASTORE, 1);
            }
            {
                mv.visitLabel(l6);
                mv.visitTypeInsn(Opcodes.NEW, "Ljava/io/IOException;");
                mv.visitInsn(DUP);
                mv.visitLdcInsn("Unable to connect PortableRemoteObject stub to an ORB, no ORB bound to java:comp/ORB");
                mv.visitMethodInsn(INVOKESPECIAL, "java/io/IOException.", "<init>", "(Ljava/lang/String;)V", false);
                mv.visitInsn(ATHROW);
            }
            {
                mv.visitLabel(l7);
                mv.visitMaxs(3, 2);
            }
        }

        /* public static Object connect(final Object obj) throws IOException */
        {
            mv = cw.visitMethod(Opcodes.ACC_PUBLIC + ACC_STATIC, "connect", "(Ljava/lang/Object;)Ljava/lang/Object;", null, new String[]{"java/io/IOException"});
            mv.visitCode();

            final Label l0 = new Label();
            final Label l1 = new Label();
            final Label l2 = new Label();
            final Label l3 = new Label();
            final Label l4 = new Label();
            final Label l5 = new Label();

            {
                mv.visitLabel(l0);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitTypeInsn(INSTANCEOF, "Ljavax/rmi/CORBA/Stub;");
                mv.visitJumpInsn(IFEQ, l1);
            }
            {
                mv.visitLabel(l2);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitTypeInsn(CHECKCAST, "Ljavax/rmi/CORBA/Stub;");
                mv.visitVarInsn(ASTORE, 1);
            }
            {
                mv.visitLabel(l3);
                mv.visitMethodInsn(INVOKESTATIC, "org/apache/openejb/client/corba/Corbas", "getORB", "()Lorg/omg/CORBA/ORB;", false);
                mv.visitVarInsn(ASTORE, 2);
            }
            {
                mv.visitLabel(l4);
                mv.visitVarInsn(ALOAD, 1);
                mv.visitVarInsn(ALOAD, 2);
                mv.visitMethodInsn(INVOKEVIRTUAL, "javax/rmi/CORBA/Stub", "connect", "(Lorg/omg/CORBA/ORB;)V", false);

            }
            {
                mv.visitLabel(l1);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitInsn(ARETURN);
            }
            {
                mv.visitLabel(l5);
                mv.visitMaxs(2, 3);
            }

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



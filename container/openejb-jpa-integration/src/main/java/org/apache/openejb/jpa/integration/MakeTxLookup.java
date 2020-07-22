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
package org.apache.openejb.jpa.integration;

import org.apache.xbean.asm8.ClassWriter;
import org.apache.xbean.asm8.Label;
import org.apache.xbean.asm8.MethodVisitor;
import org.apache.xbean.asm8.Opcodes;
import org.apache.xbean.asm8.Type;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MakeTxLookup implements Opcodes {

    public static final String HIBERNATE_FACTORY = "org.apache.openejb.hibernate.TransactionManagerLookup";
    public static final String HIBERNATE_NEW_FACTORY = "org.apache.openejb.hibernate.OpenEJBJtaPlatform";
    public static final String HIBERNATE_NEW_FACTORY2 = "org.apache.openejb.hibernate.OpenEJBJtaPlatform2";
    public static final String TOPLINK_FACTORY = "org.apache.openejb.toplink.JTATransactionController";

    public static void main(final String[] args) throws Exception {

        final File file = new File(args[0]);

        createTopLinkStrategy(file);
        createHibernteStrategy(file);
        // hibernate repackaged its SPI...keeping all the same excepted packages
        createNewHibernateStrategy(file, HIBERNATE_NEW_FACTORY, "org/hibernate/service/jta/platform/internal");
        createNewHibernateStrategy(file, HIBERNATE_NEW_FACTORY2, "org/hibernate/engine/transaction/jta/platform/internal");
    }

    private static void createNewHibernateStrategy(final File basedir, final String target, final String abstractJtaPlatformPackage) throws Exception {
        final ClassWriter cw = new ClassWriter(0);
        MethodVisitor mv;

        cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, target.replace('.', '/'), null, abstractJtaPlatformPackage + "/AbstractJtaPlatform", null);

        {
            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, abstractJtaPlatformPackage + "/AbstractJtaPlatform", "<init>", "()V", false);
            mv.visitInsn(RETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PROTECTED, "locateTransactionManager", "()Ljavax/transaction/TransactionManager;", null, null);
            mv.visitCode();
            mv.visitMethodInsn(INVOKESTATIC, "org/apache/openejb/OpenEJB", "getTransactionManager", "()Ljavax/transaction/TransactionManager;", false);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PROTECTED, "locateUserTransaction", "()Ljavax/transaction/UserTransaction;", null, null);
            mv.visitCode();
            final Label l0 = new Label();
            final Label l1 = new Label();
            final Label l2 = new Label();
            mv.visitTryCatchBlock(l0, l1, l2, "javax/naming/NamingException");
            mv.visitLabel(l0);
            mv.visitMethodInsn(INVOKESTATIC, "org/apache/openejb/loader/SystemInstance", "get", "()Lorg/apache/openejb/loader/SystemInstance;", false);
            mv.visitLdcInsn(Type.getType("Lorg/apache/openejb/spi/ContainerSystem;"));
            mv.visitMethodInsn(INVOKEVIRTUAL, "org/apache/openejb/loader/SystemInstance", "getComponent", "(Ljava/lang/Class;)Ljava/lang/Object;", false);
            mv.visitTypeInsn(CHECKCAST, "org/apache/openejb/spi/ContainerSystem");
            mv.visitMethodInsn(INVOKEINTERFACE, "org/apache/openejb/spi/ContainerSystem", "getJNDIContext", "()Ljavax/naming/Context;", true);
            mv.visitLdcInsn("comp/UserTransaction");
            mv.visitMethodInsn(INVOKEINTERFACE, "javax/naming/Context", "lookup", "(Ljava/lang/String;)Ljava/lang/Object;", true);
            mv.visitTypeInsn(CHECKCAST, "javax/transaction/UserTransaction");
            mv.visitLabel(l1);
            mv.visitInsn(ARETURN);
            mv.visitLabel(l2);
            mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[]{"javax/naming/NamingException"});
            mv.visitVarInsn(ASTORE, 1);
            mv.visitInsn(ACONST_NULL);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(2, 2);
            mv.visitEnd();
        }
        cw.visitEnd();

        write(basedir, cw, target.replace('.', '/'));
    }

    private static void createHibernteStrategy(final File baseDir) throws Exception {

        final String factory = HIBERNATE_FACTORY;

        final String classFilePath = factory.replace('.', '/');

        final String sourceFileName = factory.substring(factory.lastIndexOf('.') + 1, factory.length()) + ".java";

        final ClassWriter cw = new ClassWriter(0);
        MethodVisitor mv;

        cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, classFilePath, null, "java/lang/Object", new String[]{"org/hibernate/transaction/TransactionManagerLookup"});

        cw.visitSource(sourceFileName, null);

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
            mv = cw.visitMethod(ACC_PUBLIC, "getTransactionManager", "(Ljava/util/Properties;)Ljavax/transaction/TransactionManager;", null, new String[]{"org/hibernate/HibernateException"});
            mv.visitCode();
            mv.visitMethodInsn(INVOKESTATIC, "org/apache/openejb/OpenEJB", "getTransactionManager", "()Ljavax/transaction/TransactionManager;", false);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(1, 2);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "getUserTransactionName", "()Ljava/lang/String;", null, null);
            mv.visitCode();
            mv.visitLdcInsn("java:comp/UserTransaction");
            mv.visitInsn(ARETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "getTransactionIdentifier", "(Ljavax/transaction/Transaction;)Ljava/lang/Object;", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 1);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(1, 2);
            mv.visitEnd();
        }
        cw.visitEnd();


        write(baseDir, cw, classFilePath);
    }

    private static void createTopLinkStrategy(final File baseDir) throws Exception {

        final String factory = TOPLINK_FACTORY;

        final String classFilePath = factory.replace('.', '/');

        final String sourceFileName = factory.substring(factory.lastIndexOf('.') + 1, factory.length()) + ".java";


        final ClassWriter cw = new ClassWriter(0);
        MethodVisitor mv;

        cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, classFilePath, null, "oracle/toplink/essentials/transaction/JTATransactionController", null);

        cw.visitSource(sourceFileName, null);

        {
            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "oracle/toplink/essentials/transaction/JTATransactionController", "<init>", "()V", false);
            mv.visitInsn(RETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PROTECTED, "acquireTransactionManager", "()Ljavax/transaction/TransactionManager;", null, new String[]{"java/lang/Exception"});
            mv.visitCode();
            mv.visitMethodInsn(INVOKESTATIC, "org/apache/openejb/OpenEJB", "getTransactionManager", "()Ljavax/transaction/TransactionManager;", false);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(1, 1);
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

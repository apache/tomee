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
package org.apache.openejb.util;

import org.apache.xbean.asm.ClassWriter;
import org.apache.xbean.asm.MethodVisitor;
import org.apache.xbean.asm.Opcodes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MakeTxLookup implements Opcodes {

    public static final String HIBERNATE_FACTORY = "org.apache.openejb.hibernate.TransactionManagerLookup";
    public static final String TOPLINK_FACTORY = "org.apache.openejb.toplink.JTATransactionController";
    public static final String ECLIPSELINK_FACTORY = "org.apache.openejb.eclipselink.JTATransactionController";

    public static void main(String[] args) throws Exception {

        File file = new File(args[0]);

        createHibernteStrategy(file);
        createTopLinkStrategy(file);
        createEclipseLinkStrategy(file);
    }

    private static void createHibernteStrategy(File baseDir) throws Exception {

        String factory = HIBERNATE_FACTORY;

        String classFilePath = factory.replace('.', '/');

        String sourceFileName = factory.substring(factory.lastIndexOf('.')+1, factory.length()) + ".java";

        ClassWriter cw = new ClassWriter(0);
        MethodVisitor mv;

        cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, classFilePath, null, "java/lang/Object", new String[]{"org/hibernate/transaction/TransactionManagerLookup"});

        cw.visitSource(sourceFileName, null);

        {
            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
            mv.visitInsn(RETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "getTransactionManager", "(Ljava/util/Properties;)Ljavax/transaction/TransactionManager;", null, new String[]{"org/hibernate/HibernateException"});
            mv.visitCode();
            mv.visitMethodInsn(INVOKESTATIC, "org/apache/openejb/OpenEJB", "getTransactionManager", "()Ljavax/transaction/TransactionManager;");
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

    private static void createTopLinkStrategy(File baseDir) throws Exception {

        String factory = TOPLINK_FACTORY;

        String classFilePath = factory.replace('.', '/');

        String sourceFileName = factory.substring(factory.lastIndexOf('.')+1, factory.length()) + ".java";


        ClassWriter cw = new ClassWriter(0);
        MethodVisitor mv;

        cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, classFilePath, null, "oracle/toplink/essentials/transaction/JTATransactionController", null);

        cw.visitSource(sourceFileName, null);

        {
            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "oracle/toplink/essentials/transaction/JTATransactionController", "<init>", "()V");
            mv.visitInsn(RETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PROTECTED, "acquireTransactionManager", "()Ljavax/transaction/TransactionManager;", null, new String[]{"java/lang/Exception"});
            mv.visitCode();
            mv.visitMethodInsn(INVOKESTATIC, "org/apache/openejb/OpenEJB", "getTransactionManager", "()Ljavax/transaction/TransactionManager;");
            mv.visitInsn(ARETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        cw.visitEnd();


        write(baseDir, cw, classFilePath);
    }

    private static void createEclipseLinkStrategy(File baseDir) throws Exception {

        String factory = ECLIPSELINK_FACTORY;

        String classFilePath = factory.replace('.', '/');

        String sourceFileName = factory.substring(factory.lastIndexOf('.')+1, factory.length()) + ".java";


        ClassWriter cw = new ClassWriter(0);
        MethodVisitor mv;

        cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, classFilePath, null, "org/eclipse/persistence/transaction/JTATransactionController", null);

        cw.visitSource(sourceFileName, null);

        {
            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "org/eclipse/persistence/transaction/JTATransactionController", "<init>", "()V");
            mv.visitInsn(RETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PROTECTED, "acquireTransactionManager", "()Ljavax/transaction/TransactionManager;", null, new String[]{"java/lang/Exception"});
            mv.visitCode();
            mv.visitMethodInsn(INVOKESTATIC, "org/apache/openejb/OpenEJB", "getTransactionManager", "()Ljavax/transaction/TransactionManager;");
            mv.visitInsn(ARETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        cw.visitEnd();


        write(baseDir, cw, classFilePath);
    }

    private static void write(File file, ClassWriter cw, String classFileName) throws IOException {
        classFileName = "classes/" + classFileName + ".class";

        for (String part : classFileName.split("/")) file = new File(file, part);

        file.getParentFile().mkdirs();

        FileOutputStream out = new FileOutputStream(file);
        out.write(cw.toByteArray());
        out.close();
    }

}

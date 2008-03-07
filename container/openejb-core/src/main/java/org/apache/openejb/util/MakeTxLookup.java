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

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileOutputStream;

public class MakeTxLookup implements Opcodes {

    public static void main(String[] args) throws Exception {
        File file = new File(args[0]);

        String[] path = {"classes", "org", "apache", "openejb", "hibernate", "TransactionManagerLookup.class"};
        for (String s : path) file = new File(file, s);

        file.getParentFile().mkdirs();
        
        FileOutputStream out = new FileOutputStream(file);
        out.write(dump());
        out.close();
    }

    public static byte[] dump() throws Exception {

        ClassWriter cw = new ClassWriter(false);
        FieldVisitor fv;
        MethodVisitor mv;
        AnnotationVisitor av0;

        cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, "org/apache/openejb/hibernate/TransactionManagerLookup", null, "java/lang/Object", new String[]{"org/hibernate/transaction/TransactionManagerLookup"});

        cw.visitSource("TransactionManagerLookup.java", null);

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
        cw.visitEnd();

        return cw.toByteArray();
    }
}

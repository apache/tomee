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
import org.objectweb.asm.Type;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @version $Rev$ $Date$
 */
public class PostCreateGenerator {
    private final Class beanClass;
    private final ClassWriter cw;

    public PostCreateGenerator(Class beanClass, ClassWriter cw) {
        this.beanClass = beanClass;
        this.cw = cw;
    }

    public void generate() {
        for (Method ejbCreate : beanClass.getMethods()) {

            if (!ejbCreate.getName().startsWith("ejbCreate")) continue;

            StringBuilder ejbPostCreateName = new StringBuilder(ejbCreate.getName());
            ejbPostCreateName.replace(0, "ejbC".length(), "ejbPostC");

            if (hasMethod(beanClass, ejbPostCreateName.toString(), ejbCreate.getParameterTypes())) continue;

            createEjbPostCreate(ejbPostCreateName.toString(), ejbCreate);
        }
    }

    private boolean hasMethod(Class beanClass, String name, Class... args) {
        try {
            Method method = beanClass.getMethod(name, args);
            return !Modifier.isAbstract(method.getModifiers());
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    public void createEjbPostCreate(String ejbPostCreateName, Method ejbCreate) {
        String methodDescriptor = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getArgumentTypes(ejbCreate));
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, ejbPostCreateName, methodDescriptor, null, null);
        mv.visitCode();
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, ejbCreate.getParameterTypes().length + 1);
        mv.visitEnd();
    }
}

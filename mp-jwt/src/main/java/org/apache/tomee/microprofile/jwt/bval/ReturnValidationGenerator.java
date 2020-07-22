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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tomee.microprofile.jwt.bval;

import org.apache.xbean.asm8.AnnotationVisitor;
import org.apache.xbean.asm8.ClassWriter;
import org.apache.xbean.asm8.MethodVisitor;
import org.apache.xbean.asm8.Type;

import java.lang.reflect.Method;
import java.util.List;

public class ReturnValidationGenerator extends ValidationGenerator {

    public ReturnValidationGenerator(final Class<?> clazz, final List<MethodConstraints> constraints) {
        super(clazz, constraints, "ReturnConstraints");
    }

    protected void generateMethods(final ClassWriter cw) {
        for (final MethodConstraints methodConstraints : constraints) {
            final Method method = methodConstraints.getMethod();
            final String name = method.getName();

            // Declare a method of return type JsonWebToken for use with
            // a call to BeanValidation's ExecutableValidator.validateReturnValue
            final Type returnType = Type.getReturnType(method);
            final Type[] parameterTypes = Type.getArgumentTypes(method);
            final String descriptor = Type.getMethodDescriptor(returnType, parameterTypes);

            final MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, name, descriptor, null, null);

            // Put the method name on the
            final AnnotationVisitor av = mv.visitAnnotation(Type.getDescriptor(Generated.class), true);
            av.visit("value", this.getClass().getName());
            av.visitEnd();

            // track the MethodVisitor
            // We will later copy over the annotations
            generatedMethods.put(method.getName() + Type.getMethodDescriptor(method), new ConstrainedMethodVisitor(mv, methodConstraints));

            if (method.getReturnType().equals(Void.TYPE)) {
                mv.visitCode();
                mv.visitInsn(RETURN);
                mv.visitMaxs(0, 1);
            } else if (method.getReturnType().equals(Long.TYPE)) {
                mv.visitCode();
                mv.visitInsn(LCONST_0);
                mv.visitInsn(LRETURN);
                mv.visitMaxs(2, 4);
                mv.visitEnd();
            } else if (method.getReturnType().equals(Float.TYPE)) {
                mv.visitCode();
                mv.visitInsn(FCONST_0);
                mv.visitInsn(FRETURN);
                mv.visitMaxs(1, 3);
                mv.visitEnd();
            } else if (method.getReturnType().equals(Double.TYPE)) {
                mv.visitCode();
                mv.visitInsn(DCONST_0);
                mv.visitInsn(DRETURN);
                mv.visitMaxs(2, 4);
                mv.visitEnd();
            } else if (method.getReturnType().isPrimitive()) {
                mv.visitCode();
                mv.visitInsn(ICONST_0);
                mv.visitInsn(IRETURN);
                mv.visitMaxs(1, 3);
                mv.visitEnd();
            } else {
                // The method will simply return null
                mv.visitCode();
                mv.visitInsn(ACONST_NULL);
                mv.visitInsn(ARETURN);
                mv.visitMaxs(1, 1);
            }
        }
    }
}

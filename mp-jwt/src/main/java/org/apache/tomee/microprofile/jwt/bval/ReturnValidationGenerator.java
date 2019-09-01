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

import org.apache.xbean.asm7.AnnotationVisitor;
import org.apache.xbean.asm7.ClassWriter;
import org.apache.xbean.asm7.MethodVisitor;
import org.apache.xbean.asm7.Type;

import java.lang.reflect.Method;
import java.util.List;

public class ReturnValidationGenerator extends ValidationGenerator {

    public ReturnValidationGenerator(final Class<?> clazz, final List<MethodConstraints> constraints) {
        super(clazz, constraints, "ReturnConstraints");
    }

    protected void generateMethods(final ClassWriter cw) {
        int id = 0;
        for (final MethodConstraints methodConstraints : constraints) {
            final Method method = methodConstraints.getMethod();
            final String name = method.getName() + "$$" + (id++);

            // Declare a method of return type JsonWebToken for use with
            // a call to BeanValidation's ExecutableValidator.validateReturnValue
            final MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, name, "()"+Type.getDescriptor(method.getReturnType()), null, null);

            // Put the method name on the
            final AnnotationVisitor av = mv.visitAnnotation(Type.getDescriptor(Name.class), true);
            av.visit("value", method.toString());
            av.visitEnd();

            // track the MethodVisitor
            // We will later copy over the annotations
            generatedMethods.put(method.getName() + Type.getMethodDescriptor(method), new ConstrainedMethodVisitor(mv, methodConstraints));

            if (method.getReturnType().equals(Void.TYPE)) {
                mv.visitCode();
                mv.visitInsn(RETURN);
                mv.visitMaxs(0, 1);
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

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

import com.sun.xml.internal.ws.org.objectweb.asm.Type;
import org.apache.openejb.dyni.DynamicSubclass;
import org.apache.openejb.util.proxy.ProxyGenerationException;
import org.apache.xbean.asm7.ClassWriter;
import org.apache.xbean.asm7.MethodVisitor;
import org.apache.xbean.asm7.Opcodes;

import javax.validation.Constraint;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConstraintsGenerator implements Opcodes {

    public static byte[] generateFor(final Class<?> target) throws ProxyGenerationException {

        final Map<String, MethodVisitor> visitors = new HashMap<>();

        final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

        final String generatedClassName = (target.getName() + "$$JwtConstraints").replace('.', '/');

        cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, generatedClassName, null, "java/lang/Object", null);

        { // private constructor
            final MethodVisitor mv = cw.visitMethod(ACC_PRIVATE, "<init>", "()V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            mv.visitInsn(RETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }

        int id = 0;
        for (final Method method : getConstrainedMethods(target)) {
            final String name = method.getName() + "$$" + (id++);

            // Declare a method of return type JsonWebToken for use with
            // a call to BeanValidation's ExecutableValidator.validateReturnValue
            final MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, name, "()Lorg/eclipse/microprofile/jwt/JsonWebToken;", null, null);

            // track the MethodVisitor
            // We will later copy over the annotations
            visitors.put(method.getName() + Type.getMethodDescriptor(method), mv);

            // The method will simply return null
            mv.visitCode();
            mv.visitInsn(ACONST_NULL);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(1, 1);
        }

        DynamicSubclass.copyMethodAnnotations(target, visitors);

        // This should never be reached, but just in case
        for (final MethodVisitor visitor : visitors.values()) {
            visitor.visitEnd();
        }

        return cw.toByteArray();
    }

    public static List<Method> getConstrainedMethods(final Class<?> clazz) {
        final List<Method> constrained = new ArrayList<Method>();

        // we could have been doing this long before Streams
        for (Method method : clazz.getMethods())
            for (Annotation annotation : method.getAnnotations())
                if (isConstraint(annotation))
                    constrained.add(method);

        return constrained;
    }

    private static boolean isConstraint(final Annotation annotation) {
        return annotation.annotationType().isAnnotationPresent(Constraint.class);
    }
}

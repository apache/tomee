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

import org.apache.openejb.dyni.DynamicSubclass;
import org.apache.openejb.util.proxy.ProxyGenerationException;
import org.apache.xbean.asm7.AnnotationVisitor;
import org.apache.xbean.asm7.ClassReader;
import org.apache.xbean.asm7.ClassVisitor;
import org.apache.xbean.asm7.ClassWriter;
import org.apache.xbean.asm7.MethodVisitor;
import org.apache.xbean.asm7.Opcodes;
import org.apache.xbean.asm7.Type;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JwtValidationGenerator extends ValidationGenerator {

    final Map<String, MethodVisitor> generatedMethods = new LinkedHashMap<>();

    public JwtValidationGenerator(final Class<?> clazz, final List<MethodConstraints> constraints) {
        super(clazz, constraints, "JwtConstraints");
    }

    @Override
    public byte[] generate() throws ProxyGenerationException {
        generatedMethods.clear();

        final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

        final String generatedClassName = getName(clazz).replace('.', '/');

        cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, generatedClassName, null, "java/lang/Object", null);

        { // public constructor
            final MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            mv.visitInsn(RETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }

        int id = 0;
        for (final MethodConstraints methodConstraints : constraints) {
            final Method method = methodConstraints.getMethod();
            final String name = method.getName() + "$$" + (id++);

            // Declare a method of return type JsonWebToken for use with
            // a call to BeanValidation's ExecutableValidator.validateReturnValue
            final MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, name, "()Lorg/eclipse/microprofile/jwt/JsonWebToken;", null, null);

            // Put the method name on the
            final AnnotationVisitor av = mv.visitAnnotation(Type.getDescriptor(Name.class), true);
            av.visit("value", method.toString());
            av.visitEnd();

            // track the MethodVisitor
            // We will later copy over the annotations
            generatedMethods.put(method.getName() + Type.getMethodDescriptor(method), new ConstrainedMethodVisitor(mv, methodConstraints));

            // The method will simply return null
            mv.visitCode();
            mv.visitInsn(ACONST_NULL);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(1, 1);
        }

        /**
         * Read all parent classes and copy the public methods we need
         * into our new class.
         */
        Class current = clazz;
        while (current != null && !current.equals(Object.class)) {
            try {
                final ClassReader classReader = new ClassReader(DynamicSubclass.readClassFile(current));
                classReader.accept(new CopyMethodAnnotations(), ClassReader.SKIP_CODE);
            } catch (final IOException e) {
                throw new ProxyGenerationException(e);
            }
            current = current.getSuperclass();
        }

        for (final MethodVisitor visitor : generatedMethods.values()) {
            visitor.visitEnd();
        }

        return cw.toByteArray();
    }

    public static String getName(final Class<?> target) {
        return target.getName() + "$$JwtConstraints";
    }

    public static class MoveAnnotationsVisitor extends MethodVisitor {

        private final MethodVisitor newMethod;

        public MoveAnnotationsVisitor(final MethodVisitor movedMethod, final MethodVisitor newMethod) {
            super(Opcodes.ASM7, movedMethod);
            this.newMethod = newMethod;
        }

        @Override
        public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
            return newMethod.visitAnnotation(desc, visible);
        }

        @Override
        public AnnotationVisitor visitParameterAnnotation(final int parameter, final String desc, final boolean visible) {
            return super.visitParameterAnnotation(parameter, desc, visible);
        }

        @Override
        public void visitEnd() {
            newMethod.visitEnd();
            super.visitEnd();
        }
    }

    private class CopyMethodAnnotations extends ClassVisitor {

        public CopyMethodAnnotations() {
            super(Opcodes.ASM7);
        }

        @Override
        public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
            final MethodVisitor generatedMethod = generatedMethods.remove(name + desc);

            if (generatedMethod == null) {
                return null;
            }

            final MethodVisitor sourceMethod = super.visitMethod(access, name, desc, signature, exceptions);

            return new MoveAnnotationsVisitor(sourceMethod, generatedMethod);
        }
    }

    /**
     * Wraps a MethodVisitor and ignores all annotations that are not
     * bean validation annotations that should be on this method.
     */
    public static class ConstrainedMethodVisitor extends MethodVisitor {
        private final List<String> approved;

        public ConstrainedMethodVisitor(final MethodVisitor methodVisitor, final MethodConstraints methodConstraints) {
            super(Opcodes.ASM7, methodVisitor);
            this.approved = methodConstraints.getAnnotations().stream()
                    .map(Type::getDescriptor)
                    .collect(Collectors.toList());
        }

        @Override
        public AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible) {
            /**
             * If this is a bean validation annotation we need, allow it to be added
             */
            if (approved.contains(descriptor)) return super.visitAnnotation(descriptor, visible);

            /**
             * Otherwise tell ASM to ignore it
             */
            return null;
        }
    }
}

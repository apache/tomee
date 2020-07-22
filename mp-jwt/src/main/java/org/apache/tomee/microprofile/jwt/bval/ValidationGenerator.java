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
import org.apache.openejb.util.proxy.LocalBeanProxyFactory;
import org.apache.openejb.util.proxy.ProxyGenerationException;
import org.apache.xbean.asm8.AnnotationVisitor;
import org.apache.xbean.asm8.ClassReader;
import org.apache.xbean.asm8.ClassVisitor;
import org.apache.xbean.asm8.ClassWriter;
import org.apache.xbean.asm8.MethodVisitor;
import org.apache.xbean.asm8.Opcodes;
import org.apache.xbean.asm8.Type;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * We allow CDI and EJB beans to use BeanValidation to validate a JsonWebToken
 * instance by simply creating contstraints and putting them on that method.
 *
 * BeanValidation doesn't "see" them there so we have to generate a class
 * that has the annotations in a place BeanValidation can see.
 *
 * To accomplish this, for every method that has BeanValidation constraints
 * we generate an equivalent method that has those same annotations and
 * returns JsonWebToken.
 *
 * We can then pass the generated method to BeanValidation's
 * ExecutableValidator.validateReturnValue and pass in the JsonWebToken instance
 *
 * The only purpose of this generated class and these generated methods is to
 * make BeanValidation happy.  If BeanValidation added something like this:
 *
 *   getValidator().validate(Object instance, Annotation[] annotations);
 *
 * Then all the code here could be deleted.
 *
 * A short example of the kind of code it generates.
 *
 * This class:
 *
 *    public class Colors {
 *      @Issuer("http://foo.bar.com")
 *      public void red(String foo) {
 *      }
 *
 *      @Issuer("http://foo.bar.com")
 *      public boolean blue(boolean b) {
 *          return b;
 *      }
 *
 *      public void green() {
 *      }
 *    }
 *
 * Would result in this generated class:
 *
 *    public class Colors$$JwtConstraints {
 *
 *      private Colors$$JwtConstraints() {
 *      }
 *
 *      @Issuer("http://foo.bar.com")
 *      public JsonWebToken red$$0() {
 *          return null;
 *      }
 *
 *      @Issuer("http://foo.bar.com")
 *      public JsonWebToken blue$$1() {
 *          return null;
 *      }
 *    }
 *
 */
public abstract class ValidationGenerator implements Opcodes {

    protected final Class<?> clazz;
    protected final List<MethodConstraints> constraints;
    protected final String suffix;
    protected final Map<String, MethodVisitor> generatedMethods = new LinkedHashMap<>();

    public ValidationGenerator(final Class<?> clazz, final List<MethodConstraints> constraints, final String suffix) {
        this.clazz = clazz;
        this.constraints = new ArrayList<>(constraints);
        this.suffix = suffix;
        Collections.sort(this.constraints);
    }

    protected abstract void generateMethods(final ClassWriter cw);

    public Class<?> generateAndLoad() {
        return loadOrCreate();
    }

    public String getName() {
        return clazz.getName() + "$$" + suffix;
    }

    public Class<?> loadOrCreate() {
        final String constraintsClassName = getName();
        final ClassLoader classLoader = clazz.getClassLoader();

        try {
            return classLoader.loadClass(constraintsClassName);
        } catch (ClassNotFoundException e) {
            // ok, let's continue on and make it
        }

        final byte[] bytes;
        try {
            bytes = generate();
        } catch (ProxyGenerationException e) {
            throw new ValidationGenerationException(clazz, e);
        }

        if (bytes == null) return null;

        try {
            return LocalBeanProxyFactory.Unsafe.defineClass(classLoader, clazz, constraintsClassName, bytes);
        } catch (IllegalAccessException e) {
            throw new ValidationGenerationException(clazz, e);
        } catch (InvocationTargetException e) {
            throw new ValidationGenerationException(clazz, e.getCause());
        }
    }

    public byte[] generate() throws ProxyGenerationException {
        generatedMethods.clear();

        final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

        final String generatedClassName = getName().replace('.', '/');

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

        generateMethods(cw);

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

        return cw.toByteArray();
    }

    public class CopyMethodAnnotations extends ClassVisitor {

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

            return new MethodVisitor(Opcodes.ASM7, sourceMethod) {
                @Override
                public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
                    return generatedMethod.visitAnnotation(desc, visible);
                }

                @Override
                public void visitEnd() {
                    generatedMethod.visitEnd();
                    super.visitEnd();
                }
            };
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

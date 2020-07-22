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

package org.apache.openejb.dyni;

import org.apache.openejb.loader.IO;
import org.apache.openejb.util.Debug;
import org.apache.openejb.util.proxy.LocalBeanProxyFactory;
import org.apache.openejb.util.proxy.ProxyGenerationException;
import org.apache.xbean.asm8.AnnotationVisitor;
import org.apache.xbean.asm8.ClassReader;
import org.apache.xbean.asm8.ClassVisitor;
import org.apache.xbean.asm8.ClassWriter;
import org.apache.xbean.asm8.MethodVisitor;
import org.apache.xbean.asm8.Opcodes;
import org.apache.xbean.asm8.Type;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.Arrays.asList;

/**
 * @version $Rev$ $Date$
 */
public class DynamicSubclass implements Opcodes {

    private static final ReentrantLock LOCK = new ReentrantLock();
    public static final String IMPL_SUFFIX = "$$Impl";

    public static boolean isDynamic(final Class beanClass) {
        return Modifier.isAbstract(beanClass.getModifiers()) && InvocationHandler.class.isAssignableFrom(beanClass);
    }

    public static Class createSubclass(final Class<?> abstractClass, final ClassLoader cl) {
        return createSubclass(abstractClass, cl, false);
    }

    public static Class createSubclass(final Class<?> abstractClass, final ClassLoader cl, boolean proxyNonAbstractMethods) {
        final String proxyName = getSubclassName(abstractClass);

        try {
            return cl.loadClass(proxyName);
        } catch (final Exception e) {
            // no-op
        }

        final ReentrantLock lock = LOCK;
        lock.lock();

        try {

            try { // Try it again, another thread may have beaten this one...
                return cl.loadClass(proxyName);
            } catch (final Exception e) {
                // no-op
            }

            return LocalBeanProxyFactory.Unsafe.defineClass(cl, abstractClass, proxyName, generateBytes(abstractClass, proxyNonAbstractMethods));

        } catch (final Exception e) {
            throw new InternalError(DynamicSubclass.class.getSimpleName() + ".createSubclass: " + Debug.printStackTrace(e));
        } finally {
            lock.unlock();
        }
    }

    public static void setHandler(final Object instance, final InvocationHandler handler) {
        try {
            final Field thisHandler = instance.getClass().getDeclaredField("this$handler");
            if (!thisHandler.isAccessible()) {
                thisHandler.setAccessible(true);
            }
            thisHandler.set(instance, handler);
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static byte[] generateBytes(final Class<?> classToProxy, final boolean proxyNonAbstractMethods) throws ProxyGenerationException {

        final Map<String, MethodVisitor> visitors = new HashMap<>();

        final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

        final String proxyClassFileName = getSubclassName(classToProxy).replace('.', '/');
        final String classFileName = classToProxy.getName().replace('.', '/');

        cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, proxyClassFileName, null, classFileName, null);
        cw.visitSource(classFileName + ".java", null);


        // push InvocationHandler field
        cw.visitField(ACC_FINAL + ACC_PRIVATE, "this$handler", "Ljava/lang/reflect/InvocationHandler;", null, null).visitEnd();

        for (final Constructor<?> constructor : classToProxy.getConstructors()) {
            if (!Modifier.isPublic(constructor.getModifiers())) {
                continue;
            }

            final MethodVisitor mv = visitConstructor(cw, proxyClassFileName, classFileName, constructor);
            visitors.put("<init>" + Type.getConstructorDescriptor(constructor), mv);
        }

        final Map<String, List<Method>> methodMap = new HashMap<>();

        getNonPrivateMethods(classToProxy, methodMap);

        // Iterate over the public methods
        for (final Map.Entry<String, List<Method>> entry : methodMap.entrySet()) {

            for (final Method method : entry.getValue()) {
                if (Modifier.isAbstract(method.getModifiers()) || (proxyNonAbstractMethods && Modifier.isPublic(method.getModifiers()))) {
                    final MethodVisitor visitor = LocalBeanProxyFactory.visit(cw, method, proxyClassFileName, "this$handler");
                    visitors.put(method.getName() + Type.getMethodDescriptor(method), visitor);
                }
            }
        }

        copyClassAnnotations(classToProxy, cw);

        copyMethodAnnotations(classToProxy, visitors);

        // This should never be reached, but just in case
        for (final MethodVisitor visitor : visitors.values()) {
            visitor.visitEnd();
        }

        return cw.toByteArray();
    }

    private static MethodVisitor visitConstructor(final ClassWriter cw, final String proxyClassFileName, final String classFileName, final Constructor<?> constructor) {
        final String descriptor = Type.getConstructorDescriptor(constructor);

        final String[] exceptions = new String[constructor.getExceptionTypes().length];
        for (int i = 0; i < exceptions.length; i++) {
            exceptions[i] = Type.getInternalName(constructor.getExceptionTypes()[i]);
        }

        final MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", descriptor, null, exceptions);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        int index = 1;

        for (final Type type : Type.getArgumentTypes(descriptor)) {
            mv.visitVarInsn(type.getOpcode(ILOAD), index);
            index += size(type);
        }

        mv.visitMethodInsn(INVOKESPECIAL, classFileName, "<init>", descriptor, false);

        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(PUTFIELD, proxyClassFileName, "this$handler", "Ljava/lang/reflect/InvocationHandler;");
        mv.visitInsn(RETURN);
        mv.visitMaxs(2, 1);
        return mv;
    }

    private static String getSubclassName(final Class<?> classToProxy) {
        return classToProxy.getName() + IMPL_SUFFIX;
    }

    private static void getNonPrivateMethods(final Class<?> impl, final Map<String, List<Method>> methodMap) {
        final Class<?>[] interfaces = impl.getInterfaces();
        final Collection<Class<?>> api = new ArrayList<>(interfaces.length + 1);
        api.add(impl);
        api.addAll(asList(interfaces));

        for (Class<?> clazz : api) {
            while (clazz != null && clazz != Object.class) {
                for (final Method method : clazz.getDeclaredMethods()) {
                    final int modifiers = method.getModifiers();

                    if (Modifier.isFinal(modifiers)
                            || Modifier.isPrivate(modifiers)
                            || Modifier.isStatic(modifiers)) {
                        continue;
                    }

                    List<Method> methods = methodMap.get(method.getName());
                    if (methods == null) {
                        methods = new ArrayList<>();
                        methods.add(method);
                        methodMap.put(method.getName(), methods);
                    } else {
                        if (!isOverridden(methods, method)) {
                            // method is not overridden, so add it
                            methods.add(method);
                        }
                    }
                }

                clazz = clazz.getSuperclass();
            }
        }
    }

    private static boolean isOverridden(final List<Method> methods, final Method method) {
        for (final Method m : methods) {
            if (Arrays.equals(m.getParameterTypes(), method.getParameterTypes())) {
                return true;
            }
        }
        return false;
    }

    public static int size(final Type type) {
        if (Type.VOID_TYPE.equals(type)) {
            return 0;
        }
        if (Type.LONG_TYPE.equals(type) || Type.DOUBLE_TYPE.equals(type)) {
            return 2;
        }
        return 1;
    }

    public static byte[] readClassFile(final Class clazz) throws IOException {
        return readClassFile(clazz.getClassLoader(), clazz);
    }

    public static byte[] readClassFile(final ClassLoader classLoader, final Class clazz) throws IOException {
        final String internalName = clazz.getName().replace('.', '/') + ".class";
        final URL resource = classLoader.getResource(internalName);

        final InputStream in = IO.read(resource);
        final ByteArrayOutputStream out;
        try {
            out = new ByteArrayOutputStream();
            IO.copy(in, out);
        } finally {
            IO.close(in);
        }

        return out.toByteArray();
    }

    public static void copyMethodAnnotations(final Class<?> classToProxy, final Map<String, MethodVisitor> visitors) throws ProxyGenerationException {
        // Move all the annotations onto the newly implemented methods
        // Ensures CDI and JAX-RS and JAX-WS still work
        Class clazz = classToProxy;
        while (clazz != null && !clazz.equals(Object.class)) {
            try {
                final ClassReader classReader = new ClassReader(readClassFile(clazz));
                final ClassVisitor copyMethodAnnotations = new CopyMethodAnnotations(visitors);
                classReader.accept(copyMethodAnnotations, ClassReader.SKIP_CODE);
            } catch (final IOException e) {
                throw new ProxyGenerationException(e);
            }
            clazz = clazz.getSuperclass();
        }
    }

    private static void copyClassAnnotations(final Class<?> clazz, final ClassVisitor newClass) throws ProxyGenerationException {
        try {
            final ClassReader classReader = new ClassReader(readClassFile(clazz));
            final ClassVisitor visitor = new CopyClassAnnotations(newClass);
            classReader.accept(visitor, ClassReader.SKIP_CODE);
        } catch (final IOException e) {
            throw new ProxyGenerationException(e);
        }
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
            return newMethod.visitParameterAnnotation(parameter, desc, visible);
        }

        @Override
        public void visitEnd() {
            newMethod.visitEnd();
            super.visitEnd();
        }
    }


    private static class CopyClassAnnotations extends ClassVisitor {
        private final ClassVisitor newClass;

        public CopyClassAnnotations(final ClassVisitor newClass) {
            super(Opcodes.ASM7);
            this.newClass = newClass;
        }

        @Override
        public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
            return newClass.visitAnnotation(desc, visible);
        }
    }

    private static class CopyMethodAnnotations extends ClassVisitor {
        private final Map<String, MethodVisitor> visitors;

        public CopyMethodAnnotations(final Map<String, MethodVisitor> visitors) {
            super(Opcodes.ASM7);
            this.visitors = visitors;
        }

        @Override
        public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
            final MethodVisitor newMethod = visitors.remove(name + desc);

            if (newMethod == null) {
                return null;
            }

            final MethodVisitor oldMethod = super.visitMethod(access, name, desc, signature, exceptions);

            return new MoveAnnotationsVisitor(oldMethod, newMethod);
        }
    }
}

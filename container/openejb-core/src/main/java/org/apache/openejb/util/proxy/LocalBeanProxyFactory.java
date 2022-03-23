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

package org.apache.openejb.util.proxy;


import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Debug;
import org.apache.xbean.asm9.ClassWriter;
import org.apache.xbean.asm9.Label;
import org.apache.xbean.asm9.MethodVisitor;
import org.apache.xbean.asm9.Opcodes;
import org.apache.xbean.asm9.Type;

import jakarta.ejb.EJBException;
import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class LocalBeanProxyFactory implements Opcodes {

    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB, LocalBeanProxyFactory.class);

    public static final InvocationHandler NON_BUSINESS_HANDLER = new NonBusinessHandler();

    private static final String BUSSINESS_HANDLER_NAME = "businessHandler";
    private static final String NON_BUSINESS_HANDLER_NAME = "nonBusinessHandler";
    private static final ReentrantLock LOCK = new ReentrantLock();

    public static Object newProxyInstance(final ClassLoader classLoader, final InvocationHandler handler, final Class classToSubclass, final Class... interfaces) throws IllegalArgumentException {
        try {
            final Class proxyClass = createProxy(classToSubclass, classLoader, interfaces);
            return constructProxy(proxyClass, handler);
        } catch (final Throwable e) {
            throw new InternalError("LocalBeanProxyFactory.newProxyInstance: " + Debug.printStackTrace(e));
        }
    }

    public static void setInvocationHandler(final Object proxy, final InvocationHandler invocationHandler) {
        try {
            final Field field = proxy.getClass().getDeclaredField(BUSSINESS_HANDLER_NAME);
            field.setAccessible(true);
            try {
                field.set(proxy, invocationHandler);
            } finally {
                field.setAccessible(false);
            }
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static InvocationHandler getInvocationHandler(final Object proxy) {
        try {
            final Field field = proxy.getClass().getDeclaredField(BUSSINESS_HANDLER_NAME);
            field.setAccessible(true);
            try {
                return (InvocationHandler) field.get(proxy);
            } finally {
                field.setAccessible(false);
            }
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Object constructProxy(final Class clazz, final InvocationHandler handler) throws IllegalStateException {

        final Object instance = Unsafe.allocateInstance(clazz);

        Unsafe.setValue(getDeclaredField(clazz, BUSSINESS_HANDLER_NAME), instance, handler);
        Unsafe.setValue(getDeclaredField(clazz, NON_BUSINESS_HANDLER_NAME), instance, NON_BUSINESS_HANDLER);

        return instance;
    }

    private static Field getDeclaredField(final Class clazz, final String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (final NoSuchFieldException e) {
            final String message = String.format("Proxy class does not contain expected field \"%s\": %s", fieldName, clazz.getName());
            throw new IllegalStateException(message, e);
        }
    }

    public static boolean isProxy(final Class<?> clazz) {
        return clazz.isAnnotationPresent(Proxy.class);
    }

    public static Class createProxy(final Class<?> classToProxy, final ClassLoader cl, final String proxyName, final Class... interfaces) {
        final String classFileName = proxyName.replace('.', '/');

        try {
            return cl.loadClass(proxyName);
        } catch (final Exception e) {
            // no-op
        }

        final ReentrantLock lock = LocalBeanProxyFactory.LOCK;
        lock.lock();

        try {

            try { // Try it again, another thread may have beaten this one...
                return cl.loadClass(proxyName);
            } catch (final Exception e) {
                // no-op
            }

            final byte[] proxyBytes = generateProxy(classToProxy, classFileName, interfaces);
            return ClassDefiner.defineClass(cl, proxyName, proxyBytes, classToProxy, classToProxy.getProtectionDomain());
            // return Unsafe.defineClass(cl, classToProxy, proxyName, proxyBytes);
        } catch (final Exception e) {
            throw new InternalError("LocalBeanProxyFactory.createProxy: " + Debug.printStackTrace(e));
        } finally {
            lock.unlock();
        }
    }

    public static Class createProxy(final Class<?> classToProxy, final ClassLoader cl, final Class... interfaces) {
        return createProxy(classToProxy, cl, classToProxy.getName() + "$$LocalBeanProxy", interfaces);
    }

    public static byte[] generateProxy(final Class<?> classToProxy, final String proxyName, final Class<?>... interfaces) throws ProxyGenerationException {
        final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

        final String proxyClassFileName = proxyName.replace('.', '/');
        final String classFileName = classToProxy.getName().replace('.', '/');

        // push class signature
        final String[] interfaceNames = new String[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            final Class<?> anInterface = interfaces[i];
            interfaceNames[i] = anInterface.getName().replace('.', '/');
        }

        cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, proxyClassFileName, null, classFileName, interfaceNames);
        cw.visitSource(classFileName + ".java", null);

        cw.visitAnnotation("L" + Proxy.class.getName().replace('.', '/') + ";", true).visitEnd();

        // push InvocationHandler fields
        cw.visitField(ACC_FINAL + ACC_PRIVATE, BUSSINESS_HANDLER_NAME, "Ljava/lang/reflect/InvocationHandler;", null, null).visitEnd();
        cw.visitField(ACC_FINAL + ACC_PRIVATE, NON_BUSINESS_HANDLER_NAME, "Ljava/lang/reflect/InvocationHandler;", null, null).visitEnd();

        final Map<String, List<Method>> methodMap = new HashMap<>();

        getNonPrivateMethods(classToProxy, methodMap);

        for (final Class<?> anInterface : interfaces) {
            getNonPrivateMethods(anInterface, methodMap);
        }

        // Iterate over the public methods
        for (final Map.Entry<String, List<Method>> entry : methodMap.entrySet()) {

            for (final Method method : entry.getValue()) {
                final String name = method.getName();

                if (Modifier.isPublic(method.getModifiers())
                    || method.getParameterTypes().length == 0 && ("finalize".equals(name)
                    || "clone".equals(name))) {
                    // forward invocations of any public methods or 
                    // finalize/clone methods to businessHandler 
                    processMethod(cw, method, proxyClassFileName, BUSSINESS_HANDLER_NAME);
                } else {
                    // forward invocations of any other methods to nonBusinessHandler
                    processMethod(cw, method, proxyClassFileName, NON_BUSINESS_HANDLER_NAME);
                }
            }
        }

        return cw.toByteArray();
    }

    private static void getNonPrivateMethods(Class<?> clazz, final Map<String, List<Method>> methodMap) {
        while (clazz != null) {
            for (final Method method : clazz.getDeclaredMethods()) {
                if (method.isBridge()) {
                    continue;
                }

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
                    } // else method is overridden in superclass, so do nothing
                }
            }

            clazz = clazz.getSuperclass();
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

    public static void processMethod(final ClassWriter cw, final Method method, final String proxyName, final String handlerName) throws ProxyGenerationException {
        if ("<init>".equals(method.getName())) {
            return;
        }

        visit(cw, method, proxyName, handlerName).visitEnd();
    }

    public static MethodVisitor visit(final ClassWriter cw, final Method method, final String proxyName, final String handlerName) throws ProxyGenerationException {
        final Class<?> returnType = method.getReturnType();
        final Class<?>[] parameterTypes = method.getParameterTypes();
        final Class<?>[] exceptionTypes = method.getExceptionTypes();
        final int modifiers = method.getModifiers();

        // push the method definition
        int modifier = 0;
        if (Modifier.isPublic(modifiers)) {
            modifier = ACC_PUBLIC;
        } else if (Modifier.isProtected(modifiers)) {
            modifier = ACC_PROTECTED;
        }

        final MethodVisitor mv = cw.visitMethod(modifier, method.getName(), getMethodSignatureAsString(returnType, parameterTypes), null, null);
        mv.visitCode();

        // push try/catch block, to catch declared exceptions, and to catch java.lang.Throwable
        final Label l0 = new Label();
        final Label l1 = new Label();
        final Label l2 = new Label();

        if (exceptionTypes.length > 0) {
            mv.visitTryCatchBlock(l0, l1, l2, "java/lang/reflect/InvocationTargetException");
        }

        // push try code
        mv.visitLabel(l0);
        final String classNameToOverride = method.getDeclaringClass().getName().replace('.', '/');
        mv.visitLdcInsn(Type.getType("L" + classNameToOverride + ";"));

        // the following code generates the bytecode for this line of Java:
        // Method method = <proxy>.class.getMethod("add", new Class[] { <array of function argument classes> });

        // get the method name to invoke, and push to stack
        mv.visitLdcInsn(method.getName());

        // create the Class[]
        createArrayDefinition(mv, parameterTypes.length, Class.class);

        int length = 1;

        // push parameters into array
        for (int i = 0; i < parameterTypes.length; i++) {
            // keep copy of array on stack
            mv.visitInsn(DUP);

            final Class<?> parameterType = parameterTypes[i];

            // push number onto stack
            pushIntOntoStack(mv, i);

            if (parameterType.isPrimitive()) {
                final String wrapperType = getWrapperType(parameterType);
                mv.visitFieldInsn(GETSTATIC, wrapperType, "TYPE", "Ljava/lang/Class;");
            } else {
                mv.visitLdcInsn(Type.getType(getAsmTypeAsString(parameterType, true)));
            }

            mv.visitInsn(AASTORE);

            if (Long.TYPE.equals(parameterType) || Double.TYPE.equals(parameterType)) {
                length += 2;
            } else {
                length++;
            }
        }

        // invoke getMethod() with the method name and the array of types
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getDeclaredMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", false);

        // store the returned method for later
        mv.visitVarInsn(ASTORE, length);

        // the following code generates bytecode equivalent to:
        // return ((<returntype>) invocationHandler.invoke(this, method, new Object[] { <function arguments }))[.<primitive>Value()];

        final Label l4 = new Label();
        mv.visitLabel(l4);
        mv.visitVarInsn(ALOAD, 0);

        // get the invocationHandler field from this class
        mv.visitFieldInsn(GETFIELD, proxyName, handlerName, "Ljava/lang/reflect/InvocationHandler;");

        // we want to pass "this" in as the first parameter
        mv.visitVarInsn(ALOAD, 0);

        // and the method we fetched earlier
        mv.visitVarInsn(ALOAD, length);

        // need to construct the array of objects passed in

        // create the Object[]
        createArrayDefinition(mv, parameterTypes.length, Object.class);

        int index = 1;
        // push parameters into array
        for (int i = 0; i < parameterTypes.length; i++) {
            // keep copy of array on stack
            mv.visitInsn(DUP);

            final Class<?> parameterType = parameterTypes[i];

            // push number onto stack
            pushIntOntoStack(mv, i);

            if (parameterType.isPrimitive()) {
                final String wrapperType = getWrapperType(parameterType);
                mv.visitVarInsn(getVarInsn(parameterType), index);

                mv.visitMethodInsn(INVOKESTATIC, wrapperType, "valueOf", "(" + getPrimitiveLetter(parameterType) + ")L" + wrapperType + ";", false);
                mv.visitInsn(AASTORE);

                if (Long.TYPE.equals(parameterType) || Double.TYPE.equals(parameterType)) {
                    index += 2;
                } else {
                    index++;
                }
            } else {
                mv.visitVarInsn(ALOAD, index);
                mv.visitInsn(AASTORE);
                index++;
            }
        }

        // invoke the invocationHandler
        mv.visitMethodInsn(INVOKEINTERFACE, "java/lang/reflect/InvocationHandler", "invoke", "(Ljava/lang/Object;Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;", true);

        // cast the result
        mv.visitTypeInsn(CHECKCAST, getCastType(returnType));

        if (returnType.isPrimitive() && !Void.TYPE.equals(returnType)) {
            // get the primitive value
            mv.visitMethodInsn(INVOKEVIRTUAL, getWrapperType(returnType), getPrimitiveMethod(returnType), "()" + getPrimitiveLetter(returnType), false);
        }

        // push return
        mv.visitLabel(l1);
        if (!Void.TYPE.equals(returnType)) {
            mv.visitInsn(getReturnInsn(returnType));
        } else {
            mv.visitInsn(POP);
            mv.visitInsn(RETURN);
        }

        // catch InvocationTargetException
        if (exceptionTypes.length > 0) {
            mv.visitLabel(l2);
            mv.visitVarInsn(ASTORE, length);

            final Label l5 = new Label();
            mv.visitLabel(l5);

            for (int i = 0; i < exceptionTypes.length; i++) {
                final Class<?> exceptionType = exceptionTypes[i];

                mv.visitLdcInsn(Type.getType("L" + exceptionType.getName().replace('.', '/') + ";"));
                mv.visitVarInsn(ALOAD, length);
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/InvocationTargetException", "getCause", "()Ljava/lang/Throwable;", false);
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "equals", "(Ljava/lang/Object;)Z", false);

                final Label l6 = new Label();
                mv.visitJumpInsn(IFEQ, l6);

                final Label l7 = new Label();
                mv.visitLabel(l7);

                mv.visitVarInsn(ALOAD, length);
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/InvocationTargetException", "getCause", "()Ljava/lang/Throwable;", false);
                mv.visitTypeInsn(CHECKCAST, exceptionType.getName().replace('.', '/'));
                mv.visitInsn(ATHROW);
                mv.visitLabel(l6);

                if (i == exceptionTypes.length - 1) {
                    mv.visitTypeInsn(NEW, "java/lang/reflect/UndeclaredThrowableException");
                    mv.visitInsn(DUP);
                    mv.visitVarInsn(ALOAD, length);
                    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/reflect/UndeclaredThrowableException", "<init>", "(Ljava/lang/Throwable;)V", false);
                    mv.visitInsn(ATHROW);
                }
            }
        }

        // finish this method
        mv.visitMaxs(0, 0);
        return mv;
    }

    /**
     * Gets the appropriate bytecode instruction for RETURN, according to what type we need to return
     *
     * @param type Type the needs to be returned
     * @return The matching bytecode instruction
     */
    private static int getReturnInsn(final Class<?> type) {
        if (type.isPrimitive()) {
            if (Integer.TYPE.equals(type)) {
                return IRETURN;
            } else if (Boolean.TYPE.equals(type)) {
                return IRETURN;
            } else if (Character.TYPE.equals(type)) {
                return IRETURN;
            } else if (Byte.TYPE.equals(type)) {
                return IRETURN;
            } else if (Short.TYPE.equals(type)) {
                return IRETURN;
            } else if (Float.TYPE.equals(type)) {
                return FRETURN;
            } else if (Long.TYPE.equals(type)) {
                return LRETURN;
            } else if (Double.TYPE.equals(type)) {
                return DRETURN;
            }
        }

        return ARETURN;
    }

    /**
     * Returns the appropriate bytecode instruction to load a value from a variable to the stack
     *
     * @param type Type to load
     * @return Bytecode instruction to use
     */
    private static int getVarInsn(final Class<?> type) {
        if (type.isPrimitive()) {
            if (Integer.TYPE.equals(type)) {
                return ILOAD;
            } else if (Boolean.TYPE.equals(type)) {
                return ILOAD;
            } else if (Character.TYPE.equals(type)) {
                return ILOAD;
            } else if (Byte.TYPE.equals(type)) {
                return ILOAD;
            } else if (Short.TYPE.equals(type)) {
                return ILOAD;
            } else if (Float.TYPE.equals(type)) {
                return FLOAD;
            } else if (Long.TYPE.equals(type)) {
                return LLOAD;
            } else if (Double.TYPE.equals(type)) {
                return DLOAD;
            }
        }

        throw new IllegalStateException("Type: " + type.getCanonicalName() + " is not a primitive type");
    }

    /**
     * Returns the name of the Java method to call to get the primitive value from an Object - e.g. intValue for java.lang.Integer
     *
     * @param type Type whose primitive method we want to lookup
     * @return The name of the method to use
     */
    private static String getPrimitiveMethod(final Class<?> type) {
        if (Integer.TYPE.equals(type)) {
            return "intValue";
        } else if (Boolean.TYPE.equals(type)) {
            return "booleanValue";
        } else if (Character.TYPE.equals(type)) {
            return "charValue";
        } else if (Byte.TYPE.equals(type)) {
            return "byteValue";
        } else if (Short.TYPE.equals(type)) {
            return "shortValue";
        } else if (Float.TYPE.equals(type)) {
            return "floatValue";
        } else if (Long.TYPE.equals(type)) {
            return "longValue";
        } else if (Double.TYPE.equals(type)) {
            return "doubleValue";
        }

        throw new IllegalStateException("Type: " + type.getCanonicalName() + " is not a primitive type");
    }

    /**
     * Gets the string to use for CHECKCAST instruction, returning the correct value for any type, including primitives and arrays
     *
     * @param returnType The type to cast to with CHECKCAST
     * @return CHECKCAST parameter
     */
    static String getCastType(final Class<?> returnType) {
        if (returnType.isPrimitive()) {
            return getWrapperType(returnType);
        } else {
            return getAsmTypeAsString(returnType, false);
        }
    }

    /**
     * Returns the wrapper type for a primitive, e.g. java.lang.Integer for int
     */
    private static String getWrapperType(final Class<?> type) {
        if (Integer.TYPE.equals(type)) {
            return Integer.class.getCanonicalName().replace('.', '/');
        } else if (Boolean.TYPE.equals(type)) {
            return Boolean.class.getCanonicalName().replace('.', '/');
        } else if (Character.TYPE.equals(type)) {
            return Character.class.getCanonicalName().replace('.', '/');
        } else if (Byte.TYPE.equals(type)) {
            return Byte.class.getCanonicalName().replace('.', '/');
        } else if (Short.TYPE.equals(type)) {
            return Short.class.getCanonicalName().replace('.', '/');
        } else if (Float.TYPE.equals(type)) {
            return Float.class.getCanonicalName().replace('.', '/');
        } else if (Long.TYPE.equals(type)) {
            return Long.class.getCanonicalName().replace('.', '/');
        } else if (Double.TYPE.equals(type)) {
            return Double.class.getCanonicalName().replace('.', '/');
        } else if (Void.TYPE.equals(type)) {
            return Void.class.getCanonicalName().replace('.', '/');
        }

        throw new IllegalStateException("Type: " + type.getCanonicalName() + " is not a primitive type");
    }

    /**
     * Invokes the most appropriate bytecode instruction to put a number on the stack
     */
    private static void pushIntOntoStack(final MethodVisitor mv, final int i) {
        if (i == 0) {
            mv.visitInsn(ICONST_0);
        } else if (i == 1) {
            mv.visitInsn(ICONST_1);
        } else if (i == 2) {
            mv.visitInsn(ICONST_2);
        } else if (i == 3) {
            mv.visitInsn(ICONST_3);
        } else if (i == 4) {
            mv.visitInsn(ICONST_4);
        } else if (i == 5) {
            mv.visitInsn(ICONST_5);
        } else if (i > 5 && i <= 255) {
            mv.visitIntInsn(BIPUSH, i);
        } else {
            mv.visitIntInsn(SIPUSH, i);
        }
    }

    /**
     * pushes an array of the specified size to the method visitor. The generated bytecode will leave
     * the new array at the top of the stack.
     *
     * @param mv   MethodVisitor to use
     * @param size Size of the array to create
     * @param type Type of array to create
     * @throws ProxyGenerationException
     */
    private static void createArrayDefinition(final MethodVisitor mv, final int size, final Class<?> type) throws ProxyGenerationException {
        // create a new array of java.lang.class (2)

        if (size < 0) {
            throw new ProxyGenerationException("Array size cannot be less than zero");
        }

        pushIntOntoStack(mv, size);

        mv.visitTypeInsn(ANEWARRAY, type.getCanonicalName().replace('.', '/'));
    }

    static String getMethodSignatureAsString(final Class<?> returnType, final Class<?>[] parameterTypes) {
        final StringBuilder builder = new StringBuilder();
        builder.append("(");
        for (final Class<?> parameterType : parameterTypes) {
            builder.append(getAsmTypeAsString(parameterType, true));
        }

        builder.append(")");
        builder.append(getAsmTypeAsString(returnType, true));

        return builder.toString();
    }

    /**
     * Returns the single letter that matches the given primitive in bytecode instructions
     */
    private static String getPrimitiveLetter(final Class<?> type) {
        if (Integer.TYPE.equals(type)) {
            return "I";
        } else if (Void.TYPE.equals(type)) {
            return "V";
        } else if (Boolean.TYPE.equals(type)) {
            return "Z";
        } else if (Character.TYPE.equals(type)) {
            return "C";
        } else if (Byte.TYPE.equals(type)) {
            return "B";
        } else if (Short.TYPE.equals(type)) {
            return "S";
        } else if (Float.TYPE.equals(type)) {
            return "F";
        } else if (Long.TYPE.equals(type)) {
            return "J";
        } else if (Double.TYPE.equals(type)) {
            return "D";
        }

        throw new IllegalStateException("Type: " + type.getCanonicalName() + " is not a primitive type");
    }

    /**
     * Converts a class to a String suitable for ASM.
     *
     * @param parameterType Class to convert
     * @param wrap          True if a non-array object should be wrapped with L and ; - e.g. Ljava/lang/Integer;
     * @return String to use for ASM
     */
    public static String getAsmTypeAsString(final Class<?> parameterType, final boolean wrap) {
        if (parameterType.isArray()) {
            if (parameterType.getComponentType().isPrimitive()) {
                final Class<?> componentType = parameterType.getComponentType();
                return "[" + getPrimitiveLetter(componentType);
            } else {
                return "[" + getAsmTypeAsString(parameterType.getComponentType(), true);
            }
        } else {
            if (parameterType.isPrimitive()) {
                return getPrimitiveLetter(parameterType);
            } else {
                String className = parameterType.getCanonicalName();

                if (parameterType.isMemberClass()) {
                    final int lastDot = className.lastIndexOf('.');
                    className = className.substring(0, lastDot) + "$" + className.substring(lastDot + 1);
                }

                if (wrap) {
                    return "L" + className.replace('.', '/') + ";";
                } else {
                    return className.replace('.', '/');
                }
            }
        }
    }

    static class NonBusinessHandler implements InvocationHandler, Serializable {

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            throw new EJBException("Calling non-public methods of a local bean without any interfaces is not allowed");
        }

    }

    /**
     * The methods of this class model sun.misc.Unsafe which is used reflectively
     */
    public static class Unsafe {

        // sun.misc.Unsafe
        private static final Object unsafe;
        private static final Method unsafeDefineClass;
        private static final Method allocateInstance;
        private static final Method putObject;
        private static final Method objectFieldOffset;

        static {
            final Class<?> unsafeClass;
            try {
                unsafeClass = AccessController.doPrivileged(new PrivilegedAction<Class<?>>() {
                    @Override
                    public Class<?> run() {
                        try {
                            return Thread.currentThread().getContextClassLoader().loadClass("sun.misc.Unsafe");
                        } catch (final Exception e) {
                            try {
                                return ClassLoader.getSystemClassLoader().loadClass("sun.misc.Unsafe");
                            } catch (final ClassNotFoundException e1) {
                                throw new IllegalStateException("Cannot get sun.misc.Unsafe", e);
                            }
                        }
                    }
                });
            } catch (final Exception e) {
                throw new IllegalStateException("Cannot get sun.misc.Unsafe class", e);
            }

            unsafe = AccessController.doPrivileged(new PrivilegedAction<Object>() {
                @Override
                public Object run() {
                    try {
                        final Field field = unsafeClass.getDeclaredField("theUnsafe");
                        field.setAccessible(true);
                        return field.get(null);
                    } catch (final Exception e) {
                        throw new IllegalStateException("Cannot get sun.misc.Unsafe", e);
                    }
                }
            });
            allocateInstance = AccessController.doPrivileged(new PrivilegedAction<Method>() {
                @Override
                public Method run() {
                    try {
                        final Method mtd = unsafeClass.getDeclaredMethod("allocateInstance", Class.class);
                        mtd.setAccessible(true);
                        return mtd;
                    } catch (final Exception e) {
                        throw new IllegalStateException("Cannot get sun.misc.Unsafe.allocateInstance", e);
                    }
                }
            });
            objectFieldOffset = AccessController.doPrivileged(new PrivilegedAction<Method>() {
                @Override
                public Method run() {
                    try {
                        final Method mtd = unsafeClass.getDeclaredMethod("objectFieldOffset", Field.class);
                        mtd.setAccessible(true);
                        return mtd;
                    } catch (final Exception e) {
                        throw new IllegalStateException("Cannot get sun.misc.Unsafe.objectFieldOffset", e);
                    }
                }
            });
            putObject = AccessController.doPrivileged(new PrivilegedAction<Method>() {
                @Override
                public Method run() {
                    try {
                        final Method mtd = unsafeClass.getDeclaredMethod("putObject", Object.class, long.class, Object.class);
                        mtd.setAccessible(true);
                        return mtd;
                    } catch (final Exception e) {
                        throw new IllegalStateException("Cannot get sun.misc.Unsafe.putObject", e);
                    }
                }
            });
            unsafeDefineClass = AccessController.doPrivileged(new PrivilegedAction<Method>() {
                @Override
                public Method run() {
                    try {
                        final Method mtd = unsafeClass.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class, ClassLoader.class, ProtectionDomain.class);
                        mtd.setAccessible(true);
                        return mtd;
                    } catch (final Exception e) {
                        LOGGER.debug("Unsafe's defineClass not available, will use classloader's defineClass");
                        return null;
                    }
                }
            });
        }

        public static Object allocateInstance(final Class clazz) {
            try {
                return allocateInstance.invoke(unsafe, clazz);
            } catch (final IllegalAccessException e) {
                throw new IllegalStateException("Failed to allocateInstance of Proxy class " + clazz.getName(), e);
            } catch (final InvocationTargetException e) {
                final Throwable throwable = e.getTargetException() != null ? e.getTargetException() : e;
                throw new IllegalStateException("Failed to allocateInstance of Proxy class " + clazz.getName(), throwable);
            }
        }

        private static void setValue(final Field field, final Object object, final Object value) {
            final long offset;
            try {
                offset = (Long) objectFieldOffset.invoke(unsafe, field);
            } catch (final Exception e) {
                throw new IllegalStateException("Failed getting offset for: field=" + field.getName() + "  class=" + field.getDeclaringClass().getName(), e);
            }

            try {
                putObject.invoke(unsafe, object, offset, value);
            } catch (final Exception e) {
                throw new IllegalStateException("Failed putting field=" + field.getName() + "  class=" + field.getDeclaringClass().getName(), e);
            }
        }

        // it is super important to pass a classloader as first parameter otherwise if API class is in a "permanent" classloader then it will leak
        public static Class defineClass(final ClassLoader loader, final Class<?> clsToProxy, final String proxyName, final byte[] proxyBytes) throws IllegalAccessException, InvocationTargetException {
            if (unsafeDefineClass != null) {
                return (Class<?>) unsafeDefineClass.invoke(unsafe, proxyName, proxyBytes, 0, proxyBytes.length, loader, clsToProxy.getProtectionDomain());
            } else {
                return (Class) getClassLoaderDefineClassMethod(loader).invoke(loader, proxyName, proxyBytes, 0, proxyBytes.length, clsToProxy.getProtectionDomain());
            }
        }

        private static Method getClassLoaderDefineClassMethod(ClassLoader classLoader) {
            Class<?> clazz = classLoader.getClass();
            Method defineClassMethod = null;
            do {
                try {
                    defineClassMethod = clazz.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class, ProtectionDomain.class);
                } catch (NoSuchMethodException e) {
                    // do nothing, we need to search the superclass
                }
                clazz = clazz.getSuperclass();
            } while (defineClassMethod == null && clazz != Object.class);

            if (defineClassMethod != null && !defineClassMethod.isAccessible()) {
                defineClassMethod.setAccessible(true);
            }
            return defineClassMethod;
        }

    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    private @interface Proxy {

    }
}

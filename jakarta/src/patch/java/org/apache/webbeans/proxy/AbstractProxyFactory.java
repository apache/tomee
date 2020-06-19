/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.proxy;

import static org.apache.xbean.asm7.ClassReader.SKIP_CODE;
import static org.apache.xbean.asm7.ClassReader.SKIP_DEBUG;
import static org.apache.xbean.asm7.ClassReader.SKIP_FRAMES;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.exception.ProxyGenerationException;
import org.apache.webbeans.exception.WebBeansException;
import org.apache.webbeans.spi.DefiningClassService;
import org.apache.xbean.asm7.ClassReader;
import org.apache.xbean.asm7.ClassWriter;
import org.apache.xbean.asm7.MethodVisitor;
import org.apache.xbean.asm7.Opcodes;
import org.apache.xbean.asm7.Type;
import org.apache.xbean.asm7.shade.commons.EmptyVisitor;

/**
 * Base class for all OWB Proxy factories
 */
public abstract class AbstractProxyFactory
{
    public static final int MAX_CLASSLOAD_TRIES = 10000;

    /**
     * This is needed as the Modifier#VARARGS is not (yet) public.
     * Note that the bitcode is the same as Modifier#TRANSIENT.
     * But 'varargs' is only for methods, whereas 'transient' is only for fields.
     */
    public static final int MODIFIER_VARARGS = 0x00000080;

    protected final Unsafe unsafe;

    private final DefiningClassService definingService;

    protected WebBeansContext webBeansContext;

    private final int javaVersion;


    /**
     * The name of the field which stores the passivationID of the Bean this proxy serves.
     * This is needed in case the proxy gets de-serialized back into a JVM
     * which didn't have this bean loaded yet.
     */
    public static final String FIELD_BEAN_PASSIVATION_ID = "owbBeanPassivationId";


    protected AbstractProxyFactory(WebBeansContext webBeansContext)
    {
        this.webBeansContext = webBeansContext;
        javaVersion = determineDefaultJavaVersion();
        unsafe = new Unsafe();
        definingService = webBeansContext.getService(DefiningClassService.class);
    }

    private int determineDefaultJavaVersion()
    {
        String javaVersionProp = webBeansContext.getOpenWebBeansConfiguration().getGeneratorJavaVersion();
        if (javaVersionProp == null)  // try to align on the runtime
        {
            javaVersionProp = System.getProperty("java.version");
        }
        if (javaVersionProp != null)
        {
            if (javaVersionProp.startsWith("1.8"))
            {
                return Opcodes.V1_8;
            }
            else if (javaVersionProp.startsWith("9") || javaVersionProp.startsWith("1.9"))
            {
                return Opcodes.V9;
            }
            else if (javaVersionProp.startsWith("10"))
            {
                return Opcodes.V10;
            }
            else if (javaVersionProp.startsWith("11"))
            {
                return Opcodes.V11;
            }
            else if (javaVersionProp.startsWith("12"))
            {
                return Opcodes.V12;
            }
            else if (javaVersionProp.startsWith("13"))
            {
                return Opcodes.V13;
            }
            else
            {
                try
                {
                    final int i = Integer.parseInt(javaVersionProp);
                    if (i > 13)
                    {
                        return Opcodes.V13 + (i - 13);
                    }
                }
                catch (final NumberFormatException nfe)
                {
                    // let's default
                }
            }
        }

        // the fallback is the lowest one to ensure it supports all possible classes of current environments
        return Opcodes.V1_8;
    }


    protected ClassLoader getProxyClassLoader(Class<?> beanClass)
    {
        if (definingService != null)
        {
            return definingService.getProxyClassLoader(beanClass);
        }
        return webBeansContext.getApplicationBoundaryService().getBoundaryClassLoader(beanClass);
    }

    /**
     * @return the marker interface which should be used for this proxy.
     */
    protected abstract Class getMarkerInterface();

    /**
     * generate the bytecode for creating the instance variables of the class
     */
    protected abstract void createInstanceVariables(ClassWriter cw, Class<?> classToProxy, String classFileName);

    /**
     * generate the bytecode for serialization.
     */
    protected abstract void createSerialisation(ClassWriter cw, String proxyClassFileName, Class<?> classToProxy, String classFileName);

    /**
     * Each of our interceptor/decorator proxies has exactly 1 constructor
     * which invokes the super ct + sets the delegation field.
     *
     * @param cw
     * @param classToProxy
     * @param classFileName
     * @throws org.apache.webbeans.exception.ProxyGenerationException
     */
    protected abstract void createConstructor(ClassWriter cw, String proxyClassFileName, Class<?> classToProxy, String classFileName, Constructor<?> injectConstructor)
            throws ProxyGenerationException;

    /**
     * generate the bytecode for invoking all intercepted methods
     */
    protected abstract void delegateInterceptedMethods(ClassLoader classLoader, ClassWriter cw, String proxyClassFileName, Class<?> classToProxy, Method[] interceptedMethods)
            throws ProxyGenerationException;

    /**
     * generate the bytecode for invoking all non-intercepted methods
     */
    protected abstract void delegateNonInterceptedMethods(ClassLoader classLoader, ClassWriter cw, String proxyClassFileName, Class<?> classToProxy, Method[] noninterceptedMethods)
            throws ProxyGenerationException;

    /**
     * Detect a free classname based on the given one
     * @param proxyClassName
     * @return
     */
    protected String getUnusedProxyClassName(ClassLoader classLoader, String proxyClassName)
    {
        proxyClassName = fixPreservedPackages(proxyClassName);

        String finalName = proxyClassName;

        for (int i = 0; i < MAX_CLASSLOAD_TRIES; i++)
        {
            try
            {
                finalName = proxyClassName + i;
                Class.forName(finalName, true, classLoader);
            }
            catch (ClassNotFoundException cnfe)
            {
                // this is exactly what we need!
                return finalName;
            }
            // otherwise we continue ;)
        }

        throw new WebBeansException("Unable to detect a free proxy class name based on: " + proxyClassName);
    }

    protected  <T> String getSignedClassProxyName(final Class<T> classToProxy)
    {
        // avoid java.lang.SecurityException: class's signer information
        // does not match signer information of other classes in the same package
        return "org.apache.webbeans.custom.signed." + classToProxy.getName();
    }

    protected String fixPreservedPackages(String proxyClassName)
    {
        proxyClassName = fixPreservedPackage(proxyClassName, "java.");
        proxyClassName = fixPreservedPackage(proxyClassName, "javax.");
        proxyClassName = fixPreservedPackage(proxyClassName, "jakarta.");
        proxyClassName = fixPreservedPackage(proxyClassName, "sun.misc.");

        return proxyClassName;
    }
    /**
     * Detect if the provided className is in the forbidden package.
     * If so, move it to org.apache.webbeans.custom.
     * @param forbiddenPackagePrefix including the '.', e.g. 'javax.'
     */
    private String fixPreservedPackage(String className, String forbiddenPackagePrefix)
    {
        String fixedClassName = className;

        if (className.startsWith(forbiddenPackagePrefix))
        {
            fixedClassName = "org.apache.webbeans.custom." + className.substring(forbiddenPackagePrefix.length());
        }

        return fixedClassName;
    }

    protected <T> Class<T> createProxyClass(ClassLoader classLoader, String proxyClassName, Class<T> classToProxy,
                                            Method[] interceptedMethods, Method[] nonInterceptedMethods)
            throws ProxyGenerationException
    {
        return createProxyClass(classLoader, proxyClassName, classToProxy, interceptedMethods, nonInterceptedMethods, null);
    }

    /**
     * @param classLoader to use for creating the class in
     * @param classToProxy the class for which a subclass will get generated
     * @param interceptedMethods the list of intercepted or decorated business methods.
     * @param nonInterceptedMethods all methods which are <b>not</b> intercepted nor decorated and shall get delegated directly
     * @param <T>
     * @return the proxy class
     */
    protected <T> Class<T> createProxyClass(ClassLoader classLoader, String proxyClassName, Class<T> classToProxy,
                                                      Method[] interceptedMethods, Method[] nonInterceptedMethods,
                                                      Constructor<T> constructor)
            throws ProxyGenerationException
    {
        String proxyClassFileName = proxyClassName.replace('.', '/');

        byte[] proxyBytes = generateProxy(classLoader,
                classToProxy,
                proxyClassName,
                proxyClassFileName,
                sortOutDuplicateMethods(interceptedMethods),
                sortOutDuplicateMethods(nonInterceptedMethods),
                constructor);

        if (definingService != null)
        {
            return definingService.defineAndLoad(proxyClassName, proxyBytes, classToProxy);
        }
        return unsafe.defineAndLoadClass(classLoader, proxyClassName, proxyBytes);
    }

    private Method[] sortOutDuplicateMethods(Method[] methods)
    {
        if (methods == null || methods.length == 0)
        {
            return null;
        }

        ArrayList<Method> duplicates = new ArrayList<>();

        for (Method outer : methods)
        {
            for (Method inner : methods)
            {
                if (inner != outer
                        && hasSameSignature(outer, inner)
                        && !(duplicates.contains(outer) || duplicates.contains(inner)))
                {
                    duplicates.add(inner);
                }
            }
        }

        ArrayList<Method> outsorted = new ArrayList<>(Arrays.asList(methods));
        outsorted.removeAll(duplicates);
        return outsorted.toArray(new Method[outsorted.size()]);
    }

    private boolean hasSameSignature(Method a, Method b)
    {
        return a.getName().equals(b.getName())
                && a.getReturnType().equals(b.getReturnType())
                && Arrays.equals(a.getParameterTypes(), b.getParameterTypes());
    }

    private byte[] generateProxy(ClassLoader classLoader, Class<?> classToProxy, String proxyClassName, String proxyClassFileName,
                                 Method[] interceptedMethods, Method[] nonInterceptedMethods, Constructor<?> constructor)
            throws ProxyGenerationException
    {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        String classFileName = classToProxy.getName().replace('.', '/');

        String[] interfaceNames = {Type.getInternalName(getMarkerInterface())};
        String superClassName = classFileName;

        if (classToProxy.isInterface())
        {
            interfaceNames = new String[]{Type.getInternalName(classToProxy), interfaceNames[0]};
            superClassName = Type.getInternalName(Object.class);
        }

        cw.visit(findJavaVersion(classToProxy), Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER + Opcodes.ACC_SYNTHETIC, proxyClassFileName, null, superClassName, interfaceNames);
        cw.visitSource(classFileName + ".java", null);

        createInstanceVariables(cw, classToProxy, classFileName);
        createSerialisation(cw, proxyClassFileName, classToProxy, classFileName);



        // create a static String Field which contains the passivationId of the Bean or null if not PassivationCapable
        cw.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC,
                FIELD_BEAN_PASSIVATION_ID, Type.getDescriptor(String.class), null, null).visitEnd();

        createConstructor(cw, proxyClassFileName, classToProxy, classFileName, constructor);


        if (nonInterceptedMethods != null)
        {
            delegateNonInterceptedMethods(classLoader, cw, proxyClassFileName, classToProxy, nonInterceptedMethods);
        }

        if (interceptedMethods != null)
        {
            delegateInterceptedMethods(classLoader, cw, proxyClassFileName, classToProxy, interceptedMethods);
        }

        return cw.toByteArray();
    }

    private int findJavaVersion(final Class<?> from)
    {
        final String resource = from.getName().replace('.', '/') + ".class";
        try (final InputStream stream = from.getClassLoader().getResourceAsStream(resource))
        {
            if (stream == null)
            {
                return javaVersion;
            }
            final ClassReader reader = new ClassReader(stream);
            final VersionVisitor visitor = new VersionVisitor();
            reader.accept(visitor, SKIP_DEBUG + SKIP_CODE + SKIP_FRAMES);
            if (visitor.version != 0)
            {
                return visitor.version;
            }
        }
        catch (final Exception e)
        {
            // no-op
        }
        // mainly for JVM classes - outside the classloader, find to fallback on the JVM version
        return javaVersion;
    }



    protected boolean unproxyableMethod(Method delegatedMethod)
    {
        int modifiers = delegatedMethod.getModifiers();

        return (modifiers & (Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL | Modifier.NATIVE)) > 0 ||
               "finalize".equals(delegatedMethod.getName()) || delegatedMethod.isBridge();
    }

    /**
     * @return the wrapper type for a primitive, e.g. java.lang.Integer for int
     */
    protected String getWrapperType(Class<?> type)
    {
        if (Integer.TYPE.equals(type))
        {
            return Integer.class.getCanonicalName().replace('.', '/');
        }
        else if (Boolean.TYPE.equals(type))
        {
            return Boolean.class.getCanonicalName().replace('.', '/');
        }
        else if (Character.TYPE.equals(type))
        {
            return Character.class.getCanonicalName().replace('.', '/');
        }
        else if (Byte.TYPE.equals(type))
        {
            return Byte.class.getCanonicalName().replace('.', '/');
        }
        else if (Short.TYPE.equals(type))
        {
            return Short.class.getCanonicalName().replace('.', '/');
        }
        else if (Float.TYPE.equals(type))
        {
            return Float.class.getCanonicalName().replace('.', '/');
        }
        else if (Long.TYPE.equals(type))
        {
            return Long.class.getCanonicalName().replace('.', '/');
        }
        else if (Double.TYPE.equals(type))
        {
            return Double.class.getCanonicalName().replace('.', '/');
        }
        else if (Void.TYPE.equals(type))
        {
            return Void.class.getCanonicalName().replace('.', '/');
        }

        throw new IllegalStateException("Type: " + type.getCanonicalName() + " is not a primitive type");
    }

    /**
     * Returns the appropriate bytecode instruction to load a value from a variable to the stack
     *
     * @param type Type to load
     * @return Bytecode instruction to use
     */
    protected int getVarInsn(Class<?> type)
    {
        if (type.isPrimitive())
        {
            if (Integer.TYPE.equals(type))
            {
                return Opcodes.ILOAD;
            }
            else if (Boolean.TYPE.equals(type))
            {
                return Opcodes.ILOAD;
            }
            else if (Character.TYPE.equals(type))
            {
                return Opcodes.ILOAD;
            }
            else if (Byte.TYPE.equals(type))
            {
                return Opcodes.ILOAD;
            }
            else if (Short.TYPE.equals(type))
            {
                return Opcodes.ILOAD;
            }
            else if (Float.TYPE.equals(type))
            {
                return Opcodes.FLOAD;
            }
            else if (Long.TYPE.equals(type))
            {
                return Opcodes.LLOAD;
            }
            else if (Double.TYPE.equals(type))
            {
                return Opcodes.DLOAD;
            }
        }

        throw new IllegalStateException("Type: " + type.getCanonicalName() + " is not a primitive type");
    }

    /**
     * Invokes the most appropriate bytecode instruction to put a number on the stack
     *
     * @param mv
     * @param i
     */
    protected void pushIntOntoStack(MethodVisitor mv, int i)
    {
        if (i == 0)
        {
            mv.visitInsn(Opcodes.ICONST_0);
        }
        else if (i == 1)
        {
            mv.visitInsn(Opcodes.ICONST_1);
        }
        else if (i == 2)
        {
            mv.visitInsn(Opcodes.ICONST_2);
        }
        else if (i == 3)
        {
            mv.visitInsn(Opcodes.ICONST_3);
        }
        else if (i == 4)
        {
            mv.visitInsn(Opcodes.ICONST_4);
        }
        else if (i == 5)
        {
            mv.visitInsn(Opcodes.ICONST_5);
        }
        else if (i > 5 && i <= 255)
        {
            mv.visitIntInsn(Opcodes.BIPUSH, i);
        }
        else
        {
            mv.visitIntInsn(Opcodes.SIPUSH, i);
        }
    }

    /**
     * Gets the appropriate bytecode instruction for RETURN, according to what type we need to return
     *
     * @param type Type the needs to be returned
     * @return The matching bytecode instruction
     */
    protected int getReturnInsn(Class<?> type)
    {
        if (type.isPrimitive())
        {
            if (Void.TYPE.equals(type))
            {
                return Opcodes.RETURN;
            }
            if (Integer.TYPE.equals(type))
            {
                return Opcodes.IRETURN;
            }
            else if (Boolean.TYPE.equals(type))
            {
                return Opcodes.IRETURN;
            }
            else if (Character.TYPE.equals(type))
            {
                return Opcodes.IRETURN;
            }
            else if (Byte.TYPE.equals(type))
            {
                return Opcodes.IRETURN;
            }
            else if (Short.TYPE.equals(type))
            {
                return Opcodes.IRETURN;
            }
            else if (Float.TYPE.equals(type))
            {
                return Opcodes.FRETURN;
            }
            else if (Long.TYPE.equals(type))
            {
                return Opcodes.LRETURN;
            }
            else if (Double.TYPE.equals(type))
            {
                return Opcodes.DRETURN;
            }
        }

        return Opcodes.ARETURN;
    }

    /**
     * Gets the string to use for CHECKCAST instruction, returning the correct value for any type, including primitives and arrays
     *
     * @param returnType The type to cast to with CHECKCAST
     * @return CHECKCAST parameter
     */
    protected String getCastType(Class<?> returnType)
    {
        if (returnType.isPrimitive())
        {
            return getWrapperType(returnType);
        }
        else
        {
            return Type.getInternalName(returnType);
        }
    }

    /**
     * Returns the name of the Java method to call to get the primitive value from an Object - e.g. intValue for java.lang.Integer
     *
     * @param type Type whose primitive method we want to lookup
     * @return The name of the method to use
     */
    protected String getPrimitiveMethod(Class<?> type)
    {
        if (Integer.TYPE.equals(type))
        {
            return "intValue";
        }
        else if (Boolean.TYPE.equals(type))
        {
            return "booleanValue";
        }
        else if (Character.TYPE.equals(type))
        {
            return "charValue";
        }
        else if (Byte.TYPE.equals(type))
        {
            return "byteValue";
        }
        else if (Short.TYPE.equals(type))
        {
            return "shortValue";
        }
        else if (Float.TYPE.equals(type))
        {
            return "floatValue";
        }
        else if (Long.TYPE.equals(type))
        {
            return "longValue";
        }
        else if (Double.TYPE.equals(type))
        {
            return "doubleValue";
        }

        throw new IllegalStateException("Type: " + type.getCanonicalName() + " is not a primitive type");
    }

    protected void generateReturn(MethodVisitor mv, Method delegatedMethod)
    {
        Class<?> returnType = delegatedMethod.getReturnType();
        mv.visitInsn(getReturnInsn(returnType));
    }

    /**
     * Create an Object[] parameter which contains all the parameters of the currently invoked method
     * and store this array for use in the call stack.
     * @param mv
     * @param parameterTypes
     */
    protected void pushMethodParameterArray(MethodVisitor mv, Class<?>[] parameterTypes)
    {
        // need to construct the array of objects passed in
        // create the Object[]
        createArrayDefinition(mv, parameterTypes.length, Object.class);

        int index = 1;
        // push parameters into array
        for (int i = 0; i < parameterTypes.length; i++)
        {
            // keep copy of array on stack
            mv.visitInsn(Opcodes.DUP);

            Class<?> parameterType = parameterTypes[i];

            // push number onto stack
            pushIntOntoStack(mv, i);

            if (parameterType.isPrimitive())
            {
                String wrapperType = getWrapperType(parameterType);
                mv.visitVarInsn(getVarInsn(parameterType), index);

                mv.visitMethodInsn(Opcodes.INVOKESTATIC, wrapperType, "valueOf",
                        "(" + Type.getDescriptor(parameterType) + ")L" + wrapperType + ";", false);
                mv.visitInsn(Opcodes.AASTORE);

                if (Long.TYPE.equals(parameterType) || Double.TYPE.equals(parameterType))
                {
                    index += 2;
                }
                else
                {
                    index++;
                }
            }
            else
            {
                mv.visitVarInsn(Opcodes.ALOAD, index);
                mv.visitInsn(Opcodes.AASTORE);
                index++;
            }
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
    protected void createArrayDefinition(MethodVisitor mv, int size, Class<?> type)
            throws ProxyGenerationException
    {
        // create a new array of java.lang.class (2)

        if (size < 0)
        {
            throw new ProxyGenerationException("Array size cannot be less than zero");
        }

        pushIntOntoStack(mv, size);

        mv.visitTypeInsn(Opcodes.ANEWARRAY, type.getCanonicalName().replace('.', '/'));
    }


    private static class VersionVisitor extends EmptyVisitor
    {
        private int version;

        @Override
        public void visit(final int version, final int access, final String name,
                          final String signature, final String superName, final String[] interfaces)
        {
            this.version = version;
        }
    }
}
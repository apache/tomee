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
package org.apache.openejb.util.proxy;

import org.apache.webbeans.spi.DefiningClassService;
import org.apache.webbeans.spi.InstantiatingClassService;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;

public class ClassDefiner implements DefiningClassService, InstantiatingClassService {
    private static final Method CLASS_LOADER_DEFINE_CLASS;
    private static final Method GET_MODULE;
    private static final Method CAN_READ;
    private static final Method ADD_READS;
    private static final Method PRIVATE_LOOKUP_IN;
    private static final Method DEFINE_CLASS;

    static {
        Method classLoaderDefineClass = null;
        try {
            final java.lang.reflect.Method method = ClassLoader.class.getDeclaredMethod(
                    "defineClass", String.class, byte[].class, int.class, int.class, ProtectionDomain.class);
            method.setAccessible(true);
            classLoaderDefineClass = method;
        } catch (final Exception ex) {
            // Ignore
        }
        CLASS_LOADER_DEFINE_CLASS = classLoaderDefineClass;

        Method getModule = null;
        Method canRead = null;
        Method addReads = null;
        Method privateLookupIn = null;
        Method defineClass = null;
        try {
            getModule = Class.class.getMethod("getModule");
            final Class<?> moduleClass = getModule.getReturnType();
            canRead = moduleClass.getMethod("canRead", moduleClass);
            addReads = moduleClass.getMethod("addReads", moduleClass);
            privateLookupIn = MethodHandles.class.getMethod("privateLookupIn", Class.class, MethodHandles.Lookup.class);
            defineClass = MethodHandles.Lookup.class.getMethod("defineClass", byte[].class);
        } catch (final Exception ex) {
            // Ignore
        }
        GET_MODULE = getModule;
        CAN_READ = canRead;
        ADD_READS = addReads;
        PRIVATE_LOOKUP_IN = privateLookupIn;
        DEFINE_CLASS = defineClass;
    }

    // needs to be public because OpenWebBeans will instanciate it as a service to call the
    // implmemented methods at the bottom of the class
    public ClassDefiner() {
        // no-op
    }

    public static boolean isClassLoaderDefineClass() {
        return CLASS_LOADER_DEFINE_CLASS != null;
    }

    public static Class<?> defineClass(final ClassLoader loader, final String className, final byte[] b,
                                       final Class<?> originalClass, final ProtectionDomain protectionDomain) {
        if (isClassLoaderDefineClass()) {
            return defineClassClassLoader(loader, className, b, originalClass, protectionDomain);
        } else {
            return defineClassMethodHandles(loader, className, b, originalClass, protectionDomain);
        }
    }

    /**
     * Adapted from http://asm.ow2.org/doc/faq.html#Q5
     */
    static Class<?> defineClassClassLoader(final ClassLoader loader, final String className, final byte[] b,
                                           final Class<?> originalClass, final ProtectionDomain protectionDomain) {
        try {
            return (Class<?>) CLASS_LOADER_DEFINE_CLASS.invoke(
                    loader, className, b, Integer.valueOf(0), Integer.valueOf(b.length), protectionDomain);
        } catch (final Exception e) {
            throw e instanceof RuntimeException ? ((RuntimeException) e) : new RuntimeException(e);
        }
    }

    /**
     * Implementation based on MethodHandles.Lookup.
     */
    static Class<?> defineClassMethodHandles(final ClassLoader loader, final String className, final byte[] b,
                                             final Class<?> originalClass, final ProtectionDomain protectionDomain) {
        try {
            final Object thisModule = GET_MODULE.invoke(LocalBeanProxyFactory.class);
            final Object lookupClassModule = GET_MODULE.invoke(originalClass);
            if (!(boolean) CAN_READ.invoke(thisModule, lookupClassModule)) {
                // we need to read the other module in order to have privateLookup access
                // see javadoc for MethodHandles.privateLookupIn()
                ADD_READS.invoke(thisModule, lookupClassModule);
            }
            final Object lookup = PRIVATE_LOOKUP_IN.invoke(null, originalClass, MethodHandles.lookup());
            return (Class<?>) DEFINE_CLASS.invoke(lookup, b);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T allocateProxy(final Class<T> proxyClass) {
        // let's still use Unsafe for the moment so we avoid calling the constructor
        return (T) LocalBeanProxyFactory.Unsafe.allocateInstance(proxyClass);
    }

    @Override
    public ClassLoader getProxyClassLoader(final Class<?> forClass) {
        final ClassLoader classLoader = forClass.getClassLoader();
        if (classLoader == null) {
            return Thread.currentThread().getContextClassLoader();
        }
        return classLoader;
    }

    @Override
    public <T> Class<T> defineAndLoad(final String name, final byte[] bytecode, final Class<T> proxiedClass) {
        return (Class<T>) defineClass(getProxyClassLoader(proxiedClass),
                                      name,
                                      bytecode,
                                      proxiedClass,
                                      proxiedClass.getProtectionDomain());
    }

    @Override
    public <T> T newInstance(final Class<? extends T> proxyClass) {
        return allocateProxy(proxyClass);
    }
}
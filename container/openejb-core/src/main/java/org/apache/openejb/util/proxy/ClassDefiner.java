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

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;

public class ClassDefiner
{
    private static final Method CLASS_LOADER_DEFINE_CLASS;
    private static final Method GET_MODULE;
    private static final Method CAN_READ;
    private static final Method ADD_READS;
    private static final Method PRIVATE_LOOKUP_IN;
    private static final Method DEFINE_CLASS;

    static
    {
        Method classLoaderDefineClass = null;
        try
        {
            java.lang.reflect.Method method = ClassLoader.class.getDeclaredMethod(
                    "defineClass", String.class, byte[].class, int.class, int.class, ProtectionDomain.class);
            method.setAccessible(true);
            classLoaderDefineClass = method;
        }
        catch (Exception ex)
        {
            // Ignore
        }
        CLASS_LOADER_DEFINE_CLASS = classLoaderDefineClass;

        Method getModule = null;
        Method canRead = null;
        Method addReads = null;
        Method privateLookupIn = null;
        Method defineClass = null;
        try
        {
            getModule = Class.class.getMethod("getModule");
            Class<?> moduleClass = getModule.getReturnType();
            canRead = moduleClass.getMethod("canRead", moduleClass);
            addReads = moduleClass.getMethod("addReads", moduleClass);
            privateLookupIn = MethodHandles.class.getMethod("privateLookupIn", Class.class, MethodHandles.Lookup.class);
            defineClass = MethodHandles.Lookup.class.getMethod("defineClass", byte[].class);
        }
        catch (Exception ex)
        {
            // Ignore
        }
        GET_MODULE = getModule;
        CAN_READ = canRead;
        ADD_READS = addReads;
        PRIVATE_LOOKUP_IN = privateLookupIn;
        DEFINE_CLASS = defineClass;
    }

    private ClassDefiner()
    {

    }

    public static Class<?> defineClass(ClassLoader loader, String className, byte[] b,
                                       Class<?> originalClass, ProtectionDomain protectionDomain)
    {
        if (CLASS_LOADER_DEFINE_CLASS == null)
        {
            return defineClassMethodHandles(loader, className, b, originalClass, protectionDomain);
        }
        else
        {
            return defineClassClassLoader(loader, className, b, originalClass, protectionDomain);
        }
    }
    /**
     * Adapted from http://asm.ow2.org/doc/faq.html#Q5
     *
     * @param b
     *
     * @return Class<?>
     */
    static Class<?> defineClassClassLoader(ClassLoader loader, String className, byte[] b,
                                           Class<?> originalClass, ProtectionDomain protectionDomain)
    {
        try
        {
            return (Class<?>) CLASS_LOADER_DEFINE_CLASS.invoke(
                    loader, className, b, Integer.valueOf(0), Integer.valueOf(b.length), protectionDomain);
        }
        catch (Exception e)
        {
            throw e instanceof RuntimeException ? ((RuntimeException) e) : new RuntimeException(e);
        }
    }

    /**
     * Implementation based on MethodHandles.Lookup.
     *
     * @return Class<?>
     */
    static Class<?> defineClassMethodHandles(ClassLoader loader, String className, byte[] b,
                                             Class<?> originalClass, ProtectionDomain protectionDomain)
    {
        try
        {
            Object thisModule = GET_MODULE.invoke(LocalBeanProxyFactory.class);
            Object lookupClassModule = GET_MODULE.invoke(originalClass);
            if (!(boolean) CAN_READ.invoke(thisModule, lookupClassModule))
            {
                // we need to read the other module in order to have privateLookup access
                // see javadoc for MethodHandles.privateLookupIn()
                ADD_READS.invoke(thisModule, lookupClassModule);
            }
            Object lookup = PRIVATE_LOOKUP_IN.invoke(null, originalClass, MethodHandles.lookup());
            return (Class<?>) DEFINE_CLASS.invoke(lookup, b);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
//    static Class<?> defineClassMethodHandles(ClassLoader loader, String className, byte[] b,
//                                Class<?> originalClass, ProtectionDomain protectionDomain)
//    {
//        Module thisModule = AsmDeltaSpikeProxyClassGenerator.class.getModule();
//        try
//        {
//            Module lookupClassModule = originalClass.getModule();
//            if (!thisModule.canRead(lookupClassModule))
//            {
//                // we need to read the other module in order to have privateLookup access
//                // see javadoc for MethodHandles.privateLookupIn()
//                thisModule.addReads(lookupClassModule);
//            }
//            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(originalClass, MethodHandles.lookup());
//            return lookup.defineClass(b);
//        }
//        catch (IllegalAccessException e)
//        {
//            throw new RuntimeException(e);
//        }
//    }
}
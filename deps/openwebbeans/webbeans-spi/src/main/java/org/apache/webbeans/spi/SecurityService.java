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
package org.apache.webbeans.spi;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.Principal;
import java.security.PrivilegedActionException;
import java.util.Properties;

/**
 * <p>The SecurityService SPI provides support for all kinds
 * of JavaEE related security mechanism.</p>
 * <p>There are by default 2 basically different implementations
 * provided by OpenWebBeans. One version performs all underlying
 * class invocations via {@link java.security.AccessController#doPrivileged}
 * which is intended for use in Java EE servers. The 2nd version directly
 * invokes the underlying Class methods without any AccessControler and is
 * intended for scenarios where no Java security mechanism needs to be used.
 * Since OpenWebBeans (as any other DI framework) is heavily based on
 * reflection, using the simple NoSecurityService leads to a way better
 * application performance.
 */
public interface SecurityService
{
    /**
     * Gets the current caller identity.
     * @return current caller identity or <code>null</code> if none provided.
     */
    public Principal getCurrentPrincipal();

    /**
     * @see Class#getDeclaredConstructor(Class[])
     */
    public <T> Constructor<T> doPrivilegedGetDeclaredConstructor(Class<T> clazz, Class<?>... parameterTypes);

    /**
     * @see Class#getDeclaredConstructors()
     */
    public <T> Constructor<?>[] doPrivilegedGetDeclaredConstructors(Class<T> clazz);

    /**
     * @see Class#getDeclaredMethod(String, Class[])
     */
    public <T> Method doPrivilegedGetDeclaredMethod(Class<T> clazz, String name, Class<?>... parameterTypes);

    /**
     * @see Class#getDeclaredMethods()
     */
    public <T> Method[] doPrivilegedGetDeclaredMethods(Class<T> clazz);

    /**
     * @see Class#getDeclaredField(String)
     */
    public <T> Field doPrivilegedGetDeclaredField(Class<T> clazz, String name);

    /**
     * @see Class#getDeclaredFields()
     */
    public <T> Field[] doPrivilegedGetDeclaredFields(Class<T> clazz);

    /**
     * @see AccessibleObject#setAccessible(boolean)
     */
    public void doPrivilegedSetAccessible(AccessibleObject obj, boolean flag);

    /**
     * @see AccessibleObject#isAccessible()
     */
    public boolean doPrivilegedIsAccessible(AccessibleObject obj);

    /**
     * @see Class#newInstance()
     */
    public <T> T doPrivilegedObjectCreate(Class<T> clazz)
    throws PrivilegedActionException, IllegalAccessException, InstantiationException;

    /**
     * @see Class#
     */
    public void doPrivilegedSetSystemProperty(String propertyName, String value);

    /**
     * @see System#getProperty(String, String)
     */
    public String doPrivilegedGetSystemProperty(String propertyName, String defaultValue);

    /**
     * @see System#getProperties()
     */
    public Properties doPrivilegedGetSystemProperties();

}

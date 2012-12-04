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
package org.apache.webbeans.corespi.security;

import org.apache.webbeans.spi.SecurityService;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.Principal;
import java.security.PrivilegedActionException;
import java.util.Properties;

/**
 * A version of the {@link SecurityService} which directly invokes
 * the underlying Class methods instead of using a SecurityManager.
 * This version is activated by default and intended for JavaSE and
 * non EE-Server use.
 */
public class SimpleSecurityService implements SecurityService
{
    /**
     * @return always <code>null</code> in the default implementation
     */
    public Principal getCurrentPrincipal()
    {
        return null;
    }

    public <T> Constructor<T> doPrivilegedGetDeclaredConstructor(Class<T> clazz, Class<?>... parameterTypes)
    {
        try
        {
            return clazz.getDeclaredConstructor(parameterTypes);
        }
        catch (NoSuchMethodException e)
        {
            return null;
        }
    }

    public <T> Constructor<?>[] doPrivilegedGetDeclaredConstructors(Class<T> clazz)
    {
        return clazz.getDeclaredConstructors();
    }

    public <T> Method doPrivilegedGetDeclaredMethod(Class<T> clazz, String name, Class<?>... parameterTypes)
    {
        try
        {
            return clazz.getDeclaredMethod(name, parameterTypes);
        }
        catch (NoSuchMethodException e)
        {
            return null;
        }
    }

    public <T> Method[] doPrivilegedGetDeclaredMethods(Class<T> clazz)
    {
        return clazz.getDeclaredMethods();
    }

    public <T> Field doPrivilegedGetDeclaredField(Class<T> clazz, String name)
    {
        try
        {
            return clazz.getDeclaredField(name);
        }
        catch (NoSuchFieldException e)
        {
            return null;
        }
    }

    public <T> Field[] doPrivilegedGetDeclaredFields(Class<T> clazz)
    {
        return clazz.getDeclaredFields();
    }

    public void doPrivilegedSetAccessible(AccessibleObject obj, boolean flag)
    {
        obj.setAccessible(flag);
    }

    public boolean doPrivilegedIsAccessible(AccessibleObject obj)
    {
        return obj.isAccessible();
    }

    public <T> T doPrivilegedObjectCreate(Class<T> clazz)
    throws PrivilegedActionException, IllegalAccessException, InstantiationException
    {
        return clazz.newInstance();
    }

    public void doPrivilegedSetSystemProperty(String propertyName, String value)
    {
        System.setProperty(propertyName, value);
    }

    public String doPrivilegedGetSystemProperty(String propertyName, String defaultValue)
    {
        return System.getProperty(propertyName, defaultValue);
    }

    public Properties doPrivilegedGetSystemProperties()
    {
        return System.getProperties();
    }
}

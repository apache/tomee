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


package org.apache.openejb.cdi;

import org.apache.openejb.loader.SystemInstance;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.exception.WebBeansException;
import org.apache.webbeans.spi.SecurityService;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.*;
import java.util.Properties;

/**
 * This is a copy of the owb ManagedSecurityService with the getPrincipal method implemented as in the owb OpenEJBSecurityService.
 * This version of the {@link org.apache.webbeans.spi.SecurityService} uses the java.lang.SecurityManager
 * to check low level access to the underlying functions via doPriviliged blocks.
 *
 * The most secure way is to just copy the source over to your own class and configure
 * it in openwebbeans.properties. This way you can add whatever security features
 * you like to use.
 */
public class ManagedSecurityService implements SecurityService
{
    private static final int METHOD_CLASS_GETDECLAREDCONSTRUCTOR = 0x01;

    private static final int METHOD_CLASS_GETDECLAREDCONSTRUCTORS = 0x02;

    private static final int METHOD_CLASS_GETDECLAREDMETHOD = 0x03;

    private static final int METHOD_CLASS_GETDECLAREDMETHODS = 0x04;

    private static final int METHOD_CLASS_GETDECLAREDFIELD = 0x05;

    private static final int METHOD_CLASS_GETDECLAREDFIELDS = 0x06;

    private static final PrivilegedActionGetSystemProperties SYSTEM_PROPERTY_ACTION = new PrivilegedActionGetSystemProperties();

    public ManagedSecurityService()
    {
        // we need to make sure that only WebBeansContext gets used to create us!
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        // in the Sun Java VM-1.6 the parent ct is alwasys entry [6]
        // but we cannot rely on that because it might differ for
        // other VMs.
        boolean isCalledFromWebBeansContext = false;
        for (int i = 3; i < 20; i++)
        {
            String declaringClass = stackTrace[i].getClassName();
            String methodName = stackTrace[i].getMethodName();
            if (declaringClass.equals(WebBeansContext.class.getName()) &&
                methodName.equals("<init>"))
            {
                isCalledFromWebBeansContext = true;
                break;
            }
        }
        if (!isCalledFromWebBeansContext)
        {
            throw new SecurityException("ManagedSecurityService must directly get created by WebBeansContext!");
        }

        // we also need to make sure that this very class didn't get subclassed
        // to prevent man in the middle attacks
        if (this.getClass() != ManagedSecurityService.class)
        {
            throw new SecurityException("ManagedSecurityService must not get subclassed!");
        }
    }

    @Override
    public Principal getCurrentPrincipal()
    {
        org.apache.openejb.spi.SecurityService<?> service = SystemInstance.get().getComponent(org.apache.openejb.spi.SecurityService.class);
        if(service != null)
        {
            return service.getCallerPrincipal();
        }

        return null;
    }

    @Override
    public <T> Constructor<T> doPrivilegedGetDeclaredConstructor(Class<T> clazz, Class<?>... parameterTypes)
    {
        Object obj = AccessController.doPrivileged(
                new PrivilegedActionForClass(clazz, parameterTypes, METHOD_CLASS_GETDECLAREDCONSTRUCTOR));
        if (obj instanceof NoSuchMethodException)
        {
            return null;
        }
        return (Constructor<T>)obj;
    }

    @Override
    public <T> Constructor<?>[] doPrivilegedGetDeclaredConstructors(Class<T> clazz)
    {
        Object obj = AccessController.doPrivileged(
                new PrivilegedActionForClass(clazz, null, METHOD_CLASS_GETDECLAREDCONSTRUCTORS));
        return (Constructor<T>[])obj;
    }

    @Override
    public <T> Method doPrivilegedGetDeclaredMethod(Class<T> clazz, String name, Class<?>... parameterTypes)
    {
        Object obj = AccessController.doPrivileged(
                new PrivilegedActionForClass(clazz, new Object[] {name, parameterTypes}, METHOD_CLASS_GETDECLAREDMETHOD));
        if (obj instanceof NoSuchMethodException)
        {
            return null;
        }
        return (Method)obj;
    }

    @Override
    public <T> Method[] doPrivilegedGetDeclaredMethods(Class<T> clazz)
    {
        Object obj = AccessController.doPrivileged(
                new PrivilegedActionForClass(clazz, null, METHOD_CLASS_GETDECLAREDMETHODS));
        return (Method[])obj;
    }

    @Override
    public <T> Field doPrivilegedGetDeclaredField(Class<T> clazz, String name)
    {
        Object obj = AccessController.doPrivileged(
                new PrivilegedActionForClass(clazz, name, METHOD_CLASS_GETDECLAREDFIELD));
        if (obj instanceof NoSuchFieldException)
        {
            return null;
        }
        return (Field)obj;
    }

    @Override
    public <T> Field[] doPrivilegedGetDeclaredFields(Class<T> clazz)
    {
        Object obj = AccessController.doPrivileged(
                new PrivilegedActionForClass(clazz, null, METHOD_CLASS_GETDECLAREDFIELDS));
        return (Field[])obj;
    }

    @Override
    public void doPrivilegedSetAccessible(AccessibleObject obj, boolean flag)
    {
        AccessController.doPrivileged(new PrivilegedActionForSetAccessible(obj, flag));
    }

    @Override
    public boolean doPrivilegedIsAccessible(AccessibleObject obj)
    {
        return (Boolean) AccessController.doPrivileged(new PrivilegedActionForIsAccessible(obj));
    }

    @Override
    public <T> T doPrivilegedObjectCreate(Class<T> clazz) throws PrivilegedActionException, IllegalAccessException, InstantiationException
    {
        return (T) AccessController.doPrivileged(new PrivilegedActionForObjectCreation(clazz));
    }

    @Override
    public void doPrivilegedSetSystemProperty(String propertyName, String value)
    {
        AccessController.doPrivileged(new PrivilegedActionForSetProperty(propertyName, value));
    }

    @Override
    public String doPrivilegedGetSystemProperty(String propertyName, String defaultValue)
    {
        return AccessController.doPrivileged(new PrivilegedActionForProperty(propertyName, defaultValue));
    }

    @Override
    public Properties doPrivilegedGetSystemProperties()
    {
        return AccessController.doPrivileged(SYSTEM_PROPERTY_ACTION);
    }


    // the following block contains internal wrapper classes for doPrivileged actions

    protected static class PrivilegedActionForClass implements PrivilegedAction<Object>
    {
        private Class<?> clazz;

        private Object parameters;

        private int method;

        protected PrivilegedActionForClass(Class<?> clazz, Object parameters, int method)
        {
            this.clazz = clazz;
            this.parameters = parameters;
            this.method = method;
        }

        public Object run()
        {
            try
            {
                switch (method)
                {
                    case METHOD_CLASS_GETDECLAREDCONSTRUCTOR:
                        return clazz.getDeclaredConstructor((Class<?>[])parameters);
                    case METHOD_CLASS_GETDECLAREDCONSTRUCTORS:
                        return clazz.getDeclaredConstructors();
                    case METHOD_CLASS_GETDECLAREDMETHOD:
                        String name = (String)((Object[])parameters)[0];
                        Class<?>[] realParameters = (Class<?>[])((Object[])parameters)[1];
                        return clazz.getDeclaredMethod(name, realParameters);
                    case METHOD_CLASS_GETDECLAREDMETHODS:
                        return clazz.getDeclaredMethods();
                    case METHOD_CLASS_GETDECLAREDFIELD:
                        return clazz.getDeclaredField((String)parameters);
                    case METHOD_CLASS_GETDECLAREDFIELDS:
                        return clazz.getDeclaredFields();

                    default:
                        return new WebBeansException("unknown security method: " + method);
                }
            }
            catch (Exception exception)
            {
                return exception;
            }
        }

    }

    protected static class PrivilegedActionForSetAccessible implements PrivilegedAction<Object>
    {

        private AccessibleObject object;

        private boolean flag;

        protected PrivilegedActionForSetAccessible(AccessibleObject object, boolean flag)
        {
            this.object = object;
            this.flag = flag;
        }

        public Object run()
        {
            object.setAccessible(flag);
            return null;
        }
    }

    protected static class PrivilegedActionForIsAccessible implements PrivilegedAction<Object>
    {

        private AccessibleObject object;

        protected PrivilegedActionForIsAccessible(AccessibleObject object)
        {
            this.object = object;
        }

        public Object run()
        {
            return object.isAccessible();
        }
    }

    protected static class PrivilegedActionForProperty implements PrivilegedAction<String>
    {
        private final String propertyName;

        private final String defaultValue;

        protected PrivilegedActionForProperty(String propertyName, String defaultValue)
        {
            this.propertyName = propertyName;
            this.defaultValue = defaultValue;
        }

        @Override
        public String run()
        {
            return System.getProperty(this.propertyName,this.defaultValue);
        }

    }

    protected static class PrivilegedActionForSetProperty implements PrivilegedAction<Object>
    {
        private final String propertyName;

        private final String value;

        protected PrivilegedActionForSetProperty(String propertyName, String value)
        {
            this.propertyName = propertyName;
            this.value = value;
        }

        @Override
        public String run()
        {
            System.setProperty(propertyName, value);
            return null;
        }

    }

    protected static class PrivilegedActionGetSystemProperties implements PrivilegedAction<Properties>
    {

        @Override
        public Properties run()
        {
            return System.getProperties();
        }

    }

    protected static class PrivilegedActionForObjectCreation implements PrivilegedExceptionAction<Object>
    {
        private Class<?> clazz;

        protected PrivilegedActionForObjectCreation(Class<?> clazz)
        {
            this.clazz = clazz;
        }

        @Override
        public Object run() throws Exception
        {
            try
            {
                return clazz.newInstance();
            }
            catch (InstantiationException e)
            {
                throw e;
            }
            catch (IllegalAccessException e)
            {
                throw e;
            }
        }

    }


}

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
import org.apache.openejb.spi.SecurityService;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.Principal;
import java.security.PrivilegedActionException;
import java.util.Properties;

public class ManagedSecurityService implements org.apache.webbeans.spi.SecurityService {
    private final org.apache.webbeans.corespi.security.ManagedSecurityService delegate = new org.apache.webbeans.corespi.security.ManagedSecurityService();

    @Override
    public Principal getCurrentPrincipal() {
        final SecurityService<?> service = SystemInstance.get().getComponent(org.apache.openejb.spi.SecurityService.class);
        if(service != null) {
            return service.getCallerPrincipal();
        }
        return null;
    }

    @Override
    public <T> Constructor<T> doPrivilegedGetDeclaredConstructor(final Class<T> clazz, final Class<?>... parameterTypes) {
        return delegate.doPrivilegedGetDeclaredConstructor(clazz, parameterTypes);
    }

    @Override
    public <T> Constructor<?>[] doPrivilegedGetDeclaredConstructors(final Class<T> clazz) {
        return delegate.doPrivilegedGetDeclaredConstructors(clazz);
    }

    @Override
    public <T> Method doPrivilegedGetDeclaredMethod(final Class<T> clazz, final String name, final Class<?>... parameterTypes) {
        return delegate.doPrivilegedGetDeclaredMethod(clazz, name, parameterTypes);
    }

    @Override
    public <T> Method[] doPrivilegedGetDeclaredMethods(final Class<T> clazz) {
        return delegate.doPrivilegedGetDeclaredMethods(clazz);
    }

    @Override
    public <T> Field doPrivilegedGetDeclaredField(final Class<T> clazz, final String name) {
        return delegate.doPrivilegedGetDeclaredField(clazz, name);
    }

    @Override
    public <T> Field[] doPrivilegedGetDeclaredFields(final Class<T> clazz) {
        return delegate.doPrivilegedGetDeclaredFields(clazz);
    }

    @Override
    public void doPrivilegedSetAccessible(final AccessibleObject obj, final boolean flag) {
        delegate.doPrivilegedSetAccessible(obj, flag);
    }

    @Override
    public boolean doPrivilegedIsAccessible(final AccessibleObject obj) {
        return delegate.doPrivilegedIsAccessible(obj);
    }

    @Override
    public <T> T doPrivilegedObjectCreate(final Class<T> clazz) throws PrivilegedActionException, IllegalAccessException, InstantiationException {
        return delegate.doPrivilegedObjectCreate(clazz);
    }

    @Override
    public void doPrivilegedSetSystemProperty(final String propertyName, final String value) {
        delegate.doPrivilegedSetSystemProperty(propertyName, value);
    }

    @Override
    public String doPrivilegedGetSystemProperty(final String propertyName, final String defaultValue) {
        return delegate.doPrivilegedGetSystemProperty(propertyName, propertyName);
    }

    @Override
    public Properties doPrivilegedGetSystemProperties() {
        return delegate.doPrivilegedGetSystemProperties();
    }
}

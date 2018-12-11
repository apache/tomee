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
import org.apache.openejb.util.Join;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.webbeans.config.WebBeansContext;

import java.lang.reflect.*;
import java.security.Principal;
import java.security.PrivilegedActionException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class ManagedSecurityService implements org.apache.webbeans.spi.SecurityService {

    private final org.apache.webbeans.corespi.security.ManagedSecurityService delegate = new org.apache.webbeans.corespi.security.ManagedSecurityService();

    private final Logger logger = Logger.getInstance(LogCategory.OPENEJB_CDI, ManagedSecurityService.class.getName());

    private final boolean useWrapper;
    private Principal proxy = null;


    public ManagedSecurityService(final WebBeansContext context) {
        useWrapper = (!Boolean.parseBoolean(context.getOpenWebBeansConfiguration()
                .getProperty("org.apache.webbeans.component.PrincipalBean.proxy", "true").trim()));

        if (useWrapper) {
            final ClassLoader loader = ManagedSecurityService.class.getClassLoader();

            final String[] apiInterfaces = context.getOpenWebBeansConfiguration()
                    .getProperty("org.apache.webbeans.component.PrincipalBean.proxyApis", "org.eclipse.microprofile.jwt.JsonWebToken").split(",");

            List<Class> interfaceList = new ArrayList<>();
            List<String> notFoundInterfaceList = new ArrayList<>();

            for (final String apiInterface : apiInterfaces) {
                try {
                    final Class<?> clazz = loader.loadClass(apiInterface.trim());
                    interfaceList.add(clazz);

                } catch (NoClassDefFoundError | ClassNotFoundException e) {
                    notFoundInterfaceList.add(apiInterface);
                }
            }

            if (!notFoundInterfaceList.isEmpty()) {
                logger.info("Some Principal APIs could not be loaded: {0} out of {1} not found",
                        Join.join(",", notFoundInterfaceList),
                        Join.join(",", Arrays.asList(apiInterfaces)));
            }

            // not sure if we should do that, or simply check if we can't load the classes before
            // and then skip the proxy creation and set the useWrapper to false.
            if (interfaceList.isEmpty()) {
                interfaceList.add(java.security.Principal.class);
            }

            proxy = Principal.class.cast(Proxy.newProxyInstance(loader, interfaceList.toArray(new Class[0]), new InvocationHandler() {
                @Override
                public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                    return method.invoke(doGetPrincipal(), args);
                }
            }));
        }
    }

    @Override
    public Principal getCurrentPrincipal() {
        if (useWrapper) {
            return proxy;
        }

        return doGetPrincipal();
    }

    private Principal doGetPrincipal() {
        final SecurityService<?> service = SystemInstance.get().getComponent(SecurityService.class);
        if (service != null) {
            return service.getCallerPrincipal();
        }

        return null;
    }

    @Override
    public <T> Constructor<T> doPrivilegedGetDeclaredConstructor(final Class<T> clazz, final Class<?>... parameterTypes) {
        return delegate.doPrivilegedGetDeclaredConstructor(clazz, parameterTypes);
    }

    @Override
    public <T> Constructor<T> doPrivilegedGetConstructor(final Class<T> clazz, final Class<?>... parameterTypes) {
        return delegate.doPrivilegedGetConstructor(clazz, parameterTypes);
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

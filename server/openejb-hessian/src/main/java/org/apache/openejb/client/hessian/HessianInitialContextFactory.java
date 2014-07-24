/*
 *     Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.apache.openejb.client.hessian;

import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.core.ivm.naming.ContextWrapper;
import org.apache.openejb.util.reflection.Reflections;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import java.lang.reflect.Constructor;
import java.util.Hashtable;

// all is done by reflection to let the use provide hessian in the app
public class HessianInitialContextFactory implements InitialContextFactory {
    public static final String API = "openejb.hessian.client.api";
    public static final String FORCE_SERIALIZABLE = "openejb.hessian.client.force-serializable";
    public static final String CHUNKED = "openejb.hessian.client.chunked";
    public static final String DEBUG = "openejb.hessian.client.debug";
    public static final String READ_TIMEOUT = "openejb.hessian.client.read-timeout";
    public static final String CONNECT_TIMEOUT = "openejb.hessian.client.connect-timeout";

    private static final Class<?>[] BOOLEAN_PARAM = new Class<?>[]{boolean.class};
    private static final Class<?>[] LONG_PARAM = new Class<?>[]{long.class};
    private static final Class<?>[] STRING_PARAM = new Class<?>[]{String.class};
    private static final Class<?>[] CREATE_PARAM = new Class<?>[]{Class.class, String.class, ClassLoader.class};

    @Override
    public Context getInitialContext(final Hashtable<?, ?> environment) throws NamingException {
        return new HessianContext(environment);
    }

    private static class HessianContext extends ContextWrapper {
        private final Hashtable<?, ?> environment;
        private final ClassLoader loader;
        private final String url;
        private final Class<?> api;
        private final boolean allowNonSerializable;
        private final String user;
        private final String password;
        private final boolean chunked;
        private final boolean debug;
        private final int readTimeout;
        private final int connectTimeout;
        private final Constructor<?> factoryConstructor;
        private final Constructor<?> serializerConstructor;

        public HessianContext(final Hashtable<?, ?> environment) {
            super(null); // will lead to NPE if used but shouldn't be used in practise
            this.environment = environment;

            String baseUrl = String.class.cast(environment.get(Context.PROVIDER_URL));
            if (baseUrl == null) {
                throw new IllegalArgumentException("provider url should be set");
            }
            if (!baseUrl.endsWith("/")) {
                baseUrl += '/';
            }

            this.url = baseUrl;

            this.loader = Thread.currentThread().getContextClassLoader();
            this.user = String.class.cast(environment.get(Context.SECURITY_PRINCIPAL));
            this.password = String.class.cast(environment.get(Context.SECURITY_CREDENTIALS));
            this.allowNonSerializable = environment.get(FORCE_SERIALIZABLE) == null || !"true".equals(String.class.cast(environment.get(FORCE_SERIALIZABLE)));
            this.chunked = environment.get(CHUNKED) == null || "true".equals(String.class.cast(environment.get(CHUNKED)));
            this.debug = "true".equals(String.class.cast(environment.get(DEBUG)));
            this.readTimeout = environment.get(READ_TIMEOUT) == null ? -1 : Integer.parseInt(String.class.cast(environment.get(READ_TIMEOUT)));
            this.connectTimeout = environment.get(CONNECT_TIMEOUT) == null ? -1 : Integer.parseInt(String.class.cast(environment.get(CONNECT_TIMEOUT)));

            final String apiClassname = String.class.cast(environment.get(API));
            if (apiClassname != null) {
                try {
                    api = loader.loadClass(apiClassname);
                } catch (final ClassNotFoundException e) {
                    throw new OpenEJBRuntimeException(e);
                }
            } else {
                api = null;
            }

            try {
                factoryConstructor = loader.loadClass("com.caucho.hessian.client.HessianProxyFactory").getConstructor(ClassLoader.class);
                serializerConstructor = loader.loadClass("com.caucho.hessian.io.SerializerFactory").getConstructor(ClassLoader.class);
            } catch (final Exception e) {
                throw new OpenEJBRuntimeException(e);
            }
        }

        @Override
        public Object lookup(final Name name) throws NamingException {
            return lookup(name.toString());
        }

        @Override
        public Object lookup(final String name) throws NamingException {
            try {
                final Object clientFactory = factoryConstructor.newInstance(loader);
                final Object factory = serializerConstructor.newInstance(loader);
                Reflections.invokeByReflection(factory, "setAllowNonSerializable", BOOLEAN_PARAM, new Object[]{allowNonSerializable});
                Reflections.invokeByReflection(clientFactory, "setSerializerFactory", new Class<?>[]{serializerConstructor.getDeclaringClass()}, new Object[]{factory});
                if (user != null) {
                    Reflections.invokeByReflection(clientFactory, "setUser", STRING_PARAM, new Object[]{user});
                    Reflections.invokeByReflection(clientFactory, "setPassword", STRING_PARAM, new Object[]{password});
                }
                Reflections.invokeByReflection(clientFactory, "setChunkedPost", BOOLEAN_PARAM, new Object[]{chunked});
                Reflections.invokeByReflection(clientFactory, "setDebug", BOOLEAN_PARAM, new Object[]{debug});
                Reflections.invokeByReflection(clientFactory, "setReadTimeout", LONG_PARAM, new Object[]{readTimeout});
                Reflections.invokeByReflection(clientFactory, "setConnectTimeout", LONG_PARAM, new Object[]{connectTimeout});

                final String completeUrl = url + name;
                try {
                    if (api != null) { // just use it
                        return Reflections.invokeByReflection(clientFactory, "create", CREATE_PARAM, new Object[]{api, completeUrl, loader});
                    }

                    return Reflections.invokeByReflection(clientFactory, "create", STRING_PARAM, new Object[]{completeUrl});
                } catch (final Exception e) {
                    throw new NamingException(e.getMessage());
                }
            } catch (final Exception e) {
                throw new OpenEJBRuntimeException(e);
            }


        }

        @Override
        public Hashtable<?, ?> getEnvironment() throws NamingException {
            return environment;
        }
    }
}

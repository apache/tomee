/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
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
package org.apache.openejb.server.cxf.rs;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.apache.cxf.jaxrs.lifecycle.PerRequestResourceProvider;
import org.apache.cxf.jaxrs.utils.InjectionUtils;
import org.apache.cxf.jaxrs.utils.ResourceUtils;
import org.apache.cxf.message.Message;
import org.apache.openejb.Injection;
import org.apache.openejb.InjectionProcessor;
import org.apache.openejb.OpenEJBException;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.inject.OWBInjector;

// the constructor part is mainly copied from the parent since all is private
// and we want to invoke postconstrut ourself
public class OpenEJBPerRequestPojoResourceProvider extends PerRequestResourceProvider {
    protected Collection<Injection> injections;
    protected Context context;
    protected WebBeansContext webbeansContext;
    protected InjectionProcessor<Object> injector;
    protected OWBInjector beanInjector;
    protected Constructor<?> constructor;

    public OpenEJBPerRequestPojoResourceProvider(final Class<?> clazz, final Collection<Injection> injectionCollection, final Context initialContext, final WebBeansContext owbCtx) {
        super(clazz);
        injections = injectionCollection;
        webbeansContext = owbCtx;
        context = (Context) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{Context.class}, new InitialContextWrapper(initialContext));

        constructor = ResourceUtils.findResourceConstructor(clazz, true);
        if (constructor == null) {
            throw new RuntimeException("Resource class " + clazz
                    + " has no valid constructor");
        }
    }

    @Override
    protected Object createInstance(Message m) {
        Object[] values = ResourceUtils.createConstructorArguments(constructor, m);
        try {
            final Object instance = values.length > 0 ? constructor.newInstance(values) : constructor.newInstance(new Object[]{});
            injector = new InjectionProcessor<Object>(instance, new ArrayList<Injection>(injections), InjectionProcessor.unwrap(context));
            injector.createInstance();
            try {
                beanInjector = new OWBInjector(webbeansContext);
                beanInjector.inject(injector.getInstance());
            } catch (Throwable t) {
                // ignored
            }
            injector.postConstruct();
            return injector.getInstance();
        } catch (InstantiationException ex) {
            final String msg = "Resource class " + constructor.getDeclaringClass().getName() + " can not be instantiated";
            throw new WebApplicationException(Response.serverError().entity(msg).build());
        } catch (IllegalAccessException ex) {
            final String msg = "Resource class " + constructor.getDeclaringClass().getName() + " can not be instantiated"
                    + " due to IllegalAccessException";
            throw new WebApplicationException(Response.serverError().entity(msg).build());
        } catch (InvocationTargetException ex) {
            final String msg = "Resource class "
                    + constructor.getDeclaringClass().getName() + " can not be instantiated"
                    + " due to InvocationTargetException";
            throw new WebApplicationException(Response.serverError().entity(msg).build());
        } catch (OpenEJBException e) {
            final String msg = "An error occured injecting in class " + constructor.getDeclaringClass().getName();
            throw new WebApplicationException(Response.serverError().entity(msg).build());
        }
    }

    @Override
    public void releaseInstance(Message m, Object o) {
        try {
            if (beanInjector != null) {
                beanInjector.destroy();
            }
        } catch (Throwable t) {
            // ignored
        }
        if (injector != null) {
            injector.preDestroy();
        }
    }

    private static class InitialContextWrapper implements InvocationHandler {
        private Context ctx;

        public InitialContextWrapper(Context initialContext) {
            ctx = initialContext;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("lookup")) {
                final String name = "java:" + String.class.cast(args[0]);
                if (args[0].getClass().equals(String.class)) {
                    // Note: we catch exception instead of namingexception
                    // because in environment with proxies on Context
                    // InvocationtargetException can be thrown instead of NamingException
                    if (ctx != null) {
                        try {
                            return ctx.lookup(name);
                        } catch (Exception ne) {
                            try {
                                return ctx.lookup(String.class.cast(args[0]));
                            } catch (Exception ignored) {
                                // no-op
                            }
                        }
                    }
                    final Context initialContext = new InitialContext();
                    try {
                        return initialContext.lookup(name);
                    } catch (Exception swallowed) {
                        try {
                            return initialContext.lookup(String.class.cast(args[0]));
                        } catch (Exception ignored) {
                            // no-op
                        }
                    }
                }
            }
            return method.invoke(ctx, args);
        }
    }
}

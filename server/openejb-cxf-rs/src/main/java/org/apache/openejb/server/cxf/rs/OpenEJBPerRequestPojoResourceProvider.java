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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.ws.rs.WebApplicationException;
import org.apache.cxf.jaxrs.lifecycle.PerRequestResourceProvider;
import org.apache.cxf.message.Message;
import org.apache.openejb.Injection;
import org.apache.openejb.InjectionProcessor;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.inject.OWBInjector;

public class OpenEJBPerRequestPojoResourceProvider extends PerRequestResourceProvider {
    protected Collection<Injection> injections;
    protected Context context;
    protected WebBeansContext webbeansContext;

    public OpenEJBPerRequestPojoResourceProvider(final Class<?> clazz, final Collection<Injection> injectionCollection, final Context initialContext, final WebBeansContext owbCtx) {
        super(clazz);
        injections = injectionCollection;
        webbeansContext = owbCtx;
        context = (Context) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{Context.class}, new InitialContextWrapper(initialContext));
    }

    protected Object createInstance(Message m) {
        Object o = super.createInstance(m);
        try {
            final InjectionProcessor<?> injector = new InjectionProcessor<Object>(o, new ArrayList<Injection>(injections), InjectionProcessor.unwrap(context));
            injector.createInstance();
            try {
                final OWBInjector beanInjector = new OWBInjector(webbeansContext);
                beanInjector.inject(injector.getInstance());
            } catch (Throwable t) {
                // ignored
            }
            injector.postConstruct();
            return injector.getInstance();
        } catch (Exception e) {
            throw new WebApplicationException(e);
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

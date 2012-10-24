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

import org.apache.cxf.jaxrs.lifecycle.ResourceProvider;
import org.apache.cxf.jaxrs.utils.InjectionUtils;
import org.apache.cxf.jaxrs.utils.ResourceUtils;
import org.apache.cxf.message.Message;
import org.apache.openejb.Injection;
import org.apache.openejb.InjectionProcessor;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.rest.ThreadLocalContextManager;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.inject.AbstractInjectable;
import org.apache.webbeans.inject.OWBInjector;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.InjectionException;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

public class OpenEJBPerRequestPojoResourceProvider implements ResourceProvider {
    protected Collection<Injection> injections;
    protected Context context;
    protected WebBeansContext webbeansContext;

    protected Constructor<?> constructor;
    protected Method postConstructMethod;
    protected Method preDestroyMethod;

    private BeanCreator creator;

    public OpenEJBPerRequestPojoResourceProvider(final Class<?> clazz, final Collection<Injection> injectionCollection, final Context initialContext, final WebBeansContext owbCtx) {
        injections = injectionCollection;
        webbeansContext = owbCtx;
        context = (Context) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{Context.class}, new InitialContextWrapper(initialContext));

        constructor = ResourceUtils.findResourceConstructor(clazz, true);
        if (constructor == null) {
            throw new RuntimeException("Resource class " + clazz + " has no valid constructor");
        }
        postConstructMethod = ResourceUtils.findPostConstructMethod(clazz);
        preDestroyMethod = ResourceUtils.findPreDestroyMethod(clazz);
    }

    @Override
    public Object getInstance(Message m) {
        Contexts.bind(m.getExchange());

        final BeanManagerImpl bm = webbeansContext.getBeanManagerImpl();
        if (bm.isInUse()) {
            creator = new CdiBeanCreator(bm);
        } else { // do it manually
            creator = new DefaultBeanCreator(m);
        }

        try {
            return creator.create();
        } catch (NoBeanFoundException nbfe) {
            creator = new DefaultBeanCreator(m);
            return creator.create();
        }
    }

    @Override
    public void releaseInstance(final Message m, final Object o) {
        if (creator != null) {
            creator.release();
        }
        ThreadLocalContextManager.reset();
    }

    @Override
    public Class<?> getResourceClass() {
        return constructor.getDeclaringClass();
    }

    @Override
    public boolean isSingleton() {
        return false;
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

    private static interface BeanCreator {
        Object create();
        void release();
    }

    private class CdiBeanCreator implements BeanCreator {
        private BeanManager bm;
        private CreationalContext<?> toClean;

        public CdiBeanCreator(BeanManager bm) {
            this.bm = bm;
        }

        @Override
        public Object create() {
            final Class<?> clazz = constructor.getDeclaringClass();
            try {
                final Set<Bean<?>> beans = bm.getBeans(clazz);
                final Bean<?> bean = bm.resolve(beans);
                if (bean == null) {
                    throw new NoBeanFoundException();
                }

                toClean = bm.createCreationalContext(bean);

                try {
                    return bm.getReference(bean, clazz, toClean);
                } finally {
                    if (bm.isNormalScope(bean.getScope())) {
                        toClean = null; // will be released by the container
                    }
                }
            } catch (InjectionException ie) {
                final String msg = "Resource class " + constructor.getDeclaringClass().getName() + " can not be instantiated";
                throw new WebApplicationException(Response.serverError().entity(msg).build());
            }
        }

        @Override
        public void release() {
            if (toClean != null) {
                toClean.release();
            }
        }
    }

    private class DefaultBeanCreator implements BeanCreator {
        private Message m;
        private InjectionProcessor<?> injector;
        private CreationalContext creationalContext;
        private Object instance;

        public DefaultBeanCreator(Message m) {
            this.m = m;
        }

        @Override
        public Object create() {
            final Object[] values = ResourceUtils.createConstructorArguments(constructor, m);
            try {
                instance = constructor.newInstance(values);

                injector = new InjectionProcessor<Object>(instance, new ArrayList<Injection>(injections), InjectionProcessor.unwrap(context));
                instance = injector.createInstance();

                final BeanManager bm = webbeansContext.getBeanManagerImpl();
                creationalContext = bm.createCreationalContext(null);

                final Object oldValue = AbstractInjectable.instanceUnderInjection.get();
                AbstractInjectable.instanceUnderInjection.set(instance);
                try {
                    OWBInjector.inject(bm, instance, creationalContext);
                } catch (Exception e) {
                    // ignored
                } finally {
                    if (oldValue != null) {
                        AbstractInjectable.instanceUnderInjection.set(oldValue);
                    } else {
                        AbstractInjectable.instanceUnderInjection.remove();
                    }
                }

                // injector.postConstruct(); // it doesn't know it
                InjectionUtils.invokeLifeCycleMethod(instance, postConstructMethod);
                return instance;
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
        public void release() {
            // we can't give it to the injector so let's do it manually
            try {
                InjectionUtils.invokeLifeCycleMethod(instance, preDestroyMethod);
            } finally {
                if (injector != null) {
                    injector.preDestroy();
                }
                if (creationalContext != null) {
                    creationalContext.release();
                }
            }
        }
    }

    private static class NoBeanFoundException extends RuntimeException {
    }
}

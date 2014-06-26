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
import org.apache.webbeans.component.InjectionTargetBean;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.inject.OWBInjector;
import org.apache.webbeans.intercept.InterceptorResolutionService;
import org.apache.webbeans.portable.InjectionTargetImpl;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.InjectionException;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.Interceptor;
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
import java.util.HashSet;
import java.util.Set;

public class OpenEJBPerRequestPojoResourceProvider implements ResourceProvider {
    protected final Collection<Injection> injections;
    protected final Context context;
    protected final WebBeansContext webbeansContext;

    protected final Constructor<?> constructor;
    protected final Method postConstructMethod;
    protected final Method preDestroyMethod;
    protected final ClassLoader classLoader;

    private final Collection<Class<?>> contextTypes = new HashSet<Class<?>>();
    private final BeanManagerImpl bm;
    private final Bean<?> bean;
    private final BeanCreator normalScopeCreator;

    public OpenEJBPerRequestPojoResourceProvider(final ClassLoader loader, final Class<?> clazz, final Collection<Injection> injectionCollection, final Context initialContext, final WebBeansContext owbCtx) {
        injections = injectionCollection;
        webbeansContext = owbCtx;
        classLoader = loader;
        context = (Context) Proxy.newProxyInstance(classLoader, new Class<?>[]{Context.class}, new InitialContextWrapper(initialContext));

        constructor = ResourceUtils.findResourceConstructor(clazz, true);

        postConstructMethod = ResourceUtils.findPostConstructMethod(clazz);
        preDestroyMethod = ResourceUtils.findPreDestroyMethod(clazz);

        bm = webbeansContext.getBeanManagerImpl();
        if (bm.isInUse()) {
            try {
                final Set<Bean<?>> beans = bm.getBeans(clazz);
                bean = bm.resolve(beans);
            } catch (final InjectionException ie) {
                final String msg = "Resource class " + constructor.getDeclaringClass().getName() + " can not be instantiated";
                throw new WebApplicationException(Response.serverError().entity(msg).build());
            }

            if (bean instanceof InjectionTargetBean<?>) {
                final InterceptorResolutionService.BeanInterceptorInfo info = InjectionTargetImpl.class.cast(InjectionTargetBean.class.cast(bean).getInjectionTarget()).getInterceptorInfo();
                for (final Interceptor<?> interceptor : info.getCdiInterceptors()) {
                    if (interceptor == null || interceptor.getBeanClass() == null) {
                        continue;
                    }

                    Contexts.findContextFields(interceptor.getBeanClass(), contextTypes);
                }
                for (final Interceptor<?> interceptor : info.getEjbInterceptors()) {
                    if (interceptor == null || interceptor.getBeanClass() == null) {
                        continue;
                    }

                    Contexts.findContextFields(interceptor.getBeanClass(), contextTypes);
                }
                for (final Decorator<?> decorator : info.getDecorators()) {
                    if (decorator == null || decorator.getBeanClass() == null) {
                        continue;
                    }

                    Contexts.findContextFields(decorator.getBeanClass(), contextTypes);
                }
            }
            if (bean != null && bm.isNormalScope(bean.getScope())) {
                // singleton is faster
                normalScopeCreator = new ProvidedInstanceBeanCreator(bm.getReference(bean, bean.getBeanClass(), bm.createCreationalContext(bean)));
            } else {
                normalScopeCreator = null;
                validateConstructorExists(clazz);
            }
        } else {
            bean = null;
            normalScopeCreator = null;
            validateConstructorExists(clazz);
        }

        Contexts.findContextFields(clazz, contextTypes); // for the class itself
    }

    private void validateConstructorExists(final Class<?> clazz) {
        // only validate it here otherwise we'll fail for CDI injections
        if (constructor == null) {
            throw new RuntimeException("Resource class " + clazz + " has no valid constructor");
        }
    }

    @Override
    public Object getInstance(final Message m) {
        Contexts.bind(m.getExchange(), contextTypes);

        BeanCreator creator;
        if (bean != null) {
            if (normalScopeCreator != null) {
                creator = normalScopeCreator;
            } else {
                creator = new PseudoScopedCdiBeanCreator();
            }
        } else {
            creator = new DefaultBeanCreator(m);
        }
        m.put(BeanCreator.class, creator);

        // important to switch of classloader to get the right InitialContext
        final ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            return creator.create();
        } catch (final NoBeanFoundException nbfe) {
            creator = new DefaultBeanCreator(m);
            m.put(BeanCreator.class, creator);
            return creator.create();
        } finally {
            Thread.currentThread().setContextClassLoader(oldLoader);
        }
    }

    @Override // this method is not linked to o to consider it stateless
    public void releaseInstance(final Message m, final Object o) {
        final BeanCreator c = m.get(BeanCreator.class);
        if (c != null) {
            c.release();
        }
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
        private final Context ctx;

        public InitialContextWrapper(final Context initialContext) {
            ctx = initialContext;
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            if (method.getName().equals("lookup")) {
                final String name = "java:" + String.class.cast(args[0]);
                if (args[0].getClass().equals(String.class)) {
                    // Note: we catch exception instead of namingexception
                    // because in environment with proxies on Context
                    // InvocationtargetException can be thrown instead of NamingException
                    if (ctx != null) {
                        try {
                            return ctx.lookup(name);
                        } catch (final Exception ne) {
                            try {
                                return ctx.lookup(String.class.cast(args[0]));
                            } catch (final Exception ignored) {
                                // no-op
                            }
                        }
                    }
                    final Context initialContext = new InitialContext();
                    try {
                        return initialContext.lookup(name);
                    } catch (final Exception swallowed) {
                        try {
                            return initialContext.lookup(String.class.cast(args[0]));
                        } catch (final Exception ignored) {
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

    private class ProvidedInstanceBeanCreator implements BeanCreator {
        private final Object instance;

        private ProvidedInstanceBeanCreator(final Object instance) {
            this.instance = instance;
        }

        @Override
        public Object create() {
            try {
                return instance;
            } catch (final InjectionException ie) {
                final String msg = "Resource class " + constructor.getDeclaringClass().getName() + " can not be instantiated";
                throw new WebApplicationException(Response.serverError().entity(msg).build());
            }
        }

        @Override
        public void release() {
            // no-op
        }
    }

    private class PseudoScopedCdiBeanCreator implements BeanCreator {
        private CreationalContext<?> toClean = null;

        @Override
        public Object create() {
            try {
                toClean = bm.createCreationalContext(bean);
                return bm.getReference(bean, bean.getBeanClass(), toClean);
            } catch (final InjectionException ie) {
                final String msg = "Resource class " + constructor.getDeclaringClass().getName() + " can not be instantiated";
                throw new WebApplicationException(Response.serverError().entity(msg).build());
            }
        }

        @Override
        public void release() {
            toClean.release();
            toClean = null;
        }
    }

    private class DefaultBeanCreator implements BeanCreator {
        private final Message m;
        private InjectionProcessor<?> injector;
        private CreationalContext creationalContext;
        private Object instance;

        public DefaultBeanCreator(final Message m) {
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

                try {
                    OWBInjector.inject(bm, instance, creationalContext);
                } catch (final Exception e) {
                    // ignored
                }

                // injector.postConstruct(); // it doesn't know it
                InjectionUtils.invokeLifeCycleMethod(instance, postConstructMethod);
                return instance;
            } catch (final InstantiationException ex) {
                final String msg = "Resource class " + constructor.getDeclaringClass().getName() + " can not be instantiated";
                throw new WebApplicationException(Response.serverError().entity(msg).build());
            } catch (final IllegalAccessException ex) {
                final String msg = "Resource class " + constructor.getDeclaringClass().getName() + " can not be instantiated"
                        + " due to IllegalAccessException";
                throw new WebApplicationException(Response.serverError().entity(msg).build());
            } catch (final InvocationTargetException ex) {
                final String msg = "Resource class "
                        + constructor.getDeclaringClass().getName() + " can not be instantiated"
                        + " due to InvocationTargetException";
                throw new WebApplicationException(Response.serverError().entity(msg).build());
            } catch (final OpenEJBException e) {
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
        public NoBeanFoundException(final String name) {
            super(name);
        }
    }
}

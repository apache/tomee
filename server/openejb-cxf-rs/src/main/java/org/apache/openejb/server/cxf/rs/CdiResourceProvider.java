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
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.webbeans.component.InjectionTargetBean;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.inject.OWBInjector;
import org.apache.webbeans.intercept.InterceptorResolutionService;
import org.apache.webbeans.portable.InjectionTargetImpl;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.InjectionException;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Decorator;
import jakarta.enterprise.inject.spi.Interceptor;
import javax.naming.Context;
import javax.naming.InitialContext;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class CdiResourceProvider implements ResourceProvider {

    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB_RS, CdiResourceProvider.class);

    public static final String INSTANCE_KEY = CdiResourceProvider.class.getName() + ".instance";

    protected final Collection<Injection> injections;
    protected final Context context;
    protected final WebBeansContext webbeansContext;

    protected final Class<?> clazz;
    protected final Method postConstructMethod;
    protected final Method preDestroyMethod;
    protected final ClassLoader classLoader;

    protected final Collection<Class<?>> contextTypes = new HashSet<>();
    protected final BeanCreator normalScopeCreator;
    protected final BeanManagerImpl bm;
    protected final Bean<?> bean;
    protected Constructor<?> constructor;

    public CdiResourceProvider(final ClassLoader loader, final Class<?> clazz, final Collection<Injection> injectionCollection, final Context initialContext, final WebBeansContext owbCtx) {
        injections = injectionCollection;
        webbeansContext = owbCtx;
        classLoader = loader;
        context = (Context) Proxy.newProxyInstance(classLoader, new Class<?>[]{Context.class}, new InitialContextWrapper(initialContext));

        postConstructMethod = ResourceUtils.findPostConstructMethod(clazz);
        preDestroyMethod = ResourceUtils.findPreDestroyMethod(clazz);

        bm = webbeansContext == null ? null : webbeansContext.getBeanManagerImpl();
        this.clazz = clazz;
        if (bm != null && bm.isInUse()) {
            try {
                final Set<Bean<?>> beans = bm.getBeans(clazz);
                bean = bm.resolve(beans);
            } catch (final InjectionException ie) {
                final String msg = "Resource class " + clazz.getName() + " can not be instantiated";
                LOGGER.warning(msg, ie);
                throw new WebApplicationException(Response.serverError().entity(msg).build());
            }

            if (bean != null && bm.isNormalScope(bean.getScope())) {
                // singleton is faster
                normalScopeCreator = new ProvidedInstanceBeanCreator(bm.getReference(bean, bean.getBeanClass(), bm.createCreationalContext(bean)));
            } else {
                normalScopeCreator = null;
                validateConstructorExists();
            }
        } else {
            bean = null;
            normalScopeCreator = null;
            validateConstructorExists();
        }

        findContexts(clazz);
    }

    private void findContexts(final Class<?> clazz) {
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
        Contexts.findContextFields(clazz, contextTypes); // for the class itself
    }

    private void validateConstructorExists() {
        // only validate it here otherwise we'll fail for CDI injections
        constructor = ResourceUtils.findResourceConstructor(clazz, true);
        if (constructor == null) {
            final String message = "Resource class " + clazz + " has no valid constructor";
            LOGGER.warning(message);
            throw new RuntimeException(message);
        }
    }

    @Override
    public Object getInstance(final Message m) {
        final Object existing = m.getExchange().get(INSTANCE_KEY);
        if (existing != null) {
            return existing;
        }

        Contexts.bind(m.getExchange(), contextTypes);

        BeanCreator creator;
        if (bean != null) {
            if (normalScopeCreator != null) {
                creator = normalScopeCreator;
            } else {
                creator = new PseudoScopedCdiBeanCreator();
            }
        } else {
            creator = new DefaultBeanCreator(m, constructor);
        }
        m.put(BeanCreator.class, creator);
        m.put(CdiResourceProvider.class, this);

        // important to switch of classloader to get the right InitialContext
        final Thread thread = Thread.currentThread();
        final ClassLoader oldLoader = thread.getContextClassLoader();
        thread.setContextClassLoader(classLoader);
        Object instance;
        try {
            instance = creator.create();
        } catch (final NoBeanFoundException nbfe) {
            creator = new DefaultBeanCreator(m, constructor);
            m.put(BeanCreator.class, creator);
            instance = creator.create();
        } finally {
            thread.setContextClassLoader(oldLoader);
        }

        m.getExchange().put(INSTANCE_KEY, instance);

        return instance;
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
        return clazz;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
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
            try {
                return method.invoke(ctx, args);
            } catch (final InvocationTargetException ite) {
                throw ite.getCause();
            }
        }
    }

    protected interface BeanCreator {
        Object create();

        void release();
    }

    protected class ProvidedInstanceBeanCreator implements BeanCreator {
        private final Object instance;

        protected ProvidedInstanceBeanCreator(final Object instance) {
            this.instance = instance;
        }

        @Override
        public Object create() {
            return instance;
        }

        @Override
        public void release() {
            // no-op
        }
    }

    protected class PseudoScopedCdiBeanCreator implements BeanCreator {
        private CreationalContext<?> toClean = null;

        @Override
        public Object create() {
            try {
                toClean = bm.createCreationalContext(bean);
                return bm.getReference(bean, bean.getBeanClass(), toClean);
            } catch (final InjectionException ie) {
                final String msg = "Failed to instantiate: " + bean;
                Logger.getInstance(LogCategory.OPENEJB_CDI, this.getClass()).error(msg, ie);
                throw new WebApplicationException(Response.serverError().entity(msg).build());
            }
        }

        @Override
        public void release() {
            toClean.release();
            toClean = null;
        }
    }

    protected class DefaultBeanCreator implements BeanCreator {
        private final Message m;
        private final Constructor<?> constructor;
        private InjectionProcessor<?> injector;
        private CreationalContext creationalContext;
        private Object instance;

        public DefaultBeanCreator(final Message m, final Constructor<?> constructor) {
            this.m = m;
            this.constructor = constructor;
        }

        protected Object newInstance() throws InstantiationException, IllegalAccessException, InvocationTargetException {
            return constructor.newInstance(ResourceUtils.createConstructorArguments(constructor, m, m != null));
        }

        @Override
        public Object create() {
            try {
                instance = newInstance();
                doInit();
                return instance;
            } catch (final InstantiationException ex) {
                final String msg = "Resource class " + constructor.getDeclaringClass().getName() + " can not be instantiated";
                LOGGER.warning(msg, ex);
                throw new WebApplicationException(Response.serverError().entity(msg).build());
            } catch (final IllegalAccessException ex) {
                final String msg = "Resource class " + constructor.getDeclaringClass().getName() + " can not be instantiated"
                        + " due to IllegalAccessException";
                LOGGER.warning(msg, ex);
                throw new WebApplicationException(Response.serverError().entity(msg).build());
            } catch (final InvocationTargetException ex) {
                final String msg = "Resource class "
                        + constructor.getDeclaringClass().getName() + " can not be instantiated"
                        + " due to InvocationTargetException";
                LOGGER.warning(msg, ex);
                throw new WebApplicationException(Response.serverError().entity(msg).build());
            } catch (final OpenEJBException ex) {
                final String msg = "An error occured injecting in class " + constructor.getDeclaringClass().getName();
                LOGGER.warning(msg, ex);
                throw new WebApplicationException(Response.serverError().entity(msg).build());
            }
        }

        protected void doInit() throws OpenEJBException {
            injector = new InjectionProcessor<>(instance, new ArrayList<>(injections), InjectionProcessor.unwrap(context));
            instance = injector.createInstance();

            final BeanManager bm = webbeansContext == null ? null : webbeansContext.getBeanManagerImpl();
            if (bm != null) {
                creationalContext = bm.createCreationalContext(null);

                try {
                    OWBInjector.inject(bm, instance, creationalContext);
                } catch (final Exception e) {
                    // ignored
                }
            }

            // injector.postConstruct(); // it doesn't know it
            InjectionUtils.invokeLifeCycleMethod(instance, postConstructMethod);
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

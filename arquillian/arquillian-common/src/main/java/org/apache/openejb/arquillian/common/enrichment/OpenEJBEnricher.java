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
package org.apache.openejb.arquillian.common.enrichment;

import org.apache.openejb.AppContext;
import org.apache.openejb.BeanContext;
import org.apache.openejb.InjectionProcessor;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.arquillian.common.mockito.MockitoEnricher;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.WebContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.AppFinder;
import org.apache.webbeans.annotation.AnnotationManager;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.context.creational.CreationalContextImpl;
import org.apache.webbeans.inject.OWBInjector;
import org.apache.webbeans.portable.AnnotatedElementFactory;
import org.jboss.arquillian.test.spi.TestClass;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.Bean;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class OpenEJBEnricher {
    private static final Logger LOGGER = Logger.getLogger(OpenEJBEnricher.class.getName());

    private OpenEJBEnricher() {
        // no-op
    }

    public static void enrich(final Object testInstance, final AppContext appCtx) {
        // don't rely on arquillian since this enrichment should absolutely be done before the following ones
        new MockitoEnricher().enrich(testInstance);
        AppContext ctx = appCtx;
        if (ctx == null) {
            ctx = AppFinder.findAppContextOrWeb(Thread.currentThread().getContextClassLoader(), AppFinder.AppContextTransformer.INSTANCE);
            if (ctx == null) {
                return;
            }
        }

        final BeanContext context = SystemInstance.get().getComponent(ContainerSystem.class).getBeanContext(ctx.getId() + "_" + testInstance.getClass().getName());

        final WebBeansContext appWBC = ctx.getWebBeansContext();
        final BeanManagerImpl bm = appWBC.getBeanManagerImpl();

        boolean ok = false;
        for (final WebContext web : ctx.getWebContexts()) {
            final WebBeansContext webBeansContext = web.getWebBeansContext();
            final BeanManagerImpl webAppBm = webBeansContext.getBeanManagerImpl();
            if (webBeansContext != appWBC && webAppBm.isInUse()) {
                try {
                    doInject(testInstance, context, webAppBm);
                    ok = true;
                    break;
                } catch (final Exception e) {
                    // no-op, try next
                }
            }
        }
        if (bm != null && bm.isInUse() && !ok) {
            try {
                doInject(testInstance, context, bm);
            } catch (final Exception e) {
                LOGGER.log(Level.SEVERE, "Failed injection on: " + testInstance.getClass(), e);
                if (RuntimeException.class.isInstance(e)) {
                    throw RuntimeException.class.cast(e);
                }
                throw new OpenEJBRuntimeException(e);
            }
        }

        if (context != null) {
            final ThreadContext callContext = new ThreadContext(context, null, Operation.INJECTION);
            final ThreadContext oldContext = ThreadContext.enter(callContext);
            try {
                final InjectionProcessor processor = new InjectionProcessor(testInstance, context.getInjections(), context.getJndiContext());
                processor.createInstance();
            } catch (final OpenEJBException e) {
                // ignored
            } finally {
                ThreadContext.exit(oldContext);
            }
        }
    }

    private static void doInject(final Object testInstance, final BeanContext context, final BeanManagerImpl bm) throws Exception {
        final Set<Bean<?>> beans = bm.getBeans(testInstance.getClass());
        final Bean<?> bean = bm.resolve(beans);
        final CreationalContext<?> cc = bm.createCreationalContext(bean);
        if (context != null) {
            context.set(CreationalContext.class, cc);
        }
        OWBInjector.inject(bm, testInstance, cc);
    }

    private static BeanManagerImpl findBeanManager(final AppContext ctx) {
        if (ctx != null) {
            return ctx.getWebBeansContext().getBeanManagerImpl();
        }

        try { // else try to find it from tccl through our SingletonService
            return WebBeansContext.currentInstance().getBeanManagerImpl();
        } catch (final Exception e) { // if not found IllegalStateException or a NPE can be thrown
            // no-op
        }

        return null;
    }

    public static Object[] resolve(final AppContext appContext, final TestClass testClass, final Method method) { // suppose all is a CDI bean...
        final Object[] values = new Object[method.getParameterTypes().length];

        if (appContext == null) {
            return values;
        }

        final BeanManagerImpl beanManager = findBeanManager(appContext);
        if (beanManager == null) {
            return values;
        }

        final Class<?> clazz;
        if (testClass != null) {
            clazz = testClass.getJavaClass();
        } else {
            clazz = method.getDeclaringClass();
        }

        final AnnotatedElementFactory factory = beanManager.getWebBeansContext().getAnnotatedElementFactory();
        final AnnotatedMethod<?> am = factory.newAnnotatedMethod(method, factory.newAnnotatedType(clazz));

        final Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            try {
                values[i] = getParamInstance(beanManager, i, am);
            } catch (final Exception e) {
                LOGGER.info(e.getMessage());
            }
        }
        return values;
    }

    private static <T> T getParamInstance(final BeanManagerImpl manager, final int position, final AnnotatedMethod<?> am) {
        final AnnotationManager annotationManager = manager.getWebBeansContext().getAnnotationManager();

        final AnnotatedParameter<?> ap = am.getParameters().get(position);

        final Type baseType = ap.getBaseType();
        final Set<Bean<?>> beans = manager.getBeans(baseType, annotationManager.getInterceptorBindingMetaAnnotations(ap.getAnnotations()));
        if (beans == null) {
            return null;
        }
        final Bean<?> bean = manager.resolve(beans);
        if (bean == null) {
            return null;
        }

        // note: without a scope it can leak but that's what the user asked!
        final CreationalContextImpl<?> creational = manager.createCreationalContext(null); // null since we don't want the test class be the owner
        return (T) manager.getReference(bean, baseType, creational);
    }
}

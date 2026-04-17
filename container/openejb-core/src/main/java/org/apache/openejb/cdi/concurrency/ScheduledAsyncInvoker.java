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
package org.apache.openejb.cdi.concurrency;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.Context;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.Interceptor;
import jakarta.enterprise.inject.spi.PassivationCapable;
import jakarta.interceptor.InvocationContext;
import org.apache.webbeans.component.InjectionTargetBean;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.context.creational.CreationalContextImpl;
import org.apache.webbeans.intercept.InterceptorResolutionService;
import org.apache.webbeans.portable.InjectionTargetImpl;
import org.apache.webbeans.proxy.OwbInterceptorProxy;
import org.apache.webbeans.exception.ProxyGenerationException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

final class ScheduledAsyncInvoker {
    private static final ThreadLocal<ScheduledReentry> SCHEDULED_REENTRY = new ThreadLocal<>();

    boolean isReentry(final InvocationContext ctx) {
        final ScheduledReentry scheduledReentry = SCHEDULED_REENTRY.get();
        return scheduledReentry != null && scheduledReentry.matches(ctx);
    }

    Invocation capture(final InvocationContext ctx) {
        final Method beanMethod = ctx.getMethod();
        final Object target = ctx.getTarget();
        final Object[] params = ctx.getParameters().clone();
        final Object invocationTarget = resolveInvocationTarget(beanMethod, target);

        return new Invocation() {
            @Override
            public Object proceed() throws Exception {
                return invoke(beanMethod, target, invocationTarget, params);
            }

            @Override
            public void release() {
                if (invocationTarget instanceof ProxyHandle proxyHandle) {
                    proxyHandle.release();
                }
            }
        };
    }

    private Object resolveInvocationTarget(final Method beanMethod, final Object target) {
        final ResolvedBean resolvedBean = resolveBean(beanMethod, target);
        final Object contextualInstance = resolveContextualInstance(resolvedBean.bean());

        if (contextualInstance instanceof OwbInterceptorProxy) {
            return contextualInstance;
        }

        final InterceptorResolutionService.BeanInterceptorInfo interceptorInfo = resolvedBean.injectionTarget().getInterceptorInfo();
        if (interceptorInfo.getDecorators() != null && !interceptorInfo.getDecorators().isEmpty()) {
            throw new IllegalStateException("Scheduled async execution requires the current contextual instance "
                    + "to preserve decorators for " + beanMethod);
        }

        return createProxyHandle(resolvedBean, target);
    }

    private ResolvedBean resolveBean(final Method beanMethod, final Object target) {
        final WebBeansContext webBeansContext = WebBeansContext.currentInstance();
        final Set<Bean<?>> candidates = new LinkedHashSet<>(
                webBeansContext.getBeanManagerImpl().getBeans(target.getClass(), Any.Literal.INSTANCE));
        candidates.addAll(webBeansContext.getBeanManagerImpl().getBeans(beanMethod.getDeclaringClass(), Any.Literal.INSTANCE));

        ResolvedBean preferred = null;
        ResolvedBean fallback = null;
        for (final Bean<?> candidate : candidates) {
            if (!(candidate instanceof InjectionTargetBean<?> injectionTargetBean)) {
                continue;
            }
            if (!(injectionTargetBean.getInjectionTarget() instanceof InjectionTargetImpl<?> injectionTarget)) {
                continue;
            }

            final InterceptorResolutionService.BeanInterceptorInfo interceptorInfo = injectionTarget.getInterceptorInfo();
            if (interceptorInfo == null || !interceptorInfo.getBusinessMethodsInfo().containsKey(beanMethod)) {
                continue;
            }

            final ResolvedBean resolvedBean = new ResolvedBean(injectionTargetBean, injectionTarget);
            if (injectionTargetBean.getBeanClass().isInstance(target)) {
                if (preferred != null && preferred.bean() != candidate) {
                    throw new IllegalStateException("Ambiguous CDI bean resolution for scheduled async method " + beanMethod);
                }
                preferred = resolvedBean;
                continue;
            }

            if (fallback != null && fallback.bean() != candidate) {
                throw new IllegalStateException("Ambiguous CDI bean resolution for scheduled async method " + beanMethod);
            }
            fallback = resolvedBean;
        }

        if (preferred != null) {
            return preferred;
        }
        if (fallback != null) {
            return fallback;
        }
        throw new IllegalStateException("Unable to resolve the CDI bean for scheduled async method " + beanMethod);
    }

    private Object resolveContextualInstance(final Bean<?> bean) {
        if (bean.getScope() == Dependent.class) {
            return null;
        }

        final Context context = WebBeansContext.currentInstance().getBeanManagerImpl().getContext(bean.getScope());
        return context.get(bean);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private ProxyHandle createProxyHandle(final ResolvedBean resolvedBean, final Object target) {
        final WebBeansContext webBeansContext = WebBeansContext.currentInstance();
        final InjectionTargetBean<?> bean = resolvedBean.bean();
        final CreationalContextImpl creationalContext = webBeansContext.getBeanManagerImpl().createCreationalContext(bean);
        final InterceptorResolutionService.BeanInterceptorInfo interceptorInfo = resolvedBean.injectionTarget().getInterceptorInfo();
        final InterceptorResolutionService interceptorResolutionService = webBeansContext.getInterceptorResolutionService();
        final Map<Interceptor<?>, Object> interceptorInstances = interceptorResolutionService
                .createInterceptorInstances(interceptorInfo, creationalContext);
        final InterceptorResolutionService.MethodInterceptionPlan interceptionPlan =
                new InterceptorResolutionService.MethodInterceptionPlan(
                        interceptorResolutionService.createMethodInterceptors(interceptorInfo),
                        interceptorResolutionService.createMethodInterceptorBindings(interceptorInfo));

        final AnnotatedType annotatedType = bean.getAnnotatedType();
        final Class beanClass = annotatedType.getJavaClass();
        final ClassLoader classLoader = beanClass.getClassLoader();
        Class proxyClass = webBeansContext.getInterceptorDecoratorProxyFactory().getCachedProxyClass(bean);
        if (proxyClass == null) {
            try {
                proxyClass = webBeansContext.getInterceptorDecoratorProxyFactory()
                        .createProxyClass(interceptorInfo, annotatedType, classLoader);
            } catch (final ProxyGenerationException e) {
                creationalContext.release();
                throw new IllegalStateException("Unable to build interceptor proxy for scheduled async method", e);
            }
        }

        final String passivationId = bean instanceof PassivationCapable
                ? ((PassivationCapable) bean).getId()
                : null;
        final Object proxy = interceptorResolutionService.createProxiedInstance(
                target,
                creationalContext,
                creationalContext,
                interceptorInfo,
                proxyClass,
                interceptionPlan,
                passivationId,
                interceptorInstances,
                cc -> false,
                (instance, decorators) -> decorators);
        return new ProxyHandle(proxy, creationalContext);
    }

    private Object invoke(final Method beanMethod, final Object reentryTarget,
                          final Object invocationTarget, final Object[] params) throws Exception {
        final ScheduledReentry previous = SCHEDULED_REENTRY.get();
        SCHEDULED_REENTRY.set(new ScheduledReentry(beanMethod, reentryTarget));
        try {
            final Object target = invocationTarget instanceof ProxyHandle proxyHandle ? proxyHandle.proxy() : invocationTarget;
            return beanMethod.invoke(target, params.clone());
        } catch (final InvocationTargetException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof Error error) {
                throw error;
            }
            if (cause instanceof Exception exception) {
                throw exception;
            }
            throw new IllegalStateException(cause);
        } finally {
            if (previous == null) {
                SCHEDULED_REENTRY.remove();
            } else {
                SCHEDULED_REENTRY.set(previous);
            }
        }
    }

    interface Invocation {
        Object proceed() throws Exception;

        default void release() {
            // no-op
        }
    }

    private record ResolvedBean(InjectionTargetBean<?> bean, InjectionTargetImpl<?> injectionTarget) {
    }

    private record ProxyHandle(Object proxy, CreationalContextImpl<?> creationalContext) {
        private void release() {
            creationalContext.release();
        }
    }

    private record ScheduledReentry(Method method, Object target) {
        private boolean matches(final InvocationContext ctx) {
            return method.equals(ctx.getMethod()) && target == ctx.getTarget();
        }
    }
}

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

import org.apache.openejb.util.reflection.Reflections;
import org.apache.webbeans.component.BuiltInOwbBean;
import org.apache.webbeans.component.ExtensionBean;
import org.apache.webbeans.component.OwbBean;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.context.creational.CreationalContextImpl;
import org.apache.webbeans.util.Asserts;
import org.apache.webbeans.util.WebBeansUtil;

import jakarta.el.ELResolver;
import jakarta.el.ExpressionFactory;
import jakarta.enterprise.context.spi.Context;
import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.UnsatisfiedResolutionException;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.InjectionTarget;
import jakarta.enterprise.inject.spi.InterceptionType;
import jakarta.enterprise.inject.spi.Interceptor;
import jakarta.enterprise.inject.spi.PassivationCapable;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class WebappBeanManager extends BeanManagerImpl {
    private static final Annotation[] EMPTY_ANNOTATIONS = new Annotation[0];
    private static final ThreadLocal<Boolean> USE_PARENT_BM = new ThreadLocal<>();
    private final WebappWebBeansContext webappCtx;
    private final InheritedBeanFilter filter;
    private Set<Bean<?>> deploymentBeans;
    private boolean started/* = false*/;

    public WebappBeanManager(final WebappWebBeansContext ctx) {
        super(ctx);
        webappCtx = ctx;
        deploymentBeans = super.getBeans(); // use the parent one while starting
        Reflections.set(this, "injectionResolver", new WebAppInjectionResolver(ctx));
        filter = new InheritedBeanFilter(this);
    }

    @Override
    public List<Interceptor<?>> resolveInterceptors(InterceptionType type, Annotation... interceptorBindings) {
        final List<Interceptor<?>> interceptors = super.resolveInterceptors(type, interceptorBindings);
        final List<Interceptor<?>> parentInterceptors = getParentBm().resolveInterceptors(type, interceptorBindings);
        for (final Interceptor<?> i : parentInterceptors) {
            if (!interceptors.contains(i)) {
                interceptors.add(i);
            }
        }
        return interceptors;
    }

    @Override
    public Object getInjectableReference(final InjectionPoint injectionPoint, final CreationalContext<?> ctx) {
        Asserts.assertNotNull(injectionPoint, "injectionPoint parameter");
        if(injectionPoint == null)  {
            return null;
        }

        final BeanManagerImpl parentBm = getParentBm();
        final Boolean existing = USE_PARENT_BM.get();
        if (existing != null && existing) { // shortcut the whole logic to keep the threadlocal set up correctly
            if (parentBm == null) {
                return null;
            }
            return parentBm.getInjectableReference(injectionPoint, ctx);
        }

        // we can do it cause there is caching but we shouldn't - easy way to overide OWB actually
        final Bean<Object> injectedBean = (Bean<Object>)getInjectionResolver().getInjectionPointBean(injectionPoint);
        try {
            if (parentBm != null && injectedBean != null && injectedBean == parentBm.getInjectionResolver().getInjectionPointBean(injectionPoint)) {
                USE_PARENT_BM.set(true);
                try {
                    return parentBm.getInjectableReference(injectionPoint, ctx);
                } finally {
                    USE_PARENT_BM.remove();
                }
            }
        } catch (final UnsatisfiedResolutionException ure) {
            // skip, use this bean
        }
        return super.getInjectableReference(injectionPoint, ctx);
    }

    @Override
    public <T> CreationalContextImpl<T> createCreationalContext(Contextual<T> contextual) {
        try {
            return super.createCreationalContext(contextual);
        } catch (final RuntimeException e) { // can happen?
            try {
                return getParentBm().createCreationalContext(contextual);
            } catch (final RuntimeException ignored) {
                throw e;
            }
        }
    }

    @Override
    public boolean isNormalScope(final Class<? extends Annotation> annotationType) {
        try {
            return super.isNormalScope(annotationType);
        } catch (final RuntimeException e) {
            try {
                return getParentBm().isNormalScope(annotationType);
            } catch (final RuntimeException ignored) {
                throw e;
            }
        }
    }

    @Override
    public boolean isPassivatingScope(Class<? extends Annotation> annotationType) {
        try {
            return super.isPassivatingScope(annotationType);
        } catch (final RuntimeException e) {
            try {
                return getParentBm().isPassivatingScope(annotationType);
            } catch (final RuntimeException ignored) {
                throw e;
            }
        }
    }


    @Override
    public boolean isQualifier(final Class<? extends Annotation> annotationType) {
        try {
            return super.isQualifier(annotationType);
        } catch (final RuntimeException e) {
            try {
                return getParentBm().isQualifier(annotationType);
            } catch (final RuntimeException ignored) {
                throw e;
            }
        }
    }

    @Override
    public boolean isInterceptorBinding(final Class<? extends Annotation> annotationType) {
        try {
            return super.isInterceptorBinding(annotationType);
        } catch (final RuntimeException e) {
            try {
                return getParentBm().isInterceptorBinding(annotationType);
            } catch (final RuntimeException ignored) {
                throw e;
            }
        }
    }


    @Override
    public boolean isStereotype(final Class<? extends Annotation> annotationType) {
        try {
            return super.isStereotype(annotationType);
        } catch (final RuntimeException e) {
            try {
                return getParentBm().isStereotype(annotationType);
            } catch (final RuntimeException ignored) {
                throw e;
            }
        }
    }

    @Override
    public Set<Annotation> getInterceptorBindingDefinition(final Class<? extends Annotation> qualifier) {
        try {
            return super.getInterceptorBindingDefinition(qualifier);
        } catch (final RuntimeException e) {
            try {
                return getParentBm().getInterceptorBindingDefinition(qualifier);
            } catch (final RuntimeException ignored) {
                throw e;
            }
        }
    }

    @Override
    public Context getContext(final Class<? extends Annotation> scope) {
        try {
            return super.getContext(scope);
        } catch (final RuntimeException e) {
            try {
                return getParentBm().getContext(scope);
            } catch (final RuntimeException ignored) {
                throw e;
            }
        }
    }

    @Override
    public ELResolver getELResolver() {
        return new WebAppElResolver(super.getELResolver(), getParentBm().getELResolver());
    }

    @Override
    public <T> AnnotatedType<T> createAnnotatedType(final Class<T> type) {
        try {
            return super.createAnnotatedType(type);
        } catch (final RuntimeException e) {
            try {
                return getParentBm().createAnnotatedType(type);
            } catch (final RuntimeException ignored) {
                throw e;
            }
        }
    }

    @Override
    public <T> InjectionTarget<T> createInjectionTarget(final AnnotatedType<T> type) {
        try {
            return super.createInjectionTarget(type);
        } catch (final RuntimeException e) {
            try {
                return getParentBm().createInjectionTarget(type);
            } catch (final RuntimeException ignored) {
                throw e;
            }
        }
    }

    @Override
    public ExpressionFactory wrapExpressionFactory(final ExpressionFactory expressionFactory) {
        return super.wrapExpressionFactory(expressionFactory);
    }

    public BeanManagerImpl getParentBm() {
        final WebBeansContext parent = webappCtx.getParent();
        return parent != null ? parent.getBeanManagerImpl() : null;
    }

    @Override
    public boolean isInUse() {
        return super.isInUse() || getParentBm().isInUse();
    }

    @Override
    public Set<Bean<?>> getComponents() {
        if (!started) {
            // probably not yet merged (afterStart())
            // so reuse parent beans
            // this can happen for validations
            return new IteratorSet<>(
                new MultipleIterator<>(
                    filter,
                    deploymentBeans.iterator(),
                    getParentBm().getComponents().iterator()));
        }
        return deploymentBeans;
    }

    @Override
    public Set<Bean<?>> getBeans() {
        return deploymentBeans;
    }

    @Override
    public <T> void addAdditionalAnnotatedType(Object extension, AnnotatedType<T> inAnnotatedType, String id) {
        super.addAdditionalAnnotatedType(extension, inAnnotatedType, id);
    }

    @Override
    public Bean<?> getPassivationCapableBean(final String id) {
        final Bean<?> bean = super.getPassivationCapableBean(id);
        if (bean == null && getParentBm() != null) {
            return getParentBm().getPassivationCapableBean(id);
        }
        return bean;
    }

    public void afterStart() {
        started = true;
        deploymentBeans = mergeBeans();
        webappCtx.getBeanManagerImpl().getInjectionResolver().clearCaches(); // to force new resolution with new beans
    }

    private Set<Bean<?>> mergeBeans() {
        final Set<Bean<?>> allBeans = new CopyOnWriteArraySet<>(); // override parent one with a "webapp" bean list
        final BeanManagerImpl parentBm = getParentBm();
        if (parentBm != null) {
            for (final Bean<?> bean : parentBm.getBeans()) {
                if (filter.accept(bean)) {
                    allBeans.add(bean);
                }
            }
        }
        allBeans.addAll(super.getBeans());
        return allBeans;
    }

    public void beforeStop() {
        // no-op
    }

    private boolean isEvent(final Object event) {
        return !WebBeansUtil.isDefaultExtensionBeanEventType(event.getClass())
                && !webappCtx.getWebBeansUtil().isContainerEventType(event);
    }

    private interface Filter<A> {
        boolean accept(A a);
    }

    private static final class InheritedBeanFilter implements Filter<Bean<?>> {
        private final WebappBeanManager beanManager;

        private InheritedBeanFilter(final WebappBeanManager beanManager) {
            this.beanManager = beanManager;
        }

        @Override
        public boolean accept(final Bean<?> bean) {
            if (BuiltInOwbBean.class.isInstance(bean) || ExtensionBean.class.isInstance(bean)) {
                return false;
            }
            if (OwbBean.class.isInstance(bean)) {
                final OwbBean owbBean = OwbBean.class.cast(bean);
                if (owbBean.isPassivationCapable())
                {
                    if (hasBean(owbBean.getId()))
                    {
                        return false;
                    }
                }
            } else if (PassivationCapable.class.isInstance(bean)) {
                if (hasBean(PassivationCapable.class.cast(bean).getId())) {
                    return false;
                }
            }

            final Set<Annotation> qualifiers = bean.getQualifiers();
            return beanManager.getBeans(
                    bean.getBeanClass(),
                    qualifiers.isEmpty() ? EMPTY_ANNOTATIONS : qualifiers.toArray(new Annotation[qualifiers.size()])).isEmpty();
        }

        private boolean hasBean(final String id) {
            return beanManager.getPassivationCapableBean(id) != null;
        }
    }

    private static final class MultipleIterator<A> implements Iterator<A> {
        private final Iterator<A>[] delegates;
        private final Filter<A> filter;

        private A next/* = null*/;
        private int idx/* = 0*/;

        /**
         * @param filter    used to filter delegates from index 1 to N-1 (0 is not filtered)
         * @param delegates iterator this Iterator merges, one delegates is mandatory
         */
        private MultipleIterator(final Filter<A> filter, final Iterator<A>... delegates) {
            this.filter = filter;
            this.delegates = delegates;
        }

        @Override
        public boolean hasNext() {
            for (; idx < delegates.length; idx++) {
                while (delegates[idx].hasNext()) {
                    next = delegates[idx].next();
                    if (idx == 0 || filter.accept(next)) { // we accept all items of first iterator
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public A next() {
            return next;
        }

        @Override
        public void remove() {
            delegates[idx].remove();
        }
    }

    // hack set, only use it for Set which are used as Iterator
    // case of getComponent
    private static final class IteratorSet<A> extends HashSet<A> {
        private final Iterator<A> it;

        private IteratorSet(final Iterator<A> it) {
            this.it = it;
        }

        @Override
        public Iterator<A> iterator() {
            return it;
        }
    }
}

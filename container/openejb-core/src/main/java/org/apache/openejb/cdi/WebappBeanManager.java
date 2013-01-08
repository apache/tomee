/**
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

import org.apache.webbeans.component.BeanManagerBean;
import org.apache.webbeans.component.BuildInOwbBean;
import org.apache.webbeans.component.ConversationBean;
import org.apache.webbeans.component.InjectionPointBean;
import org.apache.webbeans.container.BeanManagerImpl;

import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.InterceptionType;
import javax.enterprise.inject.spi.Interceptor;
import javax.enterprise.inject.spi.ObserverMethod;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class WebappBeanManager extends BeanManagerImpl {
    private final WebappWebBeansContext webappCtx;
    private final ThreadLocal<Boolean> internalUse = new ThreadLocal<Boolean>() {
        @Override
        public Boolean initialValue() {
            return false;
        }
    };
    private Set<Bean<?>> deploymentBeans;

    public WebappBeanManager(WebappWebBeansContext ctx) {
        super(ctx);
        webappCtx = ctx;
        deploymentBeans = super.getBeans(); // use the parent one while starting
    }

    @Override
    public Object getReference(Bean<?> bean, Type beanType, CreationalContext<?> ctx) {
        Object ref;
        try {
            ref = getParentBm().getReference(bean, beanType, ctx);
            if (ref == null) {
                ref = super.getReference(bean, beanType, ctx);
            }
        } catch (RuntimeException e) {
            ref = super.getReference(bean, beanType, ctx);
        }
        return ref;
    }

    @Override
    public Object getInjectableReference(InjectionPoint injectionPoint, CreationalContext<?> ctx) {
        try {
            return super.getInjectableReference(injectionPoint, ctx);
        } catch (RuntimeException e) {
            return getParentBm().getInjectableReference(injectionPoint, ctx);
        }
    }

    @Override
    public <T> CreationalContext<T> createCreationalContext(Contextual<T> contextual) {
        try {
            return super.createCreationalContext(contextual);
        } catch (RuntimeException e) { // can happen?
            try {
                return getParentBm().createCreationalContext(contextual);
            } catch (RuntimeException ignored) {
                throw e;
            }
        }
    }

    @Override
    public Set<Bean<?>> getBeans(Type beanType, Annotation... qualifiers) {
        final Set<Bean<?>> beans = new HashSet<Bean<?>>();
        internalUse.set(true);
        try {
            beans.addAll(super.getBeans(beanType, qualifiers));
        } finally {
            internalUse.set(false);
        }
        if (!internalUse.get()) {
            beans.addAll(getParentBm().getBeans(beanType, qualifiers));
        }
        internalUse.remove();
        return beans;
    }

    @Override
    public Set<Bean<?>> getBeans(String name) {
        final Set<Bean<?>> beans = new HashSet<Bean<?>>();
        internalUse.set(true);
        try {
            beans.addAll(super.getBeans(name));
        } finally {
            internalUse.set(false);
        }
        if (!internalUse.get()) {
            beans.addAll(getParentBm().getBeans(name));
        }
        internalUse.remove();
        return beans;
    }

    @Override
    public Bean<?> getPassivationCapableBean(String id) {
        try {
            return getParentBm().getPassivationCapableBean(id);
        } catch (RuntimeException e) {
            return super.getPassivationCapableBean(id);
        }
    }

    @Override
    public <X> Bean<? extends X> resolve(Set<Bean<? extends X>> beans) {
        try {
            return super.resolve(beans);
        } catch (RuntimeException e) {
            try {
                return getParentBm().resolve(beans);
            } catch (RuntimeException ignored) {
                throw e;
            }
        }
    }

    @Override
    public void fireEvent(Object event, Annotation... qualifiers) {
        super.fireEvent(event, qualifiers);
        getParentBm().fireEvent(event, qualifiers);
    }

    @Override
    public <T> Set<ObserverMethod<? super T>> resolveObserverMethods(T event, Annotation... qualifiers) {
        final Set<ObserverMethod<? super T>> mtds = new HashSet<ObserverMethod<? super T>>();
        internalUse.set(true);
        try {
            mtds.addAll(super.resolveObserverMethods(event, qualifiers));
        } finally {
            internalUse.set(false);
        }
        if (!internalUse.get()) {
            mtds.addAll(getParentBm().resolveObserverMethods(event, qualifiers));
        }
        internalUse.remove();
        return mtds;
    }

    @Override
    public List<Decorator<?>> resolveDecorators(Set<Type> types, Annotation... qualifiers) {
        final List<Decorator<?>> decorators = new ArrayList<Decorator<?>>();
        internalUse.set(true);
        try {
            decorators.addAll(super.resolveDecorators(types, qualifiers));
        } finally {
            internalUse.set(false);
        }
        if (!internalUse.get()) {
            decorators.addAll(getParentBm().resolveDecorators(types, qualifiers));
        }
        return decorators;
    }

    @Override
    public List<Interceptor<?>> resolveInterceptors(InterceptionType type, Annotation... qualifiers) {
        final List<Interceptor<?>> interceptors = new ArrayList<Interceptor<?>>();
        internalUse.set(true);
        try {
            interceptors.addAll(super.resolveInterceptors(type, qualifiers));
        } finally {
            internalUse.set(false);
        }
        if (!internalUse.get()) {
            interceptors.addAll(getParentBm().resolveInterceptors(type, qualifiers));
        }
        internalUse.remove();
        return interceptors;
    }

    @Override
    public void validate(InjectionPoint injectionPoint) {
        super.validate(injectionPoint);
        // getParentBm().validate(injectionPoint); // prevent injections from webapp only
    }

    @Override
    public boolean isScope(Class<? extends Annotation> annotationType) {
        try {
            return super.isScope(annotationType);
        } catch (RuntimeException e) {
            try {
                return getParentBm().isScope(annotationType);
            } catch (RuntimeException ignored) {
                throw e;
            }
        }
    }

    @Override
    public boolean isNormalScope(Class<? extends Annotation> annotationType) {
        try {
            return super.isNormalScope(annotationType);
        } catch (RuntimeException e) {
            try {
                return getParentBm().isNormalScope(annotationType);
            } catch (RuntimeException ignored) {
                throw e;
            }
        }
    }

    @Override
    public boolean isPassivatingScope(Class<? extends Annotation> annotationType) {
        try {
            return super.isPassivatingScope(annotationType);
        } catch (RuntimeException e) {
            try {
                return getParentBm().isPassivatingScope(annotationType);
            } catch (RuntimeException ignored) {
                throw e;
            }
        }
    }


    @Override
    public boolean isQualifier(Class<? extends Annotation> annotationType) {
        try {
            return super.isQualifier(annotationType);
        } catch (RuntimeException e) {
            try {
                return getParentBm().isQualifier(annotationType);
            } catch (RuntimeException ignored) {
                throw e;
            }
        }
    }

    @Override
    public boolean isInterceptorBinding(Class<? extends Annotation> annotationType) {
        try {
            return super.isInterceptorBinding(annotationType);
        } catch (RuntimeException e) {
            try {
                return getParentBm().isInterceptorBinding(annotationType);
            } catch (RuntimeException ignored) {
                throw e;
            }
        }
    }


    @Override
    public boolean isStereotype(Class<? extends Annotation> annotationType) {
        try {
            return super.isStereotype(annotationType);
        } catch (RuntimeException e) {
            try {
                return getParentBm().isStereotype(annotationType);
            } catch (RuntimeException ignored) {
                throw e;
            }
        }
    }

    @Override
    public Set<Annotation> getInterceptorBindingDefinition(Class<? extends Annotation> qualifier) {
        try {
            return super.getInterceptorBindingDefinition(qualifier);
        } catch (RuntimeException e) {
            try {
                return getParentBm().getInterceptorBindingDefinition(qualifier);
            } catch (RuntimeException ignored) {
                throw e;
            }
        }
    }

    @Override
    public Set<Annotation> getStereotypeDefinition(Class<? extends Annotation> stereotype) {
        final Set<Annotation> mtds = new HashSet<Annotation>();
        internalUse.set(true);
        try {
            mtds.addAll(super.getStereotypeDefinition(stereotype));
        } finally {
            internalUse.set(false);
        }
        if (!internalUse.get()) {
            mtds.addAll(getParentBm().getStereotypeDefinition(stereotype));
        }
        internalUse.remove();
        return mtds;
    }

    @Override
    public Context getContext(Class<? extends Annotation> scope) {
        try {
            return super.getContext(scope);
        } catch (RuntimeException e) {
            try {
                return getParentBm().getContext(scope);
            } catch (RuntimeException ignored) {
                throw e;
            }
        }
    }

    @Override
    public ELResolver getELResolver() {
        return new WebAppElResolver(super.getELResolver(), getParentBm().getELResolver());
    }

    @Override
    public <T> AnnotatedType<T> createAnnotatedType(Class<T> type) {
        try {
            return super.createAnnotatedType(type);
        } catch (RuntimeException e) {
            try {
                return getParentBm().createAnnotatedType(type);
            } catch (RuntimeException ignored) {
                throw e;
            }
        }
    }

    @Override
    public <T> InjectionTarget<T> createInjectionTarget(AnnotatedType<T> type) {
        try {
            return super.createInjectionTarget(type);
        } catch (RuntimeException e) {
            try {
                return getParentBm().createInjectionTarget(type);
            } catch (RuntimeException ignored) {
                throw e;
            }
        }
    }

    @Override
    public ExpressionFactory wrapExpressionFactory(javax.el.ExpressionFactory expressionFactory) {
        return super.wrapExpressionFactory(expressionFactory);
    }

    public BeanManagerImpl getParentBm() {
        return webappCtx.getParent().getBeanManagerImpl();
    }

    @Override
    public boolean isInUse() {
        return super.isInUse() || getParentBm().isInUse();
    }

    @Override
    public Set<Bean<?>> getComponents() {
        if (deploymentBeans.isEmpty()) {
            // probably not yet merged (afterStart())
            // so reuse parent beans
            // this can happen for validations
            return super.getBeans();
        }
        return deploymentBeans;
    }

    @Override
    public Set<Bean<?>> getBeans() {
        return deploymentBeans;
    }

    public void afterStart() {
        deploymentBeans = new CopyOnWriteArraySet<Bean<?>>(); // override parent one with a "webapp" bean list
        for (Bean<?> bean : getParentBm().getBeans()) {
            if (bean instanceof BeanManagerBean || bean instanceof BuildInOwbBean
                    || bean instanceof ConversationBean || bean instanceof InjectionPointBean) {
                continue;
            }
            deploymentBeans.add(bean);
        }
        deploymentBeans.addAll(super.getBeans());
    }
}

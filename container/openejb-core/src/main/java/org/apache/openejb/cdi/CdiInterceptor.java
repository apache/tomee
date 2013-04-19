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

import org.apache.openejb.core.interceptor.InterceptorData;
import org.apache.openejb.core.ivm.IntraVmArtifact;
import org.apache.openejb.util.reflection.Reflections;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.context.creational.CreationalContextImpl;
import org.apache.webbeans.intercept.AbstractInvocationContext;
import org.apache.webbeans.intercept.DecoratorHandler;
import org.apache.webbeans.intercept.InterceptorResolutionService;
import org.apache.webbeans.proxy.InterceptorDecoratorProxyFactory;

import javax.decorator.Delegate;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.interceptor.AroundInvoke;
import javax.interceptor.AroundTimeout;
import javax.interceptor.InvocationContext;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class CdiInterceptor<T> implements Serializable {
    static {
        InterceptorData.cacheScan(CdiInterceptor.class);
    }

    private final CdiEjbBean<T> bean;
    private final WebBeansContext webBeansContext;

    public CdiInterceptor(final CdiEjbBean<T> bean) {
        this.bean = bean;
        this.webBeansContext = bean.getWebBeansContext();
    }

    @AroundTimeout
    @AroundInvoke
    public Object aroundInvoke(final InvocationContext ejbContext) throws Exception {
        final Class<T> proxyClass = Class.class.cast(Reflections.get(bean.getInjectionTarget(), "proxyClass"));
        if (proxyClass != null) { // means interception
            final InterceptorResolutionService.BeanInterceptorInfo interceptorInfo = bean.getBeanContext().get(InterceptorResolutionService.BeanInterceptorInfo.class);
            if (interceptorInfo.getDecorators() != null && !interceptorInfo.getDecorators().isEmpty()) {
                final InterceptorDecoratorProxyFactory pf = webBeansContext.getInterceptorDecoratorProxyFactory();
                final CreationalContext context = bean.getBeanContext().get(CurrentCreationalContext.class).get();
                final T instance = (T) ejbContext.getTarget();

                CreationalContextImpl<T> creationalContextImpl = (CreationalContextImpl<T>) context;
                if (creationalContextImpl == null) { // shouldn't occur
                    creationalContextImpl = webBeansContext.getBeanManagerImpl().createCreationalContext(bean);
                }

                // decorators
                T delegate = instance;
                final List<Decorator<?>> decorators = interceptorInfo.getDecorators();
                final Map<Decorator<?>, Object> instances = new HashMap<Decorator<?>, Object>();
                for (int i = decorators.size(); i > 0; i--) {
                    final Decorator<?> decorator = decorators.get(i - 1);
                    creationalContextImpl.putDelegate(delegate);
                    final Object decoratorInstance = decorator.create(CreationalContext.class.cast(creationalContextImpl));
                    instances.put(decorator, decoratorInstance);
                    delegate = pf.createProxyInstance(proxyClass, instance, new DecoratorHandler(interceptorInfo, instances, i - 1, instance));
                }

                return new OpenEJBInterceptorInvocationContext<T>(delegate, ejbContext).proceed();
            }
        }
        return ejbContext.proceed();
    }

    protected Object writeReplace() throws ObjectStreamException {
        return new IntraVmArtifact(this, true);
    }

    protected boolean isDelegateInjection(final CreationalContext<?> cc)
    {
        if (CreationalContextImpl.class.isInstance(cc))
        {
            final InjectionPoint ip = CreationalContextImpl.class.cast(cc).getInjectionPoint();
            if (ip == null)
            {
                return false;
            }

            final Member member = ip.getMember();
            if (member != null
                    && Field.class.isInstance(member) && Field.class.cast(member).getAnnotation(Delegate.class) != null)
            {
                return true;
            }
        }
        return false;
    }

    public static class OpenEJBInterceptorInvocationContext<T> extends AbstractInvocationContext<T> {
        protected InvocationContext ejbContext;

        public OpenEJBInterceptorInvocationContext(final T target, final InvocationContext ejbContext) {
            super(target, ejbContext.getMethod(), ejbContext.getParameters());
            this.ejbContext = ejbContext;
        }

        @Override
        public Object proceed() throws Exception {
            return ejbContext.proceed(); // todo: use target (delegate in this case)
        }
    }
}


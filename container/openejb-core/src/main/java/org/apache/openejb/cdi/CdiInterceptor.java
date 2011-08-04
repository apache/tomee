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

import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;
import org.apache.openejb.core.ivm.IntraVmArtifact;
import org.apache.webbeans.component.InjectionTargetBean;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.context.creational.CreationalContextImpl;
import org.apache.webbeans.decorator.DelegateHandler;
import org.apache.webbeans.decorator.WebBeansDecoratorConfig;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.CreationalContext;
import javax.interceptor.AroundInvoke;
import javax.interceptor.AroundTimeout;
import javax.interceptor.InvocationContext;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @version $Rev$ $Date$
 */
public class CdiInterceptor implements Serializable {

    private final CdiEjbBean<Object> bean;
    private final BeanManagerImpl manager;
    private final CdiAppContextsService contextService;
    private final WebBeansContext webBeansContext;

    public CdiInterceptor(CdiEjbBean<Object> bean, BeanManagerImpl manager, CdiAppContextsService contextService) {
        this.bean = bean;
        this.manager = manager;
        this.contextService = contextService;
        this.webBeansContext = bean.getWebBeansContext();
    }

    @AroundTimeout
    @AroundInvoke
    public Object aroundInvoke(final InvocationContext ejbContext) throws Exception {

        Callable callable = new Callable() {
            @Override
            public Object call() throws Exception {
                return invoke(ejbContext);
            }
        };

//        callable = new ScopeActivator(callable, ApplicationScoped.class);
//        callable = new ScopeActivator(callable, RequestScoped.class);
        return callable.call();
    }

    public class ScopeActivator implements Callable {
        private final Callable callable;
        private final Class<? extends Annotation> scopeType;

        public ScopeActivator(Callable callable, Class<? extends Annotation> scopeType) {
            this.callable = callable;
            this.scopeType = scopeType;
        }

        @Override
        public Object call() throws Exception {

            Context ctx = contextService.getCurrentContext(scopeType);

            if (ctx == null) {
                contextService.startContext(scopeType, null);
            } else if (!ctx.isActive()) {
                contextService.activateContext(scopeType);
            }

            try {
                return callable.call();
            } finally {
                if (ctx == null) {
                    contextService.endContext(scopeType, null);
                }
            }
        }
    }

    private Object invoke(InvocationContext ejbContext) throws Exception {
        final CreationalContext<?> context = getCreationalContext();

        Object instance = ejbContext.getTarget();

        if (bean.getDecoratorStack().size() > 0) {

            Class<?> proxyClass = webBeansContext.getJavassistProxyFactory().getInterceptorProxyClasses().get((InjectionTargetBean<?>) bean);
            if (proxyClass == null) {
                ProxyFactory delegateFactory = webBeansContext.getJavassistProxyFactory().createProxyFactory(bean);
                proxyClass = webBeansContext.getJavassistProxyFactory().getProxyClass(delegateFactory);
                webBeansContext.getJavassistProxyFactory().getInterceptorProxyClasses().put((InjectionTargetBean<?>) bean, proxyClass);
            }
            Object delegate = proxyClass.newInstance();
            DelegateHandler delegateHandler = new DelegateHandler(bean, ejbContext);
            ((ProxyObject) delegate).setHandler(delegateHandler);

            // Gets component decorator stack
            List<Object> decorators = WebBeansDecoratorConfig.getDecoratorStack(bean, instance, delegate, (CreationalContextImpl<?>) context);
            //Sets decorator stack of delegate
            delegateHandler.setDecorators(decorators);

            return delegateHandler.invoke(instance, ejbContext.getMethod(), null, ejbContext.getParameters());
        } else {
            return ejbContext.proceed();
        }
    }

    private CreationalContext<?> getCreationalContext() {
        // TODO This has the outcome that decorators are created every request
        // need to instantiate decorators at instance creation time
        // when and where we create interceptor instances
        return manager.createCreationalContext(null);
    }

    protected Object writeReplace() throws ObjectStreamException {
        return new IntraVmArtifact(this, true);
    }

}


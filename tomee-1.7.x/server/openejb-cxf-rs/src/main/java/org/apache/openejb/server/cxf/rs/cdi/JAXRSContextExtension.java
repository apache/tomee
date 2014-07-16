/*
 *     Licensed to the Apache Software Foundation (ASF) under one or more
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
package org.apache.openejb.server.cxf.rs.cdi;

import static java.util.Arrays.asList;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.AnnotationLiteral;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;

import org.apache.openejb.rest.AbstractRestThreadLocalProxy;
import org.apache.openejb.rest.ThreadLocalContextManager;
import org.apache.webbeans.annotation.AnyLiteral;
import org.apache.webbeans.annotation.EmptyAnnotationLiteral;

public class JAXRSContextExtension implements Extension {
    public static class ContextLiteral extends EmptyAnnotationLiteral<Context> implements Context {
        private static final long serialVersionUID = 1L;

        public static final AnnotationLiteral<Context> INSTANCE = new ContextLiteral();
    }

    void addContextAsQualifier(final @Observes BeforeBeanDiscovery bbd) {
        bbd.addQualifier(Context.class);
    }

    void addContextInstances(final @Observes AfterBeanDiscovery abd, final BeanManager bm) {
        abd.addBean(new ContextBean<SecurityContext>(SecurityContext.class, ThreadLocalContextManager.SECURITY_CONTEXT));
        abd.addBean(new ContextBean<UriInfo>(UriInfo.class, ThreadLocalContextManager.URI_INFO));
        abd.addBean(new ContextBean<HttpServletRequest>(HttpServletRequest.class, ThreadLocalContextManager.HTTP_SERVLET_REQUEST));
        abd.addBean(new ContextBean<HttpServletResponse>(HttpServletResponse.class, ThreadLocalContextManager.HTTP_SERVLET_RESPONSE));
        abd.addBean(new ContextBean<HttpHeaders>(HttpHeaders.class, ThreadLocalContextManager.HTTP_HEADERS));
        abd.addBean(new ContextBean<Request>(Request.class, ThreadLocalContextManager.REQUEST));
        abd.addBean(new ContextBean<ServletRequest>(ServletRequest.class, ThreadLocalContextManager.SERVLET_REQUEST));
        abd.addBean(new ContextBean<ServletContext>(ServletContext.class, ThreadLocalContextManager.SERVLET_CONTEXT));
        abd.addBean(new ContextBean<ServletConfig>(ServletConfig.class, ThreadLocalContextManager.SERVLET_CONFIG));
        abd.addBean(new ContextBean<Providers>(Providers.class, ThreadLocalContextManager.PROVIDERS));
        abd.addBean(new ContextBean<ContextResolver>(ContextResolver.class, ThreadLocalContextManager.CONTEXT_RESOLVER));
    }

    public static class ContextBean<T> implements Bean<T> {
        private final Class<T> type;
        private final Set<Type> types;
        private final Set<Annotation> qualifiers;
        private final T proxy;

        public ContextBean(final Class<T> type, final AbstractRestThreadLocalProxy<T> proxy) {
            this.type = type;
            this.proxy =
                    (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[] { type, Serializable.class },
                            new DelegateHandler(proxy));
            this.types = new HashSet<Type>(asList(Object.class, type));
            this.qualifiers = new HashSet<Annotation>(asList(ContextLiteral.INSTANCE, AnyLiteral.INSTANCE));
        }

        @Override
        public Set<Type> getTypes() {
            return types;
        }

        @Override
        public Set<Annotation> getQualifiers() {
            return qualifiers;
        }

        @Override
        public Class<? extends Annotation> getScope() {
            return ApplicationScoped.class;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public boolean isNullable() {
            return false;
        }

        @Override
        public Set<InjectionPoint> getInjectionPoints() {
            return Collections.<InjectionPoint>emptySet();
        }

        @Override
        public Class<?> getBeanClass() {
            return type;
        }

        @Override
        public Set<Class<? extends Annotation>> getStereotypes() {
            return Collections.<Class<? extends Annotation>>emptySet();
        }

        @Override
        public boolean isAlternative() {
            return false;
        }

        @Override
        public T create(final CreationalContext<T> tCreationalContext) {
            return proxy;
        }

        @Override
        public void destroy(final T t, final CreationalContext<T> tCreationalContext) {
            // no-op
        }
    }

    private static class DelegateHandler<T> implements InvocationHandler {
        private final AbstractRestThreadLocalProxy<T> proxy;

        public DelegateHandler(final AbstractRestThreadLocalProxy<T> proxy) {
            this.proxy = proxy;
        }

        @Override
        public Object invoke(final Object ignored, final Method method, final Object[] args) throws Throwable {
            try {
                return method.invoke(proxy.get(), args);
            }
            catch (final InvocationTargetException ite) {
                throw ite.getCause();
            }
        }
    }
}

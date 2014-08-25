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
package org.apache.openejb.server.cxf.rs;

import org.apache.cxf.Bus;
import org.apache.cxf.binding.BindingFactoryManager;
import org.apache.cxf.jaxrs.JAXRSBindingFactory;
import org.apache.cxf.transport.DestinationFactory;
import org.apache.cxf.transport.http.HTTPTransportFactory;
import org.apache.openejb.cdi.WebBeansContextBeforeDeploy;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.observer.Observes;
import org.apache.openejb.rest.AbstractRestThreadLocalProxy;
import org.apache.openejb.rest.RESTResourceFinder;
import org.apache.openejb.rest.ThreadLocalContextManager;
import org.apache.openejb.server.ServiceException;
import org.apache.openejb.server.cxf.transport.util.CxfUtil;
import org.apache.openejb.server.rest.RESTService;
import org.apache.openejb.server.rest.RsHttpListener;
import org.apache.webbeans.annotation.AnyLiteral;
import org.apache.webbeans.annotation.EmptyAnnotationLiteral;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.BeanManagerImpl;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.AnnotationLiteral;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import static java.util.Arrays.asList;

public class CxfRSService extends RESTService {

    private static final String NAME = "cxf-rs";
    private DestinationFactory destinationFactory;

    @Override
    public void service(final InputStream in, final OutputStream out) throws ServiceException, IOException {
        throw new UnsupportedOperationException(getClass().getName() + " cannot be invoked directly");
    }

    @Override
    public void service(final Socket socket) throws ServiceException, IOException {
        throw new UnsupportedOperationException(getClass().getName() + " cannot be invoked directly");
    }

    @Override
    public String getName() {
        return NAME;
    }

    public void integrateCDIAndJaxRsInjections(@Observes final WebBeansContextBeforeDeploy event) {
        contextCDIIntegration(event.getContext());
    }

    private void contextCDIIntegration(final WebBeansContext wbc) {
        if (!enabled) {
            return;
        }

        final BeanManagerImpl beanManagerImpl = wbc.getBeanManagerImpl();
        if (!beanManagerImpl.getAdditionalQualifiers().contains(Context.class)) {
            beanManagerImpl.addAdditionalQualifier(Context.class);
        }
        if (!hasBean(beanManagerImpl, SecurityContext.class)) {
            beanManagerImpl.addInternalBean(new ContextBean<>(SecurityContext.class, ThreadLocalContextManager.SECURITY_CONTEXT));
        }
        if (!hasBean(beanManagerImpl, UriInfo.class)) {
            beanManagerImpl.addInternalBean(new ContextBean<>(UriInfo.class, ThreadLocalContextManager.URI_INFO));
        }
        if (!hasBean(beanManagerImpl, HttpServletRequest.class)) {
            beanManagerImpl.addInternalBean(new ContextBean<>(HttpServletRequest.class, ThreadLocalContextManager.HTTP_SERVLET_REQUEST));
        }
        if (!hasBean(beanManagerImpl, HttpServletResponse.class)) {
            beanManagerImpl.addInternalBean(new ContextBean<>(HttpServletResponse.class, ThreadLocalContextManager.HTTP_SERVLET_RESPONSE));
        }
        if (!hasBean(beanManagerImpl, HttpHeaders.class)) {
            beanManagerImpl.addInternalBean(new ContextBean<>(HttpHeaders.class, ThreadLocalContextManager.HTTP_HEADERS));
        }
        if (!hasBean(beanManagerImpl, Request.class)) {
            beanManagerImpl.addInternalBean(new ContextBean<>(Request.class, ThreadLocalContextManager.REQUEST));
        }
        /* HttpServletRequest impl it
        if (!hasBean(beanManagerImpl, ServletRequest.class)) {
            beanManagerImpl.addInternalBean(new ContextBean<>(ServletRequest.class, ThreadLocalContextManager.SERVLET_REQUEST));
        }
        */
        if (!hasBean(beanManagerImpl, ServletContext.class)) {
            beanManagerImpl.addInternalBean(new ContextBean<>(ServletContext.class, ThreadLocalContextManager.SERVLET_CONTEXT));
        }
        if (!hasBean(beanManagerImpl, ServletConfig.class)) {
            beanManagerImpl.addInternalBean(new ContextBean<>(ServletConfig.class, ThreadLocalContextManager.SERVLET_CONFIG));
        }
        if (!hasBean(beanManagerImpl, Providers.class)) {
            beanManagerImpl.addInternalBean(new ContextBean<>(Providers.class, ThreadLocalContextManager.PROVIDERS));
        }
        if (!hasBean(beanManagerImpl, ContextResolver.class)) {
            beanManagerImpl.addInternalBean(new ContextBean<>(ContextResolver.class, ThreadLocalContextManager.CONTEXT_RESOLVER));
        }
        beanManagerImpl.getInjectionResolver().clearCaches(); // hasBean() usage can have cached several things
    }

    private static boolean hasBean(final BeanManagerImpl beanManagerImpl, final Class<?> type) {
        return beanManagerImpl.getInjectionResolver().implResolveByType(false, type).isEmpty();
    }

    @Override
    public void init(final Properties properties) throws Exception {
        super.init(properties);
        SystemInstance.get().setComponent(RESTResourceFinder.class, new CxfRESTResourceFinder());

        CxfUtil.configureBus();

        final Bus bus = CxfUtil.getBus();

        final ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(CxfUtil.initBusLoader());
        try {
            // force init of bindings
            if (!CxfUtil.hasService(JAXRSBindingFactory.JAXRS_BINDING_ID)) {
                // cxf does it but with the pattern "if not here install it". It is slow so installing it without testing for presence here.
                final BindingFactoryManager bfm = bus.getExtension(BindingFactoryManager.class);
                try {
                    bfm.registerBindingFactory(JAXRSBindingFactory.JAXRS_BINDING_ID, new JAXRSBindingFactory(bus));
                } catch (Throwable b) {
                    // no-op
                }
            }
        } finally {
            if (oldLoader != null) {
                CxfUtil.clearBusLoader(oldLoader);
            }
        }
    }

    @Override
    protected void beforeStart() {
        super.beforeStart();
        destinationFactory = new HTTPTransportFactory();
    }

    @Override
    protected boolean containsJaxRsConfiguration(final Properties properties) {
        return properties.containsKey(CxfRsHttpListener.PROVIDERS_KEY)
            || properties.containsKey(CxfRsHttpListener.CXF_JAXRS_PREFIX + CxfUtil.IN_FAULT_INTERCEPTORS)
            || properties.containsKey(CxfRsHttpListener.CXF_JAXRS_PREFIX + CxfUtil.IN_INTERCEPTORS)
            || properties.containsKey(CxfRsHttpListener.CXF_JAXRS_PREFIX + CxfUtil.OUT_FAULT_INTERCEPTORS)
            || properties.containsKey(CxfRsHttpListener.CXF_JAXRS_PREFIX + CxfUtil.OUT_INTERCEPTORS)
            || properties.containsKey(CxfRsHttpListener.CXF_JAXRS_PREFIX + CxfUtil.DATABINDING)
            || properties.containsKey(CxfRsHttpListener.CXF_JAXRS_PREFIX + CxfUtil.FEATURES)
            || properties.containsKey(CxfRsHttpListener.CXF_JAXRS_PREFIX + CxfUtil.ADDRESS)
            || properties.containsKey(CxfRsHttpListener.CXF_JAXRS_PREFIX + CxfUtil.ENDPOINT_PROPERTIES);
    }

    @Override
    protected RsHttpListener createHttpListener() {
        return new CxfRsHttpListener(destinationFactory, getWildcard());
    }

    private static class ContextLiteral extends EmptyAnnotationLiteral<Context> implements Context {
        private static final long serialVersionUID = 1L;

        public static final AnnotationLiteral<Context> INSTANCE = new ContextLiteral();
    }

    private static class ContextBean<T> implements Bean<T> {
        private final Class<T> type;
        private final Set<Type> types;
        private final Set<Annotation> qualifiers;
        private final T proxy;

        public ContextBean(final Class<T> type, final AbstractRestThreadLocalProxy<T> proxy) {
            this.type = type;
            this.proxy =
                (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{type, Serializable.class}, new DelegateHandler(proxy));
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
            return Collections.emptySet();
        }

        @Override
        public Class<?> getBeanClass() {
            return type;
        }

        @Override
        public Set<Class<? extends Annotation>> getStereotypes() {
            return Collections.emptySet();
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
            } catch (final InvocationTargetException ite) {
                throw ite.getCause();
            }
        }
    }
}

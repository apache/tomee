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
import org.apache.cxf.jaxrs.sse.SseContextProvider;
import org.apache.cxf.jaxrs.sse.SseEventSinkContextProvider;
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
import org.apache.openejb.threads.task.CUTask;
import org.apache.webbeans.annotation.AnyLiteral;
import org.apache.webbeans.annotation.EmptyAnnotationLiteral;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.BeanManagerImpl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.PassivationCapable;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Providers;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static java.util.Arrays.asList;

public class CxfRSService extends RESTService {

    private static final String NAME = "cxf-rs";
    private DestinationFactory destinationFactory;
    private boolean factoryByListener;
    private Properties config;

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
        if (!hasBean(beanManagerImpl, HttpServletResponse.class)) {
            beanManagerImpl.addInternalBean(new ContextBean<>(HttpServletResponse.class, ThreadLocalContextManager.HTTP_SERVLET_RESPONSE));
        }
        if (!hasBean(beanManagerImpl, HttpHeaders.class)) {
            beanManagerImpl.addInternalBean(new ContextBean<>(HttpHeaders.class, ThreadLocalContextManager.HTTP_HEADERS));
        }
        if (!hasBean(beanManagerImpl, Request.class)) {
            beanManagerImpl.addInternalBean(new ContextBean<>(Request.class, ThreadLocalContextManager.REQUEST));
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
        if (!hasBean(beanManagerImpl, ResourceInfo.class)) {
            beanManagerImpl.addInternalBean(new ContextBean<>(ResourceInfo.class, ThreadLocalContextManager.RESOURCE_INFO));
        }
        if (!hasBean(beanManagerImpl, ResourceContext.class)) {
            beanManagerImpl.addInternalBean(new ContextBean<>(ResourceContext.class, ThreadLocalContextManager.RESOURCE_CONTEXT));
        }
        if (!hasBean(beanManagerImpl, HttpServletRequest.class)) {
            beanManagerImpl.addInternalBean(new ContextBean<>(HttpServletRequest.class, ThreadLocalContextManager.HTTP_SERVLET_REQUEST));
        }
        if (!hasBean(beanManagerImpl, ServletRequest.class)) {
            beanManagerImpl.addInternalBean(new ContextBean<>(ServletRequest.class, ThreadLocalContextManager.SERVLET_REQUEST));
        }
        if (!hasBean(beanManagerImpl, ServletContext.class)) {
            beanManagerImpl.addInternalBean(new ContextBean<>(ServletContext.class, ThreadLocalContextManager.SERVLET_CONTEXT));
        }
        beanManagerImpl.getInjectionResolver().clearCaches(); // hasBean() usage can have cached several things
    }

    private static boolean hasBean(final BeanManagerImpl beanManagerImpl, final Class<?> type) {
        return beanManagerImpl.getInjectionResolver().implResolveByType(false, type).isEmpty();
    }

    @Override
    public void init(final Properties properties) throws Exception {
        super.init(properties);
        config = properties;
        factoryByListener = "true".equalsIgnoreCase(properties.getProperty("openejb.cxf-rs.factoryByListener", "false"));

        System.setProperty("org.apache.johnzon.max-string-length",
                SystemInstance.get().getProperty("org.apache.johnzon.max-string-length",
                        properties.getProperty("org.apache.johnzon.max-string-length", "8192")));

        SystemInstance.get().setComponent(RESTResourceFinder.class, new CxfRESTResourceFinder());

        try {
            CUTask.addContainerListener(new CUTask.ContainerListener() {
                @Override
                public Object onCreation() {
                    return Contexts.state();
                }

                @Override
                public Object onStart(final Object state) {
                    return Contexts.restore(state);
                }

                @Override
                public void onEnd(final Object oldState) {
                    Contexts.restore(oldState);
                }
            });
        } catch(final Throwable th) {
            // unlikely but means the container core has been customized so just ignore it
        }

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
            initCxfProviders(bus);
        } finally {
            if (oldLoader != null) {
                CxfUtil.clearBusLoader(oldLoader);
            }
        }
    }

    private void initCxfProviders(final Bus bus) {
        if (noProvidersExplicitlyAdded(bus)) {
            bus.setProperty("skip.default.json.provider.registration", "true"); // client jaxrs, we want johnzon not jettison

            final Collection<Object> defaults = new ArrayList<>();
            List<String> jsonProviders;
            String userConfiguredJsonProviders = SystemInstance.get().getProperty("openejb.jaxrs.jsonProviders");
            if (userConfiguredJsonProviders == null) {
                jsonProviders = Collections.emptyList();
            } else {
                jsonProviders = asList(userConfiguredJsonProviders.split(","));
            }
            for (final String provider : jsonProviders) {
                if (!isActive(provider)) {
                    continue;
                }
                try {
                    defaults.add(Class.forName(provider, true, CxfRSService.class.getClassLoader()).newInstance());
                } catch (final Exception e) {
                    // no-op
                }
            }

            try {
                final List<Object> all;
                final String userProviders = SystemInstance.get().getProperty("openejb.jaxrs.client.providers");
                if (userProviders == null) {
                    (all = new ArrayList<>(defaults.size())).addAll(defaults);
                } else {
                    all = new ArrayList<>(defaults.size() + 2 /* blind guess */);
                    for (String p : userProviders.split(" *, *")) {
                        p = p.trim();
                        if (p.isEmpty()) {
                            continue;
                        }

                        all.add(Thread.currentThread().getContextClassLoader().loadClass(p).newInstance());
                    }

                    // added after to be after in the list once sorted
                    all.addAll(defaults);
                }
                bus.setProperty("org.apache.cxf.jaxrs.bus.providers", all);
            } catch (final Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private boolean noProvidersExplicitlyAdded(final Bus bus) {
        final Object property = bus.getProperty("org.apache.cxf.jaxrs.bus.providers");

        final Set<Class> currentProviders = new HashSet<>();

        if (property instanceof List) {
            for (final Object item : List.class.cast(property)) {
                if (item != null) {
                    currentProviders.add(item.getClass());
                }
            }
        }

        currentProviders.remove(SseContextProvider.class);
        currentProviders.remove(SseEventSinkContextProvider.class);

        return currentProviders.isEmpty();
    }

    @Override
    public void stop() throws ServiceException {
        super.stop();
        CxfUtil.release();
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
        return new CxfRsHttpListener(!factoryByListener ? destinationFactory : new HTTPTransportFactory(), getWildcard(), this);
    }

    public boolean isActive(final String name) {
        final String key = name + ".activated";
        return "true".equalsIgnoreCase(SystemInstance.get().getProperty(key, config.getProperty(key, "true")));
    }

    private static class ContextLiteral extends EmptyAnnotationLiteral<Context> implements Context {
        private static final long serialVersionUID = 1L;

        public static final AnnotationLiteral<Context> INSTANCE = new ContextLiteral();
    }

    private static class ContextBean<T> implements Bean<T>, PassivationCapable {
        private final Class<T> type;
        private final Set<Type> types;
        private final Set<Annotation> qualifiers;
        private final T proxy;
        private final String id;

        public ContextBean(final Class<T> type, final AbstractRestThreadLocalProxy<T> proxy) {
            this.type = type;
            this.proxy =
                (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{type, Serializable.class}, new DelegateHandler(proxy));
            this.types = new HashSet<Type>(asList(Object.class, type));
            this.qualifiers = new HashSet<Annotation>(asList(ContextLiteral.INSTANCE, AnyLiteral.INSTANCE));
            this.id = ContextBean.class.getName() + "#" + type.getName();
        }

        @Override
        public String getId() {
            return id;
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

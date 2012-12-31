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

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.lifecycle.ResourceProvider;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
import org.apache.cxf.jaxrs.provider.JAXBElementProvider;
import org.apache.cxf.jaxrs.provider.json.JSONProvider;
import org.apache.cxf.service.invoker.Invoker;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.apache.cxf.transport.http.HTTPTransportFactory;
import org.apache.openejb.BeanContext;
import org.apache.openejb.Injection;
import org.apache.openejb.assembler.classic.ServiceInfo;
import org.apache.openejb.assembler.classic.util.ServiceConfiguration;
import org.apache.openejb.assembler.classic.util.ServiceInfos;
import org.apache.openejb.core.interceptor.InterceptorData;
import org.apache.openejb.server.cxf.transport.util.CxfUtil;
import org.apache.openejb.server.httpd.HttpRequest;
import org.apache.openejb.server.httpd.HttpRequestImpl;
import org.apache.openejb.server.httpd.HttpResponse;
import org.apache.openejb.server.rest.EJBRestServiceInfo;
import org.apache.openejb.server.rest.InternalApplication;
import org.apache.openejb.server.rest.RsHttpListener;
import org.apache.openejb.util.Classes;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.proxy.ProxyEJB;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.xbean.finder.AnnotationFinder;
import org.apache.xbean.finder.archive.ClassesArchive;

import javax.naming.Context;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.ws.rs.core.Application;
import javax.xml.bind.Marshaller;
import java.lang.reflect.Field;
import java.util.*;

public class CxfRsHttpListener implements RsHttpListener {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB_RS, CxfRsHttpListener.class);

    public static final String CXF_JAXRS_PREFIX = "cxf.jaxrs.";
    public static final String PROVIDERS_KEY = CXF_JAXRS_PREFIX + "providers";
    private static final String STATIC_SUB_RESOURCE_RESOLUTION_KEY = "staticSubresourceResolution";

    private HTTPTransportFactory transportFactory;
    private AbstractHTTPDestination destination;
    private Server server;
    private String context = "";

    public CxfRsHttpListener(HTTPTransportFactory httpTransportFactory) {
        transportFactory = httpTransportFactory;
    }

    @Override
    public void onMessage(final HttpRequest httpRequest, final HttpResponse httpResponse) throws Exception {
        destination.invoke(null, httpRequest.getServletContext(), new HttpServletRequestWrapper(httpRequest) {
            // see org.apache.cxf.jaxrs.utils.HttpUtils.getPathToMatch()
            // cxf uses implicitly getRawPath() from the endpoint but not for the request URI
            // so without stripping the address until the context the behavior is weird
            // this is just a workaround waiting for something better
            @Override
            public String getRequestURI() {
                if (httpRequest instanceof HttpRequestImpl) {
                    return strip(context, ((HttpRequestImpl) httpRequest).requestRawPath());
                }
                return strip(context, super.getRequestURI());
            }
        }, httpResponse);
    }

    private String strip(final String context, final String requestURI) {
        if (requestURI.startsWith(context)) {
            return requestURI.substring(context.length());
        }
        return requestURI;
    }

    @Override
    public void deploySingleton(String fullContext, Object o, Application appInstance,
                                Collection<Object> additionalProviders, ServiceConfiguration configuration) {
        deploy(o.getClass(), fullContext, new SingletonResourceProvider(o), o, appInstance, null, additionalProviders, configuration);
    }

    @Override
    public void deployPojo(String fullContext, Class<?> loadedClazz, Application app, Collection<Injection> injections,
                           Context context, WebBeansContext owbCtx, Collection<Object> additionalProviders, ServiceConfiguration configuration) {
        deploy(loadedClazz, fullContext, new OpenEJBPerRequestPojoResourceProvider(loadedClazz, injections, context, owbCtx),
                            null, app, null, additionalProviders, configuration);
    }

    @Override
    public void deployEJB(String fullContext, BeanContext beanContext, Collection<Object> additionalProviders, ServiceConfiguration configuration) {
        final Object proxy = ProxyEJB.subclassProxy(beanContext);

        addContextTypes(beanContext);

        deploy(beanContext.getBeanClass(), fullContext, new NoopResourceProvider(beanContext.getBeanClass(), proxy),
                proxy, null, new OpenEJBEJBInvoker(), additionalProviders, configuration);
    }

    private void addContextTypes(BeanContext beanContext) {
        final Set<Class<?>> classes = new HashSet<Class<?>>();
        classes.addAll(Classes.ancestors(beanContext.getBeanClass()));
        for (InterceptorData interceptorData : beanContext.getInstanceScopedInterceptors()) {
            classes.addAll(Classes.ancestors(interceptorData.getInterceptorClass()));
        }

        // We really shouldn't do this here -- should be done in the AnnotationDeployer where we already payed for the AnnotationFinder
        final AnnotationFinder finder = new AnnotationFinder(new ClassesArchive(classes));
        final List<Field> fields = finder.findAnnotatedFields(javax.ws.rs.core.Context.class);
        final Set<Class<?>> contextTypes = new HashSet<Class<?>>();
        for (Field field : fields) {
            contextTypes.add(field.getType());
        }

        beanContext.set(ContextReferenceTypes.class, new ContextReferenceTypes(contextTypes));
    }

    private void deploy(Class<?> clazz, String address, ResourceProvider rp, Object serviceBean, Application app, Invoker invoker,
                        Collection<Object> additionalProviders, ServiceConfiguration configuration) {
        final String impl;
        if (serviceBean != null) {
            impl = serviceBean.getClass().getName();
        } else {
            impl = clazz.getName();
        }

        final JAXRSServerFactoryBean factory = newFactory(address);
        configureFactory(impl, additionalProviders, configuration, factory);
        factory.setResourceClasses(clazz);


        if (rp != null) {
            factory.setResourceProvider(rp);
        }
        if (app != null) {
            factory.setApplication(app);
        }
        if (invoker != null) {
            factory.setInvoker(invoker);
        }
        if (serviceBean != null) {
            factory.setServiceBean(serviceBean);
        } else {
            factory.setServiceClass(clazz);
        }

        server = factory.create();
        destination = (AbstractHTTPDestination) server.getDestination();
    }

    private Collection<Object> providers(final Collection<ServiceInfo> services, final Collection<Object> additionalProviders) {
        final Collection<Object> instances = new ArrayList<Object>();
        for (Object o : additionalProviders) {
            if (o instanceof Class<?>) {
                final Class<?> clazz = (Class<?>) o;
                final Object instance = ServiceInfos.resolve(services, clazz.getName());
                if (instance != null) {
                    instances.add(instance);
                } else {
                    try {
                        instances.add(clazz.newInstance());
                    } catch (Exception e) {
                        LOGGER.error("can't instantiate " + clazz.getName(), e);
                    }
                }
            } else {
                instances.add(o);
            }
        }
        return instances;
    }

    public void undeploy() {
        server.stop();
    }

    @Override
    public void deployApplication(final Application application, final String prefix, final String webContext,
                                  final Collection<Object> additionalProviders,
                                  final Map<String, EJBRestServiceInfo> restEjbs, final ClassLoader classLoader,
                                  final Collection<Injection> injections, final Context context, final WebBeansContext owbCtx,
                                  final ServiceConfiguration serviceConfiguration) {
        final JAXRSServerFactoryBean factory = newFactory(prefix);
        if (InternalApplication.class.equals(application.getClass())) { // todo: check it is the good choice
            configureFactory("jaxrs-application", additionalProviders, serviceConfiguration, factory);
        } else {
            configureFactory(application.getClass().getName(), additionalProviders, serviceConfiguration, factory);
        }
        factory.setApplication(application);

        final List<Class<?>> classes = new ArrayList<Class<?>>();

        for (Class<?> clazz : application.getClasses()) {
            classes.add(clazz);
        }

        for (Object o : application.getSingletons()) {
            final Class<?> clazz = o.getClass();
            classes.add(clazz);
        }

        for (Class<?> clazz : classes) {
            final String name = clazz.getName();
            if (restEjbs.containsKey(name)) {
                final BeanContext bc = restEjbs.get(name).context;
                final Object proxy = ProxyEJB.subclassProxy(bc);
                addContextTypes(bc);
                factory.setResourceProvider(clazz, new NoopResourceProvider(bc.getBeanClass(), proxy));
            } else {
                factory.setResourceProvider(clazz, new OpenEJBPerRequestPojoResourceProvider(clazz, injections, context, owbCtx));
            }
        }

        factory.setResourceClasses(classes);
        factory.setInvoker(new AutoJAXRSInvoker(restEjbs));

        server = factory.create();
        this.context = webContext;
        if (!webContext.startsWith("/")) {
            this.context = "/" + webContext;
        }
        destination = (AbstractHTTPDestination) server.getDestination();
    }

    private JAXRSServerFactoryBean newFactory(String prefix) {
        final JAXRSServerFactoryBean factory = new JAXRSServerFactoryBean();
        factory.setDestinationFactory(transportFactory);
        factory.setBus(transportFactory.getBus());
        factory.setAddress(prefix);
        return factory;
    }

    private void configureFactory(String application, Collection<Object> additionalProviders, ServiceConfiguration serviceConfiguration, JAXRSServerFactoryBean factory) {
        CxfUtil.configureEndpoint(factory, serviceConfiguration, CXF_JAXRS_PREFIX, application);

        final Collection<ServiceInfo> services = serviceConfiguration.getAvailableServices();

        final String staticSubresourceResolution = serviceConfiguration.getProperties().getProperty(STATIC_SUB_RESOURCE_RESOLUTION_KEY);
        if (staticSubresourceResolution != null) {
            factory.setStaticSubresourceResolution("true".equalsIgnoreCase(staticSubresourceResolution));
        }

        // providers
        final String provider = serviceConfiguration.getProperties().getProperty(PROVIDERS_KEY);
        List<Object> providers = null;
        if (provider != null) {
            providers = ServiceInfos.resolve(services, provider.split(","));
            if (providers != null && additionalProviders != null && !additionalProviders.isEmpty()) {
                providers.addAll(providers(services, additionalProviders));
            }
            factory.setProviders(providers);
        }
        if (providers == null) {
            providers = new ArrayList<Object>();
            if (additionalProviders != null && !additionalProviders.isEmpty()) {
                providers.addAll(providers(services, additionalProviders));
            } else {
                providers.addAll(defaultProviders());
            }
            factory.setProviders(providers);
        }
    }

    private static List<Object> defaultProviders() {
        final JAXBElementProvider jaxb = new JAXBElementProvider();
        final Map<String, Object> jaxbProperties = new HashMap<String, Object> ();
        jaxbProperties.put(Marshaller.JAXB_FRAGMENT, true);
        jaxb.setMarshallerProperties(jaxbProperties);

        final JSONProvider json = new JSONProvider();
        // TOMEE-514
        // json.setSerializeAsArray(true);

        return Arrays.asList((Object) jaxb, json);
    }
}

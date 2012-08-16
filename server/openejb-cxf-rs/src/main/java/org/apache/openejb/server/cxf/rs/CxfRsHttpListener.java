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
import org.apache.openejb.assembler.classic.util.ServiceInfos;
import org.apache.openejb.server.cxf.transport.util.CxfUtil;
import org.apache.openejb.server.httpd.HttpRequest;
import org.apache.openejb.server.httpd.HttpRequestImpl;
import org.apache.openejb.server.httpd.HttpResponse;
import org.apache.openejb.server.rest.RsHttpListener;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.webbeans.config.WebBeansContext;

import javax.naming.Context;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.ws.rs.core.Application;
import javax.xml.bind.Marshaller;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CxfRsHttpListener implements RsHttpListener {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB_RS, CxfRsHttpListener.class);

    public static final String CXF_JAXRS_PREFIX = "cxf.jaxrs.";
    public static final String PROVIDERS_KEY = CXF_JAXRS_PREFIX + "providers";

    private HTTPTransportFactory transportFactory;
    private AbstractHTTPDestination destination;
    private Server server;

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
                    return ((HttpRequestImpl) httpRequest).requestRawPath();
                }
                return super.getRequestURI();
            }
        }, httpResponse);
    }

    @Override
    public void deploySingleton(String fullContext, Object o, Application appInstance,
                                Collection<Class<?>> additionalProviders, Collection<ServiceInfo> services) {
        deploy(o.getClass(), fullContext, new SingletonResourceProvider(o), o, appInstance, null, additionalProviders, services);
    }

    @Override
    public void deployPojo(String fullContext, Class<?> loadedClazz, Application app, Collection<Injection> injections,
                           Context context, WebBeansContext owbCtx, Collection<Class<?>> additionalProviders, Collection<ServiceInfo> services) {
        deploy(loadedClazz, fullContext, new OpenEJBPerRequestPojoResourceProvider(loadedClazz, injections, context, owbCtx),
                            null, app, null, additionalProviders, services);
    }

    @Override
    public void deployEJB(String fullContext, BeanContext beanContext, Collection<Class<?>> additionalProviders, Collection<ServiceInfo> services) {
        deploy(beanContext.getBeanClass(), fullContext, null, null, null, new OpenEJBEJBInvoker(beanContext), additionalProviders, services);
    }

    private void deploy(Class<?> clazz, String address, ResourceProvider rp, Object serviceBean, Application app, Invoker invoker,
                        Collection<Class<?>> additionalProviders, Collection<ServiceInfo> services) {
        final String impl;
        if (serviceBean != null) {
            impl = serviceBean.getClass().getName();
        } else {
            impl = clazz.getName();
        }

        final JAXRSServerFactoryBean factory = new JAXRSServerFactoryBean();
        factory.setResourceClasses(clazz);
        factory.setDestinationFactory(transportFactory);
        factory.setBus(transportFactory.getBus());
        factory.setAddress(address);

        CxfUtil.configureEndpoint(factory, services, CXF_JAXRS_PREFIX, impl);

        // providers
        final ServiceInfo info = ServiceInfos.findByClass(services, impl);
        List<Object> providers = null;
        if (info != null) {
            final String provider = info.properties.getProperty(PROVIDERS_KEY);
            if (provider != null) {
                providers = ServiceInfos.resolve(services, provider.split(","));
                if (providers != null && additionalProviders != null && !additionalProviders.isEmpty()) {
                    providers.addAll(providers(services, info, additionalProviders));
                }
                factory.setProviders(providers);
            }
        }
        if (providers == null) {
            providers = new ArrayList<Object>();
            providers.addAll(defaultProviders());
            if (additionalProviders != null && !additionalProviders.isEmpty()) {
                providers.addAll(providers(services, info, additionalProviders));
            }
            factory.setProviders(providers);
        }

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

    private Collection<Object> providers(final Collection<ServiceInfo> services, final ServiceInfo service, final Collection<Class<?>> additionalProviders) {
        final Collection<Object> instances = new ArrayList<Object>();

        final String prefix;
        if (service != null) {
            prefix = service.id + ".";
        } else {
            prefix = "";
        }

        for (Class<?> clazz : additionalProviders) {
            final Object instance = ServiceInfos.resolve(services, prefix + clazz.getName());
            if (instance != null) {
                instances.add(instance);
            } else {
                try {
                    instances.add(clazz.newInstance());
                } catch (Exception e) {
                    LOGGER.error("can't instantiate " + clazz.getName(), e);
                }
            }
        }
        return instances;
    }

    public void undeploy() {
        server.stop();
    }

    private static List<Object> defaultProviders() {
        final JAXBElementProvider jaxb = new JAXBElementProvider();
        final Map<String, Object> jaxbProperties = new HashMap<String, Object> ();
        jaxbProperties.put(Marshaller.JAXB_FRAGMENT, true);
        jaxb.setMarshallerProperties(jaxbProperties);

        final JSONProvider json = new JSONProvider();
        json.setSerializeAsArray(true);

        return Arrays.asList((Object) jaxb, json);
    }
}

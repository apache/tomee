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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.naming.Context;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.ws.rs.core.Application;
import javax.xml.bind.Marshaller;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.lifecycle.ResourceProvider;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
import org.apache.cxf.jaxrs.provider.JAXBElementProvider;
import org.apache.cxf.jaxrs.provider.JSONProvider;
import org.apache.cxf.service.invoker.Invoker;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.apache.cxf.transport.http.HTTPTransportFactory;
import org.apache.openejb.BeanContext;
import org.apache.openejb.Injection;
import org.apache.openejb.server.httpd.HttpRequest;
import org.apache.openejb.server.httpd.HttpResponse;
import org.apache.openejb.server.rest.RsHttpListener;

/**
 * @author Romain Manni-Bucau
 */
public class CxfRsHttpListener implements RsHttpListener {
    private static final List<?> PROVIDERS = createProviderList();

    private HTTPTransportFactory transportFactory;
    private AbstractHTTPDestination destination;
    private Server server;

    public CxfRsHttpListener(HTTPTransportFactory httpTransportFactory) {
        transportFactory = httpTransportFactory;
    }

    @Override public void onMessage(final HttpRequest httpRequest, final HttpResponse httpResponse) throws Exception {
        destination.invoke(null, httpRequest.getServletContext(), new HttpServletRequestWrapper(httpRequest) {
            // see org.apache.cxf.jaxrs.utils.HttpUtils.getPathToMatch()
            // cxf uses implicitly getRawPath() from the endpoint but not for the request URI
            // so without stripping the address until the context the behavior is weird
            // this is just a workaround waiting for something better
            @Override public String getRequestURI() {
                try {
                    return new URI(httpRequest.getRequestURI()).getRawPath();
                } catch (URISyntaxException e) {
                    return "/";
                }
            }
        }, httpResponse);

    }

    @Override public void deploySingleton(String fullContext, Object o, Application appInstance) {
        deploy(o.getClass(), fullContext, new SingletonResourceProvider(o), o, appInstance, null);
    }

    @Override public void deployPojo(String fullContext, Class<?> loadedClazz, Application app, Collection<Injection> injections, Context context) {
        deploy(loadedClazz, fullContext, new OpenEJBPerRequestPojoResourceProvider(loadedClazz, injections, context), null, app, null);
    }

    @Override public void deployEJB(String fullContext, BeanContext beanContext) {
        deploy(beanContext.getBeanClass(), fullContext, null, null, null, new OpenEJBEJBInvoker(beanContext));
    }

    private void deploy(Class<?> clazz, String address, ResourceProvider rp, Object serviceBean, Application app, Invoker invoker) {
        JAXRSServerFactoryBean factory = new JAXRSServerFactoryBean();
        factory.setResourceClasses(clazz);
        factory.setDestinationFactory(transportFactory);
        factory.setBus(transportFactory.getBus());
        factory.setAddress(address);
        factory.setProviders(PROVIDERS);
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

    public void undeploy() {
        server.stop();
    }

    private static List<?> createProviderList() {
        JAXBElementProvider jaxb = new JAXBElementProvider();
        Map<String, Object> jaxbProperties = new HashMap<String, Object> ();
        jaxbProperties.put(Marshaller.JAXB_FRAGMENT, true);
        jaxb.setMarshallerProperties(jaxbProperties);

        JSONProvider json = new JSONProvider();
        json.setSerializeAsArray(true);

        return Arrays.asList(jaxb, json);
    }
}

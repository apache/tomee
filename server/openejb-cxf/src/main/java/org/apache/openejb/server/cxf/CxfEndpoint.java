/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.server.cxf;

import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.endpoint.ServerImpl;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.cxf.jaxws.handler.PortInfoImpl;
import org.apache.cxf.jaxws.support.JaxWsEndpointImpl;
import org.apache.cxf.jaxws.support.JaxWsImplementorInfo;
import org.apache.cxf.jaxws.support.JaxWsServiceFactoryBean;
import org.apache.cxf.service.Service;
import org.apache.openejb.core.webservices.HandlerResolverImpl;
import org.apache.openejb.core.webservices.PortData;
import org.w3c.dom.Element;

import javax.xml.transform.Source;
import javax.xml.ws.Binding;
import javax.xml.ws.Endpoint;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.soap.SOAPBinding;
import javax.naming.Context;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

public abstract class CxfEndpoint extends Endpoint {
    protected Bus bus;

    protected PortData port;

    protected Context context;

    protected Object implementor;

    protected Server server;

    protected Service service;

    protected JaxWsImplementorInfo implInfo;

    protected JaxWsServiceFactoryBean serviceFactory;

    protected HandlerResolverImpl handlerResolver;

    public CxfEndpoint(Bus bus, PortData port, Context context, Object implementor) {
        this.bus = bus;
        this.port = port;
        this.implementor = implementor;
        this.context = context;
    }

    protected URL getWsdlURL(URL configurationBaseUrl, String wsdlFile) {
        URL wsdlURL = null;
        if (wsdlFile != null && wsdlFile.trim().length() > 0) {
            wsdlFile = wsdlFile.trim();
            try {
                wsdlURL = new URL(wsdlFile);
            } catch (MalformedURLException e) {
                // Not a URL, try as a resource
                wsdlURL = getImplementorClass().getResource("/" + wsdlFile);

                if (wsdlURL == null && configurationBaseUrl != null) {
                    // Cannot get it as a resource, try with
                    // configurationBaseUrl
                    try {
                        wsdlURL = new URL(configurationBaseUrl, wsdlFile);
                    } catch (MalformedURLException ee) {
                        // ignore
                    }
                }
            }
        }
        return wsdlURL;
    }

    protected Class getImplementorClass() {
        return this.implementor.getClass();
    }

    protected org.apache.cxf.endpoint.Endpoint getEndpoint() {
        return getServer().getEndpoint();
    }

    public boolean isSOAP11() {
        return SOAPBinding.SOAP11HTTP_BINDING.equals(implInfo.getBindingType()) ||
                SOAPBinding.SOAP11HTTP_MTOM_BINDING.equals(implInfo.getBindingType());
    }

    public ServerImpl getServer() {
        return (ServerImpl) server;
    }

    public Binding getBinding() {
        return ((JaxWsEndpointImpl) getEndpoint()).getJaxwsBinding();
    }

    public void setExecutor(Executor executor) {
        service.setExecutor(executor);
    }

    public Executor getExecutor() {
        return service.getExecutor();
    }

    public Object getImplementor() {
        return implementor;
    }

    public List<Source> getMetadata() {
        return null;
    }

    public Map<String, Object> getProperties() {
        return null;
    }

    public boolean isPublished() {
        return server != null;
    }

    public void publish(Object arg0) {
    }

    public void publish(String address) {
        doPublish(address);
    }

    public void setMetadata(List<Source> arg0) {
    }

    public void setProperties(Map<String, Object> arg0) {
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public EndpointReference getEndpointReference(Element... referenceParameters) {
        throw new UnsupportedOperationException("JaxWS 2.1 APIs are not supported");
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public <T extends EndpointReference> T getEndpointReference(Class<T> clazz, Element... referenceParameters) {
        throw new UnsupportedOperationException("JaxWS 2.1 APIs are not supported");
    }

    private static class NoInitJaxWsServerFactoryBean extends JaxWsServerFactoryBean {
        public NoInitJaxWsServerFactoryBean() {
            // disable CXF resource injection
            doInit = false;
        }
    }

    protected void doPublish(String address) {
        JaxWsServerFactoryBean svrFactory = new NoInitJaxWsServerFactoryBean();
        svrFactory.setBus(bus);
        svrFactory.setAddress(address);
        svrFactory.setServiceFactory(serviceFactory);
        svrFactory.setStart(false);
        svrFactory.setServiceBean(implementor);

        if (HTTPBinding.HTTP_BINDING.equals(implInfo.getBindingType())) {
            svrFactory.setTransportId("http://cxf.apache.org/bindings/xformat");
        }

        server = svrFactory.create();

        init();

        // todo do we need to call this?
        getEndpoint();

        if (getBinding() instanceof SOAPBinding) {
            ((SOAPBinding) getBinding()).setMTOMEnabled(port.isMtomEnabled());
        }

        server.start();
    }

    protected void init() {
    }

    /**
     * Set appropriate handlers for the port/service/bindings.
     */
    protected void initHandlers() throws Exception {
        PortInfoImpl portInfo = new PortInfoImpl(implInfo.getBindingType(), serviceFactory.getEndpointName(), service.getName());

        handlerResolver = new HandlerResolverImpl(port.getHandlerChains(), port.getInjections(), context);
        List<Handler> chain = handlerResolver.getHandlerChain(portInfo);

        getBinding().setHandlerChain(chain);
    }

    protected void destroyHandlers() {
        if (this.handlerResolver != null) {
            handlerResolver.destroyHandlers();
            handlerResolver = null;
        }
    }

    public void stop() {
        // shutdown server
        if (this.server != null) {
            this.server.stop();
        }
    }
}

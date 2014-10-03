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
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.databinding.DataBinding;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.endpoint.ServerImpl;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.cxf.jaxws.handler.PortInfoImpl;
import org.apache.cxf.jaxws.support.JaxWsEndpointImpl;
import org.apache.cxf.jaxws.support.JaxWsImplementorInfo;
import org.apache.cxf.jaxws.support.JaxWsServiceFactoryBean;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.factory.ReflectionServiceFactoryBean;
import org.apache.cxf.transport.http.HTTPTransportFactory;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.assembler.classic.ServiceInfo;
import org.apache.openejb.assembler.classic.util.ServiceConfiguration;
import org.apache.openejb.assembler.classic.util.ServiceInfos;
import org.apache.openejb.core.webservices.HandlerResolverImpl;
import org.apache.openejb.core.webservices.PortData;
import org.apache.openejb.server.cxf.transport.util.CxfUtil;

import javax.naming.Context;
import javax.xml.transform.Source;
import javax.xml.ws.Binding;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.soap.SOAPBinding;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class CxfEndpoint {
    public static final String CXF_JAXWS_PREFIX = "cxf.jaxws.";

    // to be able to switch off logs we don't want
    protected static final Logger FACTORY_BEAN_LOG = LogUtils.getLogger(ReflectionServiceFactoryBean.class);
    private static final Logger SERVER_IMPL_LOGGER = LogUtils.getL7dLogger(ServerImpl.class);

    protected Bus bus;

    protected PortData port;

    protected Context context;

    protected Object implementor;

    protected Server server;

    protected Service service;

    protected JaxWsImplementorInfo implInfo;

    protected JaxWsServiceFactoryBean serviceFactory;

    protected HandlerResolverImpl handlerResolver;

    protected HTTPTransportFactory httpTransportFactory;

    protected ServiceConfiguration serviceConfiguration;

    public CxfEndpoint(final Bus bus, final PortData port, final Context context,
                       final Object implementor, final HTTPTransportFactory httpTransportFactory,
                       final ServiceConfiguration configuration) {
        this.bus = bus;
        this.port = port;
        this.context = context;
        this.implementor = implementor;
        this.httpTransportFactory = httpTransportFactory;
        this.serviceConfiguration = configuration;
        this.bus.setExtension(this, CxfEndpoint.class);
    }

    protected Service doServiceCreate() {
        final Level level = FACTORY_BEAN_LOG.getLevel();
        try {
            FACTORY_BEAN_LOG.setLevel(Level.SEVERE);
        } catch (final UnsupportedOperationException uoe) {
             // no-op
        }
        try {
            service = serviceFactory.create();
        } finally {
            try {
                FACTORY_BEAN_LOG.setLevel(level);
            } catch (final UnsupportedOperationException uoe) {
                 // no-op
            }
        }
        return service;
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

    public boolean isHTTP() {
        return HTTPBinding.HTTP_BINDING.equals(implInfo.getBindingType());
    }

    public ServerImpl getServer() {
        return (ServerImpl) server;
    }

    public Binding getBinding() {
        return ((JaxWsEndpointImpl) getEndpoint()).getJaxwsBinding();
    }

    public void setExecutor(final Executor executor) {
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

    public void publish(final Object arg0) {
    }

    public void publish(final String address) {
        doPublish(address);
    }

    public void setMetadata(final List<Source> arg0) {
    }

    public void setProperties(final Map<String, Object> arg0) {
    }

    private class NoInitJaxWsServerFactoryBean extends JaxWsServerFactoryBean {
        public NoInitJaxWsServerFactoryBean() {
            // disable CXF resource injection
            doInit = false;
        }
    }

    protected void doPublish(final String address) {
        final JaxWsServerFactoryBean svrFactory = new NoInitJaxWsServerFactoryBean();
        svrFactory.setBus(bus);
        svrFactory.setAddress(address);
        svrFactory.setServiceFactory(serviceFactory);
        svrFactory.setStart(false);
        svrFactory.setServiceBean(implementor);
        svrFactory.setDestinationFactory(httpTransportFactory);
        svrFactory.setServiceClass(serviceFactory.getServiceClass());

        final Properties beanConfig = serviceConfiguration.getProperties();

        // endpoint properties
        if (beanConfig != null) {
            final String schemaLocations = beanConfig.getProperty(CXF_JAXWS_PREFIX + "schema-locations");
            if (schemaLocations != null) {
                svrFactory.setSchemaLocations(Arrays.asList(schemaLocations.split(",")));
            }
            final String wsdlLocation = beanConfig.getProperty(CXF_JAXWS_PREFIX + "wsdl-location");
            if (wsdlLocation != null) {
                svrFactory.setWsdlLocation(wsdlLocation);
            }
        }

        // look for bean info if exists
        CxfUtil.configureEndpoint(svrFactory, serviceConfiguration, CXF_JAXWS_PREFIX);

        if (HTTPBinding.HTTP_BINDING.equals(implInfo.getBindingType())) {
            svrFactory.setTransportId("http://cxf.apache.org/bindings/xformat");
        }

        final Level level = SERVER_IMPL_LOGGER.getLevel();
        try {
            SERVER_IMPL_LOGGER.setLevel(Level.SEVERE);
        } catch (final UnsupportedOperationException uoe) {
            // no-op
        }
        try {
            server = svrFactory.create();
        } finally {
            try {
                SERVER_IMPL_LOGGER.setLevel(level);
            } catch (final UnsupportedOperationException uoe) {
                // no-op
            }
        }

        init();

        if (getBinding() instanceof SOAPBinding) {
            ((SOAPBinding) getBinding()).setMTOMEnabled(port.isMtomEnabled());
        }

        server.start();
    }

    protected void init() {
        // no-op
    }

    /**
     * Set appropriate handlers for the port/service/bindings.
     */
    protected void initHandlers() throws Exception {
        final PortInfoImpl portInfo = new PortInfoImpl(implInfo.getBindingType(), serviceFactory.getEndpointName(), service.getName());

        handlerResolver = new HandlerResolverImpl(port.getHandlerChains(), port.getInjections(), context);
        final List<Handler> chain = handlerResolver.getHandlerChain(portInfo);

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

    protected static JaxWsServiceFactoryBean configureService(final JaxWsServiceFactoryBean serviceFactory, final ServiceConfiguration configuration, final String prefix) {
        final Properties beanConfig = configuration.getProperties();
        if (beanConfig == null || beanConfig.isEmpty()) {
            return serviceFactory;
        }

        final Collection<ServiceInfo> availableServices = configuration.getAvailableServices();

        // databinding
        final String databinding = beanConfig.getProperty(prefix + CxfUtil.DATABINDING);
        if (databinding != null && !databinding.trim().isEmpty()) {
            Object instance = ServiceInfos.resolve(availableServices, databinding);
            if (instance == null) {  // maybe id == classname
                try {
                    instance = Thread.currentThread().getContextClassLoader().loadClass(databinding).newInstance();
                } catch (final Exception e) {
                    // ignore
                }
            }
            if (!DataBinding.class.isInstance(instance)) {
                throw new OpenEJBRuntimeException(instance + " is not a " + DataBinding.class.getName()
                    + ", please check configuration of service [id=" + databinding + "]");
            }
            serviceFactory.setDataBinding((DataBinding) instance);
        }

        return serviceFactory;
    }
}

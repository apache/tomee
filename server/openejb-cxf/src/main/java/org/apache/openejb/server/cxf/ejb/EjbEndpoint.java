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
package org.apache.openejb.server.cxf.ejb;

import org.apache.cxf.Bus;
import org.apache.cxf.binding.soap.SoapBinding;
import org.apache.cxf.binding.soap.interceptor.MustUnderstandInterceptor;
import org.apache.cxf.binding.soap.saaj.SAAJInInterceptor;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.jaxws.handler.logical.LogicalHandlerInInterceptor;
import org.apache.cxf.jaxws.handler.soap.SOAPHandlerInterceptor;
import org.apache.cxf.jaxws.support.JaxWsServiceFactoryBean;
import org.apache.cxf.transport.DestinationFactory;
import org.apache.openejb.BeanContext;
import org.apache.openejb.assembler.classic.util.ServiceConfiguration;
import org.apache.openejb.core.webservices.JaxWsUtils;
import org.apache.openejb.core.webservices.PortData;
import org.apache.openejb.server.cxf.ConfigureCxfSecurity;
import org.apache.openejb.server.cxf.CxfEndpoint;
import org.apache.openejb.server.cxf.CxfServiceConfiguration;
import org.apache.openejb.server.cxf.JaxWsImplementorInfoImpl;

import jakarta.xml.ws.WebServiceException;
import java.util.List;

/**
 * A web service endpoint which invokes an EJB container.
 */
public class EjbEndpoint extends CxfEndpoint {
    private final BeanContext beanContext;

    public EjbEndpoint(Bus bus, PortData portData, BeanContext beanContext, DestinationFactory transportFactory, ServiceConfiguration config) {
        super(bus, portData, beanContext.getJndiEnc(), beanContext.getBeanClass(), transportFactory, config);
        this.beanContext = beanContext;

        String bindingURI = JaxWsUtils.getBindingURI(portData.getBindingID());
        implInfo = new JaxWsImplementorInfoImpl((Class) implementor, bindingURI);

        serviceFactory = configureService(new JaxWsServiceFactoryBean(implInfo), config, CXF_JAXWS_PREFIX);
        serviceFactory.setBus(bus);
        serviceFactory.setServiceClass(beanContext.getBeanClass());

        // install as first to overwrite annotations (wsdl-file, wsdl-port, wsdl-service)
        CxfServiceConfiguration configuration = new CxfServiceConfiguration(portData);
        serviceFactory.getConfigurations().add(0, configuration);

        service = doServiceCreate();
    }

    protected Class getImplementorClass() {
        return (Class) this.implementor;
    }

    protected void init() {
        // configure handlers
        try {
            initHandlers();
        } catch (Exception e) {
            throw new WebServiceException("Error configuring handlers", e);
        }

        // Set service to invoke the target ejb
        service.setInvoker(new EjbMethodInvoker(this.bus, beanContext));

        // Remove interceptors that perform handler processing since
        // handler processing must happen within the EJB container.
        Endpoint endpoint = getEndpoint();
        removeHandlerInterceptors(bus.getInInterceptors());
        removeHandlerInterceptors(endpoint.getInInterceptors());
        removeHandlerInterceptors(endpoint.getBinding().getInInterceptors());
        removeHandlerInterceptors(endpoint.getService().getInInterceptors());

        // Install SAAJ interceptor
        if (endpoint.getBinding() instanceof SoapBinding && !this.implInfo.isWebServiceProvider()) {
            endpoint.getService().getInInterceptors().add(new SAAJInInterceptor());
        }

        // Install WSS4J interceptor
        ConfigureCxfSecurity.configure(endpoint, port);
    }

    private static void removeHandlerInterceptors(List<? extends Interceptor> interceptors) {
        for (Interceptor interceptor : interceptors) {
            if (interceptor instanceof MustUnderstandInterceptor || interceptor instanceof LogicalHandlerInInterceptor || interceptor instanceof SOAPHandlerInterceptor) {
                interceptors.remove(interceptor);
            }
        }
    }

    public void stop() {
        // call handler preDestroy
        destroyHandlers();

        // shutdown server
        super.stop();
    }
}

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
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.core.webservices.JaxWsUtils;
import org.apache.openejb.core.webservices.PortData;
import org.apache.openejb.server.cxf.CxfEndpoint;
import org.apache.openejb.server.cxf.CxfServiceConfiguration;
import org.apache.openejb.server.cxf.JaxWsImplementorInfoImpl;

import javax.xml.ws.WebServiceException;
import java.util.List;

/**
 * A web service endpoint which invokes an EJB container.
 */
public class EjbEndpoint extends CxfEndpoint {
    private final DeploymentInfo deploymentInfo;

    public EjbEndpoint(Bus bus, PortData portData, DeploymentInfo deploymentInfo) {
        super(bus, portData, deploymentInfo.getJndiEnc(), deploymentInfo.getBeanClass());
        this.deploymentInfo = deploymentInfo;

        String bindingURI = JaxWsUtils.getBindingURI(portData.getBindingID());
        implInfo = new JaxWsImplementorInfoImpl((Class) implementor, bindingURI);

        serviceFactory = new JaxWsServiceFactoryBean(implInfo);
        serviceFactory.setBus(bus);

        // install as first to overwrite annotations (wsdl-file, wsdl-port, wsdl-service)
        CxfServiceConfiguration configuration = new CxfServiceConfiguration(portData);
        serviceFactory.getConfigurations().add(0, configuration);

        service = serviceFactory.create();
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
        service.setInvoker(new EjbMethodInvoker(this.bus, deploymentInfo));

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
    }

    private static void removeHandlerInterceptors(List<Interceptor> interceptors) {
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

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
package org.apache.openejb.server.cxf.pojo;

import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.JAXWSMethodInvoker;
import org.apache.cxf.jaxws.support.JaxWsServiceFactoryBean;
import org.apache.openejb.InjectionProcessor;
import org.apache.openejb.core.webservices.JaxWsUtils;
import org.apache.openejb.core.webservices.PortData;
import org.apache.openejb.server.cxf.CxfEndpoint;
import org.apache.openejb.server.cxf.CxfServiceConfiguration;
import org.apache.openejb.server.cxf.JaxWsImplementorInfoImpl;

import javax.naming.Context;
import javax.xml.ws.WebServiceException;

public class PojoEndpoint extends CxfEndpoint {
    private InjectionProcessor<Object> injectionProcessor;

    public PojoEndpoint(Bus bus, PortData port, Context context, Class<?> instance) {
        super(bus, port, context, instance);

        String bindingURI = null;
        if (port.getBindingID() != null) {
            bindingURI = JaxWsUtils.getBindingURI(port.getBindingID());
        }
        implInfo = new JaxWsImplementorInfoImpl(instance, bindingURI);

        serviceFactory = new JaxWsServiceFactoryBean(implInfo);
        serviceFactory.setBus(bus);

        // install as first to overwrite annotations (wsdl-file, wsdl-port, wsdl-service)
        CxfServiceConfiguration configuration = new CxfServiceConfiguration(port);
        serviceFactory.getConfigurations().add(0, configuration);

        service = serviceFactory.create();

        // instantiate and inject resources into service
        try {
            injectionProcessor = new InjectionProcessor<Object>(instance, port.getInjections(), null, null, context);
            injectionProcessor.createInstance();
            injectionProcessor.postConstruct();
            implementor = injectionProcessor.getInstance();
        } catch (Exception e) {
            throw new WebServiceException("Service resource injection failed", e);
        }

        service.setInvoker(new JAXWSMethodInvoker(implementor));
    }

    protected void init() {
        // configure and inject handlers
        try {
            initHandlers();
        } catch (Exception e) {
            throw new WebServiceException("Error configuring handlers", e);
        }
    }

    public void stop() {
        // call handler preDestroy
        destroyHandlers();

        // call service preDestroy
        if (injectionProcessor != null) {
            injectionProcessor.preDestroy();
        }

        // shutdown server
        super.stop();
    }
}

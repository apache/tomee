/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.server.axis;

import java.net.URI;

import org.apache.axis.description.JavaServiceDesc;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.providers.java.RPCProvider;
import org.apache.geronimo.axis.server.AxisWebServiceContainer;
import org.apache.geronimo.axis.server.ServiceInfo;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.webservices.SoapHandler;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.InterfaceType;

public class WSContainer implements GBeanLifecycle {

    private final SoapHandler soapHandler;
    private final URI location;

    protected WSContainer() {
        soapHandler = null;
        location = null;
    }

    public WSContainer(DeploymentInfo ejbDeploymentContext,
                       URI location,
                       URI wsdlURI,
                       SoapHandler soapHandler,
                       ServiceInfo serviceInfo,
                       String securityRealmName,
                       String realmName,
                       String transportGuarantee,
                       String authMethod,
                       String[] virtualHosts) throws Exception {

        this.soapHandler = soapHandler;
        this.location = location;
        //for use as a template
        if (ejbDeploymentContext == null) {
            return;
        }
        RPCProvider provider = new EJBContainerProvider(ejbDeploymentContext);
        SOAPService service = new SOAPService(null, provider, null);

        JavaServiceDesc serviceDesc = serviceInfo.getServiceDesc();
        service.setServiceDescription(serviceDesc);
        Class serviceEndpointInterface = ejbDeploymentContext.getInterface(InterfaceType.SERVICE_ENDPOINT);

        service.setOption("className", serviceEndpointInterface.getName());
        serviceDesc.setImplClass(serviceEndpointInterface);

        ClassLoader classLoader = ejbDeploymentContext.getClassLoader();
        AxisWebServiceContainer axisContainer = new AxisWebServiceContainer(location, wsdlURI, service, serviceInfo.getWsdlMap(), classLoader);
        if (soapHandler != null) {
            soapHandler.addWebService(location.getPath(), virtualHosts, axisContainer, securityRealmName, realmName, transportGuarantee, authMethod, classLoader);
        }
    }

    public void doStart() throws Exception {

    }

    public void doStop() throws Exception {
        if (soapHandler != null) {
            soapHandler.removeWebService(location.getPath());
        }
    }

    public void doFail() {

    }
}

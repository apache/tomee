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
package org.apache.openejb.core.ivm.naming;

import org.apache.openejb.Injection;
import org.apache.openejb.core.webservices.HandlerChainData;
import org.apache.openejb.core.webservices.HandlerResolverImpl;
import org.apache.openejb.core.webservices.PortRefData;
import org.apache.openejb.core.webservices.ProviderWrapper;
import org.apache.openejb.core.webservices.ServiceRefData;
import org.apache.openejb.core.webservices.PortAddressRegistry;
import org.apache.openejb.core.webservices.PortAddress;
import org.apache.openejb.loader.SystemInstance;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.handler.HandlerResolver;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JaxWsServiceReference extends Reference {
    private final String id;
    private final QName serviceQName;
    private final QName portQName;
    private final Class<? extends Service> serviceClass;
    private final Class<?> referenceClass;
    private final URL wsdlUrl;
    private final List<HandlerChainData> handlerChains = new ArrayList<HandlerChainData>();
    private final List<Injection> injections;
    private PortAddressRegistry portAddressRegistry;
    private final List<PortRefData> portRefs = new ArrayList<PortRefData>();

    public JaxWsServiceReference(String id, QName serviceQName, Class<? extends Service> serviceClass, QName portQName, Class<?> referenceClass, URL wsdlUrl, List<PortRefData> portRefs, List<HandlerChainData> handlerChains, List<Injection> injections) {
        this.id = id;
        this.serviceQName = serviceQName;
        this.serviceClass = serviceClass;
        this.portQName = portQName;
        this.referenceClass = referenceClass;
        this.wsdlUrl = wsdlUrl;
        if (portRefs != null) {
            this.portRefs.addAll(portRefs);
        }
        if (handlerChains != null) {
            this.handlerChains.addAll(handlerChains);
        }
        this.injections = injections;
    }

    public Object getObject() throws javax.naming.NamingException {
        Set<PortAddress> portAddresses = PortAddressRegistry().getPorts(id, serviceQName);

        // if we only have one address, use that address for the wsdl
        URL wsdlUrl = this.wsdlUrl;
        if (portAddresses.size() == 1) {
            try {
                PortAddress portAddress = portAddresses.iterator().next();
                wsdlUrl = new URL(portAddress.getAddress() + "?wsdl");
            } catch (MalformedURLException e) {
            }
        }

        // add the port addresses to the portRefData
        Map<QName,PortRefData> portsByQName = new HashMap<QName,PortRefData>();
        List<PortRefData> ports = new ArrayList<PortRefData>(portRefs.size() + portAddresses.size());
        for (PortRefData portRef : portRefs) {
            PortRefData port = new PortRefData(portRef);
            if (port.getQName() != null) {
                portsByQName.put(port.getQName(), port);
            }
            ports.add(port);
        }

        // add PortRefData for any portAddress not added above
        for (PortAddress portAddress : portAddresses) {
            PortRefData port = portsByQName.get(portAddress.getQName());
            if (port == null) {
                port = new PortRefData();
                port.setQName(portAddress.getQName());
                port.setServiceEndpointInterface(portAddress.getServiceEndpointInterface());
                port.getAddresses().add(portAddress.getAddress());
                ports.add(port);
            } else {
                port.getAddresses().add(portAddress.getAddress());
                if (port.getServiceEndpointInterface() == null) {
                    port.setServiceEndpointInterface(portAddress.getServiceEndpointInterface());
                }
            }
        }

        ProviderWrapper.beforeCreate(ports);
        Service instance;
        try {
            instance = null;
            if (Service.class.equals(serviceClass)) {
                instance = Service.create(wsdlUrl, serviceQName);
            } else {
                try {
                    instance = serviceClass.getConstructor(URL.class, QName.class).newInstance(wsdlUrl, serviceQName);
                } catch (Throwable e) {
                    throw (NamingException) new NamingException("Could not instantiate jax-ws service class " + serviceClass.getName()).initCause(e);
                }
            }
        } finally {
            ProviderWrapper.afterCreate();
        }

        if (!handlerChains.isEmpty()) {
            HandlerResolver handlerResolver = new HandlerResolverImpl(handlerChains, injections, new InitialContext());
            instance.setHandlerResolver(handlerResolver);
        }

        Object port;
        if (referenceClass != null && !Service.class.isAssignableFrom(referenceClass)) {
            // do port lookup
            port = instance.getPort(referenceClass);
        } else {
            // return service
            port = instance;
        }

        // register the service data so it can be fetched when the service is passed over the EJBd protocol
        ServiceRefData serviceRefData = new ServiceRefData(id,
                serviceQName,
                serviceClass, portQName,
                referenceClass,
                wsdlUrl,
                handlerChains,
                portRefs);
        ServiceRefData.putServiceRefData(port, serviceRefData);

        return port;
    }

    private PortAddressRegistry PortAddressRegistry() {
        if (portAddressRegistry == null) {
            portAddressRegistry = SystemInstance.get().getComponent(PortAddressRegistry.class);
        }
        return portAddressRegistry;
    }
}

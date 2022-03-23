/*
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

package org.apache.openejb.core.ivm.naming;

import org.apache.openejb.Injection;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.core.webservices.HandlerChainData;
import org.apache.openejb.core.webservices.HandlerResolverImpl;
import org.apache.openejb.core.webservices.PortAddress;
import org.apache.openejb.core.webservices.PortAddressRegistry;
import org.apache.openejb.core.webservices.PortRefData;
import org.apache.openejb.core.webservices.ProviderWrapper;
import org.apache.openejb.core.webservices.ServiceRefData;
import org.apache.openejb.loader.SystemInstance;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.WebServiceFeature;
import jakarta.xml.ws.handler.HandlerResolver;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class JaxWsServiceReference extends Reference {
    private final String id;
    private final QName serviceQName;
    private final QName portQName;
    private final Class<? extends Service> serviceClass;
    private final Class<?> referenceClass;
    private final URL wsdlUrl;
    private final List<HandlerChainData> handlerChains = new ArrayList<>();
    private final Collection<Injection> injections;
    private final Properties properties;
    private PortAddressRegistry portAddressRegistry;
    private final List<PortRefData> portRefs = new ArrayList<>();

    public JaxWsServiceReference(final String id, final QName serviceQName, final Class<? extends Service> serviceClass,
                                 final QName portQName, final Class<?> referenceClass, final URL wsdlUrl,
                                 final List<PortRefData> portRefs, final List<HandlerChainData> handlerChains,
                                 final Collection<Injection> injections,
                                 final Properties properties) {
        this.id = id;
        this.properties = properties;
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

    public Object getObject() throws NamingException {
        final String referenceClassName = referenceClass != null ? referenceClass.getName() : null;
        final Set<PortAddress> portAddresses = getPortAddressRegistry().getPorts(id, serviceQName, referenceClassName);

        // if we only have one address, use that address for the wsdl and the serviceQName
        URL wsdlUrl = this.wsdlUrl;
        QName serviceQName = this.serviceQName;
        if (portAddresses.size() == 1) {
            final PortAddress portAddress = portAddresses.iterator().next();
            try {
                wsdlUrl = new URL(portAddress.getAddress() + "?wsdl");
            } catch (final MalformedURLException e) {
                // no-op
            }
            serviceQName = portAddress.getServiceQName();
        }

        // add the port addresses to the portRefData
        final Map<QName, PortRefData> portsByQName = new HashMap<>();
        final List<PortRefData> ports = new ArrayList<>(portRefs.size() + portAddresses.size());
        for (final PortRefData portRef : portRefs) {
            final PortRefData port = new PortRefData(portRef);
            if (port.getQName() != null) {
                portsByQName.put(port.getQName(), port);
            }
            ports.add(port);
        }

        // add PortRefData for any portAddress not added above
        for (final PortAddress portAddress : portAddresses) {
            PortRefData port = portsByQName.get(portAddress.getPortQName());
            if (port == null) {
                port = new PortRefData();
                port.setQName(portAddress.getPortQName());
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

        final WebServiceClientCustomizer customizer = SystemInstance.get().getComponent(WebServiceClientCustomizer.class);
        final Properties configuration = properties == null ? new Properties() : properties;

        ProviderWrapper.beforeCreate(ports, customizer, properties);
        Service instance;
        try {
            instance = null;
            if (Service.class.equals(serviceClass)) {
                instance = Service.create(wsdlUrl, serviceQName);
            } else {
                try {
                    instance = serviceClass.getConstructor(URL.class, QName.class).newInstance(wsdlUrl, serviceQName);
                } catch (final Throwable e) {
                    throw (NamingException) new NamingException("Could not instantiate jax-ws service class " + serviceClass.getName()).initCause(e);
                }
            }
        } finally {
            ProviderWrapper.afterCreate();
        }

        if (!handlerChains.isEmpty()) {
            final HandlerResolver handlerResolver = new HandlerResolverImpl(handlerChains, injections, new InitialContext());
            instance.setHandlerResolver(handlerResolver);
        }

        final Object port;
        if (referenceClass != null && !Service.class.isAssignableFrom(referenceClass)) {
            final WebServiceFeature[] features = customizer == null ? null : customizer.features(serviceQName, configuration);
            // do port lookup
            if (features == null || features.length == 0) {
                port = instance.getPort(referenceClass);
            } else {
                port = instance.getPort(referenceClass, features);
            }
        } else {
            // return service
            port = instance;
        }

        // register the service data so it can be fetched when the service is passed over the EJBd protocol
        final ServiceRefData serviceRefData = new ServiceRefData(id,
                serviceQName,
                serviceClass, portQName,
                referenceClass,
                wsdlUrl,
                handlerChains,
                portRefs);
        ServiceRefData.putServiceRefData(port, serviceRefData);

        return port;
    }

    private PortAddressRegistry getPortAddressRegistry() {
        if (portAddressRegistry == null) {
            portAddressRegistry = SystemInstance.get().getComponent(PortAddressRegistry.class);
            if (portAddressRegistry == null) {
                throw new OpenEJBRuntimeException("No port address registry, it generally means you either didn't activate cxf or don't use tomee+");
            }
        }
        return portAddressRegistry;
    }

    public interface WebServiceClientCustomizer {
        /**
         * @param qname QName of the webservice
         * @param properties app configuration
         * @return ws features associated with this endpoint
         */
        WebServiceFeature[] features(QName qname, Properties properties);

        /**
         * Note: it is recommanded to use same key type as in features() impl (ie qname)
         *
         * @param port the client instance
         * @param properties configuration of the application
         */
        void customize(Object port, Properties properties);
    }
}

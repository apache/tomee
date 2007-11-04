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

import org.apache.openejb.core.webservices.HandlerResolverImpl;
import org.apache.openejb.core.webservices.HandlerChainData;
import org.apache.openejb.core.webservices.PortRefData;
import org.apache.openejb.core.webservices.WsdlRepo;
import org.apache.openejb.core.webservices.ProviderWrapper;
import org.apache.openejb.core.webservices.ServiceRefData;
import org.apache.openejb.Injection;
import org.apache.openejb.loader.SystemInstance;

import javax.naming.NamingException;
import javax.naming.InitialContext;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.handler.HandlerResolver;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.List;
import java.util.ArrayList;

public class JaxWsServiceReference extends Reference {
    private final Class<? extends Service> serviceClass;
    private final Class<?> referenceClass;
    private final URL wsdlUrl;
    private final QName serviceQName;
    private final List<HandlerChainData> handlerChains = new ArrayList<HandlerChainData>();
    private final List<Injection> injections;
    private WsdlRepo wsdlRepo;
    private final List<PortRefData> portRefs = new ArrayList<PortRefData>();
    private String wsdlRepoUri;

    public JaxWsServiceReference(Class<? extends Service> serviceClass, Class<?> referenceClass, URL wsdlUrl, QName serviceQName, String wsdlRepoUri, List<PortRefData> portRefs, List<HandlerChainData> handlerChains, List<Injection> injections) {
        if (portRefs != null) {
            this.portRefs.addAll(portRefs);
        }
        this.serviceClass = serviceClass;
        this.referenceClass = referenceClass;
        this.wsdlUrl = wsdlUrl;
        this.serviceQName = serviceQName;
        this.wsdlRepoUri = wsdlRepoUri;
        if (handlerChains != null) {
            this.handlerChains.addAll(handlerChains);
        }
        this.injections = injections;
    }

    public Object getObject() throws javax.naming.NamingException {
        URL wsdlUrl = getWsdlUrl();

        ProviderWrapper.beforeCreate(portRefs);
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
        ServiceRefData serviceRefData = new ServiceRefData(serviceClass, referenceClass, wsdlUrl, serviceQName, null, handlerChains, portRefs);
        ServiceRefData.putServiceRefData(port, serviceRefData);

        return port;
    }

    private URL getWsdlUrl() {
        WsdlRepo wsdlRepo = getWsdlRepo();
        if (wsdlRepo != null) {
            String wsdlLocation = wsdlRepo.getWsdl(wsdlRepoUri, serviceQName, referenceClass.getName());
            if (wsdlLocation != null) {
                try {
                    URL wsdlUrl = new URL(wsdlLocation);
                    return wsdlUrl;
                } catch (MalformedURLException e) {
                }
            }
        }

        return wsdlUrl;
    }

    private WsdlRepo getWsdlRepo() {
        if (wsdlRepo == null) {
            wsdlRepo = SystemInstance.get().getComponent(WsdlRepo.class);
        }
        return wsdlRepo;
    }
}

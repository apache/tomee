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
package org.apache.openejb.core.webservices;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

public class ServiceRefData {
    private static final WeakHashMap<Object,ServiceRefData> registry = new WeakHashMap<Object,ServiceRefData>();

    public static ServiceRefData getServiceRefData(Object key) {
        return registry.get(key);
    }

    public static ServiceRefData putServiceRefData(Object key, ServiceRefData value) {
        return registry.put(key, value);
    }

    private final Class<? extends Service> serviceClass;
    private final Class<?> referenceClass;
    private final URL wsdlURL;
    private final QName serviceQName;
    private final String wsdlRepoUri;
    private final List<HandlerChainData> handlerChains = new ArrayList<HandlerChainData>();
    private final List<PortRefData> portRefs = new ArrayList<PortRefData>();

    public ServiceRefData(Class<? extends Service> serviceClass, Class<?> referenceClass, URL wsdlURL, QName serviceQName, String wsdlRepoUri, List<HandlerChainData> handlerChains, List<PortRefData> portRefs) {
        this.wsdlRepoUri = wsdlRepoUri;
        this.serviceClass = serviceClass;
        this.referenceClass = referenceClass;
        this.serviceQName = serviceQName;
        this.wsdlURL = wsdlURL;
        if (handlerChains != null) {
            this.handlerChains.addAll(handlerChains);
        }
        if (portRefs != null) {
            this.portRefs.addAll(portRefs);
        }
    }

    public Class<? extends Service> getServiceClass() {
        return serviceClass;
    }

    public Class<?> getReferenceClass() {
        return referenceClass;
    }

    public URL getWsdlURL() {
        return wsdlURL;
    }

    public QName getServiceQName() {
        return serviceQName;
    }

    public String getWsdlRepoUri() {
        return wsdlRepoUri;
    }

    public List<HandlerChainData> getHandlerChains() {
        return handlerChains;
    }

    public List<PortRefData> getPortRefs() {
        return portRefs;
    }
}

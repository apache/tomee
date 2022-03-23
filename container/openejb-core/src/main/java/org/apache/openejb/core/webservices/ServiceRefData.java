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

package org.apache.openejb.core.webservices;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

public class ServiceRefData {
    private static final WeakHashMap<Object, ServiceRefData> registry = new WeakHashMap<Object, ServiceRefData>();

    public static ServiceRefData getServiceRefData(final Object key) {
        return registry.get(key);
    }

    public static ServiceRefData putServiceRefData(final Object key, final ServiceRefData value) {
        return registry.put(key, value);
    }

    private final String id;
    private final QName serviceQName;
    private final Class<? extends Service> serviceClass;
    private final QName portQName;
    private final Class<?> referenceClass;
    private final URL wsdlURL;
    private final List<HandlerChainData> handlerChains = new ArrayList<>();
    private final List<PortRefData> portRefs = new ArrayList<>();

    public ServiceRefData(final String id, final QName serviceQName, final Class<? extends Service> serviceClass, final QName portQName, final Class<?> referenceClass, final URL wsdlURL, final List<HandlerChainData> handlerChains, final List<PortRefData> portRefs) {
        this.id = id;
        this.serviceQName = serviceQName;
        this.serviceClass = serviceClass;
        this.portQName = portQName;
        this.referenceClass = referenceClass;
        this.wsdlURL = wsdlURL;
        if (handlerChains != null) {
            this.handlerChains.addAll(handlerChains);
        }
        if (portRefs != null) {
            this.portRefs.addAll(portRefs);
        }
    }

    public String getId() {
        return id;
    }

    public QName getServiceQName() {
        return serviceQName;
    }

    public Class<? extends Service> getServiceClass() {
        return serviceClass;
    }

    public QName getPortQName() {
        return portQName;
    }

    public Class<?> getReferenceClass() {
        return referenceClass;
    }

    public URL getWsdlURL() {
        return wsdlURL;
    }

    public List<HandlerChainData> getHandlerChains() {
        return handlerChains;
    }

    public List<PortRefData> getPortRefs() {
        return portRefs;
    }
}

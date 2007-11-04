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
package org.apache.openejb.client;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.handler.HandlerResolver;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;

public class WsMetaData implements Serializable {
    private static final long serialVersionUID = -895152184216070327L;
    private String serviceClassName;
    private String referenceClassName;
    private String wsdlUrl;
    private String serviceQName;
    private final List<HandlerChainMetaData> handlerChains = new ArrayList<HandlerChainMetaData>();
    private final List<PortRefMetaData> portRefs = new ArrayList<PortRefMetaData>();

    public String getServiceClassName() {
        return serviceClassName;
    }

    public void setServiceClassName(String serviceClassName) {
        this.serviceClassName = serviceClassName;
    }

    public String getReferenceClassName() {
        return referenceClassName;
    }

    public void setReferenceClassName(String referenceClassName) {
        this.referenceClassName = referenceClassName;
    }

    public String getWsdlUrl() {
        return wsdlUrl;
    }

    public void setWsdlUrl(String wsdlUrl) {
        this.wsdlUrl = wsdlUrl;
    }

    public String getServiceQName() {
        return serviceQName;
    }

    public void setServiceQName(String serviceQName) {
        this.serviceQName = serviceQName;
    }

    public List<HandlerChainMetaData> getHandlerChains() {
        return handlerChains;
    }

    public List<PortRefMetaData> getPortRefs() {
        return portRefs;
    }

    public Object createWebservice() throws Exception {
        // load service class which is used to construct the port
        Class<? extends Service> serviceClass = loadClass(serviceClassName).asSubclass(Service.class);
        if (serviceClass == null) {
            throw new NamingException("Could not load service type class " + serviceClassName);
        }

        // load the reference class which is the ultimate type of the port
        Class<?> referenceClass = loadClass(referenceClassName);

        // if ref class is a subclass of Service, use it for the service class
        if (referenceClass != null && Service.class.isAssignableFrom(referenceClass)) {
            serviceClass = referenceClass.asSubclass(Service.class);
        }

        // Service QName
        QName serviceQName = QName.valueOf(this.serviceQName);

        // WSDL URL
        URL wsdlLocation = new URL(this.wsdlUrl);

        JaxWsProviderWrapper.beforeCreate(portRefs);
        Service instance;
        try {
            instance = null;
            if (Service.class.equals(serviceClass)) {
                instance = Service.create(wsdlLocation, serviceQName);
            } else {
                try {
                    instance = serviceClass.getConstructor(URL.class, QName.class).newInstance(wsdlLocation, serviceQName);
                } catch (Throwable e) {
                    throw (NamingException) new NamingException("Could not instantiate jax-ws service class " + serviceClass.getName()).initCause(e);
                }
            }
        } finally {
            JaxWsProviderWrapper.afterCreate();
        }

        if (handlerChains != null && !handlerChains.isEmpty()) {
            InjectionMetaData injectionMetaData = ClientInstance.get().getComponent(InjectionMetaData.class);
            List<Injection> injections = injectionMetaData.getInjections();
            HandlerResolver handlerResolver = new ClientHandlerResolverImpl(handlerChains, injections, new InitialContext());
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
        return port;
    }

    public static Class<?> loadClass(String className) {
        if (className == null) return null;
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader != null) {
                try {
                    Class clazz = classLoader.loadClass(className);
                    return clazz;
                } catch(ClassNotFoundException e) {
                }
            }
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}

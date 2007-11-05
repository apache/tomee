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
package org.apache.openejb.assembler.classic;

import org.apache.openejb.core.webservices.HandlerChainData;
import org.apache.openejb.core.webservices.HandlerData;
import org.apache.openejb.core.webservices.PortData;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.Injection;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.net.URL;
import java.net.MalformedURLException;

public class WsBuilder {
    public static PortData toPortData(PortInfo port, Collection<Injection> injections, URL baseUrl, ClassLoader classLoader) throws OpenEJBException {
        PortData portData = new PortData();
        portData.setPortId(port.portId);
        if (port.serviceName != null && port.serviceName.length() != 0) {
            portData.setServiceName(QName.valueOf(port.serviceName));
        }
        if (port.portName != null && port.portName.length() != 0) {
            portData.setPortName(QName.valueOf(port.portName));
        }
        portData.setWsdlUrl(getWsdlURL(port.wsdlFile, baseUrl, classLoader));
        portData.getHandlerChains().addAll(toHandlerChainData(port.handlerChains, classLoader));
        portData.getInjections().addAll(injections);
        portData.setMtomEnabled(port.mtomEnabled);
        portData.setBindingID(port.binding);
        portData.setWsdlPort(port.wsdlPort);
        portData.setWsdlService(port.wsdlService);
        portData.setLocation(port.location);
        return portData;
    }

    public static List<HandlerChainData> toHandlerChainData(List<HandlerChainInfo> chains, ClassLoader classLoader) throws OpenEJBException {
        List<HandlerChainData> handlerChains = new ArrayList<HandlerChainData>();
        for (HandlerChainInfo handlerChain : chains) {
            List<HandlerData> handlers = new ArrayList<HandlerData>();
            for (HandlerInfo handler : handlerChain.handlers) {
                try {
                    Class<?> handlerClass = classLoader.loadClass(handler.handlerClass);
                    HandlerData handlerData = new HandlerData(handlerClass);
                    handlerData.getInitParams().putAll(handler.initParams);
                    handlerData.getSoapHeaders().addAll(handler.soapHeaders);
                    handlerData.getSoapRoles().addAll(handler.soapRoles);
                    handlers.add(handlerData);
                } catch (ClassNotFoundException e) {
                    throw new OpenEJBException("Could not load handler class "+ handler.handlerClass);
                }
            }

            HandlerChainData handlerChainData = new HandlerChainData(handlerChain.serviceNamePattern,
                    handlerChain.portNamePattern,
                    handlerChain.protocolBindings,
                    handlers);
            handlerChains.add(handlerChainData);

        }
        return handlerChains;
    }

    public static URL getWsdlURL(String wsdlFile, URL baseUrl, ClassLoader classLoader) {
        URL wsdlURL = null;
        if (wsdlFile != null && wsdlFile.length() > 0) {
            try {
                wsdlURL = new URL(wsdlFile);
            } catch (MalformedURLException e) {
                // Not a URL, try as a resource
                wsdlURL = classLoader.getResource(wsdlFile);

                if (wsdlURL == null && baseUrl != null) {
                    // Cannot get it as a resource, try with
                    // configurationBaseUrl
                    try {
                        wsdlURL = new URL(baseUrl, wsdlFile);
                    } catch (MalformedURLException ee) {
                        // ignore
                    }
                }
            }
        }
        return wsdlURL;
    }
}

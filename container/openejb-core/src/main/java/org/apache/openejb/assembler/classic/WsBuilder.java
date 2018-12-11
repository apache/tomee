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

package org.apache.openejb.assembler.classic;

import org.apache.openejb.Injection;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.core.webservices.HandlerChainData;
import org.apache.openejb.core.webservices.HandlerData;
import org.apache.openejb.core.webservices.PortData;

import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WsBuilder {
    public static PortData toPortData(final PortInfo port, final Collection<Injection> injections, final URL baseUrl, final ClassLoader classLoader) throws OpenEJBException {
        final PortData portData = new PortData();
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

        portData.setSecure("WS-SECURITY".equals(port.authMethod));
        portData.setProperties(port.properties);

        return portData;
    }

    public static List<HandlerChainData> toHandlerChainData(final List<HandlerChainInfo> chains, final ClassLoader classLoader) throws OpenEJBException {
        final List<HandlerChainData> handlerChains = new ArrayList<>();
        for (final HandlerChainInfo handlerChain : chains) {
            final List<HandlerData> handlers = new ArrayList<>();
            for (final HandlerInfo handler : handlerChain.handlers) {
                try {
                    final Class<?> handlerClass = classLoader.loadClass(handler.handlerClass);
                    final HandlerData handlerData = new HandlerData(handlerClass);
                    handlerData.getInitParams().putAll(handler.initParams);
                    handlerData.getSoapHeaders().addAll(handler.soapHeaders);
                    handlerData.getSoapRoles().addAll(handler.soapRoles);
                    handlers.add(handlerData);
                } catch (final ClassNotFoundException e) {
                    throw new OpenEJBException("Could not load handler class " + handler.handlerClass);
                }
            }

            final HandlerChainData handlerChainData = new HandlerChainData(handlerChain.serviceNamePattern,
                handlerChain.portNamePattern,
                handlerChain.protocolBindings,
                handlers);
            handlerChains.add(handlerChainData);

        }
        return handlerChains;
    }

    public static URL getWsdlURL(final String wsdlFile, final URL baseUrl, final ClassLoader classLoader) {
        URL wsdlURL = null;
        if (wsdlFile != null && wsdlFile.length() > 0) {
            try {
                wsdlURL = new URL(wsdlFile);
            } catch (final MalformedURLException e) {
                // Not a URL, try as a resource
                wsdlURL = classLoader.getResource(wsdlFile);

                if (wsdlURL == null && baseUrl != null) {
                    // Cannot get it as a resource, try with
                    // configurationBaseUrl
                    try {
                        wsdlURL = new URL(baseUrl, wsdlFile);
                    } catch (final MalformedURLException ee) {
                        // ignore
                    }
                }
            }
        }
        return wsdlURL;
    }
}

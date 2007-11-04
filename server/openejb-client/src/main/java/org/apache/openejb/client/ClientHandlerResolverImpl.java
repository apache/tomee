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

import javax.naming.Context;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.PortInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ClientHandlerResolverImpl implements HandlerResolver {
    private final List<HandlerChainMetaData> handlerChains;
    private final List<Injection> injections;
    private final Context context;
    private final List<ClientInjectionProcessor<Handler>> handlerInstances = new ArrayList<ClientInjectionProcessor<Handler>>();

    public ClientHandlerResolverImpl(List<HandlerChainMetaData> handlerChains, List<Injection> injections, Context context) {
        this.handlerChains = handlerChains;
        this.injections = injections;
        this.context = context;
    }

    public void destroyHandlers() {
        List<ClientInjectionProcessor<Handler>> handlerInstances = new ArrayList<ClientInjectionProcessor<Handler>>(this.handlerInstances);
        this.handlerInstances.clear();
        for (ClientInjectionProcessor<Handler> handlerInstance : handlerInstances) {
            handlerInstance.preDestroy();
        }
    }

    public List<Handler> getHandlerChain(javax.xml.ws.handler.PortInfo portInfo) {
        List<Handler> chain = new ArrayList<Handler>();
        for (HandlerChainMetaData handlerChain : handlerChains) {
            List<Handler> handlers = buildHandlers(portInfo, handlerChain);
            handlers = sortHandlers(handlers);
            chain.addAll(handlers);
        }
        chain = sortHandlers(chain);
        return chain;
    }

    private List<Handler> buildHandlers(javax.xml.ws.handler.PortInfo portInfo, HandlerChainMetaData handlerChain) {
        if (!matchServiceName(portInfo, handlerChain.getServiceNamePattern()) || !matchPortName(portInfo, handlerChain.getPortNamePattern()) || !matchBinding(portInfo, handlerChain.getProtocolBindings())) {
            return Collections.emptyList();
        }

        List<Handler> handlers = new ArrayList<Handler>(handlerChain.getHandlers().size());
        for (HandlerMetaData handler : handlerChain.getHandlers()) {
            try {
                Class<? extends Handler> handlerClass = loadClass(handler.getHandlerClass()).asSubclass(Handler.class);
                ClientInjectionProcessor<Handler> processor = new ClientInjectionProcessor<Handler>(handlerClass, injections, handler.getPostConstruct(), handler.getPreDestroy(), context);
                processor.createInstance();
                processor.postConstruct();
                Handler handlerInstance = processor.getInstance();

                handlers.add(handlerInstance);
                handlerInstances.add(processor);
            } catch (Exception e) {
                throw new WebServiceException("Failed to instantiate handler", e);
            }
        }
        return handlers;
    }

    private boolean matchServiceName(PortInfo info, String namePattern) {
        return match((info == null ? null : info.getServiceName()), namePattern);
    }

    private boolean matchPortName(PortInfo info, String namePattern) {
        return match((info == null ? null : info.getPortName()), namePattern);
    }

    private boolean matchBinding(PortInfo info, List bindings) {
        return match((info == null ? null : info.getBindingID()), bindings);
    }

    private boolean match(String binding, List bindings) {
        if (binding == null) {
            return (bindings == null || bindings.isEmpty());
        } else {
            if (bindings == null || bindings.isEmpty()) {
                return true;
            } else {
                String actualBindingURI = getBindingURI(binding);
                Iterator iter = bindings.iterator();
                while (iter.hasNext()) {
                    String bindingToken = (String) iter.next();
                    String bindingURI = getBindingURI(bindingToken);
                    if (actualBindingURI.equals(bindingURI)) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    /**
     * Performs basic localName matching, namespaces are not checked!
     */
    private boolean match(QName name, String namePattern) {
        if (name == null) {
            return (namePattern == null || namePattern.equals("*"));
        } else {
            if (namePattern == null) {
                return true;
            } else {
                String localNamePattern;

                // get the local name from pattern
                int pos = namePattern.indexOf(':');
                localNamePattern = (pos == -1) ? namePattern : namePattern
                        .substring(pos + 1);
                localNamePattern = localNamePattern.trim();

                if (localNamePattern.equals("*")) {
                    // matches anything
                    return true;
                } else if (localNamePattern.endsWith("*")) {
                    // match start
                    localNamePattern = localNamePattern.substring(0, localNamePattern.length() - 1);
                    return name.getLocalPart().startsWith(localNamePattern);
                } else {
                    // match exact
                    return name.getLocalPart().equals(localNamePattern);
                }
            }
        }
    }

    /**
     * sorts the handlers into correct order. All of the logical handlers first
     * followed by the protocol handlers
     *
     * @param handlers
     * @return sorted list of handlers
     */
    private List<Handler> sortHandlers(List<Handler> handlers) {
        List<LogicalHandler> logicalHandlers = new ArrayList<LogicalHandler>();
        List<Handler> protocolHandlers = new ArrayList<Handler>();

        for (Handler handler : handlers) {
            if (handler instanceof LogicalHandler) {
                logicalHandlers.add((LogicalHandler) handler);
            } else {
                protocolHandlers.add(handler);
            }
        }

        List<Handler> sortedHandlers = new ArrayList<Handler>();
        sortedHandlers.addAll(logicalHandlers);
        sortedHandlers.addAll(protocolHandlers);
        return sortedHandlers;
    }

    private static final Map<String, String> BINDING_MAP = new HashMap<String, String>();

    static {
        BINDING_MAP.put("##SOAP11_HTTP", "http://schemas.xmlsoap.org/wsdl/soap/http");
        BINDING_MAP.put("##SOAP12_HTTP", "http://www.w3.org/2003/05/soap/bindings/HTTP/");
        BINDING_MAP.put("##SOAP11_HTTP_MTOM", "http://schemas.xmlsoap.org/wsdl/soap/http?mtom=true");
        BINDING_MAP.put("##SOAP12_HTTP_MTOM", "http://www.w3.org/2003/05/soap/bindings/HTTP/?mtom=true");
        BINDING_MAP.put("##XML_HTTP", "http://www.w3.org/2004/08/wsdl/http");
    }

    private static String getBindingURI(String token) {
        if (token != null) {
            if (token.startsWith("##")) {
                String uri = BINDING_MAP.get(token);
                if (uri == null) {
                    throw new IllegalArgumentException("Unsupported binding token: " + token);
                }
                return uri;
            }
            return token;
        }
        return BINDING_MAP.get("##SOAP11_HTTP");
    }

    private Class<?> loadClass(String name) throws ClassNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader != null) {
            return classLoader.loadClass(name);
        }
        return Class.forName(name);
    }
}

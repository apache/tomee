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
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.handler.Handler;
import jakarta.xml.ws.handler.HandlerResolver;
import jakarta.xml.ws.handler.LogicalHandler;
import jakarta.xml.ws.handler.PortInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientHandlerResolverImpl implements HandlerResolver {

    private final List<HandlerChainMetaData> handlerChains;
    private final List<Injection> injections;
    private final Context context;
    private final List<ClientInjectionProcessor<Handler>> handlerInstances = new ArrayList<ClientInjectionProcessor<Handler>>();

    public ClientHandlerResolverImpl(final List<HandlerChainMetaData> handlerChains, final List<Injection> injections, final Context context) {
        this.handlerChains = handlerChains;
        this.injections = injections;
        this.context = context;
    }

    public void destroyHandlers() {
        final List<ClientInjectionProcessor<Handler>> handlerInstances = new ArrayList<ClientInjectionProcessor<Handler>>(this.handlerInstances);
        this.handlerInstances.clear();
        for (final ClientInjectionProcessor<Handler> handlerInstance : handlerInstances) {
            handlerInstance.preDestroy();
        }
    }

    @Override
    public List<Handler> getHandlerChain(final jakarta.xml.ws.handler.PortInfo portInfo) {
        List<Handler> chain = new ArrayList<Handler>();
        for (final HandlerChainMetaData handlerChain : handlerChains) {
            List<Handler> handlers = buildHandlers(portInfo, handlerChain);
            handlers = sortHandlers(handlers);
            chain.addAll(handlers);
        }
        chain = sortHandlers(chain);
        return chain;
    }

    private List<Handler> buildHandlers(final jakarta.xml.ws.handler.PortInfo portInfo, final HandlerChainMetaData handlerChain) {
        if (!matchServiceName(portInfo, handlerChain.getServiceNamePattern()) || !matchPortName(portInfo, handlerChain.getPortNamePattern()) || !matchBinding(portInfo,
            handlerChain.getProtocolBindings())) {
            return Collections.emptyList();
        }

        final List<Handler> handlers = new ArrayList<Handler>(handlerChain.getHandlers().size());
        for (final HandlerMetaData handler : handlerChain.getHandlers()) {
            try {
                final Class<? extends Handler> handlerClass = loadClass(handler.getHandlerClass()).asSubclass(Handler.class);
                final ClientInjectionProcessor<Handler> processor = new ClientInjectionProcessor<Handler>(handlerClass,
                    injections,
                    handler.getPostConstruct(),
                    handler.getPreDestroy(),
                    context);
                processor.createInstance();
                processor.postConstruct();
                final Handler handlerInstance = processor.getInstance();

                handlers.add(handlerInstance);
                handlerInstances.add(processor);
            } catch (Exception e) {
                throw new WebServiceException("Failed to instantiate handler", e);
            }
        }
        return handlers;
    }

    private boolean matchServiceName(final PortInfo info, final QName namePattern) {
        return match((info == null ? null : info.getServiceName()), namePattern);
    }

    private boolean matchPortName(final PortInfo info, final QName namePattern) {
        return match((info == null ? null : info.getPortName()), namePattern);
    }

    private boolean matchBinding(final PortInfo info, final List bindings) {
        return match((info == null ? null : info.getBindingID()), bindings);
    }

    private boolean match(final String binding, final List bindings) {
        if (binding == null) {
            return (bindings == null || bindings.isEmpty());
        } else {
            if (bindings == null || bindings.isEmpty()) {
                return true;
            } else {
                final String actualBindingURI = getBindingURI(binding);
                for (final Object o : bindings) {
                    final String bindingToken = (String) o;
                    final String bindingURI = getBindingURI(bindingToken);
                    if (actualBindingURI.equals(bindingURI)) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    /**
     * Performs basic localName matching
     */
    private boolean match(final QName name, final QName namePattern) {
        if (name == null) {
            return (namePattern == null || namePattern.getLocalPart().equals("*"));
        } else {
            if (namePattern == null) {
                return true;
            } else if (namePattern.getNamespaceURI() != null && !name.getNamespaceURI().equals(namePattern.getNamespaceURI())) {
                return false;
            } else {
                String localNamePattern = namePattern.getLocalPart();
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
     * @param handlers List
     * @return sorted list of handlers
     */
    private List<Handler> sortHandlers(final List<Handler> handlers) {
        final List<LogicalHandler> logicalHandlers = new ArrayList<LogicalHandler>();
        final List<Handler> protocolHandlers = new ArrayList<Handler>();

        for (final Handler handler : handlers) {
            if (handler instanceof LogicalHandler) {
                logicalHandlers.add((LogicalHandler) handler);
            } else {
                protocolHandlers.add(handler);
            }
        }

        final List<Handler> sortedHandlers = new ArrayList<Handler>();
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

    private static String getBindingURI(final String token) {
        if (token != null) {
            if (token.startsWith("##")) {
                final String uri = BINDING_MAP.get(token);
                if (uri == null) {
                    throw new IllegalArgumentException("Unsupported binding token: " + token);
                }
                return uri;
            }
            return token;
        }
        return BINDING_MAP.get("##SOAP11_HTTP");
    }

    private Class<?> loadClass(final String name) throws ClassNotFoundException {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader != null) {
            return classLoader.loadClass(name);
        }
        return Class.forName(name);
    }
}

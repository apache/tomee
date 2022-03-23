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

import static org.apache.openejb.InjectionProcessor.unwrap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.inject.InjectionException;
import jakarta.enterprise.inject.spi.Bean;
import javax.naming.Context;
import javax.xml.namespace.QName;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.handler.Handler;
import jakarta.xml.ws.handler.HandlerResolver;
import jakarta.xml.ws.handler.LogicalHandler;
import jakarta.xml.ws.handler.PortInfo;

import org.apache.openejb.Injection;
import org.apache.openejb.InjectionProcessor;
import org.apache.openejb.util.AppFinder;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.context.creational.CreationalContextImpl;

public class HandlerResolverImpl implements HandlerResolver {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB_WS, HandlerResolverImpl.class);

    private final List<HandlerChainData> handlerChains;
    private final Collection<Injection> injections;
    private final Context context;
    private final List<InjectionProcessor<Handler>> handlerInstances = new ArrayList<>();

    public HandlerResolverImpl(final List<HandlerChainData> handlerChains, final Collection<Injection> injections, final Context context) {
        this.handlerChains = handlerChains;
        this.injections = injections;
        this.context = context;
    }

    public void destroyHandlers() {
        final List<InjectionProcessor<Handler>> handlerInstances = new ArrayList<>(this.handlerInstances);
        this.handlerInstances.clear();
        for (final InjectionProcessor<Handler> handlerInstance : handlerInstances) {
            handlerInstance.preDestroy();
        }
    }

    public List<Handler> getHandlerChain(final PortInfo portInfo) {
        List<Handler> chain = new ArrayList<>();
        for (final HandlerChainData handlerChain : handlerChains) {
            List<Handler> handlers = buildHandlers(portInfo, handlerChain);
            handlers = sortHandlers(handlers);
            chain.addAll(handlers);
        }
        chain = sortHandlers(chain);
        return chain;
    }

    private List<Handler> buildHandlers(final PortInfo portInfo, final HandlerChainData handlerChain) {
        if (!matchServiceName(portInfo, handlerChain.getServiceNamePattern()) ||
            !matchPortName(portInfo, handlerChain.getPortNamePattern()) ||
            !matchBinding(portInfo, handlerChain.getProtocolBindings())) {
            return Collections.emptyList();
        }

        final List<Handler> handlers = new ArrayList<>(handlerChain.getHandlers().size());
        for (final HandlerData handler : handlerChain.getHandlers()) {
            final WebBeansContext webBeansContext = AppFinder.findAppContextOrWeb(
                    Thread.currentThread().getContextClassLoader(), AppFinder.WebBeansContextTransformer.INSTANCE);
            if (webBeansContext != null) { // cdi
                final BeanManagerImpl bm = webBeansContext.getBeanManagerImpl();
                if (bm.isInUse()) {
                    try {
                        final Set<Bean<?>> beans = bm.getBeans(handler.getHandlerClass());
                        final Bean<?> bean = bm.resolve(beans);
                        if (bean != null) { // proxy so faster to do it
                            final boolean normalScoped = bm.isNormalScope(bean.getScope());
                            final CreationalContextImpl<?> creationalContext = bm.createCreationalContext(bean);
                            final Handler instance = Handler.class.cast(bm.getReference(bean, bean.getBeanClass(), creationalContext));

                            // hack for destroyHandlers()
                            handlers.add(instance);
                            handlerInstances.add(new InjectionProcessor<Handler>(instance, Collections.<Injection>emptySet(), null) {
                                @Override
                                public void preDestroy() {
                                    if (!normalScoped) {
                                        creationalContext.release();
                                    }
                                }
                            });
                            continue;
                        }
                    } catch (final InjectionException ie) {
                        LOGGER.info(ie.getMessage(), ie);
                    }
                }
            }

            try { // old way
                final Class<? extends Handler> handlerClass = handler.getHandlerClass().asSubclass(Handler.class);
                final InjectionProcessor<Handler> processor = new InjectionProcessor<>(handlerClass,
                    injections,
                    handler.getPostConstruct(),
                    handler.getPreDestroy(),
                    unwrap(context));
                processor.createInstance();
                processor.postConstruct();
                final Handler handlerInstance = processor.getInstance();

                handlers.add(handlerInstance);
                handlerInstances.add(processor);
            } catch (final Exception e) {
                throw new WebServiceException("Failed to instantiate handler", e);
            }
        }
        return handlers;
    }

    private boolean matchServiceName(final PortInfo info, final QName namePattern) {
        return match(info == null ? null : info.getServiceName(), namePattern);
    }

    private boolean matchPortName(final PortInfo info, final QName namePattern) {
        return match(info == null ? null : info.getPortName(), namePattern);
    }

    private boolean matchBinding(final PortInfo info, final List bindings) {
        return match(info == null ? null : info.getBindingID(), bindings);
    }

    private boolean match(final String binding, final List bindings) {
        if (binding == null) {
            return bindings == null || bindings.isEmpty();
        } else {
            if (bindings == null || bindings.isEmpty()) {
                return true;
            } else {
                final String actualBindingURI = JaxWsUtils.getBindingURI(binding);
                for (Object binding1 : bindings) {
                    final String bindingToken = (String) binding1;
                    final String bindingURI = JaxWsUtils.getBindingURI(bindingToken);
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
            return namePattern == null || namePattern.getLocalPart().equals("*");
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
     * @param handlers
     * @return sorted list of handlers
     */
    private List<Handler> sortHandlers(final List<Handler> handlers) {
        final List<LogicalHandler> logicalHandlers = new ArrayList<>();
        final List<Handler> protocolHandlers = new ArrayList<>();

        for (final Handler handler : handlers) {
            if (handler instanceof LogicalHandler) {
                logicalHandlers.add((LogicalHandler) handler);
            } else {
                protocolHandlers.add(handler);
            }
        }

        final List<Handler> sortedHandlers = new ArrayList<>();
        sortedHandlers.addAll(logicalHandlers);
        sortedHandlers.addAll(protocolHandlers);
        return sortedHandlers;
    }
}

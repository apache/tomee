/*
 *     Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.apache.openejb.server.cxf.rs;

import org.apache.cxf.jaxrs.ext.ContextProvider;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.jaxrs.model.OperationResourceInfo;
import org.apache.cxf.jaxrs.provider.ProviderFactory;
import org.apache.cxf.jaxrs.utils.JAXRSUtils;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.openejb.rest.ThreadLocalContextManager;

import javax.servlet.ServletConfig;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class Contexts {
    private Contexts() {
        // no-op
    }

    public static void bind(final Exchange exchange) {
        if (exchange == null) {
            return;
        }

        final ClassResourceInfo cri = exchange.get(OperationResourceInfo.class).getClassResourceInfo();

        // binding context fields
        final Set<Class<?>> types = new HashSet<Class<?>>();
        for (Field field : cri.getContextFields()) {
            types.add(field.getType());
        }

        bind(exchange, types);
    }

    /**
     * Using a set ensures we don't set the thread local twice or more,
     * there may be super classes with injection points of identical types
     *
     * Also allows us to get context references from other sources such as interceptors
     *
     * @param exchange
     * @param types
     */
    public static void bind(Exchange exchange, Set<Class<?>> types) {
        for (Class<?> type : types) {
            if (Request.class.equals(type)) {
                Request binding = JAXRSUtils.createContextValue(exchange.getInMessage(), null, Request.class);
                ThreadLocalContextManager.REQUEST.set(binding);
            } else if (UriInfo.class.equals(type)) {
                UriInfo binding = JAXRSUtils.createContextValue(exchange.getInMessage(), null, UriInfo.class);
                ThreadLocalContextManager.URI_INFO.set(binding);
            } else if (HttpHeaders.class.equals(type)) {
                HttpHeaders binding = JAXRSUtils.createContextValue(exchange.getInMessage(), null, HttpHeaders.class);
                ThreadLocalContextManager.HTTP_HEADERS.set(binding);
            } else if (SecurityContext.class.equals(type)) {
                SecurityContext binding = JAXRSUtils.createContextValue(exchange.getInMessage(), null, SecurityContext.class);
                ThreadLocalContextManager.SECURITY_CONTEXT.set(binding);
            } else if (ContextResolver.class.equals(type)) {
                ContextResolver<?> binding = JAXRSUtils.createContextValue(exchange.getInMessage(), type, ContextResolver.class);
                ThreadLocalContextManager.CONTEXT_RESOLVER.set(binding);
            } else if (Providers.class.equals(type)) {
                Providers providers = JAXRSUtils.createContextValue(exchange.getInMessage(), null, Providers.class);
                ThreadLocalContextManager.PROVIDERS.set(providers);
            } else if (ServletRequest.class.equals(type)) {
                ServletRequest servletRequest = JAXRSUtils.createContextValue(exchange.getInMessage(), null, ServletRequest.class);
                ThreadLocalContextManager.SERVLET_REQUEST.set(servletRequest);
            } else if (HttpServletRequest.class.equals(type)) {
                HttpServletRequest httpServletRequest = JAXRSUtils.createContextValue(exchange.getInMessage(), null, HttpServletRequest.class);
                ThreadLocalContextManager.HTTP_SERVLET_REQUEST.set(httpServletRequest);
            } else if (HttpServletResponse.class.equals(type)) {
                HttpServletResponse httpServletResponse = JAXRSUtils.createContextValue(exchange.getInMessage(), null, HttpServletResponse.class);
                ThreadLocalContextManager.HTTP_SERVLET_RESPONSE.set(httpServletResponse);
            } else if (ServletConfig.class.equals(type)) {
                ServletConfig servletConfig = JAXRSUtils.createContextValue(exchange.getInMessage(), null, ServletConfig.class);
                ThreadLocalContextManager.SERVLET_CONFIG.set(servletConfig);
            } else {
                final Message message = exchange.getInMessage();
                final ContextProvider<?> provider = ProviderFactory.getInstance(message).createContextProvider(type, message);
                if (provider != null) {
                    final Object value = provider.createContext(message);
                    Map<String, Object> map = ThreadLocalContextManager.OTHERS.get();
                    if (map == null) {
                        map = new HashMap<String, Object>();
                        ThreadLocalContextManager.OTHERS.set(map);
                    }
                    map.put(type.getName(), value);
                }
            }
        }
    }
}

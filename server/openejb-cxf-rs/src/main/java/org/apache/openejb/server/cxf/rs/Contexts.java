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
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.jaxrs.model.OperationResourceInfo;
import org.apache.cxf.jaxrs.provider.ProviderFactory;
import org.apache.cxf.jaxrs.utils.JAXRSUtils;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.openejb.cdi.CdiAppContextsService;
import org.apache.openejb.rest.ThreadLocalContextManager;
import org.apache.openejb.threads.task.CUTask;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.concurrent.ManagedTaskListener;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

public final class Contexts {
    private static final ThreadLocal<Exchange> EXCHANGE = new ThreadLocal<>();
    private static final Set<Class<?>> CONTEXT_CLASSES = contextClasses();

    private Contexts() {
        // no-op
    }

    public static Collection<Class<?>> findContextFields(final Class<?> cls, final Collection<Class<?>> types) {
        if (cls == Object.class || cls == null) {
            return Collections.emptyList();
        }
        for (final Field f : cls.getDeclaredFields()) {
            for (final Annotation a : f.getAnnotations()) {
                if (a.annotationType() == Context.class || a.annotationType() == Resource.class
                    && isContextClass(f.getType())) {
                    types.add(f.getType());
                }
            }
        }
        findContextFields(cls.getSuperclass(), types);
        return types;
    }

    private static boolean isContextClass(final Class<?> type) {
        return CONTEXT_CLASSES.contains(type);
    }

    private static Set<Class<?>> contextClasses() {
        final Set<Class<?>> classes = new HashSet<>(); classes.add(UriInfo.class);
        classes.add(SecurityContext.class);
        classes.add(HttpHeaders.class);
        classes.add(ContextResolver.class);
        classes.add(Providers.class);
        classes.add(Request.class);
        /* TODO: when we have jaxrs 2
        classes.add(ResourceInfo.class);
        classes.add(ResourceContext.class);
        */
        classes.add(Application.class);
        classes.add(HttpServletRequest.class);
        classes.add(HttpServletResponse.class);
        classes.add(ServletConfig.class);
        classes.add(ServletContext.class);
        classes.add(MessageContext.class);
        return classes;
    }

    @SuppressWarnings("UnusedDeclaration")
    public static void bind(final Exchange exchange) {
        if (exchange == null) {
            return;
        }

        final ClassResourceInfo cri = exchange.get(OperationResourceInfo.class).getClassResourceInfo();

        // binding context fields
        final Set<Class<?>> types = new HashSet<>();
        for (final Field field : cri.getContextFields()) {
            types.add(field.getType());
        }

        bind(exchange, types);
    }

    /**
     * Using a set ensures we don't set the thread local twice or more,
     * there may be super classes with injection points of identical types
     * <p/>
     * Also allows us to get context references from other sources such as interceptors
     *
     * @param exchange Exchange
     * @param types    Collection
     */
    public static void bind(final Exchange exchange, final Collection<Class<?>> types) {
        EXCHANGE.set(exchange); // used in lazy mode by RESTResourceFinder if cdi beans uses @Context, === initThreadLocal
        CdiAppContextsService.pushRequestReleasable(CleanUpThreadLocal.INSTANCE);

        for (final Class<?> type : types) {
            if (Request.class.equals(type)) {
                final Request binding = JAXRSUtils.createContextValue(exchange.getInMessage(), null, Request.class);
                ThreadLocalContextManager.REQUEST.set(binding);
            } else if (UriInfo.class.equals(type)) {
                final UriInfo binding = JAXRSUtils.createContextValue(exchange.getInMessage(), null, UriInfo.class);
                ThreadLocalContextManager.URI_INFO.set(binding);
            } else if (HttpHeaders.class.equals(type)) {
                final HttpHeaders binding = JAXRSUtils.createContextValue(exchange.getInMessage(), null, HttpHeaders.class);
                ThreadLocalContextManager.HTTP_HEADERS.set(binding);
            } else if (SecurityContext.class.equals(type)) {
                final SecurityContext binding = JAXRSUtils.createContextValue(exchange.getInMessage(), null, SecurityContext.class);
                ThreadLocalContextManager.SECURITY_CONTEXT.set(binding);
            } else if (ContextResolver.class.equals(type)) {
                final ContextResolver<?> binding = JAXRSUtils.createContextValue(exchange.getInMessage(), type, ContextResolver.class);
                ThreadLocalContextManager.CONTEXT_RESOLVER.set(binding);
            } else if (Providers.class.equals(type)) {
                final Providers providers = JAXRSUtils.createContextValue(exchange.getInMessage(), null, Providers.class);
                ThreadLocalContextManager.PROVIDERS.set(providers);
            } else if (ServletRequest.class.equals(type)) {
                ServletRequest servletRequest = JAXRSUtils.createContextValue(exchange.getInMessage(), null, ServletRequest.class);
                if (servletRequest == null) { // probably the case with CXF
                    servletRequest = JAXRSUtils.createContextValue(exchange.getInMessage(), null, HttpServletRequest.class);
                }
                ThreadLocalContextManager.SERVLET_REQUEST.set(servletRequest);
            } else if (HttpServletRequest.class.equals(type)) {
                final HttpServletRequest httpServletRequest = JAXRSUtils.createContextValue(exchange.getInMessage(), null, HttpServletRequest.class);
                ThreadLocalContextManager.HTTP_SERVLET_REQUEST.set(httpServletRequest);
            } else if (HttpServletResponse.class.equals(type)) {
                final HttpServletResponse httpServletResponse = JAXRSUtils.createContextValue(exchange.getInMessage(), null, HttpServletResponse.class);
                ThreadLocalContextManager.HTTP_SERVLET_RESPONSE.set(httpServletResponse);
            } else if (ServletConfig.class.equals(type)) {
                final ServletConfig servletConfig = JAXRSUtils.createContextValue(exchange.getInMessage(), null, ServletConfig.class);
                ThreadLocalContextManager.SERVLET_CONFIG.set(servletConfig);
            } else if (Configuration.class.equals(type)) {
                final Configuration config = JAXRSUtils.createContextValue(exchange.getInMessage(), null, Configuration.class);
                ThreadLocalContextManager.CONFIGURATION.set(config);
            } else if (ResourceInfo.class.equals(type)) {
                final ResourceInfo config = JAXRSUtils.createContextValue(exchange.getInMessage(), null, ResourceInfo.class);
                ThreadLocalContextManager.RESOURCE_INFO.set(config);
            } else if (ResourceContext.class.equals(type)) {
                final ResourceContext config = JAXRSUtils.createContextValue(exchange.getInMessage(), null, ResourceContext.class);
                ThreadLocalContextManager.RESOURCE_CONTEXT.set(config);
            } else if (Application.class.equals(type)) {
                final Application config = JAXRSUtils.createContextValue(exchange.getInMessage(), null, Application.class);
                ThreadLocalContextManager.APPLICATION.set(config);
            } else {
                final Message message = exchange.getInMessage();
                final ContextProvider<?> provider = ProviderFactory.getInstance(message).createContextProvider(type, message);
                if (provider != null) {
                    final Object value = provider.createContext(message);
                    Map<String, Object> map = ThreadLocalContextManager.OTHERS.get();
                    if (map == null) {
                        map = new HashMap<>();
                        ThreadLocalContextManager.OTHERS.set(map);
                    }
                    map.put(type.getName(), value);
                }
            }
        }
    }

    public static <T> T find(final Class<T> clazz) {
        final Message m = JAXRSUtils.getCurrentMessage();
        if (m  != null) {
            return JAXRSUtils.createContextValue(m, null, clazz);
        }
        final Exchange exchange = EXCHANGE.get();
        if (exchange == null) {
            throw new IllegalStateException("No CXF message usable for JAX-RS @Context injections in that thread so can't use " + clazz);
        }
        return JAXRSUtils.createContextValue(exchange.getInMessage(), null, clazz);
    }

    public static Object state() {
        return EXCHANGE.get();
    }

    public static Object restore(final Object oldState) {
        final Object old = state();
        EXCHANGE.set(Exchange.class.cast(oldState));
        return old;
    }

    private static class CleanUpThreadLocal implements Runnable {
        public static final Runnable INSTANCE = new CleanUpThreadLocal();

        @Override
        public void run() {
            EXCHANGE.remove();
            ThreadLocalContextManager.reset();
        }
    }
}

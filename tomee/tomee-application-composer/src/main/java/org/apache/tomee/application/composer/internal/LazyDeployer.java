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
package org.apache.tomee.application.composer.internal;

import org.apache.catalina.Wrapper;
import org.apache.catalina.core.StandardContext;
import org.apache.openejb.config.WebModule;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.observer.Observes;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.apache.tomee.application.composer.component.WebComponent;
import org.apache.tomee.catalina.TomcatWebAppBuilder;
import org.apache.tomee.catalina.event.AfterApplicationCreated;
import org.apache.tomee.catalina.registration.Registrations;
import org.apache.xbean.finder.MetaAnnotatedClass;
import org.apache.xbean.finder.MetaAnnotatedMethod;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.WebInitParam;
import java.util.Collection;
import java.util.LinkedList;

// deploys all which was not possible without having an instance of class
public class LazyDeployer {
    private final WebModule module;
    private final MetaAnnotatedClass<?> app;

    public LazyDeployer(final WebModule webModule, final MetaAnnotatedClass<?> app) {
        this.module = webModule;
        this.app = app;
    }

    public void afterApplicationCreated(@Observes final AfterApplicationCreated event) {
        Object instance = null;

        final Collection<MetaAnnotatedMethod> servlets = new LinkedList<>();
        final Collection<MetaAnnotatedMethod> filters = new LinkedList<>();
        final Collection<MetaAnnotatedMethod> listeners = new LinkedList<>();

        for (final MetaAnnotatedMethod m : app.getMethods()) {
            if (m.getParameterTypes().length > 0) {
                continue;
            }

            final WebComponent webComponent = m.getAnnotation(WebComponent.class);
            if (webComponent != null) {


                final Class<?> returnType = m.get().getReturnType();
                if (Servlet.class.isAssignableFrom(returnType)) {
                    servlets.add(m);
                } else if (Filter.class.isAssignableFrom(returnType)) {
                    filters.add(m);
                } else if (ServletContextListener.class.isAssignableFrom(returnType)) {
                    listeners.add(m);
                } else {
                    throw new IllegalArgumentException("Unsupported type: " + returnType);
                }
            }
        }
        if (servlets.size() + listeners.size() + filters.size() > 0) {
            instance = createInstance();

            final StandardContext context = tomcatContext();
            final Thread thread = Thread.currentThread();
            final ClassLoader oldLoader = thread.getContextClassLoader();
            thread.setContextClassLoader(context.getLoader().getClassLoader());
            try {
                for (final MetaAnnotatedMethod m : listeners) {
                    deployListener(instance, m, m.getAnnotation(WebComponent.class), context);
                }
                for (final MetaAnnotatedMethod m : filters) {
                    deployFilter(instance, m, m.getAnnotation(WebComponent.class), context);
                }
                for (final MetaAnnotatedMethod m : servlets) {
                    deployServlet(instance, m, m.getAnnotation(WebComponent.class), context);
                }
            } finally {
                thread.setContextClassLoader(oldLoader);
            }
        }
    }

    // side note: while we do it the listener will be initialized here which means after StandardContext is started
    // which is later than usually
    private void deployListener(final Object instance, final MetaAnnotatedMethod m, final WebComponent webComponent, final StandardContext context) {
        final Object listener;
        try {
            listener = m.get().invoke(instance);
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }

        context.addApplicationLifecycleListener(listener);
        if (ServletContextListener.class.isInstance(listener)) {
            ServletContextListener.class.cast(listener).contextInitialized(new ServletContextEvent(context.getServletContext()));
        }
    }

    private void deployFilter(final Object instance, final MetaAnnotatedMethod m, final WebComponent webComponent, final StandardContext context) {
        final Filter filter;
        try {
            filter = Filter.class.cast(m.get().invoke(instance));
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }

        final FilterDef filterDef = new FilterDef();
        filterDef.setFilterName(webComponent.name().isEmpty() ? filter.getClass().getName() : webComponent.name());
        filterDef.setAsyncSupported(String.valueOf(webComponent.asyncSupported()));
        filterDef.setFilter(filter);
        filterDef.setFilterClass(filter.getClass().getName());
        context.addFilterDef(filterDef);

        final FilterMap filterMap = new FilterMap();
        for (final String pattern : webComponent.urlPatterns()) {
            filterMap.addURLPattern(pattern);
        }
        filterMap.setFilterName(filterDef.getFilterName());
        context.addFilterMap(filterMap);

        Registrations.addFilterConfig(context, filterDef);
    }

    // TODO: cdi etc
    private Object createInstance() {
        try {
            return app.get().newInstance();
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void deployServlet(final Object instance, final MetaAnnotatedMethod m, final WebComponent webComponent, final StandardContext context) {
        final Servlet servlet;
        try {
            servlet = Servlet.class.cast(m.get().invoke(instance));
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }

        final Wrapper wrapper = context.createWrapper();
        wrapper.setName(webComponent.name().isEmpty() ? servlet.getClass().getName() : webComponent.name());
        wrapper.setServletClass(servlet.getClass().getName());
        wrapper.setAsyncSupported(webComponent.asyncSupported());
        context.addChild(wrapper);
        wrapper.setServlet(servlet);
        final ServletRegistration.Dynamic registration = context.dynamicServletAdded(wrapper);
        registration.addMapping(webComponent.urlPatterns());
        for (final WebInitParam param : webComponent.initParams()) {
            registration.setInitParameter(param.name(), param.value());
        }

        if (webComponent.loadOnStartup() >= 0) {
            try {
                wrapper.load();
            } catch (final ServletException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private StandardContext tomcatContext() {
        final StandardContext context = SystemInstance.get().getComponent(TomcatWebAppBuilder.class).getContextInfo(module.getModuleId()).standardContext;
        if (context == null) {
            throw new IllegalStateException("Can't find the app");
        }
        return context;
    }
}

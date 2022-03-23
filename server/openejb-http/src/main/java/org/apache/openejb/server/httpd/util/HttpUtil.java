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
package org.apache.openejb.server.httpd.util;

import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.core.ParentClassLoaderFinder;
import org.apache.openejb.core.WebContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.httpd.FilterListener;
import org.apache.openejb.server.httpd.HttpListener;
import org.apache.openejb.server.httpd.HttpListenerRegistry;
import org.apache.openejb.server.httpd.ServletListener;
import org.apache.webbeans.container.InjectableBeanManager;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public final class HttpUtil {
    private static final String WILDCARD = SystemInstance.get().getProperty("openejb.http.wildcard", ".*");
    static {
        setJspFactory();
    }

    private HttpUtil() {
        // no-op
    }

    public static String selectSingleAddress(final List<String> addresses) {
        if (addresses == null || addresses.isEmpty()) {
            return null;
        }

        // return the first http address
        for (final String address : addresses) {
            if (address.startsWith("http:")) {
                return address;
            }
        }
        // return the first https address
        for (final String address : addresses) {
            if (address.startsWith("https:")) {
                return address;
            }
        }
        // just return the first address
        return addresses.iterator().next();
    }

    public static void addDefaultsIfAvailable(final WebContext wc) {
        try {
            final Class<?> servlet = wc.getClassLoader().loadClass("org.apache.jasper.servlet.JspServlet");
            SystemInstance.get().getComponent(ServletContext.class).setAttribute("org.apache.tomcat.InstanceManager",
                    wc.getClassLoader().loadClass("org.apache.tomee.catalina.JavaeeInstanceManager").getConstructor(WebContext.class).newInstance(wc));
            addServlet(servlet.getName(), wc, "*.jsp");
        } catch (final Exception e) {
            // no-op
        }
    }

    public static boolean addServlet(final String classname, final WebContext wc, final String mapping) {
        final HttpListenerRegistry registry = SystemInstance.get().getComponent(HttpListenerRegistry.class);
        if (registry == null || mapping == null) {
            return false;
        }

        final ServletListener listener;
        try {
            ServletContext servletContext = wc.getServletContext();
            if (servletContext == null) {
                servletContext = SystemInstance.get().getComponent(ServletContext.class);
            }
            if ("jakarta.faces.webapp.FacesServlet".equals(classname)) {
                try {
                    // faking it to let the FacesServlet starting
                    // NOTE: needs myfaces-impl + tomcat-jasper (JspFactory)
                    // TODO: handle the whole lifecycle (cleanup mainly) + use myfaces SPI to make scanning really faster (take care should work in tomee were we already have it impl)
                    final Class<?> mfListenerClass = wc.getClassLoader().loadClass("org.apache.myfaces.webapp.StartupServletContextListener");

                    final ServletContextListener servletContextListener = ServletContextListener.class.cast(mfListenerClass.newInstance());
                    servletContext.setAttribute("jakarta.enterprise.inject.spi.BeanManager", new InjectableBeanManager(wc.getWebBeansContext().getBeanManagerImpl()));
                    final Thread thread = Thread.currentThread();
                    final ClassLoader old = setClassLoader(wc, thread);
                    try {
                        servletContextListener.contextInitialized(new ServletContextEvent(servletContext));
                    } finally {
                        thread.setContextClassLoader(old);
                    }
                    servletContext.removeAttribute("jakarta.enterprise.inject.spi.BeanManager");
                } catch (final Exception e) {
                    // no-op
                }
            }
            final Thread thread = Thread.currentThread();
            final ClassLoader old = setClassLoader(wc, thread);
            try {
                listener = new ServletListener((Servlet) wc.newInstance(wc.getClassLoader().loadClass(classname)), wc.getContextRoot());
                final ServletContext sc = servletContext;
                listener.getDelegate().init(new ServletConfig() {
                    @Override
                    public String getServletName() {
                        return classname;
                    }

                    @Override
                    public ServletContext getServletContext() {
                        return sc;
                    }

                    @Override
                    public String getInitParameter(final String s) {
                        return sc.getInitParameter(s);
                    }

                    @Override
                    public Enumeration<String> getInitParameterNames() {
                        final Enumeration<String> parameterNames = sc.getInitParameterNames();
                        return parameterNames == null ? Collections.<String>emptyEnumeration() : parameterNames;
                    }


                });
            } finally {
                thread.setContextClassLoader(old);
            }
        } catch (final Exception e) {
            throw new OpenEJBRuntimeException(e);
        }

        registry.addHttpListener(listener, pattern(wc.getContextRoot(), "/".equals(mapping) ? "/*" : mapping));
        return true;
    }

    private static void setJspFactory() {
        try {
            final ClassLoader classLoader = ParentClassLoaderFinder.Helper.get();
            final Class<?> jspFactory = classLoader.loadClass("org.apache.jasper.runtime.JspFactoryImpl");
            final Class<?> jspFactoryApi = classLoader.loadClass("jakarta.servlet.jsp.JspFactory");
            jspFactoryApi.getMethod("setDefaultFactory", jspFactoryApi).invoke(null, jspFactory.newInstance());
        } catch (final Throwable t) {
            // no-op
        }
    }

    private static ClassLoader setClassLoader(final WebContext wc, final Thread thread) {
        final ClassLoader old = thread.getContextClassLoader();
        thread.setContextClassLoader(wc.getClassLoader() == null ? wc.getAppContext().getClassLoader() : wc.getClassLoader());
        return old;
    }

    public static void removeServlet(final String mapping, final WebContext wc) {
        final HttpListenerRegistry registry = SystemInstance.get().getComponent(HttpListenerRegistry.class);
        if (registry == null || mapping == null) {
            return;
        }

        final Servlet servlet = ((ServletListener) registry.removeHttpListener(pattern(wc.getContextRoot(), mapping))).getDelegate();
        servlet.destroy();
        wc.destroy(servlet);
        if (servlet.getClass().equals("org.apache.jasper.servlet.JspServlet")) {
            SystemInstance.get().getComponent(ServletContext.class).removeAttribute("org.apache.tomcat.InstanceManager");
        }
    }

    public static boolean addFilter(final String classname, final WebContext wc, final String mapping, final FilterConfig config) {
        final HttpListenerRegistry registry = SystemInstance.get().getComponent(HttpListenerRegistry.class);
        if (registry == null || mapping == null) {
            return false;
        }

        final FilterListener listener;
        try {
            listener = new FilterListener((Filter) wc.newInstance(wc.getClassLoader().loadClass(classname)), wc.getContextRoot());
            listener.getDelegate().init(config);
        } catch (Exception e) {
            throw new OpenEJBRuntimeException(e);
        }

        registry.addHttpFilter(listener, pattern(wc.getContextRoot(), mapping));
        return true;
    }

    public static void removeFilter(final String mapping, final WebContext wc) {
        final HttpListenerRegistry registry = SystemInstance.get().getComponent(HttpListenerRegistry.class);
        if (registry == null || mapping == null) {
            return;
        }

        final Collection<HttpListener> filters = registry.removeHttpFilter(pattern(wc.getContextRoot(), mapping));
        for (HttpListener listener : filters) {
            final Filter filter = ((FilterListener) listener).getDelegate();
            filter.destroy();
            wc.destroy(filter);
        }
        filters.clear();
    }

    private static String pattern(final String contextRoot, final String mapping) {
        String path = "";
        if (contextRoot != null) {
            path = contextRoot;
        }

        if (!path.startsWith("/")) {
            path = '/' + path;
        }

        if (!mapping.startsWith("/") && !path.endsWith("/")) {
            path += '/';
        }
        path += mapping.startsWith("*.") ? WILDCARD + "\\" + mapping.substring(1) : mapping;

        if (path.endsWith("*")) {
            path = path.substring(0, path.length() - 1) + WILDCARD;
        }
        return path;
    }
}

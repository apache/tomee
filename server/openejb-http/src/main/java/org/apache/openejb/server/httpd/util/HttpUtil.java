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
import org.apache.openejb.core.WebContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.httpd.FilterListener;
import org.apache.openejb.server.httpd.HttpListener;
import org.apache.openejb.server.httpd.HttpListenerRegistry;
import org.apache.openejb.server.httpd.ServletListener;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.Servlet;
import java.util.Collection;
import java.util.List;

public final class HttpUtil {
    private static  final String WILDCARD = SystemInstance.get().getProperty("openejb.http.wildcard", ".*");

    private HttpUtil() {
        // no-op
    }

    public static String selectSingleAddress(List<String> addresses) {
        if (addresses == null || addresses.isEmpty()) {
            return null;
        }

        // return the first http address
        for (String address : addresses) {
            if (address.startsWith("http:")) {
                return address;
            }
        }
        // return the first https address
        for (String address : addresses) {
            if (address.startsWith("https:")) {
                return address;
            }
        }
        // just return the first address
        String address = addresses.iterator().next();
        return address;
    }

    public static boolean addServlet(final String classname, final WebContext wc, final String mapping) {
        final HttpListenerRegistry registry = SystemInstance.get().getComponent(HttpListenerRegistry.class);
        if (registry == null || mapping == null) {
            return false;
        }

        final ServletListener listener;
        try {
            listener = new ServletListener((Servlet) wc.newInstance(wc.getClassLoader().loadClass(classname)), wc.getContextRoot());
            listener.getDelegate().init(null);
        } catch (Exception e) {
            throw new OpenEJBRuntimeException(e);
        }

        registry.addHttpListener(listener, pattern(wc.getContextRoot(), mapping));
        return true;
    }

    public static void removeServlet(final String mapping, final WebContext wc) {
        final HttpListenerRegistry registry = SystemInstance.get().getComponent(HttpListenerRegistry.class);
        if (registry == null || mapping == null) {
            return;
        }

        final Servlet servlet = ((ServletListener) registry.removeHttpListener(pattern(wc.getContextRoot(), mapping))).getDelegate();
        servlet.destroy();
        wc.destroy(servlet);
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
        path += mapping;

        if (path.endsWith("*")) {
            path = path.substring(0, path.length()) + WILDCARD;
        }
        return path;
    }
}

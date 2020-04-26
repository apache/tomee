/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
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
package org.apache.tomee.webservices;

import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.RequestFacade;
import org.apache.openejb.server.cxf.rs.CxfRsHttpListener;
import org.apache.openejb.server.httpd.ServletRequestAdapter;
import org.apache.openejb.server.httpd.ServletResponseAdapter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CXFJAXRSFilter implements Filter {
    private static final Field REQUEST;
    static {
        try {
            REQUEST = RequestFacade.class.getDeclaredField("request");
        } catch (final NoSuchFieldException e) {
            throw new IllegalStateException(e);
        }
        REQUEST.setAccessible(true);
    }

    private final CxfRsHttpListener delegate;
    private final ConcurrentMap<Wrapper, Boolean> mappingByServlet = new ConcurrentHashMap<>();
    private final String[] welcomeFiles;
    private String mapping;

    public CXFJAXRSFilter(final CxfRsHttpListener delegate, final String[] welcomeFiles) {
        this.delegate = delegate;

        this.welcomeFiles = new String[welcomeFiles.length];
        for (int i = 0; i < welcomeFiles.length; i++) {
            this.welcomeFiles[i] = '/' + welcomeFiles[i];
        }
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        mapping = filterConfig.getInitParameter("mapping");
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        if (!HttpServletRequest.class.isInstance(request)) {
            chain.doFilter(request, response);
            return;
        }

        final HttpServletRequest httpServletRequest = HttpServletRequest.class.cast(request);
        final HttpServletResponse httpServletResponse = HttpServletResponse.class.cast(response);

        if (!this.delegate.isCXFResource(httpServletRequest)) {
            chain.doFilter(request, response);
            return;
        }

        if (CxfRsHttpListener.TRY_STATIC_RESOURCES) { // else 100% JAXRS
            if (servletMappingIsUnderRestPath(httpServletRequest)) {
                chain.doFilter(request, response);
                return;
            }
            final InputStream staticContent = delegate.findStaticContent(httpServletRequest, welcomeFiles);
            if (staticContent != null) {
                chain.doFilter(request, response);
                return;
            }
        }

        try {
            delegate.doInvoke(
                    new ServletRequestAdapter(httpServletRequest, httpServletResponse, request.getServletContext()),
                    new ServletResponseAdapter(httpServletResponse));
        } catch (final Exception e) {
            throw new ServletException("Error processing webservice request", e);
        }
    }

    private boolean servletMappingIsUnderRestPath(final HttpServletRequest request) {
        final HttpServletRequest unwrapped = unwrap(request);
        if (!RequestFacade.class.isInstance(unwrapped)) {
            return false;
        }

        final Request tr;
        try {
            tr = Request.class.cast(REQUEST.get(unwrapped));
        } catch (final IllegalAccessException e) {
            return false;
        }
        final Wrapper wrapper = tr.getWrapper();
        if (wrapper == null || mapping == null) {
            return false;
        }

        Boolean accept = mappingByServlet.get(wrapper);
        if (accept == null) {
            accept = false;
            if (!"org.apache.catalina.servlets.DefaultServlet".equals(wrapper.getServletClass())) {
                for (final String mapping : wrapper.findMappings()) {
                    if (!mapping.isEmpty() && !"/*".equals(mapping) && !"/".equals(mapping) && !mapping.startsWith("*")
                            && mapping.startsWith(this.mapping)) {
                        accept = true;
                        break;
                    }
                }
            } // else will be handed by getResourceAsStream()
            mappingByServlet.putIfAbsent(wrapper, accept);
            return accept;
        }

        return accept;
    }

    private HttpServletRequest unwrap(final HttpServletRequest request) {
        HttpServletRequest unwrapped = request;
        boolean changed;
        do {
            changed = false;
            while (HttpServletRequestWrapper.class.isInstance(unwrapped)) {
                final HttpServletRequest tmp = HttpServletRequest.class.cast(HttpServletRequestWrapper.class.cast(unwrapped).getRequest());
                if (tmp != unwrapped) {
                    unwrapped = tmp;
                } else {
                    changed = false; // quit
                    break;
                }
                changed = true;
            }
            while (ServletRequestAdapter.class.isInstance(unwrapped)) {
                unwrapped = ServletRequestAdapter.class.cast(unwrapped).getRequest();
                changed = true;
            }
        } while (changed);
        return unwrapped;
    }

    @Override
    public void destroy() {
        // no-op
    }
}

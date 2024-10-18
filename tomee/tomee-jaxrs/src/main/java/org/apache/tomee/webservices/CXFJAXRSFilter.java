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

import jakarta.servlet.ServletRegistration;
import org.apache.openejb.server.cxf.rs.CxfRsHttpListener;
import org.apache.openejb.server.httpd.ServletRequestAdapter;
import org.apache.openejb.server.httpd.ServletResponseAdapter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

public class CXFJAXRSFilter implements Filter {
    private final CxfRsHttpListener delegate;
    private final String[] welcomeFiles;

    public CXFJAXRSFilter(final CxfRsHttpListener delegate, final String[] welcomeFiles) {
        this.delegate = delegate;

        this.welcomeFiles = new String[welcomeFiles.length];
        for (int i = 0; i < welcomeFiles.length; i++) {
            this.welcomeFiles[i] = '/' + welcomeFiles[i];
        }
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

        // if a servlet matched it always has priority over JAX-RS endpoints, see TOMEE-4406
        if (nonDefaultServletMatches(httpServletRequest)) {
            chain.doFilter(request, response);
            return;
        }

        // Try to figure out if Tomcat DefaultServlet would handle this request
        if (CxfRsHttpListener.TRY_STATIC_RESOURCES) {
            try (final InputStream staticContent = delegate.findStaticContent(httpServletRequest, welcomeFiles)) {
                if (staticContent != null) {
                    chain.doFilter(request, response);
                    return;
                }
            }
        }

        // else 100% JAXRS
        try {
            delegate.doInvoke(
                    new ServletRequestAdapter(httpServletRequest, httpServletResponse, request.getServletContext()),
                    new ServletResponseAdapter(httpServletResponse));
        } catch (final Exception e) {
            throw new ServletException("Error processing webservice request", e);
        }
    }

    /**
     * Checks if the request matched a defined servlet mapping matches the given request
     *
     * @param request the HttpServletRequest to check
     * @return if the servlet mapping matches and is not the tomcat DefaultServlet
     */
    private boolean nonDefaultServletMatches(final HttpServletRequest request) {
        ServletRegistration servletRegistration = request.getServletContext().getServletRegistration(
                request.getHttpServletMapping().getServletName());

        return !"org.apache.catalina.servlets.DefaultServlet".equals(servletRegistration.getClassName());
    }

    @Override
    public void destroy() {
        // no-op
    }
}

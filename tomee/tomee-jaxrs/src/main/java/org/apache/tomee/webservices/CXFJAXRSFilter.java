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

import org.apache.openejb.server.cxf.rs.CxfRsHttpListener;
import org.apache.openejb.server.httpd.ServletRequestAdapter;
import org.apache.openejb.server.httpd.ServletResponseAdapter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
    public void init(final FilterConfig filterConfig) throws ServletException {
        // no-op
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        if (!HttpServletRequest.class.isInstance(request)) {
            chain.doFilter(request, response);
            return;
        }

        final HttpServletRequest httpServletRequest = HttpServletRequest.class.cast(request);
        final HttpServletResponse httpServletResponse = HttpServletResponse.class.cast(response);

        if (CxfRsHttpListener.TRY_STATIC_RESOURCES || delegate.matchPath(httpServletRequest)) {
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

    @Override
    public void destroy() {
        // no-op
    }
}

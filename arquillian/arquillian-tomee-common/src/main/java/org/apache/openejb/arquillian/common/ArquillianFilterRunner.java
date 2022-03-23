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
package org.apache.openejb.arquillian.common;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServlet;
import java.io.IOException;
import java.util.Enumeration;

import static java.util.Collections.emptyEnumeration;

// this filter simply allows us to invoke arquillian servlet directly
// instead of needing to pass through the servlet filters
//
// it allows us to work out of the box with framework using a dispatcher (filter) on /*
// it is commonn with tapestry, spring, wicket, ....
//
// We can't rely on scanning (@WebFilter) since we can't enrich the app because we need it for client tests too and
// we need it to be added first
//
// @WebFilter(urlPatterns = "/ArquillianServletRunner", filterName = "org.apache.openejb.arquillian.common.ArquillianFilterRunner")
public class ArquillianFilterRunner implements Filter {
    private static final String ARQUILLIAN_SERVLET_RUNNER = "org.jboss.arquillian.protocol.servlet.runner.ServletTestRunner";

    private HttpServlet delegate;

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        try {
            delegate = HttpServlet.class.cast(Thread.currentThread().getContextClassLoader().loadClass(ARQUILLIAN_SERVLET_RUNNER).newInstance());
            delegate.init(new ServletConfig() {
                @Override
                public String getServletName() {
                    return ArquillianFilterRunner.class.getName();
                }

                @Override
                public ServletContext getServletContext() {
                    return filterConfig.getServletContext();
                }

                @Override
                public String getInitParameter(final String name) {
                    return null;
                }

                @Override
                public Enumeration<String> getInitParameterNames() {
                    return emptyEnumeration();
                }
            });
        } catch (final Exception e) {
            // no-op: can happen if the servlet is not present, that's a normal case
        }
    }

    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException {
        if (delegate != null) {
            delegate.service(servletRequest, servletResponse);
        }
    }

    @Override
    public void destroy() {
        if (delegate != null) {
            delegate.destroy();
        }
    }
}

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
package org.apache.tomee.catalina.routing;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;

public final class RouteFilter implements Filter {
    private SimpleRouter router = new SimpleRouter();

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        router.JMXOn(filterConfig.getServletContext().getContextPath());
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
        }

        final String destination = router.route(((HttpServletRequest) request).getRequestURI());
        if (destination == null) {
            chain.doFilter(request, response);
            return;
        }

        ((HttpServletResponse) response).sendRedirect(destination);
    }

    @Override
    public void destroy() {
        router.cleanUp();
    }

    public void initConfigurationPath(final String prefix, final URL configurationPath) {
        router.setPrefix(prefix);
        router.readConfiguration(configurationPath);
    }
}

/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.server.webservices;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.openejb.server.httpd.HttpListener;
import org.apache.openejb.server.httpd.HttpRequest;
import org.apache.openejb.server.httpd.HttpResponse;
import org.apache.openejb.server.httpd.ServletRequestAdapter;
import org.apache.openejb.server.httpd.ServletResponseAdapter;

import java.io.IOException;

public class WsServlet implements Servlet {
    public static final String WEBSERVICE_CONTAINER = WsServlet.class.getName() + "@WebServiceContainer";

    private ServletConfig config;
    private HttpListener service;

    public WsServlet() {
    }

    public WsServlet(HttpListener service) {
        this.service = service;
    }

    public void init(ServletConfig config) throws ServletException {
        this.config = config;
        getService();
    }

    @Override
    public ServletConfig getServletConfig() {
        return config;
    }

    @Override
    public String getServletInfo() {
        return "Webservice Servlet " + getService();
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        HttpListener service = getService();
        if (service == null) throw new ServletException("WebServiceContainer has not been set");

            res.setContentType("text/xml");
            HttpRequest httpRequest = new ServletRequestAdapter((HttpServletRequest) req, (HttpServletResponse) res, config.getServletContext());
            HttpResponse httpResponse = new ServletResponseAdapter((HttpServletResponse) res);

            try {
                service.onMessage(httpRequest, httpResponse);
            } catch (IOException | ServletException e) {
                throw e;
            } catch (Exception e) {
                throw new ServletException("Error processing webservice request", e);
            }
    }

    @Override
    public void destroy() {
    }

    private synchronized HttpListener getService() {
        if (service == null) {
            ServletConfig config = getServletConfig();
            String webServiceContainerId = config.getInitParameter(WEBSERVICE_CONTAINER);
            if (webServiceContainerId != null) {
                service = (HttpListener) config.getServletContext().getAttribute(webServiceContainerId);
            }
        }
        return service;
    }

}

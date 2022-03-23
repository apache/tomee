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
package org.apache.openejb.server.rest;

import org.apache.openejb.server.httpd.HttpListener;
import org.apache.openejb.server.httpd.HttpRequest;
import org.apache.openejb.server.httpd.HttpResponse;
import org.apache.openejb.server.httpd.ServletRequestAdapter;
import org.apache.openejb.server.httpd.ServletResponseAdapter;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

// Note: better to use a filter to handle correctly static resources
public class RsServlet extends HttpServlet {
    private HttpListener listener;
    private ServletConfig servletConfig;

    @Override
    public void init(ServletConfig config) throws ServletException {
        servletConfig = config;
        String listenerId = config.getInitParameter(HttpListener.class.getName());
        if (listenerId != null) {
            listener = (HttpListener) config.getServletContext().getAttribute(listenerId);
        }
    }

    @Override
    public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (listener == null) {
            throw new ServletException("RESTServiceContainer has not been set");
        }

        HttpRequest httpRequest = new ServletRequestAdapter(req, res, servletConfig.getServletContext());
        HttpResponse httpResponse = new ServletResponseAdapter(res);

        try {
            listener.onMessage(httpRequest, httpResponse);
        } catch (IOException | ServletException e) {
            throw e;
        } catch (Exception e) {
            throw new ServletException("Error processing webservice request", e);
        }
    }
}

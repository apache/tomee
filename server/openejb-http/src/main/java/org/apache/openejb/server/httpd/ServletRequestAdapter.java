/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.server.httpd;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public class ServletRequestAdapter implements HttpRequest {
    private final HttpServletRequest request;

    public ServletRequestAdapter(HttpServletRequest request) {
        this.request = request;
    }

    public ServletRequestAdapter(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) {
        this.request = request;
        request.setAttribute(HttpRequest.SERVLET_REQUEST, request);
        request.setAttribute(HttpRequest.SERVLET_RESPONSE, response);
        request.setAttribute(HttpRequest.SERVLET_CONTEXT, servletContext);
    }

    public HttpSession getSession(boolean create) {
        javax.servlet.http.HttpSession session = request.getSession(create);
        if (session != null) {
            return new ServletSessionAdapter(session);
        } else {
            return null;
        }
    }

    public HttpSession getSession() {
        javax.servlet.http.HttpSession session = request.getSession();
        if (session != null) {
            return new ServletSessionAdapter(session);
        } else {
            return null;
        }
    }

    public String getHeader(String name) {
        return request.getHeader(name);
    }

    public URI getURI() {
        try {
            return new URI(request.getScheme(), null, request.getServerName(), request.getServerPort(), request.getRequestURI(), request.getQueryString(), null);
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public int getContentLength() {
        return request.getContentLength();
    }

    public String getContentType() {
        return request.getContentType();
    }

    public String getContextPath() {
        return request.getContextPath();
    }

    public InputStream getInputStream() throws IOException {
        return request.getInputStream();
    }

    public Method getMethod() {
        try {
            return Method.valueOf(request.getMethod());
        } catch (IllegalArgumentException e) {
            return Method.UNSUPPORTED;
        }
    }

    public String getParameter(String name) {
        return request.getParameter(name);
    }

    public Map getParameters() {
        return request.getParameterMap();
    }

    public Object getAttribute(String s) {
        Object o = request.getAttribute(s);

        return o;
    }

    public void setAttribute(String s, Object o) {
        request.setAttribute(s, o);
    }

    public String getRemoteAddr() {
        return request.getRemoteAddr();
    }

}

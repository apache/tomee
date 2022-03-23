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

import jakarta.servlet.AsyncContext;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpUpgradeHandler;
import jakarta.servlet.http.Part;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
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
        jakarta.servlet.http.HttpSession session = request.getSession(create);
        if (session != null) {
            return new ServletSessionAdapter(session);
        } else {
            return null;
        }
    }

    @Override
    public Principal getUserPrincipal() {
        return request.getUserPrincipal();
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return request.isRequestedSessionIdFromCookie();
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return request.isRequestedSessionIdFromUrl();
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return request.isRequestedSessionIdFromURL();
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return request.isRequestedSessionIdValid();
    }

    @Override
    public boolean isUserInRole(String s) {
        return request.isUserInRole(s);
    }

    @Override
    public void login(String s, String s1) throws ServletException {
        request.login(s, s1);
    }

    @Override
    public void logout() throws ServletException {
        request.logout();
    }

    public HttpSession getSession() {
        jakarta.servlet.http.HttpSession session = request.getSession();
        if (session != null) {
            return new ServletSessionAdapter(session);
        } else {
            return null;
        }
    }

    @Override
    public String changeSessionId() {
        return request.changeSessionId();
    }

    public String getHeader(String name) {
        return request.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return request.getHeaderNames();
    }

    @Override
    public Enumeration<String> getHeaders(String s) {
        return request.getHeaders(s);
    }

    @Override
    public int getIntHeader(String s) {
        return request.getIntHeader(s);
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

    @Override
    public long getContentLengthLong() {
        return request.getContentLengthLong();
    }

    public String getContentType() {
        return request.getContentType();
    }

    @Override
    public DispatcherType getDispatcherType() {
        return request.getDispatcherType();
    }

    @Override
    public boolean authenticate(HttpServletResponse httpServletResponse) throws IOException, ServletException {
        return request.authenticate(httpServletResponse);
    }

    @Override
    public String getAuthType() {
        return request.getAuthType();
    }

    public String getContextPath() {
        return request.getContextPath();
    }

    @Override
    public Cookie[] getCookies() {
        return request.getCookies();
    }

    @Override
    public long getDateHeader(String s) {
        return request.getDateHeader(s);
    }

    public ServletInputStream getInputStream() throws IOException {
        return request.getInputStream();
    }

    @Override
    public String getLocalAddr() {
        return request.getLocalAddr();
    }

    @Override
    public Locale getLocale() {
        return request.getLocale();
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return request.getLocales();
    }

    @Override
    public String getLocalName() {
        return request.getLocalName();
    }

    @Override
    public int getLocalPort() {
        return request.getLocalPort();
    }

    public String getMethod() {
        return request.getMethod(); // some method can be added so don't filter it
    }

    @Override
    public Part getPart(String s) throws IOException, ServletException {
        return request.getPart(s);
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(final Class<T> httpUpgradeHandlerClass) throws IOException, ServletException {
        return request.upgrade(httpUpgradeHandlerClass);
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return request.getParts();
    }

    @Override
    public String getPathInfo() {
        return request.getPathInfo();
    }

    @Override
    public String getPathTranslated() {
        return request.getPathTranslated();
    }

    @Override
    public String getQueryString() {
        return request.getQueryString();
    }

    @Override
    public String getRemoteUser() {
        return request.getRemoteUser();
    }

    @Override
    public String getRequestedSessionId() {
        return request.getRequestedSessionId();
    }

    @Override
    public String getRequestURI() {
        return request.getRequestURI();
    }

    @Override
    public StringBuffer getRequestURL() {
        return request.getRequestURL();
    }

    @Override
    public String getServletPath() {
        return request.getServletPath();
    }

    public String getParameter(String name) {
        return request.getParameter(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return request.getParameterMap();
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return request.getParameterNames();
    }

    @Override
    public String[] getParameterValues(String s) {
        return request.getParameterValues(s);
    }

    @Override
    public String getProtocol() {
        return request.getProtocol();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return request.getReader();
    }

    @Override
    public String getRealPath(String s) {
        return request.getRealPath(s);
    }

    public Map getParameters() {
        return request.getParameterMap();
    }

    @Override
    public AsyncContext getAsyncContext() {
        return request.getAsyncContext();
    }

    public Object getAttribute(String s) {
        return request.getAttribute(s);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return request.getAttributeNames();
    }

    @Override
    public String getCharacterEncoding() {
        return request.getCharacterEncoding();
    }

    public void setAttribute(String s, Object o) {
        request.setAttribute(s, o);
    }

    @Override
    public void setCharacterEncoding(String s) throws UnsupportedEncodingException {
        request.setCharacterEncoding(s);
    }

    @Override
    public AsyncContext startAsync() {
        return request.startAsync();
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) {
        return request.startAsync(servletRequest, servletResponse);
    }

    public String getRemoteAddr() {
        return request.getRemoteAddr();
    }

    @Override
    public String getRemoteHost() {
        return request.getRemoteHost();
    }

    @Override
    public int getRemotePort() {
        return request.getRemotePort();
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String s) {
        return request.getRequestDispatcher(s);
    }

    @Override
    public String getScheme() {
        return request.getScheme();
    }

    @Override
    public String getServerName() {
        return request.getServerName();
    }

    @Override
    public int getServerPort() {
        return request.getServerPort();
    }

    @Override
    public ServletContext getServletContext() {
        return request.getServletContext();
    }

    @Override
    public boolean isAsyncStarted() {
        return request.isAsyncStarted();
    }

    @Override
    public boolean isAsyncSupported() {
        return request.isAsyncSupported();
    }

    @Override
    public boolean isSecure() {
        return request.isSecure();
    }

    @Override
    public void removeAttribute(String s) {
        request.removeAttribute(s);
    }

    public HttpServletRequest getRequest() {
        return request;
    }
}

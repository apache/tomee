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

package org.apache.openejb.rest;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

public class ThreadLocalHttpServletRequest extends AbstractRestThreadLocalProxy<HttpServletRequest>
    implements HttpServletRequest {

    protected ThreadLocalHttpServletRequest() {
        super(HttpServletRequest.class);
    }

    @Override
    public AsyncContext getAsyncContext() {
        return get().getAsyncContext();
    }

    @Override
    public Object getAttribute(String string) {
        return get().getAttribute(string);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return get().getAttributeNames();
    }

    @Override
    public String getCharacterEncoding() {
        return get().getCharacterEncoding();
    }

    @Override
    public int getContentLength() {
        return get().getContentLength();
    }

    @Override
    public String getContentType() {
        return get().getContentType();
    }

    @Override
    public DispatcherType getDispatcherType() {
        return get().getDispatcherType();
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return get().getInputStream();
    }

    @Override
    public String getLocalAddr() {
        return get().getLocalAddr();
    }

    @Override
    public Locale getLocale() {
        return get().getLocale();
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return get().getLocales();
    }

    @Override
    public String getLocalName() {
        return get().getLocalName();
    }

    @Override
    public int getLocalPort() {
        return get().getLocalPort();
    }

    @Override
    public String getParameter(String string) {
        return get().getParameter(string);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return get().getParameterMap();
    }

    @Override
    public String[] getParameterValues(String string) {
        return get().getParameterValues(string);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return get().getParameterNames();
    }

    @Override
    public String getProtocol() {
        return get().getProtocol();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return get().getReader();
    }

    @Override
    public String getRealPath(String string) {
        return get().getRealPath(string);
    }

    @Override
    public String getRemoteAddr() {
        return get().getRemoteAddr();
    }

    @Override
    public String getRemoteHost() {
        return get().getRemoteHost();
    }

    @Override
    public int getRemotePort() {
        return get().getRemotePort();
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String string) {
        return get().getRequestDispatcher(string);
    }

    @Override
    public String getScheme() {
        return get().getScheme();
    }

    @Override
    public String getServerName() {
        return get().getServerName();
    }

    @Override
    public int getServerPort() {
        return get().getServerPort();
    }

    @Override
    public ServletContext getServletContext() {
        return get().getServletContext();
    }

    @Override
    public boolean isAsyncStarted() {
        return get().isAsyncStarted();
    }

    @Override
    public boolean isAsyncSupported() {
        return get().isAsyncSupported();
    }

    @Override
    public boolean isSecure() {
        return get().isSecure();
    }

    @Override
    public void removeAttribute(String string) {
        get().removeAttribute(string);
    }

    @Override
    public void setAttribute(String string, Object object) {
        get().setAttribute(string, object);
    }

    @Override
    public void setCharacterEncoding(String string) throws UnsupportedEncodingException {
        get().setCharacterEncoding(string);
    }

    @Override
    public AsyncContext startAsync() {
        return get().startAsync();
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) {
        return get().startAsync(servletRequest, servletResponse);
    }

    @Override
    public boolean authenticate(HttpServletResponse httpServletResponse) throws IOException, ServletException {
        return get().authenticate(httpServletResponse);
    }

    @Override
    public String getAuthType() {
        return get().getAuthType();
    }

    @Override
    public String getContextPath() {
        return get().getContextPath();
    }

    @Override
    public Cookie[] getCookies() {
        return get().getCookies();
    }

    @Override
    public long getDateHeader(String s) {
        return get().getDateHeader(s);
    }

    @Override
    public String getHeader(String s) {
        return get().getHeader(s);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return get().getHeaderNames();
    }

    @Override
    public Enumeration<String> getHeaders(String s) {
        return get().getHeaders(s);
    }

    @Override
    public int getIntHeader(String s) {
        return get().getIntHeader(s);
    }

    @Override
    public String getMethod() {
        return get().getMethod();
    }

    @Override
    public Part getPart(String s) throws IOException, ServletException {
        return get().getPart(s);
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return get().getParts();
    }

    @Override
    public String getPathInfo() {
        return get().getPathInfo();
    }

    @Override
    public String getPathTranslated() {
        return get().getPathTranslated();
    }

    @Override
    public String getQueryString() {
        return get().getQueryString();
    }

    @Override
    public String getRemoteUser() {
        return get().getRemoteUser();
    }

    @Override
    public String getRequestedSessionId() {
        return get().getRequestedSessionId();
    }

    @Override
    public String getRequestURI() {
        return get().getRequestURI();
    }

    @Override
    public StringBuffer getRequestURL() {
        return get().getRequestURL();
    }

    @Override
    public String getServletPath() {
        return get().getServletPath();
    }

    @Override
    public HttpSession getSession() {
        return get().getSession();
    }

    @Override
    public HttpSession getSession(boolean b) {
        return get().getSession(b);
    }

    @Override
    public Principal getUserPrincipal() {
        return get().getUserPrincipal();
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return get().isRequestedSessionIdFromCookie();
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return get().isRequestedSessionIdFromUrl();
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return get().isRequestedSessionIdFromURL();
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return get().isRequestedSessionIdValid();
    }

    @Override
    public boolean isUserInRole(String s) {
        return get().isUserInRole(s);
    }

    @Override
    public void login(String s, String s1) throws ServletException {
        get().login(s, s1);
    }

    @Override
    public void logout() throws ServletException {
        get().logout();
    }
}

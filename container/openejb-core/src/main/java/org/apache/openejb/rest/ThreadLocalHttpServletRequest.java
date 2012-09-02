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

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.IOException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;

public class ThreadLocalHttpServletRequest extends ThreadLocalServletRequest
    implements HttpServletRequest {

    private HttpServletRequest request() {
        return (HttpServletRequest) super.get();
    }

    @Override
    public boolean authenticate(HttpServletResponse httpServletResponse) throws IOException, ServletException {
        return request().authenticate(httpServletResponse);
    }

    @Override
    public String getAuthType() {
        return request().getAuthType();
    }

    @Override
    public String getContextPath() {
        return request().getContextPath();
    }

    @Override
    public Cookie[] getCookies() {
        return request().getCookies();
    }

    @Override
    public long getDateHeader(String s) {
        return request().getDateHeader(s);
    }

    @Override
    public String getHeader(String s) {
        return request().getHeader(s);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return request().getHeaderNames();
    }

    @Override
    public Enumeration<String> getHeaders(String s) {
        return request().getHeaders(s);
    }

    @Override
    public int getIntHeader(String s) {
        return request().getIntHeader(s);
    }

    @Override
    public String getMethod() {
        return request().getMethod();
    }

    @Override
    public Part getPart(String s) throws IOException, ServletException {
        return request().getPart(s);
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return request().getParts();
    }

    @Override
    public String getPathInfo() {
        return request().getPathInfo();
    }

    @Override
    public String getPathTranslated() {
        return request().getPathTranslated();
    }

    @Override
    public String getQueryString() {
        return request().getQueryString();
    }

    @Override
    public String getRemoteUser() {
        return request().getRemoteUser();
    }

    @Override
    public String getRequestedSessionId() {
        return request().getRequestedSessionId();
    }

    @Override
    public String getRequestURI() {
        return request().getRequestURI();
    }

    @Override
    public StringBuffer getRequestURL() {
        return request().getRequestURL();
    }

    @Override
    public String getServletPath() {
        return request().getServletPath();
    }

    @Override
    public HttpSession getSession() {
        return request().getSession();
    }

    @Override
    public HttpSession getSession(boolean b) {
        return request().getSession(b);
    }

    @Override
    public Principal getUserPrincipal() {
        return request().getUserPrincipal();
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return request().isRequestedSessionIdFromCookie();
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return request().isRequestedSessionIdFromUrl();
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return request().isRequestedSessionIdFromURL();
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return request().isRequestedSessionIdValid();
    }

    @Override
    public boolean isUserInRole(String s) {
        return request().isUserInRole(s);
    }

    @Override
    public void login(String s, String s1) throws ServletException {
        request().login(s, s1);
    }

    @Override
    public void logout() throws ServletException {
        request().logout();
    }
}

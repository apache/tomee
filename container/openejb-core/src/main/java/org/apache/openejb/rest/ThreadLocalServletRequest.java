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

import jakarta.servlet.AsyncContext;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

public class ThreadLocalServletRequest extends AbstractRestThreadLocalProxy<ServletRequest>
    implements ServletRequest {

    protected ThreadLocalServletRequest() {
        super(ServletRequest.class);
    }

    @Override
    public AsyncContext getAsyncContext() {
        return get().getAsyncContext();
    }

    @Override
    public Object getAttribute(final String string) {
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
    public long getContentLengthLong() {
        return get().getContentLengthLong();
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
    public String getParameter(final String string) {
        return get().getParameter(string);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return get().getParameterMap();
    }

    @Override
    public String[] getParameterValues(final String string) {
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
    public String getRealPath(final String string) {
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
    public RequestDispatcher getRequestDispatcher(final String string) {
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
    public void removeAttribute(final String string) {
        get().removeAttribute(string);
    }

    @Override
    public void setAttribute(final String string, final Object object) {
        get().setAttribute(string, object);
    }

    @Override
    public void setCharacterEncoding(final String string) throws UnsupportedEncodingException {
        get().setCharacterEncoding(string);
    }

    @Override
    public AsyncContext startAsync() {
        return get().startAsync();
    }

    @Override
    public AsyncContext startAsync(final ServletRequest servletRequest, final ServletResponse servletResponse) {
        return get().startAsync(servletRequest, servletResponse);
    }
}

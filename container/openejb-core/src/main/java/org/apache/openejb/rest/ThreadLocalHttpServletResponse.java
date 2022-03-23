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

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;

public class ThreadLocalHttpServletResponse extends AbstractRestThreadLocalProxy<HttpServletResponse> implements HttpServletResponse {
    protected ThreadLocalHttpServletResponse() {
        super(HttpServletResponse.class);
    }

    @Override
    public void addCookie(final Cookie cookie) {
        get().addCookie(cookie);
    }

    @Override
    public void addDateHeader(final String name, final long date) {
        get().addDateHeader(name, date);
    }

    @Override
    public void addHeader(final String name, final String value) {
        get().addHeader(name, value);
    }

    @Override
    public void addIntHeader(final String name, final int value) {
        get().addIntHeader(name, value);
    }

    @Override
    public boolean containsHeader(final String name) {
        return get().containsHeader(name);
    }

    @Override
    public String encodeURL(final String url) {
        return get().encodeURL(url);
    }

    @Override
    public String encodeRedirectURL(final String url) {
        return get().encodeRedirectURL(url);
    }

    @Override
    public String encodeUrl(final String url) {
        return get().encodeUrl(url);
    }

    @Override
    public String encodeRedirectUrl(final String url) {
        return get().encodeRedirectUrl(url);
    }

    @Override
    public String getHeader(final String name) {
        return get().getHeader(name);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return get().getHeaderNames();
    }

    @Override
    public Collection<String> getHeaders(final String headerName) {
        return get().getHeaders(headerName);
    }

    @Override
    public int getStatus() {
        return get().getStatus();
    }

    @Override
    public void sendError(final int sc) throws IOException {
        get().sendError(sc);
    }

    @Override
    public void sendError(final int sc, final String msg) throws IOException {
        get().sendError(sc, msg);
    }

    @Override
    public void sendRedirect(final String location) throws IOException {
        get().sendRedirect(location);
    }

    @Override
    public void setDateHeader(final String name, final long date) {
        get().setDateHeader(name, date);
    }

    @Override
    public void setHeader(final String name, final String value) {
        get().setHeader(name, value);
    }

    @Override
    public void setIntHeader(final String name, final int value) {
        get().setIntHeader(name, value);
    }

    @Override
    public void setStatus(final int sc) {
        get().setStatus(sc);
    }

    @Override
    public void setStatus(final int sc, final String sm) {
        get().setStatus(sc, sm);
    }

    @Override
    public void flushBuffer() throws IOException {
        get().flushBuffer();
    }

    @Override
    public int getBufferSize() {
        return get().getBufferSize();
    }

    @Override
    public String getCharacterEncoding() {
        return get().getCharacterEncoding();
    }

    @Override
    public String getContentType() {
        return get().getContentType();
    }

    @Override
    public Locale getLocale() {
        return get().getLocale();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return get().getOutputStream();
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return get().getWriter();
    }

    @Override
    public boolean isCommitted() {
        return get().isCommitted();
    }

    @Override
    public void reset() {
        get().reset();
    }

    @Override
    public void resetBuffer() {
        get().resetBuffer();
    }

    @Override
    public void setBufferSize(final int size) {
        get().setBufferSize(size);
    }

    @Override
    public void setCharacterEncoding(final String charset) {
        get().setCharacterEncoding(charset);
    }

    @Override
    public void setContentLength(final int len) {
        get().setContentLength(len);
    }

    @Override
    public void setContentLengthLong(final long length) {
        get().setContentLengthLong(length);
    }

    @Override
    public void setContentType(final String type) {
        get().setContentType(type);
    }

    @Override
    public void setLocale(final Locale loc) {
        get().setLocale(loc);
    }
}

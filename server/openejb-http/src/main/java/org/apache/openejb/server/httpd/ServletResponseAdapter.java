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

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Locale;

public class ServletResponseAdapter implements HttpResponse {
    private final HttpServletResponse response;

    public ServletResponseAdapter(final HttpServletResponse response) {
        this.response = response;
    }

    public void setHeader(final String name, final String value) {
        response.setHeader(name, value);
    }

    @Override
    public void setIntHeader(final String s, final int i) {
        response.setIntHeader(s, i);
    }

    @Override
    public void setStatus(final int i) {
        response.setStatus(i);
    }

    @Override
    public void setStatus(final int i, final String s) {
        response.setStatus(i, s);
    }

    @Override
    public void addCookie(final Cookie cookie) {
        response.addCookie(cookie);
    }

    @Override
    public void addDateHeader(final String s, final long l) {
        response.addDateHeader(s, l);
    }

    @Override
    public void addHeader(final String s, final String s1) {
        response.addHeader(s, s1);
    }

    @Override
    public void addIntHeader(final String s, final int i) {
        response.addIntHeader(s, i);
    }

    @Override
    public boolean containsHeader(final String s) {
        return response.containsHeader(s);
    }

    @Override
    public String encodeURL(final String s) {
        return response.encodeURL(s);
    }

    @Override
    public String encodeRedirectURL(final String s) {
        return response.encodeRedirectURL(s);
    }

    @Override
    public String encodeUrl(final String s) {
        return response.encodeUrl(s);
    }

    @Override
    public String encodeRedirectUrl(final String s) {
        return response.encodeRedirectUrl(s);
    }

    public String getHeader(final String name) {
        throw new UnsupportedOperationException("Not possible to implement");
    }

    @Override
    public Collection<String> getHeaderNames() {
        return response.getHeaderNames();
    }

    @Override
    public Collection<String> getHeaders(final String s) {
        return response.getHeaders(s);
    }

    @Override
    public int getStatus() {
        return response.getStatus();
    }

    @Override
    public void sendError(final int i) throws IOException {
        response.sendError(i);
    }

    @Override
    public void sendError(final int i, final String s) throws IOException {
        response.sendError(i, s);
    }

    @Override
    public void sendRedirect(final String s) throws IOException {
        response.sendRedirect(s);
    }

    @Override
    public void setDateHeader(final String s, final long l) {
        response.setDateHeader(s, l);
    }

    public ServletOutputStream getOutputStream() {
        try {
            return response.getOutputStream();
        } catch (final IOException e) {
            throw (IllegalStateException) new IllegalStateException().initCause(e);
        }
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return response.getWriter();
    }

    @Override
    public boolean isCommitted() {
        return response.isCommitted();
    }

    @Override
    public void reset() {
        response.reset();
    }

    @Override
    public void resetBuffer() {
        response.resetBuffer();
    }

    @Override
    public void setBufferSize(final int i) {
        response.setBufferSize(i);
    }

    @Override
    public void setCharacterEncoding(final String s) {
        response.setCharacterEncoding(s);
    }

    @Override
    public void setContentLength(final int i) {
        response.setContentLength(i);
    }

    public void setContentType(final String type) {
        response.setContentType(type);
    }

    @Override
    public void setLocale(final Locale locale) {
        response.setLocale(locale);
    }

    public String getContentType() {
        return response.getContentType();
    }

    @Override
    public Locale getLocale() {
        return response.getLocale();
    }

    @SuppressWarnings({"deprecation"})
    public void setStatusMessage(final String responseString) {
        response.setStatus(getStatus(), responseString);
    }

    public void flushBuffer() throws IOException {
        response.flushBuffer();
    }

    @Override
    public int getBufferSize() {
        return response.getBufferSize();
    }

    @Override
    public String getCharacterEncoding() {
        return response.getCharacterEncoding();  //To change body of implemented methods use File | Settings | File Templates.
    }
}

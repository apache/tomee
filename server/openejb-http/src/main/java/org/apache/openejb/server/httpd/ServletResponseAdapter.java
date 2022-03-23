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

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Locale;

public class ServletResponseAdapter implements HttpResponse {
    private final HttpServletResponse response;

    public ServletResponseAdapter(HttpServletResponse response) {
        this.response = response;
    }

    public void setHeader(String name, String value) {
        response.setHeader(name, value);
    }

    @Override
    public void setIntHeader(String s, int i) {
        response.setIntHeader(s, i);
    }

    @Override
    public void setStatus(int i) {
        response.setStatus(i);
    }

    @Override
    public void setStatus(int i, String s) {
        response.setStatus(i, s);
    }

    @Override
    public void addCookie(Cookie cookie) {
        response.addCookie(cookie);
    }

    @Override
    public void addDateHeader(String s, long l) {
        response.addDateHeader(s, l);
    }

    @Override
    public void addHeader(String s, String s1) {
        response.addHeader(s, s1);
    }

    @Override
    public void addIntHeader(String s, int i) {
        response.addIntHeader(s, i);
    }

    @Override
    public boolean containsHeader(String s) {
        return response.containsHeader(s);
    }

    @Override
    public String encodeURL(String s) {
        return response.encodeURL(s);
    }

    @Override
    public String encodeRedirectURL(String s) {
        return response.encodeRedirectURL(s);
    }

    @Override
    public String encodeUrl(String s) {
        return response.encodeUrl(s);
    }

    @Override
    public String encodeRedirectUrl(String s) {
        return response.encodeRedirectUrl(s);
    }

    public String getHeader(String name) {
        throw new UnsupportedOperationException("Not possible to implement");
    }

    @Override
    public Collection<String> getHeaderNames() {
        return response.getHeaderNames();
    }

    @Override
    public Collection<String> getHeaders(String s) {
        return response.getHeaders(s);
    }

    @Override
    public int getStatus() {
        return response.getStatus();
    }

    @Override
    public void sendError(int i) throws IOException {
        response.sendError(i);
    }

    @Override
    public void sendError(int i, String s) throws IOException {
        response.sendError(i, s);
    }

    @Override
    public void sendRedirect(String s) throws IOException {
        response.sendRedirect(s);
    }

    @Override
    public void setDateHeader(String s, long l) {
        response.setDateHeader(s, l);
    }

    public ServletOutputStream getOutputStream() {
        try {
            return response.getOutputStream();
        } catch (IOException e) {
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
    public void setBufferSize(int i) {
        response.setBufferSize(i);
    }

    @Override
    public void setCharacterEncoding(String s) {
        response.setCharacterEncoding(s);
    }

    @Override
    public void setContentLength(int i) {
        response.setContentLength(i);
    }

    @Override
    public void setContentLengthLong(long length) {
        response.setContentLengthLong(length);
    }

    public void setContentType(String type) {
        response.setContentType(type);
    }

    @Override
    public void setLocale(Locale locale) {
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
    public void setStatusMessage(String responseString) {
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

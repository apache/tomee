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
package org.apache.tomee.security.http;

import org.apache.tomcat.util.buf.ByteChunk;

import javax.servlet.http.Cookie;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Mostly copied from org.apache.catalina.authenticator.SavedRequest.
 */
public final class SavedRequest {

    SavedRequest() {
    }

    /**
     * The set of Cookies associated with this Request.
     */
    private final List<Cookie> cookies = new ArrayList<>();

    public void addCookie(Cookie cookie) {
        cookies.add(cookie);
    }

    public Iterator<Cookie> getCookies() {
        return cookies.iterator();
    }


    /**
     * The set of Headers associated with this Request.  Each key is a header
     * name, while the value is a List containing one or more actual
     * values for this header.  The values are returned as an Iterator when
     * you ask for them.
     */
    private final Map<String, List<String>> headers = new HashMap<>();

    public void addHeader(String name, String value) {
        List<String> values = headers.get(name);
        if (values == null) {
            values = new ArrayList<>();
            headers.put(name, values);
        }
        values.add(value);
    }

    public Iterator<String> getHeaderNames() {
        return headers.keySet().iterator();
    }

    public Iterator<String> getHeaderValues(String name) {
        List<String> values = headers.get(name);
        if (values == null) { return Collections.emptyIterator(); } else { return values.iterator(); }
    }


    /**
     * The set of Locales associated with this Request.
     */
    private final List<Locale> locales = new ArrayList<>();

    public void addLocale(Locale locale) {
        locales.add(locale);
    }

    public Iterator<Locale> getLocales() {
        return locales.iterator();
    }


    /**
     * The request method used on this Request.
     */
    private String method = null;

    public String getMethod() {
        return this.method;
    }

    public void setMethod(String method) {
        this.method = method;
    }


    /**
     * The query string associated with this Request.
     */
    private String queryString = null;

    public String getQueryString() {
        return this.queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }


    /**
     * The request URI associated with this Request. See javax.servlet.http.HttpServletRequest#getRequestURI().
     */
    private String requestURI = null;

    public String getRequestURI() {
        return this.requestURI;
    }

    public void setRequestURI(String requestURI) {
        this.requestURI = requestURI;
    }


    /**
     * The decode request URL associated with this Request. See javax.servlet.http.HttpServletRequest#getRequestURL().
     */
    private String requestURL = null;

    public String getRequestURL() {
        return this.requestURL;
    }

    public void setRequestURL(String requestURL) {
        this.requestURL = requestURL;
    }


    /**
     * The body of this request.
     */
    private ByteChunk body = null;

    public ByteChunk getBody() {
        return this.body;
    }

    public void setBody(ByteChunk body) {
        this.body = body;
    }

    /**
     * The content type of the request, used if this is a POST.
     */
    private String contentType = null;

    public String getContentType() {
        return this.contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getRequestURLWithQueryString() {
        return queryString == null || queryString.isEmpty() ? requestURL : requestURL + "?" + queryString;
    }
}

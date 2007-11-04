/**
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
package org.apache.openejb.server.httpd;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;


/** An interface to take care of HTTP Requests.  It parses headers, content, form and url
 *  parameters.
 *
 */
public interface HttpRequest extends java.io.Serializable{

    /** the HTTP OPTIONS type */
    public static final int OPTIONS = 0; // Section 9.2
    /** the HTTP GET type */
    public static final int GET     = 1; // Section 9.3
    /** the HTTP HEAD type */
    public static final int HEAD    = 2; // Section 9.4
    /** the HTTP POST type */
    public static final int POST    = 3; // Section 9.5
    /** the HTTP PUT type */
    public static final int PUT     = 4; // Section 9.6
    /** the HTTP DELETE type */
    public static final int DELETE  = 5; // Section 9.7
    /** the HTTP TRACE type */
    public static final int TRACE   = 6; // Section 9.8
    /** the HTTP CONNECT type */
    public static final int CONNECT = 7; // Section 9.9
    /** the HTTP UNSUPPORTED type */
    public static final int UNSUPPORTED = 8;

    /* 
    * Header variables
    */
    /** the Accept header */
    public static final String HEADER_ACCEPT = "Accept";
    /** the Accept-Encoding header */
    public static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    /** the Accept-Language header */
    public static final String HEADER_ACCEPT_LANGUAGE = "Accept-Language";
    /** the Content-Type header */
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    /** the Content-Length header */
    public static final String HEADER_CONTENT_LENGTH = "Content-Length";
    /** the Connection header */
    public static final String HEADER_CONNECTION = "Connection";
    /** the Cache-Control header */
    public static final String HEADER_CACHE_CONTROL = "Cache-Control";
    /** the Host header */
    public static final String HEADER_HOST = "Host";
    /** the User-Agent header */
    public static final String HEADER_USER_AGENT = "User-Agent";
    /** the Set-Cookie header */
    public static final String HEADER_SET_COOKIE = "Set-Cookie";
    /** the Cookie header */
    public static final String HEADER_COOKIE = "Cookie";

    /**
     * Gets a form or URL query parameter based on the name passed in.
     * @param name
     */
    String getParameter(String name);

    /**
     * Gets all the form and URL query parameters
     * @return All the form and URL query parameters
     */
    Map getParameters();

    /**
     * Returns the current <code>HttpSession</code> associated with this
     * request or, if there is no current session and <code>create</code> is
     * true, returns a new session.
     *
     * <p>If <code>create</code> is <code>false</code> and the request has no
     * valid <code>HttpSession</code>, this method returns <code>null</code>.
     *
     * @param create <code>true</code> to create a new session for this request
     * if necessary; <code>false</code> to return <code>null</code> if there's
     * no current session
     *
     * @return the <code>HttpSession</code> associated with this request or
     * <code>null</code> if <code>create</code> is <code>false</code> and the
     * request has no valid session
     *
     * @see #getSession()
     */
    public HttpSession getSession(boolean create);

    /**
     * Returns the current session associated with this request, or if the
     * request does not have a session, creates one.
     *
     * @return the <code>HttpSession</code> associated with this request
     *
     * @see #getSession(boolean)
     */
    public HttpSession getSession();

    /** Gets a header based the header name passed in.
     * @param name The name of the header to get
     * @return The value of the header
     */
    public String getHeader(String name);

    /** Gets an integer value of the request method.  These values are:
     *
     * OPTIONS = 0
     * GET     = 1
     * HEAD    = 2
     * POST    = 3
     * PUT     = 4
     * DELETE  = 5
     * TRACE   = 6
     * CONNECT = 7
     * UNSUPPORTED = 8
     * @return The integer value of the method
     */
    public int getMethod();

    /** Gets the URI for the current URL page.
     * @return The URI
     */
    public java.net.URI getURI();

    int getContentLength();

    String getContentType();

    InputStream getInputStream() throws IOException;

    public Object getAttribute(String name);

    public void setAttribute(String name, Object value);

    String getRemoteAddr();
}

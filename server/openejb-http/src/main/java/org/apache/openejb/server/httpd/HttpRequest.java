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
    /**
     * Request methods
     */
    public static enum Method {
        OPTIONS, GET, HEAD, POST, PUT, DELETE, TRACE, CONNECT, UNSUPPORTED
    }

    //
    // Header variables
    //
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

    //
    // Common attrobute values
    //
    /**
     * If the https server implementation is based on Servlets, the real HttpServletRequest
     * will be registered in the request attributes using this name.
     */
    public static final String SERVLET_REQUEST = HttpRequest.class.getName() + "@ServletRequest";

    /**
     * If the https server implementation is based on Servlets, the real HttpServletResponse
     * will be registered in the request attributes using this name.
     */
    public static final String SERVLET_RESPONSE = HttpRequest.class.getName() + "@ServletResponse";

    /**
     * If the https server implementation is based on Servlets, the real ServletContext
     * will be registered in the request attributes using this name.  Note: a ServletContext
     * may not be registered even if HttpServletRequest and HttpServletResponse objects are
     * registered.
     */
    public static final String SERVLET_CONTEXT = HttpRequest.class.getName() + "@ServletContext";

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

    /** Gets an integer value of the request method.
     * @return The integer value of the method
     */
    public Method getMethod();

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

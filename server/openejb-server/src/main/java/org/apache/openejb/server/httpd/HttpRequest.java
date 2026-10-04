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

import jakarta.servlet.http.HttpServletRequest;

/**
 * The HTTP request handed to an {@link HttpListener}, backed by a servlet request.
 */
public interface HttpRequest extends java.io.Serializable, HttpServletRequest {

    //
    // Common attribute values
    //
    /**
     * The real HttpServletRequest is registered in the request attributes using this name.
     */
    public static final String SERVLET_REQUEST = HttpRequest.class.getName() + "@ServletRequest";

    /**
     * The real HttpServletResponse is registered in the request attributes using this name.
     */
    public static final String SERVLET_RESPONSE = HttpRequest.class.getName() + "@ServletResponse";

    /**
     * The real ServletContext is registered in the request attributes using this name.
     */
    public static final String SERVLET_CONTEXT = HttpRequest.class.getName() + "@ServletContext";

    /**
     * Gets a form or URL query parameter based on the name passed in.
     *
     * @param name
     */
    String getParameter(String name);

    int getContentLength();

    String getContentType();

    public Object getAttribute(String name);

    public void setAttribute(String name, Object value);

    String getRemoteAddr();

}

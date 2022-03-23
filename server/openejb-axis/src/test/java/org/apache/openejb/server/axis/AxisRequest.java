/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.server.axis;

import jakarta.servlet.ServletInputStream;

import org.apache.openejb.server.httpd.HttpRequest;
import org.apache.openejb.server.httpd.HttpRequestImpl;
import org.apache.openejb.server.httpd.HttpSession;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class AxisRequest extends HttpRequestImpl {
    private int contentLength;

    private String contentType;

    private ServletInputStream in;

    private Method method;

    private Map<String, String> parameters;

    private Map<String, String> headers;

    private String remoteAddress;

    public AxisRequest(int contentLength, String contentType, ServletInputStream in, Method method, Map<String, String> parameters, URI uri, Map<String, String> headers, String remoteAddress) {
        super(uri);
        this.contentLength = contentLength;
        this.contentType = contentType;
        this.in = in;
        this.method = method;
        this.parameters = parameters;
        this.headers = headers;
        this.remoteAddress = remoteAddress;
    }

    public int getContentLength() {
        return contentLength;
    }

    public String getContentType() {
        return contentType;
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    public ServletInputStream getInputStream() throws IOException {
        return in;
    }

    public String getMethod() {
        return method.name();
    }

    public String getParameter(String name) {
        return parameters.get(name);
    }

    public Map getParameters() {
        return parameters;
    }

    public URI getURI() {
        return getSocketURI();
    }

    public String getRemoteAddr() {
        return remoteAddress;
    }

    public String getContextPath() {
        return "/axis2";
    }

    public HttpSession getSession(boolean create) {
        throw new UnsupportedOperationException();
    }

    public HttpSession getSession() {
        throw new UnsupportedOperationException();
    }
}

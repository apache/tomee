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

import javax.servlet.ServletConfig;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;
import java.util.Map;

public class ThreadLocalContextManager {
    public static final ThreadLocalRequest REQUEST = new ThreadLocalRequest();
    public static final ThreadLocalServletConfig SERVLET_CONFIG = new ThreadLocalServletConfig();
    public static final ThreadLocalServletRequest SERVLET_REQUEST = new ThreadLocalServletRequest();
    public static final ThreadLocalHttpServletRequest HTTP_SERVLET_REQUEST = new ThreadLocalHttpServletRequest();
    public static final ThreadLocalHttpServletResponse HTTP_SERVLET_RESPONSE = new ThreadLocalHttpServletResponse();
    public static final ThreadLocalUriInfo URI_INFO = new ThreadLocalUriInfo();
    public static final ThreadLocalHttpHeaders HTTP_HEADERS = new ThreadLocalHttpHeaders();
    public static final ThreadLocalSecurityContext SECURITY_CONTEXT = new ThreadLocalSecurityContext();
    public static final ThreadLocalContextResolver CONTEXT_RESOLVER = new ThreadLocalContextResolver();
    public static final ThreadLocalProviders PROVIDERS = new ThreadLocalProviders();
    public static final ThreadLocal<Application> APPLICATION = new ThreadLocal<Application>();
    public static final ThreadLocal<Map<String, Object>> OTHERS = new ThreadLocal<Map<String, Object>>();

    public static void reset() {
        REQUEST.remove();
        SERVLET_REQUEST.remove();
        SERVLET_CONFIG.remove();
        HTTP_SERVLET_REQUEST.remove();
        HTTP_SERVLET_RESPONSE.remove();
        URI_INFO.remove();
        HTTP_HEADERS.remove();
        SECURITY_CONTEXT.remove();
        CONTEXT_RESOLVER.remove();
        PROVIDERS.remove();
        APPLICATION.remove();

        final Map<String, Object> map = OTHERS.get();
        if (map != null) {
            map.clear();
        }
        OTHERS.remove();
    }

    public static Object findThreadLocal(final Class<?> type) {
        if (Request.class.equals(type)) {
            return ThreadLocalContextManager.REQUEST;
        } else if (UriInfo.class.equals(type)) {
            return ThreadLocalContextManager.URI_INFO;
        } else if (HttpHeaders.class.equals(type)) {
            return ThreadLocalContextManager.HTTP_HEADERS;
        } else if (SecurityContext.class.equals(type)) {
            return ThreadLocalContextManager.SECURITY_CONTEXT;
        } else if (ContextResolver.class.equals(type)) {
            return ThreadLocalContextManager.CONTEXT_RESOLVER;
        } else if (Providers.class.equals(type)) {
            return ThreadLocalContextManager.PROVIDERS;
        } else if (ServletRequest.class.equals(type)) {
            return ThreadLocalContextManager.SERVLET_REQUEST;
        } else if (HttpServletRequest.class.equals(type)) {
            return ThreadLocalContextManager.HTTP_SERVLET_REQUEST;
        } else if (HttpServletResponse.class.equals(type)) {
            return ThreadLocalContextManager.HTTP_SERVLET_RESPONSE;
        } else if (ServletConfig.class.equals(type)) {
            return ThreadLocalContextManager.SERVLET_CONFIG;
        }
        return null;
    }
}

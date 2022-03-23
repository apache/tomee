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

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Providers;
import java.util.Map;

public class ThreadLocalContextManager {
    public static final ThreadLocalRequest REQUEST = new ThreadLocalRequest();
    public static final ThreadLocalServletConfig SERVLET_CONFIG = new ThreadLocalServletConfig();
    public static final ThreadLocalServletContext SERVLET_CONTEXT = new ThreadLocalServletContext();
    public static final ThreadLocalServletRequest SERVLET_REQUEST = new ThreadLocalServletRequest();
    public static final ThreadLocalHttpServletRequest HTTP_SERVLET_REQUEST = new ThreadLocalHttpServletRequest();
    public static final ThreadLocalHttpServletResponse HTTP_SERVLET_RESPONSE = new ThreadLocalHttpServletResponse();
    public static final ThreadLocalUriInfo URI_INFO = new ThreadLocalUriInfo();
    public static final ThreadLocalHttpHeaders HTTP_HEADERS = new ThreadLocalHttpHeaders();
    public static final ThreadLocalSecurityContext SECURITY_CONTEXT = new ThreadLocalSecurityContext();
    public static final ThreadLocalContextResolver CONTEXT_RESOLVER = new ThreadLocalContextResolver();
    public static final ThreadLocalProviders PROVIDERS = new ThreadLocalProviders();
    public static final ThreadLocalApplication APPLICATION = new ThreadLocalApplication();
    public static final ThreadLocalConfiguration CONFIGURATION = new ThreadLocalConfiguration();
    public static final ThreadLocalResourceInfo RESOURCE_INFO = new ThreadLocalResourceInfo();
    public static final ThreadLocalResourceContext RESOURCE_CONTEXT = new ThreadLocalResourceContext();
    public static final ThreadLocal<Map<String, Object>> OTHERS = new ThreadLocal<Map<String, Object>>();

    public static void reset() {
        REQUEST.remove();
        SERVLET_REQUEST.remove();
        SERVLET_CONFIG.remove();
        SERVLET_CONTEXT.remove();
        HTTP_SERVLET_REQUEST.remove();
        HTTP_SERVLET_RESPONSE.remove();
        URI_INFO.remove();
        HTTP_HEADERS.remove();
        SECURITY_CONTEXT.remove();
        CONTEXT_RESOLVER.remove();
        PROVIDERS.remove();
        APPLICATION.remove();
        CONFIGURATION.remove();
        RESOURCE_INFO.remove();
        RESOURCE_CONTEXT.remove();

        final Map<String, Object> map = OTHERS.get();
        if (map != null) {
            map.clear();
        }
        OTHERS.remove();
    }

    public static Object findThreadLocal(final Class<?> type) {
        if (Request.class.equals(type)) {
            return REQUEST;
        } else if (UriInfo.class.equals(type)) {
            return URI_INFO;
        } else if (HttpHeaders.class.equals(type)) {
            return HTTP_HEADERS;
        } else if (SecurityContext.class.equals(type)) {
            return SECURITY_CONTEXT;
        } else if (ContextResolver.class.equals(type)) {
            return CONTEXT_RESOLVER;
        } else if (Providers.class.equals(type)) {
            return PROVIDERS;
        } else if (ServletRequest.class.equals(type)) {
            return SERVLET_REQUEST;
        } else if (HttpServletRequest.class.equals(type)) {
            return HTTP_SERVLET_REQUEST;
        } else if (HttpServletResponse.class.equals(type)) {
            return HTTP_SERVLET_RESPONSE;
        } else if (ServletConfig.class.equals(type)) {
            return SERVLET_CONFIG;
        } else if (ServletContext.class.equals(type)) {
            return SERVLET_CONTEXT;
        } else if (ResourceInfo.class.equals(type)) {
            return RESOURCE_INFO;
        } else if (ResourceContext.class.equals(type)) {
            return RESOURCE_CONTEXT;
        } else if (Application.class.equals(type)) {
            return APPLICATION;
        } else if (Configuration.class.equals(type)) {
            return CONFIGURATION;
        }
        return null;
    }
}

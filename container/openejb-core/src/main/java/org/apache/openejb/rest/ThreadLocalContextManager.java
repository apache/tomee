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

import javax.ws.rs.core.Application;
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
}

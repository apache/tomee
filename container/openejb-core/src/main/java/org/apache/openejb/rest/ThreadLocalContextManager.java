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

/**
 * @author Romain Manni-Bucau
 */
public class ThreadLocalContextManager {
    public static final ThreadLocalRequest REQUEST = new ThreadLocalRequest();
    public static final ThreadLocalUriInfo URI_INFO = new ThreadLocalUriInfo();
    public static final ThreadLocalHttpHeaders HTTP_HEADERS = new ThreadLocalHttpHeaders();
    public static final ThreadLocalSecurityContext SECURITY_CONTEXT = new ThreadLocalSecurityContext();
    public static final ThreadLocalContextResolver CONTEXT_RESOLVER = new ThreadLocalContextResolver();
    public static final ThreadLocal<Application> APPLICATION = new ThreadLocal<Application>();

    public static void reset() {
        REQUEST.remove();
        URI_INFO.remove();
        HTTP_HEADERS.remove();
        SECURITY_CONTEXT.remove();
        CONTEXT_RESOLVER.remove();
        APPLICATION.remove();
    }
}

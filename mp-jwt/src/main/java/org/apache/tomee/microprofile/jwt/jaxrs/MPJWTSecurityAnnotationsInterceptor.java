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


package org.apache.tomee.microprofile.jwt.jaxrs;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

public class MPJWTSecurityAnnotationsInterceptor implements ContainerRequestFilter {

    private final jakarta.ws.rs.container.ResourceInfo resourceInfo;
    private final ConcurrentMap<Method, Set<String>> rolesAllowed;
    private final Set<Method> denyAll;
    private final Set<Method> permitAll;

    public MPJWTSecurityAnnotationsInterceptor(final jakarta.ws.rs.container.ResourceInfo resourceInfo,
                                               final ConcurrentMap<Method, Set<String>> rolesAllowed,
                                               final Set<Method> denyAll,
                                               final Set<Method> permitAll) {
        this.resourceInfo = resourceInfo;
        this.rolesAllowed = rolesAllowed;
        this.denyAll = denyAll;
        this.permitAll = permitAll;
    }

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {
        if (permitAll.contains(resourceInfo.getResourceMethod())) {
            return;
        }

        if (denyAll.contains(resourceInfo.getResourceMethod())) {
            forbidden(requestContext);
            return;
        }

        final Set<String> roles = rolesAllowed.get(resourceInfo.getResourceMethod());

        if (roles != null && !roles.isEmpty()) {
            final SecurityContext securityContext = requestContext.getSecurityContext();
            boolean hasAtLeasOneValidRole = false;
            for (String role : roles) {
                if (securityContext.isUserInRole(role)) {
                    hasAtLeasOneValidRole = true;
                    break;
                }
            }
            if (!hasAtLeasOneValidRole) {
                forbidden(requestContext);
            }
        }

    }

    private void forbidden(final ContainerRequestContext requestContext) {
        requestContext.abortWith(Response.status(HttpURLConnection.HTTP_FORBIDDEN).build());
    }
}
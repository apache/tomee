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
import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Enforces the security constraint resolved for a single resource class / resource method pair.
 */
public class MPJWTSecurityAnnotationsInterceptor implements ContainerRequestFilter {

    private final Set<String> rolesAllowed;
    private final boolean denyAll;
    private final boolean permitAll;

    /**
     * @param rolesAllowed the allowed roles, or {@code null} if no {@code @RolesAllowed} applies;
     *                     an empty set denies every caller
     * @param denyAll      whether {@code @DenyAll} applies
     * @param permitAll    whether {@code @PermitAll} applies
     */
    public MPJWTSecurityAnnotationsInterceptor(final Set<String> rolesAllowed,
                                               final boolean denyAll,
                                               final boolean permitAll) {
        this.rolesAllowed = rolesAllowed == null ? null : Collections.unmodifiableSet(new LinkedHashSet<>(rolesAllowed));
        this.denyAll = denyAll;
        this.permitAll = permitAll;
    }

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {
        if (permitAll) {
            return;
        }

        if (denyAll) {
            forbidden(requestContext);
            return;
        }

        if (rolesAllowed != null) {
            final SecurityContext securityContext = requestContext.getSecurityContext();
            boolean hasAtLeasOneValidRole = false;
            for (String role : rolesAllowed) {
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
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

import org.apache.openejb.core.security.AbstractSecurityService;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.SecurityService;

import jakarta.ws.rs.core.SecurityContext;
import java.security.Principal;

public class ThreadLocalSecurityContext extends AbstractRestThreadLocalProxy<SecurityContext>
        implements SecurityContext {
    private final String defaultUser;

    protected ThreadLocalSecurityContext() {
        super(SecurityContext.class);
        final SecurityService securityService = service();
        defaultUser = AbstractSecurityService.class.isInstance(securityService) ? AbstractSecurityService.class.cast(securityService).getDefaultUser() : null;
    }

    private static SecurityService service() {
        return SystemInstance.get().getComponent(SecurityService.class);
    }

    public String getAuthenticationScheme() {
        return get().getAuthenticationScheme();
    }

    public Principal getUserPrincipal() {
        final Principal callerPrincipal = service().getCallerPrincipal();
        if (callerPrincipal == null) {
            final SecurityContext securityContext = get();
            if (securityContext != null) {
                return securityContext.getUserPrincipal();
            }
        }
        // JAX-RS doesn't return a default Principal
        return callerPrincipal == null || callerPrincipal.getName().equals(defaultUser) ? null : callerPrincipal;
    }

    public boolean isSecure() {
        return get().isSecure();
    }

    public boolean isUserInRole(final String role) {
        if (service().isCallerInRole(role)) {
            return true;
        }
        final SecurityContext sc = get();
        return sc != null && sc.isUserInRole(role);
    }

}

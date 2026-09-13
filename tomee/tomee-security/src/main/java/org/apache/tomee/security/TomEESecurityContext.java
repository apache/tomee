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
package org.apache.tomee.security;

import org.apache.catalina.connector.Request;
import org.apache.catalina.realm.GenericPrincipal;
import org.apache.openejb.core.security.JaccProvider;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.JavaSecurityManagers;
import org.apache.tomee.catalina.OpenEJBSecurityListener;
import org.apache.tomee.catalina.TomcatSecurityService;
import org.apache.tomee.security.message.TomEEMessageInfo;

import jakarta.annotation.PostConstruct;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.SecurityContext;
import jakarta.security.enterprise.authentication.mechanism.http.AuthenticationParameters;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import static org.apache.tomee.catalina.Contexts.toAppContext;

public class TomEESecurityContext implements SecurityContext {

    private TomcatSecurityService securityService;
    private JaccProvider jaccProvider;

    @PostConstruct
    private void init() {
        final SecurityService securityService = SystemInstance.get().getComponent(SecurityService.class);
        if (securityService instanceof TomcatSecurityService) {
            this.securityService = (TomcatSecurityService) securityService;
        }
        jaccProvider = JaccProvider.get();
    }

    @Override
    public Principal getCallerPrincipal() {
        return securityService.getCallerPrincipal();
    }

    @Override
    public <T extends Principal> Set<T> getPrincipalsByType(final Class<T> pType) {
        return securityService.getPrincipalsByType(pType);
    }

    @Override
    public boolean isCallerInRole(final String role) {
        return securityService.isCallerInRole(role);
    }

    @Override
    public boolean hasAccessToWebResource(final String resource, final String... methods) {
        return jaccProvider != null && jaccProvider.hasAccessToWebResource(resource, methods);
    }

    @Override
    public AuthenticationStatus authenticate(final HttpServletRequest request,
                                             final HttpServletResponse response,
                                             final AuthenticationParameters parameters) {

        // Delegate to HttpServletRequest.authenticate() rather than driving JASPIC directly.
        request.removeAttribute(TomEEMessageInfo.LAST_AUTH_STATUS);

        if (parameters != null) {
            request.setAttribute(TomEEMessageInfo.AUTH_PARAMS, parameters);
        }
        request.setAttribute(TomEEMessageInfo.AUTHENTICATE, Boolean.toString(true));

        try {
            if (request.authenticate(response)) {
                return AuthenticationStatus.SUCCESS;
            }

            return lastAuthenticationStatus(request);

        } catch (final ServletException | IOException e) {
            return AuthenticationStatus.SEND_FAILURE;
        } finally {
            request.removeAttribute(TomEEMessageInfo.AUTH_PARAMS);
            request.removeAttribute(TomEEMessageInfo.AUTHENTICATE);
        }
    }

    private static AuthenticationStatus lastAuthenticationStatus(final HttpServletRequest request) {
        final Object status = request.getAttribute(TomEEMessageInfo.LAST_AUTH_STATUS);
        return status instanceof AuthenticationStatus
                ? (AuthenticationStatus) status
                : AuthenticationStatus.SEND_FAILURE;
    }

    public static void registerContainerAboutLogin(final Principal principal, final Set<String> groups) {

        final SecurityService securityService = SystemInstance.get().getComponent(SecurityService.class);
        if (securityService instanceof TomcatSecurityService tomcatSecurityService) {
            final Request request = OpenEJBSecurityListener.requests.get();
            if (request == null || request.getWrapper() == null) {
                return;
            }

            final GenericPrincipal genericPrincipal =
                    new GenericPrincipal(
                        principal.getName(),
                        groups == null ? Collections.emptyList() : new ArrayList<>(groups),
                        principal);

            // todo should it be done in the enterWebApp?
            JavaSecurityManagers.setContextID(toAppContext(request.getServletContext(), request.getContextPath()));

            tomcatSecurityService.enterWebApp(request.getWrapper().getRealm(),
                                              genericPrincipal,
                                              request.getWrapper().getRunAs());

            if (genericPrincipal.getName() != null) {
                request.setAuthType("JASPIC");
                request.setUserPrincipal(genericPrincipal);
            }
        }
    }

    private String getAppContextId() {
        final Request request = OpenEJBSecurityListener.requests.get();
        if (request == null || request.getServletContext() == null) {
            return null;
        }
        return toAppContext(request.getServletContext(), request.getContextPath());
    }


}

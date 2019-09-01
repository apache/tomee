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

import org.apache.catalina.authenticator.jaspic.CallbackHandlerImpl;
import org.apache.catalina.connector.Request;
import org.apache.catalina.realm.GenericPrincipal;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.SecurityService;
import org.apache.tomee.catalina.OpenEJBSecurityListener;
import org.apache.tomee.catalina.TomcatSecurityService;
import org.apache.tomee.security.message.TomEEMessageInfo;

import javax.annotation.PostConstruct;
import javax.security.auth.Subject;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.config.AuthConfigFactory;
import javax.security.auth.message.config.AuthConfigProvider;
import javax.security.auth.message.config.ServerAuthConfig;
import javax.security.auth.message.config.ServerAuthContext;
import javax.security.enterprise.AuthenticationStatus;
import javax.security.enterprise.SecurityContext;
import javax.security.enterprise.authentication.mechanism.http.AuthenticationParameters;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Set;

import static javax.security.auth.message.AuthStatus.SEND_CONTINUE;
import static javax.security.auth.message.AuthStatus.SEND_FAILURE;
import static javax.security.auth.message.AuthStatus.SUCCESS;

public class TomEESecurityContext implements SecurityContext {
    private TomcatSecurityService securityService;

    @PostConstruct
    private void init() {
        final SecurityService securityService = SystemInstance.get().getComponent(SecurityService.class);
        if (securityService instanceof TomcatSecurityService) {
            this.securityService = (TomcatSecurityService) securityService;
        }
    }

    @Override
    public Principal getCallerPrincipal() {
        return securityService.getCallerPrincipal();
    }

    @Override
    public <T extends Principal> Set<T> getPrincipalsByType(final Class<T> pType) {
        return null;
    }

    @Override
    public boolean isCallerInRole(final String role) {
        return securityService.isCallerInRole(role);
    }

    @Override
    public boolean hasAccessToWebResource(final String resource, final String... methods) {
        return false;
    }

    @Override
    public AuthenticationStatus authenticate(final HttpServletRequest request,
                                             final HttpServletResponse response,
                                             final AuthenticationParameters parameters) {

        try {
            final MessageInfo messageInfo = new TomEEMessageInfo(request, response, true, parameters);
            final ServerAuthContext serverAuthContext = getServerAuthContext(request);
            final AuthStatus authStatus = serverAuthContext.validateRequest(messageInfo, new Subject(), null);

            return mapToAuthenticationStatus(authStatus);

        } catch (final AuthException e) {
            return AuthenticationStatus.SEND_FAILURE;
        }
    }

    private AuthenticationStatus mapToAuthenticationStatus(final AuthStatus authStatus) {
        if (SUCCESS.equals(authStatus)) {
            return AuthenticationStatus.SUCCESS;
        }

        if (SEND_FAILURE.equals(authStatus)) {
            return AuthenticationStatus.SEND_FAILURE;
        }

        if (SEND_CONTINUE.equals(authStatus)) {
            return AuthenticationStatus.SEND_CONTINUE;
        }

        throw new IllegalArgumentException();
    }

    private ServerAuthContext getServerAuthContext(final HttpServletRequest request) throws AuthException {
        final String appContext = request.getServletContext().getVirtualServerName() + " " + request.getContextPath();

        final AuthConfigProvider authConfigProvider =
                AuthConfigFactory.getFactory().getConfigProvider("HttpServlet", appContext, null);
        final ServerAuthConfig serverAuthConfig =
                authConfigProvider.getServerAuthConfig("HttpServlet", appContext, CallbackHandlerImpl.getInstance());

        return serverAuthConfig.getAuthContext(null, null, null);
    }

    public static void registerContainerAboutLogin(final Principal principal, final Set<String> groups) {
        final SecurityService securityService = SystemInstance.get().getComponent(SecurityService.class);
        if (TomcatSecurityService.class.isInstance(securityService)) {
            final TomcatSecurityService tomcatSecurityService = (TomcatSecurityService) securityService;
            final Request request = OpenEJBSecurityListener.requests.get();
            final GenericPrincipal genericPrincipal =
                    new GenericPrincipal(principal.getName(), null, new ArrayList<>(groups), principal);
            tomcatSecurityService.enterWebApp(request.getWrapper().getRealm(),
                                              genericPrincipal,
                                              request.getWrapper().getRunAs());
        }
    }
}

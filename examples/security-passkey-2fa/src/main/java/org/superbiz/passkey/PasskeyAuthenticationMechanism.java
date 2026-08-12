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
package org.superbiz.passkey;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.AuthenticationException;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.authentication.mechanism.http.AutoApplySession;
import jakarta.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Set;


@ApplicationScoped
@AutoApplySession
public class PasskeyAuthenticationMechanism implements HttpAuthenticationMechanism {

    @Inject
    private WebAuthnService webAuthn;

    @Inject
    private UserRepository users;

    @Override
    public AuthenticationStatus validateRequest(final HttpServletRequest request,
                                                final HttpServletResponse response,
                                                final HttpMessageContext httpMessageContext)
            throws AuthenticationException {

        if (!"POST".equalsIgnoreCase(request.getMethod())
                || !request.getRequestURI().endsWith("/api/login/assertion")) {
            return httpMessageContext.doNothing();
        }

        final HttpSession session = request.getSession(false);
        final String firstFactorUser =
                session == null ? null : (String) session.getAttribute(PasskeyLoginServlet.FIRST_FACTOR_USER);
        if (firstFactorUser == null) {
            return httpMessageContext.responseUnauthorized();
        }

        final String assertedUser;
        try {
            assertedUser = webAuthn.finishAssertion(request, readBody(request));
        } catch (final RuntimeException e) {
            return httpMessageContext.responseUnauthorized();
        }

        if (!firstFactorUser.equals(assertedUser)) {
            return httpMessageContext.responseUnauthorized();
        }

        final Set<String> roles = users.roles(assertedUser).orElse(Set.of());
        session.removeAttribute(PasskeyLoginServlet.FIRST_FACTOR_USER);

        return httpMessageContext.notifyContainerAboutLogin(assertedUser, roles);
    }

    private static String readBody(final HttpServletRequest request) throws AuthenticationException {
        try {
            return Http.body(request);
        } catch (final IOException e) {
            throw new AuthenticationException(e.getMessage());
        }
    }
}

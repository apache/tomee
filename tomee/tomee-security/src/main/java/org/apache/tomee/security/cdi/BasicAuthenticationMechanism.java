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
package org.apache.tomee.security.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.AuthenticationException;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.authentication.mechanism.http.BasicAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import jakarta.security.enterprise.authentication.mechanism.http.LoginToContinue;
import jakarta.security.enterprise.credential.BasicAuthenticationCredential;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.IdentityStoreHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.function.Supplier;

import static jakarta.security.enterprise.identitystore.CredentialValidationResult.Status.VALID;
import static jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION;

@ApplicationScoped
public class BasicAuthenticationMechanism implements HttpAuthenticationMechanism {

    @Inject
    private IdentityStoreHandler identityStoreHandler;

    @Inject
    private Supplier<BasicAuthenticationMechanismDefinition> basicAuthenticationMechanismDefinition;


    @Override
    public AuthenticationStatus validateRequest(final HttpServletRequest request,
                                                final HttpServletResponse response,
                                                final HttpMessageContext httpMessageContext)
            throws AuthenticationException {

        try {
            final BasicAuthenticationCredential credential = parseAuthenticationHeader(request.getHeader(AUTHORIZATION));
            final CredentialValidationResult result = identityStoreHandler.validate(credential);

            if (result.getStatus().equals(VALID)) {
                return httpMessageContext.notifyContainerAboutLogin(result);
            }

        } catch (final IllegalArgumentException | IllegalStateException e) {
            // Something was sent in the header was not valid. Fallthrough to the authenticate challenge again.
        }

        if (httpMessageContext.isProtected()) {

            final String realmName = basicAuthenticationMechanismDefinition.get().realmName();
            if (realmName.isEmpty()) {
                response.setHeader("WWW-Authenticate", "Basic");
            } else {
                response.setHeader("WWW-Authenticate", String.format("Basic realm=\"%s\"", realmName));

            }

            return httpMessageContext.responseUnauthorized();
        }

        return httpMessageContext.doNothing();
    }

    private BasicAuthenticationCredential parseAuthenticationHeader(final String authenticationHeader) {
        return Optional.ofNullable(authenticationHeader)
                       .filter(header -> !header.isEmpty())
                       .filter(header -> header.startsWith("Basic "))
                       .map(header -> header.substring(6))
                       .map(BasicAuthenticationCredential::new)
                       .orElseGet(() -> new BasicAuthenticationCredential(""));
    }
}

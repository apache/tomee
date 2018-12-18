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

import org.apache.tomee.security.identitystore.TomEEIdentityStoreHandler;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.security.enterprise.AuthenticationException;
import javax.security.enterprise.AuthenticationStatus;
import javax.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import javax.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import javax.security.enterprise.credential.BasicAuthenticationCredential;
import javax.security.enterprise.identitystore.CredentialValidationResult;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;

import static javax.security.enterprise.identitystore.CredentialValidationResult.Status.VALID;

@ApplicationScoped
public class BasicAuthenticationMechanism implements HttpAuthenticationMechanism {
    @Inject
    private TomEEIdentityStoreHandler identityStoreHandler;

    @Override
    public AuthenticationStatus validateRequest(final HttpServletRequest request,
                                                final HttpServletResponse response,
                                                final HttpMessageContext httpMessageContext)
            throws AuthenticationException {

        if (!httpMessageContext.isProtected()) {
            return httpMessageContext.doNothing();
        }

        try {
            final CredentialValidationResult result =
                    identityStoreHandler.validate(new BasicAuthenticationCredential(HttpHeaders.AUTHORIZATION));

            if (result.getStatus().equals(VALID)) {
                return httpMessageContext.notifyContainerAboutLogin(result);
            }

        } catch (final IllegalArgumentException | IllegalStateException e) {
            // Something was sent in the header was not valid. Fallthrough to the authenticate challenge again.
        }

        response.setHeader("WWW-Authenticate", "Basic");
        return httpMessageContext.responseUnauthorized();
    }

    @Override
    public AuthenticationStatus secureResponse(final HttpServletRequest request,
                                               final HttpServletResponse response,
                                               final HttpMessageContext httpMessageContext)
            throws AuthenticationException {
        return null;
    }

    @Override
    public void cleanSubject(final HttpServletRequest request,
                             final HttpServletResponse response,
                             final HttpMessageContext httpMessageContext) {

    }
}

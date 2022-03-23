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

import org.apache.tomee.security.http.LoginToContinueMechanism;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.AuthenticationException;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.authentication.mechanism.http.AutoApplySession;
import jakarta.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import jakarta.security.enterprise.authentication.mechanism.http.LoginToContinue;
import jakarta.security.enterprise.identitystore.IdentityStoreHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.function.Supplier;

@ApplicationScoped
@AutoApplySession
@LoginToContinue
public class CustomFormAuthenticationMechanism implements HttpAuthenticationMechanism, LoginToContinueMechanism {
    @Inject
    private Supplier<LoginToContinue> loginToContinue;
    @Inject
    private IdentityStoreHandler identityStoreHandler;

    @Override
    public AuthenticationStatus validateRequest(final HttpServletRequest request, final HttpServletResponse response,
                                                final HttpMessageContext httpMessageContext)
            throws AuthenticationException {

        if (validateCredentials(httpMessageContext)) {
            return httpMessageContext.notifyContainerAboutLogin(
                    identityStoreHandler.validate(httpMessageContext.getAuthParameters().getCredential()));
        }

        return httpMessageContext.doNothing();
    }

    public LoginToContinue getLoginToContinue() {
        return loginToContinue.get();
    }

    private boolean validateCredentials(final HttpMessageContext httpMessageContext) {
        return httpMessageContext.getAuthParameters().getCredential() != null;
    }
}

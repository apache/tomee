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
import jakarta.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import jakarta.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanismHandler;
import jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ApplicationScoped
public class DefaultAuthenticationMechanismHandler implements HttpAuthenticationMechanismHandler {
    @Inject
    private TomEESecurityServletAuthenticationMechanismMapper authenticationMechanismMapper;

    @Override
    public AuthenticationStatus validateRequest(final HttpServletRequest request,
                                                final HttpServletResponse response,
                                                final HttpMessageContext httpMessageContext) throws AuthenticationException {
        final HttpAuthenticationMechanism authenticationMechanism =
                authenticationMechanismMapper.getCurrentAuthenticationMechanism(httpMessageContext);
        return authenticationMechanism.validateRequest(request, response, httpMessageContext);
    }

    @Override
    public AuthenticationStatus secureResponse(final HttpServletRequest request,
                                               final HttpServletResponse response,
                                               final HttpMessageContext httpMessageContext) throws AuthenticationException {
        final HttpAuthenticationMechanism authenticationMechanism =
                authenticationMechanismMapper.getCurrentAuthenticationMechanism(httpMessageContext);
        return authenticationMechanism.secureResponse(request, response, httpMessageContext);
    }

    @Override
    public void cleanSubject(final HttpServletRequest request,
                             final HttpServletResponse response,
                             final HttpMessageContext httpMessageContext) {
        final HttpAuthenticationMechanism authenticationMechanism =
                authenticationMechanismMapper.getCurrentAuthenticationMechanism(httpMessageContext);
        authenticationMechanism.cleanSubject(request, response, httpMessageContext);
    }
}

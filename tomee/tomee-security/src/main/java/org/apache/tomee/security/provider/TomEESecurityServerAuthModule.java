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
package org.apache.tomee.security.provider;

import org.apache.tomee.security.cdi.TomEESecurityServletAuthenticationMechanismMapper;

import javax.enterprise.inject.spi.CDI;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.MessagePolicy;
import javax.security.auth.message.module.ServerAuthModule;
import javax.security.enterprise.AuthenticationException;
import javax.security.enterprise.AuthenticationStatus;
import javax.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import javax.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import java.util.Map;

import static org.apache.tomee.security.http.TomEEHttpMessageContext.httpMessageContext;

public class TomEESecurityServerAuthModule implements ServerAuthModule {
    private CallbackHandler handler;

    @Override
    public Class[] getSupportedMessageTypes() {
        return new Class[0];
    }

    @Override
    public void initialize(final MessagePolicy requestPolicy, final MessagePolicy responsePolicy,
                           final CallbackHandler handler,
                           final Map options) throws AuthException {
        this.handler = handler;
    }

    @Override
    public void cleanSubject(final MessageInfo messageInfo, final Subject subject) throws AuthException {

    }

    @Override
    public AuthStatus secureResponse(final MessageInfo messageInfo, final Subject serviceSubject) throws AuthException {
        return AuthStatus.SUCCESS;
    }

    @Override
    public AuthStatus validateRequest(final MessageInfo messageInfo, final Subject clientSubject,
                                      final Subject serviceSubject)
            throws AuthException {

        final HttpMessageContext httpMessageContext =
                httpMessageContext(handler, messageInfo, clientSubject, serviceSubject);

        final HttpAuthenticationMechanism authenticationMechanism =
                CDI.current()
                   .select(TomEESecurityServletAuthenticationMechanismMapper.class)
                   .get()
                   .getCurrentAuthenticationMechanism(httpMessageContext);

        final AuthenticationStatus authenticationStatus;
        try {
            authenticationStatus =
                    authenticationMechanism.validateRequest(httpMessageContext.getRequest(),
                                                            httpMessageContext.getResponse(),
                                                            httpMessageContext);


        } catch (final AuthenticationException e) {
            final AuthException authException = new AuthException(e.getMessage());
            authException.initCause(e);
            throw authException;
        }

        return mapToAuthStatus(authenticationStatus);
    }

    private AuthStatus mapToAuthStatus(final AuthenticationStatus authenticationStatus) {
        switch (authenticationStatus) {
            case SUCCESS:
            case NOT_DONE:
                return AuthStatus.SUCCESS;
            case SEND_FAILURE:
                return AuthStatus.SEND_FAILURE;
            case SEND_CONTINUE:
                return AuthStatus.SEND_CONTINUE;
            default:
                throw new IllegalArgumentException();
        }
    }
}

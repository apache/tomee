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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.security.enterprise.AuthenticationException;
import javax.security.enterprise.AuthenticationStatus;
import javax.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import javax.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import javax.security.enterprise.authentication.mechanism.http.LoginToContinue;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.function.Supplier;

@ApplicationScoped
@LoginToContinue
public class FormAuthenticationMechanism implements HttpAuthenticationMechanism, LoginToContinueMechanism {
    @Inject
    private Supplier<LoginToContinue> loginToContinue;

    @Override
    public AuthenticationStatus validateRequest(final HttpServletRequest request, final HttpServletResponse response,
                                                final HttpMessageContext httpMessageContext)
            throws AuthenticationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public AuthenticationStatus secureResponse(final HttpServletRequest request, final HttpServletResponse response,
                                               final HttpMessageContext httpMessageContext)
            throws AuthenticationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void cleanSubject(final HttpServletRequest request, final HttpServletResponse response,
                             final HttpMessageContext httpMessageContext) {
        throw new UnsupportedOperationException();
    }

    public LoginToContinue getLoginToContinue() {
        return loginToContinue.get();
    }
}

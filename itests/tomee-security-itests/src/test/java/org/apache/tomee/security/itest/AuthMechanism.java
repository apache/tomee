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


package org.apache.tomee.security.itest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.AuthenticationException;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.authentication.mechanism.http.AutoApplySession;
import jakarta.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import jakarta.security.enterprise.authentication.mechanism.http.LoginToContinue;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.HttpMethod;

import static java.util.Collections.singleton;

@ApplicationScoped
@AutoApplySession
@LoginToContinue(
    loginPage = "/login-app",
    errorPage = "/login-error-app"
)
public class AuthMechanism implements HttpAuthenticationMechanism {

    @Override
    public AuthenticationStatus validateRequest(
        HttpServletRequest request,
        HttpServletResponse response,
        HttpMessageContext httpMessageContext) throws
                                               AuthenticationException {
        final String token = request.getParameter("token");

        if (validateForm(httpMessageContext.getRequest(), token)) {

            // validating the token would go here. We obviously send only a random string so
            // not doing anything
            if (!"1234ABCD".equals(token)) {
                return httpMessageContext.responseUnauthorized();
            }

            return httpMessageContext.notifyContainerAboutLogin("jwt-token", singleton("tomcat"));
        }

        return httpMessageContext.doNothing();
    }

    private boolean validateForm(final HttpServletRequest request, final String token) {
        return request.getMethod().equals(HttpMethod.POST) &&
               request.getRequestURI().endsWith("/login-jwt") &&
               token != null && !token.isEmpty();
    }
}
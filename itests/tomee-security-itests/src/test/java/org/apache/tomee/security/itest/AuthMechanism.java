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

import javax.enterprise.context.ApplicationScoped;
import javax.security.enterprise.AuthenticationException;
import javax.security.enterprise.AuthenticationStatus;
import javax.security.enterprise.authentication.mechanism.http.AutoApplySession;
import javax.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import javax.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import javax.security.enterprise.authentication.mechanism.http.LoginToContinue;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;

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
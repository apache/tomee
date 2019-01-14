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
import org.apache.tomee.security.http.SavedAuthentication;
import org.apache.tomee.security.http.SavedHttpServletRequest;
import org.apache.tomee.security.http.SavedRequest;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.security.enterprise.AuthenticationStatus;
import javax.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import javax.security.enterprise.authentication.mechanism.http.LoginToContinue;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

import static javax.interceptor.Interceptor.Priority.PLATFORM_BEFORE;
import static javax.security.enterprise.AuthenticationStatus.SEND_FAILURE;
import static javax.security.enterprise.AuthenticationStatus.SUCCESS;
import static org.apache.tomee.security.http.LoginToContinueMechanism.clearRequestAndAuthentication;
import static org.apache.tomee.security.http.LoginToContinueMechanism.getAuthentication;
import static org.apache.tomee.security.http.LoginToContinueMechanism.getRequest;
import static org.apache.tomee.security.http.LoginToContinueMechanism.hasAuthentication;
import static org.apache.tomee.security.http.LoginToContinueMechanism.hasRequest;
import static org.apache.tomee.security.http.LoginToContinueMechanism.matchRequest;
import static org.apache.tomee.security.http.LoginToContinueMechanism.saveAuthentication;
import static org.apache.tomee.security.http.LoginToContinueMechanism.saveRequest;

@LoginToContinue
@Interceptor
@Priority(PLATFORM_BEFORE + 220)
public class LoginToContinueInterceptor {
    @AroundInvoke
    public Object intercept(final InvocationContext invocationContext) throws Exception {
        if (invocationContext.getMethod().getName().equals("validateRequest") &&
            Arrays.equals(invocationContext.getMethod().getParameterTypes(), new Class<?>[]{
                    HttpServletRequest.class,
                    HttpServletResponse.class,
                    HttpMessageContext.class
            })) {
            return validateRequest(invocationContext);
        }

        return invocationContext.proceed();
    }

    private AuthenticationStatus validateRequest(final InvocationContext invocationContext)
            throws Exception {

        final HttpMessageContext httpMessageContext = (HttpMessageContext) invocationContext.getParameters()[2];
        clearStaleState(httpMessageContext);

        if (httpMessageContext.getAuthParameters().isNewAuthentication()) {
            return processCallerInitiatedAuthentication(httpMessageContext);
        } else {
            return processContainerInitiatedAuthentication(invocationContext, httpMessageContext);
        }
    }

    private void clearStaleState(final HttpMessageContext httpMessageContext) {

    }

    private AuthenticationStatus processCallerInitiatedAuthentication(
            final HttpMessageContext httpMessageContext) {
        return null;
    }

    private AuthenticationStatus processContainerInitiatedAuthentication(
            final InvocationContext invocationContext,
            final HttpMessageContext httpMessageContext)
            throws Exception {

        if (isOnInitialProtectedURL(httpMessageContext)) {
            saveRequest(httpMessageContext.getRequest());

            final LoginToContinue loginToContinue = getLoginToContinue(invocationContext);
            if (loginToContinue.useForwardToLogin()) {
                return httpMessageContext.forward(loginToContinue.loginPage());
            } else {
                return httpMessageContext.redirect(loginToContinue.loginPage());
            }
        }

        if (isOnLoginPostback(httpMessageContext)) {
            final AuthenticationStatus authenticationStatus = (AuthenticationStatus) invocationContext.proceed();

            if (authenticationStatus.equals(SUCCESS)) {
                if (httpMessageContext.getCallerPrincipal() == null) {
                    return SUCCESS;
                }

                if (matchRequest(httpMessageContext.getRequest())) {
                    return SUCCESS;
                }

                saveAuthentication(httpMessageContext.getRequest(),
                                   httpMessageContext.getCallerPrincipal(),
                                   httpMessageContext.getGroups());

                final SavedRequest savedRequest = getRequest(httpMessageContext.getRequest());
                return httpMessageContext.redirect(savedRequest.getRequestURLWithQueryString());
            }

            if (authenticationStatus.equals(SEND_FAILURE)) {
                final LoginToContinue loginToContinue = getLoginToContinue(invocationContext);
                if (!loginToContinue.errorPage().isEmpty()) {
                    return httpMessageContext.forward(loginToContinue.errorPage());
                }

                return authenticationStatus;
            }
        }

        if (isOnOriginalURLAfterAuthenticate(httpMessageContext)) {
            final SavedRequest savedRequest = getRequest(httpMessageContext.getRequest());
            final SavedAuthentication savedAuthentication = getAuthentication(httpMessageContext.getRequest());

            clearRequestAndAuthentication(httpMessageContext.getRequest());

            final SavedHttpServletRequest savedHttpServletRequest =
                    new SavedHttpServletRequest(httpMessageContext.getRequest(), savedRequest);

            return httpMessageContext.withRequest(savedHttpServletRequest)
                                     .notifyContainerAboutLogin(savedAuthentication.getPrincipal(),
                                                                savedAuthentication.getGroups());
        }

        return (AuthenticationStatus) invocationContext.proceed();
    }

    private boolean isOnInitialProtectedURL(final HttpMessageContext httpMessageContext) {
        return httpMessageContext.isProtected() && !hasRequest(httpMessageContext.getRequest());
    }

    private boolean isOnLoginPostback(final HttpMessageContext httpMessageContext) {
        return hasRequest(httpMessageContext.getRequest()) && !hasAuthentication(httpMessageContext.getRequest());
    }

    private boolean isOnOriginalURLAfterAuthenticate(final HttpMessageContext httpMessageContext) {
        return hasRequest(httpMessageContext.getRequest()) && hasAuthentication(httpMessageContext.getRequest());
    }

    private LoginToContinue getLoginToContinue(final InvocationContext invocationContext) {
        if (invocationContext.getTarget() instanceof LoginToContinueMechanism) {
            return ((LoginToContinueMechanism) invocationContext.getTarget()).getLoginToContinue();
        }

        throw new IllegalArgumentException();
    }
}

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

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.security.auth.message.AuthException;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import jakarta.security.enterprise.authentication.mechanism.http.LoginToContinue;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;

import static jakarta.interceptor.Interceptor.Priority.PLATFORM_BEFORE;
import static jakarta.security.enterprise.AuthenticationStatus.SEND_FAILURE;
import static jakarta.security.enterprise.AuthenticationStatus.SUCCESS;
import static org.apache.tomee.security.http.LoginToContinueMechanism.AUTHENTICATION;
import static org.apache.tomee.security.http.LoginToContinueMechanism.CALLER_AUTHENICATION;
import static org.apache.tomee.security.http.LoginToContinueMechanism.ORIGINAL_REQUEST;
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
            return processCallerInitiatedAuthentication(invocationContext, httpMessageContext);
        } else {
            return processContainerInitiatedAuthentication(invocationContext, httpMessageContext);
        }
    }

    private void clearStaleState(final HttpMessageContext httpMessageContext) {

        if (httpMessageContext.isProtected() &&
            !httpMessageContext.isAuthenticationRequest() &&
            hasRequest(httpMessageContext.getRequest()) &&
            !hasAuthentication(httpMessageContext.getRequest()) &&
            !httpMessageContext.getRequest().getRequestURI().endsWith("j_security_check")) {

            httpMessageContext.getRequest().getSession().removeAttribute(ORIGINAL_REQUEST);
            httpMessageContext.getRequest().getSession().removeAttribute(CALLER_AUTHENICATION);
        }

        if (httpMessageContext.getAuthParameters().isNewAuthentication()) {
            httpMessageContext.getRequest().getSession().setAttribute(CALLER_AUTHENICATION, true);
            httpMessageContext.getRequest().getSession().removeAttribute(ORIGINAL_REQUEST);
            httpMessageContext.getRequest().getSession().removeAttribute(AUTHENTICATION);
        }

    }

    private AuthenticationStatus processCallerInitiatedAuthentication(
        final InvocationContext invocationContext,
        final HttpMessageContext httpMessageContext) throws Exception {

        AuthenticationStatus authstatus;

        try {
            authstatus = (AuthenticationStatus) invocationContext.proceed();

        } catch (AuthException e) {
            authstatus = AuthenticationStatus.SEND_FAILURE;
        }

        if (authstatus == AuthenticationStatus.SUCCESS) {

            if (httpMessageContext.getCallerPrincipal() == null) {
                return AuthenticationStatus.SUCCESS;
            }

        }

        return authstatus;
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
                return httpMessageContext.redirect(toAbsoluteUrl(httpMessageContext.getRequest(), loginToContinue.loginPage()));
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

            } else if (authenticationStatus.equals(SEND_FAILURE)) {
                final LoginToContinue loginToContinue = getLoginToContinue(invocationContext);

                if (!loginToContinue.errorPage().isEmpty()) {
                    return httpMessageContext.redirect(toAbsoluteUrl(httpMessageContext.getRequest(), loginToContinue.errorPage()));
                }

                return authenticationStatus;

            } else {
                return authenticationStatus; // SEND_CONTINUE
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

    // when using redirect (client) as opposed to forward (server), we need the absolute URL
    // take the full URL, remove the full URI and then add the context path so the page is relative to base context URL
    private String toAbsoluteUrl(final HttpServletRequest request, final String page) {
        final String url = request.getRequestURL().toString();
        final String baseContextUrl = url.substring(0, url.length() - request.getRequestURI().length()) + request.getContextPath();

        // when context path is / and page is /login, we may end up with double /
        return baseContextUrl.endsWith("/") && page.startsWith("/")
            ? baseContextUrl.substring(0, baseContextUrl.length() - 2) + page
               : baseContextUrl + page;
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

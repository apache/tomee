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

import javax.annotation.Priority;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Intercepted;
import javax.enterprise.inject.spi.Bean;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.security.enterprise.AuthenticationStatus;
import javax.security.enterprise.CallerPrincipal;
import javax.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import javax.security.enterprise.authentication.mechanism.http.RememberMe;
import javax.security.enterprise.credential.RememberMeCredential;
import javax.security.enterprise.identitystore.CredentialValidationResult;
import javax.security.enterprise.identitystore.RememberMeIdentityStore;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import static javax.interceptor.Interceptor.Priority.PLATFORM_BEFORE;
import static javax.security.enterprise.AuthenticationStatus.SUCCESS;
import static javax.security.enterprise.identitystore.CredentialValidationResult.Status.VALID;

@RememberMe
@Interceptor
@Priority(PLATFORM_BEFORE + 210)
public class RememberMeInterceptor {
    @Inject
    @Intercepted
    private Bean<?> httpMechanismBean;

    @Inject
    private Instance<RememberMeIdentityStore> rememberMeIdentityStore;

    @AroundInvoke
    public Object intercept(final InvocationContext invocationContext) throws Exception {
        if (invocationContext.getMethod().getName().equals("validateRequest") &&
            Arrays.equals(invocationContext.getMethod().getParameterTypes(), new Class<?>[]{
                    HttpServletRequest.class,
                    HttpServletResponse.class,
                    HttpMessageContext.class
            })) {

            if (rememberMeIdentityStore.isUnsatisfied()) {
                throw new IllegalStateException("RememberMe annotated AuthenticationMechanism  " +
                                                httpMechanismBean.getBeanClass() +
                                                " required an implementation of RememberMeIndentityStore");
            }

            if (rememberMeIdentityStore.isAmbiguous()) {
                throw new IllegalStateException(
                        "Multiple implementations of RememberMeIndentityStore found. Only one should be supplied.");
            }

            return validateRequest(invocationContext);
        }

        if (invocationContext.getMethod().getName().equals("cleanSubject") &&
            Arrays.equals(invocationContext.getMethod().getParameterTypes(), new Class<?>[]{
                    HttpServletRequest.class,
                    HttpServletResponse.class,
                    HttpMessageContext.class
            })) {
            cleanSubject(invocationContext);
        }

        return invocationContext.proceed();
    }

    private AuthenticationStatus validateRequest(final InvocationContext invocationContext) throws Exception {
        final HttpMessageContext httpMessageContext = (HttpMessageContext) invocationContext.getParameters()[2];

        final RememberMe rememberMe = getRememberMe();
        final Optional<Cookie> cookie = getCookie(httpMessageContext.getRequest(), rememberMe.cookieName());

        if (cookie.isPresent()) {
            final RememberMeCredential rememberMeCredential = new RememberMeCredential(cookie.get().getValue());
            final CredentialValidationResult validate = rememberMeIdentityStore.get().validate(rememberMeCredential);

            if (VALID.equals(validate.getStatus())) {
                return httpMessageContext.notifyContainerAboutLogin(validate);
            } else {
                cookie.get().setMaxAge(0);
                httpMessageContext.getResponse().addCookie(cookie.get());
            }
        }

        final AuthenticationStatus status = (AuthenticationStatus) invocationContext.proceed();

        if (SUCCESS.equals(status) && rememberMe.isRememberMe()) {
            final CallerPrincipal principal = new CallerPrincipal(httpMessageContext.getCallerPrincipal().getName());
            final Set<String> groups = httpMessageContext.getGroups();
            final String loginToken = rememberMeIdentityStore.get().generateLoginToken(principal, groups);

            final Cookie rememberMeCookie = new Cookie(rememberMe.cookieName(), loginToken);
            rememberMeCookie.setMaxAge(rememberMe.cookieMaxAgeSeconds());
            rememberMeCookie.setHttpOnly(rememberMe.cookieHttpOnly());
            rememberMeCookie.setSecure(rememberMe.cookieSecureOnly());
            httpMessageContext.getResponse().addCookie(rememberMeCookie);
        }

        return status;
    }

    private void cleanSubject(final InvocationContext invocationContext) throws Exception {
        final HttpMessageContext httpMessageContext = (HttpMessageContext) invocationContext.getParameters()[2];

        final RememberMe rememberMe = getRememberMe();
        getCookie(httpMessageContext.getRequest(), rememberMe.cookieName())
                .ifPresent(cookie -> {
                    rememberMeIdentityStore.get().removeLoginToken(cookie.getValue());

                    cookie.setMaxAge(0);
                    httpMessageContext.getResponse().addCookie(cookie);
                });

        invocationContext.proceed();
    }

    private Optional<Cookie> getCookie(final HttpServletRequest request, final String name) {
        return Arrays.stream(request.getCookies())
                     .filter(c -> c.getName().equals(name))
                     .findFirst();
    }

    private RememberMe getRememberMe() {
        return Optional.ofNullable(httpMechanismBean.getBeanClass().getAnnotation(RememberMe.class))
                       .orElseThrow(IllegalStateException::new);
    }
}

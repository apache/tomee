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

import org.apache.tomee.security.TomEEELInvocationHandler;

import jakarta.annotation.Priority;
import jakarta.el.ELProcessor;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Intercepted;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.CallerPrincipal;
import jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import jakarta.security.enterprise.authentication.mechanism.http.RememberMe;
import jakarta.security.enterprise.credential.RememberMeCredential;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.RememberMeIdentityStore;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import static jakarta.interceptor.Interceptor.Priority.PLATFORM_BEFORE;
import static jakarta.security.enterprise.AuthenticationStatus.SUCCESS;
import static jakarta.security.enterprise.identitystore.CredentialValidationResult.Status.VALID;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@RememberMe
@Interceptor
@Priority(PLATFORM_BEFORE + 210)
public class RememberMeInterceptor {
    @Inject
    @Intercepted
    private Bean<?> httpMechanismBean;

    @Inject
    private Instance<RememberMeIdentityStore> rememberMeIdentityStore;

    @Inject
    private BeanManager beanManager;

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
                                                " required an implementation of RememberMeIdentityStore");
            }

            if (rememberMeIdentityStore.isAmbiguous()) {
                throw new IllegalStateException(
                    "Multiple implementations of RememberMeIdentityStore found. Only one should be supplied.");
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

        final RememberMe rememberMe =
            TomEEELInvocationHandler.of(RememberMe.class, getRememberMe(), getElProcessor(invocationContext, httpMessageContext));
        final Optional<Cookie> cookie = getCookie(httpMessageContext.getRequest(), rememberMe.cookieName());

        if (cookie.isPresent() && !isEmpty(cookie.get().getValue())) {
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

        if (SUCCESS.equals(status) && httpMessageContext.getCallerPrincipal() != null) {

            if (rememberMe.isRememberMe()) {
                final CallerPrincipal principal =
                    new CallerPrincipal(httpMessageContext.getCallerPrincipal().getName());
                final Set<String> groups = httpMessageContext.getGroups();
                final String loginToken = rememberMeIdentityStore.get().generateLoginToken(principal, groups);

                final Cookie rememberMeCookie = new Cookie(rememberMe.cookieName(), loginToken);
                rememberMeCookie.setPath(isEmpty(httpMessageContext.getRequest().getContextPath()) ?
                                         "/" :
                                         httpMessageContext.getRequest().getContextPath());
                rememberMeCookie.setMaxAge(rememberMe.cookieMaxAgeSeconds());
                rememberMeCookie.setHttpOnly(rememberMe.cookieHttpOnly());
                rememberMeCookie.setSecure(rememberMe.cookieSecureOnly());
                httpMessageContext.getResponse().addCookie(rememberMeCookie);
            }
        }

        return status;
    }

    private void cleanSubject(final InvocationContext invocationContext) throws Exception {
        final HttpMessageContext httpMessageContext = (HttpMessageContext) invocationContext.getParameters()[2];

        final RememberMe rememberMe =
            TomEEELInvocationHandler.of(RememberMe.class, getRememberMe(), getElProcessor(invocationContext, httpMessageContext));
        final Optional<Cookie> cookie = getCookie(httpMessageContext.getRequest(), rememberMe.cookieName());

        if (cookie.isPresent() && !isEmpty(cookie.get().getValue())) {

            // remove the cookie
            cookie.get().setValue(null);
            cookie.get().setMaxAge(0);
            cookie.get()
                  .setPath(isEmpty(httpMessageContext.getRequest().getContextPath()) ?
                           "/" :
                           httpMessageContext.getRequest().getContextPath());
            httpMessageContext.getResponse().addCookie(cookie.get());

            // remove the token from the store
            rememberMeIdentityStore.get().removeLoginToken(cookie.get().getValue());
        }

        invocationContext.proceed();
    }

    private Optional<Cookie> getCookie(final HttpServletRequest request, final String name) {
        final Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
                     .filter(c -> c.getName().equals(name))
                     .findFirst();
    }

    private RememberMe getRememberMe() {
        return Optional.ofNullable(httpMechanismBean.getBeanClass().getAnnotation(RememberMe.class))
                       .orElseThrow(IllegalStateException::new);
    }

    private ELProcessor getElProcessor(InvocationContext invocationContext, HttpMessageContext httpMessageContext) {
        ELProcessor elProcessor = new ELProcessor();

        elProcessor.getELManager().addELResolver(beanManager.getELResolver());
        elProcessor.defineBean("self", invocationContext.getTarget());
        elProcessor.defineBean("this", invocationContext.getTarget());
        elProcessor.defineBean("httpMessageContext", httpMessageContext);

        return elProcessor;
    }

}

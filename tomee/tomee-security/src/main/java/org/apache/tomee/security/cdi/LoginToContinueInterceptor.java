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
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.security.enterprise.AuthenticationException;
import javax.security.enterprise.AuthenticationStatus;
import javax.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import javax.security.enterprise.authentication.mechanism.http.LoginToContinue;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

import static javax.interceptor.Interceptor.Priority.PLATFORM_BEFORE;

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
            return validateRequest((HttpMessageContext) invocationContext.getParameters()[2]);
        }

        return invocationContext.proceed();
    }

    private AuthenticationStatus validateRequest(final HttpMessageContext httpMessageContext)
            throws AuthenticationException {

        clearStaleState(httpMessageContext);

        if (httpMessageContext.getAuthParameters().isNewAuthentication()) {
            return processCallerInitiatedAuthentication(httpMessageContext);
        } else {
            return processContainerInitiatedAuthentication(httpMessageContext);
        }
    }

    private void clearStaleState(final HttpMessageContext httpMessageContext) {

    }

    private AuthenticationStatus processCallerInitiatedAuthentication(final HttpMessageContext httpMessageContext) {
        return null;
    }

    private AuthenticationStatus processContainerInitiatedAuthentication(final HttpMessageContext httpMessageContext) {

        if (isOnInitialProtectedURL(httpMessageContext)) {
            return null;
        }

        if (isOnOnLoginPostback(httpMessageContext)) {
            return null;
        }

        if (isOnOriginalURLAfterAuthenticate(httpMessageContext)) {
            return null;
        }

        return null;
    }

    private boolean isOnInitialProtectedURL(final HttpMessageContext httpMessageContext) {
        return false;
    }

    private boolean isOnOnLoginPostback(final HttpMessageContext httpMessageContext) {
        return false;
    }

    private boolean isOnOriginalURLAfterAuthenticate(final HttpMessageContext httpMessageContext) {
        return false;
    }
}

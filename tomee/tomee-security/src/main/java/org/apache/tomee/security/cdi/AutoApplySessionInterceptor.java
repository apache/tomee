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

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import javax.security.auth.callback.Callback;
import jakarta.security.auth.message.callback.CallerPrincipalCallback;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.authentication.mechanism.http.AutoApplySession;
import jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.Arrays;

import static jakarta.interceptor.Interceptor.Priority.PLATFORM_BEFORE;

@AutoApplySession
@Interceptor
@Priority(PLATFORM_BEFORE + 200)
public class AutoApplySessionInterceptor {
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

        final Principal principal = httpMessageContext.getRequest().getUserPrincipal();
        if (principal == null) {
            final Object authenticationStatus = invocationContext.proceed();

            if (AuthenticationStatus.SUCCESS.equals(authenticationStatus)) {
                httpMessageContext.getMessageInfo().getMap().put("jakarta.servlet.http.registerSession", "true");
            }

            return (AuthenticationStatus) authenticationStatus;
        } else {
            final CallerPrincipalCallback callerPrincipalCallback =
                    new CallerPrincipalCallback(httpMessageContext.getClientSubject(), principal);

            httpMessageContext.getHandler().handle(new Callback[] {callerPrincipalCallback});

            return AuthenticationStatus.SUCCESS;
        }
    }
}

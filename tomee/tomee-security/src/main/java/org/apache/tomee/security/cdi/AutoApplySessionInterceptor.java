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
import javax.security.auth.callback.Callback;
import javax.security.auth.message.callback.CallerPrincipalCallback;
import javax.security.enterprise.AuthenticationStatus;
import javax.security.enterprise.authentication.mechanism.http.AutoApplySession;
import javax.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.Arrays;

import static javax.interceptor.Interceptor.Priority.PLATFORM_BEFORE;

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
                httpMessageContext.getMessageInfo().getMap().put("javax.servlet.http.registerSession", "true");
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

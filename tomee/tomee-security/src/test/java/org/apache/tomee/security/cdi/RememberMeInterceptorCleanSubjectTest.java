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

import jakarta.el.ELResolver;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.interceptor.InvocationContext;
import jakarta.security.enterprise.AuthenticationException;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import jakarta.security.enterprise.authentication.mechanism.http.RememberMe;
import jakarta.security.enterprise.identitystore.RememberMeIdentityStore;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RememberMeInterceptorCleanSubjectTest {

    @RememberMe
    public static class RememberMeMechanism implements HttpAuthenticationMechanism {
        @Override
        public AuthenticationStatus validateRequest(final HttpServletRequest request,
                                                    final HttpServletResponse response,
                                                    final HttpMessageContext httpMessageContext)
                throws AuthenticationException {
            return AuthenticationStatus.SUCCESS;
        }
    }

    @Test
    public void cleanSubjectRevokesTheOriginalLoginToken() throws Exception {
        final RememberMeInterceptor interceptor = new RememberMeInterceptor();

        final Bean<?> bean = mock(Bean.class);
        doReturn(RememberMeMechanism.class).when(bean).getBeanClass();
        set(interceptor, "httpMechanismBean", bean);

        @SuppressWarnings("unchecked")
        final Instance<RememberMeIdentityStore> storeInstance = mock(Instance.class);
        final RememberMeIdentityStore store = mock(RememberMeIdentityStore.class);
        when(storeInstance.get()).thenReturn(store);
        set(interceptor, "rememberMeIdentityStore", storeInstance);

        final BeanManager beanManager = mock(BeanManager.class);
        when(beanManager.getELResolver()).thenReturn(mock(ELResolver.class));
        set(interceptor, "beanManager", beanManager);

        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("JREMEMBERMEID", "the-login-token")});
        when(request.getContextPath()).thenReturn("");
        final HttpServletResponse response = mock(HttpServletResponse.class);

        final HttpMessageContext httpMessageContext = mock(HttpMessageContext.class);
        when(httpMessageContext.getRequest()).thenReturn(request);
        when(httpMessageContext.getResponse()).thenReturn(response);

        final InvocationContext invocationContext = mock(InvocationContext.class);
        when(invocationContext.getMethod()).thenReturn(
            HttpAuthenticationMechanism.class.getMethod(
                "cleanSubject", HttpServletRequest.class, HttpServletResponse.class, HttpMessageContext.class));
        when(invocationContext.getParameters()).thenReturn(new Object[]{request, response, httpMessageContext});
        when(invocationContext.getTarget()).thenReturn(new RememberMeMechanism());

        interceptor.intercept(invocationContext);

        verify(store).removeLoginToken("the-login-token");
    }

    private static void set(final Object target, final String fieldName, final Object value) throws Exception {
        final Field field = RememberMeInterceptor.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}

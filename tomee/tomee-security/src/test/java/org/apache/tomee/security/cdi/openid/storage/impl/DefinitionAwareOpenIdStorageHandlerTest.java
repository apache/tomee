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
package org.apache.tomee.security.cdi.openid.storage.impl;

import jakarta.security.enterprise.authentication.mechanism.http.OpenIdAuthenticationMechanismDefinition;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.tomee.security.cdi.openid.storage.OpenIdStorageHandler;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefinitionAwareOpenIdStorageHandlerTest {
    @Test
    public void usesCurrentDefinitionSessionStorage() {
        final DefinitionAwareOpenIdStorageHandler handler = new DefinitionAwareOpenIdStorageHandler();
        final OpenIdAuthenticationMechanismDefinition definition = mock(OpenIdAuthenticationMechanismDefinition.class);
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final HttpSession session = mock(HttpSession.class);

        when(definition.useSession()).thenReturn(true);
        when(request.getSession()).thenReturn(session);

        OpenIdStorageHandler.withDefinition(definition, () -> {
            handler.set(request, response, OpenIdStorageHandler.STATE_KEY, "state");
            return null;
        });

        verify(session).setAttribute("openid.STATE", "state");
        verify(response, never()).addCookie(any(Cookie.class));
    }

    @Test
    public void usesCurrentDefinitionCookieStorage() {
        final DefinitionAwareOpenIdStorageHandler handler = new DefinitionAwareOpenIdStorageHandler();
        final OpenIdAuthenticationMechanismDefinition definition = mock(OpenIdAuthenticationMechanismDefinition.class);
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final HttpSession session = mock(HttpSession.class);

        when(definition.useSession()).thenReturn(false);
        when(request.getSession()).thenReturn(session);

        OpenIdStorageHandler.withDefinition(definition, () -> {
            handler.set(request, response, OpenIdStorageHandler.STATE_KEY, "state");
            return null;
        });

        verify(response).addCookie(any(Cookie.class));
        verify(session, never()).setAttribute(eq("openid.STATE"), eq("state"));
    }

    @Test(expected = IllegalStateException.class)
    public void failsWithoutSelectedDefinition() {
        final DefinitionAwareOpenIdStorageHandler handler = new DefinitionAwareOpenIdStorageHandler();
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);

        handler.set(request, response, OpenIdStorageHandler.STATE_KEY, "state");
    }
}

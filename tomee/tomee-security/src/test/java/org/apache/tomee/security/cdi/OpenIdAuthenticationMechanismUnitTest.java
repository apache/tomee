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

import com.sun.net.httpserver.HttpServer;
import org.apache.openejb.util.reflection.Reflections;
import org.apache.tomee.security.cdi.openid.TomEEOpenIdContext;
import org.apache.tomee.security.cdi.openid.storage.OpenIdStorageHandler;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;

import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import jakarta.security.enterprise.authentication.mechanism.http.OpenIdAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.openid.OpenIdConstant;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.IdentityStoreHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OpenIdAuthenticationMechanismUnitTest {
    private OpenIdAuthenticationMechanism authenticationMechanism;
    private OpenIdAuthenticationMechanismDefinition definition;
    private IdentityStoreHandler identityStoreHandler;
    private SimpleStorageHandler storageHandler;

    @Before
    public void setUp() {
        authenticationMechanism = new OpenIdAuthenticationMechanism();
        definition = mock(OpenIdAuthenticationMechanismDefinition.class, Answers.RETURNS_DEEP_STUBS);
        identityStoreHandler = mock(IdentityStoreHandler.class);
        storageHandler = new SimpleStorageHandler();

        authenticationMechanism.setDefinitionSupplier(() -> definition);
        Reflections.set(authenticationMechanism, "identityStoreHandler", identityStoreHandler);
        Reflections.set(authenticationMechanism, "openIdContext", new TomEEOpenIdContext());
        Reflections.set(authenticationMechanism, "storageHandler", storageHandler);

        when(definition.clientId()).thenReturn("tomee-testing");
        when(definition.clientSecret()).thenReturn("secret");
        when(definition.redirectURI()).thenReturn("https://example.com/redirect");
        when(definition.redirectToOriginalResource()).thenReturn(true);
    }

    @Test
    public void callbackWithoutStoredOriginalRequestDoesNotThrowWhenRedirectToOriginalResourceIsEnabled() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/token", exchange -> {
            byte[] body = "{\"token_type\":\"Bearer\",\"access_token\":\"ACCESS\",\"id_token\":\"ID\",\"expires_in\":3600}"
                    .getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();

        try {
            when(definition.providerMetadata().tokenEndpoint())
                    .thenReturn("http://127.0.0.1:" + server.getAddress().getPort() + "/token");
            when(identityStoreHandler.validate(any()))
                    .thenReturn(new CredentialValidationResult("caller", Set.of("users")));

            HttpServletRequest request = mock(HttpServletRequest.class);
            HttpServletResponse response = mock(HttpServletResponse.class);
            HttpMessageContext messageContext = mock(HttpMessageContext.class, Answers.RETURNS_DEEP_STUBS);

            when(request.getParameter(OpenIdConstant.STATE)).thenReturn("STATE");
            when(request.getParameter(OpenIdConstant.CODE)).thenReturn("CODE");
            when(request.getRequestURL()).thenReturn(new StringBuffer("https://example.com/redirect"));
            when(messageContext.notifyContainerAboutLogin(any(CredentialValidationResult.class)))
                    .thenReturn(AuthenticationStatus.SUCCESS);

            storageHandler.set(request, response, OpenIdStorageHandler.STATE_KEY, "STATE");

            AuthenticationStatus status = authenticationMechanism.performAuthentication(request, response, messageContext);

            assertEquals(AuthenticationStatus.SUCCESS, status);
            verify(messageContext, never()).withRequest(any(HttpServletRequest.class));
        } finally {
            server.stop(0);
        }
    }

    private static class SimpleStorageHandler extends OpenIdStorageHandler {
        private final Map<String, String> values = new HashMap<>();

        @Override
        public String get(HttpServletRequest request, HttpServletResponse response, String key) {
            return values.get(key);
        }

        @Override
        public void set(HttpServletRequest request, HttpServletResponse response, String key, String value) {
            values.put(key, value);
        }

        @Override
        public void delete(HttpServletRequest request, HttpServletResponse response, String key) {
            values.remove(key);
        }
    }
}

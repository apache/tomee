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

import com.sun.net.httpserver.HttpExchange;
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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
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
            when(request.getRequestURI()).thenReturn("/redirect");
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

    @Test
    public void spoofedHostHeaderWithMatchingPathStillProcessesCallback() throws Exception {
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
            // Attacker-supplied Host header results in a foreign getRequestURL(), but the path
            // matches the configured redirectURI ("/redirect") so the callback must still be accepted.
            when(request.getRequestURL()).thenReturn(new StringBuffer("https://attacker.example.net/redirect"));
            when(request.getRequestURI()).thenReturn("/redirect");
            when(messageContext.notifyContainerAboutLogin(any(CredentialValidationResult.class)))
                    .thenReturn(AuthenticationStatus.SUCCESS);

            storageHandler.set(request, response, OpenIdStorageHandler.STATE_KEY, "STATE");

            AuthenticationStatus status = authenticationMechanism.performAuthentication(request, response, messageContext);

            assertEquals(AuthenticationStatus.SUCCESS, status);
        } finally {
            server.stop(0);
        }
    }

    @Test
    public void callbackWithPathMatchingNeitherRedirectNorOriginalReturnsNotValidated() {
        when(definition.redirectToOriginalResource()).thenReturn(false);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpMessageContext messageContext = mock(HttpMessageContext.class);

        when(request.getParameter(OpenIdConstant.STATE)).thenReturn("STATE");
        when(request.getRequestURL()).thenReturn(new StringBuffer("https://example.com/not-the-callback"));
        when(request.getRequestURI()).thenReturn("/not-the-callback");
        when(messageContext.notifyContainerAboutLogin(any(CredentialValidationResult.class)))
                .thenReturn(AuthenticationStatus.NOT_DONE);

        AuthenticationStatus status = authenticationMechanism.performAuthentication(request, response, messageContext);

        // Spec §2.4.4.2 — reject when the callback matches neither redirectURI nor the stored
        // original request, independent of the redirectToOriginalResource setting.
        assertEquals(AuthenticationStatus.NOT_DONE, status);
        verify(messageContext).notifyContainerAboutLogin(CredentialValidationResult.NOT_VALIDATED_RESULT);
    }

    @Test
    public void callbackWithMismatchingPathReturnsNotValidatedEvenWhenRedirectToOriginalResourceEnabled() {
        when(definition.redirectToOriginalResource()).thenReturn(true);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpMessageContext messageContext = mock(HttpMessageContext.class);

        // Prime a stored original request with a DIFFERENT path so neither comparison matches.
        storageHandler.set(request, response, OpenIdConstant.ORIGINAL_REQUEST, "https://example.com/other-path");

        when(request.getParameter(OpenIdConstant.STATE)).thenReturn("STATE");
        when(request.getRequestURL()).thenReturn(new StringBuffer("https://example.com/not-the-callback"));
        when(request.getRequestURI()).thenReturn("/not-the-callback");
        when(messageContext.notifyContainerAboutLogin(any(CredentialValidationResult.class)))
                .thenReturn(AuthenticationStatus.NOT_DONE);

        AuthenticationStatus status = authenticationMechanism.performAuthentication(request, response, messageContext);

        assertEquals(AuthenticationStatus.NOT_DONE, status);
        verify(messageContext).notifyContainerAboutLogin(CredentialValidationResult.NOT_VALIDATED_RESULT);
    }

    @Test
    public void tokenEndpointUsesClientSecretBasicByDefault() throws Exception {
        final AtomicReference<String> capturedAuthHeader = new AtomicReference<>();
        final AtomicReference<String> capturedBody = new AtomicReference<>();

        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/token", exchange -> {
            capturedAuthHeader.set(exchange.getRequestHeaders().getFirst("Authorization"));
            capturedBody.set(readRequestBody(exchange));

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
            when(request.getRequestURI()).thenReturn("/redirect");
            when(messageContext.notifyContainerAboutLogin(any(CredentialValidationResult.class)))
                    .thenReturn(AuthenticationStatus.SUCCESS);

            storageHandler.set(request, response, OpenIdStorageHandler.STATE_KEY, "STATE");

            AuthenticationStatus status = authenticationMechanism.performAuthentication(request, response, messageContext);

            assertEquals(AuthenticationStatus.SUCCESS, status);

            String expectedHeader = "Basic " + Base64.getEncoder()
                    .encodeToString("tomee-testing:secret".getBytes(StandardCharsets.UTF_8));
            assertNotNull("Authorization header must be present when using client_secret_basic", capturedAuthHeader.get());
            assertEquals(expectedHeader, capturedAuthHeader.get());

            Map<String, String> formParams = parseFormBody(capturedBody.get());
            assertFalse("client_id must NOT be in form body when using client_secret_basic",
                    formParams.containsKey(OpenIdConstant.CLIENT_ID));
            assertFalse("client_secret must NOT be in form body when using client_secret_basic",
                    formParams.containsKey(OpenIdConstant.CLIENT_SECRET));
            // Sanity: the expected grant-type and code ARE in the form body.
            assertEquals("authorization_code", formParams.get(OpenIdConstant.GRANT_TYPE));
            assertEquals("CODE", formParams.get(OpenIdConstant.CODE));
        } finally {
            server.stop(0);
        }
    }

    @Test
    public void tokenEndpointFallsBackToFormParametersWhenOnlyClientSecretPostAdvertised() throws Exception {
        final AtomicReference<String> capturedAuthHeader = new AtomicReference<>();
        final AtomicReference<String> capturedBody = new AtomicReference<>();

        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/token", exchange -> {
            capturedAuthHeader.set(exchange.getRequestHeaders().getFirst("Authorization"));
            capturedBody.set(readRequestBody(exchange));

            byte[] body = "{\"token_type\":\"Bearer\",\"access_token\":\"ACCESS\",\"id_token\":\"ID\",\"expires_in\":3600}"
                    .getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();

        // Simulate an OP that advertises ONLY client_secret_post by subclassing the mechanism
        // and overriding the preference decision. We keep the RETURNS_DEEP_STUBS wiring so the
        // rest of the flow still runs against the shared `definition` mock.
        OpenIdAuthenticationMechanism postOnlyMechanism = new OpenIdAuthenticationMechanism() {
            @Override
            protected boolean preferBasicAuth() {
                return false;
            }
        };
        postOnlyMechanism.setDefinitionSupplier(() -> definition);
        Reflections.set(postOnlyMechanism, "identityStoreHandler", identityStoreHandler);
        Reflections.set(postOnlyMechanism, "openIdContext", new TomEEOpenIdContext());
        Reflections.set(postOnlyMechanism, "storageHandler", storageHandler);

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
            when(request.getRequestURI()).thenReturn("/redirect");
            when(messageContext.notifyContainerAboutLogin(any(CredentialValidationResult.class)))
                    .thenReturn(AuthenticationStatus.SUCCESS);

            storageHandler.set(request, response, OpenIdStorageHandler.STATE_KEY, "STATE");

            AuthenticationStatus status = postOnlyMechanism.performAuthentication(request, response, messageContext);

            assertEquals(AuthenticationStatus.SUCCESS, status);

            assertNull("Authorization header must be absent when using client_secret_post",
                    capturedAuthHeader.get());

            Map<String, String> formParams = parseFormBody(capturedBody.get());
            assertTrue("client_id must be in form body when using client_secret_post",
                    formParams.containsKey(OpenIdConstant.CLIENT_ID));
            assertTrue("client_secret must be in form body when using client_secret_post",
                    formParams.containsKey(OpenIdConstant.CLIENT_SECRET));
            assertEquals("tomee-testing", formParams.get(OpenIdConstant.CLIENT_ID));
            assertEquals("secret", formParams.get(OpenIdConstant.CLIENT_SECRET));
        } finally {
            server.stop(0);
        }
    }

    private static String readRequestBody(HttpExchange exchange) throws java.io.IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (InputStream in = exchange.getRequestBody()) {
            byte[] buf = new byte[1024];
            int n;
            while ((n = in.read(buf)) != -1) {
                out.write(buf, 0, n);
            }
        }
        return out.toString(StandardCharsets.UTF_8);
    }

    private static Map<String, String> parseFormBody(String body) {
        Map<String, String> params = new HashMap<>();
        if (body == null || body.isEmpty()) {
            return params;
        }
        for (String pair : body.split("&")) {
            int eq = pair.indexOf('=');
            if (eq < 0) {
                params.put(URLDecoder.decode(pair, StandardCharsets.UTF_8), "");
            } else {
                String key = URLDecoder.decode(pair.substring(0, eq), StandardCharsets.UTF_8);
                String value = URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8);
                params.put(key, value);
            }
        }
        return params;
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

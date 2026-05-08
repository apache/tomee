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
package org.apache.tomee.security.cdi.openid;

import jakarta.security.enterprise.authentication.mechanism.http.OpenIdAuthenticationMechanismDefinition;
import org.apache.openejb.util.reflection.Reflections;
import org.apache.tomee.security.cdi.openid.storage.OpenIdStorageHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Verifies that {@link OpenIdIdentityStore} routes JWT validation through the per-mechanism
 * OIDC definition pushed by {@link OpenIdAuthenticationMechanism} via
 * {@link OpenIdStorageHandler#withDefinition}, not just the @Default-injected definition.
 *
 * <p>Background: the security extension registers one {@code OpenIdAuthenticationMechanism}
 * bean per qualified {@code @OpenIdAuthenticationMechanismDefinition}, but only one
 * {@code OpenIdIdentityStore} bean — the @Default. Without thread-local routing, a non-default
 * mechanism would exchange the auth code with provider B's token endpoint and then validate
 * the resulting tokens against provider A's issuer/audience/JWKS, silently accepting or
 * rejecting tokens against the wrong metadata.</p>
 */
public class OpenIdIdentityStoreDefinitionRoutingTest {

    private OpenIdAuthenticationMechanismDefinition defaultDefinition;
    private OpenIdAuthenticationMechanismDefinition nonDefaultDefinition;
    private OpenIdIdentityStore store;

    @Before
    public void setUp() {
        defaultDefinition = mock(OpenIdAuthenticationMechanismDefinition.class, Answers.RETURNS_DEEP_STUBS);
        nonDefaultDefinition = mock(OpenIdAuthenticationMechanismDefinition.class, Answers.RETURNS_DEEP_STUBS);

        when(defaultDefinition.providerMetadata().issuer()).thenReturn("https://idp-a.example.com");
        when(defaultDefinition.clientId()).thenReturn("client-a");

        when(nonDefaultDefinition.providerMetadata().issuer()).thenReturn("https://idp-b.example.com");
        when(nonDefaultDefinition.clientId()).thenReturn("client-b");

        store = new OpenIdIdentityStore();
        Reflections.set(store, "defaultDefinition", defaultDefinition);
    }

    @After
    public void tearDown() {
        // Defensive: don't let a thread-local leak across tests.
        OpenIdStorageHandler.withDefinition(null, () -> null);
    }

    @Test
    public void activeDefinitionFallsBackOutsideWithDefinition() {
        // Outside withDefinition() the active definition is the @Default-injected one.
        final OpenIdAuthenticationMechanismDefinition active = invokeActiveDefinition(store);
        assertEquals("client-a", active.clientId());
        assertEquals("https://idp-a.example.com", active.providerMetadata().issuer());
    }

    @Test
    public void activeDefinitionTracksThreadLocalDefinition() {
        // Inside withDefinition(), the active definition swaps to the non-default one --
        // exactly how OpenIdAuthenticationMechanism.validateRequest sets things up.
        OpenIdStorageHandler.withDefinition(nonDefaultDefinition, () -> {
            final OpenIdAuthenticationMechanismDefinition active = invokeActiveDefinition(store);
            assertEquals("client-b", active.clientId());
            assertEquals("https://idp-b.example.com", active.providerMetadata().issuer());
            return null;
        });

        // After the scope exits we are back on the default.
        assertEquals("client-a", invokeActiveDefinition(store).clientId());
    }

    @Test
    public void nestedWithDefinitionRestoresOuterOnExit() {
        // Outer scope: non-default. Inner scope: yet another (we reuse default for simplicity).
        // After inner exits we must observe outer's definition again -- otherwise nested
        // mechanisms (rare but legal) would corrupt the parent's view.
        OpenIdStorageHandler.withDefinition(nonDefaultDefinition, () -> {
            assertEquals("client-b", invokeActiveDefinition(store).clientId());

            OpenIdStorageHandler.withDefinition(defaultDefinition, () -> {
                assertEquals("client-a", invokeActiveDefinition(store).clientId());
                return null;
            });

            assertEquals("client-b", invokeActiveDefinition(store).clientId());
            return null;
        });
    }

    private static OpenIdAuthenticationMechanismDefinition invokeActiveDefinition(final OpenIdIdentityStore store) {
        try {
            final Method m = OpenIdIdentityStore.class.getDeclaredMethod("activeDefinition");
            m.setAccessible(true);
            return (OpenIdAuthenticationMechanismDefinition) m.invoke(store);
        } catch (Exception e) {
            throw new AssertionError("activeDefinition reflection failed", e);
        }
    }
}

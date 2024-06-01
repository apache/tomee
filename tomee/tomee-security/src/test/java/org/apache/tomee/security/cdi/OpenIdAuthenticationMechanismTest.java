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

import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.mockito.MockitoInjector;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.MockInjector;
import org.apache.tomee.security.cdi.openid.TomEEOpenIdContext;
import org.apache.tomee.security.cdi.openid.storage.OpenIdStorageHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Vetoed;
import jakarta.inject.Inject;
import jakarta.security.enterprise.authentication.mechanism.http.OpenIdAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.openid.DisplayType;
import jakarta.security.enterprise.authentication.mechanism.http.openid.PromptType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@Vetoed
@RunWith(ApplicationComposer.class)
@Classes(cdi = true, value = {OpenIdAuthenticationMechanism.class, TomEEOpenIdContext.class, OpenIdAuthenticationMechanismTest.SimpleStorageHandler.class})
public class OpenIdAuthenticationMechanismTest {

    @Inject
    private OpenIdAuthenticationMechanism authenticationMechanism;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private OpenIdAuthenticationMechanismDefinition definition;

    @MockInjector
    public Class<?> mockInjector() {
        return MockitoInjector.class;
    }

    @Before
    public void configureMockedDefinition() {
        when(definition.clientId()).thenReturn("tomee-testing");
        when(definition.scope()).thenReturn(new String[]{"openid", "tomee"});
        when(definition.responseType()).thenReturn("code");
        when(definition.redirectURI()).thenReturn("https://example.com/redirect");
        when(definition.providerMetadata().authorizationEndpoint()).thenReturn("https://openid.example.com/authorize");

        // Parameters appended conditionally
        when(definition.useNonce()).thenReturn(false);
        when(definition.responseMode()).thenReturn("");
        when(definition.display()).thenReturn(null);
        when(definition.prompt()).thenReturn(new PromptType[0]);
        when(definition.extraParameters()).thenReturn(new String[0]);
    }

    @Test
    public void unconditionalAuthorizationParameters() {
        String authorizationUrl = authenticationMechanism.buildAuthorizationUri(null, null).toString();

        // Parameters are defined in configureMockedDefinition
        assertEquals("https://openid.example.com/authorize"
                     + "?client_id=tomee-testing"
                     + "&scope=openid+tomee"
                     + "&response_type=code"
                     + "&state=STATE"
                     + "&redirect_uri=https%3A//example.com/redirect", authorizationUrl);
    }

    @Test
    public void authorizationNonce() {
        when(definition.useNonce()).thenReturn(true);

        String authorizationUrl = authenticationMechanism.buildAuthorizationUri(null, null).toString();

        assertTrue(authorizationUrl.contains("&nonce=NONCE"));
    }

    @Test
    public void authorizationResponseMode() {
        when(definition.responseMode()).thenReturn("special");

        String authorizationUrl = authenticationMechanism.buildAuthorizationUri(null, null).toString();

        assertTrue(authorizationUrl.contains("&response_mode=special"));
    }

    @Test
    public void authorizationDisplay() {
        when(definition.display()).thenReturn(DisplayType.POPUP);

        String authorizationUrl = authenticationMechanism.buildAuthorizationUri(null, null).toString();

        assertTrue(authorizationUrl.contains("&display=popup"));
    }

    @Test
    public void authorizationPrompt() {
        when(definition.prompt()).thenReturn(new PromptType[]{PromptType.LOGIN, PromptType.SELECT_ACCOUNT});

        String authorizationUrl = authenticationMechanism.buildAuthorizationUri(null, null).toString();

        assertTrue(authorizationUrl.contains("&prompt=login+select_account"));
    }

    @Test
    public void authorizationExtraParameters() {
        when(definition.extraParameters()).thenReturn(new String[]{"foo=bar", "bar=baz"});

        String authorizationUrl = authenticationMechanism.buildAuthorizationUri(null, null).toString();

        assertTrue(authorizationUrl.contains("&foo=bar&bar=baz"));
    }


    @Test
    public void authorizationExtraParametersMalformed() {
        when(definition.extraParameters()).thenReturn(new String[]{"foobar"});

        assertThrows(IllegalArgumentException.class, () -> authenticationMechanism.buildAuthorizationUri(null, null));
    }

    @ApplicationScoped
    protected static class SimpleStorageHandler extends OpenIdStorageHandler {

        @Override
        public String createNewState(HttpServletRequest request, HttpServletResponse response) {
            return "STATE";
        }

        @Override
        public String createNewNonce(HttpServletRequest request, HttpServletResponse response) {
            return "NONCE";
        }


        // get/set is a noop here
        @Override
        public String get(HttpServletRequest request, HttpServletResponse response, String key) {
            return null;
        }

        @Override
        public void set(HttpServletRequest request, HttpServletResponse response, String key, String value) {
        }
    }
}
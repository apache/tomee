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

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CookieBasedOpenIdStorageHandlerTest {

    private static final String KEY = "state";
    private static final String COOKIE_NAME = "openid." + KEY;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private CookieBasedOpenIdStorageHandler handler;

    @Before
    public void setUp() {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        handler = new CookieBasedOpenIdStorageHandler();
        // Keep the default (secure=true) behaviour explicit so the test is deterministic.
        System.clearProperty(CookieBasedOpenIdStorageHandler.STATE_COOKIE_SECURE_PROPERTY);
    }

    @After
    public void tearDown() {
        System.clearProperty(CookieBasedOpenIdStorageHandler.STATE_COOKIE_SECURE_PROPERTY);
    }

    @Test
    public void setEmitsSecureHttpOnlySameSiteNoneCookieRootPath() {
        handler.set(request, response, KEY, "any-state-value");

        Cookie cookie = captureAddedCookie();
        assertEquals(COOKIE_NAME, cookie.getName());
        assertTrue("cookie must be flagged Secure", cookie.getSecure());
        assertTrue("cookie must be flagged HttpOnly", cookie.isHttpOnly());
        assertEquals("/", cookie.getPath());
        assertEquals("None", cookie.getAttribute("SameSite"));
    }

    @Test
    public void setRespectsSecureOverrideSystemProperty() {
        System.setProperty(CookieBasedOpenIdStorageHandler.STATE_COOKIE_SECURE_PROPERTY, "false");

        handler.set(request, response, KEY, "value");

        Cookie cookie = captureAddedCookie();
        assertEquals("override should disable Secure flag", false, cookie.getSecure());
    }

    @Test
    public void deleteEmitsMatchingAttributesAndZeroMaxAge() {
        handler.delete(request, response, KEY);

        Cookie cookie = captureAddedCookie();
        assertEquals(COOKIE_NAME, cookie.getName());
        assertEquals("/", cookie.getPath());
        assertTrue("delete must also emit Secure so the browser overwrites the cookie", cookie.getSecure());
        assertTrue("delete must also emit HttpOnly", cookie.isHttpOnly());
        assertEquals("None", cookie.getAttribute("SameSite"));
        assertEquals(0, cookie.getMaxAge());
    }

    @Test
    public void roundTripPreservesUnicodeBytes() {
        // Characters that are not representable as single-byte in the platform default
        // charset on some JVMs (Cyrillic + CJK + emoji). Must round-trip via UTF-8.
        String original = "Привет, 世界 — \uD83D\uDE80";

        handler.set(request, response, KEY, original);
        Cookie storedCookie = captureAddedCookie();

        // The encoded value must be valid Base64 of the UTF-8 bytes, never the JVM default.
        String expectedEncoded = Base64.getEncoder().encodeToString(original.getBytes(StandardCharsets.UTF_8));
        assertEquals(expectedEncoded, storedCookie.getValue());

        // Now round-trip through get(...) by reflecting the cookie back in on the request.
        when(request.getCookies()).thenReturn(new Cookie[] {storedCookie});
        String decoded = handler.get(request, response, KEY);
        assertEquals(original, decoded);
    }

    @Test
    public void getReturnsNullWhenNoCookiesPresent() {
        when(request.getCookies()).thenReturn(null);
        assertNull(handler.get(request, response, KEY));
    }

    @Test
    public void getReturnsNullWhenCookieNameDoesNotMatch() {
        when(request.getCookies()).thenReturn(new Cookie[] {new Cookie("unrelated",
                Base64.getEncoder().encodeToString("x".getBytes(StandardCharsets.UTF_8)))});
        assertNull(handler.get(request, response, KEY));
    }

    @Test
    public void encodedValueIsNotPlaintext() {
        String original = "plaintext-marker";
        handler.set(request, response, KEY, original);
        Cookie cookie = captureAddedCookie();
        // Sanity check — the encoded cookie value must not be the literal plaintext.
        assertNotEquals(original, cookie.getValue());
    }

    private Cookie captureAddedCookie() {
        ArgumentCaptor<Cookie> captor = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(captor.capture());
        return captor.getValue();
    }
}

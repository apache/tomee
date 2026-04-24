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

import org.apache.tomee.security.cdi.openid.storage.OpenIdStorageHandler;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

public class CookieBasedOpenIdStorageHandler extends OpenIdStorageHandler {
    /**
     * System property that controls whether the OpenID state/nonce cookies are flagged
     * {@code Secure}. Defaults to {@code true} — the Jakarta Security spec §2.4.4.2
     * requires the cookie MUST be Secure when cookie-based storage is used. Operators
     * running behind a TLS-terminating proxy on plain HTTP can set this to {@code false}
     * for local testing, but production deployments should leave the default.
     */
    public static final String STATE_COOKIE_SECURE_PROPERTY = "tomee.security.openid.state-cookie-secure";

    private static boolean isSecureCookieEnabled() {
        return Boolean.parseBoolean(System.getProperty(STATE_COOKIE_SECURE_PROPERTY, "true"));
    }

    @Override
    public String get(HttpServletRequest request, HttpServletResponse response, String key) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (Objects.equals(cookie.getName(), PREFIX + key)) {
                return new String(Base64.getDecoder().decode(cookie.getValue()), StandardCharsets.UTF_8);
            }
        }

        return null;
    }

    @Override
    public void set(HttpServletRequest request, HttpServletResponse response, String key, String value) {
        Cookie cookie = new Cookie(PREFIX + key,
                Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8)));
        // OpenID callbacks are cross-origin; spec §2.4.4.2 says the cookie MUST be Secure
        // when cookie-based storage is used. Gated by a system property for local HTTP testing.
        cookie.setSecure(isSecureCookieEnabled());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        // SameSite=None is required for cross-site OIDC redirects to carry the cookie back.
        cookie.setAttribute("SameSite", "None");

        response.addCookie(cookie);
    }

    @Override
    public void delete(HttpServletRequest request, HttpServletResponse response, String key) {
        Cookie cookie = new Cookie(PREFIX + key, "");
        // Browsers only overwrite a cookie when the attributes match, so replicate everything
        // that set(...) emitted except the MaxAge which instructs the browser to drop it.
        cookie.setPath("/");
        cookie.setSecure(isSecureCookieEnabled());
        cookie.setHttpOnly(true);
        cookie.setAttribute("SameSite", "None");
        cookie.setMaxAge(0);

        response.addCookie(cookie);
    }
}

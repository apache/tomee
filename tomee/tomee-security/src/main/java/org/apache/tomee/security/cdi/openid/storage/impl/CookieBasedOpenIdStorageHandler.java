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
import java.util.Base64;
import java.util.Objects;

public class CookieBasedOpenIdStorageHandler extends OpenIdStorageHandler {
    @Override
    public String get(HttpServletRequest request, HttpServletResponse response, String key) {
        for (Cookie cookie : request.getCookies()) {
            if (Objects.equals(cookie.getName(), PREFIX + key)) {
                return new String(Base64.getDecoder().decode(cookie.getValue()));
            }
        }

        return null;
    }

    @Override
    public void set(HttpServletRequest request, HttpServletResponse response, String key, String value) {
        Cookie cookie = new Cookie(PREFIX + key, Base64.getEncoder().encodeToString(value.getBytes()));
        cookie.setSecure(request.isSecure());
        cookie.setHttpOnly(true);

        response.addCookie(cookie);
    }

    @Override
    public void delete(HttpServletRequest request, HttpServletResponse response, String key) {
        Cookie cookie = new Cookie(PREFIX + key, null);
        cookie.setMaxAge(0);

        response.addCookie(cookie);
    }
}

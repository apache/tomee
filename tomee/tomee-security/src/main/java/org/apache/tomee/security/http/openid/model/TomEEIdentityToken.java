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
package org.apache.tomee.security.http.openid.model;

import jakarta.json.Json;
import jakarta.json.JsonReader;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.security.enterprise.authentication.mechanism.http.openid.OpenIdConstant;
import jakarta.security.enterprise.identitystore.openid.IdentityToken;
import jakarta.security.enterprise.identitystore.openid.JwtClaims;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import java.io.StringReader;
import java.util.Base64;
import java.util.Map;

public class TomEEIdentityToken implements IdentityToken {
    private final Logger LOGGER = Logger.getInstance(LogCategory.TOMEE_SECURITY, TomEEIdentityToken.class);

    private final String token;
    private final long minValidity;

    private JwtClaims jwtClaims;
    private Map<String, Object> rawClaims;

    public TomEEIdentityToken(String token, long minValidity) {
        this.token = token;
        this.minValidity = minValidity;

        String json = new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]));
        try (JsonReader reader = Json.createReader(new StringReader(json)); Jsonb jsonb = JsonbBuilder.create()) {
            jwtClaims = new TomEEJwtClaims(reader.readObject());
            rawClaims = jsonb.fromJson(json, Map.class);
        } catch (Exception e) {
            LOGGER.error("Could not parse idToken claims", e);
        }
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public JwtClaims getJwtClaims() {
        return jwtClaims;
    }

    @Override
    public boolean isExpired() {
        return jwtClaims.getExpirationTime()
                .map(it -> System.currentTimeMillis() + minValidity > it.toEpochMilli())
                .orElseThrow(() -> new IllegalStateException("No " + OpenIdConstant.EXPIRATION_IDENTIFIER + " claim in identity token found"));
    }

    @Override
    public Map<String, Object> getClaims() {
        return rawClaims;
    }
}

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
import jakarta.security.enterprise.identitystore.openid.AccessToken;
import jakarta.security.enterprise.identitystore.openid.JwtClaims;
import jakarta.security.enterprise.identitystore.openid.Scope;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import java.io.StringReader;
import java.util.Base64;
import java.util.Map;

public class TomEEAccesToken implements AccessToken {
    private final Logger LOGGER = Logger.getInstance(LogCategory.TOMEE_SECURITY, TomEEAccesToken.class);

    private final boolean jwt;
    private final String token;
    private final Type type;
    private final Scope scope;
    private final Long expiresIn;
    private final long minValidity;
    private final long creationTime;


    private JwtClaims jwtClaims;
    private Map<String, Object> rawClaims;


    public TomEEAccesToken(boolean jwt, String token, Type type, Scope scope, Long expiresIn, long minValidity) {
        this.jwt = jwt;
        this.token = token;
        this.type = type;
        this.scope = scope;
        this.expiresIn = expiresIn;
        this.minValidity = minValidity;
        this.creationTime = System.currentTimeMillis() / 1000;

        if (jwt) {
            String json = new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]));
            try (JsonReader reader = Json.createReader(new StringReader(json)); Jsonb jsonb = JsonbBuilder.create()) {
                jwtClaims = new TomEEJwtClaims(reader.readObject());
                rawClaims = jsonb.fromJson(json, Map.class);
            } catch (Exception e) {
                LOGGER.error("Could not parse idToken claims", e);
            }
        }
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public boolean isJWT() {
        return jwt;
    }

    @Override
    public JwtClaims getJwtClaims() {
        return jwtClaims;
    }

    @Override
    public Map<String, Object> getClaims() {
        return rawClaims;
    }

    @Override
    public Object getClaim(String key) {
        if (!isJWT()) {
            return null;
        }

        return getClaims().get(key);
    }

    @Override
    public Long getExpirationTime() {
        return expiresIn;
    }

    @Override
    public boolean isExpired() {
        long expirationTimeSeconds;
        if (!isJWT()) {
            expirationTimeSeconds = creationTime + expiresIn;
        } else {
            expirationTimeSeconds = jwtClaims.getExpirationTime()
                    .map(it -> it.toEpochMilli() / 1000)
                    .orElseThrow(() -> new IllegalStateException("No " + OpenIdConstant.EXPIRATION_IDENTIFIER + " claim in identity token found"));
        }

        return System.currentTimeMillis() + minValidity > expirationTimeSeconds * 1000;
    }

    @Override
    public Scope getScope() {
        return scope;
    }

    @Override
    public Type getType() {
        return type;
    }
}

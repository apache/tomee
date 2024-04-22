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
import jakarta.security.enterprise.identitystore.openid.IdentityToken;
import jakarta.security.enterprise.identitystore.openid.JwtClaims;

import java.io.StringReader;
import java.util.Base64;
import java.util.Map;

public class TomEEIdentityToken implements IdentityToken {
    private final String token;

    public TomEEIdentityToken(String token) {
        this.token = token;
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public JwtClaims getJwtClaims() {
        String json = new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]));
        try (JsonReader reader = Json.createReader(new StringReader(json))) {
            return new TomEEJwtClaims(reader.readObject());
        }
    }

    @Override
    public boolean isExpired() {
        return false; // TODO
    }

    @Override
    public Map<String, Object> getClaims() {
        String json = new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]));
        try (Jsonb jsonb = JsonbBuilder.create()) {
            return jsonb.fromJson(json, Map.class);
        } catch (Exception e) {
            return null;
        }
    }
}

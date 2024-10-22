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

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.security.enterprise.identitystore.openid.Claims;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

public class TomEEJsonClaims implements Claims {
    private final JsonObject claims;

    public TomEEJsonClaims(JsonObject claims) {
        this.claims = claims;
    }

    @Override
    public Optional<String> getStringClaim(String name) {
        if (claims == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(claims.getString(name, null));
    }

    @Override
    public Optional<Instant> getNumericDateClaim(String name) {
        OptionalLong rawValue = getLongClaim(name);
        if (rawValue.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(Instant.ofEpochSecond(rawValue.getAsLong()));
    }

    @Override
    public List<String> getArrayStringClaim(String name) {
        if (claims == null || !claims.containsKey(name)) {
            return Collections.emptyList();
        }

        JsonValue claimValue = claims.get(name);
        if (claimValue instanceof JsonArray jsonArray) {
            return jsonArray.stream()
                    .map(JsonString.class::cast)
                    .map(JsonString::getString)
                    .toList();
        }

        if (claimValue instanceof JsonString jsonString) {
            return List.of(jsonString.getString());
        }

        throw new IllegalArgumentException("Claim " + name + " is of type " + claimValue.getValueType() + ", expected either " + JsonValue.ValueType.STRING + " or " + JsonValue.ValueType.ARRAY);
    }

    @Override
    public OptionalInt getIntClaim(String name) {
        if (claims == null || !claims.containsKey(name)) {
            return OptionalInt.empty();
        }

        return OptionalInt.of(claims.getInt(name));
    }

    @Override
    public OptionalLong getLongClaim(String name) {
        if (claims == null || !claims.containsKey(name)) {
            return OptionalLong.empty();
        }

        return OptionalLong.of(claims.getJsonNumber(name).longValue());
    }

    @Override
    public OptionalDouble getDoubleClaim(String name) {
        if (claims == null || !claims.containsKey(name)) {
            return OptionalDouble.empty();
        }

        return OptionalDouble.of(claims.getJsonNumber(name).doubleValue());
    }

    @Override
    public Optional<Claims> getNested(String name) {
        if (claims == null || !claims.containsKey(name)) {
            return Optional.empty();
        }

        return Optional.of(new TomEEJsonClaims(claims.getJsonObject(name)));
    }
}

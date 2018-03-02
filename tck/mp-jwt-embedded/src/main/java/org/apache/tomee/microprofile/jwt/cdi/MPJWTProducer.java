/*
 *     Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.apache.tomee.microprofile.jwt.cdi;

import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.ClaimValue;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.bind.Jsonb;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

@ApplicationScoped
public class MPJWTProducer {

    @Inject
    private Jsonb jsonb;

    private static final String TMP = "tmp";
    private static Logger log = Logger.getLogger(MPJWTProducer.class.getName());
    private static ThreadLocal<JsonWebToken> currentPrincipal = new ThreadLocal<>();

    public static void setJWTPrincipal(JsonWebToken principal) {
        currentPrincipal.set(principal);
    }

    public static JsonWebToken getJWTPrincpal() {
        return currentPrincipal.get();
    }

    <T> ClaimValue<Optional<T>> generalClaimValueProducer(String name) {
        ClaimValueWrapper<Optional<T>> wrapper = new ClaimValueWrapper<>(name);
        T value = getValue(name, false);
        Optional<T> optValue = Optional.ofNullable(value);
        wrapper.setValue(optValue);
        return wrapper;
    }

    JsonValue generalJsonValueProducer(String name) {
        Object value = getValue(name, false);
        JsonValue jsonValue = wrapValue(value);
        return jsonValue;
    }

    public <T> T getValue(String name, boolean isOptional) {
        JsonWebToken jwt = getJWTPrincpal();
        if (jwt == null) {
            log.fine(String.format("getValue(%s), null JsonWebToken", name));
            return null;
        }

        Optional<T> claimValue = jwt.claim(name);
        if (!isOptional && !claimValue.isPresent()) {
            log.fine(String.format("Failed to find Claim for: %s", name));
        }
        log.fine(String.format("getValue(%s), isOptional=%s, claimValue=%s", name, isOptional, claimValue));
        return claimValue.orElse(null);
    }

    JsonObject replaceMap(final Map<String, Object> map) {
        return jsonb.fromJson(jsonb.toJson(map), JsonObject.class);
    }

    JsonValue wrapValue(Object value) {
        JsonValue jsonValue = null;
        if (value instanceof JsonValue) {
            // This may already be a JsonValue
            jsonValue = (JsonValue) value;
        } else if (value instanceof String) {
            jsonValue = Json.createObjectBuilder()
                    .add(TMP, value.toString())
                    .build()
                    .getJsonString(TMP);
        } else if (value instanceof Number) {
            Number number = (Number) value;
            if ((number instanceof Long) || (number instanceof Integer)) {
                jsonValue = Json.createObjectBuilder()
                        .add(TMP, number.longValue())
                        .build()
                        .getJsonNumber(TMP);
            } else {
                jsonValue = Json.createObjectBuilder()
                        .add(TMP, number.doubleValue())
                        .build()
                        .getJsonNumber(TMP);
            }
        } else if (value instanceof Boolean) {
            Boolean flag = (Boolean) value;
            jsonValue = flag ? JsonValue.TRUE : JsonValue.FALSE;
        } else if (value instanceof Collection) {
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            Collection list = (Collection) value;
            for (Object element : list) {
                if (element instanceof String) {
                    arrayBuilder.add(element.toString());
                } else {
                    JsonValue jvalue = wrapValue(element);
                    arrayBuilder.add(jvalue);
                }
            }
            jsonValue = arrayBuilder.build();
        } else if (value instanceof Map) {
            jsonValue = replaceMap((Map) value);
        }
        return jsonValue;
    }

    @Produces
    @RequestScoped
    JsonWebToken currentPrincipal() {
        return currentPrincipal.get();
    }
}
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

package org.apache.tomee.security.http;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.annotation.JsonbTypeDeserializer;
import jakarta.json.bind.annotation.JsonbTypeSerializer;
import jakarta.json.bind.serializer.DeserializationContext;
import jakarta.json.bind.serializer.JsonbDeserializer;
import jakarta.json.bind.serializer.JsonbSerializer;
import jakarta.json.bind.serializer.SerializationContext;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonParser;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// JSON-B friendly class that stores the request data required for #
// both @LoginToContinue and @OpenIdAuthenticationMechanismDefinition(redirectToOriginalResource=true)
public class JsonFriendlyRequest {
    @JsonbTypeDeserializer(CookieDeSerializer.class)
    @JsonbTypeSerializer(CookieDeSerializer.class)
    private final Cookie[] cookies;
    private final Map<String, List<String>> headers = new HashMap<>();
    private final String method;
    private final String queryString;

    public JsonFriendlyRequest(HttpServletRequest request) {
        this.cookies = request.getCookies();

        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            headers.put(name, Collections.list(request.getHeaders(name)));
        }

        this.method = request.getMethod();
        this.queryString = request.getQueryString();
    }

    public HttpServletRequest mask(HttpServletRequest masked) {
        return new HttpServletRequestWrapper(masked) {
            @Override
            public Cookie[] getCookies() {
                return cookies;
            }

            @Override
            public Enumeration<String> getHeaderNames() {
                return Collections.enumeration(headers.keySet());
            }

            @Override
            public Enumeration<String> getHeaders(String name) {
                return Collections.enumeration(headers.get(name));
            }

            @Override
            public String getHeader(String name) {
                return headers.get(name).get(0);
            }

            @Override
            public String getMethod() {
                return method;
            }

            @Override
            public String getQueryString() {
                return queryString;
            }
        };
    }

    public String toJson() throws Exception {
        try (Jsonb jsonb = JsonbBuilder.create()) {
            return jsonb.toJson(this);
        }
    }

    public static JsonFriendlyRequest fromJson(String json) throws Exception {
        try (Jsonb jsonb = JsonbBuilder.create()) {
            return jsonb.fromJson(json, JsonFriendlyRequest.class);
        }
    }

    public static class CookieDeSerializer implements JsonbSerializer<Cookie>, JsonbDeserializer<Cookie> {
        @Override
        public Cookie deserialize(JsonParser parser, DeserializationContext ctx, Type rtType) {
            String name = null;
            String value = null;
            Map<String, String> attributes = null;

            while (parser.hasNext()) {
                JsonParser.Event current = parser.next();

                if (current == JsonParser.Event.KEY_NAME) {
                    String key = parser.getString();

                    // move forward to value
                    parser.next();

                    if ("name".equals(key)) {
                        name = parser.getString();
                    } else if ("value".equals(key)) {
                        value = parser.getString();
                    } else if ("attributes".equals(key)) {
                        attributes = ctx.deserialize(Map.class, parser);
                    }
                }
            }

            Cookie cookie = new Cookie(name, value);
            if (attributes != null) {
                for (Map.Entry<String, String> entry : attributes.entrySet()) {
                    cookie.setAttribute(entry.getKey(), entry.getValue());
                }
            }

            return cookie;
        }

        @Override
        public void serialize(Cookie obj, JsonGenerator generator, SerializationContext ctx) {
            generator.write("name", obj.getName());
            generator.write("value", obj.getValue());
            ctx.serialize("attributes", obj.getAttributes(), generator);
        }
    }
}

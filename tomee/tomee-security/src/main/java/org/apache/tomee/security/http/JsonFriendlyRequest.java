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

import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
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
    private static final Logger LOGGER = Logger.getInstance(LogCategory.TOMEE_SECURITY, JsonFriendlyRequest.class);

    private static final CookieDeSerializer COOKIE_DE_SERIALIZER = new CookieDeSerializer();
    private static final JsonbConfig jsonbConfig = new JsonbConfig()
            .withSerializers(COOKIE_DE_SERIALIZER)
            .withDeserializers(COOKIE_DE_SERIALIZER);

    private Cookie[] cookies;
    private Map<String, List<String>> headers;
    private String method;
    private String queryString;

    public static JsonFriendlyRequest fromRequest(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        Map<String, List<String>> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            headers.put(name, Collections.list(request.getHeaders(name)));
        }

        String method = request.getMethod();
        String queryString = request.getQueryString();

        JsonFriendlyRequest result = new JsonFriendlyRequest();
        result.setCookies(cookies);
        result.setHeaders(headers);
        result.setMethod(method);
        result.setQueryString(queryString);

        return result;
    }

    public static JsonFriendlyRequest fromJson(String json) {
        try (Jsonb jsonb = JsonbBuilder.create(jsonbConfig)) {
            return jsonb.fromJson(json, JsonFriendlyRequest.class);
        } catch (Exception e) {
            LOGGER.error("Could not restore request from JSON", e);
            return null;
        }
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

    public String toJson() {
        try (Jsonb jsonb = JsonbBuilder.create(jsonbConfig)) {
            return jsonb.toJson(this);
        } catch (Exception e) {
            LOGGER.error("Could not store request in JSON", e);
            return null;
        }
    }

    public Cookie[] getCookies() {
        return cookies;
    }

    public void setCookies(Cookie[] cookies) {
        this.cookies = cookies;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
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

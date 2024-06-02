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

import org.junit.Test;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.servlet.http.Cookie;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class JsonFriendlyRequestTest {

    @Test
    public void testSerializable() {
        JsonFriendlyRequest request = new JsonFriendlyRequest();
        assertTrue("must implement Serializable, since it will be set as a session attribute",
                request instanceof Serializable);
    }

    @Test
    public void serialization() throws Exception {
        JsonFriendlyRequest request = new JsonFriendlyRequest();
        request.setCookies(new Cookie[] {new Cookie("first", "val1"), new Cookie("second", "val2")});
        request.setHeaders(new LinkedHashMap<>());
        request.getHeaders().put("header1", List.of("h1val1", "h1val2"));
        request.getHeaders().put("header2", List.of("h2val1"));
        request.setMethod("PATCH");
        request.setQueryString("foo=bar");
        request.setUrl("http://example.com/foo");

        assertEquals("{\"cookies\":[{\"name\":\"first\",\"value\":\"val1\",\"attributes\":{}},{\"name\":\"second\",\"value\":\"val2\",\"attributes\":{}}],\"headers\":{\"header1\":[\"h1val1\",\"h1val2\"],\"header2\":[\"h2val1\"]},\"method\":\"PATCH\",\"queryString\":\"foo=bar\",\"url\":\"http://example.com/foo\"}", request.toJson());
    }

    @Test
    public void deserialization() throws Exception {
        String json = "{\"cookies\":[{\"name\":\"first\",\"value\":\"val1\",\"attributes\":{}},{\"name\":\"second\",\"value\":\"val2\",\"attributes\":{}}],\"headers\":{\"header1\":[\"h1val1\",\"h1val2\"],\"header2\":[\"h2val1\"]},\"method\":\"PATCH\",\"queryString\":\"foo=bar\",\"url\":\"http://example.com/foo\"}";
        JsonFriendlyRequest request = JsonFriendlyRequest.fromJson(json);

        assertNotNull(request);
        assertEquals(2, request.getCookies().length);
        assertEquals("first", request.getCookies()[0].getName());
        assertEquals("val1", request.getCookies()[0].getValue());
        assertEquals("second", request.getCookies()[1].getName());
        assertEquals("val2", request.getCookies()[1].getValue());
        assertEquals(2, request.getHeaders().size());
        assertEquals(List.of("h1val1", "h1val2"), request.getHeaders().get("header1"));
        assertEquals(List.of("h2val1"), request.getHeaders().get("header2"));
        assertEquals("PATCH", request.getMethod());
        assertEquals("foo=bar", request.getQueryString());
        assertEquals("http://example.com/foo", request.getUrl());
    }

    @Test
    public void cookieSerialization() throws Exception {
        JsonbConfig config = new JsonbConfig()
                    .withSerializers(new JsonFriendlyRequest.CookieDeSerializer());

        try (Jsonb jsonb = JsonbBuilder.create(config)){
            Cookie cookie = new Cookie("name", "value");
            cookie.setDomain("example.com");
            cookie.setMaxAge(123);
            cookie.setPath("/aaa");

            System.err.println(jsonb.toJson(cookie));
        }
    }

    @Test
    public void cookieDeserialization() throws Exception {
        JsonbConfig config = new JsonbConfig()
                .withDeserializers(new JsonFriendlyRequest.CookieDeSerializer());

        try (Jsonb jsonb = JsonbBuilder.create(config)){
            String json = "{\"name\":\"name\",\"value\":\"value\",\"attributes\":{\"Domain\":\"example.com\",\"Max-Age\":\"123\",\"Path\":\"/aaa\"}}";

            Cookie cookie = jsonb.fromJson(json, Cookie.class);

            assertEquals("name", cookie.getName());
            assertEquals("value", cookie.getValue());
            assertEquals("example.com", cookie.getDomain());
            assertEquals(123, cookie.getMaxAge());
            assertEquals("/aaa", cookie.getPath());
        }
    }
}
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

import static org.junit.Assert.*;

public class JsonFriendlyRequestTest {

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
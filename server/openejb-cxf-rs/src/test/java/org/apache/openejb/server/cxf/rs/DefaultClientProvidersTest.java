/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.server.cxf.rs;

import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.RandomPort;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.beans.ConstructorProperties;
import java.net.URL;
import jakarta.json.JsonObject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Classes(DefaultClientProvidersTest.TheEndpoint.class)
@EnableServices("jaxrs")
@RunWith(ApplicationComposer.class)
public class DefaultClientProvidersTest {
    @RandomPort("http")
    private URL http;

    @Test
    public void json() {
        final Json json = ClientBuilder.newBuilder().build().target(http.toExternalForm()).path("openejb/DefaultClientProvidersTest")
                .request().get(Json.class);
        assertNotNull(json);
        assertEquals("value", json.key);
    }

    @Test
    public void jsonp() {
        final JsonObject json = ClientBuilder.newBuilder().build().target(http.toExternalForm()).path("openejb/DefaultClientProvidersTest")
                .request().get(JsonObject.class);
        assertNotNull(json);
        assertEquals("value", json.getString("key"));
        assertEquals(1, json.size());
    }

    @Path("DefaultClientProvidersTest")
    public static class TheEndpoint {
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String json() {
            return "{\"key\":\"value\"}"; // not supported by org.apache.cxf.jaxrs.provider.json.JSONProvider
        }
    }

    public static class Json {
        private String key;

        @ConstructorProperties("key")
        public Json(String key) {
            this.key = key;
        }
    }
}

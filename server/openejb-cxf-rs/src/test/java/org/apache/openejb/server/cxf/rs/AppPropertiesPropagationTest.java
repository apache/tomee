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
package org.apache.openejb.server.cxf.rs;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.JaxrsProviders;
import org.apache.openejb.testing.RandomPort;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@EnableServices("jaxrs")
@JaxrsProviders(AppPropertiesPropagationTest.Registrator.class)
@RunWith(ApplicationComposer.class)
@Classes(innerClassesAsBean = true)
@Ignore("Not sure this is used - we can implement it back if needed as discussed in mailing list and slack")
public class AppPropertiesPropagationTest {
    @RandomPort("http")
    private int port;

    @Test
    public void checkStarIsNotAnIssue() {
        assertEquals("yes", WebClient.create("http://localhost:" + port + "/openejb/")
                .path("AppPropertiesPropagationTest/endpoint").get(String.class));
        assertEquals("yes", WebClient.create("http://localhost:" + port + "/openejb/")
                .path("AppPropertiesPropagationTest/endpoint/2").get(String.class));
    }

    @Path("endpoint")
    public static class MyEndpoint {
        @GET
        public String get(@Context final Application app) {
            return String.valueOf(app.getProperties().get("AppPropertiesPropagationTest"));
        }

        @GET
        @Produces("AppPropertiesPropagationTest/1")
        @Path("2")
        public MyEndpoint provider(@Context final Application app) {
            return this;
        }
    }

    @Provider
    public static class Registrator implements Feature {
        @Override
        public boolean configure(final FeatureContext context) {
            if (!context.getConfiguration().getProperties().containsKey("AppPropertiesPropagationTest")) {
                return false;
            }

            context.register(new Writer(context.getConfiguration().getProperty("AppPropertiesPropagationTest")
                    .toString().getBytes(StandardCharsets.UTF_8)));
            return true;
        }
    }

    @Provider
    @Produces("AppPropertiesPropagationTest/1")
    public static class Writer implements MessageBodyWriter<MyEndpoint> {
        private final byte[] value;

        public Writer(byte[] value) {
            this.value = value;
        }

        @Override
        public boolean isWriteable(final Class<?> type, final Type genericType,
                                   final Annotation[] annotations, final MediaType mediaType) {
            return type == MyEndpoint.class;
        }

        @Override
        public long getSize(final MyEndpoint myEndpoint, final Class<?> type, final Type genericType,
                            final Annotation[] annotations, final MediaType mediaType) {
            return -1;
        }

        @Override
        public void writeTo(final MyEndpoint myEndpoint, final Class<?> type, final Type genericType,
                            final Annotation[] annotations, final MediaType mediaType,
                            final MultivaluedMap<String, Object> httpHeaders,
                            final OutputStream entityStream) throws IOException, WebApplicationException {
            if (value != null) {
                entityStream.write(value);
            }
        }
    }

    @ApplicationPath("/AppPropertiesPropagationTest")
    public static class MyApp extends Application {
        @Override
        public Map<String, Object> getProperties() {
            return new HashMap<String, Object>(){{
                put("AppPropertiesPropagationTest", "yes");
            }};
        }
    }
}

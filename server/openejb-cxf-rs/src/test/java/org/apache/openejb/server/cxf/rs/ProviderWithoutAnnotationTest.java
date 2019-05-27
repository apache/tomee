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
package org.apache.openejb.server.cxf.rs;

import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.RandomPort;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

@EnableServices("jax-rs")
@RunWith(ApplicationComposer.class)
@Classes(innerClassesAsBean = true)
public class ProviderWithoutAnnotationTest {
    @RandomPort("http")
    private URL base;

    @Test
    public void run() {
        final Client client = ClientBuilder.newClient();
        try {
            assertEquals("foo", client.target(base.toExternalForm() + "openejb/api/ProviderWithoutAnnotationTest")
                    .request("foo/bar")
                    .get(String.class));
        } finally {
            client.close();
        }
    }

    @ApplicationPath("api")
    public static class App extends Application {
        @Override
        public Set<Class<?>> getClasses() {
            return new HashSet<>(asList(Endpoint.class, FooWriter.class));
        }
    }

    @Path("ProviderWithoutAnnotationTest")
    public static class Endpoint {
        @GET
        @Produces("foo/bar")
        public Endpoint get() {
            return this;
        }
    }

    @Produces("foo/*")
    public static class FooWriter implements MessageBodyWriter<Endpoint> {
        @Override
        public boolean isWriteable(final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType) {
            return type == Endpoint.class && mediaType.getType().equals("foo");
        }

        @Override
        public long getSize(final Endpoint s, final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType) {
            return -1;
        }

        @Override
        public void writeTo(final Endpoint s, final Class<?> type, final Type genericType, final Annotation[] annotations,
                            final MediaType mediaType, final MultivaluedMap<String, Object> httpHeaders, final OutputStream entityStream) throws IOException, WebApplicationException {
            entityStream.write("foo".getBytes(StandardCharsets.UTF_8));
        }
    }
}

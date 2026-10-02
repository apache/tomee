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
package org.apache.openejb.arquillian.tests.jaxrs.provider;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
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

@RunWith(Arquillian.class)
public class ProviderWithoutAnnotationTest {
    @ArquillianResource
    private URL base;

    @Deployment(testable = false)
    public static WebArchive war() {
        return ShrinkWrap.create(WebArchive.class, "ProviderWithoutAnnotationTest.war")
            .addClass(ProviderWithoutAnnotationTest.class)
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void run() {
        final Client client = ClientBuilder.newClient();
        try {
            assertEquals("foo", client.target(base.toExternalForm() + "api/ProviderWithoutAnnotationTest")
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

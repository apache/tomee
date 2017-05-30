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

import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.loader.IO;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.ContainerProperties;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.JaxrsProviders;
import org.apache.openejb.testing.RandomPort;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URL;

import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.junit.Assert.assertEquals;

@EnableServices("jax-rs")
@Classes(DisableTomEEJohnzonTest.Endpoint.class)
@JaxrsProviders(DisableTomEEJohnzonTest.TestWriter.class)
@ContainerProperties({
        @ContainerProperties.Property(name = "org.apache.openejb.server.cxf.rs.johnzon.TomEEJohnzonProvider.activated", value = "false")
})
@RunWith(ApplicationComposer.class)
public class DisableTomEEJohnzonTest {

    private static final String PAYLOAD = "{\"not\": \"johnzon\"}";

    @RandomPort("http")
    private URL base;

    @Test
    public void server() throws IOException {
        assertEquals(PAYLOAD, IO.slurp(new URL(url())));
    }

    @Test
    public void client() throws IOException {
        assertEquals(PAYLOAD, ClientBuilder.newClient().register(new TestWriter()).target(url()).request().post(entity(new Payload(), APPLICATION_JSON_TYPE), String.class));
    }

    private String url() {
        return base.toExternalForm() + "openejb/test";
    }

    @Path("test")
    public static class Endpoint {
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public Payload get() {
            return new Payload();
        }

        @POST
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        public String post(final String body) {
            return body;
        }
    }

    public static class Payload {
    }

    @Provider
    @Produces(MediaType.WILDCARD)
    public static class TestWriter implements MessageBodyWriter {
        @Override
        public boolean isWriteable(Class aClass, Type type, Annotation[] annotations, MediaType mediaType) {
            return aClass == Payload.class;
        }

        @Override
        public long getSize(Object o, Class aClass, Type type, Annotation[] annotations, MediaType mediaType) {
            return -1;
        }

        @Override
        public void writeTo(Object o, Class aClass, Type type, Annotation[] annotations, MediaType mediaType,
                            MultivaluedMap multivaluedMap, OutputStream outputStream) throws IOException, WebApplicationException {
            outputStream.write(PAYLOAD.getBytes("UTF-8"));

        }
    }
}
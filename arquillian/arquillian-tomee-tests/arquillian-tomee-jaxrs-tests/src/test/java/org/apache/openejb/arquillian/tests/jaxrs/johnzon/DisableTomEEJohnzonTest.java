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
package org.apache.openejb.arquillian.tests.jaxrs.johnzon;

import org.apache.openejb.loader.IO;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URL;

import static jakarta.ws.rs.client.Entity.entity;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
@Ignore("Not sure this is used - we can implement it back if needed as discussed in mailing list and slack. " +
        "This is partially implemented in the server side but not quite happy with the hack")
public class DisableTomEEJohnzonTest {

    private static final String PAYLOAD = "{\"not\": \"johnzon\"}";

    @ArquillianResource
    private URL base;

    @Deployment(testable = false)
    public static WebArchive war() { // TestWriter is a @Provider, packaging it registers it
        return ShrinkWrap.create(WebArchive.class, "DisableTomEEJohnzonTest.war")
            .addClass(DisableTomEEJohnzonTest.class)
            .addAsWebInfResource(new StringAsset(
                "<openejb-jar>\n" +
                "  <pojo-deployment class-name=\"jaxrs-application\">\n" +
                "    <properties>\n" +
                "      org.apache.openejb.server.cxf.rs.johnzon.TomEEJsonbProvider.activated = false\n" +
                "      org.apache.openejb.server.cxf.rs.johnzon.TomEEJsonpProvider.activated = false\n" +
                "    </properties>\n" +
                "  </pojo-deployment>\n" +
                "</openejb-jar>\n"), "openejb-jar.xml");
    }

    @Test
    public void server() throws IOException {
        assertEquals(PAYLOAD, IO.slurp(new URL(url())));
    }

    @Test
    public void client() throws IOException {
        assertEquals(PAYLOAD, ClientBuilder.newClient().register(new TestWriter()).target(url()).request().post(entity(new Payload(), APPLICATION_JSON_TYPE), String.class));
    }

    private String url() {
        return base.toExternalForm() + "test";
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
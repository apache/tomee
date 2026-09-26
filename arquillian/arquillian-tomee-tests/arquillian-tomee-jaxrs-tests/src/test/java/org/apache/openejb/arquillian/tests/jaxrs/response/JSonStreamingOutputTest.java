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
package org.apache.openejb.arquillian.tests.jaxrs.response;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.json.Json;
import jakarta.json.stream.JsonGenerator;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class JSonStreamingOutputTest {
    @ArquillianResource
    private URL root;

    @Deployment(testable = false)
    public static WebArchive war() {
        return ShrinkWrap.create(WebArchive.class, "JSonStreamingOutputTest.war")
            .addClass(JSonStreamingOutputTest.class)
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void run() {
        assertEquals("[{\"id\":1}]", ClientBuilder.newClient().target(root.toExternalForm()).path("streamTest").request().get(String.class));
    }

    @Path("streamTest")
    public static class En {
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public Response touch() {
            return Response.ok().entity(new StreamingOutput() {
                @Override
                public void write(final OutputStream os) throws IOException, WebApplicationException {
                    try (final JsonGenerator jg = Json.createGenerator(os)) { // in real life use the factory
                        jg.writeStartArray();
                        jg.writeStartObject().write("id", 1).writeEnd(); // simple for the assert
                        jg.writeEnd().close();
                    }
                }
            }).type(MediaType.APPLICATION_JSON).build();
        }
    }
}

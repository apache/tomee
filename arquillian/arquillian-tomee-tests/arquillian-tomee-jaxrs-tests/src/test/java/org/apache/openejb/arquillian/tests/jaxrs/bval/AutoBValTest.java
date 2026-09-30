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
package org.apache.openejb.arquillian.tests.jaxrs.bval;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URL;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class AutoBValTest {
    @ArquillianResource
    private URL base;

    @Deployment(testable = false)
    public static WebArchive war() {
        return ShrinkWrap.create(WebArchive.class, "AutoBValTest.war")
            .addClass(AutoBValTest.class)
            .addAsWebInfResource(new StringAsset("<beans bean-discovery-mode=\"all\" />"), "beans.xml")
            .addAsWebInfResource(new StringAsset(
                "<openejb-jar>\n" +
                "  <pojo-deployment class-name=\"jaxrs-application\">\n" +
                "    <properties>\n" +
                "      cxf.jaxrs.bval.log.level = INFO\n" +
                "    </properties>\n" +
                "  </pojo-deployment>\n" +
                "</openejb-jar>\n"), "openejb-jar.xml");
    }

    @Test
    public void passing() {
        final Payload payload = new Payload();
        payload.setName("ok");
        assertEquals(
                "ok",
                ClientBuilder.newClient().target(base.toExternalForm()).path("test").request(MediaType.APPLICATION_JSON_TYPE)
                        .post(Entity.entity(payload, MediaType.APPLICATION_JSON_TYPE), Payload.class)
                        .getName());
    }

    @Test
    public void inFailing() {
        final Payload payload = new Payload();
        assertEquals(
                Response.Status.BAD_REQUEST.getStatusCode(), // thanks to the mapper
                ClientBuilder.newClient().target(base.toExternalForm()).path("test").request(MediaType.APPLICATION_JSON_TYPE)
                        .post(Entity.entity(payload, MediaType.APPLICATION_JSON_TYPE)).getStatus());
    }

    @Test
    public void outFailing() {
        final Payload payload = new Payload();
        payload.setName("empty");
        assertEquals(
                Response.Status.BAD_REQUEST.getStatusCode(), // thanks to the mapper
                ClientBuilder.newClient().target(base.toExternalForm()).path("test").request(MediaType.APPLICATION_JSON_TYPE)
                        .post(Entity.entity(payload, MediaType.APPLICATION_JSON_TYPE)).getStatus());
    }

    @Test
    public void checkVoidResponse() {
        assertEquals(
                Response.Status.NO_CONTENT.getStatusCode(),
                ClientBuilder.newClient().target(base.toExternalForm()).path("test/simple").request().get().getStatus());
    }

    @Test
    public void checkResponse() {
        assertEquals(
                Response.Status.OK.getStatusCode(),
                ClientBuilder.newClient().target(base.toExternalForm()).path("test/simpleResponse").request().get().getStatus());
    }

    @Path("test")
    public static class ValidateMe {
        @POST
        @Valid
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        public Payload get(@Valid final Payload in) {
            final Payload payload = new Payload();
            payload.setName("empty".equals(in.name) ? null : in.name);
            return payload;
        }

        @GET
        @Path("simple")
        public void service() {
            // no-op; should return a 204 no content
            System.out.println("Service invoked");
        }

        @GET
        @Path("simpleResponse")
        public Response serviceResponse() {
            return Response.ok().build();
        }
    }

    public static class Payload {
        @NotNull
        private String name;

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }
    }
}

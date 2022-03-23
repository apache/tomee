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
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.RandomPort;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.validation.constraints.Size;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URL;

import static org.junit.Assert.assertEquals;

@EnableServices("jaxrs")
@Classes(innerClassesAsBean = true, cdi = true)
@RunWith(ApplicationComposer.class)
public class AutoBValSimpleTest {
    @RandomPort("http")
    private URL base;

    @Test
    public void passing() {
        assertEquals(
                "Hello ok",
                client().path("openejb/AutoBValSimpleTest/ok").request(MediaType.TEXT_PLAIN).get(String.class));
    }

    @Test
    public void failing() {
        assertEquals( // see org.apache.cxf.jaxrs.validation.ValidationExceptionMapper.toResponse()
                Response.Status.BAD_REQUEST.getStatusCode(),
                client().path("openejb/AutoBValSimpleTest/toolong").request(MediaType.TEXT_PLAIN).get().getStatus());
    }

    private WebTarget client() {
        return ClientBuilder.newClient().target(base.toExternalForm());
    }

    @Path("AutoBValSimpleTest/{world}")
    public static class HelloResource {
        @GET
        @Size(min = 1, max = 8)
        public String msg(@PathParam("world") final String world) {
            return "Hello " + world;
        }
    }
}

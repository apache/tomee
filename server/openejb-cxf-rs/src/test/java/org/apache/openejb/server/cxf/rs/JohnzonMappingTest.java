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

import org.apache.johnzon.mapper.JohnzonProperty;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.RandomPort;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import java.net.URL;

import static org.junit.Assert.assertEquals;

@EnableServices("jaxrs")
@Classes(innerClassesAsBean = true)
@RunWith(ApplicationComposer.class)
public class JohnzonMappingTest {
    @RandomPort("http")
    private URL base;

    @Test
    public void mapping() {
        assertEquals(
                "no",
                ClientBuilder.newClient().target(base.toExternalForm()).path("openejb/JohnzonMappingTest").request(MediaType.APPLICATION_JSON_TYPE)
                        .post(Entity.entity("{\"_name\":\"yes\"}", MediaType.APPLICATION_JSON_TYPE), Payload.class)
                        .getName());
    }

    @Path("JohnzonMappingTest")
    public static class ValidateMe {
        @POST
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        public Payload get(final Payload in) {
            assertEquals("yes", in.getName());
            final Payload payload = new Payload();
            payload.setName("no");
            return payload;
        }
    }

    public static class Payload {
        @NotNull
        @JohnzonProperty("_name")
        private String name;

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }
    }
}

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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URL;

import static org.junit.Assert.assertEquals;

@Classes(innerClassesAsBean = true)
@EnableServices("jaxrs")
@RunWith(ApplicationComposer.class)
public class ApplicationRsInjectionTest {
    @RandomPort("http")
    private URL root;

    @Test
    public void run() {
        assertEquals("false", ClientBuilder.newClient().target(root.toExternalForm()).path("/openejb/applicationTest").request().get(String.class));
    }

    @Path("applicationTest")
    public static class Dummy {

        // This was null. It should be injected here
        // https://issues.apache.org/jira/browse/TOMEE-2201
        @Context
        private Application application;

        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public Response touch() {
            return Response.ok().entity(this.application == null).type(MediaType.TEXT_PLAIN).build();
        }
    }

}

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Set;

import static jakarta.ws.rs.core.MediaType.WILDCARD_TYPE;
import static org.junit.Assert.assertEquals;

@EnableServices("jaxrs")
@Classes(cdi = true, innerClassesAsBean = true)
@RunWith(ApplicationComposer.class)
public class LinkTest {
    @RandomPort("http")
    private URL base;

    @Test
    public void get() throws IOException {
        final Response response = ClientBuilder.newClient()
                .target(base.toExternalForm()).path("openejb/link")
                // cxf is not consistent for Link and other headers, see org.apache.cxf.transport.http.Headers.copyToResponse()
                .property("org.apache.cxf.http.header.split", true)
                .request(WILDCARD_TYPE).get();
        assertEquals(2, Collection.class.cast(response.getHeaders().get("a")).size());

        final Set<Link> actual = response.getLinks();
        assertEquals(2, actual.size());

        final Set<Link> expected = new LinkEndpoint().doLink().getLinks();
        assertEquals(expected, actual);
    }

    @Path("link")
    @ApplicationScoped
    public static class LinkEndpoint {
        @GET
        public Response doLink() {
            return Response.ok()
                    .header("a", "1")
                    .header("a", "2")
                    .link("http://test1", "self")
                    .link("http://test2", "rel")
                    .build();
        }
    }
}

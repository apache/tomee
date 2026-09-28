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
package org.apache.tomee.microprofile.health;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.tomee.server.composer.Archive;
import org.apache.tomee.server.composer.TomEE;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * MicroProfile Health endpoints belong to the context root of every web application, whatever JAX-RS
 * {@link Application} subclasses the application declares.
 */
public class HealthEndpointTest {

    @Test
    public void applicationListingItsClasses() throws Exception {
        final TomEE tomee = deploy(Archive.archive()
                                          .add(HealthEndpointTest.class)
                                          .add(ListingApp.class)
                                          .add(HelloResource.class)
                                          .asJar());

        assertEquals(200, get(tomee, "/test/api/hello").getStatus());
        assertEquals(200, get(tomee, "/test/health").getStatus());
        assertEquals(200, get(tomee, "/test/health/live").getStatus());
    }

    @Test
    public void applicationListingNothing() throws Exception {
        final TomEE tomee = deploy(Archive.archive()
                                          .add(HealthEndpointTest.class)
                                          .add(ScanningApp.class)
                                          .add(HelloResource.class)
                                          .asJar());

        assertEquals(200, get(tomee, "/test/api/hello").getStatus());
        assertEquals(200, get(tomee, "/test/health").getStatus());
        assertEquals(200, get(tomee, "/test/health/live").getStatus());
    }

    private static TomEE deploy(final File appJar) throws Exception {
        return TomEE.microprofile()
                    .add("webapps/test/WEB-INF/beans.xml", "")
                    .add("webapps/test/WEB-INF/lib/app.jar", appJar)
                    .build();
    }

    private static Response get(final TomEE tomee, final String path) throws Exception {
        final URL base = tomee.toURI().toURL();
        return WebClient.create(base.toExternalForm()).path(path).get();
    }

    @ApplicationPath("/api")
    public static class ListingApp extends Application {
        @Override
        public Set<Class<?>> getClasses() {
            return Set.of(HelloResource.class);
        }
    }

    @ApplicationPath("/api")
    public static class ScanningApp extends Application {
    }

    @Path("hello")
    public static class HelloResource {
        @GET
        public String hello() {
            return "hello";
        }
    }
}

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
import jakarta.ws.rs.NameBinding;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.tomee.server.composer.Archive;
import org.apache.tomee.server.composer.TomEE;
import org.junit.Test;

import java.io.File;
import java.lang.annotation.Retention;
import java.net.URL;
import java.util.Set;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
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

    @Test
    public void applicationAtTheContextRoot() throws Exception {
        final TomEE tomee = deploy(Archive.archive()
                                          .add(HealthEndpointTest.class)
                                          .add(RootApp.class)
                                          .add(HelloResource.class)
                                          .asJar());

        assertEquals(200, get(tomee, "/test/hello").getStatus());
        assertEquals(200, get(tomee, "/test/health").getStatus());
        assertEquals(200, get(tomee, "/test/health/live").getStatus());
    }

    @Test
    public void scanningApplicationAtTheContextRoot() throws Exception {
        final TomEE tomee = deploy(Archive.archive()
                                          .add(HealthEndpointTest.class)
                                          .add(RootScanningApp.class)
                                          .add(HelloResource.class)
                                          .asJar());

        assertEquals(200, get(tomee, "/test/hello").getStatus());
        assertEquals(200, get(tomee, "/test/health").getStatus());
        assertEquals(200, get(tomee, "/test/health/live").getStatus());
    }

    @Test
    public void applicationWithNameBindingsAtTheContextRoot() throws Exception {
        final TomEE tomee = deploy(Archive.archive()
                                          .add(HealthEndpointTest.class)
                                          .add(Denied.class)
                                          .add(DeniedRootApp.class)
                                          .add(DenyingFilter.class)
                                          .add(HelloResource.class)
                                          .asJar());

        assertEquals(403, get(tomee, "/test/hello").getStatus());
        assertEquals(200, get(tomee, "/test/health").getStatus());
        assertEquals(200, get(tomee, "/test/health/live").getStatus());
    }

    @Test
    public void multipleApplications() throws Exception {
        final TomEE tomee = deploy(Archive.archive()
                                          .add(HealthEndpointTest.class)
                                          .add(ListingApp.class)
                                          .add(OtherApp.class)
                                          .add(HelloResource.class)
                                          .asJar());

        assertEquals(200, get(tomee, "/test/api/hello").getStatus());
        assertEquals(200, get(tomee, "/test/other/hello").getStatus());
        assertEquals(200, get(tomee, "/test/health").getStatus());
        assertEquals(200, get(tomee, "/test/health/live").getStatus());
    }

    @Test
    public void applicationWithItsOwnProvider() throws Exception {
        final TomEE tomee = deploy(Archive.archive()
                                          .add(HealthEndpointTest.class)
                                          .add(UnavailableApp.class)
                                          .add(UnavailableFilter.class)
                                          .add(HelloResource.class)
                                          .asJar());

        assertEquals(503, get(tomee, "/test/api/hello").getStatus());
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

    @ApplicationPath("/")
    public static class RootApp extends Application {
        @Override
        public Set<Class<?>> getClasses() {
            return Set.of(HelloResource.class);
        }
    }

    @ApplicationPath("/")
    public static class RootScanningApp extends Application {
    }

    @NameBinding
    @Retention(RUNTIME)
    public @interface Denied {
    }

    @Denied
    @ApplicationPath("/")
    public static class DeniedRootApp extends Application {
        @Override
        public Set<Class<?>> getClasses() {
            return Set.of(HelloResource.class, DenyingFilter.class);
        }
    }

    @Denied
    public static class DenyingFilter implements ContainerRequestFilter {
        @Override
        public void filter(final ContainerRequestContext requestContext) {
            requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
        }
    }

    @ApplicationPath("/api")
    public static class UnavailableApp extends Application {
        @Override
        public Set<Class<?>> getClasses() {
            return Set.of(HelloResource.class, UnavailableFilter.class);
        }
    }

    @PreMatching
    public static class UnavailableFilter implements ContainerRequestFilter {
        @Override
        public void filter(final ContainerRequestContext requestContext) {
            requestContext.abortWith(Response.status(Response.Status.SERVICE_UNAVAILABLE).build());
        }
    }

    @ApplicationPath("/other")
    public static class OtherApp extends Application {
        @Override
        public Set<Class<?>> getClasses() {
            return Set.of(HelloResource.class);
        }
    }

    @Path("hello")
    public static class HelloResource {
        @GET
        public String hello() {
            return "hello";
        }
    }
}

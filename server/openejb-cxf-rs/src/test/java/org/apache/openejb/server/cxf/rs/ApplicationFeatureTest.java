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

import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;

import java.util.Set;
import java.util.stream.Stream;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.RandomPort;
import org.junit.Test;
import org.junit.runner.RunWith;

@EnableServices("jaxrs")
@RunWith(ApplicationComposer.class)
@Classes({ ApplicationFeatureTest.MyEndpoint.class, ApplicationFeatureTest.MyApp.class })
public class ApplicationFeatureTest {
    @RandomPort("http")
    private int port;

    @Test
    public void checkStarIsNotAnIssue() {
        final Client client = ClientBuilder.newClient();
        try {
            assertEquals("ok", client.target("http://localhost:" + port + "/openejb/ApplicationFeatureTest-MyApp")
                    .path("test")
                    .request().get(String.class));
        } finally {
            client.close();
        }
    }

    @Path("test")
    public static class MyEndpoint {
        @GET
        public String get() {
            return "failed";
        }
    }

    // don't put @Provider here
    public static class MyFeature implements Feature {
        @Override
        public boolean configure(final FeatureContext featureContext) {
            featureContext.register((ContainerRequestFilter) ctx -> ctx.abortWith(Response.ok("ok").build()));
            return true;
        }
    }

    @ApplicationPath("ApplicationFeatureTest-MyApp")
    public static class MyApp extends Application {
        @Override
        public Set<Class<?>> getClasses() {
            return Stream.of(MyEndpoint.class, MyFeature.class).collect(toSet());
        }
    }
}

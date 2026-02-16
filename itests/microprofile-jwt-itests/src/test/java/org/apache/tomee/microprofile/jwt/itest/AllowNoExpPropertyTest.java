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

package org.apache.tomee.microprofile.jwt.itest;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import static java.util.Collections.singletonList;
import java.util.Optional;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.johnzon.jaxrs.JohnzonProvider;
import org.apache.tomee.server.composer.Archive;
import org.apache.tomee.server.composer.TomEE;
import org.eclipse.microprofile.auth.LoginConfig;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


public class AllowNoExpPropertyTest {

    @Test
    public void testNewPropertyOverridesOld1() throws Exception {
        final Tokens tokens = Tokens.rsa(2048, 256);
        final File appJar = Archive.archive()
                .add(AllowNoExpPropertyTest.class)
                .add(ColorService.class)
                .add(Api.class)
                .add("META-INF/microprofile-config.properties", "#\n" +
                        "mp.jwt.verify.publickey=" + Base64.getEncoder().encodeToString(tokens.getPublicKey().getEncoded())
                         + "\n" + "mp.jwt.tomee.allow.no-exp=false"
                         + "\n" + "tomee.mp.jwt.allow.no-exp=true")
                .asJar();

        final ArrayList<String> output = new ArrayList<>();
        final TomEE tomee = TomEE.microprofile()
                .add("webapps/test/WEB-INF/beans.xml", "")
                .add("webapps/test/WEB-INF/lib/app.jar", appJar)
                .watch("org.apache.tomee.microprofile.jwt.", "\n", output::add)
                .build();

        final WebClient webClient = createWebClient(tomee.toURI().resolve("/test").toURL());

        final String claims = "{" +
                "  \"sub\":\"Jane Awesome\"" +
                "}";

        {// invalid token
            final String token = tokens.asToken(claims);
            final Response response = webClient.reset()
                    .path("/movies")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .get();
            assertEquals(403, response.getStatus());
        }

        assertPresent(output , "mp.jwt.tomee.allow.no-exp property is deprecated");
        assertNotPresent(output, "rejected due to invalid claims");
        assertNotPresent(output, "No Expiration Time (exp) claim present.");
        assertNotPresent(output, "\tat org."); // no stack traces
    }

    @Test
    public void testNewPropertyOverridesOld2() throws Exception {
        final Tokens tokens = Tokens.rsa(2048, 256);
        final File appJar = Archive.archive()
                .add(AllowNoExpPropertyTest.class)
                .add(ColorService.class)
                .add(Api.class)
                .add("META-INF/microprofile-config.properties", "#\n" +
                        "mp.jwt.verify.publickey=" + Base64.getEncoder().encodeToString(tokens.getPublicKey().getEncoded())
                         + "\n" + "mp.jwt.tomee.allow.no-exp=true"
                         + "\n" + "tomee.mp.jwt.allow.no-exp=false")
                .asJar();

        final ArrayList<String> output = new ArrayList<>();
        final TomEE tomee = TomEE.microprofile()
                .add("webapps/test/WEB-INF/beans.xml", "")
                .add("webapps/test/WEB-INF/lib/app.jar", appJar)
                .watch("org.apache.tomee.microprofile.jwt.", "\n", output::add)
                .build();

        final WebClient webClient = createWebClient(tomee.toURI().resolve("/test").toURL());

        final String claims = "{" +
                "  \"sub\":\"Jane Awesome\"" +
                "}";

        {// invalid token
            final String token = tokens.asToken(claims);
            final Response response = webClient.reset()
                    .path("/movies")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .get();
            assertEquals(401, response.getStatus());
        }

        assertPresent(output , "mp.jwt.tomee.allow.no-exp property is deprecated");
        assertPresent(output, "rejected due to invalid claims");
        assertPresent(output, "No Expiration Time (exp) claim present.");
        assertNotPresent(output, "\tat org."); // no stack traces
    }
    
    @Test
    public void testNewProperty() throws Exception {
        final Tokens tokens = Tokens.rsa(2048, 256);
        final File appJar = Archive.archive()
                .add(AllowNoExpPropertyTest.class)
                .add(ColorService.class)
                .add(Api.class)
                .add("META-INF/microprofile-config.properties", "#\n" +
                        "mp.jwt.verify.publickey=" + Base64.getEncoder().encodeToString(tokens.getPublicKey().getEncoded())
                         + "\n" + "tomee.mp.jwt.allow.no-exp=true")
                .asJar();

        final ArrayList<String> output = new ArrayList<>();
        final TomEE tomee = TomEE.microprofile()
                .add("webapps/test/WEB-INF/beans.xml", "")
                .add("webapps/test/WEB-INF/lib/app.jar", appJar)
                .watch("org.apache.tomee.microprofile.jwt.", "\n", output::add)
                .build();

        final WebClient webClient = createWebClient(tomee.toURI().resolve("/test").toURL());

        final String claims = "{" +
                "  \"sub\":\"Jane Awesome\"" +
                "}";

        {// invalid token
            final String token = tokens.asToken(claims);
            final Response response = webClient.reset()
                    .path("/movies")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .get();
            assertEquals(403, response.getStatus());
        }

        assertNotPresent(output , "mp.jwt.tomee.allow.no-exp property is deprecated");
        assertNotPresent(output, "rejected due to invalid claims");
        assertNotPresent(output, "No Expiration Time (exp) claim present.");
        assertNotPresent(output, "\tat org."); // no stack traces
    }
    
    @Test
    public void testOldProperty() throws Exception {
        final Tokens tokens = Tokens.rsa(2048, 256);
        final File appJar = Archive.archive()
                .add(AllowNoExpPropertyTest.class)
                .add(ColorService.class)
                .add(Api.class)
                .add("META-INF/microprofile-config.properties", "#\n" +
                        "mp.jwt.verify.publickey=" + Base64.getEncoder().encodeToString(tokens.getPublicKey().getEncoded())
                         + "\n" + "mp.jwt.tomee.allow.no-exp=true")
                .asJar();

        final ArrayList<String> output = new ArrayList<>();
        final TomEE tomee = TomEE.microprofile()
                .add("webapps/test/WEB-INF/beans.xml", "")
                .add("webapps/test/WEB-INF/lib/app.jar", appJar)
                .watch("org.apache.tomee.microprofile.jwt.", "\n", output::add)
                .build();

        final WebClient webClient = createWebClient(tomee.toURI().resolve("/test").toURL());

        final String claims = "{" +
                "  \"sub\":\"Jane Awesome\"" +
                "}";

        {// invalid token
            final String token = tokens.asToken(claims);
            final Response response = webClient.reset()
                    .path("/movies")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .get();
            assertEquals(403, response.getStatus());
        }

        assertPresent(output , "mp.jwt.tomee.allow.no-exp property is deprecated");
        assertNotPresent(output, "rejected due to invalid claims");
        assertNotPresent(output, "No Expiration Time (exp) claim present.");
        assertNotPresent(output, "\tat org."); // no stack traces
    }
    
    public void assertPresent(final ArrayList<String> output, final String s) {
        final Optional<String> actual = output.stream()
                .filter(line -> line.contains(s))
                .findFirst();

        assertTrue(actual.isPresent());
    }
    public void assertNotPresent(final ArrayList<String> output, final String s) {
        final Optional<String> actual = output.stream()
                .filter(line -> line.contains(s))
                .findFirst();

        assertTrue(!actual.isPresent());
    }

    private static WebClient createWebClient(final URL base) {
        return WebClient.create(base.toExternalForm(), singletonList(new JohnzonProvider<>()), null);
    }

    @ApplicationPath("/api")
    @LoginConfig(authMethod = "MP-JWT")
    public class Api extends Application {
    }

    @Path("/movies")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RequestScoped
    public static class ColorService {

        @GET
        @RolesAllowed({"manager", "user"})
        public String getAllMovies() {
            return "Green";
        }
    }

}

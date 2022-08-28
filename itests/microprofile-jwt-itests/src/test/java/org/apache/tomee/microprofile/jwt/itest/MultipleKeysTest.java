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

import io.churchkey.Key;
import io.churchkey.Keys;
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
import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.johnzon.jaxrs.JohnzonProvider;
import org.apache.tomee.server.composer.Archive;
import org.apache.tomee.server.composer.TomEE;
import org.eclipse.microprofile.auth.LoginConfig;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

public class MultipleKeysTest {

    @Test
    public void jwksWithKid() throws Exception {
        final Tokens[] tokens = {
                Tokens.rsa(2048, 256, "orange-1"),
                Tokens.rsa(2048, 256, "orange-2"),
                Tokens.rsa(2048, 256, "orange-3"),
        };

        final List<Key> publicKeys = Stream.of(tokens)
                .map(tokens1 -> {
                    final Key key = Keys.of(tokens1.getPublicKey());
                    key.getAttributes().put("kid", tokens1.getId());
                    return key;
                })
                .collect(Collectors.toList());

        final File appJar = Archive.archive()
                .add(MultipleKeysTest.class)
                .add(ColorService.class)
                .add(Api.class)
                .add("orange.jwks", Keys.encodeSet(publicKeys, Key.Format.JWK))
                .add("META-INF/microprofile-config.properties", "#\n" +
                        "mp.jwt.verify.publickey.location=orange.jwks")
                .asJar();

        final TomEE tomee = TomEE.microprofile()
//                .debug(5005)
                .add("webapps/test/WEB-INF/beans.xml", "")
                .add("webapps/test/WEB-INF/lib/app.jar", appJar)
                .build();

        for (final Tokens token : tokens) {
            assertVerification(token, tomee);
        }
    }

    @Ignore("TOMEE-4029")
    @Test
    public void jwks() throws Exception {
        final Tokens[] tokens = {
                Tokens.rsa(2048, 256),
                Tokens.rsa(2048, 256),
                Tokens.rsa(2048, 256),
        };

        final AtomicInteger kids = new AtomicInteger();
        final List<Key> publicKeys = Stream.of(tokens)
                .map(Tokens::getPublicKey)
                .map(Keys::of)
                .peek(key -> key.getAttributes().put("kid", "orange" + kids.incrementAndGet()))
                .collect(Collectors.toList());

        final File appJar = Archive.archive()
                .add(MultipleKeysTest.class)
                .add(ColorService.class)
                .add(Api.class)
                .add("orange.jwks", Keys.encodeSet(publicKeys, Key.Format.JWK))
                .add("META-INF/microprofile-config.properties", "#\n" +
                        "mp.jwt.verify.publickey.location=orange.jwks")
                .asJar();

        final TomEE tomee = TomEE.microprofile()
                .debug(5005)
                .add("webapps/test/WEB-INF/beans.xml", "")
                .add("webapps/test/WEB-INF/lib/app.jar", appJar)
                .build();

        for (final Tokens token : tokens) {
            assertVerification(token, tomee);
        }
    }

    private void assertVerification(final Tokens tokens, final TomEE tomee) throws Exception {
        final WebClient webClient = createWebClient(tomee.toURI().resolve("/test").toURL());

        final String claims = "{" +
                "  \"sub\":\"Jane Awesome\"," +
                "  \"iss\":\"https://server.example.com\"," +
                "  \"groups\":[\"manager\",\"user\"]," +
                "  \"jti\":\"uB3r7zOr\"," +
                "  \"exp\":2552047942" +
                "}";

        {// valid token
            final String token = tokens.asToken(claims);
            final Response response = webClient.reset()
                    .path("/movies")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .get();
            assertEquals(200, response.getStatus());
        }

        {// invalid token
            final String token = tokens.asToken(claims) + "a";
            final Response response = webClient.reset()
                    .path("/movies")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .get();
            assertEquals(401, response.getStatus());
        }
    }

    private static WebClient createWebClient(final URL base) {
        return WebClient.create(base.toExternalForm(), singletonList(new JohnzonProvider<>()),
                singletonList(new LoggingFeature()), null);
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
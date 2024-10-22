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
import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.johnzon.jaxrs.JohnzonProvider;
import org.apache.tomee.server.composer.Archive;
import org.apache.tomee.server.composer.TomEE;
import org.eclipse.microprofile.auth.LoginConfig;
import org.junit.Test;

import java.io.File;
import java.net.URL;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

public class PublicKeyFormatsTest {

    @Test
    public void pkcs8Pem() throws Exception {
        final Tokens tokens = Tokens.rsa(2048, 256);

        final File appJar = Archive.archive()
                .add(PublicKeyFormatsTest.class)
                .add(ColorService.class)
                .add(Api.class)
                .add("orange.pem", "-----BEGIN PUBLIC KEY-----\n" +
                        tokens.getEncodedPublicKey() +
                        "\n-----END PUBLIC KEY-----")
                .add("META-INF/microprofile-config.properties", "#\n" +
                        "mp.jwt.verify.publickey.location=orange.pem")
                .asJar();

        final TomEE tomee = TomEE.microprofile()
                .add("webapps/test/WEB-INF/beans.xml", "")
                .add("webapps/test/WEB-INF/lib/app.jar", appJar)
                .build();

        assertVerification(tokens, tomee);
    }

    @Test
    public void jwk() throws Exception {
        final Tokens tokens = Tokens.rsa(2048, 256);

        final File appJar = Archive.archive()
                .add(PublicKeyFormatsTest.class)
                .add(ColorService.class)
                .add(Api.class)
                .add("orange", tokens.getJwkPublicKey())
                .add("META-INF/microprofile-config.properties", "#\n" +
                        "mp.jwt.verify.publickey.location=orange")
                .asJar();

        final TomEE tomee = TomEE.microprofile()
                .add("webapps/test/WEB-INF/beans.xml", "")
                .add("webapps/test/WEB-INF/lib/app.jar", appJar)
                .build();

        assertVerification(tokens, tomee);
    }

    @Test
    public void jwks() throws Exception {
        final Tokens tokens = Tokens.rsa(2048, 256);

        final File appJar = Archive.archive()
                .add(PublicKeyFormatsTest.class)
                .add(ColorService.class)
                .add(Api.class)
                .add("orange", tokens.getJwksPublicKey())
                .add("META-INF/microprofile-config.properties", "#\n" +
                        "mp.jwt.verify.publickey.location=orange")
                .asJar();

        final TomEE tomee = TomEE.microprofile()
                .add("webapps/test/WEB-INF/beans.xml", "")
                .add("webapps/test/WEB-INF/lib/app.jar", appJar)
                .build();

        assertVerification(tokens, tomee);
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
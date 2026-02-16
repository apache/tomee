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
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.johnzon.jaxrs.JohnzonProvider;
import org.apache.tomee.server.composer.Archive;
import org.apache.tomee.server.composer.TomEE;
import org.eclipse.microprofile.auth.LoginConfig;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.util.Base64;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

public class SignatureAlgorithmsTest {

    @Test
    public void rs256() throws Exception {
        assertAlgorithm("RS256", Tokens.rsa(2048, 256));
    }

    @Test
    public void rs384() throws Exception {
        assertAlgorithm("RS384", Tokens.rsa(2048, 384));
    }

    @Test
    public void rs512() throws Exception {
        assertAlgorithm("RS512", Tokens.rsa(2048, 512));
    }

    @Test
    public void es256() throws Exception {
        assertAlgorithm("ES256", Tokens.ec("secp256r1", 256));
    }

    public void assertAlgorithm(final String alg, final Tokens tokens) throws Exception {
        final File appJar = Archive.archive()
                .add(SignatureAlgorithmsTest.class)
                .add(ColorService.class)
                .add(Api.class)
                .add("META-INF/microprofile-config.properties", "#\n" +
                        "mp.jwt.verify.publickey=" + Base64.getEncoder().encodeToString(tokens.getPublicKey().getEncoded()))
                .asJar();

        final TomEE tomee = TomEE.microprofile()
                .add("webapps/test/WEB-INF/beans.xml", "")
                .add("webapps/test/WEB-INF/lib/app.jar", appJar)
//                .update()
                .build();

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

            assertAlg(alg, token);

            final Response response = webClient.reset()
                    .path("/movies")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .get();
            assertEquals(200, response.getStatus());
        }

        {// invalid token
            final String token = "a" + tokens.asToken(claims);
            final Response response = webClient.reset()
                    .path("/movies")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .get();
            assertEquals(401, response.getStatus());
        }
    }

    private void assertAlg(final String expected, final String token) {
        final String encodedHeader = token.split("\\.")[0];

        final byte[] decoded = Base64.getDecoder().decode(encodedHeader);
        final JsonReader reader = Json.createReader(new ByteArrayInputStream(decoded));
        final JsonObject jsonObject = reader.readObject();
        final String actual = jsonObject.getString("alg");

        assertEquals(expected, actual);
    }

    private static WebClient createWebClient(final URL base) {
        return WebClient.create(base.toExternalForm(), singletonList(new JohnzonProvider<>()),
                null);
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
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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tomee.microprofile.jwt.itest.keys.http;

import com.nimbusds.jose.JWSSigner;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
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
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.JarLocation;
import org.apache.tomee.itest.util.Runner;
import org.apache.tomee.microprofile.jwt.itest.Tokens;
import org.apache.tomee.microprofile.jwt.itest.keys.PublicKeyLocation;
import org.apache.tomee.server.composer.Archive;
import org.apache.tomee.server.composer.TomEE;
import org.eclipse.microprofile.auth.LoginConfig;
import org.junit.Test;
import org.tomitribe.util.Longs;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

public class HttpKeyLocationTest {

    @Test
    public void happyPath() throws Exception {

        final TomEE keyServer = TomEE.microprofile()
                .add("webapps/ROOT/WEB-INF/beans.xml", "")
                .add("webapps/ROOT/WEB-INF/lib/app.jar", Archive.archive()
                        .add(HttpKeyLocationTest.class)
                        .add(KeyServer.KeysService.class)
                        .add(KeyServer.class)
                        .add(Tokens.class)
                        .asJar())
                .add("webapps/ROOT/WEB-INF/lib/jose.jar", JarLocation.jarLocation(JWSSigner.class))
                .build();

        final TomEE tomee = TomEE.microprofile()
                .add("webapps/ROOT/WEB-INF/beans.xml", "")
                .add("webapps/ROOT/WEB-INF/lib/app.jar", Archive.archive()
                        .add(HttpKeyLocationTest.class)
                        .add(MicroProfileWebApp.class)
                        .add(MicroProfileWebApp.ColorService.class)
                        .add(MicroProfileWebApp.Api.class)
                        .add(KeyServer.class)
                        .add("META-INF/microprofile-config.properties", new PublicKeyLocation()
                                .refreshInterval(1, TimeUnit.HOURS)
                                .location(keyServer.toURI().resolve("/keys/publicKey"))
                                .build())
                        .toJar())
                .build();


        final String privateKey = IO.slurp(keyServer.toURI().resolve("/keys/privateKey").toURL());

        Runner.threads(100).run(() -> {

            final Tokens unknownKey = Tokens.rsa(2048, 256);
            final Tokens validKey = Tokens.fromPrivateKey(privateKey);

            final String id = Longs.toHex(System.nanoTime());
            final String claims = "{" +
                    "  \"sub\":\"Jane Awesome\"," +
                    "  \"iss\":\"https://server.example.com\"," +
                    "  \"groups\":[\"manager\",\"user\"]," +
                    "  \"jti\":\"" + id + "\"," +
                    "  \"exp\":2552047942" +
                    "}";

            final WebClient client = WebClient.create(tomee.toURI().toASCIIString(),
                    singletonList(new JohnzonProvider<>()),
//                    singletonList(new LoggingFeature()),
                    null);

            {// valid token
                final Response response = client.reset()
                        .path("/api/colors")
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + validKey.asToken(claims))
                        .get();
                assertEquals(200, response.getStatus());
            }
            {// invalid token
                final Response response = client.reset()
                        .path("/api/colors")
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + unknownKey.asToken(claims))
                        .get();
                assertEquals(401, response.getStatus());
            }
        }).assertNoExceptions();


        final int publicKeyCalls = Integer.parseInt(IO.slurp(keyServer.toURI().resolve("/keys/calls").toURL()));
        assertEquals(1, publicKeyCalls);
    }

    public static class MicroProfileWebApp {
        @ApplicationPath("/api")
        @LoginConfig(authMethod = "MP-JWT")
        public static class Api extends Application {
        }

        @Path("/colors")
        @Produces(MediaType.APPLICATION_JSON)
        @Consumes(MediaType.APPLICATION_JSON)
        @RequestScoped
        public static class ColorService {

            @GET
            @RolesAllowed({"manager", "user"})
            public String get() {
                return "Green";
            }
        }
    }

    public static class KeyServer {
        @Path("/keys")
        @Produces(MediaType.TEXT_PLAIN)
        @Consumes(MediaType.TEXT_PLAIN)
        @ApplicationScoped
        public static class KeysService {

            private final Tokens tokens = Tokens.rsa(2048, 256);
            private final AtomicInteger calls = new AtomicInteger();

            @GET
            @Path("publicKey")
            public String publicKey() throws Exception {
                calls.incrementAndGet();
                return tokens.getPemPublicKey();
            }

            @GET
            @Path("privateKey")
            public String privateKey() {
                return tokens.getPemPrivateKey();
            }

            @GET
            @Path("calls")
            public int calls() {
                return calls.get();
            }
        }
    }


}

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
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
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

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

/**
 * Here we test the behavior of TomEE when keys are rotated, both successfully
 * and unsuccessfully because the wrong key type is returned.
 *
 * In scenarios where the key server returns a key that is unusable on one of our
 * refresh attempts, we want TomE to keep using the last known valid keys.
 *
 * When new keys are successfully returned on one of our refresh attempts, we
 * want to make sure TomEE is not still honoring the old key pair.
 */
public class HttpKeyRotationNoValidKeysTest {

    @Test
    public void test() throws Exception {

        final TomEE keyServer = TomEE.microprofile()
                .add("webapps/ROOT/WEB-INF/beans.xml", "")
                .add("webapps/ROOT/WEB-INF/lib/app.jar", Archive.archive()
                        .add(HttpKeyRotationNoValidKeysTest.class)
                        .add(KeyServer.KeysService.class)
                        .add(KeyServer.class)
                        .add(Tokens.class)
                        .asJar())
                .add("webapps/ROOT/WEB-INF/lib/jose.jar", JarLocation.jarLocation(JWSSigner.class))
                .build();

        final TomEE tomee = TomEE.microprofile()
                .add("webapps/ROOT/WEB-INF/beans.xml", "")
                .add("webapps/ROOT/WEB-INF/lib/app.jar", Archive.archive()
                        .add(HttpKeyRotationNoValidKeysTest.class)
                        .add(MicroProfileWebApp.class)
                        .add(MicroProfileWebApp.ColorService.class)
                        .add(MicroProfileWebApp.Api.class)
                        .add(KeyServer.class)
                        .add("META-INF/microprofile-config.properties", new PublicKeyLocation()
                                .initialRetryDelay(500, TimeUnit.MILLISECONDS)
                                .accessTimeout(10, TimeUnit.SECONDS)
                                .refreshInterval(1, TimeUnit.SECONDS)
                                .location(keyServer.toURI().resolve("/keys/publicKey"))
                                .build())
                        .toJar())
                .build();


        /*
         * GET /keys/calls
         */
        Callable<Integer> getPublicKeyCalls = () -> Integer.parseInt(IO.slurp(keyServer.toURI().resolve("/keys/calls").toURL()));

        /*
         * GET /keys/privateKey
         */
        Callable<Tokens> getPrivateKey = () -> {
            final String privateKey = IO.slurp(keyServer.toURI().resolve("/keys/privateKey").toURL());
            return Tokens.fromPrivateKey(privateKey);
        };

        /*
         * GET /keys/rotate
         */
        Callable<Tokens> rotate = () -> {
            final String privateKey = IO.slurp(keyServer.toURI().resolve("/keys/rotate").toURL());
            return Tokens.fromPrivateKey(privateKey);
        };

        /*
         * Wait for a few failed calls to the key server
         */
        while (getPublicKeyCalls.call() < 3) {
            Thread.sleep(100);
        }

        /*
         * Make sure we can still call our service
         */
        final Tokens unknownKey = Tokens.rsa(2048, 256);
        final Tokens firstKey = getPrivateKey.call();
        
        Runner.threads(100).run(() -> assertKeys(tomee, unknownKey, firstKey)).assertNoExceptions();

        /*
         * Rotate the private key
         */
        final Tokens secondKey = rotate.call();

        /*
         * Wait for one successful call and some failed calls to the key server
         */
        while (getPublicKeyCalls.call() < 3) {
            Thread.sleep(100);
        }

        /*
         * Make sure the new key works and the first key no longer works
         */
        Runner.threads(100).run(() -> assertKeys(tomee, firstKey, secondKey)).assertNoExceptions();
    }

    private void assertKeys(final TomEE tomee, final Tokens invalidKey, final Tokens validKey) {
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
                    .header("Authorization", "Bearer " + invalidKey.asToken(claims))
                    .get();
            assertEquals(401, response.getStatus());
        }
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

            private final AtomicReference<Tokens> tokens = new AtomicReference<>(Tokens.rsa(2048, 256));
            private final AtomicInteger calls = new AtomicInteger();

            @GET
            @Path("publicKey")
            public String publicKey() throws Exception {
                /*
                 * Return a valid public key on the first call (initialization)
                 * After that return only private keys
                 */
                if (calls.getAndIncrement() > 0) {
                    return tokens.get().getJwksPrivateKey();
                }
                return tokens.get().getPemPublicKey();
            }

            @GET
            @Path("privateKey")
            public String privateKey() {
                return tokens.get().getPemPrivateKey();
            }

            @GET
            @Path("rotate")
            public String rotatePrivateKey() {
                final Tokens tokens = Tokens.rsa(2048, 256);
                this.tokens.set(tokens);
                calls.set(0);
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

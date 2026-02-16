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
import org.apache.tomee.itest.common.Logging;
import org.apache.tomee.itest.util.Runner;
import org.apache.tomee.microprofile.jwt.itest.Tokens;
import org.apache.tomee.microprofile.jwt.itest.keys.PublicKeyLocation;
import org.apache.tomee.server.composer.Archive;
import org.apache.tomee.server.composer.TomEE;
import org.eclipse.microprofile.auth.LoginConfig;
import org.junit.Test;
import org.tomitribe.util.Longs;

import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

/**
 * Here we test the behavior of TomEE when keys are rotated, both successfully
 * and unsuccessfully because of HTTP 500s.
 *
 * In scenarios where the key server returns an HTTP 500 on one of our refresh
 * attempts, we want TomE to keep using the last known valid keys.
 *
 * When new keys are successfully returned on one of our refresh attempts, we
 * want to make sure TomEE is not still honoring the old key pair.
 */
public class HttpKeyRotationHttp500Test {

    @Test
    public void test() throws Exception {

        final TomEE keyServer = TomEE.microprofile()
                .add("webapps/ROOT/WEB-INF/beans.xml", "")
                .add("webapps/ROOT/WEB-INF/lib/app.jar", Archive.archive()
                        .add(HttpKeyRotationHttp500Test.class)
                        .add(KeyServer.KeysService.class)
                        .add(KeyServer.class)
                        .add(Tokens.class)
                        .asJar())
                .add("webapps/ROOT/WEB-INF/lib/jose.jar", JarLocation.jarLocation(JWSSigner.class))
                .build();

        final Logging logging = new Logging();
        final TomEE tomee = TomEE.microprofile()
                .and(logging::install)
                .add("webapps/ROOT/WEB-INF/beans.xml", "")
                .add("webapps/ROOT/WEB-INF/lib/app.jar", Archive.archive()
                        .add(HttpKeyRotationHttp500Test.class)
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

        /*
         * Stop responding to publicKey requests so we can reliably assert logging
         */
        IO.slurp(keyServer.toURI().resolve("/keys/block").toURL());

        /*
         * How many times did we request the public key?  That should be the total log lines
         */
        final int total = Integer.parseInt(IO.slurp(keyServer.toURI().resolve("/keys/total").toURL()));

        logging.assertPresent(2, " INFO .* Key Server returned HTTP 200: http://localhost:[0-9]+/keys/publicKey," +
                        " text/plain, [0-9]+ bytes, [0-9]+ ms")
                .assertPresent(total - 2, "Key Server returned HTTP 500: http://localhost:[0-9]+/keys/publicKey, [0-9]+ ms")
                .assertPresent(total - 2, "Unexpected HTTP response: 500")
                .assertPresent(total - 2, "Refresh failed. Supplier PublicKeys\\{location=http://localhost:[0-9]+/keys/publicKey\\} " +
                        "threw an exception.  Next refresh will be in 1 SECONDS")
        ;
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
            private final AtomicInteger total = new AtomicInteger();
            private final Semaphore semaphore = new Semaphore(100);

            @GET
            @Path("publicKey")
            public String publicKey() throws Exception {
                semaphore.acquire();
                total.incrementAndGet();
                /*
                 * Return a valid public key on the first call (initialization)
                 * After that return an HTTP 500 on every cal (each refresh attempt fails)
                 */
                if (calls.getAndIncrement() > 0) {
                    throw new WebApplicationException(500);
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
            @Path("block")
            public void block() {
                semaphore.drainPermits();
            }

            @GET
            @Path("calls")
            public int calls() {
                return calls.get();
            }

            @GET
            @Path("total")
            public int total() {
                return total.get();
            }
        }
    }
}

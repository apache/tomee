/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.bookstore;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import net.minidev.json.JSONObject;
import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.johnzon.jaxrs.JohnzonProvider;
import org.eclipse.microprofile.jwt.Claims;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.superbiz.bookstore.model.Book;

import jakarta.ws.rs.core.Response;
import java.net.URL;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.superbiz.bookstore.TokenUtils.readPrivateKey;

@RunWith(Arquillian.class)
public class BookstoreTest {

    private final static Logger LOGGER = Logger.getLogger(BookstoreTest.class.getName());

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        final WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "test.war")
                .addPackages(true, ApplicationConfig.class.getPackage())
                .addAsResource("META-INF/microprofile-config.properties");

        System.out.println("Deployment: " + webArchive.toString(true));

        return webArchive;
    }

    @ArquillianResource
    private URL base;

    @Test
    public void movieRestTest() throws Exception {
        final WebClient webClient = WebClient
                .create(base.toExternalForm(), singletonList(new JohnzonProvider<>()),
                        singletonList(new LoggingFeature()), null);


        // Testing REST endpoint that returns the value of a JWT claim
        String responsePayload = webClient.reset()
                .path("/rest/bookstore/me")
                .header("Authorization", "Bearer " + token(true))
                .get(String.class);
        LOGGER.info("responsePayload = " + responsePayload);
        assertEquals("alice", responsePayload);


        // Testing REST endpoint with group claims manager
        Book newBook = new Book(1, "The Lord of the Rings", "J.R.R.Tolkien");
        Response response = webClient.reset()
                .path("/rest/bookstore")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token(true))
                .post(newBook);
        LOGGER.info("responseCode = " + response.getStatus());
        assertEquals(204, response.getStatus());

        // Testing REST endpoint with group claims reader
        Collection<? extends Book> books = webClient
                .reset()
                .path("/rest/bookstore")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token(false))
                .getCollection(Book.class);
        LOGGER.info(books.toString());
        assertEquals(1, books.size());


        // Should return a 403 since POST requires group claim manager but provided token has only reader.
        Book secondBook = new Book(2, "Mistborn: The Final Empire", "Brandon Sanderson");
        Response responseWithError = webClient.reset()
                .path("/rest/bookstore")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token(false))
                .post(secondBook);
        LOGGER.info("responseCode = " + responseWithError.getStatus());
        assertEquals(403, responseWithError.getStatus());


        // Should return a 401 since the POST request lacks the Authorization header
        Response responseWith401Error = webClient.reset()
                .path("/rest/bookstore")
                .header("Content-Type", "application/json")
                .post(new Book());
        LOGGER.info("responseCode = " + responseWith401Error.getStatus());
        assertEquals(401, responseWith401Error.getStatus());
    }

    private String token(boolean managerUser) {
        JSONObject claims = new JSONObject();

        claims.put(Claims.iss.name(), "https://server.example.com");
        claims.put(Claims.upn.name(), managerUser ? "alice@example.com" : "bob@exmaple.com");
        long currentTimeInSecs = System.currentTimeMillis() / 1000;
        claims.put(Claims.iat.name(), currentTimeInSecs);
        claims.put(Claims.auth_time.name(), currentTimeInSecs);
        claims.put(Claims.exp.name(), currentTimeInSecs + 300);
        claims.put(Claims.jti.name(), "a-123");
        claims.put(Claims.sub.name(), "24400320");
        claims.put(Claims.preferred_username.name(), managerUser ? "alice" : "bob");
        claims.put(Claims.aud.name(), "s6BhdRkqt3");
        List<String> groups = new ArrayList<>();
        if (managerUser) {
            groups.add("manager");
            groups.add("reader");
        } else {
            groups.add("reader");
        }
        claims.put(Claims.groups.name(), groups);

        try {
            PrivateKey pk = readPrivateKey("/privateKey.pem");
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .keyID("/privateKey.pem")
                    .type(JOSEObjectType.JWT)
                    .build();

            JWTClaimsSet claimsSet = JWTClaimsSet.parse(claims);
            SignedJWT jwt = new SignedJWT(header, claimsSet);
            jwt.sign(new RSASSASigner(pk));
            return jwt.serialize();
        } catch (Exception e) {
            throw new RuntimeException("Could not sign JWT");
        }
    }
}

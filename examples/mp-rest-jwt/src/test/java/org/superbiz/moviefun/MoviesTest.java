/**
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
package org.superbiz.moviefun;

import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.johnzon.jaxrs.JohnzonProvider;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.superbiz.moviefun.rest.ApplicationConfig;
import org.superbiz.moviefun.rest.MoviesMPJWTConfigurationProvider;
import org.superbiz.moviefun.rest.MoviesRest;

import jakarta.ws.rs.core.Response;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Logger;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class MoviesTest {

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        final WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "test.war")
                                                .addClasses(Movie.class, MoviesBean.class, MoviesTest.class)
                                                .addClasses(MoviesRest.class, ApplicationConfig.class)
                                                .addClass(MoviesMPJWTConfigurationProvider.class)
                                                .addAsWebInfResource(new StringAsset("<beans/>"), "beans.xml");

        System.out.println(webArchive.toString(true));

        return webArchive;
    }

    @ArquillianResource
    private URL base;


    private final static Logger LOGGER = Logger.getLogger(MoviesTest.class.getName());

    @Test
    public void movieRestTest() throws Exception {

        final WebClient webClient = WebClient
                .create(base.toExternalForm(), singletonList(new JohnzonProvider<>()),
                        singletonList(new LoggingFeature()), null);


        //Testing rest endpoint deployment (GET  without security header)
        String responsePayload = webClient.reset().path("/rest/cinema/").get(String.class);
        LOGGER.info("responsePayload = " + responsePayload);
        assertTrue(responsePayload.equalsIgnoreCase("ok"));


        //POST (Using token1.json with group of claims: [CRUD])
        Movie newMovie = new Movie(1, "David Dobkin", "Wedding Crashers");
        Response response = webClient.reset()
                                     .path("/rest/cinema/movies")
                                     .header("Content-Type", "application/json")
                                     .header("Authorization", "Bearer " + token(1))
                                     .post(newMovie);
        LOGGER.info("responseCode = " + response.getStatus());
        assertTrue(response.getStatus() == 204);


        //GET movies (Using token2.json with group of claims: [read-only])
        Collection<? extends Movie> movies = webClient
                .reset()
                .path("/rest/cinema/movies")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token(2))
                .getCollection(Movie.class);
        LOGGER.info(movies.toString());
        assertTrue(movies.size() == 1);


        //Should return a 403 since POST require group of claims: [crud] but Token 2 has only [read-only].
        Movie secondNewMovie = new Movie(2, "Todd Phillips", "Starsky & Hutch");
        Response responseWithError = webClient.reset()
                                              .path("/rest/cinema/movies")
                                              .header("Content-Type", "application/json")
                                              .header("Authorization", "Bearer " + token(2))
                                              .post(secondNewMovie);
        LOGGER.info("responseCode = " + responseWithError.getStatus());
        assertTrue(responseWithError.getStatus() == 403);


        //Should return a 401 since the header Authorization is not part of the POST request.
        Response responseWith401Error = webClient.reset()
                                                 .path("/rest/cinema/movies")
                                                 .header("Content-Type", "application/json")
                                                 .post(new Movie());
        LOGGER.info("responseCode = " + responseWith401Error.getStatus());
        assertTrue(responseWith401Error.getStatus() == 401);

    }


    private String token(int token_type) throws Exception {
        HashMap<String, Long> timeClaims = new HashMap<>();
        if (token_type == 1) {
            return TokenUtils.generateTokenString("/Token1.json", null, timeClaims);
        } else {
            return TokenUtils.generateTokenString("/Token2.json", null, timeClaims);
        }
    }

}

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
package org.superbiz.movie;

import org.apache.tomee.bootstrap.Archive;
import org.apache.tomee.bootstrap.Server;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.net.URI;

import static javax.ws.rs.client.Entity.entity;
import static org.junit.Assert.assertEquals;

public class MovieServiceTest {

    private static URI serverURI;

    @BeforeClass
    public static void setup() {
        // Add any classes you need to an Archive
        // or add them to a jar via any means
        final Archive classes = Archive.archive()
                .add(Api.class)
                .add(Movie.class)
                .add(MovieService.class);

        // Place the classes where you would want
        // them in a Tomcat install
        final Server server = Server.builder()
                // This effectively creates a webapp called ROOT
                .add("webapps/ROOT/WEB-INF/classes", classes)
                .build();

        serverURI = server.getURI();
    }

    @Test
    public void getAllMovies() {
        final WebTarget target = ClientBuilder.newClient().target(serverURI);

        final Movie[] movies = target.path("/api/movies").request().get(Movie[].class);

        assertEquals(6, movies.length);

        final Movie movie = movies[1];
        assertEquals("Todd Phillips", movie.getDirector());
        assertEquals("Starsky & Hutch", movie.getTitle());
        assertEquals("Action", movie.getGenre());
        assertEquals(2004, movie.getYear());
        assertEquals(2, movie.getId());
    }

    @Test
    public void addMovie() {
        final WebTarget target = ClientBuilder.newClient().target(serverURI);

        final Movie movie = new Movie("Shanghai Noon", "Tom Dey", "Comedy", 7, 2000);

        final Movie posted = target.path("/api/movies").request()
                .post(entity(movie, MediaType.APPLICATION_JSON))
                .readEntity(Movie.class);

        assertEquals("Tom Dey", posted.getDirector());
        assertEquals("Shanghai Noon", posted.getTitle());
        assertEquals("Comedy", posted.getGenre());
        assertEquals(2000, posted.getYear());
        assertEquals(7, posted.getId());
    }
}

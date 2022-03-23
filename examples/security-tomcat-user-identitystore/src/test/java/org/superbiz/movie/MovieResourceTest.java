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

import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import java.io.File;
import java.net.URI;

import static jakarta.ws.rs.client.Entity.entity;
import static org.junit.Assert.assertEquals;

public class MovieResourceTest {

    private static URI serverURI;

    @BeforeClass
    public static void setup() {
        // Add any classes you need to an Archive
        // or add them to a jar via any means
        final Archive classes = Archive.archive()
                .add(Api.class)
                .add(Movie.class)
                .add(MovieStore.class)
                .add(MovieResource.class)
                .add(MovieAdminResource.class);

        // Place the classes where you would want
        // them in a Tomcat install
        final Server server = Server.builder()
                // This effectively creates a webapp called ROOT
                .add("webapps/ROOT/WEB-INF/classes", classes)
                .add("webapps/ROOT/WEB-INF/web.xml", new File("src/main/webapp/WEB-INF/web.xml"))
                .add("conf/tomcat-users.xml", new File("src/main/resources/conf/tomcat-users.xml"))
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
    public void getAllMoviesAuthenticated() {
        final WebTarget target = ClientBuilder.newClient()
                                              .target(serverURI)
                                              .register(new BasicAuthFilter("tomcat", "tomcat"));

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
    public void getMovieAuthenticated() {
        final WebTarget target = ClientBuilder.newClient()
                                              .target(serverURI)
                                              .register(new BasicAuthFilter("bob", "secret3"));

        final Movie movie = target.path("/api/movies/2").request().get(Movie.class);

        assertEquals("Todd Phillips", movie.getDirector());
        assertEquals("Starsky & Hutch", movie.getTitle());
        assertEquals("Action", movie.getGenre());
        assertEquals(2004, movie.getYear());
        assertEquals(2, movie.getId());
    }

    @Test
    public void getAllMoviesEmma() {
        final WebTarget target = ClientBuilder.newClient()
                                              .target(serverURI)
                                              .register(new BasicAuthFilter("emma", "secret2"));

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
    public void addMovieAdmin() {
        final WebTarget target = ClientBuilder.newClient()
                                              .target(serverURI)
                                              .register(new BasicAuthFilter("tom", "secret1"));

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

    @Test
    public void addMovieNotAuthenticated() {
        final WebTarget target = ClientBuilder.newClient()
                                              .target(serverURI);

        final Movie movie = new Movie("Shanghai Noon", "Tom Dey", "Comedy", 7, 2000);

        assertEquals(401, target.path("/api/movies").request()
                                   .post(entity(movie, MediaType.APPLICATION_JSON)).getStatus());

    }

    @Test
    public void addMovieWrongPermission() {
        final WebTarget target = ClientBuilder.newClient()
                                              .target(serverURI)
                                              .register(new BasicAuthFilter("tomcat", "tomcat"));

        final Movie movie = new Movie("Shanghai Noon", "Tom Dey", "Comedy", 7, 2000);

        assertEquals(403, target.path("/api/movies").request()
                                .post(entity(movie, MediaType.APPLICATION_JSON)).getStatus());

    }
}

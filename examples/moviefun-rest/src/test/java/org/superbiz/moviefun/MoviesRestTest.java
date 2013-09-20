/**
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
package org.superbiz.moviefun;

import org.apache.tomee.embedded.EmbeddedTomEEContainer;
import org.apache.ziplock.Archive;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ejb.embeddable.EJBContainer;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import java.io.File;
import java.util.List;
import java.util.Properties;

import static org.apache.openejb.loader.JarLocation.jarLocation;

public class MoviesRestTest {

    private static EJBContainer ejbContainer;

    @BeforeClass
    public static void setUp() throws Exception {
        final File webApp = Archive.archive().copyTo("WEB-INF/classes", jarLocation(MoviesBean.class)).asDir();
        final Properties p = new Properties();
        p.setProperty(EJBContainer.APP_NAME, "moviefun-rest");
        p.setProperty(EJBContainer.PROVIDER, "tomee-embedded"); // need web feature
        p.setProperty(EJBContainer.MODULES, webApp.getAbsolutePath());
        p.setProperty(EmbeddedTomEEContainer.TOMEE_EJBCONTAINER_HTTP_PORT, "-1"); // random port
        ejbContainer = EJBContainer.createEJBContainer(p);
    }

    @AfterClass
    public static void tearDown() {
        if (ejbContainer != null) {
            ejbContainer.close();
        }
    }

//    @Before
    @After
    public void clean() throws Exception {
        Thread.sleep(100000);
        MoviesBean movies = (MoviesBean) ejbContainer.getContext().lookup("java:global/moviefun-rest/MoviesBean");
        movies.clean();
    }

    @Test
    public void testShouldAddAMovie() throws Exception {
        MoviesBean movies = (MoviesBean) ejbContainer.getContext().lookup("java:global/moviefun-rest/MoviesBean");

        Movie movie = new Movie();
        movie.setDirector("Michael Bay");
        movie.setGenre("Action");
        movie.setRating(9);
        movie.setTitle("Bad Boys");
        movie.setYear(1995);
        movies.addMovie(movie);

        Assert.assertEquals(1, movies.countAll());
        List<Movie> moviesFound = movies.findRange("title", "Bad Boys", 0, 10);

        Assert.assertEquals(1, moviesFound.size());
        Assert.assertEquals("Michael Bay", moviesFound.get(0).getDirector());
        Assert.assertEquals("Action", moviesFound.get(0).getGenre());
        Assert.assertEquals(9, moviesFound.get(0).getRating());
        Assert.assertEquals("Bad Boys", moviesFound.get(0).getTitle());
        Assert.assertEquals(1995, moviesFound.get(0).getYear());

        movies.addMovie(new Movie("Quentin Tarantino", "Reservoir Dogs", 1992));
        movies.addMovie(new Movie("Joel Coen", "Fargo", 1996));
        movies.addMovie(new Movie("Joel Coen", "The Big Lebowski", 1998));


    }

    public static interface MoviesClient {
        @GET()
        @javax.ws.rs.Path("{id}")
        public Movie find(@PathParam("id") Long id);

        @POST
        @javax.ws.rs.Path("create")
        public void addMovie(Movie movie);

        @PUT
        @javax.ws.rs.Path("edit")
        public void editMovie(Movie movie);

        @DELETE
        @javax.ws.rs.Path("delete/{id}")
        public void deleteMovieId(@PathParam("id") long id);

        @GET
        @javax.ws.rs.Path("list")
        public List<Movie> getMovies();

        @GET()
        @javax.ws.rs.Path("list/{first}/{max}")
        public List<Movie> findAll(@PathParam("first") int firstResult, @PathParam("max") int maxResults);

        @GET()
        @javax.ws.rs.Path("count")
        public int countAll() ;

        @GET()
        @javax.ws.rs.Path("count/{field}/{searchTerm}")
        public int count(@PathParam("field") String field, @PathParam("searchTerm") String searchTerm);

        @GET()
        @javax.ws.rs.Path("list/{field}/{searchTerm}/{first}/{max}")
        public List<Movie> findRange(String field, String searchTerm, int firstResult, int maxResults) ;
    }
}
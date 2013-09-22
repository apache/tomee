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
import org.junit.*;

import javax.ejb.embeddable.EJBContainer;
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

    @Before
    @After
    public void clean() throws Exception {
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

        Assert.assertEquals(1, movies.count(null, null));
        List<Movie> moviesFound = movies.getMovies(0, 10, "title", "Bad Boys");

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
}
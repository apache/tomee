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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ejb.embeddable.EJBContainer;
import java.util.List;

public class MoviesEmbeddedEJBTest {

    private static EJBContainer ejbContainer;

    @BeforeClass
    public static void setUp() throws Exception {
        ejbContainer = EJBContainer.createEJBContainer();
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
        MoviesBean movies = (MoviesBean) ejbContainer.getContext().lookup("java:global/moviefun/MoviesBean!org.superbiz.moviefun.MoviesBean");
        movies.clean();
    }

    @Test
    public void testShouldAddAMovie() throws Exception {
        MoviesBean movies = (MoviesBean) ejbContainer.getContext().lookup("java:global/moviefun/MoviesBean!org.superbiz.moviefun.MoviesBean");

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
    }

}
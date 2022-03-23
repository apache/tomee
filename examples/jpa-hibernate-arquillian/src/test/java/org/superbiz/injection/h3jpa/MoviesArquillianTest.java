/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.injection.h3jpa;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.EJB;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(Arquillian.class)
public class MoviesArquillianTest {

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addClasses(Movie.class, Movies.class, MoviesArquillianTest.class)
                .addAsResource(new ClassLoaderAsset("META-INF/persistence.xml"), "META-INF/persistence.xml");
    }

    @EJB
    private Movies movies;

    @Test
    public void shouldBeAbleToAddAMovie() throws Exception {

        assertNotNull("Verify that the ejb was injected", movies);

        //Insert a movie
        final Movie movie = new Movie();
        movie.setDirector("Michael Bay");
        movie.setTitle("Bad Boys");
        movie.setYear(1995);
        movies.addMovie(movie);

        //Count movies
        assertEquals(1, movies.count("title", "a"));

        //Insert a movie
        movies.addMovie(new Movie("David Dobkin", "Wedding Crashers", 2005));

        //Get movies
        assertEquals(2, movies.getMovies().size());

        //Delete
        movies.deleteMovie(movie);

        //Get movies
        assertEquals(2005, movies.getMovies().get(0).getYear());
    }

}
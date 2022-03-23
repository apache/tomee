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

import java.util.List;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.superbiz.moviefun.setup.ExampleDataProducer;
import org.superbiz.moviefun.setup.Examples;
import org.superbiz.moviefun.setup.Setup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(Arquillian.class)
public class MoviesEJBTest {
	@Deployment
    public static WebArchive createDeployment() {
		return ShrinkWrap.create(WebArchive.class, "test.war")
                .addClasses(Movie.class, MoviesImpl.class, Movies.class, MoviesRemote.class, MoviesEJBTest.class, Setup.class, Examples.class, ExampleDataProducer.class)
                .addAsResource(new ClassLoaderAsset("META-INF/ejb-jar.xml"), "META-INF/ejb-jar.xml")
                .addAsResource(new ClassLoaderAsset("META-INF/persistence.xml"), "META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
	}

	@EJB
    private Movies movies;

	@Inject
    private Setup setup;

    @Before
    @After
    public void clean() {
        movies.clean();
    }

	@Test
    public void shouldBeAbleToAddAMovie() throws Exception {
		assertNotNull("Verify that the ejb was injected", movies);
		assertNotNull("Verify that the setup CDI bean was injected", setup);
		
		setup.setup();
		
		assertEquals(7, movies.getMovies().size());

		Movie movie = new Movie();
		movie.setDirector("Michael Bay");
		movie.setGenre("Action");
		movie.setRating(9);
		movie.setTitle("Bad Boys");
		movie.setYear(1995);
		movies.addMovie(movie);
		
		assertEquals(8, movies.count());
		List<Movie> moviesFound = movies.findByTitle("Bad Boys");
		
		assertEquals(1, moviesFound.size());
		assertEquals("Michael Bay", moviesFound.get(0).getDirector());
		assertEquals("Action", moviesFound.get(0).getGenre());
		assertEquals(9, moviesFound.get(0).getRating());
		assertEquals("Bad Boys", moviesFound.get(0).getTitle());
		assertEquals(1995, moviesFound.get(0).getYear());
	}

}

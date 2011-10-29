package org.superbiz.moviefun;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import javax.ejb.EJB;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.impl.base.asset.ClassLoaderAsset;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(Arquillian.class)
public class MoviesEJBTest {
	@Deployment public static JavaArchive createDeployment() {
		// explicit archive name required until ARQ-77 is resolved
		return ShrinkWrap.create(JavaArchive.class, "test.jar").addClasses(Movie.class, MoviesImpl.class, Movies.class, MoviesRemote.class, MoviesEJBTest.class)
				.addAsResource(new ClassLoaderAsset("META-INF/ejb-jar.xml") , "META-INF/ejb-jar.xml")
        		.addAsResource(new ClassLoaderAsset("META-INF/persistence.xml") , "META-INF/persistence.xml");
	}

	@EJB private Movies movies;

    @Before @After public void clean() {
        movies.clean();
    }

	@Test public void shouldBeAbleToAddAMovie() throws Exception {
		assertNotNull("Verify that the ejb was injected", movies);

		Movie movie = new Movie();
		movie.setDirector("Michael Bay");
		movie.setGenre("Action");
		movie.setRating(9);
		movie.setTitle("Bad Boys");
		movie.setYear(1995);
		movies.addMovie(movie);
		
		assertEquals(1, movies.count());
		List<Movie> moviesFound = movies.findByTitle("Bad Boys");
		
		assertEquals(1, moviesFound.size());
		assertEquals("Michael Bay", moviesFound.get(0).getDirector());
		assertEquals("Action", moviesFound.get(0).getGenre());
		assertEquals(9, moviesFound.get(0).getRating());
		assertEquals("Bad Boys", moviesFound.get(0).getTitle());
		assertEquals(1995, moviesFound.get(0).getYear());
	}

}

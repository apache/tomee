package org.superbiz.moviefun;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ejb.embeddable.EJBContainer;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MoviesTest {
    private static EJBContainer ejbContainer;
	private static MoviesRemote movies;

    @BeforeClass public static void setUp() throws Exception {
        ejbContainer = EJBContainer.createEJBContainer();
        Object object = ejbContainer.getContext().lookup("java:global/arquillian-tomee-moviefun-example/Movies!org.superbiz.moviefun.MoviesRemote");

        assertTrue(object instanceof MoviesRemote);
        movies = (MoviesRemote) object;
    }

    @AfterClass public static void tearDown() {
        if (ejbContainer != null) {
            ejbContainer.close();
        }
    }

    @Before @After public void clean() {
        movies.clean();
    }

	@Test public void testShouldAddAMovie() throws Exception {
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

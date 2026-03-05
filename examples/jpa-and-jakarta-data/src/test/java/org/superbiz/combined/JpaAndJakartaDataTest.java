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
package org.superbiz.combined;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.transaction.UserTransaction;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Demonstrates using classic JPA (EntityManager) and Jakarta Data
 * repositories side by side, sharing the same persistence unit.
 *
 * <p>Data inserted via one approach is visible to the other within
 * the same transaction, proving seamless coexistence.
 */
@RunWith(ApplicationComposer.class)
public class JpaAndJakartaDataTest {

    @EJB
    private MovieDao movieDao;

    @Inject
    private MovieRepository movieRepository;

    @Inject
    private MovieService movieService;

    @Resource
    private UserTransaction utx;

    @Module
    @Classes(cdi = true, value = {MovieRepository.class, MovieService.class})
    public EjbJar beans() {
        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean(MovieDao.class));
        return ejbJar;
    }

    @Module
    public PersistenceUnit persistence() {
        final PersistenceUnit unit = new PersistenceUnit("movie-unit");
        unit.setJtaDataSource("movieDatabase");
        unit.setNonJtaDataSource("movieDatabaseUnmanaged");
        unit.getClazz().add(Movie.class.getName());
        unit.setProperty("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
        return unit;
    }

    @Configuration
    public Properties config() {
        final Properties p = new Properties();
        p.put("movieDatabase", "new://Resource?type=DataSource");
        p.put("movieDatabase.JdbcDriver", "org.hsqldb.jdbcDriver");
        p.put("movieDatabase.JdbcUrl", "jdbc:hsqldb:mem:moviedb-combined");
        return p;
    }

    // -- Both sides injected --

    @Test
    public void testBothInjected() {
        assertNotNull("Classic JPA DAO should be injected", movieDao);
        assertNotNull("Jakarta Data repository should be injected", movieRepository);
        assertNotNull("MovieService should be injected", movieService);
    }

    // -- Classic JPA EntityManager operations --

    @Test
    public void testClassicJpaInsertAndFind() throws Exception {
        utx.begin();
        try {
            movieDao.addMovie(new Movie("Joel Coen", "Fargo", 1996, Genre.THRILLER));
            movieDao.addMovie(new Movie("Joel Coen", "The Big Lebowski", 1998, Genre.COMEDY));

            final List<Movie> all = movieDao.findAll();
            assertEquals(2, all.size());
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testClassicJpaCriteriaQuery() throws Exception {
        utx.begin();
        try {
            movieDao.addMovie(new Movie("Joel Coen", "Fargo", 1996, Genre.THRILLER));
            movieDao.addMovie(new Movie("Joel Coen", "The Big Lebowski", 1998, Genre.COMEDY));
            movieDao.addMovie(new Movie("Joel Coen", "No Country for Old Men", 2007, Genre.THRILLER));

            final List<Movie> thrillers = movieDao.findByDirectorAndGenre("Joel Coen", Genre.THRILLER);
            assertEquals(2, thrillers.size());
            assertEquals("Fargo", thrillers.get(0).getTitle());
            assertEquals("No Country for Old Men", thrillers.get(1).getTitle());
        } finally {
            utx.rollback();
        }
    }

    // -- Jakarta Data repository operations --

    @Test
    public void testDataRepositoryInsertAndFind() throws Exception {
        utx.begin();
        try {
            final Movie movie = movieRepository.insert(
                new Movie("Quentin Tarantino", "Pulp Fiction", 1994, Genre.THRILLER));
            assertNotNull(movie.getId());

            final Optional<Movie> found = movieRepository.findById(movie.getId());
            assertTrue(found.isPresent());
            assertEquals("Pulp Fiction", found.get().getTitle());
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testDataRepositoryMethodNameQuery() throws Exception {
        utx.begin();
        try {
            movieRepository.insert(new Movie("Joel Coen", "Fargo", 1996, Genre.THRILLER));
            movieRepository.insert(new Movie("Quentin Tarantino", "Pulp Fiction", 1994, Genre.THRILLER));
            movieRepository.insert(new Movie("Joel Coen", "The Big Lebowski", 1998, Genre.COMEDY));

            final List<Movie> thrillers = movieRepository.findByGenre(Genre.THRILLER);
            assertEquals(2, thrillers.size());

            assertEquals(2, movieRepository.countByDirector("Joel Coen"));
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testDataRepositoryQueryAnnotation() throws Exception {
        utx.begin();
        try {
            movieRepository.insert(new Movie("Joel Coen", "Blood Simple", 1984, Genre.THRILLER));
            movieRepository.insert(new Movie("Joel Coen", "Fargo", 1996, Genre.THRILLER));
            movieRepository.insert(new Movie("Joel Coen", "The Big Lebowski", 1998, Genre.COMEDY));
            movieRepository.insert(new Movie("Joel Coen", "No Country for Old Men", 2007, Genre.THRILLER));

            final List<Movie> ninetiesThrillers =
                movieRepository.findByGenreAndYearRange(Genre.THRILLER, 1990, 2000);
            assertEquals(1, ninetiesThrillers.size());
            assertEquals("Fargo", ninetiesThrillers.get(0).getTitle());
        } finally {
            utx.rollback();
        }
    }

    // -- Cross-approach visibility: data flows between JPA and Jakarta Data --

    @Test
    public void testJpaInsertVisibleViaDataRepository() throws Exception {
        utx.begin();
        try {
            // Insert using classic JPA EntityManager
            movieDao.addMovie(new Movie("Ridley Scott", "Blade Runner", 1982, Genre.SCI_FI));
            movieDao.addMovie(new Movie("Ridley Scott", "Alien", 1979, Genre.SCI_FI));
            movieDao.addMovie(new Movie("Ridley Scott", "Gladiator", 2000, Genre.ACTION));

            // Query using Jakarta Data repository — should see the JPA-persisted data
            final List<Movie> sciFi = movieRepository.findByGenre(Genre.SCI_FI);
            assertEquals("Jakarta Data should see JPA-inserted sci-fi movies", 2, sciFi.size());

            assertEquals("Jakarta Data should count JPA-inserted movies",
                3, movieRepository.countByDirector("Ridley Scott"));
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testDataInsertVisibleViaJpaDao() throws Exception {
        utx.begin();
        try {
            // Insert using Jakarta Data repository
            movieRepository.insert(new Movie("Christopher Nolan", "Inception", 2010, Genre.SCI_FI));
            movieRepository.insert(new Movie("Christopher Nolan", "The Dark Knight", 2008, Genre.ACTION));
            movieRepository.insert(new Movie("Christopher Nolan", "Interstellar", 2014, Genre.SCI_FI));

            // Query using classic JPA DAO — should see the Jakarta Data-inserted data
            final List<Movie> all = movieDao.findAll();
            assertEquals("JPA DAO should see Jakarta Data-inserted movies", 3, all.size());

            final List<Movie> sciFi = movieDao.findByGenre(Genre.SCI_FI);
            assertEquals("JPA DAO should find Jakarta Data-inserted sci-fi movies", 2, sciFi.size());
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testMixedInsertAndQuery() throws Exception {
        utx.begin();
        try {
            // Insert some via JPA, some via Jakarta Data
            movieDao.addMovie(new Movie("Denis Villeneuve", "Arrival", 2016, Genre.SCI_FI));
            movieDao.addMovie(new Movie("Denis Villeneuve", "Sicario", 2015, Genre.THRILLER));
            movieRepository.insert(new Movie("Denis Villeneuve", "Dune", 2021, Genre.SCI_FI));
            movieRepository.insert(new Movie("Denis Villeneuve", "Blade Runner 2049", 2017, Genre.SCI_FI));

            // Both JPA and Jakarta Data should see all 4 movies
            assertEquals(4, movieDao.count());
            assertEquals(4, movieRepository.countByDirector("Denis Villeneuve"));

            // Genre queries should work across both
            final List<Movie> sciFi = movieRepository.findByGenre(Genre.SCI_FI);
            assertEquals(3, sciFi.size());

            final List<Movie> thrillers = movieDao.findByGenre(Genre.THRILLER);
            assertEquals(1, thrillers.size());
            assertEquals("Sicario", thrillers.get(0).getTitle());
        } finally {
            utx.rollback();
        }
    }

    // -- Service layer using both approaches --

    @Test
    public void testServiceMixedOperations() throws Exception {
        utx.begin();
        try {
            // Insert via classic JPA
            movieService.addMovieClassic("Martin Scorsese", "Goodfellas", 1990, Genre.DRAMA);
            movieService.addMovieClassic("Martin Scorsese", "Taxi Driver", 1976, Genre.DRAMA);

            // Insert via Jakarta Data
            final Movie departed = movieService.addMovieData("Martin Scorsese", "The Departed", 2006, Genre.THRILLER);

            // Count via classic JPA
            assertEquals(3, movieService.countClassic());

            // Count via Jakarta Data
            assertEquals(3, movieService.countByDirectorViaRepository("Martin Scorsese"));

            // Find via Jakarta Data
            final Optional<Movie> found = movieService.findByIdData(departed.getId());
            assertTrue(found.isPresent());
            assertEquals("The Departed", found.get().getTitle());

            // Find by genre via classic JPA, including Jakarta Data-inserted movie
            final List<Movie> thrillers = movieService.findByGenreViaDao(Genre.THRILLER);
            assertEquals(1, thrillers.size());
            assertEquals("The Departed", thrillers.get(0).getTitle());

            // Find by genre via Jakarta Data, including JPA-inserted movies
            final List<Movie> dramas = movieService.findByGenreData(Genre.DRAMA);
            assertEquals(2, dramas.size());
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testDeleteAcrossApproaches() throws Exception {
        utx.begin();
        try {
            // Insert via Jakarta Data
            final Movie movie = movieRepository.insert(
                new Movie("Stanley Kubrick", "2001: A Space Odyssey", 1968, Genre.SCI_FI));
            final Long id = movie.getId();

            // Verify exists via JPA
            assertNotNull(movieDao.findById(id));

            // Delete via Jakarta Data
            movieRepository.delete(movie);

            // Verify gone via JPA
            // Note: need to flush/clear for the JPA extended persistence context to see the deletion
            assertFalse(movieRepository.findById(id).isPresent());
        } finally {
            utx.rollback();
        }
    }
}

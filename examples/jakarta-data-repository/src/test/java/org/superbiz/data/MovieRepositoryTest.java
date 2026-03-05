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
package org.superbiz.data;

import jakarta.annotation.Resource;
import jakarta.inject.Inject;
import jakarta.transaction.UserTransaction;
import org.apache.openejb.jee.EjbJar;
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
 * Tests for the Jakarta Data MovieRepository.
 *
 * <p>Uses ApplicationComposer to bootstrap an embedded OpenEJB container
 * with CDI, JPA, and the Jakarta Data extension. Each test runs within
 * a JTA transaction that is rolled back afterwards to keep tests isolated.
 */
@RunWith(ApplicationComposer.class)
public class MovieRepositoryTest {

    @Inject
    private MovieRepository movieRepository;

    @Inject
    private MovieService movieService;

    @Resource
    private UserTransaction utx;

    @Module
    @Classes(cdi = true, value = {MovieRepository.class, MovieService.class})
    public EjbJar beans() {
        return new EjbJar();
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
        p.put("movieDatabase.JdbcUrl", "jdbc:hsqldb:mem:moviedb-data");
        return p;
    }

    @Test
    public void testRepositoryInjection() {
        assertNotNull("MovieRepository should be injected via CDI", movieRepository);
    }

    @Test
    public void testServiceInjection() {
        assertNotNull("MovieService should be injected via CDI", movieService);
    }

    @Test
    public void testInsertAndFindById() throws Exception {
        utx.begin();
        try {
            final Movie movie = movieRepository.insert(new Movie("Quentin Tarantino", "Reservoir Dogs", 1992));
            assertNotNull("Inserted movie should have an id", movie.getId());

            final Optional<Movie> found = movieRepository.findById(movie.getId());
            assertTrue("Should find the movie by id", found.isPresent());
            assertEquals("Reservoir Dogs", found.get().getTitle());
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testFindByDirector() throws Exception {
        utx.begin();
        try {
            movieRepository.insert(new Movie("Joel Coen", "Fargo", 1996));
            movieRepository.insert(new Movie("Joel Coen", "The Big Lebowski", 1998));
            movieRepository.insert(new Movie("Quentin Tarantino", "Pulp Fiction", 1994));

            final List<Movie> coenMovies = movieRepository.findByDirector("Joel Coen");
            assertEquals("Joel Coen should have 2 movies", 2, coenMovies.size());

            final List<Movie> tarantinoMovies = movieRepository.findByDirector("Quentin Tarantino");
            assertEquals("Tarantino should have 1 movie", 1, tarantinoMovies.size());
            assertEquals("Pulp Fiction", tarantinoMovies.get(0).getTitle());
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testFindByYearWithOrderBy() throws Exception {
        utx.begin();
        try {
            movieRepository.insert(new Movie("Joel Coen", "Fargo", 1996));
            movieRepository.insert(new Movie("Joel Coen", "Barton Fink", 1991));
            movieRepository.insert(new Movie("Quentin Tarantino", "Pulp Fiction", 1994));

            // findByYear orders by title ascending
            final List<Movie> movies1991 = movieRepository.findByYear(1991);
            assertEquals(1, movies1991.size());
            assertEquals("Barton Fink", movies1991.get(0).getTitle());

            final List<Movie> movies1996 = movieRepository.findByYear(1996);
            assertEquals(1, movies1996.size());
            assertEquals("Fargo", movies1996.get(0).getTitle());
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testQueryAnnotation() throws Exception {
        utx.begin();
        try {
            movieRepository.insert(new Movie("Joel Coen", "Blood Simple", 1984));
            movieRepository.insert(new Movie("Joel Coen", "Fargo", 1996));
            movieRepository.insert(new Movie("Joel Coen", "The Big Lebowski", 1998));
            movieRepository.insert(new Movie("Joel Coen", "No Country for Old Men", 2007));

            final List<Movie> nineties = movieRepository.findByDirectorAndYearRange("Joel Coen", 1990, 2000);
            assertEquals("Joel Coen had 2 movies in the 90s", 2, nineties.size());
            assertEquals("Fargo", nineties.get(0).getTitle());
            assertEquals("The Big Lebowski", nineties.get(1).getTitle());
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testCountByDirector() throws Exception {
        utx.begin();
        try {
            movieRepository.insert(new Movie("Joel Coen", "Fargo", 1996));
            movieRepository.insert(new Movie("Joel Coen", "The Big Lebowski", 1998));
            movieRepository.insert(new Movie("Quentin Tarantino", "Pulp Fiction", 1994));

            assertEquals(2, movieRepository.countByDirector("Joel Coen"));
            assertEquals(1, movieRepository.countByDirector("Quentin Tarantino"));
            assertEquals(0, movieRepository.countByDirector("Steven Spielberg"));
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testDeleteMovie() throws Exception {
        utx.begin();
        try {
            final Movie movie = movieRepository.insert(new Movie("Joel Coen", "Fargo", 1996));
            final Long id = movie.getId();
            assertTrue(movieRepository.findById(id).isPresent());

            movieRepository.delete(movie);
            assertFalse("Movie should be deleted", movieRepository.findById(id).isPresent());
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testServiceLayer() throws Exception {
        utx.begin();
        try {
            final Movie movie = movieService.addMovie("Joel Coen", "Fargo", 1996);
            assertNotNull(movie.getId());

            final Optional<Movie> found = movieService.findById(movie.getId());
            assertTrue(found.isPresent());

            assertEquals(1, movieService.countByDirector("Joel Coen"));
        } finally {
            utx.rollback();
        }
    }
}

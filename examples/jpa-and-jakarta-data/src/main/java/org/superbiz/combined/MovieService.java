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

import jakarta.ejb.EJB;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;

/**
 * Application service that uses BOTH classic JPA (via {@link MovieDao})
 * and Jakarta Data (via {@link MovieRepository}) in the same class.
 *
 * <p>This demonstrates a realistic migration scenario: existing code
 * uses the classic JPA DAO, while new features are added using the
 * simpler Jakarta Data repository. Both share the same persistence
 * unit and see each other's data within the same transaction.
 */
@ApplicationScoped
public class MovieService {

    @EJB
    private MovieDao movieDao;

    @Inject
    private MovieRepository movieRepository;

    // -- Classic JPA operations (via MovieDao) --

    /**
     * Add a movie using the classic JPA EntityManager approach.
     */
    public void addMovieClassic(final String director, final String title, final int year, final Genre genre) {
        movieDao.addMovie(new Movie(director, title, year, genre));
    }

    /**
     * Uses the JPA Criteria API via the classic DAO for a dynamic query.
     */
    public List<Movie> findByDirectorAndGenreClassic(final String director, final Genre genre) {
        return movieDao.findByDirectorAndGenre(director, genre);
    }

    public List<Movie> findAllClassic() {
        return movieDao.findAll();
    }

    public long countClassic() {
        return movieDao.count();
    }

    // -- Jakarta Data operations (via MovieRepository) --

    /**
     * Add a movie using the Jakarta Data repository.
     */
    public Movie addMovieData(final String director, final String title, final int year, final Genre genre) {
        return movieRepository.insert(new Movie(director, title, year, genre));
    }

    public Optional<Movie> findByIdData(final long id) {
        return movieRepository.findById(id);
    }

    public List<Movie> findByGenreData(final Genre genre) {
        return movieRepository.findByGenre(genre);
    }

    public long countByGenreData(final Genre genre) {
        return movieRepository.countByGenre(genre);
    }

    // -- Mixed: data inserted via JPA, queried via Jakarta Data (and vice versa) --

    /**
     * Demonstrates that data persisted via the classic EntityManager
     * is visible through the Jakarta Data repository within the same transaction.
     */
    public long countByDirectorViaRepository(final String director) {
        return movieRepository.countByDirector(director);
    }

    /**
     * Demonstrates that data inserted via Jakarta Data repository
     * is visible through the classic JPA DAO.
     */
    public List<Movie> findByGenreViaDao(final Genre genre) {
        return movieDao.findByGenre(genre);
    }
}

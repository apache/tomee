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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;

/**
 * Application service demonstrating Jakarta Data repository injection.
 *
 * <p>The {@link MovieRepository} is injected via CDI. All data access
 * methods are backed by the repository pattern — no manual
 * {@code EntityManager} usage needed.
 */
@ApplicationScoped
public class MovieService {

    @Inject
    private MovieRepository movieRepository;

    public Movie addMovie(final String director, final String title, final int year) {
        return movieRepository.insert(new Movie(director, title, year));
    }

    public Optional<Movie> findById(final long id) {
        return movieRepository.findById(id);
    }

    public List<Movie> findByDirector(final String director) {
        return movieRepository.findByDirector(director);
    }

    public List<Movie> findByYear(final int year) {
        return movieRepository.findByYear(year);
    }

    public List<Movie> findByDirectorAndYearRange(final String director, final int startYear, final int endYear) {
        return movieRepository.findByDirectorAndYearRange(director, startYear, endYear);
    }

    public long countByDirector(final String director) {
        return movieRepository.countByDirector(director);
    }

    public void deleteMovie(final Movie movie) {
        movieRepository.delete(movie);
    }
}

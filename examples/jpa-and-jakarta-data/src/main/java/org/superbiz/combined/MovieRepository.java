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

import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.OrderBy;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;

import java.util.List;

/**
 * Jakarta Data repository for Movie entities.
 *
 * <p>This provides a modern, declarative API for data access.
 * It operates on the same persistence unit and database table
 * as the classic JPA {@link MovieDao}.
 */
@Repository
public interface MovieRepository extends CrudRepository<Movie, Long> {

    List<Movie> findByDirector(String director);

    List<Movie> findByGenre(Genre genre);

    @OrderBy("year")
    List<Movie> findByYearGreaterThanEqual(int year);

    @Query("SELECT m FROM Movie m WHERE m.genre = ?1 AND m.year BETWEEN ?2 AND ?3 ORDER BY m.title")
    List<Movie> findByGenreAndYearRange(Genre genre, int startYear, int endYear);

    long countByGenre(Genre genre);

    long countByDirector(String director);
}

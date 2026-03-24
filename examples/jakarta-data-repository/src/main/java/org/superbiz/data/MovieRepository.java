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

import jakarta.data.repository.By;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Find;
import jakarta.data.repository.OrderBy;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;

import java.util.List;

/**
 * A Jakarta Data repository for Movie entities.
 *
 * <p>This interface extends {@link CrudRepository} which provides standard
 * CRUD operations (insert, findById, update, delete, etc.) out of the box.
 *
 * <p>Custom query methods are declared using:
 * <ul>
 *   <li><b>Method-name convention</b>: {@code findByDirector} is parsed and
 *       translated to a query filtering by the {@code director} field.</li>
 *   <li><b>{@code @Find} annotation</b>: {@code findByYear} uses the {@code @Find}
 *       annotation with {@code @OrderBy} to add sorting.</li>
 *   <li><b>{@code @Query} annotation</b>: {@code findByDirectorAndYearRange} uses
 *       JPQL for more complex queries.</li>
 * </ul>
 */
@Repository
public interface MovieRepository extends CrudRepository<Movie, Long> {

    // Method-name query: find all movies by a given director
    List<Movie> findByDirector(String director);

    // @Find + @OrderBy: find movies by year, ordered by title ascending
    @Find
    @OrderBy("title")
    List<Movie> findByYear(@By("year") int year);

    // @Query with JPQL: find movies by director within a year range
    @Query("SELECT m FROM Movie m WHERE m.director = ?1 AND m.year BETWEEN ?2 AND ?3 ORDER BY m.year")
    List<Movie> findByDirectorAndYearRange(String director, int startYear, int endYear);

    // Method-name query: count movies by director
    long countByDirector(String director);
}

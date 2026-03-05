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

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.util.List;

/**
 * Classic JPA data access object using EntityManager directly.
 *
 * <p>This demonstrates the traditional Jakarta EE approach to data access.
 * Both this DAO and the Jakarta Data {@link MovieRepository} operate
 * on the same persistence unit and database table, showing that both
 * approaches coexist seamlessly.
 */
@Stateless
public class MovieDao {

    @PersistenceContext(unitName = "movie-unit")
    private EntityManager em;

    public void addMovie(final Movie movie) {
        em.persist(movie);
    }

    public void deleteMovie(final Movie movie) {
        em.remove(em.merge(movie));
    }

    public Movie findById(final long id) {
        return em.find(Movie.class, id);
    }

    public List<Movie> findAll() {
        return em.createQuery("SELECT m FROM Movie m ORDER BY m.title", Movie.class)
            .getResultList();
    }

    public List<Movie> findByGenre(final Genre genre) {
        final TypedQuery<Movie> query =
            em.createQuery("SELECT m FROM Movie m WHERE m.genre = :genre ORDER BY m.title", Movie.class);
        query.setParameter("genre", genre);
        return query.getResultList();
    }

    /**
     * Uses the JPA Criteria API to build a dynamic query.
     * This is something that classic JPA excels at for complex dynamic queries.
     */
    public List<Movie> findByDirectorAndGenre(final String director, final Genre genre) {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<Movie> cq = cb.createQuery(Movie.class);
        final Root<Movie> root = cq.from(Movie.class);

        cq.where(
            cb.and(
                cb.equal(root.get("director"), director),
                cb.equal(root.get("genre"), genre)
            )
        );
        cq.orderBy(cb.asc(root.get("year")));

        return em.createQuery(cq).getResultList();
    }

    public long count() {
        return em.createQuery("SELECT COUNT(m) FROM Movie m", Long.class)
            .getSingleResult();
    }
}

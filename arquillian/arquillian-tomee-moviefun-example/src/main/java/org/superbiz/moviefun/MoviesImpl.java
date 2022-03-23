/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.moviefun;

import jakarta.ejb.Stateless;
import jakarta.jws.WebService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.EntityType;
import java.util.List;

@Stateless(name = "Movies")
@WebService(portName = "MoviesPort",
        serviceName = "MoviesWebService",
        targetNamespace = "http://superbiz.org/wsdl")
public class MoviesImpl implements Movies, MoviesRemote {

    @PersistenceContext(unitName = "movie-unit")
    private EntityManager entityManager;

    @Override
    public Movie find(final Long id) {
        return entityManager.find(Movie.class, id);
    }

    @Override
    public void clean() {
        entityManager.createQuery("delete from Movie").executeUpdate();
    }

    @Override
    public void addMovie(final Movie movie) {
        entityManager.persist(movie);
    }

    @Override
    public void editMovie(final Movie movie) {
        entityManager.merge(movie);
    }

    @Override
    public void deleteMovie(final Movie movie) {
        entityManager.remove(movie);
    }

    @Override
    public void deleteMovieId(final long id) {
        final Movie movie = entityManager.find(Movie.class, id);
        deleteMovie(movie);
    }

    @Override
    public List<Movie> getMovies() {
        final CriteriaQuery<Movie> cq = entityManager.getCriteriaBuilder().createQuery(Movie.class);
        cq.select(cq.from(Movie.class));
        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public List<Movie> findByTitle(final String title) {
        return findByStringField("title", title);
    }

    @Override
    public List<Movie> findByGenre(final String genre) {
        return findByStringField("genre", genre);
    }

    @Override
    public List<Movie> findByDirector(final String director) {
        return findByStringField("director", director);
    }

    private List<Movie> findByStringField(final String fieldname, final String param) {
        final CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Movie> query = builder.createQuery(Movie.class);
        final Root<Movie> root = query.from(Movie.class);
        final EntityType<Movie> type = entityManager.getMetamodel().entity(Movie.class);

        final Path<String> path = root.get(type.getDeclaredSingularAttribute(fieldname, String.class));
        final Predicate condition = builder.like(path, "%" + param + "%");

        query.where(condition);

        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public List<Movie> findRange(final int[] range) {
        final CriteriaQuery<Movie> cq = entityManager.getCriteriaBuilder().createQuery(Movie.class);
        cq.select(cq.from(Movie.class));
        final TypedQuery<Movie> q = entityManager.createQuery(cq);
        q.setMaxResults(range[1] - range[0]);
        q.setFirstResult(range[0]);
        return q.getResultList();
    }

    @Override
    public int count() {
        final CriteriaQuery<Long> cq = entityManager.getCriteriaBuilder().createQuery(Long.class);
        final Root<Movie> rt = cq.from(Movie.class);
        cq.select(entityManager.getCriteriaBuilder().count(rt));
        final TypedQuery<Long> q = entityManager.createQuery(cq);
        return q.getSingleResult().intValue();
    }

}
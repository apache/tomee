/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.superbiz.moviefun;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;
import java.util.List;

@LocalBean
@Stateless(name = "Movies")
@WebService(portName = "MoviesPort",
        serviceName = "MoviesWebService",
        targetNamespace = "http://superbiz.org/wsdl")
public class MoviesImpl implements Movies, MoviesRemote {

    @EJB
    private Notifier notifier;

    @PersistenceContext(unitName = "movie-unit")
    private EntityManager entityManager;

    @Override
    public Movie find(Long id) {
        return entityManager.find(Movie.class, id);
    }

    @Override
    public void addMovie(Movie movie) {
        entityManager.persist(movie);
    }

    @Override
    public void editMovie(Movie movie) {
        entityManager.merge(movie);
    }

    @Override
    public void deleteMovie(Movie movie) {
        entityManager.remove(movie);
        notifier.notify("Deleted Movie \"" + movie.getTitle() + "\" (" + movie.getYear() + ")");
    }

    @Override
    public void deleteMovieId(long id) {
        Movie movie = entityManager.find(Movie.class, id);
        deleteMovie(movie);
    }

    @Override
    public List<Movie> getMovies() {
        CriteriaQuery<Movie> cq = entityManager.getCriteriaBuilder().createQuery(Movie.class);
        cq.select(cq.from(Movie.class));
        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public List<Movie> findByTitle(String title) {
        return findByStringField("title", title);
    }

    @Override
    public List<Movie> findByGenre(String genre) {
        return findByStringField("genre", genre);
    }

    @Override
    public List<Movie> findByDirector(String director) {
        return findByStringField("director", director);
    }

    private List<Movie> findByStringField(String fieldname, String param) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Movie> query = builder.createQuery(Movie.class);
        Root<Movie> root = query.from(Movie.class);
        EntityType<Movie> type = entityManager.getMetamodel().entity(Movie.class);

        Path<String> path = root.get(type.getDeclaredSingularAttribute(fieldname, String.class));
        Predicate condition = builder.like(path, "%" + param + "%");

        query.where(condition);

        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public List<Movie> findRange(int[] range) {
        CriteriaQuery<Movie> cq = entityManager.getCriteriaBuilder().createQuery(Movie.class);
        cq.select(cq.from(Movie.class));
        TypedQuery<Movie> q = entityManager.createQuery(cq);
        q.setMaxResults(range[1] - range[0]);
        q.setFirstResult(range[0]);
        return q.getResultList();
    }

    @Override
    public int count() {
        CriteriaQuery<Long> cq = entityManager.getCriteriaBuilder().createQuery(Long.class);
        Root<Movie> rt = cq.from(Movie.class);
        cq.select(entityManager.getCriteriaBuilder().count(rt));
        TypedQuery<Long> q = entityManager.createQuery(cq);
        return (q.getSingleResult()).intValue();
    }

}
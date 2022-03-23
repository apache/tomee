/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.injection.jpa;

//START SNIPPET: code

import jakarta.ejb.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceContextType;
import jakarta.persistence.Query;
import java.util.List;

@Singleton
public class Movies {

    @PersistenceContext(unitName = "movie-unit", type = PersistenceContextType.TRANSACTION)
    private EntityManager entityManager;

    public void addMovie(Movie movie) throws Exception {
        entityManager.persist(movie);
    }

    public void deleteMovie(Movie movie) throws Exception {
        final Movie storedMovie = entityManager.find(Movie.class, movie.getId());
        entityManager.remove(storedMovie);
    }

    public List<Movie> getMovies() throws Exception {
        final Query query = entityManager.createQuery("SELECT m from Movie as m");
        return query.getResultList();
    }

    public void deleteAll() throws Exception {
        final Query query = entityManager.createQuery("DELETE from Movie");
        query.executeUpdate();
    }

    public long count() throws Exception {
        final Query query = entityManager.createQuery("select count(m) from Movie as m");
        return (Long) query.getSingleResult();
    }

}
//END SNIPPET: code

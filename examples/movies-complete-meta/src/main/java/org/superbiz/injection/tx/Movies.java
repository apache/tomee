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
package org.superbiz.injection.tx;

import org.superbiz.injection.tx.api.Add;
import org.superbiz.injection.tx.api.Delete;
import org.superbiz.injection.tx.api.MovieUnit;
import org.superbiz.injection.tx.api.Read;

import jakarta.ejb.Stateful;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.List;

//END SNIPPET: code

//START SNIPPET: code
@Stateful
public class Movies {

    @MovieUnit
    private EntityManager entityManager;

    @Add
    public void addMovie(Movie movie) throws Exception {
        entityManager.persist(movie);
    }

    @Delete
    public void deleteMovie(Movie movie) throws Exception {
        entityManager.remove(movie);
    }

    @Read
    public List<Movie> getMovies() throws Exception {
        Query query = entityManager.createQuery("SELECT m from Movie as m");
        return query.getResultList();
    }
}
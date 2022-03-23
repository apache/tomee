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
package org.superbiz.moviefun;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Stateless;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

@ApplicationScoped
public class MoviesBean {
    
    private HashMap<Integer,Movie> MovieStore;


    @PostConstruct
    public void MovieBean() {
        MovieStore = new HashMap();
    }

    public void addMovie(Movie newMovie) {
        MovieStore.put(newMovie.getId(), newMovie);
    }

    public void deleteMovie(int id) {
        MovieStore.remove(id);
    }

    public void updateMovie(Movie updatedMovie) {
        MovieStore.put(updatedMovie.getId(),updatedMovie);
    }

    public Movie getMovie(int id) {
        return MovieStore.get(id);
    }

    public List getMovies() {
        Collection<Movie> Movies = MovieStore.values();
        return new ArrayList<Movie>(Movies);

    }


}
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
package org.superbiz.spring;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import java.util.List;

//START SNIPPET: code

/**
 * This is a simple Spring bean that we use as an easy way
 * to seed the example with a list of persistent Movie objects
 * <p/>
 * The individual Movie objects are constructed by Spring, then
 * passed into the Movies EJB where they are transactionally
 * persisted with the EntityManager.
 */
public class AvailableMovies {

    @EJB(name = "MoviesLocal")
    private Movies moviesEjb;

    private List<Movie> movies;

    @PostConstruct
    public void construct() throws Exception {
        for (Movie movie : movies) {
            moviesEjb.addMovie(movie);
        }
    }

    public List<Movie> getMovies() {
        return movies;
    }

    public void setMovies(List<Movie> movies) {
        this.movies = movies;
    }

    public void setMoviesEjb(Movies moviesEjb) {
        this.moviesEjb = moviesEjb;
    }
}
//END SNIPPET: code

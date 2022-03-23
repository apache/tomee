/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.movie;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

// request scoped is totally desired in this simple test
// the goal is for each request we make to have always the same piece of data
// it makes the assertions easier and not dependant on the order of the execution
@RequestScoped
public class MovieStore {

    // not really required to have a concurrent map because it's request scoped bean
    private final ConcurrentMap<Integer, Movie> store = new ConcurrentHashMap<>();

    @PostConstruct
    public void construct(){
        this.addMovie(new Movie("Wedding Crashers", "David Dobkin", "Comedy", 1, 2005));
        this.addMovie(new Movie("Starsky & Hutch", "Todd Phillips", "Action", 2, 2004));
        this.addMovie(new Movie("Shanghai Knights", "David Dobkin", "Action", 3, 2003));
        this.addMovie(new Movie("I-Spy", "Betty Thomas", "Adventure", 4, 2002));
        this.addMovie(new Movie("The Royal Tenenbaums", "Wes Anderson", "Comedy", 5, 2001));
        this.addMovie(new Movie("Zoolander", "Ben Stiller", "Comedy", 6, 2001));
    }

    public List<Movie> getAllMovies() {
        return new ArrayList<>(store.values());
    }

    public Movie addMovie(final Movie newMovie) {
        store.putIfAbsent(newMovie.getId(), newMovie);
        return newMovie;
    }

    public Movie deleteMovie(final int id) {
        return store.remove(id);
    }

    public Movie getMovie(final int id) {
        return store.get(id);
    }

}

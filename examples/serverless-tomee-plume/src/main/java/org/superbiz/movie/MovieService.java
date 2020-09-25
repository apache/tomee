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

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Path("/movies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class MovieService {

    private Map<Integer, Movie> store = new ConcurrentHashMap<>();

    @PostConstruct
    public void construct(){
        this.addMovie(new Movie("Wedding Crashers", "David Dobkin", "Comedy", 1, 2005));
        this.addMovie(new Movie("Starsky & Hutch", "Todd Phillips", "Action", 2, 2004));
        this.addMovie(new Movie("Shanghai Knights", "David Dobkin", "Action", 3, 2003));
        this.addMovie(new Movie("I-Spy", "Betty Thomas", "Adventure", 4, 2002));
        this.addMovie(new Movie("The Royal Tenenbaums", "Wes Anderson", "Comedy", 5, 2001));
        this.addMovie(new Movie("Zoolander", "Ben Stiller", "Comedy", 6, 2001));
    }
    @GET
    public List<Movie> getAllMovies() {
        return new ArrayList<>(store.values());
    }

    @POST
    public Movie addMovie(final Movie newMovie) {
        store.put(newMovie.getId(), newMovie);
        return newMovie;
    }

}

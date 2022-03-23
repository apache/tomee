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
package org.superbiz.moviefun.rest;

import org.superbiz.moviefun.Movie;
import org.superbiz.moviefun.MoviesBean;

import jakarta.ejb.EJB;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("movies")
@Produces({"application/json"})
public class MoviesRest {

    @EJB
    private MoviesBean service;

    @GET
    @Path("{id}")
    public Movie find(@PathParam("id") Long id) {
        return service.find(id);
    }

    @GET
    public List<Movie> getMovies(@QueryParam("first") Integer first, @QueryParam("max") Integer max,
                                 @QueryParam("field") String field, @QueryParam("searchTerm") String searchTerm) {
        return service.getMovies(first, max, field, searchTerm);
    }

    @POST
    @Consumes("application/json")
    public Movie addMovie(Movie movie) {
        service.addMovie(movie);
        return movie;
    }

    @PUT
    @Path("{id}")
    @Consumes("application/json")
    public Movie editMovie(Movie movie) {
        service.editMovie(movie);
        return movie;
    }

    @DELETE
    @Path("{id}")
    public void deleteMovie(@PathParam("id") long id) {
        service.deleteMovie(id);
    }

    @GET
    @Path("count")
    @Produces(MediaType.TEXT_PLAIN)
    public int count(@QueryParam("field") String field, @QueryParam("searchTerm") String searchTerm) {
        return service.count(field, searchTerm);
    }

}
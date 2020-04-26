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

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("cinema")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MoviesRest {

    @Inject
    private MoviesBean moviesBean;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String status() {
        return "ok";
    }

    @GET
    @Path("/movies")
    @RolesAllowed({"crud", "read-only"})
    public List<Movie> getListOfMovies() {
        return moviesBean.getMovies();
    }

    @GET
    @Path("/movies/{id}")
    @RolesAllowed({"crud", "read-only"})
    public Movie getMovie(@PathParam("id") int id) {
        return moviesBean.getMovie(id);
    }

    @POST
    @Path("/movies")
    @RolesAllowed("crud")
    public void addMovie(Movie newMovie) {
        moviesBean.addMovie(newMovie);
    }

    @DELETE
    @Path("/movies/{id}")
    @RolesAllowed("crud")
    public void deleteMovie(@PathParam("id") int id) {
        moviesBean.deleteMovie(id);
    }

    @PUT
    @Path("/movies")
    @RolesAllowed("crud")
    public void updateMovie(Movie updatedMovie) {
        moviesBean.updateMovie(updatedMovie);
    }

}
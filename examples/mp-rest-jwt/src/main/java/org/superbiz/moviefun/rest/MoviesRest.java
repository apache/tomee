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
package org.superbiz.moviefun.rest;

import org.superbiz.moviefun.Movie;
import org.superbiz.moviefun.MoviesBean;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
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

    @POST
    @Path("/movies")
    @RolesAllowed("crud")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public void addMovie(Movie newMovie) {
        moviesBean.addMovie(newMovie);
    }

    @DELETE
    @Path("/movies/{id}")
    @RolesAllowed("read-only")
    public void deleteMovie(@PathParam("id") int id) {
        moviesBean.deleteMovie(id);
    }

    @PUT
    @Path("/movies")
    public void updateMovie(Movie updatedMovie) {
        moviesBean.updateMovie(updatedMovie);
    }

    @GET
    @Path("/movies/{id}")
    @RolesAllowed({"read-only","crud"})
    public Movie getMovie(@PathParam("id") int id) {
        return moviesBean.getMovie(id);
    }

    @GET
    @Path("/movies")
    @RolesAllowed({"crud", "read-only"})
    public List<Movie> getListOfMovies() {
        return moviesBean.getMovies();
    }


//    @Inject
//    @Claim("raw_token")
//    private ClaimValue<String> rawToken;
//
//    @Inject
//    @Claim("iss")
//    private ClaimValue<String> issuer;
//
//    @Inject
//    @Claim("jti")
//    private ClaimValue<String> jti;
//
//    @Inject
//    private JsonWebToken jwtPrincipal;
//
//    @Context
//    private SecurityContext securityContext;
//
//    @GET
//    @Path("{id}")
//    public Movie find(@PathParam("id") Long id) {
//        return service.find(id);
//    }
//
//    @GET
//    public List<Movie> getMovies(@QueryParam("first") Integer first, @QueryParam("max") Integer max,
//                                 @QueryParam("field") String field, @QueryParam("searchTerm") String searchTerm) {
//        return service.getMovies(first, max, field, searchTerm);
//    }
//
//    @POST
//    @Consumes("application/json")
//    @RolesAllowed("create")
//    public Movie addMovie(Movie movie) {
//        service.addMovie(movie);
//        return movie;
//    }
//
//    @PUT
//    @Path("{id}")
//    @Consumes("application/json")
//    @RolesAllowed("update")
//    public Movie editMovie(
//            @PathParam("id") final long id,
//            Movie movie
//    ) {
//        service.editMovie(movie);
//        return movie;
//    }
//
//    @DELETE
//    @Path("{id}")
//    @RolesAllowed("delete")
//    public void deleteMovie(@PathParam("id") long id) {
//        service.deleteMovie(id);
//    }
//
//    @GET
//    @Path("count")
//    @Produces(MediaType.TEXT_PLAIN)
//    public int count(@QueryParam("field") String field, @QueryParam("searchTerm") String searchTerm) {
//        return service.count(field, searchTerm);
//    }

}
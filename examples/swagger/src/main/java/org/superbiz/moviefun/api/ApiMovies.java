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
package org.superbiz.moviefun.api;

import javax.ws.rs.*;
import java.util.ArrayList;
import java.util.List;

@Path("movies")
@Produces({"application/json"})
public class ApiMovies {

    @GET
    public List<DtoMovie> getMovies(@QueryParam("first") Integer first,
                                    @QueryParam("max") Integer max,
                                    @QueryParam("field") String field,
                                    @QueryParam("searchTerm") String searchTerm) {
        return new ArrayList<DtoMovie>();
    }

    @GET
    @Path("{id}")
    public DtoMovie getMovie(@PathParam("id") Integer id) {
        return new DtoMovie();
    }

    @POST
    @Consumes("application/json")
    public DtoMovie addMovie(DtoMovie movie) {
        return movie;
    }

    @PUT
    @Path("{id}")
    @Consumes("application/json")
    public DtoMovie editMovie(DtoMovie movie) {
        return movie;
    }

    @DELETE
    @Path("{id}")
    public void deleteMovie(@PathParam("id") long id) {

    }

}
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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.SecurityContext;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.logging.Logger;

@Path("/movies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class MovieResource {

    private static final Logger LOGGER = Logger.getLogger(MovieResource.class.getName());

    @Inject
    private MovieStore store;

    // jakarta enterprise security context
    @Inject
    private SecurityContext securityContext;

    @GET
    public List<Movie> getAllMovies() {
        LOGGER.info(getCallerName() + " reading movies");
        return store.getAllMovies();
    }

    @GET
    @Path("{id}")
    public Movie getMovie(@PathParam("id") final int id) {
        final Movie movie = store.getMovie(id);
        LOGGER.info(getCallerName() + " reading movie " + id + " / " + movie);
        return movie;
    }

    private String getCallerName() {
        if (securityContext.getCallerPrincipal() != null) {
            return String.format("%s[admin=%s]",
                                 securityContext.getCallerPrincipal().getName(),
                                 securityContext.isCallerInRole("admin"));
        }

        return null;
    }
}

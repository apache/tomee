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

import org.apache.tomee.security.cdi.TomcatUserIdentityStoreDefinition;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.security.enterprise.authentication.mechanism.http.BasicAuthenticationMechanismDefinition;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.util.logging.Logger;

@Path("/movies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@TomcatUserIdentityStoreDefinition
@BasicAuthenticationMechanismDefinition
@ApplicationScoped
public class MovieAdminResource {

    private static final Logger LOGGER = Logger.getLogger(MovieAdminResource.class.getName());

    @Inject
    private MovieStore store;

    // JAXRS security context also wired
    @Context
    private SecurityContext securityContext;

    @POST
    public Movie addMovie(final Movie newMovie) {
        LOGGER.info(getUserName() + " adding new movie " + newMovie);
        return store.addMovie(newMovie);
    }

    @DELETE
    @Path("{id}")
    public Movie deleteMovie(@PathParam("id") final int id) {
        final Movie movie = store.deleteMovie(id);
        LOGGER.info(getUserName() + " deleting movie " + id + " / " + movie);
        return movie;
    }

    private String getUserName() {
        if (securityContext.getUserPrincipal() != null) {
            return String.format("%s[admin=%s]",
                                 securityContext.getUserPrincipal().getName(),
                                 securityContext.isUserInRole("admin"));
        }

        return null;
    }

}

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.superbiz.rest;

import org.superbiz.injection.jpa.Movie;
import org.superbiz.injection.jpa.Movies;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.Lock;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import javax.sql.DataSource;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.logging.Logger;

import static jakarta.ejb.LockType.READ;

@Lock(READ)
@Singleton
@Path("/")
@Startup
public class MovieService {

    private final static Logger LOGGER = Logger.getLogger(MovieService.class.getName());

    @Resource
    private DataSource ds;

    @EJB
    private Movies movies;

    @GET
    @Path("setup")
    public void setup() throws Exception {
        final long count = movies.count();

        if (count == 0) {
            movies.addMovie(new Movie("Quentin Tarantino", "Reservoir Dogs", 1992));
            movies.addMovie(new Movie("Joel Coen", "Fargo", 1996));
            movies.addMovie(new Movie("Joel Coen", "The Big Lebowski", 1998));
        }
    }

    @GET
    public Response checkDatabase() {

        try {
            try (final Connection connection = ds.getConnection();
                 final PreparedStatement ps = connection.prepareStatement("select 1 from dual");
                 final ResultSet rs = ps.executeQuery()) {

                int results = 0;

                while (rs.next()) {
                    results++;
                }

                LOGGER.info("Request checkDatabase() | result ["+results+"]");
                return Response.ok().entity(String.valueOf(results)).build();
            }
        } catch (Exception e) {
            throw new WebApplicationException(e, Response.serverError().build());
        }
    }

    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Movie> fetchAll() throws Exception {
        return movies.getMovies();
    }


}



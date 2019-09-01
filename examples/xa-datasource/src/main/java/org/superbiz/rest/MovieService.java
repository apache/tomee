package org.superbiz.rest;

import org.superbiz.injection.jpa.Movie;
import org.superbiz.injection.jpa.Movies;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.logging.Logger;

import static javax.ejb.LockType.READ;

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



package org.superbiz.rest;

import javax.annotation.Resource;
import javax.ejb.Lock;
import javax.ejb.Singleton;
import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;

import static javax.ejb.LockType.READ;

@Lock(READ)
@Singleton
@Path("/")
public class MovieService {

    private final static Logger LOGGER = Logger.getLogger(MovieService.class.getName());

    @Resource
    private DataSource ds;

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

//    @GET
//    public Response checkDatabase() {
//        return Response.ok().entity("101").build();
//    }


}



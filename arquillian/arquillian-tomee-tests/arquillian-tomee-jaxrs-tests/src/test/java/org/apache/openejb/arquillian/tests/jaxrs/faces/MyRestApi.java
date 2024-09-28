package org.apache.openejb.arquillian.tests.jaxrs.faces;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/api")
public class MyRestApi {

    @GET
    @Path("/test")
    public Response getTest() {
        return Response.ok("Hello world!").build();
    }
}

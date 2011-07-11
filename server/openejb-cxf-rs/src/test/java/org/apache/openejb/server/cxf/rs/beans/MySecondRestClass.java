package org.apache.openejb.server.cxf.rs.beans;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * @author Romain Manni-Bucau
 */
@Path("/second")
public class MySecondRestClass {
    @Path("/hi2/{you}") @GET public String hi(@PathParam("you") String you) {
        return "hi " + you;
    }
}

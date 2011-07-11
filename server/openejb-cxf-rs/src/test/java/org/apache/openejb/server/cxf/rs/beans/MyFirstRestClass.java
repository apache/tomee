package org.apache.openejb.server.cxf.rs.beans;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * @author Romain Manni-Bucau
 */
@Path("/first")
public class MyFirstRestClass {
    @Path("/hi") @GET public String hi() {
        return "Hi from REST World!";
    }
}

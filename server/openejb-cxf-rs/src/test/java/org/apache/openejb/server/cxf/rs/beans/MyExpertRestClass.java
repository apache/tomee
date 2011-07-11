package org.apache.openejb.server.cxf.rs.beans;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

/**
 * @author Romain Manni-Bucau
 */
@Path("/expert")
public class MyExpertRestClass {
    @Path("/still-hi/") @POST public String hi(String you) {
        return "hi " + you;
    }
}

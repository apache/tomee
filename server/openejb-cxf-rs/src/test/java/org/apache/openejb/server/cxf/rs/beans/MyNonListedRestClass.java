package org.apache.openejb.server.cxf.rs.beans;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

/**
 * @author Romain Manni-Bucau
 */
@Path("/non-listed")
public class MyNonListedRestClass {
    @Path("/yata") @GET public String yata(@QueryParam("did")String iDidIt) {
        return "Yata! " + iDidIt;
    }
}

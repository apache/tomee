package org.superbiz;

import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/rest")
@Stateless
public class SomeRest {
    @GET
    @Path("/ok")
    public String ok() {
        return "rest";
    }
}

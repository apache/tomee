package org.superbiz.rest.application;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.Date;

/**
 * @author rmannibucau
 */
@Path("/pojo")
public class SimpleRESTPojo {
    @GET
    public String pojo() {
        return "pojo ok @ " + new Date().toString();
    }
}

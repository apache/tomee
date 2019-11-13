package org.apache.openejb.arquillian.tests.jaxrs.ear;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Singleton
@Lock(LockType.READ)
@Path("api")
public class HelloEndpoint {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String greet() {
        return "Hello rest!";
    }

}

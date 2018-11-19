package org.superbiz.rest;

import org.eclipse.microprofile.metrics.annotation.Counted;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/greeting")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class GreetingService {

    @Counted(monotonic = true, name = "message_counter", absolute = true)
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String message() {
        return "Hi Microprofile Metrics!";
    }
}

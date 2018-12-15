package org.superbiz.application;

import javax.inject.Inject;
import javax.mvc.Controller;
import javax.mvc.Models;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

@Controller
@Path("hello")
public class TomeeController {

    @Inject
    private Models models;

    @GET
    public String getHello(@QueryParam("name") String name) {
        this.models.put("hello", name);
        return "hello.jsp";
    }
}
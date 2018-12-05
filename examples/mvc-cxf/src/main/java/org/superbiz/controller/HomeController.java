package org.superbiz.controller;

import javax.mvc.Controller;
import javax.mvc.View;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Controller
@Path("home")
public class HomeController {

    @GET
    @View("home.jsp")
    public void homePage() {
    }
}
package org.superbiz.rest;

import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Metered;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/weather")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class WeatherService {
    @Path("/day/status")

    @Metered(name = "dailyStatus", unit = MetricUnits.MINUTES, description = "Metrics to daily weather status method", absolute = true)
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String dayStatus() {
        return "Hi, today is a sunny day!";
    }

    @Path("/week/status")
    @Metered(name = "weeklyStatus", unit = MetricUnits.MINUTES, description = "Metrics to weekly weather status method", absolute = true)
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String weekStatus() {
        return "Hi, week will be mostly sunny!";
    }
}

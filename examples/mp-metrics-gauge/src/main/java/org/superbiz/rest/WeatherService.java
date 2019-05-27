package org.superbiz.rest;

import org.eclipse.microprofile.metrics.annotation.Gauge;

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

    @Path("/day/temperature")
    @Gauge(name = "weather_day_temperature", absolute = true, unit = "celsius",
            displayName = "Weather Day Temperature",
            description = "This metric shows the day temperature.",
            tags = {"weather=temperature"})
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Integer dayTemperature() {
        return 30;
    }
}

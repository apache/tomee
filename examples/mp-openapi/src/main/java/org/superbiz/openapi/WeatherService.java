package org.superbiz.openapi;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Path("/weather")
@ApplicationScoped
public class WeatherService {

    @Path("/status/{day}/")
    @GET
    @Operation(summary = "Finds weather for day specified in the URL ",
    description = "Describes how the day will be.")
    @APIResponse(responseCode = "400",
    description = "Weather for this day not found")
    public  Response dayStatus(
            @Parameter(description = "The day for which the weather needs to be fetched.", required = true)
            @PathParam("day") final String day) {

        if (day.equalsIgnoreCase("tomorrow")) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        String message = day + " is a sunny day.";
        return Response.status(Response.Status.OK)
                .entity(message)
                .build();
    }

    @Path("/detailedWeather/{day}/")
    @GET
    @APIResponse(description = "Detailed Weather", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Weather.class)))
    public  Weather getDetailedWeather(@Parameter(description = "The day for which the detailed weather needs to be fetched.", required = true) @PathParam("day") final String day) {

        Weather w = new Weather();
        w.setHumidity("12");
        w.setTemperature("37");
        return w;

    }
}

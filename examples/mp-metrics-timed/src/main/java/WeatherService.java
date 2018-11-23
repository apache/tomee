import org.eclipse.microprofile.metrics.annotation.Timed;

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
    @Timed(name = "weather_day_status", absolute = true,
            displayName = "Weather Day Status",
            description = "This metric shows the weather status of the day.")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String dayStatus() {
        return "Hi, today is a sunny day!";
    }
}

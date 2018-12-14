index-group=Unrevised
type=page
status=published
~~~~~~
# Microprofile Metrics
This is an example on how to use MicroProfile metrics in TomEE.

##### Run the application:

    mvn clean install tomee:run 

Within the application there is an endpoint that will give you the weather status for the day.

##### For the day status call:

    GET http://localhost:8080/mp-metrics-timed/weather/day/status
    
##### Response:

    Hi, today is a sunny day!
    

#### Timed Feature
MicroProfile Metrics has a feature that tracks the time of an event.

To use this feature you need to annotate the JAX-RS resource method with @Timed.

    @Path("/weather")
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
    ...
    }

There are some configurations, as part of @Timed, that you need to know:

**String name**
Optional. Sets the name of the metric. If not explicitly given the name of the annotated object is used.

**boolean absolute**
If true, uses the given name as the absolute name of the metric. If false, prepends the package name and class name before the given name. Default value is false.

**String displayName**
Optional. A human readable display name for metadata.

**String description**
Optional. A description of the metric.

**String[] tags**
Optional. Array of Strings in the <key>=<value> format to supply special tags to a metric.

**String unit**
Unit of the metric. Default for @Timed is nanoseconds.

#### Metric data

Check the timed metric doing a _GET_ request:

##### Prometheus format:

    GET http://localhost:8080/mp-metrics-timed/metrics/application/weather_day_status
    
##### Response:
     
    # TYPE application:weather_day_status_seconds summary timer
    # TYPE application:weather_day_status_seconds_count timer
    application:weather_day_status_seconds_count 1.0
    # TYPE application:weather_day_status_rate_per_second timer
    application:weather_day_status_rate_per_second 0.0
    # TYPE application:weather_day_status_one_min_rate_per_second timer
    application:weather_day_status_one_min_rate_per_second 0.0
    # TYPE application:weather_day_status_five_min_rate_per_second timer
    application:weather_day_status_five_min_rate_per_second 0.0
    # TYPE application:weather_day_status_fifteen_min_rate_per_second timer
    application:weather_day_status_fifteen_min_rate_per_second 0.0
    # TYPE application:weather_day_status_min_seconds timer
    application:weather_day_status_min_seconds 48352.0
    # TYPE application:weather_day_status_max_seconds timer
    application:weather_day_status_max_seconds 48352.0
    # TYPE application:weather_day_status_mean_seconds timer
    application:weather_day_status_mean_seconds 48352.0
    # TYPE application:weather_day_status_stddev_seconds timer
    application:weather_day_status_stddev_seconds 0.0
    # TYPE application:weather_day_status_seconds timer
    application:weather_day_status_seconds{quantile="0.5"} 48352.0
    # TYPE application:weather_day_status_seconds timer
    application:weather_day_status_seconds{quantile="0.75"} 48352.0
    # TYPE application:weather_day_status_seconds timer
    application:weather_day_status_seconds{quantile="0.95"} 48352.0
    # TYPE application:weather_day_status_seconds timer
    application:weather_day_status_seconds{quantile="0.98"} 48352.0
    # TYPE application:weather_day_status_seconds timer
    application:weather_day_status_seconds{quantile="0.99"} 48352.0
    # TYPE application:weather_day_status_seconds timer
    application:weather_day_status_seconds{quantile="0.999"} 48352.0

##### JSON Format:

For json format add the header _Accept=application/json_ to the request. 
  
    {
        "weather_day_status": {
            "count": 1,
            "fifteenMinRate": 0,
            "fiveMinRate": 0,
            "max": 48352,
            "mean": 48352,
            "meanRate": 0,
            "min": 48352,
            "oneMinRate": 0,
            "p50": 48352,
            "p75": 48352,
            "p95": 48352,
            "p98": 48352,
            "p99": 48352,
            "p999": 48352,
            "stddev": 0
        }
    }
   
#### Metric metadata
A metric will have metadata so you can know more about it, like displayName, description, tags e etc.

Check the metric metadata doing a _OPTIONS_ request:

##### Request

    OPTIONS http://localhost:8080/mp-metrics-timed/metrics/application/weather_day_status

##### Response:

    {
        "weather_day_status": {
            "description": "This metric shows the weather status of the day.",
            "displayName": "Weather Day Status",
            "name": "weather_day_status",
            "reusable": false,
            "tags": "",
            "type": "timer",
            "typeRaw": "TIMER",
            "unit": "nanoseconds"
        }
    }

You can also try it out using the WeatherServiceTest.java available in the project.


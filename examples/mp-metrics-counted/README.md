index-group=Unrevised
type=page
status=published
~~~~~~
# Microprofile Metrics
This is an example on how to use microprofile metrics in TomEE.


##### Run the application:

    mvn clean install tomee:run 

Within the application there is an endpoint that will give you weather status for the day and week.

##### For the day status call:

    GET http://localhost:8080/rest-mp-metrics/weather/day/status
    
##### Response:

    Hi, today is a sunny day!
    

#### Counted Feature
MicroProfile metrics has a feature that can be used to count requests to a service.

To use this feature you need to annotate the JAX-RS resource method with @Counted.

    @Path("/weather")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApplicationScoped
    public class WeatherService {

        @Path("/day/status")
        @Counted(monotonic = true, name = "weather_day_status", absolute = true)
        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public String dayStatus() {
            return "Hi, today is a sunny day!";
        }
    ...
    }

There are some configurations, as part of @Counted, that you need to know:

**String name**
Optional. Sets the name of the metric. If not explicitly given the name of the annotated object is used.

**boolean absolute**
If true, uses the given name as the absolute name of the metric. If false, prepends the package name and class name before the given name. Default value is false.

**String displayName**
Optional. A human-readable display name for metadata.

**String description**
Optional. A description of the metric.

**String[] tags**
Optional. Array of Strings in the <key>=<value> format to supply special tags to a metric.

**boolean reusable**
Denotes if a metric with a certain name can be registered in more than one place. Does not apply to gauges.

#### Metric data

Check the counter metric doing a _GET_ request:

##### Prometheus format:

    GET http://localhost:8080/mp-metrics-counted/metrics/application/weather_day_status
    
##### Response:
     
    # TYPE application:weather_day_status counter
    application:weather_day_status 1.0
    
  
##### JSON Format:

For json format add the header _Accept=application/json_ to the request. 
  
    {
        "weather_day_status": {
            "delegate": {},
            "unit": "none",
            "count": 1
        }
    }
   
#### Metric metadata
A metric will have a metadata so you can know more information about it, like displayName, description, tags e etc.

Check the metric metadata doing a _OPTIONS_ request:

##### Request

    OPTIONS http://localhost:8080/mp-metrics-counted/metrics/application/weather_day_status

##### Response:

    {
        "weather_day_status": {
            "unit": "none",
            "displayName": "Weather Day Status",
            "name": "weather_day_status",
            "typeRaw": "COUNTER",
            "description": "This metric shows the weather status of the day.",
            "type": "counter",
            "value": {
                "unit": "none",
                "displayName": "Weather Day Status",
                "name": "weather_day_status",
                "tagsAsString": "",
                "typeRaw": "COUNTER",
                "description": "This metric shows the weather status of the day.",
                "type": "counter",
                "reusable": false,
                "tags": {}
            },
            "reusable": false,
            "tags": ""
        }
    }

You can also try it out using the WeatherServiceTest.java available in the project.


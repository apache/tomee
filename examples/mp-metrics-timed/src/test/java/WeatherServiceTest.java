/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.StringReader;
import java.net.URL;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class WeatherServiceTest {

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        final WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "test.war")
                .addClass(WeatherService.class)
                .addAsWebInfResource(new StringAsset("<beans/>"), "beans.xml");
        return webArchive;
    }

    @ArquillianResource
    private URL base;

    private Client client;

    @Before
    public void before() {
        this.client = ClientBuilder.newClient();
    }

    @After
    public void after() {
        this.client.close();
    }

    @Test
    public void testTimedMetric() {
        WebTarget webTarget = client.target(base.toExternalForm());

        webTarget
                .path("/weather/day/status")
                .request().buildGet().invoke();

//        final String metricPath = "/metrics/application/weather_day_status";
        final String metricPath = "/metrics";
        assertPrometheusFormat(metricPath);

        //assertJsonFormat(metricPath); // TODO: fix JSON export
    }

    private void assertPrometheusFormat(final String metricPath) {
        WebTarget webTarget = client.target(base.toExternalForm());

        String metric = webTarget
                .path(metricPath)
                .request()
                .accept(MediaType.TEXT_PLAIN)
                .buildGet()
                .invoke()
                .readEntity(String.class);

        System.out.println("Received: " + metric);

        String[] expected = {
            "# TYPE weather_day_status_seconds summary",
            "# HELP weather_day_status_seconds This metric shows the weather status of the day.",
            "weather_day_status_seconds{mp_scope=\"weather\",quantile=\"0.5\",}",
            "weather_day_status_seconds{mp_scope=\"weather\",quantile=\"0.75\",}",
            "weather_day_status_seconds{mp_scope=\"weather\",quantile=\"0.95\",}",
            "weather_day_status_seconds{mp_scope=\"weather\",quantile=\"0.98\",}",
            "weather_day_status_seconds{mp_scope=\"weather\",quantile=\"0.99\",}",
            "weather_day_status_seconds{mp_scope=\"weather\",quantile=\"0.999\",}",
            "weather_day_status_seconds_count{mp_scope=\"weather\",}",
            "weather_day_status_seconds_sum{mp_scope=\"weather\",}",
            "# TYPE weather_day_status_seconds_max gauge",
            "# HELP weather_day_status_seconds_max This metric shows the weather status of the day.",
            "weather_day_status_seconds_max{mp_scope=\"weather\",}"
        };

        Stream.of(expected)
                .forEach(text -> assertTrue("Expected: " + text + " to be present in " + metric, metric.contains(text)));
    }

    private void assertJsonFormat(final String metricPath) {
        WebTarget webTarget = client.target(base.toExternalForm());

        String metric = webTarget
                .path(metricPath)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .buildGet()
                .invoke()
                .readEntity(String.class);

        JsonObject expectedJson = Json.createReader(new StringReader(metric)).readObject();

        String[] expected = {
                "count",
                "meanRate",
                "fifteenMinRate",
                "fiveMinRate",
                "oneMinRate",
                "min",
                "max",
                "mean",
                "stddev",
                "p50",
                "p75",
                "p95",
                "p98",
                "p99",
                "p999"
        };

        Stream.of(expected)
                .forEach(text ->
                        assertTrue(
                                "Expected: " + text + " to be present in " + metric,
                                expectedJson.getJsonObject("weather_day_status").get(text) != null));
    }

    @Test
    @Ignore
    // TODO: fix JSON export
    public void testTimedMetricMetadata() {

        WebTarget webTarget = client.target(base.toExternalForm());

//        Response response = webTarget.path("/metrics/application/weather_day_status")
        Response response = webTarget.path("/metrics")
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .build("OPTIONS")
                .invoke();

        final String metaData = response.readEntity(String.class);

        System.out.println("Received: " + metaData);

        JsonObject metadataJson = Json.createReader(new StringReader(metaData)).readObject();

        final String expected = "{\n" +
                "  \"weather_day_status\": {\n" +
                "    \"description\": \"This metric shows the weather status of the day.\",\n" +
                "    \"name\": \"weather_day_status\",\n" +
                "    \"reusable\": false,\n" +
                "    \"tags\": \"\",\n" +
                "    \"type\": \"timer\",\n" +
                "    \"typeRaw\": \"TIMER\",\n" +
                "    \"unit\": \"nanoseconds\"\n" +
                "  }\n" +
                "}";

        JsonObject expectedJson = Json.createReader(new StringReader(expected)).readObject();
        assertEquals(expectedJson.keySet().size(), metadataJson.keySet().size());

        String[] expectedKeys = {
                "description",
                "name",
                "reusable",
                "tags",
                "type",
                "typeRaw",
                "unit",
        };

        Stream.of(expectedKeys)
                .forEach(text ->
                        assertTrue(
                                "Expected: " + text + " to be present in " + expected,
                                expectedJson.getJsonObject("weather_day_status").get(text) != null));
    }
}

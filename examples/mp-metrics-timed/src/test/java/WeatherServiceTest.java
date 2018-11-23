/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.cxf.jaxrs.client.WebClient;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.StringReader;
import java.net.URL;

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

    @Test
    public void testTimedMetric() {
        WebClient.create(base.toExternalForm())
                .path("/weather/day/status")
                .get(String.class);

        final String metricPath = "/metrics/application/weather_day_status";
        assertPrometheusFormat(metricPath);
        assertJsonFormat(metricPath);
    }

    private void assertPrometheusFormat(final String metricPath) {
        final String metric = WebClient.create(base.toExternalForm())
                .path(metricPath)
                .accept(MediaType.TEXT_PLAIN)
                .get(String.class);

        assertTrue(metric.contains("# TYPE application:weather_day_status_seconds summary timer"));
        assertTrue(metric.contains("# TYPE application:weather_day_status_seconds_count timer"));
        assertTrue(metric.contains("application:weather_day_status_seconds_count 1.0"));
        assertTrue(metric.contains("# TYPE application:weather_day_status_rate_per_second timer"));
        assertTrue(metric.contains("application:weather_day_status_rate_per_second"));
        assertTrue(metric.contains("# TYPE application:weather_day_status_one_min_rate_per_second timer"));
        assertTrue(metric.contains("application:weather_day_status_one_min_rate_per_second"));
        assertTrue(metric.contains("# TYPE application:weather_day_status_five_min_rate_per_second timer"));
        assertTrue(metric.contains("application:weather_day_status_five_min_rate_per_second"));
        assertTrue(metric.contains("# TYPE application:weather_day_status_fifteen_min_rate_per_second time"));
        assertTrue(metric.contains("application:weather_day_status_fifteen_min_rate_per_second"));
        assertTrue(metric.contains("# TYPE application:weather_day_status_min_seconds timer"));
        assertTrue(metric.contains("application:weather_day_status_min_seconds"));
        assertTrue(metric.contains("# TYPE application:weather_day_status_max_seconds timer"));
        assertTrue(metric.contains("application:weather_day_status_max_seconds"));
        assertTrue(metric.contains("# TYPE application:weather_day_status_mean_seconds timer"));
        assertTrue(metric.contains("application:weather_day_status_mean_seconds"));
        assertTrue(metric.contains("# TYPE application:weather_day_status_stddev_seconds timer"));
        assertTrue(metric.contains("application:weather_day_status_stddev_seconds"));
        assertTrue(metric.contains("# TYPE application:weather_day_status_seconds timer"));
        assertTrue(metric.contains("application:weather_day_status_seconds{quantile=\"0.5\"}"));
        assertTrue(metric.contains("# TYPE application:weather_day_status_seconds timer"));
        assertTrue(metric.contains("application:weather_day_status_seconds{quantile=\"0.75\"}"));
        assertTrue(metric.contains("# TYPE application:weather_day_status_seconds timer"));
        assertTrue(metric.contains("application:weather_day_status_seconds{quantile=\"0.95\"}"));
        assertTrue(metric.contains("# TYPE application:weather_day_status_seconds timer"));
        assertTrue(metric.contains("application:weather_day_status_seconds{quantile=\"0.98\"}"));
        assertTrue(metric.contains("# TYPE application:weather_day_status_seconds timer"));
        assertTrue(metric.contains("application:weather_day_status_seconds{quantile=\"0.99\"}"));
        assertTrue(metric.contains("# TYPE application:weather_day_status_seconds timer"));
        assertTrue(metric.contains("application:weather_day_status_seconds{quantile=\"0.999\"}"));
    }

    private void assertJsonFormat(final String metricPath) {
        final String metric = WebClient.create(base.toExternalForm())
                .path(metricPath)
                .accept(MediaType.APPLICATION_JSON)
                .get(String.class);

        assertTrue(metric.contains("count"));
        assertTrue(metric.contains("meanRate"));
        assertTrue(metric.contains("fifteenMinRate"));
        assertTrue(metric.contains("fiveMinRate"));
        assertTrue(metric.contains("oneMinRate"));
        assertTrue(metric.contains("min"));
        assertTrue(metric.contains("max"));
        assertTrue(metric.contains("mean"));
        assertTrue(metric.contains("stddev"));
        assertTrue(metric.contains("p50"));
        assertTrue(metric.contains("p75"));
        assertTrue(metric.contains("p95"));
        assertTrue(metric.contains("p98"));
        assertTrue(metric.contains("p99"));
        assertTrue(metric.contains("p999"));

    }

    @Test
    public void testTimedMetricMetadata() {
        final Response response = WebClient.create(base.toExternalForm())
                .path("/metrics/application/weather_day_status")
                .accept(MediaType.APPLICATION_JSON)
                .options();
        final String metaData = response.readEntity(String.class);
        JsonObject metadataJson = Json.createReader(new StringReader(metaData)).readObject();

        final String expected = "{\n" +
                "  \"weather_day_status\": {\n" +
                "    \"description\": \"This metric shows the weather status of the day.\",\n" +
                "    \"displayName\": \"Weather Day Status\",\n" +
                "    \"name\": \"weather_day_status\",\n" +
                "    \"reusable\": false,\n" +
                "    \"tags\": \"\",\n" +
                "    \"type\": \"timer\",\n" +
                "    \"typeRaw\": \"TIMER\",\n" +
                "    \"unit\": \"nanoseconds\"\n" +
                "  }\n" +
                "}";

        JsonObject expectedJson = Json.createReader(new StringReader(expected)).readObject();
        assertEquals(expectedJson, metadataJson);
    }
}

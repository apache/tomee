package org.superbiz.rest; /**
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
import org.superbiz.rest.WeatherService;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.StringReader;
import java.net.URL;

import static org.junit.Assert.assertEquals;

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
    public void testGaugeMetric() {
        final Integer temperature = WebClient.create(base.toExternalForm())
                .path("/weather/day/temperature")
                .get(Integer.class);
        assertEquals(Integer.valueOf(30), temperature);

        final String metricPath = "/metrics/application/weather_day_temperature";
        assertPrometheusFormat(metricPath);
        assertJsonFormat(metricPath);
    }

    private void assertPrometheusFormat(final String metricPath) {
        final String metric = WebClient.create(base.toExternalForm())
                .path(metricPath)
                .accept(MediaType.TEXT_PLAIN)
                .get(String.class);
        assertEquals("# TYPE application:weather_day_temperature_celsius gauge\napplication:weather_day_temperature_celsius{weather=\"temperature\"} 30.0\n", metric);
    }

    private void assertJsonFormat(final String metricPath) {
        final String metric = WebClient.create(base.toExternalForm())
                .path(metricPath)
                .accept(MediaType.APPLICATION_JSON)
                .get(String.class);
        //TODO:FIX Bug
        assertEquals("{\"weather_day_status\":{\"delegate\":{},\"unit\":\"none\",\"count\":1}}", metric);
    }

    @Test
    public void testGaugeMetricMetadata() {
        final Response response = WebClient.create(base.toExternalForm())
                .path("/metrics/application/weather_day_temperature")
                .accept(MediaType.APPLICATION_JSON)
                .options();
        final String metaData = response.readEntity(String.class);
        JsonObject metadataJson = Json.createReader(new StringReader(metaData)).readObject();

        final String expected = "{\n" +
                "  \"weather_day_temperature\": {\n" +
                "    \"unit\": \"celsius\",\n" +
                "    \"displayName\": \"Weather Day Temperature\",\n" +
                "    \"name\": \"weather_day_temperature\",\n" +
                "    \"typeRaw\": \"GAUGE\",\n" +
                "    \"description\": \"This metric shows the day temperature.\",\n" +
                "    \"type\": \"gauge\",\n" +
                "    \"value\": {\n" +
                "      \"unit\": \"celsius\",\n" +
                "      \"displayName\": \"Weather Day Temperature\",\n" +
                "      \"name\": \"weather_day_temperature\",\n" +
                "      \"tagsAsString\": \"weather=\\\"temperature\\\"\",\n" +
                "      \"typeRaw\": \"GAUGE\",\n" +
                "      \"description\": \"This metric shows the day temperature.\",\n" +
                "      \"type\": \"gauge\",\n" +
                "      \"reusable\": false,\n" +
                "      \"tags\": {\n" +
                "        \"weather\": \"temperature\"\n" +
                "      }\n" +
                "    },\n" +
                "    \"reusable\": false,\n" +
                "    \"tags\": \"weather=temperature\"\n" +
                "  }\n" +
                "}";

        JsonObject expectedJson = Json.createReader(new StringReader(expected)).readObject();
        assertEquals(expectedJson, metadataJson);
    }
}

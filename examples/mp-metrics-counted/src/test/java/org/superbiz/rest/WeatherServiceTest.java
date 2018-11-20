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
package org.superbiz.rest;

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
    public void testCountedMetric() {
        final String message = WebClient.create(base.toExternalForm())
                .path("/weather/day/status")
                .get(String.class);
        assertEquals("Hi, today is a sunny day!", message);

        final String metricPath = "/metrics/application/weather_day_status";
        assertPrometheusFormat(metricPath);
        assertJsonFormat(metricPath);
    }

    private void assertPrometheusFormat(final String metricPath) {
        final String metric = WebClient.create(base.toExternalForm())
                .path(metricPath)
                .accept(MediaType.TEXT_PLAIN)
                .get(String.class);
        assertEquals("# TYPE application:weather_day_status counter\napplication:weather_day_status 1.0\n", metric);
    }

    private void assertJsonFormat(final String metricPath) {
        final String metric = WebClient.create(base.toExternalForm())
                .path(metricPath)
                .accept(MediaType.APPLICATION_JSON)
                .get(String.class);
        assertEquals("{\"weather_day_status\":{\"delegate\":{},\"unit\":\"none\",\"count\":1}}", metric);
    }

    @Test
    public void testCountedMetricMetadata() {
        final Response response = WebClient.create(base.toExternalForm())
                .path("/metrics/application/weather_day_status")
                .accept(MediaType.APPLICATION_JSON)
                .options();
        final String metaData = response.readEntity(String.class);
        JsonObject metadataJson = Json.createReader(new StringReader(metaData)).readObject();

        final String expected = "{\n" +
                "  \"weather_day_status\": {\n" +
                "    \"unit\": \"none\",\n" +
                "    \"displayName\": \"Weather Day Status\",\n" +
                "    \"name\": \"weather_day_status\",\n" +
                "    \"typeRaw\": \"COUNTER\",\n" +
                "    \"description\": \"This metric shows the weather status of the day.\",\n" +
                "    \"type\": \"counter\",\n" +
                "    \"value\": {\n" +
                "      \"unit\": \"none\",\n" +
                "      \"displayName\": \"Weather Day Status\",\n" +
                "      \"name\": \"weather_day_status\",\n" +
                "      \"tagsAsString\": \"\",\n" +
                "      \"typeRaw\": \"COUNTER\",\n" +
                "      \"description\": \"This metric shows the weather status of the day.\",\n" +
                "      \"type\": \"counter\",\n" +
                "      \"reusable\": false,\n" +
                "      \"tags\": {\n" +
                "        \n" +
                "      }\n" +
                "    },\n" +
                "    \"reusable\": false,\n" +
                "    \"tags\": \"\"\n" +
                "  }\n" +
                "}";

        JsonObject expectedJson = Json.createReader(new StringReader(expected)).readObject();
        assertEquals(expectedJson, metadataJson);
    }
}

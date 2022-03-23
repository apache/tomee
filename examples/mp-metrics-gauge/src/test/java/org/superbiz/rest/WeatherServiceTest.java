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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
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
import static org.junit.Assert.assertNotNull;
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
    public void testGaugeMetric() {
        WebTarget webTarget = this.client.target(this.base.toExternalForm());
        final Integer temperature = webTarget
                .path("/weather/day/temperature")
                .request()
                .get(Integer.class);
        assertEquals(Integer.valueOf(30), temperature);

        final String metricPath = "/metrics/application/weather_day_temperature";
        assertPrometheusFormat(metricPath);
        assertJsonFormat(metricPath);
    }

    private void assertPrometheusFormat(final String metricPath) {
        WebTarget webTarget = this.client.target(this.base.toExternalForm());
        final String metric = webTarget
                .path(metricPath)
                .request()
                .accept(MediaType.TEXT_PLAIN)
                .get(String.class);
        assertEquals("# TYPE application:weather_day_temperature_celsius gauge\napplication:weather_day_temperature_celsius{weather=\"temperature\"} 30.0\n", metric);
    }

    private void assertJsonFormat(final String metricPath) {
        WebTarget webTarget = this.client.target(this.base.toExternalForm());
        final String metric = webTarget
                .path(metricPath)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .get(String.class);

        assertNotNull(metric);

        JsonObject metricJson = Json.createReader(new StringReader(metric)).readObject();
        final int temperature = metricJson.getInt("weather_day_temperature");
        assertEquals(temperature, 30);
    }

    @Test
    public void testGaugeMetricMetadata() {
        WebTarget webTarget = this.client.target(this.base.toExternalForm());
        final Response response = webTarget
                .path("/metrics/application/weather_day_temperature")
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .options();

        final String metaData = response.readEntity(String.class);
        JsonObject metadataJson = Json.createReader(new StringReader(metaData)).readObject();

        String[] expectedKeys = {
                "description",
                "displayName",
                "name",
                "reusable",
                "tags",
                "type",
                "typeRaw",
                "unit"
        };

        Stream.of(expectedKeys)
                .forEach(text ->
                        assertTrue(
                                "Expected: " + text + " to be present in " + metaData,
                                metadataJson.getJsonObject("weather_day_temperature").get(text) != null));
    }
}

/*
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
package org.superbiz.test;

import java.io.StringReader;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URL;
import org.superbiz.WeatherEndpoint;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class WeatherServiceTest {

    @ArquillianResource
    private URL base;

    private Client client;

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addPackage(WeatherEndpoint.class.getPackage());
    }

    @Before
    public void before() {
        this.client = ClientBuilder.newClient();
    }

    @Test
    @InSequence(1)
    public void testHealthCheckUpService() {
        WebTarget webTarget = this.client.target(this.base.toExternalForm());
        String json = webTarget.path("/health").request(MediaType.APPLICATION_JSON).get().readEntity(String.class);

        JsonArray checks = this.readJson(json).getJsonArray("checks");
        JsonObject data = checks.getJsonObject(0).getJsonObject("data");

        assertEquals("http://api.openweathermap.org/data/2.5/", data.getString("weatherServiceApiUrl"));
        assertEquals("2.5",  data.getString("weatherServiceApiVersion"));
        assertEquals("Your account will become unavailable soon due to limitation of " +
                "your subscription type. Remaining API calls are 1",  data.getString("weatherServiceMessage"));

        assertEquals("OpenWeatherMap", checks.getJsonObject(0).getString("name"));
        assertEquals("UP", checks.getJsonObject(0).getString("state"));
    }

    @Test
    @InSequence(2)
    public void testStatusOfDay() {
        WebTarget webTarget = this.client.target(this.base.toExternalForm());
        Response response = webTarget.path("/weather/day/status").request().get();
        assertEquals("Hi, today is a sunny day!", response.readEntity(String.class));
    }

    @Test
    @InSequence(3)
    public void testHealthCheckDownService() {
        WebTarget webTarget = this.client.target(this.base.toExternalForm());
        String json = webTarget.path("/health").request(MediaType.APPLICATION_JSON).get().readEntity(String.class);

        JsonArray checks = this.readJson(json).getJsonArray("checks");
        JsonObject data = checks.getJsonObject(0).getJsonObject("data");

        assertEquals("Your account is temporary blocked due to exceeding of requests limitation of " +
                        "your subscription type. Please choose the proper subscription http://openweathermap.org/price",
                data.getString("weatherServiceErrorMessage"));

        assertEquals("OpenWeatherMap", checks.getJsonObject(0).getString("name"));
        assertEquals("DOWN", checks.getJsonObject(0).getString("state"));
    }

    @Test
    @InSequence(4)
    public void testStatusOfDayErrorMessage() {
        WebTarget webTarget = this.client.target(this.base.toExternalForm());
        Response response = webTarget.path("/weather/day/status").request().get();
        assertEquals("Weather Service is unavailable", response.readEntity(String.class));
    }

    private JsonObject readJson(String json){
        return Json.createReader(new StringReader(json)).readObject();
    }

    @After
    public void after() {
        this.client.close();
    }
}

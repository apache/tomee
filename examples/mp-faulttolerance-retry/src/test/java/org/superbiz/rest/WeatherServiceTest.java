/*
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

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URL;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class WeatherServiceTest {

    @ArquillianResource
    private URL base;

    private Client client;

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "test.war").addPackage(WeatherService.class.getPackage());
    }

    @Before
    public void before() {
        this.client = ClientBuilder.newClient();
    }

    @Test
    public void testStatusOfDay() {
        WebTarget webTarget = this.client.target(this.base.toExternalForm());
        Response response = webTarget.path("/weather/day/status").request().get();
        assertEquals("Hi, today is a sunny day!", response.readEntity(String.class));
    }

    @Test
    public void testStatusOfWeek() {
        WebTarget webTarget = this.client.target(this.base.toExternalForm());
        Response response = webTarget.path("/weather/week/status").request().get();
        assertEquals("WeatherGateway Service is Busy. Retry later", response.readEntity(String.class));
    }

    @Test
    public void testStatusOfWeekend() {
        WebTarget webTarget = this.client.target(this.base.toExternalForm());
        Response response = webTarget.path("/weather/weekend/status").request().get();
        assertEquals("The Forecast for the Weekend is Scattered Showers.", response.readEntity(String.class));
    }

    @Test
    public void testStatusOfMonth() {
        WebTarget webTarget = this.client.target(this.base.toExternalForm());
        Response response = webTarget.path("/weather/month/status").request().get();
        assertEquals("The Forecast for the Month is Sunny for most of the days", response.readEntity(String.class));
    }

    @Test
    public void testStatusOfYear() {
        WebTarget webTarget = this.client.target(this.base.toExternalForm());
        Response response = webTarget.path("/weather/year/status").request().get();
        assertEquals("WeatherGateway Service Timeout", response.readEntity(String.class));
    }

    @After
    public void after() {
        this.client.close();
    }
}

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
package org.superbiz.rest;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.johnzon.jaxrs.JohnzonProvider;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.openejb.util.NetworkUtil;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Properties;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

@EnableServices(value = "jaxrs", httpDebug = true)
@RunWith(ApplicationComposer.class)
public class GreetingServiceTest {
    private int port;

    @Configuration
    public Properties randomPort() {
        port = NetworkUtil.getNextAvailablePort();
        return new PropertiesBuilder().p("httpejbd.port", Integer.toString(port)).build();
    }

    @Module
    @Classes(value = {GreetingService.class, Greeting.class}, cdi = true) // This enables the CDI magic
    public WebApp app() {
        return new WebApp().contextRoot("test");
    }

    @Test
    public void getXml() throws IOException {
        final String message = WebClient.create("http://localhost:" + port).path("/test/greeting/")
                .accept(MediaType.APPLICATION_XML_TYPE)
                .get(GreetingService.Greet.class).getMessage();
        assertEquals("Hi REST!", message);
    }

    @Test
    public void postXml() throws IOException {
        final String message = WebClient.create("http://localhost:" + port).path("/test/greeting/")
                .accept(MediaType.APPLICATION_XML_TYPE)
                .type(MediaType.APPLICATION_XML_TYPE)
                .post(new Request("Hi REST!"), GreetingService.Greet.class).getMessage();
        assertEquals("hi rest!", message);
    }

    @Test
    public void getJson() throws IOException {
        final String message = WebClient.create("http://localhost:" + port, asList(new JohnzonProvider<GreetingService.Greet>())).path("/test/greeting/")
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(GreetingService.Greet.class).getMessage();
        assertEquals("Hi REST!", message);
    }

    @Test
    public void postJson() throws IOException {
        final String message = WebClient.create("http://localhost:" + port, asList(new JohnzonProvider<GreetingService.Greet>())).path("/test/greeting/")
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .post(new Request("Hi REST!"), GreetingService.Greet.class).getMessage();
        assertEquals("hi rest!", message);
    }
}

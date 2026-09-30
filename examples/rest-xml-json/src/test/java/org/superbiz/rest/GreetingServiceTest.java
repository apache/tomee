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
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class GreetingServiceTest {

    @ArquillianResource
    private URL base;

    @Deployment(testable = false)
    public static WebArchive app() {
        return ShrinkWrap.create(WebArchive.class).addClasses(GreetingService.class, Request.class, Response.class);
    }

    @Test
    public void getXml() throws IOException {
        final String message = WebClient.create(base.toExternalForm()).path("greeting/")
                .accept(MediaType.APPLICATION_XML_TYPE)
                .get(String.class);
        assertEquals("<response><value>Hi REST!</value></response>", message.replaceAll("<\\?[^>]*\\?>", "").trim());
    }

    @Test
    public void postXml() throws IOException {
        final String message = WebClient.create(base.toExternalForm())
                .path("greeting/")
                .type(MediaType.APPLICATION_XML_TYPE)
                .accept(MediaType.APPLICATION_XML_TYPE)
                .post("<request><value>Hi REST!</value></request>", String.class);
        assertEquals("<response><value>hi rest!</value></response>", message.replaceAll("<\\?[^>]*\\?>", "").trim());
    }

    @Test
    public void getJson() throws IOException {
        final String message = WebClient.create(base.toExternalForm())
                .path("greeting/")
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);
        assertEquals("{\"value\":\"Hi REST!\"}", message);
    }

    @Test
    public void postJson() throws IOException {
        final String message = WebClient.create(base.toExternalForm())
                .path("greeting/")
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(new Request("Hi REST!"), String.class);
        assertEquals("{\"value\":\"hi rest!\"}", message);
    }
}

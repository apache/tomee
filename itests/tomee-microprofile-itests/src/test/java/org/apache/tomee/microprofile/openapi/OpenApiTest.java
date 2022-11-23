/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.tomee.microprofile.openapi;

import jakarta.ws.rs.core.Response;
import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.johnzon.jaxrs.JohnzonProvider;
import org.apache.tomee.server.composer.Archive;
import org.apache.tomee.server.composer.TomEE;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OpenApiTest {

    @Test
    public void testApplicationWorks() throws Exception {
        final File appJar = Archive.archive()
                                   .add(this.getClass())
                                   .add(HelloService.class)
                                   .add(SampleApp.class)
                                   .asJar();

        final ArrayList<String> output = new ArrayList<>();
        final TomEE tomee = TomEE.microprofile()
                                 //.debug(5005, true)
                                 .add("webapps/test/WEB-INF/beans.xml", "")
                                 .add("webapps/test/WEB-INF/lib/app.jar", appJar)
                                 .watch("org.apache.tomee.", "\n", output::add)
                                 .update()
                                 .build();



        { // do something
            final Response response = createWebClient(tomee.toURI().toURL())
                .path("/test/api/hello/echo")
                .query("name", "mickey")
                .get();

            assertEquals(200, response.getStatus());

            // assert logs
            assertNotPresent(output, "\tat org."); // no stack traces
        }

        { // hit the openapi endpoint
            final Response response = createWebClient(tomee.toURI().toURL())
                .path("/test/openapi")
                .get();

            assertEquals(200, response.getStatus());

            // assert logs
            assertNotPresent(output, "\tat org."); // no stack traces
        }
    }

    public void assertPresent(final ArrayList<String> output, final String s) {
        final Optional<String> actual = output.stream()
                                              .filter(line -> line.contains(s))
                                              .findFirst();

        assertTrue(actual.isPresent());
    }

    public void assertNotPresent(final ArrayList<String> output, final String s) {
        final Optional<String> actual = output.stream()
                                              .filter(line -> line.contains(s))
                                              .findFirst();

        assertFalse(actual.isPresent());
    }

    private static WebClient createWebClient(final URL base) {
        return WebClient.create(base.toExternalForm(), singletonList(new JohnzonProvider<>()),
                                singletonList(new LoggingFeature()), null);
    }

}

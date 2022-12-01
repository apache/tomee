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

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.johnzon.jaxrs.JohnzonProvider;
import org.apache.tomee.server.composer.Archive;
import org.apache.tomee.server.composer.TomEE;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.junit.Test;
import org.tomitribe.util.IO;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Properties;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OpenApiTest {

    @Test
    public void testSingleApplication() throws Exception {
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

            System.out.println(IO.slurp(((InputStream) response.getEntity())));
        }
    }

    @Test
    public void testMultipleApplications() throws Exception {
        final File helloJar = Archive.archive()
                                   .add(this.getClass())
                                   .add(HelloService.class)
                                   .add(SampleApp.class)
                                   .asJar();

        final File iopsJar = Archive.archive()
                                     .add(IOops.class)
                                     .asDir();

        final File okJar = Archive.archive()
                                     .add(OkService.class)
                                     .add(OopsService.class)
                                     .asJar();

        final ArrayList<String> output = new ArrayList<>();
        final TomEE tomee = TomEE.microprofile()
                                 //.debug(5005, true)
                                 .add("webapps/app/WEB-INF/beans.xml", "")
                                 .add("webapps/app/WEB-INF/lib/app.jar", helloJar)
                                 .add("webapps/app/WEB-INF/lib/ok.jar", okJar)
                                 // make sure we see thoses in WEB-INF/classes
                                 .home(h -> {
                                     try {
                                         IO.copyDirectory(iopsJar, new File(h + "/webapps/app/WEB-INF/classes"));
                                     } catch (IOException e) {
                                         throw new IllegalArgumentException(("Can't copy WEB-INF/classes content"));
                                     }
                                 })
                                 .watch("org.apache.tomee.", "\n", output::add)
                                 .update()
                                 .build();



        { // do something
            final Response response = createWebClient(tomee.toURI().toURL())
                .path("/app/api/hello/echo")
                .query("name", "mickey")
                .get();

            assertEquals(200, response.getStatus());

            // assert logs
            assertNotPresent(output, "\tat org."); // no stack traces
        }

        { // hit the openapi endpoint
            final Response response = createWebClient(tomee.toURI().toURL())
                .path("/app/openapi")
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

    @Path("ok")
    @RequestScoped
    public static class OkService {

        @APIResponses(
            value = {
                @APIResponse(
                    responseCode = "404",
                    description = "We could not find anything",
                    content = @Content(mediaType = "text/plain")),
                @APIResponse(
                    responseCode = "200",
                    description = "We have a list of books",
                    content = @Content(mediaType = "application/json",
                                       schema = @Schema(implementation = Properties.class)))})
        @Operation(summary = "Outputs a list of books",
                   description = "This method outputs a list of books")
        @Timed(name = "get-all-books",
               description = "Monitor the time getAll Method takes",
               unit = MetricUnits.MILLISECONDS,
               absolute = true)
        @GET
        @Path("/get")
        public String getId(@QueryParam( "id" ) Integer id) {
            return "ID is " + id + "    Got it!";
        }

        @Operation(summary = "Outputs a list of books",
                   description = "This method outputs a list of books")
        @GET
        @Path("/show")
        public Response show(@QueryParam( "name" ) String name) {
            return Response.status(200).entity("name: " + name ).build();
        }
    }

    @RequestScoped
    public static class OopsService implements IOops {

        @Override
        public String getId(Integer id) {
            return "ID is " + id + "    Got it!";
        }

        @Override
        public Response show(String name) {
            return Response.status(200).entity("name: " + name ).build();
        }

    }

    @Path("oops")
    public interface IOops {

        @APIResponses(
            value = {
                @APIResponse(
                    responseCode = "404",
                    description = "We could not find anything",
                    content = @Content(mediaType = "text/plain")),
                @APIResponse(
                    responseCode = "200",
                    description = "We have a list of books",
                    content = @Content(mediaType = "application/json",
                                       schema = @Schema(implementation = Properties.class)))})
        @Operation(summary = "Outputs a list of books",
                   description = "This method outputs a list of books")
        @Timed(name = "get-all-books",
               description = "Monitor the time getAll Method takes",
               unit = MetricUnits.MILLISECONDS,
               absolute = true)
        @GET
        @Path("/get")
        public String getId(@QueryParam( "id" ) Integer id);

        @Operation(summary = "Outputs a list of books",
                   description = "This method outputs a list of books")
        @GET
        @Path("/show")
        public Response show(@QueryParam( "name" ) String name);

    }


}

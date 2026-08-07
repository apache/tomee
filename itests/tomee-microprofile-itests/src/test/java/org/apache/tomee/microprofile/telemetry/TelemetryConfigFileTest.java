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
package org.apache.tomee.microprofile.telemetry;

import jakarta.ws.rs.core.Response;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.tomee.server.composer.Archive;
import org.apache.tomee.server.composer.TomEE;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.tomitribe.util.IO;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class TelemetryConfigFileTest {

    private static final String OTEL_CONFIG_YAML = String.join("\n",
            "file_format: \"1.0\"",
            "disabled: false",
            "");

    @Parameterized.Parameters(name = "{0}")
    public static Object[][] distributions() {
        return new Object[][]{
                {"microprofile"},
                {"plus"},
                {"plume"},
        };
    }

    private final String classifier;

    public TelemetryConfigFileTest(final String classifier) {
        this.classifier = classifier;
    }

    private TomEE.Builder distribution() throws Exception {
        switch (classifier) {
            case "microprofile":
                return TomEE.microprofile();
            case "plus":
                return TomEE.plus();
            case "plume":
                // server-composer has no plume() shortcut; resolve it explicitly.
                // "version" is exported as a system property by surefire (pom.xml).
                return TomEE.of("org.apache.tomee:apache-tomee:tar.gz:plume:" + System.getProperty("version"));
            default:
                throw new IllegalArgumentException("Unknown classifier: " + classifier);
        }
    }

    @Test
    public void telemetryAppDeploysWithConfigFileSet() throws Exception {
        final File appJar = Archive.archive()
                .add(TelemetryApp.class)
                .add(WeatherResource.class)
                .add(WeatherGateway.class)
                .asJar();

        final List<String> declarativeConfigErrors = new ArrayList<>();

        final TomEE.Builder builder = distribution()
                .add("conf/otel-config.yaml", OTEL_CONFIG_YAML)
                .add("webapps/test/WEB-INF/beans.xml", "")
                .add("webapps/test/WEB-INF/lib/app.jar", appJar)
                .watch("declarative-config", "\n", declarativeConfigErrors::add);

        builder.home(home -> builder.env("CATALINA_OPTS",
                "-Dtomee.mp.scan=all"
                        + " -Dotel.config.file=" + new File(home, "conf/otel-config.yaml").getAbsolutePath()));

        final TomEE tomee = builder.build();
        try {
            final Response response = WebClient.create(tomee.toURI().toString())
                    .path("/test/api/weather/forecast/London")
                    .get();

            assertEquals(classifier + ": telemetry app did not deploy with otel.config.file set"
                            + " (missing opentelemetry-sdk-extension-declarative-config?) " + declarativeConfigErrors,
                    200, response.getStatus());
            assertEquals("Sunny in London", IO.slurp(response.readEntity(java.io.InputStream.class)));
            assertTrue(classifier + ": OpenTelemetry declarative-config failure in server log " + declarativeConfigErrors,
                    declarativeConfigErrors.isEmpty());
        } finally {
            tomee.shutdown();
        }
    }
}

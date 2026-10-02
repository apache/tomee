/*
 *     Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.apache.openejb.arquillian.tests.jaxrs.cli;

import org.apache.openejb.cli.Bootstrap;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URL;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class TestCLIFromJaxRSTest {
    @ArquillianResource
    private URL base;

    @Deployment
    public static WebArchive war() {
        return ShrinkWrap.create(WebArchive.class, "cli.war").addClasses(TestCLIFromJaxRSTest.class, ValidateMe.class);
    }

    @Test
    public void mapping() {
        final PrintStream originalOut = System.out;
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        try {
            assertEquals(
                    "nice",
                    ClientBuilder.newClient().target(base.toExternalForm()).path("endpoint").request(MediaType.TEXT_PLAIN)
                            .get(String.class));

            assertEquals("BeAUgMQKg6SzYbDM5vtzsQ==" + System.lineSeparator(), out.toString());
        } finally { // shared JVM, don't leak the capture into the next tests
            System.setOut(originalOut);
        }
    }

    @Path("endpoint")
    public static class ValidateMe {
        @GET
        public String get() {
            try {
                new Bootstrap().main(new String[]{"cipher", "my-password"});
            } catch (Exception e) {
                return "fail";
            }
            return "nice";
        }
    }
}

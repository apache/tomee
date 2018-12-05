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
package org.apache.openejb.server.cxf.rs;

import org.apache.openejb.cli.Bootstrap;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.RandomPort;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URL;

import static org.junit.Assert.assertEquals;

@EnableServices("jaxrs")
@Classes(innerClassesAsBean = true)
@RunWith(ApplicationComposer.class)
public class TestCLIFromJaxRSTest {
    @RandomPort("http")
    private URL base;

    @Test
    public void mapping() {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));


        assertEquals(
                "nice",
                ClientBuilder.newClient().target(base.toExternalForm()).path("openejb/endpoint").request(MediaType.TEXT_PLAIN)
                        .get(String.class));

        assertEquals("BeAUgMQKg6SzYbDM5vtzsQ==\n", out.toString());
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

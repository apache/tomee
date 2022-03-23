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

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.RandomPort;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Application;

import static org.junit.Assert.assertEquals;

@EnableServices("jaxrs")
@RunWith(ApplicationComposer.class)
@Classes({ ApplicationStarTest.MyEndpoint.class, ApplicationStarTest.MyApp.class })
public class ApplicationStarTest {
    @RandomPort("http")
    private int port;

    @Test
    public void checkStarIsNotAnIssue() {
        assertEquals("ok", WebClient.create("http://localhost:" + port + "/openejb/").path("test").get(String.class));
    }

    @Path("test")
    public static class MyEndpoint {
        @GET
        public String get() {
            return "ok";
        }
    }

    @ApplicationPath("/*")
    public static class MyApp extends Application {
        @GET
        public String get() {
            return "ok";
        }
    }
}

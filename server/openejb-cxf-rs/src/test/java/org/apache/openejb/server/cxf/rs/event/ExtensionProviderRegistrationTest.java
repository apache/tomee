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
package org.apache.openejb.server.cxf.rs.event;

import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.loader.IO;
import org.apache.openejb.observer.Observes;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.openejb.util.NetworkUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

@EnableServices("jaxrs")
@RunWith(ApplicationComposer.class)
public class ExtensionProviderRegistrationTest {
    private static int port = -1;

    @BeforeClass
    public static void beforeClass() {
        port = NetworkUtil.getNextAvailablePort();
    }

    @Configuration
    public Properties props() {
        return new PropertiesBuilder()
                .p("httpejbd.port", Integer.toString(port))
                .p("observer", "new://Service?class-name=" + Observer.class.getName())
                .build();
    }

    @Module
    @Classes(ServerCreatedEndpoint.class)
    public WebApp war() {
        return new WebApp().contextRoot("foo");
    }

    @Test
    public void checkEvent() throws IOException {
        assertEquals("foo", IO.slurp(new URL("http://localhost:" + port + "/foo/ExtensionProviderRegistrationTest/")));
    }

    @Path("ExtensionProviderRegistrationTest")
    public static class ServerCreatedEndpoint {
        @GET
        public String useless() {
            throw new IllegalArgumentException("foo");
        }
    }

    @Provider
    public static class MyMapper implements ExceptionMapper<IllegalArgumentException> {
        @Override
        public Response toResponse(final IllegalArgumentException e) {
            return Response.ok(e.getMessage()).build();
        }
    }

    public static class Observer {
        public void obs(@Observes final ExtensionProviderRegistration event) {
            event.getProviders().add(new MyMapper());
        }
    }
}

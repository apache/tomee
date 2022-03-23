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
import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testing.RandomPort;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.openejb.util.NetworkUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.inject.Inject;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

@EnableServices("jax-rs")
@RunWith(ApplicationComposer.class)
public class JndiForPojoEndpointsTest {
    @RandomPort("http")
    private int port;

    @Module
    @Classes(cdi = true, value = {JndiEndpoint.class})
    public WebApp war() {
        return new WebApp()
            .contextRoot("foo")
            .addServlet(Application.class.getName(), null, "/api/*");
    }

    @Test
    public void injectionWorked() {
        assertEquals("1", WebClient.create("http://localhost:" + port + "/foo/").path("/api/jndi").get(String.class));
    }

    @Path("jndi")
    public static class JndiEndpoint {
        @Inject
        private Validator val;

        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public int doIt() {
            return val.validate(new ToVal()).size();
        }
    }

    public static class ToVal {
        @NotNull
        private String val;

        public String getVal() {
            return val;
        }
    }
}

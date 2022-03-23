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
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.openejb.util.NetworkUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@EnableServices("jax-rs")
@RunWith(ApplicationComposer.class)
public class CdiConstructorInjectionTest {

    private static int port = -1;

    @BeforeClass
    public static void beforeClass() {
        port = NetworkUtil.getNextAvailablePort();
    }

    @Configuration
    public Properties props() {
        return new PropertiesBuilder().p("httpejbd.port", Integer.toString(port)).build();
    }

    @Module
    @Classes(value = { FullCDI.class, Service.class, CDIAndContext.class }, cdi = true)
    public WebApp war() {
        return new WebApp()
            .contextRoot("app")
            .addServlet("REST Application", Application.class.getName())
            .addInitParam("REST Application", "jakarta.ws.rs.Application", ConstructorApplication.class.getName());
    }

    @Test
    public void standardCDI() {
        assertEquals("service", WebClient.create("http://localhost:" + port + "/app").path("/foo").get(String.class));
    }

    @Test
    public void cdiAndContext() {
        assertEquals("GET", WebClient.create("http://localhost:" + port + "/app").path("/bar").get(String.class));
    }

    @Dependent
    public static class Service {
        public String bar() {
            return "service";
        }
    }

    @Path("/bar")
    public static class CDIAndContext {
        private final HttpServletRequest request;

        @Inject
        public CDIAndContext(final @Context HttpServletRequest request) {
            this.request = request;
        }

        @GET
        public String servletPath() {
            return request.getMethod();
        }
    }

    @Path("/foo")
    @ApplicationScoped
    public static class FullCDI {
        private final Service service;

        public FullCDI() {
            this(null);
        }

        @Inject
        public FullCDI(final Service service) {
            this.service = service;
        }

        @GET
        public String bar() {
            return service.bar();
        }
    }

    public static class ConstructorApplication extends Application {
        @Override
        public Set<Class<?>> getClasses() {
            final Set<Class<?>> classes = new HashSet<Class<?>>();
            classes.add(FullCDI.class);
            classes.add(CDIAndContext.class);
            return classes;
        }
    }
}

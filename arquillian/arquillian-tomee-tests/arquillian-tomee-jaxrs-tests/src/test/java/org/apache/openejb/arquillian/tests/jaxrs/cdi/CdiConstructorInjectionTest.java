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
package org.apache.openejb.arquillian.tests.jaxrs.cdi;

import org.apache.cxf.jaxrs.client.WebClient;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
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
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class CdiConstructorInjectionTest {
    @ArquillianResource
    private URL base;

    @Deployment(testable = false)
    public static WebArchive war() {
        return ShrinkWrap.create(WebArchive.class)
            .addClasses(FullCDI.class, Service.class, CDIAndContext.class, ConstructorApplication.class)
            .addAsWebInfResource(new StringAsset("<beans xmlns=\"https://jakarta.ee/xml/ns/jakartaee\" bean-discovery-mode=\"all\" version=\"4.0\"/>"), "beans.xml")
            .setWebXML(new StringAsset("<web-app xmlns=\"https://jakarta.ee/xml/ns/jakartaee\" version=\"6.0\">" +
                "<servlet>" +
                "<servlet-name>REST Application</servlet-name>" +
                "<servlet-class>" + Application.class.getName() + "</servlet-class>" +
                "<init-param>" +
                "<param-name>jakarta.ws.rs.Application</param-name>" +
                "<param-value>" + ConstructorApplication.class.getName() + "</param-value>" +
                "</init-param>" +
                "</servlet>" +
                "</web-app>"));
    }

    @Test
    public void standardCDI() {
        assertEquals("service", WebClient.create(base.toExternalForm()).path("/foo").get(String.class));
    }

    @Test
    public void cdiAndContext() {
        assertEquals("GET", WebClient.create(base.toExternalForm()).path("/bar").get(String.class));
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

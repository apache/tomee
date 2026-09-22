/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
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

package org.apache.openejb.arquillian.tests.jaxrs.ejb;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.openejb.arquillian.tests.jaxrs.beans.SimpleEJB;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(Arquillian.class)
public class EjbDeploymentTest {
    @ArquillianResource
    private URL base;

    @EJB
    private RESTIsCoolTwo service;

    @Deployment
    public static WebArchive war() {
        return ShrinkWrap.create(WebArchive.class)
            .addClasses(EjbDeploymentTest.class, SimpleEJB.class)
            .addAsWebInfResource(new StringAsset("<openejb-jar>" +
                "<ejb-deployment ejb-name=\"RESTIsCoolTwo\">" +
                "<properties>cxf.jaxrs.skip-provider-scanning = true</properties>" +
                "</ejb-deployment>" +
                "</openejb-jar>"), "openejb-jar.xml");
    }

    @Test
    public void normal() {
        assertNotNull(service);
        assertEquals("ok", service.normal());
    }

    @Test
    public void rest() {
        final String response = WebClient.create(base.toExternalForm()).path("/ejb/rest").accept(MediaType.TEXT_PLAIN_TYPE).get(String.class);
        assertEquals("ok", response);
    }

    @Test
    public void restParameterInjected() {
        String response = WebClient.create(base.toExternalForm()).path("/ejb/param").accept(MediaType.TEXT_PLAIN_TYPE).get(String.class);
        assertEquals("true", response);

        response = WebClient.create(base.toExternalForm()).path("/ejb/param").query("arg", "foo").accept(MediaType.TEXT_PLAIN_TYPE).get(String.class);
        assertEquals("foo", response);
    }

    @Test
    public void restFieldInjected() {
        final Boolean response = WebClient.create(base.toExternalForm()).path("/ejb/field").accept(MediaType.TEXT_PLAIN_TYPE).get(Boolean.class);
        assertEquals(true, response);
    }

    @Stateless
    @Path("/ejb")
    public static class RESTIsCoolTwo {
        @EJB
        private SimpleEJB simpleEJB;
        @jakarta.ws.rs.core.Context
        Request request;

        @Path("/normal")
        @GET
        public String normal() {
            return simpleEJB.ok();
        }

        @Path("/rest")
        @GET
        public String rest() {
            return simpleEJB.ok();
        }

        @Path("/param")
        @GET
        public String param(@QueryParam("arg") @DefaultValue("true") final String p) {
            return p;
        }

        @Path("/field")
        @GET
        public boolean field() {
            return "GET".equals(request.getMethod());
        }
    }
}

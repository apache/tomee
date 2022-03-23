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

package org.apache.openejb.server.cxf.rs;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.config.DeploymentFilterable;
import org.apache.openejb.server.cxf.rs.beans.SimpleEJB;
import org.apache.openejb.util.NetworkUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.embeddable.EJBContainer;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Request;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Properties;

import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DynamicSubclassEjbDeploymentTest {

    private static EJBContainer container;
    private static RESTIsVeryCool service;
    private static int port = -1;

    @BeforeClass
    public static void start() throws Exception {
        port = NetworkUtil.getNextAvailablePort();
        final Properties properties = new Properties();
        properties.setProperty("cxf.jaxrs.skip-provider-scanning", "true");
        properties.setProperty("httpejbd.port", Integer.toString(port));
        properties.setProperty(DeploymentFilterable.CLASSPATH_INCLUDE, ".*openejb-cxf-rs.*");
        properties.setProperty(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true");
        container = EJBContainer.createEJBContainer(properties);
        service = (RESTIsVeryCool) container.getContext().lookup("java:/global/openejb-cxf-rs/RESTIsVeryCool");
    }

    @AfterClass
    public static void close() throws Exception {
        if (container != null) {
            container.close();
        }
    }

    @Test
    public void normal() {
        assertNotNull(service);
        assertEquals("ok", service.normal());
    }

    @Test
    public void rest() {
        final String response = WebClient.create("http://localhost:" + port + "/openejb-cxf-rs")
            .path("/ejb/rest").accept(TEXT_PLAIN_TYPE).get(String.class);
        assertEquals("ok", response);
    }

    @Test
    public void restParameterInjected() {
        String response = WebClient.create("http://localhost:" + port + "/openejb-cxf-rs").path("/ejb/param").accept(TEXT_PLAIN_TYPE).get(String.class);
        assertEquals("true", response);

        response = WebClient.create("http://localhost:" + port + "/openejb-cxf-rs").path("/ejb/param").query("arg", "foo").accept(TEXT_PLAIN_TYPE).get(String.class);
        assertEquals("foo", response);
    }

    @Test
    public void restFieldInjected() {
        final Boolean response = WebClient.create("http://localhost:" + port + "/openejb-cxf-rs").path("/ejb/field").accept(TEXT_PLAIN_TYPE).get(Boolean.class);
        assertEquals(true, response);
    }

    @Stateless
    @Path("/ejb")
    public abstract static class RESTIsVeryCool implements InvocationHandler {

        @EJB
        private SimpleEJB simpleEJB;

        @jakarta.ws.rs.core.Context
        Request request;

        @Path("/normal")
        @GET
        public abstract String normal();

        @Path("/rest")
        @GET
        public abstract String rest();

        @Path("/param")
        @GET
        public String param(@QueryParam("arg") @DefaultValue("true") String p) {
            return p;
        }

        @Path("/field")
        @GET
        public boolean field() {
            return "GET".equals(request.getMethod());
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return simpleEJB.ok();
        }
    }
}

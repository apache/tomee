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

import java.util.Properties;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.embeddable.EJBContainer;
import javax.enterprise.inject.Default;
import javax.naming.Context;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.server.cxf.rs.beans.SimpleEJB;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * @author Romain Manni-Bucau
 */
public class EjbDeploymentTest {
    private static Context context;
    private static RESTIsCool service;

    @BeforeClass public static void start() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true");
        context = EJBContainer.createEJBContainer(properties).getContext();
        service = (RESTIsCool) context.lookup("java:/global/openejb-cxf-rs/RESTIsCool");
    }

    @AfterClass public static void close() throws Exception {
        if (context != null) {
            context.close();
        }
    }

    @Ignore("to be implemented")
    @Test public void deploy() {
        // service works
        assertNotNull(service);
        assertEquals("ok", service.ok(true));

        // rest invocation works
        String response = WebClient.create("http://localhost:4204").path("/ejb/rest").get(String.class);
        assertEquals("ok", response);
    }

    @Path("/ejb")
    @Stateless
    public static class RESTIsCool {
        @javax.ws.rs.core.Context private UriInfo uriInfo;
        @EJB private SimpleEJB simpleEJB;

        @Path("/rest") @GET public String ok(@QueryParam("force") @DefaultValue("false") boolean force) {
            /*if (!(uriInfo != null || force)) {
                return "ko";
            }*/
            return simpleEJB.ok();
        }
    }
}

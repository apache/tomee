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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class SubResourceTest {
    @ArquillianResource
    private URL base;

    @Deployment(testable = false)
    public static WebArchive war() {
        return ShrinkWrap.create(WebArchive.class)
            .addClass(SubResourceTest.class)
            .addAsWebInfResource(new StringAsset("<ejb-jar xmlns=\"https://jakarta.ee/xml/ns/jakartaee\" version=\"4.0\">" +
                "<enterprise-beans>" +
                "<session>" +
                "<ejb-name>Endpoint1</ejb-name>" +
                "<local-bean/>" +
                "<ejb-class>" + Endpoint1.class.getName() + "</ejb-class>" +
                "<session-type>Singleton</session-type>" +
                "</session>" +
                "</enterprise-beans>" +
                "</ejb-jar>"), "ejb-jar.xml");
    }

    @Test
    public void rest() throws IOException {
        final String response = ClientBuilder.newClient()
                .target(base.toExternalForm())
                .path("sub1/sub2/value")
                .request()
                .accept(MediaType.TEXT_PLAIN_TYPE)
                .get(String.class);
        assertEquals("2", response);
    }

    @Path("/sub1")
    public static class Endpoint1 {
        @Context
        private ResourceContext rc;

        @Path("sub{i}")
        public Endpoint2 uno(@PathParam("i") final int discr) {
            if (2 == discr) {
                final Endpoint2 resource = rc.getResource(Endpoint2.class);
                if (resource == null) {
                    throw new IllegalStateException("ResourceContext returned no " + Endpoint2.class.getName());
                }
                return resource;
            }
            return null;
        }
    }

    public static class Endpoint2 {
        @GET
        @Path("/value")
        public int due() {
            return 2;
        }
    }
}

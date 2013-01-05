/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.arquillian.tests.jaxrs.context;

import org.apache.openejb.loader.IO;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.Singleton;
import javax.servlet.ServletConfig;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertEquals;

/**
 * TODO Merge with EjbContextInjectionTest once fixed
 *
 * TOMEE-684 - JAX-RS @Context ServletRequest broken for EJBs in WARs
 *
 * @version $Rev$ $Date$
 */
@RunAsClient
@RunWith(Arquillian.class)
@Ignore
public class EjbContextInjectionServletRequestTest {


    @ArquillianResource
    private URL url;

    @Deployment
    public static WebArchive archive() {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(EjbContextInjectionServletRequestTest.class);
    }

    @Test
    public void rest() throws IOException {
        final String response = IO.slurp(new URL(url.toExternalForm() + "injections/check"));
        assertEquals("true", response);
    }


    @Singleton
    @Path("/injections")
    public static class RsInjection {

        @Context
        private ServletRequest servletRequest;

        @GET
        @Path("/check")
        public boolean check() {
            // Are they injected?
            Assert.assertNotNull("servletRequest", servletRequest);

            // Do the thread locals actually point anywhere?
            Assert.assertTrue(servletRequest.getScheme() != null);

            return true;
        }
    }
}

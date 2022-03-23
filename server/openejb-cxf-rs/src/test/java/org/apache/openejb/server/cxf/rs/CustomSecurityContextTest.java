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

import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.JaxrsProviders;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.openejb.util.NetworkUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.security.Principal;
import java.util.Properties;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;

import static org.junit.Assert.assertEquals;

@EnableServices("jax-rs")
@RunWith(ApplicationComposer.class)
public class CustomSecurityContextTest {

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
    @JaxrsProviders(MySecuCtx.class)
    @Classes(Res.class)
    public WebApp war() {
        return new WebApp()
            .contextRoot("foo");
    }

    @Test
    public void check() throws IOException {
        assertEquals("true", ClientBuilder.newClient()
                .target("http://127.0.0.1:" + port)
                .path("foo/sc")
                .queryParam("role", "therole")
                .request()
                .accept(MediaType.TEXT_PLAIN_TYPE)
                .get(String.class));
        assertEquals("false", ClientBuilder.newClient()
                .target("http://127.0.0.1:" + port)
                .path("foo/sc")
                .queryParam("role", "another")
                .request()
                .accept(MediaType.TEXT_PLAIN_TYPE)
                .get(String.class));
    }

    @Path("sc")
    public static class Res {
        @Context
        private SecurityContext sc;

        @GET
        public boolean f() {
            return sc.isUserInRole("therole");
        }
    }

    @Provider
    public static class MySecuCtx implements ContainerRequestFilter {
        @Override
        public void filter(final ContainerRequestContext containerRequestContext) throws IOException {
            final String role = containerRequestContext.getUriInfo().getQueryParameters().getFirst("role");
            containerRequestContext.setSecurityContext(new SecurityContext() {
                @Override
                public Principal getUserPrincipal() {
                    return null;
                }

                @Override
                public boolean isUserInRole(final String s) {
                    return s.equals(role);
                }

                @Override
                public boolean isSecure() {
                    return false;
                }

                @Override
                public String getAuthenticationScheme() {
                    return SecurityContext.BASIC_AUTH;
                }
            });
        }
    }
}

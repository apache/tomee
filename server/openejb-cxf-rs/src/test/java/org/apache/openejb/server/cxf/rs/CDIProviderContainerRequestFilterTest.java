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

import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.loader.IO;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.RandomPort;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.URL;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import static org.junit.Assert.assertEquals;

@Classes(cdi = true, innerClassesAsBean = true)
@RunWith(ApplicationComposer.class)
@EnableServices("jaxrs")
public class CDIProviderContainerRequestFilterTest {
    @RandomPort("http")
    private URL http;

    @Test
    public void run() throws IOException {
        assertEquals("mock@get", IO.slurp(new URL(http.toExternalForm() + "openejb/e")));
    }

    @Path("e")
    public static class Endpoint {
        @GET
        public String get() {
            return "e";
        }
    }

    @ApplicationScoped
    public static class ABean {
        public String user() {
            return "mock";
        }
    }

    @Provider
    @Priority(Priorities.AUTHENTICATION)
    public static class JWTAuthenticationFilter implements ContainerRequestFilter {
        @Context
        private ResourceInfo resourceInfo;

        @Inject
        private ABean bean;

        @Override
        public void filter(final ContainerRequestContext request) throws IOException {
            if (bean != null) { // EJBContainer tests
                request.abortWith(Response.ok(bean.user() + "@" + resourceInfo.getResourceMethod().getName()).build());
            }
        }
    }
}

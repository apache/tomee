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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.security.servlet;

import org.apache.tomee.security.AbstractTomEESecurityTest;
import org.apache.tomee.security.cdi.TomcatUserIdentityStoreDefinition;
import org.apache.tomee.security.client.BasicAuthFilter;
import org.junit.Test;

import javax.inject.Inject;
import javax.security.enterprise.SecurityContext;
import javax.security.enterprise.authentication.mechanism.http.BasicAuthenticationMechanismDefinition;
import javax.servlet.ServletException;
import javax.servlet.annotation.HttpConstraint;
import javax.servlet.annotation.HttpMethodConstraint;
import javax.servlet.annotation.ServletSecurity;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JaccPermissionServletTest extends AbstractTomEESecurityTest {

    @Test
    public void authenticate() throws Exception {
        final String servlet = getAppUrl() + "/servlet2";
        {
            final String username = "tom";
            final Response response = ClientBuilder.newBuilder().register(new BasicAuthFilter(username, "secret1")).build()
                                                   .target(servlet)
                                                   .request()
                                                   .get();
            assertEquals(200, response.getStatus());
            final String responsePayload = response.readEntity(String.class);
            System.out.println(responsePayload);

            final StringBuilder sb = new StringBuilder(100);
            sb.append("context username: ").append(username).append("\n");
            sb.append("has GET access to /protectedServlet: true").append("\n");
            sb.append("has POST access to /protectedServlet: true");

            assertTrue(responsePayload.contains(sb.toString()));
        }
        {
            final String username = "bob";
            final Response response = ClientBuilder.newBuilder().register(new BasicAuthFilter(username, "secret3")).build()
                                                   .target(servlet)
                                                   .request()
                                                   .get();
            assertEquals(200, response.getStatus());
            final String responsePayload = response.readEntity(String.class);
            System.out.println(responsePayload);

            final StringBuilder sb = new StringBuilder(100);
            sb.append("context username: ").append(username).append("\n");
            sb.append("has GET access to /protectedServlet: true").append("\n");
            sb.append("has POST access to /protectedServlet: false");

            assertTrue(responsePayload.contains(sb.toString()));
        }
    }

    @WebServlet("/protectedServlet")
    @ServletSecurity(value = @HttpConstraint(rolesAllowed = "Manager"),
                     httpMethodConstraints = { @HttpMethodConstraint("GET") })
    @TomcatUserIdentityStoreDefinition
    @BasicAuthenticationMechanismDefinition
    public static class ProtectedServlet extends HttpServlet {

        private static final long serialVersionUID = 1L;

        @Override
        public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

            response.getWriter().write("This is a servlet \n");

        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
            super.doPost(req, resp);
        }

    }

    @WebServlet("/servlet2")
    @TomcatUserIdentityStoreDefinition
    @BasicAuthenticationMechanismDefinition
    public static class Servlet2 extends HttpServlet {

        private static final long serialVersionUID = 1L;

        @Inject
        private SecurityContext securityContext;

        @Override
        public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

            response.getWriter().write("This is a servlet \n");

            String contextName = null;
            if (securityContext.getCallerPrincipal() != null) {
                contextName = securityContext.getCallerPrincipal().getName();
            }

            response.getWriter().write("context username: " + contextName + "\n");

            response.getWriter().println("has GET access to /protectedServlet: "
                                         + securityContext.hasAccessToWebResource("/protectedServlet", "GET"));

            response.getWriter().println("has POST access to /protectedServlet: "
                                         + securityContext.hasAccessToWebResource("/protectedServlet", "POST"));

        }

    }

}

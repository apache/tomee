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

import org.junit.Test;

import javax.enterprise.context.ApplicationScoped;
import javax.security.enterprise.authentication.mechanism.http.FormAuthenticationMechanismDefinition;
import javax.security.enterprise.authentication.mechanism.http.LoginToContinue;
import javax.servlet.ServletException;
import javax.servlet.annotation.HttpConstraint;
import javax.servlet.annotation.ServletSecurity;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.ClientBuilder;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class FormAuthServletTest extends AbstractTomEESecurityTest {
    @Test
    public void authenticate() throws Exception {
        final String servlet = "http://localhost:" + container.getConfiguration().getHttpPort() + "/form";
        assertEquals(200, ClientBuilder.newBuilder().build()
                                       .target(servlet)
                                       .request()
                                       .get().getStatus());
    }

    @ApplicationScoped
    @FormAuthenticationMechanismDefinition(
            loginToContinue = @LoginToContinue()
    )
    public static class ApplicationAuthentication {

    }

    @WebServlet(urlPatterns = "/login")
    public static class LoginServlet extends HttpServlet {
        @Override
        protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
                throws ServletException, IOException {

        }
    }

    @WebServlet(urlPatterns = "/login-error")
    public static class ErrorServlet extends HttpServlet {
        @Override
        protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
                throws ServletException, IOException {

        }
    }

    @WebServlet(urlPatterns = "/form")
    @ServletSecurity(@HttpConstraint(rolesAllowed = "tomcat"))
    public static class TestServlet extends HttpServlet {
        @Override
        protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
                throws ServletException, IOException {
            resp.getWriter().write("ok!");
        }
    }
}

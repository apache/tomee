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

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.tomee.security.AbstractTomEESecurityTest;
import org.apache.tomee.security.cdi.TomcatUserIdentityStoreDefinition;
import org.junit.Test;

import jakarta.security.enterprise.authentication.mechanism.http.FormAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.LoginToContinue;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.HttpConstraint;
import jakarta.servlet.annotation.ServletSecurity;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class FormAuthServletTest extends AbstractTomEESecurityTest {
    @Test
    public void authenticate() throws Exception {
        final WebClient webClient = new WebClient();
        final HtmlPage page = webClient.getPage(getAppUrl() + "/form");
        assertEquals(200, page.getWebResponse().getStatusCode());

        final HtmlForm login = page.getFormByName("login");
        login.getInputByName("j_username").setValueAttribute("tomcat");
        login.getInputByName("j_password").setValueAttribute("tomcat");

        final Page result = login.getInputByName("submit").click();
        assertEquals(200, result.getWebResponse().getStatusCode());
        assertEquals("ok!", result.getWebResponse().getContentAsString());

        assertEquals("ok!", webClient.getPage(getAppUrl() + "/form").getWebResponse().getContentAsString());
    }

    @TomcatUserIdentityStoreDefinition
    @WebServlet(urlPatterns = "/login")
    public static class LoginServlet extends HttpServlet {
        @Override
        protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
                throws ServletException, IOException {
            final String loginPage =
                    "<html>" +
                    "<body>" +
                    "  <h1>Login Page</h1>" +
                    "  <form name=\"login\" method=post action=\"j_security_check\">\n" +
                    "    <p>Username:</p>" +
                    "    <input type=\"text\" name=\"j_username\">\n" +
                    "    <p>Password:</p>" +
                    "    <input type=\"password\" name=\"j_password\">\n" +
                    "    <input type=\"submit\" name=\"submit\" value=\"Submit\">\n" +
                    "    <input type=\"reset\" value=\"Reset\">" +
                    "  </form>" +
                    "</body>" +
                    "</html>";
            resp.getWriter().write(loginPage);
        }
    }

    @WebServlet(urlPatterns = "/login-error")
    public static class ErrorServlet extends HttpServlet {
        @Override
        protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
                throws ServletException, IOException {

        }
    }

    @TomcatUserIdentityStoreDefinition
    @WebServlet(urlPatterns = "/form")
    @ServletSecurity(@HttpConstraint(rolesAllowed = "tomcat"))
    @FormAuthenticationMechanismDefinition(
            loginToContinue = @LoginToContinue()
    )
    public static class TestServlet extends HttpServlet {
        @Override
        protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
                throws ServletException, IOException {
            resp.getWriter().write("ok!");
        }
    }
}

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


package org.apache.tomee.security.itest;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import javax.enterprise.context.ApplicationScoped;
import javax.servlet.ServletException;
import javax.servlet.annotation.HttpConstraint;
import javax.servlet.annotation.ServletSecurity;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.johnzon.jaxrs.JohnzonProvider;
import org.apache.tomee.security.cdi.TomcatUserIdentityStoreDefinition;
import org.apache.tomee.server.composer.Archive;
import org.apache.tomee.server.composer.TomEE;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LoginToContinueTest {

    @Test
    public void testAnnotation() throws Exception {
        final File appJar = Archive.archive()
                                   .add(this.getClass())
                                   .add(ColorService.class)
                                   .add(Api.class)
                                   .add(LoginServlet.class)
                                   .add(FormLoginServlet.class)
                                   .add(ErrorServlet.class)
                                   .add(TestServlet.class)
                                   .add(AuthMechanism.class)
                                   .asJar();

        final ArrayList<String> output = new ArrayList<>();
        final TomEE tomee = TomEE.microprofile()
                                 //.debug(5005, true)
                                 .add("webapps/test/WEB-INF/beans.xml", "")
                                 .add("webapps/test/WEB-INF/lib/app.jar", appJar)
                                 .watch("org.apache.tomee.", "\n", output::add)
                                 .update()
                                 .build();


        { // do something
            final WebClient webClient = createWebClient(tomee.toURI().resolve("/test").toURL());
            final Response response = webClient.reset()
                                               .path("/colors")
                                               .header("Content-Type", "application/json")
                                               .get();
            assertEquals(200, response.getStatus());
        }

        // assert logs
        assertNotPresent(output, "\tat org."); // no stack traces

        {
            final com.gargoylesoftware.htmlunit.WebClient webClient = new com.gargoylesoftware.htmlunit.WebClient();
            final HtmlPage page = webClient.getPage(tomee.toURI().resolve("/test/auth-app").toURL());
            assertEquals(200, page.getWebResponse().getStatusCode());

            final HtmlForm login = page.getFormByName("login");
            login.getInputByName("token").setValueAttribute("1234ABCD");

            final Page result = login.getInputByName("submit").click();
            assertEquals(200, result.getWebResponse().getStatusCode());
            assertEquals("ok!", result.getWebResponse().getContentAsString());
        }
    }

    @Test
    public void testInterface() throws Exception {
        final File appJar = Archive.archive()
                                   .add(this.getClass())
                                   .add(ColorService.class)
                                   .add(Api.class)
                                   .add(LoginServlet.class)
                                   .add(FormLoginServlet.class)
                                   .add(ErrorServlet.class)
                                   .add(TestServlet.class)
                                   .add(AnotherAuthMechanism.class)
                                   .add(AnotherAuthMechanism.FakeAnnotationHolder.class)
                                   .asJar();

        final ArrayList<String> output = new ArrayList<>();
        final TomEE tomee = TomEE.microprofile()
                                 //.debug(5005, true)
                                 .add("webapps/test/WEB-INF/beans.xml", "")
                                 .add("webapps/test/WEB-INF/lib/app.jar", appJar)
                                 .watch("org.apache.tomee.", "\n", output::add)
                                 .update()
                                 .build();


        { // do something
            final WebClient webClient = createWebClient(tomee.toURI().resolve("/test").toURL());
            final Response response = webClient.reset()
                                               .path("/colors")
                                               .header("Content-Type", "application/json")
                                               .get();
            assertEquals(200, response.getStatus());
        }

        // assert logs
        assertNotPresent(output, "\tat org."); // no stack traces

        {
            final com.gargoylesoftware.htmlunit.WebClient webClient = new com.gargoylesoftware.htmlunit.WebClient();
            final HtmlPage page = webClient.getPage(tomee.toURI().resolve("/test/auth-app").toURL());
            assertEquals(200, page.getWebResponse().getStatusCode());

            final HtmlForm login = page.getFormByName("login");
            login.getInputByName("token").setValueAttribute("1234ABCD");

            final Page result = login.getInputByName("submit").click();
            assertEquals(200, result.getWebResponse().getStatusCode());
            assertEquals("ok!", result.getWebResponse().getContentAsString());
        }
    }

    @Test
    public void testFormDefinition() throws Exception {
        final File appJar = Archive.archive()
                                   .add(this.getClass())
                                   .add(ColorService.class)
                                   .add(Api.class)
                                   .add(LoginServlet.class)
                                   .add(FormLoginServlet.class)
                                   .add(ErrorServlet.class)
                                   .add(TestServlet.class)
                                   .add(FormAuthConfig.class)
                                   .asJar();

        final ArrayList<String> output = new ArrayList<>();
        final TomEE tomee = TomEE.microprofile()
                                 //.debug(5005, true)
                                 .add("webapps/test/WEB-INF/beans.xml", "")
                                 .add("webapps/test/WEB-INF/lib/app.jar", appJar)
                                 .watch("org.apache.tomee.", "\n", output::add)
                                 .update()
                                 .build();


        { // do something
            final WebClient webClient = createWebClient(tomee.toURI().resolve("/test").toURL());
            final Response response = webClient.reset()
                                               .path("/colors")
                                               .header("Content-Type", "application/json")
                                               .get();
            assertEquals(200, response.getStatus());
        }

        // assert logs
        assertNotPresent(output, "\tat org."); // no stack traces

        {
            final com.gargoylesoftware.htmlunit.WebClient webClient = new com.gargoylesoftware.htmlunit.WebClient();
            final HtmlPage page = webClient.getPage(tomee.toURI().resolve("/test/auth-app").toURL());
            assertEquals(200, page.getWebResponse().getStatusCode());

            final HtmlForm login = page.getFormByName("login");
            login.getInputByName("j_username").setValueAttribute("testuser");
            login.getInputByName("j_password").setValueAttribute("1234ABCD");

            final Page result = login.getInputByName("submit").click();
            assertEquals(200, result.getWebResponse().getStatusCode());
            assertEquals("ok!", result.getWebResponse().getContentAsString());
        }
    }

    public void assertPresent(final ArrayList<String> output, final String s) {
        final Optional<String> actual = output.stream()
                .filter(line -> line.contains(s))
                .findFirst();

        assertTrue(actual.isPresent());
    }
    public void assertNotPresent(final ArrayList<String> output, final String s) {
        final Optional<String> actual = output.stream()
                .filter(line -> line.contains(s))
                .findFirst();

        assertTrue(!actual.isPresent());
    }

    private static WebClient createWebClient(final URL base) {
        return WebClient.create(base.toExternalForm(), singletonList(new JohnzonProvider<>()),
                                singletonList(new LoggingFeature()), null);
    }

    @ApplicationPath("/api")
    public class Api extends Application {
    }

    @Path("/colors")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApplicationScoped
    public static class ColorService {

        @GET
        public String getColor() {
            return "Green";
        }
    }

    @TomcatUserIdentityStoreDefinition
    @WebServlet(urlPatterns = "/login-app")
    public static class LoginServlet extends HttpServlet {
        @Override
        protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
            final String loginPage =
                "<html>" +
                "<body>" +
                "  <h1>Login Page</h1>" +
                "  <form name=\"login\" method=post action=\"login-jwt\">\n" +
                "    <p>JWT Token:</p>" +
                "    <input type=\"text\" name=\"token\">\n" +
                "    <input type=\"submit\" name=\"submit\" value=\"Submit\">\n" +
                "    <input type=\"reset\" value=\"Reset\">" +
                "  </form>" +
                "</body>" +
                "</html>";
            resp.getWriter().write(loginPage);
        }
    }

    @TomcatUserIdentityStoreDefinition
    @WebServlet(urlPatterns = "/login-form-app")
    public static class FormLoginServlet extends HttpServlet {
        @Override
        protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
            final String loginPage =
                    "<html>" +
                    "<body>" +
                    "  <h1>Login Page</h1>" +
                    "  <form name=\"login\" method=post action=\"j_security_check\">\n" +
                    "    <p>Credentials:</p>" +
                    "    <input type=\"text\" name=\"j_username\">\n" +
                    "    <input type=\"password\" name=\"j_password\">\n" +
                    "    <input type=\"submit\" name=\"submit\" value=\"Submit\">\n" +
                    "    <input type=\"reset\" value=\"Reset\">" +
                    "  </form>" +
                    "</body>" +
                    "</html>";
            resp.getWriter().write(loginPage);
        }
    }

    @WebServlet(urlPatterns = "/login-error-app")
    public static class ErrorServlet extends HttpServlet {
        @Override
        protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {

        }
    }

    @TomcatUserIdentityStoreDefinition
    @WebServlet(urlPatterns = "/auth-app")
    @ServletSecurity(@HttpConstraint(rolesAllowed = "tomcat"))
    public static class TestServlet extends HttpServlet {
        @Override
        protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
            resp.getWriter().write("ok!");
        }
    }

}

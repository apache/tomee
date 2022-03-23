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
package org.apache.openejb.server.httpd;

import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.apache.openejb.loader.IO;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.openejb.util.NetworkUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

@EnableServices("httpejbd")
@RunWith(ApplicationComposer.class)
public class ServletRegistrationTest {

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
    @Classes({TestServlet.class, TestServlet2.class, TestServlet3.class, TestServlet4.class, SomeEjb.class})
    public WebApp app() {
        return new WebApp()
            .contextRoot("servlet")
            .addServlet("test", TestServlet.class.getName(), "/touch");
    }

    @Test
    public void touch() throws IOException {
        assertEquals("touched", IO.slurp(new URL("http://localhost:" + port + "/servlet/touch")));
    }

    @Test
    public void discover() throws IOException {
        assertEquals("discovered", IO.slurp(new URL("http://localhost:" + port + "/servlet/discover")));
    }

    @Test
    public void wildcard() throws IOException {
        assertEquals("wildcard", IO.slurp(new URL("http://localhost:" + port + "/servlet/bar/openejb")));
    }

    @Test
    public void injections() throws IOException {
        assertEquals("true", IO.slurp(new URL("http://localhost:" + port + "/servlet/injection")));
    }

    public static class TestServlet extends HttpServlet {
        @Override
        protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
            resp.getWriter().write("touched");
        }
    }

    @WebServlet(urlPatterns = "/discover")
    public static class TestServlet2 extends HttpServlet {
        @Override
        protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
            resp.getWriter().write("discovered");
        }
    }

    @WebServlet(urlPatterns = "/bar/*")
    public static class TestServlet3 extends HttpServlet {
        @Override
        protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
            resp.getWriter().write("wildcard");
        }
    }

    @WebServlet(urlPatterns = "/injection")
    public static class TestServlet4 extends HttpServlet {
        @EJB
        private SomeEjb ejb;

        @Override
        protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
            resp.getWriter().write(Boolean.toString(ejb != null));
        }
    }

    @Singleton
    public static class SomeEjb {

    }
}

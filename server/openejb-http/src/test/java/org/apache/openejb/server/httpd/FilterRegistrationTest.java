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
import org.apache.openejb.junit.Classes;
import org.apache.openejb.junit.EnableServices;
import org.apache.openejb.junit.Module;
import org.apache.openejb.loader.IO;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@EnableServices({ "httpejbd" })
@RunWith(ApplicationComposer.class)
public class FilterRegistrationTest {
    @Module
    public WebApp app() {
        return new WebApp()
                .contextRoot("filter")
                .addServlet("test", TestServlet.class.getName(), "/touch")
                .addFilter("filter", TestFilter.class.getName(), "/touch")
                .addFilter("filter2", TestFilter2.class.getName(), "/touch");
    }

    @Test
    public void touch() throws IOException {
        assertEquals("http://ok/filter/touch", IO.slurp(new URL("http://localhost:4204/filter/touch")));
        assertTrue(TestFilter.init);
        assertTrue(TestFilter.ok);
        assertTrue(TestFilter2.ok);
    }

    private static class TestServlet extends HttpServlet {
        @Override
        protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
            resp.getWriter().write(req.getRequestURI());
        }
    }

    private static class TestFilter implements Filter {
        public static boolean ok = false;
        private static boolean init = false;

        @Override
        public void init(final FilterConfig filterConfig) throws ServletException {
            init = true;
        }

        @Override
        public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
            ok = true;
            try {
                chain.doFilter(new HttpRequestImpl(new URI("http://ok/filter/touch")) {
                    @Override
                    public java.net.URI getURI() {
                        return super.getSocketURI();
                    }

                    @Override
                    public String getMethod() {
                        return "GET";
                    }

                }, response);
            } catch (URISyntaxException e) {
                throw new ServletException(e);
            }
        }

        @Override
        public void destroy() {
            System.out.println("destroyed");
        }
    }

    private static class TestFilter2 implements Filter {
        public static boolean ok = false;

        @Override
        public void init(final FilterConfig filterConfig) throws ServletException {
            // no-op
        }

        @Override
        public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
            ok = true;
            chain.doFilter(request, response);
        }

        @Override
        public void destroy() {
            // no-op
        }
    }
}

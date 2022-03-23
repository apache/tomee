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
import org.junit.Test;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class SimpleServletTest extends AbstractTomEESecurityTest {
    @Test
    public void testWebApp() throws Exception {
        final Client client = ClientBuilder.newBuilder().build();
        final Response response =
                client.target(getAppUrl() + "/servlet")
                      .request()
                      .get();
        assertEquals(200, response.getStatus());
    }

    @TomcatUserIdentityStoreDefinition
    @WebServlet(urlPatterns = "/servlet")
    public static class TestServlet extends HttpServlet {
        @Override
        protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
                throws ServletException, IOException {
            resp.getWriter().write("ok!");
        }
    }
}

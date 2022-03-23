/**
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

package org.apache.openejb.arquillian.tests.getresources;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "get-resources", urlPatterns = "/get-resources")
public class GetResourcesServletExporter extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/plain");
        final PrintWriter writer = resp.getWriter();
        writer.write("foundFromListener=" + GetResourcesHolder.RESOURCE_NUMBER);

        try {
            // all this tests will throw an exception if it fails
            getServletContext().getResource("/WEB-INF/classes/config/test.getresources").openStream().close();
            getServletContext().getResourceAsStream("/WEB-INF/classes/config/test.getresources").close();
            getServletContext().getResourcePaths("/WEB-INF/classes/config/").iterator().next();
            if (!new File(getServletContext().getRealPath("/WEB-INF/classes/config/test.getresources")).exists()) {
                throw new RuntimeException(); // fail
            }

            writer.write("servletContextGetResource=ok");
        } catch (Exception e) {
            writer.write("servletContextGetResource=ko");
        }
    }
}

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

package org.apache.tomee.webapp.installer;

import org.apache.tomee.installer.Installer;
import org.apache.tomee.installer.Paths;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class InstallerServlet extends HttpServlet {

    private String escape(final String str) {
        if (str == null) {
            return "";
        }
        return str.replaceAll("\"", "\\\\\"").replaceAll("\\\\", "\\\\\\\\");
    }

    private String getJsonList(final List<Map<String, String>> list) {
        final StringBuffer sb = new StringBuffer();
        for (final Map<String, String> entry : list) {
            sb.append(String.format("{\"key\": \"%s\", \"value\": \"%s\"},",
                    entry.get("key"), escape(entry.get("value"))
            ));
        }
        if (!list.isEmpty()) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return "[" + sb + "]";
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final ServletContext ctx = req.getServletContext();
        final String rootPath = ctx.getRealPath("/");
        final Runner installer = new Runner(new Installer(new Paths(new File(rootPath))));
        resp.setContentType("application/json");
        resp.getOutputStream().print(getJsonList(installer.execute(false)));
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final ServletContext ctx = req.getServletContext();
        final String rootPath = ctx.getRealPath("/");
        final Runner installer = new Runner(new Installer(new Paths(new File(rootPath))));
        if (req.getParameter("catalinaBaseDir") != null && "".equals(req.getParameter("catalinaBaseDir").trim())) {
            installer.setCatalinaBaseDir(req.getParameter("catalinaBaseDir").trim());
        }
        if (req.getParameter("catalinaHome") != null && "".equals(req.getParameter("catalinaHome").trim())) {
            installer.setCatalinaHome(req.getParameter("catalinaHome").trim());
        }
        if (req.getParameter("serverXmlFile") != null && "".equals(req.getParameter("serverXmlFile").trim())) {
            installer.setServerXmlFile(req.getParameter("serverXmlFile").trim());
        }
        resp.setContentType("application/json");
        resp.getOutputStream().print(getJsonList(installer.execute(true)));
    }
}

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

package org.apache.tomee.webapp.servlet;

import com.google.gson.Gson;
import javax.inject.Inject;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.assembler.Deployer;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class DeployServlet extends HttpServlet {
    public static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB, DeployServlet.class);

    @Inject
    private Deployer deployer;

    @Override
    protected void doPost(HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final String path;
        try {
            final AppInfo info = deployer.deploy(req.getParameter("path"));

            // the path is translated from the parameter to a file path
            // the input can be "mvn:org.superbiz/rest-example.1.0/war" for instance or an http url
            path = info.path;
        } catch (OpenEJBException e) {
            throw new OpenEJBRuntimeException(e); // TODO: show back to the user the exception
        }

        final File file = new File(path);
        final Map<String, Object> result = new HashMap<String, Object>();
        result.put("deployed", Boolean.TRUE);
        // TODO: is it needed or do we use the input path since it is more explicit for the user?
        result.put("file", file.getAbsolutePath());

        resp.setContentType("text/plain");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(new Gson().toJson(result));
    }
}

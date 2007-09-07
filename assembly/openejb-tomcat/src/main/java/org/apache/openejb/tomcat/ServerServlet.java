/**
 *
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
package org.apache.openejb.tomcat;

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.ServiceException;
import org.apache.openejb.server.ejbd.EjbServer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ServerServlet extends HttpServlet {
    private EjbServer ejbServer;

    public void init(ServletConfig config) {
        ejbServer = SystemInstance.get().getComponent(EjbServer.class);
    }

    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println(request.getContextPath() + " | " + request.getServletPath() + " | " + request.getPathInfo());
        ServletInputStream in = request.getInputStream();
        ServletOutputStream out = response.getOutputStream();
        try {
            ejbServer.service(in, out);
        } catch (ServiceException e) {
            e.printStackTrace();
            throw new ServletException("ServerService error: " + ejbServer.getClass().getName() + " -- " + e.getMessage(), e);
        }
    }
}

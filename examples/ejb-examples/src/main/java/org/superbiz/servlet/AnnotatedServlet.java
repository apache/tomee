/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.servlet;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.naming.InitialContext;
import jakarta.naming.NamingException;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.sql.DataSource;
import java.io.IOException;

public class AnnotatedServlet extends HttpServlet {

    @EJB
    private AnnotatedEJBLocal localEJB;

    @EJB
    private AnnotatedEJBRemote remoteEJB;

    @EJB
    private AnnotatedEJB localbeanEJB;

    @Resource
    private DataSource ds;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");
        ServletOutputStream out = response.getOutputStream();

        out.println("LocalBean EJB");
        out.println("@EJB=" + localbeanEJB);
        if (localbeanEJB != null) {
            out.println("@EJB.getName()=" + localbeanEJB.getName());
            out.println("@EJB.getDs()=" + localbeanEJB.getDs());
        }
        out.println("JNDI=" + lookupField("localbeanEJB"));
        out.println();

        out.println("Local EJB");
        out.println("@EJB=" + localEJB);
        if (localEJB != null) {
            out.println("@EJB.getName()=" + localEJB.getName());
            out.println("@EJB.getDs()=" + localEJB.getDs());
        }
        out.println("JNDI=" + lookupField("localEJB"));
        out.println();

        out.println("Remote EJB");
        out.println("@EJB=" + remoteEJB);
        if (localEJB != null) {
            out.println("@EJB.getName()=" + remoteEJB.getName());
        }
        out.println("JNDI=" + lookupField("remoteEJB"));
        out.println();

        out.println("DataSource");
        out.println("@Resource=" + ds);
        out.println("JNDI=" + lookupField("ds"));
    }

    private Object lookupField(String name) {
        try {
            return new InitialContext().lookup("java:comp/env/" + getClass().getName() + "/" + name);
        } catch (NamingException e) {
            return null;
        }
    }
}

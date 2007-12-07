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
package org.superbiz.servlet;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;

public class AnnotatedServlet extends HttpServlet {
    @EJB
    private AnnotatedEJBLocal localEJB;

    @EJB
    private AnnotatedEJBRemote remoteEJB;

    @Resource
    private DataSource ds;


    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");
        ServletOutputStream out = response.getOutputStream();

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

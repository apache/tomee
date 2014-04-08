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
package org.apache.openejb.test.servlet;

import javax.ejb.EJB;
import javax.ejb.EJBAccessException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintStream;
import java.security.Principal;
import java.lang.reflect.Method;

import junit.framework.Assert;

public class SecureServlet extends HttpServlet {
    @EJB
    private SecureEJBLocal secureEJBLocal;

    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");
        ServletOutputStream out = response.getOutputStream();
        PrintStream printStream = new PrintStream(out);

        String methodName = request.getParameter("method");
        if (methodName == null) {
            testAll(request, printStream);
        } else {
            try {
                Method method = getClass().getMethod(methodName, HttpServletRequest.class);
                method.invoke(this, request);
            } catch (Throwable e) {
                // response.setStatus(580);
                printStream.println("FAILED");
                e.printStackTrace(printStream);
            }
        }
        printStream.flush();
    }

    public void testAll(HttpServletRequest request, PrintStream printStream) {
        for (Method method : EjbServlet.class.getMethods()) {
            if (!method.getName().startsWith("invoke")) continue;

            try {
                method.invoke(this);
                printStream.println(method.getName() + " PASSED");
            } catch (Throwable e) {
                printStream.println(method.getName() + " FAILED");
                e.printStackTrace(printStream);
                printStream.flush();
            }
            printStream.println();
        }
    }

    public void invokeGetCallerPrincipal(HttpServletRequest request) {
        // Servlet environment
        Principal principal = request.getUserPrincipal();
        Assert.assertNotNull(principal);
        Assert.assertEquals("user", principal.getName());

        // EJB environment
        principal = secureEJBLocal.getCallerPrincipal();
        Assert.assertNotNull(principal);
        Assert.assertEquals("user", principal.getName());
    }

    public void invokeIsCallerInRole(HttpServletRequest request) {
        // Servlet environment
        Assert.assertTrue(request.isUserInRole("user"));
        Assert.assertFalse(request.isUserInRole("manager"));
        Assert.assertFalse(request.isUserInRole("UNKNOWN"));
        Assert.assertFalse(request.isUserInRole("runas"));

        // EJB environment
        Assert.assertTrue(secureEJBLocal.isCallerInRole("user"));
        Assert.assertFalse(secureEJBLocal.isCallerInRole("manager"));
        Assert.assertFalse(secureEJBLocal.isCallerInRole("UNKNOWN"));
        Assert.assertFalse(secureEJBLocal.isCallerInRole("runas"));
    }

    public void invokeIsAllowed(HttpServletRequest request) {
        try {
            secureEJBLocal.allowUserMethod();
        } catch(EJBAccessException e) {
            Assert.fail("Method allowUserMethod() NOT ALLOWED");
        }

        try {
            secureEJBLocal.allowManagerMethod();
            Assert.fail("Method allowManagerMethod() ALLOWED");
        } catch(EJBAccessException expected) {
        }

        try {
            secureEJBLocal.allowUnknownMethod();
            Assert.fail("Method allowUnknownMethod() ALLOWED");
        } catch(EJBAccessException expected) {
        }

        try {
            secureEJBLocal.allowRunasMethod();
            Assert.fail("Method allowRunasMethod() ALLOWED");
        } catch(EJBAccessException expected) {
        }

        try {
            secureEJBLocal.denyAllMethod();
            Assert.fail("Method denyAllMethod() ALLOWED");
        } catch(EJBAccessException expected) {
        }
    }
}
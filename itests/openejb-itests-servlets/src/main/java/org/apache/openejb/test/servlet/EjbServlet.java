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

import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.ejb.EJB;

import org.apache.openejb.test.stateless.BasicStatelessBusinessLocal;
import org.junit.Assert;

public class EjbServlet extends HttpServlet {
    @EJB
    private BasicStatelessBusinessLocal statelessBusinessLocal;

    protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");
        final ServletOutputStream out = response.getOutputStream();
        final PrintStream printStream = new PrintStream(out);

        final String methodName = request.getParameter("method");
        if (methodName == null) {
            testAll(printStream);
        } else {
            try {
                final Method method = getClass().getMethod(methodName);
                method.invoke(this);
            } catch (final Throwable e) {
                // response.setStatus(580);
                printStream.println("FAILED");
                e.printStackTrace(printStream);
            }
        }
        printStream.flush();
    }

    public void testAll(final PrintStream printStream) {
        for (final Method method : EjbServlet.class.getMethods()) {
            if (!method.getName().startsWith("invoke")) continue;

            try {
                method.invoke(this);
                printStream.println(method.getName() + " PASSED");
            } catch (final Throwable e) {
                printStream.println(method.getName() + " FAILED");
                e.printStackTrace(printStream);
                printStream.flush();
            }
            printStream.println();
        }
    }

    public void invokeBusinessMethod() {
        Assert.assertEquals("oof", statelessBusinessLocal.businessMethod("foo"));
    }
}

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
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.ejb.EJB;

import org.apache.openejb.test.stateless.BasicStatelessBusinessLocal;
import junit.framework.Assert;

public class EjbServlet extends HttpServlet {
    @EJB
    private BasicStatelessBusinessLocal statelessBusinessLocal;

    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");
        ServletOutputStream out = response.getOutputStream();
        PrintStream printStream = new PrintStream(out);

        String methodName = request.getParameter("method");
        if (methodName == null) {
            testAll(printStream);
        } else {
            try {
                Method method = getClass().getMethod(methodName);
                method.invoke(this);
            } catch (Throwable e) {
                // response.setStatus(580);
                printStream.println("FAILED");
                e.printStackTrace(printStream);
            }
        }
        printStream.flush();
    }

    public void testAll(PrintStream printStream) {
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

    public void invokeBusinessMethod() {
        Assert.assertEquals("oof", statelessBusinessLocal.businessMethod("foo"));
    }
}

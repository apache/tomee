/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.arquillian.tests.tomcat.contextxml;


import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.OperationNotSupportedException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NamingServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final PrintWriter writer = resp.getWriter();
        final String testToExecute = req.getParameter("test");

        try {
            final Method method = this.getClass().getDeclaredMethod(testToExecute);
            method.invoke(this);
            writer.println(testToExecute + "=true");
        } catch (Exception ex) {
            final Throwable rootCause = ex instanceof InvocationTargetException ? ex.getCause() : ex;
            writer.println(testToExecute + "=false");
            rootCause.printStackTrace(writer);
        }
    }

    public void closeNamingContextAndExpectNoException() throws Exception {
        final InitialContext initialContext = new InitialContext();
        final Context compEnv = (Context) initialContext.lookup("java:comp/env");
        compEnv.close();
    }

    public void closeNamingContextAndExpectOperationNotSupportedException() throws Exception {
        try {
            final InitialContext initialContext = new InitialContext();
            final Context compEnv = (Context) initialContext.lookup("java:comp/env");
            compEnv.close();

            throw new IllegalStateException("Context::close() should have thrown OperationNotSupportedException");
        } catch (OperationNotSupportedException ex) {
            //Do nothing, expected
        } catch (Exception ex) {
            throw new IllegalStateException("Context::close() should have thrown OperationNotSupportedException instead of " + ex.getClass(), ex);
        }
    }
}

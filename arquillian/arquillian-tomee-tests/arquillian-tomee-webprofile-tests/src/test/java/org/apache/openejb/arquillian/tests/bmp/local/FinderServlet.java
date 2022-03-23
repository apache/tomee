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


package org.apache.openejb.arquillian.tests.bmp.local;

import javax.naming.InitialContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class FinderServlet extends HttpServlet {

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final FinderTestHome testHome;
        InitialContext ctx = null;
        try {
            ctx = new InitialContext();
            testHome = (FinderTestHome) ctx.lookup("java:comp/env/ejb/FinderTest");
            resp.getWriter().println(testHome.create().runTest());
            resp.getWriter().flush();
        } catch (final Exception e) {
            throw new ServletException(e);
        } finally {
            try {
                if (ctx != null) {
                    ctx.close();
                }
            } catch (final Exception e) {
                throw new ServletException(e);
            }
        }
    }
}

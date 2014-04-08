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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.arquillian.tests.datasource;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

@WebServlet("/test")
public class PojoServlet extends HttpServlet {

    @Resource(name = "jdbc/myDB")
    private DataSource dataSource;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        if (dataSource == null) throw new ServletException();

        Connection c = null;
        try {
            c = dataSource.getConnection();
            if (!"jdbc:hsqldb:mem:foo:myDB".equals(c.getMetaData().getURL())) {
                throw new ServletException();
            }
        } catch (final SQLException e) {
            throw new ServletException(e);
        } finally {
            try {
                if (c != null) {
                    c.close();
                }
            } catch (final SQLException e) {
                // no-op
            }
        }
    }
}

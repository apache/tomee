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

package org.apache.openejb.arquillian.tests.datasource.definition;


import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

public class DataSourceServlet extends HttpServlet {
    private static final String EXPECTED_URL = "jdbc:hsqldb:mem:expected";

    @Resource(name = "jdbc/database")
    DataSource dataSource;

    @EJB
    DataSourceBean dataSourceBean;

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

    public void testDataSourceInjectionInEjb() throws SQLException {
        final String dataSourceUrl = dataSourceBean.getUrlFromInjectedDataSource();
        if (!EXPECTED_URL.equals(dataSourceUrl)) {
            throw new IllegalStateException("[BEAN - INJECTION] Unexpected URL: " + dataSourceUrl);
        }
    }

    public void testDataSourceLookUpFromEjb() throws SQLException {
        final String dataSourceUrl = dataSourceBean.getUrlFromInjectedDataSource();
        if (!EXPECTED_URL.equals(dataSourceUrl)) {
            throw new IllegalStateException("[BEAN - LOOKUP] Unexpected URL: " + dataSourceUrl);
        }
    }

    public void testDataSourceInjectionInServlet() throws SQLException {
        final String dataSourceUrl = getDataSourceUrl(dataSource);
        if (!EXPECTED_URL.equals(dataSourceUrl)) {
            throw new IllegalStateException("[SERVLET - INJECTION] Unexpected URL: " + dataSourceUrl);
        }
    }

    public void testDataSourceLookUpFromServlet() throws Exception {
        final InitialContext initialContext = new InitialContext();
        final DataSource ds = (DataSource) initialContext.lookup("java:comp/env/jdbc/database");

        final String dataSourceUrl = getDataSourceUrl(ds);
        if (!EXPECTED_URL.equals(dataSourceUrl)) {
            throw new IllegalStateException("[SERVLET - LOOKUP] Unexpected URL: " + dataSourceUrl);
        }
    }

    private String getDataSourceUrl(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return connection.getMetaData().getURL();
        }
    }
}

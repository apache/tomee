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


import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

public class DataSourceServlet extends HttpServlet {
    private static final String EXPECTED_URL_BEAN = "jdbc:hsqldb:mem:expected";
    private static final String EXPECTED_URL_POJO = "jdbc:hsqldb:mem:pojo";

    @Resource(name = "jdbc/database")
    DataSource dataSourceFromManagedBean;

    @Resource(name = "jdbc/pojodb")
    DataSource dataSourceFromPojo;

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
        final DataSource ds = dataSourceBean.getDataSource();
        verifyDataSource(ds, EXPECTED_URL_BEAN, "BEAN, INJECTION, DEF_FROM_MANAGED_BEAN");
    }

    public void testDataSourceLookUpFromEjb() throws Exception {
        final DataSource ds = dataSourceBean.getDataSource();
        verifyDataSource(ds, EXPECTED_URL_BEAN, "BEAN, LOOKUP, DEF_FROM_MANAGED_BEAN");
    }

    public void testDataSourceFromPojoInjectionInEjbDSFromPojo() throws SQLException {
        final DataSource ds = dataSourceBean.getDataSourceFromPojo();
        verifyDataSource(ds, EXPECTED_URL_POJO, "BEAN, INJECTION, DEF_FROM_POJO");
    }

    public void testDataSourceFromPojoLookUpFromEjb() throws Exception {
        final DataSource ds = dataSourceBean.getDataSourceFromPojo();
        verifyDataSource(ds, EXPECTED_URL_POJO, "BEAN, LOOKUP, DEF_FROM_POJO");
    }


    public void testDataSourceInjectionInServlet() throws SQLException {
        verifyDataSource(dataSourceFromManagedBean, EXPECTED_URL_BEAN, "SERVLET, INJECTION, DEF_FROM_MANAGED_BEAN");
    }

    public void testDataSourceLookUpFromServlet() throws Exception {
        final DataSource ds = lookUpDataSource("java:comp/env/jdbc/database");
        verifyDataSource(ds, EXPECTED_URL_BEAN, "SERVLET, LOOKUP, DEF_FROM_MANAGED_BEAN");
    }

    public void testDataSourceFromPojoInjectionInServlet() throws SQLException {
        verifyDataSource(dataSourceFromPojo, EXPECTED_URL_POJO, "SERVLET, INJECTION, DEF_FROM_POJO");
    }

    public void testDataSourceFromPojoLookUpFromServlet() throws Exception {
        final DataSource ds = lookUpDataSource("java:comp/env/jdbc/pojodb");
        verifyDataSource(ds, EXPECTED_URL_POJO, "SERVLET, LOOKUP, DEF_FROM_POJO");
    }

    private DataSource lookUpDataSource(String name) throws NamingException {
        final InitialContext initialContext = new InitialContext();
        return (DataSource) initialContext.lookup(name);
    }

    private void verifyDataSource(DataSource dataSource, String expectedUrl, String context) throws SQLException {
        final String dataSourceUrl = getDataSourceUrl(dataSource);

        if (!expectedUrl.equals(dataSourceUrl)) {
            throw new IllegalStateException("[" + context + "] "
                    + "Expected={URL=" + EXPECTED_URL_BEAN + "} "
                    + "Actual={URL=" + dataSourceUrl + "}"
            );
        }
    }

    private String getDataSourceUrl(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return connection.getMetaData().getURL();
        }
    }
}

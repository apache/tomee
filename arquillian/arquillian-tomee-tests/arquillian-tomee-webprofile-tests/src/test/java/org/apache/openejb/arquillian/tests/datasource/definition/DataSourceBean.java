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

package org.apache.openejb.arquillian.tests.datasource.definition;

import javax.annotation.Resource;
import javax.annotation.sql.DataSourceDefinition;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Stateless
@LocalBean
@DataSourceDefinition(name = "jdbc/database", className = "org.hsqldb.jdbcDriver", url = "jdbc:hsqldb:mem:unexpected")
public class DataSourceBean {
    @Resource(name = "jdbc/database")
    private DataSource dataSource;

    public String getUrlFromInjectedDataSource() throws SQLException {
        return getDataSourceUrl(dataSource);
    }

    public String getUrlFromLookedUpDataSource() throws Exception {
        final InitialContext initialContext = new InitialContext();
        final DataSource ds = (DataSource) initialContext.lookup("java:comp/env/jdbc/database");

        return getDataSourceUrl(ds);
    }

    private String getDataSourceUrl(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return connection.getMetaData().getURL();
        }
    }
}

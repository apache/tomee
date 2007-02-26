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
package org.apache.openejb.resource;

import junit.framework.TestCase;
import org.apache.geronimo.transaction.jta11.GeronimoTransactionManagerJTA11;
import org.apache.openejb.resource.jdbc.JdbcManagedConnectionFactory;

import javax.transaction.TransactionManager;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SharedLocalConnectionManagerTest extends TestCase {
    public void test() throws Exception {
        TransactionManager transactionManager = new GeronimoTransactionManagerJTA11();
        ConnectionManager connectionManager = new SharedLocalConnectionManager(transactionManager);

        ManagedConnectionFactory managedConnectionFactory = new JdbcManagedConnectionFactory("org.hsqldb.jdbcDriver",
                "jdbc:hsqldb:mem",
                "sa",
                "",
                true);

        DataSource dataSource = (DataSource) managedConnectionFactory.createConnectionFactory(connectionManager);

        for (int i = 0; i < 100; i++) {
            verifyDatasource(dataSource);
        }
    }

    private void verifyDatasource(DataSource dataSource) throws SQLException {
        Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("CALL SQRT(4.0)");
        assertTrue(resultSet.next());
        assertEquals(2.0, resultSet.getDouble(1));
        resultSet.close();
        statement.close();
        connection.close();
    }
}

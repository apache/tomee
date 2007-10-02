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
package org.apache.openejb.assembler.classic;

import junit.framework.TestCase;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
public class JdbcConfigTest extends TestCase {
    public void test() throws Exception {
        ConfigurationFactory config = new ConfigurationFactory();
        Assembler assembler = new Assembler();

        // System services
        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        // managed JDBC
        assembler.createResource(config.configureService("Default JDBC Database", ResourceInfo.class));

        // unmanaged JDBC
        assembler.createResource(config.configureService("Default Unmanaged JDBC Database", ResourceInfo.class));

        ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);

        DataSource managedDS = (DataSource) containerSystem.getJNDIContext().lookup("java:openejb/Resource/Default JDBC Database");
        assertNotNull("managedDS is null", managedDS);

        DataSource unmanagedDS = (DataSource) containerSystem.getJNDIContext().lookup("java:openejb/Resource/Default Unmanaged JDBC Database");
        assertNotNull("unmanagedDS is null", unmanagedDS);

        // test without a transaction
        // NOTE: without a transaction all connections work as unmanaged
        verifyUnmanagedConnections(managedDS);
        verifyUnmanagedConnections(unmanagedDS);

        // test in the context of a transaction
        TransactionManager transactionManager = SystemInstance.get().getComponent(TransactionManager.class);
        transactionManager.begin();
        try {
            verifyManagedConnections(managedDS);
            verifyUnmanagedConnections(unmanagedDS);
        } finally {
            // commit the transaction
            transactionManager.commit();
        }
    }

    private void verifyManagedConnections(DataSource dataSource) throws SQLException {
        List<Connection> managedConnections = new ArrayList<Connection>();
        try {
            for (int i = 0; i < 4; i++) {
                Connection connection = dataSource.getConnection();
                managedConnections.add(connection);

                try {
                    connection.setAutoCommit(true);
                    fail("expected connection.setAutoCommit(true) to throw an exception");
                } catch (SQLException expected) {
                }

                Statement statement = connection.createStatement();
                try {
                    statement.getQueryTimeout();
                } finally {
                    statement.close();
                }
            }
        } finally {
            for (Connection connection : managedConnections) {
                close(connection);
            }
        }
    }

    private void verifyUnmanagedConnections(DataSource dataSource) throws SQLException {
        List<Connection> unmanagedConnections = new ArrayList<Connection>();
        try {
            for (int i = 0; i < 4; i++) {
                Connection connection = dataSource.getConnection();
                unmanagedConnections.add(connection);
                assertTrue("Expected connection.getAutoCommit() to be true", connection.getAutoCommit());
                connection.setAutoCommit(true);
                Statement statement = connection.createStatement();
                try {
                    statement.getQueryTimeout();
                } finally {
                    statement.close();
                }
                connection.commit();
                connection.setAutoCommit(false);
            }
        } finally {
            for (Connection connection : unmanagedConnections) {
                close(connection);
            }
        }
    }

    private static void close(Connection connection) {
        if (connection == null) return;

        try {
            connection.close();
        } catch (SQLException e) {
        }
    }
}

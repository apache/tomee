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
import jakarta.transaction.TransactionManager;
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
        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        // System services
        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        // managed JDBC
        assembler.createResource(config.configureService("Default JDBC Database", ResourceInfo.class));

        // unmanaged JDBC
        assembler.createResource(config.configureService("Default Unmanaged JDBC Database", ResourceInfo.class));

        final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);

        final DataSource managedDS = (DataSource) containerSystem.getJNDIContext().lookup("openejb/Resource/Default JDBC Database");
        assertNotNull("managedDS is null", managedDS);

        final DataSource unmanagedDS = (DataSource) containerSystem.getJNDIContext().lookup("openejb/Resource/Default Unmanaged JDBC Database");
        assertNotNull("unmanagedDS is null", unmanagedDS);

        // test without a transaction
        // NOTE: without a transaction all connections work as unmanaged
        verifyUnmanagedConnections(managedDS);
        verifyUnmanagedConnections(unmanagedDS);

        // test in the context of a transaction
        final TransactionManager transactionManager = SystemInstance.get().getComponent(TransactionManager.class);
        transactionManager.begin();
        try {
            verifyManagedConnections(managedDS);
            verifyUnmanagedConnections(unmanagedDS);
        } finally {
            // commit the transaction
            transactionManager.commit();
        }
    }

    private void verifyManagedConnections(final DataSource dataSource) throws SQLException {
        final List<Connection> managedConnections = new ArrayList<>();
        try {
            for (int i = 0; i < 4; i++) {
                final Connection connection = dataSource.getConnection();
                managedConnections.add(connection);

                try {
                    connection.setAutoCommit(true);
                    fail("expected connection.setAutoCommit(true) to throw an exception");
                } catch (final SQLException expected) {
                }

                try (Statement statement = connection.createStatement()) {
                    statement.getQueryTimeout();
                }
            }
        } finally {
            for (final Connection connection : managedConnections) {
                close(connection);
            }
        }
    }

    private void verifyUnmanagedConnections(final DataSource dataSource) throws SQLException {
        final List<Connection> unmanagedConnections = new ArrayList<>();
        try {
            for (int i = 0; i < 4; i++) {
                final Connection connection = dataSource.getConnection();
                unmanagedConnections.add(connection);
                assertTrue("Expected connection.getAutoCommit() to be true", connection.getAutoCommit());
                connection.setAutoCommit(true);
                try (Statement statement = connection.createStatement()) {
                    statement.getQueryTimeout();
                }
                connection.commit();
                connection.setAutoCommit(false);
            }
        } finally {
            for (final Connection connection : unmanagedConnections) {
                close(connection);
            }
        }
    }

    private static void close(final Connection connection) {
        if (connection == null) return;

        try {
            connection.close();
        } catch (final SQLException e) {
        }
    }
}

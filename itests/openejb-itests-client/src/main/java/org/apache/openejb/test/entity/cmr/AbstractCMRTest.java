/**
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
package org.apache.openejb.test.entity.cmr;


import org.apache.openejb.test.TestManager;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * @version $Revision$ $Date$
 */
public abstract class AbstractCMRTest extends org.apache.openejb.test.NamedTestCase {
    private TransactionManager transactionManager;
    protected DataSource ds;
    protected InitialContext initialContext;

    public AbstractCMRTest(final String name) {
        super("Entity.CMR." + name);
    }

    protected synchronized void beginTransaction() throws Exception {
        transactionManager.begin();
    }

    protected synchronized void completeTransaction() throws SystemException, HeuristicMixedException, HeuristicRollbackException, RollbackException {
        final int status = transactionManager.getStatus();
        if (status == Status.STATUS_ACTIVE) {
            transactionManager.commit();
        } else if (status != Status.STATUS_NO_TRANSACTION) {
            transactionManager.rollback();
        } else {
            throw new IllegalStateException("tx status: " + status);
        }
    }

    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    protected synchronized void setUp() throws Exception {
        super.setUp();

        final Properties properties = TestManager.getServer().getContextEnvironment();
        //properties.put(Context.SECURITY_PRINCIPAL, "ENTITY_TEST_CLIENT");
        //properties.put(Context.SECURITY_CREDENTIALS, "ENTITY_TEST_CLIENT");

        initialContext = new InitialContext(properties);

        final InitialContext jndiContext = new InitialContext();
        transactionManager = (TransactionManager) jndiContext.lookup("java:openejb/TransactionManager");
        try {
            ds = (DataSource) jndiContext.lookup("java:openejb/Resource/My DataSource");
        } catch (final NamingException e) {
            ds = (DataSource) jndiContext.lookup("java:openejb/Resource/Default JDBC Database");
        }
    }

    protected static void dumpTable(final DataSource ds, final String table) throws SQLException {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = ds.getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM " + table);
            final ResultSetMetaData setMetaData = resultSet.getMetaData();
            final int columnCount = setMetaData.getColumnCount();
            while (resultSet.next()) {
                final StringBuilder row = new StringBuilder();
                for (int i = 1; i <= columnCount; i++) {
                    if (i > 1) {
                        row.append(", ");
                    }
                    final String name = setMetaData.getColumnName(i);
                    final Object value = resultSet.getObject(i);
                    row.append(name).append("=").append(value);
                }
                System.out.println(row);
            }
        } finally {
            close(resultSet);
            close(statement);
            close(connection);
        }
    }

    protected static void close(final ResultSet resultSet) {
        if (resultSet == null) return;
        try {
            resultSet.close();
        } catch (final SQLException e) {
        }
    }

    protected static void close(final Statement statement) {
        if (statement == null) return;
        try {
            statement.close();
        } catch (final SQLException e) {
        }
    }

    protected static void close(final Connection connection) {
        if (connection == null) return;
        try {
            connection.close();
        } catch (final SQLException e) {
        }
    }
}


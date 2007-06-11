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
package org.apache.openejb.resource.jdbc;

import javax.resource.spi.LocalTransaction;

import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Messages;

public class JdbcLocalTransaction implements LocalTransaction {

    protected java.sql.Connection sqlConn;
    protected JdbcManagedConnection managedConn;
    protected boolean isActiveTransaction = false;

    protected static final Messages messages = new Messages("org.apache.openejb.util.resources");
    protected static final Logger logger = Logger.getInstance("OpenEJB.resource.jdbc", "org.apache.openejb.util.resources");

    public JdbcLocalTransaction(JdbcManagedConnection managedConn) {
        this.sqlConn = managedConn.getSQLConnection();
        this.managedConn = managedConn;
    }

    public void begin() throws javax.resource.ResourceException {
        if (isActiveTransaction) {
            throw new javax.resource.spi.LocalTransactionException("Invalid transaction context. Transaction already active");
        }
        try {
            sqlConn.setAutoCommit(false);
            isActiveTransaction = true;
        } catch (java.sql.SQLException sqlE) {
            isActiveTransaction = false;
            throw new javax.resource.spi.ResourceAdapterInternalException("Can not begin transaction demarcation. Setting auto-commit to false for transaction chaining failed", sqlE);
        }
        managedConn.localTransactionStarted();
    }

    public void commit() throws javax.resource.ResourceException {
        if (isActiveTransaction) {
            isActiveTransaction = false;
            try {
                sqlConn.commit();
            } catch (java.sql.SQLException sqlE) {
                String msg = messages.format("jdbc.commit.failed", formatSqlException(sqlE));
                logger.error(msg);
                throw new javax.resource.spi.LocalTransactionException(msg, sqlE);
            }
            managedConn.localTransactionCommitted();
            try {
                sqlConn.setAutoCommit(true);
            } catch (java.sql.SQLException sqlE) {
                throw new javax.resource.spi.ResourceAdapterInternalException("Setting auto-commit to true to end transaction chaining failed", sqlE);
            }
        } else {
            throw new javax.resource.spi.LocalTransactionException("Invalid transaction context. No active transaction");
        }
    }

    public void rollback() throws javax.resource.ResourceException {
        if (isActiveTransaction) {
            isActiveTransaction = false;

            try {
                sqlConn.rollback();
            } catch (java.sql.SQLException sqlE) {
                String msg = messages.format("jdbc.rollback.failed", formatSqlException(sqlE));
                logger.error(msg);
                throw new javax.resource.spi.LocalTransactionException(msg, sqlE);
            }

            managedConn.localTransactionRolledback();

            try {
                sqlConn.setAutoCommit(true);
            } catch (java.sql.SQLException sqlE) {
                throw new javax.resource.spi.ResourceAdapterInternalException("Setting auto-commit to true to end transaction chaining failed", sqlE);
            }
        } else {
            throw new javax.resource.spi.LocalTransactionException("Invalid transaction context. No active transaction");
        }
    }

    protected void cleanup() throws javax.resource.ResourceException {
        if (isActiveTransaction) {
            rollback();
        }
    }

    protected String formatSqlException(java.sql.SQLException e) {
        return messages.format("jdbc.exception", e.getClass().getName(), e.getMessage(), e.getErrorCode() + "", e.getSQLState());
    }
}
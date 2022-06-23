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
package org.apache.openejb.resource.jdbc.dbcp;

import org.apache.commons.dbcp2.managed.TransactionContext;

import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;
import java.sql.SQLException;
public class DbcpTransactionRegistry extends org.apache.commons.dbcp2.managed.TransactionRegistry {

    private TransactionManager transactionManager;
    public DbcpTransactionRegistry(TransactionManager transactionManager) {
        this(transactionManager, null);
    }
    public DbcpTransactionRegistry(TransactionManager transactionManager, TransactionSynchronizationRegistry transactionSynchronizationRegistry) {
        super(transactionManager, transactionSynchronizationRegistry);
        this.transactionManager = transactionManager;
    }

    /**
     * Gets the active TransactionContext or null if not Transaction is active.
     *
     * @return The active TransactionContext or null if no Transaction is active.
     * @throws SQLException
     *             Thrown when an error occurs while fetching the transaction.
     */
    public TransactionContext getActiveTransactionContext() throws SQLException {
        Transaction transaction;
        try {
            transaction = transactionManager.getTransaction();

            // was there a transaction?
            if (transaction == null) {
                return null;
            }

            // is it active
            final int status = transaction.getStatus();
            if (status != Status.STATUS_ACTIVE && status != Status.STATUS_MARKED_ROLLBACK) {
                return null;
            }
        } catch (final SystemException e) {
            throw new SQLException("Unable to determine current transaction ", e);
        }

        return super.getActiveTransactionContext();
    }
}

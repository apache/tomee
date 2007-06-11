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
package org.apache.openejb.core;

import java.util.Map;
import java.util.HashMap;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.transaction.Transaction;

public class SimpleTransactionSynchronizationRegistry implements TransactionSynchronizationRegistry {
    private final TransactionManager transactionManager;
    private final Map<Transaction,Map<Object,Object>> transactionResources = new HashMap<Transaction,Map<Object,Object>>();

    public SimpleTransactionSynchronizationRegistry(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public Transaction getTransactionKey() {
        try {
            return transactionManager.getTransaction();
        } catch (SystemException e) {
            return null;
        }
    }

    public Object getResource(Object key) {
        Transaction transaction = getActiveTransaction();

        Map<Object, Object> resources = transactionResources.get(transaction);
        if (resources == null) {
            return null;
        }

        Object value = resources.get(key);
        return value;
    }

    public void putResource(Object key, Object value) {
        Transaction transaction = getActiveTransaction();

        Map<Object, Object> resources = transactionResources.get(transaction);
        if (resources == null) {
            // after transaction completes clean up resources
            try {
                transaction.registerSynchronization(new RemoveTransactionResources(transaction));
            } catch (Exception e) {
                throw new IllegalStateException("No transaction active", e);
            }
            resources = new HashMap<Object,Object>();
            transactionResources.put(transaction,resources);
        }

        resources.put(key, value);
    }

    public int getTransactionStatus() {
        try {
            return transactionManager.getStatus();
        } catch (SystemException e) {
            return Status.STATUS_NO_TRANSACTION;
        }
    }

    public void registerInterposedSynchronization(Synchronization synchronization) {
        if (synchronization == null) {
            throw new NullPointerException("synchronization is null");
        }

        Transaction transaction = getActiveTransaction();
        try {
            transaction.registerSynchronization(synchronization);
        } catch (Exception ignored) {
        }
    }


    public boolean getRollbackOnly() {
        Transaction transaction = getActiveTransaction();
        try {
            return transaction.getStatus() == Status.STATUS_MARKED_ROLLBACK;
        } catch (Exception e) {
            throw new IllegalStateException("No transaction active", e);
        }
    }

    public void setRollbackOnly() {
        Transaction transaction = getActiveTransaction();
        try {
            transaction.setRollbackOnly();
        } catch (Exception e) {
            throw new IllegalStateException("No transaction active", e);
        }
    }
    private Transaction getActiveTransaction() {
        try {
            Transaction transaction = transactionManager.getTransaction();
            if (transaction == null) {
                throw new IllegalStateException("No transaction active");
            }
            int status = transaction.getStatus();
            if (status != Status.STATUS_ACTIVE && status != Status.STATUS_MARKED_ROLLBACK) {
                throw new IllegalStateException("No transaction active");
            }
            return transaction;
        } catch (SystemException e) {
            throw new IllegalStateException("No transaction active", e);
        }
    }

    private class RemoveTransactionResources implements Synchronization {
        private final Transaction transaction;

        public RemoveTransactionResources(Transaction transaction) {
            this.transaction = transaction;
        }

        public void beforeCompletion() {
        }

        public void afterCompletion(int i) {
            transactionResources.remove(transaction);
        }
    }
}

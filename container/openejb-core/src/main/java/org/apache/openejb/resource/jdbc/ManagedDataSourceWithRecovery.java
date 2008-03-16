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
package org.apache.openejb.resource.jdbc;

import java.io.File;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

import org.apache.geronimo.transaction.manager.WrapperNamedXAResource;
import org.apache.openejb.loader.SystemInstance;

public class ManagedDataSourceWithRecovery extends BasicManagedDataSource {
    private TransactionManager suppliedTransactionManager;

    @Override
    public void setTransactionManager(TransactionManager transactionManager) {
        this.suppliedTransactionManager = transactionManager;
    }

    protected void wrapTransactionManager() {
        if (suppliedTransactionManager != null) {
            super.setTransactionManager(new TransactionManagerWrapper(suppliedTransactionManager, getUrl()));
        }
    }

    private static class TransactionManagerWrapper implements TransactionManager {

        private final TransactionManager transactionManager;
        private final String name;

        private TransactionManagerWrapper(TransactionManager transactionManager, String name) {
            this.transactionManager = transactionManager;
            this.name = name;
        }

        public void begin() throws NotSupportedException, SystemException {
            transactionManager.begin();
        }

        public void commit() throws HeuristicMixedException, HeuristicRollbackException, IllegalStateException, RollbackException, SecurityException, SystemException {
            transactionManager.commit();
        }

        public int getStatus() throws SystemException {
            return transactionManager.getStatus();
        }

        public Transaction getTransaction() throws SystemException {
            Transaction tx = transactionManager.getTransaction();
            return tx == null? null: new TransactionWrapper(transactionManager.getTransaction(), name);
        }

        public void resume(Transaction transaction) throws IllegalStateException, InvalidTransactionException, SystemException {
            transactionManager.resume(((TransactionWrapper)transaction).transaction);
        }

        public void rollback() throws IllegalStateException, SecurityException, SystemException {
            transactionManager.rollback();
        }

        public void setRollbackOnly() throws IllegalStateException, SystemException {
            transactionManager.setRollbackOnly();
        }

        public void setTransactionTimeout(int i) throws SystemException {
            transactionManager.setTransactionTimeout(i);
        }

        public Transaction suspend() throws SystemException {
            return new TransactionWrapper(transactionManager.suspend(), name);
        }
    }

    private static class TransactionWrapper implements Transaction {

        private final Transaction transaction;
        private final String name;

        private TransactionWrapper(Transaction transaction, String name) {
            this.transaction = transaction;
            this.name = name;
        }

        public void commit() throws HeuristicMixedException, HeuristicRollbackException, RollbackException, SecurityException, SystemException {
            transaction.commit();
        }

        public boolean delistResource(XAResource xaResource, int i) throws IllegalStateException, SystemException {
            XAResource wrapper = new WrapperNamedXAResource(xaResource, name);
            return transaction.delistResource(wrapper, i);
        }

        public boolean enlistResource(XAResource xaResource) throws IllegalStateException, RollbackException, SystemException {
            XAResource wrapper = new WrapperNamedXAResource(xaResource, name);
            return transaction.enlistResource(wrapper);
        }

        public int getStatus() throws SystemException {
            return transaction.getStatus();
        }

        public void registerSynchronization(Synchronization synchronization) throws IllegalStateException, RollbackException, SystemException {
            transaction.registerSynchronization(synchronization);
        }

        public void rollback() throws IllegalStateException, SystemException {
            transaction.rollback();
        }

        public void setRollbackOnly() throws IllegalStateException, SystemException {
            transaction.setRollbackOnly();
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TransactionWrapper that = (TransactionWrapper) o;

            return transaction.equals(that.transaction);
        }

        public int hashCode() {
            return transaction.hashCode();
        }
    }
}
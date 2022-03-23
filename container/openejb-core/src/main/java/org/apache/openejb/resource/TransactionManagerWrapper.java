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

package org.apache.openejb.resource;

import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.InvalidTransactionException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

public class TransactionManagerWrapper implements TransactionManager {
    private final String name;
    private final TransactionManager delegate;
    private final XAResourceWrapper xaResourceWrapper;

    public TransactionManagerWrapper(final TransactionManager delegate, final String name, final XAResourceWrapper xaResourceWrapper) {
        this.delegate = delegate;
        this.name = name;
        this.xaResourceWrapper = xaResourceWrapper;
    }

    @Override
    public void begin() throws NotSupportedException, SystemException {
        delegate.begin();
    }

    @Override
    public void commit() throws HeuristicMixedException, HeuristicRollbackException, IllegalStateException, RollbackException, SecurityException, SystemException {
        delegate.commit();
    }

    @Override
    public int getStatus() throws SystemException {
        return delegate.getStatus();
    }

    @Override
    public Transaction getTransaction() throws SystemException {
        final Transaction tx = delegate.getTransaction();
        return tx == null ? null : new TransactionWrapper(delegate.getTransaction(), name, xaResourceWrapper);
    }

    @Override
    public void resume(final Transaction transaction) throws IllegalStateException, InvalidTransactionException, SystemException {
        delegate.resume(((TransactionWrapper) transaction).transaction);
    }

    @Override
    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        delegate.rollback();
    }

    @Override
    public void setRollbackOnly() throws IllegalStateException, SystemException {
        delegate.setRollbackOnly();
    }

    @Override
    public void setTransactionTimeout(final int i) throws SystemException {
        delegate.setTransactionTimeout(i);
    }

    @Override
    public Transaction suspend() throws SystemException {
        return new TransactionWrapper(delegate.suspend(), name, xaResourceWrapper);
    }

    private static final class TransactionWrapper implements Transaction {
        private final Transaction transaction;
        private final String name;
        private final XAResourceWrapper xaResourceWrapper;

        private TransactionWrapper(final Transaction transaction, final String name, final XAResourceWrapper xaResourceWrapper) {
            this.transaction = transaction;
            this.name = name;
            this.xaResourceWrapper = xaResourceWrapper;
        }

        @Override
        public void commit() throws HeuristicMixedException, HeuristicRollbackException, RollbackException, SecurityException, SystemException {
            transaction.commit();
        }

        @Override
        public boolean delistResource(final XAResource xaResource, final int i) throws IllegalStateException, SystemException {
            final XAResource wrapper = xaResourceWrapper.wrap(xaResource, name);
            return transaction.delistResource(wrapper, i);
        }

        @Override
        public boolean enlistResource(final XAResource xaResource) throws IllegalStateException, RollbackException, SystemException {
            final XAResource wrapper = xaResourceWrapper.wrap(xaResource, name);
            return transaction.enlistResource(wrapper);
        }

        @Override
        public int getStatus() throws SystemException {
            return transaction.getStatus();
        }

        @Override
        public void registerSynchronization(final Synchronization synchronization) throws IllegalStateException, RollbackException, SystemException {
            transaction.registerSynchronization(synchronization);
        }

        @Override
        public void rollback() throws IllegalStateException, SystemException {
            transaction.rollback();
        }

        @Override
        public void setRollbackOnly() throws IllegalStateException, SystemException {
            transaction.setRollbackOnly();
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final TransactionWrapper that = (TransactionWrapper) o;
            return transaction.equals(that.transaction);
        }

        @Override
        public int hashCode() {
            return transaction.hashCode();
        }
    }
}


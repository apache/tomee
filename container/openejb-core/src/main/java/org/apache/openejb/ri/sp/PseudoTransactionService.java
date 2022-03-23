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

package org.apache.openejb.ri.sp;

import org.apache.openejb.spi.TransactionService;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import jakarta.transaction.InvalidTransactionException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.TransactionSynchronizationRegistry;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @org.apache.xbean.XBean element="pseudoTransactionService"
 */
public class PseudoTransactionService implements TransactionService, TransactionManager, TransactionSynchronizationRegistry {
    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.core.cmp");
    private final ThreadLocal<MyTransaction> threadTransaction = new ThreadLocal<>();

    public void init(final Properties props) {
    }

    public TransactionManager getTransactionManager() {
        return this;
    }

    public TransactionSynchronizationRegistry getTransactionSynchronizationRegistry() {
        return this;
    }

    public int getStatus() {
        final MyTransaction tx = threadTransaction.get();
        if (tx == null) {
            return Status.STATUS_NO_TRANSACTION;
        }
        return tx.getStatus();
    }

    public Transaction getTransaction() {
        return threadTransaction.get();
    }

    public boolean getRollbackOnly() {
        final MyTransaction tx = threadTransaction.get();
        if (tx == null) {
            throw new IllegalStateException("No transaction active");
        }
        return tx.getRollbackOnly();
    }

    public void setRollbackOnly() {
        final MyTransaction tx = threadTransaction.get();
        if (tx == null) {
            throw new IllegalStateException("No transaction active");
        }
        tx.setRollbackOnly();
    }

    public void begin() throws NotSupportedException {
        if (threadTransaction.get() != null) {
            throw new NotSupportedException("A transaction is already active");
        }

        final MyTransaction tx = new MyTransaction();
        threadTransaction.set(tx);
    }

    public void commit() throws RollbackException {
        final MyTransaction tx = threadTransaction.get();
        if (tx == null) {
            throw new IllegalStateException("No transaction active");
        }

        try {
            tx.commit();
        } finally {
            threadTransaction.set(null);
        }
    }


    public void rollback() {
        final MyTransaction tx = threadTransaction.get();
        if (tx == null) {
            throw new IllegalStateException("No transaction active");
        }

        try {
            tx.rollback();
        } finally {
            threadTransaction.set(null);
        }
    }

    public Transaction suspend() {
        return threadTransaction.get();
    }

    public void resume(final Transaction tx) throws InvalidTransactionException {
        if (tx == null) {
            throw new InvalidTransactionException("Transaction is null");
        }
        if (!(tx instanceof MyTransaction)) {
            throw new InvalidTransactionException("Unknown transaction type " + tx.getClass().getName());
        }
        final MyTransaction myTransaction = (MyTransaction) tx;

        if (threadTransaction.get() != null) {
            throw new IllegalStateException("A transaction is already active");
        }

        final int status = myTransaction.getStatus();
        if (status != Status.STATUS_ACTIVE && status != Status.STATUS_MARKED_ROLLBACK) {
            throw new InvalidTransactionException("Expected transaction to be STATUS_ACTIVE or STATUS_MARKED_ROLLBACK, but was " + status);
        }

        threadTransaction.set(myTransaction);
    }

    public Object getTransactionKey() {
        return getTransaction();
    }

    public int getTransactionStatus() {
        return getStatus();
    }

    public Object getResource(final Object key) {
        final MyTransaction tx = threadTransaction.get();
        if (tx == null) {
            throw new IllegalStateException("No transaction active");
        }

        final Object value = tx.getResource(key);
        return value;
    }

    public void putResource(final Object key, final Object value) {
        final MyTransaction tx = threadTransaction.get();
        if (tx == null) {
            throw new IllegalStateException("No transaction active");
        }

        tx.putResource(key, value);
    }

    public void registerInterposedSynchronization(final Synchronization synchronization) {
        final MyTransaction tx = threadTransaction.get();
        if (tx == null) {
            throw new IllegalStateException("No transaction active");
        }

        tx.registerInterposedSynchronization(synchronization);
    }

    public void setTransactionTimeout(final int seconds) {
    }

    public class MyTransaction implements Transaction {
        private final List<Synchronization> registeredSynchronizations = Collections.synchronizedList(new ArrayList<>());
        private final List<XAResource> xaResources = Collections.synchronizedList(new ArrayList<>());
        private final Map<Object, Object> resources = new HashMap<>();
        private int status = Status.STATUS_ACTIVE;

        public boolean delistResource(final XAResource xaRes, final int flag) {
            xaResources.remove(xaRes);
            return true;
        }

        public boolean enlistResource(final XAResource xaRes) {
            xaResources.add(xaRes);
            return true;
        }

        public int getStatus() {
            return status;
        }

        public void registerSynchronization(final Synchronization synchronization) {
            registeredSynchronizations.add(synchronization);
        }

        public void registerInterposedSynchronization(final Synchronization synchronization) {
            registeredSynchronizations.add(synchronization);
        }

        public boolean getRollbackOnly() {
            return status == Status.STATUS_MARKED_ROLLBACK;
        }

        public void setRollbackOnly() {
            status = Status.STATUS_MARKED_ROLLBACK;
        }

        public Object getResource(final Object key) {
            if (key == null) {
                throw new NullPointerException("key is null");
            }
            return resources.get(key);
        }

        public void putResource(final Object key, final Object value) {
            if (key == null) {
                throw new NullPointerException("key is null");
            }
            if (value != null) {
                resources.put(key, value);
            } else {
                resources.remove(key);
            }
        }

        public void commit() throws RollbackException {
            try {
                if (status == Status.STATUS_MARKED_ROLLBACK) {
                    rollback();
                    throw new RollbackException();
                }
                try {
                    doBeforeCompletion();
                } catch (final Exception e) {
                    rollback();
                    throw (RollbackException) new RollbackException().initCause(e);
                }
                doXAResources(Status.STATUS_COMMITTED);
                status = Status.STATUS_COMMITTED;
                doAfterCompletion(Status.STATUS_COMMITTED);
            } finally {
                threadTransaction.set(null);
            }
        }

        public void rollback() {
            try {
                doXAResources(Status.STATUS_ROLLEDBACK);
                doAfterCompletion(Status.STATUS_ROLLEDBACK);
                status = Status.STATUS_ROLLEDBACK;
                registeredSynchronizations.clear();
            } finally {
                threadTransaction.set(null);
            }
        }

        private void doBeforeCompletion() {
            for (final Synchronization sync : new ArrayList<>(registeredSynchronizations)) {
                sync.beforeCompletion();
            }
        }

        private void doAfterCompletion(final int status) {
            for (final Synchronization sync : new ArrayList<>(registeredSynchronizations)) {
                try {
                    sync.afterCompletion(status);
                } catch (final RuntimeException e) {
                    logger.warning("Synchronization afterCompletion threw a RuntimeException", e);
                }
            }
        }

        private void doXAResources(final int status) {
            for (final XAResource xaRes : new ArrayList<>(xaResources)) {
                if (status == Status.STATUS_COMMITTED) {
                    try {
                        xaRes.commit(null, true);
                    } catch (final XAException e) {
                        // no-op
                    }
                    try {
                        xaRes.end(null, XAResource.TMSUCCESS);
                    } catch (final XAException e) {
                        // no-op
                    }
                } else {
                    try {
                        xaRes.rollback(null);
                    } catch (final XAException e) {
                        // no-op
                    }
                    try {
                        xaRes.end(null, XAResource.TMFAIL);
                    } catch (final XAException e) {
                        // no-op
                    }
                }
            }
            xaResources.clear();
        }
    }
}


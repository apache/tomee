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

package org.apache.openejb.core.transaction;

import org.apache.openejb.ApplicationException;
import org.apache.openejb.SystemException;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.InvalidTransactionException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.TransactionSynchronizationRegistry;
import javax.transaction.xa.XAResource;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class JtaTransactionPolicy implements TransactionPolicy {
    protected static final Logger logger = Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources");
    protected static final Logger txLogger = Logger.getInstance(LogCategory.TRANSACTION, "org.apache.openejb.util.resources");

    protected final TransactionType transactionType;

    protected final TransactionManager transactionManager;
    private final TransactionSynchronizationRegistry synchronizationRegistry;
    private Map<Object, Object> resources;
    private final List<TransactionSynchronization> synchronizations = new LinkedList<>();
    private boolean rollbackOnly;

    public JtaTransactionPolicy(final TransactionType transactionType, final TransactionManager transactionManager) {
        this.transactionType = transactionType;
        this.transactionManager = transactionManager;
        synchronizationRegistry = SystemInstance.get().getComponent(TransactionSynchronizationRegistry.class);
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public abstract Transaction getCurrentTransaction();

    public boolean isTransactionActive() {
        final Transaction trasaction = getCurrentTransaction();
        if (trasaction == null) {
            return false;
        }

        try {
            final int status = trasaction.getStatus();
            return status == Status.STATUS_ACTIVE || status == Status.STATUS_MARKED_ROLLBACK;
        } catch (final jakarta.transaction.SystemException e) {
            return false;
        }
    }

    public boolean isRollbackOnly() {
        final Transaction trasaction = getCurrentTransaction();
        if (trasaction != null) {
            try {
                final int status = trasaction.getStatus();
                return status == Status.STATUS_MARKED_ROLLBACK;
            } catch (final jakarta.transaction.SystemException e) {
                return false;
            }
        } else {
            return rollbackOnly;
        }
    }

    public void setRollbackOnly() {
        setRollbackOnly(null);
    }

    @Override
    public void setRollbackOnly(final Throwable reason) {
        final Transaction trasaction = getCurrentTransaction();
        if (trasaction != null) {
            setRollbackOnly(trasaction, reason);
        } else {
            rollbackOnly = true;
        }
    }

    public Object getResource(final Object key) {
        if (isTransactionActive()) {
            return synchronizationRegistry.getResource(key);
        }

        if (resources == null) {
            return null;
        }
        return resources.get(key);
    }

    public void putResource(final Object key, final Object value) {
        if (isTransactionActive()) {
            synchronizationRegistry.putResource(key, value);
        }

        if (resources == null) {
            resources = new LinkedHashMap<>();
        }
        resources.put(key, value);
    }

    public Object removeResource(final Object key) {
        if (isTransactionActive()) {
            final Object value = synchronizationRegistry.getResource(key);
            synchronizationRegistry.putResource(key, null);
            return value;
        }

        if (resources == null) {
            return null;
        }
        return resources.remove(key);
    }

    public void registerSynchronization(final TransactionSynchronization synchronization) {
        if (isTransactionActive()) {
            synchronizationRegistry.registerInterposedSynchronization(new Synchronization() {
                public void beforeCompletion() {
                    synchronization.beforeCompletion();
                }

                public void afterCompletion(final int s) {
                    final TransactionSynchronization.Status status;
                    if (s == Status.STATUS_COMMITTED) {
                        status = TransactionSynchronization.Status.COMMITTED;
                    } else if (s == Status.STATUS_ROLLEDBACK) {
                        status = TransactionSynchronization.Status.ROLLEDBACK;
                    } else {
                        status = TransactionSynchronization.Status.UNKNOWN;
                    }

                    synchronization.afterCompletion(status);
                }
            });
        } else {
            synchronizations.add(synchronization);
        }
    }

    protected void fireNonTransactionalCompletion() {
        for (final TransactionSynchronization synchronization : new ArrayList<>(synchronizations)) {
            try {
                synchronization.beforeCompletion();
            } catch (final Throwable e) {
                logger.error("Exception thrown from beforeCompletion() of TransactionSynchronization " + synchronization);
            }
        }
        final TransactionSynchronization.Status status = isRollbackOnly() ? TransactionSynchronization.Status.ROLLEDBACK : TransactionSynchronization.Status.COMMITTED;
        for (final TransactionSynchronization synchronization : new ArrayList<>(synchronizations)) {
            try {
                synchronization.afterCompletion(status);
            } catch (final Exception e) {
                logger.error("Exception thrown from afterCompletion(" + status + ") of TransactionSynchronization " + synchronization);
            }
        }
    }

    public void enlistResource(final XAResource xaResource) throws SystemException {
        final Transaction transaction = getCurrentTransaction();
        if (transaction != null) {
            try {
                if (transaction.enlistResource(xaResource)) {
                    return;
                }
            } catch (final Exception e) {
                throw new SystemException("Unable to enlist xa resource in the transaction", e);
            }
        }
        throw new SystemException("Unable to enlist xa resource in the transaction");
    }

    public String toString() {
        return transactionType.toString();
    }

    protected Transaction getTransaction() throws SystemException {
        try {
            return transactionManager.getTransaction();
        } catch (final jakarta.transaction.SystemException e) {
            txLogger.error("The Transaction Manager has encountered an unexpected error condition while attempting to obtain current transaction: {0}", e.getMessage());
            throw new SystemException(e);
        }
    }


    protected void setRollbackOnly(final Transaction tx, final Throwable reason) {
        try {
            if (tx == null || tx.getStatus() != Status.STATUS_ACTIVE) {
                return;
            }

            if (reason == null) {

                tx.setRollbackOnly();

            } else {

                final Method setRollbackOnly = setRollbackOnlyMethod(tx);

                if (setRollbackOnly != null) {

                    setRollbackOnly.invoke(tx, reason);

                } else {

                    tx.setRollbackOnly();

                }
            }

            txLogger.debug("TX {0}: setRollbackOnly() on transaction {1}", transactionType, tx);

        } catch (final Exception e) {
            txLogger.error("Exception during setRollbackOnly()", e);
            throw new IllegalStateException("No transaction active", e);
        }
    }

    private Method setRollbackOnlyMethod(final Transaction tx) {
        try {
            return tx.getClass().getMethod("setRollbackOnly", Throwable.class);
        } catch (final Throwable e) {
            return null;
        }
    }

    protected Transaction beginTransaction() throws SystemException {
        final Transaction transaction;
        try {
            transactionManager.begin();
            transaction = transactionManager.getTransaction();
        } catch (final Exception e) {
            txLogger.error("The Transaction Manager has encountered an unexpected error condition while attempting to begin a new transaction: {0}", e.getMessage());
            throw new SystemException(e);
        }

        if (transaction == null) {
            throw new SystemException("Failed to begin a new transaction");
        }

        txLogger.debug("TX {0}: Started transaction {1}", transactionType, transaction);
        return transaction;
    }

    protected Transaction suspendTransaction() throws SystemException {
        try {
            final Transaction tx = transactionManager.suspend();
            if (tx == null) {
                txLogger.debug("TX {0}: No transaction to suspend", transactionType);
            } else {
                txLogger.debug("TX {0}: Suspended transaction {1}", transactionType, tx);
            }
            return tx;
        } catch (final jakarta.transaction.SystemException se) {
            txLogger.error("Exception during suspend()", se);
            throw new SystemException(se);
        }
    }

    protected void resumeTransaction(final Transaction tx) throws SystemException {
        try {
            if (tx == null) {
                txLogger.debug("TX {0}: No transaction to resume", transactionType);
            } else {
                txLogger.debug("TX {0}: Resuming transaction {1}", transactionType, tx);
                transactionManager.resume(tx);
            }
        } catch (final InvalidTransactionException ite) {

            txLogger.error("Could not resume the client's transaction, the transaction is no longer valid: {0}", ite.getMessage());
            throw new SystemException(ite);
        } catch (final IllegalStateException e) {

            txLogger.error("Could not resume the client's transaction: {0}", e.getMessage());
            throw new SystemException(e);
        } catch (final jakarta.transaction.SystemException e) {

            txLogger.error("Could not resume the client's transaction: The transaction reported a system exception: {0}", e.getMessage());
            throw new SystemException(e);
        }
    }

    protected void completeTransaction(final Transaction tx) throws SystemException, ApplicationException {
        final boolean shouldRollback;
        try {
            shouldRollback = tx.getStatus() != Status.STATUS_ACTIVE;
        } catch (final jakarta.transaction.SystemException e) {
            txLogger.error("The Transaction Manager has encountered an unexpected error condition while attempting to obtain transaction status: {0}", e.getMessage());
            throw new SystemException(e);
        }

        if (shouldRollback) {
            rollbackTransaction(tx);
            return;
        }

        try {
            txLogger.debug("TX {0}: Committing transaction {1}", transactionType, tx);
            if (tx.equals(transactionManager.getTransaction())) {

                transactionManager.commit();
            } else {
                tx.commit();
            }
        } catch (final RollbackException e) {

            txLogger.debug("The transaction has been rolled back rather than commited: {0}", e.getMessage());
            final Throwable txe = new TransactionRolledbackException("Transaction was rolled back, presumably because setRollbackOnly was called during a synchronization").initCause(e);
            throw new ApplicationException(txe);

        } catch (final HeuristicMixedException e) {

            txLogger.debug("A heuristic decision was made, some relevant updates have been committed while others have been rolled back: {0}", e.getMessage());
            throw new ApplicationException(new RemoteException("A heuristic decision was made, some relevant updates have been committed while others have been rolled back", e));

        } catch (final HeuristicRollbackException e) {

            txLogger.debug("A heuristic decision was made while commiting the transaction, some relevant updates have been rolled back: {0}", e.getMessage());
            throw new ApplicationException(new RemoteException("A heuristic decision was made while commiting the transaction, some relevant updates have been rolled back", e));

        } catch (final SecurityException e) {

            txLogger.error("The current thread is not allowed to commit the transaction: {0}", e.getMessage());
            throw new SystemException(e);

        } catch (final IllegalStateException e) {

            txLogger.error("The current thread is not associated with a transaction: {0}", e.getMessage());
            throw new SystemException(e);

        } catch (final jakarta.transaction.SystemException e) {
            txLogger.error("The Transaction Manager has encountered an unexpected error condition while attempting to commit the transaction: {0}", e.getMessage());

            throw new SystemException(e);
        }
    }

    protected void rollbackTransaction(final Transaction tx) throws SystemException {
        try {
            txLogger.debug("TX {0}: Rolling back transaction {1}", transactionType, tx);
            if (tx.equals(transactionManager.getTransaction())) {

                transactionManager.rollback();
            } else {
                tx.rollback();
            }
        } catch (final IllegalStateException | jakarta.transaction.SystemException e) {

            logger.error("The TransactionManager reported an exception while attempting to rollback the transaction: " + e.getMessage());
            throw new SystemException(e);

        }
    }
}


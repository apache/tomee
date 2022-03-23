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

package org.apache.openejb.core;

import org.apache.openejb.OpenEJB;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.UserTransaction;
import java.io.Serializable;

/**
 * @org.apache.xbean.XBean element="userTransaction"
 */
public class CoreUserTransaction implements UserTransaction, Serializable {
    private static final long serialVersionUID = 9203248912222645965L;
    private static final transient Logger transactionLogger = Logger.getInstance(LogCategory.TRANSACTION, "org.apache.openejb.util.resources");
    private static final ThreadLocal<RuntimeException> ERROR = new ThreadLocal<>();
    private transient TransactionManager transactionManager;

    public CoreUserTransaction(final TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public static RuntimeException error(final RuntimeException e) {
        final RuntimeException p = ERROR.get();
        ERROR.set(e);
        return p;
    }

    public static void resetError(final RuntimeException old) {
        ERROR.set(old);
    }

    public static RuntimeException error() {
        return ERROR.get();
    }

    private TransactionManager transactionManager() {
        if (transactionManager == null) {
            transactionManager = OpenEJB.getTransactionManager();
        }
        return transactionManager;
    }

    @Override
    public void begin() throws NotSupportedException, SystemException {
        check();
        transactionManager().begin();
        if (transactionLogger.isDebugEnabled()) {
            transactionLogger.debug("Started user transaction " + transactionManager().getTransaction());
        }
    }

    private void check() {
        final RuntimeException e = ERROR.get();
        if (e != null) {
            throw e;
        }
    }

    @Override
    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
        SecurityException, IllegalStateException, SystemException {
        check();
        if (transactionLogger.isDebugEnabled()) {
            transactionLogger.debug("Committing user transaction " + transactionManager().getTransaction());
        }
        transactionManager().commit();
    }

    @Override
    public int getStatus() throws SystemException {
        check();
        final int status = transactionManager().getStatus();
        if (transactionLogger.isDebugEnabled()) {
            transactionLogger.debug("User transaction " + transactionManager().getTransaction() + " has status " + getStatus(status));
        }
        return status;
    }

    @Override
    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        check();
        if (transactionLogger.isDebugEnabled()) {
            transactionLogger.debug("Rolling back user transaction " + transactionManager().getTransaction());
        }
        transactionManager().rollback();
    }

    @Override
    public void setRollbackOnly() throws SystemException {
        check();
        if (transactionLogger.isDebugEnabled()) {
            transactionLogger.debug("Marking user transaction for rollback: " + transactionManager().getTransaction());
        }
        transactionManager().setRollbackOnly();
    }

    @Override
    public void setTransactionTimeout(final int seconds) throws SystemException {
        check();
        transactionManager().setTransactionTimeout(seconds);
    }

    private static String getStatus(final int status) {
        final StringBuilder buffer = new StringBuilder(100);
        switch (status) {
            case Status.STATUS_ACTIVE:
                buffer.append("STATUS_ACTIVE: ");
                buffer.append("A transaction is associated with the target object and it is in the active state.");
                break;
            case Status.STATUS_COMMITTED:
                buffer.append("STATUS_COMMITTED: ");
                buffer.append("A transaction is associated with the target object and it has been committed.");
                break;
            case Status.STATUS_COMMITTING:
                buffer.append("STATUS_COMMITTING: ");
                buffer.append("A transaction is associated with the target object and it is in the process of committing.");
                break;
            case Status.STATUS_MARKED_ROLLBACK:
                buffer.append("STATUS_MARKED_ROLLBACK: ");
                buffer.append("A transaction is associated with the target object and it has been marked for rollback, perhaps as a result of a setRollbackOnly operation.");
                break;
            case Status.STATUS_NO_TRANSACTION:
                buffer.append("STATUS_NO_TRANSACTION: ");
                buffer.append("No transaction is currently associated with the target object.");
                break;
            case Status.STATUS_PREPARED:
                buffer.append("STATUS_PREPARED: ");
                buffer.append("A transaction is associated with the target object and it has been prepared, i.e.");
                break;
            case Status.STATUS_PREPARING:
                buffer.append("STATUS_PREPARING: ");
                buffer.append("A transaction is associated with the target object and it is in the process of preparing.");
                break;
            case Status.STATUS_ROLLEDBACK:
                buffer.append("STATUS_ROLLEDBACK: ");
                buffer.append("A transaction is associated with the target object and the outcome has been determined as rollback.");
                break;
            case Status.STATUS_ROLLING_BACK:
                buffer.append("STATUS_ROLLING_BACK: ");
                buffer.append("A transaction is associated with the target object and it is in the process of rolling back.");
                break;
            default:
                buffer.append("Unknown status ").append(status);
                break;
        }
        return buffer.toString();
    }
}
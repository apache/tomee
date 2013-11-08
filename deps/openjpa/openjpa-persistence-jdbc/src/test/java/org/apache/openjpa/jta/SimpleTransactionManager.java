/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.jta;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

/**
 * A very simple Transaction Manager for testing JTA resource without a container.
 * <br>
 * Provides a single transaction per-thread model.
 * 
 * @author Pinaki Poddar
 *
 */
public class SimpleTransactionManager implements TransactionManager {
    private static ThreadLocal<SimpleTransaction> txns = new ThreadLocal<SimpleTransaction>();
    
    /**
     * Begins a new transaction associated with the current thread.
     * 
     */
    public void begin() throws NotSupportedException, SystemException {
        SimpleTransaction txn = getTransaction();
        int status = txn.getStatus();
        if (status == Status.STATUS_COMMITTED || status == Status.STATUS_ROLLEDBACK || status == Status.STATUS_UNKNOWN
         || status == Status.STATUS_ACTIVE)
            txn.setStatus(Status.STATUS_ACTIVE);
        else
            throw new IllegalStateException("Can not begin " + txn);
    }

    /**
     * Commits a transaction associated with the current thread.
     * Raises IllegalStateException if no transaction is associated with the current thread. 
     * 
     */
    public void commit() throws HeuristicMixedException, HeuristicRollbackException, IllegalStateException,
            RollbackException, SecurityException, SystemException {
        assertActiveTransaction();
        try {
            getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            txns.set(null);
        }
        
    }

    /**
     * Gets the status of the transaction associated with the current thread.
     */
    public int getStatus() throws SystemException {
        if (txns.get() == null)
            return Status.STATUS_NO_TRANSACTION;
        return getTransaction().getStatus();
    }

    /**
     * Gets the transaction associated with the current thread.
     * If no transaction is associated then creates a transaction and
     * associates with the current thread.
     */
    public SimpleTransaction getTransaction() throws SystemException {
        SimpleTransaction txn = txns.get();
        if (txn == null) {
            txn = new SimpleTransaction();
            txn.setStatus(Status.STATUS_ACTIVE);
            txns.set(txn);
        }
        return txn;
    }

    /**
     * Not implemented. 
     * Raises UnsupportedOperationException.
     */
    public void resume(Transaction arg0) throws IllegalStateException, InvalidTransactionException, SystemException {
        throw new UnsupportedOperationException();
    }

    /**
     * Rolls back a transaction associated with the current thread.
     * Raises IllegalStateException if no transaction is associated with the current thread. 
     * 
     */
    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        assertActiveTransaction();
        try {
            getTransaction().rollback();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            txns.set(null);
        }
    }

    /**
     * Marks a transaction associated with the current thread for rollback.
     * Raises IllegalStateException if no transaction is associated with the current thread. 
     * 
     */
    public void setRollbackOnly() throws IllegalStateException, SystemException {
        assertActiveTransaction();
        getTransaction().setRollbackOnly();
    }

    /**
     * Not implemented. 
     * Raises UnsupportedOperationException.
     */
    public void setTransactionTimeout(int arg0) throws SystemException {
        throw new UnsupportedOperationException();
    }

    /**
     * Not implemented. 
     * Raises UnsupportedOperationException.
     */
    public Transaction suspend() throws SystemException {
        throw new UnsupportedOperationException();
    }
    
    void assertActiveTransaction() throws IllegalStateException, SystemException {
        if (getStatus() == Status.STATUS_NO_TRANSACTION)
            throw new IllegalStateException("No transaction on " + Thread.currentThread());
    }

}

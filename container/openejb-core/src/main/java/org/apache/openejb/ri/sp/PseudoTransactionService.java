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
package org.apache.openejb.ri.sp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;

import org.apache.openejb.spi.TransactionService;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

/**
 * @org.apache.xbean.XBean element="pseudoTransactionService"
 */
public class PseudoTransactionService implements TransactionService, TransactionManager, TransactionSynchronizationRegistry {
    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.core.cmp");
    private final ThreadLocal<MyTransaction> threadTransaction = new ThreadLocal<MyTransaction>();

    public void init(Properties props) {
    }

    public TransactionManager getTransactionManager() {
        return this;
    }

    public TransactionSynchronizationRegistry getTransactionSynchronizationRegistry() {
        return this;
    }

    public int getStatus() {
        MyTransaction tx = threadTransaction.get();
        if (tx == null) {
            return Status.STATUS_NO_TRANSACTION;
        }
        return tx.getStatus();
    }

    public Transaction getTransaction() {
        return threadTransaction.get();
    }

    public boolean getRollbackOnly() {
        MyTransaction tx = threadTransaction.get();
        if (tx == null) {
            throw new IllegalStateException("No transaction active");
        }
        return tx.getRollbackOnly();
    }

    public void setRollbackOnly() {
        MyTransaction tx = threadTransaction.get();
        if (tx == null) {
            throw new IllegalStateException("No transaction active");
        }
        tx.setRollbackOnly();
    }

    public void begin() throws NotSupportedException {
        if (threadTransaction.get() != null) {
            throw new NotSupportedException("A transaction is already active");
        }

        MyTransaction tx = new MyTransaction();
        threadTransaction.set(tx);
    }

    public void commit() throws RollbackException {
        MyTransaction tx = threadTransaction.get();
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
        MyTransaction tx = threadTransaction.get();
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

    public void resume(Transaction tx) throws InvalidTransactionException {
        if (tx == null) {
            throw new InvalidTransactionException("Transaction is null");
        }
        if (!(tx instanceof MyTransaction)) {
            throw new InvalidTransactionException("Unknown transaction type " + tx.getClass().getName());
        }
        MyTransaction myTransaction = (MyTransaction) tx;

        if (threadTransaction.get() != null) {
            throw new IllegalStateException("A transaction is already active");
        }

        int status = myTransaction.getStatus();
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

    public Object getResource(Object key) {
        MyTransaction tx = threadTransaction.get();
        if (tx == null) {
            throw new IllegalStateException("No transaction active");
        }

        Object value = tx.getResource(key);
        return value;
    }

    public void putResource(Object key, Object value) {
        MyTransaction tx = threadTransaction.get();
        if (tx == null) {
            throw new IllegalStateException("No transaction active");
        }

        tx.putResource(key, value);
    }

    public void registerInterposedSynchronization(Synchronization synchronization) {
        MyTransaction tx = threadTransaction.get();
        if (tx == null) {
            throw new IllegalStateException("No transaction active");
        }

        tx.registerInterposedSynchronization(synchronization);
    }

    public void setTransactionTimeout(int seconds) {
    }

    public class MyTransaction implements Transaction {
        private final List<Synchronization> registeredSynchronizations = Collections.synchronizedList(new ArrayList<Synchronization>());
        private final List<XAResource> xaResources =  Collections.synchronizedList(new ArrayList<XAResource>());
        private final Map<Object, Object> resources = new HashMap<Object,Object>();
        private int status = Status.STATUS_ACTIVE;

        public boolean delistResource(XAResource xaRes, int flag) {
            xaResources.remove(xaRes);
            return true;
        }

        public boolean enlistResource(XAResource xaRes) {
            xaResources.add(xaRes);
            return true;
        }

        public int getStatus() {
            return status;
        }

        public void registerSynchronization(Synchronization synchronization) {
            registeredSynchronizations.add(synchronization);
        }

        public void registerInterposedSynchronization(Synchronization synchronization) {
            registeredSynchronizations.add(synchronization);
        }

        public boolean getRollbackOnly() {
            return status == Status.STATUS_MARKED_ROLLBACK;
        }

        public void setRollbackOnly() {
            status = Status.STATUS_MARKED_ROLLBACK;
        }

        public Object getResource(Object key) {
            if (key == null) throw new NullPointerException("key is null");
            return resources.get(key);
        }

        public void putResource(Object key, Object value) {
            if (key == null) throw new NullPointerException("key is null");
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
                } catch (Exception e) {
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
            for (Synchronization sync : new ArrayList<Synchronization>(registeredSynchronizations)) {
                sync.beforeCompletion();
            }
        }

        private void doAfterCompletion(int status) {
            for (Synchronization sync : new ArrayList<Synchronization>(registeredSynchronizations)) {
                try {
                    sync.afterCompletion(status);
                } catch (RuntimeException e) {
                    logger.warning("Synchronization afterCompletion threw a RuntimeException", e);
                }
            }
        }

        private void doXAResources(int status) {
            for (XAResource xaRes : new ArrayList<XAResource>(xaResources)) {
                if (status == Status.STATUS_COMMITTED) {
                    try {
                        xaRes.commit(null, true);
                    } catch (XAException e) {

                    }
                    try {
                        xaRes.end(null, XAResource.TMSUCCESS);
                    } catch (XAException e) {

                    }
                } else {
                    try {
                        xaRes.rollback(null);
                    } catch (XAException e) {

                    }
                    try {
                        xaRes.end(null, XAResource.TMFAIL);
                    } catch (XAException e) {
                    }
                }
            }
            xaResources.clear();
        }
    }
}


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

import org.apache.openejb.spi.TransactionService;

import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import javax.transaction.xa.XAResource;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * @org.apache.xbean.XBean element="pseudoTransactionService"
 */
public class PseudoTransactionService implements TransactionService, TransactionManager {
    Hashtable map = new Hashtable();

    public void init(java.util.Properties props) {
    }

    public TransactionManager getTransactionManager() {
        return this;
    }

    public UserTransaction getUserTransaction(Object txID) {
        return new UserTransaction() {
            public void begin() {
                PseudoTransactionService.this.begin();
            }

            public void commit() throws RollbackException {
                PseudoTransactionService.this.commit();
            }

            public int getStatus() throws javax.transaction.SystemException {
                return PseudoTransactionService.this.getStatus();
            }

            public void rollback() {
                PseudoTransactionService.this.rollback();
            }

            public void setRollbackOnly() {
                PseudoTransactionService.this.setRollbackOnly();
            }

            public void setTransactionTimeout(int seconds) {
                PseudoTransactionService.this.setTransactionTimeout(seconds);
            }
        };
    }

    public void begin() {
        Transaction tx = new MyTransaction();
        map.put(Thread.currentThread(), tx);
    }

    public void commit() throws RollbackException {
        MyTransaction tx = (MyTransaction) map.remove(Thread.currentThread());
        if (tx != null) {
            tx.commit();
        } else {
            throw new IllegalStateException();
        }
    }

    public int getStatus() throws javax.transaction.SystemException {
        Transaction tx = (Transaction) map.get(Thread.currentThread());
        if (tx == null) {
            return Status.STATUS_NO_TRANSACTION;
        }
        return tx.getStatus();
    }

    public Transaction getTransaction() {
        return (Transaction) map.get(Thread.currentThread());
    }

    public void resume(Transaction tx)
            throws javax.transaction.SystemException, javax.transaction.InvalidTransactionException {
        Transaction ctx = (Transaction) map.get(Thread.currentThread());
        int status = tx.getStatus();

        if (ctx != null || tx == null || (status != Status.STATUS_ACTIVE && status != Status.STATUS_MARKED_ROLLBACK)) {
            throw new javax.transaction.InvalidTransactionException();
        }
        map.put(Thread.currentThread(), tx);
    }

    public Transaction suspend() {
        return (Transaction) map.remove(Thread.currentThread());
    }

    public void rollback() {
        MyTransaction tx = (MyTransaction) map.remove(Thread.currentThread());
        if (tx == null) {
            throw new IllegalStateException();
        }
        tx.rollback();
    }

    public void setRollbackOnly() {
        MyTransaction tx = (MyTransaction) map.get(Thread.currentThread());
        if (tx == null) {
            throw new IllegalStateException();
        }
        tx.setRollbackOnly();
    }

    public void setTransactionTimeout(int x) {
    }

    public class MyTransaction implements Transaction {
        Vector registeredSynchronizations = new Vector();
        Vector xaResources = new Vector();
        int status = Status.STATUS_ACTIVE;

        public void commit() throws RollbackException {
            if (status == Status.STATUS_MARKED_ROLLBACK) {
                rollback();
                throw new RollbackException();
            }
            doBeforeCompletion();
            doXAResources(Status.STATUS_COMMITTED);
            status = Status.STATUS_COMMITTED;
            doAfterCompletion(Status.STATUS_COMMITTED);
            registeredSynchronizations = new Vector();
            map.remove(Thread.currentThread());
        }

        public boolean delistResource(XAResource xaRes, int flag) {
            xaResources.remove(xaRes);
            return true;
        }

        public boolean enlistResource(XAResource xaRes) {
            xaResources.addElement(xaRes);
            return true;
        }

        public int getStatus() {
            return status;
        }

        public void registerSynchronization(Synchronization sync) {
            registeredSynchronizations.addElement(sync);
        }

        public void rollback() {
            doXAResources(Status.STATUS_ROLLEDBACK);
            doAfterCompletion(Status.STATUS_ROLLEDBACK);
            status = Status.STATUS_ROLLEDBACK;
            registeredSynchronizations = new Vector();
            map.remove(Thread.currentThread());
        }

        public void setRollbackOnly() {
            status = Status.STATUS_MARKED_ROLLBACK;
        }


        private void doBeforeCompletion() {
            Enumeration enumeration = registeredSynchronizations.elements();
            while (enumeration.hasMoreElements()) {
                try {
                    Synchronization sync = (Synchronization) enumeration.nextElement();
                    sync.beforeCompletion();
                } catch (RuntimeException re) {
                    re.printStackTrace();
                }
            }
        }

        private void doAfterCompletion(int status) {
            Enumeration enumeration = registeredSynchronizations.elements();
            while (enumeration.hasMoreElements()) {
                try {
                    Synchronization sync = (Synchronization) enumeration.nextElement();
                    sync.afterCompletion(status);
                } catch (RuntimeException re) {
                    re.printStackTrace();
                }
            }
        }

        private void doXAResources(int status) {
            Object [] resources = xaResources.toArray();
            for (int i = 0; i < resources.length; i++) {
                XAResource xaRes = (XAResource) resources[i];
                if (status == Status.STATUS_COMMITTED) {
                    try {
                        xaRes.commit(null, true);
                    } catch (javax.transaction.xa.XAException xae) {

                    }
                    try {
                        xaRes.end(null, XAResource.TMSUCCESS);
                    } catch (javax.transaction.xa.XAException xae) {

                    }
                } else {
                    try {
                        xaRes.rollback(null);
                    } catch (javax.transaction.xa.XAException xae) {

                    }
                    try {
                        xaRes.end(null, XAResource.TMFAIL);
                    } catch (javax.transaction.xa.XAException xae) {

                    }
                }
            }
            xaResources = new Vector();

        }

    }
}


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
package org.apache.openejb.core;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.Status;

import org.apache.openejb.util.Logger;

/**
 * @org.apache.xbean.XBean element="userTransaction"
 */
public class CoreUserTransaction implements javax.transaction.UserTransaction, java.io.Serializable {
    private static final long serialVersionUID = 9203248912222645965L;
    private static final Logger transactionLogger = Logger.getInstance("Transaction", "org.apache.openejb.util.resources");

    private transient TransactionManager transactionManager;

    public CoreUserTransaction(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    private TransactionManager transactionManager() {
        // DMB: taking this out is fine unless it is serialized as part of a stateful sessionbean passivation
        // when the bean is activated
//        if (transactionManager == null) {
//            transactionManager = org.apache.openejb.OpenEJB.getTransactionManager();
//        }
        return transactionManager;
    }

    public void begin() throws NotSupportedException, SystemException {
        transactionManager().begin();
        if (transactionLogger.isInfoEnabled()) {
            transactionLogger.info("Started user transaction " + transactionManager().getTransaction());
        }
    }

    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
            SecurityException, IllegalStateException, SystemException {
        if (transactionLogger.isInfoEnabled()) {
            transactionLogger.info("Committing user transaction " + transactionManager().getTransaction());
        }
        transactionManager().commit();
    }

    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        if (transactionLogger.isInfoEnabled()) {
            transactionLogger.info("Rolling back user transaction " + transactionManager().getTransaction());
        }
        transactionManager().rollback();
    }

    public int getStatus() throws SystemException {
        int status = transactionManager().getStatus();
        if (transactionLogger.isInfoEnabled()) {
            transactionLogger.info("User transaction " + transactionManager().getTransaction() + " has status " + getStatus(status));
        }
        return status;
    }

    public void setRollbackOnly() throws javax.transaction.SystemException {
        if (transactionLogger.isInfoEnabled()) {
            transactionLogger.info("Marking user transaction for rollback: " + transactionManager().getTransaction());
        }
        transactionManager().setRollbackOnly();
    }

    public void setTransactionTimeout(int seconds) throws SystemException {
        transactionManager().setTransactionTimeout(seconds);
    }

    private static String getStatus(int status) {
        StringBuffer buffer;

        buffer = new StringBuffer(100);
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
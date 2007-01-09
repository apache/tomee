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

/**
 * @org.apache.xbean.XBean element="userTransaction"
 */
public class CoreUserTransaction implements javax.transaction.UserTransaction, java.io.Serializable {

    private transient TransactionManager transactionManager;

    private transient final org.apache.log4j.Category transactionLogger;

    public CoreUserTransaction(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
        transactionLogger = org.apache.log4j.Category.getInstance("Transaction");
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
            transactionLogger.info("User transaction " + transactionManager().getTransaction() + " has status " + org.apache.openejb.core.TransactionManagerWrapper.getStatus(status));
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

}
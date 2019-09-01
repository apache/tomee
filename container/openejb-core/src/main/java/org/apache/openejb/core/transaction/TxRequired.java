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

import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

/**
 * 17.6.2.2 Required
 *
 * The Container must invoke an enterprise Bean method whose transaction
 * attribute is set to Required with a valid transaction context.
 *
 * If a client invokes the enterprise Bean's method while the client is
 * associated with a transaction context, the container invokes the enterprise
 * Bean's method in the client's transaction context.
 *
 * If the client invokes the enterprise Bean's method while the client is not
 * associated with a transaction context, the container automatically starts a
 * new transaction before delegating a method call to the enterprise Bean
 * business method. The Container automatically enlists all the resource
 * managers accessed by the business method with the transaction. If the
 * business method invokes other enterprise beans, the Container passes the
 * transaction context with the invocation. The Container attempts to commit the
 * transaction when the business method has completed. The container performs
 * the commit protocol before the method result is sent to the client.
 */
public class TxRequired extends JtaTransactionPolicy {
    private final Transaction clientTx;
    private final Transaction currentTx;

    public TxRequired(final TransactionManager transactionManager) throws SystemException {
        super(TransactionType.Required, transactionManager);

        clientTx = getTransaction();
        if (clientTx == null) {
            currentTx = beginTransaction();
        } else {
            currentTx = clientTx;
        }
    }

    public boolean isNewTransaction() {
        return clientTx == null;
    }

    public boolean isClientTransaction() {
        return !isNewTransaction();
    }

    public Transaction getCurrentTransaction() {
        return currentTx;
    }

    public void commit() throws ApplicationException, SystemException {
        // only commit if we started the transaction
        if (clientTx == null) {
            completeTransaction(currentTx);
        } else {
            fireNonTransactionalCompletion();
        }
    }
}
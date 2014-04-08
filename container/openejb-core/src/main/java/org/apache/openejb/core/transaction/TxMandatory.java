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

import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionRequiredException;

import org.apache.openejb.ApplicationException;
import org.apache.openejb.SystemException;

/**
 * 17.6.2.5 Mandatory
 * <p/>
 * The Container must invoke an enterprise Bean method whose transaction
 * attribute is set to Mandatory in a client's transaction context. The client
 * is required to call with a transaction context.
 * <p/>
 * * If the client calls with a transaction context, the container invokes the
 * enterprise Bean's method in the client's transaction context.
 * <p/>
 * * If the client calls without a transaction context, the Container throws the
 * javax.transaction.TransactionRequiredException exception if the client is a
 * remote client, or the javax.ejb.TransactionRequiredLocalException if the
 * client is a local client.
 */
public class TxMandatory extends JtaTransactionPolicy {
    private final Transaction clientTx;

    public TxMandatory(TransactionManager transactionManager) throws SystemException, ApplicationException {
        super(TransactionType.Mandatory, transactionManager);

        clientTx = getTransaction();
        if (clientTx == null) {
            throw new ApplicationException(new TransactionRequiredException());
        }
    }

    public boolean isNewTransaction() {
        return false;
    }

    public boolean isClientTransaction() {
        return true;
    }

    public Transaction getCurrentTransaction() {
        return clientTx;
    }

    public void commit() {
        fireNonTransactionalCompletion();
    }
}


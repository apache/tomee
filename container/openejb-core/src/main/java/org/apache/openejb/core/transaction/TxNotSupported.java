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

import org.apache.openejb.SystemException;

import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;

/**
 * 17.6.2.1 NotSupported
 *
 * The Container invokes an enterprise Bean method whose transaction attribute
 * is set to NotSupported with an unspecified transaction context.
 *
 * If a client calls with a transaction context, the container suspends the
 * association of the transaction context with the current thread before
 * invoking the enterprise bean's business method. The container resumes the
 * suspended association when the business method has completed. The suspended
 * transaction context of the client is not passed to the resource managers or
 * other enterprise Bean objects that are invoked from the business method.
 *
 * If the business method invokes other enterprise beans, the Container passes
 * no transaction context with the invocation.
 *
 * Refer to Subsection 17.6.5 for more details of how the Container can
 * implement this case.
 */
public class TxNotSupported extends JtaTransactionPolicy {
    private final Transaction clientTx;

    public TxNotSupported(final TransactionManager transactionManager) throws SystemException {
        super(TransactionType.NotSupported, transactionManager);

        clientTx = suspendTransaction();
    }

    public boolean isNewTransaction() {
        return false;
    }

    public boolean isClientTransaction() {
        return false;
    }

    public Transaction getCurrentTransaction() {
        return null;
    }

    public void commit() throws SystemException {
        fireNonTransactionalCompletion();

        if (clientTx != null) {
            resumeTransaction(clientTx);
        }
    }
}


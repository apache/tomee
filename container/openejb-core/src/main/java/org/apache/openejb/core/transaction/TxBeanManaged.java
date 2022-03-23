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
import org.apache.openejb.core.CoreUserTransaction;

import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.UserTransaction;
import java.rmi.RemoteException;

public class TxBeanManaged extends JtaTransactionPolicy implements BeanTransactionPolicy {
    private final Transaction clientTx;

    public TxBeanManaged(final TransactionManager transactionManager) throws SystemException {
        super(TransactionType.BeanManaged, transactionManager);

        clientTx = suspendTransaction();
    }

    public boolean isNewTransaction() {
        return false;
    }

    public boolean isClientTransaction() {
        return false;
    }

    public Transaction getCurrentTransaction() {
        try {
            return getTransaction();
        } catch (final SystemException e) {
            throw new IllegalStateException("Exception getting current transaction");
        }
    }

    public UserTransaction getUserTransaction() {
        return new CoreUserTransaction(transactionManager);
    }

    public SuspendedTransaction suspendUserTransaction() throws SystemException {
        // Get the transaction after the method invocation
        final Transaction currentTx = suspendTransaction();
        if (currentTx == null) {
            return null;
        }

        return new JtaSuspendedTransaction(currentTx);
    }

    public void resumeUserTransaction(final SuspendedTransaction suspendedTransaction) throws SystemException {
        if (suspendedTransaction == null) {
            throw new NullPointerException("suspendedTransaction is null");
        }

        final Transaction beanTransaction = ((JtaSuspendedTransaction) suspendedTransaction).transaction;
        if (beanTransaction == null) {
            throw new SystemException("Bean transaction has already been resumed or destroyed");
        }

        try {
            resumeTransaction(beanTransaction);
        } catch (final SystemException e) {
            suspendedTransaction.destroy();
            throw e;
        }
    }

    public void commit() throws ApplicationException, SystemException {
        try {
            // The Container must detect the case in which a transaction was started, but
            // not completed, in the business method, and handle it as follows:
            final Transaction currentTx = getTransaction();
            if (currentTx != null) {
                final String message = "The EJB started a transaction but did not complete it.";

                /* [1] Log this as an application error ********/
                logger.error(message);

                /* [2] Roll back the started transaction *******/
                try {
                    rollbackTransaction(currentTx);
                } catch (final Throwable t) {
                    // no-op
                }

                /* [3] Throw the RemoteException to the client */
                throw new ApplicationException(new RemoteException(message));
            }

            fireNonTransactionalCompletion();
        } finally {
            resumeTransaction(clientTx);
        }
    }

    private static class JtaSuspendedTransaction implements SuspendedTransaction {
        private Transaction transaction;

        public JtaSuspendedTransaction(final Transaction transaction) {
            if (transaction == null) {
                throw new NullPointerException("transaction is null");
            }
            this.transaction = transaction;
        }

        public void destroy() {
            final Transaction beanTransaction = transaction;
            transaction = null;
            if (beanTransaction == null) {
                return;
            }

            try {
                beanTransaction.rollback();
            } catch (final Exception e) {
                logger.error("Error rolling back suspended transaction for discarded stateful session bean instance");
            }
        }
    }
}

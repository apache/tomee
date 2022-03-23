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

import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import java.rmi.RemoteException;

/**
 * 17.6.2.6 Never
 *
 * The Container invokes an enterprise Bean method whose transaction attribute
 * is set to Never without a transaction context defined by the EJB spec.
 *
 * The client is required to call without a transaction context.
 *
 * If the client calls with a transaction context, the Container throws:<ul>
 * <li>java.rmi.RemoteException exception if the client is a remote client</li>
 * <li>jakarta.ejb.EJBException if the client is a local client</li> </ul>
 *
 * If the client calls without a transaction context, the Container performs the
 * same steps as described in the NotSupported case.
 */
public class TxNever extends JtaTransactionPolicy {
    public TxNever(final TransactionManager transactionManager) throws SystemException, ApplicationException {
        super(TransactionType.Never, transactionManager);

        if (getTransaction() != null) {
            throw new ApplicationException(new RemoteException("Transactions not supported"));
        }
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

    public void commit() {
        fireNonTransactionalCompletion();
    }
}


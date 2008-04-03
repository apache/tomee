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
package org.apache.openejb.core.transaction;

import org.apache.openejb.ApplicationException;
import org.apache.openejb.SystemException;

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
 * <li>javax.ejb.EJBException if the client is a local client</li>
 * </ul>
 * 
 * If the client calls without a transaction context, the Container performs
 * the same steps as described in the NotSupported case.
 */
public class TxNever extends TransactionPolicy {

    public TxNever(TransactionContainer container) {
        super(Type.Never, container);
    }

    public void beforeInvoke(Object instance, TransactionContext context) throws SystemException, ApplicationException {
        context.callContext.set(Type.class, getPolicyType());

        try {

            if (context.getTransactionManager().getTransaction() != null) {

                throw new ApplicationException(new RemoteException("Transactions not supported"));
            }

        } catch (javax.transaction.SystemException se) {
            logger.error("Exception during getTransaction()", se);
            throw new SystemException(se);
        }
    }

    public void afterInvoke(Object instance, TransactionContext context) throws ApplicationException, SystemException {
    }

    public void handleApplicationException(Throwable appException, boolean rollback, TransactionContext context) throws ApplicationException, SystemException {
        if (rollback && context.currentTx != null) markTxRollbackOnly(context.currentTx);

        throw new ApplicationException(appException);
    }

    public void handleSystemException(Throwable sysException, Object instance, TransactionContext context) throws ApplicationException, SystemException {
        /* [1] Log the system exception or error *********/
        logSystemException(sysException, context);

        /* [2] Discard instance. *************************/
        discardBeanInstance(instance, context.callContext);

        /* [3] Throw RemoteException to client ***********/
        throwExceptionToServer(sysException);
    }

}


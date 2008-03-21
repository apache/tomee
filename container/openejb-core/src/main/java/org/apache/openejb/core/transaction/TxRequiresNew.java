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

import javax.transaction.Status;

/**
 * 17.6.2.4 RequiresNew
 *
 * The Container must invoke an enterprise Bean method whose transaction
 * attribute is set to RequiresNew with a new transaction context.
 *
 * If the client invokes the enterprise Bean’s method while the client is not
 * associated with a transaction context, the container automatically starts a
 * new transaction before delegating a method call to the enterprise Bean
 * business method. The Container automatically enlists all the resource
 * managers accessed by the business method with the transaction. If the
 * business method invokes other enterprise beans, the Container passes the
 * transaction context with the invocation. The Container attempts to commit
 * the transaction when the business method has completed. The container
 * performs the commit protocol before the method result is sent to the client.
 *
 * If a client calls with a transaction context, the container suspends the
 * association of the transaction context with the current thread before
 * starting the new transaction and invoking the business method. The container
 * resumes the suspended transaction association after the business method and
 * the new transaction have been completed.
 *
 */
public class TxRequiresNew extends TransactionPolicy {

    public TxRequiresNew(TransactionContainer container) {
        super(Type.RequiresNew, container);
    }

    public void beforeInvoke(Object instance, TransactionContext context) throws SystemException, ApplicationException {
        context.callContext.set(Type.class, getPolicyType());

        try {

            context.clientTx = suspendTransaction(context);
            beginTransaction(context);
            context.currentTx = context.getTransactionManager().getTransaction();

        } catch (javax.transaction.SystemException se) {
            throw new SystemException(se);
        }

    }

    public void afterInvoke(Object instance, TransactionContext context) throws ApplicationException, SystemException {

        try {

            if (context.currentTx.getStatus() == Status.STATUS_ACTIVE) {
                commitTransaction(context, context.currentTx);
            } else {
                rollbackTransaction(context, context.currentTx);
            }

        } catch (javax.transaction.SystemException se) {
            throw new SystemException(se);
        } finally {
            if (context.clientTx != null) {
                resumeTransaction(context, context.clientTx);
            } else if (txLogger.isInfoEnabled()) {
                txLogger.info("TX " + policyToString() + ": No transaction to resume");
            }
        }
    }

    public void handleApplicationException(Throwable appException, boolean rollback, TransactionContext context) throws ApplicationException, SystemException {
        if (rollback && context.currentTx != null) markTxRollbackOnly(context.currentTx);

        throw new ApplicationException(appException);
    }

    public void handleSystemException(Throwable sysException, Object instance, TransactionContext context) throws ApplicationException, SystemException {

        /* [1] Log the system exception or error **********/
        logSystemException(sysException, context);

        /* [2] afterInvoke will roll back the tx */
        markTxRollbackOnly(context.currentTx);

        /* [3] Discard instance. **************************/
        discardBeanInstance(instance, context.callContext);

        /* [4] Throw RemoteException to client ************/
        throwExceptionToServer(sysException);

    }
}


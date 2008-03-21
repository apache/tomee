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

import javax.transaction.Status;

import org.apache.openejb.ApplicationException;
import org.apache.openejb.SystemException;

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
 * transaction context with the invocation. The Container attempts to commit
 * the transaction when the business method has completed. The container
 * performs the commit protocol before the method result is sent to the client.
 *
 */
public class TxRequired extends TransactionPolicy {

    public TxRequired(TransactionContainer container) {
        super(Type.Required, container);
    }

    public void beforeInvoke(Object instance, TransactionContext context) throws SystemException, ApplicationException {
        context.callContext.set(Type.class, getPolicyType());

        try {

            context.clientTx = context.getTransactionManager().getTransaction();

            if (context.clientTx == null) {
                beginTransaction(context);
            }

            context.currentTx = context.getTransactionManager().getTransaction();

        } catch (javax.transaction.SystemException se) {
            logger.error("Exception during getTransaction()", se);
            throw new SystemException(se);
        }
    }

    public void afterInvoke(Object instance, TransactionContext context) throws ApplicationException, SystemException {

        try {
            if (context.clientTx != null) return;

            if (context.currentTx.getStatus() == Status.STATUS_ACTIVE) {
                commitTransaction(context, context.currentTx);
            } else {
                rollbackTransaction(context, context.currentTx);
            }

        } catch (javax.transaction.SystemException se) {
            logger.debug("Exception during getTransaction()", se);
            throw new SystemException(se);
        }
    }

    public void handleApplicationException(Throwable appException, boolean rollback, TransactionContext context) throws ApplicationException, SystemException {
        if (rollback && context.currentTx != null) markTxRollbackOnly(context.currentTx);

        throw new ApplicationException(appException);
    }

    public void handleSystemException(Throwable sysException, Object instance, TransactionContext context) throws ApplicationException, SystemException {

        /* [1] Log the system exception or error **********/
        logSystemException(sysException, context);

        boolean runningInContainerTransaction = (!context.currentTx.equals(context.clientTx));
        if (runningInContainerTransaction) {
            /* [2] Mark the transaction for rollback. afterInvoke() will roll it back */
            markTxRollbackOnly(context.currentTx);

            /* [3] Discard instance. **************************/
            discardBeanInstance(instance, context.callContext);

            /* [4] Throw RemoteException to client ************/
            throwExceptionToServer(sysException);
        } else {
            /* [2] Mark the transaction for rollback. *********/
            markTxRollbackOnly(context.clientTx);

            /* [3] Discard instance. **************************/
            discardBeanInstance(instance, context.callContext);

            /* [4] Throw TransactionRolledbackException to client ************/
            throwTxExceptionToServer(sysException);
        }
    }
}

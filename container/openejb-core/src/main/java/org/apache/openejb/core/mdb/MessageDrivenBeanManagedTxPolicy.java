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
package org.apache.openejb.core.mdb;

import java.rmi.RemoteException;
import javax.transaction.Status;

import org.apache.openejb.ApplicationException;
import org.apache.openejb.SystemException;
import org.apache.openejb.ContainerType;
import org.apache.openejb.core.transaction.TransactionContainer;
import org.apache.openejb.core.transaction.TransactionContext;
import org.apache.openejb.core.transaction.TransactionPolicy;


public class MessageDrivenBeanManagedTxPolicy extends TransactionPolicy {

    public MessageDrivenBeanManagedTxPolicy(TransactionContainer container) {
        super(Type.BeanManaged, container);
        if (container.getContainerType() != ContainerType.MESSAGE_DRIVEN) {
            throw new IllegalArgumentException();
        }
    }

    public void beforeInvoke(Object instance, TransactionContext context) throws SystemException, ApplicationException {
        context.clientTx = suspendTransaction(context);
    }

    @SuppressWarnings({"EmptyCatchBlock"})
    public void afterInvoke(Object instance, TransactionContext context) throws ApplicationException, SystemException {

        try {
            /*
            * The Container must detect the case in which a transaction was started, but
            * not completed, in the business method, and handle it as follows:
            */
            context.currentTx = context.getTransactionManager().getTransaction();

            if (context.currentTx == null) return;

            if (context.currentTx.getStatus() != Status.STATUS_ROLLEDBACK && context.currentTx.getStatus() != Status.STATUS_COMMITTED) {
                String message = "The message driven bean started a transaction but did not complete it.";

                /* [1] Log this as an application error ********/
                logger.error(message);

                /* [2] Roll back the started transaction *******/
                try {
                    rollbackTransaction(context, context.currentTx);
                } catch (Throwable ignore) {

                }

                /* [3] Throw the RemoteException to the client */
                throwAppExceptionToServer(new RemoteException(message));
            }

        } catch (javax.transaction.SystemException e) {
            throw new SystemException(e);
        } finally {
            resumeTransaction(context, context.clientTx);
        }
    }

    public void handleApplicationException(Throwable appException, boolean rollback, TransactionContext context) throws ApplicationException, SystemException {
        if (rollback && context.currentTx != null) markTxRollbackOnly(context.currentTx);

        throw new ApplicationException(appException);
    }

    public void handleSystemException(Throwable sysException, Object instance, TransactionContext context) throws ApplicationException, SystemException {
        try {
            context.currentTx = context.getTransactionManager().getTransaction();
        } catch (javax.transaction.SystemException e) {
            context.currentTx = null;
        }

        logSystemException(sysException, context);

        if (context.currentTx != null) markTxRollbackOnly(context.currentTx);

        discardBeanInstance(instance, context.callContext);

        throwExceptionToServer(sysException);
    }

}


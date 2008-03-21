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
package org.apache.openejb.core.stateful;

import javax.transaction.Status;
import javax.transaction.Transaction;

import org.apache.openejb.ApplicationException;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.SystemException;
import org.apache.openejb.ContainerType;
import org.apache.openejb.core.transaction.TransactionContainer;
import org.apache.openejb.core.transaction.TransactionContext;
import org.apache.openejb.core.transaction.TransactionPolicy;

public class StatefulBeanManagedTxPolicy extends TransactionPolicy {
    public StatefulBeanManagedTxPolicy(TransactionContainer container) {
        super(Type.BeanManaged, container);
        if (container.getContainerType() != ContainerType.STATEFUL) {
            throw new IllegalArgumentException("Container is not an StatefulContainer");
        }
    }

    public void beforeInvoke(Object instance, TransactionContext context) throws SystemException, ApplicationException {
        context.callContext.set(Type.class, getPolicyType());
        
        try {
            StatefulInstanceManager instanceManager = ((StatefulContainer)container).getInstanceManager();

            // suspend any transaction currently associated with this thread
            // if no transaction ---> suspend returns null
            context.clientTx = suspendTransaction(context);

            // Resume previous Bean transaction if there was one
            Transaction beanTransaction = instanceManager.getBeanTransaction(context.callContext);
            if (beanTransaction != null) {
                context.currentTx = beanTransaction;
                resumeTransaction(context, context.currentTx);
            }
        } catch (OpenEJBException e) {
            handleSystemException(e.getRootCause(), instance, context);
        }
    }

    public void afterInvoke(Object instance, TransactionContext context) throws ApplicationException, SystemException {
        try {
            // Get the transaction after the method invocation
            context.currentTx = context.getTransactionManager().getTransaction();

            // If it is not complete, suspend the transaction
            if (context.currentTx != null) {
                int status = context.currentTx.getStatus();
                if (status != Status.STATUS_COMMITTED && status != Status.STATUS_ROLLEDBACK) {
                    suspendTransaction(context);
                } else {
                    // transaction is complete, so there is no need to maintain a referecne to it
                    context.clientTx = null;
                }
            }

            // Update the user transaction reference in the bean instance data
            StatefulInstanceManager instanceManager = ((StatefulContainer)container).getInstanceManager();
            instanceManager.setBeanTransaction(context.callContext, context.currentTx);
        } catch (OpenEJBException e) {
            handleSystemException(e.getRootCause(), instance, context);
        } catch (javax.transaction.SystemException e) {
            handleSystemException(e, instance, context);
        } catch (Throwable e) {
            handleSystemException(e, instance, context);
        } finally {
            resumeTransaction(context, context.clientTx);
        }
    }

    public void handleApplicationException(Throwable appException, boolean rollback, TransactionContext context) throws ApplicationException, SystemException {
        if (rollback && context.currentTx != null) markTxRollbackOnly(context.currentTx);
        throw new ApplicationException(appException);
    }

    public void handleSystemException(Throwable sysException, Object instance, TransactionContext context) throws ApplicationException, SystemException {
        logSystemException(sysException, context);

        if (context.currentTx != null) markTxRollbackOnly(context.currentTx);

        discardBeanInstance(instance, context.callContext);

        throwExceptionToServer(sysException);
    }
}


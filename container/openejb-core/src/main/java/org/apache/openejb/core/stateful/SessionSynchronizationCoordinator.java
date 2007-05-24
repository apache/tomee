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

import org.apache.openejb.ApplicationException;
import org.apache.openejb.SystemException;
import org.apache.openejb.core.BaseContext;
import org.apache.openejb.core.stateful.StatefulContext;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.interceptor.InterceptorStack;
import org.apache.openejb.core.interceptor.InterceptorData;
import org.apache.openejb.core.transaction.TransactionContext;
import org.apache.openejb.util.Logger;

import javax.ejb.SessionSynchronization;
import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.lang.reflect.Method;

public class SessionSynchronizationCoordinator implements javax.transaction.Synchronization {
    private static Logger logger = Logger.getInstance("OpenEJB", "org.apache.openejb.util.resources");

    private static Map<Transaction,SessionSynchronizationCoordinator> coordinators = new HashMap<Transaction,SessionSynchronizationCoordinator>();

    private final Map<Object,ThreadContext> sessionSynchronizations = new HashMap<Object,ThreadContext>();
    private final TransactionManager transactionManager;

    private SessionSynchronizationCoordinator(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public static void registerSessionSynchronization(StatefulInstanceManager.Instance instance, TransactionContext context) throws javax.transaction.SystemException, javax.transaction.RollbackException {
        SessionSynchronizationCoordinator coordinator = null;

        coordinator = coordinators.get(context.currentTx);

        if (coordinator == null) {
            coordinator = new SessionSynchronizationCoordinator(context.getTransactionManager());
            try {
                context.currentTx.registerSynchronization(coordinator);
            } catch (Exception e) {
                // todo this seems bad...
                logger.error("Transaction.registerSynchronization failed.", e);
                return;
            }
            coordinators.put(context.currentTx, coordinator);
        }

        coordinator._registerSessionSynchronization(instance, context.callContext);
    }

    private void _registerSessionSynchronization(StatefulInstanceManager.Instance instance, ThreadContext callContext) {
        boolean registered = sessionSynchronizations.containsKey(callContext.getPrimaryKey());

        if (registered) return;

        try {
            callContext = new ThreadContext(callContext.getDeploymentInfo(), callContext.getPrimaryKey());
        } catch (Exception e) {
        }
        sessionSynchronizations.put(callContext.getPrimaryKey(), callContext);

        Operation currentOperation = callContext.getCurrentOperation();
        callContext.setCurrentOperation(Operation.AFTER_BEGIN);
        BaseContext.State[] originalStates = callContext.setCurrentAllowedStates(StatefulContext.getStates());
        try {

            Method afterBegin = SessionSynchronization.class.getMethod("afterBegin");

            List<InterceptorData> interceptors = callContext.getDeploymentInfo().getMethodInterceptors(afterBegin);
            InterceptorStack interceptorStack = new InterceptorStack(instance.bean, afterBegin, Operation.AFTER_BEGIN, interceptors, instance.interceptors);
            interceptorStack.invoke();

        } catch (Exception e) {
            String message = "An unexpected system exception occured while invoking the afterBegin method on the SessionSynchronization object: " + e.getClass().getName() + " " + e.getMessage();
            logger.error(message, e);
            throw new RuntimeException(message);

        } finally {
            callContext.setCurrentOperation(currentOperation);
            callContext.setCurrentAllowedStates(originalStates);
        }
    }

    public void beforeCompletion() {

        Object[] contexts = sessionSynchronizations.values().toArray();

        for (int i = 0; i < contexts.length; i++) {
            // don't call beforeCompletion when transaction is marked rollback only
            if (getTransactionStatus() == Status.STATUS_MARKED_ROLLBACK) return;

            ThreadContext callContext = (ThreadContext) contexts[i];

            ThreadContext oldCallContext = ThreadContext.enter(callContext);
            StatefulInstanceManager instanceManager = null;

            try {
                StatefulContainer container = (StatefulContainer) callContext.getDeploymentInfo().getContainer();
                instanceManager = container.getInstanceManager();
                /*
                * the operation must be set before the instance is obtained from the pool, so
                * that the instance manager doesn't mistake this as a concurrent access.
                */
                callContext.setCurrentOperation(Operation.BEFORE_COMPLETION);
                callContext.setCurrentAllowedStates(StatefulContext.getStates());

                StatefulInstanceManager.Instance instance = (StatefulInstanceManager.Instance) instanceManager.obtainInstance(callContext.getPrimaryKey(), callContext);

                Method beforeCompletion = SessionSynchronization.class.getMethod("beforeCompletion");

                List<InterceptorData> interceptors = callContext.getDeploymentInfo().getMethodInterceptors(beforeCompletion);
                InterceptorStack interceptorStack = new InterceptorStack(instance.bean, beforeCompletion, Operation.BEFORE_COMPLETION, interceptors, instance.interceptors);
                interceptorStack.invoke();

                instanceManager.poolInstance(callContext, instance);
            } catch (org.apache.openejb.InvalidateReferenceException inv) {

            } catch (Exception e) {

                String message = "An unexpected system exception occured while invoking the beforeCompletion method on the SessionSynchronization object: " + e.getClass().getName() + " " + e.getMessage();

                /* [1] Log the exception or error */
                logger.error(message, e);

                /* [2] If the instance is in a transaction, mark the transaction for rollback. */
                Transaction tx = null;
                try {
                    tx = getTransactionManager().getTransaction();
                } catch (Throwable t) {
                    logger.error("Could not retreive the current transaction from the transaction manager while handling a callback exception from the beforeCompletion method of bean " + callContext.getPrimaryKey());
                }
                try {
                    markTxRollbackOnly(tx);
                } catch (Throwable t) {
                    logger.error("Could not mark the current transaction for rollback while handling a callback exception from the beforeCompletion method of bean " + callContext.getPrimaryKey());
                }

                /* [3] Discard the instance */
                discardInstance(instanceManager, callContext);

                /* [4] throw the java.rmi.RemoteException to the client */
                throw new RuntimeException(message);
            } finally {
                ThreadContext.exit(oldCallContext);
            }
        }
    }

    public void afterCompletion(int status) {

        Object[] contexts = sessionSynchronizations.values().toArray();

        try {
            Transaction tx = getTransactionManager().getTransaction();
            coordinators.remove(tx);
        } catch (Exception e) {
            logger.error("", e);
        }
        for (int i = 0; i < contexts.length; i++) {

            ThreadContext callContext = (ThreadContext) contexts[i];

            ThreadContext oldCallContext = ThreadContext.enter(callContext);
            StatefulInstanceManager instanceManager = null;

            try {
                StatefulContainer container = (StatefulContainer) callContext.getDeploymentInfo().getContainer();
                instanceManager = container.getInstanceManager();
                /*
                * the operation must be set before the instance is obtained from the pool, so
                * that the instance manager doesn't mistake this as a concurrent access.
                */
                callContext.setCurrentOperation(Operation.AFTER_COMPLETION);
                callContext.setCurrentAllowedStates(StatefulContext.getStates());

                StatefulInstanceManager.Instance instance = (StatefulInstanceManager.Instance) instanceManager.obtainInstance(callContext.getPrimaryKey(), callContext);

                Method afterCompletion = SessionSynchronization.class.getMethod("afterCompletion", boolean.class);

                List<InterceptorData> interceptors = callContext.getDeploymentInfo().getMethodInterceptors(afterCompletion);
                InterceptorStack interceptorStack = new InterceptorStack(instance.bean, afterCompletion, Operation.AFTER_COMPLETION, interceptors, instance.interceptors);
                interceptorStack.invoke(status == Status.STATUS_COMMITTED);

                instanceManager.poolInstance(callContext, instance);
            } catch (org.apache.openejb.InvalidateReferenceException inv) {

            } catch (Exception e) {

                String message = "An unexpected system exception occured while invoking the afterCompletion method on the SessionSynchronization object: " + e.getClass().getName() + " " + e.getMessage();

                /* [1] Log the exception or error */
                logger.error(message, e);

                /* [2] If the instance is in a transaction, mark the transaction for rollback. */
                Transaction tx = null;
                try {
                    tx = getTransactionManager().getTransaction();
                } catch (Throwable t) {
                    logger.error("Could not retreive the current transaction from the transaction manager while handling a callback exception from the afterCompletion method of bean " + callContext.getPrimaryKey());
                }
                try {
                    // TODO: DMB: This may not be spec compliant
                    markTxRollbackOnly(tx);
                } catch (Throwable t) {
                    logger.error("Could not mark the current transaction for rollback while handling a callback exception from the afterCompletion method of bean " + callContext.getPrimaryKey());
                }

                /* [3] Discard the instance */
                discardInstance(instanceManager, callContext);

                /* [4] throw the java.rmi.RemoteException to the client */

                throw new RuntimeException(message);
            } finally {
                ThreadContext.exit(oldCallContext);
            }
        }
    }

    protected void discardInstance(StatefulInstanceManager instanceManager, ThreadContext callContext) {
        try {
            instanceManager.freeInstance(callContext);
        } catch (org.apache.openejb.OpenEJBException oee) {

        }
    }

    protected void markTxRollbackOnly(Transaction tx) throws SystemException {
        try {
            if (tx != null) tx.setRollbackOnly();
        } catch (javax.transaction.SystemException se) {
            throw new org.apache.openejb.SystemException(se);
        }
    }

    protected TransactionManager getTransactionManager() {
        return transactionManager;
    }

    protected void throwExceptionToServer(Throwable sysException) throws ApplicationException {
        throw new ApplicationException(sysException);
    }

    protected int getTransactionStatus() {
        try {
            return transactionManager.getStatus();
        } catch (javax.transaction.SystemException e) {
            return Status.STATUS_NO_TRANSACTION;
        }
    }
}

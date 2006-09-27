package org.apache.openejb.core.stateful;

import org.apache.openejb.ApplicationException;
import org.apache.openejb.SystemException;
import org.apache.openejb.core.Operations;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.transaction.TransactionContext;
import org.apache.openejb.util.Logger;

import javax.ejb.EnterpriseBean;
import javax.ejb.SessionSynchronization;
import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import java.util.HashMap;

public class SessionSynchronizationCoordinator implements javax.transaction.Synchronization {

    private static java.util.HashMap coordinators = new java.util.HashMap();
    public static Logger logger = Logger.getInstance("OpenEJB", "org.apache.openejb.util.resources");

    private final HashMap sessionSynchronizations = new java.util.HashMap();
    private final TransactionManager transactionManager;

    private SessionSynchronizationCoordinator(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public static void registerSessionSynchronization(SessionSynchronization session, TransactionContext context) throws javax.transaction.SystemException, javax.transaction.RollbackException {
        SessionSynchronizationCoordinator coordinator = null;

        coordinator = (SessionSynchronizationCoordinator) coordinators.get(context.currentTx);

        if (coordinator == null) {
            coordinator = new SessionSynchronizationCoordinator(context.getTransactionManager());
            try {
                context.currentTx.registerSynchronization(coordinator);
            } catch (Exception e) {
                logger.error("Transaction.registerSynchronization failed.", e);
                return;
            }
            coordinators.put(context.currentTx, coordinator);
        }

        coordinator._registerSessionSynchronization(session, context.callContext);
    }

    private void _registerSessionSynchronization(SessionSynchronization session, ThreadContext callContext) {
        boolean registered = sessionSynchronizations.containsKey(callContext.getPrimaryKey());

        if (registered) return;

        try {
            callContext = (ThreadContext) callContext.clone();
        } catch (Exception e) {
        }
        sessionSynchronizations.put(callContext.getPrimaryKey(), callContext);

        byte currentOperation = callContext.getCurrentOperation();
        callContext.setCurrentOperation(Operations.OP_AFTER_BEGIN);
        try {

            session.afterBegin();

        } catch (Exception e) {
            String message = "An unexpected system exception occured while invoking the afterBegin method on the SessionSynchronization object: " + e.getClass().getName() + " " + e.getMessage();
            logger.error(message, e);
            throw new RuntimeException(message);

        } finally {
            callContext.setCurrentOperation(currentOperation);
        }
    }

    public void beforeCompletion() {

        ThreadContext originalContext = ThreadContext.getThreadContext();

        Object[] contexts = sessionSynchronizations.values().toArray();

        for (int i = 0; i < contexts.length; i++) {

            ThreadContext callContext = (ThreadContext) contexts[i];

            ThreadContext.setThreadContext(callContext);
            StatefulInstanceManager instanceManager = null;

            try {
                StatefulContainer container = (StatefulContainer) callContext.getDeploymentInfo().getContainer();
                instanceManager = container.getInstanceManager();
                /*
                * the operation must be set before the instance is obtained from the pool, so 
                * that the instance manager doesn't mistake this as a concurrent access.
                */
                callContext.setCurrentOperation(Operations.OP_BEFORE_COMPLETION);

                SessionSynchronization bean = (SessionSynchronization) instanceManager.obtainInstance(callContext.getPrimaryKey(), callContext);
                bean.beforeCompletion();
                instanceManager.poolInstance(callContext.getPrimaryKey(), (EnterpriseBean) bean);
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
                ThreadContext.setThreadContext(originalContext);
            }
        }
    }

    public void afterCompletion(int status) {

        ThreadContext originalContext = ThreadContext.getThreadContext();

        Object[] contexts = sessionSynchronizations.values().toArray();

        try {
            Transaction tx = getTransactionManager().getTransaction();
            coordinators.remove(tx);
        } catch (Exception e) {
            logger.error("", e);
        }
        for (int i = 0; i < contexts.length; i++) {

            ThreadContext callContext = (ThreadContext) contexts[i];

            ThreadContext.setThreadContext(callContext);
            StatefulInstanceManager instanceManager = null;

            try {
                StatefulContainer container = (StatefulContainer) callContext.getDeploymentInfo().getContainer();
                instanceManager = container.getInstanceManager();
                /*
                * the operation must be set before the instance is obtained from the pool, so 
                * that the instance manager doesn't mistake this as a concurrent access.
                */
                callContext.setCurrentOperation(Operations.OP_AFTER_COMPLETION);

                SessionSynchronization bean = (SessionSynchronization) instanceManager.obtainInstance(callContext.getPrimaryKey(), callContext);

                bean.afterCompletion(status == Status.STATUS_COMMITTED);
                instanceManager.poolInstance(callContext.getPrimaryKey(), (EnterpriseBean) bean);
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
                ThreadContext.setThreadContext(originalContext);
            }
        }
    }

    protected void discardInstance(StatefulInstanceManager instanceManager, ThreadContext callContext) {
        try {
            instanceManager.freeInstance(callContext.getPrimaryKey());
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
}

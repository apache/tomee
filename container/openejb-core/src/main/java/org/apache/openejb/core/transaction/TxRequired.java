package org.apache.openejb.core.transaction;

import javax.transaction.Status;

import org.apache.openejb.ApplicationException;

public class TxRequired extends TransactionPolicy {

    public TxRequired(TransactionContainer container) {
        this();
        this.container = container;
    }

    public TxRequired() {
        policyType = Required;
    }

    public String policyToString() {
        return "TX_Required: ";
    }

    public void beforeInvoke(Object instance, TransactionContext context) throws org.apache.openejb.SystemException, org.apache.openejb.ApplicationException {

        try {

            context.clientTx = context.getTransactionManager().getTransaction();

            if (context.clientTx == null) {
                beginTransaction(context);
            }

            context.currentTx = context.getTransactionManager().getTransaction();

        } catch (javax.transaction.SystemException se) {
            logger.error("Exception during getTransaction()", se);
            throw new org.apache.openejb.SystemException(se);
        }
    }

    public void afterInvoke(Object instance, TransactionContext context) throws org.apache.openejb.ApplicationException, org.apache.openejb.SystemException {

        try {
            if (context.clientTx != null) return;

            if (context.currentTx.getStatus() == Status.STATUS_ACTIVE) {
                commitTransaction(context, context.currentTx);
            } else {
                rollbackTransaction(context, context.currentTx);
            }

        } catch (javax.transaction.SystemException se) {
            logger.error("Exception during getTransaction()", se);
            throw new org.apache.openejb.SystemException(se);
        }
    }

    public void handleApplicationException(Throwable appException, TransactionContext context) throws ApplicationException {

        throw new ApplicationException(appException);
    }

    public void handleSystemException(Throwable sysException, Object instance, TransactionContext context) throws org.apache.openejb.ApplicationException, org.apache.openejb.SystemException {

        /* [1] Log the system exception or error **********/
        logSystemException(sysException);

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

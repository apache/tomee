package org.apache.openejb.core.transaction;

import javax.transaction.Status;

import org.apache.openejb.ApplicationException;

public class TxRequiresNew extends TransactionPolicy {

    public TxRequiresNew(TransactionContainer container) {
        this();
        this.container = container;
    }

    public TxRequiresNew() {
        policyType = RequiresNew;
    }

    public String policyToString() {
        return "TX_RequiresNew: ";
    }

    public void beforeInvoke(Object instance, TransactionContext context) throws org.apache.openejb.SystemException, org.apache.openejb.ApplicationException {

        try {

            context.clientTx = suspendTransaction(context);
            beginTransaction(context);
            context.currentTx = context.getTransactionManager().getTransaction();

        } catch (javax.transaction.SystemException se) {
            throw new org.apache.openejb.SystemException(se);
        }

    }

    public void afterInvoke(Object instance, TransactionContext context) throws org.apache.openejb.ApplicationException, org.apache.openejb.SystemException {

        try {

            if (context.currentTx.getStatus() == Status.STATUS_ACTIVE) {
                commitTransaction(context, context.currentTx);
            } else {
                rollbackTransaction(context, context.currentTx);
            }

        } catch (javax.transaction.SystemException se) {
            throw new org.apache.openejb.SystemException(se);
        } finally {
            if (context.clientTx != null) {
                resumeTransaction(context, context.clientTx);
            } else if (txLogger.isInfoEnabled()) {
                txLogger.info(policyToString() + "No transaction to resume");
            }
        }
    }

    public void handleApplicationException(Throwable appException, TransactionContext context) throws ApplicationException {
        throw new ApplicationException(appException);
    }

    public void handleSystemException(Throwable sysException, Object instance, TransactionContext context) throws org.apache.openejb.ApplicationException, org.apache.openejb.SystemException {

        /* [1] Log the system exception or error **********/
        logSystemException(sysException);

        /* [2] afterInvoke will roll back the tx */
        markTxRollbackOnly(context.currentTx);

        /* [3] Discard instance. **************************/
        discardBeanInstance(instance, context.callContext);

        /* [4] Throw RemoteException to client ************/
        throwExceptionToServer(sysException);

    }
}


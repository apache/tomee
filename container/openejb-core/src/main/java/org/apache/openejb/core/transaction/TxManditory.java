package org.apache.openejb.core.transaction;

import org.apache.openejb.ApplicationException;

public class TxManditory extends TransactionPolicy {

    public TxManditory(TransactionContainer container) {
        this();
        this.container = container;
    }

    public TxManditory() {
        policyType = Mandatory;
    }

    public String policyToString() {
        return "TX_Mandatory: ";
    }

    public void beforeInvoke(Object instance, TransactionContext context) throws org.apache.openejb.SystemException, org.apache.openejb.ApplicationException {

        try {

            context.clientTx = context.getTransactionManager().getTransaction();

            if (context.clientTx == null) {

                throw new ApplicationException(new javax.transaction.TransactionRequiredException());
            }

            context.currentTx = context.clientTx;

        } catch (javax.transaction.SystemException se) {
            logger.error("Exception during getTransaction()", se);
            throw new org.apache.openejb.SystemException(se);
        }
    }

    public void afterInvoke(Object instance, TransactionContext context) throws org.apache.openejb.ApplicationException, org.apache.openejb.SystemException {

    }

    public void handleApplicationException(Throwable appException, TransactionContext context) throws ApplicationException {

        throw new ApplicationException(appException);
    }

    public void handleSystemException(Throwable sysException, Object instance, TransactionContext context) throws org.apache.openejb.ApplicationException, org.apache.openejb.SystemException {

        /* [1] Log the system exception or error *********/
        logSystemException(sysException);

        /* [2] Mark the transaction for rollback. ********/
        markTxRollbackOnly(context.currentTx);

        /* [3] Discard instance. *************************/
        discardBeanInstance(instance, context.callContext);

        /* [4] TransactionRolledbackException to client **/
        throwTxExceptionToServer(sysException);
    }

}


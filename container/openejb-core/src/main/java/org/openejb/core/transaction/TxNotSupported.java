package org.openejb.core.transaction;

import org.openejb.ApplicationException;

public class TxNotSupported extends TransactionPolicy {

    public TxNotSupported(TransactionContainer container) {
        this();
        this.container = container;
    }

    public TxNotSupported() {
        policyType = NotSupported;
    }

    public String policyToString() {
        return "TX_NotSupported: ";
    }

    public void beforeInvoke(Object instance, TransactionContext context) throws org.openejb.SystemException, org.openejb.ApplicationException {

        try {

            context.clientTx = context.getTransactionManager().suspend();
        } catch (javax.transaction.SystemException se) {
            throw new org.openejb.SystemException(se);
        }
        context.currentTx = null;

    }

    public void afterInvoke(Object instance, TransactionContext context) throws org.openejb.ApplicationException, org.openejb.SystemException {

        if (context.clientTx != null) {
            try {
                context.getTransactionManager().resume(context.clientTx);
            } catch (javax.transaction.InvalidTransactionException ite) {

                logger.error("Could not resume the client's transaction, the transaction is no longer valid: " + ite.getMessage());
            } catch (IllegalStateException e) {

                logger.error("Could not resume the client's transaction: " + e.getMessage());
            } catch (javax.transaction.SystemException e) {

                logger.error("Could not resume the client's transaction: The transaction reported a system exception: " + e.getMessage());
            }
        }
    }

    public void handleApplicationException(Throwable appException, TransactionContext context) throws ApplicationException {

        throw new ApplicationException(appException);
    }

    public void handleSystemException(Throwable sysException, Object instance, TransactionContext context) throws org.openejb.ApplicationException, org.openejb.SystemException {
        /* [1] Log the system exception or error *********/
        logSystemException(sysException);

        /* [2] Discard instance. *************************/
        discardBeanInstance(instance, context.callContext);

        /* [3] Throw RemoteException to client ***********/
        throwExceptionToServer(sysException);
    }

}


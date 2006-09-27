package org.apache.openejb.core.transaction;

import org.apache.openejb.ApplicationException;

public class TxNever extends TransactionPolicy {

    public TxNever(TransactionContainer container) {
        this();
        this.container = container;
    }

    public TxNever() {
        policyType = Never;
    }

    public String policyToString() {
        return "TX_Never: ";
    }

    public void beforeInvoke(Object instance, TransactionContext context) throws org.apache.openejb.SystemException, org.apache.openejb.ApplicationException {

        try {

            if (context.getTransactionManager().getTransaction() != null) {

                throw new ApplicationException(new java.rmi.RemoteException("Transactions not supported"));
            }

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

        /* [2] Discard instance. *************************/
        discardBeanInstance(instance, context.callContext);

        /* [3] Throw RemoteException to client ***********/
        throwExceptionToServer(sysException);
    }

}


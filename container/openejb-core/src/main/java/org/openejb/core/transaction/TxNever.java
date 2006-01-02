package org.openejb.core.transaction;

import javax.ejb.EnterpriseBean;

import org.openejb.ApplicationException;

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

    public void beforeInvoke(EnterpriseBean instance, TransactionContext context) throws org.openejb.SystemException, org.openejb.ApplicationException {

        try {

            if (getTxMngr().getTransaction() != null) {

                throw new ApplicationException(new java.rmi.RemoteException("Transactions not supported"));
            }

        } catch (javax.transaction.SystemException se) {
            logger.error("Exception during getTransaction()", se);
            throw new org.openejb.SystemException(se);
        }
    }

    public void afterInvoke(EnterpriseBean instance, TransactionContext context) throws org.openejb.ApplicationException, org.openejb.SystemException {

    }

    public void handleApplicationException(Throwable appException, TransactionContext context) throws ApplicationException {

        throw new ApplicationException(appException);
    }

    public void handleSystemException(Throwable sysException, EnterpriseBean instance, TransactionContext context) throws org.openejb.ApplicationException, org.openejb.SystemException {
        /* [1] Log the system exception or error *********/
        logSystemException(sysException);

        /* [2] Discard instance. *************************/
        discardBeanInstance(instance, context.callContext);

        /* [3] Throw RemoteException to client ***********/
        throwExceptionToServer(sysException);
    }

}


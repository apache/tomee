package org.openejb.core.transaction;

import javax.ejb.EnterpriseBean;

import org.openejb.ApplicationException;

public class TxSupports extends TransactionPolicy {

    public TxSupports(TransactionContainer container){
        this();
        this.container = container;
    }

    public TxSupports(){
        policyType = Supports;
    }

    public String policyToString() {
        return "TX_Supports: ";
    }

    public void beforeInvoke(EnterpriseBean instance, TransactionContext context) throws org.openejb.SystemException, org.openejb.ApplicationException{

        try {

            context.clientTx  = getTxMngr().getTransaction();
            context.currentTx = context.clientTx;

        } catch ( javax.transaction.SystemException se ) {
            throw new org.openejb.SystemException(se);
        }
    }

    public void afterInvoke(EnterpriseBean instance, TransactionContext context) throws org.openejb.ApplicationException, org.openejb.SystemException{

    }

    public void handleApplicationException( Throwable appException, TransactionContext context) throws ApplicationException{

        throw new ApplicationException( appException );
    }

    public void handleSystemException( Throwable sysException, EnterpriseBean instance, TransactionContext context) throws org.openejb.ApplicationException, org.openejb.SystemException{

        boolean runningInTransaction = ( context.currentTx != null );

        if (runningInTransaction) {
            /* [1] Log the system exception or error *********/
            logSystemException( sysException );

            /* [2] Mark the transaction for rollback. ********/
            markTxRollbackOnly( context.currentTx );

            /* [3] Discard instance. *************************/
            discardBeanInstance(instance, context.callContext);

            /* [4] TransactionRolledbackException to client **/
            throwTxExceptionToServer( sysException );

        } else {
            /* [1] Log the system exception or error *********/
            logSystemException( sysException );

            /* [2] Discard instance. *************************/
            discardBeanInstance( instance, context.callContext);

            /* [3] Throw RemoteException to client ***********/
            throwExceptionToServer( sysException );
        }

    }

}


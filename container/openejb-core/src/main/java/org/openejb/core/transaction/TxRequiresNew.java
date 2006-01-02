package org.openejb.core.transaction;

import javax.ejb.EnterpriseBean;
import javax.transaction.Status;

import org.openejb.ApplicationException;

public class TxRequiresNew extends TransactionPolicy {

    public TxRequiresNew(TransactionContainer container){
        this();
        this.container = container;
    }

    public TxRequiresNew(){
        policyType = RequiresNew;
    }

    public String policyToString() {
        return "TX_RequiresNew: ";
    }

    public void beforeInvoke(EnterpriseBean instance, TransactionContext context) throws org.openejb.SystemException, org.openejb.ApplicationException{

        try {

            context.clientTx  = suspendTransaction();
            beginTransaction();
            context.currentTx = getTxMngr().getTransaction();

        } catch ( javax.transaction.SystemException se ) {
            throw new org.openejb.SystemException(se);
        }

    }

    public void afterInvoke(EnterpriseBean instance, TransactionContext context) throws org.openejb.ApplicationException, org.openejb.SystemException{

        try {

            if ( context.currentTx.getStatus() == Status.STATUS_ACTIVE ) {
                commitTransaction( context.currentTx );
            } else {
                rollbackTransaction( context.currentTx );
            }

        } catch ( javax.transaction.SystemException se ) {
            throw new org.openejb.SystemException(se);
        } finally {
            if ( context.clientTx != null ) {
                resumeTransaction( context.clientTx );
            } else if(txLogger.isInfoEnabled()) {
                txLogger.info(policyToString()+"No transaction to resume");
            }            
        }
    }

    public void handleApplicationException( Throwable appException, TransactionContext context) throws ApplicationException{
        throw new ApplicationException( appException );
    }

    public void handleSystemException( Throwable sysException, EnterpriseBean instance, TransactionContext context) throws org.openejb.ApplicationException, org.openejb.SystemException{

        /* [1] Log the system exception or error **********/
        logSystemException( sysException );

        /* [2] afterInvoke will roll back the tx */
        markTxRollbackOnly( context.currentTx );

        /* [3] Discard instance. **************************/
        discardBeanInstance( instance, context.callContext);

        /* [4] Throw RemoteException to client ************/
        throwExceptionToServer( sysException );

    }
}


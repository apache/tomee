package org.openejb.core.stateful;

import java.rmi.RemoteException;

import javax.ejb.EnterpriseBean;
import javax.transaction.Status;
import javax.transaction.Transaction;

import org.openejb.ApplicationException;
import org.openejb.InvalidateReferenceException;
import org.openejb.core.transaction.TransactionContainer;
import org.openejb.core.transaction.TransactionContext;
import org.openejb.core.transaction.TransactionPolicy;

public class StatefulBeanManagedTxPolicy extends TransactionPolicy {

    protected StatefulContainer statefulContainer;

    public StatefulBeanManagedTxPolicy(TransactionContainer container){
        this();
        if(container instanceof org.openejb.Container &&
           ((org.openejb.Container)container).getContainerType()!=org.openejb.Container.STATEFUL) {
            throw new IllegalArgumentException();
        }
        this.container = container;
        this.statefulContainer = (StatefulContainer)container;

    }

    public StatefulBeanManagedTxPolicy(){
        policyType = BeanManaged;
    }

    public String policyToString() {
        return "TX_BeanManaged: ";
    }
    public void beforeInvoke(EnterpriseBean instance, TransactionContext context) throws org.openejb.SystemException, org.openejb.ApplicationException{
        try {

            context.clientTx = suspendTransaction();

            Object primaryKey = context.callContext.getPrimaryKey();
            Object possibleBeanTx = statefulContainer.getInstanceManager().getAncillaryState( primaryKey );
            if ( possibleBeanTx instanceof Transaction ) {
                context.currentTx =  (Transaction)possibleBeanTx;
                resumeTransaction( context.currentTx );
            }
        } catch ( org.openejb.OpenEJBException e ) {
            handleSystemException( e.getRootCause(), instance, context );
        }
    }

    public void afterInvoke(EnterpriseBean instance, TransactionContext context) throws org.openejb.ApplicationException, org.openejb.SystemException{
        try {

            context.currentTx = getTxMngr().getTransaction();

            /*

            */
            if ( context.currentTx != null &&
                 context.currentTx.getStatus() != Status.STATUS_COMMITTED && 
                 context.currentTx.getStatus() != Status.STATUS_ROLLEDBACK ) {

                suspendTransaction();
            }

            Object primaryKey = context.callContext.getPrimaryKey();
            statefulContainer.getInstanceManager().setAncillaryState( primaryKey, context.currentTx );

        } catch ( org.openejb.OpenEJBException e ) {
            handleSystemException( e.getRootCause(), instance, context );
        } catch ( javax.transaction.SystemException e ) {
            handleSystemException( e, instance, context );
        } catch ( Throwable e ){
            handleSystemException( e, instance, context );
        } finally {
            resumeTransaction( context.clientTx );
        }
    }

    public void handleApplicationException( Throwable appException, TransactionContext context) throws ApplicationException{

        throw new ApplicationException( appException );
    }

    public void handleSystemException( Throwable sysException, EnterpriseBean instance, TransactionContext context) throws org.openejb.ApplicationException, org.openejb.SystemException{

        logSystemException( sysException );

        if ( context.currentTx != null ) markTxRollbackOnly( context.currentTx );

        discardBeanInstance( instance, context.callContext);

        throwExceptionToServer( sysException );

    }

    protected void throwExceptionToServer(Throwable sysException) throws ApplicationException{

        RemoteException re = new RemoteException("The bean encountered a non-application exception.", sysException);

        throw new InvalidateReferenceException( re );

    }

    protected void throwTxExceptionToServer( Throwable sysException ) throws ApplicationException{
        /* Throw javax.transaction.TransactionRolledbackException to remote client */

        String message = "The transaction was rolled back because the bean encountered a non-application exception :" + sysException.getClass().getName() + " : "+sysException.getMessage();
        javax.transaction.TransactionRolledbackException txException = new javax.transaction.TransactionRolledbackException(message);

        throw new InvalidateReferenceException( txException );

    }
}


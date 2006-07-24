package org.openejb.core.stateless;

import org.openejb.ApplicationException;
import org.openejb.core.transaction.TransactionContainer;
import org.openejb.core.transaction.TransactionContext;
import org.openejb.core.transaction.TransactionPolicy;

import javax.ejb.EnterpriseBean;
import javax.transaction.Status;
import java.rmi.RemoteException;

public class StatelessBeanManagedTxPolicy extends TransactionPolicy {

    public StatelessBeanManagedTxPolicy(TransactionContainer container) {
        this();
        if (container instanceof org.openejb.Container &&
                ((org.openejb.Container) container).getContainerType() != org.openejb.Container.STATELESS) {
            throw new IllegalArgumentException();
        }

        this.container = container;
    }

    public StatelessBeanManagedTxPolicy() {
        policyType = BeanManaged;
    }

    public String policyToString() {
        return "TX_BeanManaged: ";
    }

    public void beforeInvoke(EnterpriseBean instance, TransactionContext context) throws org.openejb.SystemException, org.openejb.ApplicationException {

        context.clientTx = suspendTransaction(context);
    }

    public void afterInvoke(EnterpriseBean instance, TransactionContext context) throws org.openejb.ApplicationException, org.openejb.SystemException {
        try {
            /*
            * The Container must detect the case in which a transaction was started, but
            * not completed, in the business method, and handle it as follows:
            */
            context.currentTx = context.getTransactionManager().getTransaction();

            if (context.currentTx == null) return;

            if (context.currentTx.getStatus() != Status.STATUS_ROLLEDBACK && context.currentTx.getStatus() != Status.STATUS_COMMITTED)
            {
                String message = "The stateless session bean started a transaction but did not complete it.";

                /* [1] Log this as an application error ********/
                logger.error(message);

                /* [2] Roll back the started transaction *******/
                try {
                    rollbackTransaction(context, context.currentTx);
                } catch (Throwable t) {

                }

                /* [3] Throw the RemoteException to the client */
                throwAppExceptionToServer(new RemoteException(message));
            }

        } catch (javax.transaction.SystemException e) {
            throw new org.openejb.SystemException(e);
        } finally {
            resumeTransaction(context, context.clientTx);
        }
    }

    public void handleApplicationException(Throwable appException, TransactionContext context) throws ApplicationException {

        throw new ApplicationException(appException);
    }

    public void handleSystemException(Throwable sysException, EnterpriseBean instance, TransactionContext context) throws org.openejb.ApplicationException, org.openejb.SystemException {
        try {
            context.currentTx = context.getTransactionManager().getTransaction();
        } catch (javax.transaction.SystemException e) {
            context.currentTx = null;
        }

        logSystemException(sysException);

        if (context.currentTx != null) markTxRollbackOnly(context.currentTx);

        discardBeanInstance(instance, context.callContext);

        throwExceptionToServer(sysException);

    }

}


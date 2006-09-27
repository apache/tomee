package org.apache.openejb.alt.containers.castor_cmp11;

import java.rmi.RemoteException;

import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.JDOManager;
import org.apache.openejb.ApplicationException;
import org.apache.openejb.core.transaction.TransactionContext;
import org.apache.openejb.core.transaction.TransactionPolicy;
import org.apache.openejb.core.transaction.TransactionContainer;
import org.apache.openejb.core.RpcContainerWrapper;

public class CastorCmpEntityTxPolicy extends org.apache.openejb.core.transaction.TransactionPolicy {

    protected TransactionPolicy policy;
    protected CastorCMP11_EntityContainer cmpContainer;

    protected JDOManager jdo_ForLocalTransaction = null;

    public CastorCmpEntityTxPolicy(TransactionPolicy policy) {
        this.policy = policy;
        this.container = policy.getContainer();
        this.policyType = policy.policyType;

        this.cmpContainer = getCastorContainer(container);

        this.jdo_ForLocalTransaction = cmpContainer.getLocalTxJDO();
    }

    private CastorCMP11_EntityContainer getCastorContainer(TransactionContainer container) {
        if (container instanceof RpcContainerWrapper) {
            RpcContainerWrapper wrapper = (RpcContainerWrapper) container;
            return getCastorContainer((TransactionContainer) wrapper.getContainer());
        } else {
            return (CastorCMP11_EntityContainer) container;
        }
    }

    public void beforeInvoke(Object instance, TransactionContext context) throws org.apache.openejb.SystemException, org.apache.openejb.ApplicationException {
        policy.beforeInvoke(instance, context);

//        DeploymentInfo deploymentInfo = context.callContext.getDeploymentInfo();
//        ClassLoader classLoader = deploymentInfo.getBeanClass().getClassLoader();

        Database db = null;
        try {
            if (context.currentTx == null) {
                /*
                * No current transaciton means that a local transaciton is required which 
                * must be executed on Database object aquired from a JDO object that was not
                * initated with a transaction manager name.
                */
                db = jdo_ForLocalTransaction.getDatabase();

                /*
                * The fact that there is no transaction following the processing of the wrapped
                * TransactionPolicy's beforeInvoke( ) method indicates that the request must 
                * execute in a Castor local transaction.  To get that local transacion started 
                * the begin() method is invoked. The local transaction will be committed by the
                * afterInoke() method of this class or rolled back by the handleSystemException() 
                * or handleApplicationException() methods.
                */
                db.begin();

                /* 
                * Places a non-transaction managed database object into the unspecified field 
                * of the current transaction context. This will be used later by the 
                * getDatabase( ) method of this class to provide the correct database object. 
                * Its also used by the afterInovoke() method to commit the local transaction 
                * and the handleSystemException() and handleApplicationException method to 
                * rollback the Castor's local transaction.
                */
                context.callContext.setUnspecified(db);
            } else {
                /*
                * If there is a transaction, that means that context is transaction-managed so
                * we make the unspecified field of the current ThreadContext null, which will 
                * be used by the getDatabase() method of this class to determine that a 
                * transaction-managed database object is needed.
                */
                context.callContext.setUnspecified(null);
            }
        } catch (org.exolab.castor.jdo.DatabaseNotFoundException e) {
            RemoteException re = new RemoteException("Castor JDO DatabaseNotFoundException thrown when attempting to begin a local transaciton", e);
            handleSystemException(re, instance, context);

        } catch (org.exolab.castor.jdo.PersistenceException e) {
            RemoteException re = new RemoteException("Castor JDO PersistenceException thrown when attempting to begin local transaciton", e);
            handleSystemException(re, instance, context);

        } catch (Throwable e) {
            RemoteException re = new RemoteException("Encountered and unkown error in Castor JDO when attempting to begin local transaciton", e);
            handleSystemException(re, instance, context);
        }
    }

    public void afterInvoke(Object instance, TransactionContext context) throws org.apache.openejb.ApplicationException, org.apache.openejb.SystemException {
        try {
            if (context.currentTx == null) {
                Database db = (Database) context.callContext.getUnspecified();
                if (db != null && db.isActive()) {
                    db.commit();
                }
            }
        } catch (org.exolab.castor.jdo.TransactionAbortedException e) {
            RemoteException ex = new RemoteException("Castor JDO threw a JDO TransactionAbortedException while attempting to commit a local transaciton", e);
            policy.handleApplicationException(ex, context);
        } catch (org.exolab.castor.jdo.TransactionNotInProgressException e) {
            RemoteException ex = new RemoteException("Transaction managment problem with Castor JDO, a transaction should be in progress, but this is not the case.", e);
            policy.handleSystemException(ex, instance, context);
        } catch (Throwable e) {
            RemoteException ex = new RemoteException("Encountered and unknown exception while attempting to commit the local castor database transaction", e);
            policy.handleSystemException(ex, instance, context);
        } finally {
            policy.afterInvoke(instance, context);
        }
    }

    public void handleApplicationException(Throwable appException, TransactionContext context) throws ApplicationException {
        try {
            if (context.currentTx == null) {
                Database db = (Database) context.callContext.getUnspecified();
                db.rollback();
            }
        } catch (org.exolab.castor.jdo.TransactionNotInProgressException tnipe) {

        } finally {
            policy.handleApplicationException(appException, context);
        }
    }

    public void handleSystemException(Throwable sysException, Object instance, TransactionContext context) throws org.apache.openejb.ApplicationException, org.apache.openejb.SystemException {
        try {
            if (context.currentTx == null) {
                Database db = (Database) context.callContext.getUnspecified();
                db.rollback();
            }
        } catch (org.exolab.castor.jdo.TransactionNotInProgressException tnipe) {

        } finally {
            policy.handleSystemException(sysException, instance, context);
        }
    }

}


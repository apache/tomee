package org.apache.openejb.core.transaction;

import java.rmi.RemoteException;

import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.apache.openejb.ApplicationException;
import org.apache.openejb.InvalidateReferenceException;
import org.apache.openejb.SystemException;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.util.Logger;

public abstract class TransactionPolicy {

    public static final int Mandatory = 0;
    public static final int Never = 1;
    public static final int NotSupported = 2;
    public static final int Required = 3;
    public static final int RequiresNew = 4;
    public static final int Supports = 5;
    public static final int BeanManaged = 6;

    public int policyType;
    private TransactionManager manager;
    protected TransactionContainer container;

    protected final static Logger logger = Logger.getInstance("OpenEJB", "org.apache.openejb.util.resources");
    protected final static Logger txLogger = Logger.getInstance("Transaction", "org.apache.openejb.util.resources");

    public TransactionContainer getContainer() {
        return container;
    }

    public String policyToString() {
        return "Internal Error: no such policy";
    }

    public abstract void handleApplicationException(Throwable appException, TransactionContext context) throws org.apache.openejb.ApplicationException;

    public abstract void handleSystemException(Throwable sysException, Object instance, TransactionContext context) throws org.apache.openejb.ApplicationException, org.apache.openejb.SystemException;

    public abstract void beforeInvoke(Object bean, TransactionContext context) throws org.apache.openejb.SystemException, org.apache.openejb.ApplicationException;

    public abstract void afterInvoke(Object bean, TransactionContext context) throws org.apache.openejb.ApplicationException, org.apache.openejb.SystemException;

    protected void markTxRollbackOnly(Transaction tx) throws SystemException {
        try {
            if (tx != null) {
                tx.setRollbackOnly();
                if (txLogger.isInfoEnabled()) {
                    txLogger.info(policyToString() + "setRollbackOnly() on transaction " + tx);
                }
            }
        } catch (javax.transaction.SystemException se) {
            logger.error("Exception during setRollbackOnly()", se);
            throw new org.apache.openejb.SystemException(se);
        }
    }

    protected Transaction suspendTransaction(TransactionContext context) throws SystemException {
        try {
            Transaction tx = context.getTransactionManager().suspend();
            if (txLogger.isInfoEnabled()) {
                txLogger.info(policyToString() + "Suspended transaction " + tx);
            }
            return tx;
        } catch (javax.transaction.SystemException se) {
            logger.error("Exception during suspend()", se);
            throw new org.apache.openejb.SystemException(se);
        }
    }

    protected void resumeTransaction(TransactionContext context, Transaction tx) throws SystemException {
        try {
            if (tx == null) {
                if (txLogger.isInfoEnabled()) {
                    txLogger.info(policyToString() + "No transaction to resume");
                }
            } else {
                if (txLogger.isInfoEnabled()) {
                    txLogger.info(policyToString() + "Resuming transaction " + tx);
                }
                context.getTransactionManager().resume(tx);
            }
        } catch (javax.transaction.InvalidTransactionException ite) {

            txLogger.error("Could not resume the client's transaction, the transaction is no longer valid: " + ite.getMessage());
            throw new org.apache.openejb.SystemException(ite);
        } catch (IllegalStateException e) {

            txLogger.error("Could not resume the client's transaction: " + e.getMessage());
            throw new org.apache.openejb.SystemException(e);
        } catch (javax.transaction.SystemException e) {

            txLogger.error("Could not resume the client's transaction: The transaction reported a system exception: " + e.getMessage());
            throw new org.apache.openejb.SystemException(e);
        }
    }

    protected void commitTransaction(TransactionContext context, Transaction tx) throws SystemException {
        try {
            if (txLogger.isInfoEnabled()) {
                txLogger.info(policyToString() + "Committing transaction " + tx);
            }
            if (tx.equals(context.getTransactionManager().getTransaction())) {

                context.getTransactionManager().commit();
            } else {
                tx.commit();
            }
        } catch (javax.transaction.RollbackException e) {

            txLogger.info("The transaction has been rolled back rather than commited: " + e.getMessage());

        } catch (javax.transaction.HeuristicMixedException e) {

            txLogger.info("A heuristic decision was made, some relevant updates have been committed while others have been rolled back: " + e.getMessage());

        } catch (javax.transaction.HeuristicRollbackException e) {

            txLogger.info("A heuristic decision was made while commiting the transaction, some relevant updates have been rolled back: " + e.getMessage());

        } catch (SecurityException e) {

            txLogger.error("The current thread is not allowed to commit the transaction: " + e.getMessage());
            throw new org.apache.openejb.SystemException(e);

        } catch (IllegalStateException e) {

            txLogger.error("The current thread is not associated with a transaction: " + e.getMessage());
            throw new org.apache.openejb.SystemException(e);

        } catch (javax.transaction.SystemException e) {
            txLogger.error("The Transaction Manager has encountered an unexpected error condition while attempting to commit the transaction: " + e.getMessage());

            throw new org.apache.openejb.SystemException(e);
        }
    }

    protected void rollbackTransaction(TransactionContext context, Transaction tx) throws SystemException {
        try {
            if (txLogger.isInfoEnabled()) {
                txLogger.info(policyToString() + "Rolling back transaction " + tx);
            }
            if (tx.equals(context.getTransactionManager().getTransaction())) {

                context.getTransactionManager().rollback();
            } else {
                tx.rollback();
            }
        } catch (IllegalStateException e) {

            logger.error("The TransactionManager reported an exception while attempting to rollback the transaction: " + e.getMessage());
            throw new org.apache.openejb.SystemException(e);

        } catch (javax.transaction.SystemException e) {

            logger.error("The TransactionManager reported an exception while attempting to rollback the transaction: " + e.getMessage());
            throw new org.apache.openejb.SystemException(e);
        }
    }

    protected void throwAppExceptionToServer(Throwable appException) throws ApplicationException {
        throw new ApplicationException(appException);
    }

    protected void throwTxExceptionToServer(Throwable sysException) throws ApplicationException {
        /* Throw javax.transaction.TransactionRolledbackException to remote client */

        String message = "The transaction was rolled back because the bean encountered a non-application exception :" + sysException.getClass().getName() + " : " + sysException.getMessage();
        javax.transaction.TransactionRolledbackException txException = new javax.transaction.TransactionRolledbackException(message);

        throw new InvalidateReferenceException(txException);

    }

    protected void throwExceptionToServer(Throwable sysException) throws ApplicationException {

        RemoteException re = new RemoteException("The bean encountered a non-application exception.", sysException);

        throw new InvalidateReferenceException(re);

    }

    protected void logSystemException(Throwable sysException) {

        logger.error("The bean instances business method encountered a system exception:" + sysException.getMessage(), sysException);
    }

    protected void discardBeanInstance(Object instance, ThreadContext callContext) {
        container.discardInstance(instance, callContext);
    }

    protected void beginTransaction(TransactionContext context) throws javax.transaction.SystemException {
        try {
            context.getTransactionManager().begin();
            if (txLogger.isInfoEnabled()) {
                txLogger.info(policyToString() + "Started transaction " + context.getTransactionManager().getTransaction());
            }
        } catch (javax.transaction.NotSupportedException nse) {
            logger.error("", nse);
        }
    }

    protected void handleCallbackException() {
    }
}


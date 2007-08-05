/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.core.transaction;

import org.apache.openejb.ApplicationException;
import org.apache.openejb.InvalidateReferenceException;
import org.apache.openejb.SystemException;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.InvalidTransactionException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.NotSupportedException;
import java.rmi.RemoteException;

public abstract class TransactionPolicy {
    public Type getPolicyType() {
        return policyType;
    }

    public static enum Type {
        Mandatory,
        Never,
        NotSupported,
        Required,
        RequiresNew,
        Supports,
        BeanManaged;
    }


    private final Type policyType;
    protected final TransactionContainer container;
    private TransactionManager manager;

    protected final static Logger logger = Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources");
    protected final static Logger txLogger = Logger.getInstance(LogCategory.TRANSACTION, "org.apache.openejb.util.resources");

    public TransactionPolicy(Type policyType, TransactionContainer container) {
        this.policyType = policyType;
        this.container = container;
    }

    public TransactionContainer getContainer() {
        return container;
    }

    public String policyToString() {
        return policyType.toString();
    }

    public abstract void handleApplicationException(Throwable appException, boolean rollback, TransactionContext context) throws ApplicationException, SystemException;

    public abstract void handleSystemException(Throwable sysException, Object instance, TransactionContext context) throws ApplicationException, SystemException;

    public abstract void beforeInvoke(Object bean, TransactionContext context) throws SystemException, ApplicationException;

    public abstract void afterInvoke(Object bean, TransactionContext context) throws ApplicationException, SystemException;

    protected void markTxRollbackOnly(Transaction tx) throws SystemException {
        try {
            if (tx != null) {
                tx.setRollbackOnly();
                if (txLogger.isInfoEnabled()) {
                    txLogger.info("TX " + policyToString() + ": setRollbackOnly() on transaction " + tx);
                }
            }
        } catch (javax.transaction.SystemException se) {
            logger.error("Exception during setRollbackOnly()", se);
            throw new SystemException(se);
        }
    }

    protected Transaction suspendTransaction(TransactionContext context) throws SystemException {
        try {
            Transaction tx = context.getTransactionManager().suspend();
            if (txLogger.isInfoEnabled()) {
                txLogger.info("TX " + policyToString() + ": Suspended transaction " + tx);
            }
            return tx;
        } catch (javax.transaction.SystemException se) {
            logger.error("Exception during suspend()", se);
            throw new SystemException(se);
        }
    }

    protected void resumeTransaction(TransactionContext context, Transaction tx) throws SystemException {
        try {
            if (tx == null) {
                if (txLogger.isInfoEnabled()) {
                    txLogger.info("TX " + policyToString() + ": No transaction to resume");
                }
            } else {
                if (txLogger.isInfoEnabled()) {
                    txLogger.info("TX " + policyToString() + ": Resuming transaction " + tx);
                }
                context.getTransactionManager().resume(tx);
            }
        } catch (InvalidTransactionException ite) {

            txLogger.error("Could not resume the client's transaction, the transaction is no longer valid: " + ite.getMessage());
            throw new SystemException(ite);
        } catch (IllegalStateException e) {

            txLogger.error("Could not resume the client's transaction: " + e.getMessage());
            throw new SystemException(e);
        } catch (javax.transaction.SystemException e) {

            txLogger.error("Could not resume the client's transaction: The transaction reported a system exception: " + e.getMessage());
            throw new SystemException(e);
        }
    }

    protected void commitTransaction(TransactionContext context, Transaction tx) throws SystemException {
        try {
            if (txLogger.isInfoEnabled()) {
                txLogger.info("TX " + policyToString() + ": Committing transaction " + tx);
            }
            if (tx.equals(context.getTransactionManager().getTransaction())) {

                context.getTransactionManager().commit();
            } else {
                tx.commit();
            }
        } catch (RollbackException e) {

            txLogger.info("The transaction has been rolled back rather than commited: " + e.getMessage());

        } catch (HeuristicMixedException e) {

            txLogger.info("A heuristic decision was made, some relevant updates have been committed while others have been rolled back: " + e.getMessage());

        } catch (HeuristicRollbackException e) {

            txLogger.info("A heuristic decision was made while commiting the transaction, some relevant updates have been rolled back: " + e.getMessage());

        } catch (SecurityException e) {

            txLogger.error("The current thread is not allowed to commit the transaction: " + e.getMessage());
            throw new SystemException(e);

        } catch (IllegalStateException e) {

            txLogger.error("The current thread is not associated with a transaction: " + e.getMessage());
            throw new SystemException(e);

        } catch (javax.transaction.SystemException e) {
            txLogger.error("The Transaction Manager has encountered an unexpected error condition while attempting to commit the transaction: " + e.getMessage());

            throw new SystemException(e);
        }
    }

    protected void rollbackTransaction(TransactionContext context, Transaction tx) throws SystemException {
        try {
            if (txLogger.isInfoEnabled()) {
                txLogger.info("TX " + policyToString() + ": Rolling back transaction " + tx);
            }
            if (tx.equals(context.getTransactionManager().getTransaction())) {

                context.getTransactionManager().rollback();
            } else {
                tx.rollback();
            }
        } catch (IllegalStateException e) {

            logger.error("The TransactionManager reported an exception while attempting to rollback the transaction: " + e.getMessage());
            throw new SystemException(e);

        } catch (javax.transaction.SystemException e) {

            logger.error("The TransactionManager reported an exception while attempting to rollback the transaction: " + e.getMessage());
            throw new SystemException(e);
        }
    }

    protected void throwAppExceptionToServer(Throwable appException) throws ApplicationException {
        throw new ApplicationException(appException);
    }

    protected void throwTxExceptionToServer(Throwable sysException) throws ApplicationException {
        /* Throw javax.transaction.TransactionRolledbackException to remote client */

        String message = "The transaction has been marked rollback only because the bean encountered a non-application exception :" + sysException.getClass().getName() + " : " + sysException.getMessage();
        TransactionRolledbackException txException = new TransactionRolledbackException(message, sysException);

        throw new InvalidateReferenceException(txException);

    }

    protected void throwExceptionToServer(Throwable sysException) throws ApplicationException {

        RemoteException re = new RemoteException("The bean encountered a non-application exception.", sysException);

        throw new InvalidateReferenceException(re);

    }

    protected void logSystemException(Throwable sysException) {

        logger.error("The bean instances business method encountered a system exception: " + sysException.getMessage(), sysException);
    }

    protected void discardBeanInstance(Object instance, ThreadContext callContext) {
        container.discardInstance(instance, callContext);
    }

    protected void beginTransaction(TransactionContext context) throws javax.transaction.SystemException {
        try {
            context.getTransactionManager().begin();
            if (txLogger.isInfoEnabled()) {
                txLogger.info("TX " + policyToString() + ": Started transaction " + context.getTransactionManager().getTransaction());
            }
        } catch (NotSupportedException nse) {
            logger.error("", nse);
        }
    }

    protected void handleCallbackException() {
    }
}


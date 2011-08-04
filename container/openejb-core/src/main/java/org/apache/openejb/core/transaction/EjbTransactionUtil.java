/*
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

import java.rmi.RemoteException;

import org.apache.openejb.ApplicationException;
import org.apache.openejb.BeanContext;
import org.apache.openejb.InvalidateReferenceException;
import org.apache.openejb.SystemException;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.ThreadContextListener;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

public final class EjbTransactionUtil {
    private final static Logger logger = Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources");

    static {
        ThreadContext.addThreadContextListener(new ThreadContextListener() {
            public void contextEntered(ThreadContext oldContext, ThreadContext newContext) {
                // propagate current tx environment to the new ThreadContext
                if (oldContext != null) {
                    newContext.setTransactionPolicy(oldContext.getTransactionPolicy());
                }
            }

            public void contextExited(ThreadContext exitedContext, ThreadContext reenteredContext) {
            }
        });
    }

    /**
     * Creates a new TransctionPolicy of the specified type and associates it with the specified ThreadContext.
     */
    public static TransactionPolicy createTransactionPolicy(TransactionType type, ThreadContext threadContext) throws SystemException, ApplicationException {
        // start the new transaction policy
        BeanContext beanContext = threadContext.getBeanContext();
        TransactionPolicy txPolicy = beanContext.getTransactionPolicyFactory().createTransactionPolicy(type);

        // save previous EJB ThreadContext transaction policy so it can be restored later
        TransactionPolicy oldTxPolicy = threadContext.getTransactionPolicy();
        txPolicy.putResource(CallerTransactionEnvironment.class, new CallerTransactionEnvironment(oldTxPolicy));

        // expose the new transaction policy to the EJB ThreadContext
        threadContext.setTransactionPolicy(txPolicy);

        return txPolicy;
    }

    /**
     * Completes the specified TransactionPolicy and disassociates it from the specified ThreadContext.
     */
    public static void afterInvoke(TransactionPolicy txPolicy, ThreadContext threadContext) throws SystemException, ApplicationException {
        if (txPolicy == threadContext.getTransactionPolicy()) {
            // Everything is in order, complete the transaction
            try {
                txPolicy.commit();
            } finally {
                // restore previous EJB ThreadContext transaction environment
                CallerTransactionEnvironment oldTxEnv = (CallerTransactionEnvironment) txPolicy.getResource(CallerTransactionEnvironment.class);
                if (oldTxEnv != null) {
                    threadContext.setTransactionPolicy(oldTxEnv.oldTxPolicy);
                } else {
                    threadContext.setTransactionPolicy(null);
                }
            }
        } else {
            // System is corrupted... roll back both transactions
            try {
                txPolicy.setRollbackOnly();
                txPolicy.commit();
            } catch (Exception e) {
                threadContext.setDiscardInstance(true);
                logger.error("Error rolling back transaction", e);
            }

            TransactionPolicy threadContextTxPolicy = threadContext.getTransactionPolicy();
            if (threadContextTxPolicy != null) {
                try {
                    threadContextTxPolicy.setRollbackOnly();
                    threadContextTxPolicy.commit();
                } catch (Exception e) {
                    threadContext.setDiscardInstance(true);
                    logger.error("Error rolling back transaction", e);
                }
            }

            if (threadContextTxPolicy != null) {
                throw new SystemException(new IllegalStateException("ThreadContext is bound to another transaction " + threadContextTxPolicy));
            } else {
                throw new SystemException(new IllegalStateException("ThreadContext is not bound to specified transaction " + threadContextTxPolicy));
            }
        }
    }

    /**
     * Performs EJB rules when an application exception occurs.
     */
    public static void handleApplicationException(TransactionPolicy txPolicy, Throwable appException, boolean rollback) throws ApplicationException {
        if (rollback) {
            txPolicy.setRollbackOnly(appException);
        }

        if (!(appException instanceof ApplicationException)) {
            throw new ApplicationException(appException);
        }
    }

    /**
     * Performs EJB rules when a system exception occurs.
     */
    public static void handleSystemException(TransactionPolicy txPolicy, Throwable sysException, ThreadContext callContext) throws InvalidateReferenceException {
        // Log the system exception or error
        Operation operation = null;
        if (callContext != null) {
            operation = callContext.getCurrentOperation();
        }
        if (operation != null && operation.isCallback()) {
            logger.error("startup.beanInstanceSystemExceptionThrown", sysException, sysException.getMessage());
        } else {
            logger.debug("startup.beanInstanceSystemExceptionThrown", sysException, sysException.getMessage());
        }

        // Mark the transaction for rollback
        txPolicy.setRollbackOnly(sysException);

        // Throw InvalidateReferenceException
        if (txPolicy.isClientTransaction()) {
            // using caller's transaction
            String message = "The transaction has been marked rollback only because the bean encountered a non-application exception :" + sysException.getClass().getName() + " : " + sysException.getMessage();
            TransactionRolledbackException txException = new TransactionRolledbackException(message, sysException);
            throw new InvalidateReferenceException(txException);
        } else {
            // no transaction or in a new transaction for this method call
            RemoteException re = new RemoteException("The bean encountered a non-application exception", sysException);
            throw new InvalidateReferenceException(re);
        }
    }

    private EjbTransactionUtil() {
    }

    private static class CallerTransactionEnvironment {
        private final TransactionPolicy oldTxPolicy;

        private CallerTransactionEnvironment(TransactionPolicy oldTxPolicy) {
            this.oldTxPolicy = oldTxPolicy;
        }
    }
}

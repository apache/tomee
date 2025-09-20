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

import org.apache.openejb.ApplicationException;
import org.apache.openejb.BeanContext;
import org.apache.openejb.InvalidateReferenceException;
import org.apache.openejb.SystemException;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.ThreadContextListener;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import java.rmi.RemoteException;

public final class EjbTransactionUtil {

    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources");

    static {
        ThreadContext.addThreadContextListener(new ThreadContextListener() {
            @Override
            public void contextEntered(final ThreadContext oldContext, final ThreadContext newContext) {
                // propagate current tx environment to the new ThreadContext
                if (oldContext != null) {
                    newContext.setTransactionPolicy(oldContext.getTransactionPolicy());
                }
            }

            @Override
            public void contextExited(final ThreadContext exitedContext, final ThreadContext reenteredContext) {
            }
        });
    }

    /**
     * Creates a new TransctionPolicy of the specified type and associates it with the specified ThreadContext.
     */
    public static TransactionPolicy createTransactionPolicy(final TransactionType type, final ThreadContext threadContext) throws SystemException, ApplicationException {
        // start the new transaction policy
        final BeanContext beanContext = threadContext.getBeanContext();
        final TransactionPolicy txPolicy = beanContext.getTransactionPolicyFactory().createTransactionPolicy(type);

        // save previous EJB ThreadContext transaction policy so it can be restored later
        final TransactionPolicy oldTxPolicy = threadContext.getTransactionPolicy();
        txPolicy.putResource(CallerTransactionEnvironment.class, new CallerTransactionEnvironment(oldTxPolicy));

        // expose the new transaction policy to the EJB ThreadContext
        threadContext.setTransactionPolicy(txPolicy);

        return txPolicy;
    }

    /**
     * Completes the specified TransactionPolicy and disassociates it from the specified ThreadContext.
     */
    public static void afterInvoke(final TransactionPolicy txPolicy, final ThreadContext threadContext) throws SystemException, ApplicationException {
        if (txPolicy == threadContext.getTransactionPolicy()) {
            // Everything is in order, complete the transaction
            try {
                txPolicy.commit();
            } finally {
                // restore previous EJB ThreadContext transaction environment
                final CallerTransactionEnvironment oldTxEnv = (CallerTransactionEnvironment) txPolicy.getResource(CallerTransactionEnvironment.class);
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
            } catch (final Exception e) {
                threadContext.setDiscardInstance(true);
                logger.error("Error rolling back transaction", e);
            }

            final TransactionPolicy threadContextTxPolicy = threadContext.getTransactionPolicy();
            if (threadContextTxPolicy != null) {
                try {
                    threadContextTxPolicy.setRollbackOnly();
                    threadContextTxPolicy.commit();
                } catch (final Exception e) {
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
    public static void handleApplicationException(final TransactionPolicy txPolicy, final Throwable appException, final boolean rollback) throws ApplicationException {
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
    public static void handleSystemException(final TransactionPolicy txPolicy, final Throwable sysException, final ThreadContext callContext) throws InvalidateReferenceException {
        // Log the system exception or error
        Operation operation = null;
        if (callContext != null) {
            operation = callContext.getCurrentOperation();
        }
        if (operation != null) {
            logger.error("EjbTransactionUtil.handleSystemException: " + sysException.getMessage(), sysException);
        } else {
            logger.debug("EjbTransactionUtil.handleSystemException: " + sysException.getMessage(), sysException);
        }

        // Mark the transaction for rollback
        txPolicy.setRollbackOnly(sysException);

        // Throw InvalidateReferenceException
        if (txPolicy.isClientTransaction()) {
            // using caller's transaction
            final String message = "The transaction has been marked rollback only because the bean encountered a non-application exception :" + sysException.getClass().getName() + " : " + sysException.getMessage();
            final TransactionRolledbackException txException = new TransactionRolledbackException(message, sysException);
            throw new InvalidateReferenceException(txException);
        } else {
            // no transaction or in a new transaction for this method call
            final RemoteException re = new RemoteException("The bean encountered a non-application exception", sysException);
            throw new InvalidateReferenceException(re);
        }
    }

    private EjbTransactionUtil() {
    }

    private static final class CallerTransactionEnvironment {

        private final TransactionPolicy oldTxPolicy;

        private CallerTransactionEnvironment(final TransactionPolicy oldTxPolicy) {
            this.oldTxPolicy = oldTxPolicy;
        }
    }
}

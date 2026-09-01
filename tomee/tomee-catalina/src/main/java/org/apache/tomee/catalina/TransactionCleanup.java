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
package org.apache.tomee.catalina;

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import jakarta.transaction.Status;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;

/**
 * Rolls back and unassociates any transaction a request left behind on the worker thread.
 *
 * A servlet or JSP using a bean managed {@link jakarta.transaction.UserTransaction} is not
 * wrapped by a container interceptor, so nothing restores the thread state when the request
 * ends. Since Tomcat pools its exec threads, a transaction still associated at that point is
 * inherited by whichever request is served next on the same thread, which then sees a bogus
 * transaction status. The per thread transaction timeout set via
 * {@link TransactionManager#setTransactionTimeout(int)} leaks the same way.
 *
 * This runs from {@link OpenEJBSecurityListener.RequestCapturer}, which sits on the Host
 * pipeline and therefore wraps all of {@code StandardHostValve#invoke}. That placement is
 * deliberate: the Context pipeline returns before
 * {@link jakarta.servlet.ServletRequestListener#requestDestroyed}, before the
 * {@code throwable()}/{@code status()} error handling, and before {@code <error-page>}
 * servlets and JSPs, which are dispatched through an include rather than a Pipeline. Cleaning
 * up from the Context pipeline would leave all of those uncovered and would also pre-empt an
 * application that completes its transaction in {@code requestDestroyed}.
 *
 * @see <a href="https://issues.apache.org/jira/browse/TOMEE-4652">TOMEE-4652</a>
 */
public final class TransactionCleanup {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.TRANSACTION, TransactionCleanup.class);

    private TransactionCleanup() {
        // no-op
    }

    /**
     * Restores the calling thread to a state with no transaction associated to it. Any dangling
     * transaction is rolled back since the request that started it can no longer complete it.
     */
    public static void clean() {
        final TransactionManager transactionManager = SystemInstance.get().getComponent(TransactionManager.class);
        if (transactionManager == null) {
            return;
        }

        try {
            final Transaction transaction = transactionManager.getTransaction();
            if (transaction != null && transaction.getStatus() != Status.STATUS_NO_TRANSACTION) {
                rollback(transactionManager, transaction);
            }
        } catch (final Throwable t) {
            LOGGER.error("Failed to roll back the transaction left over by this request", t);
            // the rollback failed, but the association must not survive this request either
            try {
                transactionManager.suspend();
            } catch (final Throwable suspendFailure) {
                LOGGER.error("Failed to unassociate the transaction left over by this request", suspendFailure);
            }
        }

        resetTimeout(transactionManager);
    }

    private static void rollback(final TransactionManager transactionManager, final Transaction transaction)
        throws Exception {
        // a transaction the reaper already finished is not a leak the application can be blamed
        // for, so unassociate it just the same but don't shout about it
        final int status = transaction.getStatus();
        if (status == Status.STATUS_ROLLEDBACK || status == Status.STATUS_ROLLING_BACK) {
            LOGGER.debug("Request ended with an already rolled back transaction " + transaction
                + ", unassociating it from this thread");
        } else {
            LOGGER.warning("Request ended with an active transaction " + transaction
                + ", rolling it back to avoid leaking it to the next request on this thread");
        }
        transactionManager.rollback();
    }

    private static void resetTimeout(final TransactionManager transactionManager) {
        // begin() only clears the timeout once a transaction is actually started, so a request
        // that set one without beginning leaves it on the thread.
        //
        // This does pin a ThreadLocal entry, which the CoreUserTransaction.resetError change in
        // this same commit argues against. The tradeoff differs: there is no way to read the
        // pending timeout back (Geronimo exposes no getter, only the package private
        // getTransactionTimeoutMilliseconds(long)), so skipping the call when nothing was set is
        // not an option, and setTransactionTimeout(0) stores a null value rather than an
        // exception. A null valued entry cannot hold a webapp classloader alive the way a stored
        // exception's stack trace can, so leaking the wrong timeout is the worse of the two.
        try {
            transactionManager.setTransactionTimeout(0);
        } catch (final Throwable t) {
            LOGGER.error("Failed to reset the transaction timeout left over by this request", t);
        }
    }
}

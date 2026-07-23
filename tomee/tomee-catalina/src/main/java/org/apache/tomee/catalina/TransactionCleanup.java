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
                LOGGER.warning("Request ended with an active transaction " + transaction
                    + ", rolling it back to avoid leaking it to the next request on this thread");
                transactionManager.rollback();
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

        // begin() only resets this once a transaction is actually started, so reset it explicitly
        try {
            transactionManager.setTransactionTimeout(0);
        } catch (final Throwable t) {
            LOGGER.error("Failed to reset the transaction timeout left over by this request", t);
        }
    }
}

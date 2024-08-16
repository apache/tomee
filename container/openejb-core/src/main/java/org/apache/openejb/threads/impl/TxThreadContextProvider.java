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
package org.apache.openejb.threads.impl;

import jakarta.enterprise.concurrent.ContextServiceDefinition;
import jakarta.enterprise.concurrent.spi.ThreadContextProvider;
import jakarta.enterprise.concurrent.spi.ThreadContextRestorer;
import jakarta.enterprise.concurrent.spi.ThreadContextSnapshot;
import jakarta.transaction.InvalidTransactionException;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.OpenEJBRuntimeException;

import java.util.Map;

public class TxThreadContextProvider implements ThreadContextProvider {
    @Override
    public ThreadContextSnapshot currentContext(final Map<String, String> props) {
        try {
            return new TxThreadContextSnapshot(OpenEJB.getTransactionManager().getTransaction());
        } catch (SystemException e) {
            throw new OpenEJBRuntimeException(e);
        }
    }

    @Override
    public ThreadContextSnapshot clearedContext(final Map<String, String> props) {
        return new TxThreadContextSnapshot(null);
    }

    @Override
    public String getThreadContextType() {
        return ContextServiceDefinition.TRANSACTION;
    }

    public static class TxThreadContextSnapshot implements ThreadContextSnapshot {
        private final Transaction transaction;

        public TxThreadContextSnapshot(Transaction transaction) {
            this.transaction = transaction;
        }

        @Override
        public ThreadContextRestorer begin() {
            TransactionManager transactionManager = OpenEJB.getTransactionManager();

            try {
                Transaction oldTransaction = transactionManager.suspend();
                transactionManager.resume(transaction);
                return new TxThreadContextRestorer(oldTransaction);
            } catch (SystemException | InvalidTransactionException e) {
                throw new OpenEJBRuntimeException(e);
            }
        }
    }

    public static class TxThreadContextRestorer implements ThreadContextRestorer {
        private final Transaction transaction;

        public TxThreadContextRestorer(Transaction transaction) {
            this.transaction = transaction;
        }

        @Override
        public void endContext() throws IllegalStateException {
            try {
                OpenEJB.getTransactionManager().resume(transaction);
            } catch (SystemException | InvalidTransactionException e) {
                throw new OpenEJBRuntimeException(e);
            }
        }
    }
}

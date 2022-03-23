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

import org.apache.openejb.SystemException;

import jakarta.transaction.UserTransaction;

/**
 * BeanTransactionPolicy is an extension to the TransactionPolicy which provides
 * operations for controlling a JEE bean managed transaction.
 */
public interface BeanTransactionPolicy extends TransactionPolicy {
    /**
     * Gets the UserTransaction the bean uses to manage the transaction.
     *
     * @return the UserTransaction the bean uses to manage the transaction
     */
    UserTransaction getUserTransaction();

    /**
     * Syspends the bean managed transaction.  This is mainly used by stateful
     * session beans which are required to maintain the bean managed transaction
     * between calls. After this method completes the transaction will be
     * suspended or if there is a problem it will be rolled back.
     *
     * @return the syspended transaction token or null if no transaction was
     * active
     * @throws SystemException if there was a problem suspending the
     *                         transaction
     */
    SuspendedTransaction suspendUserTransaction() throws SystemException;

    /**
     * Resumes the transaction contained in the suspended transaction token.
     * After this method completes the transaction will be resumes or if there
     * is a problem it will be rolled back.
     *
     * @param suspendedTransaction the transaction to resume; not null
     * @throws SystemException if there was a problem resuming the transaction
     */
    void resumeUserTransaction(SuspendedTransaction suspendedTransaction) throws SystemException;

    /**
     * Represents a suspended transaction.
     */
    interface SuspendedTransaction {
        /**
         * Rollsback the suspended transction.  No exceptions are thrown from
         * this method.
         */
        void destroy();
    }
}

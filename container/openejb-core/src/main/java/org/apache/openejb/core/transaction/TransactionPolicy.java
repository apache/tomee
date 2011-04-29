/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.core.transaction;

import javax.transaction.xa.XAResource;

import org.apache.openejb.ApplicationException;
import org.apache.openejb.SystemException;

/**
 * TransactionPolicy represents a JEE container managed or bean manage
 * transaction.
 * <p/>
 * This class can be used to query the transaction status, set the transaction
 * rollback only flag, associate resources with the transaction and to register
 * a listener for transaction completion events.
 */
public interface TransactionPolicy {
    /**
     * Gets the TransactionType for this policy.
     *
     * @return the TransactionType for this policy
     */
    TransactionType getTransactionType();

    /**
     * Is this a new transaction and not an inhreited transaction or no transaction?  Some
     * TransactionTypes, such as Required or Supported, use the caller's
     * transaction instead of starting a new transaction.  If there is no active
     * transaction (e.g., TransactionType is NotSupported), this method will
     *
     * @return true if this not an inherited transaction
     */
    boolean isNewTransaction();


    /**
     * Is this policy running in an inhreited transaction?  Some
     * TransactionTypes, such as Required or Supported, use the caller's
     * transaction instead of starting a new transaction.  If there is no active
     * transaction (e.g., TransactionType is NotSupported), this method will
     * return false.
     *
     * @return true if this is an inherited transaction
     */
    boolean isClientTransaction();

    /**
     * Is there a actual transaction active?
     *
     * @return true if there is an actual transaction active
     */
    boolean isTransactionActive();

    /**
     * If true, this TransactionPolicy will ultimately end with rollback.
     *
     * @return true if this TransactionPolicy will ultimately end with rollback
     */
    boolean isRollbackOnly();

    /**
     * Sets this TransactionPolicy to rollback when completed
     */
    void setRollbackOnly();

    /**
     * Sets this TransactionPolicy to rollback when completed
     */
    void setRollbackOnly(Throwable reason);

    /**
     * Commits or rolls back this TransactionPolicy.  If there the actual
     * transaction is completed or there is no actual transaction, the
     * registered TransactionSynchronization are called.  Otherwise, the
     * registered TransactionSynchronization are called when the actual
     * transaction is completed.
     *
     * @throws ApplicationException if recoverable exception is encountered
     * @throws SystemException if an unrecoverable exception is encountered
     */
    void commit() throws ApplicationException, SystemException;

    /**
     * Gets a resource associated with the specified key.  If there is an actual
     * transaction active, the resource associated with the transaction is
     * returned; otherwise the resource is scoped to this TransactionPolicy.
     *
     * @param key the resource key
     * @return the resource or null if no resource was associated with the key
     */
    Object getResource(Object key);

    /**
     * Associates the specified resource with the specified key.  If there is an
     * actual transaction active, the resource associated with the transaction
     * is set; otherwise the resource is scoped to this TransactionPolicy.
     *
     * @param key the resource key
     * @param value the resource
     */
    void putResource(Object key, Object value);

    /**
     * Removes and returns the resource associated with the specified key.  If
     * there is an actual transaction active, the resource associated with the
     * transaction is returned; otherwise the resource is scoped to this
     * TransactionPolicy.
     *
     * @param key the resource key
     * @return the resource previously associated with the key
     */
    Object removeResource(Object key);

    /**
     * Registers a listener for transaction synchronization events.  If there is
     * an actual transaction active, the events are fired when the acutal
     * transaction is commited; otherwise the events are fired when this
     * TransactionPolicy completes.
     *
     * @param synchronization the transaction synchronization listener
     */
    void registerSynchronization(TransactionSynchronization synchronization);

    /**
     * Enlists a XAResource in the actual active transaction.  This only works
     * if the TransactionPolicy is associated with an actual transaction and the
     * TransactionPolicy supports XAResouce enlistment.
     *
     * @param xaResource the XAResource to enlist
     * @throws SystemException if the xaResource could not be enlisted in the
     * transaction
     */
    void enlistResource(XAResource xaResource) throws SystemException;

    /**
     * TransactionSynchronization receives notifications as the Transaction
     * completes.
     */
    interface TransactionSynchronization {
        public enum Status {
            COMMITTED, ROLLEDBACK, UNKNOWN
        }

        /**
         * Called immediately before the transaction is completed.
         */
        void beforeCompletion();

        /**
         * Called after the transaction is completed.
         */
        void afterCompletion(Status status);
    }
}

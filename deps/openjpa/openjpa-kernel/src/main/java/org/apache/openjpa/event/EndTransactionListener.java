/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.event;

/**
 * Notified when transactions end.
 *
 * @author Patrick Linskey
 * @author Abe White
 * @since 0.2.5.0
 */
public interface EndTransactionListener {

    /**
     * Notification that the next flush is for the transaction commit.
     *
     * @see TransactionEvent#BEFORE_COMMIT
     */
    public void beforeCommit(TransactionEvent event);

    /**
     * Notification that a transaction has successfully committed.
     *
     * @see TransactionEvent#AFTER_COMMIT
     */
    public void afterCommit(TransactionEvent event);

    /**
     * Notification that a transaction has been rolled back.
     *
     * @see TransactionEvent#AFTER_ROLLBACK
     */
    public void afterRollback(TransactionEvent event);

    /**
     * Notification that state transitions are complete.
     *
     * @see TransactionEvent#AFTER_STATE_TRANSITIONS
     */
    public void afterStateTransitions(TransactionEvent event);

    /**
     * Notification that a transaction has successfully committed and
     * the transaction is no longer active.
     *
     * @see TransactionEvent#AFTER_COMMIT_COMPLETE
     */
    public void afterCommitComplete(TransactionEvent event);

    /**
     * Notification that a transaction has been rolled back and
     * the transaction is no longer active.
     *
     * @see TransactionEvent#AFTER_ROLLBACK_COMPLETE
     */
    public void afterRollbackComplete(TransactionEvent event);
}

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

import org.apache.openjpa.lib.util.concurrent.AbstractConcurrentEventManager;

/**
 * Manager that can be used to track and notify transaction listeners
 * of transaction-related events.
 *
 * @author Patrick Linskey
 * @author Abe White
 * @since 0.3.0
 * @nojavadoc
 */
public class TransactionEventManager
    extends AbstractConcurrentEventManager {

    private int _begin = 0;
    private int _flush = 0;
    private int _end = 0;

    public void addListener(Object listener) {
        super.addListener(listener);
        if (listener instanceof BeginTransactionListener)
            _begin++;
        if (listener instanceof FlushTransactionListener)
            _flush++;
        if (listener instanceof EndTransactionListener)
            _end++;
    }

    public boolean removeListener(Object listener) {
        if (!super.removeListener(listener))
            return false;

        if (listener instanceof BeginTransactionListener)
            _begin--;
        if (listener instanceof FlushTransactionListener)
            _flush--;
        if (listener instanceof EndTransactionListener)
            _end--;
        return true;
    }

    /**
     * Whether there are any begin transaction listeners.
     */
    public boolean hasBeginListeners() {
        return _begin > 0;
    }

    /**
     * Whether there are any flush transaction listeners.
     */
    public boolean hasFlushListeners() {
        return _flush > 0;
    }

    /**
     * Whether there are any end transaction listeners.
     */
    public boolean hasEndListeners() {
        return _end > 0;
    }

    /**
     * Fire the given event to all registered listeners.
     */
    protected void fireEvent(Object event, Object listener) {
        TransactionEvent ev = (TransactionEvent) event;
        switch (ev.getType()) {
            case TransactionEvent.AFTER_BEGIN:
                if (listener instanceof BeginTransactionListener)
                    ((BeginTransactionListener) listener).afterBegin(ev);
                break;
            case TransactionEvent.BEFORE_FLUSH:
                if (listener instanceof FlushTransactionListener)
                    ((FlushTransactionListener) listener).beforeFlush(ev);
                break;
            case TransactionEvent.AFTER_FLUSH:
                if (listener instanceof FlushTransactionListener)
                    ((FlushTransactionListener) listener).afterFlush(ev);
                break;
            case TransactionEvent.BEFORE_COMMIT:
                if (listener instanceof EndTransactionListener)
                    ((EndTransactionListener) listener).beforeCommit(ev);
                break;
            case TransactionEvent.AFTER_COMMIT:
                if (listener instanceof EndTransactionListener)
                    ((EndTransactionListener) listener).afterCommit(ev);
                break;
            case TransactionEvent.AFTER_ROLLBACK:
                if (listener instanceof EndTransactionListener)
                    ((EndTransactionListener) listener).afterRollback(ev);
                break;
            case TransactionEvent.AFTER_STATE_TRANSITIONS:
                if (listener instanceof EndTransactionListener)
                    ((EndTransactionListener) listener)
                        .afterStateTransitions(ev);
                break;
            case TransactionEvent.AFTER_COMMIT_COMPLETE:
                if (listener instanceof EndTransactionListener)
                    ((EndTransactionListener) listener).afterCommitComplete(ev);
                break;
            case TransactionEvent.AFTER_ROLLBACK_COMPLETE:
                if (listener instanceof EndTransactionListener)
                    ((EndTransactionListener) listener)
                        .afterRollbackComplete(ev);
                break;
        }
	}
}

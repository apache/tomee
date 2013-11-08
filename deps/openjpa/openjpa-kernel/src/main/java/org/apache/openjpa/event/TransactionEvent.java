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

import java.util.Collection;
import java.util.Collections;
import java.util.EventObject;

import org.apache.openjpa.kernel.Broker;

/**
 * A transactional event. The event source is the {@link Broker} whose
 * transaction state changed.
 *
 * @since 0.3.0
 * @author Abe White
 */
public class TransactionEvent
    extends EventObject {

    /**
     * Event type signifying that a transaction has just begun.
     */
    public static final int AFTER_BEGIN = 0;

    /**
     * Event type signifying that changes are about to be flushed to the
     * datastore. This does not necessarily mean that a commit is taking place.
     */
    public static final int BEFORE_FLUSH = 1;

    /**
     * Event type signifying that changes have been flushed to the datastore.
     * This event is only fired if flushing is successful.
     */
    public static final int AFTER_FLUSH = 2;

    /**
     * Event type signifying that the transaction is about to be committed.
     * This will be fired before the {@link #BEFORE_FLUSH} event for the
     * transaction, so that you can differentiate between flushes for commits
     * and other flushes.
     */
    public static final int BEFORE_COMMIT = 3;

    /**
     * Event type signifying that the transaction has committed.
     */
    public static final int AFTER_COMMIT = 4;

    /**
     * Event type signifying that the transaction was rolled back.
     */
    public static final int AFTER_ROLLBACK = 5;

    /**
     * Event type signifying that all state transitions have been made.
     */
    public static final int AFTER_STATE_TRANSITIONS = 6;

    /**
     * Event type signifying that the commit has completey ended and the
     * transaction is no longer active.
     */
    public static final int AFTER_COMMIT_COMPLETE = 7;

    /**
     * Event type signifying that the rollback has completey ended and the
     * transaction is no longer active.
     */
    public static final int AFTER_ROLLBACK_COMPLETE = 8;

    private final int _type;
    private transient final Collection _objs;
    private transient final Collection _addClss;
    private transient final Collection _updateClss;
    private transient final Collection _deleteClss;

    /**
     * Constructor.
     *
     * @param broker the event source
     * @param type the event type
     * @param objs transactional objects
     * @param addClss classes of added instances
     * @param updateClss classes of updated instances
     * @param deleteClss classes of deleted instances
     */
    public TransactionEvent(Broker broker, int type,
        Collection objs, Collection addClss, Collection updateClss,
        Collection deleteClss) {
        super(broker);
        _type = type;
        _objs = (objs == null) ? Collections.EMPTY_LIST : objs;
        _addClss = (addClss == null) ? Collections.EMPTY_SET : addClss;
        _updateClss = (updateClss == null) ? Collections.EMPTY_SET : updateClss;
        _deleteClss = (deleteClss == null) ? Collections.EMPTY_SET : deleteClss;
    }

    /**
     * Return the type of event.
     */
    public int getType() {
        return _type;
    }

    /**
     * Return the unmodifiable set of persistence capable objects
     * participating in the transaction. This set will contain all dirty
     * objects, but may not contain clean objects.
     */
    public Collection getTransactionalObjects() {
        return _objs;
    }

    /**
     * Return the unmodifiable the set of classes of
     * persistence capable objects that were created in the transaction.
     */
    public Collection getPersistedTypes() {
        return _addClss;
    }

    /**
     * Return the unmodifiable the set of classes of
     * persistence capable objects that were modified in the transaction.
     */
    public Collection getUpdatedTypes() {
        return _updateClss;
    }

    /**
     * Return the unmodifiable the set of classes of
     * persistence capable objects that were deleted in the transaction.
     */
    public Collection getDeletedTypes ()
	{
		return _deleteClss;
	}
}

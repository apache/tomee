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

import java.util.EventObject;

/**
 * Lifecycle event on a persistent instance. The event source
 * is the persistent instance whose state has changed.
 *
 * @author Steve Kim
 * @author Abe White
 * @since 0.3.3
 */
public class LifecycleEvent
    extends EventObject {

    /**
     * Event type before an instance is made persistent
     */
    public static final int BEFORE_PERSIST = 0;

    /**
     * Event type when an instance is made persistent
     */
    public static final int AFTER_PERSIST = 1;

    /**
     * Event type when an instance is made persistent, after the record has
     * been written to the store
     */
    public static final int AFTER_PERSIST_PERFORMED = 18;

    /**
     * Event type when an instance is loaded.
     */
    public static final int AFTER_LOAD = 2;

    /**
     * Event type when an instance is stored.
     */
    public static final int BEFORE_STORE = 3;

    /**
     * Event type when an instance is stored.
     */
    public static final int AFTER_STORE = 4;

    /**
     * Event type when an instance is cleared.
     */
    public static final int BEFORE_CLEAR = 5;

    /**
     * Event type when an instance is cleared.
     */
    public static final int AFTER_CLEAR = 6;

    /**
     * Event type when an instance is deleted.
     */
    public static final int BEFORE_DELETE = 7;

    /**
     * Event type when an instance is deleted.
     */
    public static final int AFTER_DELETE = 8;

    /**
     * Event type when an instance is deleted, after the record has been
     * deleted from the store.
     */
    public static final int AFTER_DELETE_PERFORMED = 19;

    /**
     * Event type when an instance is dirtied for the first time.
     */
    public static final int BEFORE_DIRTY = 9;

    /**
     * Event type when an instance is dirtied for the first time.
     */
    public static final int AFTER_DIRTY = 10;

    /**
     * Event type when an instance is dirtied for the first time after flush.
     */
    public static final int BEFORE_DIRTY_FLUSHED = 11;

    /**
     * Event type when an instance is dirtied for the first time after flush.
     */
    public static final int AFTER_DIRTY_FLUSHED = 12;

    /**
     * Event type when an instance is detached.
     */
    public static final int BEFORE_DETACH = 13;

    /**
     * Event type when an instance is detached.
     */
    public static final int AFTER_DETACH = 14;

    /**
     * Event type when an instance is attached.
     */
    public static final int BEFORE_ATTACH = 15;

    /**
     * Event type when an instance is attached.
     */
    public static final int AFTER_ATTACH = 16;

    /**
     * Event type when an instances is refreshed.
     */
    public static final int AFTER_REFRESH = 17;

    /**
     * Event type when an instance is modified. This is not invoked for
     * PNEW records, but is invoked for PNEWFLUSHED.
     */
    public static final int BEFORE_UPDATE = 20;

    /**
     * Event type when an instance is modified, after the change has been
     * sent to the store. This is not invoked for PNEW records, but is
     * invoked for PNEWFLUSHED records.
     */
    public static final int AFTER_UPDATE_PERFORMED = 21;

    /**
     * Convenience array of all event types.
     */
    public static final int[] ALL_EVENTS = new int[]{
        BEFORE_PERSIST,
        AFTER_PERSIST,
        AFTER_PERSIST_PERFORMED,
        AFTER_LOAD,
        BEFORE_STORE,
        AFTER_STORE,
        BEFORE_CLEAR,
        AFTER_CLEAR,
        BEFORE_DELETE,
        AFTER_DELETE,
        AFTER_DELETE_PERFORMED,
        BEFORE_DIRTY,
        AFTER_DIRTY,
        BEFORE_DIRTY_FLUSHED,
        AFTER_DIRTY_FLUSHED,
        BEFORE_DETACH,
        AFTER_DETACH,
        BEFORE_ATTACH,
        AFTER_ATTACH,
        AFTER_REFRESH,
        BEFORE_UPDATE,
        AFTER_UPDATE_PERFORMED,
    };

    private final int _type;
    private final Object _related;

    /**
     * Constructor.
     *
     * @param pc the persistent instance that triggered the event
     * @param type the event type
     */
    public LifecycleEvent(Object pc, int type) {
        this(pc, null, type);
    }

    /**
     * Constructor.
     *
     * @param pc the persistent instance that triggered the event
     * @param type the event type
     * @param related the related instance such as the detached copy.
     */
    public LifecycleEvent(Object pc, Object related, int type) {
        super(pc);
        _type = type;
        _related = related;
    }

    /**
     * Return the event type.
     */
    public int getType() {
        return _type;
    }

    /**
     * Return the related object.
	 */
	public Object getRelated ()
	{
		return _related;
	}
}

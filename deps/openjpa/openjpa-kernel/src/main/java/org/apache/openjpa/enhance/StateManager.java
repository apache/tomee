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
package org.apache.openjpa.enhance;

import java.io.IOException;
import java.io.ObjectOutput;

/**
 * Internal state manager for managed instances.
 */
public interface StateManager {
    // DO NOT ADD ADDITIONAL DEPENDENCIES TO THIS CLASS

    public static final int SET_USER = 0;
    public static final int SET_REMOTE = 1;
    public static final int SET_ATTACH = 2;

    /**
     * Persistence context.
     */
    public Object getGenericContext();

    /**
     * Return the persistence-capable primary key object by extracting the 
     * identity value of the related instance stored in the given field from 
     * the given object id.
     */
    public Object getPCPrimaryKey(Object oid, int field);

    /**
     * Change state manager.
     */
    public StateManager replaceStateManager(StateManager sm);

    /**
     * Returns the optimistic version for this instance.
     */
    public Object getVersion();

    /**
     * Whether the instance has been modified in this transaction.
     */
    public boolean isDirty();

    /**
     * Whether the instance is transactional.
     */
    public boolean isTransactional();

    /**
     * Whether the instance is persistent.
     */
    public boolean isPersistent();

    /**
     * Whether the instance is newly-persisted in this transaction.
     */
    public boolean isNew();

    /**
     * Whether the instance is deleted in this transaction.
     */
    public boolean isDeleted();

    /**
     * Whether the instance is detached (i.e. this manager is a detached
     * state manager)
     */
    public boolean isDetached();

    /**
     * Make named field dirty.
     */
    public void dirty(String field);

    /**
     * Return the object id, assigning it if necessary.
     */
    public Object fetchObjectId();

    /**
     * Callback to prepare instance for serialization.
     *
     * @return true to null detached state after serialize
     */
    public boolean serializing();

    /**
     * Write detached state object and detached state manager to the
     * given stream.
     *
     * @return true if managed fields also written to stream
     */
    public boolean writeDetached(ObjectOutput out)
        throws IOException;

    /**
     * Proxy the given detached field after deserialization.
     */
    public void proxyDetachedDeserialized(int idx);

    /**
     * Field access callback.
     */
    public void accessingField(int idx);

    /**
     * Setting state callback.
     */
    public void settingBooleanField(PersistenceCapable pc, int idx,
        boolean cur, boolean next, int set);

    /**
     * Setting state callback.
     */
    public void settingCharField(PersistenceCapable pc, int idx, char cur,
        char next, int set);

    /**
     * Setting state callback.
     */
    public void settingByteField(PersistenceCapable pc, int idx, byte cur,
        byte next, int set);

    /**
     * Setting state callback.
     */
    public void settingShortField(PersistenceCapable pc, int idx, short cur,
        short next, int set);

    /**
     * Setting state callback.
     */
    public void settingIntField(PersistenceCapable pc, int idx, int cur,
        int next, int set);

    /**
     * Setting state callback.
     */
    public void settingLongField(PersistenceCapable pc, int idx, long cur,
        long next, int set);

    /**
     * Setting state callback.
     */
    public void settingFloatField(PersistenceCapable pc, int idx, float cur,
        float next, int set);

    /**
     * Setting state callback.
     */
    public void settingDoubleField(PersistenceCapable pc, int idx, double cur,
        double next, int set);

    /**
     * Setting state callback.
     */
    public void settingStringField(PersistenceCapable pc, int idx, String cur,
        String next, int set);

    /**
     * Setting state callback.
     */
    public void settingObjectField(PersistenceCapable pc, int idx, Object cur,
        Object next, int set);

    /**
     * Provide state callback.
     */
    public void providedBooleanField(PersistenceCapable pc, int idx,
        boolean cur);

    /**
     * Provide state callback.
     */
    public void providedCharField(PersistenceCapable pc, int idx, char cur);

    /**
     * Provide state callback.
     */
    public void providedByteField(PersistenceCapable pc, int idx, byte cur);

    /**
     * Provide state callback.
     */
    public void providedShortField(PersistenceCapable pc, int idx, short cur);

    /**
     * Provide state callback.
     */
    public void providedIntField(PersistenceCapable pc, int idx, int cur);

    /**
     * Provide state callback.
     */
    public void providedLongField(PersistenceCapable pc, int idx, long cur);

    /**
     * Provide state callback.
     */
    public void providedFloatField(PersistenceCapable pc, int idx, float cur);

    /**
     * Provide state callback.
     */
    public void providedDoubleField(PersistenceCapable pc, int idx,
        double cur);

    /**
     * Provide state callback.
     */
    public void providedStringField(PersistenceCapable pc, int idx,
        String cur);

    /**
     * Provide state callback.
     */
    public void providedObjectField(PersistenceCapable pc, int idx,
        Object cur);

    /**
     * Replace state callback.
     */
    public boolean replaceBooleanField(PersistenceCapable pc, int idx);

    /**
     * Replace state callback.
     */
    public char replaceCharField(PersistenceCapable pc, int idx);

    /**
     * Replace state callback.
     */
    public byte replaceByteField(PersistenceCapable pc, int idx);

    /**
     * Replace state callback.
     */
    public short replaceShortField(PersistenceCapable pc, int idx);

    /**
     * Replace state callback.
     */
    public int replaceIntField(PersistenceCapable pc, int idx);

    /**
     * Replace state callback.
     */
    public long replaceLongField(PersistenceCapable pc, int idx);

    /**
     * Replace state callback.
     */
    public float replaceFloatField(PersistenceCapable pc, int idx);

    /**
     * Replace state callback.
     */
    public double replaceDoubleField(PersistenceCapable pc, int idx);

    /**
     * Replace state callback.
     */
    public String replaceStringField(PersistenceCapable pc, int idx);

	/**
	 * Replace state callback.
	 */
    public Object replaceObjectField (PersistenceCapable pc, int idx);
}

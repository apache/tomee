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
package org.apache.openjpa.kernel;

import java.util.Collection;

import org.apache.openjpa.lib.util.Closeable;

/**
 * Handles obtaining and releasing locks on objects. The lock manager
 * generally does not have to worry about synchronization, as the context is
 * responsible for synchronizing the calls it makes to the lock manager.
 *
 * @author Marc Prud'hommeaux
 */
public interface LockManager
    extends Closeable, LockLevels {

    /**
     * Set the context this lock manager is associated with.
     * This will be invoked in the lock manager before any other methods are
     * called.
     */
    public void setContext(StoreContext ctx);

    /**
     * Return the lock level of the specified instance, or
     * {@link LockLevels#LOCK_NONE} if not locked.
     */
    public int getLockLevel(OpenJPAStateManager sm);

    /**
     * Obtain a lock on the specified object. This method may be called when
     * a user explicitly locks an object, and is also called automatically
     * for every object accessed during a transaction. The implementation
     * must track already-locked objects, and must be optimized to return
     * quickly when the given object does not need additional locking.
     * The lock manager might use the state manager's lock object for
     * bookkeeping information.
     *
     * @param sm the object to lock
     * @param level one of the lock constants defined in {@link LockLevels},
     * or a custom level
     * @param timeout the timeout in milliseconds, or a negative number for
     * no timeout
     * @param sdata the context information passed from the store manager
     * to the persistence context, if any; lock managers
     * specific to a certain back end may be able to take
     * advantage of this; others should ignore it
     * @throws org.apache.openjpa.util.LockException if a lock cannot be 
     * obtained in the given number of milliseconds
     * @see OpenJPAStateManager#setLock
     */
    public void lock(OpenJPAStateManager sm, int level, int timeout,
        Object sdata);

    /**
     * Perform the same function as previous lock method and has the option
     * to perform a version check after the lock function has completed. 
     */
    public void refreshLock(OpenJPAStateManager sm, int level, int timeout,
        Object sdata);
    
    /**
     * Obtain locks on the specified objects.
     *
     * @see #lock
     */
    public void lockAll(Collection sms, int level, int timeout,
        Object sdata);

    /**
     * Release the lock on the given object. This method will be called
     * automatically for each state manager with a lock object set on
     * transaction completion, just before the call to {@link #endTransaction}.
     * The lock manager should null the state manager's lock object. Note
     * that some state manager may be garbage collected during a transaction;
     * thus lock managers cannot rely on this method being called for every
     * state manager.
     *
     * @see OpenJPAStateManager#setLock
     */
    public void release(OpenJPAStateManager sm);

    /**
     * Notification that a transaction is beginning. Locks are only obtained
     * within transactions, so an implementation might use this method to
     * initialize bookkeeping datastructures, etc.
     */
    public void beginTransaction();

    /**
     * Notification that the current transaction has ended. Clear all
     * datastructures, release any left over locks, etc.
     */
    public void endTransaction();

    /**
     * Free any resources.
     */
    public void close ();
}

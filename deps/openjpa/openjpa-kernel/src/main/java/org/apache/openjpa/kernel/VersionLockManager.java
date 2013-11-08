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


/**
 * {@link LockManager} implementation that provides support
 * for version checking and version updating when locks are acquired.
 * This lock manager may be used standalone or extended for additional locking.
 *
 * @author Marc Prud'hommeaux
 */
public class VersionLockManager
    extends AbstractLockManager {

    private boolean _versionCheckOnReadLock = true;
    private boolean _versionUpdateOnWriteLock = true;
    private boolean _refreshing = false;

    /**
     * Returns the given instance's lock level, assuming that the state's
     * lock object is a number. If the given instance is embedded, traverses
     * to its owner. Override if lock is not stored as a number.
     */
    public int getLockLevel(OpenJPAStateManager sm) {
        while (sm.getOwner() != null)
            sm = sm.getOwner();
        Number level = (Number) sm.getLock();
        return (level == null) ? LOCK_NONE : level.intValue();
    }

    /**
     * Sets the given instance's lock level to the given number. Override
     * to store something else as the lock.
     */
    protected void setLockLevel(OpenJPAStateManager sm, int level) {
        sm.setLock(level);
    }

    /**
     * Nulls given instance's lock object.
     */
    public void release(OpenJPAStateManager sm) {
        sm.setLock(null);
    }

    /**
     * Delegates to {@link #lockInternal} after traversing to owning
     * instance (if embedded) and assuring that the instance is persistent,
     * is not new, and is not already locked at a higher level. After
     * locking, calls {@link #setLockLevel} with the given level.
     */
    public void lock(OpenJPAStateManager sm, int level, int timeout,
        Object sdata) {
        commonLock(sm, level, timeout, sdata, !_refreshing);
    }
    
    public void refreshLock(OpenJPAStateManager sm, int level, int timeout,
            Object sdata) {
    	try {
        	_refreshing = true;
    		commonLock(sm, level, timeout, sdata, false);
    	} finally {
        	_refreshing = false;    		
    	}
    }

    private void commonLock(OpenJPAStateManager sm, int level, int timeout,
            Object sdata, boolean postLockVersionCheck) {
        if (level == LOCK_NONE)
            return;
        while (sm.getOwner() != null)
            sm = sm.getOwner();
        int oldLevel = getLockLevel(sm);
        if (!sm.isPersistent() || sm.isNew() || level <= oldLevel)
            return;

        try {
            lockInternal(sm, level, timeout, sdata, postLockVersionCheck);
        } catch (RuntimeException re) {
            // revert lock
            setLockLevel(sm, oldLevel);
            throw re;
        }
    }

    /**
     * Marks the instance's transactional status in accordance with
     * the settings of {@link #getVersionCheckOnReadLock}
     * and {@link #getVersionUpdateOnWriteLock}. Override to perform
     * additional locking.
     *
     * @see StoreContext#transactional
     */
    protected void lockInternal(OpenJPAStateManager sm, int level, int timeout,
        Object sdata, boolean postLockVersionCheck) {
        optimisticLockInternal(sm, level, timeout, sdata, postLockVersionCheck);
    }

    protected void optimisticLockInternal(OpenJPAStateManager sm, int level,
        int timeout, Object sdata, boolean postLockVersionCheck) {
        // Set lock level first to prevent infinite recursion with
        // transactional(..) call
        setLockLevel(sm, level);
        if (level >= LockLevels.LOCK_WRITE && _versionUpdateOnWriteLock)
            getContext().transactional(sm.getManagedInstance(), true, null);
        else if (level >= LockLevels.LOCK_READ && _versionCheckOnReadLock)
            getContext().transactional(sm.getManagedInstance(), false, null);
    }

    /**
     * Whether or not we should force a version check at commit
     * time when a read lock is requested in order to verify read
     * consistency. Defaults to true.
     */
    public void setVersionCheckOnReadLock(boolean versionCheckOnReadLock) {
        _versionCheckOnReadLock = versionCheckOnReadLock;
    }

    /**
     * Whether or not we should force a version check at commit
     * time when a read lock is requested in order to verify read
     * consistency. Defaults to true.
     */
    public boolean getVersionCheckOnReadLock() {
        return _versionCheckOnReadLock;
    }

    /**
     * Whether or not we should force an update to the version at commit
     * time when a write lock is requested. Defaults to true.
     */
    public void setVersionUpdateOnWriteLock(boolean versionUpdateOnWriteLock) {
        _versionUpdateOnWriteLock = versionUpdateOnWriteLock;
    }

    /**
     * Whether or not we should force an update to the version at commit
     * time when a write lock is requested. Defaults to true.
     */
    public boolean getVersionUpdateOnWriteLock() {
        return _versionUpdateOnWriteLock;
    }
}

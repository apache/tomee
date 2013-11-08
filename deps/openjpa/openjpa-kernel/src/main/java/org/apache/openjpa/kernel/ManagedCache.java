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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.util.ReferenceHashSet;
import org.apache.openjpa.util.Exceptions;
import org.apache.openjpa.util.UserException;

/**
 * Cache of managed objects. Must be static for serialization reasons.
 */
class ManagedCache implements Serializable {

    private static final Localizer _loc =
        Localizer.forPackage(ManagedCache.class);

    private Map<Object,StateManagerImpl> _main; // oid -> sm
    private Map<Object,StateManagerImpl> _conflicts = null; // conflict oid -> new sm
    private Map<Object,StateManagerImpl> _news = null; // tmp id -> new sm
    private Collection<StateManagerImpl> _embeds = null; // embedded/non-persistent sms
    private Collection<StateManagerImpl> _untracked = null; // hard refs to untracked sms
    private BrokerImpl broker;

    /**
     * Constructor; supply primary cache map.
     */
    ManagedCache(BrokerImpl broker) {
        this.broker = broker;
        _main = (Map<Object, StateManagerImpl>) broker.newManagedObjectCache();
    }

    /**
     * Return the instance for the given oid, optionally allowing
     * new instances.
     */
    public StateManagerImpl getById(Object oid, boolean allowNew) {
        if (oid == null)
            return null;

        // check main cache for oid
        StateManagerImpl sm = _main.get(oid);
        StateManagerImpl sm2;
        if (sm != null) {
            // if it's a new instance, we know it's the only match, because
            // other pers instances override new instances in _cache
            if (sm.isNew() && !sm.isDeleted())
                return (allowNew) ? sm : null;
            if (!allowNew || !sm.isDeleted())
                return sm;

            // sm is deleted; check conflict cache
            if (_conflicts != null) {
                sm2 = _conflicts.get(oid);
                if (sm2 != null)
                    return sm2;
            }
        }

        // at this point sm is null or deleted; check the new cache for
        // any matches. this allows us to match app id objects to new
        // instances without permanent oids
        if (allowNew && _news != null && !_news.isEmpty()) {
            sm2 = _news.get(oid);
            if (sm2 != null)
                return sm2;
        }
        return sm;
    }

    /**
     * Call this method when a new state manager initializes itself.
     */
    public void add(StateManagerImpl sm) {
        if (!sm.isIntercepting()) {
            if (_untracked == null)
                _untracked = new HashSet<StateManagerImpl>();
            _untracked.add(sm);
        }

        if (!sm.isPersistent() || sm.isEmbedded()) {
            if (_embeds == null)
                _embeds = new ReferenceHashSet(ReferenceHashSet.WEAK);
            _embeds.add(sm);
            return;
        }

        // initializing new instance; put in new cache because won't have
        // permanent oid yet
        if (sm.isNew()) {
            if (_news == null)
                _news = new HashMap<Object,StateManagerImpl>();
            _news.put(sm.getId(), sm);
            return;
        }

        // initializing persistent instance; put in main cache
        StateManagerImpl orig = _main.put(sm.getObjectId(), sm);
        if (orig != null) {
            _main.put(sm.getObjectId(), orig);
            throw new UserException(_loc.get("dup-load", sm.getObjectId(),
                Exceptions.toString(orig.getManagedInstance())))
                .setFailedObject(sm.getManagedInstance());
        }
    }

    /**
     * Remove the given state manager from the cache when it transitions
     * to transient.
     */
    public void remove(Object id, StateManagerImpl sm) {
        // if it has a permanent oid, remove from main / conflict cache,
        // else remove from embedded/nontrans cache, and if not there
        // remove from new cache
    	StateManagerImpl orig;
        if (sm.getObjectId() != null) {
            orig = _main.remove(id);
            if (orig != sm) {
                if (orig != null)
                    _main.put(id, orig); // put back
                if (_conflicts != null) {
                    orig = _conflicts.remove(id);
                    if (orig != null && orig != sm)
                        _conflicts.put(id, orig); // put back
                }
            }
        } else if ((_embeds == null || !_embeds.remove(sm))
            && _news != null) {
            orig = _news.remove(id);
            if (orig != null && orig != sm)
                _news.put(id, orig); // put back
        }

        if (_untracked != null)
            _untracked.remove(sm);
    }

    /**
     * An embedded or nonpersistent managed instance has been persisted.
     */
    public void persist(StateManagerImpl sm) {
        if (_embeds != null)
            _embeds.remove(sm);
    }

    /**
     * A new instance has just been assigned a permanent oid.
     */
    public void assignObjectId(Object id, StateManagerImpl sm) {
        // if assigning oid, remove from new cache and put in primary; may
        // not be in new cache if another new instance had same id
        StateManagerImpl orig = null;
        if (_news != null) {
            orig = _news.remove(id);
            if (orig != null && orig != sm)
                _news.put(id, orig); // put back
        }

        // put in main cache, but make sure we don't replace another
        // instance with the same oid
        orig = _main.put(sm.getObjectId(), sm);
        if (orig != null) {
            _main.put(sm.getObjectId(), orig);
            if (!orig.isDeleted())
                throw new UserException(_loc.get("dup-oid-assign",
                    sm.getObjectId(),
                    Exceptions.toString(sm.getManagedInstance())))
                    .setFailedObject(sm.getManagedInstance());

            // same oid as deleted instance; put in conflict cache
            if (_conflicts == null)
                _conflicts = new HashMap<Object,StateManagerImpl>();
            _conflicts.put(sm.getObjectId(), sm);
        }
    }

    /**
     * A new instance has committed; recache under permanent oid.
     */
    public void commitNew(Object id, StateManagerImpl sm) {
        // if the id didn't change, the instance was already assigned an
        // id, but it could have been in conflict cache
        StateManagerImpl orig;
        if (sm.getObjectId() == id) {
            orig = (_conflicts == null) ? null : _conflicts.remove(id);
            if (orig == sm) {
                orig = _main.put(id, sm);
                if (orig != null && !orig.isDeleted()) {
                    _main.put(sm.getObjectId(), orig);
                    throw new UserException(_loc.get("dup-oid-assign",
                        sm.getObjectId(), Exceptions.toString(
                            sm.getManagedInstance())))
                        .setFailedObject(sm.getManagedInstance())
                        .setFatal(true);
                }
            }
            return;
        }

        // oid changed, so it must previously have been a new instance
        // without an assigned oid.  remove it from the new cache; ok if
        // we end up removing another instance with same id
        if (_news != null)
            _news.remove(id);

        // and put into main cache now that id is assigned
        orig = _main.put(sm.getObjectId(), sm);
        if (orig != null && orig != sm && !orig.isDeleted()) {
            // put back orig and throw error
            _main.put(sm.getObjectId(), orig);
            throw new UserException(_loc.get("dup-oid-assign",
                sm.getObjectId(), Exceptions.toString(sm.getManagedInstance())))
                    .setFailedObject(sm.getManagedInstance()).setFatal(true);
        }
    }

    /**
     * Return a copy of all cached persistent objects.
     */
    public Collection<StateManagerImpl> copy() {
        // proxies not included here because the state manager is always
        // present in other caches too

        int size = _main.size();
        if (_conflicts != null)
            size += _conflicts.size();
        if (_news != null)
            size += _news.size();
        if (_embeds != null)
            size += _embeds.size();
        if (size == 0)
            return Collections.EMPTY_LIST;

        List<StateManagerImpl> copy = new ArrayList<StateManagerImpl>(size);
        for (StateManagerImpl sm : _main.values())
            copy.add(sm);
        if (_conflicts != null && !_conflicts.isEmpty())
            for (StateManagerImpl sm : _conflicts.values())
                copy.add(sm);
        if (_news != null && !_news.isEmpty())
        	 for (StateManagerImpl sm : _news.values())
                 copy.add(sm);
        if (_embeds != null && !_embeds.isEmpty())
        	for (StateManagerImpl sm : _embeds)
                copy.add(sm);
        return copy;
    }

    /**
     * Clear the cache.
     */
    public void clear() {
        _main = (Map<Object, StateManagerImpl>) broker.newManagedObjectCache();
        if (_conflicts != null)
            _conflicts = null;
        if (_news != null)
            _news = null;
        if (_embeds != null)
            _embeds = null;
        if (_untracked != null)
            _untracked = null;
    }

    /**
     * Clear new instances without permanent oids.
     */
    public void clearNew() {
        if (_news != null)
            _news = null;
    }

    void dirtyCheck() {
        if (_untracked == null)
            return;

        for (StateManagerImpl sm : _untracked)
        	sm.dirtyCheck();
    }
}

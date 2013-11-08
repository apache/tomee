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
package org.apache.openjpa.datacache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.event.RemoteCommitEvent;
import org.apache.openjpa.event.RemoteCommitListener;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.lib.conf.Configurable;
import org.apache.openjpa.lib.conf.Configuration;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.util.concurrent.AbstractConcurrentEventManager;
import org.apache.openjpa.util.GeneralException;

import serp.util.Strings;

/**
 * Abstract {@link DataCache} implementation that provides various
 * statistics, logging, and timeout functionality common across cache
 * implementations.
 *
 * @author Patrick Linskey
 * @author Abe White
 */
@SuppressWarnings("serial")
public abstract class AbstractDataCache extends AbstractConcurrentEventManager
    implements DataCache, Configurable {
	
    protected CacheStatisticsSPI _stats = new CacheStatisticsImpl();

    private static final BitSet EMPTY_BITSET = new BitSet(0);

    private static final Localizer s_loc = Localizer.forPackage(AbstractDataCache.class);
    

    /**
     * The configuration set by the system.
     */
    protected OpenJPAConfiguration conf;

    /**
     * The log to use.
     */
    protected Log log;

    private String _name = null;
    private boolean _closed = false;
    private String _schedule = null;
    protected Set<String> _includedTypes = new HashSet<String>();
    protected Set<String> _excludedTypes = new HashSet<String>();
    protected boolean _evictOnBulkUpdate = true;
    
    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }
    public void setEnableStatistics(boolean enable){
        if(enable == true){
            _stats.enable();
        }
    }
    public void getEnableStatistics(){
        _stats.isEnabled();
    }

    public String getEvictionSchedule() {
        return _schedule;
    }

    public void setEvictionSchedule(String s) {
        _schedule = s;
    }
    
    public void initialize(DataCacheManager manager) {
        if (_schedule != null && !"".equals(_schedule)) {
            ClearableScheduler scheduler = manager.getClearableScheduler();
            if (scheduler != null)
                scheduler.scheduleEviction(this, _schedule);
        }
        // Cast here rather than add to the interface because this is a hack to support an older way of configuring
        if(manager instanceof DataCacheManagerImpl){
            List<String> invalidConfigured = new ArrayList<String>();
            // assert that things are configured properly
            if(_includedTypes!=null){
                for(String s : _includedTypes){
                    if(_excludedTypes.contains(s)){
                        invalidConfigured.add(s);
                    }
                }
                if (invalidConfigured.size() > 0) {
                    throw new GeneralException(s_loc.get("invalid-types-excluded-types", invalidConfigured.toString()));
                }
            }
            ((DataCacheManagerImpl)manager).setTypes(_includedTypes, _excludedTypes);
        }
    }

    public void commit(Collection<DataCachePCData> additions, Collection<DataCachePCData> newUpdates,
            Collection<DataCachePCData> existingUpdates, Collection<Object> deletes) {
        // remove all objects in deletes list
        removeAllInternal(deletes);

        // next, add all the new additions
        putAllInternal(additions);
        putAllInternal(newUpdates);

        // possibly add the existing updates, depending on the
        // semantics of the cache, as dictated by recacheUpdates()
        if (recacheUpdates())
            putAllInternal(existingUpdates);

        if (log.isTraceEnabled()) {
            Collection<Object> addIds = new ArrayList<Object>(additions.size());
            Collection<Object> upIds = new ArrayList<Object>(newUpdates.size());
            Collection<Object> exIds = new ArrayList<Object>(existingUpdates.size());

            for (DataCachePCData addition : additions)
                addIds.add(addition.getId());
            for (DataCachePCData newUpdate : newUpdates)
                upIds.add(newUpdate.getId());
            for (DataCachePCData existingUpdate : existingUpdates)
                exIds.add(existingUpdate.getId());

            log.trace(s_loc.get("cache-commit", new Object[]{ addIds, upIds, exIds, deletes }));
        }
    }

    public boolean contains(Object key) {
        DataCachePCData o = getInternal(key);
        if (o != null && o.isTimedOut()) {
            o = null;
            removeInternal(key);
            if (log.isTraceEnabled())
                log.trace(s_loc.get("cache-timeout", key));
        }
        return o != null;
    }

    public BitSet containsAll(Collection<Object> keys) {
        if (keys.isEmpty())
            return EMPTY_BITSET;

        BitSet set = new BitSet(keys.size());
        int i = 0;
        for (Iterator<Object> iter = keys.iterator(); iter.hasNext(); i++)
            if (contains(iter.next()))
                set.set(i);
        return set;
    }

    public DataCachePCData get(Object key) {
        DataCachePCData o = getInternal(key);
        if (o != null && o.isTimedOut()) {
            o = null;
            removeInternal(key);
            if (log.isTraceEnabled())
                log.trace(s_loc.get("cache-timeout", key));
        }
        if (log.isTraceEnabled()) {
            if (o == null)
                log.trace(s_loc.get("cache-miss", key));
            else
                log.trace(s_loc.get("cache-hit", key));
        }

        return o;
    }


    /**
     * Returns the objects for the given key List.
     */
    public Map<Object,DataCachePCData> getAll(List<Object> keys) {
        Map<Object,DataCachePCData> resultMap = new HashMap<Object,DataCachePCData>(keys.size());
        for (Object key : keys)
            resultMap.put(key, get(key));
        return resultMap;
    }

    public DataCachePCData put(DataCachePCData data) {
        DataCachePCData o = putInternal(data.getId(), data);
        if (log.isTraceEnabled())
            log.trace(s_loc.get("cache-put", data.getId()));
        return (o == null || o.isTimedOut()) ? null : o;
    }

    public void update(DataCachePCData data) {
        if (recacheUpdates()) {
            putInternal(data.getId(), data);
        }
    }

    public DataCachePCData remove(Object key) {
        DataCachePCData o = removeInternal(key);
        if (o != null && o.isTimedOut())
            o = null;
        if (log.isTraceEnabled()) {
            if (o == null)
                log.trace(s_loc.get("cache-remove-miss", key));
            else
                log.trace(s_loc.get("cache-remove-hit", key));
        }
        return o;
    }

    public BitSet removeAll(Collection<Object> keys) {
        if (keys.isEmpty())
            return EMPTY_BITSET;

        BitSet set = new BitSet(keys.size());
        int i = 0;
        for (Iterator<Object> iter = keys.iterator(); iter.hasNext(); i++)
            if (remove(iter.next()) != null)
                set.set(i);
        return set;
    }

    /**
     * Remove the objects of the given class from the cache.
     */
    public void removeAll(Class<?> cls, boolean subClasses) {
        removeAllInternal(cls, subClasses);
    }

    public boolean pin(Object key) {
        boolean bool = pinInternal(key);
        if (log.isTraceEnabled()) {
            if (bool)
                log.trace(s_loc.get("cache-pin-hit", key));
            else
                log.trace(s_loc.get("cache-pin-miss", key));
        }
        return bool;
    }

    public BitSet pinAll(Collection<Object> keys) {
        if (keys.isEmpty())
            return EMPTY_BITSET;

        BitSet set = new BitSet(keys.size());
        int i = 0;
        for (Iterator<Object> iter = keys.iterator(); iter.hasNext(); i++)
            if (pin(iter.next()))
                set.set(i);
        return set;
    }

    public void pinAll(Class<?> cls, boolean subs) {
        if (log.isWarnEnabled())
            log.warn(s_loc.get("cache-class-pin", getName()));
    }

    public boolean unpin(Object key) {
        boolean bool = unpinInternal(key);
        if (log.isTraceEnabled()) {
            if (bool)
                log.trace(s_loc.get("cache-unpin-hit", key));
            else
                log.trace(s_loc.get("cache-unpin-miss", key));
        }
        return bool;
    }

    public BitSet unpinAll(Collection<Object> keys) {
        if (keys.isEmpty())
            return EMPTY_BITSET;

        BitSet set = new BitSet(keys.size());
        int i = 0;
        for (Iterator<Object> iter = keys.iterator(); iter.hasNext(); i++)
            if (unpin(iter.next()))
                set.set(i);
        return set;
    }

    public void unpinAll(Class<?> cls, boolean subs) {
        if (log.isWarnEnabled())
            log.warn(s_loc.get("cache-class-unpin", getName()));
    }

    public void clear() {
        clearInternal();
        if (log.isTraceEnabled())
            log.trace(s_loc.get("cache-clear", getName()));
    }

    public void close() {
        close(true);
    }

    protected void close(boolean clear) {
        if (!_closed) {
            if (clear)
                clearInternal();
            _closed = true;
        }
    }

    public boolean isClosed() {
        return _closed;
    }

    public void addExpirationListener(ExpirationListener listen) {
        addListener(listen);
    }

    public boolean removeExpirationListener(ExpirationListener listen) {
        return removeListener(listen);
    }

    public String toString() {
        return "[" + super.toString() + ":" + _name + "]";
    }

    /**
     * This method is part of the {@link RemoteCommitListener} interface. If
     * your cache subclass relies on OpenJPA for clustering support, make it
     * implement <code>RemoteCommitListener</code>. This method will take
     * care of invalidating entries from remote commits.
     */
    public void afterCommit(RemoteCommitEvent event) {
        if (_closed)
            return;

        if (event.getPayloadType() == RemoteCommitEvent.PAYLOAD_EXTENTS) {
            removeAllTypeNamesInternal(event.getUpdatedTypeNames());
            removeAllTypeNamesInternal(event.getDeletedTypeNames());
        } else {
            // drop all the committed OIDs, excepting brand
            // new OIDs. brand new OIDs either won't be in
            // the cache, or if they are, will be more up to date
            removeAllInternal(event.getUpdatedObjectIds());
            removeAllInternal(event.getDeletedObjectIds());
        }
    }

    /**
     * Invoke when a key is removed from this cache. Propagates the
     * expiration event on to all expiration listeners registered
     * with this class.
     */
    protected void keyRemoved(Object key, boolean expired) {
        // Notify any expiration listeners of the expiration.
        if (hasListeners())
            fireEvent(new ExpirationEvent(this, key, expired));

        if (expired && log.isTraceEnabled())
            log.trace(s_loc.get("cache-expired", key));
    }

    /**
     * Return <code>true</code> if updates to data already in the
     * cache (either in {@link #commit} or the {@link #update})
     * should be put back into the cache. Returns false by default.
     */
    protected boolean recacheUpdates() {
        return false;
    }

    /**
     * Return the object for the given oid.
     */
    protected abstract DataCachePCData getInternal(Object oid);

    /**
     * Add the given object to the cache, returning the old object under the
     * given oid.
     */
    protected abstract DataCachePCData putInternal(Object oid,
            DataCachePCData pc);
    
    /**
     * Add all of the given objects to the cache.
     */
    protected void putAllInternal(Collection<DataCachePCData> pcs) {
        for (DataCachePCData pc : pcs) {
            putInternal(pc.getId(), pc);
        }
    }

    /**
     * Remove the object under the given oid from the cache.
     */
    protected abstract DataCachePCData removeInternal(Object oid);

    /**
     * Evict objects in cache by class.
     */
    protected abstract void removeAllInternal(Class<?> cls, boolean subclasses);

    /**
     * Remove all objects under the given oids from the cache.
     */
    protected void removeAllInternal(Collection<Object> oids) {
        for (Object oid : oids)
            removeInternal(oid);
    }

    /**
     * Remove all objects of the given class names from the cache.
     */
    protected void removeAllTypeNamesInternal(Collection<String> classNames) {
        Collection<Class<?>> classes = Caches.addTypesByName(conf, classNames, null);
        if (classes == null)
            return;

        for (Class<?> cls : classes) {
            if (log.isTraceEnabled())
                log.trace(s_loc.get("cache-removeclass", cls.getName()));
            removeAllInternal(cls, false);
        }
    }

    /**
     * Clear the cache.
     */
    protected abstract void clearInternal();

    /**
     * Pin an object to the cache.
     */
    protected abstract boolean pinInternal(Object oid);

    /**
     * Unpin an object from the cache.
     */
    protected abstract boolean unpinInternal(Object oid);
    
    /**
     * 
     */
    public DataCache getPartition(String name, boolean create) {
        if (StringUtils.equals(_name, name))
            return this;
        return null;
    }

    /**
     * 
     */
    public Set<String> getPartitionNames() {
        return Collections.emptySet();
    }
    
    public boolean isPartitioned() {
        return false;
    }
    
     public CacheStatistics getStatistics() {
    	return _stats;
    }

    // ---------- Configurable implementation ----------

    public void setConfiguration(Configuration conf) {
        this.conf = (OpenJPAConfiguration) conf;
        this.log = conf.getLog(OpenJPAConfiguration.LOG_DATACACHE);
    }

    public void startConfiguration() {
    }

    public void endConfiguration() {
        if (_name == null)
            setName(NAME_DEFAULT);
    }

    // ---------- AbstractEventManager implementation ----------

    protected void fireEvent(Object event, Object listener) {
        ExpirationListener listen = (ExpirationListener) listener;
        ExpirationEvent ev = (ExpirationEvent) event;
        try {
            listen.onExpire(ev);
        } catch (Exception e) {
            if (log.isWarnEnabled())
                log.warn(s_loc.get("exp-listener-ex"), e);
		}
	}
    
    public Set<String> getTypes() {
        return _includedTypes;
    }

    public Set<String> getExcludedTypes() {
        return _excludedTypes;
    }

    public void setTypes(Set<String> types) {
        _includedTypes = types;
        if (log.isWarnEnabled())
            log.warn(s_loc.get("recommend_jpa2_caching", new Object[]{"Types", 
            		DataCacheMode.ENABLE_SELECTIVE.toString()}));
    }

    public void setTypes(String types) {
        _includedTypes =
            StringUtils.isEmpty(types) ? null : new HashSet<String>(Arrays.asList(Strings.split(types, ";", 0)));
        if (log.isWarnEnabled())
            log.warn(s_loc.get("recommend_jpa2_caching", new Object[]{"Types", 
            		DataCacheMode.ENABLE_SELECTIVE.toString()}));
    }

    public void setExcludedTypes(Set<String> types) {
        _excludedTypes = types;
        if (log.isWarnEnabled())
            log.warn(s_loc.get("recommend_jpa2_caching", new Object[]{"ExcludeTypes", 
            		DataCacheMode.DISABLE_SELECTIVE.toString()}));
    }

    public void setExcludedTypes(String types) {
        _excludedTypes =
            StringUtils.isEmpty(types) ? null : new HashSet<String>(Arrays.asList(Strings.split(types, ";", 0)));
        if (log.isWarnEnabled())
            log.warn(s_loc.get("recommend_jpa2_caching", new Object[]{"ExcludeTypes", 
            		DataCacheMode.DISABLE_SELECTIVE.toString()}));
    }

    public DataCache selectCache(OpenJPAStateManager sm) {
        return this;
    }
    
    public boolean getEvictOnBulkUpdate(){
        return _evictOnBulkUpdate;
    }
    
    public void setEvictOnBulkUpdate(boolean b){
        _evictOnBulkUpdate = b;
    }
}

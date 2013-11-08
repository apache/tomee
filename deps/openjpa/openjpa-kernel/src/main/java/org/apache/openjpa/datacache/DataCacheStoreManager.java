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
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.openjpa.enhance.PCDataGenerator;
import org.apache.openjpa.kernel.DataCacheRetrieveMode;
import org.apache.openjpa.kernel.DataCacheStoreMode;
import org.apache.openjpa.kernel.DelegatingStoreManager;
import org.apache.openjpa.kernel.FetchConfiguration;
import org.apache.openjpa.kernel.LockLevels;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.kernel.PCState;
import org.apache.openjpa.kernel.StoreContext;
import org.apache.openjpa.kernel.StoreManager;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.util.OpenJPAId;
import org.apache.openjpa.util.OptimisticException;

/**
 * StoreManager proxy that delegates to a data cache when possible.
 *
 * @author Patrick Linskey
 * @nojavadoc
 */
public class DataCacheStoreManager
    extends DelegatingStoreManager {

    // all the state managers changed in this transaction
    private Collection<OpenJPAStateManager> _inserts = null;
    private Map<OpenJPAStateManager, BitSet> _updates = null;
    private Collection<OpenJPAStateManager> _deletes = null;

    // the owning context
    private StoreContext _ctx = null;
    private DataCacheManager _mgr = null;
    // pc data generator
    private PCDataGenerator _gen = null;

    /**
     * Constructor.
     *
     * @param sm the store manager to delegate to
     */
    public DataCacheStoreManager(StoreManager sm) {
        super(sm);
    }

    public void setContext(StoreContext ctx) {
        _ctx = ctx;
        _mgr = ctx.getConfiguration().getDataCacheManagerInstance();
        _gen = _mgr.getPCDataGenerator();
        super.setContext(ctx);
    }

    public void begin() {
        super.begin();
    }

    public void commit() {
        try {
            super.commit();
            updateCaches();
        } finally {
            _inserts = null;
            _updates = null;
            _deletes = null;
        }
    }

    public void rollback() {
        try {
            super.rollback();
        } finally {
            _inserts = null;
            _updates = null;
            _deletes = null;
        }
    }

    /**
     * Evict all members of the given classes.
     */
    private void evictTypes(Collection<Class<?>> classes) {
        if (classes.isEmpty())
            return;

        MetaDataRepository mdr = _ctx.getConfiguration().getMetaDataRepositoryInstance();
        ClassLoader loader = _ctx.getClassLoader();

        DataCache cache;
        for (Class<?> cls : classes) {
            cache = mdr.getMetaData(cls, loader, false).getDataCache();
            if (cache != null && cache.getEvictOnBulkUpdate())
                cache.removeAll(cls, false);
        }
    }

    /**
     * Update all caches with the committed inserts, updates, and deletes.
     */
    private void updateCaches() {
        if(_ctx.getFetchConfiguration().getCacheStoreMode() != DataCacheStoreMode.BYPASS ) { 
            // map each data cache to the modifications we need to perform
            Map<DataCache,Modifications> modMap = null;
            if ((_ctx.getPopulateDataCache() && _inserts != null) || _updates != null || _deletes != null)
                modMap = new HashMap<DataCache,Modifications>();
            Modifications mods;
            DataCachePCData data;
            DataCache cache;

            // create pc datas for inserts
            if (_ctx.getPopulateDataCache() && _inserts != null) {
                for (OpenJPAStateManager sm : _inserts) {
                    cache = _mgr.selectCache(sm);
                    if (cache == null)
                        continue;

                    mods = getModifications(modMap, cache);
                    data = newPCData(sm, cache);
                    data.store(sm);
                    mods.additions.add(new PCDataHolder(data, sm));
                    CacheStatistics stats = cache.getStatistics();
                    if (stats.isEnabled()) {
                        ((CacheStatisticsSPI)stats).newPut(data.getType());
                    }
                }
            }

            // update pcdatas for updates
            if (_updates != null) {
                BitSet fields;
                OpenJPAStateManager sm;
                for (Map.Entry<OpenJPAStateManager, BitSet> entry : _updates.entrySet()) { 
                    sm = entry.getKey();
                    fields = entry.getValue();

                    cache = _mgr.selectCache(sm);
                    if (cache == null) {
                        continue;
                    }

                    // it's ok not to clone the object that we get from the cache,
                    // since we're inside the commit() method, so any modifications
                    // to the underlying cache are valid. If the commit had not
                    // already succeeded, then we'd want to clone the retrieved
                    // object.
                    data = cache.get(sm.getObjectId());
                    mods = getModifications(modMap, cache);

                    // data should always be non-null, since the object is
                    // dirty, but maybe it got dropped from the cache in the
                    // interim
                    if (data == null) {
                        data = newPCData(sm, cache);
                        data.store(sm);
                        mods.newUpdates.add(new PCDataHolder(data, sm));
                    } else {
                        data.store(sm, fields);
                        mods.existingUpdates.add(new PCDataHolder(data, sm));
                    }
                    CacheStatistics stats = cache.getStatistics();
                    if (stats.isEnabled()) {
                        ((CacheStatisticsSPI)stats).newPut(data.getType());
                    }
                }
            }

            // remove pcdatas for deletes
            if (_deletes != null) {
                for (OpenJPAStateManager sm : _deletes) { 
                    cache = _mgr.selectCache(sm);
                    if (cache == null)
                        continue;

                    mods = getModifications(modMap, cache);
                    mods.deletes.add(sm.getObjectId());
                }
            }

            // notify the caches of the changes
            if (modMap != null) {
                for (Map.Entry<DataCache,Modifications> entry : modMap.entrySet()) {
                    cache = entry.getKey();
                    mods = entry.getValue();

                    // make sure we're not caching old versions
                    cache.writeLock();
                    try {
                        cache.commit(
                                transformToVersionSafePCDatas(cache, mods.additions), 
                                transformToVersionSafePCDatas(cache, mods.newUpdates), 
                                transformToVersionSafePCDatas(cache, mods.existingUpdates), 
                                mods.deletes);
                    } finally {
                        cache.writeUnlock();
                    }
                }
            }

            // if we were in largeTransaction mode, then we have recorded
            // the classes of updated/deleted objects and these now need to be
            // evicted
            if (_ctx.isTrackChangesByType()) {
                evictTypes(_ctx.getDeletedTypes());
                evictTypes(_ctx.getUpdatedTypes());
            }

        }
    }

    /**
     * Transforms a collection of {@link PCDataHolder}s that might contain
     * stale instances into a collection of up-to-date {@link DataCachePCData}s.
     */
    private List<DataCachePCData> transformToVersionSafePCDatas(DataCache cache, List<PCDataHolder> holders) {
        List<DataCachePCData> transformed = new ArrayList<DataCachePCData>(holders.size());
        Map<Object,Integer> ids = new HashMap<Object,Integer>(holders.size());
        // this list could be removed if DataCache.getAll() took a Collection
        List<Object> idList = new ArrayList<Object>(holders.size());
        int i = 0;
        for (PCDataHolder holder : holders) {
            ids.put(holder.sm.getObjectId(), i++);
            idList.add(holder.sm.getObjectId());
        }

        Map<Object,DataCachePCData> pcdatas = cache.getAll(idList);
        for (Entry<Object,DataCachePCData> entry : pcdatas.entrySet()) {
            Integer index = ids.get(entry.getKey());
            DataCachePCData oldpc = entry.getValue();
            PCDataHolder holder = holders.get(index);
            if (oldpc != null && compareVersion(holder.sm,
                holder.sm.getVersion(), oldpc.getVersion()) == VERSION_EARLIER)
                continue;
            else
                transformed.add(holder.pcdata);
        }
        return transformed;
    }

    /**
     * Return a {@link Modifications} instance to track modifications
     * to the given cache, creating and caching the instance if it does
     * not already exist in the given map.
     */
    private static Modifications getModifications(Map<DataCache,Modifications> modMap, DataCache cache) {
        Modifications mods = (Modifications) modMap.get(cache);
        if (mods == null) {
            mods = new Modifications();
            modMap.put(cache, mods);
        }
        return mods;
    }

    public boolean exists(OpenJPAStateManager sm, Object edata) {
        DataCache cache = _mgr.selectCache(sm);
        CacheStatistics stats = (cache == null) ? null : cache.getStatistics();
        if (cache != null && !isLocking(null) && cache.contains(sm.getObjectId())){
            if (stats != null && stats.isEnabled()) {
                // delay this call ONLY if stats collection is enabled
                Class<?> cls = sm.getMetaData().getDescribedType();
                ((CacheStatisticsSPI)stats).newGet(cls, false);
            }
            return true;
        }
        // If isLocking(null)==true && cache.contains(..) == true... probably shouldn't count?
        if (stats != null && stats.isEnabled()) {
            // delay this call ONLY if stats collection is enabled
            Class<?> cls = sm.getMetaData().getDescribedType();
            ((CacheStatisticsSPI)stats).newGet(cls, false);
        }
        return super.exists(sm, edata);
    }

    public boolean isCached(List<Object> oids, BitSet edata) {
        // If using partitioned cache, we were and still are broke.
        DataCache cache = _mgr.getSystemDataCache();
        if (cache != null && !isLocking(null)) {
            // BitSet size is not consistent.
            for(int i = 0; i < oids.size(); i++) {
                Object oid = oids.get(i);
                // Only check the cache if we haven't found the current oid.
                if (edata.get(i) == false && cache.contains(oid)) {
                    edata.set(i);
                }
            }
            if (edata.cardinality() == oids.size()){
                return true;
            }
        }

        return super.isCached(oids, edata);
    }

    public boolean syncVersion(OpenJPAStateManager sm, Object edata) {
        DataCache cache = _mgr.selectCache(sm);
        FetchConfiguration fc = sm.getContext().getFetchConfiguration();
        CacheStatistics stats = (cache == null) ? null : cache.getStatistics();
        if (cache == null || sm.isEmbedded() || fc.getCacheRetrieveMode() == DataCacheRetrieveMode.BYPASS) {
            if (stats != null && stats.isEnabled()) {
                ((CacheStatisticsSPI) stats).newGet(sm.getMetaData().getDescribedType(), false);
            }
            return super.syncVersion(sm, edata);
        }
        DataCachePCData data;
        Object version = null;
        data = cache.get(sm.getObjectId());
        if (!isLocking(null) && data != null)
            version = data.getVersion(); 

        // if we have a cached version update from there
        if (version != null) {
            if (stats != null && stats.isEnabled()) {
                ((CacheStatisticsSPI)stats).newGet(data.getType(), true);
            }
            if (!version.equals(sm.getVersion())) {
                sm.setVersion(version);
                return false;
            }
            return true;
        }

        if(stats.isEnabled()){
            Class<?> cls = (data == null) ? sm.getMetaData().getDescribedType() : data.getType();
            ((CacheStatisticsSPI) stats).newGet(cls, false);
        }
        // use data store version
        return super.syncVersion(sm, edata);
    }

    public boolean initialize(OpenJPAStateManager sm, PCState state, FetchConfiguration fetch, Object edata) {
        DataCache cache = _mgr.selectCache(sm);
        if (cache == null) {
            return super.initialize(sm, state, fetch, edata);
        }

        DataCachePCData data = cache.get(sm.getObjectId());
        CacheStatistics stats = cache.getStatistics();
        boolean fromDatabase = false; 
        boolean alreadyCached = data != null; 
        if (sm.isEmbedded() 
         || fetch.getCacheRetrieveMode() == DataCacheRetrieveMode.BYPASS
         || fetch.getCacheStoreMode() == DataCacheStoreMode.REFRESH) {
            // stats -- Skipped reading from the cache, noop
            fromDatabase = super.initialize(sm, state, fetch, edata);
        } else {
            if (alreadyCached && !isLocking(fetch)) {
                if (stats.isEnabled()) {
                    ((CacheStatisticsSPI)stats).newGet(data.getType(), true);
                }
                sm.initialize(data.getType(), state);
                data.load(sm, fetch, edata);
            } else {
                if (!alreadyCached) {
                    if (stats.isEnabled()) {
                        // Get the classname from MetaData... but this won't be right in every case. 
                        ((CacheStatisticsSPI)stats).newGet(sm.getMetaData().getDescribedType(), false);
                    }
                }
                fromDatabase = super.initialize(sm, state, fetch, edata);
            }
        }
        // update cache if the result came from the database and configured to use or refresh the cache.
        boolean updateCache = fromDatabase && _ctx.getPopulateDataCache()
                           && ((fetch.getCacheStoreMode() == DataCacheStoreMode.USE && !alreadyCached)
                            || (fetch.getCacheStoreMode() == DataCacheStoreMode.REFRESH));
        if (updateCache) {
            // It is possible that the "cacheability" of the provided SM changed after hitting the DB. This can happen
            // when we are operating against an Entity that is in some sort of inheritance structure.
            cache = _mgr.selectCache(sm);
            if (cache != null) {
                cacheStateManager(cache, sm, data);
                if (stats.isEnabled()) {
                    ((CacheStatisticsSPI) stats).newPut(sm.getMetaData().getDescribedType());
                }
            }
        }
        return fromDatabase || alreadyCached;
    }
    
    private void cacheStateManager(DataCache cache, OpenJPAStateManager sm, DataCachePCData data) {
        if (sm.isFlushed()) { 
            return;
        }
        // make sure that we're not trying to cache an old version
        cache.writeLock();
        try {
            if (data != null && compareVersion(sm, sm.getVersion(), data.getVersion()) == VERSION_EARLIER) {
                return;
            }

            // cache newly loaded info. It is safe to cache data frorm
            // initialize() because this method is only called upon
            // initial load of the data.
            boolean isNew = data == null;
            if (isNew) {
                data = newPCData(sm, cache);
            }
            data.store(sm);
            if (isNew) { 
                cache.put(data);
            } else {
                cache.update(data);
            }
        } finally {
            cache.writeUnlock();
        }
    }

    public boolean load(OpenJPAStateManager sm, BitSet fields,
        FetchConfiguration fetch, int lockLevel, Object edata) {
        DataCache cache = _mgr.selectCache(sm);

        boolean found = false;
        if (cache == null || sm.isEmbedded() || bypass(fetch, StoreManager.FORCE_LOAD_NONE)) {
            found = super.load(sm, fields, fetch, lockLevel, edata);
            updateDataCache(found, sm, fetch);
            return found;
        }

        CacheStatistics stats = cache.getStatistics();
        DataCachePCData data = cache.get(sm.getObjectId());
        if (lockLevel == LockLevels.LOCK_NONE && !isLocking(fetch) && data != null)
            data.load(sm, fields, fetch, edata);
        if (fields.length() == 0){
            if (stats.isEnabled()) {
                Class<?> cls = (data == null) ? sm.getMetaData().getDescribedType() : data.getType();
                ((CacheStatisticsSPI)stats).newGet(cls, true);
            }
            return true;
        }

        // load from store manager; clone the set of still-unloaded fields
        // so that if the store manager decides to modify it it won't affect us
        found = super.load(sm, (BitSet) fields.clone(), fetch, lockLevel, edata);

        // Get new instance of cache after DB load since it may have changed
        updateDataCache(found, sm, fetch);

        return found;
    }

    /**
     * Updates or inserts and item into the data cache.  If storeMode=USE and not in the cache,
     * the item is inserted.  If storeMode=REFRESH the item is inserted, updated, or if found=false, 
     * removed from the cache.
     * @param found whether the entity was found by the store manager
     * @param sm the state manager
     * @param fetch fetch configuration
     */
    private void updateDataCache(boolean found, OpenJPAStateManager sm, FetchConfiguration fetch) {

        if (!_ctx.getPopulateDataCache() || sm == null || fetch.getCacheStoreMode() == DataCacheStoreMode.BYPASS) {
            return;
        }

        DataCache cache = _mgr.selectCache(sm);
        if (cache == null) {
            return;
        }

        DataCachePCData data = cache.get(sm.getObjectId());
        boolean alreadyCached = data != null;

        if ((fetch.getCacheStoreMode() == DataCacheStoreMode.USE && !alreadyCached) ||
             fetch.getCacheStoreMode() == DataCacheStoreMode.REFRESH) {
            // If not found in the DB and the item is in the cache, and not locking remove the item
            if (!found && data != null && !isLocking(fetch)) {
                cache.remove(sm.getObjectId());
                return;
            }
            // Update or insert the item into the cache
            if (found) {
                cacheStateManager(cache, sm, data);
                CacheStatistics stats = cache.getStatistics();
                if (stats.isEnabled()) {
                    ((CacheStatisticsSPI) stats).newPut(sm.getMetaData().getDescribedType());
                }
            }
        }
    }

    public Collection<Object> loadAll(Collection<OpenJPAStateManager> sms, PCState state, int load,
        FetchConfiguration fetch, Object edata) {
        if (bypass(fetch, load)) {
            return super.loadAll(sms, state, load, fetch, edata);
        }

        Map<OpenJPAStateManager, BitSet> unloaded = null;
        List<OpenJPAStateManager> smList = null;
        Map<DataCache,List<OpenJPAStateManager>> caches = new HashMap<DataCache,List<OpenJPAStateManager>>();
        DataCache cache;
        DataCachePCData data;
        BitSet fields;

        for (OpenJPAStateManager sm : sms) {
            cache = _mgr.selectCache(sm);
            if (cache == null || sm.isEmbedded()) {
                unloaded = addUnloaded(sm, null, unloaded);
                continue;
            }

            if (sm.getManagedInstance() == null
                || load != FORCE_LOAD_NONE
                || sm.getPCState() == PCState.HOLLOW) {
                smList = caches.get(cache);
                if (smList == null) {
                    smList = new ArrayList<OpenJPAStateManager>();
                    caches.put(cache, smList);
                }
                smList.add(sm);
            } else if (!cache.contains(sm.getObjectId()))
                unloaded = addUnloaded(sm, null, unloaded);
        }
        
    for(Entry<DataCache,List<OpenJPAStateManager>> entry : caches.entrySet()){
            cache = entry.getKey();
            smList = entry.getValue();
            List<Object> oidList = new ArrayList<Object>(smList.size());

            for (OpenJPAStateManager sm : smList) {
                oidList.add((OpenJPAId) sm.getObjectId());
            }
            
            Map<Object,DataCachePCData> dataMap = cache.getAll(oidList);

            for (OpenJPAStateManager sm : smList) {
                data = dataMap.get(sm.getObjectId());
                CacheStatistics stats = cache.getStatistics();
                if (sm.getManagedInstance() == null) {
                    if (data != null) {
                        //### the 'data.type' access here probably needs
                        //### to be addressed for bug 511
                        if (stats.isEnabled()) {
                            ((CacheStatisticsSPI) stats).newGet(data.getType(), true);
                        }
                        sm.initialize(data.getType(), state);
                        data.load(sm, fetch, edata);
                    } else {
                        unloaded = addUnloaded(sm, null, unloaded);
                        if (stats.isEnabled()) {
                            ((CacheStatisticsSPI)stats).newGet(sm.getMetaData().getDescribedType(), false);
                        }
                    }
                } else if (load != FORCE_LOAD_NONE
                        || sm.getPCState() == PCState.HOLLOW) {
                    data = cache.get(sm.getObjectId());
                    if (data != null) {
                        // load unloaded fields
                        fields = sm.getUnloaded(fetch);
                        data.load(sm, fields, fetch, edata);
                        if (fields.length() > 0){
                            unloaded = addUnloaded(sm, fields, unloaded);
                            if (stats.isEnabled()) {
                                ((CacheStatisticsSPI)stats).newGet(data.getType(), false);
                            }
                        }else{
                            if (stats.isEnabled()) {
                                ((CacheStatisticsSPI)stats).newGet(data.getType(), true);
                            }
                        }
                    } else{
                        unloaded = addUnloaded(sm, null, unloaded);
                        if (stats.isEnabled()) {
                            ((CacheStatisticsSPI)stats).newGet(sm.getMetaData().getDescribedType(), false);
                        }
                    }
                }
            }
        }

        if (unloaded == null)
            return Collections.emptyList();

        // load with delegate
        Collection<Object> failed = super.loadAll(unloaded.keySet(), state, load, fetch, edata);
        if (!_ctx.getPopulateDataCache())
            return failed;

        // for each loaded instance, merge loaded state into cached data

        boolean isNew;

        for(Map.Entry<OpenJPAStateManager, BitSet> entry : unloaded.entrySet()) { 
            OpenJPAStateManager sm = entry.getKey();
            fields = entry.getValue();

            cache = _mgr.selectCache(sm);
            if (cache == null || sm.isEmbedded() || (failed != null
                && failed.contains(sm.getId())))
                continue;

            // make sure that we're not trying to cache an old version
            cache.writeLock();
            try {
                data = cache.get(sm.getObjectId());
                if (data != null && compareVersion(sm, sm.getVersion(),
                    data.getVersion()) == VERSION_EARLIER)
                    continue;

                isNew = data == null;
                if (isNew)
                    data = newPCData(sm, cache);
                if (fields == null)
                    data.store(sm);
                else
                    data.store(sm, fields);
                if (isNew)
                    cache.put(data);
                else
                    cache.update(data);
                CacheStatistics stats = cache.getStatistics();
                if (stats.isEnabled()) {
                    ((CacheStatisticsSPI)stats).newPut(data.getType());
                }
            } finally {
                cache.writeUnlock();
            }
        }
        return failed;
    }
    
    /**
     * Helper method to add an unloaded instance to the given map.
     */
    private static Map<OpenJPAStateManager, BitSet> addUnloaded(OpenJPAStateManager sm, BitSet fields,
        Map<OpenJPAStateManager, BitSet> unloaded) {
        if (unloaded == null)
            unloaded = new HashMap<OpenJPAStateManager, BitSet>();
        unloaded.put(sm, fields);
        return unloaded;
    }

    public Collection<Exception> flush(Collection<OpenJPAStateManager> states) {
        Collection<Exception> exceps = super.flush(states);

        // if there were errors evict bad instances and don't record changes
        if (!exceps.isEmpty()) {
            for (Exception e : exceps) {
                if (e instanceof OptimisticException)
                    notifyOptimisticLockFailure((OptimisticException) e);
            }
            return exceps;
        }

        // if large transaction mode don't record individual changes
        if (_ctx.isTrackChangesByType())
            return exceps;

        for (OpenJPAStateManager sm : states) {
            if (sm.getPCState() == PCState.PNEW && !sm.isFlushed()) {
                if (_inserts == null) {
                    _inserts = new ArrayList<OpenJPAStateManager>();
                }
                _inserts.add(sm);

                // may have been re-persisted
                if (_deletes != null) {
                    _deletes.remove(sm); 
                }
            } else if (_inserts != null 
                && (sm.getPCState() == PCState.PNEWDELETED 
                || sm.getPCState() == PCState.PNEWFLUSHEDDELETED)) {
                _inserts.remove(sm);
            }
            else if (sm.getPCState() == PCState.PDIRTY) {
                if (_updates == null) {
                    _updates = new HashMap<OpenJPAStateManager, BitSet>();
                }
                _updates.put(sm, sm.getDirty());
            } else if (sm.getPCState() == PCState.PDELETED) {
                if (_deletes == null) {
                    _deletes = new HashSet<OpenJPAStateManager>();
                }
                _deletes.add(sm);
            }
        }
        return Collections.emptyList();
    }

    /**
     * Fire local staleness detection events from the cache the OID (if
     * available) that resulted in an optimistic lock exception iff the
     * version information in the cache matches the version information
     * in the state manager for the failed instance. This means that we
     * will evict data from the cache for records that should have
     * successfully committed according to the data cache but
     * did not. The only predictable reason that could cause this behavior
     * is a concurrent out-of-band modification to the database that was not 
     * communicated to the cache. This logic makes OpenJPA's data cache 
     * somewhat tolerant of such behavior, in that the cache will be cleaned 
     * up as failures occur.
     */
    private void notifyOptimisticLockFailure(OptimisticException e) {
        Object o = e.getFailedObject();
        OpenJPAStateManager sm = _ctx.getStateManager(o);
        if (sm == null)
            return;
        Object oid = sm.getId();
        boolean remove;

        // this logic could be more efficient -- we could aggregate
        // all the cache->oid changes, and then use DataCache.removeAll() 
        // and less write locks to do the mutation.
        DataCache cache = _mgr.selectCache(sm);
        if (cache == null)
            return;

        cache.writeLock();
        try {
            DataCachePCData data = cache.get(oid);
            if (data == null)
                return;

            switch (compareVersion(sm, sm.getVersion(), data.getVersion())) {
                case StoreManager.VERSION_LATER:
                case StoreManager.VERSION_SAME:
                    // This tx's current version is later than or the same as 
                    // the data cache version. In this case, the commit should 
                    // have succeeded from the standpoint of the cache. Remove 
                    // the instance from cache in the hopes that the cache is 
                    // out of sync.
                    remove = true;
                    break;
                case StoreManager.VERSION_EARLIER:
                    // This tx's current version is earlier than the data 
                    // cache version. This is a normal optimistic lock failure. 
                    // Do not clean up the cache; it probably already has the 
                    // right values, and if not, it'll get cleaned up by a tx
                    // that fails in one of the other case statements.
                    remove = false;
                    break;
                case StoreManager.VERSION_DIFFERENT:
                    // The version strategy for the failed object does not
                    // store enough information to optimize for expected
                    // failures. Clean up the cache.
                    remove = true;
                    break;
                default:
                    // Unexpected return value. Remove to be future-proof.
                    remove = true;
                    break;
            }
            if (remove)
                // remove directly instead of via the RemoteCommitListener
                // since we have a write lock here already, so this is more
                // efficient than read-locking and then write-locking later.
                cache.remove(sm.getId());
        } finally {
            cache.writeUnlock();
        }

        // fire off a remote commit stalenesss detection event.
        _ctx.getConfiguration().getRemoteCommitEventManager()
            .fireLocalStaleNotification(oid);
    }
   
    /**
     * Create a new cacheable instance for the given state manager.
     */
    private DataCachePCData newPCData(OpenJPAStateManager sm, DataCache cache) {
        ClassMetaData meta = sm.getMetaData();
        if (_gen != null)
            return (DataCachePCData) _gen.generatePCData(sm.getObjectId(), meta);
        return new DataCachePCDataImpl(sm.fetchObjectId(), meta, cache.getName());
    }

    /**
     * Affirms if a load operation must bypass the L2 cache.
     * If lock is active, always bypass.
     * 
     */
    boolean bypass(FetchConfiguration fetch, int load) {
        // Order of checks are important
        if (isLocking(fetch))
            return true;
        if (_ctx.getConfiguration().getRefreshFromDataCache()) 
            return false;
        if (fetch.getCacheRetrieveMode() == DataCacheRetrieveMode.BYPASS)
            return true;
        if (load == StoreManager.FORCE_LOAD_REFRESH)
            return true;
        return false;
    }

    /**
     * Return whether the context is locking loaded data.
     */
    private boolean isLocking(FetchConfiguration fetch) {
        if (fetch == null)
            fetch = _ctx.getFetchConfiguration();
        return fetch.getReadLockLevel() > LockLevels.LOCK_NONE;
    }  
    
    /**
     * Structure used during the commit process to track cache modifications.
     */
    private static class Modifications {

        public final List<PCDataHolder> additions = new ArrayList<PCDataHolder>();
        public final List<PCDataHolder> newUpdates = new ArrayList<PCDataHolder>();
        public final List<PCDataHolder> existingUpdates = new ArrayList<PCDataHolder>();
        public final List<Object> deletes = new ArrayList<Object>();
    }

    /**
     * Utility structure holds the tuple of cacheable instance and its corresponding state manager. 
     *
     */
    private static class PCDataHolder {

        public final DataCachePCData pcdata;
        public final OpenJPAStateManager sm;

        public PCDataHolder(DataCachePCData pcdata, OpenJPAStateManager sm) {
            this.pcdata = pcdata;
            this.sm = sm;
		}
	}
}


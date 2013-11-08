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

import java.util.Collection;

import org.apache.openjpa.event.RemoteCommitListener;
import org.apache.openjpa.util.CacheMap;

/**
 * A {@link QueryCache} implementation that is optimized for concurrent
 * access. When the cache fill up, values to remove from the cache are chosen
 * randomly. Due to race conditions, it is possible that a get call might not
 * retur a cached instance if that instance is being transferred between
 * internal datastructures.
 *
 * @since 0.4.1
 */
public class ConcurrentQueryCache
    extends AbstractQueryCache
    implements RemoteCommitListener {

    private CacheMap _cache;
    protected boolean _lru = false;
    private int _cacheSize = Integer.MIN_VALUE;
    private int _softRefs = Integer.MIN_VALUE;
    
    /**
     * Returns the underlying {@link CacheMap} that this cache is using.
     * This is not an unmodifiable view on the map, so care should be taken
     * with this reference. Implementations should probably not modify the
     * contents of the cache, but should only use this reference
     * to obtain cache metrics. Additionally, this map may contain
     * expired data. Removal of timed-out data is done in a lazy
     * fashion, so the actual size of the map may be greater than the
     * number of non-expired query results in cache.
     */
    public CacheMap getCacheMap() {
        return _cache;
    }

    /**
     * Returns the maximum number of unpinned objects to keep hard
     * references to.
     */
    public int getCacheSize() {
        return _cache.getCacheSize();
    }

    /**
     * Sets the maximum number of unpinned objects to keep hard
     * references to. If the map contains more unpinned objects than
     * <code>size</code>, then this method will result in the cache
     * flushing old values.
     */
    public void setCacheSize(int size) {
        _cacheSize = size;
    }

    /**
     * Returns the maximum number of unpinned objects to keep soft
     * references to. Defaults to <code>-1</code>.
     */
    public int getSoftReferenceSize() {
        return _cache.getSoftReferenceSize();
    }

    /**
     * Sets the maximum number of unpinned objects to keep soft
     * references to. If the map contains more soft references than
     * <code>size</code>, then this method will result in the cache
     * flushing values.
     */
    public void setSoftReferenceSize(int size) {
        _softRefs = size;
    }

    public void initialize(DataCacheManager mgr) {
        super.initialize(mgr);
        conf.getRemoteCommitEventManager().addInternalListener(this);
        _cache = newCacheMap();
        if (_cacheSize != Integer.MIN_VALUE) {
            _cache.setCacheSize(_cacheSize);
        }
        if (_softRefs != Integer.MIN_VALUE) {
            _cache.setSoftReferenceSize(_softRefs);
        }
    }

    public void writeLock() {
        // delegate actually does nothing, but in case that changes...
        _cache.writeLock();
    }

    public void writeUnlock() {
        // delegate actually does nothing, but in case that changes...
        _cache.writeUnlock();
    }

    /**
     * Return the map to use as an internal cache.
     */
    protected CacheMap newCacheMap() {
        CacheMap res = new CacheMap(_lru);
        
        return res;
    }

    protected QueryResult getInternal(QueryKey qk) {
        return (QueryResult) _cache.get(qk);
    }

    protected QueryResult putInternal(QueryKey qk, QueryResult result) {
        return (QueryResult) _cache.put(qk, result);
    }

    protected QueryResult removeInternal(QueryKey qk) {
        return (QueryResult) _cache.remove(qk);
    }

    protected void clearInternal() {
        _cache.clear();
    }

    protected boolean pinInternal(QueryKey qk) {
        return _cache.pin(qk);
    }

    protected boolean unpinInternal(QueryKey qk) {
        return _cache.unpin(qk);
    }

    protected Collection keySet() {
        return _cache.keySet ();
	}

    /**
     * Returns the eviction policy of the query cache
     */
    public EvictPolicy getEvictPolicy() {
        return super.evictPolicy;
    }
    
    public void setLru(boolean l) {
        _lru = l;
    }

    public boolean getLru() {
        return _lru;
    }
}

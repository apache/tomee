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

import org.apache.openjpa.event.RemoteCommitListener;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.CacheMap;

/**
 * A {@link DataCache} implementation that is optimized for concurrent
 * access. When the cache fills up, values to remove from cache are chosen
 * randomly. Due to race conditions, it is possible that a get call might not
 * return a cached instance if that instance is being transferred between
 * internal datastructures.
 *
 * @since 0.4.0
 */
public class ConcurrentDataCache
    extends AbstractDataCache
    implements RemoteCommitListener {

    private static final Localizer _loc = Localizer.forPackage
        (ConcurrentDataCache.class);

    private CacheMap _cache;
    private int _cacheSize = Integer.MIN_VALUE;
    private int _softRefs = Integer.MIN_VALUE;
    protected boolean _lru = false;
    
    /**
     * Returns the underlying {@link CacheMap} that this cache is using.
     * This is not an unmodifiable view on the map, so care should be taken
     * with this reference. Implementations should probably not modify the
     * contents of the cache, but should only use this reference to
     * obtain cache metrics.
     */
    public CacheMap getCacheMap() {
        return _cache;
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
     * Returns the maximum number of unpinned objects to keep hard
     * references to.
     */
    public int getCacheSize() {
        return _cache.getCacheSize();
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

    /**
     * Returns the maximum number of unpinned objects to keep soft
     * references to. Defaults to <code>-1</code>.
     */
    public int getSoftReferenceSize() {
        return _cache.getSoftReferenceSize();
    }

    public void initialize(DataCacheManager mgr) {
        super.initialize(mgr);
        conf.getRemoteCommitEventManager().addInternalListener(this);
        // Wait to instantiate _cache so that we know the proper value of _cache
        _cache = newCacheMap();
        if (_cacheSize != Integer.MIN_VALUE) {
            _cache.setCacheSize(_cacheSize);
        }
        if (_softRefs != Integer.MIN_VALUE) {
            _cache.setSoftReferenceSize(_softRefs);
        }
    }

    public void unpinAll(Class<?> cls, boolean subs) {
        if (log.isWarnEnabled())
            log.warn(_loc.get("cache-class-unpin-all", getName()));
        unpinAll(_cache.getPinnedKeys());
    }

    public void writeLock() {
        _cache.writeLock();
    }

    public void writeUnlock() {
        _cache.writeUnlock();
    }

    /**
     * Return the map to use as an internal cache; entry expirations must
     * invoke {@link AbstractDataCache#keyRemoved}.
     */
    protected CacheMap newCacheMap() {
        CacheMap res = new CacheMap(_lru) {
            protected void entryRemoved(Object key, Object value, boolean expired) {
                keyRemoved(key, expired);
            }
        };
        
        return res;
    }

    protected DataCachePCData getInternal(Object key) {
        return (DataCachePCData) _cache.get(key);
    }

    protected DataCachePCData putInternal(Object key, DataCachePCData pc) {
        return (DataCachePCData) _cache.put(key, pc);
    }

    protected DataCachePCData removeInternal(Object key) {
        return (DataCachePCData) _cache.remove(key);
    }

    protected void removeAllInternal(Class<?> cls, boolean subs) {
        // The performance in this area can be improved upon, however it seems
        // unlikely that this method will be called in a performance intensive
        // environment. In any event applications can revert to the old behavior
        // by simply calling removeAll().
        _cache.clear();
    }

    protected void clearInternal() {
        _cache.clear();
    }

    protected boolean pinInternal(Object key) {
        return _cache.pin(key);
    }

    protected boolean unpinInternal(Object key) {
        return _cache.unpin (key);
	}
    
    protected boolean recacheUpdates() {
        return true;
    }

    public void setLru(boolean l) {
        _lru = l;
    }

    public boolean getLru() {
        return _lru;
    }
}

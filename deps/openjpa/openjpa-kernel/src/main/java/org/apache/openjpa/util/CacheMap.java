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
package org.apache.openjpa.util;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.iterators.FilterIterator;
import org.apache.commons.collections.iterators.IteratorChain;
import org.apache.openjpa.lib.util.LRUMap;
import org.apache.openjpa.lib.util.ReferenceMap;
import org.apache.openjpa.lib.util.SizedMap;
import org.apache.openjpa.lib.util.concurrent.ConcurrentHashMap;
import org.apache.openjpa.lib.util.concurrent.ConcurrentReferenceHashMap;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Fixed-size map that has ability to pin/unpin entries and move overflow to
 * a backing soft map.
 *
 * @author Patrick Linskey
 * @author Abe White
 */
public class CacheMap
    implements Map {
 
    /**
     * The map for non-expired and non-pinned references.
     */
    protected final SizedMap cacheMap;

    /**
     * The map for expired references.
     */
    protected final SizedMap softMap;

    /**
     * The set of objects pinned into the cache.
     */
    protected final Map pinnedMap;

    // number of pinned values (not including keys not mapped to values)
    private int _pinnedSize = 0;

    private final ReentrantLock _writeLock = new ReentrantLock();
    private final ReentrantLock _readLock;

    /**
     * Create a non-LRU (and therefore highly concurrent) cache map with a
     * size of 1000.
     */
    public CacheMap() {
        this(false, 1000);
    }
    
    /**
     * Create a cache map with a size of 1000.
     * 
     * @param lru if true, create a LRU cache map otherwise a non-LRU map will be created.
     */
    public CacheMap(boolean lru) {
        this(lru, 1000);
    }

    /**
     * Create a cache map with the given properties.
     */
    public CacheMap(boolean lru, int max) {
        this(lru, max, max / 2, .75F);
    }

    /**
     * @deprecated use {@link CacheMap#CacheMap(boolean, int, int, float, int)}
     * instead.
     */
    public CacheMap(boolean lru, int max, int size, float load) {
        this(lru, max, size, load, 16);
    }

    /**
     * Create a cache map with the given properties.
     *
     * @since 1.1.0
     */
    public CacheMap(boolean lru, int max, int size, float load,
        int concurrencyLevel) {
        if (size < 0)
            size = 500;

        softMap = new ConcurrentReferenceHashMap(ReferenceMap.HARD,
            ReferenceMap.SOFT, size, load) {
            public void overflowRemoved(Object key, Object value) {
                softMapOverflowRemoved(key, value);
            }

            public void valueExpired(Object key) {
                softMapValueExpired(key);
            }
        };
        pinnedMap = new ConcurrentHashMap();

        if (!lru) {
            cacheMap = new ConcurrentHashMap(size, load) {
                public void overflowRemoved(Object key, Object value) {
                    cacheMapOverflowRemoved(key, value);
                }
            };
            _readLock = null;
        } else {
            cacheMap = new LRUMap(size, load) {
                public void overflowRemoved(Object key, Object value) {
                    cacheMapOverflowRemoved(key, value);
                }
            };
            _readLock = _writeLock;
        }
        if (max < 0)
            max = Integer.MAX_VALUE;
        cacheMap.setMaxSize(max);
    }

    /**
     * Called from {@link SizedMap#overflowRemoved} in the cache map.
     */
    protected void cacheMapOverflowRemoved(Object key, Object value) {
        if (softMap.size() < softMap.getMaxSize())
            put(softMap, key, value);
        else
            entryRemoved(key, value, true);
    }

    /**
     * Called from {@link SizedMap#overflowRemoved} in the soft map.
     */
    protected void softMapOverflowRemoved(Object key, Object value) {
        entryRemoved(key, value, true);
    }

    /**
     * Called when a value expires from the soft map.
     */
    protected void softMapValueExpired(Object key) {
        entryRemoved(key, null, true);
    }

    /**
     * Put the given entry into the given map. Allows subclasses to
     * take additional actions.
     */
    protected Object put(Map map, Object key, Object value) {
        return map.put(key, value);
    }

    /**
     * Remove the given key from the given map. Allows subclasses to
     * take additional actions.
     */
    protected Object remove(Map map, Object key) {
        return map.remove(key);
    }

    /**
     * Acquire read lock.
     */
    public void readLock() {
        if (_readLock != null)
            _readLock.lock();
    }

    /**
     * Release read lock.
     */
    public void readUnlock() {
        if (_readLock != null)
            _readLock.unlock();
    }

    /**
     * Acquire write lock.
     */
    public void writeLock() {
        _writeLock.lock();
    }

    /**
     * Release write lock.
     */
    public void writeUnlock() {
        _writeLock.unlock();
    }

    /**
     * Whether this cache map uses LRU eviction.
     */
    public boolean isLRU() {
        return _readLock != null;
    }

    /**
     * The maximum number of hard references to maintain, or -1 for no limit.
     */
    public void setCacheSize(int size) {
        writeLock();
        try {
            cacheMap.setMaxSize((size < 0) ? Integer.MAX_VALUE : size);
        } finally {
            writeUnlock();
        }
    }

    /**
     * The maximum number of hard references to maintain, or -1 for no limit.
     */
    public int getCacheSize() {
        int max = cacheMap.getMaxSize();
        return (max == Integer.MAX_VALUE) ? -1 : max;
    }

    /**
     * The maximum number of soft references to maintain, or -1 for no limit.
     */
    public void setSoftReferenceSize(int size) {
        writeLock();
        try {
            softMap.setMaxSize((size < 0) ? Integer.MAX_VALUE : size);
        } finally {
            writeUnlock();
        }
    }

    /**
     * The maximum number of soft references to maintain, or -1 for no limit.
     */
    public int getSoftReferenceSize() {
        int max = softMap.getMaxSize();
        return (max == Integer.MAX_VALUE) ? -1 : max;
    }

    /**
     * The keys pinned into the map.
     */
    public Set getPinnedKeys() {
        readLock();
        try {
            return Collections.unmodifiableSet(pinnedMap.keySet());
        } finally {
            readUnlock();
        }
    }

    /**
     * Locks the given key and its value into the map. Objects pinned into
     * the map are not counted towards the maximum cache size, and are never
     * evicted implicitly. You may pin keys for which no value is in the map.
     *
     * @return true if the givne key's value was pinned; false if no value
     * for the given key is cached
     */
    public boolean pin(Object key) {
        writeLock();
        try {
            // if we don't have a pinned map we need to create one; else if the
            // pinned map already contains the key, nothing to do
            if (pinnedMap.containsKey(key))
                return pinnedMap.get(key) != null;

            // check other maps for key
            Object val = remove(cacheMap, key);
            if (val == null)
                val = remove(softMap, key);

            // pin key
            put(pinnedMap, key, val);
            if (val != null) {
                _pinnedSize++;
                return true;
            }
            return false;
        } finally {
            writeUnlock();
        }
    }

    /**
     * Undo a pinning.
     */
    public boolean unpin(Object key) {
        writeLock();
        try {
            Object val = remove(pinnedMap, key);
            if (val != null) {
                // put back into unpinned cache
                put(key, val);
                _pinnedSize--;
                return true;
            }
            return false;
        } finally {
            writeUnlock();
        }
    }

    /**
     * Invoked when a key-value pair is evicted from this data
     * structure. This is invoked with <code>expired</code> set to
     * <code>true</code> when an object is dropped because of space
     * requirements or through garbage collection of soft references.
     * It is invoked with <code>expired</code> set to <code>false</code>
     * when an object is explicitly removed via the {@link #remove} or
     * {@link #clear} methods. This may be invoked more than once for a
     * given entry.
     *
     * @param value may be null if the value was a soft reference that has
     * been GCd
     * @since 0.2.5.0
     */
    protected void entryRemoved(Object key, Object value, boolean expired) {
    }

    /**
     * Invoked when an entry is added to the cache. This may be invoked
     * more than once for an entry.
     */
    protected void entryAdded(Object key, Object value) {
    }

    public Object get(Object key) {
        readLock();
        try {
            // Check the main map first
            Object  val = cacheMap.get(key);
            if (val == null) {
                // if we find the key in the soft map, move it back into
                // the primary map
                val = softMap.get(key);
                if (val != null){
                    put(key, val);
                }else{
                    val = pinnedMap.get(key);
                }
            }
           
            return val;
        } finally {
            readUnlock();
        }
    }

    public Object put(Object key, Object value) {
        writeLock();
        try {
            // if the key is pinned, just interact directly with the pinned map
            Object val;
            if (pinnedMap.containsKey(key)) {
                val = put(pinnedMap, key, value);
                if (val == null) {
                    _pinnedSize++;
                    entryAdded(key, value);
                } else {
                    entryRemoved(key, val, false);
                    entryAdded(key, value);
                }
                return val;
            }

            // if no hard refs, don't put anything
            if (cacheMap.getMaxSize() == 0)
                return null;

            // otherwise, put the value into the map and clear it from the
            // soft map
            val = put(cacheMap, key, value);
            if (val == null) {
                val = remove(softMap, key);
                if (val == null)
                    entryAdded(key, value);
                else {
                    entryRemoved(key, val, false);
                    entryAdded(key, value);
                }
            } else {
                entryRemoved(key, val, false);
                entryAdded(key, value);
            }
            return val;
        } finally {
            writeUnlock();
        }
    }

    public void putAll(Map map) { 
        putAll(map, true);
    }
    
    public void putAll(Map map, boolean replaceExisting) {
        Map.Entry entry;
        for (Iterator itr = map.entrySet().iterator(); itr.hasNext();) {
            entry = (Map.Entry) itr.next();
            if(replaceExisting || !containsKey(entry.getKey())) { 
                put(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * If <code>key</code> is pinned into the cache, the pin is
     * cleared and the object is removed.
     */
    public Object remove(Object key) {
        writeLock();
        try {
            // if the key is pinned, just interact directly with the
            // pinned map
            Object val;
            if (pinnedMap.containsKey(key)) {
                // re-put with null value; we still want key pinned
                val = put(pinnedMap, key, null);
                if (val != null) {
                    _pinnedSize--;
                    entryRemoved(key, val, false);
                }
                return val;
            }

            val = remove(cacheMap, key);
            if (val == null)
                val = softMap.remove(key);
            if (val != null)
                entryRemoved(key, val, false);

            return val;
        } finally {
            writeUnlock();
        }
    }

    /**
     * Removes pinned objects as well as unpinned ones.
     */
    public void clear() {
        writeLock();
        try {
            notifyEntryRemovals(pinnedMap.entrySet());
            pinnedMap.clear();
            _pinnedSize = 0;

            notifyEntryRemovals(cacheMap.entrySet());
            cacheMap.clear();

            notifyEntryRemovals(softMap.entrySet());
            softMap.clear();
        } finally {
            writeUnlock();
        }
    }

    private void notifyEntryRemovals(Set set) {
        Map.Entry entry;
        for (Iterator itr = set.iterator(); itr.hasNext();) {
            entry = (Map.Entry) itr.next();
            if (entry.getValue() != null)
                entryRemoved(entry.getKey(), entry.getValue(), false);
        }
    }

    public int size() {
        readLock();
        try {
            return _pinnedSize + cacheMap.size() + softMap.size();
        } finally {
            readUnlock();
        }
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public boolean containsKey(Object key) {
        readLock();
        try {
            return cacheMap.containsKey(key) || pinnedMap.get(key) != null || softMap.containsKey(key);
        } finally {
            readUnlock();
        }
    }

    public boolean containsValue(Object val) {
        readLock();
        try {
            return cacheMap.containsValue(val) || pinnedMap.containsValue(val) || softMap.containsValue(val);
        } finally {
            readUnlock();
        }
    }

    public Set keySet() {
        return new KeySet();
    }

    public Collection values() {
        return new ValueCollection();
    }

    public Set entrySet() {
        return new EntrySet();
    }

    public String toString() {
        readLock();
        try {
            return "CacheMap:" + cacheMap.toString() + "::" + softMap.toString();
        } finally {
            readUnlock();
        }
    }

    /**
     * View of the entry set.
     */
    private class EntrySet
        extends AbstractSet {

        public int size() {
            return CacheMap.this.size();
        }

        public boolean add(Object o) {
            Map.Entry entry = (Map.Entry) o;
            put(entry.getKey(), entry.getValue());
            return true;
        }

        public Iterator iterator() {
            return new EntryIterator(EntryIterator.ENTRY);
        }
    }

    /**
     * View of the key set.
     */
    private class KeySet
        extends AbstractSet {

        public int size() {
            return CacheMap.this.size();
        }

        public Iterator iterator() {
            return new EntryIterator(EntryIterator.KEY);
        }
    }

    /**
     * View of the value collection.
     */
    private class ValueCollection
        extends AbstractCollection {

        public int size() {
            return CacheMap.this.size();
        }

        public Iterator iterator() {
            return new EntryIterator(EntryIterator.VALUE);
        }
    }

    /**
     * Iterator over all entries.
     */
    private class EntryIterator
        implements Iterator, Predicate {

        public static final int ENTRY = 0;
        public static final int KEY = 1;
        public static final int VALUE = 2;

        private final IteratorChain _itr = new IteratorChain();
        private final int _type;

        public EntryIterator(int type) {
            _type = type;
            _itr.addIterator(new FilterIterator(getView(pinnedMap), this));
            _itr.addIterator(getView(cacheMap));
            _itr.addIterator(getView(softMap));
        }

        /**
         * Return an iterator over the appropriate view of the given map.
         */
        private Iterator getView(Map m) {
            if (m == null)
                return null;

            switch (_type) {
                case KEY:
                    return m.keySet().iterator();
                case VALUE:
                    return m.values().iterator();
                default:
                    return m.entrySet().iterator();
            }
        }

        public boolean hasNext() {
            return _itr.hasNext();
        }

        public Object next() {
            return _itr.next();
        }

        public void remove() {
            _itr.remove();
        }

        public boolean evaluate(Object obj) {
            switch (_type) {
                case ENTRY:
                    return ((Map.Entry) obj).getValue() != null;
			case VALUE:
				return obj != null;
			default:
				return true;
			}
		}
	}
}


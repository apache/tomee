/**
 *
 * Copyright 2005-2006 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.xbean.propertyeditor;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.ref.ReferenceQueue;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Streamlined version of a WeakIdentityHashMap. Provides Identity semantics with 
 * Weak References to keys. This allows proxies to be GC'ed when no longer referenced
 * by clients. <code>BasicProxymanager.destroyProxy()</code> need not be invoked when a
 * proxy is no longer needed. Note that this is not a full Map implementation. 
 * The iteration and collection capabilities of Map have been discarded to keep the 
 * implementation lightweight.
 * <p>
 * Much of this code was cribbed from the Commons Collection 3.1 implementation of 
 * <code>ReferenceIdentityMap</code> and <code>AbstractReferenceMap</code>.
 */
public class ReferenceIdentityMap implements Map {

    /** The default capacity to use. Always use a power of 2!!! */
    private static final int DEFAULT_CAPACITY = 16;
    /** The default load factor to use */
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;
    /** The maximum capacity allowed */
    private static final int MAXIMUM_CAPACITY = 1 << 30;
    
    /** Load factor, normally 0.75 */
    private float loadFactor;
    /** The size of the map */
    private transient int size;
    /** Map entries */
    private transient ReferenceEntry[] data;
    /** Size at which to rehash */
    private transient int threshold;

    /**
     * ReferenceQueue used to eliminate GC'ed entries.
     */
    private ReferenceQueue purgeQueue;

    public ReferenceIdentityMap() {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        this.data = new ReferenceEntry[DEFAULT_CAPACITY];
        this.threshold = calculateThreshold(DEFAULT_CAPACITY, loadFactor);
        this.purgeQueue = new ReferenceQueue();
    }
    
    /**
     * Gets the size of the map.
     * 
     * @return the size
     */
    public int size() {
        purge();
        return size;
    }

    /**
     * Checks whether the map is currently empty.
     * 
     * @return true if the map is currently size zero
     */
    public boolean isEmpty() {
        purge();
        return (size == 0);
    }

    /**
     * Checks whether the map contains the specified key.
     * 
     * @param key  the key to search for
     * @return true if the map contains the key
     */
    public boolean containsKey(Object key) {
        purge();
        ReferenceEntry entry = getEntry(key);
        if (entry == null) {
            return false;
        }
        return (entry.getValue() != null);
    }

    /**
     * Checks whether the map contains the specified value.
     * 
     * @param value  the value to search for
     * @return true if the map contains the value
     */
    public boolean containsValue(Object value) {
        purge();
        if (value == null || size == 0) {
            return false;
        }
        ReferenceEntry [] table = data;
        for (int i = 0; i < table.length; i++) {
            ReferenceEntry entry = table[i];
            while (entry != null) {
                if (value.equals(entry.getValue())) {
                    return true;
                }
                entry = entry.next;
            }
        }
        return false;
    }

    /**
     * Gets the value mapped to the key specified.
     * 
     * @param key  the key
     * @return the mapped value, null if no match
     */
    public Object get(Object key) {
        purge();
        ReferenceEntry entry = getEntry(key);
        if (entry == null) {
            return null;
        }
        return entry.getValue();
    }


    /**
     * Puts a key-value entry into this map.
     * Neither the key nor the value may be null.
     * 
     * @param key  the key to add, must not be null
     * @param value  the value to add, must not be null
     * @return the value previously mapped to this key, null if none
     */
    public Object put(Object key, Object value) {
        assert key != null: "key is null";
        assert value != null: "value is null";

        purge();

        int hashCode = hash(key);
        int index = hashIndex(hashCode, data.length);
        ReferenceEntry entry = data[index];
        while (entry != null) {
            if (entry.hashCode == hashCode && key == entry.getKey()) {
                return entry.setValue(value);
            }
            entry = entry.next;
        }   
            
        createEntry(index, hashCode, key, value);
        return null;
    }
    
    /**
     * Removes the specified mapping from this map.
     * 
     * @param key  the mapping to remove
     * @return the value mapped to the removed key, null if key not in map
     */
    public Object remove(Object key) {
        if (key == null) {
            return null;
        }
        purge();
        int hashCode = hash(key);
        int index = hashIndex(hashCode, data.length);
        ReferenceEntry entry = data[index];
        ReferenceEntry previous = null;
        while (entry != null) {
            if (entry.hashCode == hashCode && (key == entry.getKey())) {
                Object oldValue = entry.getValue();
                removeEntry(entry, index, previous);
                return oldValue;
            }
            previous = entry;
            entry = entry.next;
        }
        return null;
    }

    /**
     * Clears the map, resetting the size to zero and nullifying references
     * to avoid garbage collection issues.
     */
    public void clear() {
        ReferenceEntry[] data = this.data;
        for (int i = data.length - 1; i >= 0; i--) {
            data[i] = null;
        }
        size = 0;
        while (purgeQueue.poll() != null) {} // drain the queue
    }

    public Collection values() {
        throw new UnsupportedOperationException();
    }

    public void putAll(Map t) {
        throw new UnsupportedOperationException();
    }

    public Set entrySet() {
        throw new UnsupportedOperationException();
    }

    public Set keySet() {
        throw new UnsupportedOperationException();
    }

    // end of public methods
    
    /**
     * Gets the entry mapped to the key specified.
     * @param key  the key
     * @return the entry, null if no match
     */
    private ReferenceEntry getEntry(Object key) {
        if (key == null) {
            return null;
        }
        int hashCode = hash(key);
        ReferenceEntry entry = data[hashIndex(hashCode, data.length)];
        while (entry != null) {
            if (entry.hashCode == hashCode && (key == entry.getKey())) {
                return entry;
            }
            entry = entry.next;
        }
        return null;
    }

    /**
     * Creates a new ReferenceEntry.
     * 
     * @param index the index into the data map
     * @param hashCode  the hash code for the new entry
     * @param key  the key to store
     * @param value  the value to store
     * @return the newly created entry
     */
    private ReferenceEntry createEntry(int index, int hashCode, Object key, Object value) {
        ReferenceEntry newEntry = new ReferenceEntry(this, data[index], hashCode, key, value);
        data[index] = newEntry;
        size++;
        checkCapacity();
        return newEntry;
    }

    /**
     * Removes an entry from the chain stored in a particular index.
     * <p>
     * This implementation removes the entry from the data storage table.
     * The size is not updated.
     * 
     * @param entry  the entry to remove
     * @param hashIndex  the index into the data structure
     * @param previous  the previous entry in the chain
     */
    private void removeEntry(ReferenceEntry entry, int hashIndex, ReferenceEntry previous) {
        if (previous == null) {
            data[hashIndex] = entry.next;
        } else {
            previous.next = entry.next;
        }
        size--;
        entry.next = null;
        entry.clear();
        entry.value = null;
    }
    
    /**
     * Checks the capacity of the map and enlarges it if necessary.
     * <p>
     * This implementation uses the threshold to check if the map needs enlarging
     */
    private void checkCapacity() {
        if (size >= threshold) {
            int newCapacity = data.length * 2;
            if (newCapacity <= MAXIMUM_CAPACITY) {
                ensureCapacity(newCapacity);
            }
        }
    }
    
    /**
     * Changes the size of the data structure to the capacity proposed.
     * 
     * @param newCapacity  the new capacity of the array (a power of two, less or equal to max)
     */
    private void ensureCapacity(int newCapacity) {
        int oldCapacity = data.length;
        if (newCapacity <= oldCapacity) {
            return;
        }

        ReferenceEntry oldEntries[] = data;
        ReferenceEntry newEntries[] = new ReferenceEntry[newCapacity];

        for (int i = oldCapacity - 1; i >= 0; i--) {
            ReferenceEntry entry = oldEntries[i];
            if (entry != null) {
                oldEntries[i] = null;  // gc
                do {
                    ReferenceEntry next = entry.next;
                    int index = hashIndex(entry.hashCode, newCapacity);  
                    entry.next = newEntries[index];
                    newEntries[index] = entry;
                    entry = next;
                } while (entry != null);
            }
        }
        threshold = calculateThreshold(newCapacity, loadFactor);
        data = newEntries;
    }

    /**
     * Calculates the new threshold of the map, where it will be resized.
     * This implementation uses the load factor.
     * 
     * @param newCapacity  the new capacity
     * @param factor  the load factor
     * @return the new resize threshold
     */
    private int calculateThreshold(int newCapacity, float factor) {
        return (int) (newCapacity * factor);
    }

    /**
     * Gets the hash code for the key specified.
     * <p>
     * This implementation uses the identity hash code.
     * 
     * @param key  the key to get a hash code for
     * @return the hash code
     */
    private int hash(Object key) {
        return System.identityHashCode(key);
    }

    /**
     * Gets the index into the data storage for the hashCode specified.
     * This implementation uses the least significant bits of the hashCode.
     * 
     * @param hashCode  the hash code to use
     * @param dataSize  the size of the data to pick a bucket from
     * @return the bucket index
     */
    private int hashIndex(int hashCode, int dataSize) {
        return hashCode & (dataSize - 1);
    }

    // Code that handles WeakReference cleanup... Invoked prior to 
    // any operation accessing the ReferenceEntry array...
    
    /**
     * Purges stale mappings from this map.
     * <p>
     * Note that this method is not synchronized!  Special
     * care must be taken if, for instance, you want stale
     * mappings to be removed on a periodic basis by some
     * background thread.
     */
    private void purge() {
        Reference entryRef = purgeQueue.poll();
        while (entryRef != null) {
            purge(entryRef);
            entryRef = purgeQueue.poll();
        }
    }

    /**
     * Purges the specified reference.
     * 
     * @param purgedEntry the reference to purge
     */
    private void purge(Reference purgedEntry) {
        int hash = ((ReferenceEntry)purgedEntry).hashCode;
        int index = hashIndex(hash, data.length);
        ReferenceEntry previous = null;
        ReferenceEntry currentEntry = data[index];
        while (currentEntry != null) {
            if (currentEntry == purgedEntry) {
                currentEntry.purged();
                if (previous == null) {
                    data[index] = currentEntry.next;
                } else {
                    previous.next = currentEntry.next;
                }
                this.size--;
                return;
            }
            previous = currentEntry;
            currentEntry = currentEntry.next;
        }
    }

    /**
     * Each entry in the Map is represented with a ReferenceEntry.
     * <p>
     * If getKey() or getValue() returns null, it means
     * the mapping is stale and should be removed.
     * 
     * @since Commons Collections 3.1
     */
    private static class ReferenceEntry extends WeakReference {
        /** The next entry in the hash chain */
        private ReferenceEntry next;
        /** The hash code of the key */
        private int hashCode;
        /** The value */
        private Object value;

        /**
         * Creates a new entry object for the ReferenceMap.
         * 
         * @param parent  the parent map
         * @param next  the next entry in the hash bucket
         * @param hashCode  the hash code of the key
         * @param key  the key
         * @param value  the value
         */
        private ReferenceEntry(ReferenceIdentityMap parent, ReferenceEntry next, int hashCode, Object key, Object value) {
            super(key, parent.purgeQueue);
            this.next = next;
            this.hashCode = hashCode;
            this.value = value;
        }

        /**
         * Gets the key from the entry.
         * This method dereferences weak and soft keys and thus may return null.
         * 
         * @return the key, which may be null if it was garbage collected
         */
        private Object getKey() {
            return this.get();
        }

        /**
         * Gets the value from the entry.
         * This method dereferences weak and soft value and thus may return null.
         * 
         * @return the value, which may be null if it was garbage collected
         */
        private Object getValue() {
            return value;
        }

        /**
         * Sets the value of the entry.
         * 
         * @param obj  the object to store
         * @return the previous value
         */
        private Object setValue(Object obj) {
            Object old = getValue();
            value = obj;
            return old;
        }

        /**
         * Purges this entry.
         */
        private void purged() {
            this.clear();
            value = null;
        }
    }
}

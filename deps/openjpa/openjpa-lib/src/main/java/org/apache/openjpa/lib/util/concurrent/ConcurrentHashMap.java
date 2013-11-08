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
package org.apache.openjpa.lib.util.concurrent;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;

import org.apache.openjpa.lib.util.SizedMap;

/**
 * This class implements a HashMap which has limited synchronization.
 * In particular mutators are generally synchronized while accessors
 * are generally not. Additionally the Iterators returned by this
 * class are not "fail-fast", but instead try to continue to iterate
 * over the data structure after changes have been made.
 * The synchronization semantics are built right in to the
 * implementation rather than using a delegating wrapper like the
 * other collection classes do because it wasn't clear to me that the
 * how the two should be seperated or that it would be useful to do
 * so. This can probably be a topic for further debate in the future.
 * This class is based heavily on the HashMap class in the Java
 * collections package.
 */
public class ConcurrentHashMap extends AbstractMap
    implements ConcurrentMap, SizedMap, Cloneable, Serializable {

    /**
     * The default initial capacity - MUST be a power of two.
     */
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    /**
     * The maximum capacity, used if a higher value is implicitly specified
     * by either of the constructors with arguments.
     * MUST be a power of two <= 1<<30.
     */
    private static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * The load fast used when none specified in constructor.
     */
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * Cache of random numbers used in "random" methods, since generating them
     * is expensive. We hope each map changes enough between cycling through
     * this list that the overall effect is random enough.
     */
    static final double[] RANDOMS = new double[1000];

    static {
        Random random = new Random();
        for (int i = 0; i < RANDOMS.length; i++)
            RANDOMS[i] = random.nextDouble();
    }

    /**
     * The table, resized as necessary. Length MUST Always be a power of two.
     */
    private transient Entry[] table;

    /**
     * The number of key-value mappings contained in this identity hash map.
     */
    private transient int size;

    /**
     * The next size value at which to resize(capacity * load factor).
     *
     * @serial
     */
    private int threshold;

    /**
     * The load factor for the hash table.
     *
     * @serial
     */
    private final float loadFactor;

    /**
     * Spread "random" removes and iteration.
     */
    private int randomEntry = 0;

    /**
     * Maximum entries.
     */
    private int maxSize = Integer.MAX_VALUE;

    /**
     * Constructs an empty <tt>ConcurrentHashMap</tt> with the specified initial
     * capacity and load factor.
     *
     * @param initialCapacity The initial capacity.
     * @param loadFactor The load factor.
     * @throws IllegalArgumentException if the initial capacity is negative
     * or the load factor is nonpositive.
     */
    public ConcurrentHashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal initial capacity: " +
                initialCapacity);
        }
        if (initialCapacity > MAXIMUM_CAPACITY)
            initialCapacity = MAXIMUM_CAPACITY;
        if (loadFactor <= 0 || loadFactor > 1) {
            throw new IllegalArgumentException("Illegal load factor: " +
                loadFactor);
        }

        // Find a power of 2 >= initialCapacity
        int capacity = 1;
        while (capacity < initialCapacity) capacity <<= 1;

        this.loadFactor = loadFactor;
        threshold = (int) (capacity * loadFactor);
        table = new Entry[capacity];
    }

    /**
     * Constructs an empty <tt>ConcurrentHashMap</tt> with the specified initial
     * capacity and the default load factor(0.75).
     *
     * @param initialCapacity the initial capacity.
     * @throws IllegalArgumentException if the initial capacity is negative.
     */
    public ConcurrentHashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Constructs an empty <tt>ConcurrentHashMap</tt> with the default initial
     * capacity(16) and the default load factor(0.75).
     */
    public ConcurrentHashMap() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Constructs a new <tt>ConcurrentHashMap</tt> with the same mappings as the
     * specified <tt>Map</tt>. The <tt>ConcurrentHashMap</tt> is created with
     * default load factor(0.75) and an initial capacity sufficient to
     * hold the mappings in the specified <tt>Map</tt>.
     *
     * @param m the map whose mappings are to be placed in this map.
     * @throws NullPointerException if the specified map is null.
     */
    public ConcurrentHashMap(Map m) {
        this(Math.max((int) (m.size() / DEFAULT_LOAD_FACTOR) + 1,
            DEFAULT_INITIAL_CAPACITY), DEFAULT_LOAD_FACTOR);
        putAll(m);
    }

    // internal utilities

    /**
     * Value representing null keys inside tables.
     */
    private static final Object NULL_KEY = new Object();

    /**
     * Returns internal representation for key. Use NULL_KEY if key is null.
     */
    private static Object maskNull(Object key) {
        return (key == null ? NULL_KEY : key);
    }

    /**
     * Returns key represented by specified internal representation.
     */
    private static Object unmaskNull(Object key) {
        return (key == NULL_KEY ? null : key);
    }

    /**
     * Returns a hash code for non-null Object x.
     */
    private static int hash(Object x) {
        int h = x.hashCode();
        return h - (h << 7); // i.e., -127 * h
    }

    /**
     * Check for equality of non-null reference x and possibly-null y.
     */
    private static boolean eq(Object x, Object y) {
        return x == y || x.equals(y);
    }

    /**
     * Returns the current capacity of backing table in this map.
     *
     * @return the current capacity of backing table in this map.
     */
    public final int capacity() {
        return table.length;
    }

    /**
     * Returns the load factor for this map.
     *
     * @return the load factor for this map.
     */
    public final float loadFactor() {
        return loadFactor;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = (maxSize < 0) ? Integer.MAX_VALUE : maxSize;
        if (this.maxSize != Integer.MAX_VALUE)
            removeOverflow(this.maxSize);
    }

    public boolean isFull() {
        return maxSize != Integer.MAX_VALUE && size() >= maxSize;
    }

    public void overflowRemoved(Object key, Object value) {
    }

    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return the number of key-value mappings in this map.
     */
    public final int size() {
        return size;
    }

    /**
     * Returns <tt>true</tt> if this map contains no key-value mappings.
     *
     * @return <tt>true</tt> if this map contains no key-value mappings.
     */
    public final boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns the value to which the specified key is mapped in this identity
     * hash map, or <tt>null</tt> if the map contains no mapping for this key.
     * A return value of <tt>null</tt> does not <i>necessarily</i> indicate
     * that the map contains no mapping for the key; it is also possible that
     * the map explicitly maps the key to <tt>null</tt>. The
     * <tt>containsKey</tt> method may be used to distinguish these two cases.
     *
     * @param key the key whose associated value is to be returned.
     * @return the value to which this map maps the specified key, or
     * <tt>null</tt> if the map contains no mapping for this key.
     * @see #put(Object, Object)
     */
    public Object get(Object key) {
        Entry e = getEntry(key);
        return e == null ? null : e.value;
    }

    /**
     * Returns <tt>true</tt> if this map contains a mapping for the
     * specified key.
     *
     * @param key The key whose presence in this map is to be tested
     * @return <tt>true</tt> if this map contains a mapping for the specified
     * key.
     */
    public final boolean containsKey(Object key) {
        return getEntry(key) != null;
    }

    /**
     * Returns the entry associated with the specified key in the
     * ConcurrentHashMap. Returns null if the ConcurrentHashMap contains no
     * mapping for this key.
     */
    protected Entry getEntry(Object key) {
        Object k = maskNull(key);
        int hash = hash(k);
        Entry[] tab = table;
        for (Entry e = tab[hash & (tab.length - 1)]; e != null; e = e.next) {
            if (e.hash == hash && eq(k, e.key)) return e;
        }
        return null;
    }

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for this key, the old
     * value is replaced.
     *
     * @param key key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     * @return previous value associated with specified key, or <tt>null</tt>
     * if there was no mapping for key. A <tt>null</tt> return can
     * also indicate that the ConcurrentHashMap previously associated
     * <tt>null</tt> with the specified key.
     */
    public Object put(Object key, Object value) {
        Object k = maskNull(key);
        int hash = hash(k);
        synchronized (this) {
            int i = hash & (table.length - 1);

            for (Entry e = table[i]; e != null; e = e.next) {
                if (e.hash == hash && eq(k, e.key)) {
                    Object oldValue = e.value;
                    e.value = value;
                    return oldValue;
                }
            }

            if (maxSize != Integer.MAX_VALUE)
                removeOverflow(maxSize - 1);
            table[i] = createEntry(hash, k, value, table[i]);
            if (size++ >= threshold) resize(2 * table.length);
        }
        return null;
    }

    /**
     * Remove any entries equal to or over the max size.
     */
    private void removeOverflow(int maxSize) {
        while (size > maxSize) {
            Map.Entry entry = removeRandom();
            if (entry == null)
                break;
            overflowRemoved(entry.getKey(), entry.getValue());
        }
    }

    public Object putIfAbsent(Object key, Object value) {
        Object k = maskNull(key);
        int hash = hash(k);
        synchronized (this) {
            int i = hash & (table.length - 1);

            for (Entry e = table[i]; e != null; e = e.next) {
                if (e.hash == hash && eq(k, e.key)) {
                    return e.value;
                }
            }

            if (maxSize != Integer.MAX_VALUE)
                removeOverflow(maxSize - 1);
            table[i] = createEntry(hash, k, value, table[i]);
            if (size++ >= threshold) resize(2 * table.length);
        }
        return null;
    }

    /**
     * Rehashes the contents of this map into a new <tt>ConcurrentHashMap</tt>
     * instance with a larger capacity. This method is called automatically when
     * the number of keys in this map exceeds its capacity and load factor.
     *
     * @param newCapacity the new capacity, MUST be a power of two.
     */
    private void resize(int newCapacity) {
        // assert(newCapacity & -newCapacity) == newCapacity; // power of 2
        Entry[] oldTable = table;
        int oldCapacity = oldTable.length;

        // check if needed
        if (size < threshold || oldCapacity > newCapacity) return;

        Entry[] newTable = new Entry[newCapacity];
        int mask = newCapacity - 1;
        for (int i = oldCapacity; i-- > 0;) {
            for (Entry e = oldTable[i]; e != null; e = e.next) {
                Entry clone = (Entry) e.clone();
                int j = clone.hash & mask;
                clone.next = newTable[j];
                newTable[j] = clone;
            }
        }
        table = newTable;
        threshold = (int) (newCapacity * loadFactor);
    }

    /**
     * Copies all of the mappings from the specified map to this map
     * These mappings will replace any mappings that
     * this map had for any of the keys currently in the specified map.
     *
     * @param t mappings to be stored in this map.
     * @throws NullPointerException if the specified map is null.
     */
    public final synchronized void putAll(Map t) {
        // Expand enough to hold t's elements without resizing.
        int n = t.size();
        if (n == 0) return;
        if (n >= threshold) {
            n = (int) (n / loadFactor + 1);
            if (n > MAXIMUM_CAPACITY) n = MAXIMUM_CAPACITY;
            int capacity = table.length;
            while (capacity < n) capacity <<= 1;
            resize(capacity);
        }

        for (Iterator i = t.entrySet().iterator(); i.hasNext();) {
            Map.Entry e = (Map.Entry) i.next();
            put(e.getKey(), e.getValue());
        }
    }

    /**
     * Removes the mapping for this key from this map if present.
     *
     * @param key key whose mapping is to be removed from the map.
     * @return previous value associated with specified key, or <tt>null</tt>
     * if there was no mapping for key. A <tt>null</tt> return can
     * also indicate that the map previously associated <tt>null</tt>
     * with the specified key.
     */
    public Object remove(Object key) {
        Entry e = removeEntryForKey(key);
        return (e == null ? e : e.value);
    }

    /**
     * Removes and returns the entry associated with the specified key in the
     * ConcurrentHashMap. Returns null if the ConcurrentHashMap contains no
     * mapping for this key.
     */
    private Entry removeEntryForKey(Object key) {
        Object k = maskNull(key);
        int hash = hash(k);
        synchronized (this) {
            int i = hash & (table.length - 1);
            Entry e = table[i];

            if (e == null) return null;
            if (e.hash == hash && eq(k, e.key)) {
                size--;
                table[i] = e.next;
                return e;
            }

            Entry prev = e;
            for (e = e.next; e != null; prev = e, e = e.next) {
                if (e.hash == hash && eq(k, e.key)) {
                    size--;
                    prev.next = e.next;
                    return e;
                }
            }
        }
        return null;
    }

    /**
     * Special version of remove for EntrySet.
     */
    private Entry removeMapping(Object o) {
        if (!(o instanceof Map.Entry)) return null;

        Map.Entry entry = (Map.Entry) o;
        Object k = maskNull(entry.getKey());
        int hash = hash(k);
        synchronized (this) {
            int i = hash & (table.length - 1);
            Entry e = table[i];

            if (e == null) return null;
            if (e.hash == hash && e.equals(entry)) {
                size--;
                table[i] = e.next;
                return e;
            }

            Entry prev = e;
            for (e = e.next; e != null; prev = e, e = e.next) {
                if (e.hash == hash && e.equals(entry)) {
                    size--;
                    prev.next = e.next;
                    return e;
                }
            }
        }
        return null;
    }

    /**
     * Removes all mappings from this map.
     */
    public synchronized void clear() {
        table = new Entry[table.length];
        size = 0;
    }

    /**
     * Return an arbitrary entry index.
     */
    private int randomEntryIndex() {
        if (randomEntry == RANDOMS.length)
            randomEntry = 0;
        return (int) (RANDOMS[randomEntry++] * table.length);
    }

    public Map.Entry removeRandom() {
        if (size == 0)
            return null;

        synchronized (this) {
            int random = randomEntryIndex();
            int index = findEntry(random, random % 2 == 0, false);
            if (index == -1)
                return null;
            Entry rem = table[index];
            table[index] = rem.next;
            size--;
            return rem;
        }
    }

    /**
     * Find the index of the entry nearest the given index, starting in the
     * given direction.
     */
    private int findEntry(int start, boolean forward, boolean searchedOther) {
        if (forward) {
            for (int i = start; i < table.length; i++)
                if (table[i] != null)
                    return i;
            return (searchedOther || start == 0) ? -1
                : findEntry(start - 1, false, true);
        } else {
            for (int i = start; i >= 0; i--)
                if (table[i] != null)
                    return i;
            return (searchedOther || start == table.length - 1) ? -1
                : findEntry(start + 1, true, true);
        }
    }

    public Iterator randomEntryIterator() {
        // pass index so calculated before iterator refs table, in case table
        // gets replace with a larger one
        return new HashIterator(ENTRIES, randomEntryIndex());
    }

    /**
     * Returns <tt>true</tt> if this map maps one or more keys to the
     * specified value.
     *
     * @param value value whose presence in this map is to be tested.
     * @return <tt>true</tt> if this map maps one or more keys to the
     * specified value.
     */
    public final boolean containsValue(Object value) {
        if (value == null) return containsNullValue();

        Entry tab[] = table;
        for (int i = 0; i < tab.length; i++) {
            for (Entry e = tab[i]; e != null; e = e.next) {
                if (value.equals(e.value)) return true;
            }
        }
        return false;
    }

    /**
     * Special-case code for containsValue with null argument
     */
    private boolean containsNullValue() {
        Entry tab[] = table;
        for (int i = 0; i < tab.length; i++) {
            for (Entry e = tab[i]; e != null; e = e.next) {
                if (e.value == null) return true;
            }
        }
        return false;
    }

    /**
     * Returns a shallow copy of this <tt>ConcurrentHashMap</tt> instance: the
     * keys and values themselves are not cloned.
     *
     * @return a shallow copy of this map.
     */
    public final Object clone() {
        return new ConcurrentHashMap(this);
    }

    protected Entry createEntry(int h, Object k, Object v, Entry n) {
        return new Entry(h, k, v, n);
    }

    protected static class Entry implements Map.Entry {

        final Object key;
        Object value;
        final int hash;
        Entry next;

        /**
         * Create new entry.
         */
        protected Entry(int h, Object k, Object v, Entry n) {
            value = v;
            next = n;
            key = k;
            hash = h;
        }

        public Object getKey() {
            return unmaskNull(key);
        }

        public Object getValue() {
            return value;
        }

        public Object setValue(Object newValue) {
            Object oldValue = value;
            value = newValue;
            return oldValue;
        }

        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry)) return false;
            Map.Entry e = (Map.Entry) o;
            Object k1 = getKey();
            Object k2 = e.getKey();
            if (k1 == k2 || (k1 != null && k1.equals(k2))) {
                Object v1 = getValue();
                Object v2 = e.getValue();
                if (v1 == v2 || (v1 != null && v1.equals(v2)))
                    return true;
            }
            return false;
        }

        public int hashCode() {
            return (key == NULL_KEY ? 0 : key.hashCode()) ^
                (value == null ? 0 : value.hashCode());
        }

        public String toString() {
            return getKey() + "=" + getValue();
        }

        protected Object clone() {
            // It is the callers responsibility to set the next field
            // correctly.
            return new Entry(hash, key, value, null);
        }
    }

    // Types of Enumerations/Iterations
    private static final int KEYS = 0;
    private static final int VALUES = 1;
    private static final int ENTRIES = 2;

    /**
     * Map iterator.
     */
    private class HashIterator implements Iterator {

        final Entry[] table = ConcurrentHashMap.this.table;
        final int type;
        int startIndex;
        int stopIndex = 0;
        int index;
        Entry entry = null;
        Entry lastReturned = null;

        HashIterator(int type, int startIndex) {
            this.type = type;
            this.startIndex = startIndex;
            index = startIndex;
        }

        public boolean hasNext() {
            if (entry != null) {
                return true;
            }
            while (index >= stopIndex) {
                if ((entry = table[index--]) != null) {
                    return true;
                }
            }
            if (stopIndex == 0) {
                index = table.length - 1;
                stopIndex = startIndex + 1;
                while (index >= stopIndex) {
                    if ((entry = table[index--]) != null) {
                        return true;
                    }
                }
            }
            return false;
        }

        public Object next() {
            if (!hasNext())
                throw new NoSuchElementException();
            Entry e = lastReturned = entry;
            entry = e.next;
            return type == KEYS ? e.key : (type == VALUES ? e.value : e);
        }

        public void remove() {
            if (lastReturned == null)
                throw new IllegalStateException();
            synchronized (ConcurrentHashMap.this) {
                Entry[] tab = ConcurrentHashMap.this.table;
                int index = (lastReturned.hash & 0x7FFFFFFF) % tab.length;

                for (Entry e = tab[index], prev = null; e != null;
                    prev = e, e = e.next) {
                    if (e == lastReturned) {
                        if (prev == null)
                            tab[index] = e.next;
                        else
                            prev.next = e.next;
                        size--;
                        lastReturned = null;
                        return;
                    }
                }
                throw new Error("Iterated off table when doing remove");
            }
        }
    }

    // Views

    private transient Set entrySet = null;
    private transient Set keySet = null;
    private transient Collection values = null;

    /**
     * Returns a set view of the keys contained in this map. The set is
     * backed by the map, so changes to the map are reflected in the set, and
     * vice-versa. The set supports element removal, which removes the
     * corresponding mapping from this map, via the <tt>Iterator.remove</tt>,
     * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt>, and
     * <tt>clear</tt> operations. It does not support the <tt>add</tt> or
     * <tt>addAll</tt> operations.
     *
     * @return a set view of the keys contained in this map.
     */
    public final Set keySet() {
        Set ks = keySet;
        return (ks != null ? ks : (keySet = new KeySet()));
    }

    private final class KeySet extends AbstractSet {

        public Iterator iterator() {
            return new HashIterator(KEYS, table.length - 1);
        }

        public int size() {
            return size;
        }

        public boolean contains(Object o) {
            return containsKey(o);
        }

        public boolean remove(Object o) {
            return ConcurrentHashMap.this.removeEntryForKey(o) != null;
        }

        public void clear() {
            ConcurrentHashMap.this.clear();
        }
    }

    /**
     * Returns a collection view of the values contained in this map. The
     * collection is backed by the map, so changes to the map are reflected in
     * the collection, and vice-versa. The collection supports element
     * removal, which removes the corresponding mapping from this map, via the
     * <tt>Iterator.remove</tt>, <tt>Collection.remove</tt>,
     * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt> operations.
     * It does not support the <tt>add</tt> or <tt>addAll</tt> operations.
     *
     * @return a collection view of the values contained in this map.
     */
    public final Collection values() {
        Collection vs = values;
        return (vs != null ? vs : (values = new Values()));
    }

    private final class Values extends AbstractCollection {

        public Iterator iterator() {
            return new HashIterator(VALUES, table.length - 1);
        }

        public int size() {
            return size;
        }

        public boolean contains(Object o) {
            return containsValue(o);
        }

        public void clear() {
            ConcurrentHashMap.this.clear();
        }
    }

    /**
     * Returns a collection view of the mappings contained in this map. Each
     * element in the returned collection is a <tt>Map.Entry</tt>. The
     * collection is backed by the map, so changes to the map are reflected in
     * the collection, and vice-versa. The collection supports element
     * removal, which removes the corresponding mapping from the map, via the
     * <tt>Iterator.remove</tt>, <tt>Collection.remove</tt>,
     * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt> operations.
     * It does not support the <tt>add</tt> or <tt>addAll</tt> operations.
     *
     * @return a collection view of the mappings contained in this map.
     * @see Map.Entry
     */
    public final Set entrySet() {
        Set es = entrySet;
        return (es != null ? es : (entrySet = new EntrySet()));
    }

    private final class EntrySet extends AbstractSet {

        public Iterator iterator() {
            return new HashIterator(ENTRIES, table.length - 1);
        }

        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry)) return false;
            Map.Entry e = (Map.Entry) o;
            Entry candidate = getEntry(e.getKey());
            return candidate != null && candidate.equals(e);
        }

        public boolean remove(Object o) {
            return removeMapping(o) != null;
        }

        public int size() {
            return size;
        }

        public void clear() {
            ConcurrentHashMap.this.clear();
        }
    }

    /**
     * Save the state of the <tt>ConcurrentHashMap</tt> instance to a stream
     * (i.e., serialize it).
     *
     * @serialData The <i>capacity</i> of the ConcurrentHashMap(the length of
     * the bucket array) is emitted(int), followed by the <i>size</i> of the
     * ConcurrentHashMap(the number of key-value mappings), followed by the key
     * (Object) and value(Object) for each key-value mapping represented by the
     * ConcurrentHashMap The key-value mappings are emitted in the order that
     * they are returned by <tt>entrySet().iterator()</tt>.
     */
    private void writeObject(ObjectOutputStream s) throws IOException {
        // Write out the threshold, loadfactor, and any hidden stuff
        s.defaultWriteObject();

        // Write out number of buckets
        s.writeInt(table.length);

        // Write out size(number of Mappings)
        s.writeInt(size);
        s.writeInt(maxSize);

        // Write out keys and values(alternating)
        for (Iterator i = entrySet().iterator(); i.hasNext();) {
            Map.Entry e = (Map.Entry) i.next();
            s.writeObject(e.getKey());
            s.writeObject(e.getValue());
        }
    }

    private static final long serialVersionUID = -6452706556724125778L;

    /**
     * Reconstitute the <tt>ConcurrentHashMap</tt> instance from a stream(i.e.,
     * deserialize it).
     */
    private void readObject(ObjectInputStream s)
        throws IOException, ClassNotFoundException {
        // Read in the threshold, loadfactor, and any hidden stuff
        s.defaultReadObject();

        // Read in number of buckets and allocate the bucket array;
        int numBuckets = s.readInt();
        table = new Entry[numBuckets];

        // Read in size(number of Mappings)
        int size = s.readInt();
        
        // read the max size
        maxSize = s.readInt();

        // Read the keys and values, and put the mappings in the
        // ConcurrentHashMap
        for (int i = 0; i < size; i++) {
            Object key = s.readObject();
            Object value = s.readObject();
            put(key, value);
        }
    }
}

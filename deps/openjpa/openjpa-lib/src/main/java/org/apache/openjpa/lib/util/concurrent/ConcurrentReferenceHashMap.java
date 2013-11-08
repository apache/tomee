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

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;

import org.apache.openjpa.lib.util.ReferenceMap;
import org.apache.openjpa.lib.util.SizedMap;

/**
 * This class implements a HashMap which has limited synchronization
 * and reference keys or values(but not both). In particular mutators are
 * generally synchronized while accessors are generally not. Additionally the
 * Iterators returned by this class are not "fail-fast", but instead try to
 * continue to iterate over the data structure after changes have been
 * made. Finally purging of the reference queue is only done inside mutators.
 * Null keys are not supported if keys use references. Null values are not
 * supported if values use references.
 * This class is based heavily on the WeakHashMap class in the Java
 * collections package.
 */
public class ConcurrentReferenceHashMap extends AbstractMap
    implements ConcurrentMap, ReferenceMap, SizedMap, Cloneable {

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
     * The hash table data.
     */
    private transient Entry[] table;

    /**
     * The total number of entries in the hash table.
     */
    private transient int count;

    /**
     * Rehashes the table when count exceeds this threshold.
     */
    private int threshold;

    /**
     * The load factor for the HashMap.
     */
    private float loadFactor;

    /**
     * The key reference type.
     */
    private int keyType;

    /**
     * The value reference type.
     */
    private int valueType;

    /**
     * Reference queue for cleared Entries
     */
    private final ReferenceQueue queue = new ReferenceQueue();

    /**
     * Spread "random" removes and iteration.
     */
    private int randomEntry = 0;

    /**
     * Maximum entries.
     */
    private int maxSize = Integer.MAX_VALUE;

    /**
     * Compare two objects. These might be keys, values, or Entry instances.
     * This implementation uses a normal null-safe object equality algorithm.
     *
     * @since 1.0.0
     */
    protected boolean eq(Object x, Object y) {
        return x == y || (x != null && x.equals(y));
    }

    /**
     * Obtain the hashcode of an object. The object might be a key, a value,
     * or an Entry. This implementation just delegates to
     * {@link Object#hashCode}
     *
     * @since 1.0.0
     */
    protected int hc(Object o) {
        return o == null ? 0 : o.hashCode();
    }

    /**
     * Constructs a new, empty HashMap with the specified initial
     * capacity and the specified load factor.
     *
     * @param keyType the reference type of map keys
     * @param valueType the reference type of map values
     * @param initialCapacity the initial capacity of the HashMap.
     * @param loadFactor a number between 0.0 and 1.0.
     * @throws IllegalArgumentException if neither keys nor values use hard
     * references, if the initial capacity is less than or equal to zero, or if
     * the load factor is less than or equal to zero
     */
    public ConcurrentReferenceHashMap(int keyType, int valueType,
        int initialCapacity, float loadFactor) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal Initial Capacity: " +
                initialCapacity);
        }
        if ((loadFactor > 1) || (loadFactor <= 0)) {
            throw new IllegalArgumentException("Illegal Load factor: " +
                loadFactor);
        }
        if (keyType != HARD && valueType != HARD) {
            throw new IllegalArgumentException("Either keys or values must " +
                "use hard references.");
        }
        this.keyType = keyType;
        this.valueType = valueType;
        this.loadFactor = loadFactor;
        table = new Entry[initialCapacity];
        threshold = (int) (initialCapacity * loadFactor);
    }

    /**
     * Constructs a new, empty HashMap with the specified initial capacity
     * and default load factor.
     *
     * @param keyType the reference type of map keys
     * @param valueType the reference type of map values
     * @param initialCapacity the initial capacity of the HashMap.
     */
    public ConcurrentReferenceHashMap(int keyType, int valueType,
        int initialCapacity) {
        this(keyType, valueType, initialCapacity, 0.75f);
    }

    /**
     * Constructs a new, empty HashMap with a default capacity and load factor.
     *
     * @param keyType the reference type of map keys
     * @param valueType the reference type of map values
     */
    public ConcurrentReferenceHashMap(int keyType, int valueType) {
        this(keyType, valueType, 11, 0.75f);
    }

    /**
     * Constructs a new HashMap with the same mappings as the given
     * Map. The HashMap is created with a capacity of thrice the number
     * of entries in the given Map or 11 (whichever is greater), and a
     * default load factor.
     *
     * @param keyType the reference type of map keys
     * @param valueType the reference type of map values
     */
    public ConcurrentReferenceHashMap(int keyType, int valueType, Map t) {
        this(keyType, valueType, Math.max(3 * t.size(), 11), 0.75f);
        putAll(t);
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
     * Returns the number of key-value mappings in this Map. This
     * result is a snapshot, and may not reflect unprocessed entries
     * that will be removed before next attempted access because they
     * are no longer referenced.
     */
    public int size() {
        return count;
    }

    /**
     * Returns true if this Map contains no key-value mappings. This
     * result is a snapshot, and may not reflect unprocessed entries
     * that will be removed before next attempted access because they
     * are no longer referenced.
     */
    public boolean isEmpty() {
        return count == 0;
    }

    /**
     * Returns true if this HashMap maps one or more keys to the specified
     * value.
     *
     * @param value value whose presence in this Map is to be tested.
     */
    public boolean containsValue(Object value) {
        Entry[] tab = table;

        if (value == null) {
            if (valueType != HARD)
                return false;
            for (int i = tab.length; i-- > 0;)
                for (Entry e = tab[i]; e != null; e = e.getNext())
                    if (e.getValue() == null)
                        return true;
        } else {
            for (int i = tab.length; i-- > 0;)
                for (Entry e = tab[i]; e != null; e = e.getNext())
                    if (eq(value, e.getValue()))
                        return true;
        }
        return false;
    }

    /**
     * Returns true if this HashMap contains a mapping for the specified key.
     *
     * @param key key whose presence in this Map is to be tested.
     */
    public boolean containsKey(Object key) {
        if (key == null && keyType != HARD)
            return false;

        Entry[] tab = table;
        int hash = hc(key);
        int index = (hash & 0x7FFFFFFF) % tab.length;
        for (Entry e = tab[index]; e != null; e = e.getNext())
            if (e.getHash() == hash && eq(key, e.getKey()))
                return true;
        return false;
    }

    /**
     * Returns the value to which this HashMap maps the specified key.
     * Returns null if the HashMap contains no mapping for this key.
     *
     * @param key key whose associated value is to be returned.
     */
    public Object get(Object key) {
        if (key == null && keyType != HARD)
            return null;

        Entry[] tab = table;
        int hash = hc(key);
        int index = (hash & 0x7FFFFFFF) % tab.length;
        for (Entry e = tab[index]; e != null; e = e.getNext())
            if ((e.getHash() == hash) && eq(key, e.getKey()))
                return e.getValue();

        return null;
    }

    /**
     * Rehashes the contents of the HashMap into a HashMap with a
     * larger capacity. This method is called automatically when the
     * number of keys in the HashMap exceeds this HashMap's capacity
     * and load factor.
     */
    private void rehash() {
        int oldCapacity = table.length;
        Entry oldMap[] = table;

        int newCapacity = oldCapacity * 2 + 1;
        Entry newMap[] = new Entry[newCapacity];

        for (int i = oldCapacity; i-- > 0;) {
            for (Entry old = oldMap[i]; old != null;) {
                if ((keyType != HARD && old.getKey() == null)
                    || valueType != HARD && old.getValue() == null) {
                    Entry e = old;
                    old = old.getNext();
                    e.setNext(null);
                    count--;
                } else {
                    Entry e = (Entry) old.clone(queue);
                    old = old.getNext();

                    int index = (e.getHash() & 0x7FFFFFFF) % newCapacity;
                    e.setNext(newMap[index]);
                    newMap[index] = e;
                }
            }
        }

        threshold = (int) (newCapacity * loadFactor);
        table = newMap;
    }

    /**
     * Associates the specified value with the specified key in this HashMap.
     * If the HashMap previously contained a mapping for this key, the old
     * value is replaced.
     *
     * @param key key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     * @return previous value associated with specified key, or null if there
     * was no mapping for key. A null return can also indicate that
     * the HashMap previously associated null with the specified key.
     */
    public Object put(Object key, Object value) {
        if ((key == null && keyType != HARD)
            || (value == null && valueType != HARD))
            throw new IllegalArgumentException("Null references not supported");

        int hash = hc(key);
        synchronized (this) {
            expungeStaleEntries();

            Entry[] tab = table;
            int index = 0;

            index = (hash & 0x7FFFFFFF) % tab.length;
            for (Entry e = tab[index], prev = null; e != null; prev = e,
                e = e.getNext()) {
                if ((e.getHash() == hash) && eq(key, e.getKey())) {
                    Object old = e.getValue();
                    if (valueType == HARD)
                        e.setValue(value);
                    else {
                        e = newEntry(hash, e.getKey(), value, e.getNext());
                        if (prev == null)
                            tab[index] = e;
                        else
                            prev.setNext(e);
                    }
                    return old;
                }
            }

            if (count >= threshold) {
                // Rehash the table if the threshold is exceeded
                rehash();

                tab = table;
                index = (hash & 0x7FFFFFFF) % tab.length;
            }

            if (maxSize != Integer.MAX_VALUE)
                removeOverflow(maxSize - 1);
            tab[index] = newEntry(hash, key, value, tab[index]);
            count++;
        }
        return null;
    }

    /**
     * Creates a new entry.
     */
    private Entry newEntry(int hash, Object key, Object value, Entry next) {
        int refType = (keyType != HARD) ? keyType : valueType;
        switch (refType) {
            case WEAK:
                return new WeakEntry(hash, key, value, refType == keyType, next,
                    queue);
            case SOFT:
                return new SoftEntry(hash, key, value, refType == keyType, next,
                    queue);
            default:
                return new HardEntry(hash, key, value, next);
        }
    }

    /**
     * Remove any entries equal to or over the max size.
     */
    private void removeOverflow(int maxSize) {
        while (count > maxSize) {
            Map.Entry entry = removeRandom();
            if (entry == null)
                break;
            overflowRemoved(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Removes the mapping for this key from this HashMap if present.
     *
     * @param key key whose mapping is to be removed from the Map.
     * @return previous value associated with specified key, or null if there
     * was no mapping for key. A null return can also indicate that
     * the HashMap previously associated null with the specified key.
     */
    public Object remove(Object key) {
        if (key == null && keyType != HARD)
            return null;

        int hash = hc(key);
        synchronized (this) {
            expungeStaleEntries();

            Entry[] tab = table;

            int index = (hash & 0x7FFFFFFF) % tab.length;
            for (Entry e = tab[index], prev = null; e != null;
                prev = e, e = e.getNext()) {
                if ((e.getHash() == hash) && eq(key, e.getKey())) {
                    if (prev != null)
                        prev.setNext(e.getNext());
                        // otherwise put the bucket after us
                    else
                        tab[index] = e.getNext();

                    count--;
                    return e.getValue();
                }
            }
        }
        return null;
    }

    public void removeExpired() {
        synchronized (this) {
            expungeStaleEntries();
        }
    }

    public void keyExpired(Object value) {
    }

    public void valueExpired(Object key) {
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
        synchronized (this) {
            expungeStaleEntries();
            if (count == 0)
                return null;

            int random = randomEntryIndex();
            int index = findEntry(random, random % 2 == 0, false);
            if (index == -1)
                return null;
            Entry rem = table[index];
            table[index] = rem.getNext();
            count--;
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
     * Copies all of the mappings from the specified Map to this HashMap
     * These mappings will replace any mappings that this HashMap had for any
     * of the keys currently in the specified Map.
     *
     * @param t Mappings to be stored in this Map.
     */
    public void putAll(Map t) {
        Iterator i = t.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry e = (Map.Entry) i.next();
            put(e.getKey(), e.getValue());
        }
    }

    /**
     * Removes all mappings from this HashMap.
     */
    public synchronized void clear() {
        // clear out ref queue. We don't need to expunge entries
        // since table is getting cleared.
        while (queue.poll() != null)
            ;
        table = new Entry[table.length];
        count = 0;
        // Allocation of array may have caused GC, which may have caused
        // additional entries to go stale.    Removing these entries from
        // the reference queue will make them eligible for reclamation.
        while (queue.poll() != null)
            ;
    }

    /**
     * Returns a shallow copy of this HashMap. The keys and values
     * themselves are not cloned.
     */
    public synchronized Object clone() {
        try {
            expungeStaleEntries();

            ConcurrentReferenceHashMap t = (ConcurrentReferenceHashMap)
                super.clone();
            t.table = new Entry[table.length];
            for (int i = table.length; i-- > 0;) {
                Entry e = table[i];
                if (e != null) {
                    t.table[i] = (Entry) e.clone(t.queue);
                    e = e.getNext();
                    for (Entry k = t.table[i]; e != null; e = e.getNext()) {
                        k.setNext((Entry) e.clone(t.queue));
                        k = k.getNext();
                    }
                }
            }
            t.keySet = null;
            t.entrySet = null;
            t.values = null;
            return t;
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError();
        }
    }

    // Views

    private transient Set keySet = null;
    private transient Set entrySet = null;
    private transient Collection values = null;

    /**
     * Returns a Set view of the keys contained in this HashMap. The Set is
     * backed by the HashMap, so changes to the HashMap are reflected in the
     * Set, and vice-versa. The Set supports element removal, which removes
     * the corresponding mapping from the HashMap, via the Iterator.remove,
     * Set.remove, removeAll retainAll, and clear operations. It does not
     * support the add or addAll operations.
     */
    public Set keySet() {
        if (keySet == null) {
            keySet = new java.util.AbstractSet() {
                public Iterator iterator() {
                    return new HashIterator(KEYS, table.length - 1);
                }

                public int size() {
                    return count;
                }

                public boolean contains(Object o) {
                    return containsKey(o);
                }

                public boolean remove(Object o) {
                    return ConcurrentReferenceHashMap.this.remove(o) != null;
                }

                public void clear() {
                    ConcurrentReferenceHashMap.this.clear();
                }
            };
        }
        return keySet;
    }

    /**
     * Returns a Collection view of the values contained in this HashMap.
     * The Collection is backed by the HashMap, so changes to the HashMap are
     * reflected in the Collection, and vice-versa. The Collection supports
     * element removal, which removes the corresponding mapping from the
     * HashMap, via the Iterator.remove, Collection.remove, removeAll,
     * retainAll and clear operations. It does not support the add or addAll
     * operations.
     */
    public Collection values() {
        if (values == null) {
            values = new java.util.AbstractCollection() {
                public Iterator iterator() {
                    return new HashIterator(VALUES, table.length - 1);
                }

                public int size() {
                    return count;
                }

                public boolean contains(Object o) {
                    return containsValue(o);
                }

                public void clear() {
                    ConcurrentReferenceHashMap.this.clear();
                }
            };
        }
        return values;
    }

    /**
     * Returns a Collection view of the mappings contained in this HashMap.
     * Each element in the returned collection is a Map.Entry. The Collection
     * is backed by the HashMap, so changes to the HashMap are reflected in the
     * Collection, and vice-versa. The Collection supports element removal,
     * which removes the corresponding mapping from the HashMap, via the
     * Iterator.remove, Collection.remove, removeAll, retainAll and clear
     * operations. It does not support the add or addAll operations.
     *
     * @see Map.Entry
     */
    public Set entrySet() {
        if (entrySet == null) {
            entrySet = new java.util.AbstractSet() {
                public Iterator iterator() {
                    return new HashIterator(ENTRIES, table.length - 1);
                }

                public boolean contains(Object o) {
                    if (!(o instanceof Map.Entry))
                        return false;
                    Map.Entry entry = (Map.Entry) o;
                    Object key = entry.getKey();
                    Entry[] tab = table;
                    int hash = hc(key);
                    int index = (hash & 0x7FFFFFFF) % tab.length;

                    for (Entry e = tab[index]; e != null; e = e.getNext())
                        if (e.getHash() == hash && eq(e, entry))
                            return true;
                    return false;
                }

                public boolean remove(Object o) {
                    if (!(o instanceof Map.Entry))
                        return false;
                    Map.Entry entry = (Map.Entry) o;
                    Object key = entry.getKey();
                    synchronized (ConcurrentReferenceHashMap.this) {
                        Entry[] tab = table;
                        int hash = hc(key);
                        int index = (hash & 0x7FFFFFFF) % tab.length;

                        for (Entry e = tab[index], prev = null; e != null;
                            prev = e, e = e.getNext()) {
                            if (e.getHash() == hash && eq(e, entry)) {
                                if (prev != null)
                                    prev.setNext(e.getNext());
                                else
                                    tab[index] = e.getNext();

                                count--;
                                return true;
                            }
                        }
                        return false;
                    }
                }

                public int size() {
                    return count;
                }

                public void clear() {
                    ConcurrentReferenceHashMap.this.clear();
                }
            };
        }

        return entrySet;
    }

    /**
     * Expunge stale entries from the table.
     */
    private void expungeStaleEntries() {
        Object r;
        while ((r = queue.poll()) != null) {
            Entry entry = (Entry) r;
            int hash = entry.getHash();
            Entry[] tab = table;
            int index = (hash & 0x7FFFFFFF) % tab.length;

            for (Entry e = tab[index], prev = null; e != null;
                prev = e, e = e.getNext()) {
                if (e == entry) {
                    if (prev != null)
                        prev.setNext(e.getNext());
                        // otherwise put the bucket after us
                    else
                        tab[index] = e.getNext();

                    count--;
                    if (keyType == HARD)
                        valueExpired(e.getKey());
                    else
                        keyExpired(e.getValue());
                }
            }
        }
    }

    /**
     * HashMap collision list entry.
     */
    private static interface Entry extends Map.Entry {

        public int getHash();

        public Entry getNext();

        public void setNext(Entry next);

        public Object clone(ReferenceQueue queue);
    }

    /**
     * Hard entry.
     */
    private class HardEntry implements Entry {

        private int hash;
        private Object key;
        private Object value;
        private Entry next;

        HardEntry(int hash, Object key, Object value, Entry next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        public int getHash() {
            return hash;
        }

        public Entry getNext() {
            return next;
        }

        public void setNext(Entry next) {
            this.next = next;
        }

        public Object clone(ReferenceQueue queue) {
            // It is the callers responsibility to set the next field
            // correctly.
            return new HardEntry(hash, key, value, null);
        }

        // Map.Entry Ops

        public Object getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }

        public Object setValue(Object value) {
            Object oldValue = this.value;
            this.value = value;
            return oldValue;
        }

        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry)) return false;
            Map.Entry e = (Map.Entry) o;

            Object k1 = key;
            Object k2 = e.getKey();

            return (k1 == null ? k2 == null : eq(k1, k2)) &&
                (value == null ? e.getValue() == null
                    : eq(value, e.getValue()));
        }

        public int hashCode() {
            return hash ^ (value == null ? 0 : value.hashCode());
        }

        public String toString() {
            return key + "=" + value.toString();
        }
    }

    /**
     * Weak entry.
     */
    private class WeakEntry extends WeakReference implements Entry {

        private int hash;
        private Object hard;
        private boolean keyRef;
        private Entry next;

        WeakEntry(int hash, Object key, Object value, boolean keyRef,
            Entry next, ReferenceQueue queue) {
            super((keyRef) ? key : value, queue);
            this.hash = hash;
            this.hard = (keyRef) ? value : key;
            this.keyRef = keyRef;
            this.next = next;
        }

        public int getHash() {
            return hash;
        }

        public Entry getNext() {
            return next;
        }

        public void setNext(Entry next) {
            this.next = next;
        }

        public Object clone(ReferenceQueue queue) {
            // It is the callers responsibility to set the next field
            // correctly.
            return new WeakEntry(hash, getKey(), getValue(), keyRef, null,
                queue);
        }

        // Map.Entry Ops

        public Object getKey() {
            return (keyRef) ? super.get() : hard;
        }

        public Object getValue() {
            return (keyRef) ? hard : super.get();
        }

        public Object setValue(Object value) {
            if (!keyRef)
                throw new Error("Attempt to reset reference value.");

            Object oldValue = hard;
            hard = value;
            return oldValue;
        }

        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry)) return false;
            Map.Entry e = (Map.Entry) o;
            return eq(getKey(), e.getKey()) && eq(getValue(), e.getValue());
        }

        public int hashCode() {
            Object val = getValue();
            return hash ^ (val == null ? 0 : val.hashCode());
        }

        public String toString() {
            return getKey() + "=" + getValue();
        }
    }

    /**
     * Soft entry.
     */
    private class SoftEntry extends SoftReference implements Entry {

        private int hash;
        private Object hard;
        private boolean keyRef;
        private Entry next;

        SoftEntry(int hash, Object key, Object value, boolean keyRef,
            Entry next, ReferenceQueue queue) {
            super((keyRef) ? key : value, queue);
            this.hash = hash;
            this.hard = (keyRef) ? value : key;
            this.keyRef = keyRef;
            this.next = next;
        }

        public int getHash() {
            return hash;
        }

        public Entry getNext() {
            return next;
        }

        public void setNext(Entry next) {
            this.next = next;
        }

        public Object clone(ReferenceQueue queue) {
            // It is the callers responsibility to set the next field
            // correctly.
            return new SoftEntry(hash, getKey(), getValue(), keyRef, null,
                queue);
        }

        // Map.Entry Ops

        public Object getKey() {
            return (keyRef) ? super.get() : hard;
        }

        public Object getValue() {
            return (keyRef) ? hard : super.get();
        }

        public Object setValue(Object value) {
            if (!keyRef)
                throw new Error("Attempt to reset reference value.");

            Object oldValue = hard;
            hard = value;
            return oldValue;
        }

        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry)) return false;
            Map.Entry e = (Map.Entry) o;
            return eq(getKey(), e.getKey()) && eq(getValue(), e.getValue());
        }

        public int hashCode() {
            Object val = getValue();
            return hash ^ (val == null ? 0 : val.hashCode());
        }

        public String toString() {
            return getKey() + "=" + getValue();
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

        final Entry[] table = ConcurrentReferenceHashMap.this.table;
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
            entry = e.getNext();
            return type == KEYS ? e.getKey()
                : (type == VALUES ? e.getValue() : e);
        }

        public void remove() {
            if (lastReturned == null)
                throw new IllegalStateException();
            synchronized (ConcurrentReferenceHashMap.this) {
                Entry[] tab = ConcurrentReferenceHashMap.this.table;
                int index = (lastReturned.getHash() & 0x7FFFFFFF) % tab.length;

                for (Entry e = tab[index], prev = null; e != null;
                    prev = e, e = e.getNext()) {
                    if (e == lastReturned) {
                        if (prev == null)
                            tab[index] = e.getNext();
                        else
                            prev.setNext(e.getNext());
                        count--;
                        lastReturned = null;
                        return;
                    }
                }
                throw new Error("Iterated off table when doing remove");
            }
        }
    }

    int capacity() {
        return table.length;
    }

    float loadFactor() {
        return loadFactor;
    }
}

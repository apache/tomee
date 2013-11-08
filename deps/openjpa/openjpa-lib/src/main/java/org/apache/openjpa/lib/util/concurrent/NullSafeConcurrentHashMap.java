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

import java.util.concurrent.ConcurrentHashMap;
import java.util.Enumeration;
import java.util.Set;
import java.util.Collection;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.AbstractCollection;
import java.util.Random;
import java.util.HashSet;

import org.apache.commons.collections.set.MapBackedSet;

/**
 * A subclass of {@link ConcurrentHashMap} that allows null keys and values.
 * In exchange, it weakens the contract of {@link #putIfAbsent} and the other
 * concurrent methods added in {@link #ConcurrentHashMap}.
 *
 * @since 1.1.0
 */
public class NullSafeConcurrentHashMap extends ConcurrentHashMap {

    private enum Markers {
        NULL,
        MAP_BACKED_SET_DUMMY_VAL
    }

    // The second argument is used within MapBackedSet as the value for
    // all the key-val pairs that are put into the underlying Map. This
    // is required for our usage since ConcurrentHashMap does not allow
    // null values.
    private Set randomKeys = MapBackedSet.decorate(
        new ConcurrentHashMap(), Markers.MAP_BACKED_SET_DUMMY_VAL);

    private Random random = new Random();

    public NullSafeConcurrentHashMap(int size, float load,
        int concurrencyLevel) {
        super(size, load, concurrencyLevel);
    }

    public NullSafeConcurrentHashMap() {
    }

    /**
     * Returns internal representation for object.
     */
    private static Object maskNull(Object o) {
        return (o == null ? Markers.NULL : o);
    }

    /**
     * Returns object represented by specified internal representation.
     */
    private static Object unmaskNull(Object o) {
        return (o == Markers.NULL ? null : o);
    }

    public Entry removeRandom() {
        // this doesn't just use randomEntryIterator() because that iterator
        // has weaker concurrency guarantees than this method. In particular,
        // this method will continue to attempt to remove random entries even
        // as other threads remove the same entries, whereas the random
        // iterator may return values that have been removed.

        for (Iterator iter = randomKeys.iterator(); iter.hasNext(); ) {
            // randomKeys contains null-masked data
            Object key = iter.next();
            if (key != null && randomKeys.remove(key)) {
                Object val = super.remove(key);
                if (val != null)
                    return new EntryImpl(unmaskNull(key), unmaskNull(val));
            }
        }

        // if randomKeys is empty, fall back to non-random behavior.
        for (Iterator iter = super.keySet().iterator(); iter.hasNext(); ) {
            Object key = iter.next();
            if (key == null)
                continue;
            Object val = super.remove(key);
            if (val != null)
                return new EntryImpl(unmaskNull(key), unmaskNull(val));
        }
        return null;
    }

    /**
     * The returned data structure should not be shared among multiple
     * threads.
     */
    public Iterator<Entry> randomEntryIterator() {
        return new Iterator<Entry>() {

            Iterator randomIter = randomKeys.iterator();
            Iterator nonRandomIter = NullSafeConcurrentHashMap.super.keySet()
                .iterator();

            Set returned = new HashSet();
            Entry next;
            boolean nextSet = false;

            public boolean hasNext() {
                // we've set the next value and we haven't returned it yet
                if (nextSet)
                    return true;

                // compute the next value. If the computation returns null,
                // return false. Else, store the next value and return true.
                Object nextKey;
                Object nextValue;
                if (randomIter.hasNext()) {
                    nextKey = randomIter.next();
                    nextValue = NullSafeConcurrentHashMap.super.get(nextKey);
                    if (nextValue != null) {
                        returned.add(nextKey);
                        next = new EntryImpl(unmaskNull(nextKey),
                            unmaskNull(nextValue));
                        nextSet = true;
                        return true;
                    }
                }

                while (nonRandomIter.hasNext()) {
                    nextKey = nonRandomIter.next();

                    if (returned.contains(nextKey))
                        continue;

                    nextValue = NullSafeConcurrentHashMap.super.get(nextKey);
                    if (nextValue != null) {
                        returned.add(nextKey);
                        next = new EntryImpl(unmaskNull(nextKey),
                            unmaskNull(nextValue));
                        nextSet = true;
                        return true;
                    }
                }
                return false;
            }

            public Entry next() {
                // hasNext() will initialize this.next
                if (!nextSet && !hasNext())
                    return null;

                // if we get here, then we're about to return a next value
                nextSet = false;
                
                if (containsKey(next.getKey()))
                    return next;

                // something has changed since the last iteration (presumably
                // due to multi-threaded access to the underlying data
                // structure); recurse
                return next();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public Object remove(Object key) {
        Object maskedKey = maskNull(key);
        Object val = unmaskNull(super.remove(maskedKey));
        randomKeys.remove(maskedKey);
        return val;
    }

    @Override
    public boolean remove(Object key, Object value) {
        Object maskedKey = maskNull(key);
        boolean val = super.remove(maskedKey, maskNull(value));
        randomKeys.remove(maskedKey);
        return val;
    }

    @Override
    public boolean replace(Object key, Object oldValue, Object newValue) {
        return super.replace(maskNull(key), maskNull(oldValue),
            maskNull(newValue));
    }

    @Override
    public Object replace(Object key, Object value) {
        return unmaskNull(super.replace(maskNull(key), maskNull(value)));
    }

    @Override
    public Object putIfAbsent(Object key, Object value) {
        Object maskedKey = maskNull(key);
        Object superVal = super.putIfAbsent(maskedKey, maskNull(value));
        addRandomKey(maskedKey);
        return unmaskNull(superVal);
    }

    @Override
    public Object put(Object key, Object value) {
        Object maskedKey = maskNull(key);
        Object superVal = super.put(maskedKey, maskNull(value));
        addRandomKey(maskedKey);
        return unmaskNull(superVal);
    }

    /**
     * Potentially adds <code>maskedKey</ccode> to the set of random keys
     * to be removed by {@link #removeRandom()}.
     *
     * @since 1.1.0
     */
    private void addRandomKey(Object maskedKey) {
        // Add one in every three keys to the set. Only do this when
        // there are less than 16 elements in the random key set; this
        // means that the algorithm will be pseudo-random for up to
        // 16 removes (either via removeRandom() or normal remove()
        // calls) that have no intervening put() calls.
        if (random != null && randomKeys.size() < 16 && random.nextInt(10) < 3)
            randomKeys.add(maskedKey);
    }

    @Override
    public Object get(Object key) {
        return unmaskNull(super.get(maskNull(key)));
    }

    @Override
    public boolean containsKey(Object key) {
        return super.containsKey(maskNull(key));
    }

    @Override
    public boolean containsValue(Object value) {
        return super.containsValue(maskNull(value));
    }

    @Override
    public boolean contains(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Enumeration elements() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set entrySet() {
        return new TranslatingSet(super.entrySet()) {
            protected Object unmask(Object internal) {
                final Entry e = (Entry) internal;
                return new Entry() {

                    public Object getKey() {
                        return unmaskNull(e.getKey());
                    }

                    public Object getValue() {
                        return unmaskNull(e.getValue());
                    }

                    public Object setValue(Object value) {
                        return unmaskNull(e.setValue(maskNull(value)));
                    }

                    @Override
                    public int hashCode() {
                        return e.hashCode();
                    }
                };
            }
        };
    }

    @Override
    public Enumeration keys() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set keySet() {
        return new TranslatingSet(super.keySet()) {
            protected Object unmask(Object internal) {
                return unmaskNull(internal);
            }
        };
    }

    @Override
    public Collection values() {
        return new TranslatingCollection(super.values()) {

            protected Object unmask(Object internal) {
                return unmaskNull(internal);
            }
        };
    }

    private abstract class TranslatingSet extends AbstractSet {

        private Set backingSet;

        private TranslatingSet(Set backing) {
            this.backingSet = backing;
        }

        protected abstract Object unmask(Object internal);

        public Iterator iterator() {
            final Iterator iterator = backingSet.iterator();
            return new Iterator() {
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                public Object next() {
                    return unmask(iterator.next());
                }

                public void remove() {
                    iterator.remove();
                }
            };
        }

        public int size() {
            return backingSet.size();
        }
    }

    private abstract class TranslatingCollection extends AbstractCollection {

        private Collection backingCollection;

        private TranslatingCollection(Collection backing) {
            this.backingCollection = backing;
        }

        protected abstract Object unmask(Object internal);

        public Iterator iterator() {
            final Iterator iterator = backingCollection.iterator();
            return new Iterator() {
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                public Object next() {
                    return unmask(iterator.next());
                }

                public void remove() {
                    iterator.remove();
                }
            };
        }

        public int size() {
            return backingCollection.size();
        }
    }

    private class EntryImpl implements Entry {

        final Object key;
        final Object val;

        private EntryImpl(Object key, Object val) {
            this.key = key;
            this.val = val;
        }

        public Object getKey() {
            return key;
        }

        public Object getValue() {
            return val;
        }

        public Object setValue(Object value) {
            throw new UnsupportedOperationException();
        }
    }

    public interface KeyFilter {

        /**
         * @param key may be null
         * @return whether or not <code>key</code> shuold be excluded
         */
        public boolean exclude(Object key);
    }
}

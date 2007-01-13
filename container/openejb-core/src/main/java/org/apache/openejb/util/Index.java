/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.util;

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

/**
 * @version $Revision$ $Date$
 */
public class Index<K,V> extends AbstractMap<K,V> {

    private final IndexEntry<K,V>[] entries;
    private final LinkedHashMap<K,Integer> keyIndicies;
    private final IndexEntrySet entrySet;
    private IndexValueList<V> indexValueList;

    public Index(Map<K,V> map) {
        entries = new IndexEntry[map.size()];
        keyIndicies = new LinkedHashMap<K,Integer>(map.size());

        int i = 0;
        for (Entry<K, V> entry : map.entrySet()) {
            entries[i] = new IndexEntry<K,V>(entry);
            keyIndicies.put(entry.getKey(), new Integer(i));
            i++;
        }

        entrySet = new IndexEntrySet();
    }

    public Index(List<K> keys) {
        entries = new IndexEntry[keys.size()];
        keyIndicies = new LinkedHashMap<K,Integer>(keys.size());
        for (int i = 0; i < keys.size(); i++) {
            K key = keys.get(i);
            entries[i] = new IndexEntry<K,V>(key, null);
            keyIndicies.put(key, new Integer(i));
        }

        entrySet = new IndexEntrySet();
    }

    public Index(K[] keys) {
        entries = new IndexEntry[keys.length];
        keyIndicies = new LinkedHashMap<K,Integer>(keys.length);
        for (int i = 0; i < keys.length; i++) {
            K key = keys[i];
            entries[i] = new IndexEntry<K,V>(key, null);
            keyIndicies.put(key, new Integer(i));
        }

        entrySet = new IndexEntrySet();
    }

    public List<V> values() {
        if (indexValueList == null) {
            indexValueList = new IndexValueList<V>();
        }
        return indexValueList;
    }

    public Set<Entry<K,V>> entrySet() {
        return entrySet;
    }

    public K getKey(int index) {
        if (index < 0 || index >= entries.length) throw new IndexOutOfBoundsException("" + index);
        return entries[index].getKey();
    }

    public V get(int index) {
        if (index < 0 || index >= entries.length) throw new IndexOutOfBoundsException("" + index);
        return entries[index].getValue();
    }

    public V set(int index, V value) {
        if (index < 0 || index >= entries.length) throw new IndexOutOfBoundsException("" + index);
        IndexEntry<K,V> entry = entries[index];
        V oldValue = entry.getValue();
        entry.setValue(value);
        return oldValue;
    }

    public V put(K key, V value) {
        int i = indexOf(key);
        if (i < 0) {
            throw new IllegalArgumentException("Index does not contain this key and new entries cannot be added: " + (K) key);
        }

        IndexEntry<K,V> entry = entries[i];
        V oldValue = entry.getValue();
        entry.setValue(value);
        return oldValue;
    }

    public boolean containsKey(Object key) {
        return keyIndicies.containsKey(key);
    }

    public int indexOf(K key) {
        Integer index = keyIndicies.get(key);
        if (index == null) {
            return -1;
        }
        return index;
    }

    public V get(Object key) {
        int i = indexOf((K) key);
        if (i < 0) {
            return null;
        }

        IndexEntry<K,V> entryMetadata = entries[i];
        V value = entryMetadata.getValue();
        return value;
    }

    public Iterator<V> iterator() {
        return new IndexIterator<V>();
    }

    public ListIterator<V> listIterator() {
        return new IndexListIterator<V>(0);
    }

    public ListIterator<V> listIterator(int index) {
        if (index < 0 || index >= entries.length) throw new IndexOutOfBoundsException("" + index);
        return new IndexListIterator<V>(index);
    }

    public Object[] toArray() {
        return toArray(new Object[entries.length]);
    }

    public static interface ListSet extends List, Set {
    }

    public Object[] toArray(Object values[]) {
        if (values.length < entries.length) {
            values = (Object[]) Array.newInstance(values.getClass().getComponentType(), entries.length);
        }

        for (int i = 0; i < entries.length; i++) {
            IndexEntry indexEntry = entries[i];
            values[i] = indexEntry.getValue();
        }
        return values;
    }

    private static class IndexEntry<K,V> implements Map.Entry<K,V> {
        private final K key;
        private V value;

        private IndexEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        private IndexEntry(Map.Entry<K, V> entry) {
            this.key = entry.getKey();
            this.value = entry.getValue();
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public V setValue(V value) {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }
    }

    private class IndexEntrySet extends AbstractSet {
        public Iterator iterator() {
            return new Iterator() {
                private int index = 0;
                public boolean hasNext() {
                    return index < entries.length;
                }

                public Object next() {
                    return entries[index++];
                }

                public void remove() {
                    throw new UnsupportedOperationException("Index entries cannot be removed");
                }
            };
        }

        public int size() {
            return entries.length;
        }
    }

    private class IndexValueList<E> extends AbstractList<E> {
        public E get(int index) {
            return (E) Index.this.get(index);
        }

        public E set(int index, E element) {
            return (E) Index.this.set(index, (V) element);
        }

        public int size() {
            return Index.this.size();
        }
    }

    private class IndexIterator<E> implements Iterator<E> {
        protected int index;

        public boolean hasNext() {
            return index < entries.length;
        }

        public E next() {
            IndexEntry<K, V> entryMetadata = entries[index++];
            return (E) entryMetadata.getValue();
        }

        public void remove() {
            throw new UnsupportedOperationException("Index entries cannot be removed");
        }
    }

    private class IndexListIterator<E> extends IndexIterator<E> implements ListIterator<E> {
        public IndexListIterator(int index) {
            this.index = index;
        }

        public int nextIndex() {
            return index;
        }

        public boolean hasPrevious() {
            return index > 0;
        }

        public E previous() {
            return (E) entries[--index].getValue();
        }

        public int previousIndex() {
            return index - 1;
        }

        public void set(E o) {
            entries[index].setValue((V) o);
        }

        public void add(E o) {
            throw new UnsupportedOperationException("Entries cannot be added to a Index");
        }

    }
}

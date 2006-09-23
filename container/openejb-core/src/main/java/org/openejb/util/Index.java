/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact info@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.util;

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
import java.util.Collection;

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
        keyIndicies = new LinkedHashMap(map.size());

        int i = 0;
        for (Entry<K, V> entry : map.entrySet()) {
            entries[i] = new IndexEntry<K,V>(entry);
            keyIndicies.put(entry.getKey(), new Integer(i));
            i++;
        }

        entrySet = new IndexEntrySet();
    }

    public Index(K[] keys) {
        entries = new IndexEntry[keys.length];
        keyIndicies = new LinkedHashMap(keys.length);
        for (int i = 0; i < keys.length; i++) {
            K key = keys[i];
            entries[i] = new IndexEntry<K,V>(key, null);
            keyIndicies.put(key, new Integer(i));
        }

        entrySet = new IndexEntrySet();
    }

    public List<V> valuesList() {
        if (indexValueList == null) {
            indexValueList = new IndexValueList<V>();
        }
        return indexValueList;
    }

    public Collection<V> values() {
        return valuesList();
    }

    public Set entrySet() {
        return entrySet;
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
        int i = indexOf((K) key);
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
        Integer index = (Integer) keyIndicies.get(key);
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

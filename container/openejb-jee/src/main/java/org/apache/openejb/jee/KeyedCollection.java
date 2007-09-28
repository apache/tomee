/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.jee;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A KeyedCollection is a light weight wrapper around a Map (LinkedHashMap) values set.
 * When a value is added using the add method of this class a key is obtained for
 * the value using either the provided KeyExtractor, or if no KeyExtractor was
 * provided, the value is cast to Keyable and the getKey() method is called.
 *
 * The underlying Map can be obtainded with the toMap method.  Any changes to this
 * map are directly reflected in this collection.  Additions to the map do not
 * need to implement Keyable, nor do the values need to be keyed using the key
 * returned from the KeyExtractor.getKey(value) or the key returned from the
 * Keyable.getKey() Method.
 */
public class KeyedCollection<K,V> extends AbstractCollection<V> {
    private final KeyExtractor<? extends K, ? super V> keyExtractor;
    private final LinkedHashMap<K,V> map;

    @SuppressWarnings({"unchecked"})
    public KeyedCollection() {
        // NOTE: V must implement Keyable or class cast exception will be thrown on add
        keyExtractor = null;
        map = new LinkedHashMap<K,V>();
    }

    public KeyedCollection(KeyExtractor<? extends K, ? super V> keyExtractor) {
        this.keyExtractor = keyExtractor;
        map = new LinkedHashMap<K,V>();
    }

    @SuppressWarnings({"unchecked"})
    public KeyedCollection(Collection<? extends V> c) {
        if (c instanceof KeyedCollection) {
            KeyedCollection keyedCollection = (KeyedCollection) c;
            // NOTE: if types don't match bad things could happen
            keyExtractor = keyedCollection.keyExtractor;
        } else {
            // NOTE: V must implement Keyable or class cast exception will be thrown on add
            keyExtractor = null;
        }
        map = new LinkedHashMap<K,V>();
        addAll(c);
    }

    @SuppressWarnings({"unchecked"})
    public KeyedCollection(int initialCapacity) {
        // NOTE: V must implement Keyable or class cast exception will be thrown on add
        keyExtractor = null;
        map = new LinkedHashMap<K,V>(initialCapacity);
    }

    /**
     * Get the underlying map used by this collection.
     *
     * Any changes to this
     * map are directly reflected in this collection.  Additions to the map do not
     * need to implement Keyable, nor do the values need to be keyed using the key
     * returned from the KeyExtractor.getKey(value) or the key returned from the
     * Keyable.getKey() Method.
     * @return the indexed contents of this collection
     */
    public Map<K,V> toMap() {
        return map;
    }

    public boolean add(V value) {
        K key = getKey(value);
        V oldValue = map.put(key, value);
        return value != oldValue;
    }

    public Iterator<V> iterator() {
        return map.values().iterator();
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public void clear() {
        map.clear();
    }

    public String toString() {
        return map.toString();
    }

    @SuppressWarnings({"unchecked"})
    protected K getKey(V value) {
        if (keyExtractor == null) {
            return ((Keyable<? extends K>)value).getKey();
        } else {
            return keyExtractor.getKey(value);
        }
    }

}

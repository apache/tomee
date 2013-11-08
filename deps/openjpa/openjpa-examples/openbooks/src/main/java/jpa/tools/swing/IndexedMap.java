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
package jpa.tools.swing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A map with indexed access.
 * The keys are indexed in their order of insertion.
 * The index of a given key is stable. It never changes unless the map is cleared.
 * On <code>remove(k)</code>, the key is not removed, but its value is nullified.
 * Then <code>indexOf(k)</code> will return <code>-1</code>.
 * 
 *  
 * @author Pinaki Poddar
 *
 * @param <K>
 * @param <V>
 */
public class IndexedMap<K,V> implements Map<K, V> {
    private final List<K> _keys = new ArrayList<K>();
    private final List<V> _values = new ArrayList<V>();
    private final Set<K> _nulls = new HashSet<K>();
    
    public void clear() {
        _keys.clear();
        _values.clear();
    }
    
    
    public boolean containsKey(Object key) {
        return _keys.contains(key) && !_nulls.contains(key);
    }
    
    
    public boolean containsValue(Object value) {
        return _values.contains(value);
    }
    
    /**
     * Not supported.
     */
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        throw new UnsupportedOperationException();
    }
    
    
    public V get(Object key) {
        int i = indexOf(key);
        return i == -1 ? null : _values.get(i);
    }
    
    public boolean isEmpty() {
        return (_keys.size() - _nulls.size()) == 0;
    }
    
    public Set<K> keySet() {
        Set<K> result = new HashSet<K>(_keys);
        result.removeAll(_nulls);
        return result;
    }
    
    public V put(K key, V value) {
        int i = _keys.indexOf(key);
        if (i == -1) {
            _keys.add(key);
            _values.add(value);
            return null;
        } else {
            _nulls.remove(key);
            return _values.set(i, value);
        }
    }
    
    public void putAll(Map<? extends K, ? extends V> m) {
        for (K k : m.keySet()) {
            this.put(k, m.get(k));
        }
    }
    
    @SuppressWarnings("unchecked")
    public V remove(Object key) {
        V v = get(key);
        _nulls.add((K)key);
        return v;
    }
    
    public int size() {
        return _keys.size() - _nulls.size();
    }
    
    public Collection<V> values() {
        Collection<V> result = new ArrayList<V>();
        for (int i = 0; i < _values.size(); i++) {
            if (!_nulls.contains(_keys.get(i)))
                result.add(_values.get(i));
        }
        return result;
    }
    
    public int indexOf(Object key) {
        return _keys.indexOf(key);
    }
}

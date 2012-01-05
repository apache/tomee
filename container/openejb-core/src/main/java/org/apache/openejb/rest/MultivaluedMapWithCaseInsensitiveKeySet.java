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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.openejb.rest;

import javax.ws.rs.core.MultivaluedMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class MultivaluedMapWithCaseInsensitiveKeySet<V> implements MultivaluedMap<String, V> {
    private MultivaluedMap<String, V> delegate;

    public MultivaluedMapWithCaseInsensitiveKeySet(MultivaluedMap<String, V> map) {
        delegate = map;
    }

    @Override public void add(String key, V value) {
        delegate.add(key, value);
    }

    @Override public V getFirst(String key) {
        return delegate.getFirst(realKey(key));
    }

    @Override public void putSingle(String key, V value) {
        delegate.putSingle(key, value);
    }

    @Override public int size() {
        return delegate.size();
    }

    @Override public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override public boolean containsKey(Object key) {
        return getInsensitiveKeySet(delegate.keySet()).contains(key.toString());
    }

    @Override public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    @Override public List<V> get(Object key) {
        return delegate.get(realKey(key));
    }

    @Override public List<V> put(String key, List<V> value) {
        return delegate.put(key, value);
    }

    @Override public List<V> remove(Object key) {
        return delegate.remove(realKey(key));
    }

    @Override public void putAll(Map<? extends String, ? extends List<V>> m) {
        delegate.putAll(m);
    }

    @Override public void clear() {
        delegate.clear();
    }

    @Override public Set<String> keySet() {
        return getInsensitiveKeySet(delegate.keySet());
    }

    @Override public Collection<List<V>> values() {
        return delegate.values();
    }

    @Override public Set<Entry<String, List<V>>> entrySet() {
        return delegate.entrySet();
    }

    private static Set<String> getInsensitiveKeySet(Set<String> values) {
        Set<String> set = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        set.addAll(values);
        return set;
    }

    private static Object lowerCase(Object key) {
        if (key instanceof String ) {
            return ((String) key).toLowerCase();
        }
        return key;
    }

    private String realKey(Object key) {
        for (Map.Entry<String, ?> entry : delegate.entrySet()) {
            if (entry.getKey().toLowerCase().equals(lowerCase(key))) {
                return entry.getKey();
            }
        }
        return null;
    }
}

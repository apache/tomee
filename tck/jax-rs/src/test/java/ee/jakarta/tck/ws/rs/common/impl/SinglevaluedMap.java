/*
 * Copyright (c) 2012, 2021 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package ee.jakarta.tck.ws.rs.common.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.ws.rs.core.MultivaluedMap;

public class SinglevaluedMap<K, V> implements MultivaluedMap<K, V> {

  Map<K, V> map = new HashMap<K, V>();

  @Override
  public int size() {
    return map.size();
  }

  @Override
  public boolean isEmpty() {
    return map.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return map.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return map.containsValue(value);
  }

  @Override
  public List<V> get(Object key) {
    return Collections.singletonList(map.get(key));
  }

  @Override
  public List<V> put(K key, List<V> value) {
    V v;
    if (value == null)
      v = map.put(key, null);
    else
      v = map.put(key, value.iterator().next());
    return Collections.singletonList(v);
  }

  @Override
  public List<V> remove(Object key) {
    return Collections.singletonList(map.remove(key));
  }

  @Override
  public void putAll(Map<? extends K, ? extends List<V>> m) {
    for (Entry<? extends K, ? extends List<V>> e : m.entrySet())
      map.put(e.getKey(), e.getValue().iterator().next());
  }

  @Override
  public void clear() {
    map.clear();
  }

  @Override
  public Set<K> keySet() {
    return map.keySet();
  }

  @Override
  public Collection<List<V>> values() {
    Collection<List<V>> col = new ArrayList<List<V>>();
    for (V v : map.values())
      col.add(Collections.singletonList(v));
    return col;
  }

  @Override
  public Set<java.util.Map.Entry<K, List<V>>> entrySet() {
    Map<K, List<V>> adapter = new HashMap<K, List<V>>();
    for (Entry<K, V> e : map.entrySet())
      adapter.put(e.getKey(), Collections.singletonList(e.getValue()));
    return adapter.entrySet();
  }

  @Override
  public void add(K key, V value) {
    map.put(key, value);
  }

  @Override
  public V getFirst(K key) {
    return map.get(key);
  }

  @Override
  public void putSingle(K key, V value) {
    map.put(key, value);
  }

  @Override
  public void addAll(K key, V... value) {
    if (value != null)
      putSingle(key, value[0]);
  }

  @Override
  public void addAll(K arg0, List<V> arg1) {
    if (arg1.iterator().hasNext())
      putSingle(arg0, arg1.iterator().next());
  }

  @Override
  public void addFirst(K arg0, V arg1) {
    putSingle(arg0, arg1);
  }

  @Override
  public boolean equalsIgnoreValueOrder(MultivaluedMap<K, V> arg0) {
    for (java.util.Map.Entry<K, List<V>> set : arg0.entrySet()) {
      if (!map.containsKey(set.getKey()))
        return false;
      if (!map.containsValue(set.getValue().iterator().next()))
        return false;
    }
    return true;
  }
}

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

package org.apache.openejb.config;

import org.apache.openejb.BeanContext;
import org.apache.openejb.jee.KeyedCollection;
import org.apache.openejb.jee.ManagedBean;
import org.apache.openejb.jee.PersistenceContextRef;
import org.apache.openejb.jee.PersistenceContextType;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class CompManagedBean extends ManagedBean {
    public CompManagedBean(String name, Class<BeanContext.Comp> compClass) {
        super(name, compClass);
    }

    @Override
    public Collection<PersistenceContextRef> getPersistenceContextRef() {
        if (persistenceContextRef == null) {
            persistenceContextRef = new NoExtendedKeyedCollection();
        }
        return this.persistenceContextRef;
    }

    @Override
    public Map<String, PersistenceContextRef> getPersistenceContextRefMap() {
        if (persistenceContextRef == null) {
            persistenceContextRef = new NoExtendedKeyedCollection();
        }
        return this.persistenceContextRef.toMap();
    }

    private static class NoExtendedKeyedCollection extends KeyedCollection<String, PersistenceContextRef> {
        @Override
        public boolean add(PersistenceContextRef value) {
            if (!PersistenceContextType.EXTENDED.equals(value.getPersistenceContextType())
                    && !super.contains(value)) {
                return super.add(value);
            }
            return false;
        }

        @Override
        public Map<String, PersistenceContextRef> toMap() {
            return new NoExtendedMap(super.toMap());
        }

        private static class NoExtendedMap implements Map<String, PersistenceContextRef> {
            private Map<String, PersistenceContextRef> delegate;

            public NoExtendedMap(Map<String, PersistenceContextRef> map) {
                delegate = map;
            }

            @Override
            public int size() {
                return delegate.size();
            }

            @Override
            public boolean isEmpty() {
                return delegate.isEmpty();
            }

            @Override
            public boolean containsKey(Object key) {
                return delegate.containsKey(key);
            }

            @Override
            public boolean containsValue(Object value) {
                return delegate.containsValue(value);
            }

            @Override
            public PersistenceContextRef get(Object key) {
                return delegate.get(key);
            }

            @Override
            public PersistenceContextRef put(String key, PersistenceContextRef value) {
                if (!PersistenceContextType.EXTENDED.equals(value.getPersistenceContextType())
                        && !delegate.containsValue(key)) {
                    return delegate.put(key, value);
                }
                return null;
            }

            @Override
            public PersistenceContextRef remove(Object key) {
                return delegate.remove(key);
            }

            @Override
            public void putAll(Map<? extends String, ? extends PersistenceContextRef> m) {
                for (Map.Entry<? extends String, ? extends PersistenceContextRef> entry : m.entrySet()) {
                    put(entry.getKey(), entry.getValue());
                }
            }

            @Override
            public void clear() {
                delegate.clear();
            }

            @Override
            public Set<String> keySet() {
                return delegate.keySet();
            }

            @Override
            public Collection<PersistenceContextRef> values() {
                return delegate.values();
            }

            @Override
            public Set<Entry<String, PersistenceContextRef>> entrySet() {
                return delegate.entrySet();
            }
        }
    }
}

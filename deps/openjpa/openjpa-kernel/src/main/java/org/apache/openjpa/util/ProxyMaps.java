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
package org.apache.openjpa.util;

import java.io.InputStream;
import java.io.ObjectStreamException;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Map;
import java.util.Iterator;
import java.util.Set;

/**
 * Utility methods used by map proxies.
 *
 * @author Abe White
 * @nojavadoc
 */
public class ProxyMaps 
    extends Proxies {

    /**
     * Call before invoking {@link Map#clear} on super.
     */
    public static void beforeClear(ProxyMap map) {
        dirty(map, true);
        Map.Entry entry;
        for (Iterator itr = map.entrySet().iterator(); itr.hasNext();) {
            entry = (Map.Entry) itr.next();
            removed(map, entry.getKey(), true);
            removed(map, entry.getValue(), false);
        }
    }

    /**
     * Override for {@link Map#keySet}.
     */
    public static Set keySet(ProxyMap map) {
        ProxyEntrySet entries = (ProxyEntrySet) map.entrySet();
        entries.setView(ProxyEntrySet.VIEW_KEYS);
        return entries; 
    }

    /**
     * Override for {@link Map#values}.
     */
    public static Collection values(ProxyMap map) {
        ProxyEntrySet entries = (ProxyEntrySet) map.entrySet();
        entries.setView(ProxyEntrySet.VIEW_VALUES);
        return entries; 
    }

    /**
     * Wrap the given entry set in a proxy.
     */
    public static Set afterEntrySet(ProxyMap map, Set entries) {
        return new ProxyEntrySetImpl(map, entries);
    }

    /**
     * Call before invoking {@link Map#get} on super.
     */
    public static boolean beforeGet(ProxyMap map, Object key) {
        assertAllowedType(key, map.getKeyType());
        return map.containsKey(key);
    }

    /**
     * Call after invoking {@link Map#get} on super.
     * The potential change is tracked when the get method is called. This change
     * will not translated to an update statement if the retrieved value 
     * is not dirty. 
     *
     * @param ret the return value from the super's method
     * @param before the return value from {@link #beforeGet}
     * @return the value to return from {@link Map#get}
     */
    public static Object afterGet(ProxyMap map, Object key,
        Object ret, boolean before) {
        if (before) {
            if (map.getChangeTracker() != null)
                ((MapChangeTracker) map.getChangeTracker()).changed(key, ret, 
                    ret);
        }
        return ret;
    }


    /**
     * Call before invoking {@link Map#put} on super.
     */
    public static boolean beforePut(ProxyMap map, Object key, Object value) {
        assertAllowedType(key, map.getKeyType());
        assertAllowedType(value, map.getValueType());
        dirty(map, false);
        return map.containsKey(key);
    }

    /**
     * Call after invoking {@link Map#put} on super.
     *
     * @param ret the return value from the super's method
     * @param before the return value from {@link #beforePut}
     * @return the value to return from {@link Map#put}
     */
    public static Object afterPut(ProxyMap map, Object key, Object value,
        Object ret, boolean before) {
        if (before) {
            if (map.getChangeTracker() != null)
                ((MapChangeTracker) map.getChangeTracker()).changed(key, ret, 
                    value);
            removed(map, ret, false);
        } else if (map.getChangeTracker() != null)
            ((MapChangeTracker) map.getChangeTracker()).added(key, value);
        return ret;
    }

    /**
     * Call before invoking {@link Properties#setProperty} on super.
     */
    public static boolean beforeSetProperty(ProxyMap map, String key, 
        String value) {
        return beforePut(map, key, value);
    }

    /**
     * Call after invoking {@link Properties#setProperty} on super.
     *
     * @param ret the return value from the super's method
     * @param before the return value from {@link #beforeSetProperty}
     * @return the value to return from {@link Properties#setProperty}
     */
    public static Object afterSetProperty(ProxyMap map, String key, 
        String value, Object ret, boolean before) {
        return afterPut(map, key, value, ret, before);
    }

    /**
     * Call before invoking {@link Properties#load} on super.
     */
    public static void beforeLoad(ProxyMap map, InputStream in) {
        dirty(map, true);
    }

    /**
     * Call before invoking {@link Properties#loadXML} on super.
     */
    public static void beforeLoadFromXML(ProxyMap map, InputStream in) {
        dirty(map, true);
    }

    /**
     * Overload for {@link Map#putAll}.
     */
    public static void putAll(ProxyMap map, Map values) {
        Map.Entry entry;
        for (Iterator itr = values.entrySet().iterator(); itr.hasNext();) {
            entry = (Map.Entry) itr.next();
            map.put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Call before invoking {@link Map#remove} on super.
     */
    public static boolean beforeRemove(ProxyMap map, Object key) {
        dirty(map, false);
        return map.containsKey(key);
    }

    /**
     * Call after invoking {@link Map#remove} on super.
     *
     * @param ret the return value from the super's method
     * @param before the return value from {@link #beforeRemove}
     * @return the value to return from {@link Map#remove}
     */
    public static Object afterRemove(ProxyMap map, Object key, Object ret, 
        boolean before) {
        if (before) {
            if (map.getChangeTracker() != null)
                ((MapChangeTracker) map.getChangeTracker()).removed(key, ret);
            removed(map, key, true);
            removed(map, ret, false);
        } 
        return ret;
    }

    /**
     * Marker interface for a proxy entry set.
     */
    public static interface ProxyEntrySet 
        extends Set {

        public static final int VIEW_KEYS = 0;
        public static final int VIEW_VALUES = 1;
        public static final int VIEW_ENTRIES = 2;

        /**
         * Set what entry view this set exposes.
         */
        public void setView(int view);
    }

    /**
     * Dirtying proxy for map entry set.
     */
    private static class ProxyEntrySetImpl
        extends AbstractSet 
        implements ProxyEntrySet {
        
        private final ProxyMap _map;
        private final Set _entries;
        private int _view = VIEW_ENTRIES;

        /**
         * Supply owning map and delegate entry set on construction.
         */
        public ProxyEntrySetImpl(ProxyMap map, Set entries) {
            _map = map;
            _entries = entries; 
        }

        /**
         * View mode.
         */
        public int getView() {
            return _view;
        }

        /**
         * View mode.
         */
        public void setView(int view) {
            _view = view;
        }

        public int size() {
            return _entries.size();
        }

        public boolean remove(Object o) {
            if (_view != VIEW_KEYS)
                throw new UnsupportedOperationException();

            if (!_map.containsKey(o))
                return false;
            _map.remove(o);
            return true;
        }

        public Iterator iterator() {
            final Iterator itr = _entries.iterator();
            return new Iterator() {
                private Map.Entry _last = null;

                public boolean hasNext() {
                    return itr.hasNext();
                }

                public Object next() {
                    _last = (Map.Entry) itr.next();
                    switch (_view) {
                        case VIEW_KEYS:
                            return _last.getKey();
                        case VIEW_VALUES:
                            return _last.getValue();
                        default:
                            return _last;
                    }
                }

                public void remove() {
                    dirty(_map, false);
                    itr.remove();
                    if (_map.getChangeTracker() != null)
                        ((MapChangeTracker) _map.getChangeTracker()).
                            removed(_last.getKey(), _last.getValue());
                    Proxies.removed(_map, _last.getKey(), true);
                    Proxies.removed(_map, _last.getValue(), false);
                }
            };
        }

        protected Object writeReplace()
            throws ObjectStreamException {
            switch (_view) {
                case VIEW_KEYS:
                    return ((Map) _map).keySet();
                case VIEW_VALUES:
                    return ((Map) _map).values();
                default:
                    return ((Map) _map).entrySet();
            }
        }
    }
}

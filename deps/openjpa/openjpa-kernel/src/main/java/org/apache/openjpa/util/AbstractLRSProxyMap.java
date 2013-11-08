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

import java.io.ObjectStreamException;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.iterators.FilterIterator;
import org.apache.commons.collections.iterators.IteratorChain;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.lib.util.Closeable;
import org.apache.openjpa.lib.util.Localizer;

/**
 * A map proxy designed for maps backed by extremely large result sets in
 * which each call to {@link #get} or {@link #containsKey} may perform a
 * database query. Changes to the map are tracked through a
 * {@link ChangeTracker}. This map has the following limitations:
 * <ul>
 * <li>The <code>size</code> method may return {@link Integer#MAX_VALUE}.</li>
 * <li>Null keys and values are not supported.</li>
 * </ul>
 *
 * @author Abe White
 */
public abstract class AbstractLRSProxyMap<K,V>
    implements Map<K,V>, LRSProxy, MapChangeTracker, Predicate {

    private static final int MODE_KEY = 0;
    private static final int MODE_VALUE = 1;
    private static final int MODE_ENTRY = 2;

    private static final Localizer _loc = Localizer.forPackage
        (AbstractLRSProxyMap.class);

    private Class<K> _keyType = null;
    private Class<V> _valueType = null;
    private MapChangeTrackerImpl _ct = null;
    private OpenJPAStateManager _sm = null;
    private int _field = -1;
    private OpenJPAStateManager _origOwner = null;
    private int _origField = -1;
    private Map<K,V> _map = null;
    private int _count = -1;
    private boolean _iterated = false;

    public AbstractLRSProxyMap(Class<K> keyType, Class<V> valueType) {
        _keyType = keyType;
        _valueType = valueType;
        _ct = new MapChangeTrackerImpl(this,false);
        _ct.setAutoOff(false);
    }

    public void setOwner(OpenJPAStateManager sm, int field) {
        // can't transfer ownership of an lrs proxy
        if (sm != null && _origOwner != null
            && (_origOwner != sm || _origField != field)) {
            throw new InvalidStateException(_loc.get("transfer-lrs",
                _origOwner.getMetaData().getField(_origField)));
        }

        _sm = sm;
        _field = field;

        // keep track of original owner so we can detect transfer attempts
        if (sm != null) {
            _origOwner = sm;
            _origField = field;
        }
    }

    public OpenJPAStateManager getOwner() {
        return _sm;
    }

    public int getOwnerField() {
        return _field;
    }

    public ChangeTracker getChangeTracker() {
        return this;
    }

    public Object copy(Object orig) {
        // used to store fields for rollbac; we don't store lrs fields
        return null;
    }

    /**
     * used in testing; we need to be able to make sure that OpenJPA does not
     * iterate lrs fields during standard crud operations
     */
    boolean isIterated() {
        return _iterated;
    }

    /**
     * used in testing; we need to be able to make sure that OpenJPA does not
     * iterate lrs fields during standard crud operations
     */
    void setIterated(boolean it) {
        _iterated = it;
    }

    public int size() {
        if (_count == -1)
            _count = count();
        if (_count == Integer.MAX_VALUE)
            return _count;
        return _count + _ct.getAdded().size() - _ct.getRemoved().size();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public boolean containsKey(Object key) {
        if (_keyType != null && !_keyType.isInstance(key))
            return false;
        if (_map != null && _map.containsKey(key))
            return true;
        if (_ct.getTrackKeys()) {
            if (_ct.getRemoved().contains(key))
                return false;
            return hasKey(key);
        }

        // value tracking:
        // if we've removed values, we need to see if this key represents
        // a removed instance. otherwise we can rely on the 1-1 between
        // keys and values when using value tracking
        if (_ct.getRemoved().isEmpty())
            return hasKey(key);
        return get(key) != null;
    }

    public boolean containsValue(Object val) {
        if (_valueType != null && !_valueType.isInstance(val))
            return false;
        if (_map != null && _map.containsValue(val))
            return true;
        if (!_ct.getTrackKeys()) {
            if (_ct.getRemoved().contains(val))
                return false;
            return hasValue(val);
        }

        // key tracking
        Collection<K> keys = keys(val);
        if (keys == null || keys.isEmpty())
            return false;
        keys.removeAll(_ct.getRemoved());
        keys.removeAll(_ct.getChanged());
        return keys.size() > 0;
    }

    public V get(Object key) {
        if (_keyType != null && !_keyType.isInstance(key))
            return null;
        V ret = (_map == null) ? null : _map.get(key);
        if (ret != null)
            return ret;
        if (_ct.getTrackKeys() && _ct.getRemoved().contains(key))
            return null;
        V val = value(key);
        if (!_ct.getTrackKeys() && _ct.getRemoved().contains(val))
            return null;
        return val;
    }

    public V put(K key, V value) {
        Proxies.assertAllowedType(key, _keyType);
        Proxies.assertAllowedType(value, _valueType);
        Proxies.dirty(this, false);
        if (_map == null)
            _map = new HashMap<K,V>();
        V old = _map.put(key, value);
        if (old == null && (!_ct.getTrackKeys()
            || !_ct.getRemoved().contains(key)))
            old = value(key);
        if (old != null) {
            _ct.changed(key, old, value);
            Proxies.removed(this, old, false);
        } else
            _ct.added(key, value);
        return old;
    }

    public void putAll(Map<? extends K,? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public V remove(Object key) {
        Proxies.dirty(this, false);
        V old = (_map == null) ? null : _map.remove(key);
        if (old == null && (!_ct.getTrackKeys()
            || !_ct.getRemoved().contains(key)))
            old = value(key);
        if (old != null) {
            _ct.removed(key, old);
            Proxies.removed(this, key, true);
            Proxies.removed(this, old, false);
        }
        return old;
    }

    public void clear() {
        Proxies.dirty(this, false);
        Itr itr = iterator(MODE_ENTRY);
        try {
            Map.Entry<K,V> entry;
            while (itr.hasNext()) {
                entry = (Map.Entry<K,V>) itr.next();
                Proxies.removed(this, entry.getKey(), true);
                Proxies.removed(this, entry.getValue(), false);
                _ct.removed(entry.getKey(), entry.getValue());
            }
        }
        finally {
            itr.close();
        }
    }

    public Set<K> keySet() {
        return new AbstractSet<K>() {
            public int size() {
                return AbstractLRSProxyMap.this.size();
            }

            public boolean remove(Object o) {
                return AbstractLRSProxyMap.this.remove(o) != null;
            }

            public Iterator<K> iterator() {
                return AbstractLRSProxyMap.this.iterator(MODE_KEY);
            }
        };
    }

    public Collection<V> values() {
        return new AbstractCollection<V>() {
            public int size() {
                return AbstractLRSProxyMap.this.size();
            }

            public Iterator<V> iterator() {
                return AbstractLRSProxyMap.this.iterator(MODE_VALUE);
            }
        };
    }

    public Set<Map.Entry<K, V>> entrySet() {
        return new AbstractSet<Map.Entry<K, V>>() {
            public int size() {
                return AbstractLRSProxyMap.this.size();
            }

            public Iterator<Map.Entry<K, V>> iterator() {
                return AbstractLRSProxyMap.this.iterator(MODE_ENTRY);
            }
        };
    }

    protected Object writeReplace()
        throws ObjectStreamException {
        Itr itr = iterator(MODE_ENTRY);
        try {
            Map<K,V> map = new HashMap<K,V>();
            Map.Entry<K,V> entry;
            while (itr.hasNext()) {
                entry = (Map.Entry<K,V>) itr.next();
                map.put(entry.getKey(), entry.getValue());
            }
            return map;
        } finally {
            itr.close();
        }
    }

    /**
     * Return whether the given key exists in the map.
     */
    protected abstract boolean hasKey(Object key);

    /**
     * Return whether the given value exists in the map.
     */
    protected abstract boolean hasValue(Object value);

    /**
     * Return all keys for the given value.
     */
    protected abstract Collection<K> keys(Object value);

    /**
     * Return the value of the given key.
     */
    protected abstract V value(Object key);

    /**
     * Implement this method to return an iterator over the entries
     * in the map. Each returned object must implement the
     * <code>Map.Entry</code> interface. This method may be invoked multiple
     * times. The iterator does not have to support the
     * {@link Iterator#remove} method, and may implement
     * {@link org.apache.openjpa.lib.util.Closeable}.
     */
    protected abstract Iterator<?> itr();

    /**
     * Return the number of entries in the map, or {@link Integer#MAX_VALUE}.
     */
    protected abstract int count();

    private Itr iterator(int mode) {
        _iterated = true;

        // have to copy the entry set of _map to prevent concurrent mod errors
        IteratorChain chain = new IteratorChain();
        if (_map != null)
            chain.addIterator(new ArrayList(_map.entrySet()).iterator());
        chain.addIterator(new FilterIterator(itr(), this));
        return new Itr(mode, chain);
    }

    ////////////////////////////
    // Predicate Implementation
    ////////////////////////////

    public boolean evaluate(Object obj) {
        Map.Entry entry = (Map.Entry) obj;
        return (_ct.getTrackKeys()
            && !_ct.getRemoved().contains(entry.getKey())
            || (!_ct.getTrackKeys()
            && !_ct.getRemoved().contains(entry.getValue())))
            && (_map == null || !_map.containsKey(entry.getKey()));
    }

    ///////////////////////////////////
    // MapChangeTracker Implementation
    ///////////////////////////////////

    public boolean isTracking() {
        return _ct.isTracking();
    }

    public void startTracking() {
        _ct.startTracking();
        reset();
    }

    public void stopTracking() {
        _ct.stopTracking();
        reset();
    }

    private void reset() {
        if (_map != null)
            _map.clear();
        if (_count != Integer.MAX_VALUE)
            _count = -1;
    }

    public boolean getTrackKeys() {
        return _ct.getTrackKeys();
    }

    public void setTrackKeys(boolean keys) {
        _ct.setTrackKeys(keys);
    }

    public Collection getAdded() {
        return _ct.getAdded();
    }

    public Collection getRemoved() {
        return _ct.getRemoved();
    }

    public Collection getChanged() {
        return _ct.getChanged();
    }

    public void added(Object key, Object val) {
        _ct.added(key, val);
    }

    public void removed(Object key, Object val) {
        _ct.removed(key, val);
    }

    public void changed(Object key, Object orig, Object val) {
        _ct.changed(key, orig, val);
    }

    public int getNextSequence() {
        return _ct.getNextSequence();
    }

    public void setNextSequence(int seq) {
        _ct.setNextSequence(seq);
    }

    /**
     * Wrapper around our filtering iterator chain.
     */
    private class Itr
        implements Iterator, Closeable {

        private static final int OPEN = 0;
        private static final int LAST_ELEM = 1;
        private static final int CLOSED = 2;

        private final int _mode;
        private final IteratorChain _itr;
        private Map.Entry _last = null;
        private int _state = OPEN;

        public Itr(int mode, IteratorChain itr) {
            _mode = mode;
            _itr = itr;
        }

        public boolean hasNext() {
            if (_state != OPEN)
                return false;

            // close automatically if no more elements
            if (!_itr.hasNext()) {
                free();
                _state = LAST_ELEM;
                return false;
            }
            return true;
        }

        public Object next() {
            if (_state != OPEN)
                throw new NoSuchElementException();

            _last = (Map.Entry) _itr.next();
            switch (_mode) {
                case MODE_KEY:
                    return _last.getKey();
                case MODE_VALUE:
                    return _last.getValue();
                default:
                    return _last;
            }
        }

        public void remove() {
            if (_state == CLOSED || _last == null)
                throw new NoSuchElementException();

            Proxies.dirty(AbstractLRSProxyMap.this, false);
            Proxies.removed(AbstractLRSProxyMap.this, _last.getKey(), true);
            Proxies.removed(AbstractLRSProxyMap.this, _last.getValue(), false);

            // need to get a reference to the key before we remove it
            // from the map, since in JDK 1.3-, the act of removing an entry
            // from the map will also null the entry's value, which would
            // result in incorrectly passing a null to the change tracker
            Object key = _last.getKey();
            Object value = _last.getValue();

            if (_map != null)
                _map.remove(key);
            _ct.removed(key, value);
            _last = null;
        }

        public void close() {
            free();
            _state = CLOSED;
        }

        private void free() {
            if (_state != OPEN)
                return;

            List itrs = _itr.getIterators();
            Iterator itr;
            for (int i = 0; i < itrs.size(); i++) {
                itr = (Iterator) itrs.get(i);
                if (itr instanceof FilterIterator)
                    itr = ((FilterIterator) itr).getIterator();
                ImplHelper.close(itr);
            }
        }

        protected void finalize() {
            close ();
		}
	}
}

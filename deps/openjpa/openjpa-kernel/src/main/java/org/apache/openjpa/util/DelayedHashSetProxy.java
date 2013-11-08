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
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.openjpa.kernel.AutoDetach;
import org.apache.openjpa.kernel.Broker;
import org.apache.openjpa.kernel.BrokerFactory;
import org.apache.openjpa.kernel.DetachedStateManager;
import org.apache.openjpa.kernel.OpenJPAStateManager;

/**
 * HashSet proxy with delay loading capability. Allows non-indexed add and
 * remove operations to occur on an unloaded collection. Operations that require
 * a load will trigger a load.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class DelayedHashSetProxy extends HashSet implements DelayedProxy, ProxyCollection {

    private transient OpenJPAStateManager sm;
    private transient int field;
    private transient CollectionChangeTracker changeTracker;
    private transient Class<?> elementType;

    private transient OpenJPAStateManager _ownerSm;
    private transient boolean _directAccess = false;
    private transient BrokerFactory _brokerFactory = null;
    private transient Broker _broker = null;
    private transient OpenJPAStateManager _delayedSm;
    private transient int _delayedField;
    private transient boolean _detached = false;

    public DelayedHashSetProxy(Collection<?> paramCollection) {
        super(paramCollection);
    }

    public DelayedHashSetProxy(int paramInt, float paramFloat) {
        super(paramInt, paramFloat);
    }

    public DelayedHashSetProxy(int paramInt) {
        super(paramInt);
    }

    public DelayedHashSetProxy() {
    }

    public void setOwner(OpenJPAStateManager paramOpenJPAStateManager,
            int paramInt) {
        // If clearing the owner of this proxy, store away what is necessary for
        // delayed loading
        if (sm != null && detaching(paramOpenJPAStateManager, paramInt)) {
            _detached = true;
            _delayedSm = sm;
            _delayedField = field;
        } else {
            _detached = false;
        }

        this.sm = paramOpenJPAStateManager;
        if (sm != null && sm.getPersistenceCapable() != null) {
            _ownerSm = (OpenJPAStateManager) sm.getPersistenceCapable()
                    .pcGetStateManager();
        }
        this.field = paramInt;
        if (sm != null && sm.getContext() != null) {
            _brokerFactory = sm.getContext().getBroker().getBrokerFactory();
        }
    }

    private boolean detaching(OpenJPAStateManager paramOpenJPAStateManager,
            int paramInt) {
        if ((paramOpenJPAStateManager == null && paramInt == -1)
                || (paramOpenJPAStateManager != null && paramOpenJPAStateManager instanceof DetachedStateManager)) {
            return true;
        }
        return false;
    }

    public OpenJPAStateManager getOwner() {
        return this.sm;
    }

    public int getOwnerField() {
        return this.field;
    }

    public ChangeTracker getChangeTracker() {
        return this.changeTracker;
    }

    protected void setChangeTracker(CollectionChangeTracker ct) {
        changeTracker = ct;
    }

    @Override
    public Object copy(Object paramObject) {
        return new HashSet((Collection) paramObject);
    }

    public Class getElementType() {
        return this.elementType;
    }

    protected void setElementType(Class<?> elemType) {
        elementType = elemType;
    }

    public ProxyCollection newInstance(Class paramClass,
            Comparator paramComparator, boolean paramBoolean1,
            boolean paramBoolean2) {
        DelayedHashSetProxy localproxy = new DelayedHashSetProxy();
        localproxy.elementType = paramClass;
        if (paramBoolean1)
            localproxy.changeTracker = new DelayedCollectionChangeTrackerImpl(
                    localproxy, false, false, paramBoolean2);
        return localproxy;
    }

    @Override
    public Object clone() {
        if (isDirectAccess()) {
            return super.clone();
        }
        if (isDelayLoad()) {
            load();
        }
        Proxy localProxy = (Proxy) super.clone();
        localProxy.setOwner(null, 0);
        return localProxy;
    }

    @Override
    public boolean add(Object paramObject) {
        if (_directAccess) {
            return super.add(paramObject);
        }
        ProxyCollections.beforeAdd(this, paramObject);
        boolean bool = super.add(paramObject);
        return ProxyCollections.afterAdd(this, paramObject, bool);
    }

    @Override
    public void clear() {
        if (!_directAccess) {
            if (isDelayLoad()) {
                load();
            }
            ProxyCollections.beforeClear(this);
        }
        super.clear();
    }

    @Override
    public Iterator iterator() {
        if (_directAccess) {
            return super.iterator();
        }
        if (isDelayLoad()) {
            load();
        }
        Iterator localIterator = super.iterator();
        return ProxyCollections.afterIterator(this, localIterator);
    }

    @Override
    public boolean remove(Object paramObject) {
        if (_directAccess) {
            return super.remove(paramObject);
        }
        ProxyCollections.beforeRemove(this, paramObject);
        setDirectAccess(true);
        boolean bool = super.remove(paramObject);
        setDirectAccess(false);
        return ProxyCollections.afterRemove(this, paramObject, bool);
    }

    @Override
    public boolean removeAll(Collection paramCollection) {
        if (_directAccess) {
            return super.removeAll(paramCollection);
        }
        return ProxyCollections.removeAll(this, paramCollection);
    }

    @Override
    public boolean addAll(Collection paramCollection) {
        if (_directAccess) {
            return super.addAll(paramCollection);
        }
        return ProxyCollections.addAll(this, paramCollection);
    }

    @Override
    public boolean retainAll(Collection paramCollection) {
        if (_directAccess) {
            return super.retainAll(paramCollection);
        }
        if (isDelayLoad()) {
            load();
        }
        return ProxyCollections.retainAll(this, paramCollection);
    }

    protected Object writeReplace() throws ObjectStreamException {
        if (isDelayLoad()) {
            load();
        }
        return Proxies.writeReplace(this, true);
    }

    @Override
    public int size() {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.size();
    }

    @Override
    public boolean isEmpty() {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.contains(o);
    }

    @Override
    public Object[] toArray() {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.toArray();
    }

    @Override
    public Object[] toArray(Object[] a) {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.toArray(a);
    }

    @Override
    public boolean containsAll(Collection c) {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.containsAll(c);
    }

    @Override
    public String toString() {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.toString();
    }

    public boolean equals(Object paramObject) {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.equals(paramObject);
    }

    public int hashCode() {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.hashCode();
    }

    // //////////////////////////////////////
    // DelayedProxy methods
    // //////////////////////////////////////
    public int getDelayedField() {
        if (field == -1 || _detached) {
            return _delayedField;
        }
        return field;
    }

    public OpenJPAStateManager getDelayedOwner() {
        if (sm == null || _detached) {
            return _delayedSm;
        }
        return sm;
    }

    public boolean isDirectAccess() {
        return _directAccess;
    }

    public void setDirectAccess(boolean direct) {
        _directAccess = direct;
    }

    public BrokerFactory getBrokerFactory() {
        return _brokerFactory;
    }

    @Override
    public void load() {
        ProxyCollections.loadCollection(this);
    }

    @Override
    public Broker getBroker() {
        if (_broker == null || _broker.isClosed()) {
            if (_brokerFactory != null) {
                _broker = _brokerFactory.newBroker();
            }
        }
        return _broker;
    }

    @Override
    public void closeBroker() {
        if (_broker != null && !_broker.isClosed()) {
            _broker.setAutoDetach(AutoDetach.DETACH_CLOSE);
            _broker.close();
            _broker = null;
        }
    }

    @Override
    public OpenJPAStateManager getOwnerStateManager() {
        return _ownerSm;
    }

    @Override
    public boolean isDetached() {
        return _detached;
    }

    public boolean isDelayLoad() {
        return ProxyCollections.isDelayed(this);
    }
}

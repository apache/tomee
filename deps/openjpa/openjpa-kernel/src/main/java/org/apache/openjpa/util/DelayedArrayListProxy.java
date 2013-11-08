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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.openjpa.kernel.AutoDetach;
import org.apache.openjpa.kernel.Broker;
import org.apache.openjpa.kernel.BrokerFactory;
import org.apache.openjpa.kernel.DetachedStateManager;
import org.apache.openjpa.kernel.OpenJPAStateManager;

/**
 * ArrayList proxy with delay loading capability.  Allows non-indexed
 * add and remove operations to occur on an unloaded collection.  Operations
 * that require a load will trigger a load.
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class DelayedArrayListProxy extends ArrayList implements ProxyCollection, DelayedProxy {

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

    public DelayedArrayListProxy() {
    }

    public DelayedArrayListProxy(Collection paramCollection) {
        super(paramCollection);
    }

    public DelayedArrayListProxy(int paramInt) {
        super(paramInt);
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

    public OpenJPAStateManager getOwner() {
        return sm;
    }

    public int getOwnerField() {
        return field;
    }

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

    public ChangeTracker getChangeTracker() {
        return this.changeTracker;
    }

    protected void setChangeTracker(CollectionChangeTracker ct) {
        changeTracker = ct;
    }

    public Object copy(Object paramObject) {
        if (isDelayLoad()) {
            load();
        }
        return new ArrayList((Collection) paramObject);
    }

    public Class getElementType() {
        return this.elementType;
    }

    protected void setElementType(Class<?> elemType) {
        elementType = elemType;
    }

    @Override
    public ProxyCollection newInstance(Class paramClass,
            Comparator paramComparator, boolean paramBoolean1,
            boolean paramBoolean2) {
        DelayedArrayListProxy proxy = new DelayedArrayListProxy();
        proxy.elementType = paramClass;
        proxy.changeTracker = new DelayedCollectionChangeTrackerImpl(proxy,
                true, true, paramBoolean2);
        return proxy;
    }

    public boolean add(Object paramObject) {
        if (_directAccess) {
            return super.add(paramObject);
        }
        ProxyCollections.beforeAdd(this, paramObject);
        boolean bool = super.add(paramObject);
        return ProxyCollections.afterAdd(this, paramObject, bool);
    }

    public void add(int paramInt, Object paramObject) {
        if (!_directAccess) {
            if (isDelayLoad()) {
                load();
            }
        }
        ProxyCollections.beforeAdd(this, paramInt, paramObject);
        super.add(paramInt, paramObject);
    }

    public void clear() {
        if (!_directAccess) {
            if (isDelayLoad()) {
                load();
            }
            ProxyCollections.beforeClear(this);
        }
        super.clear();
    }

    public boolean addAll(int paramInt, Collection paramCollection) {
        if (isDelayLoad()) {
            load();
        }
        return ProxyCollections.addAll(this, paramInt, paramCollection);
    }

    public boolean addAll(Collection paramCollection) {
        if (_directAccess) {
            return super.addAll(paramCollection);
        }
        return ProxyCollections.addAll(this, paramCollection);
    }

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

    public Object remove(int paramInt) {
        if (_directAccess) {
            return super.remove(paramInt);
        }
        if (isDelayLoad()) {
            load();
        }
        ProxyCollections.beforeRemove(this, paramInt);
        Object localObject = super.remove(paramInt);
        return ProxyCollections.afterRemove(this, paramInt, localObject);
    }

    public Object set(int paramInt, Object paramObject) {
        if (_directAccess) {
            return super.set(paramInt, paramObject);
        }
        if (isDelayLoad()) {
            load();
        }
        ProxyCollections.beforeSet(this, paramInt, paramObject);
        Object localObject = super.set(paramInt, paramObject);
        return ProxyCollections.afterSet(this, paramInt, paramObject,
                localObject);
    }

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

    public ListIterator listIterator(int paramInt) {
        if (_directAccess) {
            return super.listIterator(paramInt);
        }
        if (isDelayLoad()) {
            load();
        }
        ListIterator localListIterator = super.listIterator(paramInt);
        return ProxyCollections.afterListIterator(this, paramInt,
                localListIterator);
    }

    public ListIterator listIterator() {
        if (_directAccess) {
            return super.listIterator();
        }
        if (isDelayLoad()) {
            load();
        }
        ListIterator localListIterator = super.listIterator();
        return ProxyCollections.afterListIterator(this, localListIterator);
    }

    public boolean removeAll(Collection paramCollection) {
        if (_directAccess) {
            return super.removeAll(paramCollection);
        }
        return ProxyCollections.removeAll(this, paramCollection);
    }

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

    public boolean isDelayLoad() {
        return ProxyCollections.isDelayed(this);
    }

    @Override
    public Object get(int location) {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.get(location);
    }

    @Override
    public int indexOf(Object object) {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.indexOf(object);
    }

    @Override
    public int lastIndexOf(Object object) {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.lastIndexOf(object);
    }

    @Override
    public List subList(int start, int end) {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.subList(start, end);
    }

    @Override
    public boolean contains(Object object) {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.contains(object);
    }

    @Override
    public boolean containsAll(Collection collection) {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.containsAll(collection);
    }

    @Override
    public boolean isEmpty() {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.isEmpty();
    }

    @Override
    public int size() {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.size();
    }

    @Override
    public Object[] toArray() {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.toArray();
    }

    @Override
    public Object[] toArray(Object[] array) {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.toArray(array);
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
}

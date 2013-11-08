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
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.SortedSet;

import org.apache.openjpa.kernel.AutoDetach;
import org.apache.openjpa.kernel.Broker;
import org.apache.openjpa.kernel.BrokerFactory;
import org.apache.openjpa.kernel.DetachedStateManager;
import org.apache.openjpa.kernel.OpenJPAStateManager;

/**
 * PriorityQueue proxy with delay loading capability.  Allows non-indexed
 * add and remove operations to occur on an unloaded collection.  Operations
 * that require a load will trigger a load.
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class DelayedPriorityQueueProxy extends PriorityQueue implements ProxyCollection, DelayedProxy {
    private transient OpenJPAStateManager sm;
    private transient int field;
    private transient CollectionChangeTracker changeTracker;
    private transient Class elementType;

    private transient OpenJPAStateManager _ownerSm;
    private transient boolean _directAccess = false;
    private transient BrokerFactory _brokerFactory = null;
    private transient Broker _broker = null;
    private transient OpenJPAStateManager _delayedSm;
    private transient int _delayedField;
    private transient boolean _detached = false;

    public DelayedPriorityQueueProxy(int paramInt) {
        super(paramInt);
    }

    public DelayedPriorityQueueProxy(int paramInt, Comparator paramComparator) {
        super(paramInt, paramComparator);
    }

    public DelayedPriorityQueueProxy(Collection paramCollection) {
        super(paramCollection);
    }

    public DelayedPriorityQueueProxy(PriorityQueue paramPriorityQueue) {
        super(paramPriorityQueue);
    }

    public DelayedPriorityQueueProxy(SortedSet paramSortedSet) {
        super(paramSortedSet);
    }

    public DelayedPriorityQueueProxy() {
    }

    @Override
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

    @Override
    public Object clone() throws CloneNotSupportedException {
        if (_directAccess) {
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
        return new PriorityQueue((PriorityQueue) paramObject);
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
        DelayedPriorityQueueProxy localproxy = new DelayedPriorityQueueProxy();
        localproxy.elementType = paramClass;
        if (paramBoolean1)
            localproxy.changeTracker = new DelayedCollectionChangeTrackerImpl(
                    localproxy, true, false, paramBoolean2);
        return localproxy;
    }

    @Override
    public boolean add(Object paramObject) {
        if (_directAccess) {
            return super.add(paramObject);
        }
        ProxyCollections.beforeAdd(this, paramObject);
        boolean bool = false;
        try {
            setDirectAccess(true);
            bool = super.add(paramObject);
        } finally {
            setDirectAccess(false);
        }
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
    public Object poll() {
        if (_directAccess) {
            return super.poll();
        }
        // queue operations require proper ordering. the collection
        // must be loaded in order to ensure order.
        if (isDelayLoad()) {
            load();
        }
        ProxyCollections.beforePoll(this);
        Object localObject = super.poll();
        return ProxyCollections.afterPoll(this, localObject);
    }

    @Override
    public boolean offer(Object paramObject) {
        if (_directAccess) {
            return super.offer(paramObject);
        }
        ProxyCollections.beforeOffer(this, paramObject);
        boolean bool = super.offer(paramObject);
        return ProxyCollections.afterOffer(this, paramObject, bool);
    }

    @Override
    public boolean addAll(Collection paramCollection) {
        if (_directAccess) {
            return super.addAll(paramCollection);
        }
        return ProxyCollections.addAll(this, paramCollection);
    }

    @Override
    public Object remove() {
        if (_directAccess) {
            return super.remove();
        }
        // queue operations require proper ordering. the collection
        // must be loaded in order to ensure order.
        if (isDelayLoad()) {
            load();
        }
        ProxyCollections.beforeRemove(this);
        Object localObject = super.remove();
        return ProxyCollections.afterRemove(this, localObject);
    }

    @Override
    public boolean removeAll(Collection paramCollection) {
        if (_directAccess) {
            return super.removeAll(paramCollection);
        }
        return ProxyCollections.removeAll(this, paramCollection);
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
    public boolean contains(Object object) {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.contains(object);
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

    @Override
    public boolean containsAll(Collection c) {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.containsAll(c);
    }

    @Override
    public Object element() {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.element();
    }

    @Override
    public Object peek() {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.peek();
    }

    @Override
    public boolean equals(Object paramObject) {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.equals(paramObject);
    }

    @Override
    public int hashCode() {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.hashCode();
    }

    // //////////////////////////////////////
    // DelayedProxy methods
    // //////////////////////////////////////
    @Override
    public int getDelayedField() {
        if (field == -1 || _detached) {
            return _delayedField;
        }
        return field;
    }

    @Override
    public OpenJPAStateManager getDelayedOwner() {
        if (sm == null || _detached) {
            return _delayedSm;
        }
        return sm;
    }

    @Override
    public boolean isDirectAccess() {
        return _directAccess;
    }

    @Override
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

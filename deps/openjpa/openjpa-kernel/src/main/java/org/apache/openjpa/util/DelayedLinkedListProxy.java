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
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.apache.openjpa.kernel.AutoDetach;
import org.apache.openjpa.kernel.Broker;
import org.apache.openjpa.kernel.BrokerFactory;
import org.apache.openjpa.kernel.DetachedStateManager;
import org.apache.openjpa.kernel.OpenJPAStateManager;

/**
 * LinkedList proxy with delay loading capability.  Allows non-indexed
 * add and remove operations to occur on an unloaded collection.  Operations
 * that require a load will trigger a load.
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class DelayedLinkedListProxy extends LinkedList implements ProxyCollection, DelayedProxy {

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

    public DelayedLinkedListProxy(Collection paramCollection) {
        super(paramCollection);
    }

    public DelayedLinkedListProxy() {
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

    @Override
    public OpenJPAStateManager getOwner() {
        return this.sm;
    }

    @Override
    public int getOwnerField() {
        return this.field;
    }

    @Override
    public ChangeTracker getChangeTracker() {
        return this.changeTracker;
    }

    protected void setChangeTracker(CollectionChangeTracker ct) {
        changeTracker = ct;
    }
    
    @Override
    public Object copy(Object paramObject) {
        return new LinkedList((Collection) paramObject);
    }

    @Override
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
        DelayedLinkedListProxy localproxy = new DelayedLinkedListProxy();
        localproxy.elementType = paramClass;
        if (paramBoolean1)
            localproxy.changeTracker = new DelayedCollectionChangeTrackerImpl(
                    localproxy, true, true, paramBoolean2);
        return localproxy;
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

    // //////////////////////////////////////
    // Implementation method wrappers
    // //////////////////////////////////////

    @Override
    public Object clone() {
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

    @Override
    public void add(int paramInt, Object paramObject) {
        if (!_directAccess) {
            if (isDelayLoad()) {
                load();
            }
        }
        ProxyCollections.beforeAdd(this, paramInt, paramObject);
        super.add(paramInt, paramObject);
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
    public boolean addAll(Collection paramCollection) {
        if (_directAccess) {
            return super.addAll(paramCollection);
        }
        return ProxyCollections.addAll(this, paramCollection);
    }

    @Override
    public boolean addAll(int paramInt, Collection paramCollection) {
        if (_directAccess) {
            return super.addAll(paramInt, paramCollection);
        }
        if (isDelayLoad()) {
            load();
        }
        return ProxyCollections.addAll(this, paramInt, paramCollection);
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

    @Override
    public void addFirst(Object paramObject) {
        if (_directAccess) {
            super.addFirst(paramObject);
            return;
        }
        if (isDelayLoad()) {
            load();
        }
        ProxyCollections.beforeAddFirst(this, paramObject);
        super.addFirst(paramObject);
    }

    @Override
    public void addLast(Object paramObject) {
        if (_directAccess) {
            super.addLast(paramObject);
            return;
        }
        if (isDelayLoad()) {
            load();
        }
        ProxyCollections.beforeAddLast(this, paramObject);
        super.addLast(paramObject);
        ProxyCollections.afterAddLast(this, paramObject);
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
    public Object removeFirst() {
        if (_directAccess) {
            return super.removeFirst();
        }
        if (isDelayLoad()) {
            load();
        }
        ProxyCollections.beforeRemoveFirst(this);
        Object localObject = super.removeFirst();
        return ProxyCollections.afterRemoveFirst(this, localObject);
    }

    @Override
    public Object removeLast() {
        if (_directAccess) {
            return super.removeLast();
        }
        if (isDelayLoad()) {
            load();
        }
        ProxyCollections.beforeRemoveLast(this);
        Object localObject = super.removeLast();
        return ProxyCollections.afterRemoveLast(this, localObject);
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

    @Override
    public boolean removeFirstOccurrence(Object paramObject) {
        if (_directAccess) {
            return super.removeFirstOccurrence(paramObject);
        }
        if (isDelayLoad()) {
            load();
        }
        Proxies.dirty(this, true);
        return super.removeFirstOccurrence(paramObject);
    }

    @Override
    public boolean removeLastOccurrence(Object paramObject) {
        if (_directAccess) {
            return super.removeLastOccurrence(paramObject);
        }
        if (isDelayLoad()) {
            load();
        }
        Proxies.dirty(this, true);
        return super.removeLastOccurrence(paramObject);
    }

    protected Object writeReplace() throws ObjectStreamException {
        if (isDelayLoad()) {
            load();
        }
        return Proxies.writeReplace(this, true);
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

    @Override
    public List subList(int fromIndex, int toIndex) {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.subList(fromIndex, toIndex);
    }

    @Override
    public int lastIndexOf(Object o) {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.lastIndexOf(o);
    }

    @Override
    public int indexOf(Object o) {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.indexOf(o);
    }

    @Override
    public Object get(int index) {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.get(index);
    }

    @Override
    public boolean containsAll(Collection c) {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.containsAll(c);
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
    public boolean contains(Object object) {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.contains(object);
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
    public boolean offerFirst(Object paramObject) {
        if (_directAccess) {
            return super.offerFirst(paramObject);
        }
        if (isDelayLoad()) {
            load();
        }
        return super.offerFirst(paramObject);
    }

    @Override
    public boolean offerLast(Object paramObject) {
        if (_directAccess) {
            return super.offerLast(paramObject);
        }
        if (isDelayLoad()) {
            load();
        }
        return super.offerLast(paramObject);
    }

    @Override
    public Object pollFirst() {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.pollFirst();
        
    }

    @Override
    public Object pollLast() {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.pollLast();
    }
    
    @Override
    public Object getFirst() {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.getFirst();        
    }

    @Override
    public Object getLast() {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.getLast();
    }
    
    @Override
    public Object peekFirst() {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.peekFirst();
    }
    
    @Override
    public Object peekLast() {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.peekLast();
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
    public void push(Object o) {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        super.push(o);
    }
    
    @Override
    public Object pop() {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.pop();
    }

    @Override
    public Iterator descendingIterator() {
        if (_directAccess) {
            return super.descendingIterator();
        }
        if (isDelayLoad()) {
            load();
        }
        Iterator localIterator = super.descendingIterator();
        return ProxyCollections.afterIterator(this, localIterator);
    }
}

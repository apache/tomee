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
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import org.apache.openjpa.kernel.AutoDetach;
import org.apache.openjpa.kernel.Broker;
import org.apache.openjpa.kernel.BrokerFactory;
import org.apache.openjpa.kernel.DetachedStateManager;
import org.apache.openjpa.kernel.OpenJPAStateManager;

/**
 * Vector proxy with delay loading capability.  Allows non-indexed
 * add and remove operations to occur on an unloaded collection.  Operations
 * that require a load will trigger a load.
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class DelayedVectorProxy extends Vector implements ProxyCollection, DelayedProxy {
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

    public DelayedVectorProxy(int paramInt) {
        super(paramInt);
    }

    public DelayedVectorProxy() {
    }

    public DelayedVectorProxy(Collection paramCollection) {
        super(paramCollection);
    }

    public DelayedVectorProxy(int paramInt1, int paramInt2) {
        super(paramInt1, paramInt2);
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
    public OpenJPAStateManager getOwner() {
        return this.sm;
    }

    @Override
    public int getOwnerField() {
        return this.field;
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

    @Override
    public synchronized Object clone() {
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
    public ChangeTracker getChangeTracker() {
        return this.changeTracker;
    }

    protected void setChangeTracker(CollectionChangeTracker ct) {
        changeTracker = ct;
    }
    
    @Override
    public Object copy(Object paramObject) {
        if (isDelayLoad()) {
            load();
        }
        return new Vector((Collection) paramObject);
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
        DelayedVectorProxy localproxy = new DelayedVectorProxy();
        localproxy.elementType = paramClass;
        if (paramBoolean1) {
            localproxy.changeTracker = new DelayedCollectionChangeTrackerImpl(
                    localproxy, true, true, paramBoolean2);
        }
        return localproxy;
    }

    @Override
    public synchronized boolean add(Object paramObject) {
        if (_directAccess) {
            return super.add(paramObject);
        }

        ProxyCollections.beforeAdd(this, paramObject);
        boolean bool = super.add(paramObject);
        return ProxyCollections.afterAdd(this, paramObject, bool);
    }

    @Override
    public synchronized void add(int paramInt, Object paramObject) {
        if (!_directAccess) {
            if (isDelayLoad()) {
                load();
            }
        }
        ProxyCollections.beforeAdd(this, paramInt, paramObject);
        super.add(paramInt, paramObject);
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
    public synchronized boolean addAll(int paramInt, Collection paramCollection) {
        if (_directAccess) {
            return super.addAll(paramInt, paramCollection);
        }
        if (isDelayLoad()) {
            load();
        }
        return ProxyCollections.addAll(this, paramInt, paramCollection);
    }

    @Override
    public synchronized boolean addAll(Collection paramCollection) {
        if (_directAccess) {
            return super.addAll(paramCollection);
        }
        return ProxyCollections.addAll(this, paramCollection);
    }

    @Override
    public synchronized void addElement(Object paramObject) {
        if (_directAccess) {
            super.addElement(paramObject);
            return;
        }

        ProxyCollections.beforeAddElement(this, paramObject);
        super.addElement(paramObject);
        ProxyCollections.afterAddElement(this, paramObject);
    }

    @Override
    public synchronized Object remove(int paramInt) {
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
    public synchronized boolean remove(Object paramObject) {
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
    public synchronized Object set(int paramInt, Object paramObject) {
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
    public synchronized boolean removeAll(Collection paramCollection) {
        if (_directAccess) {
            return super.removeAll(paramCollection);
        }
        return ProxyCollections.removeAll(this, paramCollection);
    }

    @Override
    public synchronized boolean retainAll(Collection paramCollection) {
        if (_directAccess) {
            return super.retainAll(paramCollection);
        }
        if (isDelayLoad()) {
            load();
        }
        return ProxyCollections.retainAll(this, paramCollection);
    }

    @Override
    public synchronized void insertElementAt(Object paramObject, int paramInt) {
        if (_directAccess) {
            super.insertElementAt(paramObject, paramInt);
            return;
        }
        if (isDelayLoad()) {
            load();
        }

        ProxyCollections.beforeInsertElementAt(this, paramObject, paramInt);
        super.insertElementAt(paramObject, paramInt);
    }

    @Override
    public synchronized void removeAllElements() {
        if (_directAccess) {
            super.removeAllElements();
            return;
        }
        if (isDelayLoad()) {
            load();
        }
        ProxyCollections.beforeRemoveAllElements(this);
        super.removeAllElements();
    }

    @Override
    public synchronized boolean removeElement(Object paramObject) {
        if (_directAccess) {
            return super.removeElement(paramObject);
        }
        ProxyCollections.beforeRemoveElement(this, paramObject);
        setDirectAccess(true);
        boolean bool = super.removeElement(paramObject);
        setDirectAccess(false);
        return ProxyCollections.afterRemoveElement(this, paramObject, bool);
    }

    @Override
    public synchronized void removeElementAt(int paramInt) {
        if (_directAccess) {
            super.removeElementAt(paramInt);
            return;
        }
        if (isDelayLoad()) {
            load();
        }
        ProxyCollections.beforeRemoveElementAt(this, paramInt);
        super.removeElementAt(paramInt);
    }

    @Override
    public synchronized void setElementAt(Object paramObject, int paramInt) {
        if (_directAccess) {
            super.setElementAt(paramObject, paramInt);
            return;
        }
        if (isDelayLoad()) {
            load();
        }
        ProxyCollections.beforeSetElementAt(this, paramObject, paramInt);
        super.setElementAt(paramObject, paramInt);
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
    public synchronized void setSize(int paramInt) {
        if (_directAccess) {
            super.setSize(paramInt);
            return;
        }
        if (isDelayLoad()) {
            load();
        }
        Proxies.dirty(this, true);
        super.setSize(paramInt);
    }

    protected synchronized Object writeReplace() throws ObjectStreamException {
        if (isDelayLoad()) {
            load();
        }
        return Proxies.writeReplace(this, true);
    }

    @Override
    public synchronized boolean contains(Object object) {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.contains(object);
    }

    @Override
    public synchronized boolean containsAll(Collection collection) {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.containsAll(collection);
    }

    @Override
    public synchronized boolean isEmpty() {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.isEmpty();
    }

    @Override
    public synchronized int size() {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.size();
    }

    @Override
    public synchronized Object[] toArray() {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.toArray();
    }

    @Override
    public synchronized Object[] toArray(Object[] array) {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.toArray(array);
    }

    @Override
    public synchronized boolean equals(Object paramObject) {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.equals(paramObject);
    }

    @Override
    public synchronized int hashCode() {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.hashCode();
    }

    @Override
    public synchronized int lastIndexOf(Object object) {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.lastIndexOf(object);
    }

    @Override
    public synchronized List subList(int start, int end) {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.subList(start, end);
    }

    @Override
    public synchronized Object get(int location) {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.get(location);
    }

    @Override
    public synchronized int indexOf(Object object) {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.indexOf(object);
    }

    @Override
    public synchronized int indexOf(Object object, int index) {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.indexOf(object, index);
    }

    @Override
    public synchronized void copyInto(Object[] anArray) {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        super.copyInto(anArray);
    }

    public synchronized void trimToSize() {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        super.trimToSize();
    }

    public synchronized void ensureCapacity(int minCapacity) {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        super.ensureCapacity(minCapacity);
    }

    public synchronized int capacity() {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.capacity();
    }

    public Enumeration elements() {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.elements();
    }

    public synchronized int lastIndexOf(Object o, int index) {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.lastIndexOf(o, index);
    }

    public synchronized Object elementAt(int index) {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.elementAt(index);
    }

    public synchronized Object firstElement() {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.firstElement();
    }

    public synchronized Object lastElement() {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.lastElement();
    }

    public synchronized String toString() {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        return super.toString();
    }

    protected synchronized void removeRange(int fromIndex, int toIndex) {
        if (!_directAccess && isDelayLoad()) {
            load();
        }
        super.removeRange(fromIndex, toIndex);
    }
}

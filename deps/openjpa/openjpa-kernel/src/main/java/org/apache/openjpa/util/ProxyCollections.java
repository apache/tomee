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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.openjpa.kernel.Broker;
import org.apache.openjpa.kernel.BrokerImpl;
import org.apache.openjpa.kernel.DetachedValueStateManager;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.kernel.StateManagerImpl;

/**
 * Utility methods used by collection proxies.
 *
 * @author Abe White
 * @nojavadoc
 */
public class ProxyCollections 
    extends Proxies {

    /**
     * Call before invoking {@link List#add(int,Object)} on super.
     */
    public static void beforeAdd(ProxyCollection coll, int index, Object value){
        assertAllowedType(value, coll.getElementType());
        dirty(coll, true);
    }

    /**
     * Call before invoking {@link Vector#insertElementAt(Object,int)} on super.
     */
    public static void beforeInsertElementAt(ProxyCollection coll, Object value,
        int index) {
        beforeAdd(coll, index, value);
    }

    /**
     * Call before invoking {@link Collection#add(Object)} on super.
     */
    public static void beforeAdd(ProxyCollection coll, Object value) {
        assertAllowedType(value, coll.getElementType());
        // Must only dirty the collection outside of a delayed load
        if (!isDirectAccess(coll)) {
            dirty(coll, false);
        }
    }

    /**
     * Call after invoking {@link Collection#add(Object)} on super.
     *
     * @param added whether the object was added
     * @return <code>added</code>, for convenience
     */
    public static boolean afterAdd(ProxyCollection coll, Object value, 
        boolean added) {
        if (!isDirectAccess(coll) && added && coll.getChangeTracker() != null) {
            setDirectAccess(coll,true);
            ((CollectionChangeTracker) coll.getChangeTracker()).added(value);
            setDirectAccess(coll,false);
        }
        return added;
    }

    /**
     * Call before invoking {@link Vector#addElement(Object)} on super.
     */
    public static void beforeAddElement(ProxyCollection coll, Object value) {
        beforeAdd(coll, value);
    }

    /**
     * Call after invoking {@link Vector#addElement(Object)} on super.
     */
    public static void afterAddElement(ProxyCollection coll, Object value) {
        afterAdd(coll, value, true);
    }

    /**
     * Call before invoking {@link LinkedList#addFirst(Object)} on super.
     */
    public static void beforeAddFirst(ProxyCollection coll, Object value) {
        beforeAdd(coll, 0, value);
    }

    /**
     * Call before invoking {@link LinkedList#addLast(Object)} on super.
     */
    public static void beforeAddLast(ProxyCollection coll, Object value) {
        beforeAdd(coll, value);
    }

    /**
     * Call after invoking {@link LinkedList#addLast(Object)} on super.
     */
    public static void afterAddLast(ProxyCollection coll, Object value) {
        afterAdd(coll, value, true);
    }

    /**
     * Call before invoking {@link Queue#offer(Object)} on super.
     */
    public static void beforeOffer(ProxyCollection coll, Object value) {
        beforeAdd(coll, value);
    }

    /**
     * Call after invoking {@link Queue#offer(Object)} on super.
     *
     * @param added whether the object was added
     * @return <code>added</code>, for convenience
     */
    public static boolean afterOffer(ProxyCollection coll, Object value, 
        boolean added) {
        return afterAdd(coll, value, added);
    }

    /**
     * Override for {@link List#addAll(int, Collection)}.
     */
    public static boolean addAll(ProxyCollection coll, int index, 
        Collection values) {
        List list = (List) coll;
        for (Iterator itr = values.iterator(); itr.hasNext(); index++)
            list.add(index, itr.next());
        return values.size() > 0;
    }

    /**
     * Override for {@link Collection#addAll}.
     */
    public static boolean addAll(ProxyCollection coll, Collection values) {
        boolean added = false;
        for (Iterator<?> itr = values.iterator(); itr.hasNext();)
            added |= coll.add(itr.next());
        return added;
    }

    /**
     * Call before clearing collection.
     */
    public static void beforeClear(ProxyCollection coll) {
        dirty(coll, true);
        for (Iterator<?> itr = coll.iterator(); itr.hasNext();)
            removed(coll, itr.next(), false);
    }

    /**
     * Call before clearing vector.
     */
    public static void beforeRemoveAllElements(ProxyCollection coll) {
        beforeClear(coll);
    }

    /**
     * Wrap given iterator in a proxy.
     */
    public static Iterator afterIterator(final ProxyCollection coll, 
        final Iterator itr) {
        // check for proxied; some coll impls delegate iterator methods
        if (itr instanceof ProxyIterator)
            return itr;
        return new ProxyIterator() {
            private Object _last = null;

            public boolean hasNext() {
                return itr.hasNext();
            }

            public Object next() {
                _last = itr.next();
                return _last;
            }

            public void remove() {
                dirty(coll, false);
                itr.remove();
                if (coll.getChangeTracker() != null)
                    ((CollectionChangeTracker) coll.getChangeTracker()).
                        removed(_last);
                Proxies.removed(coll, _last, false);
            }
        };
    }

    /**
     * Wrap given iterator in a proxy.
     */
    public static ListIterator afterListIterator(final ProxyCollection coll, 
        int idx, final ListIterator itr) {
        return afterListIterator(coll, itr);
    }

    /**
     * Wrap given iterator in a proxy.
     */
    public static ListIterator afterListIterator(final ProxyCollection coll, 
        final ListIterator itr) {
        // check for proxied; some coll impls delegate iterator methods
        if (itr instanceof ProxyListIterator)
            return itr;
        return new ProxyListIterator() {
            private Object _last = null;

            public boolean hasNext() {
                return itr.hasNext();
            }

            public int nextIndex() {
                return itr.nextIndex();
            }

            public Object next() {
                _last = itr.next();
                return _last;
            }

            public boolean hasPrevious() {
                return itr.hasPrevious();
            }

            public int previousIndex() {
                return itr.previousIndex();
            }

            public Object previous() {
                _last = itr.previous();
                return _last;
            }

            public void set(Object o) {
                assertAllowedType(o, coll.getElementType());
                dirty(coll, false);
                itr.set(o);
                if (coll.getChangeTracker() != null)
                    coll.getChangeTracker().stopTracking();
                Proxies.removed(coll, _last, false);
                _last = o;
            }

            public void add(Object o) {
                assertAllowedType(o, coll.getElementType());
                dirty(coll, false);
                itr.add(o);
                if (coll.getChangeTracker() != null) {
                    if (hasNext())
                        coll.getChangeTracker().stopTracking();
                    else
                        ((CollectionChangeTracker) coll.getChangeTracker()).
                            added(o);
                }
                _last = o;
            }

            public void remove() {
                dirty(coll, false);
                itr.remove();
                if (coll.getChangeTracker() != null)
                    ((CollectionChangeTracker) coll.getChangeTracker()).
                        removed(_last);
                Proxies.removed(coll, _last, false);
            }
        };
    }

    /**
     * Call before invoking {@link List#remove(int)} on super.
     */ 
    public static void beforeRemove(ProxyCollection coll, int index) {
        dirty(coll, false);
    }

    /**
     * Call after invoking {@link List#remove(int)} on super.
     *
     * @param removed the removed object
     * @return the removed object, for convenience
     */ 
    public static Object afterRemove(ProxyCollection coll, int index, 
        Object removed) {
        if (coll.getChangeTracker() != null)
            ((CollectionChangeTracker) coll.getChangeTracker()).
                    removed(removed);
        removed(coll, removed, false);
        return removed;
    }

    /**
     * Call before invoking {@link Vector#removeElementAt(int)} on super.
     */ 
    public static void beforeRemoveElementAt(ProxyCollection coll, int index) {
        beforeRemove(coll, index);
    }

    /**
     * Call before invoking {@link Collection#remove} on super.
     */
    public static void beforeRemove(ProxyCollection coll, Object o) {
        // Must only dirty the collection outside of a delayed load
        if (!isDirectAccess(coll)) {
            dirty(coll, false);
        }
    }

    /**
     * Call after invoking {@link Collection#remove} on super.
     *
     * @param removed whether the object was removed
     * @return whether the object was removed, for convenience
     */ 
    public static boolean afterRemove(ProxyCollection coll, Object o, 
        boolean removed){
        boolean isDelayed = isDelayed(coll);
        boolean direct = isDirectAccess(coll);
        if (!isDelayed) {
            if (!removed)
                return false;
        }
        if (!direct && coll.getChangeTracker() != null) {
            // switch on direct access to prevent the removed op from 
            // inadvertently loading the collection
            setDirectAccess(coll, true);
            ((CollectionChangeTracker) coll.getChangeTracker()).removed(o);
            setDirectAccess(coll, false);
        }
        if (!isDelayed) {
            removed(coll, o, false);
        }
        return true;
    }

    private static boolean isDirectAccess(ProxyCollection coll) {
        if (coll instanceof DelayedProxy) {
            DelayedProxy dpxy = (DelayedProxy)coll;
            return dpxy.isDirectAccess();
        }
        return false;
    }
    
    private static void setDirectAccess(ProxyCollection coll, boolean direct) {
        if (coll instanceof DelayedProxy) {
            DelayedProxy dpxy = (DelayedProxy)coll;
            dpxy.setDirectAccess(direct);
        }
    }

    /**
     * Call before invoking {@link Vector#removeElement} on super.
     */
    public static void beforeRemoveElement(ProxyCollection coll, Object o) {
        beforeRemove(coll, o);
    }

    /**
     * Call after invoking {@link Vector#removeElement} on super.
     */ 
    public static boolean afterRemoveElement(ProxyCollection coll, Object o, 
        boolean removed) {
        return afterRemove(coll, o, removed);
    }

    /**
     * Call before invoking {@link LinkedList#removeFirst} on super.
     */
    public static void beforeRemoveFirst(ProxyCollection coll) {
        beforeRemove(coll, 0);
    }

    /**
     * Call after invoking {@link LinkedList#removeFirst} on super.
     */
    public static Object afterRemoveFirst(ProxyCollection coll, Object removed){
        return afterRemove(coll, 0, removed);
    }

    /**
     * Call after invoking {@link LinkedList#removeLast} on super.
     */
    public static void beforeRemoveLast(ProxyCollection coll) {
        beforeRemove(coll, coll.size() - 1);
    }

    /**
     * Call after invoking {@link LinkedList#removeLast} on super.
     */
    public static Object afterRemoveLast(ProxyCollection coll, Object removed) {
        return afterRemove(coll, coll.size(), removed);
    }

    /**
     * Call before invoking {@link Queue#remove} on super.
     */
    public static void beforeRemove(ProxyCollection coll) {
        beforeRemove(coll, 0);
    }

    /**
     * Call after invoking {@link Queue#remove} on super.
     */
    public static Object afterRemove(ProxyCollection coll, Object removed){
        return afterRemove(coll, 0, removed);
    }

    /**
     * Call before invoking {@link Queue#poll} on super.
     */
    public static void beforePoll(ProxyCollection coll) {
        if (!coll.isEmpty())
            beforeRemove(coll, 0);
    }

    /**
     * Call after invoking {@link Queue#poll} on super.
     */
    public static Object afterPoll(ProxyCollection coll, Object removed) {
        if (removed != null)
            afterRemove(coll, 0, removed);
        return removed;
    }

    /**
     * Override for {@link Collection#removeAll}.
     */
    public static boolean removeAll(ProxyCollection coll, Collection<?> vals) {
        boolean removed = false;
        for (Iterator<?> itr = vals.iterator(); itr.hasNext();)
            removed |= coll.remove(itr.next());
        return removed;
    }

    /**
     * Override for {@link Collection#retainAll}.
     */
    public static boolean retainAll(ProxyCollection coll, Collection<?> vals) {
        int size = coll.size();
        for (Iterator<?> itr = coll.iterator(); itr.hasNext();)
            if (!vals.contains(itr.next()))
                itr.remove();
        return coll.size() < size;
    }

    /**
     * Call before invoking {@link List#set} on super.
     */
    public static void beforeSet(ProxyCollection coll, int index, 
        Object element) {
        assertAllowedType(element, coll.getElementType());
        dirty(coll, true);
    }

    /**
     * Call after invoking {@link List#set} on super.
     *
     * @param replaced the replaced object
     * @return the replaced object, for convenience
     */
    public static Object afterSet(ProxyCollection coll, int index, 
        Object element, Object replaced) {
        if (replaced != element)
            removed(coll, replaced, false);
        return replaced;
    }

    /**
     * Call before invoking {@link Vector#setElementAt} on super.
     */
    public static void beforeSetElementAt(ProxyCollection coll, Object element,
        int index) {
        beforeSet(coll, index, element);
    }

    /**
     * Call after invoking {@link Vector#setElementAt} on super.
     */
    public static Object afterSetElementAt(ProxyCollection coll, Object element,
        int index, Object replaced) {
        return afterSet(coll, index, element, replaced);
    }

    /**
     * Marker interface for a proxied iterator. 
     */
    public static interface ProxyIterator 
        extends Iterator {
    }

    /**
     * Marker interface for a proxied list iterator. 
     */
    public static interface ProxyListIterator 
        extends ProxyIterator, ListIterator {
    }

    public static void loadCollection(ProxyCollection proxy) {
        loadCollection(proxy, false);
    }

    public static void loadCollection(ProxyCollection proxy, boolean detaching) {
        if (!isDelayed(proxy)) {
            return;
        }
        DelayedProxy dProxy = (DelayedProxy)proxy;
        if (dProxy.isDirectAccess()) {
            return;
        }
        boolean state[] = new boolean[2];
        try {
            dProxy.setDirectAccess(true);
            state = checkState(proxy);
            boolean tracking = false;
            ChangeTracker ct = proxy.getChangeTracker();
            Collection<?> added = null;
            Collection<?> removed = null;
            if (ct != null && ct.isTracking() ) {
                if (!ct.getAdded().isEmpty()) {
                    added = new ArrayList(ct.getAdded());
                }
                if (!ct.getRemoved().isEmpty()) {
                    removed = new ArrayList(ct.getRemoved());
                }
                tracking = true;
                ct.stopTracking();
            }
            if (proxy.size() > 0) {
                proxy.clear();
            }
            dProxy.getDelayedOwner().loadDelayedField(dProxy.getDelayedField());
            if (!detaching && tracking && !ct.isTracking()) {
                ct.startTracking();
            }
            // add new elements
            if (added != null && added.size() > 0) {
                dProxy.setDirectAccess(false);
                proxy.addAll(added);
                added.clear();
            }
            // purge removed elements
            if (removed != null && removed.size() > 0) {
                dProxy.setDirectAccess(false);
                proxy.removeAll(removed);
                removed.clear();
            }
        } finally {
            dProxy.setDirectAccess(false);
            if (state[0]) {
                dProxy.closeBroker();
            }
            if (state[1]) {
                clearStateManager(proxy);
            }
        }
    }
    
    public static boolean isDelayed(ProxyCollection proxy) {
        if (proxy instanceof DelayedProxy) {
            DelayedProxy dProxy = (DelayedProxy)proxy;
            OpenJPAStateManager sm = dProxy.getDelayedOwner();
            return (sm != null &&
                    sm.isDelayed(dProxy.getDelayedField()));
        }
        return false;
    }
    
    private static boolean[] checkState(ProxyCollection proxy) {
        boolean[] state = new boolean[2];
        DelayedProxy dProxy = (DelayedProxy)proxy;

        OpenJPAStateManager sm = dProxy.getDelayedOwner();
        if (sm != null) {
            // If the broker assigned to this proxy is null, closed or no longer
            // manages the pc, produce a new one
            Broker broker = sm.getContext().getBroker();
            if (dProxy.isDetached() || broker == null || broker.isClosed() 
                || (!broker.isClosed() && !broker.isPersistent(sm.getPersistenceCapable()))) {
                state[0] = true;
                broker = dProxy.getBroker();
                ((StateManagerImpl)sm).setBroker((BrokerImpl)broker);
            }
            if (dProxy.isDetached() || sm.getPersistenceCapable().pcGetStateManager() == null) {
                state[1] = true;
                if (dProxy.getOwnerStateManager() != null) {
                    sm.getPersistenceCapable().pcReplaceStateManager(dProxy.getOwnerStateManager());
                    ((StateManagerImpl)dProxy.getOwnerStateManager()).setBroker((BrokerImpl)broker);
                } else {
                    sm.getPersistenceCapable().pcReplaceStateManager(
                            new DetachedValueStateManager(sm.getPersistenceCapable(), sm.getContext()));
                }
            }
        }
        return state;
    }
    
    private static void clearStateManager(ProxyCollection proxy) {
        OpenJPAStateManager sm = proxy.getOwner();
        if (sm != null) {
            sm.getPersistenceCapable().pcReplaceStateManager(null);
        }
    }
}

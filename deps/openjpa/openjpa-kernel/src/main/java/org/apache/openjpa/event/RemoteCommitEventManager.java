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
package org.apache.openjpa.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.kernel.Broker;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.lib.util.Closeable;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.util.concurrent.AbstractConcurrentEventManager;
import org.apache.openjpa.util.UserException;

/**
 * Manager that can be used to track and notify
 * {@link RemoteCommitListener}s on remote commit events. If remote events
 * are enabled, this manager should be installed as a transaction listener on
 * all brokers so that it knows when commits are made.
 *
 * @author Patrick Linskey
 * @author Abe White
 * @since 0.3.0
 */
public class RemoteCommitEventManager
    extends AbstractConcurrentEventManager
    implements EndTransactionListener, Closeable {

    private static final Localizer _loc = Localizer.forPackage
        (RemoteCommitEventManager.class);

    private final RemoteCommitProvider _provider;
    private boolean _transmitPersIds = false;

    /**
     * Constructor. Supply configuration.
     */
    public RemoteCommitEventManager(OpenJPAConfiguration conf) {
        _provider = conf.newRemoteCommitProviderInstance();
        if (_provider != null) {
            _provider.setRemoteCommitEventManager(this);
        }
    }

    /**
     * Return true if remote events are enabled.
     */
    public boolean areRemoteEventsEnabled() {
        return _provider != null;
    }

    /**
     * Return the {@link RemoteCommitProvider} that this manager uses.
     *
     * @since 0.3.1
     */
    public RemoteCommitProvider getRemoteCommitProvider() {
        return _provider;
    }

    /**
     * Whether the oids of added instances will be transmitted.
     */
    public boolean getTransmitPersistedObjectIds() {
        return _transmitPersIds;
    }

    /**
     * Whether the oids of added instances will be transmitted.
     */
    public void setTransmitPersistedObjectIds(boolean transmit) {
        _transmitPersIds = transmit;
    }

    /**
     * Adds an OpenJPA-internal listener to this RemoteCommitEventManager.
     * Listeners so registered will be fired before any that are registered
     * via {@link #addListener}. This means that the external listeners can
     * rely on internal caches and data structures being up-to-date by the
     * time that they are invoked.
     *
     * @since 1.0.0
     */
    public void addInternalListener(RemoteCommitListener listen) {
        if (_provider == null)
            throw new UserException(_loc.get("no-provider"));
        ((List) _listeners).add(0, listen);
    }

    public void addListener(RemoteCommitListener listen) {
        if (_provider == null)
            throw new UserException(_loc.get("no-provider"));
        super.addListener(listen);
    }

    /**
     * Close this manager and all registered listeners.
     */
    public void close() {
        if (_provider != null) {
            _provider.close();
            Collection listeners = getListeners();
            for (Iterator itr = listeners.iterator(); itr.hasNext();)
                ((RemoteCommitListener) itr.next()).close();
        }
    }

    protected void fireEvent(Object event, Object listener) {
        RemoteCommitListener listen = (RemoteCommitListener) listener;
        RemoteCommitEvent ev = (RemoteCommitEvent) event;
        listen.afterCommit(ev);
    }

    /**
     * Fire an event to local listeners only notifying them of a detected
     * stale record.
     *
     * @since 1.0.0
     */
    public void fireLocalStaleNotification(Object oid) {
        RemoteCommitEvent ev = new RemoteCommitEvent(
            RemoteCommitEvent.PAYLOAD_LOCAL_STALE_DETECTION,
            null, null, Collections.singleton(oid), null);
        fireEvent(ev);
    }

    //////////////////////////////////////
    // TransactionListener implementation
    //////////////////////////////////////

    public void afterCommit(TransactionEvent event) {
        if (_provider != null) {
            RemoteCommitEvent rce = createRemoteCommitEvent(event);
            if (rce != null)
                _provider.broadcast(rce);
        }
    }

    /**
     * Create a remote commit event from the given transaction event.
     */
    private RemoteCommitEvent createRemoteCommitEvent(TransactionEvent event) {
        Broker broker = (Broker) event.getSource();
        int payload;
        Collection persIds = null;
        Collection addClassNames = null;
        Collection updates = null;
        Collection deletes = null;

        if (broker.isTrackChangesByType()) {
            payload = RemoteCommitEvent.PAYLOAD_EXTENTS;
            addClassNames = toClassNames(event.getPersistedTypes());
            updates = toClassNames(event.getUpdatedTypes());
            deletes = toClassNames(event.getDeletedTypes());
            if (addClassNames == null && updates == null && deletes == null)
                return null;
        } else {
            Collection trans = event.getTransactionalObjects();
            if (trans.isEmpty())
                return null;

            payload = (_transmitPersIds)
                ? RemoteCommitEvent.PAYLOAD_OIDS_WITH_ADDS
                : RemoteCommitEvent.PAYLOAD_OIDS;
            Object oid;
            Object obj;
            OpenJPAStateManager sm;
            for (Iterator itr = trans.iterator(); itr.hasNext();) {
                obj = itr.next();
                sm = broker.getStateManager(obj);

                if (sm == null || !sm.isPersistent() || !sm.isDirty())
                    continue;
                if (sm.isNew() && sm.isDeleted())
                    continue;

                oid = sm.fetchObjectId();
                if (sm.isNew()) {
                    if (_transmitPersIds) {
                        if (persIds == null)
                            persIds = new ArrayList();
                        persIds.add(oid);
                    }
                    if (addClassNames == null)
                        addClassNames = new HashSet();
                    addClassNames.add(obj.getClass().getName());
                } else if (sm.isDeleted()) {
                    if (deletes == null)
                        deletes = new ArrayList();
                    deletes.add(oid);
                } else {
                    if (updates == null)
                        updates = new ArrayList();
                    updates.add(oid);
                }
            }
            if (addClassNames == null && updates == null && deletes == null)
                return null;
        }
        return new RemoteCommitEvent(payload, persIds, addClassNames, updates,
            deletes);
    }

    /**
     * Transform a collection of classes to class names.
     */
    private static Collection toClassNames(Collection clss) {
        if (clss.isEmpty())
            return null;

        List names = new ArrayList(clss);
        for (int i = 0; i < names.size(); i++)
            names.set(i, ((Class) names.get(i)).getName());
        return names;
    }

    public void beforeCommit(TransactionEvent event) {
    }

    public void afterRollback(TransactionEvent event) {
    }

    public void afterCommitComplete(TransactionEvent event) {
    }

    public void afterRollbackComplete(TransactionEvent event) {
    }

    public void afterStateTransitions(TransactionEvent event)
	{
	}
}

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
package org.apache.openjpa.persistence;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.apache.openjpa.event.DeleteListener;
import org.apache.openjpa.event.LifecycleCallbacks;
import org.apache.openjpa.event.LifecycleEvent;
import org.apache.openjpa.event.LifecycleEventManager;
import org.apache.openjpa.event.LoadListener;
import org.apache.openjpa.event.PersistListener;
import org.apache.openjpa.event.PostPersistListener;
import org.apache.openjpa.event.UpdateListener;
import org.apache.openjpa.event.PostDeleteListener;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.CallbackException;

class PersistenceListenerAdapter
    implements LifecycleEventManager.ListenerAdapter, PersistListener,
    PostPersistListener, LoadListener, UpdateListener, DeleteListener,
    PostDeleteListener {

    private static final Localizer _loc = Localizer.forPackage
        (PersistenceListenerAdapter.class);

    private final LifecycleCallbacks[][] _callbacks;

    public PersistenceListenerAdapter(LifecycleCallbacks[][] callbacks) {
        _callbacks = callbacks;
    }

    public PersistenceListenerAdapter(Collection<LifecycleCallbacks>[] calls) {
        _callbacks = new LifecycleCallbacks[LifecycleEvent.ALL_EVENTS.length][];
        for (int i = 0; i < LifecycleEvent.ALL_EVENTS.length; i++) {
            if (calls[i] == null)
                continue;
            _callbacks[i] = calls[i].toArray
                (new LifecycleCallbacks[calls[i].size()]);
        }
    }

    public boolean respondsTo(int eventType) {
        return _callbacks[eventType] != null;
    }

    private void makeCallback(LifecycleEvent ev) {
        int eventType = ev.getType();
        if (_callbacks[eventType] == null)
            return;
        Object src = ev.getSource();
        for (LifecycleCallbacks callback : _callbacks[eventType]) {
            try {
                callback.makeCallback(src, ev.getRelated(), eventType);
            } catch (Throwable t) {
                if (t instanceof InvocationTargetException)
                    t = t.getCause();
                if (t instanceof RuntimeException)
                    throw (RuntimeException) t;

                throw new CallbackException(_loc.get("system-listener-err",
                    src)).setCause(t).setFatal(true);
            }
        }
    }

    public void beforePersist(LifecycleEvent event) {
        makeCallback(event);
    }

    public void afterPersist(LifecycleEvent event) {
        throw new UnsupportedOperationException();
    }

    public void afterPersistPerformed(LifecycleEvent event) {
        makeCallback(event);
    }

    public void afterLoad(LifecycleEvent event) {
        makeCallback(event);
    }

    public void afterRefresh(LifecycleEvent event) {
        // no analagous callback
    }

    public void beforeUpdate(LifecycleEvent event) {
        makeCallback(event);
    }

    public void afterUpdatePerformed(LifecycleEvent event) {
        makeCallback(event);
    }

    public void beforeDelete(LifecycleEvent event) {
        makeCallback(event);
    }

    public void afterDelete(LifecycleEvent event) {
        throw new UnsupportedOperationException();
    }

    public void afterDeletePerformed(LifecycleEvent event) {
        makeCallback(event);
    }
}

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
package org.apache.openjpa.lib.util.concurrent;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.openjpa.lib.util.EventManager;

/**
 * Base event manager that handles adding/removing listeners
 * and firing events. Meant for high concurrency. This class is
 * reentrant-safe; listeners can be added and removed by other listeners when 
 * they receive events. The changes will not be visible until the event fire
 * that initiated the recursive sequence of calls completes, however.
 *
 * @author Abe White
 */
public abstract class AbstractConcurrentEventManager
    implements EventManager, Serializable {

    private static final Exception[] EMPTY_EXCEPTIONS = new Exception[0];

    protected final Collection _listeners;
    private boolean _failFast = false;

    /**
     * Default constructor.
     */
    public AbstractConcurrentEventManager() {
        _listeners = newListenerCollection();
    }

    /**
     * Whether to fail after the first exception thrown by any listener.
     */
    public boolean isFailFast() {
        return _failFast;
    }

    /**
     * Whether to fail after the first exception thrown by any listener.
     */
    public void setFailFast(boolean failFast) {
        _failFast = failFast;
    }

    /**
     * Register an event listener.
     */
    public void addListener(Object listener) {
        if (listener != null)
            _listeners.add(listener);
    }

    /**
     * Remove an event listener.
     */
    public boolean removeListener(Object listener) {
        return _listeners.remove(listener);
    }

    /**
     * Return whether the given instance is in the list of listeners.
     */
    public boolean hasListener(Object listener) {
        return _listeners.contains(listener);
    }

    /**
     * Return true if there are any registered listeners.
     */
    public boolean hasListeners() {
        return !_listeners.isEmpty();
    }

    /**
     * Return a read-only list of listeners.
     */
    public Collection getListeners() {
        return Collections.unmodifiableCollection(_listeners);
    }

    /**
     * Fire the given event to all listeners.
     */
    public Exception[] fireEvent(Object event) {
        if (_listeners.isEmpty())
            return EMPTY_EXCEPTIONS;

        List exceptions = null;
        for (Iterator itr = _listeners.iterator(); itr.hasNext();) {
            try {
                fireEvent(event, itr.next());
            } catch (Exception e) {
                if (_failFast)
                    return new Exception[] { e };
                if (exceptions == null)
                    exceptions = new LinkedList();
                exceptions.add(e);
            }
        }

        if (exceptions == null)
            return EMPTY_EXCEPTIONS;
        return (Exception[]) exceptions.toArray
            (new Exception[exceptions.size()]);
    }

    /**
     * Implement this method to fire the given event to the given listener.
     */
    protected abstract void fireEvent(Object event, Object listener)
        throws Exception;

    /**
     * Return a new concurrent container for listeners. Uses a 
     * {@link CopyOnWriteArrayList} by default.
     */
    protected Collection newListenerCollection() {
        return new CopyOnWriteArrayList();
    }
}

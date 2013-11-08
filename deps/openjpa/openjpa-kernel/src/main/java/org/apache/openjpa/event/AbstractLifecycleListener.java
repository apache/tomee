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

/**
 * Abstract implementation of the {@link LifecycleListener} interface
 * which delegates events to a single method.
 *
 * @author Steve Kim
 * @author Abe White
 */
public abstract class AbstractLifecycleListener
    implements LifecycleListener {

    /**
     * Should be implemented to handle the specific lifecycle event.
     */
    protected void eventOccurred(LifecycleEvent event) {
    }

    public void beforePersist(LifecycleEvent event) {
        eventOccurred(event);
    }

    public void afterPersist(LifecycleEvent event) {
        eventOccurred(event);
    }

    public void beforeClear(LifecycleEvent event) {
        eventOccurred(event);
    }

    public void afterClear(LifecycleEvent event) {
        eventOccurred(event);
    }

    public void afterLoad(LifecycleEvent event) {
        eventOccurred(event);
    }

    public void beforeDelete(LifecycleEvent event) {
        eventOccurred(event);
    }

    public void afterDelete(LifecycleEvent event) {
        eventOccurred(event);
    }

    public void beforeStore(LifecycleEvent event) {
        eventOccurred(event);
    }

    public void afterStore(LifecycleEvent event) {
        eventOccurred(event);
    }

    public void beforeDirty(LifecycleEvent event) {
        eventOccurred(event);
    }

    public void afterDirty(LifecycleEvent event) {
        eventOccurred(event);
    }

    public void beforeDirtyFlushed(LifecycleEvent event) {
        eventOccurred(event);
    }

    public void afterDirtyFlushed(LifecycleEvent event) {
        eventOccurred(event);
    }

    public void afterRefresh(LifecycleEvent event) {
        eventOccurred(event);
    }

    public void beforeDetach(LifecycleEvent event) {
        eventOccurred(event);
    }

    public void afterDetach(LifecycleEvent event) {
        eventOccurred(event);
    }

    public void beforeAttach(LifecycleEvent event) {
        eventOccurred(event);
    }

    public void afterAttach(LifecycleEvent event) {
        eventOccurred(event);
    }
}

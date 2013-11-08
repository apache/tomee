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
package org.apache.openjpa.persistence.callbacks;

import org.apache.openjpa.event.DeleteListener;
import org.apache.openjpa.event.DirtyListener;
import org.apache.openjpa.event.LifecycleEvent;
import org.apache.openjpa.event.LoadListener;
import org.apache.openjpa.event.PersistListener;
import org.apache.openjpa.event.PostDeleteListener;
import org.apache.openjpa.event.PostPersistListener;
import org.apache.openjpa.event.StoreListener;

public class PerInstanceListener implements PersistListener, DeleteListener, DirtyListener,
        LoadListener, StoreListener, PostPersistListener, PostDeleteListener {

    int beforeStore;
    int afterStore;
    int afterLoad;
    int afterRefresh;
    int beforeDirty;
    int afterDirty;
    int beforeDirtyFlushed;
    int afterDirtyFlushed;
    int beforeDelete;
    int afterDelete;
    int beforePersist;
    int afterPersist;

    @Override
    public void afterDeletePerformed(LifecycleEvent event) {
        afterDelete++;
    }

    @Override
    public void afterPersistPerformed(LifecycleEvent event) {
        afterPersist++;
    }

    @Override
    public void beforeStore(LifecycleEvent event) {
        beforeStore++;
    }

    @Override
    public void afterStore(LifecycleEvent event) {
        afterStore++;
    }

    @Override
    public void afterLoad(LifecycleEvent event) {
        afterLoad++;
    }

    @Override
    public void afterRefresh(LifecycleEvent event) {
        afterRefresh++;
    }

    @Override
    public void beforeDirty(LifecycleEvent event) {
        beforeDirty++;
    }

    @Override
    public void afterDirty(LifecycleEvent event) {
        afterDirty++;
    }

    @Override
    public void beforeDirtyFlushed(LifecycleEvent event) {
        beforeDirtyFlushed++;
    }

    @Override
    public void afterDirtyFlushed(LifecycleEvent event) {
        afterDirtyFlushed++;
    }

    @Override
    public void beforeDelete(LifecycleEvent event) {
        beforeDelete++;
    }

    @Override
    public void afterDelete(LifecycleEvent event) {
        afterDelete++;
    }

    @Override
    public void beforePersist(LifecycleEvent event) {
        beforePersist++;
    }

    @Override
    public void afterPersist(LifecycleEvent event) {
        afterPersist++;
    }
}

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
package org.apache.openjpa.persistence.event;


import org.apache.openjpa.persistence.event.common.apps.RuntimeTest1;
import org.apache.openjpa.persistence.event.common.apps.RuntimeTest2;
import org.apache.openjpa.persistence.event.common.apps.RuntimeTest4;
import org.apache.openjpa.persistence.test.AllowFailure;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;
import org.apache.openjpa.event.LifecycleEvent;
import org.apache.openjpa.event.LifecycleEventManager;
import org.apache.openjpa.event.LoadListener;
import org.apache.openjpa.event.StoreListener;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.OpenJPAPersistence;

/**
 * <p>Test the {@link LifecycleEventManager}.</p>
 *
 * @author Abe White
 */
@AllowFailure(message="surefire excluded")
public class TestLifecycleEventManager
    extends AbstractTestCase {

    public TestLifecycleEventManager(String s) {
        super(s, "eventcactusapp");
    }

    public void testAllClassListener() {
        MetaDataRepository repos =
            ((OpenJPAEntityManagerFactorySPI) OpenJPAPersistence.cast(
                OpenJPAPersistence.createEntityManagerFactory("TestConv2", "")))
                .
                    getConfiguration().getMetaDataRepositoryInstance();
        ClassMetaData meta = repos.getMetaData(RuntimeTest2.class, null, true);
        LifecycleEventManager mgr = new LifecycleEventManager();
        RuntimeTest2 pc = new RuntimeTest2();
        Listener listener = new Listener();

        assertFalse(mgr.hasLoadListeners(pc, meta));
        assertFalse(mgr.hasStoreListeners(pc, meta));

        mgr.addListener(listener, null);
        assertEquals(0, listener.load);
        assertEquals(0, listener.store);
        assertTrue(mgr.hasLoadListeners(pc, meta));
        assertTrue(mgr.hasStoreListeners(pc, meta));
        assertFalse(mgr.hasDirtyListeners(pc, meta));
        assertEquals(0, listener.load);
        assertEquals(0, listener.store);

        Listener listener2 = new Listener();
        mgr.addListener(listener2, null);
        assertTrue(mgr.hasLoadListeners(pc, meta));
        assertTrue(mgr.hasStoreListeners(pc, meta));
        assertFalse(mgr.hasDirtyListeners(pc, meta));

        mgr.fireEvent(pc, meta, LifecycleEvent.AFTER_LOAD);
        assertEquals(1, listener.load);
        assertEquals(0, listener.store);
        assertEquals(0, listener.preStore);
        assertEquals(1, listener2.load);
        assertEquals(0, listener2.store);

        mgr.removeListener(listener2);
        assertTrue(mgr.hasLoadListeners(pc, meta));
        assertTrue(mgr.hasStoreListeners(pc, meta));
        assertFalse(mgr.hasDirtyListeners(pc, meta));

        mgr.fireEvent(pc, meta, LifecycleEvent.AFTER_LOAD);
        assertEquals(2, listener.load);
        assertEquals(0, listener.store);
        assertEquals(0, listener.preStore);
        assertEquals(1, listener2.load);
        assertEquals(0, listener2.store);

        mgr.fireEvent(pc, meta, LifecycleEvent.AFTER_STORE);
        assertEquals(2, listener.load);
        assertEquals(1, listener.store);
        assertEquals(0, listener.preStore);
        assertEquals(1, listener2.load);
        assertEquals(0, listener2.store);

        mgr.fireEvent(pc, meta, LifecycleEvent.BEFORE_STORE);
        assertEquals(2, listener.load);
        assertEquals(2, listener.store);
        assertEquals(1, listener.preStore);
        assertEquals(1, listener2.load);
        assertEquals(0, listener2.store);

        mgr.fireEvent(pc, meta, LifecycleEvent.AFTER_DIRTY);
        assertEquals(2, listener.load);
        assertEquals(2, listener.store);

        mgr.removeListener(listener);
        assertFalse(mgr.hasLoadListeners(pc, meta));
        assertFalse(mgr.hasStoreListeners(pc, meta));
        mgr.fireEvent(pc, meta, LifecycleEvent.AFTER_STORE);
        assertEquals(2, listener.load);
        assertEquals(2, listener.store);
    }

    public void testBaseClassListener() {
        MetaDataRepository repos =
            ((OpenJPAEntityManagerFactorySPI) OpenJPAPersistence.cast(
                OpenJPAPersistence.createEntityManagerFactory("TestConv2", "")))
                .
                    getConfiguration().getMetaDataRepositoryInstance();
        ClassMetaData meta = repos.getMetaData(RuntimeTest2.class, null, true);

        LifecycleEventManager mgr = new LifecycleEventManager();
        RuntimeTest2 pc = new RuntimeTest2();
        Listener listener = new Listener();

        assertFalse(mgr.hasLoadListeners(pc, meta));
        assertFalse(mgr.hasStoreListeners(pc, meta));

        mgr.addListener(listener, new Class[]{ RuntimeTest1.class });
        assertEquals(0, listener.load);
        assertEquals(0, listener.store);
        assertTrue(mgr.hasLoadListeners(pc, meta));
        assertTrue(mgr.hasStoreListeners(pc, meta));
        assertFalse(mgr.hasDirtyListeners(pc, meta));
        assertFalse(mgr.hasLoadListeners(new RuntimeTest4("foo"), meta));
        assertEquals(0, listener.load);
        assertEquals(0, listener.store);

        Listener listener2 = new Listener();
        mgr.addListener(listener2, new Class[]{ RuntimeTest2.class });
        assertTrue(mgr.hasLoadListeners(pc, meta));
        assertTrue(mgr.hasStoreListeners(pc, meta));
        assertFalse(mgr.hasDirtyListeners(pc, meta));
        assertFalse(mgr.hasLoadListeners(new RuntimeTest4("foo"), meta));

        mgr.fireEvent(pc, meta, LifecycleEvent.AFTER_LOAD);
        assertEquals(1, listener.load);
        assertEquals(0, listener.store);
        assertEquals(1, listener2.load);
        assertEquals(0, listener2.store);

        mgr.fireEvent(pc, meta, LifecycleEvent.AFTER_LOAD);
        assertEquals(2, listener.load);
        assertEquals(0, listener.store);
        assertEquals(2, listener2.load);
        assertEquals(0, listener2.store);

        mgr.fireEvent(new RuntimeTest1(), meta, LifecycleEvent.AFTER_LOAD);
        assertEquals(3, listener.load);
        assertEquals(0, listener.store);
        assertEquals(2, listener2.load);
        assertEquals(0, listener2.store);

        mgr.removeListener(listener2);
        assertTrue(mgr.hasLoadListeners(pc, meta));
        assertTrue(mgr.hasStoreListeners(pc, meta));

        mgr.fireEvent(pc, meta, LifecycleEvent.AFTER_STORE);
        assertEquals(3, listener.load);
        assertEquals(1, listener.store);
        assertEquals(2, listener2.load);
        assertEquals(0, listener2.store);

        mgr.fireEvent(pc, meta, LifecycleEvent.AFTER_DIRTY);
        assertEquals(3, listener.load);
        assertEquals(1, listener.store);

        mgr.fireEvent(new RuntimeTest4("foo"), meta,
            LifecycleEvent.AFTER_STORE);
        assertEquals(3, listener.load);
        assertEquals(1, listener.store);

        mgr.removeListener(listener);
        assertFalse(mgr.hasLoadListeners(pc, meta));
        assertFalse(mgr.hasStoreListeners(pc, meta));
        mgr.fireEvent(pc, meta, LifecycleEvent.AFTER_STORE);
        assertEquals(3, listener.load);
        assertEquals(1, listener.store);
    }

    private static class Listener
        implements LoadListener, StoreListener {

        public int load = 0;
        public int preStore = 0;
        public int store = 0;

        public void afterLoad(LifecycleEvent ev) {
            load++;
        }

        public void afterRefresh(LifecycleEvent ev) {
            // TODO
        }

        public void beforeStore(LifecycleEvent ev) {
            preStore++;
            store++;
        }

        public void afterStore(LifecycleEvent ev) {
            store++;
        }
    }
}

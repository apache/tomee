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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


import org.apache.openjpa.persistence.event.common.apps.RuntimeTest1;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;

import org.apache.openjpa.event.RemoteCommitEvent;
import org.apache.openjpa.event.RemoteCommitListener;
import org.apache.openjpa.lib.conf.Configurations;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactory;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.OpenJPAEntityManagerSPI;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.StoreCache;
import org.apache.openjpa.util.Id;

public class RemoteEventBase extends AbstractTestCase {

    public Id roid;
    public StoreCache datacatch;

    public RemoteEventBase(String s) {
        super(s, "eventcactusapp");
    }

    @Override
    public void setUp() {
        deleteAll(RuntimeTest1.class);
        datacatch.evictAll();
    }

    @Override
    public void tearDown() throws Exception {
        ((OpenJPAEntityManagerSPI) OpenJPAPersistence
            .cast(currentEntityManager())).getConfiguration()
            .getRemoteCommitEventManager().close();
        super.tearDown();
    }

    protected void doTest(Class providerClass, String classProps1,
        String classProps2) {
        String transmit = "TransmitPersistedObjectIds=true";
        if (classProps1 == null || classProps1.length() == 0)
            classProps1 = transmit;
        else
            classProps1 += "," + transmit;

        Map propsMap = new HashMap();
        propsMap.put("openjpa.RemoteCommitProvider",
            Configurations.getPlugin(providerClass.getName(), classProps1));
        propsMap.put("openjpa.FetchGroups", "differentiatingFetchGroup1");
        propsMap.put("openjpa.DataCache", "true");
        OpenJPAEntityManagerFactory factory1 = getEmf(propsMap);

        TriggerRemoteCommitListener listener1 =
            new TriggerRemoteCommitListener();
        ((OpenJPAEntityManagerFactorySPI) factory1).getConfiguration()
            .getRemoteCommitEventManager().addListener(listener1);

        if (classProps2 == null || classProps2.length() == 0)
            classProps2 = transmit;
        else
            classProps2 += ", " + transmit;

        propsMap = new HashMap();
        propsMap.put("openjpa.RemoteCommitProvider",
            Configurations.getPlugin(providerClass.getName(), classProps2));
        propsMap.put("openjpa.FetchGroups", "differentiatingFetchGroup2");
        propsMap.put("openjpa.DataCache", "true");
        OpenJPAEntityManagerFactory factory2 = getEmf(propsMap);

        RemoteCommitListenerTestImpl listener2 =
            new RemoteCommitListenerTestImpl();
        ((OpenJPAEntityManagerFactorySPI) factory2).getConfiguration()
            .getRemoteCommitEventManager().addListener(listener2);

        OpenJPAEntityManager pm =
            (OpenJPAEntityManager) factory1.createEntityManager();
        datacatch = pm.getEntityManagerFactory().getStoreCache();
        // get an object id
        RuntimeTest1 t1 = new RuntimeTest1("foo", 5);
        startTx(pm);
        pm.persist(t1);
        Object oid = pm.getObjectId(t1);
        roid = Id.newInstance(RuntimeTest1.class, oid);
        endTx(pm);

        try {
            Thread.currentThread().sleep(250);
        }
        catch (InterruptedException ie) {
        }

        // ensure that the commit info was not propagated to factory1.
        assertFalse(listener1.commitNotificationReceived);

        // ensure that the commit info propagated to the
        // factories correctly.
        assertNotNull(listener2.added);
        assertNotNull(listener2.updated);
        assertNotNull(listener2.deleted);

        boolean pass = false;
        for (Iterator iter = listener2.added.iterator(); iter.hasNext();) {
            Id roid = Id.newInstance(RuntimeTest1.class, oid);
            Id it = (Id) iter.next();
            //FixMe --det. why it.equals(roid) fails when the are actually equal
            if (it.toString().equals(roid.toString())) {
                pass = true;
                break;
            }
        }
        assertTrue("pass = " + pass, pass);
        assertTrue(listener2.updated.size() == 0);
        assertTrue(listener2.deleted.size() == 0);

        // modify an object
        startTx(pm);
        t1.setStringField("baz");
        endTx(pm);

        try {
            Thread.currentThread().sleep(250);
        }
        catch (InterruptedException ie) {
        }

        // ensure that the commit info was not propagated to factory1.
        assertFalse(listener1.commitNotificationReceived);

        // ensure that the commit info propagated to the remote
        // factories correctly.
        assertNotNull(listener2.added);
        assertNotNull(listener2.updated);
        assertNotNull(listener2.deleted);

        pass = false;
        for (Iterator iter = listener2.updated.iterator(); iter.hasNext();) {
            Id it = (Id) iter.next();
            if (it.toString().equals(roid.toString())) {
                pass = true;
                break;
            }
        }
        assertTrue(pass);
        assertTrue(listener2.added.size() == 0);
        assertTrue(listener2.deleted.size() == 0);

        // delete an object
        startTx(pm);
        pm.remove(t1);
        endTx(pm);

        try {
            Thread.currentThread().sleep(250);
        }
        catch (InterruptedException ie) {
        }

        // ensure that the commit info was not propagated to factory1.
        assertFalse(listener1.commitNotificationReceived);

        // ensure that the commit info propagated to the remote
        // factories correctly.
        assertNotNull(listener2.added);
        assertNotNull(listener2.updated);
        assertNotNull(listener2.deleted);

        pass = false;
        for (Iterator iter = listener2.deleted.iterator(); iter.hasNext();) {
            Id it = (Id) iter.next();
            if (it.toString().equals(roid.toString())) {
                pass = true;
                break;
            }
        }
        assertTrue(pass);
        assertTrue(listener2.added.size() == 0);
        assertTrue(listener2.updated.size() == 0);
    }

    protected static class RemoteCommitListenerTestImpl implements
        RemoteCommitListener {

        transient Collection added;

        transient Collection updated;

        transient Collection deleted;

        public void afterCommit(RemoteCommitEvent event) {
            this.added = event.getPersistedObjectIds();
            this.updated = event.getUpdatedObjectIds();
            this.deleted = event.getDeletedObjectIds();
        }

        public void close() {
        }
    }

    protected static class TriggerRemoteCommitListener
        implements RemoteCommitListener {

        boolean commitNotificationReceived = false;

        public void afterCommit(RemoteCommitEvent event) {
            commitNotificationReceived = true;
        }

        public void close() {
        }
    }
}
// looks like this might be creating another factory that is
// connecting to the same ports, causing the failure. Should
// probably debug by putting a third conf in RemoteEventBase.

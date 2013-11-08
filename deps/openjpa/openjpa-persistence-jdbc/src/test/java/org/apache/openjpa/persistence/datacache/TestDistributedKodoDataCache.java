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
package org.apache.openjpa.persistence.datacache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


import org.apache.openjpa.persistence.datacache.common.apps.RuntimeTest1;
import org.apache.openjpa.persistence.datacache.common.apps.RuntimeTest2;
import org.apache.openjpa.persistence.test.AllowFailure;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;
import org.apache.openjpa.datacache.DataCache;
import org.apache.openjpa.event.TCPRemoteCommitProvider;
import org.apache.openjpa.lib.conf.Configurations;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactory;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.util.Id;

@AllowFailure(message="surefire excluded")
public class TestDistributedKodoDataCache extends AbstractTestCase {

    // We want more than 1 instance of each type of class.
    private static final int NUM_OBJECTS = 4;

    private Object[] _runtime1sOids;

    private Object[] _runtime2sOids;

    private Object spec_oid;

    public TestDistributedKodoDataCache(String test) {
        super(test, "datacachecactusapp");
    }

    public void setUp() {
        deleteAll(RuntimeTest1.class);
        deleteAll(RuntimeTest2.class);
    }

    private interface ChangeOperation {

        public String getName();

        public void operation(OpenJPAEntityManagerFactory kpmf,
            boolean asLarge);
    }

    private class performAsModify implements ChangeOperation {

        public String getName() {
            return "Modify SpecialRuntimeTest1";
        }

        public void operation(OpenJPAEntityManagerFactory kpmf,
            boolean asLarge) {
            OpenJPAEntityManager pm;
            pm = (OpenJPAEntityManager) kpmf.createEntityManager();
            if (asLarge)
                //pm.setLargeTransaction(true);
                pm.setTrackChangesByType(true);
            startTx(pm);
            RuntimeTest1 special = pm.find(RuntimeTest1.class, spec_oid);
            assertNotNull(special);
            special.setStringField("SpeicalRuntimeTest1_MODIFIED");

            endTx(pm);
            endEm(pm);
        }
    }

    private class performAsDelete implements ChangeOperation {

        public String getName() {
            return "Delete SpecialRuntimeTest1";
        }

        public void operation(OpenJPAEntityManagerFactory kpmf,
            boolean asLarge) {
            OpenJPAEntityManager pm;
            pm = (OpenJPAEntityManager) kpmf.createEntityManager();
            if (asLarge)
                //pm.setLargeTransaction(true);
                pm.setTrackChangesByType(true);
            startTx(pm);
            RuntimeTest1 specialObj = pm.find(RuntimeTest1.class, spec_oid);
            assertNotNull(specialObj);

            pm.remove(specialObj);

            endTx(pm);
            endEm(pm);
        }
    }

    public void testNormalTransAndDataCacheDelete() {
        coreTestTransAndChange(new performAsDelete(), false, true);
    }

    public void testNormalTransAndDataCacheModify() {
        coreTestTransAndChange(new performAsModify(), false, false);
    }

    public void testLargeTransAndDataCacheDelete() {
        coreTestTransAndChange(new performAsDelete(), true, true);
    }

    public void testLargeTransAndDataCacheModify() {
        coreTestTransAndChange(new performAsModify(), true, false);
    }

    public void coreTestTransAndChange(ChangeOperation changeOperation,
        boolean asLargeTransaction, boolean isDelete) {
        // Create two pmfs in a cluster that are using RCPTCP.
        OpenJPAEntityManagerFactory pmfSender = createDistinctFactory(
            TCPRemoteCommitProvider.class,
            "Port=5636, Addresses=127.0.0.1:6636");
        OpenJPAEntityManagerFactory pmfReceiver = createDistinctFactory(
            TCPRemoteCommitProvider.class,
            "Port=6636, Addresses=127.0.0.1:5636");
        // Get the datacaches from each pmf
        DataCache dcSender =
            ((OpenJPAEntityManagerFactorySPI) pmfSender).getConfiguration()
                .getDataCacheManagerInstance().getSystemDataCache();
        DataCache dcReceiver =
            ((OpenJPAEntityManagerFactorySPI) pmfReceiver).getConfiguration()
                .getDataCacheManagerInstance().getSystemDataCache();

        deleteAll(RuntimeTest1.class);
        deleteAll(RuntimeTest2.class);

        _runtime1sOids = null;
        _runtime2sOids = null;

        String transType = "normal";
        String rcpType = "OIDs";
        if (asLargeTransaction) {
            transType = "large";
            rcpType = "classes";
        }
        System.out.println("-------------------");
        System.out.println(
            "2 PMFs created, acting as a cluster using ports 5636 and 6636");
        System.out.println("Testing scenario:");
        System.out
            .println("  1 Seed datastore with instances of RuntimeTest1 AND "
                + "RuntimeTest2 objs.\n"
                + "  2 pmf2 fills its cache with both sets of objs.\n"
                + "  3 pmf1 does a "
                + transType
                + " tx that invokes an operation of "
                + changeOperation.getName()
                + " to affect a single Runtime1 \n"
                + "    assert that both pmf1 and pmf2's data caches dropped all"
                + "Runtime1s");
        System.out.println("Remote commit event is transmiting " + rcpType);

        // Create runtimes1s and 2s.
        // Noraml transaction
        OpenJPAEntityManager pmSender = (OpenJPAEntityManager) pmfSender
            .createEntityManager();
        seedDataStore(pmSender, NUM_OBJECTS);
        endEm(pmSender);

        // wait a bit so they get stored
        pause(1);

        OpenJPAEntityManager pm2;
        pm2 = (OpenJPAEntityManager) pmfReceiver.createEntityManager();
        performLoadAll(pm2);
        endEm(pm2);

        pmSender = (OpenJPAEntityManager) pmfSender.createEntityManager();
        performLoadAll(pmSender);
        endEm(pmSender);

        // assert that pmf2's data cache now has all the Runtime1 and 2s.
        for (int i = 0; i < _runtime1sOids.length; i++) {
            assertTrue(dcReceiver.contains(
                Id.newInstance(RuntimeTest1.class, _runtime1sOids[i])));
        }
        for (int i = 0; i < _runtime2sOids.length; i++) {
            assertTrue(dcReceiver.contains(
                Id.newInstance(RuntimeTest2.class, _runtime2sOids[i])));
        }

        // Modify or delete exactly 1 RuntimeTest1 object during a
        // largeTransaction
        changeOperation.operation(pmfSender, asLargeTransaction);

        // assert that pmf1's data cache now only has Runtime2 objects
        if (asLargeTransaction) {
            for (int i = 0; i < _runtime1sOids.length; i++) {
                assertFalse(dcSender.contains(
                    Id.newInstance(RuntimeTest1.class, _runtime1sOids[i])));
            }
        } else {
            // Normal transaction
            for (int i = 0; i < _runtime1sOids.length; i++) {
                if (isDelete && i == 0) {
                    assertFalse(dcSender.contains(
                        Id.newInstance(RuntimeTest1.class, _runtime1sOids[i])));
                } else {
                    // modified the first elemnt, which just updated it.
                    // (for Kodo's data cache).
                    assertTrue(dcSender.contains(
                        Id.newInstance(RuntimeTest1.class, _runtime1sOids[i])));
                }
            }
        }
        for (int i = 0; i < _runtime2sOids.length; i++) {
            assertTrue(dcSender.contains(
                Id.newInstance(RuntimeTest2.class, _runtime2sOids[i])));
        }
        // wait a tiny bit so the rce propagates
        pause(2);
        // assert the pmf2's data cache also now only has Runtime2 objects
        if (asLargeTransaction) {
            for (int i = 0; i < _runtime1sOids.length; i++) {
                assertFalse(dcReceiver.contains(Id.newInstance(
                    RuntimeTest1.class, _runtime1sOids[i]))); //failing here
            }
        } else {
            for (int i = 0; i < _runtime1sOids.length; i++) {
                if (i == 0) {
                    assertFalse(dcReceiver.contains(Id.newInstance(
                        RuntimeTest1.class,
                        _runtime1sOids[i])));  //failing here
                } else {
                    assertTrue(dcReceiver.contains(
                        Id.newInstance(RuntimeTest1.class, _runtime1sOids[i])));
                }
            }
        }
        for (int i = 0; i < _runtime2sOids.length; i++) {
            assertTrue(dcReceiver.contains(
                Id.newInstance(RuntimeTest2.class, _runtime2sOids[i])));
        }

        // shutdown
        pmfSender.close();
        pmfReceiver.close();
    }

    protected void performLoadAll(OpenJPAEntityManager pm)
    // load in (and thus cache) all the 1s and 2s
    {
        startTx(pm);
        RuntimeTest1 temp1;
        Collection runtime1s = (Collection) pm
            .createQuery("SELECT a FROM RuntimeTest1 a").getResultList();

        for (Iterator itr = runtime1s.iterator(); itr.hasNext();)
            temp1 = (RuntimeTest1) itr.next();
        RuntimeTest2 temp2;
        Collection runtime2s = (Collection) pm
            .createQuery("SELECT a FROM RuntimeTest2 a").getResultList();

        for (Iterator itr = runtime2s.iterator(); itr.hasNext();)
            temp2 = (RuntimeTest2) itr.next();
        endTx(pm);
    }

    protected void seedDataStore(OpenJPAEntityManager pm, int numObjects) {
        startTx(pm);
        // create objects
        RuntimeTest1[] persistables = new RuntimeTest1[numObjects];
        _runtime1sOids = new Object[numObjects];
        for (int i = 0; i < persistables.length; i++) {
            persistables[i] = new RuntimeTest1("foo #" + i, i);
            pm.persist(persistables[i]);
            _runtime1sOids[i] = pm.getObjectId(persistables[i]);
            if (i == 0) {
                persistables[i].setStringField("SpecialRuntimeTest1");
                spec_oid = pm.getObjectId(persistables[i]);
            }
        }
        RuntimeTest2[] persistables2 = new RuntimeTest2[numObjects];
        _runtime2sOids = new Object[numObjects];
        for (int i = 0; i < persistables2.length; i++) {
            persistables2[i] = new RuntimeTest2("bar #" + i, i);
            pm.persist(persistables2[i]);
            _runtime2sOids[i] = pm.getObjectId(persistables2[i]);
        }
        endTx(pm);
    }

    static int _fetchGroupSerial = 0;

    protected OpenJPAEntityManagerFactory createDistinctFactory(
        Class providerClass, String classProps1) {
        Map propsMap;

        if (providerClass != null) {
            // This test is for the combination of RCP, largeTrans,
            // and Kodo's builtin DataCache.
            // use this property to differentiate the factory

            propsMap = new HashMap();
            propsMap.put("openjpa.DataCache", "lru");
            propsMap.put("openjpa.RemoteCommitProvider",
                Configurations.getPlugin(providerClass.getName(), classProps1));
            propsMap.put("openjpa.FetchGroups", "differentiatingFetchGroup"
                + _fetchGroupSerial);
        } else {
            // No RCP
            propsMap = new HashMap();
            propsMap.put("openjpa.RemoteCommitProvider", "sjvm");
            propsMap.put("openjpa.FetchGroups", "differentiatingFetchGroup"
                + _fetchGroupSerial);
        }
        _fetchGroupSerial += 1;

        return (OpenJPAEntityManagerFactory) getEmf(propsMap);
    }

    private void pause(double seconds) {
        try {
            Thread.currentThread().yield();
            Thread.currentThread().sleep((int) seconds * 1000);
        }
        catch (Exception e) {
        }
    }
}

/*
 remove all Runteim1, and 2
 create a few 1 and 2s
 get them into the cache, assert
 large transaction, and modify a runtime2, assert cache only has runtime1s


 remove all Runteim1, and 2
 create a few 1 and 2s
 get them into the cache, assert
 large transaction, delete a runtime2, assert cache only has runteime1s
 */


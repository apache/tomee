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
import java.util.Map;
import javax.persistence.EntityManager;


import org.apache.openjpa.persistence.event.common.apps.Duration;
import org.apache.openjpa.persistence.event.common.apps.RuntimeTest1;
import org.apache.openjpa.persistence.test.AllowFailure;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;

import org.apache.openjpa.event.RemoteCommitEvent;
import org.apache.openjpa.event.RemoteCommitListener;
import org.apache.openjpa.event.TCPRemoteCommitProvider;
import org.apache.openjpa.lib.conf.Configurations;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactory;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;

@AllowFailure(message="surefire excluded")
public class TestTCPRemoteClassChanges
    extends AbstractTestCase {

    public TestTCPRemoteClassChanges(String s) {
        super(s, "eventcactusapp");
    }

    public void setUp() {
        deleteAll(RuntimeTest1.class);
    }

    public void tearDownTestClass()
        throws Exception {
        //super.tearDownTestClass();
    }

    private static final int NUM_OBJECTS = 4;

    private void pause(double seconds) {
        try {
            Thread.currentThread().yield();
            Thread.currentThread().sleep((int) seconds * 1000);
        } catch (Exception e) {
        }
    }

    public void testAddedClasses() {
        // Create two pmfs in a cluster that are using RCPTCP.
        OpenJPAEntityManagerFactory pmfSender = createDistinctFactory(
            TCPRemoteCommitProvider.class,
            "Port=5636, Addresses=127.0.0.1:5636;127.0.0.1:6636");
        OpenJPAEntityManagerFactory pmfReceiver = createDistinctFactory(
            TCPRemoteCommitProvider.class,
            "Port=6636, Addresses=127.0.0.1:6636;127.0.0.1:5636");

        // Register a RCEListener with the RCEM. Our RCEListener will
        // record the total number of a,d, and u recevied from each
        // RCE as the sending PM performs commits.
        RemoteCommitListenerTestImpl listenerAtReceiver
            = new RemoteCommitListenerTestImpl();
        ((OpenJPAEntityManagerFactorySPI) pmfReceiver).getConfiguration()
            .getRemoteCommitEventManager().
            addListener(listenerAtReceiver);

        OpenJPAEntityManager pmSender = (OpenJPAEntityManager)
            pmfSender.createEntityManager();

        System.out.println("-------------------");
        System.out.println(
            "2 PMFs created, acting as a cluster using ports 5636 and 6636");
        System.out.println(
            "Testing scenario:");
        System.out.println(
            "  - tx of inserts (normal trans)\n" +
                "  - tx of inserts (large trans)\n" +
                "  - tx of inserts,updates, dels (large trans)");
        System.out.println(
            "Remote commit event will transmit classes.");

        // Perform transaction that adds objects.
        // Noraml transaction
        performAdds(pmSender, NUM_OBJECTS);

        // Wait for a bit so the receiver can get the event.
        pause(1);
        assertEquals(NUM_OBJECTS, listenerAtReceiver.totalAddedClasses);

        // largeTransaction
        //pmSender.setLargeTransaction(true);
        pmSender.setTrackChangesByType(true);
        performAdds(pmSender, NUM_OBJECTS);
        pause(1);
        assertEquals(2 * NUM_OBJECTS, listenerAtReceiver.totalAddedClasses);
        assertEquals(0, listenerAtReceiver.totalUpdatedClasses);
        assertEquals(0, listenerAtReceiver.totalDeletedClasses);
        assertEquals(NUM_OBJECTS, listenerAtReceiver.receivedExtentEvCount);

        // Still in large trans mode, perform updates and deletes of
        // check that class names are communicated

        //pmSender.setLargeTransaction(true);
        pmSender.setTrackChangesByType(true);
        performAddsModifiesDeletes(pmSender, NUM_OBJECTS);
        pause(1);
        assertEquals(3 * NUM_OBJECTS, listenerAtReceiver.totalAddedClasses);
        assertEquals(NUM_OBJECTS, listenerAtReceiver.totalUpdatedClasses);
        // all deletes then a commit
        assertEquals(NUM_OBJECTS, listenerAtReceiver.totalDeletedClasses);
        assertEquals(4 * NUM_OBJECTS,
            listenerAtReceiver.receivedExtentEvCount);
        assertEquals(0, listenerAtReceiver.totalOidUpdated);
        assertEquals(0, listenerAtReceiver.totalOidDeleted);
        assertEquals(0, listenerAtReceiver.totalOidAdded);

        // shutdown
        pmSender.close();
        pmfSender.close();
        pmfReceiver.close();
    }

    protected double performAdds(EntityManager pm,
        int numObjects) {
        // Perform a transaction that will trigger adds
        Duration timeToAMD = new Duration(
            "Adds, " + numObjects + " objects.");
        timeToAMD.start();

        // create objects
        RuntimeTest1[] persistables = new RuntimeTest1[numObjects];
        for (int i = 0; i < persistables.length; i++) {
            persistables[i] = new RuntimeTest1("foo #" + i, i);
        }

        // add them, a commit per object
        for (int i = 0; i < persistables.length; i++) {
            startTx(pm);
            pm.persist(persistables[i]);
            endTx(pm);
        }

        timeToAMD.stop();
        return timeToAMD.getDurationAsSeconds();
    }

    protected void performAddsModifiesDeletes(EntityManager pm,
        int numObjects) {
        // Perform a series of transactions that will trigger adds,
        // deletes, and udpates

        // create objects
        RuntimeTest1[] persistables = new RuntimeTest1[numObjects];
        for (int i = 0; i < persistables.length; i++) {
            persistables[i] = new RuntimeTest1("foo #" + i, i);
        }

        // add them
        for (int i = 0; i < persistables.length; i++) {
            startTx(pm);
            pm.persist(persistables[i]);
            endTx(pm);
        }

        // modify them
        for (int i = 0; i < persistables.length; i++) {
            startTx(pm);
            persistables[i].setStringField("bazzed" + i);
            endTx(pm);
        }

        // delete them
        for (int i = 0; i < persistables.length; i++) {
            startTx(pm);
            pm.remove(persistables[i]);
            endTx(pm);
        }
    }

    static int _fetchGroupSerial = 0;

    protected OpenJPAEntityManagerFactory createDistinctFactory(
        Class providerClass, String classProps1) {
        Map propsMap;

        if (providerClass != null) {

            propsMap = new HashMap();
            propsMap.put("openjpa.RemoteCommitProvider", Configurations.
                getPlugin(providerClass.getName(), classProps1));
            propsMap.put("openjpa.FetchGroups", "differentiatingFetchGroup" +
                _fetchGroupSerial);
        } else {
            // No RCP
            propsMap = new HashMap();
            propsMap.put("openjpa.RemoteCommitProvider", "sjvm");
            propsMap.put("openjpa.FetchGroups", "differentiatingFetchGroup" +
                _fetchGroupSerial);
        }
        _fetchGroupSerial += 1;

        return (OpenJPAEntityManagerFactory) getEmf(propsMap);
    }

    protected static class RemoteCommitListenerTestImpl
        implements RemoteCommitListener {

        Collection updated;
        Collection deleted;
        int totalAddedClasses = 0;
        int totalUpdatedClasses = 0;
        int totalDeletedClasses = 0;

        int totalOidAdded = 0;
        int totalOidUpdated = 0;
        int totalOidDeleted = 0;

        int receivedExtentEvCount = 0;

        public synchronized void afterCommit(RemoteCommitEvent event) {
            totalAddedClasses += event.getPersistedTypeNames().size();
            if (event.getPayloadType() == RemoteCommitEvent.PAYLOAD_EXTENTS) {
                receivedExtentEvCount += 1;
                totalUpdatedClasses += event.getUpdatedTypeNames().size();
                totalDeletedClasses += event.getDeletedTypeNames().size();
            } else {
                if (event.getPayloadType() ==
                    RemoteCommitEvent.PAYLOAD_OIDS_WITH_ADDS)
                    totalOidAdded = event.getPersistedObjectIds().size();
                this.updated = event.getUpdatedObjectIds();
                this.deleted = event.getDeletedObjectIds();
                totalOidUpdated += updated.size();
                totalOidDeleted += deleted.size();
            }
        }

        public void resetCounts() {
            totalAddedClasses = 0;
            totalUpdatedClasses = 0;
            totalDeletedClasses = 0;
            totalOidAdded = 0;
            totalOidUpdated = 0;
            totalOidDeleted = 0;
        }

        public void close() {
        }

        public String toString() {
            String returnString = "Clsses add=" + totalAddedClasses + " dels=" +
                totalDeletedClasses + " ups=" + totalUpdatedClasses;
            returnString = returnString + "Oids add=" + totalAddedClasses +
                " dels=" + totalDeletedClasses + " ups=" + totalUpdatedClasses;
            return returnString;
        }
    }
}

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
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactory;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;

@AllowFailure(message="surefire excluded")
public class TestTCPRemoteRecoveryTransmitAdds
    extends AbstractTestCase {

    public TestTCPRemoteRecoveryTransmitAdds(String s) {
        super(s, "eventcactusapp");
    }

    public void setUp() {
        deleteAll(RuntimeTest1.class);
    }

    public void tearDownTestClass()
        throws Exception {
        //super.tearDownTestClass();
    }

    private static final int NUM_OBJECTS = 1;

    private void pause(double seconds) {
        try {
            Thread.currentThread().yield();
            Thread.currentThread().sleep((int) seconds * 1000);
        } catch (Exception e) {
        }
    }

    public void testReceiverRecovers() {
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

        EntityManager pmSender = pmfSender.createEntityManager();

        System.out.println("-------------------");
        System.out.println("2 PMFs created, acting as a cluster using ports " +
            "5636 and 6636");
        System.out.println("Testing scenario where receiver is failed, then " +
            "recovered ");
        System.out.println("after two timeouts all the while with the " +
            "sending pm continuing");
        System.out.println("to send.");

        // Perform a set of transactions. Events will be communicated
        performAddsModifiesDeletes(pmSender, NUM_OBJECTS);

        // Wait for a bit so the receiver can get the event.
        pause(3);
        // Now Fail the receiver in the cluster
        System.out.println("About to close the receiving pmf.");
        pmfReceiver.close();
        // Wait for a bit longer so the listener's threads all
        // get closed out.
        pause(3);
        assertEquals(NUM_OBJECTS, listenerAtReceiver.totalAdded);
        assertEquals(NUM_OBJECTS, listenerAtReceiver.totalDeleted);
        assertEquals(NUM_OBJECTS, listenerAtReceiver.totalUpdated);

        System.out.println("You should now see 1 WARN triggered as the " +
            "sender-pmf tries to send.");
        // Perform second set of transactions. This will trigger a single
        // log WARN as the pmf won't be able to communciate events to the
        // second member of the cluster.
        performAddsModifiesDeletes(pmSender, NUM_OBJECTS);

        // Wait for a recoverytime, try transactions again, this will
        // trigger an INFO
        pause(15.1);
        System.out.println("Waited for a while. Should see 1 INFO for next " +
            "transaction.");

        // This will trigger a single log INFO
        performAddsModifiesDeletes(pmSender, NUM_OBJECTS);
        // This delay should ensure this second sent of
        // transmissions is dropped as expected. If we
        // don't pause, the new pmf can be created, then the
        // events will be sent by the worker threads, and
        // the new pmf will receive this messages (which
        // are supposed to be dropped)
        pause(1.1);

        // -----
        // Now recovery the Receiver and test that messages
        // resume being delivered.
        // -----

        System.out.println("Recovering receiver pmf.");
        // Recreate the listener pmf of the cluster.
        pmfReceiver = createDistinctFactory(
            TCPRemoteCommitProvider.class,
            "Port=6636, Addresses=127.0.0.1:6636;127.0.0.1:5636");
        pause(1.0);
        // reRegister the same listener
        ((OpenJPAEntityManagerFactorySPI) pmfReceiver).getConfiguration()
            .getRemoteCommitEventManager().
            addListener(listenerAtReceiver);
        assertEquals(NUM_OBJECTS, listenerAtReceiver.totalAdded);
        assertEquals(NUM_OBJECTS, listenerAtReceiver.totalDeleted);
        assertEquals(NUM_OBJECTS, listenerAtReceiver.totalUpdated);

        System.out.println("Now waiting a recoverytime so that the sender");
        System.out.println("will resume trying to connect to the receiver.");
        pause(15.1);

        // These events should get communicated.
        performAddsModifiesDeletes(pmSender, NUM_OBJECTS);

        // Wait for a last little bit so the listener thread in
        // the receiver PMF can get all messages.
        pause(1.0);
        assertEquals(2 * NUM_OBJECTS, listenerAtReceiver.totalAdded);
        assertEquals(2 * NUM_OBJECTS, listenerAtReceiver.totalDeleted);
        assertEquals(2 * NUM_OBJECTS, listenerAtReceiver.totalUpdated);

        // shutdown
        pmSender.close();
        pmfSender.close();
        pmfReceiver.close();
    }

    public void testSenderRecovers() {
        // Create two pmfs in a cluster that are using RCPTCP.
        OpenJPAEntityManagerFactory pmfSender = createDistinctFactory(
            TCPRemoteCommitProvider.class,
            "Port=5637, Addresses=127.0.0.1:5637;127.0.0.1:6637");
        OpenJPAEntityManagerFactory pmfReceiver = createDistinctFactory(
            TCPRemoteCommitProvider.class,
            "Port=6637, Addresses=127.0.0.1:6637;127.0.0.1:5637");

        RemoteCommitListenerTestImpl listenerAtReceiver
            = new RemoteCommitListenerTestImpl();
        ((OpenJPAEntityManagerFactorySPI) pmfReceiver).getConfiguration()
            .getRemoteCommitEventManager().
            addListener(listenerAtReceiver);

        EntityManager pmSender = pmfSender.createEntityManager();

        System.out.println("-------------------");
        System.out.println("2 PMFs created, acting as a cluster using ports " +
            "5637 and 6637");
        System.out.println("Testing scenario where sender fails and then " +
            "later recovers.");
        System.out.println("All the while the receiving pm stays up and " +
            "should receive");
        System.out.println("Events (both before and after the sender's " +
            "failure).");

        // Perform a set of transactions. Events in the cluster will be
        // communicated
        performAddsModifiesDeletes(pmSender, NUM_OBJECTS);

        // Wait for a bit so the sockets in our sender PMF can fully transmit
        // their Event messages to the receiver PMF.
        pause(2.1);
        // Fail the Sender in our cluster
        System.out.println("Sender pmf closed.");
        pmSender.close();
        pmfSender.close();

        // Wait for a while, try again, this will let close exception propagate
        pause(4.1);
        System.out.println("Waited for a while.");
        System.out.println("Recovering the sender pmf.");

        pmfSender = createDistinctFactory(
            TCPRemoteCommitProvider.class,
            "Port=5637, Addresses=127.0.0.1:5637;127.0.0.1:6637");
        pmSender = pmfSender.createEntityManager();
        // Perform a second set of transactions. Events in the cluster will be
        // communicated
        performAddsModifiesDeletes(pmSender, NUM_OBJECTS);

        // Wait for a bit so the listener thread in the receiver PMF can get all
        // messages.
        pause(4.1);
        assertEquals(2 * NUM_OBJECTS, listenerAtReceiver.totalAdded);
        assertEquals(2 * NUM_OBJECTS, listenerAtReceiver.totalDeleted);
        assertEquals(2 * NUM_OBJECTS, listenerAtReceiver.totalUpdated);

        // shutdown
        pmSender.close();
        pmfSender.close();
        pmfReceiver.close();
    }

    protected double performAddsModifiesDeletes(EntityManager pm,
        int numObjects) {
        // Perform a series of transactions that will trigger adds,
        // deletes, and udpates
        Duration timeToAMD = new Duration(
            "Adds, removes, and dletes for " + numObjects + " objects.");
        timeToAMD.start();

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
        startTx(pm);
        for (int i = 0; i < persistables.length; i++) {
            persistables[i].setStringField("bazzed" + i);
        }
        endTx(pm);

        // delete them
        startTx(pm);
        for (int i = 0; i < persistables.length; i++) {
            pm.remove(persistables[i]);
        }
        endTx(pm);

        timeToAMD.stop();
        return timeToAMD.getDurationAsSeconds();
    }

    static int _fetchGroupSerial = 0;

    protected OpenJPAEntityManagerFactory createDistinctFactory(
        Class providerClass, String classProps1) {
        String transmit = "TransmitPersistedObjectIds=true";
        if (classProps1 == null || classProps1.length() == 0)
            classProps1 = transmit;
        else
            classProps1 += "," + transmit;

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
            propsMap.put("openjpa.RemoteCommitProvider",
                "sjvm(TransmitPersistedObjectIds=true)");
            propsMap.put("openjpa.FetchGroups", "differentiatingFetchGroup" +
                _fetchGroupSerial);
        }
        _fetchGroupSerial += 1;

        return getEmf(propsMap);
    }

    protected static class RemoteCommitListenerTestImpl
        implements RemoteCommitListener {

        Collection added;
        Collection updated;
        Collection deleted;
        int totalAdded;
        int totalUpdated;
        int totalDeleted;

        public synchronized void afterCommit(RemoteCommitEvent event) {

            this.added = event.getPersistedObjectIds();
            this.updated = event.getUpdatedObjectIds();
            this.deleted = event.getDeletedObjectIds();

            totalAdded += added.size();
            totalUpdated += updated.size();
            totalDeleted += deleted.size();
        }

        public void close() {
        }

        public String toString() {
            String returnString = "Adds " + totalAdded + " Dels " +
                totalDeleted + " Ups " + totalUpdated;
            return returnString;
        }
    }
}

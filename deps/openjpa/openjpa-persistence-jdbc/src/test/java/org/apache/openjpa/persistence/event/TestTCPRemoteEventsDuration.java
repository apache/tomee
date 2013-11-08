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
public class TestTCPRemoteEventsDuration
    extends AbstractTestCase {

    public TestTCPRemoteEventsDuration(String s) {
        super(s, "eventcactusapp");
    }

    public void setUp() {
        deleteAll(RuntimeTest1.class);
    }

    public void tearDownTestClass()
        throws Exception {
        //super.tearDownTestClass();
    }

    // FYI For 1000 objects with MySQL -- 1005 transactions,
    // doTransactions () takes 45 seconds
    private static final int NUM_OBJECTS = 200;
    private static final int NUM_TO_AVERAGE = 3;
    private static final int NUM_CONCURRENT = 3;

    public void testDurationDifference() {
        // create a non-cluserted, single pmf
        OpenJPAEntityManagerFactory factorySingle = createDistinctFactory(
            null, "");
        EntityManager pmSingle = factorySingle.createEntityManager();

        // Create 2 pmfs that are using RCPTCP. run same benachmark of
        // transactions
        OpenJPAEntityManagerFactory senderFactory1TCP =
            createDistinctFactory(TCPRemoteCommitProvider.class,
                "MaxActive=4, RecoveryTimeMillis=1000, Port=5636, " +
                    "Addresses=127.0.0.1:5636;127.0.0.1:6636");
        OpenJPAEntityManagerFactory factory2TCP = createDistinctFactory(
            TCPRemoteCommitProvider.class,
            "Port=6636, Addresses=127.0.0.1:6636;127.0.0.1:5636");

        // Register a RCEListener with the RCEM. The RCEListener
        // will record the number of
        // a,d, and u recevied when the sender PM performs commits.
        RemoteCommitListenerTestImpl listenerAtReceiver
            = new RemoteCommitListenerTestImpl();
        ((OpenJPAEntityManagerFactorySPI) factory2TCP).getConfiguration()
            .getRemoteCommitEventManager().
            addListener(listenerAtReceiver);

        System.out.println("-------------------");
        System.out.println("3 PMFs created, 1 as standalone, and 2 acting " +
            "as a cluster using ports 5636 and 6636");
        // This call is a "throw away" run to seed caches, etc.
        doTransactions(pmSingle, NUM_OBJECTS);

        double benchmarkSingle = 0.0;
        for (int i = 0; i < NUM_TO_AVERAGE; i++) {
            benchmarkSingle += doTransactions(pmSingle, NUM_OBJECTS);
        }
        benchmarkSingle /= NUM_TO_AVERAGE;

        EntityManager pmOneThread =
            senderFactory1TCP.createEntityManager();
        double benchmarkClusterOneThread = doTransactions(pmOneThread,
            NUM_OBJECTS);
        pmOneThread.close();

        Duration timeThreaded = new Duration("For " + NUM_CONCURRENT +
            " concurrent threads performing Adds, removes, and dletes for " +
            NUM_OBJECTS + " objects.");

        Thread[] concurrentThreads = new Thread[NUM_CONCURRENT];
        timeThreaded.start();
        for (int i = 0; i < NUM_CONCURRENT; i++) {
            Worker worker = new Worker
                (senderFactory1TCP.createEntityManager());
            concurrentThreads[i] = new Thread(worker);
            concurrentThreads[i].start();
        }
        // Wait for all threads to finish
        for (int i = 0; i < NUM_CONCURRENT; i++) {
            try {
                concurrentThreads[i].join();
            } catch (Exception e) {
                assertTrue(false);
                break;
            }
        }
        timeThreaded.stop();
        double benchmarkCluster = timeThreaded.getDurationAsSeconds();

        System.out.println("For " + NUM_OBJECTS + " objects, and " +
            NUM_CONCURRENT + " concurrent threads, the receiving pmf of the " +
            "cluster received :" +
            listenerAtReceiver.totalAddedClasses + " claases adds, " +
            listenerAtReceiver.totalDeleted + " deletes, " +
            listenerAtReceiver.totalUpdated + " updates");
        System.out.println(
            "\nSingle pmf - " + benchmarkSingle +
                "(s).\n Clustered pmfs (one worker thread) -"
                + benchmarkClusterOneThread +
                "(s).\n Clustered pmfs (" + NUM_CONCURRENT
                + " threads - " + benchmarkCluster + "(s).\n");

        Thread.currentThread().yield();
        try {
            Thread.currentThread().sleep((int) 500);
        } catch (InterruptedException e) {
            fail("unexecpted exception during pause");
        }
        assertEquals((NUM_CONCURRENT + 1) * NUM_OBJECTS,
            listenerAtReceiver.totalAddedClasses);
        assertEquals((NUM_CONCURRENT + 1) * NUM_OBJECTS,
            listenerAtReceiver.totalDeleted);
        assertEquals((NUM_CONCURRENT + 1) * NUM_OBJECTS,
            listenerAtReceiver.totalUpdated);

        // shutdown
        pmSingle.close();
        factorySingle.close();

        factory2TCP.close();
        senderFactory1TCP.close();
    }

    /*
      *	Worker thread that takes ownership of a PM to perform
      *	transactions. Once transactions are complete the tread
      *	will close the PM and end.
      */
    private class Worker
        implements Runnable {

        private EntityManager _pm;

        public Worker(EntityManager pm) {
            _pm = pm;
        }

        public void run() {
            doTransactions(_pm, NUM_OBJECTS);
            endEm(_pm);
        }
    }

    protected double doTransactions(EntityManager pm, int numObjects) {
        // Perform a series of transactions  that will trigger
        // adds, deletes, and udpates
        Duration timeToAMD = new Duration(
            "Adds, removes, and dletes for " + numObjects + " objects.");
        timeToAMD.start();

        // create objects
        RuntimeTest1[] persistables = new RuntimeTest1[numObjects];
        for (int i = 0; i < persistables.length; i++) {
            persistables[i] = new RuntimeTest1("foo #" + i, i);
        }

        // add them
        // This will generate a larger number of transaction in a very
        // short amount of time (old socket-per-transaction would
        // exhaust jvm socket pool)
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

        Collection addClasses;
        Collection added;
        Collection updated;
        Collection deleted;
        int totalAddedClasses;
        int totalUpdated;
        int totalDeleted;

        public synchronized void afterCommit(RemoteCommitEvent event) {
            this.addClasses = event.getPersistedTypeNames();
            this.updated = event.getUpdatedObjectIds();
            this.deleted = event.getDeletedObjectIds();

            totalAddedClasses += addClasses.size();
            totalUpdated += updated.size();
            totalDeleted += deleted.size();
        }

        public void close() {
        }
    }
}

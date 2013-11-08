/*
 * TestPMMemory.java
 *
 * Created on October 13, 2006, 3:28 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
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
package org.apache.openjpa.persistence.kernel;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;



import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest1;

import org.apache.openjpa.event.AbstractTransactionListener;
import org.apache.openjpa.event.TransactionEvent;
import org.apache.openjpa.event.TransactionListener;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactory;
import org.apache.openjpa.persistence.OpenJPAQuery;

public class TestPMMemory extends BaseKernelTest {

    /**
     * Creates a new instance of TestPMMemory
     */
    public TestPMMemory() {
    }

    public TestPMMemory(String s) {
        super(s);
    }

    public boolean skipTest() {
        return true;
    }

    private static final boolean _doPause = false;
    private static final int NUM_OBJECTS = 2000;
    private static final int NUM_FLUSHES = 5;

    // Hack to run stand alone.
    public static void main(String[] args)
        throws Exception {
        TestPMMemory testpm = new TestPMMemory("testPMMemory");
        testpm.setUp();
        testpm.testMemoryUse();
    }

    public void setUp() {
        System.out.println("About to delete all");
        deleteAllStaged(getPM(), RuntimeTest1.class);
        // deleteAll (RuntimeTest1.class);
        System.out.println("Done delete all");
    }

    public void deleteAllStaged(OpenJPAEntityManager pmArg, Class classType) {
        /*
          // create 64000 objects
          // now call this, WITH the datacache on,
          // and despite using a fetchBatch size,
          //		this _will_ run out of memory
          // props:
  kodo.DataCache: true(CacheSize=5, SoftReferenceSize=1)
  kodo.RemoteCommitProvider: sjvm
          */
        int delCount = 0;
        OpenJPAEntityManager pm;

        boolean needToDelete = true;
        while (needToDelete) {
            pm = getPM();
            startTx(pm);
            //pm.setLargeTransaction(true);
            pm.setTrackChangesByType(true);
            String cstrng = classType.getName();
            OpenJPAQuery kq = pm.createQuery("SELECT o FROM " + cstrng + " o");
            kq.getFetchPlan().setFetchBatchSize(100);
            Collection results = (Collection) kq.getResultList();
            if (results.size() == 0) {
                needToDelete = false;
                break;
            }
            System.out.println("We need to delete " + results.size());
            Iterator iter = results.iterator();
            while (iter.hasNext()) {
                pm.remove(iter.next());
                delCount += 1;
                if ((delCount % 800) == 0) {
                    pm.flush();
                    // is the trans cahce now holding
                    // all these objects?
                    break;
                }
            }
            System.out.print("deleted 200");
            endTx(pm);
            endEm(pm);
        }
        System.out.println("Done deleting");
    }

    private void reportMemory() {
        reportMemory("Memory used");
        /*
      DataCacheImpl dc;
      dc = (DataCacheImpl) kpm.getConfiguration ().getDataCacheManager ().
          getDataCache ()
      CacheMap cacheMap = dc.getCacheMap ();
      values/keySet
      */
    }

    private void reportMemory(String msg) {
        System.gc();
        long memUsed = Runtime.getRuntime().totalMemory();
        long memFree = Runtime.getRuntime().freeMemory();
        System.out.println("" + msg + " : " + memUsed + ", " +
            memFree);
    }

    private void pause(double seconds) {
        if (!_doPause)
            return;
        try {
            Thread.currentThread().yield();
            Thread.currentThread().sleep((int) seconds * 1000);
        } catch (Exception e) {
        }
    }

    public void testMemoryUse() throws Exception {

        System.out.println("Baseline, starting memory for N objects of " +
            NUM_OBJECTS);
        OpenJPAEntityManagerFactory kpmf =
            (OpenJPAEntityManagerFactory) getEmf();
        OpenJPAEntityManager kpm = (OpenJPAEntityManager)
            kpmf.createEntityManager();

        startTx(kpm);
        int runningId = performAddsModifiesDeletes(kpm, NUM_OBJECTS, 0);
        endTx(kpm);

        System.out.println("Baseline, starting memory ");
        reportMemory();

        TransactionListener l = new AbstractTransactionListener() {
            public void afterCommit(TransactionEvent ev) {
                System.out.println(
                    "My Listener in afterCommit");
                System.out.println(
                    "Num objects in transaction "
                        + ev.getTransactionalObjects().size());

                // send out an email confirming that the
                // transaction was a success
            }
        };

        // kpm.registerListener (l);

        // jprobe, jprofiler.
        // prefer treeview

        // Run a transaction for a whilw and report memory
        startTx(kpm);
        int objCount = 0;
        for (int i = 0; i < NUM_FLUSHES; i++) {
            System.out.println();
            System.out.println("Iteration #" + i + " created " +
                objCount);
            reportMemory();
            //kpm.setLargeTransaction(true);
            kpm.setTrackChangesByType(true);
            runningId = performAddsModifiesDeletes(kpm, NUM_OBJECTS, runningId);
            objCount += NUM_OBJECTS;
            kpm.flush();
            grabAllMemory();
            //    pause(30);
        }

        System.out.println("Created objects, about to commit ()");
        pause(90);
        endTx(kpm);
        pause(1);
        System.out.println("Now commit ()");
        reportMemory();
        pause(33);
    }

    protected void grabAllMemory() {
        // exhaust all memory so that GC is run.
        int size = 4096;
        boolean grab = true;
        int[] glob;
        while (grab) {
            try {
                glob = new int[size];
                size *= 2;
            } catch (OutOfMemoryError e) {
                System.out.println("Mem grabbed " + size);
                grab = false;
                glob = null;
            }
        }
        glob = null;
    }

    protected int performAddsModifiesDeletes(OpenJPAEntityManager pm,
        int numObjects, int runningId) {
        // pm should be active. Function does not perform commit.

        // Perform a series of transactions that will trigger adds,
        // deletes, and udpates

        // create objects
        RuntimeTest1[] persistables = new RuntimeTest1[numObjects];
        for (int i = 0; i < persistables.length; i++) {
            persistables[i] = new RuntimeTest1("foo #" + i, runningId + i);
        }
        runningId += persistables.length;

        // add them
        for (int i = 0; i < persistables.length; i++) {
            pm.persist(persistables[i]);
        }

        // modify them
        for (int i = 0; i < persistables.length; i++) {
            persistables[i].setIntField1(i + 1);
        }

/*
		// delete them
		for (int i = 0; i < persistables.length; i++)
		{
			pm.deletePersistent (persistables [i]);
		}
*/
        return runningId + 1;
    }

    static int _fetchGroupSerial = 0;

    protected OpenJPAEntityManagerFactory createDistinctFactory(
        Class providerClass, String classProps1) {
        Map props = null;

        //FIXME jthomas
        /*
        if (providerClass != null) {
            props = new String[]{
                "openjpa.RemoteCommitProvider", Configurations.getPlugin(
                providerClass.getNameclassProps1),
                // use this property to differentiate the factory
                "openjpa.FetchGroups", "differentiatingFetchGroup" +
                _fetchGroupSerial,
            };
        } else {
            // No RCP
            props = new String[]{
                // use this property to differentiate the factory
                "openjpa.RemoteCommitProvider", "sjvm",
                "openjpa.FetchGroups", "differentiatingFetchGroup" +
                _fetchGroupSerial,
            };
        }
        _fetchGroupSerial += 1;
         */

        return (OpenJPAEntityManagerFactory) getEmf(props);
    }
}

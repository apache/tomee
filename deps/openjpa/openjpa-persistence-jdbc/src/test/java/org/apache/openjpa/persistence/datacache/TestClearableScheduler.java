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

import java.util.Calendar;
import java.util.Date;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.datacache.ClearableScheduler;
import org.apache.openjpa.datacache.ConcurrentDataCache;
import org.apache.openjpa.persistence.OpenJPAEntityManagerSPI;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestClearableScheduler extends SingleEMFTestCase {

    private static String getMinutesString() {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < 60; i++) {
            if (i % 2 == 0)
                buf.append(i).append(',');
        }
        return buf.toString();
    }

    public void setUp() {
        setUp(
            "openjpa.DataCache", "true(EvictionSchedule=+1)"
            , "openjpa.QueryCache", "true"
            ,"openjpa.RemoteCommitProvider", "sjvm"
            ,CachedPerson.class, CLEAR_TABLES
            );
    }

    public void testBasic() throws Exception {
        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        OpenJPAConfiguration conf = ((OpenJPAEntityManagerSPI) em).getConfiguration();
        ClearableScheduler scheduler = new ClearableScheduler(conf);
        // Make the scheduler run every 1 minute
        scheduler.setInterval(1);
        DummyCache cache1 = new DummyCache();
        DummyCache cache2 = new DummyCache();

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        int currMin = cal.get(Calendar.MINUTE);
        int plusOne = currMin+1;
        int plusTwo = plusOne+1;
        if(plusOne>=60){
            plusOne-=60;
        }
        if(plusTwo>=60){
            plusTwo-=60;
        }
        // Schedule eviction to happen the next two minutes
        scheduler.scheduleEviction(cache2, plusOne+","+plusTwo+" * * * *");

        // Schedule eviction to happen every mintue on cache 1
        scheduler.scheduleEviction(cache1, ("+1"));
        
        Thread.currentThread().sleep(61000);
        assertEquals(1,cache1.getClearCount());
        assertEquals(1,cache2.getClearCount());
        
        Thread.currentThread().sleep(60000);
        assertEquals(2,cache1.getClearCount());
        assertEquals(2,cache2.getClearCount());
        
        Thread.currentThread().sleep(60000);
        assertEquals(3,cache1.getClearCount());
        assertEquals(2,cache2.getClearCount());
    }
    
// Comment this test out while investigating OPENJPA-1692
//    public void testMultithreadedInitialization() throws Exception {
//        final OpenJPAConfiguration conf =  emf.getConfiguration();
//        final List<DataCacheManager> dcms = new Vector<DataCacheManager>();
//        Runnable r = new Runnable(){
//            public void run() {
//                dcms.add(conf.getDataCacheManagerInstance());
//            }
//        };
//        List<Thread> workers = new ArrayList<Thread>();
//        for(int i = 0;i<20;i++){
//            workers.add(new Thread(r));
//        }
//        for(Thread t : workers){
//            t.start();
//        }
//        for(Thread t : workers){
//            t.join();
//        }
//        DataCacheManager prev = dcms.get(0);
//        for(DataCacheManager dcm : dcms){
//            assertTrue(prev == dcm);
//            prev = dcm;
//        }
//
//    }

    /**
     * Pass in 4 out of 5 tokens.
     */
    // private void doTest(String valid, String invalid) throws Exception {
    //
    // OpenJPAEntityManagerFactory emf = (OpenJPAEntityManagerFactory) getEmf();
    // OpenJPAConfiguration conf = ((OpenJPAEntityManagerFactorySPI)
    // OpenJPAPersistence.cast(emf)).getConfiguration();
    //
    // DataCacheScheduler scheduler = new DataCacheScheduler(conf);
    // scheduler.setInterval(1);
    //
    // Calendar cal = Calendar.getInstance();
    // cal.setTime(new Date());
    // String sched = ((cal.get(Calendar.MINUTE) + 1) % 60) + " ";
    // DummyCache validCache = new DummyCache();
    // scheduler.scheduleEviction(validCache, sched + valid);
    // DummyCache invalidCache = new DummyCache();
    // scheduler.scheduleEviction(invalidCache, sched + invalid);
    // Thread thread = new Thread(scheduler);
    // thread.setDaemon(true);
    // thread.start();
    // // test that it did not run yet...
    // Thread.currentThread().sleep(70 * 1000); // 70 seconds
    // scheduler.stop();
    // // assertEquals(2, validCache.clearCount);
    // assertTrue("Wrong invocation count: " + validCache.clearCount, validCache.clearCount == 1
    // || validCache.clearCount == 2);
    // assertEquals(0, invalidCache.clearCount);
    // }
    private class DummyCache extends ConcurrentDataCache {

        int clearCount = 0;

        public synchronized int getClearCount(){
            return clearCount;
        }
        public synchronized void clear() {
            clearCount++;
        }
    }
}

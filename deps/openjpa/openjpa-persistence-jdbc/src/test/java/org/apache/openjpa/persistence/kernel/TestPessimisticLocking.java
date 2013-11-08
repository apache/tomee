/*
 * TestPessimisticLocking.java
 *
 * Created on October 13, 2006, 3:17 PM
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

import java.util.HashMap;
import java.util.Map;



import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest1;
import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest2;
import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest3;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactory;
import org.apache.openjpa.persistence.jdbc.FetchMode;
import org.apache.openjpa.persistence.jdbc.JDBCFetchPlan;

public class TestPessimisticLocking extends BaseKernelTest {

    private Object _id = null;
    private int _bugCount = 0;
    private OpenJPAEntityManagerFactory _factory = null;

    public TestPessimisticLocking(String name) {
        super(name);
    }

    /**
     * Creates a new instance of TestPessimisticLocking
     */
    public TestPessimisticLocking() {
    }

    protected boolean skipTest() {
        // pointbase doesn't really lock
        if (getCurrentPlatform() == AbstractTestCase.Platform.POINTBASE)
            return true;

        if (getConfiguration() instanceof JDBCConfiguration) {
            JDBCConfiguration conf = (JDBCConfiguration) getConfiguration();
            return !conf.getDBDictionaryInstance().supportsSelectForUpdate;
        }
        return false;
    }

    /**
     * Use a locking persistence manager with subclass fetch mode to set to
     * "none" to avoid subclass joins.
     */
    protected OpenJPAEntityManager getLockingPM() {
        OpenJPAEntityManager pm = _factory.createEntityManager();
        ((JDBCFetchPlan) pm.getFetchPlan())
            .setSubclassFetchMode(FetchMode.NONE);
        return pm;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp(RuntimeTest1.class, RuntimeTest2.class, RuntimeTest3.class);

        Map propsMap = new HashMap();
        propsMap.put("openjpa.LockManager", "pessimistic");
        _factory = getEmf(propsMap);

        OpenJPAEntityManager pm = getLockingPM();
        startTx(pm);

        RuntimeTest1 a = new RuntimeTest1("name", 0);
        pm.persist(a);
        _id = pm.getObjectId(a);

        endTx(pm);
        endEm(pm);
    }

    @Override
    public void tearDown() throws Exception {
        try {
            if (_factory != null) {
                _factory.close();
                _factory = null;
            }
        } catch (Exception e) {
        }
        super.tearDown();
    }

    /**
     * Test that pessimistic locking is working in the data store.
     */
    public void testPessimisticLocking() throws Throwable {
        pessimisticLockingTest(false);
    }

    /**
     * Test that the test case itself is working be using a ReentrantLock. This
     * test will validate that the test case itself is working correctly, not
     * that the datastore's pessimistic locking is working.
     */
    public void testPessimisticLockingInternal() throws Throwable {
        pessimisticLockingTest(true);
    }

    /**
     * Test that pessimistic locking is working by attempting to update the same
     * object in the data store.
     *
     * @param useReentrantLock true if we want to synchronize on a lock instead
     * of relying on the data store (used for validating the test case).
     */
    public void pessimisticLockingTest(boolean useReentrantLock)
        throws Throwable {
        long timeout = System.currentTimeMillis() + (60 * 5 * 1000);

        ReentrantLock lock = null;
        if (useReentrantLock)
            lock = new ReentrantLock();

        TestThread t1 = new TestThread(lock);
        TestThread t2 = new TestThread(lock);
        t1.start();
        t2.start();

        getLog().trace("started thread");

        // wait for threads to die or timeout
        while ((t1.isAlive() || t2.isAlive())
            && System.currentTimeMillis() < timeout) {
            Thread.sleep(1000);
            getLog().trace(
                "thread waiting for completion ("
                    + (timeout - System.currentTimeMillis())
                    + " ms left)");
        }

        getLog().trace("checking if thread is alive");
        System.out.flush();

        if (t1.isAlive() || t2.isAlive()) {
            getLog().trace("thread is still alive");
            System.out.flush();

            // do out best to clean them up
            try {
                t1.interrupt();
            }
            catch (Exception e) {
            }
            try {
                t2.interrupt();
            }
            catch (Exception e) {
            }

            throw new Exception("Thread did not complete after timeout ("
                + timeout + "): possible deadlock");
        }

        getLog().trace("checking exception for t1");
        if (t1.exception != null)
            throw t1.exception;

        getLog().trace("checking exception for t2");
        if (t2.exception != null)
            throw t2.exception;

        getLog().trace("verifying pessimistic locking worked...");
        OpenJPAEntityManager pm = getLockingPM();
        RuntimeTest1 a = pm.find(RuntimeTest1.class, _id);
        assertEquals(20 - _bugCount, a.getIntField1());
        getLog().trace("closing pm");
        endEm(pm);
        getLog().trace("done");
    }

    /**
     * Update thread that tries to increment an int field.
     *
     * @author <a href="mailto:marc@solarmetric.com">Marc Prud'hommeaux</a>
     */
    private class TestThread extends Thread {

        private OpenJPAEntityManager _pm = getLockingPM();

        public Exception exception = null;

        private final ReentrantLock _lock;

        /**
         * Constructor
         *
         * @param lock the ReentrantLock we should use, or null to rely on
         * pessimistic locking in the data store.
         */
        public TestThread(ReentrantLock lock) {
            this._lock = lock;
        }

        public synchronized void run() {
            getLog().trace(
                Thread.currentThread().getName()
                    + ": starting update thread");
            try {
                for (int i = 0; i < 10; i++) {
                    if (_lock != null)
                        _lock.lock();

                    try {
                        _pm.setOptimistic(false);
                        startTx(_pm);
                        RuntimeTest1 a = (RuntimeTest1) _pm.find(
                            RuntimeTest1.class, _id);
                        getLog().trace(
                            Thread.currentThread().getName()
                                + ": obtained and locked: " + a);
                        yield();
                        super.wait(50);
                        getLog().trace(
                            Thread.currentThread().getName()
                                + ": updating age from "
                                + a.getIntField1());
                        a.setIntField1(a.getIntField1() + 1);
                        getLog().trace(
                            Thread.currentThread().getName()
                                + ": committed update");
                        try {
                            _pm.flush();
                            endTx(_pm);
                        }
                        catch (Exception ex) {
                            throw new org.apache.openjpa.util.UserException(
                                "Optimistic lock probably failed after "
                                    + i + " iterations ("
                                    + Thread.currentThread().getName()
                                    + ")", ex);
                        }
                        yield();
                    }
                    finally {
                        if (_lock != null)
                            _lock.unlock();
                    }
                }
            }
			catch (Exception e)
			{
				exception = e;
			}
		}
	}

}

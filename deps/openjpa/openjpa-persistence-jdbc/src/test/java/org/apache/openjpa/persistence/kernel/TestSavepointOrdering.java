/*
 * TestSavepointOrdering.java
 *
 * Created on October 16, 2006, 10:45 AM
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
import java.util.HashMap;
import java.util.Map;



import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest1;
import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest2;
import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest3;

import org.apache.openjpa.kernel.Broker;
import org.apache.openjpa.kernel.OpenJPASavepoint;
import org.apache.openjpa.kernel.SavepointManager;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactory;

public class TestSavepointOrdering extends BaseKernelTest {

    private static final int USER = 1;
    private static final int RELEASED = 2;
    private static final int ROLLBACK = 4;

    static Map<String, TrackingSavepoint> _assigned = new HashMap<String, TrackingSavepoint>();

    /**
     * Creates a new instance of TestSavepointOrdering
     */
    public TestSavepointOrdering(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp(RuntimeTest1.class, RuntimeTest2.class, RuntimeTest3.class);
        _assigned.clear();
    }

    public void testCleanUpCommit() {
        doCleanUpTest(true);
    }

    public void testCleanUpRollback() {
        doCleanUpTest(false);
    }

    public void doCleanUpTest(boolean commit) {
        Map<String, String> props = new HashMap<String, String>();
        props.put("openjpa.SavepointManager",
            TrackingSavepointManager.class.getName());
        OpenJPAEntityManagerFactory pmf = getEmf(props);
        OpenJPAEntityManager pm = pmf.createEntityManager();
        startTx(pm);
        pm.setSavepoint("test");
        pm.setSavepoint("test2");
        if (commit)
            endTx(pm);
        else
            rollbackTx(pm);
        assertFlags("test", RELEASED, USER | ROLLBACK);
        assertFlags("test2", RELEASED, USER | ROLLBACK);
        endEm(pm);
    }

    public void testOrderingWithRollback() {
        doOrderingTest(true);
    }

    public void testOrderingWithRelease() {
        doOrderingTest(false);
    }

    private void doOrderingTest(boolean rollback) {
        Map<String, String> props = new HashMap<String, String>();
        props.put("openjpa.SavepointManager",
            TrackingSavepointManager.class.getName());
        OpenJPAEntityManagerFactory pmf = getEmf(props);
        OpenJPAEntityManager pm = pmf.createEntityManager();
        startTx(pm);
        pm.setSavepoint("before");
        pm.setSavepoint("before2");
        pm.setSavepoint("test");
        pm.setSavepoint("test2");
        pm.setSavepoint("after");
        pm.setSavepoint("after2");
        if (rollback)
            pm.rollbackToSavepoint("test2");
        else
            pm.releaseSavepoint("test2");

        assertFlags("before", 0, RELEASED | ROLLBACK);
        assertFlags("before2", 0, RELEASED | ROLLBACK);
        assertFlags("test", 0, RELEASED | ROLLBACK);
        assertFlags("after", RELEASED, USER);
        assertFlags("after2", RELEASED, USER);

        if (rollback)
            assertFlags("test2", ROLLBACK, 0);
        else
            assertFlags("test2", RELEASED | USER, 0);

        pm.setSavepoint("after3");
        if (rollback)
            pm.rollbackToSavepoint("test");
        else
            pm.releaseSavepoint("test");

        assertFlags("before", 0, RELEASED | ROLLBACK);
        assertFlags("before2", 0, RELEASED | ROLLBACK);
        if (rollback)
            assertFlags("test", ROLLBACK, 0);
        else
            assertFlags("test", RELEASED | USER, 0);
        assertFlags("after3", RELEASED, USER);
        rollbackTx(pm);
        endEm(pm);
    }

    public void testDisallowFlush() {
        Map<String, String> props = new HashMap<String, String>();
        props.put("openjpa.SavepointManager",
            TrackingSavepointManager.class.getName() + "(AllowFlush=false)");
        OpenJPAEntityManagerFactory pmf = getEmf(props);
        OpenJPAEntityManager pm = pmf.createEntityManager();
        startTx(pm);
        pm.persist(new RuntimeTest1());
        pm.setSavepoint("a");
        try {
            pm.flush();
            fail("should have failed.");
        } catch (Exception e) {
        }
        rollbackTx(pm);
        endEm(pm);
    }

    public void testDisallowFlush2() {
        Map<String, String> props = new HashMap<String, String>();
        props.put("openjpa.SavepointManager",
            TrackingSavepointManager.class.getName() + "(AllowFlush=false)");
        OpenJPAEntityManagerFactory pmf = getEmf(props);
        OpenJPAEntityManager pm = pmf.createEntityManager();

        startTx(pm);
        pm.persist(new RuntimeTest1());
        pm.flush();
        try {
            pm.setSavepoint("a");
            fail("should have failed.");
        } catch (Exception e) {
        }
        rollbackTx(pm);
        endEm(pm);
    }

    public void testAllowFlush() {
        Map<String, String> props = new HashMap<String, String>();
        props.put("openjpa.SavepointManager",
            TrackingSavepointManager.class.getName() + "(AllowFlush=true)");
        OpenJPAEntityManagerFactory pmf = getEmf(props);
        OpenJPAEntityManager pm = pmf.createEntityManager();

        startTx(pm);
        pm.persist(new RuntimeTest1());
        pm.setSavepoint("a");
        try {
            pm.flush();
        } catch (Exception e) {
            fail("allows flush.");
        }
        rollbackTx(pm);
        endEm(pm);
    }

    public void testAllowFlush2() {
        Map<String, String> props = new HashMap<String, String>();
        props.put("openjpa.SavepointManager",
            TrackingSavepointManager.class.getName() + "(AllowFlush=true)");
        OpenJPAEntityManagerFactory pmf = getEmf(props);
        OpenJPAEntityManager pm = pmf.createEntityManager();

        startTx(pm);
        pm.persist(new RuntimeTest1());
        pm.flush();
        try {
            pm.setSavepoint("a");
        } catch (Exception e) {
            fail("allows flush.");
        }
        rollbackTx(pm);
        endEm(pm);
    }

    private void assertFlags(String name, int flag, int noflag) {
        TrackingSavepoint sp = _assigned.get(name);
        assertNotNull(sp);
        assertEquals(sp.flags & flag, flag);
        assertTrue((sp.flags & noflag) == 0);
    }

    public static class TrackingSavepointManager implements SavepointManager {

        public boolean allowFlush = false;

        public boolean supportsIncrementalFlush() {
            return allowFlush;
        }

        public OpenJPASavepoint newSavepoint(String name, Broker broker) {
            TrackingSavepoint sp = new TrackingSavepoint(broker, name);
            _assigned.put(sp.getName(), sp);
            return sp;
        }
    }

    private static class TrackingSavepoint extends OpenJPASavepoint {

        int flags = 0;

        public TrackingSavepoint(Broker broker, String name) {
            super(broker, name, false);
        }

        public Collection rollback(Collection previous) {
            if ((flags & (ROLLBACK | RELEASED)) != 0)
                fail("already used");
            flags |= ROLLBACK;
            return super.rollback(previous);
        }

        public void release(boolean user) {
            if ((flags & (ROLLBACK | RELEASED)) != 0)
                fail("already used");
            flags |= RELEASED;
            if (user) {
                if ((flags & USER) != 0)
                    fail("already released");
                flags |= USER;
            }

            super.release(user);
        }
    }
}

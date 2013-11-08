/*
 * TestIncrementalFlushes.java
 *
 * Created on October 12, 2006, 11:24 AM
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



import org.apache.openjpa.persistence.kernel.common.apps.
        ModInstanceCallbackTests;
import org.apache.openjpa.persistence.kernel.common.apps.ModRuntimeTest1;

import org.apache.openjpa.event.AbstractTransactionListener;
import org.apache.openjpa.event.TransactionEvent;
import org.apache.openjpa.kernel.PCState;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerSPI;

public class TestIncrementalFlushes extends BaseKernelTest {

    public TestIncrementalFlushes(String str) {
        super(str);
    }

    /**
     * Creates a new instance of TestIncrementalFlushes
     */
    public TestIncrementalFlushes() {
    }

    public void setUp() {
        deleteAll(ModRuntimeTest1.class);
//        deleteAll(ModInstanceCallbackTests.class);
    }

    public void testBasicJdoPreStore() {
        OpenJPAEntityManager pm = getPM(true, false);
        startTx(pm);
        ModInstanceCallbackTests a = new ModInstanceCallbackTests("foo", 10);
        pm.persist(a);
        pm.flush();
        assertTrue(a.preStoreCalled);
        endTx(pm);
    }

    public void testNoFlush() {
        OpenJPAEntityManager pm = getPM(true, false);
        startTx(pm);
        ModInstanceCallbackTests a = new ModInstanceCallbackTests("foo", 10);
        pm.persist(a);
        endTx(pm);
        assertTrue(a.preStoreCalled);
    }

    public void testFlushNoChange() {
        OpenJPAEntityManager pm = getPM(true, false);
        startTx(pm);
        ModInstanceCallbackTests a = new ModInstanceCallbackTests("foo", 10);
        pm.persist(a);
        pm.flush();
        endTx(pm);
        assertTrue(a.preStoreCalled);
        assertEquals(10, a.getIntField());
    }

    /**
     * Helper method for some common test cases. See utilizations of
     * this below.
     */
    private void basicHelper(boolean update, boolean multi, boolean dfg,
        boolean nonDFG) {
        OpenJPAEntityManager pm = getPM(true, false);
        startTx(pm);

        ModInstanceCallbackTests a = new ModInstanceCallbackTests("foo", 10);
        pm.persist(a);
        if (update) {
            endTx(pm);
            Object oid = pm.getObjectId(a);
            endEm(pm);
            pm = getPM(true, false);
            startTx(pm);
            a = (ModInstanceCallbackTests) pm
                .find(ModInstanceCallbackTests.class, oid);
        } else {
            pm.flush();
        }

        if (dfg)
            a.setIntField(11);
        if (nonDFG)
            a.setNonDFGField(11);

        if (multi) {
            pm.flush();

            if (dfg)
                a.setIntField(12);
            if (nonDFG)
                a.setNonDFGField(12);
        }

        endTx(pm);

        // if no changes were made and we're in update mode, then this
        // object won't have had jdoPreStore() called.
//        if (!(update && (!dfg && !nonDFG)))
//            assertTrue("a.prestoreCalled is false", a.preStoreCalled);

        if (multi) {
            if (dfg)
                assertEquals("a.getIntField is not 12", 12, a.getIntField());
            if (nonDFG)
                assertEquals("a.getNonDFGField is not 12", 12,
                    a.getNonDFGField());
        } else {
            if (dfg)
                assertEquals("a.getIntField is not 12", 11, a.getIntField());
            if (nonDFG)
                assertEquals("a.getNonDFGField is not 12", 11,
                    a.getNonDFGField());
        }
    }

    public void testFlushStorePrimaryDFGChange() {
        basicHelper(false, false, true, false);
        basicHelper(false, true, true, false);
        basicHelper(true, false, true, false);
        basicHelper(true, true, true, false);
    }

    public void testFlushStorePrimaryNonDFGChange() {
        basicHelper(false, false, false, true);
        basicHelper(false, true, false, true);
        basicHelper(true, false, false, true);
        basicHelper(true, true, false, true);
    }

    public void testFlushStorePrimaryNonDFGAndDFGChange() {
        basicHelper(false, false, true, true);
        basicHelper(false, true, true, true);
        basicHelper(true, false, true, true);
        basicHelper(true, true, true, true);
    }

    public void testFlushStorePrimaryNoChanges() {
        basicHelper(false, false, false, false);
        basicHelper(false, true, false, false);
        basicHelper(true, false, false, false);
        basicHelper(true, true, false, false);
    }

    public void testJdoPreStoreWithModificationBeforeFlush() {
        tjpswmHelper(true);
    }

    public void testJdoPreStoreWithModificationAfterFlush() {
        tjpswmHelper(false);
    }

    private void tjpswmHelper(boolean before) {
        // set retainvalues to false so that we can ensure that the
        // data in the database is correct, and that we're not just
        // testing that the JVM data is correct.
        OpenJPAEntityManager pm = getPM(true, false);
        startTx(pm);
        ModInstanceCallbackTests a = new ModInstanceCallbackTests("foo", 10);
        pm.persist(a);

        // by setting the name to 'bar', the jdoPreStore() invocation
        // will set the parent to a new object. This ensures that new
        // objects created in jdoPreStore() make their way into the DB
        // during commit.
        if (before) {
            a.setStringField("bar");
            pm.flush();
        } else {
            pm.flush();
            a.setStringField("bar");
        }
        endTx(pm);
        assertTrue("a.preStoreCalled is false", a.preStoreCalled);
        assertNotNull("a.getOneOne is null", a.getOneOne());
        assertTrue("getOneOne().getstrngfld.equals(jdoPrestore) is false",
            a.getOneOne().getStringField().equals("jdoPreStore"));
    }

    public void testOneToOneBefore() {
        totoHelper(true, true, false);
        totoHelper(true, false, false);
        totoHelper(true, true, true);
        totoHelper(true, false, true);
    }

    public void testOneToOneAfter() {
        totoHelper(false, true, false);
        totoHelper(false, false, false);
        totoHelper(false, true, true);
        totoHelper(false, false, true);
    }

    private void totoHelper(boolean before, boolean persist,
        boolean multi) {
        // set retainvalues to false so that we can ensure that the
        // data in the database is correct, and that we're not just
        // testing that the JVM data is correct.
        OpenJPAEntityManager pm = getPM(true, false);
        startTx(pm);
        ModInstanceCallbackTests a = new ModInstanceCallbackTests("foo", 10);
        pm.persist(a);

        ModRuntimeTest1 parent = new ModRuntimeTest1("baz", 11);
        if (!before)
            pm.flush();

        if (persist)
            pm.persist(parent);

        a.setOneOne(parent);

        if (before)
            pm.flush();

        ModRuntimeTest1 oldParent = null;
        if (multi) {
            oldParent = parent;
            parent = new ModRuntimeTest1("newParent", 12);

            if (!before)
                pm.flush();

            if (persist)
                pm.persist(parent);

            a.setOneOne(parent);

            if (before)
                pm.flush();
        }

        endTx(pm);
        assertTrue("a.preStoreCalled is false", a.preStoreCalled);
        assertNotNull("a.getOneOne is null", a.getOneOne());
        if (!multi)
            assertTrue("a.getOneOne().getStringField().equals(baz) is false",
                a.getOneOne().getStringField().equals("baz"));
        else {
            assertTrue(
                "a.getOneOne().getStringField().equals(newParent) is false",
                a.getOneOne().getStringField().equals("newParent"));

            // if multi, then we really should delete the baz
            // parent. This isn't happening right now.
            // ### should be a bug
            //assertTrue (JDOHelper.isDeleted (oldParent));
        }
    }

    private void assertState(Object o, PCState state, OpenJPAEntityManager pm) {
        assertEquals(state, getStateManager(o, pm).getPCState());
    }

    private void commitAndTestDelete(OpenJPAEntityManager pm, Object o) {
        Object oid = pm.getObjectId(o);
        endTx(pm);

        pm = getPM();
        try {
            pm.find(Object.class, oid);
            fail("should not be able to load deleted object");
        } catch (Exception e) {
            // expected case
        }
    }

    public void testDeleteNew() {
        OpenJPAEntityManager pm = getPM(true, false);
        startTx(pm);
        ModRuntimeTest1 a = new ModRuntimeTest1("foo", 10);
        pm.persist(a);
        pm.remove(a);
        assertState(a, PCState.PNEWDELETED, pm);
    }

    public void testOptimisticLockGivesCorrectError() {
        OpenJPAEntityManager pm1 = getPM(true, false);
        OpenJPAEntityManager pm2 = getPM(true, false);

        ModRuntimeTest1 a1 = new ModRuntimeTest1("foo", 10);
        startTx(pm1);
        pm1.persist(a1);
        endTx(pm1);

        ModRuntimeTest1 a2 = (ModRuntimeTest1)
            pm2.find(ModRuntimeTest1.class, pm2.getObjectId(a1));
        startTx(pm2);
        a2.setStringField("foobar");
        endTx(pm2);

        startTx(pm1);
        a1.setStringField("foobarbaz");
        try {
            endTx(pm1);
        } catch (Exception ole) {
            // expected case
        } finally {
            rollbackTx(pm1);

            pm1.close();
            pm2.close();
        }
    }

    /**
     * Verify that flushes to the datastore are isolated from other
     * PersistenceManagers. This is mostly a test of the underlying
     * datastore's transactional isolation capabilities.
     * <p/>
     * Disabled: this hangs on Sybase.
     */
    public void XXXtestFlushesAreIsolated() {
        final String name = "testFlushesAreIsolated";

        deleteAll(ModRuntimeTest1.class);

        OpenJPAEntityManager flushPM = getPM(true, false);
        startTx(flushPM);

        OpenJPAEntityManager readPM = getPM(true, false);
        startTx(readPM);

        assertSize(0, flushPM.createNativeQuery("stringField == '" + name + "'",
            ModRuntimeTest1.class));
        assertSize(0, readPM.createNativeQuery("stringField == '" + name + "'",
            ModRuntimeTest1.class));

        ModRuntimeTest1 a = new ModRuntimeTest1(name, randomInt().intValue());

        flushPM.persist(a);

        assertSize(0, readPM.createNativeQuery("name == '" + name + "'",
            ModRuntimeTest1.class));

        flushPM.flush();

        // make sure the other pm doesn't see the flushed object
        assertSize(0, readPM.createNativeQuery("name == '" + name + "'",
            ModRuntimeTest1.class));

        flushPM.remove(a);

        assertSize(0, flushPM.createNativeQuery("name == '" + name + "'",
            ModRuntimeTest1.class));
        assertSize(0, readPM.createNativeQuery("name == '" + name + "'",
            ModRuntimeTest1.class));

        endTx(flushPM);
        endEm(flushPM);

        endTx(readPM);
        endEm(readPM);
    }

    public void testEmptyFlush() {
        OpenJPAEntityManager pm = getPM();
        TListener listener = new TListener();
        ((OpenJPAEntityManagerSPI) pm).addTransactionListener(listener);
        startTx(pm);
        ModRuntimeTest1 pc = new ModRuntimeTest1();
        pm.persist(pc);
        pm.flush();
        assertEquals(1, listener.flushes);
        assertEquals(0, listener.commits);

        pm.flush();
        assertEquals(1, listener.flushes);
        assertEquals(0, listener.commits);

        pc.setIntField(3);
        pm.flush();
        assertEquals(2, listener.flushes);
        assertEquals(0, listener.commits);

        endTx(pm);
        assertEquals(2, listener.flushes);
        assertEquals(1, listener.commits);

        endEm(pm);
    }

    public void testEmptyRollback() {
        OpenJPAEntityManager pm = getPM();
        TListener listener = new TListener();
        ((OpenJPAEntityManagerSPI) pm).addTransactionListener(listener);
        startTx(pm);
        pm.flush();
        rollbackTx(pm);
        assertEquals(0, listener.flushes);
        assertEquals(0, listener.commits);
        endEm(pm);
    }

    public void testEmptyCommit() {
        OpenJPAEntityManager pm = getPM();
        TListener listener = new TListener();
        ((OpenJPAEntityManagerSPI) pm).addTransactionListener(listener);
        startTx(pm);
        endTx(pm);
        assertEquals(0, listener.flushes);
        assertEquals(1, listener.commits);
        endEm(pm);
    }

    private static class TListener
        extends AbstractTransactionListener {

        public int flushes = 0;
        public int commits = 0;

        protected void eventOccurred(TransactionEvent event) {
            if (event.getType() == event.BEFORE_FLUSH)
                flushes++;
            else if (event.getType() == event.BEFORE_COMMIT)
                commits++;
        }
    }
}

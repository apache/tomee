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



import org.apache.openjpa.persistence.kernel.common.apps.ModRuntimeTest1;
import org.apache.openjpa.persistence.kernel.common.apps.ModRuntimeTest2;
import org.apache.openjpa.persistence.kernel.common.apps.PersistenceAware;

import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.RestoreStateType;

public class TestTransition2 extends BaseKernelTest {

    private boolean supportsPessimistic = true;

    private int _id = 0;

    /**
     * Creates a new instance of TestTransitions
     */
    public TestTransition2() {
    }

    public TestTransition2(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp(ModRuntimeTest1.class, ModRuntimeTest2.class, PersistenceAware.class);
        try {
            OpenJPAEntityManager pm = getPM(false, false);
            supportsPessimistic = !pm.getOptimistic();
            pm.close();
        }
        catch (Exception e) {
            supportsPessimistic = false;
        }

        OpenJPAEntityManager pm = getPM();
        startTx(pm);

        ModRuntimeTest2 b = createTest2();
        pm.persist(b);
        _id = b.getId();

        endTx(pm);
        endEm(pm);
    }

    /**
     * Tests basic jdo flag transitions from transient to
     * persistent-transactional and back to transient after rollback.
     */
    public void testFlagTransitions1()
        throws Exception {
        if (!supportsPessimistic)
            return;

        ModRuntimeTest2 b = createTest2();
        assertTransient(b);
        assertTransient(b.getSelfOneOne());

        OpenJPAEntityManager pm = getPM(false, false);
        startTx(pm);
        pm.persist(b);

        assertPersistent(b, true, true, false, true);

        rollbackTx(pm);

        assertTransient(b);
        assertTransient(b.getSelfOneOne());
        endEm(pm);
    }

    /**
     * Tests basic jdo flag transitions from transient to
     * persistent-transactional to persistent-nontransactional after commit.
     */
    public void testFlagTransitions2()
        throws Exception {
        if (!supportsPessimistic)
            return;

        ModRuntimeTest2 b = createTest2();
        assertTransient(b);
        assertTransient(b.getSelfOneOne());

        OpenJPAEntityManager pm = getPM(false, false);
        startTx(pm);
        pm.persist(b);

        assertPersistent(b, true, true, false, true);

        endTx(pm);

        assertPersistent(b, false, false, false, false);
        assertPersistent(b.getSelfOneOne(), false, false, false, false);
        endEm(pm);
    }

    /**
     * Tests basic jdo flag transitions when finding a transactional instance by
     * id, then committing.
     */
    public void testFlagTransitions3() throws Exception {
        if (!supportsPessimistic)
            return;

        OpenJPAEntityManager pm = getPM(false, false);
        startTx(pm);

        ModRuntimeTest2 b = pm.find(ModRuntimeTest2.class, _id);
        assertPersistent(b, true, false, false, false);
        assertPersistent(b.getSelfOneOne(), true, false, false, false);

        endTx(pm);

        assertPersistent(b, false, false, false, false);
        assertPersistent(b.getSelfOneOne(), false, false, false, false);
        endEm(pm);
    }

    /**
     * Tests basic jdo flag transitions when finding a transactional
     * instance by id, then rolling back.
     */
    public void testFlagTransitions4()
        throws Exception {
        if (!supportsPessimistic)
            return;

        OpenJPAEntityManager pm = getPM(false, false);
        startTx(pm);

        ModRuntimeTest2 b = pm.find(ModRuntimeTest2.class, _id);
        assertPersistent(b, true, false, false, false);
        assertPersistent(b.getSelfOneOne(), true, false, false, false);

        rollbackTx(pm);
        //pm.getTransaction().rollback();

        assertPersistent(b, false, false, false, false);
        assertPersistent(b.getSelfOneOne(), false, false, false, false);
        endEm(pm);
    }

    /**
     * Tests basic jdo flag transitions when finding a non-transactional
     * instance by id.
     */
    public void testFlagTransitions5()
        throws Exception {
        if (!supportsPessimistic)
            return;

        OpenJPAEntityManager pm = getPM(false, false);
        ModRuntimeTest2 b = pm.find(ModRuntimeTest2.class, _id);

        assertPersistent(b, false, false, false, false);
        assertPersistent(b.getSelfOneOne(), false, false, false, false);

        endEm(pm);
    }

    /**
     * Tests basic jdo flag transitions from persistent-transactional
     * to transient after delete and commit.
     */
    public void testFlagTransitions6()
        throws Exception {
        if (!supportsPessimistic)
            return;

        OpenJPAEntityManager pm = getPM(false, false);
        startTx(pm);

        ModRuntimeTest2 b = pm.find(ModRuntimeTest2.class, _id);
        ModRuntimeTest1 parent = b.getSelfOneOne();
        pm.remove(b);
        assertPersistent(b, true, false, true, true);
        assertPersistent(parent, true, false, false, false);

        endTx(pm);

        assertTransient(b);
        assertPersistent(parent, false, false, false, false);
        endEm(pm);
    }

    /**
     * Tests basic jdo flag transitions from persistent-transactional
     * to persistent-nontransactional after delete and rollback.
     */
    public void testFlagTransitions7()
        throws Exception {
        if (!supportsPessimistic)
            return;

        OpenJPAEntityManager pm = getPM(false, false);
        startTx(pm);

        ModRuntimeTest2 b = pm.find(ModRuntimeTest2.class, _id);
        ModRuntimeTest1 parent = b.getSelfOneOne();
        pm.remove(b);
        assertPersistent(b, true, false, true, true);
        assertPersistent(parent, true, false, false, false);

        rollbackTx(pm);

        assertPersistent(b, false, false, false, false);
        assertPersistent(parent, false, false, false, false);
        endEm(pm);
    }

    /**
     * Tests basic state transitions from transient to
     * persistent-transactional and back to transient after rollback.
     */
    public void testStateTransitions1()
        throws Exception {
        if (!supportsPessimistic)
            return;

        ModRuntimeTest2 b = createTest2();

        OpenJPAEntityManager pm = getPM(false, false);
        startTx(pm);

        pm.persist(b);
        pm.persist(b.getSelfOneOne());
        assertTest2Orig(b);
        changeTest2(b);

        rollbackTx(pm);

        assertTest2Orig(b);
        endEm(pm);
    }

    /**
     * Tests basic state transitions from transient to
     * persistent-transactional to persistent-nontransactional after commit.
     */
    public void testStateTransitions2()
        throws Exception {
        if (!supportsPessimistic)
            return;

        ModRuntimeTest2 b = createTest2();

        OpenJPAEntityManager pm = getPM(false, false);
        startTx(pm);

        pm.persist(b);
        assertTest2Orig(b);
        changeTest2(b);

        endTx(pm);

        assertTest2Changed(b, false);
        endEm(pm);
    }

    /**
     * Tests basic state transitions when finding a transactional
     * instance by id, then committing.
     */
    public void testStateTransitions3()
        throws Exception {
        if (!supportsPessimistic)
            return;

        OpenJPAEntityManager pm = getPM(false, false);
        startTx(pm);

        ModRuntimeTest2 b = pm.find(ModRuntimeTest2.class, _id);
        changeTest2(b);

        endTx(pm);

        assertTest2Changed(b, false);
        endEm(pm);
    }

    /**
     * Tests basic state transitions when finding a transactional
     * instance by id, then committing.
     */
    public void testStateTransitions3a()
        throws Exception {
        if (!supportsPessimistic)
            return;

        OpenJPAEntityManager pm = getPM(false, false);
        startTx(pm);

        ModRuntimeTest2 b = pm.find(ModRuntimeTest2.class, _id);
        changeTest2(b);

        endTx(pm);

        assertTest2Changed(b, false);
        endEm(pm);
    }

    /**
     * Tests basic state transitions when finding a transactional
     * instance by id, then rolling back.
     */
    public void testStateTransitions4()
        throws Exception {
        if (!supportsPessimistic)
            return;

        OpenJPAEntityManager pm = getPM(false, false);
        startTx(pm);

        ModRuntimeTest2 b = pm.find(ModRuntimeTest2.class, _id);
        assertTest2Orig(b);
        changeTest2(b);

        rollbackTx(pm);

        assertTest2Orig(b);
        endEm(pm);
    }

    /**
     * Tests basic state transitions when finding a transactional
     * instance by id, then rolling back.
     */
    public void testStateTransitions4a()
        throws Exception {
        if (!supportsPessimistic)
            return;

        OpenJPAEntityManager pm = getPM(false, false);
        startTx(pm);

        ModRuntimeTest2 b = pm.find(ModRuntimeTest2.class, _id);
        assertTest2Orig(b);
        changeTest2(b);

        rollbackTx(pm);

        assertTest2Orig(b);
        endEm(pm);
    }

    /**
     * Tests basic state transitions from persistent-transactional
     * to transient after delete and commit.
     */
    public void testStateTransitions5()
        throws Exception {
        if (!supportsPessimistic)
            return;

        OpenJPAEntityManager pm = getPM(false, false);
        startTx(pm);

        ModRuntimeTest2 b = pm.find(ModRuntimeTest2.class, _id);
        ModRuntimeTest1 parent = b.getSelfOneOne();
        assertNotNull("parent is null", parent);
        pm.remove(b);

        endTx(pm);

        // parent should be valid
        assertEquals("PARENT", parent.getStringField());

        // 'b' should be cleared
        assertNull(b.getStringField());
        assertEquals(0, b.getIntField());
        assertNull(b.getSelfOneOne());
    }

    /**
     * Tests basic state transitions from persistent-transactional
     * to persistent-nontransactional after delete and rollback.
     */
    public void testStateTransitions6()
        throws Exception {
        if (!supportsPessimistic)
            return;

        OpenJPAEntityManager pm = getPM(false, false);
        startTx(pm);

        ModRuntimeTest2 b = pm.find(ModRuntimeTest2.class, _id);
        assertTest2Orig(b);
        pm.remove(b);

        rollbackTx(pm);

        assertTest2Orig(b);
        endEm(pm);
    }

    /**
     * Tests basic state transitions from transient to
     * persistent-transactional and back to transient after rollback.
     */
    public void testOptStateTransitions1()
        throws Exception {
        ModRuntimeTest2 b = createTest2();

        OpenJPAEntityManager pm = getPM(true, false);
        startTx(pm);

        pm.persist(b);
        pm.persist(b.getSelfOneOne());
        assertTest2Orig(b);
        changeTest2(b);

        rollbackTx(pm);

        assertTest2Orig(b);
        endEm(pm);
    }

    /**
     * Tests basic state transitions from transient to
     * persistent-transactional to persistent-nontransactional after commit.
     */
    public void testOptStateTransitions2()
        throws Exception {
        ModRuntimeTest2 b = createTest2();

        OpenJPAEntityManager pm = getPM(true, false);
        startTx(pm);

        pm.persist(b);
        assertTest2Orig(b);
        changeTest2(b);

        endTx(pm);

        assertTest2Changed(b, false);
        endEm(pm);
    }

    /**
     * Tests basic state transitions when finding a transactional
     * instance by id, then committing.
     */
    public void testOptStateTransitions3()
        throws Exception {
        OpenJPAEntityManager pm = getPM(true, false);
        startTx(pm);

        ModRuntimeTest2 b = pm.find(ModRuntimeTest2.class, _id);
        assertTest2Orig(b);
        changeTest2(b);

        endTx(pm);

        assertTest2Changed(b, false);
        endEm(pm);
    }

    /**
     * Tests basic state transitions when finding a transactional
     * instance by id, then rolling back.
     */
    public void testOptStateTransitions4()
        throws Exception {
        OpenJPAEntityManager pm = getPM(true, false);
        startTx(pm);

        ModRuntimeTest2 b = pm.find(ModRuntimeTest2.class, _id);
        assertTest2Orig(b);
        changeTest2(b);

        rollbackTx(pm);

        assertTest2Orig(b);
        endEm(pm);
    }

    /**
     * Tests basic state transitions from persistent-transactional
     * to transient after delete and commit.
     */
    public void testOptStateTransitions5()
        throws Exception {
        OpenJPAEntityManager pm = getPM(true, false);
        startTx(pm);

        ModRuntimeTest2 b = pm.find(ModRuntimeTest2.class, _id);
        ModRuntimeTest1 parent = b.getSelfOneOne();
        assertNotNull("parent is null", parent);
        pm.remove(b);

        endTx(pm);

        // parent should be valid
        assertEquals("PARENT", parent.getStringField());

        // 'b' should not be cleared JPA 2.0 "3.2.3 Removal"
        assertNotNull(b.getStringField());
        assertNotEquals(0, b.getIntField());
        assertNotNull(b.getSelfOneOne());
    }

    /**
     * Tests basic state transitions from persistent-transactional
     * to persistent-nontransactional after delete and rollback.
     */
    public void testOptStateTransitions6()
        throws Exception {
        OpenJPAEntityManager pm = getPM(true, false);
        startTx(pm);

        ModRuntimeTest2 b = pm.find(ModRuntimeTest2.class, _id);
        pm.remove(b);

        rollbackTx(pm);

        assertTest2Orig(b);
        endEm(pm);
    }

    /**
     * Tests basic state transitions from transient to
     * persistent-transactional and back to transient after rollback.
     */
    public void testOptRetainStateTransitions1()
        throws Exception {
        ModRuntimeTest2 b = createTest2();

        OpenJPAEntityManager pm = getPM(true, true);
        startTx(pm);

        pm.persist(b);
        pm.persist(b.getSelfOneOne());
        assertTest2Orig(b);
        changeTest2(b);

        rollbackTx(pm);

        assertTest2Orig(b);
        endEm(pm);
    }

    /**
     * Tests basic state transitions from transient to
     * persistent-transactional to persistent-nontransactional after commit.
     */
    public void testOptRetainStateTransitions2()
        throws Exception {
        ModRuntimeTest2 b = createTest2();

        OpenJPAEntityManager pm = getPM(true, true);
        startTx(pm);

        pm.persist(b);
        assertTest2Orig(b);
        changeTest2(b);

        endTx(pm);

        assertTest2Changed(b, true);
        endEm(pm);
    }

    /**
     * Tests basic state transitions when finding a transactional
     * instance by id, then committing.
     */
    public void testOptRetainStateTransitions3()
        throws Exception {
        OpenJPAEntityManager pm = getPM(true, true);
        startTx(pm);

        ModRuntimeTest2 b = pm.find(ModRuntimeTest2.class, _id);
        assertTest2Orig(b);
        changeTest2(b);

        endTx(pm);

        assertTest2Changed(b, true);
        endEm(pm);
    }

    /**
     * Tests basic state transitions when finding a transactional
     * instance by id, then rolling back.
     */
    public void testOptRetainStateTransitions4()
        throws Exception {
        OpenJPAEntityManager pm = getPM(true, true);
        startTx(pm);

        ModRuntimeTest2 b = pm.find(ModRuntimeTest2.class, _id);
        assertTest2Orig(b);
        changeTest2(b);

        rollbackTx(pm);

        assertTest2Orig(b);
        endEm(pm);
    }

    /**
     * Tests basic state transitions from persistent-transactional
     * to transient after delete and commit.
     */
    public void testOptRetainStateTransitions5()
        throws Exception {
        OpenJPAEntityManager pm = getPM(true, true);
        startTx(pm);

        ModRuntimeTest2 b = pm.find(ModRuntimeTest2.class, _id);
        ModRuntimeTest1 parent = b.getSelfOneOne();

        assertNotNull("parent is null", parent);
        pm.remove(b);

        endTx(pm);

        // parent should be valid
        assertEquals("PARENT", parent.getStringField());

        // 'b' should not be cleared JPA 2.0 "3.2.3 Removal"
        assertNotNull(b.getStringField());
        assertNotEquals(0, b.getIntField());
        assertNotNull(b.getSelfOneOne());
    }

    /**
     * Tests basic state transitions from persistent-transactional
     * to persistent-nontransactional after delete and rollback.
     */
    public void testOptRetainStateTransitions6()
        throws Exception {
        OpenJPAEntityManager pm = getPM(true, true);
        startTx(pm);

        ModRuntimeTest2 b = pm.find(ModRuntimeTest2.class, _id);
        pm.remove(b);

        rollbackTx(pm);

        if (pm.getRestoreState() != RestoreStateType.NONE)
            assertTest2Orig(b);
        else
            assertNull(b.getStringField());
        endEm(pm);
    }

    /**
     * Tests basic state transitions from transient to
     * persistent-transactional and back to transient after rollback.
     */
    public void testRetainStateTransitions1()
        throws Exception {
        if (!supportsPessimistic)
            return;

        ModRuntimeTest2 b = createTest2();

        OpenJPAEntityManager pm = getPM(false, true);
        startTx(pm);

        pm.persist(b);
        pm.persist(b.getSelfOneOne());
        assertTest2Orig(b);
        changeTest2(b);

        rollbackTx(pm);

        assertTest2Orig(b);
        endEm(pm);
    }

    /**
     * Tests basic state transitions from transient to
     * persistent-transactional to persistent-nontransactional after commit.
     */
    public void testRetainStateTransitions2()
        throws Exception {
        if (!supportsPessimistic)
            return;

        ModRuntimeTest2 b = createTest2();

        OpenJPAEntityManager pm = getPM(false, true);
        startTx(pm);

        pm.persist(b);
        assertTest2Orig(b);
        changeTest2(b);

        endTx(pm);

        assertTest2Changed(b, true);
        endEm(pm);
    }

    /**
     * Tests basic state transitions when finding a transactional
     * instance by id, then committing.
     */
    public void testRetainStateTransitions3()
        throws Exception {
        if (!supportsPessimistic)
            return;

        OpenJPAEntityManager pm = getPM(false, true);
        startTx(pm);

        ModRuntimeTest2 b = pm.find(ModRuntimeTest2.class, _id);
        assertTest2Orig(b);
        changeTest2(b);

        endTx(pm);

        assertTest2Changed(b, true);
        endEm(pm);
    }

    /**
     * Tests basic state transitions when finding a transactional
     * instance by id, then rolling back.
     */
    public void testRetainStateTransitions4()
        throws Exception {
        if (!supportsPessimistic)
            return;

        OpenJPAEntityManager pm = getPM(false, true);
        startTx(pm);

        ModRuntimeTest2 b = pm.find(ModRuntimeTest2.class, _id);
        assertTest2Orig(b);
        changeTest2(b);

        rollbackTx(pm);

        assertTest2Orig(b);
        endEm(pm);
    }

    /**
     * Tests basic state transitions from persistent-transactional
     * to transient after delete and commit.
     */
    public void testRetainStateTransitions5() throws Exception {
        if (!supportsPessimistic)
            return;

        OpenJPAEntityManager pm = getPM(false, true);
        startTx(pm);

        ModRuntimeTest2 b = pm.find(ModRuntimeTest2.class, _id);
        ModRuntimeTest1 parent = b.getSelfOneOne();
        assertNotNull("parent is null", parent);
        pm.remove(b);

        endTx(pm);

        // parent should be valid
        assertEquals("PARENT", parent.getStringField());

        // 'b' should be cleared
        assertNull(b.getStringField());
        assertEquals(0, b.getIntField());
        assertNull(b.getSelfOneOne());
    }

    /**
     * Tests basic state transitions from persistent-transactional
     * to persistent-nontransactional after delete and rollback.
     */
    public void testRetainStateTransitions6()
        throws Exception {
        if (!supportsPessimistic)
            return;

        OpenJPAEntityManager pm = getPM(false, true);
        startTx(pm);

        ModRuntimeTest2 b = pm.find(ModRuntimeTest2.class, _id);
        pm.remove(b);

        rollbackTx(pm);

        assertTest2Orig(b);
        endEm(pm);
    }

    /**
     * Tests basic state transitions from transient to
     * transient-dirty and back to transient after rollback.
     */
    public void testTransientStateTransitions1()
        throws Exception {
        if (!supportsPessimistic)
            return;

        ModRuntimeTest2 b = createTest2();

        OpenJPAEntityManager pm = getPM(false, false);
        startTx(pm);

        pm.transactional(b, true);
        pm.transactional(b.getSelfOneOne(), true);
        changeTest2(b);

        rollbackTx(pm);

        assertTest2Orig(b);
        endEm(pm);
    }

    /**
     * Tests basic state transitions from transient to
     * transient-transactional and stick on commit.
     */
    public void testTransientStateTransitions2()
        throws Exception {
        if (!supportsPessimistic)
            return;

        ModRuntimeTest2 b = createTest2();

        OpenJPAEntityManager pm = getPM(false, false);
        startTx(pm);

        pm.transactional(b, true);
        pm.transactional(b.getSelfOneOne(), true);
        changeTest2(b);

        endTx(pm);

        assertTest2Changed(b, false);
        endEm(pm);
    }

    /**
     * Tests state transitions from PClean to transient.
     */
    public void testTransientStateTransitions3()
        throws Exception {
        if (!supportsPessimistic)
            return;

        ModRuntimeTest2 b = createTest2();

        OpenJPAEntityManager pm = getPM(false, false);
        startTx(pm);
        pm.persist(b);
        int oid = b.getId();
        endTx(pm);
        endEm(pm);

        pm = getPM(false, false);
        //FIXME jthomas
        b = pm.find(ModRuntimeTest2.class, oid);
        pm.retrieve(b);
        //FIXME jthomas
        endEm(pm);

        // note that at this point, parent is not transient, just retrieved.
        assertNotNull("b is null", b.getSelfOneOne());
    }

    private ModRuntimeTest2 createTest2() {
        return createTest2("NAME", 50);
    }

    private ModRuntimeTest2 createTest2(String str, int i) {
        return createTest2(new ModRuntimeTest2(str, i));
    }

    private ModRuntimeTest2 createTest2(ModRuntimeTest2 b) {
        ModRuntimeTest1 parent = new ModRuntimeTest1("PARENT", 70);
        b.setSelfOneOne(parent);
        return b;
    }

    private void changeTest2(ModRuntimeTest2 b) {
        PersistenceAware.setModTransString(b, "999");
        b.setStringField("CHANGED");
        b.setIntField(1000);
        b.getSelfOneOne().setStringField("PCHANGED");
        b.setSelfOneOne(null);
    }

    private void assertTest2Changed(ModRuntimeTest2 b, boolean retainValues) {
        if (retainValues)
            assertEquals("999", PersistenceAware.getModTransString(b));
        assertEquals("CHANGED", b.getStringField());
        assertEquals(1000, b.getIntField());
        assertNull(b.getSelfOneOne());
    }

    private void assertTest2Orig(ModRuntimeTest2 b) {
        assertEquals("NAME", b.getStringField());
        assertEquals(50, b.getIntField());
        assertNotNull(b.getSelfOneOne());
        assertEquals("PARENT", b.getSelfOneOne().getStringField());

        assertEquals(
            "transactional field 'transString' was not the "
                + "same as it was originally. Ensure that "
                + "openjpa.kernel.PersistenceAware is enhanced. "
                +
                "It is persistence-aware, so will not be enhanced by commands "
                + "like jdoc $(find test -name '*.jdo').", null,
            PersistenceAware.getModTransString(b));
    }

    /**
     * Assert that the given object is persistent.
     */
    public void assertTransient(Object a) {
        OpenJPAEntityManager pm = currentEntityManager();
        assertTrue(!pm.isPersistent(a));
        assertTrue(!pm.isTransactional(a));
        assertTrue(!pm.isNewlyPersistent(a));
        assertTrue(!pm.isDirty(a));
        assertTrue(!pm.isRemoved(a));
        assertNull(pm.getObjectId(a));
        assertNull(OpenJPAPersistence.getEntityManager(a));
        endEm(pm);
    }

    /**
     * Assert that the given object is persistent and is in the given state.
     */
    public void assertPersistent(Object a, boolean isTrans, boolean isNew,
        boolean isDeleted, boolean isDirty) {
        OpenJPAEntityManager pm = currentEntityManager();
        assertTrue(pm.isPersistent(a));

        assertTrue(a instanceof PersistenceCapable);

        assertEquals(isTrans, pm.isTransactional(a));
		assertEquals(isNew, pm.isNewlyPersistent(a));
		assertEquals(isDeleted, pm.isRemoved(a));
		assertEquals(isDirty || isNew || isDeleted, pm.isDirty(a));
		assertNotNull(pm.getObjectId(a));
		assertNotNull(OpenJPAPersistence.getEntityManager(a));

		endEm(pm);
	}

}

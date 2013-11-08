/*
 * TestSavepoints.java
 *
 * Created on October 16, 2006, 11:16 AM
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

import java.util.Iterator;
import java.util.Properties;



import org.apache.openjpa.persistence.kernel.common.apps.ModRuntimeTest1;
import org.apache.openjpa.persistence.kernel.common.apps.ModRuntimeTest2;
import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest4;
import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest5;

import org.apache.openjpa.kernel.PCState;
import org.apache.openjpa.persistence.JPAFacadeHelper;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAPersistence;

public class TestSavepoints extends BaseKernelTest {

    /**
     * Creates a new instance of TestSavepoints
     */
    public TestSavepoints(String name) {
        super(name);
    }

    protected String getSavepointPlugin() {
        return "in-mem";
    }

    public Properties getProperties(String[] props) {
        Properties properties = super.getProperties(props);
        properties.put("openjpa.SavepointManager", getSavepointPlugin());
        return properties;
    }

    public void setUp() throws Exception {
        super.setUp(ModRuntimeTest1.class, ModRuntimeTest2.class, RuntimeTest4.class, RuntimeTest5.class);
    }

    public void testSimple() {
        doSimpleTest(true, 0, 0);
        doSimpleTest(true, 2, 0);
        doSimpleTest(true, 0, 2);
        doSimpleTest(false, 0, 0);
        doSimpleTest(false, 2, 0);
        doSimpleTest(false, 0, 2);
    }

    private void doSimpleTest(boolean newPC, int before, int after) {
        OpenJPAEntityManager pm = getPM();
        ModRuntimeTest1 pc = new ModRuntimeTest1();
        startTx(pm);
        pc.setStringField("orig");
        pc.setIntField(-11);
        //FIXME jthomas - setDateField
        //pc.setDateField(randomDate());
        pm.persist(pc);
        Object oid = pm.getObjectId(pc);

        if (!newPC) {
            endTx(pm);
            pm = getPM();
            startTx(pm);
            pc = pm.find(ModRuntimeTest1.class, oid);
        }
        for (int i = 0; i < before; i++) {
            pc.setStringField("before" + i);
            pc.setIntField(i);
            //FIXME jthomas - setDateField
            //pc.setDateField(randomDate());
            pm.setSavepoint("before" + i);
        }

        pc.setStringField("value");
        pc.setIntField(333);
        //FIXME jthomas - setDateField
        //pc.setDateField(randomDate());
        //Date date = (Date) pc.getDateField().clone();
        pm.setSavepoint("test");

        for (int i = 0; i < after; i++) {
            pc.setStringField("after" + i);
            pc.setIntField(i * 10);
            //FIXME jthomas - setDateField
            //pc.setDateField(randomDate());
            pm.setSavepoint("after" + i);
        }

        pm.rollbackToSavepoint("test");
        assertEquals("value", pc.getStringField());
        assertEquals(333, pc.getIntField());
        //FIXME jthomas - setDateField
        //assertEquals(date, pc.getDateField());
        endTx(pm);
        endEm(pm);

        pm = getPM();
        pc = pm.find(ModRuntimeTest1.class, oid);
        assertEquals("value", pc.getStringField());
        assertEquals(333, pc.getIntField());
        //FIXME jthomas - setDateField
        //assertEquals(date, pc.getDateField());
        endEm(pm);
    }

    public void testCleanOrdering() {
        OpenJPAEntityManager pm = getPM();
        ModRuntimeTest1 pc = new ModRuntimeTest1("orig", 1);
        startTx(pm);
        pm.persist(pc);
        Object oid = pm.getObjectId(pc);
        endTx(pm);
        endEm(pm);

        pm = getPM();
        pm.setOptimistic(false);
        startTx(pm);
        ModRuntimeTest1 pc2 = new ModRuntimeTest1("foo", 2);
        pm.persist(pc2);
        pm.setSavepoint("s1");
        pc = pm.find(ModRuntimeTest1.class, oid);
        assertTrue(pm.isTransactional(pc));
        pc.setStringField("test");
        pm.setSavepoint("s2");
        pc.setStringField("bar");
        pm.rollbackToSavepoint("s2");
        assertTrue(pm.isTransactional(pc));

        rollbackTx(pm);
        endEm(pm);
    }

    public void testLastSavepoint() {
        OpenJPAEntityManager pm = getPM();
        ModRuntimeTest1 pc = new ModRuntimeTest1("orig", 1);
        startTx(pm);
        pm.persist(pc);
        Object oid = pm.getObjectId(pc);
        endTx(pm);
        endEm(pm);

        pm = getPM();
        pm.setOptimistic(false);
        startTx(pm);
        pc = pm.find(ModRuntimeTest1.class, oid);
        pc.setStringField("s1");
        pm.setSavepoint("s1");
        pc.setStringField("s2");
        pm.setSavepoint("s2");
        pc.setStringField("diff");
        pm.rollbackToSavepoint();
        assertEquals("s2", pc.getStringField());
        pm.releaseSavepoint();
        try {
            pm.rollbackToSavepoint("s1");
            fail("Exhausted.");
        } catch (Exception e) {
        }
        rollbackTx(pm);
        endEm(pm);
    }

    public void testNewRollback() {
        doNewRollbackTest(false, 0, 0);
        doNewRollbackTest(false, 2, 0);
        doNewRollbackTest(false, 0, 2);
        doNewRollbackTest(true, 0, 0);
        doNewRollbackTest(true, 2, 0);
        doNewRollbackTest(true, 0, 2);
    }

    public void doNewRollbackTest(boolean restore, int before, int after) {
        OpenJPAEntityManager pm = getPM();
        pm.setRetainState(restore);
        startTx(pm);

        for (int i = 0; i < before; i++) {
            pm.persist(new ModRuntimeTest1("s" + i, i));
            //pm.setSavepoint("before" + i);
        }
        pm.setSavepoint("test");

        ModRuntimeTest1 pc = new ModRuntimeTest1();
        pc.setStringField("orig");
        pm.persist(pc);
        Object oid = pm.getObjectId(pc);

        for (int i = 0; i < after; i++) {
            pm.persist(new ModRuntimeTest1());
            pm.setSavepoint("after" + i);
        }

        pm.rollbackToSavepoint("test");
        assertEquals("orig", pc.getStringField());
        assertFalse(pm.isPersistent(pc));
        assertEquals(before, pm.getTransactionalObjects().size());
        endTx(pm);
        endEm(pm);

        pm = getPM();
        assertNull(pm.find(ModRuntimeTest1.class, oid));
        endEm(pm);
    }

    public void testNewRelation() {
        doNewRelationTest(true, 0, 0);
        doNewRelationTest(true, 2, 0);
        doNewRelationTest(true, 0, 2);
        doNewRelationTest(false, 0, 0);
        doNewRelationTest(false, 2, 0);
        doNewRelationTest(false, 0, 2);
    }

    public void doNewRelationTest(boolean nullRel, int before, int after) {
        deleteAll(ModRuntimeTest1.class);

        OpenJPAEntityManager pm = getPM();
        startTx(pm);
        ModRuntimeTest1 pc = new ModRuntimeTest1();
        pc.setStringField("orig");
        if (!nullRel)
            pc.setSelfOneOne(new ModRuntimeTest1("one", 1));
        pm.persist(pc);
        Object oid = pm.getObjectId(pc);
        endTx(pm);
        endEm(pm);

        pm = getPM();
        pm.setRetainState(true);
        startTx(pm);
        pc = pm.find(ModRuntimeTest1.class, oid);

        for (int i = 0; i < before; i++) {
            pc.setSelfOneOne(new ModRuntimeTest1("before" + i, i));
            pm.setSavepoint("before" + i);
        }

        pm.setSavepoint("test");
        pc.setSelfOneOne(new ModRuntimeTest1("new", 2));
        ModRuntimeTest1 pc2 = pc.getSelfOneOne();

        for (int i = 0; i < after; i++) {
            pc.setSelfOneOne(new ModRuntimeTest1());
            pm.setSavepoint("after" + i);
        }

        pm.rollbackToSavepoint("test");
        assertEquals("orig", pc.getStringField());
        assertFalse(pm.isPersistent(pc2));
        if (before > 0)
            assertEquals("before" + (before - 1),
                pc.getSelfOneOne().getStringField());
        else {
            if (nullRel)
                assertNull(pc.getSelfOneOne());
            else
                assertEquals("one", pc.getSelfOneOne().getStringField());
        }
        endTx(pm);
        endEm(pm);

        pm = getPM();
        pc = pm.find(ModRuntimeTest1.class, oid);
        assertEquals("orig", pc.getStringField());
        if (before > 0)
            assertEquals("before" + (before - 1),
                pc.getSelfOneOne().getStringField());
        else {
            if (nullRel)
                assertNull(pc.getSelfOneOne());
            else
                assertEquals("one", pc.getSelfOneOne().getStringField());
        }
        endEm(pm);
    }

    public void testNullRelation() {
        doNullRelationTest(true, 0, 0);
        doNullRelationTest(true, 2, 0);
        doNullRelationTest(true, 0, 2);
        doNullRelationTest(false, 0, 0);
        doNullRelationTest(false, 2, 0);
        doNullRelationTest(false, 0, 2);
    }

    public void doNullRelationTest(boolean retain, int before, int after) {
        OpenJPAEntityManager pm = getPM();
        startTx(pm);
        ModRuntimeTest1 pc = new ModRuntimeTest1();
        pc.setStringField("orig");
        pc.setSelfOneOne(new ModRuntimeTest1("one", 1));
        pm.persist(pc);
        Object oid = pm.getObjectId(pc);
        endTx(pm);
        endEm(pm);

        pm = getPM();
        pm.setRetainState(true);
        startTx(pm);
        pc = pm.find(ModRuntimeTest1.class, oid);

        for (int i = 0; i < before; i++) {
            pc.setSelfOneOne(new ModRuntimeTest1("before" + i, i));
            pm.setSavepoint("before" + i);
        }

        pm.setSavepoint("test");
        pc.setSelfOneOne(null);

        for (int i = 0; i < after; i++) {
            pc.setSelfOneOne(new ModRuntimeTest1());
            pm.setSavepoint("after" + i);
        }

        pm.rollbackToSavepoint("test");
        assertEquals("orig", pc.getStringField());
        if (before > 0)
            assertEquals("before" + (before - 1),
                pc.getSelfOneOne().getStringField());
        else
            assertEquals("one", pc.getSelfOneOne().getStringField());
        endTx(pm);
        endEm(pm);

        pm = getPM();
        pc = pm.find(ModRuntimeTest1.class, oid);
        assertEquals("orig", pc.getStringField());
        if (before > 0)
            assertEquals("before" + (before - 1),
                pc.getSelfOneOne().getStringField());
        else
            assertEquals("one", pc.getSelfOneOne().getStringField());
        endEm(pm);
    }

    public void testCollection() {
        doCollectionTest(true, 0, 0);
        doCollectionTest(true, 2, 0);
        doCollectionTest(true, 0, 2);
        doCollectionTest(false, 0, 0);
        doCollectionTest(false, 2, 0);
        doCollectionTest(false, 0, 2);
    }

    public void doCollectionTest(boolean newPC, int before, int after) {
        OpenJPAEntityManager pm = getPM();
        startTx(pm);
        ModRuntimeTest1 pc = new ModRuntimeTest1("orig", 1);
        ModRuntimeTest1 pc2 = newElement(pc, "persist", 2);
        ModRuntimeTest1 pc3 = newElement(pc, "delete", 3);
        pm.persist(pc);
        pm.persist(pc3);
        Object oid = pm.getObjectId(pc);
        ModRuntimeTest1 temp;
        if (!newPC) {
            endTx(pm);
            endEm(pm);

            pm = getPM();
            startTx(pm);
            pc = pm.find(ModRuntimeTest1.class, oid);
            assertEquals(2, pc.getSelfOneMany().size());
            for (Iterator it = pc.getSelfOneMany().iterator(); it.hasNext();) {
                temp = (ModRuntimeTest1) it.next();
                if (temp.getIntField() == 2)
                    pc2 = temp;
                else if (temp.getIntField() == 3)
                    pc3 = temp;
                else
                    fail("unknown");
            }
        }

        for (int i = 0; i < before; i++) {
            newElement(pc, "before" + i, (i + 1) * 10);
            pm.setSavepoint("before" + i);
        }
        pm.setSavepoint("test");
        pm.remove(pc3);
        pc.getSelfOneMany().remove(pc2);

        // kodo 4 is more stringent on deleted relations.
        pc.getSelfOneMany().remove(pc3);
        pc2.setSelfOneMany(null);

        for (int i = 0; i < after; i++) {
            newElement(pc, "after" + i, (i + 1) * -10);
            pm.setSavepoint("after" + i);
        }

        pm.rollbackToSavepoint("test");

        assertEquals("orig", pc.getStringField());
        assertFalse(pm.isRemoved(pc2));
        for (Iterator it = pc.getSelfOneMany().iterator(); it.hasNext();) {
            temp = (ModRuntimeTest1) it.next();
            assertFalse(pm.isRemoved(temp));
            assertEquals(pc, temp.getSelfOneOne());
            if (temp.getIntField() < 0)
                fail("shouldn't be here:" + temp.getStringField());
        }
        assertTrue(pc.getSelfOneMany().contains(pc2));
        assertTrue(pc.getSelfOneMany().contains(pc3));
        assertEquals(2 + before, pc.getSelfOneMany().size());
        endTx(pm);
        endEm(pm);

        pm = getPM();
        pc = pm.find(ModRuntimeTest1.class, oid);
        assertEquals("orig", pc.getStringField());
        assertEquals(2 + before, pc.getSelfOneMany().size());
        boolean found2 = false;
        boolean found3 = false;
        for (Iterator it = pc.getSelfOneMany().iterator(); it.hasNext();) {
            temp = (ModRuntimeTest1) it.next();
            assertEquals(pc, temp.getSelfOneOne());
            if (temp.getIntField() < 0)
                fail("shouldn't be here:" + temp.getStringField());
            else if (temp.getIntField() == 2)
                found2 = true;
            else if (temp.getIntField() == 3)
                found3 = true;
        }
        assertTrue(found2 && found3);
        endEm(pm);
    }

    public void testChangeTracker() {
        OpenJPAEntityManager pm = getPM();
        startTx(pm);
        RuntimeTest4 pc = new RuntimeTest4("orig");
        for (int i = 0; i < 12; i++)
            pc.getRuntimeTest5s().add(new RuntimeTest5("five" + i));
        pm.persist(pc);
        Object oid = pm.getObjectId(pc);
        endTx(pm);
        endEm(pm);

        pm = getPM();
        startTx(pm);
        pc = pm.find(RuntimeTest4.class, oid);
        assertEquals(12, pc.getRuntimeTest5s().size());
        int count = 0;
        for (Iterator i = pc.getRuntimeTest5s().iterator();
            count < 2; count++) {
            i.next();
            i.remove();
        }
        assertEquals(10, pc.getRuntimeTest5s().size());
        pm.setSavepoint("test");
        count = 0;
        for (Iterator i = pc.getRuntimeTest5s().iterator();
            count < 2; count++) {
            i.next();
            i.remove();
        }
        assertEquals(8, pc.getRuntimeTest5s().size());
        endTx(pm);
        endEm(pm);

        pm = getPM();
        pc = pm.find(RuntimeTest4.class, oid);
        assertEquals(8, pc.getRuntimeTest5s().size());
        endEm(pm);
    }

    private ModRuntimeTest1 newElement(ModRuntimeTest1 one, String str, int i) {
        ModRuntimeTest1 two = new ModRuntimeTest1(str, i);
        two.setSelfOneOne(one);
        one.getSelfOneMany().add(two);
        return two;
    }

    public static PCState getState(Object o) {
        OpenJPAEntityManager pm = OpenJPAPersistence.getEntityManager(o);

        if (pm == null)
            return null;
        return JPAFacadeHelper.toBroker(pm).getStateManager(o).getPCState();
    }
}

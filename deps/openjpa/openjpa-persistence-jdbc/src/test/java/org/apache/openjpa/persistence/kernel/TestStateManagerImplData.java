/*
 * TestStateManagerImplData.java
 *
 * Created on October 13, 2006, 5:40 PM
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



import org.apache.openjpa.persistence.kernel.common.apps.ModRuntimeTest1;
import org.apache.openjpa.persistence.kernel.common.apps.ModRuntimeTest2;

import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.persistence.OpenJPAEntityManager;

public class TestStateManagerImplData extends BaseKernelTest {

    ClassMetaData _meta;

    Boolean _f1;

    Boolean _f3;

    /**
     * Creates a new instance of TestStateManagerImplData
     */
    public TestStateManagerImplData() {
    }

    public TestStateManagerImplData(String test) {
        super(test);
    }

    public void setUp() throws Exception {
        super.setUp(ModRuntimeTest1.class, ModRuntimeTest2.class);
    }

    public void setUpMetaData(ClassMetaData meta) {
        _meta = meta;
        _f1 = _meta.getField(1).usesImplData();
        _f3 = _meta.getField(3).usesImplData();
        _meta.getField(1).setUsesImplData(Boolean.TRUE);
        _meta.getField(3).setUsesImplData(null);
    }

    public void tearDownMetaData() {
        _meta.getField(1).setUsesImplData(_f1);
        _meta.getField(3).setUsesImplData(_f3);
    }

    public void testImplData() {
        ModRuntimeTest1 pc = new ModRuntimeTest1();

        OpenJPAEntityManager pm = getPM(false, false);
        startTx(pm);
        pm.persist(pc);
        OpenJPAStateManager sm = getStateManager(pc, pm);
        setUpMetaData(sm.getMetaData());
        try {
            // test instance level
            Object inst = new Object();
            assertNull(sm.getImplData());
            assertNull(sm.setImplData(inst, true));
            assertEquals(inst, sm.getImplData());
            assertTrue(sm.isImplDataCacheable());
            assertEquals(inst, sm.setImplData(null, false));
            assertNull(sm.getImplData());
            assertFalse(sm.isImplDataCacheable());
            sm.setImplData(inst, false);
            assertFalse(sm.isImplDataCacheable());

            // test field level
            Object f1 = new Object();
            Object f3 = new Object();

            assertNull(sm.getImplData(1));
            assertFalse(sm.isImplDataCacheable(1));
            assertNull(sm.setImplData(1, f1));
            assertEquals(f1, sm.getImplData(1));
            assertTrue(!sm.isImplDataCacheable(1));
            assertEquals(f1, sm.setImplData(1, null));
            assertNull(sm.getImplData(1));
            assertFalse(sm.isImplDataCacheable(1));
            sm.setImplData(1, f1);

            assertNull(sm.setImplData(3, f3));
            assertEquals(f3, sm.getImplData(3));
            assertTrue(sm.isImplDataCacheable(3));
            assertEquals(f1, sm.getImplData(1));

            // this should clear field data
            endTx(pm);

            assertEquals(inst, sm.getImplData());
            assertNull(sm.getImplData(1));
            assertNull(sm.getImplData(3));

            endEm(pm);
        } finally {
            tearDownMetaData();
        }
    }

    public void testNotClearedIfRetainValues() {
        notClearedIfRetainValuesTest(true);
        notClearedIfRetainValuesTest(false);
    }

    private void notClearedIfRetainValuesTest(boolean optimistic) {
        ModRuntimeTest1 pc = new ModRuntimeTest1("pc", 1);
        int key = 1;
        Object value = new Object();

        // make instance persistent
        OpenJPAEntityManager pm = getPM(optimistic, true);
        startTx(pm);
        pm.persist(pc);
        Object oid = pm.getObjectId(pc);

        OpenJPAStateManager sm = getStateManager(pc, pm);
        assertNotNull(sm);
        setUpMetaData(sm.getMetaData());
        try {
            // set impl data
            sm.setImplData(key, value);
            assertEquals(value, sm.getImplData(key));
            endTx(pm);

            // test in nontransactional setting
            assertEquals(value, sm.getImplData(key));

            // test in next transaction
            startTx(pm);
            pc = pm.find(ModRuntimeTest1.class, oid);
            sm = getStateManager(pc, pm);
            assertNotNull(sm);
            if (pm.getOptimistic())
                assertEquals(value, sm.getImplData(key));
            else
                assertNull(sm.getImplData(key));
            endTx(pm);
            endEm(pm);
        } finally {
            tearDownMetaData();
        }

        // test in another pm for good measure
        pm = getPM(optimistic, true);
        pc = pm.find(ModRuntimeTest1.class, oid);
        sm = getStateManager(pc, pm);
        assertNotNull(sm);
        setUpMetaData(sm.getMetaData());
        try {
            sm.setImplData(key, value);
            assertEquals(value, sm.getImplData(key));

            // test in transaction; re-lookup pc to be sure it enters the trans
            startTx(pm);
            pc = pm.find(ModRuntimeTest1.class, oid);
            if (pm.getOptimistic())
                assertEquals(value, sm.getImplData(key));
            else {
                assertNull(sm.getImplData(key));
                sm.setImplData(key, value);
            }
            endTx(pm);

            // test outside of transaction
            assertEquals(value, sm.getImplData(key));
            endEm(pm);
        } finally {
            tearDownMetaData();
        }
    }
}

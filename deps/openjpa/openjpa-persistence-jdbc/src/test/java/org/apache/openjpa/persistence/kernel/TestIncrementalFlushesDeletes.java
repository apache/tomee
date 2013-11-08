/*
 * TestIncrementalFlushesDeletes.java
 *
 * Created on October 12, 2006, 11:38 AM
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



import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest1;
import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest2;
import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest3;

import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.kernel.PCState;
import org.apache.openjpa.persistence.OpenJPAEntityManager;

public class TestIncrementalFlushesDeletes extends BaseKernelTest {

    private OpenJPAEntityManager pm;
    private Object oid;
    private RuntimeTest1 a;

    public TestIncrementalFlushesDeletes(String str) {
        super(str);
    }

    /**
     * Creates a new instance of TestIncrementalFlushesDeletes
     */
    public TestIncrementalFlushesDeletes() {
    }

    public void setUp() throws Exception {
        super.setUp(RuntimeTest1.class, RuntimeTest2.class, RuntimeTest3.class);

        pm = getPM(true, false);
        startTx(pm);
        a = new RuntimeTest1("foo", 10);
        pm.persist(a);
        oid = pm.getObjectId(a);
    }

    public void tearDown() throws Exception {
        endTx(pm);

        OpenJPAEntityManager newPm = getPM();
        try {
            Object o = newPm.find(RuntimeTest1.class, oid);
            if (o != null) {
                fail("should not be able to load deleted object");
            }
        } catch (Exception e) {
            // expected case
        }

        endEm(newPm);
        endEm(pm);

        super.tearDown();
    }

    private void assertState(PCState state, boolean flushed) {
        OpenJPAStateManager sm = getStateManager(a, pm);
        assertNotNull(sm);
        assertEquals(flushed, sm.isFlushed());
        assertEquals(state, sm.getPCState());
    }

    /**
     * PNew => PNewDeleted
     */
    public void testNewDeleted() {
        pm.remove(a);
        assertState(PCState.PNEWDELETED, false);
    }

    /**
     * PNew => PNewDeleted => PNewDeletedFlushed
     */
    public void testNewDeletedFlushed() {
        pm.remove(a);
        assertState(PCState.PNEWDELETED, false);
        pm.flush();
        assertState(PCState.PNEWDELETED, true);
    }

    /**
     * PNew => PNewFlushed => PNewFlushedDeleted
     */
    public void testNewFlushedDeleted() {
        pm.flush();
        assertState(PCState.PNEW, true);
        pm.remove(a);
        assertState(PCState.PNEWFLUSHEDDELETED, true);
    }

    /**
     * PNew => PNewFlushed => PNewFlushedDeleted => PNewFlushedDeletedFlushed
     */
    public void testNewFlushedDeletedFlushed() {
        pm.flush();
        assertState(PCState.PNEW, true);
        pm.remove(a);
        assertState(PCState.PNEWFLUSHEDDELETED, true);
        pm.flush();
        assertState(PCState.PNEWFLUSHEDDELETEDFLUSHED, true);
    }

/*
* 	### some remaining test deletes:
*		PDirty => PDeleted => PDeletedFlushed
*       PDirty => (change and flush) PDirty => PDeleted => PDeletedFlushed
*		PClean => PDeleted => PDeletedFlushed
*		Hollow => PDeleted => PDeletedFlushed
*/
}

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
package org.apache.openjpa.persistence.meta;


import org.apache.openjpa.persistence.meta.common.apps.SequenceAssigned;
import org.apache.openjpa.persistence.meta.common.apps.SequenceAssigned2;
import org.apache.openjpa.persistence.meta.common.apps.SequenceAssigned3;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;
import org.apache.openjpa.persistence.OpenJPAEntityManager;

public class TestSequenceAssigned
    extends AbstractTestCase {

    public TestSequenceAssigned(String testName) {
        super(testName, "metacactusapp");
    }

    public void setUp() {
        deleteAll(SequenceAssigned.class);
        deleteAll(SequenceAssigned3.class);
    }

    public void testGetObjectId() {
        SequenceAssigned pc = new SequenceAssigned();
        OpenJPAEntityManager pm =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(pm);
        pm.persist(pc);
        SequenceAssigned.Id id = (SequenceAssigned.Id)
            pm.getObjectId(pc);
        assertTrue(0 != id.pk);
        assertTrue(0 != pc.getPK());
        assertEquals(pc.getPK(), id.pk);
        endTx(pm);
        endEm(pm);
    }

    public void testGetValue() {
        SequenceAssigned pc = new SequenceAssigned();
        OpenJPAEntityManager pm =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(pm);
        assertEquals(0, pc.getPK());
        pm.persist(pc);
        assertTrue(0 != pc.getPK());
        endTx(pm);
        endEm(pm);
    }

    public void testPrimitive() {
        SequenceAssigned pc = new SequenceAssigned();
        SequenceAssigned pc2 = new SequenceAssigned();
        OpenJPAEntityManager pm =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(pm);
        pm.persist(pc);
        pm.persist(pc2);
        pm.flush();
        assertTrue(0 != pc.getPK());
        assertTrue(0 != pc2.getPK());
        assertTrue(pc.getPK() != pc2.getPK());
        endTx(pm);
        endEm(pm);
    }

    public void testManualAssign() {
        SequenceAssigned pc = new SequenceAssigned();
        SequenceAssigned3 pc2 = new SequenceAssigned3();
        OpenJPAEntityManager pm =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(pm);
        pc.setPK(-100);
        pc2.setPK(new Long(-100));
        pm.persist(pc);
        pm.persist(pc2);
        pm.flush();
        endTx(pm);
        Object oid = pm.getObjectId(pc);
        Object oid2 = pm.getObjectId(pc2);
        endEm(pm);
        pm = (OpenJPAEntityManager) currentEntityManager();
        pc = (SequenceAssigned) pm.find(SequenceAssigned.class, oid);
        pc2 = (SequenceAssigned3) pm.find(SequenceAssigned3.class, oid2);
        assertEquals(-100, pc.getPK());
        assertEquals(new Long(-100), pc2.getPK());
        endEm(pm);
    }

    public void testInheritance() {
        SequenceAssigned2 pc = new SequenceAssigned2();
        SequenceAssigned2 pc2 = new SequenceAssigned2();
        OpenJPAEntityManager pm =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(pm);
        pm.persist(pc);
        pm.persist(pc2);
        pm.flush();
        assertTrue(0 != pc.getPK());
        assertTrue(0 != pc2.getPK());
        assertTrue(pc.getPK() != pc2.getPK());
        endTx(pm);
        endEm(pm);
    }

    public void testWrapper() {
        SequenceAssigned3 pc = new SequenceAssigned3();
        SequenceAssigned3 pc2 = new SequenceAssigned3();
        OpenJPAEntityManager pm =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(pm);
        pm.persist(pc);
        pm.persist(pc2);
        pm.flush();
        assertNotNull(pc.getPK());
        assertNotNull(pc2.getPK());
        assertTrue(0 != pc.getPK().longValue());
        assertTrue(0 != pc2.getPK().longValue());
        assertNotEquals(pc.getPK(), pc2.getPK());
        endTx(pm);
        endEm(pm);
    }

    public void testGetObjectById() {
        OpenJPAEntityManager pm =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(pm);
        SequenceAssigned pc = new SequenceAssigned();
        pm.persist(pc);
        Object oid = pm.getObjectId(pc);
        assertTrue(0 != ((SequenceAssigned.Id) oid).pk);
        endTx(pm);
        endEm(pm);

        pm = (OpenJPAEntityManager) currentEntityManager();
        pm.find(SequenceAssigned.class, oid);
        endEm(pm);
    }

    public void testReachability() {
        SequenceAssigned pc = new SequenceAssigned();
        SequenceAssigned pc2 = new SequenceAssigned();
        pc.setOther(pc2);
        OpenJPAEntityManager pm =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(pm);
        pm.persist(pc);
        endTx(pm);
        SequenceAssigned.Id id = (SequenceAssigned.Id)
            pm.getObjectId(pc2);
        assertTrue(0 != id.pk);
        assertTrue(0 != pc2.getPK());
        assertEquals(pc2.getPK(), id.pk);
        endEm(pm);
    }
}

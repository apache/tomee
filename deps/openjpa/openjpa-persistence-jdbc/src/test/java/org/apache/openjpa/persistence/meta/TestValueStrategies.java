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

import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManagerFactory;


import org.apache.openjpa.persistence.meta.common.apps.ValueStrategyPC;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.util.OpenJPAException;

/**
 * <p>Test value and update strategies.  Also tests version fields, which
 * are represented in JDO as a value strategy.</p>
 *
 * @author Abe White
 */
public class TestValueStrategies
    extends AbstractTestCase {

    public TestValueStrategies(String test) {
        super(test, "metacactusapp");
    }

    public void setUp() {
        deleteAll(ValueStrategyPC.class);
    }

    public void testIgnoreUpdate() {
        ValueStrategyPC pc = new ValueStrategyPC();
        pc.setName("pc");
        pc.setIgnoreUpdate(10);

        OpenJPAEntityManager pm =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(pm);
        pm.persist(pc);
        assertEquals(10, pc.getIgnoreUpdate());
        endTx(pm);
        Object oid = pm.getObjectId(pc);
        endEm(pm);

        //pm = getPM(false, false);
        pm = (OpenJPAEntityManager) currentEntityManager();
        pc = (ValueStrategyPC) pm.find(ValueStrategyPC.class, oid);
        assertNotNull(pc);
        assertEquals(10, pc.getIgnoreUpdate());
        startTx(pm);
        pc.setIgnoreUpdate(100);
        assertFalse(pm.isDirty(pc));
        pm.transactional(pc, false);
        endTx(pm);
        assertEquals(10, pc.getIgnoreUpdate());
        endEm(pm);

        pm = (OpenJPAEntityManager) currentEntityManager();
        pc = (ValueStrategyPC) pm.find(ValueStrategyPC.class, oid);
        assertNotNull(pc);
        assertEquals(10, pc.getIgnoreUpdate());
        endEm(pm);
    }

    public void testRestrictUpdate() {
        ValueStrategyPC pc = new ValueStrategyPC();
        pc.setName("pc");
        pc.setRestrictUpdate(10);

        OpenJPAEntityManager pm =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(pm);
        pm.persist(pc);
        assertEquals(10, pc.getRestrictUpdate());
        endTx(pm);
        Object oid = pm.getObjectId(pc);
        endEm(pm);

        //pm = getPM(false, false);
        pm = (OpenJPAEntityManager) currentEntityManager();
        pc = (ValueStrategyPC) pm.find(ValueStrategyPC.class, oid);
        assertNotNull(pc);
        assertEquals(10, pc.getRestrictUpdate());
        startTx(pm);
        try {
            pc.setRestrictUpdate(100);
            fail("Allowed update of restricted field.");
        } catch (RuntimeException re) {
        }
        endTx(pm);
        assertEquals(10, pc.getRestrictUpdate());
        endEm(pm);
    }

    public void testUUID() {
        ValueStrategyPC pc = new ValueStrategyPC();
        ValueStrategyPC pc2 = new ValueStrategyPC();
        pc.setName("pc");
        pc2.setName("pc2");
        assertNull(pc.getUUID());
        assertNull(pc2.getUUID());
        assertNull(pc.getUUIDHex());
        assertNull(pc2.getUUIDHex());

        OpenJPAEntityManager pm =
            (OpenJPAEntityManager) currentEntityManager();
        pm.setOptimistic(true);
        startTx(pm);
        pm.persist(pc);
        pm.persist(pc2);
        String str = pc.getUUID();
        String hex = pc.getUUIDHex();
        assertTrue(!pm.isStoreActive());    // no flush needed
        endTx(pm);
        String str2 = pc2.getUUID();
        String hex2 = pc2.getUUIDHex();
        Object oid = pm.getObjectId(pc);
        Object oid2 = pm.getObjectId(pc2);
        endEm(pm);

        assertNotNull(str);
        assertNotNull(str2);
        assertTrue(!str.equals(str2));
        assertNotNull(hex);
        assertNotNull(hex2);
        assertTrue(!hex.equals(hex2));

        pm = (OpenJPAEntityManager) currentEntityManager();
        pc = (ValueStrategyPC) pm.find(ValueStrategyPC.class, oid);
        pc2 = (ValueStrategyPC) pm.find(ValueStrategyPC.class, oid2);
        assertEquals(str, pc.getUUID());
        assertEquals(str2, pc2.getUUID());
        assertEquals(hex, pc.getUUIDHex());
        assertEquals(hex2, pc2.getUUIDHex());
        startTx(pm);
        pc.setUUIDHex("foo");
        pc2.setUUIDHex("bar");
        endTx(pm);
        endEm(pm);

        pm = (OpenJPAEntityManager) currentEntityManager();
        pc = (ValueStrategyPC) pm.find(ValueStrategyPC.class, oid);
        pc2 = (ValueStrategyPC) pm.find(ValueStrategyPC.class, oid2);
        assertEquals("foo", pc.getUUIDHex());
        assertEquals("bar", pc2.getUUIDHex());
        endEm(pm);
    }

    public void testSequence() {
        ValueStrategyPC pc = new ValueStrategyPC();
        ValueStrategyPC pc2 = new ValueStrategyPC();
        pc.setName("pc");
        pc2.setName("pc2");
        assertEquals(0, pc.getSequence());
        assertEquals(0, pc2.getSequence());

        OpenJPAEntityManager pm =
            (OpenJPAEntityManager) currentEntityManager();
        pm.setOptimistic(true);
        startTx(pm);
        pm.persist(pc);
        pm.persist(pc2);
        int seq = pc.getSequence();
        assertTrue(!pm.isStoreActive());    // no flush needed
        endTx(pm);
        int seq2 = pc2.getSequence();
        Object oid = pm.getObjectId(pc);
        Object oid2 = pm.getObjectId(pc2);
        endEm(pm);

        assertTrue(seq > 0);
        assertTrue(seq2 > 0);
        assertTrue(seq != seq2);

        pm = (OpenJPAEntityManager) currentEntityManager();
        pc = (ValueStrategyPC) pm.find(ValueStrategyPC.class, oid);
        pc2 = (ValueStrategyPC) pm.find(ValueStrategyPC.class, oid2);
        assertEquals(seq, pc.getSequence());
        assertEquals(seq2, pc2.getSequence());
        startTx(pm);
        pc.setSequence(99);
        pc2.setSequence(100);
        endTx(pm);
        endEm(pm);

        pm = (OpenJPAEntityManager) currentEntityManager();
        pc = (ValueStrategyPC) pm.find(ValueStrategyPC.class, oid);
        pc2 = (ValueStrategyPC) pm.find(ValueStrategyPC.class, oid2);
        assertEquals(99, pc.getSequence());
        assertEquals(100, pc2.getSequence());
        endEm(pm);
    }

    public void testVersion() {
        versionTest(getEmf());
    }

    public void testVersionDataCache() {

        Map map = new HashMap();
        map.put("openjpa.DataCache", "true");
        map.put("openjpa.RemoteCommitProvider", "sjvm");
        versionTest(getEmf(map));
    }

    private void versionTest(EntityManagerFactory pmf) {
        ValueStrategyPC pc = new ValueStrategyPC();
        pc.setName("pc");

        OpenJPAEntityManager pm = (OpenJPAEntityManager)
            pmf.createEntityManager();
        startTx(pm);
        pm.persist(pc);
        ClassMetaData meta = getConfiguration()
            .getMetaDataRepositoryInstance().
            getMetaData(pc.getClass(), null, false);
        assertNotNull(meta.getVersionField());
        assertEquals("version", meta.getVersionField().getName());
        assertEquals(0, pc.getVersion());
        endTx(pm);
        assertEquals(1, pc.getVersion());
        Object oid = pm.getObjectId(pc);
        endEm(pm);

        // do no-op commit
        pm = (OpenJPAEntityManager) pmf.createEntityManager();
        pc = (ValueStrategyPC) pm.find(ValueStrategyPC.class, oid);
        assertNotNull(pc);
        assertEquals(1, pc.getVersion());
        startTx(pm);
        try {
            pc.setVersion(10);
            fail("Allowed change to version field.");
        } catch (RuntimeException re) {
        }
        endTx(pm);
        assertEquals(1, pc.getVersion());
        endEm(pm);

        // do real commit
        pm = (OpenJPAEntityManager) pmf.createEntityManager();
        pc = (ValueStrategyPC) pm.find(ValueStrategyPC.class, oid);
        assertNotNull(pc);
        assertEquals(1, pc.getVersion());
        startTx(pm);
        pc.setName("changed");
        pm.flush();
        assertEquals(1, pc.getVersion());
        endTx(pm);
        assertEquals("changed", pc.getName());
        assertEquals(2, pc.getVersion());
        endEm(pm);

        // rollback
        pm = (OpenJPAEntityManager) pmf.createEntityManager();
        pc = (ValueStrategyPC) pm.find(ValueStrategyPC.class, oid);
        assertNotNull(pc);
        assertEquals(2, pc.getVersion());
        startTx(pm);
        pc.setName("changed2");
        pm.flush();
        assertEquals(2, pc.getVersion());
        rollbackTx(pm);
        assertEquals(2, pc.getVersion());
        endEm(pm);
    }

    public void testVersionDetach() {
        ValueStrategyPC pc = new ValueStrategyPC();
        ValueStrategyPC pc2 = new ValueStrategyPC();
        pc.setName("pc");
        pc2.setName("pc2");

        OpenJPAEntityManager pm =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(pm);
        pm.persist(pc);
        pm.persist(pc2);
        endTx(pm);
        startTx(pm);
        pc.setName("changed");
        pc2.setName("changed2");
        endTx(pm);
        assertEquals(2, pc.getVersion());
        assertEquals(2, pc2.getVersion());
        ValueStrategyPC detached = (ValueStrategyPC) pm.detachCopy(pc);
        ValueStrategyPC detached2 = (ValueStrategyPC) pm.detachCopy(pc2);
        endEm(pm);

        // clean attach
        pm = (OpenJPAEntityManager) currentEntityManager();
        startTx(pm);
        pc = (ValueStrategyPC) pm.merge(detached);
        assertEquals(2, pc.getVersion());
        endTx(pm);
        assertEquals(2, pc.getVersion());
        endEm(pm);

        // dirty attach
        detached.setName("changed-detached");
        pm = (OpenJPAEntityManager) currentEntityManager();
        startTx(pm);
        pc = (ValueStrategyPC) pm.merge(detached);
        assertEquals(2, pc.getVersion());
        endTx(pm);
        assertEquals(3, pc.getVersion());
        endEm(pm);

        // stale attach
        detached.setName("stale");
        pm = (OpenJPAEntityManager) currentEntityManager();
        startTx(pm);
        try {
            pm.merge(detached);
            endTx(pm);
            fail("Committed stale version.");
        } catch (OpenJPAException je) {
        }
        if (isActiveTx(pm))
            rollbackTx(pm);
        endEm(pm);

        // modify version field in detached; allow either exception or
        // allow the update to be ignored
        detached2.setName("changed2-detached");
        detached2.setVersion(99);
        pm = (OpenJPAEntityManager) currentEntityManager();
        startTx(pm);
        try {
            pc2 = (ValueStrategyPC) pm.merge(detached2);
            assertEquals(2, pc2.getVersion());
            endTx(pm);
            assertEquals(3, pc2.getVersion());
        } catch (OpenJPAException je) {
        }
        if (isActiveTx(pm))
            rollbackTx(pm);
        ;
        endEm(pm);
    }

    public void testVersionRefresh() {
        ValueStrategyPC pc = new ValueStrategyPC();
        pc.setName("pc");

        OpenJPAEntityManager pm =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(pm);
        pm.persist(pc);
        endTx(pm);
        startTx(pm);
        pc.setName("changed");
        endTx(pm);
        assertEquals(2, pc.getVersion());

        // clean refresh
        startTx(pm);
        pm.refresh(pc);
        assertEquals(2, pc.getVersion());

        // concurrent mod
        OpenJPAEntityManager pm2 =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(pm2);
        ValueStrategyPC pc2 = (ValueStrategyPC) pm2.find
            (ValueStrategyPC.class, pm2.getObjectId(pc));
        pc2.setName("changed2");
        endTx(pm2);
        assertEquals(3, pc2.getVersion());
        endEm(pm2);

        // stale refresh
        pm.refresh(pc);
        assertEquals(3, pc.getVersion());

        // dirty refresh
        pc.setName("changed-1");
        pm.refresh(pc);
        assertEquals(3, pc.getVersion());

        pc.setName("changed-2");
        endTx(pm);
        assertEquals(4, pc.getVersion());
        endEm(pm);
    }
}

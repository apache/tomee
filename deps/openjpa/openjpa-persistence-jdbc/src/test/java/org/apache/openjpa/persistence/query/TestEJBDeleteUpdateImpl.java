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
package org.apache.openjpa.persistence.query;

import javax.persistence.Query;


import org.apache.openjpa.persistence.query.common.apps.Entity1;
import org.apache.openjpa.persistence.query.common.apps.RuntimeTest1;
import org.apache.openjpa.persistence.query.common.apps.RuntimeTest2;
import org.apache.openjpa.persistence.OpenJPAEntityManager;

public class TestEJBDeleteUpdateImpl extends BaseQueryTest {

    RuntimeTest1 runt1;
    RuntimeTest2 runt2;
    Entity1 ent;

    public TestEJBDeleteUpdateImpl(String test) {
        super(test);
    }

    public void setUp() {
        deleteAll(RuntimeTest1.class);
        deleteAll(RuntimeTest2.class);
        deleteAll(Entity1.class);

        OpenJPAEntityManager em =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(em);

        runt1 = new RuntimeTest1(1);
        runt1.setStringField("runt1");
        runt1.setSelfOneOne(new RuntimeTest1(2));
        runt2 = new RuntimeTest2(3);
        runt2.setStringField("runt2");

        ent = new Entity1(23, "UPDATEST", 100);

        em.persist(runt1);
        em.persist(runt2);
        em.persist(ent);

        endTx(em);
        endEm(em);
    }

    public void testUpdate1() {
        OpenJPAEntityManager em =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(em);

        RuntimeTest1 ret = em.find(RuntimeTest1.class, em.getObjectId(runt1));

        assertNotNull(ret);
        assertEquals("runt1", ret.getStringField());
        assertNotNull(ret.getSelfOneOne());

        String ejbqlUpdate =
            "UPDATE RuntimeTest1 x SET x.stringField = :strngfld " +
            "WHERE x.stringField = :stdfield";
        int updatedEntities = em.createQuery(ejbqlUpdate)
            .setParameter("strngfld", "runner13")
            .setParameter("stdfield", "runt1")
            .executeUpdate();

        assertEquals(1, updatedEntities);

        endTx(em);

        RuntimeTest1 ret2 = em.find(RuntimeTest1.class, em.getObjectId(runt1));
        em.refresh(ret2);

        assertNotNull(ret2);
        assertEquals("runner13", ret2.getStringField());

        endEm(em);
    }

    public void testUpdate2() {
        OpenJPAEntityManager em =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(em);

        RuntimeTest1 run = em.find(RuntimeTest1.class, em.getObjectId(runt1));
        assertNotNull(run);
        assertEquals("runt1", run.getStringField());
        assertNotNull(run.getSelfOneOne());

        String ejbqlUpdate = "UPDATE RuntimeTest1 x " +
            "SET x.stringField = :strngfld " +
            "WHERE x.stringField = :field";
        int upEntities = em.createQuery(ejbqlUpdate)
            .setParameter("strngfld", "upd")
            .setParameter("field", "runt1")
            .executeUpdate();

        assertEquals(1, upEntities);

        endTx(em);

        em = (OpenJPAEntityManager) currentEntityManager();
        startTx(em);

        RuntimeTest1 inst = em.find(RuntimeTest1.class, em.getObjectId(runt1));
        em.refresh(inst);

        assertNotNull(inst);
        assertEquals("upd", inst.getStringField());

        endTx(em);
        endEm(em);
    }

    public void testDelete1() {
        OpenJPAEntityManager em =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(em);

        RuntimeTest1 run = em.find(RuntimeTest1.class, em.getObjectId(runt1));
        assertNotNull(run);
        assertEquals("runt1", run.getStringField());
        assertNotNull(run.getSelfOneOne());

        String ejbdelUpdate = "DELETE  FROM RuntimeTest1 s " +
            "WHERE s.stringField = :strngfld";
        int delEntity = em.createQuery(ejbdelUpdate)
            .setParameter("strngfld", "runt1")
            .executeUpdate();

        assertEquals(1, delEntity);

        RuntimeTest1 del = em.find(RuntimeTest1.class, em.getObjectId(runt1));
        assertNull(del);

        endTx(em);
        endEm(em);
    }

    public void testDelete2() {
        OpenJPAEntityManager em =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(em);

        RuntimeTest1 run = em.find(RuntimeTest1.class, em.getObjectId(runt1));
        assertNotNull(run);
        assertEquals("runt1", run.getStringField());
        assertNotNull(run.getSelfOneOne());

        String ejbdelUpdate =
            "DELETE  FROM RuntimeTest1 r WHERE r.stringField = ?1";
        Query query = em.createQuery(ejbdelUpdate);

        query.setParameter(1, "runt1");
        int ok = query.executeUpdate();

        assertEquals(1, ok);

        endTx(em);
        endEm(em);
    }

    public void testUpdate3() {
        OpenJPAEntityManager em =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(em);

        Entity1 ent = em.find(Entity1.class, 23);

        assertNotNull(ent);
        assertEquals("UPDATEST", ent.getStringField());

        int upd = em.createQuery(
            "UPDATE Entity1 e SET e.stringField = \'UPDATEFAILED\' WHERE " +
                "e.pk = 23").executeUpdate();

        assertEquals(1, upd);

        endTx(em);
        endEm(em);
    }
}

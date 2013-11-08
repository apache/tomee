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

import java.util.List;
import javax.persistence.EntityManager;



import org.apache.openjpa.persistence.query.common.apps.Entity1;
import org.apache.openjpa.persistence.query.common.apps.Entity2;

public class TestEJBQueryInterface extends BaseQueryTest {

    public TestEJBQueryInterface(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp(Entity1.class, Entity2.class, Order.class, OrderItem.class);

        int instNum = 10;

        EntityManager em = currentEntityManager();
        startTx(em);

        //create and persist multiple entity1 instances
        for (int i = 0; i < instNum; i++) {
            Entity1 ent = new Entity1(i, "string" + i, i + 2);
            Entity2 ent2 = new Entity2(i * 2, "ent2" + i, i);
            ent.setEntity2Field(ent2);
            em.persist(ent);
        }

        endTx(em);
        endEm(em);
    }

    public void testResultList() {
        EntityManager em = currentEntityManager();
        List list = em.createQuery("Select object(o) from Entity1 o")
            .getResultList();

        assertEquals(10, list.size());

        endEm(em);
    }

    public void testGetSingleList() {
        EntityManager em = currentEntityManager();

        Entity1 ret =
            (Entity1) em.createQuery("SELECT o FROM Entity1 o WHERE o.pk = 2")
                .getSingleResult();

        assertNotNull(ret);
        assertEquals("string2", ret.getStringField());
        assertEquals(4, ret.getIntField());

        endEm(em);
    }

    public void testExecuteUpdate() {
        EntityManager em = currentEntityManager();
        startTx(em);
        int ret = em.createQuery("DELETE FROM Entity1 o WHERE o.pk = 2")
            .executeUpdate();

        assertEquals(ret, 1);

        int ret2 = em.createQuery("DELETE FROM Entity1 o WHERE o.pk = 22")
            .executeUpdate();

        assertEquals(ret2, 0);

        endTx(em);
        endEm(em);
    }

    public void testSetMaxResults() {
        EntityManager em = currentEntityManager();

        List l = em.createQuery("Select object(o) from Entity1 o")
            .setMaxResults(5)
            .getResultList();

        assertNotNull(l);
        assertEquals(5, l.size());

        endEm(em);
    }

    public void testSetFirstResults() {
        EntityManager em = currentEntityManager();

        List l = em.createQuery("Select object(o) from Entity1 o order by o.pk")
            .setFirstResult(3)
            .getResultList();

        Entity1 ent = (Entity1) l.get(0);

        assertNotNull(ent);
        assertEquals("string3", ent.getStringField());
        assertEquals(5, ent.getIntField());

        endEm(em);
    }

    // Tests Binding an argument to a named parameter.
    // pk, the named parameter --Not working yet--
    public void testSetParameter1() {
        EntityManager em = currentEntityManager();
        startTx(em);

        List ret =
            em.createQuery("SELECT o FROM Entity1 o WHERE o.stringField = :fld")
                .setParameter("fld", "string1")
                .getResultList();

        assertNotNull(ret);
        assertEquals(1, ret.size());

        ret = em.createNamedQuery("setParam1")
            .setParameter("fld", "string1")
            .getResultList();

        assertNotNull(ret);
        assertEquals(1, ret.size());

        endTx(em);
        endEm(em);
    }

    public void testOrderBy() {
        EntityManager em = currentEntityManager();
        startTx(em);
        String jpql = "SELECT o.oid FROM OrderItem l LEFT JOIN l.order o GROUP BY o.oid ORDER BY o.oid ";
        try {
            List ret = em.createQuery(jpql).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    //rest of the interface is tested by the CTS
}

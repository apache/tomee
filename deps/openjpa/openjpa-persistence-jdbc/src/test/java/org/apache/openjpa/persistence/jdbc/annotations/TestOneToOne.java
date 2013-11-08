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
package org.apache.openjpa.persistence.jdbc.annotations;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Test for 1-1
 *
 * @author Steve Kim
 */
public class TestOneToOne
    extends SingleEMFTestCase {

    public void setUp() {
        setUp(AnnoTest1.class, AnnoTest2.class, Flat1.class, CLEAR_TABLES);
    }

    public void testOneToOne() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        AnnoTest1 pc = new AnnoTest1(5);
        AnnoTest2 pc2 = new AnnoTest2(15, "foo");
        pc.setOneOne(pc2);
        em.persist(pc);
        em.persist(pc2);
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        pc = em.find(AnnoTest1.class, new Long(5));
        pc2 = pc.getOneOne();
        assertNotNull(pc2);
        assertEquals(15, pc2.getPk1());
        assertEquals("foo", pc2.getPk2());
        em.close();
    }

    public void testSelfOneToOne() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        AnnoTest1 pc = new AnnoTest1(5);
        AnnoTest1 pc2 = new AnnoTest1(15);
        pc.setSelfOneOne(pc2);
        em.persist(pc);
        em.persist(pc2);
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        pc = em.find(AnnoTest1.class, new Long(5));
        pc2 = pc.getSelfOneOne();
        assertNotNull(pc2);
        assertEquals(new Long(15), pc2.getPk());
        em.close();
    }

    public void testPKJoinSelfOneToOne() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        AnnoTest1 pc = new AnnoTest1(5);
        AnnoTest1 pc2 = new AnnoTest1(15);
        pc.setSelfOneOne(pc2);
        em.persist(pc);
        em.persist(pc2);
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        pc = em.find(AnnoTest1.class, new Long(5));
        pc2 = pc.getSelfOneOne();
        assertNotNull(pc2);
        assertEquals(new Long(15), pc2.getPk());
        em.close();
    }

    public void testOtherTableOneToOne() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        AnnoTest1 pc = new AnnoTest1(5);
        AnnoTest2 pc2 = new AnnoTest2(15, "foo");
        pc.setOtherTableOneOne(pc2);
        em.persist(pc);
        em.persist(pc2);
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        pc = em.find(AnnoTest1.class, new Long(5));
        pc2 = pc.getOtherTableOneOne();
        assertNotNull(pc2);
        assertEquals(15, pc2.getPk1());
        assertEquals("foo", pc2.getPk2());
        em.close();
    }

    public void testInverseOneToOne() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        AnnoTest1 pc = new AnnoTest1(5);
        AnnoTest2 pc2 = new AnnoTest2(15, "foo");
        pc2.setInverseOneOne(pc);
        em.persist(pc);
        em.persist(pc2);
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        pc = em.find(AnnoTest1.class, new Long(5));
        pc2 = pc.getInverseOwnerOneOne();
        assertNotNull(pc2);
        assertEquals(15, pc2.getPk1());
        assertEquals("foo", pc2.getPk2());
        assertEquals(pc, pc2.getInverseOneOne());
        em.close();
    }
}

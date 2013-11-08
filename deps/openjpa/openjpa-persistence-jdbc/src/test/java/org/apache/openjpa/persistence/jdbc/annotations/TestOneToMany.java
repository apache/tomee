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

import java.util.Collection;

import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Test for 1-m
 *
 * @author Steve Kim
 */
public class TestOneToMany
    extends SingleEMFTestCase {

    public void setUp() {
        setUp(AnnoTest1.class, AnnoTest2.class, Flat1.class, CLEAR_TABLES);
    }

    public void testOneToMany() {
        OpenJPAEntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        AnnoTest1 pc = new AnnoTest1(5);
        pc.getOneMany().add(new AnnoTest2(15, "foo"));
        pc.getOneMany().add(new AnnoTest2(20, "foobar"));
        em.persist(pc);
        em.persistAll(pc.getOneMany());
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        pc = em.find(AnnoTest1.class, new Long(5));
        Collection<AnnoTest2> many = pc.getOneMany();
        assertEquals(2, many.size());
        for (AnnoTest2 pc2 : many) {
            switch ((int) pc2.getPk1()) {
                case 15:
                    assertEquals("foo", pc2.getPk2());
                    break;
                case 20:
                    assertEquals("foobar", pc2.getPk2());
                    break;
                default:
                    fail("unknown element:" + pc2.getPk1());
            }
        }
        em.close();
    }

    public void testInverseOwnerOneToMany() {
        OpenJPAEntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        AnnoTest1 pc = new AnnoTest1(5);
        AnnoTest2 pc2 = new AnnoTest2(15, "foo");
        pc.getInverseOwnerOneMany().add(pc2);
        pc2.setOneManyOwner(pc);
        pc2 = new AnnoTest2(20, "foobar");
        pc.getInverseOwnerOneMany().add(pc2);
        pc2.setOneManyOwner(pc);
        em.persist(pc);
        em.persistAll(pc.getInverseOwnerOneMany());
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        pc = em.find(AnnoTest1.class, new Long(5));
        Collection<AnnoTest2> many = pc.getInverseOwnerOneMany();
        assertEquals(2, many.size());
        for (AnnoTest2 pc3 : many) {
            assertEquals(pc, pc3.getOneManyOwner());
            switch ((int) pc3.getPk1()) {
                case 15:
                    assertEquals("foo", pc3.getPk2());
                    break;
                case 20:
                    assertEquals("foobar", pc3.getPk2());
                    break;
                default:
                    fail("unknown element:" + pc3.getPk1());
            }
        }
        em.close();
    }
}

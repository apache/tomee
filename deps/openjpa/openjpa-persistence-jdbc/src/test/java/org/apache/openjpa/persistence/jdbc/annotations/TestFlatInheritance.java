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
 * Test for InheritanceType.SINGLE_TABLE
 *
 * @author Steve Kim
 */
public class TestFlatInheritance extends SingleEMFTestCase {

    public void setUp() {
        setUp(Flat1.class, Flat2.class, CLEAR_TABLES);
    }

    public void testInheritance() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Flat1 pc = new Flat1(1);
        Flat2 pc2 = new Flat2(2);
        pc.setBasic(10);
        pc2.setBasic(20);
        pc2.setBasic2("DOG");
        em.persist(pc);
        em.persist(pc2);
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        pc = em.find(Flat1.class, 1);
        assertEquals(10, pc.getBasic());
        pc = em.find(Flat1.class, 2);
        assertEquals(Flat2.class, pc.getClass());
        assertEquals(20, pc.getBasic());
        assertEquals("DOG", ((Flat2) pc).getBasic2());
        em.close();
    }
}

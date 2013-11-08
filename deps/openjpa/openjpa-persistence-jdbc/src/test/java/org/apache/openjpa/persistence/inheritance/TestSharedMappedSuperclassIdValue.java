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
package org.apache.openjpa.persistence.inheritance;

import javax.persistence.EntityManager;

import junit.textui.TestRunner;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Test that sibling classes with a shared id value declared in their 
 * mapped superclass are distinguished correctly.
 *
 * @author Abe White
 */
public class TestSharedMappedSuperclassIdValue
    extends SingleEMFTestCase {

    public void setUp() {
        setUp(CLEAR_TABLES, NoGenMappedSuperclassBase.class, 
                NoGenMappedSuperclassL2.class, NoGenEntityL3.class, 
                NoGenEntityL3Sibling.class);

        NoGenEntityL3 ent = new NoGenEntityL3();
        ent.setId(1L);
        ent.setL2Data(99); 
        ent.setL3Data(100);
        NoGenEntityL3Sibling sib = new NoGenEntityL3Sibling();
        sib.setId(1L);
        sib.setL2Data(100); 
        sib.setSiblingL3Data(101);

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(ent);
        em.persist(sib);
        em.getTransaction().commit();
        em.close();
    }

    public void testFind() {
        EntityManager em = emf.createEntityManager();
        NoGenEntityL3 ent = em.find(NoGenEntityL3.class, 1L);
        assertNotNull(ent);
        assertEquals(99, ent.getL2Data());
        assertEquals(100, ent.getL3Data());

        NoGenEntityL3Sibling sib = em.find(NoGenEntityL3Sibling.class, 1L);
        assertNotNull(sib);
        assertEquals(100, sib.getL2Data());
        assertEquals(101, sib.getSiblingL3Data());

        em.close();
    }

    public void testGetReference() {
        EntityManager em = emf.createEntityManager();
        NoGenEntityL3 ent = em.getReference(NoGenEntityL3.class, 1L);
        assertNotNull(ent);

        NoGenEntityL3Sibling sib =
            em.getReference(NoGenEntityL3Sibling.class, 1L);
        assertNotNull(sib);

        assertEquals(99, ent.getL2Data());
        assertEquals(100, ent.getL3Data());
        assertEquals(100, sib.getL2Data());
        assertEquals(101, sib.getSiblingL3Data());

        em.close();
    }

    public static void main(String[] args) {
        TestRunner.run(TestSharedMappedSuperclassIdValue.class);
    }
}


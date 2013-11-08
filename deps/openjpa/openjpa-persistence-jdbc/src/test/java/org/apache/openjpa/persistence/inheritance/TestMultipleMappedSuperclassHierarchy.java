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
 * Perform basic operations on an inheritance hierarchy involving multiple
 * @MappedSuperclasses.
 *
 * @author Abe White
 */
public class TestMultipleMappedSuperclassHierarchy
    extends SingleEMFTestCase {

    public void setUp() {
        setUp(MappedSuperclassBase.class, MappedSuperclassL2.class,
            EntityL3.class);
    }

    public void testPersist() {
        EntityL3 ent = new EntityL3();
        ent.setL2Data(99); 
        ent.setL3Data(100);

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(ent);
        em.getTransaction().commit();
        long id = ent.getId();
        assertTrue(id != 0);
        em.close();

        em = emf.createEntityManager();
        ent = em.find(EntityL3.class, id);
        assertNotNull(ent);
        assertEquals(99, ent.getL2Data());
        assertEquals(100, ent.getL3Data());
        em.close();
    }

    public static void main(String[] args) {
        TestRunner.run(TestMultipleMappedSuperclassHierarchy.class);
    }
}


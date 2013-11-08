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
package org.apache.openjpa.persistence.relations;

import javax.persistence.EntityManager;

import junit.textui.TestRunner;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Test ordering a one-many field on the primary key of the related entity.
 *
 * @author Abe White
 */
public class TestIdOrderedOneMany
    extends SingleEMFTestCase {

    private long id;

    public void setUp() {
        setUp(IdOrderedOneManyParent.class, IdOrderedOneManyChild.class);

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        IdOrderedOneManyParent parent = new IdOrderedOneManyParent();
        parent.setName("parent");
        em.persist(parent);

        for (int i = 0; i < 3; i++) {
            IdOrderedOneManyChild explicit = new IdOrderedOneManyChild();
            explicit.setId(3 - i);
            explicit.setName("explicit" + explicit.getId());
            explicit.setExplicitParent(parent);
            parent.getExplicitChildren().add(explicit);
            em.persist(explicit);

            IdOrderedOneManyChild implicit = new IdOrderedOneManyChild();
            implicit.setId(100 - i);
            implicit.setName("implicit" + implicit.getId());
            implicit.setImplicitParent(parent);
            parent.getImplicitChildren().add(implicit);
            em.persist(implicit);
        }

        em.getTransaction().commit();
        id = parent.getId();
        em.close();
    }

    public void testExplicitOrdering() {
        EntityManager em = emf.createEntityManager();
        IdOrderedOneManyParent parent = em.find(IdOrderedOneManyParent.class, 
            id);
        assertNotNull(parent);
        assertEquals("parent", parent.getName());
        assertEquals(3, parent.getExplicitChildren().size());
        for (int i = 0; i < 3; i++) {
            assertEquals(i + 1, parent.getExplicitChildren().get(i).getId());
            assertEquals("explicit" + (i + 1), parent.getExplicitChildren().
                get(i).getName());
        }
        em.close();
    }

    public void testImplicitOrdering() {
        EntityManager em = emf.createEntityManager();
        IdOrderedOneManyParent parent = em.find(IdOrderedOneManyParent.class, 
            id);
        assertNotNull(parent);
        assertEquals("parent", parent.getName());
        assertEquals(3, parent.getExplicitChildren().size());
        for (int i = 0; i < 3; i++) {
            assertEquals(i + 98, parent.getImplicitChildren().get(i).getId());
            assertEquals("implicit" + (i + 98), parent.getImplicitChildren().
                get(i).getName());
        }
        em.close();
    }

    public static void main(String[] args) {
        TestRunner.run(TestIdOrderedOneMany.class);
    }
}


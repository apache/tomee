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
package org.apache.openjpa.persistence.merge;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.merge.model.Toy;
import org.apache.openjpa.persistence.merge.model.ToyBox;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestCascadeOneToManyMerge extends SingleEMFTestCase {
    public void setUp() {
        setUp(Toy.class, ToyBox.class, CLEAR_TABLES);
    }

    /**
     * Create an instance of Toy and Toybox, establish the bidirectional relationship between the two, and call
     * em.merge() on the instance of Toybox. The merge should cascade across the inverse relationship, adding both
     * entities to the persistence context. The reference to Toy by the owning side of the relationship should be
     * updated to point to the managed instance (Toy' because of the merge op).
     * 
     */
    public void testOneToManyCascadeMergeSingleEntity() {
        EntityManager em = emf.createEntityManager();

        try {
            // Create toy
            Toy toy = new Toy(1);
            toy.setToyName("Toy Train");

            // Create toybox
            ToyBox toybox = new ToyBox(1);
            toybox.setOwnerName("Evan");

            // Establish relationship
            toy.setToybox(toybox);
            toybox.getToyList().add(toy);

            // Perform the merge
            em.getTransaction().begin();
            ToyBox mergedToyBox = em.merge(toybox);
            assertNotNull("Assert em.merge() didn't return null", mergedToyBox);

            // Verify the merge
            ToyBox toyboxFind = em.find(ToyBox.class, 1);
            Toy toyFind = em.find(Toy.class, 1);
            assertNotNull("Assert em.find() for ToyBox(id=1) did not return null.", toyboxFind);
            assertNotNull("Assert em.find() for Toy(id=1) did not return null.", toyFind);
            assertTrue("Assert em.find() returns the ToyBox returned by em.merge()", mergedToyBox == toyboxFind);
            assertTrue("Assert tahat ToyBox(id=1).toyList is size 1", toyboxFind.getToyList().size() == 1);
            assertTrue("Assert that ToyBox(id=1).toyList contains the managed Toy(id=1).", toyboxFind.getToyList()
                .contains(toyFind));
            assertTrue("Assert that Toy(id=1) references the managed ToyBox(id=1).", toyFind.getToybox() == toyboxFind);

            em.getTransaction().commit();

            // Verify successful save to the database
            em.clear();
            assertNotNull("Assert em.find(Toy.class, 1) doesn't return null.", em.find(Toy.class, 1));
            assertNotNull("Assert em.find(ToyBox.class, 1) doesn't return null.", em.find(ToyBox.class, 1));
        } finally {
            if (em != null) {
                if (em.getTransaction().isActive())
                    em.getTransaction().rollback();
                em.close();
            }
        }
    }

    /**
     * Create an instance of Toys (2) and Toybox, establish the bidirectional relationship between the two, and call
     * em.merge() on the instance of Toybox. The merge should cascade across the inverse relationship, adding all
     * entities to the persistence context. The reference to the Toys by the owning side of the relationship should be
     * updated to point to the managed instance (Toy' because of the merge op).
     * 
     */
    public void testOneToManyCascadeMergeDoubleEntity() {
        EntityManager em = emf.createEntityManager();

        try {
            // Create toys
            Toy toy1 = new Toy(1);
            toy1.setToyName("Toy Train");

            Toy toy2 = new Toy(2);
            toy2.setToyName("Toy Plane");

            // Create toybox
            ToyBox toybox = new ToyBox(1);
            toybox.setOwnerName("Evan");

            // Establish relationship
            toy1.setToybox(toybox);
            toy2.setToybox(toybox);
            toybox.getToyList().add(toy1);
            toybox.getToyList().add(toy2);

            // Perform the merge
            em.getTransaction().begin();
            ToyBox mergedToyBox = em.merge(toybox);
            assertNotNull("Assert em.merge() didn't return null", mergedToyBox);
            em.getTransaction().commit();

            // Verify the merge
            ToyBox toyboxFind = em.find(ToyBox.class, 1);
            Toy toy1Find = em.find(Toy.class, 1);
            Toy toy2Find = em.find(Toy.class, 2);
            assertNotNull("Assert em.find() for ToyBox(id=1) did not return null.", toyboxFind);
            assertNotNull("Assert em.find() for Toy(id=1) did not return null.", toy1Find);
            assertNotNull("Assert em.find() for Toy(id=2) did not return null.", toy2Find);
            assertTrue("Assert em.find() returns the ToyBox returned by em.merge()", mergedToyBox == toyboxFind);
            assertTrue("Assert tahat ToyBox(id=1).toyList is size 2", toyboxFind.getToyList().size() == 2);
            assertTrue("Assert that ToyBox(id=1).toyList contains the managed Toy(id=1).", toyboxFind.getToyList()
                .contains(toy1Find));
            assertTrue("Assert that ToyBox(id=1).toyList contains the managed Toy(id=2).", toyboxFind.getToyList()
                .contains(toy2Find));
            assertTrue("Assert that Toy(id=1) references the managed ToyBox(id=1).",
                toy1Find.getToybox() == toyboxFind);
            assertTrue("Assert that Toy(id=2) references the managed ToyBox(id=1).", 
                toy2Find.getToybox() == toyboxFind);

            em.clear();
            assertNotNull("Assert em.find(Toy.class, 1) doesn't return null.", em.find(Toy.class, 1));
            assertNotNull("Assert em.find(Toy.class, 2) doesn't return null.", em.find(Toy.class, 2));
            assertNotNull("Assert em.find(ToyBox.class, 1) doesn't return null.", em.find(ToyBox.class, 1));
        } finally {
            if (em != null) {
                if (em.getTransaction().isActive())
                    em.getTransaction().rollback();
                em.close();
            }
        }
    }
}

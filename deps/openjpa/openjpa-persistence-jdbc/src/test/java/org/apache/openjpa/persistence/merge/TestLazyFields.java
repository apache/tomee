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

import java.util.ArrayList;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.merge.model.Inner;
import org.apache.openjpa.persistence.merge.model.Outer;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestLazyFields extends SingleEMFTestCase {
    public void setUp() {
        setUp(CLEAR_TABLES, Outer.class, Inner.class);
    }

    public void testMergeOfLazyFields() {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Outer o1 = new Outer();
            Inner i1 = new Inner();
            o1.setInners(new ArrayList<Inner>());
            o1.getInners().add(i1);

            em.persist(o1);

            em.getTransaction().commit();
            em.clear(); // the objects will now get detached.

            long id = o1.getId();

            em.getTransaction().begin();
            Outer o2 = em.find(Outer.class, id);

            // Since o2 is in the context, it should be ignored... but the merge will needs to be cascaded
            // to loaded fields.
            Outer mergedO2 = em.merge(o2);
            
            // Make sure that the merge operation didn't return a different outer.
            assertEquals(mergedO2, o2);

            em.getTransaction().commit();
            em.clear();

            // Fetch again
            em.getTransaction().begin();
            Outer o3 = em.find(Outer.class, id);

            // We're checking that the merge didn't cascade to the unloaded field and wipe out all Inners
            assertTrue(o3.getInners().size() > 0);
            
            em.getTransaction().commit();
            em.clear();

        } finally {
            if (em != null) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }
}

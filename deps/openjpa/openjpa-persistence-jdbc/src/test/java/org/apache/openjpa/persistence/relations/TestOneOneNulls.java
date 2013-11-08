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

import java.util.List;
import javax.persistence.EntityManager;

import junit.textui.TestRunner;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAQuery;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Test that both sides of a mapped-by relation are null when not set
 * or when explicitly set to null.
 *
 */
public class TestOneOneNulls extends SingleEMFTestCase {

    private long pid1;
    private long pid2;
    private long cid1;
    private long cid2;

    public void setUp() {
        setUp(BidiParent.class, BidiChild.class, CLEAR_TABLES);
        
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        for (int i = 1; i <= 2; i++) {
            BidiParent parent = new BidiParent();
            parent.setName("parent" + i);
            em.persist(parent);

            BidiChild child = new BidiChild();
            child.setName("child" + i);
            // for first set, don't set the relationship
            // but explicitly set to null for the second set
            if (i == 2) {
                child.setOneToOneParent(null);
                parent.setOneToOneChild(null);
            }
            em.persist(child);

            if (i == 1) {
                pid1 = parent.getId();
                cid1 = child.getId();
            } else {
                pid2 = parent.getId();
                cid2 = child.getId();
            }
        }

        em.getTransaction().commit();
        em.close();
    }

    public void testFindOneToOneNullDefaults() {
        BidiParent parent;
        BidiChild child;

        OpenJPAEntityManager em = emf.createEntityManager();
        em.getFetchPlan().addField(BidiParent.class, "oneToOneChild");
        em.getFetchPlan().addField(BidiChild.class, "oneToOneParent");

        // test first set w/o explicit nulls
        parent = em.find(BidiParent.class, pid1);
        assertEquals("parent1", parent.getName());
        assertNull(parent.getOneToOneChild());
        child = em.find(BidiChild.class, cid1);
        assertEquals("child1", child.getName());
        assertNull(child.getOneToOneParent());

        em.close();
    }

    public void testFindOneToOneNullExplicit() {
        BidiParent parent;
        BidiChild child;

        OpenJPAEntityManager em = emf.createEntityManager();
        em.getFetchPlan().addField(BidiParent.class, "oneToOneChild");
        em.getFetchPlan().addField(BidiChild.class, "oneToOneParent");

        // test second set with explicit nulls
        parent = em.find(BidiParent.class, pid2);
        assertEquals("parent2", parent.getName());
        assertNull(parent.getOneToOneChild());
        child = em.find(BidiChild.class, cid2);
        assertEquals("child2", child.getName());
        assertNull(child.getOneToOneParent());

        em.close();
    }

    public void testQueryOneToOneNullChild() {
        OpenJPAEntityManager em = emf.createEntityManager();
        OpenJPAQuery q = em.createQuery("SELECT o FROM BidiParent o "
            + "ORDER BY o.name ASC");
        q.getFetchPlan().addField(BidiParent.class, "oneToOneChild");
        q.getFetchPlan().addField(BidiChild.class, "oneToOneParent");
        List<BidiParent> res = (List<BidiParent>) q.getResultList(); 
        assertEquals(2, res.size());
        for (int i = 0; i < res.size(); i++) {
            assertEquals("parent" + (i + 1), res.get(i).getName());
            assertNull(res.get(i).getOneToOneChild());
        }
        em.close();
    }

    public void testQueryOneToOneNullParent() {
        OpenJPAEntityManager em = emf.createEntityManager();
        OpenJPAQuery q = em.createQuery("SELECT o FROM BidiChild o "
            + "ORDER BY o.name ASC");
        q.getFetchPlan().addField(BidiParent.class, "oneToOneChild");
        q.getFetchPlan().addField(BidiChild.class, "oneToOneParent");
        List<BidiChild> res = (List<BidiChild>) q.getResultList(); 
        assertEquals(2, res.size());
        for (int i = 0; i < res.size(); i++) {
            assertEquals("child" + (i + 1), res.get(i).getName());
            assertNull(res.get(i).getOneToOneParent());
        }
        em.close();
    }

    public static void main(String[] args) {
        TestRunner.run(TestOneOneNulls.class);
    }
}


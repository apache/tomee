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
package org.apache.openjpa.persistence.detachment;

import org.apache.openjpa.persistence.test.SingleEMFTestCase;
import org.apache.openjpa.persistence.detachment.model.*;

import javax.persistence.EntityManager;

import junit.textui.TestRunner;

/**
 * Test merge case for 3 level one to many relation entities.
 * SimpleA -> *SimpleB -> *SimpleC. SimpleC has no CascadeType.MERGE annotation.
 *
 */
public class TestNoCascadeOneToManyMerge extends SingleEMFTestCase {
    private int a_id;

    public void setUp() {
        setUp(SimpleA.class, SimpleRef.class, SimpleB.class, SimpleC.class,
                CLEAR_TABLES);
        createEntities();
    }

    private void createEntities() {
        SimpleA a = new SimpleA();
        a.setName("a1");

        SimpleB b = new SimpleB();
        b.setName("b1");
        a.addB(b);

        SimpleC c1 = new SimpleC();
        c1.setName("c1");
        b.addC(c1);

        SimpleC c2 = new SimpleC();
        c2.setName("c2");
        b.addC(c2);

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(a);
        em.getTransaction().commit();
        em.close();
        a_id = a.getId();
    }

    public void testMergeAttached () {
        EntityManager em = emf.createEntityManager();
        SimpleA a = em.find(SimpleA.class, a_id);
        assertNotNull(a);

        SimpleB b = new SimpleB();
        b.setName("b2");
        a.addB(b);

        SimpleC c = new SimpleC();
        c.setName("c3");
        b.addC(c);

        c = new SimpleC();
        c.setName("c4");
        b.addC(c);

        em.getTransaction().begin();
        a = em.merge (a);
        em.getTransaction().commit ();
        em.close();

        assertEquals(2, a.getBs().size());
    }

    /**
     * This is the case for openjpa-231.
     * When "B" and "C" are both newly added to a detached "A" and then merge
     * "A", it couldn't find "B" because previous code assume B was detached.
     */
    public void testMergeDetached () {
        EntityManager em = emf.createEntityManager();
        SimpleA a = em.find(SimpleA.class, a_id);
        assertNotNull(a);
        assertEquals(1, a.getBs().size());
        em.close(); //detach a

        SimpleB b = new SimpleB();
        b.setName("b2");
        a.addB(b);

        SimpleC c = new SimpleC();
        c.setName("c3");
        b.addC(c);

        c = new SimpleC();
        c.setName("c4");
        b.addC(c);

        em = emf.createEntityManager();
        em.getTransaction().begin();
        a = em.merge(a);
        em.getTransaction().commit();
        em.close();

        assertEquals(2, a.getBs().size());
    }

    public static void main(String[] args) {
        TestRunner.run(TestNoCascadeOneToManyMerge.class);
    }
}

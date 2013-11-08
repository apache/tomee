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
 * Tests merging an entity having a unidirectional one-to-one relation and
 * cascade=none.
 *
 * @author Gokhan Ergul
 */
public class TestNoCascadeOneToOneMerge
    extends SingleEMFTestCase {

    private int a_id;
    private int b1_id;
    private int b2_id;

    public void setUp() {
        setUp(SimpleA.class, SimpleRef.class, SimpleB.class, SimpleC.class, 
            "openjpa.Compatibility", "FlushBeforeDetach=true," +
            "CopyOnDetach=true",
            CLEAR_TABLES);
        createEntities();
    }

    private void createEntities() {
        SimpleRef b1 = new SimpleRef();
        b1.setName("b1-name");

        SimpleRef b2 = new SimpleRef();
        b2.setName("b2-name");

        SimpleA a = new SimpleA();
        a.setName("a-name");
        a.setRef(b1);

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(b1);
        em.persist(b2);
        em.persist(a);
        em.getTransaction().commit();
        em.close();
        a_id = a.getId();
        b1_id = b1.getId();
        b2_id = b2.getId();
    }

    public void testMergeAllAttached () {
        EntityManager em = emf.createEntityManager();
        SimpleA a = em.find(SimpleA.class, a_id);
        SimpleRef b2 = em.find(SimpleRef.class, b2_id);
        assertNotNull(a);
        assertNotNull(b2);

        // change a.b from b1 to b2 and merge
        a.setRef(b2);

        em.getTransaction().begin();
        em.merge(a);
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        a = em.find(SimpleA.class, a_id);
        em.close();
        assertNotNull(a);
        assertEquals(b2_id, a.getRef().getId());
    }

    public void testMergeRefAttached () {
        EntityManager em = emf.createEntityManager();
        SimpleA a = em.find(SimpleA.class, a_id);
        assertNotNull(a);
        em.close(); // detach a only

        em = emf.createEntityManager();
        SimpleRef b2 = em.find(SimpleRef.class, b2_id);
        assertNotNull(b2);
        // do not detach b2

        // change a.b from b1 to b2 and merge
        a.setRef(b2);

        em.getTransaction().begin();
        em.merge(a);
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        a = em.find(SimpleA.class, a_id);
        em.close();
        assertNotNull(a);
        assertEquals(b2_id, a.getRef().getId());
    }

    public void testMergeDetached () {
        EntityManager em = emf.createEntityManager();
        SimpleA a = em.find(SimpleA.class, a_id);
        SimpleRef b1 = em.find(SimpleRef.class, b1_id);
        SimpleRef b2 = em.find(SimpleRef.class, b2_id);
        assertNotNull(a);
        assertNotNull(b1);
        assertNotNull(b2);
        em.close(); // detach all

        // change a.b from b1 to b2 and merge
        a.setRef(b2);

        em = emf.createEntityManager();
        em.getTransaction().begin();
        em.merge(a);
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        a = em.find(SimpleA.class, a_id);
        em.close();
        assertNotNull(a);
        assertEquals(b2_id, a.getRef().getId());
    }

    public static void main(String[] args) {
        TestRunner.run(TestNoCascadeOneToOneMerge.class);
    }
}

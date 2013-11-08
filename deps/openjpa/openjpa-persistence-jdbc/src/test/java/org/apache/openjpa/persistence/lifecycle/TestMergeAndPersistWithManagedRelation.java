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
package org.apache.openjpa.persistence.lifecycle;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.relations.BidiChild;
import org.apache.openjpa.persistence.relations.BidiParent;
import org.apache.openjpa.persistence.test.SingleEMTestCase;

public class TestMergeAndPersistWithManagedRelation
    extends SingleEMTestCase {

    private long childId;

    public void setUp() {
        setUp(BidiParent.class, BidiChild.class, CLEAR_TABLES);

        BidiChild child = new BidiChild();
        child.setName("child");

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(child);
        em.getTransaction().commit();
        childId = child.getId();
        em.close();
    }

    public void testPersistWithManagedRelation() {
        BidiParent parent = new BidiParent();
        BidiChild child = em.find(BidiChild.class, childId);
        parent.setOneToOneChild(child);
        em.getTransaction().begin();
        em.persist(parent);
        em.getTransaction().commit();
        long id = parent.getId();
        em.close();

        em = emf.createEntityManager();
        parent = em.find(BidiParent.class, id);
        assertNotNull(parent);
    }

    public void testMergeWithManagedRelation() {
        BidiParent parent = new BidiParent();
        BidiChild child = em.find(BidiChild.class, childId);
        parent.setOneToOneChild(child);
        em.getTransaction().begin();
        parent = em.merge(parent);
        em.getTransaction().commit();
        long id = parent.getId();
        em.close();

        em = emf.createEntityManager();
        parent = em.find(BidiParent.class, id);
        assertNotNull(parent);
    }
}

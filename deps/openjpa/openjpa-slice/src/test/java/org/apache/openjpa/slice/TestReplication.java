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
package org.apache.openjpa.slice;

import java.util.Set;

import javax.persistence.EntityManager;

/**
 * Tests that parent-child relation both replicated are stored in all replicated
 * slices.
 * 
 * <A HREF="https://issues.apache.org/jira/browse/OPENJPA-981">OPENJPA-981</A>
 * 
 * @author Pinaki Poddar
 *
 */
public class TestReplication extends SingleEMFTestCase {
    private static int CHILD_COUNT = 3;

    public void setUp() {
        super.setUp(CLEAR_TABLES);
        createData();
    }

    protected String getPersistenceUnitName() {
        return "replication";
    }
    
    void createData() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        ReplicatedParent parent = new ReplicatedParent();
        parent.setName("parent");
        for (int i = 0; i < CHILD_COUNT; i++) {
            ReplicatedChild child = new ReplicatedChild();
            child.setName("child-" + i);
            parent.addChild(child);
        }
        em.persist(parent);
        em.getTransaction().commit();
        em.clear();
    }
    
    public void testPersistInReplicatedSlices() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        String jpql = "select p from ReplicatedParent p where p.name=:name";
        ReplicatedParent pOne = (ReplicatedParent) em.createQuery(jpql)
            .setParameter("name", "parent")
            .setHint(SlicePersistence.HINT_TARGET, "One")
            .getSingleResult();
        assertNotNull(pOne);

        assertEquals("[One, Two]", SlicePersistence.getSlice(pOne));

        ReplicatedParent pTwo = (ReplicatedParent) em.createQuery(jpql)
            .setParameter("name", "parent")
            .setHint(SlicePersistence.HINT_TARGET, "Two")
            .getSingleResult();
        assertNotNull(pTwo);
        assertEquals("[One, Two]", SlicePersistence.getSlice(pTwo));

        jpql = "select p from ReplicatedChild p where p.name=:name";
        ReplicatedChild cOne = (ReplicatedChild) em.createQuery(jpql)
            .setParameter("name", "child-0")
            .setHint(SlicePersistence.HINT_TARGET, "One")
            .getSingleResult();
        assertNotNull(cOne);
        ReplicatedChild cTwo = (ReplicatedChild) em.createQuery(jpql)
            .setParameter("name", "child-0")
            .setHint(SlicePersistence.HINT_TARGET, "Two")
            .getSingleResult();
        assertNotNull(cTwo);
    }
    
    public void testQuery() {
        EntityManager em = emf.createEntityManager();
        String jpql = "select p from ReplicatedParent p where p.name=:name";
        ReplicatedParent parent = (ReplicatedParent) em.createQuery(jpql)
            .setParameter("name", "parent")
            .setHint(SlicePersistence.HINT_TARGET, "One")
            .getSingleResult();
        assertNotNull(parent);
        Set<ReplicatedChild> children = parent.getChildren();
        assertNotNull(children);
        assertEquals(CHILD_COUNT, children.size());
    }
    
    public void testAggregateQuery() {
        EntityManager em = emf.createEntityManager();
        String jpql = "select count(p) from ReplicatedParent p";
        long pCount = (Long) em.createQuery(jpql).getSingleResult();
        assertEquals(1, pCount);
        
        jpql = "select count(p) from ReplicatedChild p";
        long cCount = (Long) em.createQuery(jpql).getSingleResult();
        assertEquals(CHILD_COUNT, cCount);
    }
    
}

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
package org.apache.openjpa.persistence.detach;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.openjpa.conf.Compatibility;
import org.apache.openjpa.persistence.DetachStateType;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Test the various options for openjpa.DetachState with the new DETACH
 * cascade option.
 * 
 * @author dianner
 * @since 2.0.0
 *
 */
public class TestDetachStateCascade extends SingleEMFTestCase {
    OpenJPAEntityManager em;
    int id = 0;
    Compatibility compat;
    
    EntityA entityA;
    EntityB entityB;
    EntityC entityC;
    EntityD entityD;
    EntityE entityE;
    
    List<EntityC> list = new ArrayList<EntityC>();
    
    Collection<Object> allEntities = new HashSet<Object>();
    
    public void setUp() throws Exception {
        setUp(org.apache.openjpa.persistence.detach.EntityA.class,
            org.apache.openjpa.persistence.detach.EntityB.class,
            org.apache.openjpa.persistence.detach.EntityC.class,
            org.apache.openjpa.persistence.detach.EntityD.class,
            org.apache.openjpa.persistence.detach.EntityE.class);
        assertNotNull(emf);
        em = emf.createEntityManager();
        assertNotNull(em);
        compat = emf.getConfiguration().getCompatibilityInstance();
        assertNotNull(compat);
        compat.setFlushBeforeDetach(true);
        compat.setCopyOnDetach(false);
        compat.setCascadeWithDetach(false);
        id++;
        create(id);
        
        em.getTransaction().begin();
        em.persist(entityA);
        em.getTransaction().commit();
    }

    private void create(int id) {
        entityA = new EntityA(id, "entityA", "top level class");
        entityB = new EntityB(id, "entityB");
        entityC = new EntityC(id, "entityC");
        entityD = new EntityD(id, "entityD");
        entityE = new EntityE(id, "entityE");
        
        entityA.setEntityB(entityB);
        entityA.setEntityC(entityC);
        entityA.setEntityD(entityD);
        entityA.setEntityE(entityE);
    }
    
    
    
    /**
     * The default DetachState of LOADED is tested. In this scenario:
     * A is the main entity to be detached
     * B is loaded and DETACH cascade is specified
     * C is loaded but DETACH cascade is NOT specified
     * D is not loaded and DETACH cascade is specified
     * E is not loaded but DETACH cascade is NOT specified
     */
    public void testLoaded() {
        em.getTransaction().begin();
        // Clear the persistence context so we start fresh
        em.persist(entityA);
        em.clear();
        EntityA eA = em.find(EntityA.class, id); // Loads B and C by default
        assertTrue(em.contains(eA));
        EntityD eD = em.find(EntityD.class, id); // Load independently
        assertTrue(em.contains(eD));
        EntityE eE = em.find(EntityE.class, id); // Load independently
        assertTrue(em.contains(eE));
        em.detach(eA);
        assertEquals(id, eA.getId());
        assertEquals("entityA", eA.getName());
        assertNull(eA.getDescription()); // should not be loaded
        assertNotNull(eA.getEntityB());
        assertNotNull(eA.getEntityC());
        assertNull(eA.getEntityD());
        assertNull(eA.getEntityE());
        assertTrue(em.isDetached(eA));
        assertTrue(em.isDetached(eA.getEntityB()));
        assertFalse(em.isDetached(eA.getEntityC()));
        assertFalse(em.isDetached(eD));
        assertFalse(em.isDetached(eE));
        em.getTransaction().commit();
    }
    
    /**
     * The DetachState of ALL is tested. In this scenario:
     * A is the main entity to be detached.
     * B, C, D, and E are all loaded
     * DETACH cascade is specified for B and D
     * DETACH cascade is NOT specified for C and E
     */
    public void testDetachStateAll() {
        em.setDetachState(DetachStateType.ALL);
        em.getTransaction().begin();
        // Clear the persistence context so we start fresh
        em.persist(entityA);
        em.clear();
        EntityA eA = em.find(EntityA.class, id);
        assertTrue(em.contains(eA));
        em.detach(eA);
        assertEquals(id, eA.getId());
        assertEquals("entityA", eA.getName());
        assertNotNull(eA.getDescription());
        assertNotNull(eA.getEntityB());
        assertNotNull(eA.getEntityC());
        assertNotNull(eA.getEntityD());
        assertNotNull(eA.getEntityE());
        assertTrue(em.isDetached(eA));
        assertTrue(em.isDetached(eA.getEntityB()));
        assertFalse(em.isDetached(eA.getEntityC()));
        assertTrue(em.isDetached(eA.getEntityD()));
        assertFalse(em.isDetached(eA.getEntityE()));
        em.getTransaction().commit();
    }
    
    /**
     * The default DetachState of LOADED is tested. In this scenario:
     * A is the main entity to be detached
     * B is loaded by default and DETACH cascade is specified
     * C is loaded by default but DETACH cascade is NOT specified
     * D is loaded by fetch plan and DETACH cascade is specified
     * E is not loaded but DETACH cascade is NOT specified
     */
    public void testDetachStateFetchGroup() {
        em.setDetachState(DetachStateType.FETCH_GROUPS);
        em.getFetchPlan().addFetchGroup("loadD");
        em.getTransaction().begin();
        // Clear the persistence context so we start fresh
        em.persist(entityA);
        em.clear();
        EntityA eA = em.find(EntityA.class, id);
        assertTrue(em.contains(eA));
        EntityE eE = em.find(EntityE.class, id); // Load independently
        assertTrue(em.contains(eE));
        em.detach(eA);
        assertEquals(id, eA.getId());
        assertEquals("entityA", eA.getName());
        assertNull(eA.getDescription());
        assertNotNull(eA.getEntityB());
        assertNotNull(eA.getEntityC());
        assertNotNull(eA.getEntityD());
        assertNull(eA.getEntityE());
        assertTrue(em.isDetached(eA));
        assertTrue(em.isDetached(eA.getEntityB()));
        assertFalse(em.isDetached(eA.getEntityC()));
        assertTrue(em.isDetached(eA.getEntityD()));
        assertFalse(em.isDetached(eE));
        em.getTransaction().commit();
    }
    
}

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
package org.apache.openjpa.persistence.cache.jpa;

import java.util.Random;

import javax.persistence.Cache;
import javax.persistence.CacheStoreMode;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.LockModeType;


import org.apache.openjpa.persistence.cache.jpa.model.CacheableEntity;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Verifies L2 operations using multiple entity manager factories. The EMF which
 * does updates does not have the data cache enabled thus, it does not send
 * sjvm updates upon commit.
 */
public class TestMultiEMFCacheModes extends SingleEMFTestCase {

    public void setUp() {
        setUp( CLEAR_TABLES, CacheableEntity.class,
               "openjpa.ConnectionFactoryProperties", "PrintParameters=true",
               "openjpa.DataCache", "true");
    }

    /**
     * Verifies that the data cache us updated via a em.refresh operation when 
     * javax.persistence.cache.storeMode = CacheStoreMode.REFRESH and the
     * entity is updated in the database.
     */
    public void testCacheRefreshModeRefresh() {

        EntityManager em = emf.createEntityManager();
        
        // Create a new cacheable entity
        CacheableEntity ce = createEntity(em);
        int ceid = ce.getId();

        // Clear the L1
        em.clear();
        
        // Clear the L2 cache
        Cache cache = emf.getCache();
        cache.evictAll();
        assertFalse(cache.contains(CacheableEntity.class, ceid));

        // Find the entity, reloading it into the L2 
        em.getTransaction().begin();
        ce = em.find(CacheableEntity.class, ceid);
        assertTrue(em.contains(ce));
        assertTrue(cache.contains(CacheableEntity.class, ceid));
        assertTrue(em.getLockMode(ce) == LockModeType.NONE);
        assertEquals(ce.getName(), "Cached Entity");
        assertEquals(ce.getVersion(), 1);
        em.getTransaction().commit();

        // Create a new EMF -WITHOUT- the L2 enabled.  If the L2 was enabled, the
        // sjvm remote commit provider would evict the entity upon update, throwing
        // off the intent of this variation.
        EntityManagerFactory emf2 = this.createEMF(CacheableEntity.class,
            "openjpa.LockManager", "mixed",
            "openjpa.ConnectionFactoryProperties", "PrintParameters=true");
        EntityManager em2 = emf2.createEntityManager();
        
        // Find + lock, then update the entity and commit
        em2.getTransaction().begin();
        CacheableEntity ce2 = em2.find(CacheableEntity.class, ceid, LockModeType.PESSIMISTIC_FORCE_INCREMENT);
        ce2.setName("Updated Cached Entity");
        em2.getTransaction().commit();
        em2.close();
        emf2.close();

        // Refresh the entity - this will load the entity into the L1 and with storeMode=REFRESH, 
        // should also refresh it in the L2
        java.util.Map<String, Object> cacheStoreModeMap = new java.util.HashMap<String, Object>();
        cacheStoreModeMap.put("javax.persistence.cache.storeMode", CacheStoreMode.REFRESH);
        em.refresh(ce, cacheStoreModeMap);

        // Verify the entity was updated
        verifyUpdatedEntity(ce, ceid);

        // Verify loading from the L1
        ce = em.find(CacheableEntity.class, ceid);
        
        // Verify the entity was updated
        verifyUpdatedEntity(ce, ceid);

        // Clear the L1
        em.clear();

        // Assert the L2 contains the entity
        assertTrue(cache.contains(CacheableEntity.class, ceid));

        // Reload the entity from the L2
        ce = em.find(CacheableEntity.class, ceid);
        
        // Verify the entity in the L2 was updated
        verifyUpdatedEntity(ce, ceid);

        em.close();
    }

    /**
     * Verifies that the data cache us updated via a em.refresh operation when 
     * javax.persistence.cache.storeMode = CacheStoreMode.REFRESH and the 
     * record is removed from the database.
     */
    public void testCacheRefreshModeRefreshDelete() {

        EntityManager em = emf.createEntityManager();

        // Create a new cachable entity
        CacheableEntity ce = createEntity(em);
        int ceid = ce.getId();

        // Clear the L2 cache
        Cache cache = emf.getCache();
        cache.evictAll();
        assertFalse(cache.contains(CacheableEntity.class, ceid));

        // Find the entity, reloading it into the L2 
        em.getTransaction().begin();
        ce = em.find(CacheableEntity.class, ceid);
        assertTrue(em.contains(ce));
        assertTrue(cache.contains(CacheableEntity.class, ceid));
        assertTrue(em.getLockMode(ce) == LockModeType.NONE);
        assertEquals(ce.getName(), "Cached Entity");
        assertEquals(ce.getVersion(), 1);
        em.getTransaction().commit();

        // Create a new EMF -WITHOUT- the L2 enabled.  If the L2 was enabled, the
        // sjvm remote commit provider would evict the entity upon delete, throwing
        // off the intent of this variation.
        EntityManagerFactory emf2 = this.createEMF(CacheableEntity.class,
            "openjpa.LockManager", "mixed",
            "openjpa.ConnectionFactoryProperties", "PrintParameters=true");
        EntityManager em2 = emf2.createEntityManager();

        // Find and delete the entity in a separate context with no L2 configured
        em2.getTransaction().begin();
        CacheableEntity ce2 = em2.find(CacheableEntity.class, ceid);
        assertNotNull(ce2);
        em2.remove(ce2);
        em2.getTransaction().commit();
        em2.close();
        emf2.close();

        // Refresh the entity with storeMode=REFRESH.  The entity has been deleted so it will be
        // purged from the L2 cache when the DB load fails.
        java.util.Map<String, Object> cacheStoreModeMap = new java.util.HashMap<String, Object>();
        cacheStoreModeMap.put("javax.persistence.cache.storeMode", CacheStoreMode.REFRESH);
        try {
            em.refresh(ce, cacheStoreModeMap);
            fail("Refresh operation should have thrown an exception");
        } catch (EntityNotFoundException e) {
            // expected exception
        }

        // Try loading from the L1 - OpenJPA will detect the entity was
        // removed in another transaction.
        try {
            ce = em.find(CacheableEntity.class, ceid);
            fail("OpenJPA should have detected the removed entity");
        } catch (EntityNotFoundException e) {
            // expected exception
        }

        // Clear the L1
        em.clear();

        // Assert the L2 no longer contains the entity
        assertFalse(cache.contains(CacheableEntity.class, ceid));

        // Attempt to reload entity from the L2 or database
        ce = em.find(CacheableEntity.class, ceid);

        // Verify the entity was removed from L2 and DB
        assertNull(ce);

        em.close();
	}

	private CacheableEntity createEntity(EntityManager em) {
        CacheableEntity ce = new CacheableEntity();
        int ceid = new Random().nextInt();
        ce.setId(ceid);
        ce.setName("Cached Entity");

        // Persist the new cachable entity
        em.getTransaction().begin();
        em.persist(ce);
        em.getTransaction().commit();
        em.clear();
        return ce;
    }

    private void verifyUpdatedEntity(CacheableEntity ce, int id) {
        assertEquals(id, ce.getId());
        assertEquals("Updated Cached Entity", ce.getName());
        assertEquals(2, ce.getVersion());
    }
}

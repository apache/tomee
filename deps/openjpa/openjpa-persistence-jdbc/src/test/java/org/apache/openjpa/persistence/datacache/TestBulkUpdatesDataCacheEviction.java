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
package org.apache.openjpa.persistence.datacache;

import javax.persistence.Cache;
import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.OpenJPAEntityManagerSPI;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestBulkUpdatesDataCacheEviction extends SingleEMFTestCase {
    Object[] props = new Object[] { CLEAR_TABLES, CachedEntityStatistics.class, "openjpa.DataCache", "true" };
    Object[] noEvictProps = new Object[] { CLEAR_TABLES, CachedEntityStatistics.class
        , "openjpa.DataCache", "true(EvictOnBulkUpdate=false)" };

    public void setUp() throws Exception {
        super.setUp(props);
    }

    /**
     * This test ensures that after executing an update against the db, the updated type is purged from
     * the DataCache.
     */
    public void testUpdate() throws Exception {
        EntityManager em = emf.createEntityManager();
        Cache cache = emf.getCache();
        try {
            CachedEntityStatistics e = createEntity(em);
            assertTrue(cache.contains(CachedEntityStatistics.class, e.getId()));
            em.clear();

            String update = "UPDATE CachedEntityStatistics s SET s.firstName = :name WHERE s.id = :id";
            String name = "name_" + System.currentTimeMillis();
            // execute update, this should result in a cache eviction
            em.getTransaction().begin();
            assertEquals(1, em.createQuery(update).setParameter("name", name).setParameter("id", e.getId())
                .executeUpdate());
            em.getTransaction().commit();
            assertFalse(cache.contains(CachedEntityStatistics.class, e.getId()));

            CachedEntityStatistics postUpdate = em.find(CachedEntityStatistics.class, e.getId());
            assertEquals(name, postUpdate.getFirstName());

        } finally {
            em.close();
        }
    }

    /**
     * This test ensures that after executing a delete against the db, the deleted type is purged from
     * the DataCache.
     */
    public void testDelete() throws Exception {
        EntityManager em = emf.createEntityManager();
        Cache cache = emf.getCache();
        try {
            CachedEntityStatistics e = createEntity(em);
            assertTrue(cache.contains(CachedEntityStatistics.class, e.getId()));
            em.clear();

            String delete = "DELETE FROM CachedEntityStatistics s WHERE s.id = :id";
            // execute update, this should result in a cache eviction
            em.getTransaction().begin();
            assertEquals(1, em.createQuery(delete).setParameter("id", e.getId()).executeUpdate());
            em.getTransaction().commit();
            assertFalse(cache.contains(CachedEntityStatistics.class, e.getId()));

            CachedEntityStatistics postUpdate = em.find(CachedEntityStatistics.class, e.getId());
            assertNull(postUpdate);

        } finally {
            em.close();
        }
    }
    
    public void testUpdateNoEvict(){
        OpenJPAEntityManagerFactorySPI emf = createNamedEMF(getPersistenceUnitName(), noEvictProps);
        Cache cache = emf.getCache();
        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        try {
            CachedEntityStatistics e = createEntity(em);
            assertTrue(cache.contains(CachedEntityStatistics.class, e.getId()));
            em.clear();

            String update = "UPDATE CachedEntityStatistics s SET s.firstName = :name WHERE s.id = :id";
            String name = "name_" + System.currentTimeMillis();
            // execute update, this should result in a cache eviction
            em.getTransaction().begin();
            assertEquals(1, em.createQuery(update).setParameter("name", name).setParameter("id", e.getId())
                .executeUpdate());
            em.getTransaction().commit();
            assertTrue(cache.contains(CachedEntityStatistics.class, e.getId()));

            CachedEntityStatistics postUpdate = em.find(CachedEntityStatistics.class, e.getId());
            assertNotEquals(name, postUpdate.getFirstName());
        }finally{
            emf.close();
        }
    }

    public void testDeleteNoEvict() throws Exception {
        OpenJPAEntityManagerFactorySPI emf = createNamedEMF(getPersistenceUnitName(), noEvictProps);
        Cache cache = emf.getCache();
        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        try {
            CachedEntityStatistics e = createEntity(em);
            assertTrue(cache.contains(CachedEntityStatistics.class, e.getId()));
            em.clear();

            String delete = "DELETE FROM CachedEntityStatistics s WHERE s.id = :id";
            // execute update, this should NOT result in a cache eviction
            em.getTransaction().begin();
            assertEquals(1, em.createQuery(delete).setParameter("id", e.getId()).executeUpdate());
            em.getTransaction().commit();
            assertTrue(cache.contains(CachedEntityStatistics.class, e.getId()));

            em.clear();
            
            CachedEntityStatistics postUpdate = em.find(CachedEntityStatistics.class, e.getId());
            assertNotNull(postUpdate);

        } finally {
            em.close();
        }
    }
    
    private CachedEntityStatistics createEntity(EntityManager em) {
        em.getTransaction().begin();
        CachedEntityStatistics e = new CachedEntityStatistics();
        em.persist(e);
        em.getTransaction().commit();

        return e;
    }

}

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
package org.apache.openjpa.persistence.simple;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.CacheRetrieveMode;
import javax.persistence.CacheStoreMode;

import org.apache.openjpa.persistence.JPAProperties;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.test.SingleEMTestCase;

public class TestRefresh extends SingleEMTestCase {

    public void setUp() {
        super.setUp(CLEAR_TABLES, Item.class, 
             "openjpa.AutoDetach", "commit",
             "openjpa.DataCache", "true",
             "openjpa.RemoteCommitProvider", "sjvm");
    }

    public void testFlushRefreshNewInstance() {
        em.getTransaction().begin();
        Item item = new Item();
        item.setItemData("Test Data");
        em.persist(item);
        em.flush();
        em.refresh(item);
        em.getTransaction().commit();
        assertEquals("Test Data", item.getItemData());
    }
    
    /**
     * Refresh always bypass L2 cache.
     * According to JPA 2.0 Spec Section 3.7.2:
     * "The retrieveMode property is ignored for the refresh method,
     *  which always causes data to be retrieved from the database, not the cache."
     */
    public void testRefreshBypassL2Cache() {
        String original = "Original L2 Cached Data";
        String sneakUpdate = "Sneak Update";
        em.getTransaction().begin();
        Item item = new Item();
        item.setItemData(original);
        em.persist(item);
        em.getTransaction().commit();
        assertCached(Item.class, item.getItemId());
        
        // Sneakily update with SQL
        String sql = "UPDATE I_ITEM SET I_DATA=?1 WHERE I_ID=?2";
        em.getTransaction().begin();
        int updateCount = em.createNativeQuery(sql)
            .setParameter(1, sneakUpdate)
            .setParameter(2, item.getItemId())
            .executeUpdate();
        assertEquals(1, updateCount);
        em.getTransaction().commit();
        
        em.getTransaction().begin();
        // Find will find the L2 cached data
        item = em.find(Item.class, item.getItemId());
        assertEquals(original, item.getItemData());
        // But refresh will get the actual database record
        em.refresh(item);
        assertEquals(sneakUpdate, item.getItemData());

        // Even if cache retrieve mode is set to USE
        em.setProperty(JPAProperties.CACHE_RETRIEVE_MODE, CacheRetrieveMode.USE);
        em.refresh(item);
        assertEquals(sneakUpdate, item.getItemData());
        em.getTransaction().rollback();
    }
    
    public void testCacheRetrieveModeSetting() {
        OpenJPAEntityManager em = emf.createEntityManager();
        em.setProperty(JPAProperties.CACHE_RETRIEVE_MODE, CacheRetrieveMode.USE);
        Map<String, Object> properties = em.getProperties();
        if (!properties.containsKey(JPAProperties.CACHE_RETRIEVE_MODE)) {
            System.err.println(properties);
            fail("Expected " + JPAProperties.CACHE_RETRIEVE_MODE + " properties be returned");
        }
        Object mode = properties.get(JPAProperties.CACHE_RETRIEVE_MODE);
        assertEquals(mode, CacheRetrieveMode.USE);
    }
    
    public void testCacheStoreModeSetting() {
        OpenJPAEntityManager em = emf.createEntityManager();
        em.setProperty(JPAProperties.CACHE_STORE_MODE, CacheStoreMode.USE);
        Map<String, Object> properties = em.getProperties();
        if (!properties.containsKey(JPAProperties.CACHE_STORE_MODE)) {
            System.err.println(properties);
            fail("Expected " + JPAProperties.CACHE_STORE_MODE + " properties be returned");
        }
        Object mode = properties.get(JPAProperties.CACHE_STORE_MODE);
        assertEquals(mode, CacheStoreMode.USE);
    }
    
    public void testRefreshAfterRemove() {
        try {
            em.getTransaction().begin();
            Item item = new Item();
            item.setItemData("Test Data");
            em.persist(item);
            em.flush();
            em.remove(item);
            em.flush();
            em.refresh(item);
            em.getTransaction().commit();
            fail("Did not catch expected IllegalArgumentException for refresh() of removed entity");
        } catch (IllegalArgumentException e) {
            // Expected exception
        }
    }
    
    public void testFindWithCacheRetrieveProperty() {
        String key = "Test property in find.";
        OpenJPAEntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Item item = new Item();
        item.setItemData(key);
        em.persist(item);
        em.flush();
        em.getTransaction().commit();
        int id = item.getItemId();
        em.clear();
        emf.getCache().evictAll();

        assertEquals(key, item.getItemData());

        em.setProperty(JPAProperties.CACHE_STORE_MODE, CacheStoreMode.USE);
        em.setProperty(JPAProperties.CACHE_RETRIEVE_MODE, CacheRetrieveMode.USE);
        Map<String, Object> properties = em.getProperties();
        if (!properties.containsKey(JPAProperties.CACHE_STORE_MODE)) {
            System.err.println(properties);
            fail("Expected " + JPAProperties.CACHE_STORE_MODE + " properties be returned");
        }
        if (!properties.containsKey(JPAProperties.CACHE_RETRIEVE_MODE)) {
            System.err.println(properties);
            fail("Expected " + JPAProperties.CACHE_RETRIEVE_MODE + " properties be returned");
        }
        Map<String, Object> paramProperties = new HashMap<String, Object>();
        paramProperties.put(JPAProperties.CACHE_STORE_MODE, CacheStoreMode.BYPASS);
        paramProperties.put(JPAProperties.CACHE_RETRIEVE_MODE, CacheRetrieveMode.BYPASS);
        Item fItem = em.find(Item.class, id, paramProperties);
        assertEquals(fItem.getItemData(), key);
        assertNotCached(Item.class, id);

        Object mode = em.getProperties().get(JPAProperties.CACHE_STORE_MODE);
        assertEquals(mode, CacheStoreMode.USE);        
        mode = em.getProperties().get(JPAProperties.CACHE_RETRIEVE_MODE);
        assertEquals(mode, CacheRetrieveMode.USE);        
    }

    public void testRefreshWithCacheRetrieveProperty() {
        String key = "Test property in refresh.";
        String updatedKey = "Updated test property in refresh.";
        OpenJPAEntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Item item = new Item();
        item.setItemData(key);
        em.persist(item);
        em.flush();
        em.getTransaction().commit();
        assertEquals(key, item.getItemData());
        
        int id = item.getItemId();
        emf.getCache().evictAll();

        assertEquals(key, item.getItemData());

        em.setProperty(JPAProperties.CACHE_STORE_MODE, CacheStoreMode.USE);
        em.setProperty(JPAProperties.CACHE_RETRIEVE_MODE, CacheRetrieveMode.USE);
        Map<String, Object> properties = em.getProperties();
        if (!properties.containsKey(JPAProperties.CACHE_STORE_MODE)) {
            System.err.println(properties);
            fail("Expected " + JPAProperties.CACHE_STORE_MODE + " properties be returned");
        }
        if (!properties.containsKey(JPAProperties.CACHE_RETRIEVE_MODE)) {
            System.err.println(properties);
            fail("Expected " + JPAProperties.CACHE_RETRIEVE_MODE + " properties be returned");
        }
        Map<String, Object> paramProperties = new HashMap<String, Object>();
        paramProperties.put(JPAProperties.CACHE_STORE_MODE, CacheStoreMode.BYPASS);
        paramProperties.put(JPAProperties.CACHE_RETRIEVE_MODE, CacheRetrieveMode.BYPASS);
        Item fItem = em.find(Item.class, id, paramProperties);
        assertEquals(key, fItem.getItemData());
        assertNotCached(Item.class, id);

        fItem.setItemData(updatedKey);
        assertEquals(updatedKey, fItem.getItemData());

        em.refresh(fItem, paramProperties);
        assertEquals(key, fItem.getItemData());
        assertNotCached(Item.class, id);

        Object mode = em.getProperties().get(JPAProperties.CACHE_STORE_MODE);
        assertEquals(mode, CacheStoreMode.USE);        
        mode = em.getProperties().get(JPAProperties.CACHE_RETRIEVE_MODE);
        assertEquals(mode, CacheRetrieveMode.USE);        
    }
    
    void assertCached(Class<?> cls, Object oid) {
        assertTrue(cls + ":" + oid + " should be in L2 cache, but not", emf.getCache().contains(cls, oid));
    }
    
    void assertNotCached(Class<?> cls, Object oid) {
        assertTrue(cls + ":" + oid + " should not be in L2 cache, but is", !emf.getCache().contains(cls, oid));
    }
    
}

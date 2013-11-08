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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.openjpa.datacache.ConcurrentQueryCache;
import org.apache.openjpa.datacache.DataCache;
import org.apache.openjpa.persistence.OpenJPAEntityManagerSPI;
import org.apache.openjpa.persistence.StoreCacheImpl;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;
import org.apache.openjpa.util.IntId;

public class TestLRUCache extends SingleEMFTestCase {
    private final int cacheSize = 5;
    private final String QUERY = "SELECT p FROM CachedPerson p WHERE p.id=";
    
    @Override
    protected void setUp(Object... props) {
        super.setUp(CLEAR_TABLES, 
            CachedPerson.class, 
            "openjpa.RemoteCommitProvider", "sjvm",
            "openjpa.DataCache","true(SoftReferenceSize=0,Lru=true,CacheSize="+cacheSize+")",
            "openjpa.QueryCache","true(SoftReferenceSize=0,Lru=true,CacheSize="+cacheSize+")"
        );
    }

    public void testQueryCacheOverFlow() {
        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        ConcurrentQueryCache cache =
            (ConcurrentQueryCache) emf.getConfiguration().getDataCacheManagerInstance().getSystemQueryCache();

        // populate entities.
        em.getTransaction().begin();
        for (int i = 0; i < cacheSize + 1; i++) {
            CachedPerson person = new CachedPerson();
            person.setId(i);
            em.persist(person);
        }
        em.getTransaction().commit();
        // Clean up persistence context.
        em.clear();

        // Populate query cache
        for (int i = 0; i < cacheSize + 1; i++) {
            em.createQuery(QUERY + i, CachedPerson.class).getSingleResult();
        }

        Set<?> keys = cache.getCacheMap().keySet();
        assertEquals(cacheSize, keys.size());
        List<String> strKeys = new ArrayList<String>();
        for (Object key : keys) {
            strKeys.add(key.toString());
        }

        for (int i = 0; i < keys.size(); i++) {
            boolean res = contains(QUERY + i, strKeys);
            if (i == 0) {
                assertFalse(res);
            } else {
                assertTrue(res);
            }
        }
        em.close();
    }

    public void testDataCacheOverFlow() {
        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        StoreCacheImpl storeCache = (StoreCacheImpl) emf.getCache();
        DataCache cache = (DataCache) storeCache.getDelegate();
        LinkedList<CachedPerson> people = new LinkedList<CachedPerson>();

        // Persist cacheSize + 1 Entites.
        for (int i = 0; i < cacheSize + 1; i++) {
            em.getTransaction().begin();
            CachedPerson person = new CachedPerson();
            person.setId(i);
            em.persist(person);
            people.addFirst(person);
            em.getTransaction().commit();
        }

        // Assert that the first persisted entity isn't in the cache and everything else is.
        for (int i = 0; i < cacheSize + 1; i++) {
            IntId id = new IntId(CachedPerson.class, i);
            boolean contains = cache.get(id) != null;
            if (i == 0) {
                assertFalse(contains);
            } else {
                assertTrue(contains);
            }
        }
        em.close();
    }

    private boolean contains(String needle, List<String> haystack) {
        for (String s : haystack) {
            if (s.contains(needle)) {
                return true;
            }
        }
        return false;
    }
}

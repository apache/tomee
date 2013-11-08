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
package org.apache.openjpa.persistence.jdbc.sqlcache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.kernel.FinderCache;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.test.SQLListenerTestCase;

/**
 * Basic test to check FinderQuery caches.
 *   
 * @author Pinaki Poddar
 *
 */
public class TestFinderCache extends SQLListenerTestCase {
    public static final long[] BOOK_IDS = {1000, 2000, 3000};
    public static final String[] BOOK_NAMES = {"Argumentative Indian", "Tin Drum", "Blink"};
    public static final long[] CD_IDS = {1001, 2001, 3001};
    public static final String[] CD_LABELS =  {"Beatles", "Sinatra", "Don't Rock My Boat"};
    
    void createTestData() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        for (int i = 0; i < BOOK_IDS.length; i++) {
            Book book = new Book();
            book.setId(BOOK_IDS[i]);
            book.setTitle(BOOK_NAMES[i]);
            em.persist(book);
        }
        for (int i = 0; i < CD_IDS.length; i++) {
            CD cd = new CD();
            cd.setId(CD_IDS[i]);
            cd.setLabel(CD_LABELS[i]);
            em.persist(cd);
        }
        em.getTransaction().commit();
        em.close();
    }
    
    public void setUp() {
        super.setUp(CLEAR_TABLES, 
                "openjpa.RuntimeUnenhancedClasses", "unsupported",
                "openjpa.DynamicEnhancementAgent", "false",
                "openjpa.DataCache","false",
                Merchandise.class, Book.class, CD.class, 
            Author.class, Person.class, Singer.class, Address.class);
        createTestData();
    }
    
    public void testFinder() {
        //closeEMF(emf);  // close EMF provided by SingleEMFTestCase
        OpenJPAEntityManagerFactorySPI emf1 = createEMF("openjpa.jdbc.FinderCache", "false");
        run(1, Book.class, BOOK_IDS); // for warmup
        assertNull(getCache(emf1));
        OpenJPAEntityManagerFactorySPI emf2 = createEMF("openjpa.jdbc.FinderCache", "true");
        assertNotNull(getCache(emf2));
        closeEMF(emf1);
        closeEMF(emf2);
    }
    
    public void testSQLEventListener() {
        EntityManager em = emf.createEntityManager();
        int N = 3;
        sql.clear();
        for (int i = 0; i < N; i++) {
            em.clear();
            for (long id : BOOK_IDS) {
                Book pc = em.find(Book.class, id);
                assertNotNull(pc);
            }
        }
        assertEquals(BOOK_IDS.length*N, sql.size());
        em.close();
    }
    
    /**
     * Run a finder query for each identifiers N times and report the median
     * execution time.
     */
    <T> long run(int N, Class<T> cls, long[] ids) {
        EntityManager em = emf.createEntityManager();
        List<Long> stats = new ArrayList<Long>();
        for (int n = 0; n < N; n++) {
            em.clear();
            long start = System.nanoTime();
            for (int i = 0; i < ids.length; i++) {
                T pc = em.find(cls, ids[i]);
                assertNotNull(pc);
                assertTrue(cls.isInstance(pc));
            }
            long end = System.nanoTime();
            stats.add(end-start);
        }
        Collections.sort(stats);
        return stats.get(N/2);
    }
    
    FinderCache getCache(OpenJPAEntityManagerFactorySPI oemf) {
        return ((JDBCConfiguration) oemf.getConfiguration()).
                getFinderCacheInstance();
    }

}

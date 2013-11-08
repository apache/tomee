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
package org.apache.openjpa.persistence.compat;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.openjpa.persistence.EntityManagerImpl;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.relations.TblChild;
import org.apache.openjpa.persistence.relations.TblGrandChild;
import org.apache.openjpa.persistence.relations.TblParent;
import org.apache.openjpa.persistence.simple.Person;
import org.apache.openjpa.persistence.test.AllowFailure;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * <b>TestQuerySQLCache</b> is used to verify multiple permutations of openjpa.jdbc.QuerySQLCache settings that were 
 * valid in JPA 1.2 but may not be valid in JPA 2.0
 */
public class TestQuerySQLCache extends SingleEMFTestCase {
    
    final int nThreads = 5;
    final int nPeople = 100;
    final int nIterations = 10;

    @Override
    public void setUp() {
        // need this to cleanup existing tables as some entity names are reused
        setUp(DROP_TABLES, Person.class, TblChild.class, TblGrandChild.class, TblParent.class);
    }

    /*
     * Verify an exception is thrown if a bad cache implementation class is specified
     */
    public void testBadCustomCacheSetting() {
        Map props = new HashMap(System.getProperties());
        props.put("openjpa.MetaDataFactory", "jpa(Types=" + Person.class.getName() + ")");
        props.put("openjpa.jdbc.QuerySQLCache", 
                  "org.apache.openjpa.persistence.compatible.TestQuerySQLCache.BadCacheMap");

        OpenJPAEntityManagerFactorySPI emf1 = null;
        try {
            emf1 = (OpenJPAEntityManagerFactorySPI)OpenJPAPersistence.
                                                 cast(Persistence.createEntityManagerFactory("test", props));
            // 
            // EMF creation must throw an exception because the cache implementation class will not be found
            // 
            fail("EMF creation must throw an exception because the cache implementation class will not be found");
        }
        catch (Exception e) {
            assertTrue(true);
        } finally {
            closeEMF(emf1);
        }
    }


    /*
     * Verify multi-threaded multi-entity manager finder works with the QuerySQLCache set to "all"
     */
    @AllowFailure(message="OPENJPA-1179 2.0 doesn't allow 'all' as in previous releases")
    public void testMultiEMCachingAll() {
        Map props = new HashMap(System.getProperties());
        props.put("openjpa.MetaDataFactory", "jpa(Types=" + Person.class.getName() + ")");
        props.put("openjpa.jdbc.QuerySQLCache", "all");
        runMultiEMCaching(props);        
    }


    /*
     * Verify multi-threaded multi-entity manager finder works with the QuerySQLCache set to "true"
     */
    public void testMultiEMCachingTrue() {
        Map props = new HashMap(System.getProperties());
        props.put("openjpa.MetaDataFactory", "jpa(Types=" + Person.class.getName() + ")");
        props.put("openjpa.jdbc.QuerySQLCache", "true");
        runMultiEMCaching(props);
    }


    /*
     * Verify QuerySQLCacheValue setting "true" uses the expected cache implementation and is caching
     */
    @AllowFailure(message="Fails after first run with duplicate key value in a unique PK constraint or index")
    public void testEagerFetch() {
        Map props = new HashMap(System.getProperties());
        props.put("openjpa.MetaDataFactory", "jpa(Types=" + TblChild.class.getName() + ";"
                                                          + TblGrandChild.class.getName() + ";"
                                                          + TblParent.class.getName() + ")");
        props.put("openjpa.jdbc.QuerySQLCache", "true");

        OpenJPAEntityManagerFactorySPI emf1 = (OpenJPAEntityManagerFactorySPI) OpenJPAPersistence.
                                             cast(Persistence.createEntityManagerFactory("test", props));
        try {
            EntityManagerImpl em = (EntityManagerImpl)emf1.createEntityManager();
    
            em.getTransaction().begin();
    
            for (int i = 1; i < 3; i++) {
                TblParent p = new TblParent();
                p.setParentId(i);
                TblChild c = new TblChild();
                c.setChildId(i);
                c.setTblParent(p);
                p.addTblChild(c);
                em.persist(p);
                em.persist(c);
    
                TblGrandChild gc = new TblGrandChild();
                gc.setGrandChildId(i);
                gc.setTblChild(c);
                c.addTblGrandChild(gc);
    
                em.persist(p);
                em.persist(c);
                em.persist(gc);
            }
            em.flush();
            em.getTransaction().commit();
            em.clear();
    
            for (int i = 1; i < 3; i++) {
                TblParent p = em.find(TblParent.class, i);
                int pid = p.getParentId();
                assertEquals(pid, i);
                Collection<TblChild> children = p.getTblChildren();
                boolean hasChild = false;
                for (TblChild c : children) {
                    hasChild = true;
                    Collection<TblGrandChild> gchildren = c.getTblGrandChildren();
                    int cid = c.getChildId();
                    assertEquals(cid, i);
                    boolean hasGrandChild = false;
                    for (TblGrandChild gc : gchildren) {
                        hasGrandChild = true;
                        int gcId = gc.getGrandChildId();
                        assertEquals(gcId, i);
                    }
                    assertTrue(hasGrandChild);
                }
                assertTrue(hasChild);
                em.close();
            }
        } finally {
            closeEMF(emf1);
        }
    }


    private void runMultiEMCaching(Map props) {
        EntityManagerFactory emfac = Persistence.createEntityManagerFactory("test", props);
        try {
            EntityManager em = emfac.createEntityManager();            

            // 
            // Create some entities
            // 
            em.getTransaction().begin();
            for (int i = 0; i < nPeople; i++) {
                Person p = new Person();
                p.setId(i);
                em.persist(p);
            }
            em.flush();
            em.getTransaction().commit();
            em.close();

            Thread[] newThreads = new Thread[nThreads];
            FindPeople[] customer = new FindPeople[nThreads];
            for (int i=0; i < nThreads; i++) {
                customer[i] = new FindPeople(emfac, 0, nPeople, nIterations, i);
                newThreads[i] = new Thread(customer[i]);
                newThreads[i].start();
            }

            // 
            // Wait for the worker threads to complete
            // 
            for (int i = 0; i < nThreads; i++) {
                try {
                    newThreads[i].join();
                }
                catch (InterruptedException e) {
                    this.fail("Caught Interrupted Exception: " + e);
                }
            }   

            // 
            // Run through the state of all runnables to assert if any of them failed.
            // 
            for (int i = 0; i < nThreads; i++) {
                assertFalse(customer[i].hadFailures());
            }

            // 
            // Clean up the entities used in this test
            // 
            em = emfac.createEntityManager();            
            em.getTransaction().begin();
            for (int i = 0; i < nPeople; i++) {
                Person p = em.find(Person.class, i);
                em.remove(p);
            }
            em.flush();
            em.getTransaction().commit();
            em.close();
        } finally {
            closeEMF(emfac);
        }
    }
    
    
    /*
     * Simple runnable to test finder in a tight loop.  Multiple instances of this runnable will run simultaneously
     */
    private class FindPeople implements Runnable {
        private int startId;
        private int endId;
        private int thread;
        private int iterations;
        private EntityManagerFactory emf;
        private boolean failures = false;

        public FindPeople(EntityManagerFactory emf, int startId, int endId, int iterations, int thread) {
            super();
            this.startId = startId;
            this.endId = endId;
            this.thread = thread;
            this.iterations = iterations;
            this.emf = emf;
        }

        public boolean hadFailures() {
            return failures;
        }

        public void run() {
            try {
                EntityManager em = emf.createEntityManager();            
                for (int j = 0; j < iterations; j++) {

                    for (int i = startId; i < endId; i++) {
                        Person p1 = em.find(Person.class, i);
                        if (p1.getId() != i) {
                            System.out.println("Finder failed: " + i);
                            failures = true;
                            break;
                        }
                    }
                    em.clear();  
                }
                em.close();  
            }
            catch (Exception e) {
                failures = true;
                System.out.println("Thread " + thread + " exception :" + e );
            }
        }
    }
}

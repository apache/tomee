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

import java.util.List;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import junit.framework.TestCase;

import org.apache.openjpa.kernel.QueryStatistics;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactory;
import org.apache.openjpa.persistence.OpenJPAPersistence;

/**
 * Test reparameterization of cached query under heavy load. 
 * 
 * @author Pinaki Poddar
 *
 */
public class TestMultithreadedReparametrization extends TestCase {
    private static String RESOURCE = "META-INF/persistence.xml"; 
    private static String UNIT_NAME = "PreparedQuery";
    protected static OpenJPAEntityManagerFactory emf;
    
    public void setUp() throws Exception {
        super.setUp();
        if (emf == null) {
            Properties config = new Properties();
            config.put("openjpa.Log", "SQL=WARN");
            config.put("openjpa.jdbc.QuerySQLCache", "true(EnableStatistics=true)");
            config.put("openjpa.ConnectionFactoryProperties", "PrintParameters=true");
            emf = OpenJPAPersistence.createEntityManagerFactory(UNIT_NAME, RESOURCE, config);
        }
    }
    
    public void testReparametrizationUnderHeavyLoad() throws Exception {
        long baseId = System.currentTimeMillis();
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        int nThreads = 80;
        for (int i = 0; i < nThreads; i++) {
            Person p = new Person();
            p.setId(baseId+i);
            p.setFirstName("First"+i);
            p.setLastName("Last"+i);
            p.setAge((short)(20+i));
            em.persist(p);
        }
        em.getTransaction().commit();
    
        String jpql = "select p from Person p " 
                    + "where p.id=:id and p.firstName=:first and p.lastName=:last and p.age=:age";
        int nRepeats = 20;
        Thread[] threads = new Thread[nThreads];
        for (int i = 0; i < nThreads; i++) {
            Object[] args = {"id", baseId+i, "first", "First"+i, "last", "Last"+i, "age", (short)(20+i)};
            QueryThread thread = new QueryThread(emf.createEntityManager(), jpql, args, nRepeats);
            threads[i] = new Thread(thread);
        }
        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        QueryStatistics<String> stats = emf.getConfiguration().getQuerySQLCacheInstance().getStatistics();
        assertEquals(nThreads*nRepeats,stats.getExecutionCount(), stats.getExecutionCount(jpql));
        assertEquals(nThreads*nRepeats-1,stats.getExecutionCount(), stats.getHitCount(jpql));
        
    }
    
    /**
     * Each thread executes same query with same parameters repeatedly.
     * 
     * @author Pinaki Poddar
     *
     */
    public static class QueryThread implements Runnable {
        public final EntityManager em;
        public final String jpql;
        public final Object[] args;
        public final int nTimes;
        public QueryThread(EntityManager em, String jpql, Object[] args, int r) {
            this.em = em;
            this.jpql = jpql;
            this.args = args;
            this.nTimes = r;
        }
        
        public void run()  {
            try {
            for (int i = 0; i < nTimes; i++) {
                TypedQuery<Person> q = em.createQuery(jpql, Person.class);
                for (int j = 0; j < args.length; j += 2) {
                    q.setParameter(args[j].toString(), args[j+1]);
                }
                List<Person> result = q.getResultList();
                assertEquals(Thread.currentThread() + " failed", 1, result.size());
                Person p = result.get(0);
                assertEquals(args[1], p.getId());
                assertEquals(args[3], p.getFirstName());
                assertEquals(args[5], p.getLastName());
                assertEquals(args[7], p.getAge());
                
            }
            } catch (Exception ex) {
                ex.printStackTrace();
                fail();
            }
        }
        
    }
}

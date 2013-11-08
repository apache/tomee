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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

/**
 * Tests when multiple user threads enter the same EntityManager and executes 
 * query. 
 * 
 * @author Pinaki Poddar
 * 
 */
public class TestQueryMultiThreaded extends SliceTestCase {

    private int POBJECT_COUNT = 25;
    private int VALUE_MIN = 100;
    private int VALUE_MAX = VALUE_MIN + POBJECT_COUNT - 1;
    private static int THREADS = 5;
    private static int MAX_TIMEOUT = 300;
    private ExecutorService group; 
    private Future[] futures;

    protected String getPersistenceUnitName() {
        return "ordering";
    }

    public void setUp() throws Exception {
        super.setUp(PObject.class, Person.class, Address.class, Country.class,
                CLEAR_TABLES, "openjpa.Multithreaded", "true");
        int count = count(PObject.class);
        if (count == 0) {
            create(POBJECT_COUNT);
        }
        group = new ThreadPoolExecutor(THREADS, THREADS,
                60, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(), new ThreadFactory() {
                    public Thread newThread(Runnable r) {
                        return new Thread(r);
                    }
                
                });
        futures = new Future[THREADS];
    }
    
    public void tearDown()  throws Exception {
        group.shutdown();
        super.tearDown();
    }

    void create(int N) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        for (int i = 0; i < POBJECT_COUNT; i++) {
            PObject pc = new PObject();
            pc.setValue(VALUE_MIN + i);
            em.persist(pc);
            String slice = SlicePersistence.getSlice(pc);
            String expected = (pc.getValue() % 2 == 0) ? "Even" : "Odd";
            assertEquals(expected, slice);
        }
        Person p1 = new Person();
        Person p2 = new Person();
        Address a1 = new Address();
        Address a2 = new Address();
        p1.setName("Even");
        p2.setName("Odd");
        a1.setCity("San Francisco");
        a2.setCity("Rome");
        p1.setAddress(a1);
        p2.setAddress(a2);
        em.persist(p1);
        em.persist(p2);
        assertEquals("Even", SlicePersistence.getSlice(p1));
        assertEquals("Odd", SlicePersistence.getSlice(p2));

        em.getTransaction().commit();
    }
    
    public void testQueryResultIsOrderedAcrossSlice() {
        final EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        final Query query = em.createQuery(
                "SELECT p.value,p FROM PObject p ORDER BY p.value ASC");
        for (int i = 0; i < THREADS; i++) {
            futures[i] = group.submit(new Callable<Object>() {
                public Object call() {
                    List result = query.getResultList();
                    Integer old = Integer.MIN_VALUE;
                    for (Object row : result) {
                        Object[] line = (Object[]) row;
                        int value = ((Integer) line[0]).intValue();
                        PObject pc = (PObject) line[1];
                        assertTrue(value >= old);
                        old = value;
                        assertEquals(value, pc.getValue());
                    }
                    return null;
                }
            });
        }
        
        waitForTermination();
        em.getTransaction().rollback();
    }

    public void testAggregateQuery() {
        final EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        final Query countQ = em.createQuery("SELECT COUNT(p) FROM PObject p");
        final Query maxQ = em.createQuery("SELECT MAX(p.value) FROM PObject p");
        final Query minQ = em.createQuery("SELECT MIN(p.value) FROM PObject p");
        final Query sumQ = em.createQuery("SELECT SUM(p.value) FROM PObject p");
        final Query minmaxQ = em.createQuery(
                "SELECT MIN(p.value),MAX(p.value) FROM PObject p");
        for (int i = 0; i < THREADS; i++) {
            futures[i] = group.submit(new Callable<Object>() {
                public Object call() {
                    Object count = countQ.getSingleResult();
                    Object max = maxQ.getSingleResult();
                    Object min = minQ.getSingleResult();
                    Object sum = sumQ.getSingleResult();
                    Object minmax = minmaxQ.getSingleResult();
                    
                    Object min1 = ((Object[]) minmax)[0];
                    Object max1 = ((Object[]) minmax)[1];


                    assertEquals(POBJECT_COUNT, ((Number) count).intValue());
                    assertEquals(VALUE_MAX, ((Number) max).intValue());
                    assertEquals(VALUE_MIN, ((Number) min).intValue());
                    assertEquals((VALUE_MIN + VALUE_MAX) * POBJECT_COUNT,
                            2 * ((Number) sum).intValue());
                    assertEquals(min, min1);
                    assertEquals(max, max1);
                    return null;
                }
            });
        }
        waitForTermination();
        em.getTransaction().rollback();
    }

    public void testAggregateQueryWithMissingValueFromSlice() {
        final EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        final Query maxQ = em.createQuery(
                "SELECT MAX(p.value) FROM PObject p WHERE MOD(p.value,2)=0");
        for (int i = 0; i < THREADS; i++) {
            futures[i] = group.submit(new Callable<Object>() {
                public Object call() {
                    Object max = maxQ.getSingleResult();
                    assertEquals(VALUE_MAX, ((Number) max).intValue());
                    return null;
                }
            });
        }
        waitForTermination();
        em.getTransaction().rollback();
    }

    public void testSetMaxResult() {
        final EntityManager em = emf.createEntityManager();
        final int limit = 3;
        em.getTransaction().begin();
        final Query q = em.createQuery(
                "SELECT p.value,p FROM PObject p ORDER BY p.value ASC");
        for (int i = 0; i < THREADS; i++) {
            futures[i] = group.submit(new Callable<Object>() {
                public Object call() {
                    List result = q.setMaxResults(limit).getResultList();
                    int i = 0;
                    for (Object row : result) {
                        Object[] line = (Object[]) row;
                        int value = ((Integer) line[0]).intValue();
                        PObject pc = (PObject) line[1];
                    }
                    assertEquals(limit, result.size());
                    return null;
                }

            });
        }
        waitForTermination();
        em.getTransaction().rollback();
    }
    
    public void testHeavyLoad() {
        Thread[] threads = new Thread[800];
        for (int i = 0; i < 800; i++) {
            Runnable r = new Runnable() {
                public void run() {
                    EntityManager em = emf.createEntityManager();
                    em.getTransaction().begin();
                    for (int j = 0; j < 10; j ++) {
                        PObject pc = new PObject();
                        pc.setValue((int)System.currentTimeMillis()%10);
                        em.persist(pc);
                    }
                    em.getTransaction().commit();
                }
            };
            threads[i] = new Thread(r);
            threads[i].start();
        }
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
                fail();
            }
        }
    }

    public void testHint() {
        final List<String> targets = new ArrayList<String>();
        targets.add("Even");
        final EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        final Query query = em.createQuery("SELECT p FROM PObject p");
        for (int i = 0; i < THREADS; i++) {
            futures[i] = group.submit(new Callable<Object>() {

                public Object call() {
                    query.setHint(SlicePersistence.HINT_TARGET, "Even");
                    List result = query.getResultList();
                    for (Object pc : result) {
                        String slice = SlicePersistence.getSlice(pc);
                        assertTrue(targets.contains(slice));
                    }
                    return null;
                }

            });
        }
        waitForTermination();
        em.getTransaction().rollback();
    }

    public void testInMemoryOrderBy() {
        final EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        final Query query =
            em.createQuery("SELECT p FROM PObject p ORDER BY p.value");
        for (int i = 0; i < THREADS; i++) {
            futures[i] = group.submit(new Callable<Object>() {
                public Object call() {
                    List result = query.getResultList();
                    return null;
                }
            });
        }
        waitForTermination();
        em.getTransaction().rollback();
    }

    public void testQueryParameter() {
        final EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        final Query query =
            em.createQuery("SELECT p FROM PObject p WHERE p.value > :v");
        for (int i = 0; i < THREADS; i++) {
            futures[i] = group.submit(new Callable<Object>() {
                public Object call() {
                    query.setParameter("v", 200);
                    List result = query.getResultList();
                    return null;
                }

            });
        }
        waitForTermination();
        em.getTransaction().rollback();
    }

    public void testQueryParameterEntity() {
        final EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        final TypedQuery<Address> addressQ = em.createQuery(
                "select a from Address a where a.city = :city", Address.class);

        final TypedQuery<Person> personQ = em.createQuery(
                "SELECT p FROM Person p WHERE p.address = :a", Person.class);
        for (int i = 0; i < THREADS; i++) {
            futures[i] = group.submit(new Callable<Object>() {
                public Object call() {
                    Address a = addressQ.setParameter("city", "Rome")
                        .getSingleResult();
                    assertNotNull(a);
                    assertEquals("Odd", SlicePersistence.getSlice(a));
                    List<Person> result = personQ.setParameter("a", a).getResultList();
                    assertEquals(1, result.size());
                    Person p = result.get(0);
                    assertEquals("Odd", SlicePersistence.getSlice(p));
                    assertEquals("Rome", p.getAddress().getCity());
                    return null;
                }

            });
        }
        waitForTermination();
        em.getTransaction().rollback();
    }

    void waitForTermination() {
        try {
            for (Future f : futures)
                try {
                    f.get(MAX_TIMEOUT, TimeUnit.SECONDS);
                } catch (TimeoutException te) {
                    fail("Failed " + te + "\r\n" + getStackDump(te));
                } catch (ExecutionException e) {
                    fail("Failed " + "\r\n" + getStackDump(e.getCause()));
                }
        } catch (InterruptedException e) {

        }
    }
    
    String getStackDump(Throwable t) {
        StringWriter writer = new StringWriter();
        t.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

}

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

import javax.persistence.EntityManager;

import junit.framework.AssertionFailedError;

import org.apache.openjpa.datacache.CacheStatistics;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactory;
import org.apache.openjpa.persistence.StoreCache;
import org.apache.openjpa.persistence.StoreCacheImpl;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Tests statistics of data cache operation.
 * 
 */
public class TestStatistics extends SingleEMFTestCase {
    private static final boolean L2Cached = true;
    private static final boolean L1Cached = true;
    private static final String cls = CachedEntityStatistics.class.getName();

    Object[] p =
        new Object[] { CLEAR_TABLES, CachedEntityStatistics.class
            ,"openjpa.DataCache", "true(EnableStatistics=true)","openjpa.QueryCache", "true", 
//            "openjpa.ConnectionFactoryProperties", "PrintParameters=True", "openjpa.Log","SQL=trace",
        };
    
    private EntityManager em;
    private StoreCache cache;
    CacheStatistics stats;

    public void setUp() {
        super.setUp(p);
        cache = emf.getStoreCache();
        assertNotNull(cache);
        stats = cache.getStatistics();
        assertNotNull(stats);
        em = emf.createEntityManager();

        stats.reset();
        em.clear();
    }

    /**
     * Test that the CacheStatistics is disabled by default.
     */
    public void testDefaultSettings() {
        Object[] props = { "openjpa.DataCache", "true", "openjpa.RemoteCommitProvider", "sjvm" };
        OpenJPAEntityManagerFactory emf1 = createNamedEMF("second-persistence-unit", props);
        assertFalse(emf1.getStoreCache().getStatistics().isEnabled());
        closeEMF(emf1);
    }

    /**
     * Finding an entity from a clean should hit the L2 cache.
     */
    public void testSimpleFind() {
        int hit = 0, read = 0, write = 0;
        CachedEntityStatistics person = createData(false, false);
        em.clear();
        cache.getStatistics().reset();
        assertTrue(cache.getStatistics().isEnabled());
        Object pid = person.getId();
        // Note -- the StoreCache interface doesn't calculate statistics
        assertCached(person, pid, !L1Cached, L2Cached);

        CachedEntityStatistics p = em.find(CachedEntityStatistics.class, pid);
        read++;
        hit++;

        assertion(cls, hit, read, write, stats);

        em.find(CachedEntityStatistics.class, -1);
        read++;

        assertCached(p, pid, L1Cached, L2Cached);
    }

    public void testFind() {
        int hit = 0, evict = 0, read = 0, write = 0;
        CachedEntityStatistics person = createData(true, true);
        em.clear();
        cache.evictAll();
        cache.getStatistics().reset();

        // Make sure cache is enabled and empty
        assertTrue(cache.getStatistics().isEnabled());
        assertion(cls, hit, read, write, stats);

        Object pid = person.getId();

        // Should have 3 reads and 3 writes because of pid and it's eager relationship
        CachedEntityStatistics p = em.find(CachedEntityStatistics.class, pid);
        read++;
        read++;
        read++;
        write++;
        write++;
        write++;
        assertion(cls, hit,  read, write, stats);

        em.clear();
        em.find(CachedEntityStatistics.class, person.getEagerList().toArray(new CachedEntityStatistics[0])[0].getId());
        read++;
        hit++;
        em.clear();

        // Should have two reads and two hits
        person = em.find(CachedEntityStatistics.class, pid);
        read++;
        read++;
        read++;
        hit++;
        hit++;
        hit++;
        assertion(cls, hit,  read, write, stats);
        em.clear();

        // Evict 1 eager field data from cache
        cache.evict(CachedEntityStatistics.class, person.getEagerList().toArray(new CachedEntityStatistics[0])[0]
            .getId());
        evict++;
        p = em.find(CachedEntityStatistics.class, pid);
        read++;
        read++;
        read++;
        hit++;
        hit++;
        write++;

        assertion(cls, hit,  read, write, stats);

        // Test lazy field -- should be a cache miss
        assertEquals(1, p.getLazyList().size());
        read++;
        write++;
        assertion(cls, hit,  read, write, stats);
        em.clear();

        em.find(CachedEntityStatistics.class, p.getLazyList().toArray(new CachedEntityStatistics[0])[0].getId());
        read++;
        hit++;
        assertion(cls, hit,  read, write, stats);

    }

    public void testMultipleUnits() {
        String[] props = { "openjpa.DataCache", "true", "openjpa.RemoteCommitProvider", "sjvm" };
        OpenJPAEntityManagerFactory emf1 = createNamedEMF("test", props);
        OpenJPAEntityManagerFactory emf2 = createNamedEMF("empty-pu", props);
        assertNotSame(emf1, emf2);
        assertNotSame(emf1.getStoreCache(), emf2.getStoreCache());
        assertNotSame(emf1.getStoreCache().getStatistics(), emf2.getStoreCache().getStatistics());
        assertNotSame(((StoreCacheImpl) emf1.getStoreCache()).getDelegate(), ((StoreCacheImpl) emf2.getStoreCache())
            .getDelegate());
        closeEMF(emf1);
        closeEMF(emf2);
    }

    public void testPersist() {
        int hit = 0, read = 0, write = 0;

        em = emf.createEntityManager();
        // test single
        em.getTransaction().begin();
        em.persist(new CachedEntityStatistics());
        write++;
        em.getTransaction().commit();

        assertion(cls, hit,  read, write, stats);

        // test multiple
        CachedEntityStatistics root = new CachedEntityStatistics();
        root.addEager(new CachedEntityStatistics());
        root.addEager(new CachedEntityStatistics());
        root.addLazy(new CachedEntityStatistics());
        root.addLazy(new CachedEntityStatistics());
        write += 5;
        em.getTransaction().begin();
        em.persist(root);
        em.getTransaction().commit();
        assertion(cls, hit,  read, write, stats);

    }

    public void testRefresh() {
        int hit = 0, read = 0, write = 0;
        CachedEntityStatistics e = new CachedEntityStatistics();
        em = emf.createEntityManager();
        // test single
        em.getTransaction().begin();
        em.persist(e);
        write++;
        em.getTransaction().commit();
        assertion(cls, hit,  read, write, stats);

        em.refresh(e);
        read++;
        assertion(cls, hit,  read, write, stats);
        em.clear();

    }

    public void testMerge() {
        int hit = 0, read = 0, write = 0;
        CachedEntityStatistics e = new CachedEntityStatistics();
        em = emf.createEntityManager();
        // test single
        em.getTransaction().begin();
        em.persist(e);
        write++;
        em.getTransaction().commit();
        assertion(cls, hit,  read, write, stats);
        em.clear();
        cache.evictAll();

        em.getTransaction().begin();
        em.merge(e);

        em.getTransaction().commit();
        // TODO -- BROKEN
        // DataCacheStoreManager.flush(...) doesn't account for some of this traffic.
        // read++;
        assertion(cls, hit,  read, write, stats);

    }

    CachedEntityStatistics createData(boolean lazy, boolean eager) {
        em.getTransaction().begin();
        CachedEntityStatistics p = new CachedEntityStatistics();
        if (lazy) {
            p.addLazy(new CachedEntityStatistics());
        }
        if (eager) {
            p.addEager(new CachedEntityStatistics());
            p.addEager(new CachedEntityStatistics());
        }
        em.persist(p);

        em.getTransaction().commit();
        return p;
    }

    /**
     * Get {hit,read,write} count for the cache across all instances.
     */
    long[] snapshot() {
        return new long[] { stats.getReadCount(), stats.getHitCount(), stats.getWriteCount() };
    }

    /**
     * Assert that the passed in hit/eviction/read/write match those values collected by stats.
     */
    private static final void assertion(String cls, int hit, int read, int write, CacheStatistics stats) {
        if (cls == null) {
            throw new RuntimeException("invalid assertion. Null class");
        } else {
            try {
                assertEquals("Hit count doesn't match", hit, stats.getHitCount(cls));
                assertEquals("Read count doesn't match", read, stats.getReadCount(cls));
                assertEquals("Write count doesn't match", write, stats.getWriteCount(cls));
            } catch (AssertionFailedError t) {
                System.out.println("hit : " + stats.getHitCount(cls) + " read: " + stats.getReadCount(cls) + " write: "
                    + stats.getWriteCount(cls));
                throw t;
            }
        }

    }

    void assertDelta(long[] before, long[] after, long readDelta, long hitDelta, long writeDelta) {
        assertEquals("READ count mismatch", readDelta, after[0] - before[0]);
        assertEquals("HIT count mismatch", hitDelta, after[1] - before[1]);
        assertEquals("WRITE count mismatch", writeDelta, after[2] - before[2]);
    }

    void assertCached(Object o, Object oid, boolean l1, boolean l2) {
        boolean l1a = em.contains(o);
        boolean l2a = cache.contains(o.getClass(), oid);
        if (l1 != l1a) {
            fail("Expected " + (l1 ? "" : "not") + " to find instance " + o.getClass().getSimpleName() + ":" + oid
                + " in L1 cache");
        }
        if (l2 != l2a) {
            fail("Expected " + (l2 ? "" : "not") + " to find instance " + o.getClass().getSimpleName() + ":" + oid
                + " in L2 cache");
        }
    }

    void print(String msg, CacheStatistics stats) {
        System.err.println(msg + stats + " H:" + stats.getHitCount() + " R:" + stats.getReadCount() + " W:"
            + stats.getWriteCount());
    }
}

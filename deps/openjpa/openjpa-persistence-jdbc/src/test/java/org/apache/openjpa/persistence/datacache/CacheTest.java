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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;


import org.apache.openjpa.persistence.datacache.common.apps.AppIdCacheObject;
import org.apache.openjpa.persistence.datacache.common.apps.CacheObjectA;
import org.apache.openjpa.persistence.datacache.common.apps.CacheObjectAChild1;
import org.apache.openjpa.persistence.datacache.common.apps.CacheObjectAChild2;
import org.apache.openjpa.persistence.datacache.common.apps.CacheObjectB;
import org.apache.openjpa.persistence.datacache.common.apps.CacheObjectBChild1;
import org.apache.openjpa.persistence.datacache.common.apps.CacheObjectC;
import org.apache.openjpa.persistence.datacache.common.apps.CacheObjectD;
import org.apache.openjpa.persistence.datacache.common.apps.CacheObjectE;
import org.apache.openjpa.persistence.datacache.common.apps.CacheObjectF;
import org.apache.openjpa.persistence.datacache.common.apps.CacheObjectG;
import org.apache.openjpa.persistence.datacache.common.apps.CacheObjectH;
import org.apache.openjpa.persistence.datacache.common.apps.
        CacheObjectInterface;
import org.apache.openjpa.persistence.datacache.common.apps.CacheObjectJ;
import org.apache.openjpa.persistence.datacache.common.apps.RuntimeTest1;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;
import junit.framework.AssertionFailedError;
import org.apache.openjpa.datacache.ConcurrentDataCache;
import org.apache.openjpa.datacache.DataCache;
import org.apache.openjpa.datacache.DelegatingDataCache;
import org.apache.openjpa.datacache.QueryCache;
import org.apache.openjpa.datacache.TypesChangedEvent;
import org.apache.openjpa.datacache.DataCacheManager;
import org.apache.openjpa.kernel.Broker;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.kernel.PCData;
import org.apache.openjpa.kernel.jpql.JPQLParser;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.persistence.Extent;
import org.apache.openjpa.persistence.JPAFacadeHelper;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactory;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.OpenJPAQuery;
import org.apache.openjpa.util.CacheMap;
import org.apache.openjpa.util.Id;
import org.apache.openjpa.util.ImplHelper;
import org.apache.openjpa.util.OpenJPAException;
import org.apache.openjpa.util.ProxyDate;

/**
 * ### should add 1..1 relation test ### app id compound key test
 */
public abstract class CacheTest extends AbstractTestCase {

    private static String ORIG_NAME = "origName";

    private static String NEW_NAME = "newName";

    private static int ORIG_AGE = 30;

    private static String ORIG_PARENT_NAME = "origParentName";

    private static int ORIG_PARENT_AGE = 31;

    private OpenJPAEntityManagerFactory timeoutFactory = null;

    private OpenJPAEntityManagerFactory factory = null;

    private OpenJPAEntityManagerFactory factory2 = null;

    private MetaDataRepository repos;

    private Object oid;

    private Object parentOid;

    private Object oidwithclass;

    private OpenJPAEntityManager em;

    private CacheObjectA a;

    public CacheTest(String test) {
        super(test, "datacachecactusapp");
    }

    public void clear() throws Exception {
        OpenJPAEntityManager em = (OpenJPAEntityManager) factory
            .createEntityManager();
        try {
            Class[] toDelete = new Class[]{ CacheObjectA.class,
                CacheObjectB.class, CacheObjectC.class, CacheObjectD.class,
                CacheObjectE.class, CacheObjectJ.class,
                AppIdCacheObject.class, };
            for (int i = 0; i < toDelete.length; i++) {
                startTx(em);
                Extent e = em.createExtent(toDelete[i], true);
                Iterator it = e.iterator();
                while (it.hasNext()) {
                    em.remove(it.next());
                }
                endTx(em);
            }
        }
        catch (OpenJPAException jpae) {
            Throwable[] ts = jpae.getNestedThrowables();
            for (int i = 0; ts != null && i < ts.length; i++) {
                ts[i].printStackTrace();
            }
//			jpae.printStackTrace();

        }
        finally {
            endEm(em);
        }
    }

    /**
     * Return a string array of extra configuration options for the specified
     * cache.
     */
    protected abstract String[] getConfs();

    /**
     * Return a string array of extra configuration options for a second cache.
     */
    protected abstract String[] getConfs2();

    /**
     * Return true if this cache is a coherent one (one where changes in one
     * cache are immediately visible elsewhere); otherwise returns false. In the
     * context of this test class, coherence is a single-JVM thing only.
     */
    protected boolean isCacheCoherent() {
        return false;
    }

    public void setUp() throws Exception {

        /*
         * OpenJPA does not seem to support plural configuration properties.  (Although it seems
         * that Kodo does...)  Until OpenJPA is updated to support this multiple configuration
         * setting, the following configuration item will be disabled...
         * 
         * Specifically, this type of configuration is currently not allowed...
         * <property name="openjpa.DataCache" value="true, true(Name=xxx)"/>
         */
        String[] confs = getConfs();
//        for (int i = 0; i < confs.length; i = i + 2) {
//            if ("openjpa.DataCache".equals(confs[i]))
//                confs[i + 1] +=
//                    ", true(Name=not-the-default-cache, CacheSize=10)";
//        }
//
        String[] confs2 = getConfs2();
//        for (int i = 0; i < confs2.length; i = i + 2) {
//            if ("openjpa.DataCache".equals(confs2[i]))
//                confs2[i + 1] +=
//                    ", true(Name=not-the-default-cache, CacheSize=10)";
//        }

        Map propsMap1 = new HashMap();
        for (int i = 0; i < confs.length; i += 2) {
            propsMap1.put(confs[i], confs[i + 1]);
        }
        Map propsMap2 = new HashMap();
        for (int i = 0; i < confs2.length; i += 2) {
            propsMap2.put(confs2[i], confs2[i + 1]);
        }

        factory = (OpenJPAEntityManagerFactory) getEmf(propsMap1);
        factory2 = (OpenJPAEntityManagerFactory) getEmf(propsMap2);

        repos = JPAFacadeHelper.toBrokerFactory(factory).getConfiguration()
            .getMetaDataRepositoryInstance();

        String[] biggerConfs = new String[confs.length + 2];
        System.arraycopy(confs, 0, biggerConfs, 0, confs.length);
        biggerConfs[biggerConfs.length - 2] = "openjpa.DataCacheTimeout";
        biggerConfs[biggerConfs.length - 1] = "1000";
        Map propsMap3 = new HashMap();
        for (int i = 0; i < biggerConfs.length; i += 2) {
            propsMap3.put(biggerConfs[i], biggerConfs[i + 1]);
        }
        timeoutFactory = (OpenJPAEntityManagerFactory) getEmf(propsMap3);

        clear();

        OpenJPAEntityManager em = (OpenJPAEntityManager) factory
            .createEntityManager();

        CacheObjectA a;
        CacheObjectA aparent;
        try {
            // we can't specify this for UserTransaction
            /*
                * pm.currentTransaction().setNontransactionalRead(true);
                * pm.currentTransaction().setOptimistic(true);
                */

//			em.setNontransactionalRead(true);
//			em.setOptimistic(true);

            a = new CacheObjectA(ORIG_NAME, ORIG_AGE);
            aparent = new CacheObjectA(ORIG_PARENT_NAME, ORIG_PARENT_AGE);
            a.setRelatedObject(aparent);
            LinkedList children = new LinkedList();
            children.add(a);
            aparent.setRelatedCollection(children);

            startTx(em);
            em.persist(a);
            em.persist(aparent);
            oid = em.getObjectId(a);
            oidwithclass = new Id(CacheObjectA.class, oid.toString());
            parentOid = em.getObjectId(aparent);
            endTx(em);
        }
        finally {
            endEm(em);
        }

        // load an object in a separate pm before the update
        // happens. This should not change, else we're in
        // violation of the spec.
        this.em = factory.createEntityManager();
        startTx(this.em);
        try {
            // OpenJPAEntityManager openEm=(OpenJPAEntityManager) this.em;
            this.a = (CacheObjectA) this.em.find(CacheObjectA.class, oid);

            // load the parent for testCollections().
            CacheObjectA rel = this.a.getRelatedObject();
            rel.getRelatedCollection();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            endTx(this.em);
            // endEm(this.em);
        }

        em = factory.createEntityManager();
        try {
            startTx(em);
            a = (CacheObjectA) em.find(CacheObjectA.class, oid);
            a.setName(NEW_NAME);

            aparent = (CacheObjectA) em.find(CacheObjectA.class, parentOid);

            CacheObjectA a2 = new CacheObjectA(ORIG_NAME, ORIG_AGE);
            a2.setRelatedObject(aparent);
            aparent.getRelatedCollection().add(a2);
            em.persist(a2);
            endTx(em);

            assertNew(a);
        }
        finally {
            endEm(em);
        }
    }

    public void tearDown() throws Exception {
        endEm(em);
        em = null;
        repos = null;
        try {
            closeEMF(factory);
        }
        catch (Exception e) {
        }
        factory = null;
        try {
            closeEMF(factory2);
        }
        catch (Exception e) {
        }
        factory2 = null;
        try {
            closeEMF(timeoutFactory);
        }
        catch (Exception e) {
        }
        super.tearDown();
        timeoutFactory = null;
        oid = null;
        parentOid = null;
        a = null;
    }

    public void testDeletedOneToOneRelations() throws Exception {
        EntityManager em = factory.createEntityManager();
        try {
            startTx(em);
            CacheObjectA a = (CacheObjectA) em.find(CacheObjectA.class, oid);
            assertNotNull(a.getRelatedObject());
            em.remove(a.getRelatedObject());
            endTx(em);
        }
        finally {
            endEm(em);
        }

        EntityManager em2 = factory.createEntityManager();
        try {
            CacheObjectA a2 = (CacheObjectA) em2.find(CacheObjectA.class, oid);
            assertNull(a2.getRelatedObject());
        }
        finally {
            endEm(em2);
        }
    }

    public void testCanCacheExtension() throws Exception {
        DataCache cache = cacheManager(factory).getSystemDataCache();

        // first, test caching of newly created objects.
        OpenJPAEntityManager em = (OpenJPAEntityManager) factory
            .createEntityManager();
        Object o;
        Object oid;
        try {
            startTx(em);
            o = new CacheObjectB("foo");
            em.persist(o);
            endTx(em);
            oid = em.getObjectId(o);
            assertNotNull(oid);
            assertNull(cache.get(oid));
        }
        finally {
            endEm(em);
        }

        // now, test caching of data loaded from the data store.
        em = factory.createEntityManager();
        try {
            o = em.find(CacheObjectB.class, oid);
            assertNotNull(o);
            assertNull(cache.get(oid));
        }
        finally {
            endEm(em);
        }
    }

    public void testGetCache() {
        // first, test caching of newly created objects.
        DataCache defaultCache = cacheManager(factory).getDataCache(
            DataCache.NAME_DEFAULT, false);
        assertNotNull(defaultCache);

        DataCache cache = cacheManager(factory).getSystemDataCache();
        assertEquals(defaultCache, cache);

        ClassMetaData aMeta = repos.getMetaData(CacheObjectA.class, null, true);
        ClassMetaData aChild1Meta = repos.getMetaData(CacheObjectAChild1.class,
            null, true);
        ClassMetaData aChild2Meta = repos.getMetaData(CacheObjectAChild2.class,
            null, true);
        ClassMetaData bMeta = repos.getMetaData(CacheObjectB.class, null, true);
        ClassMetaData bChild1Meta = repos.getMetaData(CacheObjectBChild1.class,
            null, true);
        ClassMetaData cMeta = repos.getMetaData(CacheObjectC.class, null, true);
        ClassMetaData dMeta = repos.getMetaData(CacheObjectD.class, null, true);
        ClassMetaData eMeta = repos.getMetaData(CacheObjectE.class, null, true);

        cache = aMeta.getDataCache();
        assertEquals(defaultCache, cache);
        System.out.println("******DataCacheName:"
            + aChild2Meta.getDataCacheName());
        assertNull(aChild2Meta.getDataCache());

        assertNull(bMeta.getDataCache());

        assertEquals(cMeta.getDataCache(), dMeta.getDataCache());
        if (dMeta.getDataCache() instanceof ConcurrentDataCache) {
            ConcurrentDataCache dCacheImpl =
                (ConcurrentDataCache) dMeta.getDataCache();
            assertEquals(10, dCacheImpl.getCacheSize());
        }
        assertEquals(aMeta.getDataCache(), eMeta.getDataCache());
    }

    public void testPrimitives() throws Exception {
        // make sure that the 'a' that was found before changes
        // were made is still valid.
        assertOld(a);
        em.refresh(a);
        assertNew(a);
    }

    // FIXME Seetha Sep 25,2006
    /*
      * public void testCollections() throws Exception { CacheObjectA parent =
      * (CacheObjectA) em.find(CacheObjectA.class,ORIG_PARENT_NAME);
      * assertEquals(1, parent.getRelatedCollection().size());
      * em.refresh(parent); assertEquals(2,
      * parent.getRelatedCollection().size()); }
      */

    // FIXME Seetha Sep 25,2006
    /*
      * public void testExpiredCollections() { CacheObjectA parent =
      * (CacheObjectA) em.find(CacheObjectA.class,ORIG_PARENT_NAME);
      * em.refresh(parent); Collection relatedOids = new HashSet();
      * for (Iterator iter = parent.getRelatedCollection().iterator();
      *     iter.hasNext();) {
      * relatedOids.add(JDOHelper.getObjectId(iter.next())); }
      *
      * ClassMetaData meta = repos.getMetaData(CacheObjectA.class, null, true);
      * DataCache cache = meta.getDataCache();
      *  // drop the related data from the cache for (Iterator iter =
      * relatedOids.iterator(); iter.hasNext();) cache.remove(iter.next());
      *
      * PersistenceManager pm2 = factory.getPersistenceManager(); try {
      * assertTrue(cache.contains(parentOid)); parent = (CacheObjectA)
      * pm2.getObjectById(parentOid, true);
      *
      * try { for (Iterator iter = relatedOids.iterator(); iter.hasNext();)
      * assertFalse(cache.contains(iter.next())); } catch (AssertionFailedError
      * e) { bug(467, "data cache can over-eagerly load relation data"); } }
      * finally { close(pm2); } }
      */

    public void testExpiredRelations() {
        CacheObjectA a = (CacheObjectA) em.find(CacheObjectA.class, oid);
        em.refresh(a);
        Object relationOid = em.getObjectId(a.getRelatedObject());
        relationOid = new Id(CacheObjectA.class, relationOid.toString());

        ClassMetaData meta = repos.getMetaData(CacheObjectA.class, null, true);
        DataCache cache = meta.getDataCache();

        // drop the related data from the cache
        cache.remove(relationOid);

        OpenJPAEntityManager em2 = (OpenJPAEntityManager) factory
            .createEntityManager();
        try {
            assertTrue(cache.contains(oidwithclass));
            //a = (CacheObjectA) em2.find(CacheObjectA.class, oid);

            try {
                assertFalse(cache.contains(relationOid));
            }
            catch (AssertionFailedError e) {
                // bug(467, "data cache can over-eagerly load relation data");
                /*
                 * I don't think this is a bug, nor should this exception
                 * occur.  Since we're doing a find() operation above and this
                 * field (RelatedObj) has a default Fetch type of EAGER, then
                 * we should be re-loading the RelatedObj and it will be put back
                 * in the cache...  So, by commenting out the above find()
                 * operation (or overriding the default Fetch type to EAGER), then
                 * this assertFalse works...
                 */
                e.printStackTrace();
            }
        }
        finally {
            endEm(em2);
        }
    }

    public void testPCArrays() throws Exception {
        OpenJPAEntityManager newEm = (OpenJPAEntityManager) factory
            .createEntityManager();
        try {
            startTx(newEm);
            CacheObjectA parent = (CacheObjectA) newEm.find(CacheObjectA.class,
                parentOid);
            CacheObjectA a = (CacheObjectA) newEm.find(CacheObjectA.class, oid);
            a.setRelatedArray(new CacheObjectA[]{ parent, a });
            endTx(newEm);
        }
        finally {
            endEm(newEm);
        }

        newEm = (OpenJPAEntityManager) factory.createEntityManager();
        try {
            a = (CacheObjectA) newEm.find(CacheObjectA.class, oid);
            CacheObjectA[] array = a.getRelatedArray();
            assertEquals(2, array.length);
            assertTrue(array[0] instanceof CacheObjectA);
            assertTrue(array[1] instanceof CacheObjectA);

            Object arrayOid = newEm.getObjectId(array[0]);
            if (!arrayOid.equals(parentOid) && !arrayOid.equals(oid)) {
                fail("array does not contain correct oids");
            }

            arrayOid = newEm.getObjectId(array[1]);
            if (!arrayOid.equals(parentOid) && !arrayOid.equals(oid)) {
                fail("array does not contain correct oids");
            }
        }
        finally {
            endEm(newEm);
        }
    }

    public void testStringArrays() throws Exception {
        OpenJPAEntityManager newEm = (OpenJPAEntityManager) factory
            .createEntityManager();
        try {
            startTx(newEm);
            CacheObjectA a = (CacheObjectA) newEm.find(CacheObjectA.class, oid);
            a.setStringArray(new String[]{ "string0", "string1", "string2" });
            endTx(newEm);
        }
        finally {
            endEm(newEm);
        }

        newEm = (OpenJPAEntityManager) factory.createEntityManager();
        try {
            a = (CacheObjectA) newEm.find(CacheObjectA.class, oid);
            String[] array = a.getStringArray();
            assertEquals(3, array.length);
            assertEquals("string0", array[0]);
            assertEquals("string1", array[1]);
            assertEquals("string2", array[2]);
        }
        finally {
            endEm(newEm);
        }
    }

    public void testPrimitiveArrays() throws Exception {
        OpenJPAEntityManager newEm = (OpenJPAEntityManager) factory
            .createEntityManager();
        try {
            startTx(newEm);
            CacheObjectA a = (CacheObjectA) newEm.find(CacheObjectA.class, oid);
            a.setPrimitiveArray(new float[]{ 0, 1, 2 });
            endTx(newEm);
        }
        finally {
            endEm(newEm);
        }

        newEm = (OpenJPAEntityManager) factory.createEntityManager();
        try {
            a = (CacheObjectA) newEm.find(CacheObjectA.class, oid);
            float[] array = a.getPrimitiveArray();
            assertEquals(3, array.length);
            assertEquals(0.0F, array[0], 0);
            assertEquals(1.0F, array[1], 0);
            assertEquals(2.0f, array[2], 0);
        }
        finally {
            endEm(newEm);
        }
    }

    public void testDateArrays() throws Exception {
        OpenJPAEntityManager newEm = (OpenJPAEntityManager) factory
            .createEntityManager();
        CacheObjectA a;
        Date[] dateArray;
        try {
            startTx(newEm);
            a = (CacheObjectA) newEm.find(CacheObjectA.class, oid);
            dateArray = new Date[]{ new Date(), new Date(), new Date() };
            a.setDateArray(dateArray);
            endTx(newEm);
        }
        finally {
            endEm(newEm);
        }

        newEm = (OpenJPAEntityManager) factory.createEntityManager();
        try {
            a = (CacheObjectA) newEm.find(CacheObjectA.class, oid);
            Date[] array = a.getDateArray();
            if (array[0] == dateArray[0]) {
                fail("date objects are the same");
            }
        }
        finally {
            endEm(newEm);
        }
    }

    public void testDate() throws Exception {
        OpenJPAEntityManager newEm = (OpenJPAEntityManager) factory
            .createEntityManager();
        CacheObjectA a;
        Date d;
        try {
            startTx(newEm);
            a = (CacheObjectA) newEm.find(CacheObjectA.class, oid);
            d = new Date();
            a.setDate(d);
            endTx(newEm);
        }
        finally {
            endEm(newEm);
        }

        // sleep a bit so we can ensure that the date doesn't just
        // happen to be the same.
        Thread.sleep(100);

        newEm = (OpenJPAEntityManager) factory.createEntityManager();
        try {
            a = (CacheObjectA) newEm.find(CacheObjectA.class, oid);
            Date d2 = a.getDate();
            if (d == d2) {
                fail("date objects are the same");
            }

            assertEquals(d.getTime(), d2.getTime());
        }
        finally {
            endEm(newEm);
        }
    }

    public void testLocale() throws Exception {
        OpenJPAEntityManager newEm = (OpenJPAEntityManager) factory
            .createEntityManager();
        startTx(newEm);
        CacheObjectA a = (CacheObjectA) newEm.find(CacheObjectA.class, oid);
        Locale l = Locale.getDefault();
        a.setLocale(l);
        endTx(newEm);

        OpenJPAEntityManager newEm2 = (OpenJPAEntityManager) factory
            .createEntityManager();

        a = (CacheObjectA) newEm2.find(CacheObjectA.class, oid);
        Locale l2 = a.getLocale();
        // locales are immutable and final, so the different cached
        // copies should be ==.
        if (l != l2) {
            fail("locale objects are not the same.");
        }

        endEm(newEm);
        endEm(newEm2);
    }

    // ---------- Test query caching ----------
    // * FCOs as params
    // * multi-threaded stuff
    // * access path stuff (see also TestQueryAccessPath)
    // * serializability of returned lists
    // * PM.setQueryCacheEnabled (false);
    // * Query.setQueryCacheEnabled (false);
    // * Pessimistic transactions

    public void testBasicQuery() {
        basicQueries(factory.createEntityManager(), Boolean.FALSE, 3, 1);
        basicQueries(factory.createEntityManager(), Boolean.TRUE, 3, 1);

        // manually notify the cache of changes
        QueryCache cache = cacheManager(factory).getSystemQueryCache();

        // test to see if modifying B causes A's query cache to be flushed
        Set s = new HashSet();
        s.add(CacheObjectB.class);
        cache.onTypesChanged(new TypesChangedEvent(this, s));
        basicQueries(factory.createEntityManager(), Boolean.TRUE, 3, 1);

        // test to see if modifying A causes A's query cache to be flushed
        s.add(CacheObjectA.class);
        cache.onTypesChanged(new TypesChangedEvent(this, s));
        basicQueries(factory.createEntityManager(), Boolean.FALSE, 3, 1);

        // make sure that non-manual notification works
        EntityManager em = factory.createEntityManager();
        try {
            startTx(em);
            CacheObjectA a = new CacheObjectA(ORIG_NAME, ORIG_AGE);
            em.persist(a);
            endTx(em);
        }
        finally {
            endEm(em);
        }

        basicQueries(factory.createEntityManager(), Boolean.FALSE, 4, 2);
    }

    protected void basicQueries(EntityManager em, Boolean inCache, int allSize,
        int origSize) {
        try {
            long start;
            long q1p1;
            long q1p2;
            long q2p1;
            long q2p2;

            Broker broker = JPAFacadeHelper.toBroker(em);
            org.apache.openjpa.kernel.Query q = broker.newQuery(
                JPQLParser.LANG_JPQL, "Select a FROM "
                + CacheObjectA.class.getSimpleName() + " a");
            q.setCandidateExtent(broker.newExtent(CacheObjectA.class, false));
            start = System.currentTimeMillis();
            assertInCache(q, inCache);
            List l = (List) q.execute();
            iterate(l);
            q1p1 = System.currentTimeMillis() - start;

            assertEquals(allSize, l.size());

            start = System.currentTimeMillis();
            List l2 = (List) q.execute();
            iterate(l2);
            q1p2 = System.currentTimeMillis() - start;
            assertEquals(allSize, l2.size());

            q = broker.newQuery(JPQLParser.LANG_JPQL,
                "select a.name,a.age from "
                    + CacheObjectA.class.getSimpleName()
                    + " a where a.name = :n AND a.age = :a");
            q.setCandidateExtent(broker.newExtent(CacheObjectA.class, false));
            start = System.currentTimeMillis();
            assertInCache(q, inCache, new Object[]{ ORIG_NAME,
                new Integer(ORIG_AGE) });
            l = (List) q.execute(new Object[]{ ORIG_NAME,
                new Integer(ORIG_AGE) });
            iterate(l);
            q2p1 = System.currentTimeMillis() - start;

            assertEquals(origSize, l.size());

            start = System.currentTimeMillis();
            l2 = (List) q.execute(new Object[]{ ORIG_NAME,
                new Integer(ORIG_AGE) });
            iterate(l2);
            q2p2 = System.currentTimeMillis() - start;

            assertEquals(origSize, l2.size());
            // System.out.println ("inCache: " + inCache + ";\t q1p1: " + q1p1
            // + ";\t q1p2: " + q1p2 + ";\t q2p1: " + q2p1 + ";\t q2p2: "
            // + q2p2);
        }
        finally {
            endEm(em);
        }
    }

    public void testNonCacheableClass() {
        Broker broker = JPAFacadeHelper.toBrokerFactory(factory).newBroker();
        try {
            org.apache.openjpa.kernel.Query q = broker.newQuery(
                JPQLParser.LANG_JPQL, "Select a FROM "
                + CacheObjectB.class.getSimpleName() + " a");

            Collection c = (Collection) q.execute();
            iterate(c);

            // Query results are no longer dependent on cacheability of an entity.
            assertInCache(q, Boolean.TRUE);
        }
        finally {
            close(broker);
        }
    }

    public void testNonCacheableAccessPath() {
        Broker broker = JPAFacadeHelper.toBrokerFactory(factory).newBroker();
        try {
            org.apache.openjpa.kernel.Query q = broker.newQuery(
                JPQLParser.LANG_JPQL, "Select a FROM "
                + CacheObjectA.class.getSimpleName()
                + " a where a.relatedB.str = 'foo'");
            // "relatedB.str == 'foo'");
            q.setCandidateExtent(broker.newExtent(CacheObjectA.class, false));

            Collection c = (Collection) q.execute();
            iterate(c);

         // Query results are no longer dependent on cacheability of an entity.
            assertInCache(q, Boolean.TRUE);
        }
        finally {
            close(broker);
        }
    }

    public void testNonCacheableSubclasses1() {
        Broker broker = JPAFacadeHelper.toBrokerFactory(factory).newBroker();
        try {
            // a query on the CacheObjectA class includes an uncacheable
            // class; it should therefore not be cacheable.
            org.apache.openjpa.kernel.Query q = broker.newQuery(
                JPQLParser.LANG_JPQL, "Select a FROM "
                + CacheObjectA.class.getSimpleName() + " a");

            Collection c = (Collection) q.execute();
            iterate(c);

            assertInCache(q, Boolean.FALSE);
        }
        finally {
            close(broker);
        }
    }

    public void testNonCacheableSubclasses2() {
        Broker broker = JPAFacadeHelper.toBrokerFactory(factory).newBroker();
        try {
            // a query on the CacheObjectA extent configured without
            // subclasses does not include an uncacheable class; it should
            // therefore be cacheable.
            org.apache.openjpa.kernel.Query q = broker.newQuery(
                JPQLParser.LANG_JPQL, "select a from "
                + CacheObjectA.class.getSimpleName() + " a");
            q.setCandidateExtent(broker.newExtent(CacheObjectA.class, false));

            Collection c = (Collection) q.execute();
            iterate(c);

            assertInCache(q, Boolean.TRUE);
        }
        finally {
            close(broker);
        }
    }

    public void testCacheNames() {
        assertCacheName(CacheObjectA.class, DataCache.NAME_DEFAULT);
        assertCacheName(CacheObjectAChild1.class, DataCache.NAME_DEFAULT);
        assertCacheName(CacheObjectAChild2.class, null);
        assertCacheName(CacheObjectB.class, null);
        /*
         * Due to the problem documented in the setup() routine, the following tests are not valid...
         */
//        assertCacheName(CacheObjectBChild1.class, null);// sub-classes should inherit parent's @Cacheable setting
//        assertCacheName(CacheObjectC.class, "not-the-default-cache"); multiple datacache instantiation not working...
//        assertCacheName(CacheObjectD.class, "not-the-default-cache");
        assertCacheName(CacheObjectE.class, DataCache.NAME_DEFAULT);
        assertCacheName(CacheObjectF.class, DataCache.NAME_DEFAULT);
        assertCacheName(CacheObjectG.class, DataCache.NAME_DEFAULT);
        assertCacheName(CacheObjectH.class, DataCache.NAME_DEFAULT);
        assertCacheName(CacheObjectJ.class, DataCache.NAME_DEFAULT);
        assertCacheName(AppIdCacheObject.class, DataCache.NAME_DEFAULT);
    }

    private void assertCacheName(Class cls, String cacheName) {
        ClassMetaData meta = JPAFacadeHelper.getMetaData(factory, cls);
        if (cacheName == null)
            assertNull(meta.getDataCache());
        else {
            assertNotNull(meta.getDataCache());
            assertEquals(cacheName, meta.getDataCache().getName());
        }
    }

    // FIXME Seetha Sep 26,2006
    // not able to replace pm.newQuery(CacheObjectA.class);
    /*
      * public void testQueryAggregates() { PersistenceManager pm =
      * factory.getPersistenceManager(); try { Query q =
      * pm.newQuery(CacheObjectA.class); q.setResult("max (age)"); Object o =
      * q.execute(); assertTrue("o must be instanceof Number", o instanceof
      * Number); } finally { close(pm); } }
      */

    public void testCache2() {
        OpenJPAEntityManager em1 =
            (OpenJPAEntityManager) factory.createEntityManager();
        OpenJPAEntityManager em2 = null;
        DataCache cache;

        try {
            CacheObjectA a1 = (CacheObjectA) em1.find(CacheObjectA.class, oid);

            em2 = (OpenJPAEntityManager) factory2.createEntityManager();
            CacheObjectA a2 = (CacheObjectA) em2.find(CacheObjectA.class, oid);

            // assert that the oid is in factory2's cache
            //MetaDataRepository repos2 = factory2.getConfiguration()
            //    .getMetaDataRepositoryInstance();
            MetaDataRepository repos2 =
                ((((OpenJPAEntityManagerFactorySPI) factory2))
                .getConfiguration()).getMetaDataRepositoryInstance();
            ClassMetaData meta = repos2
                .getMetaData(CacheObjectA.class, em2.getClassLoader(), true);
            cache = meta.getDataCache();
            assertTrue(cache.contains(oidwithclass));

            // modify the object.
            startTx(em1);
            a1.setName(a1.getName() + " changed");
            endTx(em1);
        }
        finally {
            if (em2 != null)
                endEm(em2);
            endEm(em1);
        }

        // if the cache is a coherent one, then the changes should be
        // seen. Otherwise, they should not.
        if (isCacheCoherent() || factory == factory2)
            assertTrue("key " + oid + " was not in cache; should have been",
                cache.contains(oidwithclass));
        else
            assertFalse("key " + oid + " was in cache; should not have been",
                cache.contains(oidwithclass));
    }

    public void testTimeouts1() throws Exception {
        timeoutsTest1(30);
    }

    public void timeoutsTest1(int tries) throws Exception {
        // this crazy for looping stuff is here because we're seeing
        // intermittent failures with the garbage collector kicking in
        // during testing. So, this decreases the chances that that
        // will happen.
        Exception e = null;
        int i;
        for (i = 0; i < tries; i++) {
            try {
                timeoutsHelper(factory);
                // any successes will pass the test
                return;
            }
            catch (Exception ex) {
                e = ex;
            }
        }

        throw e;
    }

    public void testTimeouts2() throws Exception {
        timeoutsTest2(30);
    }

    public void timeoutsTest2(int tries) throws Exception {
        Error e = null;
        for (int i = 0; i < tries; i++) {
            try {
                timeoutsHelper(timeoutFactory);
                // any successes will pass the test
                return;
            }
            catch (AssertionFailedError afe) {
                e = afe;
            }
        }

        throw e;
    }

    private void timeoutsHelper(OpenJPAEntityManagerFactory factory)
        throws Exception {
        OpenJPAEntityManager em = (OpenJPAEntityManager) factory
            .createEntityManager();
        try {
            startTx(em);

            // get starting time for sleep calculations below
            Date startTime = new Date();
            
            CacheObjectE e = new CacheObjectE("e");
            em.persist(e);

            CacheObjectF f = new CacheObjectF("f");
            em.persist(f);

            CacheObjectG g = new CacheObjectG("g");
            em.persist(g);

            CacheObjectH h = new CacheObjectH("h");
            em.persist(h);
            
            endTx(em);

            // get post-persist time for sleep calculations below
            Date persistTime = new Date();

            Object[] ids = new Object[4];
            ids[0] = new Id(CacheObjectE.class, em.getObjectId(e).toString());
            ids[1] = new Id(CacheObjectF.class, em.getObjectId(f).toString());
            ids[2] = new Id(CacheObjectG.class, em.getObjectId(g).toString());
            ids[3] = new Id(CacheObjectH.class, em.getObjectId(h).toString());

            // build up some queries to test

            // this one should be only on the superclass, since
            // CacheObjectF has a timeout.
            Broker broker = JPAFacadeHelper.toBroker(em);
            org.apache.openjpa.kernel.Query q1 = broker.newQuery(
                JPQLParser.LANG_JPQL, "select a from "
                + CacheObjectE.class.getSimpleName() + " a");
            q1.setCandidateExtent(broker.newExtent(CacheObjectE.class, false));
            iterate((Collection) q1.execute());
            assertInCache(q1, Boolean.TRUE);

            org.apache.openjpa.kernel.Query q2 = broker.newQuery(
                JPQLParser.LANG_JPQL, "Select a FROM "
                + CacheObjectF.class.getSimpleName() + " a");
            iterate((Collection) q2.execute());
            assertInCache(q2, Boolean.TRUE);

            Date currentTime = new Date();
            long diff = (currentTime.getTime() - startTime.getTime());
            long diff2 = (currentTime.getTime() - persistTime.getTime());
            long sleep = 0;
            
            getLog().info("CacheTest.timeoutsHelper() testing all are still in the cache, elapsed time="+diff);
            DataCache cache = cacheManager(factory).getDataCache(
                DataCache.NAME_DEFAULT, false);
            diff = Math.max(diff, diff2);
            if (diff < 450) {
                // all should still be in the cache
                checkCache(cache, ids, new boolean[]{ true, true, true, true });
            } else {
                // need to skip the test on slow systems or when using remote DB connections
                getLog().warn("CacheTest.timeoutsHelper() skipping checkCache(all, <500) because diff=" +
                    diff + " and diff2=" + diff2);
            }
            
            // should cause h to be dropped (timeout=500)
            currentTime = new Date();
            diff = (currentTime.getTime() - startTime.getTime());
            diff2 = (currentTime.getTime() - persistTime.getTime());
            sleep = Math.min((800 - diff), (800 - diff2));
            if (sleep < 0) {
                // we already missed the window
                getLog().warn("CacheTest.timeoutsHelper() skipping sleep for checkCache(h=500) because sleep="+sleep);
            } else if (sleep > 10) {
                getLog().info("CacheTest.timeoutsHelper() testing h to be dropped by waiting sleep="+sleep);
                Thread.currentThread().sleep(700);
                Thread.yield();
            } else {
                sleep = 0;
            }
            // recalc diff again
            currentTime = new Date();
            diff = (currentTime.getTime() - startTime.getTime());
            diff2 = (currentTime.getTime() - persistTime.getTime());
            diff = Math.max(diff, diff2);
            if (diff > 600 && diff < 900) {
                // only h should be dropped
                checkCache(cache, ids, new boolean[]{ true, true, true, false });
            } else {
                // need to skip the test on slow systems or when using remote DB connections
                getLog().warn("CacheTest.timeoutsHelper() skipping checkCache(h=500) because diff=" + diff +
                    " and diff2=" + diff2);
            }

            // if this run has a default timeout (set to 1 sec in the test
            // case), e should be timed out by this point.
            //boolean eStatus =
            //    !(factory.getConfiguration().getDataCacheTimeout() > 0);
            boolean eStatus = !((((OpenJPAEntityManagerFactorySPI) factory)
                    .getConfiguration()).getDataCacheTimeout() > 0);

            // should cause f to be dropped (timeout=1000)
            currentTime = new Date();
            diff = currentTime.getTime() - startTime.getTime();
            diff2 = (currentTime.getTime() - persistTime.getTime());
            diff = Math.max(diff, diff2);
            sleep = 2000 - diff;
            if (sleep < 0) {
                // we already missed the window
                getLog().warn("CacheTest.timeoutsHelper() skipping sleep for checkCache(f=000) because sleep="+sleep);
            } else if (sleep > 10) {
                getLog().info("CacheTest.timeoutsHelper() testing f to be dropped by waiting sleep="+sleep);
                Thread.currentThread().sleep(sleep);
                Thread.yield();
            } else {
                sleep = 0;
            }
            // recalc diff again
            currentTime = new Date();
            diff = currentTime.getTime() - startTime.getTime();
            diff2 = currentTime.getTime() - persistTime.getTime();
            diff = Math.max(diff, diff2);
            if (diff < 4900) {
                // e is conditional, h and f should be dropped, but not g yet
                checkCache(cache, ids, new boolean[]{ eStatus, false, true, false });
            } else {
                // need to skip the test on slow systems or when using remote DB connections
                getLog().warn("CacheTest.timeoutsHelper() skipping checkCache(f=1000) because diff="+diff);
            }

            // at this point, q2 should be dropped (because its candidate
            // class is CacheObjectF), and q1 might be dropped, depending
            // on whether or not we've got a timeout configured.
            assertInCache(q1, (eStatus) ? Boolean.TRUE : Boolean.FALSE);
            assertInCache(q2, Boolean.FALSE);

            // should cause g to be dropped (timeout=5000)
            currentTime = new Date();
            diff = currentTime.getTime() - startTime.getTime();
            diff2 = currentTime.getTime() - persistTime.getTime();
            diff = Math.max(diff, diff2);
            sleep = 6000 - diff;
            if (sleep > 0) {
                getLog().info("CacheTest.timeoutsHelper() testing g to be dropped by waiting sleep="+sleep);
                Thread.currentThread().sleep(sleep);
                Thread.yield();
            }
            // all of them should be dropped now, since diff > 5000
            checkCache(cache, ids, new boolean[]{ eStatus, false, false, false });
        }
        finally {
            endEm(em);
        }
    }

    public void testQueryTimeouts() throws Exception {
        queryTimeoutsHelper(factory);
        queryTimeoutsHelper(timeoutFactory);
    }

    private void queryTimeoutsHelper(OpenJPAEntityManagerFactory factory)
        throws Exception {
        OpenJPAEntityManager em = (OpenJPAEntityManager) factory
            .createEntityManager();
        try {
            startTx(em);

            // get starting time for sleep calculations below
            Date startTime = new Date();

            CacheObjectE e = new CacheObjectE("e");
            em.persist(e);

            CacheObjectF f = new CacheObjectF("f");
            em.persist(f);

            endTx(em);

            // build up some queries to test
            Broker broker = JPAFacadeHelper.toBroker(em);
            org.apache.openjpa.kernel.Query q1 = broker
                .newQuery(JPQLParser.LANG_JPQL, "SELECT a FROM CacheObjectE a");

            q1.setCandidateExtent(broker.newExtent(CacheObjectE.class, false));
            iterate((Collection) q1.execute());
            assertInCache(q1, Boolean.TRUE);

            org.apache.openjpa.kernel.Query q2 = broker
                .newQuery(JPQLParser.LANG_JPQL, "SELECT a FROM CacheObjectF a");
            iterate((Collection) q2.execute());
            assertInCache(q2, Boolean.TRUE);

            // if this run has a default timeout (set to 1 sec in the test
            // case), e should be timed out by this point.
            //boolean eTimedOut =
            //    factory.getConfiguration().getDataCacheTimeout() > 0;
            boolean eTimedOut =
                ((((OpenJPAEntityManagerFactorySPI) factory).getConfiguration())
                    .getDataCacheTimeout() > 0);

            // should cause f to be dropped.
            Date currentTime = new Date();
            long diff = currentTime.getTime() - startTime.getTime();
            long sleep = 2000 - diff;
            if (sleep > 0) {
                getLog().trace("CacheTest.queryTimeoutsHelper() testing f to be dropped by waiting sleep="+sleep);
                Thread.currentThread().sleep(sleep);
                Thread.yield();
            }

            // at this point, q2 should be dropped (because its candidate
            // class is CacheObjectF), and q1 might be dropped, depending
            // on whether or not we've got a timeout configured.
            assertInCache(q1, (eTimedOut) ? Boolean.FALSE : Boolean.TRUE);
            assertInCache(q2, Boolean.FALSE);
        }
        finally {
            endEm(em);
        }
    }

    public void testQueryImplicitEvictions() throws Exception {
        OpenJPAEntityManager em = (OpenJPAEntityManager) factory
            .createEntityManager();
        try {
            RuntimeTest1[] helperObjs = new RuntimeTest1[5];
            helperObjs[0] = new RuntimeTest1();
            helperObjs[1] = new RuntimeTest1();
            helperObjs[2] = new RuntimeTest1();
            helperObjs[3] = new RuntimeTest1();
            helperObjs[4] = new RuntimeTest1();
            startTx(em);
            em.persist(helperObjs[0]);
            em.persist(helperObjs[1]);
            em.persist(helperObjs[2]);
            em.persist(helperObjs[3]);
            em.persist(helperObjs[4]);
            endTx(em);

            DataCache cache = cacheManager(factory).getDataCache(
                DataCache.NAME_DEFAULT, false);

            if (!isOpenJPACache(cache)) {
                bug(627, "Tangosol cache impl needs modernization");
                return;
            }

            if (cache instanceof DelegatingDataCache)
                cache = ((DelegatingDataCache) cache).getInnermostDelegate();
            if (cache instanceof ConcurrentDataCache) {
                CacheMap map = ((ConcurrentDataCache) cache).getCacheMap();
                map.setCacheSize(3);
                map.setSoftReferenceSize(0);
            } 

            startTx(em);
            CacheObjectH h = new CacheObjectH("h");
            em.persist(h);
            CacheObjectJ j = new CacheObjectJ("j", h);
            em.persist(j);
            endTx(em);
            Object hoid = em.getObjectId(h);
            Object joid = em.getObjectId(j);

            Object hoidwithclass = new Id(CacheObjectH.class, hoid.toString());
            Object joidwithclass = new Id(CacheObjectJ.class, joid.toString());
            endEm(em);

            // make sure j and h are in cache; may not be if not LRU
            int attempts = 0;
            for (; attempts < 100 && !cache.contains(joidwithclass); attempts++)
            {
                em = factory.createEntityManager();
                if (!cache.contains(hoidwithclass))
                    em.find(CacheObjectH.class, hoid);
                if (!cache.contains(joidwithclass))
                    em.find(CacheObjectJ.class, joid);
                endEm(em);
            }
            assertTrue("Could not get queried objects into cache",
                attempts < 100);

            // build up a query that uses H in its access path...
            em = factory.createEntityManager();
            Broker broker = JPAFacadeHelper.toBroker(em);
            org.apache.openjpa.kernel.Query q = broker.newQuery(
                JPQLParser.LANG_JPQL, "Select a FROM "
                + CacheObjectJ.class.getSimpleName()
                + " a where a.str = 'h'");
            iterate((Collection) q.execute());
            assertInCache(q, Boolean.TRUE);
            endEm(em);

            // force h out of the cache. we might have to try multiple times
            // if the cache is not LRU
            attempts = 0;
            for (; attempts < 100 && cache.contains(joidwithclass); attempts++)
            {
                em = factory.createEntityManager();
                for (int i = 0; i < 5; i++)
                    em.find(RuntimeTest1.class, em.getObjectId(helperObjs[i]));
                endEm(em);
            }
            assertTrue("Could not kick queried objects out of cache",
                attempts < 100);
            
            /*
             * Not a valid test...  At least not with the current implementation...
             * 
             * Just removing items from the DataCache (as done via the previous loop) is not sufficient
             * to remove the entries from the QueryCache.  Currently, this notification is done at the end
             * of a transaction after inserts, updates, and deletes have been performed.  Then, the 
             * updateCaches() method is invoked on the DataCacheStoreManager which will flow the request to
             * the QueryCache.  With no direct updates to the "Entities of interest", then there's nothing to
             * flow over to the QueryCache for cleanup.  Even putting the above loop within a transaction is
             * not sufficient, since there have been no updates to the "Entities of interest".
             */
//            em = factory.createEntityManager();
//            broker = JPAFacadeHelper.toBroker(em);
//            q = broker.newQuery(JPQLParser.LANG_JPQL, "Select a FROM "
//                + CacheObjectJ.class.getSimpleName()
//                + " a where a.str = 'h'");
//            try {
//                assertInCache(q, null);
//            }
//            catch (AssertionFailedError e) {
//                bug(626, "query cache invalidation is broken");
//            }

            // ### should test remote events causing queries to evict.
        }
        finally {
            endEm(em);
        }
    }

    // FIXME SEetha Sep 26,2006
    // not able to replace pm.newQuery(CacheObjectE.class);
    /*
      * public void testAllegedQueryOrderingChanges() throws Exception { //
      * inspired by tsc 3013. pcl: I have not been able to get this // test case
      * to actually fail. However, during analysis of // 3013's stack traces, I
      * discovered that the // QueryKey.equals() method did not deal with the
      * ordering // field correctly, possibly causing the problem.
      *
      * OpenJPAEntityManager em = (OpenJPAEntityManager)
      * factory.createEntityManager(); try { startTx(em,
      * ()); CacheObjectE e = new CacheObjectE("e"); em.persist(e);
      * endTx(em); } finally {
      * endEm(em); }
      *
      * em = factory.createEntityManager(); Query q; Collection c; List l; try {
      * q = em.createQuery(CacheObjectE.class); q.setOrdering("str ascending");
      * c = (Collection) q.execute(); l = new LinkedList(c);
      * assertEquals(1, c.size()); } finally { endEm(em); }
      *
      * em = factory.createEntityManager(); try { q =
      * em.createQuery(CacheObjectE.class); q.setOrdering("str ascending"); c =
      * (Collection) q.execute(); l = new LinkedList(c); assertEquals(1,
      * c.size()); } finally { endEm(em); }
      *
      * try { em = factory.createEntityManager(); q =
      * em.createQuery(CacheObjectE.class); q.setOrdering("str descending"); c =
      * (Collection) q.execute(); assertEquals(1, c.size()); l = new
      * LinkedList(c); } finally { endEm(em); } }
      */

    public void testAllegedConcurrentModificationException() throws Exception {
        OpenJPAEntityManager em = (OpenJPAEntityManager) factory
            .createEntityManager();
        CacheObjectE e;
        try {
            ClassMetaData meta = JPAFacadeHelper.getMetaData(em,
                CacheObjectE.class);
            if (!isOpenJPACache(meta.getDataCache()))
                return;

            startTx(em);
            e = new CacheObjectE("e");
            em.persist(e);
            endTx(em);
        }
        finally {
            endEm(em);
        }

        em = factory.createEntityManager();
        try {
            startTx(em);

            // find the new object...
            OpenJPAQuery q = em.createQuery("select a FROM "
                + CacheObjectE.class.getSimpleName()
                + " a where a.str = 'e'");
            e = (CacheObjectE) ((Collection) q.getResultList()).iterator()
                .next();

            // ... and modify the changed object.
            e.setStr("e2");
            e.setStr("e3");
            endTx(em);
        }
        finally {
            endEm(em);
        }
    }

    private boolean isOpenJPACache(DataCache cache) {
        if (cache instanceof DelegatingDataCache)
            cache = ((DelegatingDataCache) cache).getInnermostDelegate();

        return cache instanceof ConcurrentDataCache;
    }

    // ---------- utility methods ----------

    private void checkCache(DataCache cache, Object[] ids, boolean[] stati) {
        CacheTestHelper.checkCache(this, cache, ids, stati);
    }

    private void assertInCache(org.apache.openjpa.kernel.Query q,
        Boolean inCache) {
        CacheTestHelper.assertInCache(this, q, inCache);
    }

    private void assertInCache(org.apache.openjpa.kernel.Query q,
        Boolean inCache, Object[] args) {
        CacheTestHelper.assertInCache(this, q, inCache, args);
    }

    private void iterate(Collection c) {
        CacheTestHelper.iterate(c);
    }

    public void testInterface() throws Exception {
        OpenJPAEntityManager newEm =
            (OpenJPAEntityManager) factory.createEntityManager();
        startTx(newEm);
        CacheObjectA a = (CacheObjectA) newEm.find(CacheObjectA.class, oid);
        CacheObjectC c = new CacheObjectC("blah");
        a.setRelatedInterface(c);
        endTx(newEm);
        Object cId = newEm.getObjectId(c);
        endEm(newEm);

        newEm = (OpenJPAEntityManager) factory.createEntityManager();
        a = (CacheObjectA) newEm.find(CacheObjectA.class, oid);

        CacheObjectInterface c2 = a.getRelatedInterface();
        assertNotNull(c2);

        assertEquals(cId, newEm.getObjectId(c2));
    }

    public void testQueriesOnCollectionsDontUseCache() {
        OpenJPAEntityManager em = (OpenJPAEntityManager) factory
            .createEntityManager();
        try {
            startTx(em);
            CacheObjectE e = new CacheObjectE("e");
            em.persist(e);
            endTx(em);
        }
        finally {
            endEm(em);
        }

        em = (OpenJPAEntityManager) factory.createEntityManager();
        OpenJPAQuery q;
        Collection c;
        try {
            q = em.createQuery("select a FROM "
                + CacheObjectE.class.getSimpleName()
                + " a where a.str = 'e'");
            c = new ArrayList((Collection) q.getResultList());
            assertEquals(1, c.size());
            q.closeAll();
        }
        finally {
            endEm(em);
        }

        try {
            em = (OpenJPAEntityManager) factory.createEntityManager();
            q = em.createQuery("select a FROM "
                + CacheObjectE.class.getSimpleName()
                + " a where a.str = 'e'");
            q.setCandidateCollection(new ArrayList(0));
            c = (Collection) q.getResultList();
            assertEquals(0, c.size());
            q.closeAll();
        }
        finally {
            endEm(em);
        }
    }

    public void testDFGFieldsLoaded1() {
        dfgFieldsLoadedHelper(false);
    }

    public void testDFGFieldsLoaded2() {
        dfgFieldsLoadedHelper(true);
    }

    public void dfgFieldsLoadedHelper(boolean related) {
        OpenJPAEntityManager em = (OpenJPAEntityManager) factory
            .createEntityManager();
        startTx(em);
        OpenJPAQuery q;
        Collection c;
        try {

            q = em.createQuery(
                "select a FROM " + CacheObjectA.class.getSimpleName()
                    + " a where a.name = :pName").setParameter("pName",
                ORIG_NAME);

            c = new ArrayList((Collection) q.getResultList());
            assertEquals(1, c.size());
            CacheObjectA a = (CacheObjectA) c.iterator().next();
            if (related)
                a.getRelatedArray();
            em.detach(a);
            assertEquals(ORIG_NAME, a.getName());
            q.closeAll();
        }
        finally {
            rollbackTx(em);
            endEm(em);
        }
    }

    // FIXME Seetha Sep 26,2006
    /*
      * public void testQueriesAfterModificationAreNotInCache() {
      * OpenJPAEntityManager em = (OpenJPAEntityManager)
      * factory.createEntityManager(); OpenJPAEntityManager em2 =
      * (OpenJPAEntityManager) factory.createEntityManager();
      *
      * //FIXME Seetha Sep 26,2006 //em.setIgnoreCache(false);
      * //em2.setIgnoreCache(false); ((FetchPlan) em.getFetchPlan()).
      * setFlushBeforeQueries(FetchPlan.FLUSH_TRUE); ((FetchPlan)
      * em2.getFetchPlan()). setFlushBeforeQueries(FetchPlan.FLUSH_TRUE);
      *
      * try { startTx(em); CacheObjectE e = new
      * CacheObjectE("e"); em.persist(e); endTx(em,
      * ());
      *
      * startTx(em);
      *  // first, a query that should get into the cache. Broker broker =
      * JPAFacadeHelper.toBroker(em); org.apache.openjpa.kernel.Query q =
      * broker.newQuery(JPQLParser.LANG_JPQL, CacheObjectE.class, "str ==
      * \"e\""); Collection c = (Collection) q.execute(); for (Iterator iter =
      * c.iterator(); iter.hasNext();) iter.next();
      *
      * assertEquals(1, c.size()); assertInCache(q, Boolean.TRUE);
      *
      * Broker broker2 = JPAFacadeHelper.toBroker(em2);
      * org.apache.openjpa.kernel.Query q2 = broker2.newQuery(q.getLanguage(),
      * q);
      *  // make some modifications and look again. Should return //two results.
      * e = new CacheObjectE("e"); em.persist(e);
      *
      * q = broker.newQuery(JPQLParser.LANG_JPQL, CacheObjectE.class, "str ==
      * \"e\""); c = (Collection) q.execute(); assertEquals(2, c.size()); for
      * (Iterator iter = c.iterator(); iter.hasNext();) iter.next();
      *  // original query should still be in cache assertInCache(q2,
      * Boolean.TRUE);
      *
      * Collection c2 = (Collection) q2.execute(); assertEquals(1, c2.size());
      *  // new query should not make it into cache
      *
      * q = broker .newQuery(JPQLParser.LANG_JPQL, CacheObjectE.class, null);
      * c = (Collection) q.execute(); assertEquals(2, c.size());
      * for (Iterator iter = c.iterator(); iter.hasNext();) iter.next();
      *
      * assertInCache(q, Boolean.FALSE); } finally {
      * rollbackTx(em);
      * endEm(em);
      * endEm(em2); } }
      */

    public void testCachedQueryClosureReleasesResources() {
        // PersistenceManagerFactory factory =
        // KodoHelper.createEntityManagerFactory ();
        EntityManager initEm = factory.createEntityManager();
        startTx(initEm);
        CacheObjectE e = new CacheObjectE("e");
        initEm.persist(e);
        endTx(initEm);
        endEm(initEm);

        Broker broker = JPAFacadeHelper.toBrokerFactory(factory).newBroker();
        org.apache.openjpa.kernel.Query q = broker.newQuery(
            JPQLParser.LANG_JPQL, "Select a FROM "
            + CacheObjectE.class.getSimpleName()
            + " a where a.str = 'e'");
        Collection c = (Collection) q.execute();
        for (Iterator iter = c.iterator(); iter.hasNext();)
            iter.next();

        assertEquals(1, c.size());
        assertInCache(q, Boolean.TRUE);

        ImplHelper.close(c);

        broker.close();
    }

    public void testMutableSCOsAreConverted() {
        OpenJPAEntityManager em0 = (OpenJPAEntityManager) factory
            .createEntityManager();
        OpenJPAEntityManager em1 = (OpenJPAEntityManager) factory
            .createEntityManager();

        startTx(em0);
        CacheObjectA a = (CacheObjectA) em0.find(CacheObjectA.class, oid);

        Date d = new Date();
        a.setDate(d);

        endTx(em0);
        DataCache cache = cacheManager(factory).getDataCache(
            DataCache.NAME_DEFAULT, false);
        assertTrue(cache.contains(oidwithclass));
        cache.remove(oidwithclass);

        a = (CacheObjectA) em1.find(CacheObjectA.class, oid);
        assertTrue(cache.contains(oidwithclass));

        try {
            PCData data = cache.get(oidwithclass);
            ClassMetaData meta =
                ((OpenJPAEntityManagerFactorySPI) OpenJPAPersistence
                    .cast(factory)).getConfiguration()
                    .getMetaDataRepositoryInstance().getMetaData(a.getClass(),
                    null, false);
            FieldMetaData fmd = meta.getField("date");
            d = (Date) data.getData(fmd.getIndex());
            Broker broker = JPAFacadeHelper.toBroker(em1);
            OpenJPAStateManager sm = broker.getStateManager(a);
            assertTrue(sm == ((ProxyDate) a.getDate()).getOwner());
            assertEquals(Date.class, d.getClass());
        }
        finally {
            endEm(em0);
            endEm(em1);
        }
    }

    public void testEmptyResultsAreCached() {
        Broker broker = JPAFacadeHelper.toBrokerFactory(factory).newBroker();
        org.apache.openjpa.kernel.Query q = broker.newQuery(
            JPQLParser.LANG_JPQL, "Select a FROM "
            + CacheObjectAChild1.class.getSimpleName()
            + " a where a.name = 'testEmptyResultsAreCached'");
        Collection c = (Collection) q.execute();
        assertEquals(0, c.size());
        assertInCache(q, Boolean.TRUE);
        broker.close();
    }

    private void doassertTrue(EntityManager em, String name, int age)
        throws Exception {
        CacheObjectA a = (CacheObjectA) em.find(CacheObjectA.class, oid);
        assertTrue(name.equals(a.getName()));
        assertTrue(a.getAge() == age);
        endEm(em);
    }

    private void assertNew(CacheObjectA a) {
        assertTrue(NEW_NAME.equals(a.getName()));
        assertTrue(ORIG_AGE == a.getAge());
    }

    private void assertOld(CacheObjectA a) {
        assertTrue(ORIG_NAME.equals(a.getName()));
        assertTrue(ORIG_AGE == a.getAge());
    }

    private DataCacheManager cacheManager(OpenJPAEntityManagerFactory factory) {
        return CacheTestHelper
            .cacheManager(JPAFacadeHelper.toBrokerFactory(factory));
    }

    private void close(EntityManager em) {
        rollbackTx(em);
        endEm(em);
    }

    private void close(Broker broker) {
        if (broker.isActive())
            broker.rollback();
        broker.close();
    }

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < args.length; i++) {
            String type = args[i];

            CacheTest c;
            if (type.equals("tcp")) {
                c = new DistributedCacheTest("time test",
                    ConcurrentDataCache.class);
            } else if (type.equals("jms")) {
                c = new DistributedCacheTest("time test",
                    ConcurrentDataCache.class);
            } else {
                c = new TestLocalCache("time test");
            }

            c.setUp();
            long start = System.currentTimeMillis();
            int count = 1000;
            for (int j = 0; j < count; j++) {
                c.doassertTrue(c.factory.createEntityManager(), NEW_NAME,
                    ORIG_AGE);
            }
            System.out.println(count + " iterations in "
                + (System.currentTimeMillis() - start) + " millis");
            c.tearDown();
        }
    }
}

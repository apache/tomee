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

import java.util.List;

import javax.persistence.Cache;
import javax.persistence.CacheRetrieveMode;
import javax.persistence.CacheStoreMode;
import javax.persistence.EntityManager;

import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.OpenJPAEntityManagerSPI;
import org.apache.openjpa.persistence.cache.jpa.model.CacheEntity;
import org.apache.openjpa.persistence.cache.jpa.model.CacheableEntity;
import org.apache.openjpa.persistence.cache.jpa.model.NegatedCachableEntity;
import org.apache.openjpa.persistence.cache.jpa.model.NegatedUncacheableEntity;
import org.apache.openjpa.persistence.cache.jpa.model.UncacheableEntity;
import org.apache.openjpa.persistence.cache.jpa.model.UnspecifiedEntity;
import org.apache.openjpa.persistence.cache.jpa.model.XmlCacheableEntity;
import org.apache.openjpa.persistence.cache.jpa.model.XmlUncacheableEntity;

public abstract class AbstractCacheModeTestCase extends AbstractCacheTestCase {
    public abstract OpenJPAEntityManagerFactorySPI getEntityManagerFactory();

    public abstract List<String> getSql();

    protected abstract Class<?>[] getExpectedNotInCache();

    protected abstract Class<?>[] getExpectedInCache();

    // =======================================================================
    // Asserts
    // =======================================================================
    /**
     * Assert whether the cache contains the expected results.
     * 
     * @param cache
     *            The JPA Cache to verify
     * @param expectCacheables
     *            Whether entities with @Cacheable(true) should be in the cache
     *            (almost always true)
     * @param expectUncacheables
     *            Whether entities with @Cacheable(false) should be in the cache
     *            (almost always false)
     * @param expectUnspecified
     *            Whether entities with no @Cacheable annotation should be in
     *            the cache (varies per config).
     */
    protected void assertCacheContents(Cache cache, boolean expectCacheables, boolean expectUncacheables,
        boolean expectUnspecified) {
        assertCacheables(cache, expectCacheables);
        assertUncacheables(cache, expectUncacheables);
        assertUnspecified(cache, expectUnspecified);
    }

    /**
     * Assert whether the cacheable types are in the cache. This method exits on
     * the first cache 'miss'.
     * 
     * @param cache
     *            JPA Cache to verify
     * @param expected
     *            If true the cacheable types should be in the cache, if false
     *            they should not be.
     */
    protected void assertCacheables(Cache cache, boolean expected) {
        assertCached(cache, CacheableEntity.class, 1, expected);
        assertCached(cache, NegatedUncacheableEntity.class, 1, expected);
        assertCached(cache, XmlCacheableEntity.class, 1, expected);
    }

    /**
     * Assert whether the uncacheable types are in the cache. This method exits
     * on the first cache 'miss'.
     * 
     * @param cache
     *            JPA Cache to verify
     * @param expected
     *            If true the uncacheable types should be in the cache, if false
     *            they should not be.
     */
    protected void assertUncacheables(Cache cache, boolean expected) {
        assertCached(cache, UncacheableEntity.class, 1, expected);
        assertCached(cache, XmlUncacheableEntity.class, 1, expected);
        assertCached(cache, NegatedCachableEntity.class, 1, expected);
    }

    /**
     * Assert whether the unspecified types are in the cache. This method exits
     * on the first cache 'miss'.
     * 
     * @param cache
     *            JPA Cache to verify
     * @param expected
     *            If true the unspecified types should be in the cache, if false
     *            they should not be.
     */
    protected void assertUnspecified(Cache cache, boolean expected) {
        assertCached(cache, UnspecifiedEntity.class, 1, expected);
    }

    /**
     * Assert that no sql is executed when running the supplied Action.
     * 
     * @param act
     *            Action to execute.
     */
    public void assertNoSql(Action act) {
        assertSqlInc(act, 0);
    }

    /**
     * Assert that <literal>expectedSqls</literal> SQL statements are executed
     * when running <literal>act</literal>
     * 
     * @param act
     *            Action to run.
     * @param expectedSqls
     *            Number of SQL statements that should be executed.
     */
    public void assertSqlInc(Action act, int expectedSqls) {
        int before = getSql().size();
        act.run();
        assertEquals(before + expectedSqls, getSql().size());
    }

    // =======================================================================
    // Utility classes
    // =======================================================================
    /**
     * Basic 'runnable' interface used to run a set of commands, then analyze
     * the number of SQL statements that result.
     */
    public interface Action {
        public void run();
    }

    // =======================================================================
    // Test utilities
    // =======================================================================
    public boolean getCacheEnabled() {
        return true;
    }

    // =======================================================================
    // Common test methods.
    // =======================================================================
    /**
     * Ensure that each call the em.find generates an SQL statement when
     * CacheRetrieveMode.BYPASS is used.
     */
    public void testReadModeByass() {
        assertSqlInc(new Action() {
            public void run() {
                EntityManager em = getEntityManagerFactory().createEntityManager();
                em.setProperty(RETRIEVE_MODE_PROP, CacheRetrieveMode.BYPASS);
                for (Class<?> cls : persistentTypes) {
                    em.find(cls, 1);
                }
                em.close();
            }
        }, persistentTypes.length);
    }

    /**
     * <p>
     * Ensure that each entity in getExpectedInCache():
     * <ul>
     * <li>is in the cache</li>
     * <li>does not go to the database for a find operation</li>
     * <li>is not null</li>
     * </ul>
     * </p>
     * <p>
     * and
     * </p>
     * <p>
     * Ensure that each entity in getExpectedNotInCache() :
     * <ul>
     * <li>is not in the cache</li>
     * <li>results in a single SQL statement when em.find() is called</li>
     * <li>is not null</li>
     * </ul>
     * </p>
     * 
     */
    public void testRetrieveModeUse() {
        if (getCacheEnabled()) {
            assertNoSql(new Action() {
                public void run() {
                    EntityManager em = getEntityManagerFactory().createEntityManager();
                    em.setProperty(RETRIEVE_MODE_PROP, CacheRetrieveMode.USE);
                    for (Class<?> cls : getExpectedInCache()) {
                        assertCached(getEntityManagerFactory().getCache(), cls, 1, true);
                        assertNotNull(em.find(cls, 1));
                    }
                    em.close();
                }
            });
            assertSqlInc(new Action() {
                public void run() {
                    EntityManager em = getEntityManagerFactory().createEntityManager();
                    em.setProperty(RETRIEVE_MODE_PROP, CacheRetrieveMode.USE);
                    for (Class<?> cls : getExpectedNotInCache()) {
                        assertCached(getEntityManagerFactory().getCache(), cls, 1, false);
                        assertNotNull(em.find(cls, 1));
                    }
                    em.close();
                }
            }, getExpectedNotInCache().length);
        }
    }

    public void updateAndFind(Class<? extends CacheEntity> classToUpdate, int idToUpdate,
            Class<? extends CacheEntity> classToFind, int idToFind,
            CacheStoreMode storeMode, CacheRetrieveMode retrieveMode) {
        EntityManager em = getEntityManagerFactory().createEntityManager();

        if (storeMode != null) {
            em.setProperty(STORE_MODE_PROP, storeMode);
        }
        if (retrieveMode != null) {
            em.setProperty(RETRIEVE_MODE_PROP, retrieveMode);
        }

        em.getTransaction().begin();
        CacheEntity ce1 = em.find(classToUpdate, idToUpdate);
        CacheEntity ce2 = em.find(classToFind, idToFind);
        assertNotNull(ce1);
        assertNotNull(ce2);
        ce1.setName(ce1.getName() + "UPD");
        em.getTransaction().commit();
        em.close();
    }

    /**
     * <p>
     * Test logic to validate different CacheStoreModes. It should behave
     * identically for all shared-cache-modes except NONE which never caches
     * anything.
     * </p>
     * <p>
     * This method only tests setting the store mode on the EntityManager
     * itself.
     * </p>
     * <p>
     * The first transaction updates CacheableEntity::1 with CacheStoreMode
     * tran1StoreMode, calls find for CacheableEntity::1 and
     * XmlCacheableEntity::1. This will never trigger a cache refresh since the
     * data is up to date - but it could trigger additional SQL
     * </p>
     * <p>
     * The second transaction updates XmlCacheableEntity::1 with CacheStoreMode
     * tran2StoreMode, calls find for CacheableEntity::1 and
     * XmlCacheableEntity::1. In this case if tran2StoreMode ==
     * CacheStoreMode.REFRESH we may update the cache with the state of
     * CacheableEntity::1.
     * </p>
     * 
     * @param tran1StoreMode
     *            CacheStoreMode to use in transaction 1.
     * @param tran2StoreMode
     *            cacheStoreMode to use in transaction 2.
     * @param cacheUpdatedForTran1
     *            Whether the cache will contain an updated version of
     *            CacheableEntity::1
     * @param cacheUpdatedForTran2
     *            Whether the cache will contain an updated version of
     *            XmlCacheableEntity::1
     * @param version
     *            Expected starting version of for both entities
     */
    public void entityManagerStoreModeTest(CacheStoreMode tran1StoreMode, CacheStoreMode tran2StoreMode,
        boolean cacheUpdatedForTran1, boolean cacheUpdatedForTran2, int version) {
        updateAndFind(CacheableEntity.class, 1, XmlCacheableEntity.class, 1, tran1StoreMode, null);
        updateAndFind(XmlCacheableEntity.class, 1, CacheableEntity.class, 1, tran2StoreMode, null);

        // get entities from the cache and ensure their versions are as
        // expected.
        EntityManager em = getEntityManagerFactory().createEntityManager();
        em = getEntityManagerFactory().createEntityManager();
        CacheableEntity ceFromEM = em.find(CacheableEntity.class, 1);
        XmlCacheableEntity xceFromEM = em.find(XmlCacheableEntity.class, 1);
        em.close();
        assertEquals(cacheUpdatedForTran1 ? version + 1 : version, ceFromEM.getVersion());
        assertEquals(cacheUpdatedForTran2 ? version + 1 : version, xceFromEM.getVersion());

        // get the data from the database. Version should always have been
        // updated in this case.
        em = getEntityManagerFactory().createEntityManager();
        em.setProperty(RETRIEVE_MODE_PROP, CacheRetrieveMode.BYPASS);
        CacheableEntity ceFromDB =
            (CacheableEntity) em.createNativeQuery("Select * from CacheableEntity where ID = 1", CacheableEntity.class)
                .getSingleResult();

        XmlCacheableEntity xceFromDB =
            (XmlCacheableEntity) em.createNativeQuery("Select * from XmlCacheableEntity where ID = 1",
                XmlCacheableEntity.class).getSingleResult();

        assertEquals(version + 1, ceFromDB.getVersion());
        assertEquals(version + 1, xceFromDB.getVersion());
        em.close();
    }

    /**
     * Execute the defaultStoreModeTest with
     */
    public void testStoreModeUseBypass() throws Exception {
        if (getCacheEnabled()) {
            entityManagerStoreModeTest(CacheStoreMode.USE, CacheStoreMode.BYPASS, true, false, 1);
        }
    }

    public void testStoreModeUseUse() {
        if (getCacheEnabled()) {
            entityManagerStoreModeTest(CacheStoreMode.USE, CacheStoreMode.USE, true, true, 1);
        }
    }
    
    public void testRefresh() {
        if (getCacheEnabled()) {
            OpenJPAEntityManagerSPI em = getEntityManagerFactory().createEntityManager();
            CacheableEntity e1 = em.find(CacheableEntity.class, 1);
            XmlCacheableEntity e2 = em.find(XmlCacheableEntity.class, 1);
            assertNotNull(e1);
            assertNotNull(e2);
            int e1Version = e1.getVersion();
            int e2Version = e2.getVersion();

            String e1Sql = "UPDATE CacheableEntity SET VERSN=?1 WHERE ID=?2";
            String e2Sql = "UPDATE XmlCacheableEntity SET VERSN=?1 WHERE ID=?2";
            em.getTransaction().begin();
            assertEquals(1, em.createNativeQuery(e1Sql).setParameter(1, e1Version + 1).setParameter(2, e1.getId())
                .executeUpdate());
            assertEquals(1, em.createNativeQuery(e2Sql).setParameter(1, e2Version + 1).setParameter(2, e2.getId())
                .executeUpdate());
            em.getTransaction().commit();
            em.refresh(e1);
            em.refresh(e2);
            assertEquals(e1Version + 1, e1.getVersion());
            assertEquals(e2Version + 1, e2.getVersion());
            em.close();
        }
    }

    public void testStoreModeUseRefresh() {
        if (getCacheEnabled()) {
            entityManagerStoreModeTest(CacheStoreMode.USE, CacheStoreMode.REFRESH, true, true, 1);
        }
    }

    public void entityManagerStoreModeTest() {
        if (getCacheEnabled()) {
            entityManagerStoreModeTest(CacheStoreMode.BYPASS, CacheStoreMode.BYPASS, false, false, 1);
        }
    }

    public void testStoreModeBypassUse() {
        if (getCacheEnabled()) {
            entityManagerStoreModeTest(CacheStoreMode.BYPASS, CacheStoreMode.USE, false, true, 1);
        }
    }

    public void testStoreModeBypassRefresh() {
        if (getCacheEnabled()) {
            // REFRESH picks up the changes from the database, even though the
            // first update was done with BYPASS
            entityManagerStoreModeTest(CacheStoreMode.BYPASS, CacheStoreMode.REFRESH, true, true, 1);
        }
    }

    public void testStoreModeRefreshUse() {
        if (getCacheEnabled()) {
            entityManagerStoreModeTest(CacheStoreMode.REFRESH, CacheStoreMode.USE, true, true, 1);
        }
    }

    public void testStoreModeRefreshBypass() {
        if (getCacheEnabled()) {
            entityManagerStoreModeTest(CacheStoreMode.REFRESH, CacheStoreMode.BYPASS, true, false, 1);
        }
    }

    public void testStoreModeRefreshRefresh() {
        if (getCacheEnabled()) {
            entityManagerStoreModeTest(CacheStoreMode.REFRESH, CacheStoreMode.REFRESH, true, true, 1);
        }
    }
    
    public void testResultsFromQueryAreInCache() { 
        if (getCacheEnabled()) {
            // clear cache
            getEntityManagerFactory().getStoreCache().evictAll();
            getEntityManagerFactory().getQueryResultCache().evictAll();

            EntityManager em = getEntityManagerFactory().createEntityManager();
            String query; 
            for(Class<?> cls : persistentTypes) {
                query = "Select e from " + getAlias(cls) + " e";
                List<?> res = em.createQuery(query).getResultList();
                assertNotNull(String.format("Expected to find some results when running query %s",query), res);
                assertTrue(String.format("Expected more than 0 results running query %s",query),res.size() != 0 ) ;
            }
            for(Class<?> cls : getExpectedInCache()) { 
                assertCached(getEntityManagerFactory().getCache(), cls, 1, true);
            }
            
            for(Class<?> cls : getExpectedNotInCache()) { 
                assertCached(getEntityManagerFactory().getCache(), cls, 1, false);
            }
            em.close();
        }
    }
    
    public void testResultsFromFindAreInCache() { 
        if (getCacheEnabled()) {
            // clear cache
            getEntityManagerFactory().getStoreCache().evictAll();
            getEntityManagerFactory().getQueryResultCache().evictAll();

            EntityManager em = getEntityManagerFactory().createEntityManager();
            for(Class<?> cls : persistentTypes) {
                assertNotNull(String.format("Expected to find %s::%d from database or from cache", cls, 1),
                		em.find(cls, 1));
            }
            for(Class<?> cls : getExpectedInCache()) { 
                assertCached(getEntityManagerFactory().getCache(), cls, 1, true);
            }

            for(Class<?> cls : getExpectedNotInCache()) { 
                assertCached(getEntityManagerFactory().getCache(), cls, 1, false);
            }
            em.close();
        }
    }
    
    private String getAlias(Class<?> cls) {
        ClassMapping mapping =
            (ClassMapping) getEntityManagerFactory().getConfiguration().getMetaDataRepositoryInstance().getMetaData(
                cls, null, true);
        return mapping.getTypeAlias();
    }
}

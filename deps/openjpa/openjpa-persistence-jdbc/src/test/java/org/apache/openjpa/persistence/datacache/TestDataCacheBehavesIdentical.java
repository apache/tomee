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
import javax.persistence.LockModeType;

import org.apache.openjpa.persistence.EntityManagerImpl;
import org.apache.openjpa.persistence.EntityNotFoundException;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.OpenJPAEntityManagerSPI;
import org.apache.openjpa.persistence.StoreCache;
import org.apache.openjpa.persistence.StoreCacheImpl;
import org.apache.openjpa.persistence.querycache.common.apps.BidirectionalOne2OneOwned;
import org.apache.openjpa.persistence.querycache.common.apps.BidirectionalOne2OneOwner;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;
import org.apache.openjpa.persistence.datacache.common.apps.PObject;

/**
 * Tests various application behavior with or without DataCache.
 * Ideally, an application should behave identically irrespective of the 
 * DataCache. However, purpose of this test is to identify specific scenarios
 * where this ideal is violated. The test case also demonstrates, wherever
 * possible, what extra step an application may take to ensure that its 
 * behavior with or without DataCache remains identical.   
 * 
 * So far following use cases are found to demonstrate behavioral differences:
 * 1. Inconsistent bidirectional relation
 * 2. Refresh
 * 
 * @author Pinaki Poddar
 * 
 */
public class TestDataCacheBehavesIdentical extends AbstractTestCase {
    private static OpenJPAEntityManagerFactorySPI emfWithDataCache;
    private static OpenJPAEntityManagerFactorySPI emfWithoutDataCache;
    
    private static final boolean WITH_DATACACHE = true;
    private static final boolean CONSISTENT = true;
    private static final boolean DIRTY = true;
    private static final boolean REFRESH_FROM_DATACACHE = true;
    private static final LockModeType NOLOCK = null;
    private static final Class<?> ENTITY_NOT_FOUND_ERROR = EntityNotFoundException.class;
    private static final Class<?> NO_ERROR = null;

    private static final String MARKER_DATACACHE = "in DataCache";
    private static final String MARKER_DATABASE  = "in Database";
    private static final String MARKER_CACHE     = "in Object Cache";
    private static final String MARKER_DIRTY_CACHE = "in Object Cache (dirty)";
    private static long ID_COUNTER = System.currentTimeMillis();
    private static int TEST_COUNT = 0;

    /**
     * Sets up two EntityManagerFactory: one with DataCache another without.
     */
    public void setUp() throws Exception {
        super.setUp();
        if (emfWithDataCache == null) {
            emfWithDataCache = createEMF(
                    "openjpa.jdbc.SynchronizeMappings",    "buildSchema", 
                    "openjpa.RuntimeUnenhancedClasses",    "unsupported", 
                    "openjpa.DataCache", "true",
                    "openjpa.jdbc.UpdateManager", "constraint",
                    PObject.class,
                    BidirectionalOne2OneOwner.class,
                    BidirectionalOne2OneOwned.class, CLEAR_TABLES);
            emfWithoutDataCache = createEMF(
                    "openjpa.RuntimeUnenhancedClasses",    "unsupported", 
                    "openjpa.DataCache", "false",
                    "openjpa.jdbc.UpdateManager", "constraint",
                    PObject.class,
                    BidirectionalOne2OneOwned.class,
                    BidirectionalOne2OneOwner.class, CLEAR_TABLES);

            assertNotNull(emfWithDataCache);
            assertNotNull(emfWithoutDataCache);

            // StoreCache is, by design, always non-null 
            assertNotNull(emfWithDataCache.getStoreCache());
            assertNotNull(emfWithoutDataCache.getStoreCache());

            // however, following distinguishes whether DataCache is active  
            assertTrue(isDataCacheActive(emfWithDataCache));
            assertFalse(isDataCacheActive(emfWithoutDataCache));
        }
        TEST_COUNT++;
    }
    
    public void tearDown() throws Exception {
        // HACK - need to manually close EMFs after all tests have run
        if (TEST_COUNT >= 21) {
            closeEMF(emfWithDataCache);
            emfWithDataCache = null;
            closeEMF(emfWithoutDataCache);
            emfWithoutDataCache = null;
            super.tearDown();            
        }
    }
    
    /**
     * Affirms via internal structures if the given factory is configured with
     * active DataCache. Because, even when DataCache is configured to be
     * false, a no-op StoreCache is instantiated by design.
     */
    boolean isDataCacheActive(OpenJPAEntityManagerFactorySPI emf) {
        return ((StoreCacheImpl) emf.getStoreCache()).getDelegate() != null
            && emf.getConfiguration()
                  .getDataCacheManagerInstance()
                  .getSystemDataCache() != null;
    }

    /**
     * Create one-to-one bidirectional relation (may or may not be consistent)
     * between two pairs of instances. Creates four instances Owner1, Owned1,
     * Owner2, Owned2. The first instance has the given id. The id of the other
     * instances monotonically increase by 1. The relationship is set either
     * consistently or inconsistently. Consistent relation is when Owner1 points
     * to Owned1 and Owned1 points back to Owner1. Inconsistent relation is when
     * Owner1 points to Owned1 but Owned1 points to Owner2 instead of Owner1.
     * 
     * 
     * @param em
     *            the entity manager to persist the instances
     * @param id
     *            the identifier of the first owner instance. The identifier for
     *            the other instances are sequential in order of creation.
     * @param consistent
     *            if true sets the relationship as consistent.
     */
    public void createBidirectionalRelation(EntityManager em, long id,
            boolean consistent) {
        BidirectionalOne2OneOwner owner1 = new BidirectionalOne2OneOwner();
        BidirectionalOne2OneOwned owned1 = new BidirectionalOne2OneOwned();
        BidirectionalOne2OneOwner owner2 = new BidirectionalOne2OneOwner();
        BidirectionalOne2OneOwned owned2 = new BidirectionalOne2OneOwned();
        
        owner1.setId(id++);
        owned1.setId(id++);
        owner2.setId(id++);
        owned2.setId(id++);
        
        owner1.setName("Owner1");
        owned1.setName("Owned1");
        owned2.setName("Owned2");
        owner2.setName("Owner2");

        owner1.setOwned(owned1);
        owner2.setOwned(owned2);

        if (consistent) {
            owned1.setOwner(owner1);
            owned2.setOwner(owner2);
        } else {
            owned1.setOwner(owner2);
            owned2.setOwner(owner1);
        }

        em.getTransaction().begin();
        em.persist(owner1);
        em.persist(owned1);
        em.persist(owner2);
        em.persist(owned2);
        em.getTransaction().commit();
        em.clear();
    }

    /**
     * Verifies that bidirectionally related objects can be persisted 
     * and later retrieved in a different transaction. 
     * 
     * Creates interrelated set of four instances.
     * Establish their relation either consistently or inconsistently based
     * on the given flag.
     * Persist them and then clear the context. 
     * Fetch the instances in memory again by their identifiers. 
     * Compare the interrelations between the fetched instances with the 
     * relations of the original instances (which can be consistent or 
     * inconsistent). 
     * 
     * The mapping specification is such that the bidirectional relation is 
     * stored in database by a single foreign key. Hence database relation
     * is always consistent. Hence the instances retrieved from database are
     * always consistently related irrespective of whether they were created
     * with consistent or inconsistent relation.
     * However, when the instances are retrieved from the data cache, data cache
     * will preserve the in-memory relations even when they are inconsistent.
     *    
     * @param useDataCache
     *            use DataCache
     * @param consistent
     *            assume that the relationship were created as consistent.
     */
    public void verifyBidirectionalRelation(boolean useDataCache,
            boolean createConsistent, boolean expectConsistent) {
        EntityManager em = (useDataCache) 
                         ? emfWithDataCache.createEntityManager() 
                         : emfWithoutDataCache.createEntityManager();
                         
        long id = ID_COUNTER++;
        ID_COUNTER += 4;
        createBidirectionalRelation(em, id, createConsistent);
        
        
        BidirectionalOne2OneOwner owner1 =
            em.find(BidirectionalOne2OneOwner.class, id);
        BidirectionalOne2OneOwned owned1 =
            em.find(BidirectionalOne2OneOwned.class, id + 1);
        BidirectionalOne2OneOwner owner2 =
            em.find(BidirectionalOne2OneOwner.class, id + 2);
        BidirectionalOne2OneOwned owned2 =
            em.find(BidirectionalOne2OneOwned.class, id + 3);

        assertNotNull(owner1);
        assertNotNull(owner2);
        assertNotNull(owned1);
        assertNotNull(owned2);

        assertEquals(owner1, expectConsistent 
                    ? owner1.getOwned().getOwner() 
                    : owner2.getOwned().getOwner());
        assertEquals(owner2, expectConsistent 
                    ? owner2.getOwned().getOwner() 
                    : owner1.getOwned().getOwner());


        assertEquals(owned1, owner1.getOwned());
        assertEquals(expectConsistent ? owner1 : owner2, owned1.getOwner());
        assertEquals(owned2, owner2.getOwned());
        assertEquals(expectConsistent ? owner2 : owner1, owned2.getOwner());
    }

    public void testConsitentBidirectionalRelationIsPreservedWithDataCache() {
        verifyBidirectionalRelation(WITH_DATACACHE, CONSISTENT, CONSISTENT);
    }

    public void testConsitentBidirectionalRelationIsPreservedWithoutDataCache()
    {
        verifyBidirectionalRelation(!WITH_DATACACHE, CONSISTENT, CONSISTENT);
    }

    public void testInconsitentBidirectionalRelationIsPreservedWithDataCache() {
        verifyBidirectionalRelation(WITH_DATACACHE, !CONSISTENT, !CONSISTENT);
    }

    public void
        testInconsitentBidirectionalRelationIsNotPreservedWithoutDataCache() {
        verifyBidirectionalRelation(!WITH_DATACACHE, !CONSISTENT, CONSISTENT);
    }
    
    /**
     * Verify that refresh() may fetch state from either the data cache or the
     * database based on different conditions. 
     * The conditions that impact are 
     * a) whether current lock is stronger than NONE 
     * b) whether the instance being refreshed is dirty
     * 
     * An instance is created with data cache marker and persisted. 
     * A native SQL is used to update the database record with database marker. 
     * The in-memory instance is not aware of this out-of-band update. 
     * Then the in-memory instance is refreshed. The marker of the refreshed 
     * instance tells whether the instance is refreshed from the data cache
     * of the database. 
     * 
     * @param useDataCache flags if data cache is active. if not, then surely
     * refresh always fetch state from the database.
     * 
     * @param datacache the marker for the copy of the data cached instance
     * @param database the marker for the database record
     * @param lock lock to be used
     * @param makeDirtyBeforeRefresh flags if the instance be dirtied before
     * refresh()
     * @param expected The expected marker i.e. where the state is refreshed 
     * from. This should be always <code>MARKER_DATABASE</code>.
     * a) whether DataCache is active
     * b) whether current Lock is stronger than NOLOCK
     * c) whether the object to be refreshed is dirty
     * 
     * The following truth table enumerates the possibilities
     * 
     * Use Cache?   Lock?   Dirty?     Target
     *    Y          Y       Y         Database
     *    Y          N       Y         Data Cache
     *    Y          Y       N         Data Cache
     *    Y          N       N         Data Cache
     *    
     *    N          Y       Y         Database
     *    N          N       Y         Database
     *    N          Y       N         Object Cache
     *    N          N       N         Object Cache

     */
    public void verifyRefresh(boolean useDataCache, LockModeType lock, 
            boolean makeDirtyBeforeRefresh, boolean refreshFromDataCache, 
            String expected) {
        OpenJPAEntityManagerFactorySPI emf = (useDataCache)
            ? emfWithDataCache : emfWithoutDataCache;
        emf.getConfiguration().setRefreshFromDataCache(refreshFromDataCache);
        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        
        em.getTransaction().begin();
        PObject pc = new PObject();
        pc.setName(useDataCache ? MARKER_DATACACHE : MARKER_CACHE);
        em.persist(pc);
        em.getTransaction().commit();
        
        Object oid = pc.getId();
        StoreCache dataCache = emf.getStoreCache();
        assertEquals(useDataCache, dataCache.contains(PObject.class, oid));
        
        // Modify the record in the database in a separate transaction using
        // native SQL so that the in-memory instance is not altered 
        em.getTransaction().begin();
        String sql = "UPDATE L2_PObject SET NAME='" + MARKER_DATABASE
        + "' WHERE id=" + oid;
        em.createNativeQuery(sql).executeUpdate();
        em.getTransaction().commit();
        
        assertEquals(useDataCache ? MARKER_DATACACHE : MARKER_CACHE,
                pc.getName());
        
        em.getTransaction().begin();
        if (makeDirtyBeforeRefresh) {
            pc.setName(MARKER_DIRTY_CACHE);
        } 
        assertEquals(makeDirtyBeforeRefresh, em.isDirty(pc));

        if (lock != null) {
            ((EntityManagerImpl)em).getFetchPlan().setReadLockMode(lock);
        }
        em.refresh(pc);
        
        assertEquals(expected, pc.getName());
        em.getTransaction().commit();
    }
    
    /**
     * The expected marker i.e. where the state is refreshed from depends on
     * a) whether DataCache is active
     * b) whether current Lock is stronger than NOLOCK
     * c) whether the object to be refreshed is dirty
     * 
     * The following truth table enumerates the possibilities
     * 
     * Use Cache?   Lock?   Dirty?     Target
     *    Y          Y       Y         Database
     *    Y          N       Y         Data Cache
     *    Y          Y       N         Data Cache
     *    Y          N       N         Data Cache
     *    
     *    N          Y       Y         Database
     *    N          N       Y         Database
     *    N          Y       N         Object Cache
     *    N          N       N         Object Cache
     *    
     * @param datacache the marker for 
     * @param database
     * @param useDataCache
     * @param lock
     * @param makeDirtyBeforeRefresh
     * @return
     */
    String getExpectedMarker(boolean useDataCache, LockModeType lock, 
            boolean makeDirtyBeforeRefresh) {
        if (useDataCache) {
            return (lock != null) ? MARKER_DATABASE : MARKER_DATACACHE; 
        } else {
            return MARKER_DATABASE;
        }
    }
    
    public void testDirtyRefreshWithNoLockHitsDatabase() {
        verifyRefresh(WITH_DATACACHE, NOLOCK, DIRTY, !REFRESH_FROM_DATACACHE,
                MARKER_DATABASE);
    }
    
    public void testDirtyRefreshWithNoLockHitsDataCache() {
        verifyRefresh(WITH_DATACACHE, NOLOCK, DIRTY, REFRESH_FROM_DATACACHE,
                MARKER_DATACACHE);
    }
    
    public void testCleanRefreshWithNoLockDoesNotHitDatabase() {
        verifyRefresh(WITH_DATACACHE, NOLOCK, !DIRTY, !REFRESH_FROM_DATACACHE,
                MARKER_DATACACHE);
    }
    
    public void testCleanRefreshWithNoLockHitsDataCache() {
        verifyRefresh(WITH_DATACACHE, NOLOCK, !DIRTY, REFRESH_FROM_DATACACHE,
                MARKER_DATACACHE);
    }
    
    public void testDirtyRefreshWithReadLockHitsDatabase() {
        verifyRefresh(WITH_DATACACHE, LockModeType.READ, DIRTY,
                REFRESH_FROM_DATACACHE, MARKER_DATABASE);
        verifyRefresh(WITH_DATACACHE, LockModeType.READ, DIRTY,
                !REFRESH_FROM_DATACACHE, MARKER_DATABASE);
    }
    
    public void testCleanRefreshWithReadLockDoesNotHitDatabase() {
        verifyRefresh(WITH_DATACACHE, LockModeType.READ, !DIRTY,
                REFRESH_FROM_DATACACHE, MARKER_DATACACHE);
        verifyRefresh(WITH_DATACACHE, LockModeType.READ, !DIRTY,
                !REFRESH_FROM_DATACACHE, MARKER_DATACACHE);
    }
    
    public void testDirtyRefreshWithWriteLockHitsDatabase() {
        verifyRefresh(WITH_DATACACHE, LockModeType.WRITE, DIRTY,
                REFRESH_FROM_DATACACHE, MARKER_DATABASE);
        verifyRefresh(WITH_DATACACHE, LockModeType.WRITE, DIRTY,
                !REFRESH_FROM_DATACACHE, MARKER_DATABASE);
    }
    
    public void testCleanRefreshWithWriteLockDoesNotHitDatabase() {
        verifyRefresh(WITH_DATACACHE, LockModeType.WRITE, !DIRTY,
                REFRESH_FROM_DATACACHE, MARKER_DATACACHE);
        verifyRefresh(WITH_DATACACHE, LockModeType.WRITE, !DIRTY,
                !REFRESH_FROM_DATACACHE, MARKER_DATACACHE);
    }
    
    public void testDirtyRefreshWithoutDataCacheAlwaysHitsDatabase() {
        verifyRefresh(!WITH_DATACACHE, NOLOCK, DIRTY, REFRESH_FROM_DATACACHE,
                MARKER_DATABASE);
        verifyRefresh(!WITH_DATACACHE, LockModeType.READ, DIRTY,
                REFRESH_FROM_DATACACHE, MARKER_DATABASE);
        verifyRefresh(!WITH_DATACACHE, LockModeType.WRITE, DIRTY,
                REFRESH_FROM_DATACACHE, MARKER_DATABASE);
        
        verifyRefresh(!WITH_DATACACHE, NOLOCK, DIRTY, !REFRESH_FROM_DATACACHE,
                MARKER_DATABASE);
        verifyRefresh(!WITH_DATACACHE, LockModeType.READ, DIRTY,
                !REFRESH_FROM_DATACACHE, MARKER_DATABASE);
        verifyRefresh(!WITH_DATACACHE, LockModeType.WRITE, DIRTY,
                !REFRESH_FROM_DATACACHE, MARKER_DATABASE);
    }
    
    public void testCleanRefreshWithoutDataCacheDoesNotHitDatabase() {
        verifyRefresh(!WITH_DATACACHE, NOLOCK, !DIRTY, REFRESH_FROM_DATACACHE,
                MARKER_CACHE);
        verifyRefresh(!WITH_DATACACHE, LockModeType.READ, !DIRTY,
                REFRESH_FROM_DATACACHE,  MARKER_CACHE);
        verifyRefresh(!WITH_DATACACHE, LockModeType.WRITE, !DIRTY,
                REFRESH_FROM_DATACACHE,  MARKER_CACHE);
        
        verifyRefresh(!WITH_DATACACHE, NOLOCK, !DIRTY, !REFRESH_FROM_DATACACHE,
                MARKER_CACHE);
        verifyRefresh(!WITH_DATACACHE, LockModeType.READ, !DIRTY,
                !REFRESH_FROM_DATACACHE, MARKER_CACHE);
        verifyRefresh(!WITH_DATACACHE, LockModeType.WRITE, !DIRTY,
                !REFRESH_FROM_DATACACHE, MARKER_CACHE);
    }
    
    /**
     * Verify behavior of refreshing an instance which has been deleted by
     * out-of-band process (e.g. a native SQL in a separate transaction).
     * The behavior differs when refresh() without a lock fetches the data from
     * DataCache even when the original database record is deleted.
     * 
     * @param useDataCache
     * @param lock
     */
    public void verifyDeleteDetectionOnRefresh(boolean useDataCache, 
            boolean dirty, LockModeType lock, Class<?> expectedExceptionType) {
        OpenJPAEntityManagerFactorySPI emf = (useDataCache)
            ? emfWithDataCache : emfWithoutDataCache;
            
        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        
        em.getTransaction().begin();
        PObject pc = new PObject();
        pc.setName(useDataCache ? MARKER_DATACACHE : MARKER_CACHE);
        em.persist(pc);
        em.getTransaction().commit();
        
        Object oid = pc.getId();
        StoreCache dataCache = emf.getStoreCache();
        assertEquals(useDataCache, dataCache.contains(PObject.class, oid));
        
        // delete the record in the database in a separate transaction using
        // native SQL so that the in-memory instance is not altered 
        em.getTransaction().begin();
        String sql = "DELETE FROM L2_PObject WHERE id="+oid;
        em.createNativeQuery(sql).executeUpdate();
        em.getTransaction().commit();
        
        // the object cache does not know that the record was deleted
        assertTrue(em.contains(pc));
        // nor does the data cache
        assertEquals(useDataCache, dataCache.contains(PObject.class, oid));
        
        /**
         * refresh behavior no more depends on current lock. Refresh
         * will always attempt to fetch the instance from database 
         * raising EntityNotFoundException.
         *   
         */
        em.getTransaction().begin();
        em.getFetchPlan().setReadLockMode(lock);
        if (dirty) 
            pc.setName("Dirty Name");
        try {
            em.refresh(pc);
            if (expectedExceptionType != null) {
                fail("expected " + expectedExceptionType.getSimpleName() + 
                        " for PObject:" + oid);
            }
        } catch (Exception ex) {
            boolean expectedException = expectedExceptionType != null &&
                expectedExceptionType.isAssignableFrom(ex.getClass());
            if (!expectedException) {
                ex.printStackTrace();
                String error = (expectedExceptionType == null) 
                    ? "no exception" : expectedExceptionType.getName();
                fail("expected " + error + " for PObject:" + oid);
            }
        } finally {
            em.getTransaction().rollback();
        }
    }

    public void testDeleteIsNotDetectedOnCleanRefreshWithoutLockWithDataCache() {
        verifyDeleteDetectionOnRefresh(WITH_DATACACHE, !DIRTY, NOLOCK, ENTITY_NOT_FOUND_ERROR);
    }
    
    public void testDeleteIsDetectedOnCleanRefreshWithLockWithDataCache() {
        verifyDeleteDetectionOnRefresh(WITH_DATACACHE, !DIRTY, LockModeType.READ,  ENTITY_NOT_FOUND_ERROR);
        verifyDeleteDetectionOnRefresh(WITH_DATACACHE, !DIRTY, LockModeType.WRITE, ENTITY_NOT_FOUND_ERROR);
    }

    public void testDeleteIsDetectedOnDirtyRefreshWithoutLockWithDataCache() {
        verifyDeleteDetectionOnRefresh(WITH_DATACACHE, DIRTY, NOLOCK, ENTITY_NOT_FOUND_ERROR);
    }
    
    public void testDeleteIsDetectedOnDirtyRefreshWithLockWithDataCache() {
        verifyDeleteDetectionOnRefresh(WITH_DATACACHE, DIRTY, LockModeType.READ,  ENTITY_NOT_FOUND_ERROR);
        verifyDeleteDetectionOnRefresh(WITH_DATACACHE, DIRTY, LockModeType.WRITE, ENTITY_NOT_FOUND_ERROR);
    }
    
    public void testDeleteIsDetectedOnDirtyRefreshWitDataCache() {
        verifyDeleteDetectionOnRefresh(WITH_DATACACHE, DIRTY, LockModeType.READ,  ENTITY_NOT_FOUND_ERROR);
        verifyDeleteDetectionOnRefresh(WITH_DATACACHE, DIRTY, LockModeType.WRITE, ENTITY_NOT_FOUND_ERROR);
    }
    
    public void testDeleteIsDetectedOnCleanRefreshWithoutLockWithoutDataCache() {
        verifyDeleteDetectionOnRefresh(!WITH_DATACACHE, !DIRTY, NOLOCK, ENTITY_NOT_FOUND_ERROR);
    }
    
    public void testDeleteIsDetectedOnCleanRefreshWithLockWithoutDataCache() {
        verifyDeleteDetectionOnRefresh(!WITH_DATACACHE, !DIRTY, LockModeType.READ,  ENTITY_NOT_FOUND_ERROR);
        verifyDeleteDetectionOnRefresh(!WITH_DATACACHE, !DIRTY, LockModeType.WRITE, ENTITY_NOT_FOUND_ERROR);
    }

}

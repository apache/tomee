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

import org.apache.openjpa.datacache.DataCacheManager;
import org.apache.openjpa.datacache.DataCacheStoreManager;
import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.kernel.DelegatingStoreManager;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.persistence.EntityManagerImpl;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * 
 * This test was added for OPENJPA-1882.
 * <p>
 * When caching is enabled and a given type isn't being cached, make sure calling exists/syncVersion with that type
 * doesn't result in a NPE.
 * 
 */
public class TestDataCacheStoreManager extends SingleEMFTestCase {
    Object[] p =
        new Object[] { CLEAR_TABLES, CachedEntityStatistics.class, "openjpa.DataCache", "true(EnableStatistics=true)",
            "openjpa.QueryCache", "true", };

    private EntityManager em;
    private DataCacheStoreManager dsm;
    private DataCacheManager dcm;
    private OpenJPAStateManager sm;

    public void setUp() {
        super.setUp(p);

        em = emf.createEntityManager();
        dcm = emf.getConfiguration().getDataCacheManagerInstance();
        dsm =
            (DataCacheStoreManager) ((DelegatingStoreManager) ((EntityManagerImpl) em).getBroker().getStoreManager())
                .getDelegate();

        em.getTransaction().begin();
        CachedEntityStatistics p = new CachedEntityStatistics();
        em.persist(p);
        em.getTransaction().commit();

        dcm.stopCaching(CachedEntityStatistics.class.getName());

        sm = (OpenJPAStateManager) ((PersistenceCapable) p).pcGetStateManager();
    }

    public void tearDown() throws Exception {
        dcm.startCaching(CachedEntityStatistics.class.getName());
        em.close();

        super.tearDown();
    }

    public void testExists() {
        dsm.exists(sm, null);
    }

    public void testsyncVersion() {
        dsm.syncVersion(sm, null);
    }
}

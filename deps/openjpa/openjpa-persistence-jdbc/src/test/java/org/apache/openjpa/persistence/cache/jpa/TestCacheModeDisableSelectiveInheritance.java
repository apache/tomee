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

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.cache.jpa.model.ChildUncacheable;
import org.apache.openjpa.persistence.cache.jpa.model.ParentUnspecifiedEntity;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * This test was added for https://issues.apache.org/jira/browse/OPENJPA-1892.
 * 
 * The key to this test is that the ChildUncacheable is uncacheable and ParentUnspecifiedEntity is cacheable. In the
 * case were we pass the root Entity in em.find(ParentUnspecifiedEntity.class, 1) AND the id we passed corresponds to
 * the child Entity we shouldn't be caching this result.
 * 
 */
public class TestCacheModeDisableSelectiveInheritance extends SingleEMFTestCase {
    Object[] params =
        new Object[] { ChildUncacheable.class, ParentUnspecifiedEntity.class, CLEAR_TABLES,
            "javax.persistence.sharedCache.mode", "DISABLE_SELECTIVE", "openjpa.DataCache", "true" };

    @Override
    public void setUp() throws Exception {
        super.setUp(params);
    }

    @Override
    public void tearDown() throws Exception {

    }

    public void testSimpleFind() throws Exception {
        EntityManager em = emf.createEntityManager();
        try {
            ChildUncacheable c = new ChildUncacheable();
            em.getTransaction().begin();
            em.persist(c);
            em.getTransaction().commit();
            assertEquals(c, em.find(ChildUncacheable.class, c.getId()));
            em.clear();
            assertEquals(c.getId(), em.find(ChildUncacheable.class, c.getId()).getId());

        } finally {
            em.close();
        }
    }
}

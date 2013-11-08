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
package org.apache.openjpa.persistence.detach;

import org.apache.openjpa.persistence.OpenJPAEntityManagerSPI;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Added for OPENJPA-1896
 */
public class TestMergeNoStateManager extends SingleEMFTestCase {
    Object[] args =
        new Object[] { TimestampVersionEntity.class, IntVersionEntity.class, NoVersionEntity.class,
            IntegerVersionEntity.class, CLEAR_TABLES
//            , "openjpa.Log", "SQL=trace" 
            };

    IntVersionEntity _ive;
    NoVersionEntity _nve;
    IntegerVersionEntity _integerVe;

    @Override
    public void setUp() throws Exception {
        super.setUp(args);
        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        try {
            if (em.find(IntVersionEntity.class, 1) == null) {
                em.getTransaction().begin();
                _ive = new IntVersionEntity(1);
                _nve = new NoVersionEntity(1);
                _integerVe = new IntegerVersionEntity(1);

                em.persist(_ive);
                em.persist(_nve);
                em.persist(_integerVe);

                em.getTransaction().commit();
            }
        } finally {
            em.close();
        }
    }

    /**
     * This test is commented out is it will fail. 
     */
//    public void testOLE() throws Exception {
//        OpenJPAEntityManagerSPI em = emf.createEntityManager();
//        try {
//            String updatedName = "updatedName_" + System.currentTimeMillis();
//            IntVersionEntity ive = em.find(IntVersionEntity.class, _ive.getId());
//            em.clear();
//
//            IntVersionEntity detachedIve = new IntVersionEntity(_ive.getId());
//            // Set the version to older than currently in the db to simulate having stale data
//            detachedIve.setId(0);
//            detachedIve.setName(updatedName);
//            // serialize
//            detachedIve = roundtrip(detachedIve);
//
//            em.getTransaction().begin();
//            // This merge should throw an OLE since we have older version than current
//            try {
//                em.merge(detachedIve);
//                throw new RuntimeException("Expected an OLE, but didn't get one!");
//            } catch (OptimisticLockException ole) {
//                // expected
//            }
//        } finally {
//            if (em.getTransaction().isActive()) {
//                em.getTransaction().rollback();
//            }
//            em.close();
//        }
//    }

    public void test() throws Exception {
        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        try {
            String updatedName = "updatedName_" + System.currentTimeMillis();
            IntVersionEntity detachedIve = new IntVersionEntity(_ive.getId());
            NoVersionEntity detachedNve = new NoVersionEntity(_nve.getId());
            IntegerVersionEntity detachedIntegerVe = new IntegerVersionEntity(_integerVe.getId());

            detachedIntegerVe.setName(updatedName);
            detachedNve.setName(updatedName);
            detachedIve.setName(updatedName);

            em.getTransaction().begin();
            em.merge(detachedIntegerVe);
            em.merge(detachedNve);
            em.merge(detachedIve);
            em.getTransaction().commit();

            em.clear();

            detachedIntegerVe = em.find(IntegerVersionEntity.class, _integerVe.getId());
            detachedNve = em.find(NoVersionEntity.class, _nve.getId());
            detachedIve = em.find(IntVersionEntity.class, _ive.getId());

            // Make sure the updated values were persisted
            assertEquals(detachedIntegerVe.getName(), updatedName);
            assertEquals(detachedNve.getName(), updatedName);
            assertEquals(detachedIve.getName(), updatedName);

        } finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().commit();
            }
            em.close();
        }
    }
}

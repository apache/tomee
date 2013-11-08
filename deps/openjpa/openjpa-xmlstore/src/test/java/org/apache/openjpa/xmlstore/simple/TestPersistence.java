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
package org.apache.openjpa.xmlstore.simple;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.test.AbstractPersistenceTestCase;

/**
 * Simple XMLStore test case to get an EntityManager and perform some basic operations.
 */
public class TestPersistence extends AbstractPersistenceTestCase {

    public void testCreateEntityManager() {
        OpenJPAEntityManagerFactorySPI emf = createNamedEMF("xmlstore-simple");
        try {
            EntityManager em = emf.createEntityManager();

            EntityTransaction t = em.getTransaction();
            assertNotNull(t);
            t.begin();
            t.setRollbackOnly();
            t.rollback();

            // openjpa-facade test
            assertTrue(em instanceof OpenJPAEntityManager);
            OpenJPAEntityManager ojem = (OpenJPAEntityManager) em;
            ojem.getFetchPlan().setMaxFetchDepth(1);
            assertEquals(1, ojem.getFetchPlan().getMaxFetchDepth());
            em.close();
        } finally {
            closeEMF(emf);
        }
    }

    public void testQuery() {
        OpenJPAEntityManagerFactorySPI emf = createNamedEMF("xmlstore-simple",
            CLEAR_TABLES, AllFieldTypes.class);
        try {
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();
            AllFieldTypes aft = new AllFieldTypes();
            aft.setStringField("foo");
            aft.setIntField(10);
            em.persist(aft);
            em.getTransaction().commit();
            em.close();

            em = emf.createEntityManager();
            em.getTransaction().begin();
            assertEquals(1, em.createQuery
                ("select x from AllFieldTypes x where x.stringField = 'foo'").
                getResultList().size());
            assertEquals(0, em.createQuery
                ("select x from AllFieldTypes x where x.stringField = 'bar'").
                getResultList().size());
            assertEquals(1, em.createQuery
                ("select x from AllFieldTypes x where x.intField >= 10").
                getResultList().size());
            em.getTransaction().rollback();
            em.close();
        } finally {
            closeEMF(emf);
        }
    }

    public void testNewDeleteNew() {
        OpenJPAEntityManagerFactorySPI emf = createNamedEMF("xmlstore-simple",
            CLEAR_TABLES, Place.class);
        try {
            EntityManager em = emf.createEntityManager();

            // create new
            Place place = new Place();
            place.setLocation("Lexington");
            assertFalse(em.contains(place));
            em.getTransaction().begin();
            em.persist(place);
            em.getTransaction().commit();
            assertTrue(em.contains(place));

            // find and verify
            place = em.find(Place.class, "Lexington");
            assertNotNull(place);
            assertEquals("Lexington", place.getLocation());

            // delete
            em.getTransaction().begin();
            em.remove(place);
            em.getTransaction().commit();
            assertFalse(em.contains(place));

            // recreate
            place = new Place();
            place.setLocation("Lexington");
            assertFalse(em.contains(place));
            em.getTransaction().begin();
            em.persist(place);
            em.getTransaction().commit();
            assertTrue(em.contains(place));

            // find and verify
            place = em.find(Place.class, "Lexington");
            assertNotNull(place);
            assertEquals("Lexington", place.getLocation());
            em.close();
        } finally {
            closeEMF(emf);
        }
    }

}


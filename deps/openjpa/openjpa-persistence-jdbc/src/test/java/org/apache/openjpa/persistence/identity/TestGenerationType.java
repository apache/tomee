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
package org.apache.openjpa.persistence.identity;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import junit.textui.TestRunner;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Simple test case to test the GenerationType for
 * {@link javax.persistence.Id}.
 * Tests both ways of generated keys retrieval: separate query
 * and JDBC3 {@link java.sql.Statement#getGeneratedKeys}
 * if the database supports them.
 *
 * @author Kevin Sutter
 */
public class TestGenerationType
    extends SingleEMFTestCase {

    public void setUp() {
        if (getName().endsWith("WithoutGetGeneratedKeys")) {
            setUp(IdentityGenerationType.class,
                "openjpa.jdbc.DBDictionary",
                "supportsGetGeneratedKeys=false");
        } else {
            setUp(IdentityGenerationType.class);
        }
    }

    /**
     * Not all databases support GenerationType.IDENTITY column(s).
     */
    private boolean supportsAutoAssign() {
        return ((JDBCConfiguration) emf.getConfiguration())
            .getDBDictionaryInstance().supportsAutoAssign;
    }
    
    public void testCreateEntityManager() {
        if (!supportsAutoAssign()) {
            return;
        }
        EntityManager em = emf.createEntityManager();

        EntityTransaction t = em.getTransaction();
        assertNotNull(t);
        t.begin();
        t.setRollbackOnly();
        t.rollback();

        // openjpa-facade test
        assertTrue(em instanceof OpenJPAEntityManager);
        OpenJPAEntityManager ojem = (OpenJPAEntityManager) em;
        ojem.getFetchPlan().setMaxFetchDepth(-1);
        assertEquals(-1, ojem.getFetchPlan().getMaxFetchDepth());
        em.close();
    }

    public void testPersist() {
        if (!supportsAutoAssign()) {
            return;
        }
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(new IdentityGenerationType());
        em.getTransaction().commit();
        em.close();
    }

    public void testQuery() {
        if (!supportsAutoAssign()) {
            return;
        }
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        IdentityGenerationType igt = new IdentityGenerationType();
        igt.setSomeData("SomeString");
        em.persist(igt);
        // add another IdentityGenerationType object
        em.persist(new IdentityGenerationType());
        em.getTransaction().commit();

        // Check to make sure there are two objects...
        Query q = em.createQuery("select x from IdentityGenerationType x");
        List l = q.getResultList();
        assertEquals(2, l.size());
        em.close();
    }

    public void testPersistWithoutGetGeneratedKeys() {
        testPersist();
    }

    public void testQueryWithoutGetGeneratedKeys() {
        testQuery();
    }

    public static void main(String[] args) {
        TestRunner.run(TestGenerationType.class);
    }
}


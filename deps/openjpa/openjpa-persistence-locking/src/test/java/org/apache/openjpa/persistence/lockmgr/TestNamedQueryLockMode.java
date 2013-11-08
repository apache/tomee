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
package org.apache.openjpa.persistence.lockmgr;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.Query;

import org.apache.openjpa.jdbc.sql.DB2Dictionary;
import org.apache.openjpa.persistence.FetchPlan;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerSPI;
import org.apache.openjpa.persistence.OpenJPAQuery;
import org.apache.openjpa.persistence.TransactionRequiredException;
import org.apache.openjpa.persistence.test.AllowFailure;
import org.apache.openjpa.persistence.test.SQLListenerTestCase;

/**
 * Tests the lock mode on named query emits a FOR UPDATE clause in target SQL
 * query.
 * 
 * 
 */
public class TestNamedQueryLockMode extends SQLListenerTestCase {
    String lockClause = "FOR UPDATE";
    
    @Override
    protected String getPersistenceUnitName() {
        return "locking-test";
    }

    public void setUp() {
        super.setUp(CLEAR_TABLES, LockEmployee.class, 
            "openjpa.LockManager", "pessimistic", 
            "openjpa.Optimistic", "false"
            );
    }

    public void testForUpdateClausePresentInNamedQueryWithLockMode() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        lockClause = getForUpdateClause();
        assertClausePresentInSQL(lockClause, em.createNamedQuery("findEmployeeByIdWithLock").setParameter("id", 0));
        em.getTransaction().rollback();
        em.getTransaction().begin();
        assertClausePresentInSQL(lockClause, em.createNamedQuery("findEmployeeByIdWithLock").setParameter("id", 0));
        em.getTransaction().rollback();
        em.getTransaction().begin();
        assertClausePresentInSQL(lockClause, em.createNamedQuery("findEmployeeByIdWithLock").setParameter("id", 0));
        em.getTransaction().rollback();
    }

    @AllowFailure
    public void testNamedQueryWithLockModeMustExecuteInTransaction() {
        EntityManager em = emf.createEntityManager();
        // execute without a transaction
         try {
         em.createNamedQuery("findEmployeeByIdWithLock").setParameter("id", 0).getResultList();
         fail("Expected " + TransactionRequiredException.class.getName());
         } catch (TransactionRequiredException e) {
         // Expected
         }
    }

    public void testForUpdateClausePresentInQueryWithDefault() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        lockClause = getForUpdateClause();
		if (!DB2Dictionary.class.isAssignableFrom(getDBDictionary().getClass())) {
			// Skip test because "for update" clause in DB2 depends on LockMode type (i.e. WITH RR/RS)
			// plus dict.forUpdateClause is not sensitive to this implementation.  
            assertClausePresentInSQL(lockClause, em.createNamedQuery("findEmployeeById").setParameter("id", 0));
            assertClausePresentInSQL(lockClause, em.createNamedQuery("findEmployeeById").setParameter("id", 0));
        }
        
        OpenJPAEntityManager oem = (OpenJPAEntityManager)em;
        OpenJPAQuery<?> q = oem.createNamedQuery("findEmployeeById").setParameter("id", 0); 
        FetchPlan fp = q.getFetchPlan();
        fp.setReadLockMode(LockModeType.NONE);

        em.getTransaction().commit();
    }
    
    public void testForUpdateClauseAbsentInQueryWithFetchPlanNoneLockMode() {
        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        em.getTransaction().begin();        
        lockClause = getForUpdateClause();
        OpenJPAQuery<?> q = em.createNamedQuery("findEmployeeById").setParameter("id", 0); 
        FetchPlan fp = q.getFetchPlan();
        fp.setReadLockMode(LockModeType.NONE);
        assertClauseAbsentInSQL(lockClause, q);
        
        q = em.createNamedQuery("findEmployeeByIdWithNoLock").setParameter("id", 0); 
        fp = q.getFetchPlan();
        fp.setReadLockMode(LockModeType.NONE);
        assertClauseAbsentInSQL(lockClause, q);
        
        em.getTransaction().commit();
    }

    @AllowFailure
    public void testForUpdateClauseAbsentInQueryWithExplictNoLock() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        lockClause = getForUpdateClause();
        assertClauseAbsentInSQL(lockClause, em.createNamedQuery("findEmployeeByIdWithNoLock").setParameter("id", 0));
        assertClauseAbsentInSQL(lockClause, em.createNamedQuery("findEmployeeByIdWithNoLock").setParameter("id", 0));
        em.getTransaction().commit();
    }

    String getLastSQL() {
        String last = sql.get(getSQLCount() - 1);
        assertNotNull("No last sql found", last);
        return last;
    }

    void assertClausePresentInSQL(String clause, Query q) {
        q.getResultList();
        String last = getLastSQL();
        assertTrue(clause + " is not present in " + last, last.toUpperCase().indexOf(clause) != -1);
    }

    void assertClauseAbsentInSQL(String clause, Query q) {
        q.getResultList();
        String last = getLastSQL();
        assertTrue(clause + " is not absent in " + last, last.toUpperCase().indexOf(clause) == -1);
    }
}

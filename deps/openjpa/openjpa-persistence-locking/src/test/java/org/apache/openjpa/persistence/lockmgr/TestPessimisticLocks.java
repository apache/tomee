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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PessimisticLockException;
import javax.persistence.Query;
import javax.persistence.QueryTimeoutException;
import javax.persistence.TypedQuery;

import junit.framework.AssertionFailedError;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.sql.DB2Dictionary;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.jdbc.sql.DerbyDictionary;
import org.apache.openjpa.jdbc.sql.InformixDictionary;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.persistence.LockTimeoutException;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.test.SQLListenerTestCase;
import org.apache.openjpa.util.OpenJPAException;

/**
 * Test Pessimistic Lock and exception behavior against EntityManager and Query
 * interface methods.
 */
public class TestPessimisticLocks extends SQLListenerTestCase {

    private DBDictionary dict = null;
    private int lockWaitTime = 2000;

    @Override
    protected String getPersistenceUnitName() {
        return "locking-test";
    }

    public void setUp() {
        // Disable tests for any DB that has supportsQueryTimeout==false, like Postgres
        OpenJPAEntityManagerFactorySPI tempEMF = emf;
        if (tempEMF == null) {
            tempEMF = createEMF();
        }
        assertNotNull(tempEMF);
        dict = ((JDBCConfiguration)tempEMF.getConfiguration()).getDBDictionaryInstance();
        assertNotNull(dict);
        if (!dict.supportsQueryTimeout)
            setTestsDisabled(true);
        if (emf == null) {
            closeEMF(tempEMF);
        }

        if (isTestsDisabled())
            return;
        
        setUp(CLEAR_TABLES, Employee.class, Department.class, VersionEntity.class, "openjpa.LockManager", "mixed");

        EntityManager em = null;
        em = emf.createEntityManager();
        em.getTransaction().begin();

        Employee e1, e2;
        Department d1, d2;
        d1 = new Department();
        d1.setId(10);
        d1.setName("D10");

        e1 = new Employee();
        e1.setId(1);
        e1.setDepartment(d1);
        e1.setFirstName("first.1");
        e1.setLastName("last.1");

        d2 = new Department();
        d2.setId(20);
        d2.setName("D20");

        e2 = new Employee();
        e2.setId(2);
        e2.setDepartment(d2);
        e2.setFirstName("first.2");
        e2.setLastName("last.2");

        em.persist(d1);
        em.persist(d2);
        em.persist(e1);
        em.persist(e2);
        em.getTransaction().commit();
        em.close();
    }

    /*
     * Test find with pessimistic lock after a query with pessimistic lock.
     */
    public void testFindAfterQueryWithPessimisticLocks() {
        EntityManager em1 = emf.createEntityManager();
        EntityManager em2 = emf.createEntityManager();
        em1.getTransaction().begin();
        TypedQuery<Employee> query = em1.createQuery("select e from Employee e where e.id < 10", Employee.class)
                .setFirstResult(1);
        // Lock all selected Employees, skip the first one, i.e should lock
        // Employee(2)
        query.setLockMode(LockModeType.PESSIMISTIC_READ);
        query.setHint("javax.persistence.query.timeout", lockWaitTime);
        List<Employee> employees = query.getResultList();
        assertEquals("Expected 1 element with emplyee id=2", employees.size(), 1);
        assertTrue("Test Employee first name = 'first.2'", employees.get(0).getFirstName().equals("first.1")
                || employees.get(0).getFirstName().equals("first.2"));

        em2.getTransaction().begin();
        Map<String, Object> hints = new HashMap<String, Object>();
        hints.put("javax.persistence.lock.timeout", lockWaitTime);
        // find Employee(2) with a lock, should block and expected a
        // PessimisticLockException
        try {
            em2.find(Employee.class, 2, LockModeType.PESSIMISTIC_READ, hints);
            fail("Unexcpected find succeeded. Should throw a PessimisticLockException.");
        } catch (Throwable e) {            
            assertError(e, PessimisticLockException.class, LockTimeoutException.class);
        } finally {
            if (em1.getTransaction().isActive())
                em1.getTransaction().rollback();
            if (em2.getTransaction().isActive())
                em2.getTransaction().rollback();
        }

        em1.getTransaction().begin();
        TypedQuery<Department> query2 = em1.createQuery("select e.department from Employee e where e.id < 10",
                Department.class).setFirstResult(1);
        // Lock all selected Departments, skip the first one, i.e should
        // lock Department(20)
        query.setLockMode(LockModeType.PESSIMISTIC_READ);
        query.setHint("javax.persistence.query.timeout", lockWaitTime);
        List<Department> depts = query2.getResultList();
        assertEquals("Expected 1 element with department id=20", depts.size(), 1);
        assertTrue("Test department name = 'D20'", depts.get(0).getName().equals("D10")
                || depts.get(0).getName().equals("D20"));

        em2.getTransaction().begin();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("javax.persistence.lock.timeout", lockWaitTime);
        // find Employee(2) with a lock, no block since only department was
        // locked
        try {
            Employee emp = em2.find(Employee.class, 1, LockModeType.PESSIMISTIC_READ, map);
            assertNotNull("Query locks department only, therefore should find Employee.", emp);
            assertEquals("Test Employee first name = 'first.1'", emp.getFirstName(), "first.1");
        } catch (Exception ex) {
            fail("Caught unexpected " + ex.getClass().getName() + ":" + ex.getMessage());
        } finally {
            if (em1.getTransaction().isActive())
                em1.getTransaction().rollback();
            if (em2.getTransaction().isActive())
                em2.getTransaction().rollback();
        }
        em1.close();
        em2.close();
    }

    /*
     * Test find with pessimistic lock after a query with pessimistic lock.
     */
    public void testFindAfterQueryOrderByWithPessimisticLocks() {
        EntityManager em1 = emf.createEntityManager();
        EntityManager em2 = emf.createEntityManager();
        em1.getTransaction().begin();
        Query query = em1.createQuery("select e from Employee e where e.id < 10 order by e.id").setFirstResult(1);
        // Lock all selected Employees, skip the first one, i.e should lock
        // Employee(2)
        query.setLockMode(LockModeType.PESSIMISTIC_READ);
        query.setHint("javax.persistence.query.timeout", lockWaitTime);
        List<Employee> q = query.getResultList();
        assertEquals("Expected 1 element with emplyee id=2", q.size(), 1);
        assertEquals("Test Employee first name = 'first.2'", q.get(0).getFirstName(), "first.2");

        em2.getTransaction().begin();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("javax.persistence.lock.timeout", lockWaitTime);
        // find Employee(2) with a lock, should block and expected a
        // PessimisticLockException
        try {
            em2.find(Employee.class, 2, LockModeType.PESSIMISTIC_READ, map);
            fail("Unexcpected find succeeded. Should throw a PessimisticLockException.");
        } catch (Exception e) {
            assertError(e, PessimisticLockException.class, LockTimeoutException.class);
        } finally {
            if (em1.getTransaction().isActive())
                em1.getTransaction().rollback();
            if (em2.getTransaction().isActive())
                em2.getTransaction().rollback();
        }

        em1.getTransaction().begin();
        query = em1.createQuery("select e.department from Employee e where e.id < 10 order by e.department.id")
                .setFirstResult(1);
        // Lock all selected Departments, skip the first one, i.e should
        // lock Department(20)
        query.setLockMode(LockModeType.PESSIMISTIC_READ);
        query.setHint("javax.persistence.query.timeout", lockWaitTime);
        List<Department> result = query.getResultList();
        assertEquals("Expected 1 element with department id=20", q.size(), 1);
        assertEquals("Test department name = 'D20'", result.get(0).getName(), "D20");

        em2.getTransaction().begin();
        map.clear();
        map.put("javax.persistence.lock.timeout", lockWaitTime);
        // find Employee(2) with a lock, no block since only department was
        // locked
        try {
            Employee emp = em2.find(Employee.class, 1, LockModeType.PESSIMISTIC_READ, map);
            assertNotNull("Query locks department only, therefore should find Employee.", emp);
            assertEquals("Test Employee first name = 'first.1'", emp.getFirstName(), "first.1");
        } catch (Exception ex) {
            if (!dict.supportsLockingWithOrderClause)
                fail("Caught unexpected " + ex.getClass().getName() + ":" + ex.getMessage());
            else 
                assertError(ex, LockTimeoutException.class);
        } finally {
            if (em1.getTransaction().isActive())
                em1.getTransaction().rollback();
            if (em2.getTransaction().isActive())
                em2.getTransaction().rollback();
        }
        em1.close();
        em2.close();
    }

    /*
     * Test query with pessimistic lock after a find with pessimistic lock.
     */
    public void testQueryAfterFindWithPessimisticLocks() {
        EntityManager em1 = emf.createEntityManager();
        EntityManager em2 = emf.createEntityManager();
        try {
            em2.getTransaction().begin();
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("javax.persistence.lock.timeout", lockWaitTime);
            // Lock Emplyee(1), no department should be locked
            em2.find(Employee.class, 1, LockModeType.PESSIMISTIC_READ, map);

            em1.getTransaction().begin();
            Query query = em1.createQuery("select e.department from Employee e where e.id < 10").setFirstResult(1);
            query.setLockMode(LockModeType.PESSIMISTIC_READ);
            query.setHint("javax.persistence.query.timeout", lockWaitTime);
            // Lock all selected Department but skip the first, i.e. lock
            // Department(20), should query successfully.
            List<Department> q = query.getResultList();
            assertEquals("Expected 1 element with department id=20", q.size(), 1);
            assertTrue("Test department name = 'D20'", q.get(0).getName().equals("D10")
                    || q.get(0).getName().equals("D20"));
        } catch (Exception ex) {
            assertError(ex, QueryTimeoutException.class);
        } finally {
            if (em1.getTransaction().isActive())
                em1.getTransaction().rollback();
            if (em2.getTransaction().isActive())
                em2.getTransaction().rollback();
        }

        em2.getTransaction().begin();

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("javax.persistence.lock.timeout", lockWaitTime);
        // Lock Emplyee(2), no department should be locked
        em2.find(Employee.class, 2, LockModeType.PESSIMISTIC_READ, map);

        em1.getTransaction().begin();
        Query query = em1.createQuery("select e from Employee e where e.id < 10").setFirstResult(1);
        // Lock all selected Employees, skip the first one, i.e should lock
        // Employee(2)
        query.setLockMode(LockModeType.PESSIMISTIC_READ);
        query.setHint("javax.persistence.query.timeout", lockWaitTime);
        try {
            List<Employee> q = query.getResultList();
            fail("Unexcpected find succeeded. Should throw a PessimisticLockException.");
        } catch (Exception e) {
            assertError(e, PessimisticLockException.class, QueryTimeoutException.class);
        } finally {
            if (em1.getTransaction().isActive())
                em1.getTransaction().rollback();
            if (em2.getTransaction().isActive())
                em2.getTransaction().rollback();
        }
        em1.close();
        em2.close();
    }

    /*
     * Test query with pessimistic lock after a find with pessimistic lock.
     */
    public void testQueryOrderByAfterFindWithPessimisticLocks() {
        EntityManager em1 = emf.createEntityManager();
        EntityManager em2 = emf.createEntityManager();
        em2.getTransaction().begin();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("javax.persistence.lock.timeout", lockWaitTime);
        // Lock Emplyee(1), no department should be locked
        em2.find(Employee.class, 1, LockModeType.PESSIMISTIC_READ, map);

        em1.getTransaction().begin();
        Query query = em1.createQuery("select e.department from Employee e where e.id < 10 order by e.department.id")
                .setFirstResult(1);
        query.setLockMode(LockModeType.PESSIMISTIC_READ);
        query.setHint("javax.persistence.query.timeout", lockWaitTime);
        // Lock all selected Department but skip the first, i.e. lock
        // Department(20), should query successfully.
        try {
            List<Department> q = query.getResultList();
            assertEquals("Expected 1 element with department id=20", q.size(), 1);
            assertEquals("Test department name = 'D20'", q.get(0).getName(), "D20");
        } catch (Exception ex) {
            assertError(ex, QueryTimeoutException.class);
        } finally {
            if (em1.getTransaction().isActive())
                em1.getTransaction().rollback();
            if (em2.getTransaction().isActive())
                em2.getTransaction().rollback();
        }

        em2.getTransaction().begin();

        map.clear();
        map.put("javax.persistence.lock.timeout", lockWaitTime);
        // Lock Emplyee(2), no department should be locked
        em2.find(Employee.class, 2, LockModeType.PESSIMISTIC_READ, map);

        em1.getTransaction().begin();
        query = em1.createQuery("select e from Employee e where e.id < 10 order by e.department.id").setFirstResult(1);
        // Lock all selected Employees, skip the first one, i.e should lock
        // Employee(2)
        query.setLockMode(LockModeType.PESSIMISTIC_READ);
        query.setHint("javax.persistence.query.timeout", lockWaitTime);
        try {
            List<?> q = query.getResultList();
            fail("Unexcpected find succeeded. Should throw a PessimisticLockException.");
        } catch (Exception e) {
            assertError(e, PessimisticLockException.class, QueryTimeoutException.class);
        } finally {
            if (em1.getTransaction().isActive())
                em1.getTransaction().rollback();
            if (em2.getTransaction().isActive())
                em2.getTransaction().rollback();
        }
        em1.close();
        em2.close();
    }

    /*
     * Test multiple execution of the same query with pessimistic lock.
     */
    public void testRepeatedQueryWithPessimisticLocks() {
        EntityManager em = emf.createEntityManager();
        resetSQL();
        em.getTransaction().begin();
        String jpql = "select e.firstName from Employee e where e.id = 1";
        Query q1 = em.createQuery(jpql);
        q1.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        String firstName1 = (String) q1.getSingleResult();
        //Expected sql for Derby is:
        //SELECT t0.firstName FROM Employee t0 WHERE (t0.id = CAST(? AS BIGINT)) FOR UPDATE WITH RR
        String SQL1 = getLastSQL(sql);
        
        // run the second time
        resetSQL();
        Query q2 = em.createQuery(jpql);
        q2.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        String firstName2 = (String) q2.getSingleResult();
        String SQL2 = getLastSQL(sql);
        assertEquals(SQL1, SQL2);
        em.getTransaction().commit();
    }
    
    protected Log getLog() {
        return emf.getConfiguration().getLog("Tests");
    }

    /**
     * This variation introduces a row level write lock in a secondary thread,
     * issues a refresh in the main thread with a lock timeout, and expects a 
     * LockTimeoutException.
     */
    public void testRefreshLockTimeout() {

        // Only run this test on DB2 and Derby for now.  It could cause
        // the test to hang on other platforms.
        if (!(dict instanceof DerbyDictionary ||
              dict instanceof DB2Dictionary ||
              dict instanceof InformixDictionary)) {
            return;
        }
        
        // Informix currently requires the lock timeout to be set directly on the dictionary
        if (dict instanceof InformixDictionary) {
            InformixDictionary ifxDict = (InformixDictionary)((JDBCConfiguration)emf.getConfiguration()).getDBDictionaryInstance();
            ifxDict.lockModeEnabled = true;
            ifxDict.lockWaitSeconds = 5;
        }

        EntityManager em = emf.createEntityManager();
        
        resetSQL();
        VersionEntity ve = new VersionEntity();
        int veid = new Random().nextInt();
        ve.setId(veid);
        ve.setName("Versioned Entity");

        em.getTransaction().begin();
        em.persist(ve);
        em.getTransaction().commit();
                
        em.getTransaction().begin();
        // Assert that the department can be found and no lock mode is set
        ve = em.find(VersionEntity.class, veid);
        assertTrue(em.contains(ve));        
        assertTrue(em.getLockMode(ve) == LockModeType.NONE);
        em.getTransaction().commit();
        
        // Kick of a thread to lock the DB for update
        ExecutorService executor = Executors.newFixedThreadPool(1);
        Future<Boolean> result = executor.submit(new RefreshWithLock(veid, this));
        try {
            // Wait for the thread to lock the row
            getLog().trace("Main: waiting");
            synchronized (this) {
                // The derby lock timeout is configured for 60 seconds, by default.
                wait(70000);
            }
            getLog().trace("Main: done waiting");
            Map<String,Object> props = new HashMap<String,Object>();
            // This property does not have any effect on Derby for the locking
            // condition produced by this test.  Instead, Derby uses the 
            // lock timeout value specified in the config (pom.xml).  On Informix,
            // the dictionary level timeout (set above) will be used.
            if (!(dict instanceof InformixDictionary)) {
                props.put("javax.persistence.lock.timeout", 5000);
            }
            em.getTransaction().begin();
            getLog().trace("Main: refresh with force increment");
            em.refresh(ve, LockModeType.PESSIMISTIC_FORCE_INCREMENT, props);  
            getLog().trace("Main: commit");
            em.getTransaction().commit();
            getLog().trace("Main: done commit");
            fail("Expected LockTimeoutException");
        } catch (Throwable t) {
            getLog().trace("Main: exception - " + t.getMessage(), t);
            assertTrue( t instanceof LockTimeoutException);
        } finally {
            try {
                // Wake the thread and wait for the thread to finish
                synchronized(this) {
                    this.notify();
                }
                result.get();
            } catch (Throwable t) { 
                fail("Caught throwable waiting for thread finish: " + t);
            }
        }
    }
        
    /**
     * Assert that an exception of proper type has been thrown. Also checks that
     * that the exception has populated the failed object.
     * 
     * @param actual
     *            exception being thrown
     * @param expeceted
     *            type of the exception
     */
    void assertError(Throwable actual, Class<? extends Throwable> ... expected) {
		boolean matched = false;
		String expectedNames = "";
		for (Class<? extends Throwable> aExpected : expected) {
			expectedNames += aExpected.getName() + ", ";
			if (aExpected.isAssignableFrom(actual.getClass())) {
				matched = true;
			}
		}
		if (!matched) {
			actual.printStackTrace();
			throw new AssertionFailedError(actual.getClass().getName()
					+ " was raised but expecting one of the following: ["
					+ expectedNames.substring(0, expectedNames.length() - 2) + "]");
		}

        Object failed = getFailedObject(actual);
        assertNotNull("Failed object is null", failed);
        assertNotEquals("null", failed);
    }

    Object getFailedObject(Throwable e) {
        if (e instanceof LockTimeoutException) {
            return ((LockTimeoutException) e).getObject();
        }
        if (e instanceof PessimisticLockException) {
            return ((PessimisticLockException) e).getEntity();
        }
        if (e instanceof QueryTimeoutException) {
            return ((QueryTimeoutException) e).getQuery();
        }
        if (e instanceof OpenJPAException) {
            return ((OpenJPAException) e).getFailedObject();
        }
        return null;
    }

    /**
     * Separate execution thread used to forcing a lock condition on 
     * a row in the VersionEntity table.
     */
    public class RefreshWithLock implements Callable<Boolean> {

        private int _id;
        private Object _monitor;
        
        public RefreshWithLock(int id, Object monitor) {
            _id = id;
            _monitor = monitor;
        }
        
        public Boolean call() throws Exception {
            try {
                EntityManager em = emf.createEntityManager();
                
                em.getTransaction().begin();
                // Find with pessimistic force increment.  Will lock row for duration of TX.
                VersionEntity ve = em.find(VersionEntity.class, _id, LockModeType.PESSIMISTIC_FORCE_INCREMENT);
                assertTrue(em.getLockMode(ve) == LockModeType.PESSIMISTIC_FORCE_INCREMENT);
                // Wake up the main thread
                getLog().trace("Thread: wake up main thread");
                synchronized(_monitor) {
                    _monitor.notify();
                }
                // Wait up to 120 seconds for main thread to complete.  The default derby timeout is 60 seconds. 
                try {
                    getLog().trace("Thread: waiting up to 120 secs for notify");
                    synchronized(_monitor) {
                        _monitor.wait(120000);
                    }
                    getLog().trace("Thread: done waiting");
                } catch (Throwable t) {
                    getLog().trace("Unexpected thread interrupt",t);
                }
                
                em.getTransaction().commit();
                em.close();
                getLog().trace("Thread: done");
            } catch (Throwable t) {
                getLog().trace("Thread: caught - " + t.getMessage(), t);
            }
            return Boolean.TRUE;
        }
    }
}

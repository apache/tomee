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

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;

import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.persistence.test.SQLListenerTestCase;

/**
 * Test find, lock and refresh em methods against non-versioned entity.
 */
public class TestMixedLockManagerNonVersion extends SQLListenerTestCase {

    private String empTableName;

    @Override
    protected String getPersistenceUnitName() {
        return "locking-test";
    }

    public void setUp() {
        setUp(LockEmployeeNonVersion.class
            , "openjpa.LockManager", "mixed"
            , "openjpa.Optimistic", "false"
        );
        commonSetUp();
    }

    /*
     * Test em.find(lockMode) on non-versioned entity.
     */
    public void testFindNonVersionWithLock() {
        commonFindNonVerWithLock(LockModeType.NONE);
        commonFindNonVerWithLock(LockModeType.READ);
        commonFindNonVerWithLock(LockModeType.WRITE);
        commonFindNonVerWithLock(LockModeType.OPTIMISTIC);
        commonFindNonVerWithLock(LockModeType.OPTIMISTIC_FORCE_INCREMENT);
        commonFindNonVerWithLock(LockModeType.PESSIMISTIC_READ);
        commonFindNonVerWithLock(LockModeType.PESSIMISTIC_WRITE);
        commonFindNonVerWithLock(LockModeType.PESSIMISTIC_FORCE_INCREMENT);
    }
    
    private void commonFindNonVerWithLock(LockModeType lockMode) {
        EntityManager em = emf.createEntityManager();
        
        em.getTransaction().begin();
        LockEmployeeNonVersion emp = em.find(LockEmployeeNonVersion.class, 1,
            lockMode);
        assertNotNull(emp);
        String lastLastName = emp.getLastName();
        em.getTransaction().commit();
        
        em.clear();
        
        em.getTransaction().begin();
        emp = em.find(LockEmployeeNonVersion.class, 1);
        assertNotNull(emp);
        assertEquals(lastLastName, emp.getLastName());
        
        emp = em.find(LockEmployeeNonVersion.class, 1, lockMode);
        emp.setLastName(lockMode.toString());
        em.getTransaction().commit();
        
        emp = em.find(LockEmployeeNonVersion.class, 1);
        assertNotNull(emp);
        assertEquals(lockMode.toString(), emp.getLastName());
        
        em.close();
    }
    
    /*
     * Test em.lock(lockMode) on non-versioned entity.
     */
    public void testLockNonVersionWithLock() {
        commonLockNonVerWithLock(LockModeType.NONE);
        commonLockNonVerWithLock(LockModeType.READ);
        commonLockNonVerWithLock(LockModeType.WRITE);
        commonLockNonVerWithLock(LockModeType.OPTIMISTIC);
        commonLockNonVerWithLock(LockModeType.OPTIMISTIC_FORCE_INCREMENT);
        commonLockNonVerWithLock(LockModeType.PESSIMISTIC_READ);
        commonLockNonVerWithLock(LockModeType.PESSIMISTIC_WRITE);
        commonLockNonVerWithLock(LockModeType.PESSIMISTIC_FORCE_INCREMENT);
    }
    
    private void commonLockNonVerWithLock(LockModeType lockMode) {
        EntityManager em = emf.createEntityManager();
        
        em.getTransaction().begin();
        LockEmployeeNonVersion emp = em.find(LockEmployeeNonVersion.class, 1);
        assertNotNull(emp);
        em.lock(emp, lockMode);
        String lastLastName = emp.getLastName();
        em.getTransaction().commit();
        
        em.clear();
        
        em.getTransaction().begin();
        emp = em.find(LockEmployeeNonVersion.class, 1);
        assertNotNull(emp);
        assertEquals(lastLastName, emp.getLastName());
        
        emp = em.find(LockEmployeeNonVersion.class, 2);
        em.lock(emp, lockMode);
        emp.setLastName(lockMode.toString());
        em.getTransaction().commit();
        
        emp = em.find(LockEmployeeNonVersion.class, 2);
        assertNotNull(emp);
        assertEquals(lockMode.toString(), emp.getLastName());
        
        em.close();
    }
    
    /*
     * Test em.refresh(lockMode) on non-versioned entity.
     */
    public void testRefreshNonVersionWithLock() {
        commonRefreshNonVerWithLock(LockModeType.NONE);
        commonRefreshNonVerWithLock(LockModeType.READ);
        commonRefreshNonVerWithLock(LockModeType.WRITE);
        commonRefreshNonVerWithLock(LockModeType.OPTIMISTIC);
        commonRefreshNonVerWithLock(LockModeType.OPTIMISTIC_FORCE_INCREMENT);
        commonRefreshNonVerWithLock(LockModeType.PESSIMISTIC_READ);
        commonRefreshNonVerWithLock(LockModeType.PESSIMISTIC_WRITE);
        commonRefreshNonVerWithLock(LockModeType.PESSIMISTIC_FORCE_INCREMENT);
    }
    
    private void commonRefreshNonVerWithLock(LockModeType lockMode) {
        EntityManager em = emf.createEntityManager();
        
        em.getTransaction().begin();
        LockEmployeeNonVersion emp = em.find(LockEmployeeNonVersion.class, 1);
        assertNotNull(emp);
        String lastLastName = emp.getLastName();
        em.refresh(emp, lockMode);
        assertEquals(lastLastName, emp.getLastName());
        em.getTransaction().commit();
        
        em.clear();
        
        em.getTransaction().begin();
        emp = em.find(LockEmployeeNonVersion.class, 1);
        assertNotNull(emp);
        assertEquals(lastLastName, emp.getLastName());
        
        emp = em.find(LockEmployeeNonVersion.class, 3);
        em.refresh(emp, lockMode);
        emp.setLastName(lockMode.toString());
        em.getTransaction().commit();
        
        emp = em.find(LockEmployeeNonVersion.class, 3);
        assertNotNull(emp);
        assertEquals(lockMode.toString(), emp.getLastName());
        
        em.close();
    }
    
    protected void commonSetUp() {
        empTableName = getMapping(LockEmployeeNonVersion.class).getTable()
            .getFullName();

        cleanupDB();

        LockEmployeeNonVersion e1, e2, e3;
        e1 = newEmployee(1);
        e2 = newEmployee(2);
        e3 = newEmployee(3);

        resetSQL();
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            em.getTransaction().begin();
            em.persist(e1);
            em.persist(e2);
            em.persist(e3);
            em.getTransaction().commit();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        assertAllSQLInOrder("INSERT INTO " + empTableName + " .*");
    }

    private void cleanupDB() {
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            em.getTransaction().begin();

            em.createQuery("delete from " + empTableName).executeUpdate();

            em.getTransaction().commit();
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    private LockEmployeeNonVersion newEmployee(int id) {
        LockEmployeeNonVersion e = new LockEmployeeNonVersion();
        e.setId(id);
        return e;
    }

    protected Log getLog() {
        return emf.getConfiguration().getLog("Tests");
    }

    protected Log getDumpStackLog() {
        return emf.getConfiguration().getLog("DumpStack");
    }

    protected void logStack(Throwable t) {
        StringWriter str = new StringWriter();
        PrintWriter print = new PrintWriter(str);
        t.printStackTrace(print);
        getDumpStackLog().trace(str.toString());
    }
}

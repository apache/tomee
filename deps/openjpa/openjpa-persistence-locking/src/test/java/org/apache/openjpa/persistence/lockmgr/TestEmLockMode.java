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
import javax.persistence.TransactionRequiredException;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.kernel.JDBCFetchConfigurationImpl;
import org.apache.openjpa.jdbc.sql.DB2Dictionary;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.persistence.EntityManagerImpl;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerSPI;

/**
 * Test hints using EntityManager interface.
 */
public class TestEmLockMode extends SequencedActionsTest {
    private static String NON_SUPPORTED_OPTIMISTIC_SQL = 
        "SELECT .* FROM LockEmployee .*";
    private static String NON_SUPPORTED_FOR_UPDATE_SQL = "" ; // append lock clause from dict
    private static String VERSION_UPDATE_SQL = 
        "UPDATE LockEmployee SET version .* WHERE .*";
    private static String DB2_OPTIMISTIC_SQL = 
        "SELECT .* FROM LockEmployee .* WHERE .*";
    private static String DB2_PESSIMISTIC_RS_SQL = 
        "SELECT .* FROM LockEmployee .* WITH RS USE .*";
    private static String DB2_PESSIMISTIC_RR_SQL = 
        "SELECT .* FROM LockEmployee .* WITH RR USE .*";

    public void setUp() {
        setUp(LockEmployee.class, "openjpa.LockManager", "mixed");
        commonSetUp();
        NON_SUPPORTED_FOR_UPDATE_SQL = NON_SUPPORTED_OPTIMISTIC_SQL + " " + escapeRegex(getForUpdateClause()) + ".*";
    }
    
    private String escapeRegex(String clause) {
        // escape an update clause for use in a regex. 
        // only handling ( ) for now
        String rval = clause.replace("(", "\\(");
        rval = rval.replace(")", "\\)");
        return rval;
    }

    /*
     * Test em.find(lockmode);
     */
    public void testFindLockModeIsolations() {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();

        commonTestFindLockModeIsolations(em, LockModeType.NONE, 1,
            DB2_OPTIMISTIC_SQL, 1, NON_SUPPORTED_OPTIMISTIC_SQL, 0, null);
        commonTestFindLockModeIsolations(em, LockModeType.READ, 1,
            DB2_OPTIMISTIC_SQL, 1, NON_SUPPORTED_OPTIMISTIC_SQL, 1,
            NON_SUPPORTED_OPTIMISTIC_SQL);
        commonTestFindLockModeIsolations(em, LockModeType.WRITE, 1,
            DB2_OPTIMISTIC_SQL, 1, NON_SUPPORTED_OPTIMISTIC_SQL, 1,
            VERSION_UPDATE_SQL);
        commonTestFindLockModeIsolations(em, LockModeType.OPTIMISTIC, 1,
            DB2_OPTIMISTIC_SQL, 1, NON_SUPPORTED_OPTIMISTIC_SQL, 1,
            NON_SUPPORTED_OPTIMISTIC_SQL);
        commonTestFindLockModeIsolations(em,
            LockModeType.OPTIMISTIC_FORCE_INCREMENT, 1, DB2_OPTIMISTIC_SQL, 1,
            NON_SUPPORTED_OPTIMISTIC_SQL, 1, VERSION_UPDATE_SQL);
        commonTestFindLockModeIsolations(em, LockModeType.PESSIMISTIC_READ, 2,
            DB2_PESSIMISTIC_RS_SQL, 2, NON_SUPPORTED_FOR_UPDATE_SQL, 1,
            NON_SUPPORTED_OPTIMISTIC_SQL);
        commonTestFindLockModeIsolations(em, LockModeType.PESSIMISTIC_WRITE, 2,
            DB2_PESSIMISTIC_RR_SQL, 2, NON_SUPPORTED_FOR_UPDATE_SQL, 1,
            NON_SUPPORTED_OPTIMISTIC_SQL);
        commonTestFindLockModeIsolations(em,
            LockModeType.PESSIMISTIC_FORCE_INCREMENT, 2,
            DB2_PESSIMISTIC_RR_SQL, 2, NON_SUPPORTED_FOR_UPDATE_SQL, 1,
            VERSION_UPDATE_SQL);

        em.getTransaction().rollback();
        em.close();
    }

    private void commonTestFindLockModeIsolations(EntityManager em,
        LockModeType lockMode, int expectedSupportSQLCount,
        String expectedSupportSQL, int expectedNonSupportSQLCount,
        String expectedNonSupportSQL, int expectedVersionUpdateCount,
        String expectedVersionUpdateSQL) {
        OpenJPAEntityManager oem = (OpenJPAEntityManager) em.getDelegate();
        JDBCFetchConfigurationImpl fConfig = (JDBCFetchConfigurationImpl)
            ((EntityManagerImpl) oem).getBroker().getFetchConfiguration();
        DBDictionary dict = ((JDBCConfiguration) ((OpenJPAEntityManagerSPI) oem)
            .getConfiguration()).getDBDictionaryInstance();

        em.clear();
        resetSQL();
        int beforeIsolation = fConfig.getIsolation();
        em.find(LockEmployee.class, 1, lockMode);
        if (dict.supportsIsolationForUpdate() && 
            dict instanceof DB2Dictionary) {
            assertEquals(expectedSupportSQLCount, getSQLCount());
            assertAllSQLInOrder(expectedSupportSQL);
        } else {
            assertEquals(expectedNonSupportSQLCount, getSQLCount());
            assertAllSQLInOrder(expectedNonSupportSQL);
        }

        resetSQL();
        em.flush();
        assertEquals(expectedVersionUpdateCount, getSQLCount());
        if (expectedVersionUpdateSQL != null)
            assertAllSQLInOrder(expectedVersionUpdateSQL);

        assertEquals(beforeIsolation, fConfig.getIsolation());
    }

    /*
     * Test em.refresh(lockmode);
     */
    public void testRefreshLockModeIsolations() {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();

        commonTestRefreshLockModeIsolations(em, LockModeType.NONE, 1,
            DB2_OPTIMISTIC_SQL, 1, NON_SUPPORTED_OPTIMISTIC_SQL, 0, null);
        commonTestRefreshLockModeIsolations(em, LockModeType.READ, 1,
            DB2_OPTIMISTIC_SQL, 1, NON_SUPPORTED_OPTIMISTIC_SQL, 1,
            NON_SUPPORTED_OPTIMISTIC_SQL);
        commonTestRefreshLockModeIsolations(em, LockModeType.WRITE, 1,
            DB2_OPTIMISTIC_SQL, 1, NON_SUPPORTED_OPTIMISTIC_SQL, 1,
            VERSION_UPDATE_SQL);
        commonTestRefreshLockModeIsolations(em, LockModeType.OPTIMISTIC, 1,
            DB2_OPTIMISTIC_SQL, 1, NON_SUPPORTED_OPTIMISTIC_SQL, 1,
            NON_SUPPORTED_OPTIMISTIC_SQL);
        commonTestRefreshLockModeIsolations(em,
            LockModeType.OPTIMISTIC_FORCE_INCREMENT, 1, DB2_OPTIMISTIC_SQL, 1,
            NON_SUPPORTED_OPTIMISTIC_SQL, 1, VERSION_UPDATE_SQL);
        commonTestRefreshLockModeIsolations(em, LockModeType.PESSIMISTIC_READ,
            2, DB2_PESSIMISTIC_RS_SQL, 2, NON_SUPPORTED_FOR_UPDATE_SQL, 1,
            NON_SUPPORTED_OPTIMISTIC_SQL);
        commonTestRefreshLockModeIsolations(em, LockModeType.PESSIMISTIC_WRITE,
            2, DB2_PESSIMISTIC_RR_SQL, 2, NON_SUPPORTED_FOR_UPDATE_SQL, 1,
            NON_SUPPORTED_OPTIMISTIC_SQL);
        commonTestRefreshLockModeIsolations(em,
            LockModeType.PESSIMISTIC_FORCE_INCREMENT, 2,
            DB2_PESSIMISTIC_RR_SQL, 2, NON_SUPPORTED_FOR_UPDATE_SQL, 1,
            VERSION_UPDATE_SQL);

        em.getTransaction().rollback();
        em.close();
    }

    private void commonTestRefreshLockModeIsolations(EntityManager em,
        LockModeType lockMode, int expectedSupportSQLCount,
        String expectedSupportSQL, int expectedNonSupportSQLCount,
        String expectedNonSupportSQL, int expectedVersionUpdateCount,
        String expectedVersionUpdateSQL) {
        OpenJPAEntityManager oem = (OpenJPAEntityManager) em.getDelegate();
        JDBCFetchConfigurationImpl fConfig = (JDBCFetchConfigurationImpl) 
            ((EntityManagerImpl) oem).getBroker().getFetchConfiguration();
        DBDictionary dict = ((JDBCConfiguration) ((OpenJPAEntityManagerSPI) oem)
            .getConfiguration()).getDBDictionaryInstance();

        em.clear();
        LockEmployee employee = em.find(LockEmployee.class, 1);
        resetSQL();
        int beforeIsolation = fConfig.getIsolation();
        em.refresh(employee, lockMode);
        if (dict.supportsIsolationForUpdate() && 
            dict instanceof DB2Dictionary) {
            assertEquals(expectedSupportSQLCount, getSQLCount());
            assertAllSQLInOrder(expectedSupportSQL);
        } else {
            assertEquals(expectedNonSupportSQLCount, getSQLCount());
            assertAllSQLInOrder(expectedNonSupportSQL);
        }

        resetSQL();
        em.flush();
        assertEquals(expectedVersionUpdateCount, getSQLCount());
        if (expectedVersionUpdateSQL != null)
            assertAllSQLInOrder(expectedVersionUpdateSQL);

        assertEquals(beforeIsolation, fConfig.getIsolation());
    }

    /*
     * Test em.lock(lockmode);
     */
    public void testLockLockModeIsolations() {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();

        commonTestLockLockModeIsolations(em, LockModeType.NONE, 0, null, 0,
            null, 0, null);
        commonTestLockLockModeIsolations(em, LockModeType.READ, 0, null, 0,
            null, 1, NON_SUPPORTED_OPTIMISTIC_SQL);
        commonTestLockLockModeIsolations(em, LockModeType.WRITE, 0, null, 0,
            null, 1, VERSION_UPDATE_SQL);
        commonTestLockLockModeIsolations(em, LockModeType.OPTIMISTIC, 0, null,
            0, null, 1, NON_SUPPORTED_OPTIMISTIC_SQL);
        commonTestLockLockModeIsolations(em,
            LockModeType.OPTIMISTIC_FORCE_INCREMENT, 0, null, 0, null, 1,
            VERSION_UPDATE_SQL);
        commonTestLockLockModeIsolations(em, LockModeType.PESSIMISTIC_READ, 2,
            DB2_PESSIMISTIC_RS_SQL, 2, NON_SUPPORTED_FOR_UPDATE_SQL, 1,
            NON_SUPPORTED_OPTIMISTIC_SQL);
        commonTestLockLockModeIsolations(em, LockModeType.PESSIMISTIC_WRITE, 2,
            DB2_PESSIMISTIC_RR_SQL, 2, NON_SUPPORTED_FOR_UPDATE_SQL, 1,
            NON_SUPPORTED_OPTIMISTIC_SQL);
        commonTestLockLockModeIsolations(em,
            LockModeType.PESSIMISTIC_FORCE_INCREMENT, 2,
            DB2_PESSIMISTIC_RR_SQL, 2, NON_SUPPORTED_FOR_UPDATE_SQL, 1,
            VERSION_UPDATE_SQL);

        em.getTransaction().rollback();
        em.close();
    }

    private void commonTestLockLockModeIsolations(EntityManager em,
        LockModeType lockMode, int expectedSupportSQLCount,
        String expectedSupportSQL, int expectedNonSupportSQLCount,
        String expectedNonSupportSQL, int expectedVersionUpdateCount,
        String expectedVersionUpdateSQL) {
        OpenJPAEntityManager oem = (OpenJPAEntityManager) em.getDelegate();
        JDBCFetchConfigurationImpl fConfig = (JDBCFetchConfigurationImpl)
            ((EntityManagerImpl) oem).getBroker().getFetchConfiguration();
        DBDictionary dict = ((JDBCConfiguration) ((OpenJPAEntityManagerSPI) oem)
            .getConfiguration()).getDBDictionaryInstance();

        em.clear();
        LockEmployee employee = em.find(LockEmployee.class, 1);
        resetSQL();
        int beforeIsolation = fConfig.getIsolation();
        em.lock(employee, lockMode);
        if (dict.supportsIsolationForUpdate() && 
            dict instanceof DB2Dictionary) {
            assertEquals(expectedSupportSQLCount, getSQLCount());
            if (expectedSupportSQL != null)
                assertAllSQLInOrder(expectedSupportSQL);
        } else {
            assertEquals(expectedNonSupportSQLCount, getSQLCount());
            if (expectedNonSupportSQL != null)
                assertAllSQLInOrder(expectedNonSupportSQL);
        }
        
        resetSQL();
        em.flush();
        assertEquals(expectedVersionUpdateCount, getSQLCount());
        if (expectedVersionUpdateSQL != null)
            assertAllSQLInOrder(expectedVersionUpdateSQL);

        assertEquals(beforeIsolation, fConfig.getIsolation());
    }

    /*
     * Test em.getLockMode();
     */
    public void testGetLockMode() {
        EntityManager em = emf.createEntityManager();

        LockEmployee employee = em.find(LockEmployee.class, 1);
        try {
            em.getLockMode(employee);
            fail("Expecting TransactionRequiredException.");
        } catch (TransactionRequiredException tre) {
        } catch (Exception e){
            fail("Expecting TransactionRequiredException.");
        }

        em.getTransaction().begin();
        try {
            assertEquals("getLockMode only allows in transaction.",LockModeType.NONE, em.getLockMode(employee));
        } catch (Exception e){
            fail("Do not expecting any exception.");
        }        
        em.getTransaction().rollback();

        em.clear();
        em.getTransaction().begin();
        try {
            // getLockMode on a detached entity;
            em.getLockMode(employee);
            fail("Expecting IllegalArgumentException for getLockMode on a detached entity in an active transaction.");
        } catch (IllegalArgumentException iae) {
        } catch (Exception e){
            fail("Expecting IllegalArgumentException for getLockMode on a detached entity in an active transaction.");
        }        
        em.getTransaction().rollback();

        em.getTransaction().begin();
        try {
            employee = em.find(LockEmployee.class, 1, LockModeType.PESSIMISTIC_WRITE);
            assertEquals("Test getLockMode on non-NONE lock mode type.", LockModeType.PESSIMISTIC_WRITE, em
                    .getLockMode(employee));
        } catch (Exception e){
            fail("Do not expecting any exception.");
        }        
        em.getTransaction().rollback();

        em.close();
    }
}

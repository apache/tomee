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
package org.apache.openjpa.persistence.lock.extended;

import javax.persistence.EntityManager;

/**
 * LockScopeTestCase subclass to test entities:
 * - with Basic attributes 
 * - uses secondary table
 * - uses inheritance in single table
 * - uses inheritance and join table
 * - uses element collection - lazy fetch (default) 
 * - uses element collection - eager fetch 
 */
public class TestBasicLockScope extends LockScopeTestCase {

    public void setUp() {
        setSupportedDatabases(
                org.apache.openjpa.jdbc.sql.DerbyDictionary.class,
                org.apache.openjpa.jdbc.sql.OracleDictionary.class,
                org.apache.openjpa.jdbc.sql.DB2Dictionary.class);
        if (isTestsDisabled()) {
            return;
        }

        setUp(LSEBase.class
            , LSESecTbl.class
            , LSESngTblCon.class
            , LSESngTblAbs.class
            , LSEJoinCon.class
            , LSEJoinAbs.class
            , LSEEleCol.class
            , LSEEleColEgr.class
            , "openjpa.LockManager", "mixed",
            "openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)"
        );
        commonSetUp(LSEBase.class
            , LSESecTbl.class
            , LSESngTblCon.class
            , LSESngTblAbs.class
            , LSEJoinCon.class
            , LSEJoinAbs.class
            , LSEEleCol.class
            , LSEEleColEgr.class
        );
    }

    public void testNormalBasicLock() {
        commonBasicLock("testNormalBasicLock", 000, false);
    }

    public void testExtendedBasicLock() {
        commonBasicLock("testExtendedBasicLock", 010, true);
    }

    private void commonBasicLock(String testName, int id0, boolean extended) {
        final String tableName = "LSEBase";
        getLog().info("** " + testName + "()");
        String scope = extended ? "Extended" : "Normal";
        int id1 = id0 + 1;

        // create test entity.
        LSEBase e0 = new LSEBase();
        e0.setId(id0);
        e0.setFirstName("firstName " + id0);
        e0.setLastName("lastName " + id0);
        LSEBase e1 = new LSEBase();
        e1.setId(id1);
        e1.setFirstName("firstName " + id1);
        e1.setLastName("lastName " + id1);

        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            em.getTransaction().begin();
            em.persist(e0);
            em.persist(e1);
            em.getTransaction().commit();
        } finally {
            em = null;
            e0 = e1 = null;
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        commonLockTest(testName, LSEBase.class, id0, extended,
                "SELECT c FROM LSEBase c WHERE c.firstName LIKE :firstName", "findLSEBase" + scope,
                new AssertCallback() {
                    public void findNoLockDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.version, t0.firstName, t0.lastName FROM LSEBase t0 WHERE t0.id = ?
                            //      optimize for 1 row [params=(int) 0]
                            assertLockTestSQLs(Select + tableName + Where + NoDB2Lock);
                            break;
                        case derby:
                            // SELECT t0.version, t0.firstName, t0.lastName FROM LSEBase t0 WHERE t0.id = ? 
                            //      [params=(int) 0]
                        case oracle:
                            // SELECT t0.version, t0.firstName, t0.lastName FROM LSEBase t0 WHERE t0.id = ? 
                            //      [params=(int) 0]
                        default:
                            assertLockTestSQLs(Select + tableName + Where + NoForUpdate);
                        }
                    }

                    public void findPessimisticForcIncDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.version, t0.firstName, t0.lastName FROM LSEBase t0 WHERE t0.id = ?
                            //      optimize for 1 row FOR READ ONLY WITH RR USE AND KEEP UPDATE LOCKS [params=(int) 1]
                            // SELECT t0.version FROM LSEBase t0 WHERE t0.id = ?
                            //      FOR READ ONLY WITH RR USE AND KEEP UPDATE LOCKS [params=(int) 1]
                            assertLockTestSQLs(Select + tableName + Where + DB2Lock);
                            break;
                        case derby:
                            // SELECT t0.version, t0.firstName, t0.lastName FROM
                            //     LSEBase t0 WHERE t0.id = ? FOR UPDATE WITH RR [params=(int) 1]
                        case oracle:
                            // SELECT t0.version, t0.firstName, t0.lastName FROM LSEBase t0 WHERE t0.id = ? FOR UPDATE 
                            //      [params=(int) 1]
                            // SELECT t0.version FROM LSEBase t0 WHERE t0.id = ? FOR UPDATE [params=(int) 1]
                        default:
                            assertLockTestSQLs(Select + tableName + Where + ForUpdate);
                        }
                    }

                    public void queryPessimisticReadDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.id, t0.version, t0.firstName, t0.lastName FROM LSEBase t0
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\')
                            //      FOR READ ONLY WITH RS USE AND KEEP UPDATE LOCKS [params=(String) firstName%0]
                            // SELECT t0.version FROM LSEBase t0 WHERE t0.id = ?  [params=(int) 0]
                            assertLockTestSQLs(Select + tableName + Where + DB2Lock);
                            break;
                        case derby:
                            // SELECT t0.id, t0.version, t0.firstName, t0.lastName FROM LSEBase t0 WHERE (t0.firstName
                            //      LIKE ? ESCAPE '\') FOR UPDATE WITH RR [params=(String) firstName%0]
                        case oracle:
                            // SELECT t0.id, t0.version, t0.firstName, t0.lastName FROM LSEBase t0 
                            //      WHERE (t0.firstName LIKE ?) FOR UPDATE [params=(String) firstName%0]
                            // SELECT t0.version FROM LSEBase t0 WHERE t0.id = ? [params=(int) 0]
                        default:
                            assertLockTestSQLs(Select + tableName + Where + ForUpdate);
                        }
                    }

                    public void findNoLockAfterQueryPessimisticReadDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.version, t0.firstName, t0.lastName FROM LSEBase t0 WHERE t0.id = ?
                            //      optimize for 1 row [params=(int) 1]
                            assertLockTestSQLs(Select + tableName + Where + NoDB2Lock);
                            break;
                        case derby:
                            // SELECT t0.version, t0.firstName, t0.lastName FROM LSEBase t0 WHERE t0.id = ?
                            //      [params=(int) 1]
                        case oracle:
                            // SELECT t0.version, t0.firstName, t0.lastName FROM LSEBase t0 WHERE t0.id = ? 
                            //      [params=(int) 1]
                        default:
                            assertLockTestSQLs(Select + tableName + Where + NoForUpdate);
                        }
                    }

                    public void namedQueryPessimisticWriteDbSql(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.id, t0.version, t0.firstName, t0.lastName FROM LSEBase t0
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\')
                            //      FOR READ ONLY WITH RR USE AND KEEP UPDATE LOCKS [params=(String) firstName%0]
                            // SELECT t0.version FROM LSEBase t0 WHERE t0.id = ?  [params=(int) 0]
                            assertLockTestSQLs(Select + tableName + Where + DB2Lock);
                            break;
                        case derby:
                            // SELECT t0.id, t0.version, t0.firstName, t0.lastName FROM LSEBase t0
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\') FOR UPDATE WITH RR
                        case oracle:
                            // SELECT t0.id, t0.version, t0.firstName, t0.lastName FROM LSEBase t0 
                            //      WHERE (t0.firstName LIKE ?) FOR UPDATE [params=(String) firstName%0]
                            // SELECT t0.version FROM LSEBase t0 WHERE t0.id = ? [params=(int) 0]
                        default:
                            assertLockTestSQLs(Select + tableName + Where + ForUpdate);
                        }
                    }

                    public void findNoLockAfterNamedQueryPessimisticWriteDbSql(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.version, t0.firstName, t0.lastName FROM LSEBase t0 WHERE t0.id = ?
                            //      optimize for 1 row [params=(int) 1]
                            assertLockTestSQLs(Select + tableName + Where + NoDB2Lock);
                            break;
                        case derby:
                            // SELECT t0.version, t0.firstName, t0.lastName FROM LSEBase t0 WHERE t0.id = ?
                        case oracle:
                            // SELECT t0.version, t0.firstName, t0.lastName FROM LSEBase t0 WHERE t0.id = ? 
                            //      [params=(int) 1]
                        default:
                            assertLockTestSQLs(Select + tableName + Where + NoForUpdate);
                        }
                    }
                });
    }

    public void testNormalSecTableLock() {
        commonSecTableLock("testNormalSecTableLock", 100, false);
    }
    
    public void testExtendedSecTableLock() {
        commonSecTableLock("testExtendedSecTableLock", 110, true); 
    }

    private void commonSecTableLock(String testName, int id0, boolean extended) {
        final String table1Name = "LSESecTbl";
        final String table2Name = "LSESecTblDtl";
        final String joinTables = table1Name + ".*JOIN.*" + table2Name;
        getLog().info("** " + testName + "()");
        String scope = extended ? "Extended" : "Normal";
        int id1 = id0 + 1;

        // create test entity.
        LSESecTbl e0 = new LSESecTbl();
        e0.setId(id0);
        e0.setFirstName("firstName " + id0);
        e0.setLastName("lastName " + id0);
        LSESecTbl e1 = new LSESecTbl();
        e1.setId(id1);
        e1.setFirstName("firstName " + id1);
        e1.setLastName("lastName " + id1);

        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            em.getTransaction().begin();
            em.persist(e0);
            em.persist(e1);
            em.getTransaction().commit();
        } finally {
            em = null;
            e0 = e1 = null;
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        commonLockTest(testName, LSESecTbl.class, id0, extended,
                "SELECT c FROM LSESecTbl c WHERE c.firstName LIKE :firstName", "findLSESecTbl" + scope,
                new AssertCallback() {
                    public void findNoLockDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.version, t0.firstName, t1.lastName FROM LSESecTbl t0
                            //      INNER JOIN LSESecTblDtl t1 ON t0.id = t1.LSESECTBL_ID WHERE t0.id = ?
                            //      optimize for 1 row [params=(int) 100]
                            assertLockTestSQLs(Select + joinTables + Where + NoDB2Lock);
                            break;
                        case oracle:
                            // SELECT t0.version, t0.firstName, t1.lastName FROM LSESecTbl t0, LSESecTblDtl t1 
                            //      WHERE t0.id = ? AND t0.id = t1.LSESECTBL_ID [params=(int) 100]
                            assertLockTestSQLs(Select + table1Name + Any + table2Name + Where + NoForUpdate);
                            break;
                        case derby:
                            // SELECT t0.version, t0.firstName, t1.lastName FROM LSESecTbl t0
                            //      INNER JOIN LSESecTblDtl t1 ON t0.id = t1.LSESECTBL_ID WHERE t0.id = ?
                            //      [params=(int) 100]
                        default:
                            assertLockTestSQLs(Select + joinTables + Where + NoForUpdate);
                        }
                    }

                    public void findPessimisticForcIncDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.version, t0.firstName, t1.lastName FROM LSESecTbl t0
                            //      INNER JOIN LSESecTblDtl t1 ON t0.id = t1.LSESECTBL_ID WHERE t0.id = ?
                            //      optimize for 1 row FOR READ ONLY WITH RR USE AND KEEP UPDATE LOCKS [params=(int) 101]
                            // SELECT t0.version FROM LSESecTbl t0 WHERE t0.id = ?
                            //      FOR READ ONLY WITH RR USE AND KEEP UPDATE LOCKS [params=(int) 101]
                            assertLockTestSQLs(Select + joinTables + Where + DB2Lock);
                            break;
                        case oracle:
                            // SELECT t0.version, t0.firstName, t1.lastName FROM LSESecTbl t0, LSESecTblDtl t1 
                            //      WHERE t0.id = ? AND t0.id = t1.LSESECTBL_ID FOR UPDATE [params=(int) 101]
                            // SELECT t0.version FROM LSESecTbl t0 WHERE t0.id = ? FOR UPDATE [params=(int) 101]
                            assertLockTestSQLs(Select + table1Name + Any + table2Name + Where + ForUpdate);
                            break;
                        case derby:
                            // The database is unable to lock this query.  Each object matching the query will be 
                            //  locked individually after it is loaded; however, it is technically possible that
                            //  another transaction could modify the data before the lock is obtained.
                            // SELECT t0.version, t0.firstName, t1.lastName FROM LSESecTbl t0
                            //      INNER JOIN LSESecTblDtl t1 ON t0.id = t1.LSESECTBL_ID WHERE t0.id = ?
                            //      [params=(int) 101]
                            // SELECT t0.id FROM LSESecTbl t0 WHERE t0.id = ? FOR UPDATE WITH RR [params=(int) 101]
                            // SELECT t0.id FROM LSESecTblDtl t0 WHERE t0.id = ? FOR UPDATE WITH RR [params=(int) 101]
                            // SELECT t0.version FROM LSESecTbl t0 WHERE t0.id = ? FOR UPDATE WITH RR [params=(int) 101]
                            assertLockTestSQLs(Select + joinTables + Where + NoForUpdate,
                                    Select + NoJoin + Any + table1Name + Any + NoJoin + Where + ForUpdate,
                                    Select + NoJoin + Any + table2Name + Any + NoJoin + Where + ForUpdate);
                            break;
                        default:
                            assertLockTestSQLs(Select + joinTables + Where + ForUpdate);
                        }
                    }

                    public void queryPessimisticReadDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.id, t0.version, t0.firstName, t1.lastName FROM LSESecTbl t0
                            //      INNER JOIN LSESecTblDtl t1 ON t0.id = t1.LSESECTBL_ID
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\')
                            //      FOR READ ONLY WITH RS USE AND KEEP UPDATE LOCKS [params=(String) firstName%100]
                            // SELECT t0.version FROM LSESecTbl t0 WHERE t0.id = ?  [params=(int) 100]
                            assertLockTestSQLs(Select + joinTables + Where + DB2Lock);
                            break;
                        case oracle:
                            // SELECT t0.id, t0.version, t0.firstName, t1.lastName FROM LSESecTbl t0, LSESecTblDtl t1 
                            //      WHERE (t0.firstName LIKE ?) AND t0.id = t1.LSESECTBL_ID FOR UPDATE 
                            //      [params=(String) firstName%100]
                            // SELECT t0.version FROM LSESecTbl t0 WHERE t0.id = ? [params=(int) 100]
                            assertLockTestSQLs(Select + table1Name + Any + table2Name + Where + ForUpdate);
                            break;
                        case derby:
                            // The database is unable to lock this query.  Each object matching the query will be 
                            //  locked individually after it is loaded; however, it is technically possible that
                            //  another transaction could modify the data before the lock is obtained.
                            // SELECT t0.id, t0.version, t0.firstName, t1.lastName FROM LSESecTbl t0
                            //      INNER JOIN LSESecTblDtl t1 ON t0.id = t1.LSESECTBL_ID 
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\') [params=(String) firstName%100]
                            // SELECT t0.id FROM LSESecTbl t0 WHERE t0.id = ? FOR UPDATE WITH RR [params=(int) 100]
                            // SELECT t0.id FROM LSESecTblDtl t0 WHERE t0.id = ? FOR UPDATE WITH RR [params=(int) 100]
                            // SELECT t0.version FROM LSESecTbl t0 WHERE t0.id = ? [params=(int) 100]
                            assertLockTestSQLs(Select + joinTables + Where + NoForUpdate,
                                    Select + NoJoin + Any + table1Name + Any + NoJoin + Where + ForUpdate,
                                    Select + NoJoin + Any + table2Name + Any + NoJoin + Where + ForUpdate);
                            break;
                        default:
                            assertLockTestSQLs(Select + joinTables + Where + ForUpdate);
                        }
                    }

                    public void findNoLockAfterQueryPessimisticReadDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.version, t0.firstName, t1.lastName FROM LSESecTbl t0 
                            //      INNER JOIN LSESecTblDtl t1 ON t0.id = t1.LSESECTBL_ID WHERE t0.id = ?
                            //      optimize for 1 row [params=(int) 101]
                            assertLockTestSQLs(Select + joinTables + Where + NoDB2Lock);
                            break;
                        case oracle:
                            // SELECT t0.version, t0.firstName, t1.lastName FROM LSESecTbl t0, LSESecTblDtl t1 
                            //      WHERE t0.id = ? AND t0.id = t1.LSESECTBL_ID [params=(int) 101]
                            assertLockTestSQLs(Select + table1Name + Any + table2Name + Where + NoForUpdate);
                            break;
                        case derby:
                            // SELECT t0.version, t0.firstName, t1.lastName FROM LSESecTbl t0
                            //      INNER JOIN LSESecTblDtl t1 ON t0.id = t1.LSESECTBL_ID WHERE t0.id = ?
                            //      [params=(int) 101]
                        default:
                            assertLockTestSQLs(Select + joinTables + Where + NoForUpdate);
                        }
                    }

                    public void namedQueryPessimisticWriteDbSql(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.id, t0.version, t0.firstName, t1.lastName FROM LSESecTbl t0
                            //      INNER JOIN LSESecTblDtl t1 ON t0.id = t1.LSESECTBL_ID
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\')
                            //      FOR READ ONLY WITH RR USE AND KEEP UPDATE LOCKS [params=(String) firstName%100]
                            // SELECT t0.version FROM LSESecTbl t0 WHERE t0.id = ?  [params=(int) 100]
                            assertLockTestSQLs(Select + joinTables + Where + DB2Lock);
                            break;
                        case oracle:
                            // SELECT t0.id, t0.version, t0.firstName, t1.lastName FROM LSESecTbl t0, LSESecTblDtl t1 
                            //      WHERE (t0.firstName LIKE ?) AND t0.id = t1.LSESECTBL_ID FOR UPDATE 
                            //      [params=(String) firstName%100]
                            // SELECT t0.version FROM LSESecTbl t0 WHERE t0.id = ? [params=(int) 100]
                            assertLockTestSQLs(Select + table1Name + Any + table2Name + Where + ForUpdate);
                            break;
                        case derby:
                            // The database is unable to lock this query.  Each object matching the query will be 
                            //  locked individually after it is loaded; however, it is technically possible that
                            //  another transaction could modify the data before the lock is obtained.
                            // SELECT t0.id, t0.version, t0.firstName, t1.lastName FROM LSESecTbl t0
                            //      INNER JOIN LSESecTblDtl t1 ON t0.id = t1.LSESECTBL_ID
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\') [params=(String) firstName%100]
                            // SELECT t0.id FROM LSESecTbl t0 WHERE t0.id = ? FOR UPDATE WITH RR [params=(int) 100]
                            // SELECT t0.id FROM LSESecTblDtl t0 WHERE t0.id = ? FOR UPDATE WITH RR [params=(int) 100]
                            // SELECT t0.version FROM LSESecTbl t0 WHERE t0.id = ? [params=(int) 100]
                            assertLockTestSQLs(Select + joinTables + Where + NoForUpdate,
                                    Select + NoJoin + Any + table1Name + Any + NoJoin + Where + ForUpdate,
                                    Select + NoJoin + Any + table2Name + Any + NoJoin + Where + ForUpdate);
                            break;
                        default:
                            assertLockTestSQLs(Select + joinTables + Where + ForUpdate);
                        }
                    }

                    public void findNoLockAfterNamedQueryPessimisticWriteDbSql(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.version, t0.firstName, t1.lastName FROM LSESecTbl t0
                            //      INNER JOIN LSESecTblDtl t1 ON t0.id = t1.LSESECTBL_ID WHERE t0.id = ?
                            //      optimize for 1 row [params=(int) 101]
                            assertLockTestSQLs(Select + joinTables + Where + NoDB2Lock);
                            break;
                        case oracle:
                            // SELECT t0.version, t0.firstName, t1.lastName FROM LSESecTbl t0, LSESecTblDtl t1 
                            //      WHERE t0.id = ? AND t0.id = t1.LSESECTBL_ID [params=(int) 101]
                            assertLockTestSQLs(Select + table1Name + Any + table2Name + Where + NoForUpdate);
                            break;
                        case derby:
                            // SELECT t0.version, t0.firstName, t1.lastName FROM LSESecTbl t0
                            //      INNER JOIN LSESecTblDtl t1 ON t0.id = t1.LSESECTBL_ID WHERE t0.id = ?
                            //      [params=(int) 101]
                        default:
                            assertLockTestSQLs(Select + joinTables + Where + NoForUpdate);
                        }
                    }
                });
    }

    public void testNormalSingleTableLock() {
        commonSingleTableLock("testNormalSingleTableLock", 200, false);
    }
    
    public void testExtendedlSingleTableLock() {
        commonSingleTableLock("testExtendedlSingleTableLock", 210, true);
    }
    
    private void commonSingleTableLock(String testName, int id0, boolean extended) {
        final String tableName = "LSESngTblAbs";
        getLog().info("** " + testName + "()");
        String scope = extended ? "Extended" : "Normal";
        int id1 = id0 + 1;

        // create test entity.
        LSESngTblCon e0 = new LSESngTblCon();
        e0.setId(id0);
        e0.setFirstName("firstName " + id0);
        e0.setLastName("lastName " + id0);
        LSESngTblCon e1 = new LSESngTblCon();
        e1.setId(id1);
        e1.setFirstName("firstName " + id1);
        e1.setLastName("lastName " + id1);

        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            em.getTransaction().begin();
            em.persist(e0);
            em.persist(e1);
            em.getTransaction().commit();
        } finally {
            em = null;
            e0 = e1 = null;
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        commonLockTest(testName, LSESngTblCon.class, id0, extended,
                "SELECT c FROM LSESngTblAbs c WHERE c.firstName LIKE :firstName",
                "findLSESngTblCon" + scope, new AssertCallback() {

                    public void findNoLockDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.DTYPE, t0.version, t0.firstName, t0.lastName FROM LSESngTblAbs t0
                            //      WHERE t0.DTYPE = ? AND t0.id = ?
                            //      optimize for 1 row [params=(String) LSESngTblCon, (int) 200]
                            assertLockTestSQLs(Select + tableName + Where + NoDB2Lock);
                            break;
                        case derby:
                            // SELECT t0.DTYPE, t0.version, t0.firstName, t0.lastName FROM LSESngTblAbs t0
                            //      WHERE t0.DTYPE = ? AND t0.id = ? [params=(String) LSESngTblCon, (int) 200]
                        case oracle:
                            // SELECT t0.DTYPE, t0.version, t0.firstName, t0.lastName FROM LSESngTblAbs t0 
                            //      WHERE t0.DTYPE = ? AND t0.id = ? [params=(String) LSESngTblCon, (int) 200]
                        default:
                            assertLockTestSQLs(Select + tableName + Where + NoForUpdate);
                        }
                    }

                    public void findPessimisticForcIncDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.DTYPE, t0.version, t0.firstName, t0.lastName FROM LSESngTblAbs t0
                            //      WHERE t0.DTYPE = ? AND t0.id = ?
                            //      optimize for 1 row FOR READ ONLY WITH RR USE AND KEEP UPDATE LOCKS
                            //      [params=(String) LSESngTblCon, (int) 201]
                            // SELECT t0.version FROM LSESngTblAbs t0 WHERE t0.id = ?
                            //      FOR READ ONLY WITH RR USE AND KEEP UPDATE LOCKS [params=(int) 201]
                            assertLockTestSQLs(Select + tableName + Where + DB2Lock);
                            break;
                        case derby:
                            // SELECT t0.DTYPE, t0.version, t0.firstName, t0.lastName FROM LSESngTblAbs t0 
                            //      WHERE t0.DTYPE = ? AND t0.id = ? FOR UPDATE WITH RR 
                            //      [params=(String) LSESngTblCon, (int) 201]
                            // SELECT t0.version FROM LSESngTblAbs t0 WHERE t0.id = ? FOR UPDATE WITH RR
                            //      [params=(int) 201]
                        case oracle:
                            // SELECT t0.DTYPE, t0.version, t0.firstName, t0.lastName FROM LSESngTblAbs t0 
                            //      WHERE t0.DTYPE = ? AND t0.id = ? FOR UPDATE 
                            //      [params=(String) LSESngTblCon, (int) 201]
                            // SELECT t0.version FROM LSESngTblAbs t0 WHERE t0.id = ? FOR UPDATE [params=(int) 201]
                        default:
                            assertLockTestSQLs(Select + tableName + Where + ForUpdate);
                        }
                    }

                    public void queryPessimisticReadDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.id, t0.DTYPE, t0.version, t0.firstName, t0.lastName FROM LSESngTblAbs t0
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\')
                            //      FOR READ ONLY WITH RS USE AND KEEP UPDATE LOCKS [params=(String) firstName%200]
                            // SELECT t0.version FROM LSESngTblAbs t0 WHERE t0.id = ?  [params=(int) 200]
                            assertLockTestSQLs(Select + tableName + Where + DB2Lock);
                            break;
                        case derby:
                            // SELECT t0.id, t0.DTYPE, t0.version, t0.firstName, t0.lastName FROM LSESngTblAbs t0
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\') FOR UPDATE WITH RR
                            //      [params=(String) firstName%200]
                            // SELECT t0.version FROM LSESngTblAbs t0 WHERE t0.id = ? [params=(int) 200]
                        case oracle:
                            // SELECT t0.id, t0.DTYPE, t0.version, t0.firstName, t0.lastName FROM LSESngTblAbs t0 
                            //      WHERE (t0.firstName LIKE ?) FOR UPDATE [params=(String) firstName%200]
                            // SELECT t0.version FROM LSESngTblAbs t0 WHERE t0.id = ? [params=(int) 200]
                        default:
                            assertLockTestSQLs(Select + tableName + Where + ForUpdate);
                        }
                    }

                    public void findNoLockAfterQueryPessimisticReadDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.DTYPE, t0.version, t0.firstName, t0.lastName FROM LSESngTblAbs t0
                            //      WHERE t0.DTYPE = ? AND t0.id = ?
                            //      optimize for 1 row [params=(String) LSESngTblCon, (int) 201]
                            assertLockTestSQLs(Select + tableName + Where + NoDB2Lock);
                            break;
                        case derby:
                            // SELECT t0.DTYPE, t0.version, t0.firstName, t0.lastName FROM LSESngTblAbs t0
                            //      WHERE t0.DTYPE = ? AND t0.id = ? [params=(String) LSESngTblCon, (int) 201]
                        case oracle:
                            // SELECT t0.DTYPE, t0.version, t0.firstName, t0.lastName FROM LSESngTblAbs t0 
                            //      WHERE t0.DTYPE = ? AND t0.id = ? [params=(String) LSESngTblCon, (int) 201]
                        default:
                            assertLockTestSQLs(Select + tableName + Where + NoForUpdate);
                        }
                    }

                    public void namedQueryPessimisticWriteDbSql(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.id, t0.DTYPE, t0.version, t0.firstName, t0.lastName FROM LSESngTblAbs t0
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\') AND t0.DTYPE = ?
                            //      FOR READ ONLY WITH RR USE AND KEEP UPDATE LOCKS [params=(String) firstName%200, (String) LSESngTblCon]
                            // SELECT t0.version FROM LSESngTblAbs t0 WHERE t0.id = ?  [params=(int) 200]
                            assertLockTestSQLs(Select + tableName + Where + DB2Lock);
                            break;
                        case derby:
                            // SELECT t0.id, t0.DTYPE, t0.version, t0.firstName, t0.lastName FROM LSESngTblAbs t0
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\') AND t0.DTYPE = ? FOR UPDATE WITH RR
                            //      [params=(String) firstName%200, (String) LSESngTblCon]
                            // SELECT t0.version FROM LSESngTblAbs t0 WHERE t0.id = ? [params=(int) 200]
                        case oracle:
                            // SELECT t0.id, t0.DTYPE, t0.version, t0.firstName, t0.lastName FROM LSESngTblAbs t0 
                            //      WHERE (t0.firstName LIKE ?) AND t0.DTYPE = ? FOR UPDATE 
                            //      [params=(String) firstName%200, (String) LSESngTblCon]
                            // SELECT t0.version FROM LSESngTblAbs t0 WHERE t0.id = ? [params=(int) 200]
                        default:
                            assertLockTestSQLs(Select + tableName + Where + ForUpdate);
                        }
                    }

                    public void findNoLockAfterNamedQueryPessimisticWriteDbSql(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.DTYPE, t0.version, t0.firstName, t0.lastName FROM LSESngTblAbs t0
                            //      WHERE t0.DTYPE = ? AND t0.id = ?
                            //      optimize for 1 row [params=(String) LSESngTblCon, (int) 201]
                            assertLockTestSQLs(Select + tableName + Where + NoDB2Lock);
                            break;
                        case derby:
                            // SELECT t0.DTYPE, t0.version, t0.firstName, t0.lastName FROM LSESngTblAbs t0
                            //      WHERE t0.DTYPE = ? AND t0.id = ? [params=(String) LSESngTblCon, (int) 201]
                        case oracle:
                            // SELECT t0.DTYPE, t0.version, t0.firstName, t0.lastName FROM LSESngTblAbs t0 
                            //      WHERE t0.DTYPE = ? AND t0.id = ? [params=(String) LSESngTblCon, (int) 201]
                        default:
                            assertLockTestSQLs(Select + tableName + Where + NoForUpdate);
                        }
                    }
                });
    }

    public void testNormalJoinedLock() {
        commonJoinedLock("testNormalJoinedLock", 400, false);
    }
    
    public void testExtendedJoinedLock() {
        commonJoinedLock("testExtendedJoinedLock", 410, true);
    }
    
    private void commonJoinedLock(String testName, int id0, boolean extended) {
        final String table1Name = "LSEJoinCon";
        final String table2Name = "LSEJoinAbs";
        final String joinTables = table1Name + ".*JOIN.*" + table2Name;
        getLog().info("** " + testName + "()");
        String scope = extended ? "Extended" : "Normal";
        int id1 = id0 + 1;

        // create test entity.
        LSEJoinCon e0 = new LSEJoinCon();
        e0.setId(id0);
        e0.setFirstName("firstName " + id0);
        e0.setLastName("lastName " + id0);
        LSEJoinCon e1 = new LSEJoinCon();
        e1.setId(id1);
        e1.setFirstName("firstName " + id1);
        e1.setLastName("lastName " + id1);

        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            em.getTransaction().begin();
            em.persist(e0);
            em.persist(e1);
            em.getTransaction().commit();
        } finally {
            em = null;
            e0 = e1 = null;
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        commonLockTest(testName, LSEJoinCon.class, id0, extended,
                "SELECT c FROM LSEJoinCon c WHERE c.firstName LIKE :firstName", "findLSEJoinCon"
                        + scope, new AssertCallback() {

                    public void findNoLockDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.id, t1.version, t1.firstName, t0.lastName FROM LSEJoinCon t0
                            //      INNER JOIN LSEJoinAbs t1 ON t0.id = t1.id WHERE t0.id = ?
                            //      optimize for 1 row [params=(int) 400]
                            assertLockTestSQLs(Select + joinTables + Where + NoDB2Lock);
                            break;
                        case oracle:
                            // SELECT t0.id, t1.version, t1.firstName, t0.lastName FROM LSEJoinCon t0, LSEJoinAbs t1 
                            //      WHERE t0.id = ? AND t0.id = t1.id [params=(int) 400]
                            assertLockTestSQLs(Select + table1Name + Any + table2Name + Where + NoForUpdate);
                            break;
                        case derby:
                            // SELECT t0.id, t1.version, t1.firstName, t0.lastName FROM LSEJoinCon t0
                            //      INNER JOIN LSEJoinAbs t1 ON t0.id = t1.id WHERE t0.id = ? [params=(int) 400]
                        default:
                            assertLockTestSQLs(Select + joinTables + Where + NoForUpdate);
                        }
                    }

                    public void findPessimisticForcIncDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.id, t1.version, t1.firstName, t0.lastName FROM LSEJoinCon t0
                            //      INNER JOIN LSEJoinAbs t1 ON t0.id = t1.id WHERE t0.id = ?
                            //      optimize for 1 row FOR READ ONLY WITH RR USE AND KEEP UPDATE LOCKS[params=(int) 401]
                            // SELECT t0.version FROM LSEJoinAbs t0 WHERE t0.id = ?
                            //      FOR READ ONLY WITH RR USE AND KEEP UPDATE LOCKS [params=(int) 401]
                            assertLockTestSQLs(Select + joinTables + Where + DB2Lock);
                            break;
                        case oracle:
                            // SELECT t0.id, t1.version, t1.firstName, t0.lastName FROM LSEJoinCon t0, LSEJoinAbs t1 
                            //      WHERE t0.id = ? AND t0.id = t1.id FOR UPDATE [params=(int) 401]
                            // SELECT t0.version FROM LSEJoinAbs t0 WHERE t0.id = ? FOR UPDATE [params=(int) 401]
                            assertLockTestSQLs(Select + table1Name + Any + table2Name + Where + ForUpdate);
                            break;
                        case derby:     //TODO: **Non-atomic lock, LSEJoinCon NOT locked *********
                            // The database is unable to lock this query.  Each object matching the query will be 
                            //  locked individually after it is loaded; however, it is technically possible that
                            //  another transaction could modify the data before the lock is obtained.
                            // SELECT t0.id, t1.version, t1.firstName, t0.lastName FROM LSEJoinCon t0
                            //      INNER JOIN LSEJoinAbs t1 ON t0.id = t1.id WHERE t0.id = ? [params=(int) 401]
                            // SELECT t0.id FROM LSEJoinAbs t0 WHERE t0.id = ? FOR UPDATE WITH RR [params=(int) 401]
                            // SELECT t0.version FROM LSEJoinAbs t0 WHERE t0.id = ? FOR UPDATE WITH RR
                            //      [params=(int) 401]
                            assertLockTestSQLs(Select + joinTables + Where + NoForUpdate,
                                    // Select + NoJoin + table1Name + NoJoin + Where + ForUpdate,
                                    Select + NoJoin + Any + table2Name + Any + NoJoin + Where + ForUpdate);
                            break;
                        default:
                            assertLockTestSQLs(Select + joinTables + Where + ForUpdate);
                        }
                    }

                    public void queryPessimisticReadDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t1.id, t0.id, t1.version, t1.firstName, t0.lastName FROM LSEJoinCon t0
                            //      INNER JOIN LSEJoinAbs t1 ON t0.id = t1.id
                            //      WHERE (t1.firstName LIKE ? ESCAPE '\')
                            //      FOR READ ONLY WITH RS USE AND KEEP UPDATE LOCKS [params=(String) firstName%400]
                            // SELECT t0.version FROM LSEJoinAbs t0 WHERE t0.id = ?  [params=(int) 400]
                            assertLockTestSQLs(Select + joinTables + Where + DB2Lock);
                            break;
                        case oracle:
                            // SELECT t1.id, t0.id, t1.version, t1.firstName, t0.lastName 
                            //      FROM LSEJoinCon t0, LSEJoinAbs t1 WHERE (t1.firstName LIKE ?) AND t0.id = t1.id 
                            //      FOR UPDATE [params=(String) firstName%400]
                            // SELECT t0.version FROM LSEJoinAbs t0 WHERE t0.id = ? [params=(int) 400]
                            assertLockTestSQLs(Select + table1Name + Any + table2Name + Where + ForUpdate);
                            break;
                        case derby:     //TODO: **Non-atomic lock, LSEJoinCon NOT locked *********
                            // The database is unable to lock this query.  Each object matching the query will be 
                            //  locked individually after it is loaded; however, it is technically possible that
                            //  another transaction could modify the data before the lock is obtained.
                            // SELECT t1.id, t0.id, t1.version, t1.firstName, t0.lastName FROM LSEJoinCon t0
                            //      INNER JOIN LSEJoinAbs t1 ON t0.id = t1.id WHERE (t1.firstName LIKE ? ESCAPE '\')
                            //      [params=(String) firstName%400]
                            // SELECT t0.id FROM LSEJoinAbs t0 WHERE t0.id = ? FOR UPDATE WITH RR [params=(int) 400]
                            // SELECT t0.version FROM LSEJoinAbs t0 WHERE t0.id = ? [params=(int) 400]
                            assertLockTestSQLs(Select + joinTables + Where + NoForUpdate,
                                    // Select + NoJoin + table1Name + NoJoin + Where + ForUpdate,
                                    Select + NoJoin + Any + table2Name + Any + NoJoin + Where + ForUpdate);
                            break;
                        default:
                            assertLockTestSQLs(Select + joinTables + Where + ForUpdate);
                        }
                    }

                    public void findNoLockAfterQueryPessimisticReadDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.id, t1.version, t1.firstName, t0.lastName FROM LSEJoinCon t0
                            //      INNER JOIN LSEJoinAbs t1 ON t0.id = t1.id WHERE t0.id = ?
                            //      optimize for 1 row [params=(int) 401]
                            assertLockTestSQLs(Select + joinTables + Where + NoDB2Lock);
                            break;
                        case oracle:
                            // SELECT t0.id, t1.version, t1.firstName, t0.lastName FROM LSEJoinCon t0, LSEJoinAbs t1 
                            //      WHERE t0.id = ? AND t0.id = t1.id [params=(int) 401]
                            assertLockTestSQLs(Select + table1Name + Any + table2Name + Where + NoForUpdate);
                            break;
                        case derby:
                            // SELECT t0.id, t1.version, t1.firstName, t0.lastName FROM LSEJoinCon t0
                            //      INNER JOIN LSEJoinAbs t1 ON t0.id = t1.id WHERE t0.id = ? [params=(int) 401]
                        default:
                            assertLockTestSQLs(Select + joinTables + Where + NoForUpdate);
                        }
                    }

                    public void namedQueryPessimisticWriteDbSql(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t1.id, t0.id, t1.version, t1.firstName, t0.lastName FROM LSEJoinCon t0
                            //      INNER JOIN LSEJoinAbs t1 ON t0.id = t1.id
                            //      WHERE (t1.firstName LIKE ? ESCAPE '\')
                            //      FOR READ ONLY WITH RR USE AND KEEP UPDATE LOCKS [params=(String) firstName%400]
                            // SELECT t0.version FROM LSEJoinAbs t0 WHERE t0.id = ?  [params=(int) 400]
                            assertLockTestSQLs(Select + joinTables + Where + DB2Lock);
                            break;
                        case oracle:
                            // SELECT t1.id, t0.id, t1.version, t1.firstName, t0.lastName 
                            //      FROM LSEJoinCon t0, LSEJoinAbs t1 WHERE (t1.firstName LIKE ?) AND t0.id = t1.id 
                            //      FOR UPDATE [params=(String) firstName%400]
                            // SELECT t0.version FROM LSEJoinAbs t0 WHERE t0.id = ? [params=(int) 400]
                            assertLockTestSQLs(Select + table1Name + Any + table2Name + Where + ForUpdate);
                            break;
                        case derby:     //TODO: **Non-atomic lock, LSEJoinCon NOT locked *********
                            // The database is unable to lock this query.  Each object matching the query will be 
                            //  locked individually after it is loaded; however, it is technically possible that
                            //  another transaction could modify the data before the lock is obtained.
                            // SELECT t1.id, t0.id, t1.version, t1.firstName, t0.lastName FROM LSEJoinCon t0
                            //      INNER JOIN LSEJoinAbs t1 ON t0.id = t1.id WHERE (t1.firstName LIKE ? ESCAPE '\')
                            //      [params=(String) firstName%400]
                            // SELECT t0.id FROM LSEJoinAbs t0 WHERE t0.id = ? FOR UPDATE WITH RR [params=(int) 400]
                            // SELECT t0.version FROM LSEJoinAbs t0 WHERE t0.id = ? [params=(int) 400]
                            assertLockTestSQLs(Select + joinTables + Where + NoForUpdate,
                                    // Select + NoJoin + table1Name + NoJoin + Where + ForUpdate,
                                    Select + NoJoin + Any + table2Name + Any + NoJoin + Where + ForUpdate);
                            break;
                        default:
                            assertLockTestSQLs(Select + joinTables + Where + ForUpdate);
                        }
                    }

                    public void findNoLockAfterNamedQueryPessimisticWriteDbSql(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.id, t1.version, t1.firstName, t0.lastName FROM LSEJoinCon t0
                            //      INNER JOIN LSEJoinAbs t1 ON t0.id = t1.id WHERE t0.id = ?
                            //      optimize for 1 row [params=(int) 401]
                            assertLockTestSQLs(Select + joinTables + Where + NoDB2Lock);
                            break;
                        case oracle:
                            // SELECT t0.id, t1.version, t1.firstName, t0.lastName FROM LSEJoinCon t0, LSEJoinAbs t1 
                            //      WHERE t0.id = ? AND t0.id = t1.id [params=(int) 401]
                            assertLockTestSQLs(Select + table1Name + Any + table2Name + Where + NoForUpdate);
                            break;
                        case derby:
                            // SELECT t0.id, t1.version, t1.firstName, t0.lastName FROM LSEJoinCon t0
                            //      INNER JOIN LSEJoinAbs t1 ON t0.id = t1.id WHERE t0.id = ? [params=(int) 401]
                        default:
                            assertLockTestSQLs(Select + joinTables + Where + NoForUpdate);
                        }
                    }
                });
    }

    public void testNormalElementCollectionLock() {
        commonElementCollectionLock("testNormalElementCollectionLock", 500, false);
    }
    
    public void testExtendedElementCollectionLock() {
        commonElementCollectionLock("testExtendedElementCollectionLock", 510, true);
    }
    
    private void commonElementCollectionLock(String testName, int id0, boolean extended) {
        final String tableName ="LSEEleCol";
        getLog().info("** " + testName + "()");
        String scope = extended ? "Extended" : "Normal";
        int id1 = id0 + 1;
        
        // create test entity.
        LSEEleCol e0 = new LSEEleCol();
        e0.setId(id0);
        e0.setFirstName("firstName lazy " + id0);
        e0.addCollection(id0 + "String1");
        e0.addCollection(id0 + "String2");
        LSEEleCol e1 = new LSEEleCol();
        e1.setId(id1);
        e1.setFirstName("lazy " + id1);
        e1.addCollection(id1 + "String1");
        e1.addCollection(id1 + "String2");

        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            em.getTransaction().begin();
            em.persist(e0);
            em.persist(e1);
            em.getTransaction().commit();
        } finally {
            em = null;
            e0 = e1 = null;
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        commonLockTest(testName, LSEEleCol.class, id0, extended,
                "SELECT c FROM LSEEleCol c WHERE c.firstName LIKE :firstName", "findLSEEleCol" + scope,
                new AssertCallback() {

                    public void findNoLockDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.version, t0.firstName FROM LSEEleCol t0 WHERE t0.id = ?
                            //      optimize for 1 row [params=(int) 500]
                            assertLockTestSQLs(Select + tableName + Where + NoDB2Lock);
                            break;
                        case derby:
                            // SELECT t0.version, t0.firstName FROM LSEEleCol t0 WHERE t0.id = ? [params=(int) 500]
                        case oracle:
                            // SELECT t0.version, t0.firstName FROM LSEEleCol t0 WHERE t0.id = ? [params=(int) 500]
                        default:
                            assertLockTestSQLs(Select + tableName + Where + NoForUpdate);
                        }
                    }

                    public void findPessimisticForcIncDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.version, t0.firstName FROM LSEEleCol t0 WHERE t0.id = ?
                            //      optimize for 1 row FOR READ ONLY WITH RR USE AND KEEP UPDATE LOCKS
                            //      [params=(int) 501]
                            // SELECT t0.version FROM LSEEleCol t0 WHERE t0.id = ?
                            //      FOR READ ONLY WITH RR USE AND KEEP UPDATE LOCKS [params=(int) 501]
                            assertLockTestSQLs(Select + tableName + Where + DB2Lock);
                            break;
                        case derby:
                            // SELECT t0.version, t0.firstName FROM LSEEleCol t0 WHERE t0.id = ? FOR UPDATE WITH RR
                            //      [params=(int) 501]
                            // SELECT t0.version FROM LSEEleCol t0 WHERE t0.id = ? FOR UPDATE WITH RR [params=(int) 501]
                        case oracle:
                            // SELECT t0.version, t0.firstName FROM LSEEleCol t0 WHERE t0.id = ? FOR UPDATE 
                            //      [params=(int) 501]
                            // SELECT t0.version FROM LSEEleCol t0 WHERE t0.id = ? FOR UPDATE [params=(int) 501]
                        default:
                            assertLockTestSQLs(Select + tableName + Where + ForUpdate);
                        }
                    }

                    public void queryPessimisticReadDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.id, t0.version, t0.firstName FROM LSEEleCol t0
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\')
                            //      FOR READ ONLY WITH RS USE AND KEEP UPDATE LOCKS [params=(String) firstName%500]
                            // SELECT t0.version FROM LSEEleCol t0 WHERE t0.id = ?  [params=(int) 500]
                            assertLockTestSQLs(Select + tableName + Where + DB2Lock);
                            break;
                        case derby:
                            // SELECT t0.id, t0.version, t0.firstName FROM LSEEleCol t0 WHERE
                            //      (t0.firstName LIKE ? ESCAPE '\') FOR UPDATE WITH RR [params=(String) firstName%500]
                            // SELECT t0.version FROM LSEEleCol t0 WHERE t0.id = ? [params=(int) 500]
                        case oracle:
                            // SELECT t0.id, t0.version, t0.firstName FROM LSEEleCol t0 WHERE (t0.firstName LIKE ?) 
                            //      FOR UPDATE [params=(String) firstName%500]
                            // SELECT t0.version FROM LSEEleCol t0 WHERE t0.id = ? [params=(int) 500]
                        default:
                            assertLockTestSQLs(Select + tableName + Where + ForUpdate);
                        }
                    }

                    public void findNoLockAfterQueryPessimisticReadDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.version, t0.firstName FROM LSEEleCol t0 WHERE t0.id = ?
                            //      optimize for 1 row [params=(int) 501]
                            assertLockTestSQLs(Select + tableName + Where + NoDB2Lock);
                            break;
                        case derby:
                            // SELECT t0.version, t0.firstName FROM LSEEleCol t0 WHERE t0.id = ? [params=(int) 501]
                        case oracle:
                            // SELECT t0.version, t0.firstName FROM LSEEleCol t0 WHERE t0.id = ? [params=(int) 501]
                        default:
                            assertLockTestSQLs(Select + tableName + Where + NoForUpdate);
                        }
                    }

                    public void namedQueryPessimisticWriteDbSql(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.id, t0.version, t0.firstName FROM LSEEleCol t0
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\')
                            //      FOR READ ONLY WITH RR USE AND KEEP UPDATE LOCKS [params=(String) firstName%500]
                            // SELECT t0.version FROM LSEEleCol t0 WHERE t0.id = ?  [params=(int) 500]
                            assertLockTestSQLs(Select + tableName + Where + DB2Lock);
                            break;
                        case derby:
                            // SELECT t0.id, t0.version, t0.firstName FROM LSEEleCol t0 WHERE
                            //      (t0.firstName LIKE ? ESCAPE '\') FOR UPDATE WITH RR [params=(String) firstName%500]
                            // SELECT t0.version FROM LSEEleCol t0 WHERE t0.id = ? [params=(int) 500]
                        case oracle:
                            // SELECT t0.id, t0.version, t0.firstName FROM LSEEleCol t0 WHERE (t0.firstName LIKE ?) 
                            //      FOR UPDATE [params=(String) firstName%500]
                            // SELECT t0.version FROM LSEEleCol t0 WHERE t0.id = ? [params=(int) 500]
                        default:
                            assertLockTestSQLs(Select + tableName + Where + ForUpdate);
                        }
                    }

                    public void findNoLockAfterNamedQueryPessimisticWriteDbSql(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.version, t0.firstName FROM LSEEleCol t0 WHERE t0.id = ?
                            //      optimize for 1 row [params=(int) 501]
                            assertLockTestSQLs(Select + tableName + Where + NoDB2Lock);
                            break;
                        case derby:
                            // SELECT t0.version, t0.firstName FROM LSEEleCol t0 WHERE t0.id = ? [params=(int) 501]
                        case oracle:
                            // SELECT t0.version, t0.firstName FROM LSEEleCol t0 WHERE t0.id = ? [params=(int) 501]
                        default:
                            assertLockTestSQLs(Select + tableName + Where + NoForUpdate);
                        }
                    }
                });
    }

    public void testNormalElementCollectionEagerLock() {
        commonElementCollectionEagerLock("testNormalElementCollectionEagerLock", 600, false);
    }

    public void testExtendedElementCollectionEagerLock() {
        commonElementCollectionEagerLock("testExtendedElementCollectionEagerLock", 610, true);
    }
    
    private void commonElementCollectionEagerLock(String testName, int id0, boolean extended) {
        final String table1Name = "LSEEleColEgr";
        final String table2Name = "LSEEleColEgr_collection";
//        final String table2Name_oracle = table2Name;//.toUpperCase().substring(0, Math.min(table2Name.length(), 30));
        final String joinTables = table1Name + ".*JOIN.*" + table2Name;
        getLog().info("** " + testName + "()");
        String scope = extended ? "Extended" : "Normal";
        int id1 = id0 + 1;

        // create test entity.
        LSEEleColEgr e0 = new LSEEleColEgr();
        e0.setId(id0);
        e0.setFirstName("firstName eager " + id0);
        e0.addCollection(id0 + "String1");
        e0.addCollection(id0 + "String2");
        LSEEleColEgr e1 = new LSEEleColEgr();
        e1.setId(id1);
        e1.setFirstName("firstName eager " + id1);
        e1.addCollection(id1 + "String1");
        e1.addCollection(id1 + "String2");

        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            em.getTransaction().begin();
            em.persist(e0);
            em.persist(e1);
            em.getTransaction().commit();
        } finally {
            em = null;
            e0 = e1 = null;
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        commonLockTest(testName, LSEEleColEgr.class, id0, extended,
                "SELECT c FROM LSEEleColEgr c WHERE c.firstName LIKE :firstName",
                "findLSEEleColEgr" + scope, new AssertCallback() {

                    public void findNoLockDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.version, t0.firstName, t1.LSEELECOLEGR_ID, t1.element FROM LSEEleColEgr t0
                            //      LEFT OUTER JOIN LSEEleColEgr_collection t1 ON t0.id = t1.LSEELECOLEGR_ID 
                            //      WHERE t0.id = ?  [params=(int) 600]
                            assertLockTestSQLs(Select + joinTables + Where + NoDB2Lock);
                            break;
                        case oracle:
                            // SELECT t0.version, t0.firstName, t1.LSEELECOLEGR_ID, t1.element 
                            //      FROM LSEEleColEgr t0, LSEEleColEgr_collection t1 
                            //      WHERE t0.id = ? AND t0.id = t1.LSEELECOLEGR_ID(+) [params=(int) 600]
                            assertLockTestSQLs(Select + table1Name + Any + table2Name + Where + "\\(\\+\\).*"
                                    + NoForUpdate);
                            break;
                        case derby:
                            // SELECT t0.version, t0.firstName, t1.LSEELECOLEGR_ID, t1.element FROM LSEEleColEgr t0
                            //      LEFT OUTER JOIN LSEEleColEgr_collection t1 ON t0.id = t1.LSEELECOLEGR_ID
                            //      WHERE t0.id = ? [params=(int) 600]
                        default:
                            assertLockTestSQLs(Select + joinTables + Where + NoForUpdate);
                        }
                    }

                    public void findPessimisticForcIncDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.version, t0.firstName, t1.LSEELECOLEGR_ID, t1.element FROM LSEEleColEgr t0
                            //      LEFT OUTER JOIN LSEEleColEgr_collection t1 ON t0.id = t1.LSEELECOLEGR_ID
                            //      WHERE t0.id = ?  FOR READ ONLY WITH RR USE AND KEEP UPDATE LOCKS [params=(int) 601]
                            // SELECT t0.version FROM LSEEleColEgr t0 WHERE t0.id = ?
                            //      FOR READ ONLY WITH RR USE AND KEEP UPDATE LOCKS [params=(int) 601]
                            assertLockTestSQLs(Select + joinTables + Where + DB2Lock);
                            break;
                        case oracle:
                            // SELECT t0.version, t0.firstName, t1.LSEELECOLEGR_ID, t1.element 
                            //      FROM LSEEleColEgr t0, LSEEleColEgr_collection t1 
                            //      WHERE t0.id = ? AND t0.id = t1.LSEELECOLEGR_ID(+) FOR UPDATE [params=(int) 601]
                            // SELECT t0.version FROM LSEEleColEgr t0 WHERE t0.id = ? FOR UPDATE [params=(int) 601]
                            assertLockTestSQLs(Select + table1Name + Any + table2Name + Where + "\\(\\+\\).*"
                                    + NoForUpdate);
                            break;
                        case derby:     // **Non-atomic lock, No need to lock LSEEleColEgr_collection *********
                            // TODO: Can do the same as query below, if extended scope. i.e. select LSEEleColEgr
                            //          with lock and fetch element collection without lock.
                            // The database is unable to lock this query.  Each object matching the query will be 
                            //  locked individually after it is loaded; however, it is technically possible that
                            //  another transaction could modify the data before the lock is obtained.
                            // SELECT t0.version, t0.firstName, t1.LSEELECOLEGR_ID, t1.element FROM LSEEleColEgr t0
                            //      LEFT OUTER JOIN LSEEleColEgr_collection t1 ON t0.id = t1.LSEELECOLEGR_ID
                            //      WHERE t0.id = ? [params=(int) 601]
                            // SELECT t0.id FROM LSEEleColEgr t0 WHERE t0.id = ? FOR UPDATE WITH RR [params=(int) 601]
                            // SELECT t0.version FROM LSEEleColEgr t0 WHERE t0.id = ? FOR UPDATE WITH RR
                            //      [params=(int) 601]
                            assertLockTestSQLs(Select + joinTables + Where + NoForUpdate, 
                                    Select + table1Name + Where + ForUpdate);
                            break;
                        default:
                            assertLockTestSQLs(Select + joinTables + Where + ForUpdate);
                        }
                    }

                    public void queryPessimisticReadDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:     // **Non-atomic lock, No need to lock LSEEleColEgr_collection *********   
                            // The database is unable to lock this query.  Each object matching the query will be 
                            //  locked individually after it is loaded; however, it is technically possible that
                            //  another transaction could modify the data before the lock is obtained.
                            // SELECT t0.id, t0.version, t0.firstName FROM LSEEleColEgr t0
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\')
                            //      FOR READ ONLY WITH RS USE AND KEEP UPDATE LOCKS [params=(String) firstName%600]
                            // SELECT t0.id, t1.element FROM LSEEleColEgr t0 INNER JOIN LSEEleColEgr_collection t1
                            //      ON t0.id = t1.LSEELECOLEGR_ID WHERE (t0.firstName LIKE ? ESCAPE '\')
                            //      ORDER BY t0.id ASC  [params=(String) firstName%600]
                            // SELECT t0.version FROM LSEEleColEgr t0 WHERE t0.id = ?  [params=(int) 600]
                            assertLockTestSQLs(Select + "LSEEleColEgr.*" + Where + DB2Lock,
                                    Select + joinTables + Where + NoDB2Lock);
                            break;
                        case oracle:
                            // SELECT t0.id, t0.version, t0.firstName FROM LSEEleColEgr t0 WHERE (t0.firstName LIKE ?) 
                            //      FOR UPDATE [params=(String) firstName%600]
                            // SELECT t0.id, t1.element FROM LSEEleColEgr t0, LSEEleColEgr_collection t1 
                            //      WHERE (t0.firstName LIKE ?) AND t0.id = t1.LSEELECOLEGR_ID ORDER BY t0.id ASC 
                            //      FOR UPDATE [params=(String) firstName%600]
                            // SELECT t0.version FROM LSEEleColEgr t0 WHERE t0.id = ? [params=(int) 600]
                            assertLockTestSQLs(Select + table1Name + Any + table2Name + Where + ForUpdate);
                            break;
                        case derby:     //**Non-atomic lock, No need to lock LSEEleColEgr_Collection *********
                            // The database is unable to lock this query.  Each object matching the query will be 
                            //  locked individually after it is loaded; however, it is technically possible that
                            //  another transaction could modify the data before the lock is obtained.
                            // SELECT t0.id, t0.version, t0.firstName FROM LSEEleColEgr t0 WHERE
                            //      (t0.firstName LIKE ? ESCAPE '\') FOR UPDATE WITH RR [params=(String) firstName%600]
                            // SELECT t0.id, t1.element FROM LSEEleColEgr t0 INNER JOIN LSEEleColEgr_collection t1 
                            //      ON t0.id = t1.LSEELECOLEGR_ID WHERE (t0.firstName LIKE ? ESCAPE '\')
                            //      ORDER BY t0.id ASC [params=(String) firstName%600]
                            // SELECT t0.version FROM LSEEleColEgr t0 WHERE t0.id = ? [params=(int) 600]
                            assertLockTestSQLs(Select + joinTables + Where + NoForUpdate,
                                    Select + table1Name + Where + ForUpdate);
                            break;
                        default:
                            assertLockTestSQLs(Select + joinTables + Where + ForUpdate);
                        }
                    }

                    public void findNoLockAfterQueryPessimisticReadDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.version, t0.firstName, t1.LSEELECOLEGR_ID, t1.element FROM LSEEleColEgr t0
                            //      LEFT OUTER JOIN LSEEleColEgr_collection t1 ON t0.id = t1.LSEELECOLEGR_ID
                            //      WHERE t0.id = ?  [params=(int) 601]
                            assertLockTestSQLs(Select + joinTables + Where + NoDB2Lock);
                            break;
                        case oracle:
                            // SELECT t0.version, t0.firstName, t1.LSEELECOLEGR_ID, t1.element 
                            //      FROM LSEEleColEgr t0, LSEEleColEgr_collection t1 
                            //      WHERE t0.id = ? AND t0.id = t1.LSEELECOLEGR_ID(+) [params=(int) 601]
                            assertLockTestSQLs(Select + table1Name + Any + table2Name + Where + "\\(\\+\\).*"
                                    + NoForUpdate);
                            break;
                        case derby:
                            // SELECT t0.version, t0.firstName, t1.LSEELECOLEGR_ID, t1.element FROM LSEEleColEgr t0
                            //      LEFT OUTER JOIN LSEEleColEgr_collection t1 ON t0.id = t1.LSEELECOLEGR_ID
                            //      WHERE t0.id = ? [params=(int) 601]
                        default:
                            assertLockTestSQLs(Select + joinTables + Where + NoForUpdate);
                        }
                    }

                    public void namedQueryPessimisticWriteDbSql(EntityManager em) {
                        switch (getDBType(em)) {
                        case db2:     // **Non-atomic lock, No need to lock LSEEleColEgr_collection *********   
                            // The database is unable to lock this query.  Each object matching the query will be 
                            //  locked individually after it is loaded; however, it is technically possible that
                            //  another transaction could modify the data before the lock is obtained.
                            // SELECT t0.id, t0.version, t0.firstName FROM LSEEleColEgr t0
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\')
                            //      FOR READ ONLY WITH RR USE AND KEEP UPDATE LOCKS [params=(String) firstName%600]
                            // SELECT t0.id, t1.element FROM LSEEleColEgr t0 INNER JOIN LSEEleColEgr_collection t1
                            //      ON t0.id = t1.LSEELECOLEGR_ID WHERE (t0.firstName LIKE ? ESCAPE '\')
                            //      ORDER BY t0.id ASC  [params=(String) firstName%600]
                            // SELECT t0.version FROM LSEEleColEgr t0 WHERE t0.id = ?  [params=(int) 600]
                            assertLockTestSQLs(Select + "LSEEleColEgr.*" + Where + DB2Lock,
                                    Select + joinTables + Where + NoDB2Lock);
                            break;
                        case oracle:
                            // SELECT t0.id, t0.version, t0.firstName FROM LSEEleColEgr t0 WHERE (t0.firstName LIKE ?) 
                            //      FOR UPDATE [params=(String) firstName%600]
                            // SELECT t0.id, t1.element FROM LSEEleColEgr t0, LSEEleColEgr_collection t1 
                            //      WHERE (t0.firstName LIKE ?) AND t0.id = t1.LSEELECOLEGR_ID ORDER BY t0.id ASC 
                            //      FOR UPDATE [params=(String) firstName%600]
                            // SELECT t0.version FROM LSEEleColEgr t0 WHERE t0.id = ? [params=(int) 600]
                            assertLockTestSQLs(Select + table1Name + Any + table2Name + Where + ForUpdate);
                            break;
                        case derby:     // **Non-atomic lock, No need to lock LSEEleColEgr_collection *********   
                            // The database is unable to lock this query.  Each object matching the query will be 
                            //  locked individually after it is loaded; however, it is technically possible that
                            //  another transaction could modify the data before the lock is obtained.
                            // SELECT t0.id, t0.version, t0.firstName FROM LSEEleColEgr t0 WHERE
                            //      (t0.firstName LIKE ? ESCAPE '\') FOR UPDATE WITH RR [params=(String) firstName%600]
                            // SELECT t0.id, t1.element FROM LSEEleColEgr t0 INNER JOIN LSEEleColEgr_collection t1
                            //      ON t0.id = t1.LSEELECOLEGR_ID WHERE (t0.firstName LIKE ? ESCAPE '\')
                            //      ORDER BY t0.id ASC [params=(String) firstName%600]
                            // SELECT t0.version FROM LSEEleColEgr t0 WHERE t0.id = ? [params=(int) 600]
                            assertLockTestSQLs(Select + joinTables + Where + NoForUpdate, 
                                    Select + table1Name + Where + ForUpdate);
                            break;
                        default:
                            assertLockTestSQLs(Select + joinTables + Where + ForUpdate);
                        }
                    }

                    public void findNoLockAfterNamedQueryPessimisticWriteDbSql(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.version, t0.firstName, t1.LSEELECOLEGR_ID, t1.element FROM LSEEleColEgr t0 
                            //      LEFT OUTER JOIN LSEEleColEgr_collection t1 ON t0.id = t1.LSEELECOLEGR_ID
                            //      WHERE t0.id = ?  [params=(int) 601]
                            assertLockTestSQLs(Select + joinTables + Where + NoDB2Lock);
                            break;
                        case oracle:
                            // SELECT t0.version, t0.firstName, t1.LSEELECOLEGR_ID, t1.element 
                            //      FROM LSEEleColEgr t0, LSEEleColEgr_collection t1 
                            //      WHERE t0.id = ? AND t0.id = t1.LSEELECOLEGR_ID(+) [params=(int) 601]
                            assertLockTestSQLs(Select + table1Name + Any + table2Name + Where + "\\(\\+\\).*"
                                    + NoForUpdate);
                            break;
                        case derby:
                            // SELECT t0.version, t0.firstName, t1.LSEELECOLEGR_ID, t1.element FROM LSEEleColEgr t0
                            //      LEFT OUTER JOIN LSEEleColEgr_collection t1 ON t0.id = t1.LSEELECOLEGR_ID
                            //      WHERE t0.id = ? [params=(int) 601]
                        default:
                            assertLockTestSQLs(Select + joinTables + Where + NoForUpdate);
                        }
                    }
                });
    }
}

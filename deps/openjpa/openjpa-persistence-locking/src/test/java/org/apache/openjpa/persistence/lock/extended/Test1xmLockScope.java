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
 * LockScopeTestCase subclass to test entity with:
 * - Uni-1xm - lazy fetch (default) 
 * - Uni-1xm - eager fetch 
 * - Uni-1xm use join table - lazy fetch (default) 
 * - Uni-1xm use join table - eager fetch 
 */
public class Test1xmLockScope extends LockScopeTestCase {

    public void setUp() {
        setSupportedDatabases(
                org.apache.openjpa.jdbc.sql.DerbyDictionary.class,
                org.apache.openjpa.jdbc.sql.OracleDictionary.class,
                org.apache.openjpa.jdbc.sql.DB2Dictionary.class);
        if (isTestsDisabled()) {
            return;
        }

        setUp(LSE1xmLf.class
            , LSE1xmLfEgr.class
            , LSE1xmLfJT.class
            , LSE1xmLfJTEgr.class
            , LSE1xmRt.class
            , "openjpa.LockManager", "mixed",
            "openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)"
        );
        commonSetUp(LSE1xmLf.class
            , LSE1xmLfEgr.class
            , LSE1xmLfJT.class
            , LSE1xmLfJTEgr.class
            , LSE1xmRt.class
        );
    }

    public void testNormalUni1xmLock() {
        common1xmLock("testNormalUni1xmLock", 2111101, false);
    }

    public void testExtendedUni1xmLock() {
        common1xmLock("testExtendedUni1xmLock", 2111111, true);
    }

    private void common1xmLock(String testName, int idLf0, boolean extended) {
        final String tableLfName = "LSE1xmLf";
//        final String tableRtName = "LSE1xmRt";
//        final String joinTables  = tableLfName + ".*JOIN.*" + tableRtName;
        getLog().info("** " + testName + "()");
        String scope = extended ? "Extended" : "Normal";
        int idRt00 = idLf0 + 10000; // right table
        int idRt01 = idRt00 + 1;
        int idLf1  = idLf0 + 1;
        int idRt10 = idLf1 + 10000 + 1; // right table
        int idRt11 = idRt10 + 1;
        // create test entity.
        LSE1xmLf eLf0 = new LSE1xmLf();
        LSE1xmRt eRt00 = new LSE1xmRt();
        LSE1xmRt eRt01 = new LSE1xmRt();
        eLf0.setId(idLf0);
        eLf0.setFirstName("firstName " + idLf0);
        eLf0.addUnitRight(eRt00);
        eLf0.addUnitRight(eRt01);
        eRt00.setId(idRt00);
        eRt00.setLastName("lastName " + idRt00);
        eRt01.setId(idRt01);
        eRt01.setLastName("lastName " + idRt01);
        
        LSE1xmLf eLf1 = new LSE1xmLf();
        LSE1xmRt eRt10 = new LSE1xmRt();
        LSE1xmRt eRt11 = new LSE1xmRt();
        eLf1.setId(idLf1);
        eLf1.setFirstName("firstName " + idLf1);
        eLf1.addUnitRight(eRt10);
        eLf1.addUnitRight(eRt11);
        eRt10.setId(idRt10);
        eRt10.setLastName("lastName " + idRt10);
        eRt11.setId(idRt11);
        eRt11.setLastName("lastName " + idRt11);
       
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            em.getTransaction().begin();
            em.persist(eRt00);
            em.persist(eRt01);
            em.persist(eLf0);
            em.persist(eRt10);
            em.persist(eRt11);
            em.persist(eLf1);
            em.getTransaction().commit();
        } finally {
            em = null;
            eLf0 = eLf1 = null;
            eRt00 = eRt01 = eRt10 = eRt11 = null;
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        commonLockTest(testName, LSE1xmLf.class, idLf0, extended,
                "SELECT c FROM LSE1xmLf c WHERE c.firstName LIKE :firstName", "findLSE1xmLf" + scope,
                new AssertCallback() {
                    public void findNoLockDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.version, t0.firstName FROM LSE1xmLf t0 WHERE t0.id = ?  
                            //      optimize for 1 row [params=(int) 2111101]
                            assertLockTestSQLs(Select + tableLfName + Where + NoDB2Lock);
                            break;
                        case oracle:
                            // SELECT t0.version, t0.firstName FROM LSE1xmLf t0 WHERE t0.id = ? [params=(int) 2111101]
                        case derby:
                            // SELECT t0.version, t0.firstName FROM LSE1xmLf t0 WHERE t0.id = ? [params=(int) 2111101]
                        default:
                            assertLockTestSQLs(Select + tableLfName + Where + NoForUpdate);
                        }
                    }

                    public void findPessimisticForcIncDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.version, t0.firstName FROM LSE1xmLf t0 WHERE t0.id = ?
                            //      optimize for 1 row FOR READ ONLY WITH RR USE AND KEEP UPDATE LOCKS 
                            //      [params=(int) 2111102]
                            // SELECT t0.version FROM LSE1xmLf t0 WHERE t0.id = ?  
                            //      FOR READ ONLY WITH RR USE AND KEEP UPDATE LOCKS [params=(int) 2111102]
                            assertLockTestSQLs(Select + tableLfName + Where + DB2Lock);
                            break;
                        case oracle:
                            // SELECT t0.version, t0.firstName FROM LSE1xmLf t0 WHERE t0.id = ? FOR UPDATE 
                            //      [params=(int) 2111102]
                            // SELECT t0.version FROM LSE1xmLf t0 WHERE t0.id = ? FOR UPDATE [params=(int) 2111102]
                        case derby:
                            // SELECT t0.version, t0.firstName FROM LSE1xmLf t0 WHERE t0.id = ? FOR UPDATE WITH RR
                            //      [params=(int) 2111102]
                            // SELECT t0.version FROM LSE1xmLf t0 WHERE t0.id = ? FOR UPDATE WITH RR
                            //      [params=(int) 2111102]
                        default:
                            assertLockTestSQLs(Select + tableLfName + Where + ForUpdate);
                        }
                    }

                    public void queryPessimisticReadDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.id, t0.version, t0.firstName FROM LSE1xmLf t0 
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\')  
                            //      FOR READ ONLY WITH RS USE AND KEEP UPDATE LOCKS [params=(String) firstName%2111101]
                            // SELECT t0.version FROM LSE1xmLf t0 WHERE t0.id = ?  [params=(int) 2111101]
                            assertLockTestSQLs(Select + tableLfName + Where + DB2Lock);
                            break;
                        case oracle:
                            // SELECT t0.id, t0.version, t0.firstName FROM LSE1xmLf t0 WHERE (t0.firstName LIKE ?) 
                            //      FOR UPDATE [params=(String) firstName%2111101]
                            // SELECT t0.version FROM LSE1xmLf t0 WHERE t0.id = ? [params=(int) 2111101] 
                        case derby:
                            // SELECT t0.id, t0.version, t0.firstName FROM LSE1xmLf t0
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\') FOR UPDATE WITH RR
                            //      [params=(String) firstName%2111101]
                            // SELECT t0.version FROM LSE1xmLf t0 WHERE t0.id = ? [params=(int) 2111101]
                        default:
                            assertLockTestSQLs(Select + tableLfName + Where + ForUpdate);
                        }
                    }

                    public void findNoLockAfterQueryPessimisticReadDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.version, t0.firstName FROM LSE1xmLf t0 WHERE t0.id = ?  
                            //      optimize for 1 row [params=(int) 2111102]
                            assertLockTestSQLs(Select + tableLfName + Where + NoDB2Lock);
                            break;
                        case oracle:
                            // SELECT t0.version, t0.firstName FROM LSE1xmLf t0 WHERE t0.id = ? [params=(int) 2111102] 
                        case derby:
                            // SELECT t0.version, t0.firstName FROM LSE1xmLf t0 WHERE t0.id = ? [params=(int) 2111102]
                        default:
                            assertLockTestSQLs(Select + tableLfName + Where + NoForUpdate);
                        }
                    }

                    public void namedQueryPessimisticWriteDbSql(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.id, t0.version, t0.firstName FROM LSE1xmLf t0 
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\')  
                            //      FOR READ ONLY WITH RR USE AND KEEP UPDATE LOCKS [params=(String) firstName%2111101]
                            // SELECT t0.version FROM LSE1xmLf t0 WHERE t0.id = ?  [params=(int) 2111101]
                            assertLockTestSQLs(Select + tableLfName + Where + DB2Lock);
                            break;
                        case oracle:
                            // SELECT t0.id, t0.version, t0.firstName FROM LSE1xmLf t0 WHERE (t0.firstName LIKE ?) 
                            //      FOR UPDATE [params=(String) firstName%2111101]
                            // SELECT t0.version FROM LSE1xmLf t0 WHERE t0.id = ? [params=(int) 2111101] 
                        case derby:
                            // SELECT t0.id, t0.version, t0.firstName FROM LSE1xmLf t0
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\') FOR UPDATE WITH RR
                            //      [params=(String) firstName%2111101]
                            // SELECT t0.version FROM LSE1xmLf t0 WHERE t0.id = ? [params=(int) 2111101]
                        default:
                            assertLockTestSQLs(Select + tableLfName + Where + ForUpdate);
                        }
                    }

                    public void findNoLockAfterNamedQueryPessimisticWriteDbSql(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.version, t0.firstName FROM LSE1xmLf t0 WHERE t0.id = ?  
                            //      optimize for 1 row [params=(int) 2111102]
                            assertLockTestSQLs(Select + tableLfName + Where + NoDB2Lock);
                            break;
                        case oracle:
                            // SELECT t0.version, t0.firstName FROM LSE1xmLf t0 WHERE t0.id = ? [params=(int) 2111102] 
                        case derby:
                            // SELECT t0.version, t0.firstName FROM LSE1xmLf t0 WHERE t0.id = ? [params=(int) 2111102]
                        default:
                            assertLockTestSQLs(Select + tableLfName + Where + NoForUpdate);
                        }
                    }
                });
    }

    public void testNormalUni1xmEagerLock() {
        common1xmEagerLock("testNormalUni1xmEagerLock", 2111201, false);
    }

    public void testExtendedUni1xmEagerLock() {
        common1xmEagerLock("testExtendedUni1xmEagerLock", 2111211, true);
    }

    private void common1xmEagerLock(String testName, int idLf0, boolean extended) {
        final String tableLfName = "LSE1xmLfEgr";
        final String tableJTName = "LSE1xmLfEgr_LSE1xmRt";
        final String tableRtName = "LSE1xmRt";
        final String joinTables  = tableLfName + ".*JOIN.*" + tableJTName + ".*JOIN.*" + tableRtName;
        getLog().info("** " + testName + "()");
        String scope = extended ? "Extended" : "Normal";
        int idRt00 = idLf0 + 10000; // right table
        int idRt01 = idRt00 + 1;
        int idLf1  = idLf0 + 1;
        int idRt10 = idLf1 + 10000 + 1; // right table
        int idRt11 = idRt10 + 1;
        // create test entity.
        LSE1xmLfEgr eLf0 = new LSE1xmLfEgr();
        LSE1xmRt eRt00 = new LSE1xmRt();
        LSE1xmRt eRt01 = new LSE1xmRt();
        eLf0.setId(idLf0);
        eLf0.setFirstName("firstName " + idLf0);
        eLf0.addUnitRight(eRt00);
        eLf0.addUnitRight(eRt01);
        eRt00.setId(idRt00);
        eRt00.setLastName("lastName " + idRt00);
        eRt01.setId(idRt01);
        eRt01.setLastName("lastName " + idRt01);
        
        LSE1xmLfEgr eLf1 = new LSE1xmLfEgr();
        LSE1xmRt eRt10 = new LSE1xmRt();
        LSE1xmRt eRt11 = new LSE1xmRt();
        eLf1.setId(idLf1);
        eLf1.setFirstName("firstName " + idLf1);
        eLf1.addUnitRight(eRt10);
        eLf1.addUnitRight(eRt11);
        eRt10.setId(idRt10);
        eRt10.setLastName("lastName " + idRt10);
        eRt11.setId(idRt11);
        eRt11.setLastName("lastName " + idRt11);
       
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            em.getTransaction().begin();
            em.persist(eRt00);
            em.persist(eRt01);
            em.persist(eLf0);
            em.persist(eRt10);
            em.persist(eRt11);
            em.persist(eLf1);
            em.getTransaction().commit();
        } finally {
            em = null;
            eLf0 = eLf1 = null;
            eRt00 = eRt01 = eRt10 = eRt11 = null;
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        commonLockTest(testName, LSE1xmLfEgr.class, idLf0, extended,
                "SELECT c FROM LSE1xmLfEgr c WHERE c.firstName LIKE :firstName", "findLSE1xmLfEgr"
                        + scope,
                new AssertCallback() {
                    public void findNoLockDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.version, t0.firstName, t1.LSE1XMLFEGR_ID, t2.id, t2.version, t2.lastName 
                            //      FROM LSE1xmLfEgr t0 LEFT OUTER JOIN LSE1xmLfEgr_LSE1xmRt t1 
                            //      ON t0.id = t1.LSE1XMLFEGR_ID LEFT OUTER JOIN LSE1xmRt t2 
                            //      ON t1.UNIRIGHT_ID = t2.id WHERE t0.id = ?  [params=(int) 2111201]
                            assertLockTestSQLs(Select + joinTables + Where + NoDB2Lock);
                            break;
                        case oracle:
                            // SELECT t0.version, t0.firstName, t1.LSE1XMLFEGR_ID, t2.id, t2.version, t2.lastName 
                            //      FROM LSE1xmLfEgr t0, LSE1xmLfEgr_LSE1xmRt t1, LSE1xmRt t2 
                            //      WHERE t0.id = ? AND t0.id = t1.LSE1XMLFEGR_ID(+) AND t1.UNIRIGHT_ID = t2.id(+) 
                            //      [params=(int) 2111201] 
                            assertLockTestSQLs(Select + tableLfName + Any + tableJTName + Any + tableRtName + Where
                                    + "\\(\\+\\).*" + NoForUpdate);
                            break;
                        case derby:
                            // SELECT t0.version, t0.firstName, t1.LSE1XMLFEGR_ID, t2.id, t2.version, t2.lastName 
                            //      FROM LSE1xmLfEgr t0 LEFT OUTER JOIN LSE1xmLfEgr_LSE1xmRt t1 
                            //      ON t0.id = t1.LSE1XMLFEGR_ID LEFT OUTER JOIN LSE1xmRt t2 ON t1.UNIRIGHT_ID = t2.id
                            //      WHERE t0.id = ? [params=(int) 2111201]
                        default:
                            assertLockTestSQLs(Select + joinTables + Where + NoForUpdate);
                        }
                    }

                    public void findPessimisticForcIncDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.version, t0.firstName, t1.LSE1XMLFEGR_ID, t2.id, t2.version, t2.lastName 
                            //      FROM LSE1xmLfEgr t0 LEFT OUTER JOIN LSE1xmLfEgr_LSE1xmRt t1 
                            //      ON t0.id = t1.LSE1XMLFEGR_ID LEFT OUTER JOIN LSE1xmRt t2 
                            //      ON t1.UNIRIGHT_ID = t2.id WHERE t0.id = ?  
                            //      FOR READ ONLY WITH RR USE AND KEEP UPDATE LOCKS [params=(int) 2111202]
                            // SELECT t0.version FROM LSE1xmRt t0 WHERE t0.id = ?
                            //      FOR READ ONLY WITH RR USE AND KEEP UPDATE LOCKS [params=(int) 2121204]
                            // SELECT t0.version FROM LSE1xmRt t0 WHERE t0.id = ?  
                            //      FOR READ ONLY WITH RR USE AND KEEP UPDATE LOCKS [params=(int) 2121203]
                            // SELECT t0.version FROM LSE1xmLfEgr t0 WHERE t0.id = ?  
                            //      FOR READ ONLY WITH RR USE AND KEEP UPDATE LOCKS [params=(int) 2111202]
                            assertLockTestSQLs(Select + joinTables + Where + DB2Lock);
                            break;
                        case oracle:    // TODO: if jpa2, DO NOT lock LSE1xmRT using "FOR UPDATE OF col"
                            // SELECT t0.version, t0.firstName, t1.LSE1XMLFEGR_ID, t2.id, t2.version, t2.lastName 
                            //      FROM LSE1xmLfEgr t0, LSE1xmLfEgr_LSE1xmRt t1, LSE1xmRt t2 
                            //      WHERE t0.id = ? AND t0.id = t1.LSE1XMLFEGR_ID(+) AND t1.UNIRIGHT_ID = t2.id(+) 
                            //      FOR UPDATE [params=(int) 2111202]
                            // SELECT t0.version FROM LSE1xmRt t0 WHERE t0.id = ? FOR UPDATE [params=(int) 2121203]
                            // SELECT t0.version FROM LSE1xmRt t0 WHERE t0.id = ? FOR UPDATE [params=(int) 2121204]
                            // SELECT t0.version FROM LSE1xmLfEgr t0 WHERE t0.id = ? FOR UPDATE [params=(int) 2111202]
                            assertLockTestSQLs(Select + tableLfName + Any + tableJTName + Any + tableRtName + Where
                                    + "\\(\\+\\).*" + ForUpdate);
                            break;
                        case derby:     //TODO: **Non-atomic lock. If jpa2, DO NOT lock LSE1xmRt, 
                                        // if jpa2/extended, LOCK LSE1xmLfEgr_LSE1xmRt
                            // The database is unable to lock this query.  Each object matching the query will be 
                            //  locked individually after it is loaded; however, it is technically possible that
                            //  another transaction could modify the data before the lock is obtained.
                            // SELECT t0.version, t0.firstName, t1.LSE1XMLFEGR_ID, t2.id, t2.version, t2.lastName
                            //      FROM LSE1xmLfEgr t0 LEFT OUTER JOIN LSE1xmLfEgr_LSE1xmRt t1
                            //      ON t0.id = t1.LSE1XMLFEGR_ID LEFT OUTER JOIN LSE1xmRt t2 ON t1.UNIRIGHT_ID = t2.id
                            //      WHERE t0.id = ? [params=(int) 2111202]
                            // SELECT t0.id FROM LSE1xmRt t0 WHERE t0.id = ? FOR UPDATE WITH RR
                            //      [params=(int) 2121203]
                            // SELECT t0.version FROM LSE1xmRt t0 WHERE t0.id = ? FOR UPDATE WITH RR
                            //      [params=(int) 2121203]
                            // SELECT t0.id FROM LSE1xmRt t0 WHERE t0.id = ? FOR UPDATE WITH RR
                            //      [params=(int) 2121204]
                            // SELECT t0.version FROM LSE1xmRt t0 WHERE t0.id = ? FOR UPDATE WITH RR
                            //      [params=(int) 2121204]
                            // SELECT t0.id FROM LSE1xmLfEgr t0 WHERE t0.id = ? FOR UPDATE WITH RR
                            //      [params=(int) 2111202]
                            // SELECT t0.version FROM LSE1xmLfEgr t0 WHERE t0.id = ? FOR UPDATE WITH RR
                            //      [params=(int) 2111202]
                            assertLockTestSQLs(Select + joinTables + Where + NoForUpdate,
                                    Select + NoJoin + Any + tableLfName + Any + NoJoin + Where + ForUpdate,
                                    Select + NoJoin + Any + tableRtName + Any + NoJoin + Where + ForUpdate,
                                    Select + NoJoin + Any + tableRtName + Any + NoJoin + Where + ForUpdate
                                    );
                            break;
                        default:
                            assertLockTestSQLs(Select + joinTables + Where + ForUpdate);
                        }
                    }

                    public void queryPessimisticReadDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:       //TODO: **Non-atomic lock. if jpa2, DO NOT lock LSE1xmRt
                                        // if jpa2/extended, LOCK LSE1xmLfEgr_LSE1xmRt
                            // The database is unable to lock this query.  Each object matching the query will be 
                            //  locked individually after it is loaded; however, it is technically possible that
                            //  another transaction could modify the data before the lock is obtained.
                            // SELECT t0.id, t0.version, t0.firstName FROM LSE1xmLfEgr t0 
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\')  
                            //      FOR READ ONLY WITH RS USE AND KEEP UPDATE LOCKS [params=(String) firstName%2111201]
                            // SELECT t0.id, t2.id, t2.version, t2.lastName FROM LSE1xmLfEgr t0 
                            //      INNER JOIN LSE1xmLfEgr_LSE1xmRt t1 ON t0.id = t1.LSE1XMLFEGR_ID 
                            //      INNER JOIN LSE1xmRt t2 ON t1.UNIRIGHT_ID = t2.id 
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\') ORDER BY t0.id ASC  
                            //      [params=(String) firstName%2111201]
                            // SELECT t0.id FROM LSE1xmRt t0 WHERE t0.id = ?  
                            //      FOR READ ONLY WITH RS USE AND KEEP UPDATE LOCKS [params=(int) 2121201]
                            // SELECT t0.version FROM LSE1xmRt t0 WHERE t0.id = ?  [params=(int) 2121201]
                            // SELECT t0.id FROM LSE1xmRt t0 WHERE t0.id = ?  
                            //      FOR READ ONLY WITH RS USE AND KEEP UPDATE LOCKS [params=(int) 2121202]
                            // SELECT t0.version FROM LSE1xmRt t0 WHERE t0.id = ?  [params=(int) 2121202]
                            // SELECT t0.version FROM LSE1xmLfEgr t0 WHERE t0.id = ?  [params=(int) 2111201]
                            assertLockTestSQLs(Select + NoJoin + Any + tableLfName + Any + NoJoin + Where + DB2Lock,
                                    Select + joinTables + Where + NoDB2Lock,
                                    Select + NoJoin + Any + tableRtName + Any + NoJoin + Where + DB2Lock,
                                    Select + NoJoin + Any + tableRtName + Any + NoJoin + Where + DB2Lock);
                            break;
                        case oracle:    // TODO: if jpa2, DO NOT lock LSE1xmRT using "FOR UPDATE OF col"
                            // SELECT t0.id, t0.version, t0.firstName FROM LSE1xmLfEgr t0 WHERE (t0.firstName LIKE ?) 
                            //      FOR UPDATE [params=(String) firstName%2111201]
                            // SELECT t0.id, t2.id, t2.version, t2.lastName 
                            //      FROM LSE1xmLfEgr t0, LSE1xmLfEgr_LSE1xmRt t1, LSE1xmRt t2 
                            //      WHERE (t0.firstName LIKE ?) AND t0.id = t1.LSE1XMLFEGR_ID AND t1.UNIRIGHT_ID = t2.id
                            //      ORDER BY t0.id ASC FOR UPDATE [params=(String) firstName%2111201]
                            // SELECT t0.version FROM LSE1xmRt t0 WHERE t0.id = ? [params=(int) 2121202]
                            // SELECT t0.version FROM LSE1xmRt t0 WHERE t0.id = ? [params=(int) 2121201]
                            // SELECT t0.version FROM LSE1xmLfEgr t0 WHERE t0.id = ? [params=(int) 2111201]
                            assertLockTestSQLs(Select + NoJoin + tableLfName + NoJoin + Where + ForUpdate,
                                    Select + tableLfName + Any + tableJTName + Any + tableRtName + Where
                                    + ForUpdate);
                            break;
                        case derby:     //TODO: **Non-atomic lock. If jpa2, DO NOT lock LSE1xmRt, 
                                        // if jpa2/extended, LOCK LSE1xmLfEgr_LSE1xmRt
                            // The database is unable to lock this query.  Each object matching the query will be 
                            //  locked individually after it is loaded; however, it is technically possible that
                            //  another transaction could modify the data before the lock is obtained.
                            // SELECT t0.id, t0.version, t0.firstName FROM LSE1xmLfEgr t0
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\') FOR UPDATE WITH RR
                            //      [params=(String) firstName%2111201]
                            // SELECT t0.id, t2.id, t2.version, t2.lastName FROM LSE1xmLfEgr t0
                            //      INNER JOIN LSE1xmLfEgr_LSE1xmRt t1 ON t0.id = t1.LSE1XMLFEGR_ID
                            //      INNER JOIN LSE1xmRt t2 ON t1.UNIRIGHT_ID = t2.id
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\') ORDER BY t0.id ASC
                            //      [params=(String) firstName%2111201]
                            // SELECT t0.id FROM LSE1xmRt t0 WHERE t0.id = ? FOR UPDATE WITH RR [params=(int) 2121202]
                            // SELECT t0.version FROM LSE1xmRt t0 WHERE t0.id = ? [params=(int) 2121202]
                            // SELECT t0.id FROM LSE1xmRt t0 WHERE t0.id = ? FOR UPDATE WITH RR [params=(int) 2121201]
                            // SELECT t0.version FROM LSE1xmRt t0 WHERE t0.id = ? [params=(int) 2121201]
                            // SELECT t0.version FROM LSE1xmLfEgr t0 WHERE t0.id = ? [params=(int) 2111201]
                            assertLockTestSQLs(Select + joinTables + Where + NoForUpdate,
                                    Select + NoJoin + tableLfName + NoJoin + Where + ForUpdate,
                                    Select + NoJoin + tableRtName + NoJoin + Where + ForUpdate,
                                    Select + NoJoin + tableRtName + NoJoin + Where + ForUpdate
                                    );
                            break;
                        default:
                            assertLockTestSQLs(Select + joinTables + Where + ForUpdate);
                        }
                    }

                    public void findNoLockAfterQueryPessimisticReadDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.version, t0.firstName, t1.LSE1XMLFEGR_ID, t2.id, t2.version, t2.lastName 
                            //      FROM LSE1xmLfEgr t0 LEFT OUTER JOIN LSE1xmLfEgr_LSE1xmRt t1 
                            //      ON t0.id = t1.LSE1XMLFEGR_ID LEFT OUTER JOIN LSE1xmRt t2 
                            //      ON t1.UNIRIGHT_ID = t2.id WHERE t0.id = ?  [params=(int) 2111202]
                            assertLockTestSQLs(Select + joinTables + Where + NoDB2Lock);
                            break;
                        case oracle:
                            // SELECT t0.version, t0.firstName, t1.LSE1XMLFEGR_ID, t2.id, t2.version, t2.lastName 
                            //      FROM LSE1xmLfEgr t0, LSE1xmLfEgr_LSE1xmRt t1, LSE1xmRt t2 
                            //      WHERE t0.id = ? AND t0.id = t1.LSE1XMLFEGR_ID(+) AND t1.UNIRIGHT_ID = t2.id(+) 
                            //      [params=(int) 2111202] 
                            assertLockTestSQLs(Select + tableLfName + Any + tableJTName + Any + tableRtName + Where
                                    + "\\(\\+\\).*" + NoForUpdate);
                            break;
                        case derby:
                            // SELECT t0.version, t0.firstName, t1.LSE1XMLFEGR_ID, t2.id, t2.version, t2.lastName
                            //      FROM LSE1xmLfEgr t0 LEFT OUTER JOIN LSE1xmLfEgr_LSE1xmRt t1
                            //      ON t0.id = t1.LSE1XMLFEGR_ID LEFT OUTER JOIN LSE1xmRt t2
                            //      ON t1.UNIRIGHT_ID = t2.id WHERE t0.id = ? [params=(int) 2111202]
                        default:
                            assertLockTestSQLs(Select + joinTables + Where + NoForUpdate);
                        }
                    }

                    public void namedQueryPessimisticWriteDbSql(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:   //TODO: **Non-atomic lock. if jpa2, DO NOT lock LSE1xmRt
                                    // if jpa2/extended, LOCK LSE1xmLfEgr_LSE1xmRt
                            // The database is unable to lock this query.  Each object matching the query will be 
                            //  locked individually after it is loaded; however, it is technically possible that
                            //  another transaction could modify the data before the lock is obtained.
                            // SELECT t0.id, t0.version, t0.firstName FROM LSE1xmLfEgr t0 
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\')  
                            //      FOR READ ONLY WITH RR USE AND KEEP UPDATE LOCKS [params=(String) firstName%2111201]
                            // SELECT t0.id, t2.id, t2.version, t2.lastName FROM LSE1xmLfEgr t0 
                            //      INNER JOIN LSE1xmLfEgr_LSE1xmRt t1 ON t0.id = t1.LSE1XMLFEGR_ID 
                            //      INNER JOIN LSE1xmRt t2 ON t1.UNIRIGHT_ID = t2.id 
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\') ORDER BY t0.id ASC  
                            //      [params=(String) firstName%2111201]
                            // SELECT t0.id FROM LSE1xmRt t0 WHERE t0.id = ?  
                            //      FOR READ ONLY WITH RS USE AND KEEP UPDATE LOCKS [params=(int) 2121201]
                            // SELECT t0.version FROM LSE1xmRt t0 WHERE t0.id = ?  [params=(int) 2121201]
                            // SELECT t0.id FROM LSE1xmRt t0 WHERE t0.id = ?  
                            //      FOR READ ONLY WITH RS USE AND KEEP UPDATE LOCKS [params=(int) 2121202]
                            // SELECT t0.version FROM LSE1xmRt t0 WHERE t0.id = ?  [params=(int) 2121202]
                            // SELECT t0.version FROM LSE1xmLfEgr t0 WHERE t0.id = ?  [params=(int) 2111201]
                            assertLockTestSQLs(Select + NoJoin + Any + tableLfName + Any + NoJoin + Where + DB2Lock,
                                    Select + joinTables + Where + NoDB2Lock,
                                    Select + NoJoin + Any + tableRtName + Any + NoJoin + Where + DB2Lock,
                                    Select + NoJoin + Any + tableRtName + Any + NoJoin + Where + DB2Lock);
                            break;
                        case oracle:    // TODO: if jpa2, DO NOT lock LSE1xmRT using "FOR UPDATE OF col"
                            // SELECT t0.id, t0.version, t0.firstName FROM LSE1xmLfEgr t0 WHERE (t0.firstName LIKE ?) 
                            //      FOR UPDATE [params=(String) firstName%2111201]
                            // SELECT t0.id, t2.id, t2.version, t2.lastName 
                            //      FROM LSE1xmLfEgr t0, LSE1xmLfEgr_LSE1xmRt t1, LSE1xmRt t2 
                            //      WHERE (t0.firstName LIKE ?) AND t0.id = t1.LSE1XMLFEGR_ID AND t1.UNIRIGHT_ID = t2.id 
                            //      ORDER BY t0.id ASC FOR UPDATE [params=(String) firstName%2111201]
                            // SELECT t0.version FROM LSE1xmRt t0 WHERE t0.id = ? [params=(int) 2121202]
                            // SELECT t0.version FROM LSE1xmRt t0 WHERE t0.id = ? [params=(int) 2121201]
                            // SELECT t0.version FROM LSE1xmLfEgr t0 WHERE t0.id = ? [params=(int) 2111201]
                            assertLockTestSQLs(Select + NoJoin + tableLfName + NoJoin + Where + ForUpdate,
                                    Select + tableLfName + Any + tableJTName + Any + tableRtName + Where
                                    + ForUpdate);
                            break;
                        case derby:     //TODO: **Non-atomic lock. If jpa2, DO NOT lock LSE1xmRt, 
                                        // if jpa2/extended, LOCK LSE1xmLfEgr_LSE1xmRt
                            // The database is unable to lock this query.  Each object matching the query will be 
                            //  locked individually after it is loaded; however, it is technically possible that
                            //  another transaction could modify the data before the lock is obtained.
                            // SELECT t0.id, t0.version, t0.firstName FROM LSE1xmLfEgr t0 
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\') FOR UPDATE WITH RR
                            //      [params=(String) firstName%2111201]
                            // SELECT t0.id, t2.id, t2.version, t2.lastName FROM LSE1xmLfEgr t0
                            //      INNER JOIN LSE1xmLfEgr_LSE1xmRt t1 ON t0.id = t1.LSE1XMLFEGR_ID
                            //      INNER JOIN LSE1xmRt t2 ON t1.UNIRIGHT_ID = t2.id
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\') ORDER BY t0.id ASC
                            //      [params=(String) firstName%2111201]
                            // SELECT t0.id FROM LSE1xmRt t0 WHERE t0.id = ? FOR UPDATE WITH RR [params=(int) 2121202]
                            // SELECT t0.version FROM LSE1xmRt t0 WHERE t0.id = ? [params=(int) 2121202]
                            // SELECT t0.id FROM LSE1xmRt t0 WHERE t0.id = ? FOR UPDATE WITH RR [params=(int) 2121201]
                            // SELECT t0.version FROM LSE1xmRt t0 WHERE t0.id = ? [params=(int) 2121201]
                            // SELECT t0.version FROM LSE1xmLfEgr t0 WHERE t0.id = ? [params=(int) 2111201]
                            assertLockTestSQLs(Select + joinTables + Where + NoForUpdate,
                                    Select + NoJoin + tableLfName + NoJoin + Where + ForUpdate,
                                    Select + NoJoin + tableRtName + NoJoin + Where + ForUpdate,
                                    Select + NoJoin + tableRtName + NoJoin + Where + ForUpdate
                                    );
                            break;
                        default:
                            assertLockTestSQLs(Select + joinTables + Where + ForUpdate);
                        }
                    }

                    public void findNoLockAfterNamedQueryPessimisticWriteDbSql(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.version, t0.firstName, t1.LSE1XMLFEGR_ID, t2.id, t2.version, t2.lastName 
                            //      FROM LSE1xmLfEgr t0 LEFT OUTER JOIN LSE1xmLfEgr_LSE1xmRt t1 
                            //      ON t0.id = t1.LSE1XMLFEGR_ID LEFT OUTER JOIN LSE1xmRt t2 
                            //      ON t1.UNIRIGHT_ID = t2.id WHERE t0.id = ?  [params=(int) 2111202]
                            assertLockTestSQLs(Select + joinTables + Where + NoDB2Lock);
                            break;
                        case oracle:
                            // SELECT t0.version, t0.firstName, t1.LSE1XMLFEGR_ID, t2.id, t2.version, t2.lastName 
                            //      FROM LSE1xmLfEgr t0, LSE1xmLfEgr_LSE1xmRt t1, LSE1xmRt t2 
                            //      WHERE t0.id = ? AND t0.id = t1.LSE1XMLFEGR_ID(+) AND t1.UNIRIGHT_ID = t2.id(+) 
                            //      [params=(int) 2111202]
                            assertLockTestSQLs(Select + tableLfName + Any + tableJTName + Any + tableRtName + Where
                                    + "\\(\\+\\).*" + NoForUpdate);
                            break;
                        case derby:
                            // SELECT t0.version, t0.firstName, t1.LSE1XMLFEGR_ID, t2.id, t2.version, t2.lastName
                            //      FROM LSE1xmLfEgr t0 LEFT OUTER JOIN LSE1xmLfEgr_LSE1xmRt t1
                            //      ON t0.id = t1.LSE1XMLFEGR_ID LEFT OUTER JOIN LSE1xmRt t2
                            //      ON t1.UNIRIGHT_ID = t2.id WHERE t0.id = ? [params=(int) 2111202]
                        default:
                            assertLockTestSQLs(Select + joinTables + Where + NoForUpdate);
                        }
                    }
                });
    }

    public void testNormalUni1xmJTLock() {
        common1xmJTLock("testNormalUni1xmJTLock", 2112101, false);
    }

    public void testExtendedUni1xmJTLock() {
        common1xmJTLock("testExtendedUni1xmJTLock", 2112111, true);
    }

    private void common1xmJTLock(String testName, int idLf0, boolean extended) {
        final String tableLfName = "LSE1xmLfJT";
//        final String tableRtName = "LSE1xmRt";
//        final String joinTables  = tableLfName + ".*JOIN.*" + tableRtName;
        getLog().info("** " + testName + "()");
        String scope = extended ? "Extended" : "Normal";
        int idRt00 = idLf0 + 10000; // right table
        int idRt01 = idRt00 + 1;
        int idLf1  = idLf0 + 1;
        int idRt10 = idLf1 + 10000 + 1; // right table
        int idRt11 = idRt10 + 1;
        // create test entity.
        LSE1xmLfJT eLf0 = new LSE1xmLfJT();
        LSE1xmRt eRt00 = new LSE1xmRt();
        LSE1xmRt eRt01 = new LSE1xmRt();
        eLf0.setId(idLf0);
        eLf0.setFirstName("firstName " + idLf0);
        eLf0.addUnitRight(eRt00);
        eLf0.addUnitRight(eRt01);
        eRt00.setId(idRt00);
        eRt00.setLastName("lastName " + idRt00);
        eRt01.setId(idRt01);
        eRt01.setLastName("lastName " + idRt01);
        
        LSE1xmLfJT eLf1 = new LSE1xmLfJT();
        LSE1xmRt eRt10 = new LSE1xmRt();
        LSE1xmRt eRt11 = new LSE1xmRt();
        eLf1.setId(idLf1);
        eLf1.setFirstName("firstName " + idLf1);
        eLf1.addUnitRight(eRt10);
        eLf1.addUnitRight(eRt11);
        eRt10.setId(idRt10);
        eRt10.setLastName("lastName " + idRt10);
        eRt11.setId(idRt11);
        eRt11.setLastName("lastName " + idRt11);
       
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            em.getTransaction().begin();
            em.persist(eRt00);
            em.persist(eRt01);
            em.persist(eLf0);
            em.persist(eRt10);
            em.persist(eRt11);
            em.persist(eLf1);
            em.getTransaction().commit();
        } finally {
            em = null;
            eLf0 = eLf1 = null;
            eRt00 = eRt01 = eRt10 = eRt11 = null;
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        commonLockTest(testName, LSE1xmLfJT.class, idLf0, extended,
                "SELECT c FROM LSE1xmLfJT c WHERE c.firstName LIKE :firstName", "findLSE1xmLfJT" + scope,
                new AssertCallback() {
                    public void findNoLockDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.version, t0.firstName FROM LSE1xmLfJT t0 WHERE t0.id = ?  
                            //      optimize for 1 row [params=(int) 2112101]
                            assertLockTestSQLs(Select + tableLfName + Where + NoDB2Lock);
                            break;
                        case oracle:
                            // SELECT t0.version, t0.firstName FROM LSE1xmLfJT t0 WHERE t0.id = ? [params=(int) 2112101]
                        case derby:
                            // SELECT t0.version, t0.firstName FROM LSE1xmLfJT t0 WHERE t0.id = ? [params=(int) 2112101]
                        default:
                            assertLockTestSQLs(Select + tableLfName + Where + NoForUpdate);
                        }
                    }

                    public void findPessimisticForcIncDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.version, t0.firstName FROM LSE1xmLfJT t0 WHERE t0.id = ?  
                            //      optimize for 1 row FOR READ ONLY WITH RR USE AND KEEP UPDATE LOCKS 
                            //      [params=(int) 2112102]
                            // SELECT t0.version FROM LSE1xmLfJT t0 WHERE t0.id = ?  
                            //      FOR READ ONLY WITH RR USE AND KEEP UPDATE LOCKS [params=(int) 2112102]
                            assertLockTestSQLs(Select + tableLfName + Where + DB2Lock);
                            break;
                        case oracle:
                            // SELECT t0.version, t0.firstName FROM LSE1xmLfJT t0 WHERE t0.id = ? FOR UPDATE 
                            //      [params=(int) 2112102]
                            // SELECT t0.version FROM LSE1xmLfJT t0 WHERE t0.id = ? FOR UPDATE [params=(int) 2112102] 
                        case derby:
                            // SELECT t0.version, t0.firstName FROM LSE1xmLfJT t0 WHERE t0.id = ? FOR UPDATE WITH RR
                            //      [params=(int) 2112102]
                            // SELECT t0.version FROM LSE1xmLfJT t0 WHERE t0.id = ? FOR UPDATE WITH RR 
                            //      [params=(int) 2112102]
                        default:
                            assertLockTestSQLs(Select + tableLfName + Where + ForUpdate);
                        }
                    }

                    public void queryPessimisticReadDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.id, t0.version, t0.firstName FROM LSE1xmLfJT t0 
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\')  
                            //      FOR READ ONLY WITH RS USE AND KEEP UPDATE LOCKS [params=(String) firstName%2112101]
                            // SELECT t0.version FROM LSE1xmLfJT t0 WHERE t0.id = ?  [params=(int) 2112101]
                            assertLockTestSQLs(Select + tableLfName + Where + DB2Lock);
                            break;
                        case oracle:
                            // SELECT t0.id, t0.version, t0.firstName FROM LSE1xmLfJT t0 WHERE (t0.firstName LIKE ?) 
                            //      FOR UPDATE [params=(String) firstName%2112101]
                            // SELECT t0.version FROM LSE1xmLfJT t0 WHERE t0.id = ? [params=(int) 2112101] 
                        case derby:
                            // SELECT t0.id, t0.version, t0.firstName FROM LSE1xmLfJT t0
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\') FOR UPDATE WITH RR
                            //      [params=(String) firstName%2112101]
                            // SELECT t0.version FROM LSE1xmLfJT t0 WHERE t0.id = ? [params=(int) 2112101]
                        default:
                            assertLockTestSQLs(Select + tableLfName + Where + ForUpdate);
                        }
                    }

                    public void findNoLockAfterQueryPessimisticReadDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.version, t0.firstName FROM LSE1xmLfJT t0 WHERE t0.id = ? 
                            //      optimize for 1 row [params=(int) 2112102]
                            assertLockTestSQLs(Select + tableLfName + Where + NoDB2Lock);
                            break;
                        case oracle:
                            // SELECT t0.version, t0.firstName FROM LSE1xmLfJT t0 WHERE t0.id = ? [params=(int) 2112102] 
                        case derby:
                            // SELECT t0.version, t0.firstName FROM LSE1xmLfJT t0 WHERE t0.id = ? [params=(int) 2112102]
                        default:
                            assertLockTestSQLs(Select + tableLfName + Where + NoForUpdate);
                        }
                    }

                    public void namedQueryPessimisticWriteDbSql(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.id, t0.version, t0.firstName FROM LSE1xmLfJT t0 
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\')  
                            //      FOR READ ONLY WITH RR USE AND KEEP UPDATE LOCKS [params=(String) firstName%2112101]
                            // SELECT t0.version FROM LSE1xmLfJT t0 WHERE t0.id = ?  [params=(int) 2112101]
                            assertLockTestSQLs(Select + tableLfName + Where + DB2Lock);
                            break;
                        case oracle:
                            // SELECT t0.id, t0.version, t0.firstName FROM LSE1xmLfJT t0 WHERE (t0.firstName LIKE ?) 
                            //      FOR UPDATE [params=(String) firstName%2112101]
                            // SELECT t0.version FROM LSE1xmLfJT t0 WHERE t0.id = ? [params=(int) 2112101] 
                        case derby:
                            // SELECT t0.id, t0.version, t0.firstName FROM LSE1xmLfJT t0
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\') FOR UPDATE WITH RR
                            //      [params=(String) firstName%2112101]
                            // SELECT t0.version FROM LSE1xmLfJT t0 WHERE t0.id = ? [params=(int) 2112101]
                        default:
                            assertLockTestSQLs(Select + tableLfName + Where + ForUpdate);
                        }
                    }

                    public void findNoLockAfterNamedQueryPessimisticWriteDbSql(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.version, t0.firstName FROM LSE1xmLfJT t0 WHERE t0.id = ?  
                            //      optimize for 1 row [params=(int) 2112102]
                            assertLockTestSQLs(Select + tableLfName + Where + NoDB2Lock);
                            break;
                        case oracle:
                            // SELECT t0.version, t0.firstName FROM LSE1xmLfJT t0 WHERE t0.id = ? [params=(int) 2112102] 
                        case derby:
                            // SELECT t0.version, t0.firstName FROM LSE1xmLfJT t0 WHERE t0.id = ? [params=(int) 2112102]
                        default:
                            assertLockTestSQLs(Select + tableLfName + Where + NoForUpdate);
                        }
                    }
                });
    }

    public void testNormalUni1xmJTEagerLock() {
        common1xmJTEagerLock("testNormalUni1xmJTEagerLock", 2112201, false);
    }

    public void testExtendedUni1xmJTEagerLock() {
        common1xmJTEagerLock("testExtendedUni1xmJTEagerLock", 2112211, true);
    }

    private void common1xmJTEagerLock(String testName, int idLf0, boolean extended) {
        final String tableLfName = "LSE1xmLfJTEgr";
        final String tableJTName = "LSE1xmLfJTEgr_LSE1xmRt";
        final String tableRtName = "LSE1xmRt";
        final String joinTables  = tableLfName + ".*JOIN.*" + tableJTName + ".*JOIN.*" + tableRtName;
        getLog().info("** " + testName + "()");
        String scope = extended ? "Extended" : "Normal";
        int idRt00 = idLf0 + 10000; // right table
        int idRt01 = idRt00 + 1;
        int idLf1  = idLf0 + 1;
        int idRt10 = idLf1 + 10000 + 1; // right table
        int idRt11 = idRt10 + 1;
        // create test entity.
        LSE1xmLfJTEgr eLf0 = new LSE1xmLfJTEgr();
        LSE1xmRt eRt00 = new LSE1xmRt();
        LSE1xmRt eRt01 = new LSE1xmRt();
        eLf0.setId(idLf0);
        eLf0.setFirstName("firstName " + idLf0);
        eLf0.addUnitRight(eRt00);
        eLf0.addUnitRight(eRt01);
        eRt00.setId(idRt00);
        eRt00.setLastName("lastName " + idRt00);
        eRt01.setId(idRt01);
        eRt01.setLastName("lastName " + idRt01);
        
        LSE1xmLfJTEgr eLf1 = new LSE1xmLfJTEgr();
        LSE1xmRt eRt10 = new LSE1xmRt();
        LSE1xmRt eRt11 = new LSE1xmRt();
        eLf1.setId(idLf1);
        eLf1.setFirstName("firstName " + idLf1);
        eLf1.addUnitRight(eRt10);
        eLf1.addUnitRight(eRt11);
        eRt10.setId(idRt10);
        eRt10.setLastName("lastName " + idRt10);
        eRt11.setId(idRt11);
        eRt11.setLastName("lastName " + idRt11);
       
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            em.getTransaction().begin();
            em.persist(eRt00);
            em.persist(eRt01);
            em.persist(eLf0);
            em.persist(eRt10);
            em.persist(eRt11);
            em.persist(eLf1);
            em.getTransaction().commit();
        } finally {
            em = null;
            eLf0 = eLf1 = null;
            eRt00 = eRt01 = eRt10 = eRt11 = null;
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        commonLockTest(testName, LSE1xmLfJTEgr.class, idLf0, extended,
                "SELECT c FROM LSE1xmLfJTEgr c WHERE c.firstName LIKE :firstName", "findLSE1xmLfJTEgr"
                        + scope,
                new AssertCallback() {
                    public void findNoLockDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.version, t0.firstName, t1.LSE1XMLFJTEGR_ID, t2.id, t2.version, t2.lastName 
                            //      FROM LSE1xmLfJTEgr t0 LEFT OUTER JOIN LSE1xmLfJTEgr_LSE1xmRt t1 
                            //      ON t0.id = t1.LSE1XMLFJTEGR_ID LEFT OUTER JOIN LSE1xmRt t2 
                            //      ON t1.UNIRIGHT_ID = t2.id WHERE t0.id = ?  [params=(int) 2112201]
                            assertLockTestSQLs(Select + joinTables + Where + NoDB2Lock);
                            break;
                        case oracle:
                            // SELECT t0.version, t0.firstName, t1.LSE1XMLFJTEGR_ID, t2.id, t2.version, t2.lastName 
                            //      FROM LSE1xmLfJTEgr t0, LSE1xmLfJTEgr_LSE1xmRt t1, LSE1xmRt t2 
                            //      WHERE t0.id = ? AND t0.id = t1.LSE1XMLFJTEGR_ID(+) AND t1.UNIRIGHT_ID = t2.id(+) 
                            //      [params=(int) 2112201]
                            assertLockTestSQLs(Select + tableLfName + Any + tableJTName + Any + tableRtName + Where
                                    + "\\(\\+\\).*" + NoForUpdate);
                            break;
                        case derby:
                            // SELECT t0.version, t0.firstName, t1.LSE1XMLFJTEGR_ID, t2.id, t2.version, t2.lastName
                            //      FROM LSE1xmLfJTEgr t0 LEFT OUTER JOIN LSE1xmLfJTEgr_LSE1xmRt t1
                            //      ON t0.id = t1.LSE1XMLFJTEGR_ID LEFT OUTER JOIN LSE1xmRt t2
                            //      ON t1.UNIRIGHT_ID = t2.id WHERE t0.id = ? [params=(int) 2112201]
                        default:
                            assertLockTestSQLs(Select + joinTables + Where + NoForUpdate);
                        }
                    }

                    public void findPessimisticForcIncDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.version, t0.firstName, t1.LSE1XMLFJTEGR_ID, t2.id, t2.version, t2.lastName 
                            //      FROM LSE1xmLfJTEgr t0 LEFT OUTER JOIN LSE1xmLfJTEgr_LSE1xmRt t1 
                            //      ON t0.id = t1.LSE1XMLFJTEGR_ID LEFT OUTER JOIN LSE1xmRt t2 
                            //      ON t1.UNIRIGHT_ID = t2.id WHERE t0.id = ?  
                            //      FOR READ ONLY WITH RR USE AND KEEP UPDATE LOCKS [params=(int) 2112202]
                            // SELECT t0.version FROM LSE1xmRt t0 WHERE t0.id = ?  
                            //      FOR READ ONLY WITH RR USE AND KEEP UPDATE LOCKS [params=(int) 2122203]
                            // SELECT t0.version FROM LSE1xmRt t0 WHERE t0.id = ?  
                             //     FOR READ ONLY WITH RR USE AND KEEP UPDATE LOCKS [params=(int) 2122204]
                            // SELECT t0.version FROM LSE1xmLfJTEgr t0 WHERE t0.id = ?  
                            //      FOR READ ONLY WITH RR USE AND KEEP UPDATE LOCKS [params=(int) 2112202]
                            assertLockTestSQLs(Select + joinTables + Where + DB2Lock);
                            break;
                        case oracle:    // TODO: if jpa2, DO NOT lock LSE1xmRT using "FOR UPDATE OF col"
                            // SELECT t0.version, t0.firstName, t1.LSE1XMLFJTEGR_ID, t2.id, t2.version, t2.lastName 
                            //      FROM LSE1xmLfJTEgr t0, LSE1xmLfJTEgr_LSE1xmRt t1, LSE1xmRt t2 
                            //      WHERE t0.id = ? AND t0.id = t1.LSE1XMLFJTEGR_ID(+) AND t1.UNIRIGHT_ID = t2.id(+) 
                            //      FOR UPDATE [params=(int) 2112202]
                            // SELECT t0.version FROM LSE1xmRt t0 WHERE t0.id = ? FOR UPDATE [params=(int) 2122203]
                            // SELECT t0.version FROM LSE1xmRt t0 WHERE t0.id = ? FOR UPDATE [params=(int) 2122204]
                            // SELECT t0.version FROM LSE1xmLfJTEgr t0 WHERE t0.id = ? FOR UPDATE [params=(int) 2112202]
                            assertLockTestSQLs(Select + tableLfName + Any + tableJTName + Any + tableRtName + Where
                                    + "\\(\\+\\).*" + ForUpdate);
                            break;
                        case derby:     //TODO: **Non-atomic lock. If jpa2, DO NOT lock LSE1xmRt, 
                                        // if jpa2/extended, LOCK LSE1xmLfJTEgr_LSE1xmRt
                            // The database is unable to lock this query.  Each object matching the query will be 
                            //  locked individually after it is loaded; however, it is technically possible that
                            //  another transaction could modify the data before the lock is obtained.
                            // SELECT t0.version, t0.firstName, t1.LSE1XMLFJTEGR_ID, t2.id, t2.version, t2.lastName 
                            //      FROM LSE1xmLfJTEgr t0 LEFT OUTER JOIN LSE1xmLfJTEgr_LSE1xmRt t1
                            //      ON t0.id = t1.LSE1XMLFJTEGR_ID LEFT OUTER JOIN LSE1xmRt t2
                            //      ON t1.UNIRIGHT_ID = t2.id WHERE t0.id = ? [params=(int) 2112202]
                            // SELECT t0.id FROM LSE1xmRt t0 WHERE t0.id = ? FOR UPDATE WITH RR
                            //      [params=(int) 2122203]
                            // SELECT t0.version FROM LSE1xmRt t0 WHERE t0.id = ? FOR UPDATE WITH RR
                            //      [params=(int) 2122203]
                            // SELECT t0.id FROM LSE1xmRt t0 WHERE t0.id = ? FOR UPDATE WITH RR
                            //      [params=(int) 2122204]
                            // SELECT t0.version FROM LSE1xmRt t0 WHERE t0.id = ? FOR UPDATE WITH RR
                            //      [params=(int) 2122204]
                            // SELECT t0.id FROM LSE1xmLfJTEgr t0 WHERE t0.id = ? FOR UPDATE WITH RR
                            //      [params=(int) 2112202]
                            // SELECT t0.version FROM LSE1xmLfJTEgr t0 WHERE t0.id = ? FOR UPDATE WITH RR
                            //      [params=(int) 2112202]
                            assertLockTestSQLs(Select + joinTables + Where + NoForUpdate,
                                    Select + NoJoin + Any + tableLfName + Any + NoJoin + Where + ForUpdate,
                                    Select + NoJoin + Any + tableRtName + Any + NoJoin + Where + ForUpdate,
                                    Select + NoJoin + Any + tableRtName + Any + NoJoin + Where + ForUpdate
                                    );
                            break;
                        default:
                            assertLockTestSQLs(Select + joinTables + Where + ForUpdate);
                        }
                    }

                    public void queryPessimisticReadDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:       //TODO: **Non-atomic lock. if jpa2, DO NOT lock LSE1xmRt
                                        // if jpa2/extended, LOCK LSE1xmLfJTEgr_LSE1xmRt
                            // The database is unable to lock this query.  Each object matching the query will be 
                            //  locked individually after it is loaded; however, it is technically possible that
                            //  another transaction could modify the data before the lock is obtained.
                            // SELECT t0.id, t0.version, t0.firstName FROM LSE1xmLfJTEgr t0 
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\')  
                            //      FOR READ ONLY WITH RS USE AND KEEP UPDATE LOCKS [params=(String) firstName%2112201]
                            // SELECT t0.id, t2.id, t2.version, t2.lastName FROM LSE1xmLfJTEgr t0 
                            //      INNER JOIN LSE1xmLfJTEgr_LSE1xmRt t1 ON t0.id = t1.LSE1XMLFJTEGR_ID 
                            //      INNER JOIN LSE1xmRt t2 ON t1.UNIRIGHT_ID = t2.id 
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\') ORDER BY t0.id ASC  
                            //      [params=(String) firstName%2112201]
                            // SELECT t0.id FROM LSE1xmRt t0 WHERE t0.id = ?  
                            //      FOR READ ONLY WITH RS USE AND KEEP UPDATE LOCKS [params=(int) 2122201]
                            // SELECT t0.version FROM LSE1xmRt t0 WHERE t0.id = ?  [params=(int) 2122201]
                            // SELECT t0.id FROM LSE1xmRt t0 WHERE t0.id = ?  
                            //      FOR READ ONLY WITH RS USE AND KEEP UPDATE LOCKS [params=(int) 2122202]
                            // SELECT t0.version FROM LSE1xmRt t0 WHERE t0.id = ?  [params=(int) 2122202]
                            // SELECT t0.version FROM LSE1xmLfJTEgr t0 WHERE t0.id = ?  [params=(int) 2112201]
                            assertLockTestSQLs(Select + NoJoin + Any + tableLfName + Any + NoJoin + Where + DB2Lock,
                                    Select + joinTables + Where + NoDB2Lock,
                                    Select + NoJoin + Any + tableRtName + Any + NoJoin + Where + DB2Lock,
                                    Select + NoJoin + Any + tableRtName + Any + NoJoin + Where + DB2Lock);
                            break;
                        case oracle:    // TODO: if jpa2, DO NOT lock LSE1xmRT using "FOR UPDATE OF col"
                            // SELECT t0.id, t0.version, t0.firstName FROM LSE1xmLfJTEgr t0 WHERE (t0.firstName LIKE ?) 
                            //      FOR UPDATE [params=(String) firstName%2112201]
                            // SELECT t0.id, t2.id, t2.version, t2.lastName 
                            //      FROM LSE1xmLfJTEgr t0, LSE1xmLfJTEgr_LSE1xmRt t1, LSE1xmRt t2 
                            //      WHERE (t0.firstName LIKE ?) AND t0.id = t1.LSE1XMLFJTEGR_ID 
                            //      AND t1.UNIRIGHT_ID = t2.id ORDER BY t0.id ASC FOR UPDATE 
                            //      [params=(String) firstName%2112201]
                            // SELECT t0.version FROM LSE1xmRt t0 WHERE t0.id = ? [params=(int) 2122201]
                            // SELECT t0.version FROM LSE1xmRt t0 WHERE t0.id = ? [params=(int) 2122202]
                            // SELECT t0.version FROM LSE1xmLfJTEgr t0 WHERE t0.id = ? [params=(int) 2112201]
                            assertLockTestSQLs(Select + NoJoin + tableLfName + NoJoin + Where + ForUpdate,
                                    Select + tableLfName + Any + tableJTName + Any + tableRtName + Where
                                    + ForUpdate);
                            break;
                        case derby:     //TODO: **Non-atomic lock. If jpa2, DO NOT lock LSE1xmRt, 
                                        // if jpa2/extended, LOCK LSE1xmLfEgr_LSE1xmRt
                            // The database is unable to lock this query.  Each object matching the query will be 
                            //  locked individually after it is loaded; however, it is technically possible that
                            //  another transaction could modify the data before the lock is obtained.
                            // SELECT t0.id, t0.version, t0.firstName FROM LSE1xmLfJTEgr t0
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\') FOR UPDATE WITH RR
                            //      [params=(String) firstName%2112201]
                            // SELECT t0.id, t2.id, t2.version, t2.lastName FROM LSE1xmLfJTEgr t0 
                            //      NNER JOIN LSE1xmLfJTEgr_LSE1xmRt t1 ON t0.id = t1.LSE1XMLFJTEGR_ID
                            //      INNER JOIN LSE1xmRt t2 ON t1.UNIRIGHT_ID = t2.id
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\') ORDER BY t0.id ASC
                            //      [params=(String) firstName%2112201]
                            // SELECT t0.id FROM LSE1xmRt t0 WHERE t0.id = ? FOR UPDATE WITH RR [params=(int) 2122202]
                            // SELECT t0.version FROM LSE1xmRt t0 WHERE t0.id = ? [params=(int) 2122202]
                            // SELECT t0.id FROM LSE1xmRt t0 WHERE t0.id = ? FOR UPDATE WITH RR [params=(int) 2122201]
                            // SELECT t0.version FROM LSE1xmRt t0 WHERE t0.id = ? [params=(int) 2122201]
                            // SELECT t0.version FROM LSE1xmLfJTEgr t0 WHERE t0.id = ? [params=(int) 2112201]
                            assertLockTestSQLs(Select + joinTables + Where + NoForUpdate,
                                    Select + NoJoin + tableLfName + NoJoin + Where + ForUpdate,
                                    Select + NoJoin + tableRtName + NoJoin + Where + ForUpdate,
                                    Select + NoJoin + tableRtName + NoJoin + Where + ForUpdate
                                    );
                            break;
                        default:
                            assertLockTestSQLs(Select + joinTables + Where + ForUpdate);
                        }
                    }

                    public void findNoLockAfterQueryPessimisticReadDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.version, t0.firstName, t1.LSE1XMLFJTEGR_ID, t2.id, t2.version, t2.lastName 
                            //      FROM LSE1xmLfJTEgr t0 LEFT OUTER JOIN LSE1xmLfJTEgr_LSE1xmRt t1 
                            //      ON t0.id = t1.LSE1XMLFJTEGR_ID LEFT OUTER JOIN LSE1xmRt t2 
                            //      ON t1.UNIRIGHT_ID = t2.id WHERE t0.id = ?  [params=(int) 2112202]
                            assertLockTestSQLs(Select + joinTables + Where + NoDB2Lock);
                            break;
                        case oracle:
                            // SELECT t0.version, t0.firstName, t1.LSE1XMLFJTEGR_ID, t2.id, t2.version, t2.lastName 
                            //      FROM LSE1xmLfJTEgr t0, LSE1xmLfJTEgr_LSE1xmRt t1, LSE1xmRt t2 
                            //      WHERE t0.id = ? AND t0.id = t1.LSE1XMLFJTEGR_ID(+) AND t1.UNIRIGHT_ID = t2.id(+) 
                            //      [params=(int) 2112202]
                            assertLockTestSQLs(Select + tableLfName + Any + tableJTName + Any + tableRtName + Where
                                    + "\\(\\+\\).*" + NoForUpdate);
                            break;
                        case derby:
                            // SELECT t0.version, t0.firstName, t1.LSE1XMLFJTEGR_ID, t2.id, t2.version, t2.lastName
                            //      FROM LSE1xmLfJTEgr t0 LEFT OUTER JOIN LSE1xmLfJTEgr_LSE1xmRt t1
                            //      ON t0.id = t1.LSE1XMLFJTEGR_ID LEFT OUTER JOIN LSE1xmRt t2
                            //      ON t1.UNIRIGHT_ID = t2.id WHERE t0.id = ? [params=(int) 2112202]
                        default:
                            assertLockTestSQLs(Select + joinTables + Where + NoForUpdate);
                        }
                    }

                    public void namedQueryPessimisticWriteDbSql(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:   //TODO: **Non-atomic lock. if jpa2, DO NOT lock LSE1xmRt
                                    // if jpa2/extended, LOCK LSE1xmLfJTEgr_LSE1xmRt
                            // The database is unable to lock this query.  Each object matching the query will be 
                            //  locked individually after it is loaded; however, it is technically possible that
                            //  another transaction could modify the data before the lock is obtained.
                            // SELECT t0.id, t0.version, t0.firstName FROM LSE1xmLfJTEgr t0 
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\')  
                            //      FOR READ ONLY WITH RR USE AND KEEP UPDATE LOCKS [params=(String) firstName%2112201]
                            // SELECT t0.id, t2.id, t2.version, t2.lastName FROM LSE1xmLfJTEgr t0 
                            //      INNER JOIN LSE1xmLfJTEgr_LSE1xmRt t1 ON t0.id = t1.LSE1XMLFJTEGR_ID 
                            //      INNER JOIN LSE1xmRt t2 ON t1.UNIRIGHT_ID = t2.id 
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\') ORDER BY t0.id ASC  
                            //      [params=(String) firstName%2112201]
                            // SELECT t0.id FROM LSE1xmRt t0 WHERE t0.id = ?  
                            //      FOR READ ONLY WITH RS USE AND KEEP UPDATE LOCKS [params=(int) 2122201]
                            // SELECT t0.version FROM LSE1xmRt t0 WHERE t0.id = ?  [params=(int) 2122201]
                            // SELECT t0.id FROM LSE1xmRt t0 WHERE t0.id = ?  
                            //      FOR READ ONLY WITH RS USE AND KEEP UPDATE LOCKS [params=(int) 2122202]
                            // SELECT t0.version FROM LSE1xmRt t0 WHERE t0.id = ?  [params=(int) 2122202]
                            // SELECT t0.version FROM LSE1xmLfJTEgr t0 WHERE t0.id = ?  [params=(int) 2112201]
                            assertLockTestSQLs(Select + NoJoin + Any + tableLfName + Any + NoJoin + Where + DB2Lock,
                                    Select + joinTables + Where + NoDB2Lock,
                                    Select + NoJoin + Any + tableRtName + Any + NoJoin + Where + DB2Lock,
                                    Select + NoJoin + Any + tableRtName + Any + NoJoin + Where + DB2Lock);
                            break;
                        case oracle:    // TODO: if jpa2, DO NOT lock LSE1xmRT using "FOR UPDATE OF col"
                            // SELECT t0.id, t0.version, t0.firstName FROM LSE1xmLfJTEgr t0 WHERE (t0.firstName LIKE ?) 
                            //      FOR UPDATE [params=(String) firstName%2112201]
                            // SELECT t0.id, t2.id, t2.version, t2.lastName 
                            //      FROM LSE1xmLfJTEgr t0, LSE1xmLfJTEgr_LSE1xmRt t1, LSE1xmRt t2 
                            //      WHERE (t0.firstName LIKE ?) AND t0.id = t1.LSE1XMLFJTEGR_ID 
                            //      AND t1.UNIRIGHT_ID = t2.id ORDER BY t0.id ASC FOR UPDATE
                            //      [params=(String) firstName%2112201]
                            // SELECT t0.version FROM LSE1xmRt t0 WHERE t0.id = ? [params=(int) 2122201]
                            // SELECT t0.version FROM LSE1xmRt t0 WHERE t0.id = ? [params=(int) 2122202]
                            // SELECT t0.version FROM LSE1xmLfJTEgr t0 WHERE t0.id = ? [params=(int) 2112201]
                            assertLockTestSQLs(Select + NoJoin + tableLfName + NoJoin + Where + ForUpdate,
                                    Select + tableLfName + Any + tableJTName + Any + tableRtName + Where
                                    + ForUpdate);
                            break;
                        case derby:     //TODO: **Non-atomic lock. If jpa2, DO NOT lock LSE1xmRt, 
                                        // if jpa2/extended, LOCK LSE1xmLfEgr_LSE1xmRt
                            // The database is unable to lock this query.  Each object matching the query will be 
                            //  locked individually after it is loaded; however, it is technically possible that
                            //  another transaction could modify the data before the lock is obtained.
                            // SELECT t0.id, t0.version, t0.firstName FROM LSE1xmLfJTEgr t0
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\') FOR UPDATE WITH RR 
                            //      params=(String) firstName%2112201]
                            // SELECT t0.id, t2.id, t2.version, t2.lastName FROM LSE1xmLfJTEgr t0
                            //      INNER JOIN LSE1xmLfJTEgr_LSE1xmRt t1 ON t0.id = t1.LSE1XMLFJTEGR_ID
                            //      INNER JOIN LSE1xmRt t2 ON t1.UNIRIGHT_ID = t2.id
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\') ORDER BY t0.id ASC
                            //      [params=(String) firstName%2112201]
                            // SELECT t0.id FROM LSE1xmRt t0 WHERE t0.id = ? FOR UPDATE WITH RR [params=(int) 2122202]
                            // SELECT t0.version FROM LSE1xmRt t0 WHERE t0.id = ? [params=(int) 2122202]
                            // SELECT t0.id FROM LSE1xmRt t0 WHERE t0.id = ? FOR UPDATE WITH RR [params=(int) 2122201]
                            // SELECT t0.version FROM LSE1xmRt t0 WHERE t0.id = ? [params=(int) 2122201]
                            // SELECT t0.version FROM LSE1xmLfJTEgr t0 WHERE t0.id = ? [params=(int) 2112201]
                            assertLockTestSQLs(Select + joinTables + Where + NoForUpdate,
                                    Select + tableLfName + NoJoin + Where + ForUpdate,
                                    Select + NoJoin + tableRtName + NoJoin + Where + ForUpdate,
                                    Select + NoJoin + tableRtName + NoJoin + Where + ForUpdate
                                    );
                            break;
                        default:
                            assertLockTestSQLs(Select + joinTables + Where + ForUpdate);
                        }
                    }

                    public void findNoLockAfterNamedQueryPessimisticWriteDbSql(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.version, t0.firstName, t1.LSE1XMLFJTEGR_ID, t2.id, t2.version, t2.lastName 
                            //      FROM LSE1xmLfJTEgr t0 LEFT OUTER JOIN LSE1xmLfJTEgr_LSE1xmRt t1 
                            //      ON t0.id = t1.LSE1XMLFJTEGR_ID LEFT OUTER JOIN LSE1xmRt t2 
                            //      ON t1.UNIRIGHT_ID = t2.id WHERE t0.id = ?  [params=(int) 2112202]
                            assertLockTestSQLs(Select + joinTables + Where + NoDB2Lock);
                            break;
                        case oracle:
                            // SELECT t0.version, t0.firstName, t1.LSE1XMLFJTEGR_ID, t2.id, t2.version, t2.lastName 
                            //      FROM LSE1xmLfJTEgr t0, LSE1xmLfJTEgr_LSE1xmRt t1, LSE1xmRt t2 
                            //      WHERE t0.id = ? AND t0.id = t1.LSE1XMLFJTEGR_ID(+) AND t1.UNIRIGHT_ID = t2.id(+) 
                            //      [params=(int) 2112202]
                            assertLockTestSQLs(Select + tableLfName + Any + tableJTName + Any + tableRtName + Where
                                    + "\\(\\+\\).*" + NoForUpdate);
                            break;
                        case derby:
                            // SELECT t0.version, t0.firstName, t1.LSE1XMLFJTEGR_ID, t2.id, t2.version, t2.lastName
                            //      FROM LSE1xmLfJTEgr t0 LEFT OUTER JOIN LSE1xmLfJTEgr_LSE1xmRt t1
                            //      ON t0.id = t1.LSE1XMLFJTEGR_ID LEFT OUTER JOIN LSE1xmRt t2
                            //      ON t1.UNIRIGHT_ID = t2.id WHERE t0.id = ? [params=(int) 2112202]
                        default:
                            assertLockTestSQLs(Select + joinTables + Where + NoForUpdate);
                        }
                    }
                });
    }
}

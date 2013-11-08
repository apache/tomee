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
 * - Uni-1x1 - eager fetch (default) 
 * - Uni-1x1 - lazy fetch 
 * - Uni-1x1 use join table - eager fetch (default) 
 * - Uni-1x1 use join table - lazy fetch 
 */
public class Test1x1LockScope extends LockScopeTestCase {

    public void setUp() {
        setSupportedDatabases(
                org.apache.openjpa.jdbc.sql.DerbyDictionary.class,
                org.apache.openjpa.jdbc.sql.OracleDictionary.class,
                org.apache.openjpa.jdbc.sql.DB2Dictionary.class);
        if (isTestsDisabled()) {
            return;
        }

        setUp(LSE1x1Lf.class
            , LSE1x1LfLzy.class
            , LSE1x1LfJT.class
            , LSE1x1LfJTLzy.class
            , LSE1x1Rt.class
            , "openjpa.LockManager", "mixed"
            , "openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)"
        );
        commonSetUp(LSE1x1Lf.class
                , LSE1x1LfLzy.class
                , LSE1x1LfJT.class
                , LSE1x1LfJTLzy.class
                , LSE1x1Rt.class
        );
    }

    public void testNormalUni1x1Lock() {
        common1x1Lock("testNormalUni1x1Lock", 1111201, false);
    }

    public void testExtendedUni1x1Lock() {
        common1x1Lock("testExtendedUni1x1Lock", 1111211, true);
    }

    private void common1x1Lock(String testName, int idLf0, boolean extended) {
        final String tableLfName = "LSE1x1Lf";
        final String tableRtName = "LSE1x1Rt";
        final String joinTables  = tableLfName + ".*JOIN.*" + tableRtName;
        getLog().info("** " + testName + "()");
        String scope = extended ? "Extended" : "Normal";
        int idRt0 = idLf0 + 10000; // right table
        int idLf1 = idLf0 + 1;
        int idRt1 = idRt0 + 1;

        // create test entity.
        LSE1x1Lf eLf0 = new LSE1x1Lf();
        LSE1x1Rt eRt0 = new LSE1x1Rt();
        eLf0.setId(idLf0);
        eLf0.setFirstName("firstName " + idLf0);
        eLf0.setUniRight(eRt0);
        eRt0.setId(idRt0);
        eRt0.setLastName("lastName " + idRt0);
        LSE1x1Lf eLf1 = new LSE1x1Lf();
        LSE1x1Rt eRt1 = new LSE1x1Rt();
        eLf1.setId(idLf1);
        eLf1.setFirstName("firstName " + idLf1);
        eLf1.setUniRight(eRt1);
        eRt1.setId(idRt1);
        eRt1.setLastName("lastName " + idRt1);
       
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            em.getTransaction().begin();
            em.persist(eLf0);
            em.persist(eRt0);
            em.persist(eLf1);
            em.persist(eRt1);
            em.getTransaction().commit();
        } finally {
            em = null;
            eLf0 = eLf1 = null;
            eRt0 = eRt1 = null;
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        commonLockTest(testName, LSE1x1Lf.class, idLf0, extended,
                "SELECT c FROM LSE1x1Lf c WHERE c.firstName LIKE :firstName", "findLSE1x1Lf" + scope,
                new AssertCallback() {
                    public void findNoLockDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.version, t0.firstName, t1.id, t1.version, t1.lastName FROM LSE1x1Lf t0
                            //      LEFT OUTER JOIN LSE1x1Rt t1 ON t0.UNIRIGHT_ID = t1.id WHERE t0.id = ?
                            //      optimize for 1 row [params=(int) 1111201]
                            assertLockTestSQLs(Select + joinTables + Where + NoDB2Lock);
                            break;
                        case oracle:
                            // SELECT t0.version, t0.firstName, t1.id, t1.version, t1.lastName 
                            //      FROM LSE1x1Lf t0, LSE1x1Rt t1 WHERE t0.id = ? AND t0.UNIRIGHT_ID = t1.id(+) 
                            //      [params=(int) 1111201]
                            assertLockTestSQLs(Select + tableLfName + Any + tableRtName + Where + "\\(\\+\\).*"
                                    + NoForUpdate);
                            break;
                        case derby:
                            // SELECT t0.version, t0.firstName, t1.id, t1.version, t1.lastName FROM LSE1x1Lf t0
                            //      LEFT OUTER JOIN LSE1x1Rt t1 ON t0.UNIRIGHT_ID = t1.id WHERE t0.id = ?
                            //      [params=(int) 1111201]
                        default:
                            assertLockTestSQLs(Select + joinTables + Where + NoForUpdate);
                        }
                    }

                    public void findPessimisticForcIncDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.version, t0.firstName, t1.id, t1.version, t1.lastName FROM LSE1x1Lf t0
                            //      LEFT OUTER JOIN LSE1x1Rt t1 ON t0.UNIRIGHT_ID = t1.id WHERE t0.id = ?
                            //      optimize for 1 row FOR READ ONLY WITH RR USE AND KEEP UPDATE LOCKS
                            //      [params=(int) 1111202]
                            // SELECT t0.version FROM LSE1x1Rt t0 WHERE t0.id = ?
                            //      -FOR READ ONLY WITH RR USE AND KEEP UPDATE LOCKS- [params=(int) 1121202]                                                                                                                                 
                            // SELECT t0.version FROM LSE1x1Lf t0 WHERE t0.id = ?
                            //      -FOR READ ONLY WITH RR USE AND KEEP UPDATE LOCKS- [params=(int) 1111202]
                            assertLockTestSQLs(Select + joinTables + Where + DB2Lock,
                                    SelectVersion + NoJoin + Any + tableLfName + Any + NoJoin + Where + NoForUpdate,
                                    SelectVersion + NoJoin + Any + tableRtName + Any + NoJoin + Where + NoForUpdate
                                    );
                            assertLockTestNoSQLs(Select + NoJoin + Any + tableRtName + Any + NoJoin + Where + ForUpdate
                                    );
                            break;
                        case oracle:    // TODO: if jpa2, DO NOT lock LSE1x1RT using "FOR UPDATE OF col"
                            // SELECT t0.version, t0.firstName, t1.id, t1.version, t1.lastName 
                            //      FROM LSE1x1Lf t0, LSE1x1Rt t1 WHERE t0.id = ? AND t0.UNIRIGHT_ID = t1.id(+) 
                            //      FOR UPDATE [params=(int) 1111202]
                            // SELECT t0.version FROM LSE1x1Rt t0 WHERE t0.id = ? FOR UPDATE [params=(int) 1121202]
                            // SELECT t0.version FROM LSE1x1Lf t0 WHERE t0.id = ? FOR UPDATE [params=(int) 1111202]
                            assertLockTestSQLs(Select + tableLfName + Any + tableRtName + Where + "\\(\\+\\).*"
                                    + ForUpdate);
                            break;
                        case derby:     //-TODO: **Non-atomic lock.
                            // The database is unable to lock this query.  Each object matching the query will be 
                            //  locked individually after it is loaded; however, it is technically possible that
                            //  another transaction could modify the data before the lock is obtained.
                            // SELECT t0.version, t0.firstName, t1.id, t1.version, t1.lastName FROM LSE1x1Lf t0
                            //      LEFT OUTER JOIN LSE1x1Rt t1 ON t0.UNIRIGHT_ID = t1.id WHERE t0.id = ?
                            //      [params=(int) 1111202]
                            // SELECT t0.version FROM LSE1x1Rt t0 WHERE t0.id = ? -FOR UPDATE WITH RR-
                            //      [params=(int) 1121202]
                            // SELECT t0.id FROM LSE1x1Lf t0 WHERE t0.id = ? FOR UPDATE WITH RR [params=(int) 1111202]
                            // SELECT t0.version FROM LSE1x1Lf t0 WHERE t0.id = ? -FOR UPDATE WITH RR-
                            //      [params=(int) 1111202]
                            assertLockTestSQLs(Select + joinTables + Where + NoForUpdate,
                                    Select + NoJoin + Any + tableLfName + Any + NoJoin + Where + ForUpdate,
                                    SelectVersion + NoJoin + Any + tableLfName + Any + NoJoin + Where + NoForUpdate,
                                    SelectVersion + NoJoin + Any + tableRtName + Any + NoJoin + Where + NoForUpdate
                                    );
                            //-SELECT t0.id FROM LSE1x1Rt t0 WHERE t0.id = ? FOR UPDATE WITH RR [params=(int) 1121202]-
                            assertLockTestNoSQLs(Select + NoJoin + Any + tableRtName + Any + NoJoin + Where + ForUpdate
                                    );
                            break;
                        default:
                            assertLockTestSQLs(Select + joinTables + Where + ForUpdate);
                        }
                    }

                    public void queryPessimisticReadDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.id, t0.version, t0.firstName, t1.id, t1.version, t1.lastName FROM LSE1x1Lf t0
                            //      LEFT OUTER JOIN LSE1x1Rt t1 ON t0.UNIRIGHT_ID = t1.id
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\')
                            //      FOR READ ONLY WITH RS USE AND KEEP UPDATE LOCKS [params=(String) firstName%1111201]
                            // SELECT t0.version FROM LSE1x1Rt t0 WHERE t0.id = ?  [params=(int) 1121201]
                            // SELECT t0.version FROM LSE1x1Lf t0 WHERE t0.id = ?  [params=(int) 1111201]
                            assertLockTestSQLs(Select + joinTables + Where + DB2Lock,
                                    SelectVersion + NoJoin + Any + tableLfName + Any + NoJoin + Where + NoForUpdate,
                                    SelectVersion + NoJoin + Any + tableRtName + Any + NoJoin + Where + NoForUpdate
                                    );
                            assertLockTestNoSQLs(Select + NoJoin + Any + tableRtName + Any + NoJoin + Where + ForUpdate
                                    );
                            break;
                        case oracle:    // TODO: if jpa2, DO NOT lock LSE1x1RT using "FOR UPDATE OF col"
                            // SELECT t0.id, t0.version, t0.firstName, t1.id, t1.version, t1.lastName 
                            //      FROM LSE1x1Lf t0, LSE1x1Rt t1 
                            //      WHERE (t0.firstName LIKE ?) AND t0.UNIRIGHT_ID = t1.id(+) 
                            //      FOR UPDATE [params=(String) firstName%1111201]
                            // SELECT t0.version FROM LSE1x1Rt t0 WHERE t0.id = ? [params=(int) 1121201]
                            // SELECT t0.version FROM LSE1x1Lf t0 WHERE t0.id = ? [params=(int) 1111201]
                            assertLockTestSQLs(Select + tableLfName + Any + tableRtName + Where + "\\(\\+\\).*"
                                    + ForUpdate);
                            break;
                        case derby:     //-TODO: **Non-atomic lock.
                            // The database is unable to lock this query.  Each object matching the query will be 
                            //  locked individually after it is loaded; however, it is technically possible that
                            //  another transaction could modify the data before the lock is obtained.
                            // SELECT t0.id, t0.version, t0.firstName, t1.id, t1.version, t1.lastName FROM LSE1x1Lf t0
                            //      LEFT OUTER JOIN LSE1x1Rt t1 ON t0.UNIRIGHT_ID = t1.id
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\') [params=(String) firstName%1111201]
                            // SELECT t0.version FROM LSE1x1Rt t0 WHERE t0.id = ? [params=(int) 1121201]
                            // SELECT t0.id FROM LSE1x1Lf t0 WHERE t0.id = ? FOR UPDATE WITH RR [params=(int) 1111201]
                            // SELECT t0.version FROM LSE1x1Lf t0 WHERE t0.id = ? [params=(int) 1111201]
                            assertLockTestSQLs(Select + joinTables + Where + NoForUpdate,
                                    Select + NoJoin + Any + tableLfName + Any + NoJoin + Where + ForUpdate,
                                    SelectVersion + NoJoin + Any + tableLfName + Any + NoJoin + Where + NoForUpdate,
                                    SelectVersion + NoJoin + Any + tableRtName + Any + NoJoin + Where + NoForUpdate
                                    );
                            //-SELECT t0.id FROM LSE1x1Rt t0 WHERE t0.id = ? FOR UPDATE WITH RR [params=(int) 1121201]-
                            assertLockTestNoSQLs(Select + NoJoin + Any + tableRtName + Any + NoJoin + Where + ForUpdate
                                    );
                            break;
                        default:
                            assertLockTestSQLs(Select + joinTables + Where + ForUpdate);
                        }
                    }

                    public void findNoLockAfterQueryPessimisticReadDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.version, t0.firstName, t1.id, t1.version, t1.lastName FROM LSE1x1Lf t0
                            //      LEFT OUTER JOIN LSE1x1Rt t1 ON t0.UNIRIGHT_ID = t1.id WHERE t0.id = ?
                            //      optimize for 1 row [params=(int) 1111202]
                            assertLockTestSQLs(Select + joinTables + Where + NoDB2Lock);
                            break;
                        case oracle:
                            // SELECT t0.version, t0.firstName, t1.id, t1.version, t1.lastName 
                            //      FROM LSE1x1Lf t0, LSE1x1Rt t1 WHERE t0.id = ? AND t0.UNIRIGHT_ID = t1.id(+) 
                            //      [params=(int) 1111202]
                            assertLockTestSQLs(Select + tableLfName + Any + tableRtName + Where + "\\(\\+\\).*"
                                    + NoForUpdate);
                            break;
                        case derby:
                            // SELECT t0.version, t0.firstName, t1.id, t1.version, t1.lastName FROM LSE1x1Lf t0
                            //      LEFT OUTER JOIN LSE1x1Rt t1 ON t0.UNIRIGHT_ID = t1.id WHERE t0.id = ?
                            //      [params=(int) 1111202]
                        default:
                            assertLockTestSQLs(Select + joinTables + Where + NoForUpdate);
                        }
                    }

                    public void namedQueryPessimisticWriteDbSql(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.id, t0.version, t0.firstName, t1.id, t1.version, t1.lastName FROM LSE1x1Lf t0
                            //      LEFT OUTER JOIN LSE1x1Rt t1 ON t0.UNIRIGHT_ID = t1.id
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\')
                            //      FOR READ ONLY WITH RR USE AND KEEP UPDATE LOCKS [params=(String) firstName%1111201]
                            // SELECT t0.version FROM LSE1x1Rt t0 WHERE t0.id = ?  [params=(int) 1121201]
                            // SELECT t0.version FROM LSE1x1Lf t0 WHERE t0.id = ?  [params=(int) 1111201]
                            assertLockTestSQLs(Select + joinTables + Where + DB2Lock,
                                    SelectVersion + NoJoin + Any + tableLfName + Any + NoJoin + Where + NoForUpdate,
                                    SelectVersion + NoJoin + Any + tableRtName + Any + NoJoin + Where + NoForUpdate
                                    );
                            assertLockTestNoSQLs(Select + NoJoin + Any + tableRtName + Any + NoJoin + Where + ForUpdate
                                    );
                            break;
                        case oracle:    // TODO: if jpa2, DO NOT lock LSE1x1RT using "FOR UPDATE OF col"
                            // SELECT t0.id, t0.version, t0.firstName, t1.id, t1.version, t1.lastName 
                            //      FROM LSE1x1Lf t0, LSE1x1Rt t1 
                            //      WHERE (t0.firstName LIKE ?) AND t0.UNIRIGHT_ID = t1.id(+) 
                            //      FOR UPDATE [params=(String) firstName%1111201]
                            // SELECT t0.version FROM LSE1x1Rt t0 WHERE t0.id = ? [params=(int) 1121201]
                            // SELECT t0.version FROM LSE1x1Lf t0 WHERE t0.id = ? [params=(int) 1111201]
                            assertLockTestSQLs(Select + tableLfName + Any + tableRtName + Where + "\\(\\+\\).*"
                                    + ForUpdate);
                            break;
                        case derby:     //-TODO: **Non-atomic lock.
                            // The database is unable to lock this query.  Each object matching the query will be 
                            //  locked individually after it is loaded; however, it is technically possible that
                            //  another transaction could modify the data before the lock is obtained.
                            // SELECT t0.id, t0.version, t0.firstName, t1.id, t1.version, t1.lastName FROM LSE1x1Lf t0
                            //      LEFT OUTER JOIN LSE1x1Rt t1 ON t0.UNIRIGHT_ID = t1.id
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\') [params=(String) firstName%1111201]
                            // SELECT t0.version FROM LSE1x1Rt t0 WHERE t0.id = ? [params=(int) 1121201]
                            // SELECT t0.id FROM LSE1x1Lf t0 WHERE t0.id = ? FOR UPDATE WITH RR [params=(int) 1111201]
                            // SELECT t0.version FROM LSE1x1Lf t0 WHERE t0.id = ? [params=(int) 1111201]
                            assertLockTestSQLs(Select + joinTables + Where + NoForUpdate,
                                    Select + NoJoin + Any + tableLfName + Any + NoJoin + Where + ForUpdate,
                                    SelectVersion + NoJoin + Any + tableLfName + Any + NoJoin + Where + NoForUpdate,
                                    SelectVersion + NoJoin + Any + tableRtName + Any + NoJoin + Where + NoForUpdate
                                    );
                            //-SELECT t0.id FROM LSE1x1Rt t0 WHERE t0.id = ? FOR UPDATE WITH RR [params=(int) 1121201]-
                            assertLockTestNoSQLs(Select + NoJoin + Any + tableRtName + Any + NoJoin + Where + ForUpdate
                                    );
                            break;
                        default:
                            assertLockTestSQLs(Select + joinTables + Where + ForUpdate);
                        }
                    }

                    public void findNoLockAfterNamedQueryPessimisticWriteDbSql(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.version, t0.firstName, t1.id, t1.version, t1.lastName FROM LSE1x1Lf t0
                            //      LEFT OUTER JOIN LSE1x1Rt t1 ON t0.UNIRIGHT_ID = t1.id WHERE t0.id = ?
                            //      optimize for 1 row [params=(int) 1111202]
                            assertLockTestSQLs(Select + joinTables + Where + NoDB2Lock);
                            break;
                        case oracle:
                            // SELECT t0.version, t0.firstName, t1.id, t1.version, t1.lastName 
                            //      FROM LSE1x1Lf t0, LSE1x1Rt t1 WHERE t0.id = ? AND t0.UNIRIGHT_ID = t1.id(+) 
                            //      [params=(int) 1111202]
                            assertLockTestSQLs(Select + tableLfName + Any + tableRtName + Where + "\\(\\+\\).*"
                                    + NoForUpdate);
                            break;
                        case derby:
                            // SELECT t0.version, t0.firstName, t1.id, t1.version, t1.lastName FROM LSE1x1Lf t0
                            //      LEFT OUTER JOIN LSE1x1Rt t1 ON t0.UNIRIGHT_ID = t1.id WHERE t0.id = ?
                            //      [params=(int) 1111202]
                        default:
                            assertLockTestSQLs(Select + joinTables + Where + NoForUpdate);
                        }
                    }
                });
    }

    public void testNormalUni1x1LazyLock() {
        common1x1LazyLock("testNormalUni1x1LazyLock", 1111101, false);
    }

    public void testExtendedUni1x1LazyLock() {
        common1x1LazyLock("testExtendedUni1x1LazyLock", 1111111, true);
    }

    private void common1x1LazyLock(String testName, int idLf0, boolean extended) {
        final String tableLfName = "LSE1x1LfLzy";
//        final String tableRtName = "LockSEUni1x1RT";
        getLog().info("** " + testName + "()");
        String scope = extended ? "Extended" : "Normal";
        int idRt0 = idLf0 + 10000; // right table
        int idLf1 = idLf0 + 1;
        int idRt1 = idRt0 + 1;

        // create test entity.
        LSE1x1LfLzy eLf0 = new LSE1x1LfLzy();
        LSE1x1Rt eRt0 = new LSE1x1Rt();
        eLf0.setId(idLf0);
        eLf0.setFirstName("firstName " + idLf0);
        eLf0.setUniRight(eRt0);
        eRt0.setId(idRt0);
        eRt0.setLastName("lastName " + idRt0);
        LSE1x1LfLzy eLf1 = new LSE1x1LfLzy();
        LSE1x1Rt eRt1 = new LSE1x1Rt();
        eLf1.setId(idLf1);
        eLf1.setFirstName("firstName " + idLf1);
        eLf1.setUniRight(eRt1);
        eRt1.setId(idRt1);
        eRt1.setLastName("lastName " + idRt1);
       
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            em.getTransaction().begin();
            em.persist(eLf0);
            em.persist(eRt0);
            em.persist(eLf1);
            em.persist(eRt1);
            em.getTransaction().commit();
        } finally {
            em = null;
            eLf0 = eLf1 = null;
            eRt0 = eRt1 = null;
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        commonLockTest(testName, LSE1x1LfLzy.class, idLf0, extended,
                "SELECT c FROM LSE1x1LfLzy c WHERE c.firstName LIKE :firstName", "findLSE1x1LfLzy" + scope,
                new AssertCallback() {
                    public void findNoLockDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.version, t0.firstName FROM LSE1x1LfLzy t0 WHERE t0.id = ?
                            //      optimize for 1 row [params=(int) 1111101]
                            assertLockTestSQLs(Select + tableLfName + Where + NoDB2Lock);
                            break;
                        case oracle:
                            // SELECT t0.version, t0.firstName FROM LSE1x1LfLzy t0 WHERE t0.id = ? 
                            //      [params=(int) 1111101]
                        case derby:
                            // SELECT t0.version, t0.firstName FROM LSE1x1LfLzy t0 WHERE t0.id = ?
                            //      [params=(int) 1111101]
                        default:
                            assertLockTestSQLs(Select + tableLfName + Where + NoForUpdate);
                        }
                    }

                    public void findPessimisticForcIncDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.version, t0.firstName FROM LSE1x1LfLzy t0 WHERE t0.id = ?
                            //      optimize for 1 row FOR READ ONLY WITH RR USE AND KEEP UPDATE LOCKS
                            //      [params=(int) 1111102]
                            // SELECT t0.version FROM LSE1x1LfLzy t0 WHERE t0.id = ?  
                            //      FOR READ ONLY WITH RR USE AND KEEP UPDATE LOCKS [params=(int) 1111102]
                            assertLockTestSQLs(Select + tableLfName + Where + DB2Lock);
                            break;
                        case oracle:
                            // SELECT t0.version, t0.firstName FROM LSE1x1LfLzy t0 WHERE t0.id = ? 
                            //      FOR UPDATE [params=(int) 1111102]
                            // SELECT t0.version FROM LSE1x1LfLzy t0 WHERE t0.id = ? FOR UPDATE [params=(int) 1111102]
                        case derby:
                            // SELECT t0.version, t0.firstName FROM LSE1x1LfLzy t0 WHERE t0.id = ? FOR UPDATE WITH RR
                            //      [params=(int) 1111102]
                            // SELECT t0.version FROM LSE1x1LfLzy t0 WHERE t0.id = ? FOR UPDATE WITH RR
                            //      [params=(int) 1111102]
                        default:
                            assertLockTestSQLs(Select + tableLfName + Where + ForUpdate);
                        }
                    }

                    public void queryPessimisticReadDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.id, t0.version, t0.firstName FROM LSE1x1LfLzy t0 
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\')
                            //      FOR READ ONLY WITH RS USE AND KEEP UPDATE LOCKS [params=(String) firstName%1111101]
                            // SELECT t0.version FROM LSE1x1LfLzy t0 WHERE t0.id = ?  [params=(int) 1111101]
                            assertLockTestSQLs(Select + tableLfName + Where + DB2Lock);
                            break;
                        case oracle:
                            // SELECT t0.id, t0.version, t0.firstName FROM LSE1x1LfLzy t0 WHERE (t0.firstName LIKE ?)
                            //      FOR UPDATE [params=(String) firstName%1111101]
                            // SELECT t0.version FROM LSE1x1LfLzy t0 WHERE t0.id = ? [params=(int) 1111101]
                        case derby:
                            // SELECT t0.id, t0.version, t0.firstName FROM LSE1x1LfLzy t0
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\') FOR UPDATE WITH RR
                            //      [params=(String) firstName%1111101]
                            // SELECT t0.version FROM LSE1x1LfLzy t0 WHERE t0.id = ? [params=(int) 1111101]
                        default:
                            assertLockTestSQLs(Select + tableLfName + Where + ForUpdate);
                        }
                    }

                    public void findNoLockAfterQueryPessimisticReadDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.version, t0.firstName FROM LSE1x1LfLzy t0 WHERE t0.id = ?
                            //      optimize for 1 row [params=(int) 1111102]
                            assertLockTestSQLs(Select + tableLfName + Where + NoDB2Lock);
                            break;
                        case oracle:
                            // SELECT t0.version, t0.firstName FROM LSE1x1LfLzy t0 WHERE t0.id = ? 
                            //      [params=(int) 1111102]
                        case derby:
                            // SELECT t0.version, t0.firstName FROM LSE1x1LfLzy t0 WHERE t0.id = ?
                            //      [params=(int) 1111102]
                        default:
                            assertLockTestSQLs(Select + tableLfName + Where + NoForUpdate);
                        }
                    }

                    public void namedQueryPessimisticWriteDbSql(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.id, t0.version, t0.firstName FROM LSE1x1LfLzy t0 
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\')
                            //      FOR READ ONLY WITH RR USE AND KEEP UPDATE LOCKS [params=(String) firstName%1111101]
                            // SELECT t0.version FROM LSE1x1LfLzy t0 WHERE t0.id = ?  [params=(int) 1111101]
                            assertLockTestSQLs(Select + tableLfName + Where + DB2Lock);
                            break;
                        case oracle:
                            // SELECT t0.id, t0.version, t0.firstName FROM LSE1x1LfLzy t0 WHERE (t0.firstName LIKE ?) 
                            //      FOR UPDATE [params=(String) firstName%1111101]
                            // SELECT t0.version FROM LSE1x1LfLzy t0 WHERE t0.id = ? [params=(int) 1111101]
                        case derby:
                            // SELECT t0.id, t0.version, t0.firstName FROM LSE1x1LfLzy t0
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\') FOR UPDATE WITH RR
                            //      [params=(String) firstName%1111101]
                            // SELECT t0.version FROM LSE1x1LfLzy t0 WHERE t0.id = ? [params=(int) 1111101]
                        default:
                            assertLockTestSQLs(Select + tableLfName + Where + ForUpdate);
                        }
                    }

                    public void findNoLockAfterNamedQueryPessimisticWriteDbSql(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.version, t0.firstName FROM LSE1x1LfLzy t0 WHERE t0.id = ?
                            //      optimize for 1 row [params=(int) 1111102]
                            assertLockTestSQLs(Select + tableLfName + Where + NoDB2Lock);
                            break;
                        case oracle:
                            // SELECT t0.version, t0.firstName FROM LSE1x1LfLzy t0 WHERE t0.id = ? 
                            //      [params=(int) 1111102]
                        case derby:
                            // SELECT t0.version, t0.firstName FROM LSE1x1LfLzy t0 WHERE t0.id = ?
                            //      [params=(int) 1111102]
                        default:
                            assertLockTestSQLs(Select + tableLfName + Where + NoForUpdate);
                        }
                    }
                });
    }

    public void testNormalUni1x1JTLock() {
        common1x1JTLock("testNormalUni1x1JTLock", 1112201, false);
    }

    public void testExtendedUni1x1JTLock() {
        common1x1JTLock("testExtendedUni1x1JTLock", 1112211, true);
    }

    private void common1x1JTLock(String testName, int idLf0, boolean extended) {
        final String tableLfName = "LSE1x1LfJT";
        final String tableJTName = "Uni1x1LfJT_Uni1x1RT";
        final String tableRtName = "LSE1x1Rt";
        final String joinTables  = tableLfName + ".*JOIN.*" + tableJTName + ".*JOIN.*" + tableRtName;
        
        getLog().info("** " + testName + "()");
        String scope = extended ? "Extended" : "Normal";
        int idRt0 = idLf0 + 10000; // right table
        int idLf1 = idLf0 + 1;
        int idRt1 = idRt0 + 1;

        // create test entity.
        LSE1x1LfJT eLf0 = new LSE1x1LfJT();
        LSE1x1Rt eRt0 = new LSE1x1Rt();
        eLf0.setId(idLf0);
        eLf0.setFirstName("firstName " + idLf0);
        eLf0.setUniRightJT(eRt0);
        eRt0.setId(idRt0);
        eRt0.setLastName("lastName " + idRt0);
        LSE1x1LfJT eLf1 = new LSE1x1LfJT();
        LSE1x1Rt eRt1 = new LSE1x1Rt();
        eLf1.setId(idLf1);
        eLf1.setFirstName("firstName " + idLf1);
        eLf1.setUniRightJT(eRt1);
        eRt1.setId(idRt1);
        eRt1.setLastName("lastName " + idRt1);
       
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            em.getTransaction().begin();
            em.persist(eLf0);
            em.persist(eRt0);
            em.persist(eLf1);
            em.persist(eRt1);
            em.getTransaction().commit();
        } finally {
            em = null;
            eLf0 = eLf1 = null;
            eRt0 = eRt1 = null;
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        commonLockTest(testName, LSE1x1LfJT.class, idLf0, extended,
                "SELECT c FROM LSE1x1LfJT c WHERE c.firstName LIKE :firstName", "findLSE1x1LfJT" + scope,
                new AssertCallback() {
                    public void findNoLockDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.version, t0.firstName, t2.id, t2.version, t2.lastName FROM LSE1x1LfJT t0 
                            //      INNER JOIN Uni1x1LfJT_Uni1x1RT t1 ON t0.id = t1.LSE1X1LFJT_ID 
                            //      LEFT OUTER JOIN LSE1x1Rt t2 ON t1.UNIRIGHTJT_ID = t2.id WHERE t0.id = ?
                            //      optimize for 1 row [params=(int) 1112201]
                            assertLockTestSQLs(Select + joinTables + Where + NoDB2Lock);
                            break;
                        case oracle:
                            // SELECT t0.version, t0.firstName, t2.id, t2.version, t2.lastName 
                            //      FROM LSE1x1LfJT t0, Uni1x1LfJT_Uni1x1RT t1, LSE1x1Rt t2 
                            //      WHERE t0.id = ? AND t0.id = t1.LSE1X1LFJT_ID AND t1.UNIRIGHTJT_ID = t2.id(+) 
                            //      [params=(int) 1112201]
                            assertLockTestSQLs(Select + tableLfName + Any + tableJTName + Any + tableRtName + Where
                                    + "\\(\\+\\).*" + NoForUpdate);
                            break;
                        case derby:
                            // SELECT t0.version, t0.firstName, t2.id, t2.version, t2.lastName FROM LSE1x1LfJT t0
                            //      INNER JOIN Uni1x1LfJT_Uni1x1RT t1 ON t0.id = t1.LSE1X1LFJT_ID 
                            //      LEFT OUTER JOIN LSE1x1Rt t2 ON t1.UNIRIGHTJT_ID = t2.id WHERE t0.id = ?
                            //      [params=(int) 1112201]
                        default:
                            assertLockTestSQLs(Select + joinTables + Where + NoForUpdate);
                        }
                    }

                    public void findPessimisticForcIncDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.version, t0.firstName, t2.id, t2.version, t2.lastName FROM LSE1x1LfJT t0 
                            //      INNER JOIN Uni1x1LfJT_Uni1x1RT t1 ON t0.id = t1.LSE1X1LFJT_ID 
                            //      LEFT OUTER JOIN LSE1x1Rt t2 ON t1.UNIRIGHTJT_ID = t2.id WHERE t0.id = ?
                            //      optimize for 1 row FOR READ ONLY WITH RR USE AND KEEP UPDATE LOCKS 
                            //      [params=(int) 1112202]
                            // SELECT t0.version FROM LSE1x1Rt t0 WHERE t0.id = ? 
                            //      FOR READ ONLY WITH RR USE AND KEEP UPDATE LOCKS [params=(int) 1122202]
                            // SELECT t0.version FROM LSE1x1LfJT t0 WHERE t0.id = ?
                            //      FOR READ ONLY WITH RR USE AND KEEP UPDATE LOCKS [params=(int) 1112202]
                            assertLockTestSQLs(Select + joinTables + Where + DB2Lock,
                                    SelectVersion + NoJoin + Any + tableLfName + Any + NoJoin + Where + NoForUpdate,
                                    SelectVersion + NoJoin + Any + tableRtName + Any + NoJoin + Where + NoForUpdate
                                    );
                            assertLockTestNoSQLs(Select + NoJoin + Any + tableRtName + Any + NoJoin + Where + ForUpdate
                                    );
                            break;
                        case oracle:    // TODO: If jpa2, DO NOT lock LSE1x1RT using "FOR UDPATE OF col"
                            // SELECT t0.version, t0.firstName, t2.id, t2.version, t2.lastName 
                            //      FROM LSE1x1LfJT t0, Uni1x1LfJT_Uni1x1RT t1, LSE1x1Rt t2
                            //      WHERE t0.id = ? AND t0.id = t1.LSE1X1LFJT_ID AND t1.UNIRIGHTJT_ID = t2.id(+) 
                            //      FOR UPDATE [params=(int) 1112202]
                            // SELECT t0.version FROM LSE1x1Rt t0 WHERE t0.id = ? FOR UPDATE [params=(int) 1122202]
                            // SELECT t0.version FROM LSE1x1LfJT t0 WHERE t0.id = ? FOR UPDATE [params=(int) 1112202]
                            assertLockTestSQLs(Select + tableLfName + Any + tableJTName + Any + tableRtName + Where
                                    + "\\(\\+\\).*" + ForUpdate);
                            break;
                        case derby:     //-TODO: **Non-atomic lock, if jpa2/extended scope, LOCK Uni1x1LfJT_Uni1x1RT
                                        // DO NOT lock LSE1x1Rt
                            // The database is unable to lock this query.  Each object matching the query will be 
                            //  locked individually after it is loaded; however, it is technically possible that
                            //  another transaction could modify the data before the lock is obtained.
                            // SELECT t0.version, t0.firstName, t2.id, t2.version, t2.lastName FROM LSE1x1LfJT t0
                            //      INNER JOIN Uni1x1LfJT_Uni1x1RT t1 ON t0.id = t1.LSE1X1LFJT_ID
                            //      LEFT OUTER JOIN LSE1x1Rt t2 ON t1.UNIRIGHTJT_ID = t2.id WHERE t0.id = ?
                            //      [params=(int) 1112202]
                            // SELECT t0.version FROM LSE1x1Rt t0 WHERE t0.id = ? -FOR UPDATE WITH RR- 
                            //      [params=(int) 1122202]
                            // SELECT t0.id FROM LSE1x1LfJT t0 WHERE t0.id = ? FOR UPDATE WITH RR [params=(int) 1112202]
                            // SELECT t0.version FROM LSE1x1LfJT t0 WHERE t0.id = ? -FOR UPDATE WITH RR-
                            //      [params=(int) 1112202]
                            assertLockTestSQLs(Select + joinTables + Where + NoForUpdate,
                                    Select + NoJoin + Any + tableLfName + Any + NoJoin + Where + ForUpdate,
                                    SelectVersion + NoJoin + Any + tableLfName + Any + NoJoin + Where + NoForUpdate,
                                    SelectVersion + NoJoin + Any + tableRtName + Any + NoJoin + Where + NoForUpdate
                                    );
                            //-SELECT t0.id FROM LSE1x1Rt t0 WHERE t0.id = ? FOR UPDATE WITH RR [params=(int) 1122202]-
                            assertLockTestNoSQLs(Select + NoJoin + Any + tableRtName + Any + NoJoin + Where + ForUpdate
                                    );
                            break;
                        default:
                            assertLockTestSQLs(Select + joinTables + Where + ForUpdate);
                        }
                    }

                    public void queryPessimisticReadDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.id, t0.version, t0.firstName, t2.id, t2.version, t2.lastName FROM LSE1x1LfJT t0
                            //      INNER JOIN Uni1x1LfJT_Uni1x1RT t1 ON t0.id = t1.LSE1X1LFJT_ID 
                            //      LEFT OUTER JOIN LSE1x1Rt t2 ON t1.UNIRIGHTJT_ID = t2.id 
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\')
                            //      FOR READ ONLY WITH RS USE AND KEEP UPDATE LOCKS [params=(String) firstName%1112201]
                            // SELECT t0.version FROM LSE1x1Rt t0 WHERE t0.id = ?  [params=(int) 1122201]
                            // SELECT t0.version FROM LSE1x1LfJT t0 WHERE t0.id = ?  [params=(int) 1112201]
                            assertLockTestSQLs(Select + joinTables + Where + DB2Lock,
                                    SelectVersion + NoJoin + Any + tableLfName + Any + NoJoin + Where + NoForUpdate,
                                    SelectVersion + NoJoin + Any + tableRtName + Any + NoJoin + Where + NoForUpdate
                                    );
                            assertLockTestNoSQLs(Select + NoJoin + Any + tableRtName + Any + NoJoin + Where + ForUpdate
                                    );
                            break;
                        case oracle:    // TODO: If jpa2, DO NOT lock LSE1x1RT using "FOR UDPATE OF col"
                            // SELECT t0.id, t0.version, t0.firstName, t2.id, t2.version, t2.lastName 
                            //      FROM LSE1x1LfJT t0, Uni1x1LfJT_Uni1x1RT t1, LSE1x1Rt t2 
                            //      WHERE (t0.firstName LIKE ?) AND t0.id = t1.LSE1X1LFJT_ID 
                            //      AND t1.UNIRIGHTJT_ID = t2.id(+) FOR UPDATE [params=(String) firstName%1112201]
                            // SELECT t0.version FROM LSE1x1Rt t0 WHERE t0.id = ? [params=(int) 1122201]
                            // SELECT t0.version FROM LSE1x1LfJT t0 WHERE t0.id = ? [params=(int) 1112201]
                            assertLockTestSQLs(Select + tableLfName + Any + tableJTName + Any + tableRtName + Where
                                    + "\\(\\+\\).*" + ForUpdate);
                            break;
                        case derby:     //-TODO: **Non-atomic lock, if jpa2/extended scope, LOCK Uni1x1LfJT_Uni1x1RT
                                        // DO NOT lock LSE1x1Rt
                            // The database is unable to lock this query.  Each object matching the query will be 
                            //  locked individually after it is loaded; however, it is technically possible that
                            //  another transaction could modify the data before the lock is obtained.
                            // SELECT t0.id, t0.version, t0.firstName, t2.id, t2.version, t2.lastName FROM LSE1x1LfJT t0
                            //      INNER JOIN Uni1x1LfJT_Uni1x1RT t1 ON t0.id = t1.LSE1X1LFJT_ID 
                            //      LEFT OUTER JOIN LSE1x1Rt t2 ON t1.UNIRIGHTJT_ID = t2.id 
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\') [params=(String) firstName%1112201]
                            // SELECT t0.version FROM LSE1x1Rt t0 WHERE t0.id = ? [params=(int) 1122201]
                            // SELECT t0.id FROM LSE1x1LfJT t0 WHERE t0.id = ? FOR UPDATE WITH RR [params=(int) 1112201]
                            // SELECT t0.version FROM LSE1x1LfJT t0 WHERE t0.id = ? [params=(int) 1112201]
                            assertLockTestSQLs(Select + joinTables + Where + NoForUpdate,
                                    Select + NoJoin + Any + tableLfName + Any + NoJoin + Where + ForUpdate,
                                    SelectVersion + NoJoin + Any + tableLfName + Any + NoJoin + Where + NoForUpdate,
                                    SelectVersion + NoJoin + Any + tableRtName + Any + NoJoin + Where + NoForUpdate
                                    );
                            //-SELECT t0.id FROM LSE1x1Rt t0 WHERE t0.id = ? FOR UPDATE WITH RR [params=(int) 1122201]-
                            assertLockTestNoSQLs(Select + NoJoin + Any + tableRtName + Any + NoJoin + Where + ForUpdate
                                    );
                            break;
                        default:
                            assertLockTestSQLs(Select + joinTables + Where + ForUpdate);
                        }
                    }

                    public void findNoLockAfterQueryPessimisticReadDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.version, t0.firstName, t2.id, t2.version, t2.lastName FROM LSE1x1LfJT t0 
                            //      INNER JOIN Uni1x1LfJT_Uni1x1RT t1 ON t0.id = t1.LSE1X1LFJT_ID 
                            //      LEFT OUTER JOIN LSE1x1Rt t2 ON t1.UNIRIGHTJT_ID = t2.id WHERE t0.id = ?
                            //      optimize for 1 row [params=(int) 1112202]
                            assertLockTestSQLs(Select + joinTables + Where + NoDB2Lock);
                            break;
                        case oracle:
                            // SELECT t0.version, t0.firstName, t2.id, t2.version, t2.lastName 
                            //      FROM LSE1x1LfJT t0, Uni1x1LfJT_Uni1x1RT t1, LSE1x1Rt t2 
                            //      WHERE t0.id = ? AND t0.id = t1.LSE1X1LFJT_ID AND t1.UNIRIGHTJT_ID = t2.id(+) 
                            //      [params=(int) 1112202]
                            assertLockTestSQLs(Select + tableLfName + Any + tableJTName + Any + tableRtName + Where
                                    + "\\(\\+\\).*" + NoForUpdate);
                            break;
                        case derby:
                            // SELECT t0.version, t0.firstName, t2.id, t2.version, t2.lastName FROM LSE1x1LfJT t0
                            //      INNER JOIN Uni1x1LfJT_Uni1x1RT t1 ON t0.id = t1.LSE1X1LFJT_ID
                            //      LEFT OUTER JOIN LSE1x1Rt t2 ON t1.UNIRIGHTJT_ID = t2.id WHERE t0.id = ?
                            //      [params=(int) 1112202]
                        default:
                            assertLockTestSQLs(Select + joinTables + Where + NoForUpdate);
                        }
                    }

                    public void namedQueryPessimisticWriteDbSql(EntityManager em) {
                        switch (getDBType(em)) {
                        case db2:
                            //SELECT t0.id, t0.version, t0.firstName, t2.id, t2.version, t2.lastName FROM LSE1x1LfJT t0
                            //      INNER JOIN Uni1x1LfJT_Uni1x1RT t1 ON t0.id = t1.LSE1X1LFJT_ID
                            //      LEFT OUTER JOIN LSE1x1Rt t2 ON t1.UNIRIGHTJT_ID = t2.id
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\')
                            //      FOR READ ONLY WITH RR USE AND KEEP UPDATE LOCKS [params=(String) firstName%1112201]
                            // SELECT t0.version FROM LSE1x1Rt t0 WHERE t0.id = ?  [params=(int) 1122201]
                            // SELECT t0.version FROM LSE1x1LfJT t0 WHERE t0.id = ?  [params=(int) 1112201]
                            assertLockTestSQLs(Select + joinTables + Where + DB2Lock,
                                    SelectVersion + NoJoin + Any + tableLfName + Any + NoJoin + Where + NoForUpdate,
                                    SelectVersion + NoJoin + Any + tableRtName + Any + NoJoin + Where + NoForUpdate
                                    );
                            assertLockTestNoSQLs(Select + NoJoin + Any + tableRtName + Any + NoJoin + Where + ForUpdate
                                    );
                            break;
                        case oracle:    // TODO: If jpa2, DO NOT lock LSE1x1RT using "FOR UDPATE OF col"
                            // SELECT t0.id, t0.version, t0.firstName, t2.id, t2.version, t2.lastName 
                            //      FROM LSE1x1LfJT t0, Uni1x1LfJT_Uni1x1RT t1, LSE1x1Rt t2 
                            //      WHERE (t0.firstName LIKE ?) AND t0.id = t1.LSE1X1LFJT_ID 
                            //      AND t1.UNIRIGHTJT_ID = t2.id(+) FOR UPDATE [params=(String) firstName%1112201]
                            // SELECT t0.version FROM LSE1x1Rt t0 WHERE t0.id = ? [params=(int) 1122201]
                            // SELECT t0.version FROM LSE1x1LfJT t0 WHERE t0.id = ? [params=(int) 1112201]
                            assertLockTestSQLs(Select + tableLfName + Any + tableJTName + Any + tableRtName + Where
                                    + "\\(\\+\\).*" + ForUpdate);
                            break;
                        case derby:     //-TODO: **Non-atomic lock, if jpa2/extended scope, LOCK Uni1x1LfJT_Uni1x1RT
                                        // DO NOT lock LSE1x1Rt
                            // The database is unable to lock this query.  Each object matching the query will be 
                            //  locked individually after it is loaded; however, it is technically possible that
                            //  another transaction could modify the data before the lock is obtained.
                            // SELECT t0.id, t0.version, t0.firstName, t2.id, t2.version, t2.lastName FROM LSE1x1LfJT t0
                            //      INNER JOIN Uni1x1LfJT_Uni1x1RT t1 ON t0.id = t1.LSE1X1LFJT_ID
                            //      LEFT OUTER JOIN LSE1x1Rt t2 ON t1.UNIRIGHTJT_ID = t2.id
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\') [params=(String) firstName%1112201]
                            // SELECT t0.version FROM LSE1x1Rt t0 WHERE t0.id = ? [params=(int) 1122201]
                            // SELECT t0.id FROM LSE1x1LfJT t0 WHERE t0.id = ? FOR UPDATE WITH RR [params=(int) 1112201]
                            // SELECT t0.version FROM LSE1x1LfJT t0 WHERE t0.id = ? [params=(int) 1112201]
                            assertLockTestSQLs(Select + joinTables + Where + NoForUpdate,
                                    Select + NoJoin + Any + tableLfName + Any + NoJoin + Where + ForUpdate,
                                    SelectVersion + NoJoin + Any + tableLfName + Any + NoJoin + Where + NoForUpdate,
                                    SelectVersion + NoJoin + Any + tableRtName + Any + NoJoin + Where + NoForUpdate
                                    );
                            //-SELECT t0.id FROM LSE1x1Rt t0 WHERE t0.id = ? FOR UPDATE WITH RR [params=(int) 1122201]
                            assertLockTestNoSQLs(Select + NoJoin + Any + tableRtName + Any + NoJoin + Where + ForUpdate
                                    );
                            break;
                        default:
                            assertLockTestSQLs(Select + joinTables + Where + ForUpdate);
                        }
                    }

                    public void findNoLockAfterNamedQueryPessimisticWriteDbSql(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.version, t0.firstName, t2.id, t2.version, t2.lastName FROM LSE1x1LfJT t0
                            //      INNER JOIN Uni1x1LfJT_Uni1x1RT t1 ON t0.id = t1.LSE1X1LFJT_ID 
                            //      LEFT OUTER JOIN LSE1x1Rt t2 ON t1.UNIRIGHTJT_ID = t2.id WHERE t0.id = ?
                            //      optimize for 1 row [params=(int) 1112202]
                            assertLockTestSQLs(Select + joinTables + Where + NoDB2Lock);
                            break;
                        case oracle:
                            // SELECT t0.version, t0.firstName, t2.id, t2.version, t2.lastName 
                            //      FROM LSE1x1LfJT t0, Uni1x1LfJT_Uni1x1RT t1, LSE1x1Rt t2 
                            //      WHERE t0.id = ? AND t0.id = t1.LSE1X1LFJT_ID AND t1.UNIRIGHTJT_ID = t2.id(+) 
                            //      [params=(int) 1112202]
                            assertLockTestSQLs(Select + tableLfName + Any + tableJTName + Any + tableRtName + Where
                                    + "\\(\\+\\).*" + NoForUpdate);
                            break;
                        case derby:
                            // SELECT t0.version, t0.firstName, t2.id, t2.version, t2.lastName FROM LSE1x1LfJT t0
                            //      INNER JOIN Uni1x1LfJT_Uni1x1RT t1 ON t0.id = t1.LSE1X1LFJT_ID
                            //      LEFT OUTER JOIN LSE1x1Rt t2 ON t1.UNIRIGHTJT_ID = t2.id 
                            //      WHERE t0.id = ? [params=(int) 1112202]
                        default:
                            assertLockTestSQLs(Select + joinTables + Where + NoForUpdate);
                        }
                    }
                });
    }

    public void testNormalUni1x1JTLazyLock() {
        common1x1JTLazyLock("testNormalUni1x1JTLazyLock", 1112101, false);
    }

    public void testExtendedUni1x1JTLazyLock() {
        common1x1JTLazyLock("testExtendedUni1x1JTLazyLock", 1112111, true);
    }

    private void common1x1JTLazyLock(String testName, int idLf0, boolean extended) {
        final String tableLfName = "LSE1x1LfJTLzy";
//        final String tableRtName = "LockSEUni1x1RT";
        getLog().info("** " + testName + "()");
        String scope = extended ? "Extended" : "Normal";
        int idRt0 = idLf0 + 10000; // right table
        int idLf1 = idLf0 + 1;
        int idRt1 = idRt0 + 1;

        // create test entity.
        LSE1x1LfJTLzy eLf0 = new LSE1x1LfJTLzy();
        LSE1x1Rt eRt0 = new LSE1x1Rt();
        eLf0.setId(idLf0);
        eLf0.setFirstName("firstName " + idLf0);
        eLf0.setUniRightJT(eRt0);
        eRt0.setId(idRt0);
        eRt0.setLastName("lastName " + idRt0);
        LSE1x1LfJTLzy eLf1 = new LSE1x1LfJTLzy();
        LSE1x1Rt eRt1 = new LSE1x1Rt();
        eLf1.setId(idLf1);
        eLf1.setFirstName("firstName " + idLf1);
        eLf1.setUniRightJT(eRt1);
        eRt1.setId(idRt1);
        eRt1.setLastName("lasttName " + idRt1);
       
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            em.getTransaction().begin();
            em.persist(eLf0);
            em.persist(eRt0);
            em.persist(eLf1);
            em.persist(eRt1);
            em.getTransaction().commit();
        } finally {
            em = null;
            eLf0 = eLf1 = null;
            eRt0 = eRt1 = null;
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        commonLockTest(testName, LSE1x1LfJTLzy.class, idLf0, extended,
                "SELECT c FROM LSE1x1LfJTLzy c WHERE c.firstName LIKE :firstName", "findLSE1x1LfJTLzy" + scope,
                new AssertCallback() {
                    public void findNoLockDbSQL(EntityManager em) {
                        switch (getDBType(em)) {
                        case db2:
                            // SELECT t0.version, t0.firstName FROM LSE1x1LfJTLzy t0 WHERE t0.id = ?
                            //      optimize for 1 row [params=(int) 1112101]
                            assertLockTestSQLs(Select + tableLfName + Where + NoDB2Lock);
                            break;
                        case oracle:
                            // SELECT t0.version, t0.firstName FROM LSE1x1LfJTLzy t0 WHERE t0.id = ? 
                            //  [params=(int) 1112101]
                        case derby:
                            // SELECT t0.version, t0.firstName FROM LSE1x1LfJTLzy t0 WHERE t0.id = ?
                            //      [params=(int) 1112101]
                        default:
                            assertLockTestSQLs(Select + tableLfName + Where + NoForUpdate);
                        }
                    }

                    public void findPessimisticForcIncDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.version, t0.firstName FROM LSE1x1LfJTLzy t0 WHERE t0.id = ? 
                            //      optimize for 1 row FOR READ ONLY WITH RR USE AND KEEP UPDATE LOCKS
                            //      [params=(int) 1112102]
                            // SELECT t0.version FROM LSE1x1LfJTLzy t0 WHERE t0.id = ?  
                            //      FOR READ ONLY WITH RR USE AND KEEP UPDATE LOCKS [params=(int) 1112102]
                            assertLockTestSQLs(Select + tableLfName + Where + DB2Lock);
                            break;
                        case derby:
                            // SELECT t0.version, t0.firstName FROM LSE1x1LfJTLzy t0 WHERE t0.id = ? FOR UPDATE WITH RR
                            //      [params=(int) 1112102]
                            // SELECT t0.version FROM LSE1x1LfJTLzy t0 WHERE t0.id = ? FOR UPDATE WITH RR
                            //      [params=(int) 1112102]
                        case oracle:
                            // SELECT t0.version, t0.firstName FROM LSE1x1LfJTLzy t0 WHERE t0.id = ? FOR UPDATE 
                            //      [params=(int) 1112102]
                            // SELECT t0.version FROM LSE1x1LfJTLzy t0 WHERE t0.id = ? FOR UPDATE [params=(int) 1112102]
                        default:
                            assertLockTestSQLs(Select + tableLfName + Where + ForUpdate);
                        }
                    }

                    public void queryPessimisticReadDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.id, t0.version, t0.firstName FROM LSE1x1LfJTLzy t0 
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\')
                            //      FOR READ ONLY WITH RS USE AND KEEP UPDATE LOCKS [params=(String) firstName%1112101]
                            // SELECT t0.version FROM LSE1x1LfJTLzy t0 WHERE t0.id = ?  [params=(int) 1112101]
                            assertLockTestSQLs(Select + tableLfName + Where + DB2Lock);
                            break;
                        case derby:
                            // SELECT t0.id, t0.version, t0.firstName FROM LSE1x1LfJTLzy t0
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\') FOR UPDATE WITH RR
                            //      [params=(String) firstName%1112101]
                            // SELECT t0.version FROM LSE1x1LfJTLzy t0 WHERE t0.id = ? [params=(int) 1112101]
                        case oracle:
                            // SELECT t0.id, t0.version, t0.firstName FROM LSE1x1LfJTLzy t0 
                            //      WHERE (t0.firstName LIKE ?) FOR UPDATE [params=(String) firstName%1112101]
                            // SELECT t0.version FROM LSE1x1LfJTLzy t0 WHERE t0.id = ? [params=(int) 1112101]
                        default:
                            assertLockTestSQLs(Select + tableLfName + Where + ForUpdate);
                        }
                    }

                    public void findNoLockAfterQueryPessimisticReadDbSQL(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.version, t0.firstName FROM LSE1x1LfJTLzy t0 WHERE t0.id = ?
                            //      optimize for 1 row [params=(int) 1112102]
                            assertLockTestSQLs(Select + tableLfName + Where + NoDB2Lock);
                            break;
                        case derby:
                            // SELECT t0.version, t0.firstName FROM LSE1x1LfJTLzy t0 WHERE t0.id = ?
                            //      [params=(int) 1112102]
                        case oracle:
                            // SELECT t0.version, t0.firstName FROM LSE1x1LfJTLzy t0 WHERE t0.id = ? 
                            //      [params=(int) 1112102]
                        default:
                            assertLockTestSQLs(Select + tableLfName + Where + NoForUpdate);
                        }
                    }

                    public void namedQueryPessimisticWriteDbSql(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.id, t0.version, t0.firstName FROM LSE1x1LfJTLzy t0
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\')  
                            //      FOR READ ONLY WITH RR USE AND KEEP UPDATE LOCKS [params=(String) firstName%1112101]
                            // SELECT t0.version FROM LSE1x1LfJTLzy t0 WHERE t0.id = ?  [params=(int) 1112101]
                            assertLockTestSQLs(Select + tableLfName + Where + DB2Lock);
                            break;
                        case derby:
                            // SELECT t0.id, t0.version, t0.firstName FROM LSE1x1LfJTLzy t0
                            //      WHERE (t0.firstName LIKE ? ESCAPE '\') FOR UPDATE WITH RR
                            //      [params=(String) firstName%1112101]
                            // SELECT t0.version FROM LSE1x1LfJTLzy t0 WHERE t0.id = ? [params=(int) 1112101]
                        case oracle:
                            // SELECT t0.id, t0.version, t0.firstName FROM LSE1x1LfJTLzy t0 
                            //      WHERE (t0.firstName LIKE ?) FOR UPDATE [params=(String) firstName%1112101]
                            // SELECT t0.version FROM LSE1x1LfJTLzy t0 WHERE t0.id = ? [params=(int) 1112101]
                        default:
                            assertLockTestSQLs(Select + tableLfName + Where + ForUpdate);
                        }
                    }

                    public void findNoLockAfterNamedQueryPessimisticWriteDbSql(EntityManager em) {
                        switch (getDBType(em)) {    // **Check
                        case db2:
                            // SELECT t0.version, t0.firstName FROM LSE1x1LfJTLzy t0 WHERE t0.id = ?
                            //      optimize for 1 row [params=(int) 1112102]
                            assertLockTestSQLs(Select + tableLfName + Where + NoDB2Lock);
                            break;
                        case derby:
                            // SELECT t0.version, t0.firstName FROM LSE1x1LfJTLzy t0 WHERE t0.id = ?
                            //      [params=(int) 1112102]
                        case oracle:
                            // SELECT t0.version, t0.firstName FROM LSE1x1LfJTLzy t0 WHERE t0.id = ?
                            //      [params=(int) 1112102]
                        default:
                            assertLockTestSQLs(Select + tableLfName + Where + NoForUpdate);
                        }
                    }
                });
    }
}

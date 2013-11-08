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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PessimisticLockScope;
import javax.persistence.Query;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.conf.JDBCConfigurationImpl;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.persistence.MixedLockLevelsHelper;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.test.SQLListenerTestCase;

/**
 * Base class for locking extended scope tests.
 *
 * Test JPA 2.0 EM interface normal lock scope behaviors with "mixed" lock
 * manager. When an entity instance is locked using pessimistic locking, the
 * persistence provider must lock the database row(s) that correspond to the
 * non-collection-valued persistent state of that instance. If a joined
 * inheritance strategy is used, or if the entity is otherwise mapped to a
 * secondary table, this entails locking the row(s) for the entity instance in
 * the additional table(s). Entity relationships for which the locked entity
 * contains the foreign key will also be locked, but not the state of the
 * referenced entities (unless hose entities are explicitly locked). Element
 * collections and relationships for which the entity does not contain the
 * foreign key (such as relationships that are mapped to join tables or
 * unidirectional one-to-many relationships for which the target entity contains
 * the foreign key) will not be locked by default.
 *
 * Element collections and relationships owned by the entity that are contained
 * in join tables will be locked if the javax.persistence.lock.scope property is
 * specified with a value of PessimisticLockScope.EXTENDED. The state of
 * entities referenced by such relationships will not be locked (unless those
 * entities are explicitly locked). This property may be passed as an argument
 * to the methods of the EntityManager and Query interfaces that allow lock
 * modes to be specified or used with the NamedQuery annotation.
 * 
 * @since 2.0
 */
public abstract class LockScopeTestCase extends SQLListenerTestCase {
    
    protected final String Any              = ".*";
    protected final String Select           = "SELECT.*FROM.*";
    protected final String SelectVersion    = "SELECT.*version.*FROM.*";
    protected final String Where            = ".*WHERE.*";
//    protected final String Join             = ".*(JOIN){1}.*";
    protected final String NoJoin           = "(JOIN){0}";
    protected final String ForUpdateRex     = "FOR UPDATE.*";
    protected final String ForUpdateClause  = "(" + ForUpdateRex + ")";
    protected final String ForUpdate        = ForUpdateClause + "{1}";
    protected final String NoForUpdate      = ForUpdateClause + "{0}";
    protected final String DB2LockClause    = "(" + ForUpdateRex +
                                              "|FOR READ ONLY WITH R. USE AND KEEP (UPDATE|EXCLUSIVE) LOCKS)";
    protected final String DB2Lock          = DB2LockClause + "{1}"; 
    protected final String NoDB2Lock        = DB2LockClause + "{0}"; 

    protected List<String> empTableName = new ArrayList<String>();;

    protected Map<String, Object> normalProps;
    protected Map<String, Object> extendedProps;

    @Override
    protected String getPersistenceUnitName() {
        return "locking-test";
    }
    
    protected void commonSetUp(Class<?>... eClasses ) {
        normalProps = new HashMap<String, Object>();
        extendedProps = new HashMap<String, Object>();
        extendedProps.put("javax.persistence.lock.scope", PessimisticLockScope.EXTENDED);

        for( Class<?> eClazz : eClasses) {
            empTableName.add(getMapping(eClazz).getTable().getFullName());
        }
        cleanupDB();
    }

    private void cleanupDB() {
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            em.getTransaction().begin();
            for (String tableName : empTableName.toArray(new String[empTableName.size()])) {
                em.createQuery("delete from " + tableName).executeUpdate();
            }
            em.getTransaction().commit();
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    protected enum DBType {
        access, db2, derby, empress, foxpro, h2, hsql, informix, ingres, jdatastore, mariadb, mysql, oracle, pointbase,
        postgres, sqlserver, sybase
    };

    protected DBType getDBType(EntityManager em) {
        JDBCConfigurationImpl conf = (JDBCConfigurationImpl) getConfiguration(em);
        String dictClassName = getConfiguration(em).getDBDictionaryInstance().getClass().getName();
        String db = conf.dbdictionaryPlugin.alias(dictClassName);
        return DBType.valueOf(db);
    }

    @SuppressWarnings( { "unused", "deprecation" })
    protected JDBCConfiguration getConfiguration(EntityManager em) {
        return ((JDBCConfiguration) ((OpenJPAEntityManager) em).getConfiguration());
    }

    protected Log getLog() {
        return emf.getConfiguration().getLog("Tests");
    }

    protected Log getDumpStackLog() {
        return emf.getConfiguration().getLog("DumpStack");
    }

    /*
     * Set Log=LockTestSQL=TRACE to dump the SQL caught by the SQL listener but do not perform SQL assertion.
     */
    protected Log getDumpSQLLog() {
        return emf.getConfiguration().getLog("LockTestSQL");
    }

    public void assertLockTestSQLs(String... expected) {
        Log log = getDumpSQLLog(); 
        if( log.isTraceEnabled()) {
            log.trace("\r\n" + toString(sql));
            return;
        }
        assertAllSQLAnyOrder(expected);
    }
    
    public void assertLockTestNoSQLs(String... expected) {
        Log log = getDumpSQLLog(); 
        if( log.isTraceEnabled()) {
            log.trace("\r\n" + toString(sql));
            return;
        }
        assertNoneSQLAnyOrder(expected);
    }

    protected void logStack(Throwable t) {
        StringWriter str = new StringWriter();
        PrintWriter print = new PrintWriter(str);
        t.printStackTrace(print);
        getDumpStackLog().trace(str.toString());
    }

    // Id designation-
    // for basic test:
    //      [basic=0,sectable=1,singletable=2,join=4,eleColl=5,eleCollEager=6][normal=0|extended=1][entity#]
    // For 1x1/1xm tests:
    //      [1x1=1,1xM=2] [uni=1|bi=2] [left=1|right=2] [normal=1|join=2] [default=0|lazy=1|eager=2] 
    //          [normal=0|extended=1] [n-th entity]
    protected <T> void commonLockTest(String testName, Class<T> type, int id0, boolean extended, String queryString,
            String namedQueryString, AssertCallback verify) {
        getLog().info("** " + testName + "()");
        String entityName = type.getName();
        String scope = extended ? "Extended" : "Normal";
        Map<String, Object> props = extended ? extendedProps : normalProps;
        int id1 = id0 + 1;

        EntityManager em = null;
        T e0 = null;
        T e1 = null;
        try {
            getLog().info("-- Test find with no lock in " + scope + " scope");
            em = emf.createEntityManager();
            getLog().info(" *Begin a transaction.");
            em.getTransaction().begin();
            resetSQL();
            getLog().info(" *Find " + entityName + "(" + id0 + ") with no lock");
            e0 = em.find(type, id0, props);
            getLog().info(" *" + (e0 != null ? "F" : "Can not f") + "ind entity");
            verify.findNoLockDbSQL(em);
            getLog().info(" *Found entity:" + e0);
            assertNotNull(" *Found " + entityName + "(" + id0 + ")", e0);
            assertEquals(" *Assert no lock applied", LockModeType.NONE, em.getLockMode(e0));

            getLog().info(" *Find " + entityName + "(" + id1 + ") with pessimistic force increment lock");
            resetSQL();
            e1 = em.find(type, id1, LockModeType.PESSIMISTIC_FORCE_INCREMENT, props);
            getLog().info(" *" + (e1 != null ? "F" : "Can not f") + "ind entity");
            verify.findPessimisticForcIncDbSQL(em);
            getLog().info(" *Found entity:" + e1);
            assertNotNull(" *Found " + entityName + "(" + id1 + ")", e1);
            assertEquals(" *Assert pessimistic force increment lock applied", LockModeType.PESSIMISTIC_FORCE_INCREMENT,
                    em.getLockMode(e1));

            getLog().info("Committing transaction.");
            em.getTransaction().commit();
        } finally {
            em = null;
            e0 = e1 = null;
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        try {
            getLog().info("-- Test query with pessimistic read lock in " + scope + " scope");
            em = emf.createEntityManager();
            getLog().info(" *Begin a transaction.");
            em.getTransaction().begin();
            resetSQL();
            int beforeReadLevel = getConfiguration(em).getReadLockLevelConstant();
            LockModeType beforeReadMode = MixedLockLevelsHelper.fromLockLevel(beforeReadLevel);
            getLog().info(" *Save ReadLockLevel before Query:" + beforeReadMode);
            getLog().info(" *Query " + entityName + "(" + id0 + ") with PESSIMISTIC_READ lock");
            Query q = em.createQuery(queryString);
            if (extended) {
                q = q.setHint("javax.persistence.lock.scope", PessimisticLockScope.EXTENDED);
            }
            q = q.setLockMode(LockModeType.PESSIMISTIC_READ);
            q = q.setParameter("firstName", "firstName%" + id0);
            List<T> es = q.getResultList();
            getLog().info(" *Found " + es.size() + " entity");
            assertEquals(" *Should find 1 entity", es.size(), 1);
            verify.queryPessimisticReadDbSQL(em);
            e0 = es.get(0);
            getLog().info(" *Found entity:" + e0);
            assertNotNull(" *Found " + entityName + "(" + id0 + ")", e0);
            assertEquals("Assert pessimistic read lock applied", LockModeType.PESSIMISTIC_READ, em.getLockMode(e0));
            assertEquals(" *Read lock should still be " + beforeReadMode + "after query set lock mode",
                    beforeReadLevel, getConfiguration(em).getReadLockLevelConstant());

            getLog().info(
                    " *Find " + entityName + "(" + id1
                            + ") with no lock to verify query lock set does not affect em lock mode.");
            resetSQL();
            e1 = em.find(type, id1);
            getLog().info(" *" + (e1 != null ? "F" : "Can not f") + "ind entity");
            verify.findNoLockAfterQueryPessimisticReadDbSQL(em);
            getLog().info(" *Found entity:" + e1);
            assertNotNull(" *Found " + entityName + "(" + id1 + ")", e1);
            assertEquals(" *Assert default lock applied", LockModeType.NONE, em.getLockMode(e1));

            getLog().info("Committing transaction.");
            em.getTransaction().commit();
        } finally {
            em = null;
            e0 = e1 = null;
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        try {
            getLog().info("-- Test name query with pessimistic write lock in " + scope + " scope");
            em = emf.createEntityManager();
            getLog().info(" *Begin a transaction.");
            em.getTransaction().begin();
            resetSQL();
            int beforeReadLevel = getConfiguration(em).getReadLockLevelConstant();
            LockModeType beforeReadMode = MixedLockLevelsHelper.fromLockLevel(beforeReadLevel);
            getLog().info(" *Save ReadLockLevel before Query:" + beforeReadMode);
            getLog().info(" *Query " + entityName + "(" + id0 + ") with PESSIMISTIC_WRITE lock");
            Query q = em.createNamedQuery(namedQueryString);
            if (extended) {
                q = q.setHint("javax.persistence.lock.scope", PessimisticLockScope.EXTENDED);
            }
            q = q.setParameter("firstName", "firstName%" + id0);
            List<T> es = q.getResultList();
            getLog().info(" *Found " + es.size() + " entity");
            assertEquals(" *Found 1 entity", es.size(), 1);
            verify.namedQueryPessimisticWriteDbSql(em);
            e0 = es.get(0);
            getLog().info(" *Found entity:" + e0);
            assertNotNull(" *Found " + entityName + "(" + id0 + ")", e0);
            assertEquals("Assert pessimistic write lock applied", LockModeType.PESSIMISTIC_WRITE, em.getLockMode(e0));

            getLog().info(" *Ensure ReadLockLevel remains at level " + beforeReadMode);
            assertEquals(" *Read lock should still be " + beforeReadMode + "after query set lock mode",
                    beforeReadLevel, getConfiguration(em).getReadLockLevelConstant());

            getLog().info(
                    " *Find " + entityName + "(" + id1
                            + ") with no lock to verify query lock set does not affect em lock mode.");
            resetSQL();
            e1 = em.find(type, id1);
            getLog().info(" *" + (e1 != null ? "F" : "Can not f") + "ind an entity");
            verify.findNoLockAfterNamedQueryPessimisticWriteDbSql(em);
            getLog().info(" *Found entity:" + e1);
            assertNotNull(" *Found " + entityName + "(" + id1 + ")", e1);
            assertEquals(" *Assert default lock applied", LockModeType.NONE, em.getLockMode(e1));

            getLog().info("Committing transaction.");
            em.getTransaction().commit();
        } finally {
            em = null;
            e0 = e1 = null;
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    protected interface AssertCallback {
        public void findNoLockDbSQL(EntityManager em);

        public void findPessimisticForcIncDbSQL(EntityManager em);

        public void queryPessimisticReadDbSQL(EntityManager em);

        public void findNoLockAfterQueryPessimisticReadDbSQL(EntityManager em);

        public void namedQueryPessimisticWriteDbSql(EntityManager em);

        public void findNoLockAfterNamedQueryPessimisticWriteDbSql(EntityManager em);
    }
}

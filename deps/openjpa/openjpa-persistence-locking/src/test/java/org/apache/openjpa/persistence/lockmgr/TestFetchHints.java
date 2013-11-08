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

import java.sql.Connection;
import java.sql.ResultSet;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.Query;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.kernel.EagerFetchModes;
import org.apache.openjpa.jdbc.kernel.JDBCFetchConfigurationImpl;
import org.apache.openjpa.jdbc.kernel.LRSSizes;
import org.apache.openjpa.jdbc.sql.JoinSyntaxes;
import org.apache.openjpa.kernel.FetchConfiguration;
import org.apache.openjpa.kernel.MixedLockLevels;
import org.apache.openjpa.kernel.QueryFlushModes;
import org.apache.openjpa.persistence.EntityManagerImpl;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.jdbc.FetchDirection;
import org.apache.openjpa.persistence.jdbc.FetchMode;
import org.apache.openjpa.persistence.jdbc.IsolationLevel;
import org.apache.openjpa.persistence.jdbc.JDBCFetchPlan;
import org.apache.openjpa.persistence.jdbc.JoinSyntax;
import org.apache.openjpa.persistence.jdbc.LRSSizeAlgorithm;
import org.apache.openjpa.persistence.jdbc.ResultSetType;

/**
 * Test FetchPlan and FetchConfiguration hints processing for use in
 * JPA 2.0 em.find/refresh/lock(... Map) and query.X(... Map) methods.
 *
 * Supported hints:
 *   javax.persistence.lock.timeout
 *   javax.persistence.query.timeout
 *   openjpa.ExtendedPathLookup (?)
 *   openjpa.FetchBatchSize
 *   openjpa.FetchPlan.EagerFetchMode
 *   openjpa.FetchPlan.ExtendedPathLookup (?)
 *   openjpa.FetchPlan.FetchBatchSize
 *   openjpa.FetchPlan.FetchDirection
 *   openjpa.FetchPlan.Isolation
 *   openjpa.FetchPlan.JoinSyntax
 *   openjpa.FetchPlan.LRSSize
 *   openjpa.FetchPlan.LRSSizeAlgorithm
 *   openjpa.FetchPlan.LockTimeout
 *   openjpa.FetchPlan.MaxFetchDepth
 *   openjpa.FetchPlan.QueryTimeout
 *   openjpa.FetchPlan.ReadLockMode
 *   openjpa.FetchPlan.ResultSetType
 *   openjpa.FetchPlan.SubclassFetchMode
 *   openjpa.FetchPlan.WriteLockMode
 *   openjpa.FlushBeforeQueries
 *   openjpa.LockTimeout
 *   openjpa.MaxFetchDepth
 *   openjpa.QueryTimeout
 *   openjpa.ReadLockLevel
 *   openjpa.WriteLockLevel
 *   openjpa.jdbc.EagerFetchMode
 *   openjpa.jdbc.FetchDirection
 *   openjpa.jdbc.JoinSyntax
 *   openjpa.jdbc.LRSSize
 *   openjpa.jdbc.ResultSetType
 *   openjpa.jdbc.SubclassFetchMode
 *   openjpa.jdbc.TransactionIsolation
 */
public class TestFetchHints extends SequencedActionsTest {
    public void setUp() {
        setUp(LockEmployee.class
            , "openjpa.LockManager", "mixed"
            );
        commonSetUp();
    }

    /* Good
     * Test "openjpa.FetchBatchSize" hint
     */
    public void testFetchBatchSizeHint() {
        String hintName = "openjpa.FetchBatchSize";

        EntityManager em = emf.createEntityManager();
        OpenJPAEntityManager oem = (OpenJPAEntityManager)em.getDelegate();
        JDBCFetchPlan fPlan = (JDBCFetchPlan) oem.getFetchPlan();
        JDBCFetchConfigurationImpl fConfig = (JDBCFetchConfigurationImpl)
            ((EntityManagerImpl) oem).getBroker().getFetchConfiguration();

        fetchBatchSizeHintTest(fPlan, fConfig, hintName, "-1", -1);
        fetchBatchSizeHintTest(fPlan, fConfig, hintName, -1, -1);
        fetchBatchSizeHintTest(fPlan, fConfig, hintName, "100", 100);
        fetchBatchSizeHintTest(fPlan, fConfig, hintName, 100, 100);

        try {
            fPlan.setHint(hintName, "xxxxx");
            fPlan.setHint(hintName, "xxxxx");
            fail("Expecting a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setFetchBatchSize(999);
            fPlan.setHint(hintName, FetchConfiguration.DEFAULT);
            assertEquals(fPlan.getFetchBatchSize(), -1);
        } catch (Exception e) {
            fail("Unexpected " + e.getClass().getName());
        }
        em.close();
    }

    /* Good
     * Test "openjpa.FetchPlan.FetchBatchSize" hint
     */
    public void testFetchPlanFetchBatchSizeHint() {
        String hintName = "openjpa.FetchPlan.FetchBatchSize";

        EntityManager em = emf.createEntityManager();
        OpenJPAEntityManager oem = (OpenJPAEntityManager)em.getDelegate();
        JDBCFetchPlan fPlan = (JDBCFetchPlan) oem.getFetchPlan();
        JDBCFetchConfigurationImpl fConfig = (JDBCFetchConfigurationImpl)
            ((EntityManagerImpl) oem).getBroker().getFetchConfiguration();

        fetchBatchSizeHintTest(fPlan, fConfig, hintName, "0", 0);
        fetchBatchSizeHintTest(fPlan, fConfig, hintName, 0, 0);
        fetchBatchSizeHintTest(fPlan, fConfig, hintName, "500", 500);
        fetchBatchSizeHintTest(fPlan, fConfig, hintName, 500, 500);

        try {
            fPlan.setHint(hintName, "xxxxx");
            fPlan.setHint(hintName, "yyyyy");
            fail("Expecting a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setFetchBatchSize(999);
            fPlan.setHint(hintName, FetchConfiguration.DEFAULT);
            assertEquals(fPlan.getFetchBatchSize(), -1);
        } catch (Exception e) {
            fail("Unexpected " + e.getClass().getName());
        }
        em.close();
    }

    private void fetchBatchSizeHintTest(JDBCFetchPlan fPlan,
        JDBCFetchConfigurationImpl fConfig, String hint, Object value,
        int expected) {
        fConfig.setFetchBatchSize(999);
        fPlan.setHint(hint, value);
        Object getValue = fPlan.getHint(hint);
        assertEquals(value.getClass(), getValue.getClass());
        assertEquals(value, getValue);
        assertEquals(expected, fPlan.getFetchBatchSize());
        assertEquals(expected, fConfig.getFetchBatchSize());
    }

    /* Good
     * Test "openjpa.FetchPlan.EagerFetchMode" hint
     */
    public void testFetchPlanEagerFetchModeHint() {
        String hintName = "openjpa.FetchPlan.EagerFetchMode";

        EntityManager em = emf.createEntityManager();
        OpenJPAEntityManager oem = (OpenJPAEntityManager) em.getDelegate();
        JDBCFetchPlan fPlan = (JDBCFetchPlan) oem.getFetchPlan();
        JDBCFetchConfigurationImpl fConfig = (JDBCFetchConfigurationImpl)
            ((EntityManagerImpl) oem).getBroker().getFetchConfiguration();

        eagerFetchModeHintTest(fPlan, fConfig, hintName, "none",
            FetchMode.NONE, EagerFetchModes.EAGER_NONE);
        eagerFetchModeHintTest(fPlan, fConfig, hintName, FetchMode.NONE.name(),
            FetchMode.NONE, EagerFetchModes.EAGER_NONE);
        eagerFetchModeHintTest(fPlan, fConfig, hintName, FetchMode.NONE,
            FetchMode.NONE, EagerFetchModes.EAGER_NONE);

        eagerFetchModeHintTest(fPlan, fConfig, hintName, "parallel",
            FetchMode.PARALLEL, EagerFetchModes.EAGER_PARALLEL);
        eagerFetchModeHintTest(fPlan, fConfig, hintName, FetchMode.PARALLEL
            .name(), FetchMode.PARALLEL, EagerFetchModes.EAGER_PARALLEL);
        eagerFetchModeHintTest(fPlan, fConfig, hintName, FetchMode.PARALLEL,
            FetchMode.PARALLEL, EagerFetchModes.EAGER_PARALLEL);

        eagerFetchModeHintTest(fPlan, fConfig, hintName, "join",
            FetchMode.JOIN, EagerFetchModes.EAGER_JOIN);
        eagerFetchModeHintTest(fPlan, fConfig, hintName, FetchMode.JOIN.name(),
            FetchMode.JOIN, EagerFetchModes.EAGER_JOIN);
        eagerFetchModeHintTest(fPlan, fConfig, hintName, FetchMode.JOIN,
            FetchMode.JOIN, EagerFetchModes.EAGER_JOIN);

        try {
            fPlan.setHint(hintName, "xxxxx");
            fPlan.setHint(hintName, "yyyyy");
            fail("Expecting a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, "12345");
            fPlan.setHint(hintName, "67890");
            fail("Expecting a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, -2);
            fPlan.setHint(hintName, -3);
            fail("Expecting a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        em.close();
    }

    /* Good
     * Test "openjpa.jdbc.EagerFetchMode" hint
     */
    public void testJdbcEagerFetchModeHint() {
        String hintName = "openjpa.jdbc.EagerFetchMode";

        EntityManager em = emf.createEntityManager();
        OpenJPAEntityManager oem = (OpenJPAEntityManager) em.getDelegate();
        JDBCFetchPlan fPlan = (JDBCFetchPlan) oem.getFetchPlan();
        JDBCFetchConfigurationImpl fConfig = (JDBCFetchConfigurationImpl)
            ((EntityManagerImpl) oem).getBroker().getFetchConfiguration();

        eagerFetchModeHintTest(fPlan, fConfig, hintName, String
            .valueOf(EagerFetchModes.EAGER_NONE), FetchMode.NONE,
            EagerFetchModes.EAGER_NONE);
        eagerFetchModeHintTest(fPlan, fConfig, hintName,
            EagerFetchModes.EAGER_NONE, FetchMode.NONE,
            EagerFetchModes.EAGER_NONE);

        eagerFetchModeHintTest(fPlan, fConfig, hintName,
            EagerFetchModes.EAGER_PARALLEL, FetchMode.PARALLEL,
            EagerFetchModes.EAGER_PARALLEL);
        eagerFetchModeHintTest(fPlan, fConfig, hintName, String
            .valueOf(EagerFetchModes.EAGER_PARALLEL), FetchMode.PARALLEL,
            EagerFetchModes.EAGER_PARALLEL);

        eagerFetchModeHintTest(fPlan, fConfig, hintName, String
            .valueOf(EagerFetchModes.EAGER_JOIN), FetchMode.JOIN,
            EagerFetchModes.EAGER_JOIN);
        eagerFetchModeHintTest(fPlan, fConfig, hintName,
            EagerFetchModes.EAGER_JOIN, FetchMode.JOIN,
            EagerFetchModes.EAGER_JOIN);

        try {
            fPlan.setHint(hintName, "xxxxx");
            fPlan.setHint(hintName, "yyyyy");
            fail("Expecting a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, "12345");
            fPlan.setHint(hintName, "67890");
            fail("Expecting a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, -1);
            fPlan.setHint(hintName, -2);
            fail("Expecting a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, FetchConfiguration.DEFAULT);
            assertEquals(fPlan.getEagerFetchMode(), FetchMode.PARALLEL);
        } catch (Exception e) {
              fail("Unexpected " + e.getClass().getName());
        }

        em.close();
    }

    private void eagerFetchModeHintTest(JDBCFetchPlan fPlan,
        JDBCFetchConfigurationImpl fConfig, String hint, Object value,
        Object expectedValue, int expected) {
        fPlan.setHint(hint, value);
        Object getValue = fPlan.getHint(hint);
        assertEquals(value.getClass(), getValue.getClass());
        assertEquals(value, getValue);
        assertEquals(expectedValue, fPlan.getEagerFetchMode());
        assertEquals(expected, fConfig.getEagerFetchMode());
    }

    /* Good
     * Test "openjpa.FetchPlan.JoinSyntax" hint
     */
    public void testFetchPlanJoinSyntaxHint() {
        String hintName = "openjpa.FetchPlan.JoinSyntax";

        EntityManager em = emf.createEntityManager();
        OpenJPAEntityManager oem = (OpenJPAEntityManager)em.getDelegate();
        JDBCFetchPlan fPlan = (JDBCFetchPlan) oem.getFetchPlan();
        JDBCFetchConfigurationImpl fConfig = (JDBCFetchConfigurationImpl)
            ((EntityManagerImpl) oem).getBroker().getFetchConfiguration();

        joinSyntaxHintTest(fPlan, fConfig, hintName, "sql92",
            JoinSyntax.SQL92, JoinSyntaxes.SYNTAX_SQL92);
        joinSyntaxHintTest(fPlan, fConfig, hintName, JoinSyntax.SQL92.name(),
            JoinSyntax.SQL92, JoinSyntaxes.SYNTAX_SQL92);
        joinSyntaxHintTest(fPlan, fConfig, hintName, JoinSyntax.SQL92,
            JoinSyntax.SQL92, JoinSyntaxes.SYNTAX_SQL92);

        joinSyntaxHintTest(fPlan, fConfig, hintName, "traditional",
            JoinSyntax.TRADITIONAL, JoinSyntaxes.SYNTAX_TRADITIONAL);
        joinSyntaxHintTest(fPlan, fConfig, hintName, JoinSyntax.TRADITIONAL
            .name(), JoinSyntax.TRADITIONAL, JoinSyntaxes.SYNTAX_TRADITIONAL);
        joinSyntaxHintTest(fPlan, fConfig, hintName, JoinSyntax.TRADITIONAL,
            JoinSyntax.TRADITIONAL, JoinSyntaxes.SYNTAX_TRADITIONAL);

        joinSyntaxHintTest(fPlan, fConfig, hintName, "database",
            JoinSyntax.DATABASE, JoinSyntaxes.SYNTAX_DATABASE);
        joinSyntaxHintTest(fPlan, fConfig, hintName,
            JoinSyntax.DATABASE.name(), JoinSyntax.DATABASE,
            JoinSyntaxes.SYNTAX_DATABASE);
        joinSyntaxHintTest(fPlan, fConfig, hintName, JoinSyntax.DATABASE,
            JoinSyntax.DATABASE, JoinSyntaxes.SYNTAX_DATABASE);

        try {
            fPlan.setHint(hintName, "xxxxx");
            fPlan.setHint(hintName, "yyyyy");
            fail("Expecting a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, "12345");
            fPlan.setHint(hintName, "67890");
            fail("Expecting a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, -1);
            fPlan.setHint(hintName, -2);
            fail("Expecting a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        em.close();
    }

    /* Good
     * Test "openjpa.jdbc.JoinSyntax" hint
     */
    public void testJdbcJoinSyntaxHint() {
        String hintName = "openjpa.jdbc.JoinSyntax";

        EntityManager em = emf.createEntityManager();
        OpenJPAEntityManager oem = (OpenJPAEntityManager) em.getDelegate();
        JDBCFetchPlan fPlan = (JDBCFetchPlan) oem.getFetchPlan();
        JDBCFetchConfigurationImpl fConfig = (JDBCFetchConfigurationImpl)
            ((EntityManagerImpl) oem).getBroker().getFetchConfiguration();

        joinSyntaxHintTest(fPlan, fConfig, hintName, String
            .valueOf(JoinSyntaxes.SYNTAX_SQL92), JoinSyntax.SQL92,
            JoinSyntaxes.SYNTAX_SQL92);
        joinSyntaxHintTest(fPlan, fConfig, hintName, JoinSyntaxes.SYNTAX_SQL92,
            JoinSyntax.SQL92, JoinSyntaxes.SYNTAX_SQL92);

        joinSyntaxHintTest(fPlan, fConfig, hintName, String
            .valueOf(JoinSyntaxes.SYNTAX_TRADITIONAL), JoinSyntax.TRADITIONAL,
            JoinSyntaxes.SYNTAX_TRADITIONAL);
        joinSyntaxHintTest(fPlan, fConfig, hintName,
            JoinSyntaxes.SYNTAX_TRADITIONAL, JoinSyntax.TRADITIONAL,
            JoinSyntaxes.SYNTAX_TRADITIONAL);

        joinSyntaxHintTest(fPlan, fConfig, hintName, String
            .valueOf(JoinSyntaxes.SYNTAX_DATABASE), JoinSyntax.DATABASE,
            JoinSyntaxes.SYNTAX_DATABASE);
        joinSyntaxHintTest(fPlan, fConfig, hintName,
            JoinSyntaxes.SYNTAX_DATABASE, JoinSyntax.DATABASE,
            JoinSyntaxes.SYNTAX_DATABASE);

        try {
            fPlan.setHint(hintName, "xxxxx");
            fPlan.setHint(hintName, "yyyyy");
            fail("Expecting a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, "12345");
            fPlan.setHint(hintName, "67890");
            fail("Expecting a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, -1);
            fPlan.setHint(hintName, -2);
            fail("Expecting a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, FetchConfiguration.DEFAULT);
            assertEquals(fConfig.getJoinSyntax(),
                ((JDBCConfiguration) fConfig.getContext().getConfiguration())
                    .getDBDictionaryInstance().joinSyntax);
        } catch (Exception e) {
            fail("Unexpected " + e.getClass().getName());
        }
        em.close();
    }

    private void joinSyntaxHintTest(JDBCFetchPlan fPlan,
        JDBCFetchConfigurationImpl fConfig, String hint, Object value,
        Object expectedValue, int expected) {
        fPlan.setHint(hint, value);
        Object getValue = fPlan.getHint(hint);
        assertEquals(value.getClass(), getValue.getClass());
        assertEquals(value, getValue);
        assertEquals(expectedValue, fPlan.getJoinSyntax());
        assertEquals(expected, fConfig.getJoinSyntax());
    }

    /* Good
     * Test "openjpa.FetchPlan.FetchDirection" hint
     */
    public void testFetchPlanFetchDirectionHint() {
        String hintName = "openjpa.FetchPlan.FetchDirection";

        EntityManager em = emf.createEntityManager();
        OpenJPAEntityManager oem = (OpenJPAEntityManager) em.getDelegate();
        JDBCFetchPlan fPlan = (JDBCFetchPlan) oem.getFetchPlan();
        JDBCFetchConfigurationImpl fConfig = (JDBCFetchConfigurationImpl)
            ((EntityManagerImpl) oem).getBroker().getFetchConfiguration();

        fetchDirectionHintTest(fPlan, fConfig, hintName, "forward",
            FetchDirection.FORWARD, ResultSet.FETCH_FORWARD);
        fetchDirectionHintTest(fPlan, fConfig, hintName, FetchDirection.FORWARD
            .name(), FetchDirection.FORWARD, ResultSet.FETCH_FORWARD);
        fetchDirectionHintTest(fPlan, fConfig, hintName,
            FetchDirection.FORWARD, FetchDirection.FORWARD,
            ResultSet.FETCH_FORWARD);

        fetchDirectionHintTest(fPlan, fConfig, hintName, "reverse",
            FetchDirection.REVERSE, ResultSet.FETCH_REVERSE);
        fetchDirectionHintTest(fPlan, fConfig, hintName, FetchDirection.REVERSE
            .name(), FetchDirection.REVERSE, ResultSet.FETCH_REVERSE);
        fetchDirectionHintTest(fPlan, fConfig, hintName,
            FetchDirection.REVERSE, FetchDirection.REVERSE,
            ResultSet.FETCH_REVERSE);

        fetchDirectionHintTest(fPlan, fConfig, hintName, "unknown",
            FetchDirection.UNKNOWN, ResultSet.FETCH_UNKNOWN);
        fetchDirectionHintTest(fPlan, fConfig, hintName, FetchDirection.UNKNOWN
            .name(), FetchDirection.UNKNOWN, ResultSet.FETCH_UNKNOWN);
        fetchDirectionHintTest(fPlan, fConfig, hintName,
            FetchDirection.UNKNOWN, FetchDirection.UNKNOWN,
            ResultSet.FETCH_UNKNOWN);

        try {
            fPlan.setHint(hintName, "xxxxx");
            fPlan.setHint(hintName, "yyyyy");
            fail("Expecting a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, "12345");
            fPlan.setHint(hintName, "67890");
            fail("Expecting a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, -1);
            fPlan.setHint(hintName, -2);
            fail("Expecting a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        em.close();
    }

    /* Good
     * Test "openjpa.jdbc.FetchDirection" hint
     */
    public void testJdbcFetchDirectionHint() {
        String hintName = "openjpa.jdbc.FetchDirection";

        EntityManager em = emf.createEntityManager();
        OpenJPAEntityManager oem = (OpenJPAEntityManager) em.getDelegate();
        JDBCFetchPlan fPlan = (JDBCFetchPlan) oem.getFetchPlan();
        JDBCFetchConfigurationImpl fConfig = (JDBCFetchConfigurationImpl)
            ((EntityManagerImpl) oem).getBroker().getFetchConfiguration();

        fetchDirectionHintTest(fPlan, fConfig, hintName, String
            .valueOf(ResultSet.FETCH_FORWARD), FetchDirection.FORWARD,
            ResultSet.FETCH_FORWARD);
        fetchDirectionHintTest(fPlan, fConfig, hintName,
            ResultSet.FETCH_FORWARD, FetchDirection.FORWARD,
            ResultSet.FETCH_FORWARD);

        fetchDirectionHintTest(fPlan, fConfig, hintName, String
            .valueOf(ResultSet.FETCH_REVERSE), FetchDirection.REVERSE,
            ResultSet.FETCH_REVERSE);
        fetchDirectionHintTest(fPlan, fConfig, hintName,
            ResultSet.FETCH_REVERSE, FetchDirection.REVERSE,
            ResultSet.FETCH_REVERSE);

        fetchDirectionHintTest(fPlan, fConfig, hintName, String
            .valueOf(ResultSet.FETCH_UNKNOWN), FetchDirection.UNKNOWN,
            ResultSet.FETCH_UNKNOWN);
        fetchDirectionHintTest(fPlan, fConfig, hintName,
            ResultSet.FETCH_UNKNOWN, FetchDirection.UNKNOWN,
            ResultSet.FETCH_UNKNOWN);

        try {
            fPlan.setHint(hintName, "xxxxx");
            fPlan.setHint(hintName, "yyyyy");
            fail("Expecting a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, "12345");
            fPlan.setHint(hintName, "67890");
            fail("Expecting a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, -1);
            fPlan.setHint(hintName, -2);
            fail("Expecting a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, FetchConfiguration.DEFAULT);
            assertEquals(fConfig.getFetchDirection(), ResultSet.FETCH_FORWARD);
        } catch (Exception e) {
            fail("Unexpected " + e.getClass().getName());
        }
        em.close();
    }

    private void fetchDirectionHintTest(JDBCFetchPlan fPlan,
        JDBCFetchConfigurationImpl fConfig, String hint, Object value,
        Object expectedValue, int expected) {
        fPlan.setHint(hint, value);
        Object getValue = fPlan.getHint(hint);
        assertEquals(value.getClass(), getValue.getClass());
        assertEquals(value, getValue);
        assertEquals(expectedValue, fPlan.getFetchDirection());
        assertEquals(expected, fConfig.getFetchDirection());
    }

    /* Good
     * Test "openjpa.FetchPlan.Isolation" hint
     */
    public void testFetchPlanIsolationHint() {
        String hintName = "openjpa.FetchPlan.Isolation";

        EntityManager em = emf.createEntityManager();
        OpenJPAEntityManager oem = (OpenJPAEntityManager) em.getDelegate();
        JDBCFetchPlan fPlan = (JDBCFetchPlan) oem.getFetchPlan();
        JDBCFetchConfigurationImpl fConfig = (JDBCFetchConfigurationImpl)
            ((EntityManagerImpl) oem).getBroker().getFetchConfiguration();

        isolationHintTest(oem, fPlan, fConfig, hintName, "default",
            IsolationLevel.DEFAULT, -1);
        isolationHintTest(oem, fPlan, fConfig, hintName, "DEFAULT",
            IsolationLevel.DEFAULT, -1);
        isolationHintTest(oem, fPlan, fConfig, hintName,
            IsolationLevel.DEFAULT, IsolationLevel.DEFAULT, -1);

        boolean supportIsolationForUpdate = ((JDBCConfiguration) fConfig
            .getContext().getConfiguration()).getDBDictionaryInstance()
            .supportsIsolationForUpdate();
        if (supportIsolationForUpdate) {
            isolationHintTest(oem, fPlan, fConfig, hintName, "none",
                IsolationLevel.NONE, Connection.TRANSACTION_NONE);
            isolationHintTest(oem, fPlan, fConfig, hintName, "NONE",
                IsolationLevel.NONE, Connection.TRANSACTION_NONE);
            isolationHintTest(oem, fPlan, fConfig, hintName,
                IsolationLevel.NONE, IsolationLevel.NONE,
                Connection.TRANSACTION_NONE);

            isolationHintTest(oem, fPlan, fConfig, hintName,
                "read-uncommitted", IsolationLevel.READ_UNCOMMITTED,
                Connection.TRANSACTION_READ_UNCOMMITTED);
            isolationHintTest(oem, fPlan, fConfig, hintName,
                "READ_UNCOMMITTED", IsolationLevel.READ_UNCOMMITTED,
                Connection.TRANSACTION_READ_UNCOMMITTED);
            isolationHintTest(oem, fPlan, fConfig, hintName,
                IsolationLevel.READ_UNCOMMITTED,
                IsolationLevel.READ_UNCOMMITTED,
                Connection.TRANSACTION_READ_UNCOMMITTED);

            isolationHintTest(oem, fPlan, fConfig, hintName, "read-committed",
                IsolationLevel.READ_COMMITTED,
                Connection.TRANSACTION_READ_COMMITTED);
            isolationHintTest(oem, fPlan, fConfig, hintName, "READ_COMMITTED",
                IsolationLevel.READ_COMMITTED,
                Connection.TRANSACTION_READ_COMMITTED);
            isolationHintTest(oem, fPlan, fConfig, hintName,
                IsolationLevel.READ_COMMITTED, IsolationLevel.READ_COMMITTED,
                Connection.TRANSACTION_READ_COMMITTED);

            isolationHintTest(oem, fPlan, fConfig, hintName, "repeatable-read",
                IsolationLevel.REPEATABLE_READ,
                Connection.TRANSACTION_REPEATABLE_READ);
            isolationHintTest(oem, fPlan, fConfig, hintName, "REPEATABLE_READ",
                IsolationLevel.REPEATABLE_READ,
                Connection.TRANSACTION_REPEATABLE_READ);
            isolationHintTest(oem, fPlan, fConfig, hintName,
                IsolationLevel.REPEATABLE_READ, IsolationLevel.REPEATABLE_READ,
                Connection.TRANSACTION_REPEATABLE_READ);

            isolationHintTest(oem, fPlan, fConfig, hintName, "serializable",
                IsolationLevel.SERIALIZABLE,
                Connection.TRANSACTION_SERIALIZABLE);
            isolationHintTest(oem, fPlan, fConfig, hintName, "SERIALIZABLE",
                IsolationLevel.SERIALIZABLE,
                Connection.TRANSACTION_SERIALIZABLE);
            isolationHintTest(oem, fPlan, fConfig, hintName,
                IsolationLevel.SERIALIZABLE, IsolationLevel.SERIALIZABLE,
                Connection.TRANSACTION_SERIALIZABLE);
        }

        try {
            fPlan.setHint(hintName, "xxxxx");
            fPlan.setHint(hintName, "yyyyy");
            fail("Expecting a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, "12345");
            fPlan.setHint(hintName, "67890");
            fail("Expecting a a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        // Is not -1 is valid value for transaction isolation level?
//        try {
//            fPlan.setHint(hintName, -1);
//            fPlan.setHint(hintName, -1);
//            fail("Expecting a a IllegalArgumentException.");
//        } catch (Exception e) {
//            assertTrue("Caught expected exception",
//                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
//        }
        em.close();
    }

    /* Good
     * Test "openjpa.jdbc.TransactionIsolation" hint
     */
    public void testJdbcTransactionIsolationHint() {
        String hintName = "openjpa.jdbc.TransactionIsolation";

        EntityManager em = emf.createEntityManager();
        OpenJPAEntityManager oem = (OpenJPAEntityManager) em.getDelegate();
        JDBCFetchPlan fPlan = (JDBCFetchPlan) oem.getFetchPlan();
        JDBCFetchConfigurationImpl fConfig = (JDBCFetchConfigurationImpl)
            ((EntityManagerImpl) oem).getBroker().getFetchConfiguration();

        isolationHintTest(oem, fPlan, fConfig, hintName, "-1",
            IsolationLevel.DEFAULT, -1);
        isolationHintTest(oem, fPlan, fConfig, hintName, -1,
            IsolationLevel.DEFAULT, -1);

        boolean supportIsolationForUpdate = ((JDBCConfiguration) fConfig
            .getContext().getConfiguration()).getDBDictionaryInstance()
            .supportsIsolationForUpdate();
        if (supportIsolationForUpdate) {
            isolationHintTest(oem, fPlan, fConfig, hintName, String
                .valueOf(Connection.TRANSACTION_NONE), IsolationLevel.NONE,
                Connection.TRANSACTION_NONE);
            isolationHintTest(oem, fPlan, fConfig, hintName,
                Connection.TRANSACTION_NONE, IsolationLevel.NONE,
                Connection.TRANSACTION_NONE);

            isolationHintTest(oem, fPlan, fConfig, hintName, String
                .valueOf(Connection.TRANSACTION_READ_UNCOMMITTED),
                IsolationLevel.READ_UNCOMMITTED,
                Connection.TRANSACTION_READ_UNCOMMITTED);
            isolationHintTest(oem, fPlan, fConfig, hintName,
                Connection.TRANSACTION_READ_UNCOMMITTED,
                IsolationLevel.READ_UNCOMMITTED,
                Connection.TRANSACTION_READ_UNCOMMITTED);

            isolationHintTest(oem, fPlan, fConfig, hintName, String
                .valueOf(Connection.TRANSACTION_READ_COMMITTED),
                IsolationLevel.READ_COMMITTED,
                Connection.TRANSACTION_READ_COMMITTED);
            isolationHintTest(oem, fPlan, fConfig, hintName,
                Connection.TRANSACTION_READ_COMMITTED,
                IsolationLevel.READ_COMMITTED,
                Connection.TRANSACTION_READ_COMMITTED);

            isolationHintTest(oem, fPlan, fConfig, hintName, String
                .valueOf(Connection.TRANSACTION_REPEATABLE_READ),
                IsolationLevel.REPEATABLE_READ,
                Connection.TRANSACTION_REPEATABLE_READ);
            isolationHintTest(oem, fPlan, fConfig, hintName,
                Connection.TRANSACTION_REPEATABLE_READ,
                IsolationLevel.REPEATABLE_READ,
                Connection.TRANSACTION_REPEATABLE_READ);

            isolationHintTest(oem, fPlan, fConfig, hintName, String
                .valueOf(Connection.TRANSACTION_SERIALIZABLE),
                IsolationLevel.SERIALIZABLE,
                Connection.TRANSACTION_SERIALIZABLE);
            isolationHintTest(oem, fPlan, fConfig, hintName,
                Connection.TRANSACTION_SERIALIZABLE,
                IsolationLevel.SERIALIZABLE,
                Connection.TRANSACTION_SERIALIZABLE);
        }
        try {
            fPlan.setHint(hintName, "xxxxx");
            fPlan.setHint(hintName, "yyyyy");
            fail("Expecting a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, "12345");
            fPlan.setHint(hintName, "67890");
            fail("Expecting a a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, -2);
            fPlan.setHint(hintName, -3);
            fail("Expecting a a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, FetchConfiguration.DEFAULT);
            assertEquals(IsolationLevel.DEFAULT, fPlan.getIsolation());
            assertEquals(-1, fConfig.getIsolation());
        } catch (Exception e) {
            fail("Unexpected " + e.getClass().getName());
        }
        em.close();
    }

    @SuppressWarnings("deprecation")
    private void isolationHintTest(OpenJPAEntityManager oem,
        JDBCFetchPlan fPlan, JDBCFetchConfigurationImpl fConfig, String hint,
        Object value, Object expectedValue, int expected) {
        fPlan.setHint(hint, value);
        Object getValue = fPlan.getHint(hint);
        assertEquals(value.getClass(), getValue.getClass());
        assertEquals(value, getValue);
        assertEquals(expectedValue, fPlan.getIsolation());
        assertEquals(expected, fConfig.getIsolation());
    }

    /* Good
     * Test "openjpa.FetchPlan.LRSSizeAlgorithm" hint
     */
    public void testFetchPlanLRSSizeAlgorithmHint() {
        String hintName = "openjpa.FetchPlan.LRSSizeAlgorithm";

        EntityManager em = emf.createEntityManager();
        OpenJPAEntityManager oem = (OpenJPAEntityManager) em.getDelegate();
        JDBCFetchPlan fPlan = (JDBCFetchPlan) oem.getFetchPlan();
        JDBCFetchConfigurationImpl fConfig = (JDBCFetchConfigurationImpl)
            ((EntityManagerImpl) oem).getBroker().getFetchConfiguration();

        lrsSizeHintTest(fPlan, fConfig, hintName, "query",
            LRSSizeAlgorithm.QUERY, LRSSizes.SIZE_QUERY);
        lrsSizeHintTest(fPlan, fConfig, hintName, LRSSizeAlgorithm.QUERY.name(),
            LRSSizeAlgorithm.QUERY, LRSSizes.SIZE_QUERY);
        lrsSizeHintTest(fPlan, fConfig, hintName, LRSSizeAlgorithm.QUERY,
            LRSSizeAlgorithm.QUERY, LRSSizes.SIZE_QUERY);

        lrsSizeHintTest(fPlan, fConfig, hintName, "last",
            LRSSizeAlgorithm.LAST, LRSSizes.SIZE_LAST);
        lrsSizeHintTest(fPlan, fConfig, hintName, LRSSizeAlgorithm.LAST.name(),
            LRSSizeAlgorithm.LAST, LRSSizes.SIZE_LAST);
        lrsSizeHintTest(fPlan, fConfig, hintName, LRSSizeAlgorithm.LAST,
            LRSSizeAlgorithm.LAST, LRSSizes.SIZE_LAST);

        lrsSizeHintTest(fPlan, fConfig, hintName, "unknown",
            LRSSizeAlgorithm.UNKNOWN, LRSSizes.SIZE_UNKNOWN);
        lrsSizeHintTest(fPlan, fConfig, hintName, LRSSizeAlgorithm.UNKNOWN
            .name(), LRSSizeAlgorithm.UNKNOWN, LRSSizes.SIZE_UNKNOWN);
        lrsSizeHintTest(fPlan, fConfig, hintName, LRSSizeAlgorithm.UNKNOWN,
            LRSSizeAlgorithm.UNKNOWN, LRSSizes.SIZE_UNKNOWN);

        try {
            fPlan.setHint(hintName, "xxxxx");
            fPlan.setHint(hintName, "yyyyy");
            fail("Expecting a a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, "12345");
            fPlan.setHint(hintName, "67890");
            fail("Expecting a a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, -1);
            fPlan.setHint(hintName, -2);
            fail("Expecting a a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught unexpected exception " + e,
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        em.close();
    }

    /* Good
     * Test "openjpa.FetchPlan.LRSSize" hint
     */
    @SuppressWarnings("deprecation")
    public void testFetchPlanLRSSizeHint() {
        String hintName = "openjpa.FetchPlan.LRSSize";

        EntityManager em = emf.createEntityManager();
        OpenJPAEntityManager oem = (OpenJPAEntityManager) em.getDelegate();
        JDBCFetchPlan fPlan = (JDBCFetchPlan) oem.getFetchPlan();
        JDBCFetchConfigurationImpl fConfig = (JDBCFetchConfigurationImpl)
            ((EntityManagerImpl) oem).getBroker().getFetchConfiguration();

        lrsSizeHintTest(fPlan, fConfig, hintName, String
            .valueOf(LRSSizes.SIZE_QUERY), LRSSizeAlgorithm.QUERY,
            LRSSizes.SIZE_QUERY);
        lrsSizeHintTest(fPlan, fConfig, hintName, LRSSizes.SIZE_QUERY,
            LRSSizeAlgorithm.QUERY, LRSSizes.SIZE_QUERY);

        lrsSizeHintTest(fPlan, fConfig, hintName, String
            .valueOf(LRSSizes.SIZE_LAST), LRSSizeAlgorithm.LAST,
            LRSSizes.SIZE_LAST);
        lrsSizeHintTest(fPlan, fConfig, hintName, LRSSizes.SIZE_LAST,
            LRSSizeAlgorithm.LAST, LRSSizes.SIZE_LAST);

        lrsSizeHintTest(fPlan, fConfig, hintName, String
            .valueOf(LRSSizes.SIZE_UNKNOWN), LRSSizeAlgorithm.UNKNOWN,
            LRSSizes.SIZE_UNKNOWN);
        lrsSizeHintTest(fPlan, fConfig, hintName, LRSSizes.SIZE_UNKNOWN,
            LRSSizeAlgorithm.UNKNOWN, LRSSizes.SIZE_UNKNOWN);

        try {
            fPlan.setHint(hintName, "xxxxx");
            fPlan.setHint(hintName, "yyyyy");
            fail("Expecting a a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, "12345");
            fPlan.setHint(hintName, "67890");
            fail("Expecting a a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, -1);
            fPlan.setHint(hintName, -2);
            fail("Expecting a a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, FetchConfiguration.DEFAULT);
            assertEquals(LRSSizeAlgorithm.QUERY, fPlan.getLRSSizeAlgorithm());
            assertEquals(LRSSizes.SIZE_QUERY, fPlan.getLRSSize());
            assertEquals(LRSSizes.SIZE_QUERY, fConfig.getLRSSize());
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected " + e.getClass().getName());
        }
        em.close();
    }

    /* Good
     * Test "openjpa.jdbc.LRSSize" hint
     */
    @SuppressWarnings("deprecation")
    public void testJdbcLRSSizeHint() {
        String hintName = "openjpa.jdbc.LRSSize";

        EntityManager em = emf.createEntityManager();
        OpenJPAEntityManager oem = (OpenJPAEntityManager) em.getDelegate();
        JDBCFetchPlan fPlan = (JDBCFetchPlan) oem.getFetchPlan();
        JDBCFetchConfigurationImpl fConfig = (JDBCFetchConfigurationImpl)
            ((EntityManagerImpl) oem).getBroker().getFetchConfiguration();

        lrsSizeHintTest(fPlan, fConfig, hintName, String
            .valueOf(LRSSizes.SIZE_QUERY), LRSSizeAlgorithm.QUERY,
            LRSSizes.SIZE_QUERY);
        lrsSizeHintTest(fPlan, fConfig, hintName, LRSSizes.SIZE_QUERY,
            LRSSizeAlgorithm.QUERY, LRSSizes.SIZE_QUERY);

        lrsSizeHintTest(fPlan, fConfig, hintName, String
            .valueOf(LRSSizes.SIZE_LAST), LRSSizeAlgorithm.LAST,
            LRSSizes.SIZE_LAST);
        lrsSizeHintTest(fPlan, fConfig, hintName, LRSSizes.SIZE_LAST,
            LRSSizeAlgorithm.LAST, LRSSizes.SIZE_LAST);

        lrsSizeHintTest(fPlan, fConfig, hintName, String
            .valueOf(LRSSizes.SIZE_UNKNOWN), LRSSizeAlgorithm.UNKNOWN,
            LRSSizes.SIZE_UNKNOWN);
        lrsSizeHintTest(fPlan, fConfig, hintName, LRSSizes.SIZE_UNKNOWN,
            LRSSizeAlgorithm.UNKNOWN, LRSSizes.SIZE_UNKNOWN);

        try {
            fPlan.setHint(hintName, "xxxxx");
            fPlan.setHint(hintName, "yyyyy");
            fail("Expecting a a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, "12345");
            fPlan.setHint(hintName, "67890");
            fail("Expecting a a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, -1);
            fPlan.setHint(hintName, -2);
            fail("Expecting a a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, FetchConfiguration.DEFAULT);
            assertEquals(LRSSizeAlgorithm.QUERY, fPlan.getLRSSizeAlgorithm());
            assertEquals(LRSSizes.SIZE_QUERY, fPlan.getLRSSize());
            assertEquals(LRSSizes.SIZE_QUERY, fConfig.getLRSSize());
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected " + e.getClass().getName());
        }
        em.close();
    }

    @SuppressWarnings("deprecation")
    private void lrsSizeHintTest(JDBCFetchPlan fPlan,
        JDBCFetchConfigurationImpl fConfig, String hint, Object value,
        Object expectedValue, int expected) {
        fPlan.setHint(hint, value);
        Object getValue = fPlan.getHint(hint);
        assertEquals(value.getClass(), getValue.getClass());
        assertEquals(value, getValue);
        assertEquals(expectedValue, fPlan.getLRSSizeAlgorithm());
        assertEquals(expected, fPlan.getLRSSize());
        assertEquals(expected, fConfig.getLRSSize());
    }

    /* Good
     * Test "openjpa.FetchPlan.MaxFetchDepth" hint
     */
    public void testFetchPlanMaxFetchDepthHint() {
        String hintName = "openjpa.FetchPlan.MaxFetchDepth";

        EntityManager em = emf.createEntityManager();
        OpenJPAEntityManager oem = (OpenJPAEntityManager)em.getDelegate();
        JDBCFetchPlan fPlan = (JDBCFetchPlan) oem.getFetchPlan();
        JDBCFetchConfigurationImpl fConfig = (JDBCFetchConfigurationImpl)
            ((EntityManagerImpl) oem).getBroker().getFetchConfiguration();

        maxFetchDepthHintTest(fPlan, fConfig, hintName, "-1", -1);
        maxFetchDepthHintTest(fPlan, fConfig, hintName, -1, -1);
        maxFetchDepthHintTest(fPlan, fConfig, hintName, "500", 500);
        maxFetchDepthHintTest(fPlan, fConfig, hintName, 500, 500);

        try {
            fPlan.setHint(hintName, "xxxxx");
            fPlan.setHint(hintName, "yyyyy");
            fail("Expecting a a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, FetchConfiguration.DEFAULT);
            assertEquals(-1, fPlan.getMaxFetchDepth());
            assertEquals(-1, fConfig.getMaxFetchDepth());
        } catch (IllegalArgumentException e) {
            fail("Unexpected IllegalArgumentException.");
        }
        em.close();
    }

    /* Good
     * Test "openjpa.MaxFetchDepth" hint
     */
    public void testMaxFetchDepthHint() {
        String hintName = "openjpa.MaxFetchDepth";

        EntityManager em = emf.createEntityManager();
        OpenJPAEntityManager oem = (OpenJPAEntityManager)em.getDelegate();
        JDBCFetchPlan fPlan = (JDBCFetchPlan) oem.getFetchPlan();
        JDBCFetchConfigurationImpl fConfig = (JDBCFetchConfigurationImpl)
            ((EntityManagerImpl) oem).getBroker().getFetchConfiguration();

        maxFetchDepthHintTest(fPlan, fConfig, hintName, "-1", -1);
        maxFetchDepthHintTest(fPlan, fConfig, hintName, -1, -1);
        maxFetchDepthHintTest(fPlan, fConfig, hintName, "100", 100);
        maxFetchDepthHintTest(fPlan, fConfig, hintName, 100, 100);

        try {
            fPlan.setHint(hintName, "xxxxx");
            fPlan.setHint(hintName, "yyyyy");
            fail("Expecting a a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, FetchConfiguration.DEFAULT);
            assertEquals(-1, fPlan.getMaxFetchDepth());
            assertEquals(-1, fConfig.getMaxFetchDepth());
        } catch (Exception e) {
            fail("Unexpected " + e.getClass().getName());
        }
        em.close();
    }

    private void maxFetchDepthHintTest(JDBCFetchPlan fPlan,
        JDBCFetchConfigurationImpl fConfig, String hint, Object value,
        int expected) {
        fPlan.setHint(hint, value);
        Object getValue = fPlan.getHint(hint);
        assertEquals(value.getClass(), getValue.getClass());
        assertEquals(value, getValue);
        assertEquals(expected, fPlan.getMaxFetchDepth());
        assertEquals(expected, fConfig.getMaxFetchDepth());
    }

    /* Good
     * Test "openjpa.LockTimeout" hint
     */
    public void testLockTimeoutHint() {
        String hintName = "openjpa.LockTimeout";

        EntityManager em = emf.createEntityManager();
        OpenJPAEntityManager oem = (OpenJPAEntityManager)em.getDelegate();
        JDBCFetchPlan fPlan = (JDBCFetchPlan) oem.getFetchPlan();
        JDBCFetchConfigurationImpl fConfig = (JDBCFetchConfigurationImpl)
            ((EntityManagerImpl) oem).getBroker().getFetchConfiguration();

        lockTimeoutHintTest(fPlan, fConfig, hintName, "-1", -1);
        lockTimeoutHintTest(fPlan, fConfig, hintName, -1, -1);
        lockTimeoutHintTest(fPlan, fConfig, hintName, "0", 0);
        lockTimeoutHintTest(fPlan, fConfig, hintName, 0, 0);
        lockTimeoutHintTest(fPlan, fConfig, hintName, "100", 100);
        lockTimeoutHintTest(fPlan, fConfig, hintName, 100, 100);

        try {
            fPlan.setHint(hintName, "xxxxx");
            fPlan.setHint(hintName, "yyyyy");
            fail("Expecting a a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, -2);
            fPlan.setHint(hintName, -3);
            fail("Expecting a a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, FetchConfiguration.DEFAULT);
            int defTimeout = fConfig.getContext().getConfiguration()
                .getLockTimeout();
            assertEquals(defTimeout, fPlan.getLockTimeout());
            assertEquals(defTimeout, fConfig.getLockTimeout());
        } catch (Exception e) {
            fail("Unexpected " + e.getClass().getName());
        }
        em.close();
    }

    /* Good
     * Test "openjpa.FetchPlan.LockTimeout" hint
     */
    public void testFetchPlanLockTimeoutHint() {
        String hintName = "openjpa.FetchPlan.LockTimeout";

        EntityManager em = emf.createEntityManager();
        OpenJPAEntityManager oem = (OpenJPAEntityManager)em.getDelegate();
        JDBCFetchPlan fPlan = (JDBCFetchPlan) oem.getFetchPlan();
        JDBCFetchConfigurationImpl fConfig = (JDBCFetchConfigurationImpl)
            ((EntityManagerImpl) oem).getBroker().getFetchConfiguration();

        lockTimeoutHintTest(fPlan, fConfig, hintName, "-1", -1);
        lockTimeoutHintTest(fPlan, fConfig, hintName, -1, -1);
        lockTimeoutHintTest(fPlan, fConfig, hintName, "0", 0);
        lockTimeoutHintTest(fPlan, fConfig, hintName, 0, 0);
        lockTimeoutHintTest(fPlan, fConfig, hintName, "1500", 1500);
        lockTimeoutHintTest(fPlan, fConfig, hintName, 1500, 1500);

        try {
            fPlan.setHint(hintName, "xxxxx");
            fPlan.setHint(hintName, "yyyyy");
            fail("Expecting a a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, -2);
            fPlan.setHint(hintName, -3);
            fail("Expecting a a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, FetchConfiguration.DEFAULT);
            int defTimeout = fConfig.getContext().getConfiguration()
                .getLockTimeout();
            assertEquals(defTimeout, fPlan.getLockTimeout());
            assertEquals(defTimeout, fConfig.getLockTimeout());
        } catch (Exception e) {
            fail("Unexpected " + e.getClass().getName());
        }
        em.close();
    }

    /* Good
     * Test "javax.persistence.lock.timeout" hint
     */
    public void testJavaxLockTimeoutHint() {
        String hintName = "javax.persistence.lock.timeout";

        EntityManager em = emf.createEntityManager();
        OpenJPAEntityManager oem = (OpenJPAEntityManager)em.getDelegate();
        JDBCFetchPlan fPlan = (JDBCFetchPlan) oem.getFetchPlan();
        JDBCFetchConfigurationImpl fConfig = (JDBCFetchConfigurationImpl)
            ((EntityManagerImpl) oem).getBroker().getFetchConfiguration();

        lockTimeoutHintTest(fPlan, fConfig, hintName, "-1", -1);
        lockTimeoutHintTest(fPlan, fConfig, hintName, -1, -1);
        lockTimeoutHintTest(fPlan, fConfig, hintName, "0", 0);
        lockTimeoutHintTest(fPlan, fConfig, hintName, 0, 0);
        lockTimeoutHintTest(fPlan, fConfig, hintName, "2000", 2000);
        lockTimeoutHintTest(fPlan, fConfig, hintName, 2000, 2000);

        try {
            fPlan.setHint(hintName, "xxxxx");
            fPlan.setHint(hintName, "yyyyy");
            fail("Expecting a a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, -2);
            fPlan.setHint(hintName, -3);
            fail("Expecting a a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, FetchConfiguration.DEFAULT);
            int defTimeout = fConfig.getContext().getConfiguration()
                .getLockTimeout();
            assertEquals(defTimeout, fPlan.getLockTimeout());
            assertEquals(defTimeout, fConfig.getLockTimeout());
        } catch (Exception e) {
            fail("Unexpected " + e.getClass().getName());
        }
        em.close();
    }

    private void lockTimeoutHintTest(JDBCFetchPlan fPlan,
        JDBCFetchConfigurationImpl fConfig, String hint, Object value,
        int expected) {
        fPlan.setHint(hint, value);
        Object getValue = fPlan.getHint(hint);
        assertEquals(value.getClass(), getValue.getClass());
        assertEquals(value, getValue);
        assertEquals(expected, fPlan.getLockTimeout());
        assertEquals(expected, fConfig.getLockTimeout());
    }

    /* Good
     * Test "openjpa.QueryTimeout" hint
     */
    public void testQueryTimeoutHint() {
        String hintName = "openjpa.QueryTimeout";

        EntityManager em = emf.createEntityManager();
        OpenJPAEntityManager oem = (OpenJPAEntityManager)em.getDelegate();
        JDBCFetchPlan fPlan = (JDBCFetchPlan) oem.getFetchPlan();
        JDBCFetchConfigurationImpl fConfig = (JDBCFetchConfigurationImpl)
            ((EntityManagerImpl) oem).getBroker().getFetchConfiguration();

        queryTimeoutHintTest(fPlan, fConfig, hintName, "-1", -1);
        queryTimeoutHintTest(fPlan, fConfig, hintName, -1, -1);
        queryTimeoutHintTest(fPlan, fConfig, hintName, "0", 0);
        queryTimeoutHintTest(fPlan, fConfig, hintName, 0, 0);
        queryTimeoutHintTest(fPlan, fConfig, hintName, "100", 100);
        queryTimeoutHintTest(fPlan, fConfig, hintName, 100, 100);

        try {
            fPlan.setHint(hintName, "xxxxx");
            fPlan.setHint(hintName, "yyyyy");
            fail("Expecting a a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, -2);
            fPlan.setHint(hintName, -3);
            fail("Expecting a a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, FetchConfiguration.DEFAULT);
            int defTimeout = fConfig.getContext().getConfiguration()
                .getQueryTimeout();
            assertEquals(defTimeout, fPlan.getQueryTimeout());
            assertEquals(defTimeout, fConfig.getQueryTimeout());
        } catch (Exception e) {
            fail("Unexpected " + e.getClass().getName());
        }
        em.close();
    }

    /* Good
     * Test "openjpa.FetchPlan.QueryTimeout" hint
     */
    public void testFetchPlanQueryTimeoutHint() {
        String hintName = "openjpa.FetchPlan.QueryTimeout";

        EntityManager em = emf.createEntityManager();
        OpenJPAEntityManager oem = (OpenJPAEntityManager)em.getDelegate();
        JDBCFetchPlan fPlan = (JDBCFetchPlan) oem.getFetchPlan();
        JDBCFetchConfigurationImpl fConfig = (JDBCFetchConfigurationImpl)
            ((EntityManagerImpl) oem).getBroker().getFetchConfiguration();

        queryTimeoutHintTest(fPlan, fConfig, hintName, "-1", -1);
        queryTimeoutHintTest(fPlan, fConfig, hintName, -1, -1);
        queryTimeoutHintTest(fPlan, fConfig, hintName, "0", 0);
        queryTimeoutHintTest(fPlan, fConfig, hintName, 0, 0);
        queryTimeoutHintTest(fPlan, fConfig, hintName, "1500", 1500);
        queryTimeoutHintTest(fPlan, fConfig, hintName, 1500, 1500);

        try {
            fPlan.setHint(hintName, "xxxxx");
            fPlan.setHint(hintName, "yyyyy");
            fail("Expecting a a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, -2);
            fPlan.setHint(hintName, -3);
            fail("Expecting a a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, FetchConfiguration.DEFAULT);
            int defTimeout = fConfig.getContext().getConfiguration()
                .getQueryTimeout();
            assertEquals(defTimeout, fPlan.getQueryTimeout());
            assertEquals(defTimeout, fConfig.getQueryTimeout());
        } catch (Exception e) {
            fail("Unexpected " + e.getClass().getName());
        }
        em.close();
    }

    /* Good
     * Test "javax.persistence.query.timeout" hint
     */
    public void testJavaxQueryTimeoutHint() {
        String hintName = "javax.persistence.query.timeout";

        EntityManager em = emf.createEntityManager();
        OpenJPAEntityManager oem = (OpenJPAEntityManager)em.getDelegate();
        JDBCFetchPlan fPlan = (JDBCFetchPlan) oem.getFetchPlan();
        JDBCFetchConfigurationImpl fConfig = (JDBCFetchConfigurationImpl)
            ((EntityManagerImpl) oem).getBroker().getFetchConfiguration();

        queryTimeoutHintTest(fPlan, fConfig, hintName, "-1", -1);
        queryTimeoutHintTest(fPlan, fConfig, hintName, -1, -1);
        queryTimeoutHintTest(fPlan, fConfig, hintName, "0", 0);
        queryTimeoutHintTest(fPlan, fConfig, hintName, 0, 0);
        queryTimeoutHintTest(fPlan, fConfig, hintName, "2000", 2000);
        queryTimeoutHintTest(fPlan, fConfig, hintName, 2000, 2000);

        try {
            fPlan.setHint(hintName, "xxxxx");
            fPlan.setHint(hintName, "yyyyy");
            fail("Expecting a a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, -2);
            fPlan.setHint(hintName, -3);
            fail("Expecting a a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, FetchConfiguration.DEFAULT);
            int defTimeout = fConfig.getContext().getConfiguration()
                .getQueryTimeout();
            assertEquals(defTimeout, fPlan.getQueryTimeout());
            assertEquals(defTimeout, fConfig.getQueryTimeout());
        } catch (Exception e) {
            fail("Unexpected " + e.getClass().getName());
        }
        em.close();
    }

    private void queryTimeoutHintTest(JDBCFetchPlan fPlan,
        JDBCFetchConfigurationImpl fConfig, String hint, Object value,
        int expected) {
        fPlan.setHint(hint, value);
        Object getValue = fPlan.getHint(hint);
        assertEquals(value.getClass(), getValue.getClass());
        assertEquals(value, getValue);
        assertEquals(expected, fPlan.getQueryTimeout());
        assertEquals(expected, fConfig.getQueryTimeout());
    }

    /* Good
     * Test "openjpa.FetchPlan.ResultSetType" hint
     */
    public void testFetchPlanResultSetTypeHint() {
        String hintName = "openjpa.FetchPlan.ResultSetType";

        EntityManager em = emf.createEntityManager();
        OpenJPAEntityManager oem = (OpenJPAEntityManager)em.getDelegate();
        JDBCFetchPlan fPlan = (JDBCFetchPlan) oem.getFetchPlan();
        JDBCFetchConfigurationImpl fConfig = (JDBCFetchConfigurationImpl)
            ((EntityManagerImpl) oem).getBroker().getFetchConfiguration();

        resultSetTypeHintTest(fPlan, fConfig, hintName,
            ResultSetType.FORWARD_ONLY.name(), ResultSetType.FORWARD_ONLY,
            ResultSet.TYPE_FORWARD_ONLY);
        resultSetTypeHintTest(fPlan, fConfig, hintName,
            ResultSetType.FORWARD_ONLY, ResultSetType.FORWARD_ONLY,
            ResultSet.TYPE_FORWARD_ONLY);

        resultSetTypeHintTest(fPlan, fConfig, hintName,
            ResultSetType.SCROLL_SENSITIVE.name(),
            ResultSetType.SCROLL_SENSITIVE, ResultSet.TYPE_SCROLL_SENSITIVE);
        resultSetTypeHintTest(fPlan, fConfig, hintName,
            ResultSetType.SCROLL_SENSITIVE, ResultSetType.SCROLL_SENSITIVE,
            ResultSet.TYPE_SCROLL_SENSITIVE);

        resultSetTypeHintTest(fPlan, fConfig, hintName,
            ResultSetType.SCROLL_INSENSITIVE.name(),
            ResultSetType.SCROLL_INSENSITIVE,
            ResultSet.TYPE_SCROLL_INSENSITIVE);
        resultSetTypeHintTest(fPlan, fConfig, hintName,
            ResultSetType.SCROLL_INSENSITIVE, ResultSetType.SCROLL_INSENSITIVE,
            ResultSet.TYPE_SCROLL_INSENSITIVE);

        try {
            fPlan.setHint(hintName, "xxxxx");
            fPlan.setHint(hintName, "yyyyy");
            fail("Expecting a a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, "12345");
            fPlan.setHint(hintName, "67890");
            fail("Expecting a a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, -1);
            fPlan.setHint(hintName, -2);
            fail("Expecting a a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught unexpected exception " + e,
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        em.close();
    }

    /* Good
     * Test "openjpa.jdbc.ResultSetType" hint
     */
    public void testJdbcResultSetTypeHint() {
        String hintName = "openjpa.jdbc.ResultSetType";

        EntityManager em = emf.createEntityManager();
        OpenJPAEntityManager oem = (OpenJPAEntityManager) em.getDelegate();
        JDBCFetchPlan fPlan = (JDBCFetchPlan) oem.getFetchPlan();
        JDBCFetchConfigurationImpl fConfig = (JDBCFetchConfigurationImpl)
            ((EntityManagerImpl) oem).getBroker().getFetchConfiguration();

        resultSetTypeHintTest(fPlan, fConfig, hintName, String
            .valueOf(ResultSet.TYPE_FORWARD_ONLY), ResultSetType.FORWARD_ONLY,
            ResultSet.TYPE_FORWARD_ONLY);
        resultSetTypeHintTest(fPlan, fConfig, hintName,
            ResultSet.TYPE_FORWARD_ONLY, ResultSetType.FORWARD_ONLY,
            ResultSet.TYPE_FORWARD_ONLY);

        resultSetTypeHintTest(fPlan, fConfig, hintName, String
            .valueOf(ResultSet.TYPE_SCROLL_SENSITIVE),
            ResultSetType.SCROLL_SENSITIVE, ResultSet.TYPE_SCROLL_SENSITIVE);
        resultSetTypeHintTest(fPlan, fConfig, hintName,
            ResultSet.TYPE_SCROLL_SENSITIVE, ResultSetType.SCROLL_SENSITIVE,
            ResultSet.TYPE_SCROLL_SENSITIVE);

        resultSetTypeHintTest(fPlan, fConfig, hintName, String
            .valueOf(ResultSet.TYPE_SCROLL_INSENSITIVE),
            ResultSetType.SCROLL_INSENSITIVE,
            ResultSet.TYPE_SCROLL_INSENSITIVE);
        resultSetTypeHintTest(fPlan, fConfig, hintName,
            ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSetType.SCROLL_INSENSITIVE,
            ResultSet.TYPE_SCROLL_INSENSITIVE);

        try {
            fPlan.setHint(hintName, "xxxxx");
            fPlan.setHint(hintName, "yyyyy");
            fail("Expecting a a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, "12345");
            fPlan.setHint(hintName, "67890");
            fail("Expecting a a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, -1);
            fPlan.setHint(hintName, -2);
            fail("Expecting a a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, FetchConfiguration.DEFAULT);
            assertEquals(ResultSet.TYPE_FORWARD_ONLY, fConfig
                .getResultSetType());
        } catch (Exception e) {
            fail("Unexpected " + e.getClass().getName());
        }
        em.close();
    }

    private void resultSetTypeHintTest(JDBCFetchPlan fPlan,
        JDBCFetchConfigurationImpl fConfig, String hint, Object value,
        Object expectedValue, int expected) {
        fPlan.setHint(hint, value);
        Object getValue = fPlan.getHint(hint);
        assertEquals(value.getClass(), getValue.getClass());
        assertEquals(value, getValue);
        assertEquals(expectedValue, fPlan.getResultSetType());
        assertEquals(expected, fConfig.getResultSetType());
    }

    /* Good
     * Test "openjpa.FetchPlan.SubclassFetchMode" hint
     */
    public void testFetchPlanSubclassFetchModeHint() {
        String hintName = "openjpa.FetchPlan.SubclassFetchMode";

        EntityManager em = emf.createEntityManager();
        OpenJPAEntityManager oem = (OpenJPAEntityManager)em.getDelegate();
        JDBCFetchPlan fPlan = (JDBCFetchPlan) oem.getFetchPlan();
        JDBCFetchConfigurationImpl fConfig = (JDBCFetchConfigurationImpl)
            ((EntityManagerImpl) oem).getBroker().getFetchConfiguration();

        subclassFetchModeHintTest(fPlan, fConfig, hintName, "none",
            FetchMode.NONE, EagerFetchModes.EAGER_NONE);
        subclassFetchModeHintTest(fPlan, fConfig, hintName, FetchMode.NONE
            .name(), FetchMode.NONE, EagerFetchModes.EAGER_NONE);
        subclassFetchModeHintTest(fPlan, fConfig, hintName, FetchMode.NONE,
            FetchMode.NONE, EagerFetchModes.EAGER_NONE);

        subclassFetchModeHintTest(fPlan, fConfig, hintName, "parallel",
            FetchMode.PARALLEL, EagerFetchModes.EAGER_PARALLEL);
        subclassFetchModeHintTest(fPlan, fConfig, hintName, FetchMode.PARALLEL
            .name(), FetchMode.PARALLEL, EagerFetchModes.EAGER_PARALLEL);
        subclassFetchModeHintTest(fPlan, fConfig, hintName, FetchMode.PARALLEL,
            FetchMode.PARALLEL, EagerFetchModes.EAGER_PARALLEL);

        subclassFetchModeHintTest(fPlan, fConfig, hintName, "join",
            FetchMode.JOIN, EagerFetchModes.EAGER_JOIN);
        subclassFetchModeHintTest(fPlan, fConfig, hintName, FetchMode.JOIN
            .name(), FetchMode.JOIN, EagerFetchModes.EAGER_JOIN);
        subclassFetchModeHintTest(fPlan, fConfig, hintName, FetchMode.JOIN,
            FetchMode.JOIN, EagerFetchModes.EAGER_JOIN);

        try {
            fPlan.setHint(hintName, "xxxxx");
            fPlan.setHint(hintName, "yyyyy");
            fail("Expecting a a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, "12345");
            fPlan.setHint(hintName, "67890");
            fail("Expecting a a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, -1);
            fPlan.setHint(hintName, -2);
            fail("Expecting a a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        em.close();
    }

    /* Good
     * Test "openjpa.jdbc.SubclassFetchMode" hint
     */
    public void testJdbcSubclassFetchModeHint() {
        String hintName = "openjpa.jdbc.SubclassFetchMode";

        EntityManager em = emf.createEntityManager();
        OpenJPAEntityManager oem = (OpenJPAEntityManager) em.getDelegate();
        JDBCFetchPlan fPlan = (JDBCFetchPlan) oem.getFetchPlan();
        JDBCFetchConfigurationImpl fConfig = (JDBCFetchConfigurationImpl)
            ((EntityManagerImpl) oem).getBroker().getFetchConfiguration();

        subclassFetchModeHintTest(fPlan, fConfig, hintName, String
            .valueOf(EagerFetchModes.EAGER_NONE), FetchMode.NONE,
            EagerFetchModes.EAGER_NONE);
        subclassFetchModeHintTest(fPlan, fConfig, hintName,
            EagerFetchModes.EAGER_NONE, FetchMode.NONE,
            EagerFetchModes.EAGER_NONE);

        subclassFetchModeHintTest(fPlan, fConfig, hintName, String
            .valueOf(EagerFetchModes.EAGER_PARALLEL), FetchMode.PARALLEL,
            EagerFetchModes.EAGER_PARALLEL);
        subclassFetchModeHintTest(fPlan, fConfig, hintName,
            EagerFetchModes.EAGER_PARALLEL, FetchMode.PARALLEL,
            EagerFetchModes.EAGER_PARALLEL);

        subclassFetchModeHintTest(fPlan, fConfig, hintName, String
            .valueOf(EagerFetchModes.EAGER_JOIN), FetchMode.JOIN,
            EagerFetchModes.EAGER_JOIN);
        subclassFetchModeHintTest(fPlan, fConfig, hintName,
            EagerFetchModes.EAGER_JOIN, FetchMode.JOIN,
            EagerFetchModes.EAGER_JOIN);

        try {
            fPlan.setHint(hintName, "xxxxx");
            fPlan.setHint(hintName, "yyyyy");
            fail("Expecting a a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, "12345");
            fPlan.setHint(hintName, "67890");
            fail("Expecting a a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, -1);
            fPlan.setHint(hintName, -2);
            fail("Expecting a a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, FetchConfiguration.DEFAULT);
            assertEquals(EagerFetchModes.EAGER_JOIN, fConfig
                .getSubclassFetchMode());
        } catch (Exception e) {
            fail("Unexpected " + e.getClass().getName());
        }
        em.close();
    }

    private void subclassFetchModeHintTest(JDBCFetchPlan fPlan,
        JDBCFetchConfigurationImpl fConfig, String hint, Object value,
        Object expectedValue, int expected) {
        fPlan.setHint(hint, value);
        Object getValue = fPlan.getHint(hint);
        assertEquals(value.getClass(), getValue.getClass());
        assertEquals(value, getValue);
        assertEquals(expectedValue, fPlan.getSubclassFetchMode());
        assertEquals(expected, fConfig.getSubclassFetchMode());
    }

    /* Good
     * Test "openjpa.FlushBeforeQueries" hint
     */
    public void testFlushBeforeQueriesHint() {
        String hintName = "openjpa.FlushBeforeQueries";

        EntityManager em = emf.createEntityManager();
        OpenJPAEntityManager oem = (OpenJPAEntityManager) em.getDelegate();
        JDBCFetchPlan fPlan = (JDBCFetchPlan) oem.getFetchPlan();
        JDBCFetchConfigurationImpl fConfig = (JDBCFetchConfigurationImpl)
            ((EntityManagerImpl) oem).getBroker().getFetchConfiguration();

        flushBeforeQueriesHintTest(fPlan, fConfig, hintName, String
            .valueOf(QueryFlushModes.FLUSH_TRUE), QueryFlushModes.FLUSH_TRUE);
        flushBeforeQueriesHintTest(fPlan, fConfig, hintName,
            QueryFlushModes.FLUSH_TRUE, QueryFlushModes.FLUSH_TRUE);

        flushBeforeQueriesHintTest(fPlan, fConfig, hintName, String
            .valueOf(QueryFlushModes.FLUSH_FALSE), QueryFlushModes.FLUSH_FALSE);
        flushBeforeQueriesHintTest(fPlan, fConfig, hintName,
            QueryFlushModes.FLUSH_FALSE, QueryFlushModes.FLUSH_FALSE);

        flushBeforeQueriesHintTest(fPlan, fConfig, hintName, String
            .valueOf(QueryFlushModes.FLUSH_WITH_CONNECTION),
            QueryFlushModes.FLUSH_WITH_CONNECTION);
        flushBeforeQueriesHintTest(fPlan, fConfig, hintName,
            QueryFlushModes.FLUSH_WITH_CONNECTION,
            QueryFlushModes.FLUSH_WITH_CONNECTION);

        try {
            fPlan.setHint(hintName, "xxxxx");
            fPlan.setHint(hintName, "yyyyy");
            fail("Expecting a a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, "12345");
            fPlan.setHint(hintName, "67890");
            fail("Expecting a a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, -1);
            fPlan.setHint(hintName, -2);
            fail("Expecting a a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, FetchConfiguration.DEFAULT);
            fConfig.getFlushBeforeQueries();
            assertEquals(QueryFlushModes.FLUSH_TRUE, fConfig
                .getFlushBeforeQueries());
        } catch (Exception e) {
            fail("Unexpected " + e.getClass().getName());
        }
        em.close();
    }

    private void flushBeforeQueriesHintTest(JDBCFetchPlan fPlan,
        JDBCFetchConfigurationImpl fConfig, String hint, Object value,
        int expected) {
        fPlan.setHint(hint, value);
        Object getValue = fPlan.getHint(hint);
        assertEquals(value.getClass(), getValue.getClass());
        assertEquals(value, getValue);
        assertEquals(expected, fConfig.getFlushBeforeQueries());
    }

    /* Good
     * Test "openjpa.ReadLockLevel" hint
     */
    public void testReadLockLevelHint() {
        String hintName = "openjpa.ReadLockLevel";

        EntityManager em = emf.createEntityManager();
        OpenJPAEntityManager oem = (OpenJPAEntityManager)em.getDelegate();
        JDBCFetchPlan fPlan = (JDBCFetchPlan) oem.getFetchPlan();
        JDBCFetchConfigurationImpl fConfig = (JDBCFetchConfigurationImpl)
            ((EntityManagerImpl) oem).getBroker().getFetchConfiguration();
        em.getTransaction().begin();

        readLockLevelHintTest(fPlan, fConfig, hintName, String
            .valueOf(MixedLockLevels.LOCK_NONE), LockModeType.NONE,
            MixedLockLevels.LOCK_NONE);
        readLockLevelHintTest(fPlan, fConfig, hintName,
            MixedLockLevels.LOCK_NONE, LockModeType.NONE,
            MixedLockLevels.LOCK_NONE);

        readLockLevelHintTest(fPlan, fConfig, hintName, String
            .valueOf(MixedLockLevels.LOCK_READ), LockModeType.READ,
            MixedLockLevels.LOCK_READ);
        readLockLevelHintTest(fPlan, fConfig, hintName,
            MixedLockLevels.LOCK_READ, LockModeType.READ,
            MixedLockLevels.LOCK_READ);

        readLockLevelHintTest(fPlan, fConfig, hintName, String
            .valueOf(MixedLockLevels.LOCK_WRITE), LockModeType.WRITE,
            MixedLockLevels.LOCK_WRITE);
        readLockLevelHintTest(fPlan, fConfig, hintName,
            MixedLockLevels.LOCK_WRITE, LockModeType.WRITE,
            MixedLockLevels.LOCK_WRITE);

        readLockLevelHintTest(fPlan, fConfig, hintName, String
            .valueOf(MixedLockLevels.LOCK_OPTIMISTIC), LockModeType.OPTIMISTIC,
            MixedLockLevels.LOCK_OPTIMISTIC);
        readLockLevelHintTest(fPlan, fConfig, hintName,
            MixedLockLevels.LOCK_OPTIMISTIC, LockModeType.OPTIMISTIC,
            MixedLockLevels.LOCK_OPTIMISTIC);

        readLockLevelHintTest(fPlan, fConfig, hintName, String
            .valueOf(MixedLockLevels.LOCK_OPTIMISTIC_FORCE_INCREMENT),
            LockModeType.OPTIMISTIC_FORCE_INCREMENT,
            MixedLockLevels.LOCK_OPTIMISTIC_FORCE_INCREMENT);
        readLockLevelHintTest(fPlan, fConfig, hintName,
            MixedLockLevels.LOCK_OPTIMISTIC_FORCE_INCREMENT,
            LockModeType.OPTIMISTIC_FORCE_INCREMENT,
            MixedLockLevels.LOCK_OPTIMISTIC_FORCE_INCREMENT);

        readLockLevelHintTest(fPlan, fConfig, hintName,
            String.valueOf(MixedLockLevels.LOCK_PESSIMISTIC_READ),
            LockModeType.PESSIMISTIC_READ,
            MixedLockLevels.LOCK_PESSIMISTIC_READ);
        readLockLevelHintTest(fPlan, fConfig, hintName,
            MixedLockLevels.LOCK_PESSIMISTIC_READ,
            LockModeType.PESSIMISTIC_READ,
            MixedLockLevels.LOCK_PESSIMISTIC_READ);

        readLockLevelHintTest(fPlan, fConfig, hintName,
            String.valueOf(MixedLockLevels.LOCK_PESSIMISTIC_WRITE),
            LockModeType.PESSIMISTIC_WRITE,
            MixedLockLevels.LOCK_PESSIMISTIC_WRITE);
        readLockLevelHintTest(fPlan, fConfig, hintName,
            MixedLockLevels.LOCK_PESSIMISTIC_WRITE,
            LockModeType.PESSIMISTIC_WRITE,
            MixedLockLevels.LOCK_PESSIMISTIC_WRITE);

        readLockLevelHintTest(fPlan, fConfig, hintName, String
            .valueOf(MixedLockLevels.LOCK_PESSIMISTIC_FORCE_INCREMENT),
            LockModeType.PESSIMISTIC_FORCE_INCREMENT,
            MixedLockLevels.LOCK_PESSIMISTIC_FORCE_INCREMENT);
        readLockLevelHintTest(fPlan, fConfig, hintName,
            MixedLockLevels.LOCK_PESSIMISTIC_FORCE_INCREMENT,
            LockModeType.PESSIMISTIC_FORCE_INCREMENT,
            MixedLockLevels.LOCK_PESSIMISTIC_FORCE_INCREMENT);

        try {
            fPlan.setHint(hintName, "xxxxx");
            fPlan.setHint(hintName, "yyyyy");
            fail("Expecting a a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, "12345");
            fPlan.setHint(hintName, "67890");
            fail("Expecting a a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, -1);
            fPlan.setHint(hintName, -2);
            fail("Expecting a a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, FetchConfiguration.DEFAULT);
            assertEquals(MixedLockLevels.LOCK_READ, fConfig.getReadLockLevel());
        } catch (Exception e) {
            fail("Unexpected " + e.getClass().getName());
        }
        em.getTransaction().rollback();
        em.close();
    }

    private void readLockLevelHintTest(JDBCFetchPlan fPlan,
        JDBCFetchConfigurationImpl fConfig, String hint, Object value,
        Object expectedMode, int expectedLevel) {
        fPlan.setHint(hint, value);
        Object getValue = fPlan.getHint(hint);
        assertEquals(value.getClass(), getValue.getClass());
        assertEquals(value, getValue);
        assertEquals(expectedMode, fPlan.getReadLockMode());
        assertEquals(expectedLevel, fConfig.getReadLockLevel());
    }

    /* Good
     * Test "openjpa.WriteLockLevel" hint
     */
    public void testWriteLockLevelHint() {
        String hintName = "openjpa.WriteLockLevel";

        EntityManager em = emf.createEntityManager();
        OpenJPAEntityManager oem = (OpenJPAEntityManager)em.getDelegate();
        JDBCFetchPlan fPlan = (JDBCFetchPlan) oem.getFetchPlan();
        JDBCFetchConfigurationImpl fConfig = (JDBCFetchConfigurationImpl)
            ((EntityManagerImpl) oem).getBroker().getFetchConfiguration();
        em.getTransaction().begin();

        writeLockLevelHintTest(fPlan, fConfig, hintName, String
            .valueOf(MixedLockLevels.LOCK_NONE), LockModeType.NONE,
            MixedLockLevels.LOCK_NONE);
        writeLockLevelHintTest(fPlan, fConfig, hintName,
            MixedLockLevels.LOCK_NONE, LockModeType.NONE,
            MixedLockLevels.LOCK_NONE);

        writeLockLevelHintTest(fPlan, fConfig, hintName, String
            .valueOf(MixedLockLevels.LOCK_READ), LockModeType.READ,
            MixedLockLevels.LOCK_READ);
        writeLockLevelHintTest(fPlan, fConfig, hintName,
            MixedLockLevels.LOCK_READ, LockModeType.READ,
            MixedLockLevels.LOCK_READ);

        writeLockLevelHintTest(fPlan, fConfig, hintName, String
            .valueOf(MixedLockLevels.LOCK_WRITE), LockModeType.WRITE,
            MixedLockLevels.LOCK_WRITE);
        writeLockLevelHintTest(fPlan, fConfig, hintName,
            MixedLockLevels.LOCK_WRITE, LockModeType.WRITE,
            MixedLockLevels.LOCK_WRITE);

        writeLockLevelHintTest(fPlan, fConfig, hintName, String
            .valueOf(MixedLockLevels.LOCK_OPTIMISTIC), LockModeType.OPTIMISTIC,
            MixedLockLevels.LOCK_OPTIMISTIC);
        writeLockLevelHintTest(fPlan, fConfig, hintName,
            MixedLockLevels.LOCK_OPTIMISTIC, LockModeType.OPTIMISTIC,
            MixedLockLevels.LOCK_OPTIMISTIC);

        writeLockLevelHintTest(fPlan, fConfig, hintName, String
            .valueOf(MixedLockLevels.LOCK_OPTIMISTIC_FORCE_INCREMENT),
            LockModeType.OPTIMISTIC_FORCE_INCREMENT,
            MixedLockLevels.LOCK_OPTIMISTIC_FORCE_INCREMENT);
        writeLockLevelHintTest(fPlan, fConfig, hintName,
            MixedLockLevels.LOCK_OPTIMISTIC_FORCE_INCREMENT,
            LockModeType.OPTIMISTIC_FORCE_INCREMENT,
            MixedLockLevels.LOCK_OPTIMISTIC_FORCE_INCREMENT);

        writeLockLevelHintTest(fPlan, fConfig, hintName,
            String.valueOf(MixedLockLevels.LOCK_PESSIMISTIC_READ),
            LockModeType.PESSIMISTIC_READ,
            MixedLockLevels.LOCK_PESSIMISTIC_READ);
        writeLockLevelHintTest(fPlan, fConfig, hintName,
            MixedLockLevels.LOCK_PESSIMISTIC_READ,
            LockModeType.PESSIMISTIC_READ,
            MixedLockLevels.LOCK_PESSIMISTIC_READ);

        writeLockLevelHintTest(fPlan, fConfig, hintName,
            String.valueOf(MixedLockLevels.LOCK_PESSIMISTIC_WRITE),
            LockModeType.PESSIMISTIC_WRITE,
            MixedLockLevels.LOCK_PESSIMISTIC_WRITE);
        writeLockLevelHintTest(fPlan, fConfig, hintName,
            MixedLockLevels.LOCK_PESSIMISTIC_WRITE,
            LockModeType.PESSIMISTIC_WRITE,
            MixedLockLevels.LOCK_PESSIMISTIC_WRITE);

        writeLockLevelHintTest(fPlan, fConfig, hintName, String
            .valueOf(MixedLockLevels.LOCK_PESSIMISTIC_FORCE_INCREMENT),
            LockModeType.PESSIMISTIC_FORCE_INCREMENT,
            MixedLockLevels.LOCK_PESSIMISTIC_FORCE_INCREMENT);
        writeLockLevelHintTest(fPlan, fConfig, hintName,
            MixedLockLevels.LOCK_PESSIMISTIC_FORCE_INCREMENT,
            LockModeType.PESSIMISTIC_FORCE_INCREMENT,
            MixedLockLevels.LOCK_PESSIMISTIC_FORCE_INCREMENT);

        try {
            fPlan.setHint(hintName, "xxxxx");
            fPlan.setHint(hintName, "yyyyy");
            fail("Expecting a a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, "12345");
            fPlan.setHint(hintName, "67890");
            fail("Expecting a a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, -1);
            fPlan.setHint(hintName, -2);
            fail("Expecting a a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, FetchConfiguration.DEFAULT);
            assertEquals(MixedLockLevels.LOCK_WRITE, fConfig
                .getWriteLockLevel());
        } catch (Exception e) {
            fail("Unexpected " + e.getClass().getName());
        }
        em.getTransaction().rollback();
        em.close();
    }

    private void writeLockLevelHintTest(JDBCFetchPlan fPlan,
        JDBCFetchConfigurationImpl fConfig, String hint, Object value,
        Object expectedMode, int expectedLevel) {
        fPlan.setHint(hint, value);
        Object getValue = fPlan.getHint(hint);
        assertEquals(value.getClass(), getValue.getClass());
        assertEquals(value, getValue);
        assertEquals(expectedMode, fPlan.getWriteLockMode());
        assertEquals(expectedLevel, fConfig.getWriteLockLevel());
    }

    /* Good
     * Test "openjpa.FetchPlan.ReadLockMode" hint
     */
    public void testFetchPlanReadLockModeHintInTx() {
        fetchPlanReadLockModeHint(true);
    }

    public void testFetchPlanReadLockModeHintNotInTx() {
        fetchPlanReadLockModeHint(false);
    }

    private void fetchPlanReadLockModeHint(boolean inTransaction) {
        String hintName = "openjpa.FetchPlan.ReadLockMode";

        EntityManager em = emf.createEntityManager();
        OpenJPAEntityManager oem = (OpenJPAEntityManager) em.getDelegate();
        JDBCFetchPlan fPlan = (JDBCFetchPlan) oem.getFetchPlan();
        JDBCFetchConfigurationImpl fConfig = (JDBCFetchConfigurationImpl)
            ((EntityManagerImpl) oem).getBroker().getFetchConfiguration();
        if(inTransaction)
            em.getTransaction().begin();

        readLockModeHintTest(fPlan, fConfig, hintName, "none",
            LockModeType.NONE, MixedLockLevels.LOCK_NONE);
        readLockModeHintTest(fPlan, fConfig, hintName, LockModeType.NONE,
            LockModeType.NONE, MixedLockLevels.LOCK_NONE);
        readLockModeHintTest(fPlan, fConfig, hintName,
            LockModeType.NONE.name(), LockModeType.NONE,
            MixedLockLevels.LOCK_NONE);

        readLockModeHintTest(fPlan, fConfig, hintName, "read",
            LockModeType.READ, MixedLockLevels.LOCK_READ);
        readLockModeHintTest(fPlan, fConfig, hintName, LockModeType.READ,
            LockModeType.READ, MixedLockLevels.LOCK_READ);
        readLockModeHintTest(fPlan, fConfig, hintName,
            LockModeType.READ.name(), LockModeType.READ,
            MixedLockLevels.LOCK_READ);

        readLockModeHintTest(fPlan, fConfig, hintName, "write",
            LockModeType.WRITE, MixedLockLevels.LOCK_WRITE);
        readLockModeHintTest(fPlan, fConfig, hintName, LockModeType.WRITE,
            LockModeType.WRITE, MixedLockLevels.LOCK_WRITE);
        readLockModeHintTest(fPlan, fConfig, hintName, LockModeType.WRITE
            .name(), LockModeType.WRITE, MixedLockLevels.LOCK_WRITE);

        readLockModeHintTest(fPlan, fConfig, hintName, "optimistic",
            LockModeType.OPTIMISTIC, MixedLockLevels.LOCK_OPTIMISTIC);
        readLockModeHintTest(fPlan, fConfig, hintName, LockModeType.OPTIMISTIC,
            LockModeType.OPTIMISTIC, MixedLockLevels.LOCK_OPTIMISTIC);
        readLockModeHintTest(fPlan, fConfig, hintName, LockModeType.OPTIMISTIC
            .name(), LockModeType.OPTIMISTIC, MixedLockLevels.LOCK_OPTIMISTIC);

        readLockModeHintTest(fPlan, fConfig, hintName,
            "optimistic-force-increment",
            LockModeType.OPTIMISTIC_FORCE_INCREMENT,
            MixedLockLevels.LOCK_OPTIMISTIC_FORCE_INCREMENT);
        readLockModeHintTest(fPlan, fConfig, hintName,
            LockModeType.OPTIMISTIC_FORCE_INCREMENT,
            LockModeType.OPTIMISTIC_FORCE_INCREMENT,
            MixedLockLevels.LOCK_OPTIMISTIC_FORCE_INCREMENT);
        readLockModeHintTest(fPlan, fConfig, hintName,
            LockModeType.OPTIMISTIC_FORCE_INCREMENT.name(),
            LockModeType.OPTIMISTIC_FORCE_INCREMENT,
            MixedLockLevels.LOCK_OPTIMISTIC_FORCE_INCREMENT);

        readLockModeHintTest(fPlan, fConfig, hintName,
            "pessimistic-read", LockModeType.PESSIMISTIC_READ,
            MixedLockLevels.LOCK_PESSIMISTIC_READ);
        readLockModeHintTest(fPlan, fConfig, hintName,
            LockModeType.PESSIMISTIC_READ, LockModeType.PESSIMISTIC_READ,
            MixedLockLevels.LOCK_PESSIMISTIC_READ);
        readLockModeHintTest(fPlan, fConfig, hintName,
            LockModeType.PESSIMISTIC_READ.name(),
            LockModeType.PESSIMISTIC_READ,
            MixedLockLevels.LOCK_PESSIMISTIC_READ);

        readLockModeHintTest(fPlan, fConfig, hintName,
            "pessimistic-write", LockModeType.PESSIMISTIC_WRITE,
            MixedLockLevels.LOCK_PESSIMISTIC_WRITE);
        readLockModeHintTest(fPlan, fConfig, hintName,
            LockModeType.PESSIMISTIC_WRITE, LockModeType.PESSIMISTIC_WRITE,
            MixedLockLevels.LOCK_PESSIMISTIC_WRITE);
        readLockModeHintTest(fPlan, fConfig, hintName,
            LockModeType.PESSIMISTIC_WRITE.name(),
            LockModeType.PESSIMISTIC_WRITE,
            MixedLockLevels.LOCK_PESSIMISTIC_WRITE);

        readLockModeHintTest(fPlan, fConfig, hintName,
            "pessimistic-force-increment",
            LockModeType.PESSIMISTIC_FORCE_INCREMENT,
            MixedLockLevels.LOCK_PESSIMISTIC_FORCE_INCREMENT);
        readLockModeHintTest(fPlan, fConfig, hintName,
            LockModeType.PESSIMISTIC_FORCE_INCREMENT,
            LockModeType.PESSIMISTIC_FORCE_INCREMENT,
            MixedLockLevels.LOCK_PESSIMISTIC_FORCE_INCREMENT);
        readLockModeHintTest(fPlan, fConfig, hintName,
            LockModeType.PESSIMISTIC_FORCE_INCREMENT.name(),
            LockModeType.PESSIMISTIC_FORCE_INCREMENT,
            MixedLockLevels.LOCK_PESSIMISTIC_FORCE_INCREMENT);

        try {
            fPlan.setHint(hintName, "xxxxx");
            fPlan.setHint(hintName, "yyyyy");
            fail("Expecting a a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, "12345");
            fPlan.setHint(hintName, "67890");
            fail("Expecting a a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, -1);
            fPlan.setHint(hintName, -2);
            fail("Expecting a a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        if(inTransaction)
            em.getTransaction().rollback();
        em.close();
    }

    private void readLockModeHintTest(JDBCFetchPlan fPlan,
        JDBCFetchConfigurationImpl fConfig, String hint, Object value,
        Object expectedMode, int expectedLevel) {
        fPlan.setHint(hint, value);
        Object getValue = fPlan.getHint(hint);
        assertEquals(value.getClass(), getValue.getClass());
        assertEquals(value, getValue);
        assertEquals(expectedMode, fPlan.getReadLockMode());
        assertEquals(expectedLevel, fConfig.getReadLockLevel());
    }

    /* Good
     * Test "openjpa.FetchPlan.WriteLockMode" hint
     */
    public void testFetchPlanWriteLockModeHintInTx() {
        fetchPlanWriteLockModeHint(true);
    }

    public void testFetchPlanWriteLockModeHintNotInTx() {
        fetchPlanWriteLockModeHint(false);
    }

    private void fetchPlanWriteLockModeHint(boolean inTransaction) {
        String hintName = "openjpa.FetchPlan.WriteLockMode";

        EntityManager em = emf.createEntityManager();
        OpenJPAEntityManager oem = (OpenJPAEntityManager)em.getDelegate();
        JDBCFetchPlan fPlan = (JDBCFetchPlan) oem.getFetchPlan();
        JDBCFetchConfigurationImpl fConfig = (JDBCFetchConfigurationImpl)
            ((EntityManagerImpl) oem).getBroker().getFetchConfiguration();
        if(inTransaction)
            em.getTransaction().begin();

        writeLockModeHintTest(fPlan, fConfig, hintName, "none",
            LockModeType.NONE, MixedLockLevels.LOCK_NONE);
        writeLockModeHintTest(fPlan, fConfig, hintName, LockModeType.NONE,
            LockModeType.NONE, MixedLockLevels.LOCK_NONE);
        writeLockModeHintTest(fPlan, fConfig, hintName, LockModeType.NONE
            .name(), LockModeType.NONE, MixedLockLevels.LOCK_NONE);

        writeLockModeHintTest(fPlan, fConfig, hintName, "read",
            LockModeType.READ, MixedLockLevels.LOCK_READ);
        writeLockModeHintTest(fPlan, fConfig, hintName, LockModeType.READ,
            LockModeType.READ, MixedLockLevels.LOCK_READ);
        writeLockModeHintTest(fPlan, fConfig, hintName, LockModeType.READ
            .name(), LockModeType.READ, MixedLockLevels.LOCK_READ);

        writeLockModeHintTest(fPlan, fConfig, hintName, "write",
            LockModeType.WRITE, MixedLockLevels.LOCK_WRITE);
        writeLockModeHintTest(fPlan, fConfig, hintName, LockModeType.WRITE,
            LockModeType.WRITE, MixedLockLevels.LOCK_WRITE);
        writeLockModeHintTest(fPlan, fConfig, hintName, LockModeType.WRITE
            .name(), LockModeType.WRITE, MixedLockLevels.LOCK_WRITE);

        writeLockModeHintTest(fPlan, fConfig, hintName,
            "optimistic", LockModeType.OPTIMISTIC,
            MixedLockLevels.LOCK_OPTIMISTIC);
        writeLockModeHintTest(fPlan, fConfig, hintName,
            LockModeType.OPTIMISTIC, LockModeType.OPTIMISTIC,
            MixedLockLevels.LOCK_OPTIMISTIC);
        writeLockModeHintTest(fPlan, fConfig, hintName, LockModeType.OPTIMISTIC
            .name(), LockModeType.OPTIMISTIC, MixedLockLevels.LOCK_OPTIMISTIC);

        writeLockModeHintTest(fPlan, fConfig, hintName,
            "optimistic-force-increment",
            LockModeType.OPTIMISTIC_FORCE_INCREMENT,
            MixedLockLevels.LOCK_OPTIMISTIC_FORCE_INCREMENT);
        writeLockModeHintTest(fPlan, fConfig, hintName,
            LockModeType.OPTIMISTIC_FORCE_INCREMENT,
            LockModeType.OPTIMISTIC_FORCE_INCREMENT,
            MixedLockLevels.LOCK_OPTIMISTIC_FORCE_INCREMENT);
        writeLockModeHintTest(fPlan, fConfig, hintName,
            LockModeType.OPTIMISTIC_FORCE_INCREMENT.name(),
            LockModeType.OPTIMISTIC_FORCE_INCREMENT,
            MixedLockLevels.LOCK_OPTIMISTIC_FORCE_INCREMENT);

        writeLockModeHintTest(fPlan, fConfig, hintName,
            "pessimistic-read", LockModeType.PESSIMISTIC_READ,
            MixedLockLevels.LOCK_PESSIMISTIC_READ);
        writeLockModeHintTest(fPlan, fConfig, hintName,
            LockModeType.PESSIMISTIC_READ, LockModeType.PESSIMISTIC_READ,
            MixedLockLevels.LOCK_PESSIMISTIC_READ);
        writeLockModeHintTest(fPlan, fConfig, hintName,
            LockModeType.PESSIMISTIC_READ.name(),
            LockModeType.PESSIMISTIC_READ,
            MixedLockLevels.LOCK_PESSIMISTIC_READ);

        writeLockModeHintTest(fPlan, fConfig, hintName,
            "pessimistic-write", LockModeType.PESSIMISTIC_WRITE,
            MixedLockLevels.LOCK_PESSIMISTIC_WRITE);
        writeLockModeHintTest(fPlan, fConfig, hintName,
            LockModeType.PESSIMISTIC_WRITE, LockModeType.PESSIMISTIC_WRITE,
            MixedLockLevels.LOCK_PESSIMISTIC_WRITE);
        writeLockModeHintTest(fPlan, fConfig, hintName,
            LockModeType.PESSIMISTIC_WRITE.name(),
            LockModeType.PESSIMISTIC_WRITE,
            MixedLockLevels.LOCK_PESSIMISTIC_WRITE);

        writeLockModeHintTest(fPlan, fConfig, hintName,
            "pessimistic-force-increment",
            LockModeType.PESSIMISTIC_FORCE_INCREMENT,
            MixedLockLevels.LOCK_PESSIMISTIC_FORCE_INCREMENT);
        writeLockModeHintTest(fPlan, fConfig, hintName,
            LockModeType.PESSIMISTIC_FORCE_INCREMENT,
            LockModeType.PESSIMISTIC_FORCE_INCREMENT,
            MixedLockLevels.LOCK_PESSIMISTIC_FORCE_INCREMENT);
        writeLockModeHintTest(fPlan, fConfig, hintName,
            LockModeType.PESSIMISTIC_FORCE_INCREMENT.name(),
            LockModeType.PESSIMISTIC_FORCE_INCREMENT,
            MixedLockLevels.LOCK_PESSIMISTIC_FORCE_INCREMENT);

        try {
            fPlan.setHint(hintName, "xxxxx");
            fPlan.setHint(hintName, "yyyyy");
            fail("Expecting a a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, "12345");
            fPlan.setHint(hintName, "67890");
            fail("Expecting a a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        try {
            fPlan.setHint(hintName, -1);
            fPlan.setHint(hintName, -2);
            fail("Expecting a a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        if(inTransaction)
            em.getTransaction().rollback();
        em.close();
    }

    private void writeLockModeHintTest(JDBCFetchPlan fPlan,
        JDBCFetchConfigurationImpl fConfig, String hint, Object value,
        Object expectedMode, Object expectedLevel) {
        fPlan.setHint(hint, value);
        Object getValue = fPlan.getHint(hint);
        assertEquals(value.getClass(), getValue.getClass());
        assertEquals(value, getValue);
        assertEquals(expectedMode, fPlan.getWriteLockMode());
        assertEquals(expectedLevel, fConfig.getWriteLockLevel());
    }

    /* Good
     * Test precendence order of similiar/equivalent LockTimeout hints.
     */
    public void testSimiliarLockTimeoutHints() {
        String JavaxLockTimeout = "javax.persistence.lock.timeout";
        String OpenJPALockTimeout = "openjpa.LockTimeout";
        String FetchPlanLockTimeout = "openjpa.FetchPlan.LockTimeout";

        EntityManager em = emf.createEntityManager();
        OpenJPAEntityManager oem = (OpenJPAEntityManager)em.getDelegate();

        similarLockTimeoutHintsTest(oem, JavaxLockTimeout, 333,
            JavaxLockTimeout, 333,
            OpenJPALockTimeout, 111,
            FetchPlanLockTimeout, 222);
        similarLockTimeoutHintsTest(oem, JavaxLockTimeout, 333,
            OpenJPALockTimeout, 111,
            FetchPlanLockTimeout, 222,
            JavaxLockTimeout, 333);
        similarLockTimeoutHintsTest(oem, JavaxLockTimeout, 333,
            JavaxLockTimeout, 333,
            FetchPlanLockTimeout, 222,
            OpenJPALockTimeout, 111);
        similarLockTimeoutHintsTest(oem, FetchPlanLockTimeout, 222,
            OpenJPALockTimeout, 111,
            FetchPlanLockTimeout, 222);
        similarLockTimeoutHintsTest(oem, FetchPlanLockTimeout, 222,
            FetchPlanLockTimeout, 222,
            OpenJPALockTimeout, 111);
        similarLockTimeoutHintsTest(oem, OpenJPALockTimeout, 111,
            OpenJPALockTimeout, 111);
        similarLockTimeoutHintsTest(oem, FetchPlanLockTimeout, 222,
            FetchPlanLockTimeout, 222);
        similarLockTimeoutHintsTest(oem, JavaxLockTimeout, 333,
            JavaxLockTimeout, 333);

        em.close();
    }

    @SuppressWarnings("deprecation")
    private void similarLockTimeoutHintsTest(OpenJPAEntityManager oem, String winner, 
        Object expected, Object... hintNvalues) {
        JDBCFetchPlan fPlan = (JDBCFetchPlan) oem.pushFetchPlan();
        JDBCFetchConfigurationImpl fConfig = (JDBCFetchConfigurationImpl) fPlan
            .getDelegate();

        for( int i = 0 ; i < hintNvalues.length ; i += 2) {
            fPlan.setHint((String)hintNvalues[i], hintNvalues[i+1]);
        }
        for( int i = 0 ; i < hintNvalues.length ; i += 2) {
            String hintName = (String)hintNvalues[i];
            Object expectedValue = hintNvalues[i+1];
            Object getValue = fPlan.getHint(hintName);
            if (hintName.equals(winner)) {
                assertEquals(expectedValue.getClass(), getValue.getClass());
                assertEquals(expectedValue, getValue);
            } 
        }
        assertEquals(expected, fPlan.getLockTimeout());
        assertEquals(expected, fConfig.getLockTimeout());

        oem.popFetchPlan();
    }

    /* Good
     * Test precendence order of similiar/equivalent QueryTimeout hints.
     */
    public void testSimiliarQueryTimeoutHints() {
        String JavaxQueryTimeout = "javax.persistence.query.timeout";
        String OpenJPAQueryTimeout = "openjpa.QueryTimeout";
        String FetchPlanQueryTimeout = "openjpa.FetchPlan.QueryTimeout";

        EntityManager em = emf.createEntityManager();
        OpenJPAEntityManager oem = (OpenJPAEntityManager)em.getDelegate();

        similarQueryTimeoutHintsTest(oem, JavaxQueryTimeout, 333, 
            JavaxQueryTimeout, 333,
            OpenJPAQueryTimeout, 111,
            FetchPlanQueryTimeout, 222);
        similarQueryTimeoutHintsTest(oem, JavaxQueryTimeout, 333, 
            OpenJPAQueryTimeout, 111,
            FetchPlanQueryTimeout, 222,
            JavaxQueryTimeout, 333);
        similarQueryTimeoutHintsTest(oem, JavaxQueryTimeout, 333, 
            JavaxQueryTimeout, 333,
            FetchPlanQueryTimeout, 222,
            OpenJPAQueryTimeout, 111);
        similarQueryTimeoutHintsTest(oem, FetchPlanQueryTimeout, 222, 
            OpenJPAQueryTimeout, 111,
            FetchPlanQueryTimeout, 222);
        similarQueryTimeoutHintsTest(oem, FetchPlanQueryTimeout, 222, 
            FetchPlanQueryTimeout, 222,
            OpenJPAQueryTimeout, 111);
        similarQueryTimeoutHintsTest(oem, OpenJPAQueryTimeout, 111, 
            OpenJPAQueryTimeout, 111);
        similarQueryTimeoutHintsTest(oem, FetchPlanQueryTimeout, 222, 
            FetchPlanQueryTimeout, 222);
        similarQueryTimeoutHintsTest(oem, JavaxQueryTimeout, 333, 
            JavaxQueryTimeout, 333);

        em.close();
    }

    @SuppressWarnings("deprecation")
    private void similarQueryTimeoutHintsTest(OpenJPAEntityManager oem, String winner, 
        Object expected, Object... hintNvalues) {
        JDBCFetchPlan fPlan = (JDBCFetchPlan) oem.pushFetchPlan();
        JDBCFetchConfigurationImpl fConfig = (JDBCFetchConfigurationImpl) fPlan
            .getDelegate();

        for( int i = 0 ; i < hintNvalues.length ; i += 2) {
            fPlan.setHint((String)hintNvalues[i], hintNvalues[i+1]);
        }
        for( int i = 0 ; i < hintNvalues.length ; i += 2) {
            String hintName = (String)hintNvalues[i];
            Object expectedValue = hintNvalues[i+1];
            Object getValue = fPlan.getHint(hintName);
            if (hintName.equals(winner)) {
                assertEquals(expectedValue.getClass(), getValue.getClass());
                assertEquals(expectedValue, getValue);
            }
        }
        assertEquals(expected, fPlan.getQueryTimeout());
        assertEquals(expected, fConfig.getQueryTimeout());

        oem.popFetchPlan();
    }

    /*
     * Test various forms setHint argument.
     */
    @SuppressWarnings("deprecation")
    public void testSetHintAddHintsArgument() {
        EntityManager em = emf.createEntityManager();
        OpenJPAEntityManager oem = (OpenJPAEntityManager)em.getDelegate();
        JDBCFetchPlan fPlan = (JDBCFetchPlan) oem.getFetchPlan();

        fPlan.setHint("unrecognized.prop.name", "unrecognized.prop.value");
        assertEquals(null, fPlan.getHint("unrecognized.prop.name"));


        OpenJPAConfiguration conf = oem.getConfiguration();
        if (conf instanceof JDBCConfiguration
            && ((JDBCConfiguration) conf).getDBDictionaryInstance()
                .supportsIsolationForUpdate()) {
            try {
                fPlan.setHint("openjpa.jdbc.TransactionIsolation", 9999);
                fail("Expecting a a IllegalArgumentException.");
            } catch (Exception e) {
                assertTrue("Caught expected exception",
                    IllegalArgumentException.class.isAssignableFrom(e.getClass()));
            }
        }

        try {
            fPlan.setHint("openjpa.FetchPlan.Isolation", new Integer(13));
            fPlan.setHint("openjpa.FetchPlan.Isolation", new Integer(14));
            fail("Expecting a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        em.close();
    }

    /*
     * Test invalid hint via Query interface.
     */
    public void testInvalidQuerySetHint() {
        EntityManager em = emf.createEntityManager();
        OpenJPAEntityManager oem = (OpenJPAEntityManager)em.getDelegate();
            ((EntityManagerImpl) oem).getBroker().getFetchConfiguration();

        Query q = em.createQuery("select o from LockEmployee o");
        try {
            q.setHint("openjpa.FetchPlan.Isolation", "yyyyy");
            fail("Expecting a IllegalArgumentException.");
        } catch (Exception e) {
            assertTrue("Caught expected exception",
                IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
        em.close();
    }
}

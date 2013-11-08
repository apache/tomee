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
package org.apache.openjpa.persistence.jdbc;

import javax.persistence.Query;

import org.apache.openjpa.persistence.test.SQLListenerTestCase;
import org.apache.openjpa.persistence.simple.AllFieldTypes;
import org.apache.openjpa.persistence.OpenJPAQuery;
import org.apache.openjpa.persistence.InvalidStateException;
import org.apache.openjpa.persistence.OpenJPAEntityManagerSPI;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.jdbc.sql.DB2Dictionary;
import org.apache.openjpa.jdbc.sql.HSQLDictionary;
import org.apache.openjpa.jdbc.conf.JDBCConfiguration;

public class TestIsolationLevelOverride
    extends SQLListenerTestCase {

    public void setUp() {
        setUp(AllFieldTypes.class,
            "openjpa.Optimistic", "false",
            "openjpa.LockManager", "pessimistic");
    }

    public void testIsolationOverrideViaFetchPlan() {
        testIsolationLevelOverride(false, false);
    }

    public void testIsolationOverrideViaHint() {
        testIsolationLevelOverride(true, false);
    }

    public void testIsolationOverrideViaStringHint() {
        testIsolationLevelOverride(true, true);
    }

    public void testIsolationLevelOverride(boolean useHintsAndQueries,
        boolean useStringHints) {
        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        DBDictionary dict = ((JDBCConfiguration) em.getConfiguration())
            .getDBDictionaryInstance();

        // hsql doesn't support locking; circumvent the test
        if (dict instanceof HSQLDictionary)
            return;

        sql.clear();
        try {
            em.getTransaction().begin();
            if (useHintsAndQueries) {
                Query q = em.createQuery(
                "select o from AllFieldTypes o where o.intField = :p");
                q.setParameter("p", 0);
                if (useStringHints) {
                    q.setHint("openjpa.FetchPlan.Isolation", "SERIALIZABLE");
                } else {
                    q.setHint("openjpa.FetchPlan.Isolation",
                        IsolationLevel.SERIALIZABLE);
                }

                assertEquals(IsolationLevel.SERIALIZABLE,
                    ((JDBCFetchPlan) ((OpenJPAQuery) q).getFetchPlan())
                        .getIsolation());

                q.getResultList();
                if (dict instanceof DB2Dictionary) {
                    int db2server = ((DB2Dictionary) dict).getDb2ServerType();
                    if (db2server == DB2Dictionary.db2ISeriesV5R3OrEarlier
                        || db2server == DB2Dictionary.db2UDBV81OrEarlier) {
                        assertEquals(1, sql.size());
                        assertContainsSQL(" FOR UPDATE");
                    }
                    // it is DB2 v82 or later
                    else if (db2server == DB2Dictionary.db2ZOSV8xOrLater
                        || db2server == DB2Dictionary.db2UDBV82OrLater) {
                        assertEquals(1, sql.size());
                        assertContainsSQL(" FOR READ ONLY WITH RR USE AND KEEP "
                            + "UPDATE LOCKS");
                    }
                    else if (db2server == DB2Dictionary.db2ISeriesV5R4OrLater) {
                        assertEquals(1, sql.size());
                        assertContainsSQL(" FOR READ ONLY WITH RR USE AND KEEP" 
                            + " EXCLUSIVE LOCKS");
                    }    
                    else {
                        fail("OpenJPA currently only supports " 
                            +"per-query isolation level configuration on the" 
                            +" following databases: DB2");
                    }
                }    
            } else {
                ((JDBCFetchPlan) em.getFetchPlan())
                    .setIsolation(IsolationLevel.SERIALIZABLE);
                em.find(AllFieldTypes.class, 0);
                if (dict instanceof DB2Dictionary ) {
                    int db2server = ((DB2Dictionary) dict).getDb2ServerType();
                    if (db2server == DB2Dictionary.db2ISeriesV5R3OrEarlier
                        || db2server == DB2Dictionary.db2UDBV81OrEarlier) {
                        assertEquals(1, sql.size());
                        assertContainsSQL(" optimize for 1 row FOR UPDATE");
                    }
                    // it is DB2 v82 or later
                    else if (db2server == DB2Dictionary.db2ZOSV8xOrLater
                        || db2server == DB2Dictionary.db2UDBV82OrLater) {
                        assertEquals(1, sql.size());
                        assertContainsSQL(" optimize for 1 row"
                            + " FOR READ ONLY WITH RR USE AND KEEP UPDATE LOCKS"
                            );
                    }
                    else if (db2server == DB2Dictionary.db2ISeriesV5R4OrLater) {
                        assertEquals(1, sql.size());
                        assertContainsSQL(" optimize for 1 row"
                            + " FOR READ ONLY WITH RR USE AND KEEP EXCLUSIVE" 
                            + " LOCKS");
                    }    
                    else {
                        fail("OpenJPA currently only supports per-query" 
                            +" isolation level configuration on the following" 
                            +" databases: DB2");
                    }
                }    
            }
        } catch (InvalidStateException pe) {
            // if we're not using DB2, we expect an InvalidStateException.
            if (dict instanceof DB2Dictionary)
                throw pe;
        } finally {
            em.getTransaction().rollback();
            em.close();
        }
    }
}

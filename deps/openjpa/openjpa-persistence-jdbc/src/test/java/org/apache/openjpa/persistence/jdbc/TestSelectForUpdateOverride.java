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

import javax.persistence.LockModeType;

import org.apache.openjpa.persistence.test.SQLListenerTestCase;
import org.apache.openjpa.persistence.simple.AllFieldTypes;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.OpenJPAEntityManagerSPI;
import org.apache.openjpa.jdbc.sql.DB2Dictionary;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.jdbc.sql.HSQLDictionary;
import org.apache.openjpa.jdbc.conf.JDBCConfiguration;

public class TestSelectForUpdateOverride
    extends SQLListenerTestCase {

    public void setUp() {
        setUp(AllFieldTypes.class,
            "openjpa.Optimistic", "false",
            "openjpa.LockManager", "pessimistic",
            "openjpa.ReadLockLevel", "none");
    }

    public void testSelectForUpdateOverride() {
        OpenJPAEntityManagerSPI em = (OpenJPAEntityManagerSPI)
            OpenJPAPersistence.cast(emf.createEntityManager());
        DBDictionary dict = ((JDBCConfiguration) em.getConfiguration())
            .getDBDictionaryInstance();

        // hsql doesn't support locking; circumvent the test
        if (dict instanceof HSQLDictionary)
            return;

        sql.clear();
        try {
            em.getTransaction().begin();
            OpenJPAPersistence.cast(em).getFetchPlan()
                .setReadLockMode(LockModeType.WRITE);
            em.find(AllFieldTypes.class, 0);
            assertEquals(1, sql.size());
            if (dict instanceof DB2Dictionary) {
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
                        + " FOR READ ONLY WITH RS USE AND KEEP UPDATE LOCKS");
                }
                else if (db2server == DB2Dictionary.db2ISeriesV5R4OrLater) {
                    assertEquals(1, sql.size());
                    assertContainsSQL(" optimize for 1 row"
                        + " FOR READ ONLY WITH RS USE AND KEEP EXCLUSIVE LOCKS"
                        );
                }    
                else {
                    fail("OpenJPA currently only supports per-query isolation " 
                        + "level configuration on the following databases: "
                        + "DB2");
                }
            }    
        } finally {
            em.getTransaction().rollback();
            em.close();
        }
    }
}

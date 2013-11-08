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
package org.apache.openjpa.persistence.sequence.hsql;

import java.util.List;

import javax.persistence.Query;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.test.SQLListenerTestCase;

/**
 * The test case demonstrates the expected return id sequence should be:
 * "1, 2, 3, 4, 5, ..." regardless of the HSQL database version being used.
 * 
 * In version 2.1.* onward, "SELECT NEXT VALUE ..." query returns two times the
 * sequence's "INCREMENT BY" value, for example: allocationSize=1 returns
 * "1, 2". The next call returns "3, 4". Therefore, the id sequence returned
 * (without "LIMIT 1") are: "1, 3, 5, 7, 9, ...". By the same token, using
 * allocationSize=2 returns "1, 2, 3, 4". The next call returns "5, 6, 7, 8".
 * The id sequence returned then (without "LIMIT 1") are: "1, 2, 5, 6, 9, ...".
 * 
 * For HSQL version 2.1.0 rc4 and beyond, the dictionary needs to append
 * "LIMIT 1" to the nextSequenceQuery field in HSQLDictionary.java to ensure
 * single value is returned in "SELECT NEXT VALUE ..." request. This forces HSQL
 * to hand back only one value at a time. Therefore, the returned id sequence is
 * "1, 2, 3, 4, 5, ..." independent of HSQL version.
 */
public class TestHSQLSequence extends SQLListenerTestCase {
    OpenJPAEntityManager em;
    JDBCConfiguration conf;
    DBDictionary dict;

    public void setUp() throws Exception {
        setSupportedDatabases(org.apache.openjpa.jdbc.sql.HSQLDictionary.class);
        if (isTestsDisabled()) {
            return;
        }

        super.setUp(HSQLEmployee.class, HSQLEmployee2.class, DROP_TABLES,
                "openjpa.ConnectionFactoryProperties", "PrintParameters=true"
                );
        assertNotNull(emf);

        conf = (JDBCConfiguration) emf.getConfiguration();
        dict = conf.getDBDictionaryInstance();
        boolean supportsNativeSequence = dict.nextSequenceQuery != null;
        if (supportsNativeSequence) {
            em = emf.createEntityManager();
            // Drop all sequences to eliminate non-consecutive "SELECT NEXT VALUE FOR ..."
            assertNotNull(em);
            Query q = em.createNativeQuery(
                    "SELECT SEQUENCE_SCHEMA, SEQUENCE_NAME FROM INFORMATION_SCHEMA.SYSTEM_SEQUENCES");
            List<Object[]> rs = q.getResultList();
            em.getTransaction().begin();
            for(Object[] os : rs) {
                String schemaQualifier = (String)os[0];
                String schemaName = (String)os[1];
                if( "PUBLIC".equals(schemaQualifier) && !schemaName.equals("HEMP_SEQ")) {
                    q = em.createNativeQuery("DROP SEQUENCE " + schemaName);
                    q.executeUpdate();
                }
            }
            em.getTransaction().commit();
            em.clear();
        }
    }

    // create HSQLEmployee entity and persist it
    public HSQLEmployee createHSQLEmployee(String first, String last) {
        HSQLEmployee e = new HSQLEmployee();
        e.setFirstName(first);
        e.setLastName(last);
        em.getTransaction().begin();
        em.persist(e);
        em.getTransaction().commit();
        int id = e.getId();
        em.clear();
        return em.find(HSQLEmployee.class, id);
    }

    public HSQLEmployee2 createHSQLEmployee2(String first, String last) {
        HSQLEmployee2 e = new HSQLEmployee2();
        e.setFirstName(first);
        e.setLastName(last);
        em.getTransaction().begin();
        em.persist(e);
        em.getTransaction().commit();
        int id = e.getId();
        em.clear();
        return em.find(HSQLEmployee2.class, id);
    }

    public void testId() {
        int counter = 1;
        int id = 0;
        while (counter <= 20) {
            HSQLEmployee2 e = createHSQLEmployee2("Ferris" + counter, "Erris");
            assertNotNull(e);
            id = e.getId();
            assertEquals(counter, id);
            ++counter;
        }
    }

    public void testId2() {
        int counter = 1;
        int id = 0;
        while (counter <= 20) {
            HSQLEmployee e = createHSQLEmployee("Ferris" + counter, "Erris");
            assertNotNull(e);
            id = e.getId();
            assertEquals(counter, id);
            ++counter;
        }
    }
}

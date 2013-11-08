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
package org.apache.openjpa.persistence.jdbc.query;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.jdbc.sql.MariaDBDictionary;
import org.apache.openjpa.jdbc.sql.MySQLDictionary;
import org.apache.openjpa.jdbc.sql.OracleDictionary;
import org.apache.openjpa.persistence.jdbc.query.domain.TimeKeeper;
import org.apache.openjpa.persistence.test.SQLListenerTestCase;

/**
 * Tests database-specific query hints.
 */
public class TestHintedQuery extends SQLListenerTestCase {

    public void setUp() {
        super.setUp(CLEAR_TABLES, TimeKeeper.class);
    }
    
    public void testHintedQuery() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(new TimeKeeper());
        em.persist(new TimeKeeper());
        em.getTransaction().commit();

        String jpql = "SELECT tk FROM TimeKeeper tk";
        String mariadbHint = "SQL_NO_CACHE";
        String mysqlHint = "SQL_NO_CACHE";
        String oracleHint = "/*+ first_rows(100) */";
        Query query = em.createQuery(jpql);
        query.setHint(MariaDBDictionary.SELECT_HINT, mariadbHint);
        query.setHint(MySQLDictionary.SELECT_HINT, mysqlHint);
        query.setHint(OracleDictionary.SELECT_HINT, oracleHint);
        List keepers = query.getResultList();
        assertEquals(2, keepers.size());

        // The dictionaries the hints are meant for should use the hints.
        // Other dictionaries should ignore them.
        DBDictionary dict = ((JDBCConfiguration) emf.getConfiguration())
            .getDBDictionaryInstance();
        if (dict instanceof MariaDBDictionary) {
            assertContainsSQL("SELECT " + mariadbHint + " ");
            return;
        }
        if (dict instanceof MySQLDictionary) {
            assertContainsSQL("SELECT " + mysqlHint + " ");
            return;
        }
        if (dict instanceof OracleDictionary) {
            assertContainsSQL("SELECT " + oracleHint + " ");
            return;
        }
        assertNotSQL(".*" + mariadbHint + ".*");
        assertNotSQL(".*" + mysqlHint + ".*");
        assertNotSQL(".*" + oracleHint + ".*");
    }
}

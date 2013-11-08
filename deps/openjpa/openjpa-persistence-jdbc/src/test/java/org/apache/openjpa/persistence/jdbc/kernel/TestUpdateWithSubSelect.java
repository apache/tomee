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
package org.apache.openjpa.persistence.jdbc.kernel;

import javax.persistence.EntityManager;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.persistence.jdbc.sqlcache.Address;
import org.apache.openjpa.persistence.jdbc.sqlcache.Person;
import org.apache.openjpa.persistence.test.SQLListenerTestCase;

/**
 * Tests bulk update with join instead of subselect.
 * Reported in <A HREF="https://issues.apache.org/jira/browse/OPENJPA-459">OPENJPA-459</A>
 * that update in MySQL fails when the UPDATE SQL included a SubSelect because of
 * limitation mentioned in <A HREF="http://lists.mysql.com/mysql/185623">MySQL forum</A>.
 * <br>
 * The solution is {@link DBDictionary#toBulkOperation()} modified to accommodate
 * scenarios that selects from a single table but may involve joins of multiple tables.
 *  
 * @author Pinaki Poddar
 *
 */
public class TestUpdateWithSubSelect extends SQLListenerTestCase {

    public void setUp() throws Exception {
        super.setUp(CLEAR_TABLES, Person.class, Address.class);
        DBDictionary dict = ((JDBCConfiguration)emf.getConfiguration()).getDBDictionaryInstance();
        setTestsDisabled(!dict.supportsSubselect || !dict.allowsAliasInBulkClause);
        getLog().trace(this + " is disabled because " + dict.getClass().getSimpleName() + 
          " either both or one of supportsSubselect and allowsAliasInBulkClause is false");
    }
    
    /**
     * Tests that a bulk update issues a single SQL and that uses a join. 
     */
    public void testUpdateBySubSelect() {
        String jpql = "UPDATE Person p SET p.age = :age WHERE p.address.city = :city";
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        sql.clear();
        em.createQuery(jpql)
          .setParameter("age", (short)25)
          .setParameter("city", "SFO")
          .executeUpdate();
        em.getTransaction().commit();
        assertEquals(1,sql.size());
        String sqlString = sql.get(0).toUpperCase().trim();
        assertTrue(sqlString.startsWith("UPDATE"));
        // assert JOIN condition
        assertTrue(sqlString.indexOf("T0.ADDRESS_ID = T1.ID") != -1);
    }

}

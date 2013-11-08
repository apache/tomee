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
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.OpenJPAQuery;
import org.apache.openjpa.persistence.InvalidStateException;
import org.apache.openjpa.persistence.OpenJPAEntityManagerSPI;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.jdbc.sql.DB2Dictionary;
import org.apache.openjpa.jdbc.sql.HSQLDictionary;
import org.apache.openjpa.jdbc.conf.JDBCConfiguration;

public class TestOptimizeForClause
    extends SQLListenerTestCase {

    public void setUp() {
        setUp(AllFieldTypes.class);
    }

    public void testOptimizeForClauseViaGetSingleResult() {
        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        DBDictionary dict = ((JDBCConfiguration) em.getConfiguration())
            .getDBDictionaryInstance();
        
        em.getTransaction().begin();
        AllFieldTypes a = new AllFieldTypes();
        a.setIntField(123);
        em.persist(a);
        em.getTransaction().commit();
        em.clear();

        sql.clear();
        Object result = em.createQuery
            ("select o from AllFieldTypes o where o.intField = 123").
            getSingleResult();

        assertNotNull(result);
        if (dict instanceof DB2Dictionary ) {
            assertContainsSQL(" optimize for 1 row");
        }
        em.close();
    }

    public void testOptimizeForClauseViaHint() {
        tstOptimizeForClause(true,false,false);
    }

    public void testOptimizeForClauseViaFind() {
        tstOptimizeForClause(false,true,false);
    }
    public void testOptimizeForClauseViaQueryHint() {
        tstOptimizeForClause(false,true,true);
    }
    public void tstOptimizeForClause(boolean hint,
        boolean find, boolean queryHint) {
        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        DBDictionary dict = ((JDBCConfiguration) em.getConfiguration())
            .getDBDictionaryInstance();

        // hsql doesn't support optimizing; circumvent the test
        if (dict instanceof HSQLDictionary)
            return;

        sql.clear();
        try {
            em.getTransaction().begin();
            if (hint || queryHint) {
                if (hint) {
                    Query q = em.createQuery(
                    "select o from AllFieldTypes o where o.intField = :p");
                    q.setParameter("p", 0);
                    q.setHint("openjpa.hint.OptimizeResultCount"
                         ,new Integer(8));
                    q.getResultList();
                }    
                else {
                    OpenJPAQuery q =  OpenJPAPersistence.cast (em.createQuery 
                        ("select o from AllFieldTypes o where o.intField " +
                         "= :p"));
                    q.setParameter("p", 0);
                    q.setHint(q.HINT_RESULT_COUNT, new Integer(8)); 
                    q.getResultList();
               }     
               if (dict instanceof DB2Dictionary) {
                   assertEquals(1, sql.size());
                   assertContainsSQL(" optimize for 8 row");
               }
            }
            else {
                 em.find(AllFieldTypes.class, 0);
                 if (dict instanceof DB2Dictionary ) {
                    assertEquals(1, sql.size());
                    assertContainsSQL(" optimize for 1 row");
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

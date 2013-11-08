/*
 * TestMathQueries.java
 *
 * Created on October 18, 2006, 1:06 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
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

package org.apache.openjpa.persistence.query;

import javax.persistence.EntityManager;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.persistence.common.apps.RuntimeTest1;
import org.apache.openjpa.persistence.test.SQLListenerTestCase;

/**
 * Tests usage of COUNT(*) in SQL query.
 * <br>
 * Further details available at <br>
 * <A HREF="https://issues.apache.org/jira/browse/OPENJPA-1440">OPENJPA-1440</HREF>
 * 
 * @author Pinaki Poddar
 *
 */
public class TestWildCardCount extends SQLListenerTestCase {
    private EntityManager em;
    public void setUp() throws Exception {
        super.setUp(RuntimeTest1.class, "openjpa.jdbc.QuerySQLCache", "false");
        em = emf.createEntityManager();
        sql.clear();
    }
    
    public void testWildCardForCountInSingleProjectTerm() {
        String jpql = "select COUNT(p) from RuntimeTest1 p";
        executeAndAssert(jpql);
    }
    
    public void testWildCardForCountInMultipleProjectTerms() {
        String jpql = "select COUNT(p.intField),SUM(p.intField) from RuntimeTest1 p GROUP BY p.intField";
        executeAndAssert(jpql);
    }
    
    public void testWildCardForCountInMultipleProjectTermsButCountIsNotFirstTerm() {
        String jpql = "select SUM(p.intField),COUNT(p.intField) from RuntimeTest1 p GROUP BY p.intField";
        executeAndAssert(jpql);
    }
    
    void executeAndAssert(String jpql) {
        executeAndAssert(true, jpql);
        executeAndAssert(false, jpql);
    }
    
    void executeAndAssert(boolean useWildCard, String jpql) {
        setWildCardForCount(useWildCard);
        sql.clear();
        em.createQuery(jpql).getResultList();
        assertEquals(1, sql.size());
        assertEquals(getWildCardForCount(), usesWildCardForCount(sql.get(0))); 
    }
    
    boolean getWildCardForCount() {
        return ((JDBCConfiguration)emf.getConfiguration()).getDBDictionaryInstance().useWildCardForCount;
    }
    
    void setWildCardForCount(boolean flag) {
        ((JDBCConfiguration)emf.getConfiguration()).getDBDictionaryInstance().useWildCardForCount = flag;
    }

    boolean usesWildCardForCount(String sql) {
        return sql.contains("COUNT(*)");
    }

}

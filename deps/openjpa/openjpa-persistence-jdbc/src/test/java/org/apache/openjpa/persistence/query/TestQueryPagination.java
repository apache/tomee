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

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.persistence.test.SQLListenerTestCase;

/**
 * Test that query pagination works properly.
 */
public class TestQueryPagination
    extends SQLListenerTestCase {

    public void setUp() {
        setUp(SimpleEntity.class, CLEAR_TABLES);

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(new SimpleEntity("foo", "bar" + 0));
        em.persist(new SimpleEntity("foo", "bar" + 1));
        em.persist(new SimpleEntity("foo", "bar" + 2));
        em.persist(new SimpleEntity("foo", "bar" + 3));
        em.persist(new SimpleEntity("foo", "bar" + 4));
        em.persist(new SimpleEntity("foo", "bar" + 5));
        em.getTransaction().commit();
        em.close();
    }

    public void testFirstThenMax() {
        helper(true, 2, 3, 3);
    }

    public void testMaxThenFirst() {
        helper(false, 2, 3, 3);
    }

    public void testNoResultsFirstFirst() {
        helper(true, 10, 3, 0);
    }

    public void testNoResultsFirstLast() {
        helper(false, 10, 3, 0);
    }

    public void testAllResultsFirstFirst() {
        helper(true, 0, 10, 6);
    }

    public void testAllResultsFirstLast() {
        helper(false, 0, 10, 6);
    }

    private void helper(boolean firstFirst, int first, int max, int expected) {
        EntityManager em = emf.createEntityManager();
        Query q = em.createQuery("select e from simple e order by e.value");
        sql.clear();
        List<SimpleEntity> fullList = q.getResultList();
        if (firstFirst)
            q.setFirstResult(first).setMaxResults(max);
        else
            q.setMaxResults(max).setFirstResult(first);
        List<SimpleEntity> list = q.getResultList();
        checkSQL();
        assertEquals(expected, list.size());
        for (int i = 0; i < list.size(); i++) {
            assertEquals("bar" + (first + i), list.get(i).getValue());
        }
        em.close();
    }

    private void checkSQL() {
        assertEquals(2, sql.size());
        String noRange = this.sql.get(0);
        String withRange = this.sql.get(1);
        DBDictionary dict = ((JDBCConfiguration) emf.getConfiguration())
            .getDBDictionaryInstance();
        if (dict.supportsSelectStartIndex || dict.supportsSelectEndIndex)
            assertNotEquals(noRange, withRange);
        else
            assertEquals(noRange, withRange);
    }
}

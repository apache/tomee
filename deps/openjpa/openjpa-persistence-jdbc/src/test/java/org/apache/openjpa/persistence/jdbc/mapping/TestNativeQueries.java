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
package org.apache.openjpa.persistence.jdbc.mapping;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.openjpa.kernel.QueryLanguages;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.jdbc.common.apps.mappingApp.Entity1;
import org.apache.openjpa.persistence.jdbc.common.apps.mappingApp.Entity2;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestNativeQueries extends SingleEMFTestCase {
    private static final String TABLE_NAME = "entity_1";
    private static final String TABLE_NAME_2 = "ENTITY2";
    
    private static final String CONST_NAME = "testSimple";
    private static final int CONST_INT = 42;
    
    private EntityManager em;
    
    public void setUp() {
        super.setUp(CLEAR_TABLES, Entity1.class, Entity2.class);

        em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(new Entity1(1, CONST_NAME, CONST_INT));
        em.persist(new Entity1(2, CONST_NAME+" Changed", CONST_INT+1));
        em.persist(new Entity1(3, CONST_NAME+" Changed 2", CONST_INT+2));
        em.getTransaction().commit();
        em.getTransaction().begin();
    }

    public void testNoParameter() {
        String sql = "SELECT * FROM " + TABLE_NAME;
        assertSize(3, em.createNativeQuery(sql, Entity1.class).getResultList());
    }
    
    public void testLiteral() {
        String sql = "SELECT * FROM " + TABLE_NAME 
                   + " WHERE INTFIELD = " + CONST_INT;
        assertSize(1, em.createNativeQuery(sql, Entity1.class).getResultList());
    }
    
    public void testParameter() {
        String sql = "SELECT * FROM " + TABLE_NAME 
                   + " WHERE INTFIELD = ?1";
        assertSize(1, em.createNativeQuery(sql, Entity1.class)
            .setParameter(1, CONST_INT)
            .getResultList());
    }
    
    public void testOutOfOrderParameter() {
        String sql = "SELECT * FROM " + TABLE_NAME 
                   + " WHERE INTFIELD = ?2 AND STRINGFIELD = ?1";
        assertSize(1, em.createNativeQuery(sql, Entity1.class)
            .setParameter(2, CONST_INT)
            .setParameter(1, CONST_NAME)
            .getResultList());
    }
    
    public void testDuplicateParameter() {
        String sql = "SELECT * FROM " + TABLE_NAME
                   + " WHERE INTFIELD = ?1 AND INTFIELD = ?1";
        assertSize(1, em.createNativeQuery(sql, Entity1.class)
            .setParameter(1, CONST_INT)
            .getResultList());
    }
    
    public void testDifferentParameterToSameField() {
        String sql = "SELECT * FROM " + TABLE_NAME
                   + " WHERE INTFIELD = ?1 OR INTFIELD = ?2";
        assertSize(2, em.createNativeQuery(sql, Entity1.class)
            .setParameter(1, CONST_INT)
            .setParameter(2, CONST_INT+1)
            .getResultList());
    }

    public void testQuoteParameterIgnored() {
        String sql = "SELECT * FROM " + TABLE_NAME
                   + " WHERE INTFIELD = ?1 OR STRINGFIELD = '?2'";
        assertSize(1, em.createNativeQuery(sql, Entity1.class)
            .setParameter(1, CONST_INT)
            .getResultList());
    }
    
    public void testParameterMarkerWithoutSpaces() {
        String sql = "SELECT * FROM " + TABLE_NAME
                   + " WHERE INTFIELD=?1";
        assertSize(1, em.createNativeQuery(sql, Entity1.class)
            .setParameter(1, CONST_INT)
            .getResultList());
    }
    
    public void testZeroBasedParameterSettingFails() {
        try {
            String sql = "SELECT * FROM " + TABLE_NAME 
                       + " WHERE INTFIELD = ?1";
            em.createNativeQuery(sql, Entity1.class)
                .setParameter(0, 12);
            fail("Expected to fail with 0 parameter index");
        } catch (Exception e) {
            // as expected
        }
    }

    public void testNamedParameterFails() {
        /*
         * Named parameters are not supported according to Section 3.6.8 of
         * JPA 2.0 (pp 100) public draft Oct 31, 2008:
         * "The use of named parameters is not defined for native queries. 
         * Only positional parameter binding for SQL queries may be used by 
         * portable applications."
         */
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE INTFIELD = :p";
        try {
            em.createNativeQuery (sql, Entity1.class)
              .setParameter ("p", 12);
            fail("Expected to fail with NAMED parameter");
        } catch (IllegalArgumentException ex) {
            // good
        }
    }
    
    public void testHintsAreProcessed() {
        Query q = em.createNamedQuery("SQLWithHints");
        assertEquals(QueryLanguages.LANG_SQL, 
            OpenJPAPersistence.cast(q).getLanguage());
        String hintKey = "XYZ";
        assertTrue(q.getHints().containsKey(hintKey));
        assertEquals("abc", q.getHints().get(hintKey));
        
    }
    
    public void testNullResult(){
        String sql = "SELECT max(pk) FROM " + TABLE_NAME_2+ "";
        assertNull(em.createNativeQuery(sql, Long.class).getSingleResult());
    }

    public void assertSize(int num, List l) {
        assertNotNull(l);
        assertEquals(num, l.size());
    }
}

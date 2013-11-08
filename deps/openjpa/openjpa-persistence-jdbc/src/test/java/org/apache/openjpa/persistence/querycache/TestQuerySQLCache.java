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
package org.apache.openjpa.persistence.querycache;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.openjpa.persistence.EntityManagerImpl;
import org.apache.openjpa.persistence.test.SQLListenerTestCase;

public class TestQuerySQLCache extends SQLListenerTestCase {
    EntityManager em;
    
    public void setUp() {
        super.setUp(
            DROP_TABLES,
            "openjpa.jdbc.QuerySQLCache", "true",
            "openjpa.DataCache", "false",
            QCEntity.class
            );
        em = emf.createEntityManager();
        
        em.getTransaction().begin();
        QCEntity qc1 = new QCEntity("pk1", "description", Long.valueOf(1));
        QCEntity qc2 = new QCEntity("pk2", "description-2", Long.valueOf(1));
        QCEntity qc3 = new QCEntity("pk3", null, null);
        
        em.persist(qc1);
        em.persist(qc2);
        em.persist(qc3);
        
        em.getTransaction().commit();
        
        em.clear();
    }
    
    public void testNullParamsWithNumericPosition01() {
        // Verify Query SQL Cache is enabled
        EntityManagerImpl eml = (EntityManagerImpl) em;
        assertTrue(eml.getQuerySQLCache());
               
        Query q = em.createQuery("SELECT o from QCEntity o WHERE o.amount=?1");
        
        // Test with NULL parameter, SQL should contain a IS NULL predicate
        resetSQL();
        q.setParameter(1, null);
        List resultListNull1A = q.getResultList();
        assertTrue((getLastSQL(sql) != null) && (getLastSQL(sql).contains("IS NULL")));
        assertNotNull(resultListNull1A);
        assertEquals(1, resultListNull1A.size());
        
        resetSQL();
        q.setParameter(1, null);
        List resultListNull1B = q.getResultList();
        assertTrue((getLastSQL(sql) != null) && (getLastSQL(sql).contains("IS NULL")));
        assertNotNull(resultListNull1B);
        assertEquals(1, resultListNull1B.size());
        
        // Test with non-NULL paramter, SQL should contain the = predicate
        resetSQL();
        q.setParameter(1, new Long(1));
        List resultListNotNull = q.getResultList();
        assertTrue((getLastSQL(sql) != null) && !(getLastSQL(sql).contains("IS NULL")));
        assertNotNull(resultListNotNull);
        assertEquals(2, resultListNotNull.size());      
        
        // Test again with NULL parameter, SQL should contain a IS NULL predicate
        resetSQL();
        q.setParameter(1, null);
        List resultListNull2 = q.getResultList();
        assertTrue((getLastSQL(sql) != null) && (getLastSQL(sql).contains("IS NULL")));
        assertNotNull(resultListNull2);
        assertEquals(1, resultListNull2.size());
    }
    
    public void testNullParamsWithNumericPosition02() {
        // Verify Query SQL Cache is enabled
        EntityManagerImpl eml = (EntityManagerImpl) em;
        assertTrue(eml.getQuerySQLCache());
                      
        // Test with NULL parameter, SQL should contain a IS NULL predicate
        resetSQL();
        Query q1 = em.createQuery("SELECT o from QCEntity o WHERE o.amount=?1");
        q1.setParameter(1, null);
        List resultListNull1A = q1.getResultList();
        assertTrue((getLastSQL(sql) != null) && (getLastSQL(sql).contains("IS NULL")));
        assertNotNull(resultListNull1A);
        assertEquals(1, resultListNull1A.size());
        
        resetSQL();
        Query q2 = em.createQuery("SELECT o from QCEntity o WHERE o.amount=?1");
        q2.setParameter(1, null);
        List resultListNull1B = q2.getResultList();
        assertTrue((getLastSQL(sql) != null) && (getLastSQL(sql).contains("IS NULL")));
        assertNotNull(resultListNull1B);
        assertEquals(1, resultListNull1B.size());
        
        // Test with non-NULL paramter, SQL should contain the = predicate
        resetSQL();
        Query q3 = em.createQuery("SELECT o from QCEntity o WHERE o.amount=?1");
        q3.setParameter(1, new Long(1));
        List resultListNotNull = q3.getResultList();
        assertTrue((getLastSQL(sql) != null) && !(getLastSQL(sql).contains("IS NULL")));
        assertNotNull(resultListNotNull);
        assertEquals(2, resultListNotNull.size());      
        
        // Test again with NULL parameter, SQL should contain a IS NULL predicate
        resetSQL();
        Query q4 = em.createQuery("SELECT o from QCEntity o WHERE o.amount=?1");
        q4.setParameter(1, null);
        List resultListNull2 = q4.getResultList();
        assertTrue((getLastSQL(sql) != null) && (getLastSQL(sql).contains("IS NULL")));
        assertNotNull(resultListNull2);
        assertEquals(1, resultListNull2.size());
    }
    
    public void testNullParamsWithNamedQuery01() {
        // Verify Query SQL Cache is enabled
        EntityManagerImpl eml = (EntityManagerImpl) em;
        assertTrue(eml.getQuerySQLCache());        
        
        Query q = em.createNamedQuery("QCEntity.getByAmount");
        
        resetSQL();       
        q.setParameter("amount", null);
        List resultListNull1A = q.getResultList();
        assertTrue((getLastSQL(sql) != null) && (getLastSQL(sql).contains("IS NULL")));
        assertNotNull(resultListNull1A);
        assertEquals(1, resultListNull1A.size());
        em.clear();
        
        // Test with NULL parameter, SQL should contain a IS NULL predicate
        resetSQL();
        q.setParameter("amount", null);
        List resultListNull1B = q.getResultList();
        assertTrue((getLastSQL(sql) != null) && (getLastSQL(sql).contains("IS NULL")));
        assertNotNull(resultListNull1B);
        assertEquals(1, resultListNull1B.size());
        em.clear();
        
        // Test with non-NULL parameter, SQL should contain the = predicate
        resetSQL();
        q.setParameter("amount", new Long(1));
        List resultListNotNull = q.getResultList();
        assertTrue((getLastSQL(sql) != null) && !(getLastSQL(sql).contains("IS NULL")));
        assertNotNull(resultListNotNull);
        assertEquals(2, resultListNotNull.size());
        em.clear();
        
        // Test again with NULL parameter, SQL should contain a IS NULL predicate
        resetSQL();
        q.setParameter("amount", null);
        List resultListNull2 = q.getResultList();
        assertTrue((getLastSQL(sql) != null) && (getLastSQL(sql).contains("IS NULL")));
        assertNotNull(resultListNull2);
        assertEquals(1, resultListNull2.size());
        em.clear();
    }
    
    public void testNullParamsWithNamedQuery02() {
        // Verify Query SQL Cache is enabled
        EntityManagerImpl eml = (EntityManagerImpl) em;
        assertTrue(eml.getQuerySQLCache());        
        
        resetSQL();
        Query q1A = em.createNamedQuery("QCEntity.getByAmount");
        q1A.setParameter("amount", null);
        List resultListNull1A = q1A.getResultList();
        assertTrue((getLastSQL(sql) != null) && (getLastSQL(sql).contains("IS NULL")));
        assertNotNull(resultListNull1A);
        assertEquals(1, resultListNull1A.size());
        em.clear();
        
        // Test with NULL parameter, SQL should contain a IS NULL predicate
        resetSQL();
        Query q1B = em.createNamedQuery("QCEntity.getByAmount");
        q1B.setParameter("amount", null);
        List resultListNull1B = q1B.getResultList();
        assertTrue((getLastSQL(sql) != null) && (getLastSQL(sql).contains("IS NULL")));
        assertNotNull(resultListNull1B);
        assertEquals(1, resultListNull1B.size());
        em.clear();
        
        // Test with non-NULL parameter, SQL should contain the = predicate
        resetSQL();
        Query q2 = em.createNamedQuery("QCEntity.getByAmount");
        q2.setParameter("amount", new Long(1));
        List resultListNotNull = q2.getResultList();
        assertTrue((getLastSQL(sql) != null) && !(getLastSQL(sql).contains("IS NULL")));
        assertNotNull(resultListNotNull);
        assertEquals(2, resultListNotNull.size());
        em.clear();
        
        // Test again with NULL parameter, SQL should contain a IS NULL predicate
        resetSQL();
        Query q3 = em.createNamedQuery("QCEntity.getByAmount");
        q3.setParameter("amount", null);
        List resultListNull2 = q3.getResultList();
        assertTrue((getLastSQL(sql) != null) && (getLastSQL(sql).contains("IS NULL")));
        assertNotNull(resultListNull2);
        assertEquals(1, resultListNull2.size());
        em.clear();
    }
}

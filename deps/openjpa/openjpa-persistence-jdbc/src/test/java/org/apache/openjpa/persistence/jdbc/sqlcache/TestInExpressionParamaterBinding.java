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

package org.apache.openjpa.persistence.jdbc.sqlcache;

import java.util.Arrays;
import java.util.List;

import javax.persistence.TypedQuery;

import org.apache.openjpa.kernel.PreparedQuery;
import org.apache.openjpa.kernel.PreparedQueryCache;
import org.apache.openjpa.persistence.ArgumentException;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactory;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Tests parameter binding to IN() expressions. 
 * IN() expressions accept parameters in following forms according to JPA 2.0 specification<br>
 * <tt>select p from PObject p where p.name IN (:n1,:n2,:n3)</tt> // where n1,n2,n3 are of bound to type of p.name<br>
 * <tt>select p from PObject p where p.name IN :n</tt> // where n is bound to collection of type of p.name</br>
 * <p>
 * For backward compatibility to 1.2 version, we also allow<br>
 * <tt>select p from PObject p where p.name IN (:n)</tt> where n is a collection and within parentheses<br>
 * <p>
 * So, collection-valued parameter is allowed with or without parenthese. But, single-valued parameters are 
 * <em>only</em> allowed with parentheses. 
 * <p>
 * The test also validates that such binding will work with PreparedQuery Cache because Prepared Query cache
 * rebinds parameters and designed to ignore queries with IN() expression. 
 * 
 * @author Pinaki Poddar
 *
 */
public class TestInExpressionParamaterBinding extends SingleEMFTestCase {
    private static OpenJPAEntityManagerFactory _emf;
    private static List<Integer> ORIGINAL_ZIPS;
    private OpenJPAEntityManager _em;
    
    public void setUp() throws Exception {
        if (_emf == null) {
            super.setUp(Address.class, "openjpa.ConnectionFactoryProperties", "PrintParameters=true", CLEAR_TABLES);
            _emf = emf; // from the super class
            ORIGINAL_ZIPS = Arrays.asList(12345, 23456, 34567, 45678, 56789, 67890);
            createData();
        }
        _em = _emf.createEntityManager();
    }

    public void tearDown() throws Exception {
        // block super class tear down
    }
    
    private void createData() {
        OpenJPAEntityManager em = _emf.createEntityManager();
        em.getTransaction().begin();
        for (int i = 0; i < ORIGINAL_ZIPS.size(); i++) {
            Address a = new Address();
            a.setZip(ORIGINAL_ZIPS.get(i));
            em.persist(a);
        }
        em.getTransaction().commit();
    }
    
    public void testWithCollectionParamOfDifferentSize() {
        String jpql = "select a from Address a where a.zip in (:p)";
        List<Integer> zips1 = ORIGINAL_ZIPS.subList(0, 3);
        List<Address> result1 = _em.createQuery(jpql, Address.class).setParameter("p", zips1).getResultList();
        assertEquals(zips1.size(), result1.size());
        assertNotCached(jpql);
        
        List<Integer> zips2 = ORIGINAL_ZIPS.subList(2, 4);
        List<Address> result2 = _em.createQuery(jpql, Address.class).setParameter("p", zips2).getResultList();
        assertEquals(zips2.size(), result2.size());
        
    }
    
    public void testWithCollectionParamOfDifferentSizeNoParentheses() {
        String jpql = "select a from Address a where a.zip in :p";
        List<Integer> zips1 = ORIGINAL_ZIPS.subList(0, 3);
        List<Address> result1 = _em.createQuery(jpql, Address.class).setParameter("p", zips1).getResultList();
        assertEquals(zips1.size(), result1.size());
        assertNotCached(jpql);

        List<Integer> zips2 = ORIGINAL_ZIPS.subList(2, 4);
        List<Address> result2 = _em.createQuery(jpql, Address.class).setParameter("p", zips2).getResultList();
        assertEquals(zips2.size(), result2.size());
    }
    
    public void testWithSingleParam() {
        String jpql = "select a from Address a where a.zip in (:p)";
        Integer zip1 = ORIGINAL_ZIPS.get(4);
        List<Address> result1 = _em.createQuery(jpql, Address.class).setParameter("p", zip1).getResultList();
        assertEquals(1, result1.size());
        assertEquals(zip1.intValue(), result1.get(0).getZip());
        assertNotCached(jpql);

        Integer zip2 = ORIGINAL_ZIPS.get(2);
        List<Address> result2 = _em.createQuery(jpql, Address.class).setParameter("p", zip2).getResultList();
        assertEquals(1, result2.size());
        assertEquals(zip2.intValue(), result2.get(0).getZip());
    }
    
    public void testWithMultiplParamOfDifferentSizeNoParentheses() {
        String jpql = "select a from Address a where a.zip in (:p1,:p2,:p3)";
        List<Integer> zips1 = ORIGINAL_ZIPS.subList(0, 3);
        TypedQuery<Address> query1 = _em.createQuery(jpql, Address.class);
        query1.setParameter("p1", zips1.get(0));
        query1.setParameter("p2", zips1.get(1));
        query1.setParameter("p3", zips1.get(2));
        List<Address> result1 = query1.getResultList();
        assertEquals(zips1.size(), result1.size());
        assertNotCached(jpql);
        
        List<Integer> zips2 = ORIGINAL_ZIPS.subList(2, 5);
        TypedQuery<Address> query2 = _em.createQuery(jpql, Address.class);
        query2.setParameter("p1", zips2.get(0));
        query2.setParameter("p2", zips2.get(1));
        query2.setParameter("p3", zips2.get(2));
        List<Address> result2 = query2.getResultList();
        assertEquals(zips2.size(), result2.size());
    }
    
    public void testWithSingleParamNoParentheses() {
        OpenJPAEntityManager em = _emf.createEntityManager();
        String jpql = "select a from Address a where a.zip in :p";
        Integer zip = ORIGINAL_ZIPS.get(4);
        TypedQuery<Address> q = em.createQuery(jpql, Address.class);
        q.setParameter("p", zip);
        try {
            List<Address> result = q.getResultList();
            fail("Expected error in execution because single-valued parameter not acceptable without parenthese");
        } catch (ArgumentException e) {
            // expected 
        }
        
    }
    
    
    void assertCached(String id) {
        PreparedQuery cached = getPreparedQueryCache().get(id);
        assertNotNull(getPreparedQueryCache() + ": " + getPreparedQueryCache().getMapView() + 
                " does not contain " + id, cached);
    }
    
    void assertNotCached(String id) {
        PreparedQueryCache cache = getPreparedQueryCache();
        if (cache != null) {
            assertNull(cache.get(id));
        }
    }
    PreparedQueryCache getPreparedQueryCache() {
        return _emf.getConfiguration().getQuerySQLCacheInstance();
    }

}

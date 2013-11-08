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



import org.apache.openjpa.kernel.PreparedQuery;
import org.apache.openjpa.kernel.PreparedQueryCache;
import org.apache.openjpa.persistence.query.common.apps.QueryTest1;
import org.apache.openjpa.persistence.query.common.apps.QueryTest2;
import org.apache.openjpa.persistence.query.common.apps.QueryTest4;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestQueryResultClassAPI extends SingleEMFTestCase {

    public void setUp() {
        setUp(QueryTest1.class, QueryTest2.class, QueryTest4.class,
            "openjpa.jdbc.QuerySQLCache", "true",
            CLEAR_TABLES);

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        QueryTest1 pc = null;
        for (int i = 0; i < 10; i++) {
            pc = new QueryTest1();
            if (i < 5)
                pc.setNum(4);
            else
                pc.setNum(i + 10);
            pc.setDate(new java.util.Date(9999));
            em.persist(pc);
        }
        em.getTransaction().commit();
        em.close();
    }


    public void testQueryWithResultClass() {
        EntityManager em = emf.createEntityManager();
        String jpql = "SELECT o FROM QueryTest1 o";

        Query query = em.createQuery(jpql, QueryTest1.class);

        List rs = query.getResultList();
        assertTrue(rs.get(0) instanceof QueryTest1);
        PreparedQuery pq = getPreparedQueryCache().get(jpql);
        assertNotNull(pq);

        Query query2 = em.createQuery(jpql, QueryTest1.class);
        PreparedQuery pq2 = getPreparedQueryCache().get(jpql);
        assertEquals(pq, pq2);
        rs = query2.getResultList();
        assertTrue(rs.get(0) instanceof QueryTest1);

        jpql = "SELECT o.datum FROM QueryTest1 o";
        query = em.createQuery(jpql, java.util.Date.class);
        rs = query.getResultList();
        assertTrue(rs.get(0) instanceof java.util.Date);

        query = em.createNamedQuery("named", QueryTest1.class);
        rs = query.getResultList();
        assertTrue(rs.get(0) instanceof QueryTest1);

        em.close();
    }
    
    PreparedQueryCache getPreparedQueryCache() {
        return emf.getConfiguration().getQuerySQLCacheInstance();
    }
}

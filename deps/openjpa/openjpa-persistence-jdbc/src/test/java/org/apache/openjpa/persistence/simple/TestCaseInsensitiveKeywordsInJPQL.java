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
package org.apache.openjpa.persistence.simple;

import javax.persistence.Query;
import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestCaseInsensitiveKeywordsInJPQL
    extends SingleEMFTestCase {

    public void setUp() {
        // expecting only data that is inserted in this test case
        setUp(AllFieldTypes.class, CLEAR_TABLES);
    }

    public void testCaseInsensitiveBooleans() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        AllFieldTypes aft = new AllFieldTypes();
        em.persist(aft);
        aft.setBooleanField(true);

        aft = new AllFieldTypes();
        em.persist(aft);
        aft.setBooleanField(false);

        em.flush();

        Query q = em.createQuery(
            "select count(o) from AllFieldTypes o where o.booleanField = TrUe");
        Number n = (Number) q.getSingleResult();
        assertEquals(1, n.intValue());

        q = em.createQuery("select count(o) from AllFieldTypes o "
            + "where o.booleanField = falSe");
        n = (Number) q.getSingleResult();
        assertEquals(1, n.intValue());
        
        em.getTransaction().rollback();
    }
}

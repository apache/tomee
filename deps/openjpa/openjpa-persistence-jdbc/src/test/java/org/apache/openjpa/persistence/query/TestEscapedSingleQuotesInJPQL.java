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

import java.util.Collection;
import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.test.SingleEMFTestCase;
import org.apache.openjpa.persistence.simple.AllFieldTypes;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAQuery;

public class TestEscapedSingleQuotesInJPQL
    extends SingleEMFTestCase {

    public void setUp() {
        setUp(AllFieldTypes.class, CLEAR_TABLES);

        AllFieldTypes aft = new AllFieldTypes();
        aft.setStringField("foo'bar");
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(aft);
        em.getTransaction().commit();
        em.close();
    }

    public void testEscapedSingleQuotesInJPQL() {
        OpenJPAEntityManager em = emf.createEntityManager();
        OpenJPAQuery q = em.createQuery("select count(o) " +
            "from AllFieldTypes o where o.stringField = 'foo''bar'");
        assertEquals(1, ((Number) q.getSingleResult()).longValue());

        Collection all = em.createQuery("select o from AllFieldTypes o")
            .getResultList();
        q.setCandidateCollection(all);
        assertEquals(1, ((Number) q.getSingleResult()).longValue());
    }
}

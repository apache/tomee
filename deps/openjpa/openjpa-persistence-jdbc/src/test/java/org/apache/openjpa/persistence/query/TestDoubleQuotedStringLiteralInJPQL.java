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
import javax.persistence.Query;

import org.apache.openjpa.persistence.simple.AllFieldTypes;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestDoubleQuotedStringLiteralInJPQL
    extends SingleEMFTestCase {

    public void setUp() {
        setUp(AllFieldTypes.class, CLEAR_TABLES);

        AllFieldTypes aft = new AllFieldTypes();
        aft.setStringField("foo'bar");
        AllFieldTypes aft2 = new AllFieldTypes();
        aft2.setStringField("foo-bar");
        AllFieldTypes aft3 = new AllFieldTypes();
        aft3.setStringField("foo\"bar");
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(aft);
        em.persist(aft2);
        em.persist(aft3);
        em.getTransaction().commit();
        em.close();
    }

    public void testdDoubleQuotedStringLiteralInJPQL() {
        EntityManager em = emf.createEntityManager();
        Query q = em.createQuery("select count(o) " +
            "from AllFieldTypes o where o.stringField = \"foo'bar\"");
        assertEquals(1, ((Number) q.getSingleResult()).longValue());
        Query q2 = em.createQuery("select count(o) " +
            "from AllFieldTypes o where o.stringField = \"foo-bar\"");
        assertEquals(1, ((Number) q2.getSingleResult()).longValue());
        Query q3 = em.createQuery("select count(o) " +
            "from AllFieldTypes o where o.stringField = 'foo\"bar'");
        assertEquals(1, ((Number) q3.getSingleResult()).longValue());
        Query q4 = em.createQuery("select count(o) " +
            "from AllFieldTypes o where o.stringField like \"%bar\"");
        assertEquals(3, ((Number) q4.getSingleResult()).longValue());
    }
}

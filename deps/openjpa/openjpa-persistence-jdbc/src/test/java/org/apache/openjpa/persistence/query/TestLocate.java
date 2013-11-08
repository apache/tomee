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

import org.apache.openjpa.persistence.test.SingleEMTestCase;

/**
 * Test for JPQL LOCATE function.
 */
public class TestLocate extends SingleEMTestCase {

    public void setUp() {
        super.setUp(SimpleEntity.class, CLEAR_TABLES);

        EntityManager em1 = emf.createEntityManager();
        em1.getTransaction().begin();
        em1.persist(new SimpleEntity("foo", "bar"));
        em1.getTransaction().commit();
        em1.close();
    }

    public void testLocate() {
        assertEquals((long) 1, em.createQuery("select count(o) from simple o " +
            "where LOCATE('bar', o.value) = 1").getSingleResult());
        assertEquals((long) 1, em.createQuery("select count(o) from simple o " +
            "where LOCATE('ar', o.value) = 2").getSingleResult());
        assertEquals((long) 1, em.createQuery("select count(o) from simple o " +
            "where LOCATE('zzz', o.value) = 0").getSingleResult());
        assertEquals((long) 1, em.createQuery("select count(o) from simple o " +
            "where LOCATE('ar', o.value, 1) = 2").getSingleResult());
        assertEquals((long) 1, em.createQuery("select count(o) from simple o " +
            "where LOCATE('ar', o.value, 2) = 2").getSingleResult());
    }

    public void testLocateInMemory() {
        List<SimpleEntity> allEntities = em.createQuery("select o from simple o", SimpleEntity.class).getResultList();
        Object inMemoryResult = em.createQuery("select LOCATE('bar', o.value) from simple o")
            .setCandidateCollection(allEntities).getSingleResult();
        assertEquals(1, inMemoryResult);
        inMemoryResult = em.createQuery("select LOCATE('ar', o.value, 2) from simple o")
            .setCandidateCollection(allEntities).getSingleResult();
        assertEquals(2, inMemoryResult);
    }
}

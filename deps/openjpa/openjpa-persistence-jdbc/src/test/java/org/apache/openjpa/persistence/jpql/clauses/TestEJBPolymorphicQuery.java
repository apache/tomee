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
package org.apache.openjpa.persistence.jpql.clauses;

import java.util.List;
import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.common.apps.RuntimeTest1;
import org.apache.openjpa.persistence.common.apps.RuntimeTest2;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;

public class TestEJBPolymorphicQuery extends AbstractTestCase {

    public TestEJBPolymorphicQuery(String name) {
        super(name, "jpqlclausescactusapp");
    }

    public void setUp() {
        deleteAll(RuntimeTest1.class);
        deleteAll(RuntimeTest2.class);

        EntityManager em = currentEntityManager();
        startTx(em);

        int run1 = 10;
        int run2 = 15;

        for (int i = 0; i < run1; i++) {
            RuntimeTest1 rt = new RuntimeTest1(i);
            rt.setStringField("foo " + i);
            em.persist(rt);
        }

        for (int i = 10; i < run2; i++) {
            em.persist(new RuntimeTest2(i));
        }

        endTx(em);
        endEm(em);
    }

    /**
     * Ensures that when a select query is ran against an entity at the top of
     * the hierarchy that the result is its instances and that of all its
     * subclass.
     */
    public void testPolymorphicSelect() {
        EntityManager em = currentEntityManager();

        List l = em.createQuery("Select object(o) from RuntimeTest1 o")
            .getResultList();

        assertNotNull(l);
        assertEquals(15, l.size());

        endEm(em);
    }

    public void testPolymorphicDelete() {
        EntityManager em = currentEntityManager();
        startTx(em);

        int l = em.createQuery("Delete from RuntimeTest1")
            .executeUpdate();

        assertNotNull(l);
        assertEquals(15, l);

        endTx(em);
        endEm(em);
    }
}

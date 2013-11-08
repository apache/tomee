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
package org.apache.openjpa.persistence.relations;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.test.SQLListenerTestCase;
import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.enhance.DynamicPersistenceCapable;

public class TestLazyManyToOne extends SQLListenerTestCase {

    private int id;

    public void setUp() {
        setUp(BasicEntity.class, UnidirectionalManyToOne.class, CLEAR_TABLES);

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        UnidirectionalManyToOne pc = new UnidirectionalManyToOne();
        pc.setRelated(new BasicEntity());
        pc.getRelated().setName("foo");
        em.persist(pc);
        em.getTransaction().commit();
        id = pc.getId();
        em.close();

        sql.clear();
    }

    public void testLazyManyToOne() {
        EntityManager em = emf.createEntityManager();
        UnidirectionalManyToOne pc = em.find(UnidirectionalManyToOne.class, id);
        if (pc instanceof PersistenceCapable // unenhanced has inefficiencies
            && !(pc instanceof DynamicPersistenceCapable))
            assertEquals(1, sql.size());
        assertNotNull(pc.getRelated());
        if (pc instanceof PersistenceCapable // unenhanced has inefficiencies
            && !(pc instanceof DynamicPersistenceCapable))
            assertEquals(2, sql.size());
        em.close();
    }

}

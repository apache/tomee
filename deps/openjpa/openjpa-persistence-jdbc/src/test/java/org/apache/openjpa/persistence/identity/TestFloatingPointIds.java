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
package org.apache.openjpa.persistence.identity;

import javax.persistence.EntityManager;

import junit.textui.TestRunner;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Test that entities can use floating point ids.
 *
 * @author Abe White
 */
public class TestFloatingPointIds
    extends SingleEMFTestCase {

    public void setUp() {
        setUp(FloatIdEntity.class, DoubleObjIdEntity.class);
    }

    public void testPersistFloat() {
        FloatIdEntity e = new FloatIdEntity();
        e.setId(3F);
        e.setData(33);

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(e);
        em.getTransaction().commit();
        assertEquals(3F, e.getId());
        em.close();

        em = emf.createEntityManager();
        e = em.find(FloatIdEntity.class, 3F);
        assertEquals(33, e.getData());
        em.close();
    }

    public void testPersistDoubleObj() {
        DoubleObjIdEntity e = new DoubleObjIdEntity();
        e.setId(new Double(4D));
        e.setData(44);

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(e);
        em.getTransaction().commit();
        assertEquals(new Double(4D), e.getId());
        em.close();

        em = emf.createEntityManager();
        e = em.find(DoubleObjIdEntity.class, new Double(4D));
        assertEquals(44, e.getData());
        em.close();
    }

    public static void main(String[] args) {
        TestRunner.run(TestFloatingPointIds.class);
    }
}


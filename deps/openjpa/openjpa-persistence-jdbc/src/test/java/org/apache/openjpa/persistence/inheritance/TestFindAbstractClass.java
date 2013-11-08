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
package org.apache.openjpa.persistence.inheritance;

import java.util.List;

import javax.persistence.EntityManager;

import junit.textui.TestRunner;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Test that you can find a concrete subclass record when passing in its
 * abstract base class to EntityManager.find().
 *
 * @author Abe White
 */
public class TestFindAbstractClass
    extends SingleEMFTestCase {

    public void setUp() {
        setUp(AbstractBase.class, ConcreteSubclass.class, CLEAR_TABLES);

        ConcreteSubclass e = new ConcreteSubclass();
        e.setId("id");
        e.setSubclassData(1); 

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(e);
        em.getTransaction().commit();
        em.close();
    }

    public void testEntityTypeInheritanceTypeJoined() {
        EntityManager em = emf.createEntityManager();
        String query = "select c from AbstractBase c where TYPE(c) = ConcreteSubclass";
        List rs = em.createQuery(query).getResultList();
        assertTrue(rs.get(0) instanceof ConcreteSubclass);
        query = "select c from AbstractBase c";
        rs = em.createQuery(query).getResultList();
        assertTrue(rs.get(0) instanceof ConcreteSubclass);
        em.close();
    }

    public void testFind() {
        EntityManager em = emf.createEntityManager();
        AbstractBase e = em.find(AbstractBase.class, "id");
        assertNotNull(e);
        assertTrue(e instanceof ConcreteSubclass);
        assertEquals(1, ((ConcreteSubclass) e).getSubclassData());
        em.close();
    }

    public static void main(String[] args) {
        TestRunner.run(TestFindAbstractClass.class);
    }
}


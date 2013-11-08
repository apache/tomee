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
package org.apache.openjpa.persistence.jdbc.strategy;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.openjpa.persistence.test.SingleEMFTestCase;


public class TestStrategyAnnotaion extends SingleEMFTestCase {

    @Override
    public void setUp() {
        super.setUp(Person.class, Address.class, CLEAR_TABLES);
    }

    public void testStrategy() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory(
            "testStrategy", System.getProperties());
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Person person = new Person();
        person.setName("parul");
        person.setAge(21);

        // no address entered for person.
        em.persist(person);
        em.getTransaction().commit();
        em.clear();

        // In case of inner join, the result set should be empty
        List pers = em.createQuery("select p from Person p").getResultList();
        assertTrue(pers.isEmpty());
    }
}


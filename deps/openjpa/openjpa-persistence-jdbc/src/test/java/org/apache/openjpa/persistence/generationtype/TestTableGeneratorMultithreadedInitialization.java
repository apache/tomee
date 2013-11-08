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
package org.apache.openjpa.persistence.generationtype;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.openjpa.persistence.test.AbstractPersistenceTestCase;

public class TestTableGeneratorMultithreadedInitialization extends AbstractPersistenceTestCase {
    Object[] props = new Object[] { Dog.class, CLEAR_TABLES };

    public void setUp() throws Exception {
    }

    public void test() throws Exception {

        EntityManagerFactory emf1 = createNamedEMF(getPersistenceUnitName(), props);
        EntityManagerFactory emf2 = createNamedEMF(getPersistenceUnitName(), props);
        EntityManagerFactory emf3 = createNamedEMF(getPersistenceUnitName(), props);

        assertNotEquals(emf1, emf2);

        emf1.createEntityManager().close();
        emf2.createEntityManager().close();

        final EntityManager em1 = emf1.createEntityManager();
        final EntityManager em2 = emf2.createEntityManager();
        final EntityManager em3 = emf3.createEntityManager();

        Worker w1 = new Worker(em1);
        Worker w2 = new Worker(em2);

        w1.start();
        w2.start();

        w1.join();
        w2.join();

        assertNull("Caught an exception in worker 1" + w1.getException(), w1.getException());
        assertNull("Caught an exception in worker 2" + w2.getException(), w2.getException());

        Dog d1 = w1.getDog();
        Dog d2 = w2.getDog();
        assertNotNull(d1);
        assertNotNull(d2);
        assertNotEquals(d1, d2);

        Dog d1_found = em3.find(Dog.class, d1.getId());
        Dog d2_found = em3.find(Dog.class, d2.getId());

        assertEquals(d1_found, d1);
        assertEquals(d2_found, d2);

        emf1.close();
        emf2.close();
        emf3.close();
    }

    class Worker extends Thread {
        final EntityManager em;
        Dog dog = new Dog();
        Exception exception;

        Worker(EntityManager e) {
            em = e;
        }

        public Dog getDog() {
            return dog;
        }

        public Exception getException() {
            return exception;
        }

        @Override
        public void run() {
            try {
                em.getTransaction().begin();
                em.persist(dog);
                em.getTransaction().commit();
                em.close();
            } catch (Exception e) {
                exception = e;
                e.printStackTrace();
                // TODO: handle exception
            }
        }
    }
}

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
package org.apache.openjpa.persistence.discriminator.fetch;

import java.util.HashSet;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.apache.openjpa.persistence.test.AbstractPersistenceTestCase;

public class TestLazyFetch extends AbstractPersistenceTestCase {
    private static int N_EMPS = 3;

    public EntityManagerFactory newEmf() {
        EntityManagerFactory emf = createEMF(Person.class, Employee.class, Manager.class);
        assertNotNull("Unable to create EntityManagerFactory", emf);
        return emf;
    }

    @Override
    public void setUp() throws Exception {
        EntityManagerFactory emf = newEmf();
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();

        // cleanup from previous execution
        tran.begin();
        em.createQuery("Delete from D_F_Manager").executeUpdate();
        em.createQuery("Delete from D_F_Employee").executeUpdate();
        tran.commit();

        // populate a small graph.
        tran.begin();
        Manager m = new Manager();
        m.setId(10);
        m.setEmployees(new HashSet<Employee>());
        em.persist(m);

        Employee e;
        for (int i = 0; i < N_EMPS; i++) {
            e = new Employee();
            e.setId(i + 1);
            e.setManager(m);
            m.getEmployees().add(e);
            em.persist(e);
        }
        tran.commit();

        em.close();
        emf.close();
    }

    @SuppressWarnings("unchecked")
    public void testFetchOneSideFirst() {
        EntityManagerFactory emf = newEmf();
        EntityManager em = emf.createEntityManager();

        List<Manager> managers = em.createQuery("Select m from D_F_Manager m").getResultList();
        assertEquals(1, managers.size());
        Manager m = managers.get(0);
        
        List<Employee> emps = em.createQuery("Select e from D_F_Employee e").getResultList();
        assertEquals(N_EMPS, emps.size());
        
        for(Employee e : emps) { 
            assertNotNull(e.getManager());
            assertTrue(m.getEmployees().contains(e));
            assertEquals(m, e.getManager());
        }
        closeEM(em);
        closeEMF(emf);
    }   
    
    @SuppressWarnings("unchecked")
    public void testFetchManySideFirst() {
        EntityManagerFactory emf = newEmf();
        EntityManager em = emf.createEntityManager();
        
        List<Employee> emps = em.createQuery("Select e from D_F_Employee e").getResultList();
        assertEquals(N_EMPS, emps.size());
        
        List<Manager> managers = em.createQuery("Select m from D_F_Manager m").getResultList();
        assertEquals(1, managers.size());
        Manager m = managers.get(0);
        
        for(Employee e : emps) { 
            assertNotNull(e.getManager());
            assertTrue(m.getEmployees().contains(e));
            assertEquals(m, e.getManager());
        }
        closeEM(em);
        closeEMF(emf);
    }
}

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
package org.apache.openjpa.persistence.inheritance.datacache;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.inheritance.entity.Department;
import org.apache.openjpa.persistence.inheritance.entity.Employee;
import org.apache.openjpa.persistence.inheritance.entity.FTEmployee;
import org.apache.openjpa.persistence.inheritance.entity.Manager;
import org.apache.openjpa.persistence.inheritance.entity.PTEmployee;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestInheritanceWithDataCache extends SingleEMFTestCase {
    Object[] props =
        new Object[] { FTEmployee.class, Employee.class, Manager.class, PTEmployee.class, Department.class,
            "openjpa.DataCache", "true", CLEAR_TABLES };

    @Override
    public void setUp() throws Exception {
        super.setUp(props);
    }

    public void test() throws Exception {
        EntityManager em = emf.createEntityManager();
        try {
            // Create a manager and a department
            em.getTransaction().begin();
            Manager m = new Manager();
            m.setId(1);
            em.persist(m);
            Department dept = new Department();
            dept.setId(1);
            dept.setDepartmentManager(m);
            em.persist(dept);
            m.setDepartment(dept);
            em.getTransaction().commit();
            em.clear();

            emf.getCache().evictAll();

            Employee e = em.find(Employee.class, 1);
            assertNotNull(e);
            assertTrue(e instanceof Manager);
            em.clear();
            e = em.find(Employee.class, 1);
            assertNotNull(e);
            assertTrue(e instanceof Manager);
        } finally {
            em.close();
        }

    }
}

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

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.inheritance.entity.Department;
import org.apache.openjpa.persistence.inheritance.entity.Employee;
import org.apache.openjpa.persistence.inheritance.entity.FTEmployee;
import org.apache.openjpa.persistence.inheritance.entity.PTEmployee;
import org.apache.openjpa.persistence.inheritance.entity.Manager;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Originally reported in the context of entities of a inheritance hierarchy with
 * JOIN_TABLE strategy.
 * 
 * <A HREF="http://issues.apache.org/jira/browse/OPENJPA-1536">OPENJPA-1536</A>
 * 
 * @author Jody Grassel
 * @author Fay Wang
 * 
 */
public class TestJoinTableStrategy extends SingleEMFTestCase {

	public void setUp() {
		super.setUp(CLEAR_TABLES, Department.class, Employee.class,
				PTEmployee.class, FTEmployee.class, Manager.class);

		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
        Manager m = new Manager();
        m.setId(1);
        m.setFirstName("mf1");
        m.setLastName("ml1");
        m.setSalary(1000000);
        m.setVacationDays(20);
                
        Department d = new Department();
        d.setId(1);
        d.setDepartmentName("d1");
        d.setDepartmentManager(m);
        m.setDepartment(d);
        
        Employee e1 = new Employee();
        e1.setId(2);
        e1.setFirstName("ef1");
        e1.setLastName("el1");
        e1.setDepartment(d);
        e1.setManager(m);
        e1.setVacationDays(20);
        
        em.persist(m);
        em.persist(d);
        em.persist(e1);
		em.getTransaction().commit();
		em.close();
	}

	public void testFindEntity() {
		EntityManager em1 = emf.createEntityManager();
        Manager m = em1.find(Manager.class, 1);
        assertNotNull(m);
		em1.close();
	}
}

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
package org.apache.openjpa.persistence.jdbc.query.cache;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import org.apache.openjpa.persistence.test.SQLListenerTestCase;

/**
 * Tests that find() queries that use non-primary keys can be cached.
 * 
 * SQL Query Cache caches SQL queries generated to select single entity.
 * However, single instance queries may also join to other relations. Hence,
 * primary key and foreign keys are normally the parameters to these queries
 * which cached query binds again when being reused.
 * 
 * The test verifies the case where non-primary keys are used as query
 * parameters. The test employs a inheritance hierarchy mapped to SINGLE_TABLE.
 * When derived instances are used in relationship, the discriminator values
 * must be used in to join to the target type.
 * 
 * For further details, refer <A
 * HREF="https://issues.apache.org/jira/browse/OPENJPA-660">OPENJPA-660</A>
 * 
 * 
 * @author Pinaki Poddar
 * @author Vikram Bhatia
 * @author David Blevins
 */
public class TestNonPrimaryKeyQueryParameters extends SQLListenerTestCase {
	private static final int FULLTIME_EMPLOYEE_COUNT = 3;
	private static final int PARTTIME_EMPLOYEE_COUNT = 2;
    private static final int LINEITEM_PER_INVOICE = 1;
	private static final String DEPT_NAME = "ENGINEERING";

	public void setUp() {
		super.setUp(CLEAR_TABLES, Department.class, Employee.class,
				FullTimeEmployee.class, PartTimeEmployee.class,
				Invoice.class, LineItem.class,
				"openjpa.jdbc.QuerySQLCache", "true");
		createDepartment(DEPT_NAME);
		createInvoice();
		sql.clear();
	}

	public void testSelectQueryWithPrimaryKeyParameter() {
		EntityManager em = emf.createEntityManager();

		Query query = em
                .createQuery("SELECT d from Department d where d.name=?1");
		query.setParameter(1, DEPT_NAME);
		Department dept = (Department) query.getSingleResult();

        assertEquals(FULLTIME_EMPLOYEE_COUNT, dept.getFullTimeEmployees()
                .size());
        assertEquals(PARTTIME_EMPLOYEE_COUNT, dept.getPartTimeEmployees()
				.size());
		assertSQL(".* AND t0.TYPE = .*");
		em.close();
	}

	public void testSelectQueryWithNoParameter() {
		EntityManager em = emf.createEntityManager();

		Query query = em.createQuery("SELECT d from Department d");

		Department dept = (Department) query.getSingleResult();

        assertEquals(FULLTIME_EMPLOYEE_COUNT, dept.getFullTimeEmployees()
                .size());
        assertEquals(PARTTIME_EMPLOYEE_COUNT, dept.getPartTimeEmployees()
				.size());

		assertSQL(".* AND t0.TYPE = .*");
		em.close();
	}

	public void testFind() {
		EntityManager em = emf.createEntityManager();

		Department dept = em.find(Department.class, DEPT_NAME);

        assertEquals(FULLTIME_EMPLOYEE_COUNT, dept.getFullTimeEmployees()
                .size());
        assertEquals(PARTTIME_EMPLOYEE_COUNT, dept.getPartTimeEmployees()
				.size());

		assertSQL(".* AND t0.TYPE = .*");
		
        Invoice invoice = em.find(Invoice.class, new InvoiceKey(1, "Red"));
        List<LineItem> list = invoice.getLineItems();
        assertEquals(LINEITEM_PER_INVOICE, list.size());
		em.close();
	}

	public void testSelectSubClass() {
		EntityManager em = emf.createEntityManager();

        Query query = em.createQuery("SELECT e from FullTimeEmployee e");
        assertEquals(FULLTIME_EMPLOYEE_COUNT,
                query.getResultList().size());

		query = em.createQuery("SELECT e from PartTimeEmployee e");
        assertEquals(PARTTIME_EMPLOYEE_COUNT,
                query.getResultList().size());

		assertSQL(".* WHERE t0.TYPE = .*");
	}

	public void testSelectBaseClass() {
		EntityManager em = emf.createEntityManager();

		Query query = em.createQuery("SELECT e from Employee e");
        assertEquals(FULLTIME_EMPLOYEE_COUNT + PARTTIME_EMPLOYEE_COUNT,
                query.getResultList().size());
		assertNotSQL(".* WHERE t0.TYPE = .*");
	}

	private void createDepartment(String deptName) {
		if (count(Department.class) > 0)
			return;

		Department dept = new Department();
		dept.setName(deptName);

		for (int i = 1; i <= FULLTIME_EMPLOYEE_COUNT; i++) {
			FullTimeEmployee e = new FullTimeEmployee();
			e.setSsn("888-PP-001" + i);
			e.setSalary(100000);
			dept.addEmployee(e);
		}
		for (int i = 1; i <= PARTTIME_EMPLOYEE_COUNT; i++) {
			PartTimeEmployee e = new PartTimeEmployee();
			e.setSsn("999-PP-001" + i);
			e.setHourlyWage(20);
			dept.addEmployee(e);
		}

		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		em.persist(dept);
		em.getTransaction().commit();
		em.close();

	}
	
    private void createInvoice() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        tran.begin();
        Invoice invoice = new Invoice(1, "Red", 1.30);
        for (int i = 1;  i <= LINEITEM_PER_INVOICE; i++) {
            LineItem item = new LineItem(String.valueOf(i), 10);
            item.setInvoice(invoice);
            invoice.getLineItems().add(item);
            em.persist(invoice);
        }
        em.flush();
        tran.commit();
        em.close();        
    }	
}

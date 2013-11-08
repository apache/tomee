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
package org.apache.openjpa.persistence.compat;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import org.apache.openjpa.conf.OpenJPAVersion;
import org.apache.openjpa.persistence.ArgumentException;
import org.apache.openjpa.persistence.test.SQLListenerTestCase;

/**
 * Tests that find() queries that use non-primary keys can be cached
 * 
 *
 * <b>Compatible testcases</b> are used to test various backwards compatibility scenarios between JPA 2.0 and JPA 1.2
 * 
 * <p>The following scenarios are tested:
 * <ol>
 * <li>query.setParameter()
 * </ol>
 * <p> 
 * <b>Note(s):</b>
 * <ul>
 * <li>The proper openjpa.Compatibility value(s) must be provided in order for the testcase(s) to succeed
 * </ul>
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

    public void testSelectQueryWithNoParameter() {
        EntityManager em = emf.createEntityManager();

        try {
            Query query = em.createQuery("SELECT d from Department d");
            query.setParameter(1, DEPT_NAME);
            Department dept = (Department) query.getSingleResult();

            if (((OpenJPAVersion.MAJOR_RELEASE == 1) &&
                 (OpenJPAVersion.MINOR_RELEASE >= 3)) ||
                (OpenJPAVersion.MAJOR_RELEASE >= 2)) {
                // should never get here, as parameter substitution should fail
                fail("Test should have failed on OpenJPA 1.3 or above.");
            } else {
                // OpenJPA 1.2.x and earlier ignored unused parameters
                assertEquals(FULLTIME_EMPLOYEE_COUNT, dept.getFullTimeEmployees().size());
                assertEquals(PARTTIME_EMPLOYEE_COUNT, dept.getPartTimeEmployees().size());
                assertSQL(".* AND t0.TYPE = .*");
            }
        } catch (ArgumentException ae) {
            if ((OpenJPAVersion.MAJOR_RELEASE == 1) &&
                 (OpenJPAVersion.MINOR_RELEASE >= 3)) {
                // expected exception for new behavior
            } else {
                // unexpected exception
                throw ae;
            }
        } catch (IllegalArgumentException iae) {
            if (OpenJPAVersion.MAJOR_RELEASE >= 2) {
                // expected exception for new behavior
            } else {
                // unexpected exception
                throw iae;
            }
        } finally {
            em.close();
        }
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


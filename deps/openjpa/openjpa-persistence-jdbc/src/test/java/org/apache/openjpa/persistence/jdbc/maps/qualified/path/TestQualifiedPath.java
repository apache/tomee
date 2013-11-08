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
package org.apache.openjpa.persistence.jdbc.maps.qualified.path;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.test.SQLListenerTestCase;

/**
 * Test queries containing qualified paths of the form:
 * <pre>
 * general_identification_variable.{single_valued_object_field.}*single_valued_object_field 
 *    or
 * general_identification_variable.{single_valued_object_field.}*collection_valued_field 
 * </pre>
 */
public class TestQualifiedPath extends SQLListenerTestCase {
    private int numDivisions = 2;
    private int numEmployeesPerDivision = 3;
    private int numMobilePhonesPerEmployee = 2;
    
    private int divisionId = 0;
    private int employeeId = 0;
    private int nameCount = 0;
    private int phoneId = 0;
    private int phoneNumber = 1234567890;
    
    OpenJPAEntityManager em;
    
    public void setUp() {
        super.setUp(CLEAR_TABLES,
            Division.class, Employee.class, Phone.class, PersonalInfo.class);
        assertNotNull(emf);
        em = emf.createEntityManager();
        assertNotNull(em);
        createObj();
    }

    public void testQueries() {
        em.clear();
        String query = "select p " + 
            " from Division d, in(d.employees) e, in(KEY(e).personalInfo.phones) p";
        Query q = em.createQuery(query);
        List<?> rs = q.getResultList();
        assertEquals(numDivisions*numEmployeesPerDivision*(2 + numMobilePhonesPerEmployee), rs.size());
       
        em.clear();
        query = "select KEY(e) " +
            "from Division d, in(d.employees) e " +
            "where KEY(e).personalInfo.lastName = 'lName2'";
        q = em.createQuery(query);
        rs = q.getResultList();
        assertEquals(1, rs.size());
        Employee employee = (Employee)rs.get(0);
        assertEquals("lName2", employee.getPersonalInfo().getLastName());
        
        em.clear();
        query = "select KEY(e) " +
            "from Division d, in(d.employees) e " +
            "order by KEY(e).personalInfo.lastName";
        q = em.createQuery(query);
        rs = q.getResultList();
        assertEquals(numDivisions * numEmployeesPerDivision, rs.size());
        employee = (Employee)rs.get(0);
        assertTrue(employee.getPersonalInfo().getLastName().equals("lName1"));
        employee = (Employee)rs.get(1);
        assertTrue(employee.getPersonalInfo().getLastName().equals("lName2"));
        
        em.clear();
        query = "select KEY(e).personalInfo.lastName " +
            "from Division d, in (d.employees) e " +
            "group by KEY(e).personalInfo.lastName " +
            "having KEY(e).personalInfo.lastName = 'lName3'";
        q = em.createQuery(query);
        rs = q.getResultList();
        assertEquals(1, rs.size());
        em.close();
        em = null;
    }
    
    private void createObj() {
        em.getTransaction().begin();
        for (int i = 0; i < numDivisions; i++) {
            createDivision(divisionId++);
        }
        em.flush();
        em.getTransaction().commit();
    }
    
    private void createDivision(int id) {
        Division division = new Division();
        division.setId(id);
        Map<Employee, String> employees = new HashMap<Employee, String>();
        for (int i = 0; i < numEmployeesPerDivision; i++) {
            Employee employee = createEmployee(employeeId++);
            employees.put(employee, employee.getPersonalInfo().getLastName());
        }
        division.setEmployees(employees);
        em.persist(division);
    }
    
    private Employee createEmployee(int id) {
        Employee employee = new Employee();
        employee.setId(id);
        
        PersonalInfo personalInfo = new PersonalInfo();
        personalInfo.setFirstName("fName" + nameCount++);
        personalInfo.setLastName("lName" + nameCount);
        
        Phone homePhone = new Phone(phoneId++, Phone.HOME, phoneNumber++);
        personalInfo.addPhone(homePhone);
        Phone officePhone = new Phone(phoneId++, Phone.OFFICE, phoneNumber++);
        personalInfo.addPhone(officePhone);
        for (int i = 0; i < numMobilePhonesPerEmployee; i++) {
            Phone mobilePhone = new Phone(phoneId++, Phone.MOBILE, phoneNumber++);
            personalInfo.addPhone(mobilePhone);
        }
        
        employee.setPersonalInfo(personalInfo);
        
        em.persist(employee);
        
        return employee;
    }

}

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
package org.apache.openjpa.jdbc.kernel;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestM21UniVersion extends SingleEMFTestCase {
    public static String SALESID = "SALES";
    public static String MARKETINGID = "MARKETING";
    
    public static String EMPLOYEE1ID = "EMPLOYEE1";
    public static String EMPLOYEE2ID = "EMPLOYEE2";
    public static String EMPLOYEE3ID = "EMPLOYEE3";
    
    
    public void setUp() {
        setUp(
                M21UniDepartment.class, 
                M21UniEmployee.class,
                CLEAR_TABLES);        
        
        createEntities();        
    }
    
    void createEntities() {        
        EntityManager em = emf.createEntityManager();
        
        em.getTransaction().begin();
        M21UniDepartment sales = new M21UniDepartment();
        sales.setDeptid(SALESID);        
        sales.setName("SALES");
        sales.setCostCode("1000");
        M21UniDepartment marketing = new M21UniDepartment();
        marketing.setDeptid(MARKETINGID);        
        marketing.setName("marketing");
        marketing.setCostCode("3000");        
        
        M21UniEmployee e1 = new M21UniEmployee();
        M21UniEmployee e2 = new M21UniEmployee();
        e1.setEmpid(EMPLOYEE1ID);
        e1.setName("Gilgamesh_1");
        e2.setEmpid(EMPLOYEE2ID);
        e2.setName("Enkidu_1");
        e1.setDepartment(sales);
        e2.setDepartment(sales);
        
        em.persist(e1);
        em.persist(e2);
        em.persist(sales);
        em.persist(marketing);
        em.flush();
        em.getTransaction().commit();
        em.close();
        
    }
    
    public void testNonRelationalFieldInverseSideVersionUpdate() {
        // Change only non-relation fields on Department.
        // Version number of Department should be updated.
        // Version numbers of Employee should not be updated.
        
        EntityManager em = emf.createEntityManager();
        M21UniDepartment sales = em.find(M21UniDepartment.class, SALESID);
        M21UniEmployee e1 = em.find(M21UniEmployee.class, EMPLOYEE1ID);
        M21UniEmployee e2 = em.find(M21UniEmployee.class, EMPLOYEE2ID);
        
        int salesVersionPre = sales.getVersion();
        int e1VersionPre = e1.getVersion();
        int e2VersionPre = e2.getVersion();
        
        em.getTransaction().begin();
        sales.setCostCode("1001");
        em.getTransaction().commit();
        em.close();
        
        em = emf.createEntityManager();
        sales = em.find(M21UniDepartment.class, SALESID);
        e1 = em.find(M21UniEmployee.class, EMPLOYEE1ID);
        e2 = em.find(M21UniEmployee.class, EMPLOYEE2ID);
        
        int salesVersionPost = sales.getVersion();
        int e1VersionPost = e1.getVersion();
        int e2VersionPost = e2.getVersion();
        em.close();
        
        assertEquals(salesVersionPost, salesVersionPre + 1);
        assertEquals(e1VersionPost, e1VersionPre);
        assertEquals(e2VersionPost, e2VersionPre);
    }


    public void testNonRelationalFieldOwnerSideVersionUpdate() {
        // Change only non-relation fields on Employee.
        // Version number of Employee should be updated.
        // Version number of Department should not change.
        EntityManager em = emf.createEntityManager();
        M21UniDepartment sales = em.find(M21UniDepartment.class, SALESID);
        M21UniEmployee e1 = em.find(M21UniEmployee.class, EMPLOYEE1ID);
        M21UniEmployee e2 = em.find(M21UniEmployee.class, EMPLOYEE2ID);
        
        int salesVersionPre = sales.getVersion();
        int e1VersionPre = e1.getVersion();
        int e2VersionPre = e2.getVersion();
        
        em.getTransaction().begin();
        e1.setName("Gilgamesh_2");
        e2.setName("Enkidu_2");
        em.getTransaction().commit();
        em.close();
        
        em = emf.createEntityManager();
        sales = em.find(M21UniDepartment.class, SALESID);
        e1 = em.find(M21UniEmployee.class, EMPLOYEE1ID);
        e2 = em.find(M21UniEmployee.class, EMPLOYEE2ID);
        
        int salesVersionPost = sales.getVersion();
        int e1VersionPost = e1.getVersion();
        int e2VersionPost = e2.getVersion();
        em.close();
        
        assertEquals(salesVersionPost, salesVersionPre);
        assertEquals(e1VersionPost, e1VersionPre + 1);
        assertEquals(e2VersionPost, e2VersionPre + 1);        
    }
    
    public void testRelationalFieldOwnerSideVersionUpdate() {
        // Assign employees to a new Department. 
        // Since there is a unidirectional ManyToOne relationship 
        // from  Employee to Department, only the Employee
        // version should be updated. Department version
        // should remain the same as before.
        
        EntityManager em = emf.createEntityManager();
        M21UniDepartment sales = em.find(M21UniDepartment.class, SALESID);
        M21UniDepartment marketing = em.find(M21UniDepartment.class,
                MARKETINGID);
        M21UniEmployee e1 = em.find(M21UniEmployee.class, EMPLOYEE1ID);
        M21UniEmployee e2 = em.find(M21UniEmployee.class, EMPLOYEE2ID);
        
        int salesVersionPre = sales.getVersion();
        int marketingVersionPre = marketing.getVersion();
        int e1VersionPre = e1.getVersion();
        int e2VersionPre = e2.getVersion();
                
        em.getTransaction().begin();        
        e1.setDepartment(marketing);
        // Don't update e2, so we can check for unchanged
        // version number for e2.        
        em.getTransaction().commit();
        em.close();
        
        em = emf.createEntityManager();
        sales = em.find(M21UniDepartment.class, SALESID);
        marketing = em.find(M21UniDepartment.class, MARKETINGID);
        e1 = em.find(M21UniEmployee.class, EMPLOYEE1ID);
        e2 = em.find(M21UniEmployee.class, EMPLOYEE2ID);
        
        int salesVersionPost = sales.getVersion();
        int marketingVersionPost = marketing.getVersion();
        int e1VersionPost = e1.getVersion();
        int e2VersionPost = e2.getVersion();
                
        em.close();
        
        assertEquals(salesVersionPost, salesVersionPre);
        assertEquals(marketingVersionPost, marketingVersionPre);
        assertEquals(e1VersionPost, e1VersionPre + 1);
        assertEquals(e2VersionPost, e2VersionPre);        
    }
}

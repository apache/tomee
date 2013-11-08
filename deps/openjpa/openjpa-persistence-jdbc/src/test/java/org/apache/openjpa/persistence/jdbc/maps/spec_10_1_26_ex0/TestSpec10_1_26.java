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
package org.apache.openjpa.persistence.jdbc.maps.spec_10_1_26_ex0;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import junit.framework.Assert;

import org.apache.openjpa.kernel.QueryImpl;
import org.apache.openjpa.persistence.test.AllowFailure;
import org.apache.openjpa.persistence.test.SQLListenerTestCase;

public class TestSpec10_1_26 extends SQLListenerTestCase {

    public int numDepartments = 2;
    public int numEmployeesPerDept = 2;
    public List<String> namedQueries = new ArrayList<String>();

    public int deptId = 1;
    public int empId = 1;

    public List<Department1> rsAllDepartment1 = null;
    public List<Department2> rsAllDepartment2 = null;
    public List<Department3> rsAllDepartment3 = null;

    public void setUp() {
        super.setUp(DROP_TABLES,
            Department1.class,
            Department2.class,
            Department3.class,
            Employee1.class,
            Employee2.class,
            Employee3.class,
            EmployeeName3.class,
            EmployeePK2.class);
        createObj();
        rsAllDepartment1 = getAll(Department1.class);
        rsAllDepartment2 = getAll(Department2.class);
        rsAllDepartment3 = getAll(Department3.class);
    }

    public void testHavingClauseWithEntityExpression() throws Exception {
        EntityManager em = emf.createEntityManager();
        Employee1 e1 = em.find(Employee1.class, 1);
        em.clear();
        String query = "select e from Department1 d, " +
            " in (d.empMap) e " +
            "group by e " +
            "having e = ?1"; 
        Query q = em.createQuery(query); 
        q.setParameter(1, e1);
        List<Employee1> rs = (List<Employee1>) q.getResultList();
        Employee1 e2 = rs.get(0);
        assertEquals(e1.getEmpId(), e2.getEmpId());

        em.clear();
        query = "select e from Department1 d, " +
            " in (d.empMap) e " +
            "group by e " +
            "having e <> ?1"; 
        q = em.createQuery(query); 
        q.setParameter(1, e1);
        rs = (List<Employee1>) q.getResultList();
        Employee1 e3 = rs.get(0);
        assertNotEquals(e1.getEmpId(), e3.getEmpId());

        em.clear();
        query = "select value(e) from Department1 d, " +
            " in (d.empMap) e " +
            "group by value(e) " +
            "having value(e) = ?1"; 
        q = em.createQuery(query); 
        q.setParameter(1, e1);
        rs = (List<Employee1>) q.getResultList();
        Employee1 e4 = rs.get(0);
        assertEquals(e1.getEmpId(), e4.getEmpId());
        em.close();
    }

    @AllowFailure
    public void testQueryInMemoryQualifiedId() throws Exception {
        queryQualifiedId(true);
    }
    
    public void testQueryQualifiedId() throws Exception {
        queryQualifiedId(false);
    }

    public void setCandidate(Query q, Class<?> clz) 
        throws Exception {
        org.apache.openjpa.persistence.QueryImpl q1 = 
            (org.apache.openjpa.persistence.QueryImpl) q;
        org.apache.openjpa.kernel.Query q2 = q1.getDelegate();
        org.apache.openjpa.kernel.QueryImpl qi = (QueryImpl) q2;
        if (clz == Department1.class)
            qi.setCandidateCollection(rsAllDepartment1);
        else if (clz == Department2.class)
            qi.setCandidateCollection(rsAllDepartment2);
        else if (clz == Department3.class)
            qi.setCandidateCollection(rsAllDepartment3);
    }

    public void queryQualifiedId(boolean inMemory) throws Exception {
        EntityManager em = emf.createEntityManager();
        String query = "select KEY(e) from Department1 d, " +
            " in (d.empMap) e";
        Query q = em.createQuery(query);
        if (inMemory) 
            setCandidate(q, Department1.class);
        List<?> rs = q.getResultList();
        Integer d = (Integer) rs.get(0);
        
        query = "select KEY(e) from Department2 d, " +
            " in (d.empMap) e";
        q = em.createQuery(query);
        if (inMemory) 
            setCandidate(q, Department2.class);
        rs = q.getResultList();
        EmployeePK2 d2 = (EmployeePK2) rs.get(0);
        
        query = "select KEY(e) from Department3 d, " +
            " in (d.emps) e";
        q = em.createQuery(query);
        if (inMemory) 
            setCandidate(q, Department3.class);
        rs = q.getResultList();
        EmployeeName3 d3 = (EmployeeName3) rs.get(0);
        
        // Check HAVING clause support for KEY
        query = "select KEY(e) from Department1 d, " +
        " in (d.empMap) e " +
        "group by KEY(e) " +
        "having KEY(e) = 2";
        q = em.createQuery(query);
        if (inMemory) 
            setCandidate(q, Department1.class);
        rs = q.getResultList();
        Integer deptId = (Integer) rs.get(0);
        assertEquals("dept id is not 2", 2, deptId.intValue());
        
        query = "select KEY(e).lName from Department3 d, " + "in (d.emps) e " + "group by KEY(e).lName "
                + "having KEY(e).lName like 'l%'";
        q = em.createQuery(query);
        if (inMemory)
            setCandidate(q, Department1.class);
        rs = q.getResultList();
        assertEquals("number of employees is not equal to numDepartments*numEmployeesPerDept", numDepartments
                * numEmployeesPerDept, rs.size());

        em.close();
    }

    public void testQueryObject() {
        queryObj();
    }

    public void createObj() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        for (int i = 0; i < numDepartments; i++)
            createDepartment1(em, deptId++);

        for (int i = 0; i < numDepartments; i++)
            createDepartment2(em, deptId++);

        for (int i = 0; i < numDepartments; i++)
            createDepartment3(em, deptId++);

        tran.begin();
        em.flush();
        tran.commit();
        em.close();
    }

    public void createDepartment1(EntityManager em, int id) {
        Department1 d = new Department1();
        d.setDeptId(id);
        Map<Integer,Employee1> empMap = new HashMap<Integer,Employee1>();
        for (int i = 0; i < numEmployeesPerDept; i++) {
            Employee1 e = createEmployee1(em, empId++);
            //d.addEmployee1(e);
            empMap.put(e.getEmpId(), e);
            e.setDepartment(d);
            em.persist(e);
        }
        d.setEmpMap(empMap);
        em.persist(d);
    }

    public Employee1 createEmployee1(EntityManager em, int id) {
        Employee1 e = new Employee1();
        e.setEmpId(id);
        return e;
    }

    public void createDepartment2(EntityManager em, int id) {
        Department2 d = new Department2();
        d.setDeptId(id);
        for (int i = 0; i < numEmployeesPerDept; i++) {
            Employee2 e = createEmployee2(em, empId++);
            d.addEmployee(e);
            e.setDepartment(d);
            em.persist(e);
        }
        em.persist(d);
    }

    public Employee2 createEmployee2(EntityManager em, int id) {
        Employee2 e = new Employee2("e" + id, new Date());
        return e;
    }

    public void createDepartment3(EntityManager em, int id) {
        Department3 d = new Department3();
        d.setDeptId(id);
        for (int i = 0; i < numEmployeesPerDept; i++) {
            Employee3 e = createEmployee3(em, empId++);
            d.addEmployee(e);
            e.setDepartment(d);
            em.persist(e);
        }
        em.persist(d);
    }

    public Employee3 createEmployee3(EntityManager em, int id) {
        Employee3 e = new Employee3();
        EmployeeName3 name = new EmployeeName3("f" + id, "l" + id);
        e.setEmpId(id);
        e.setName(name);
        return e;
    }

    public void findObj() {
        EntityManager em = emf.createEntityManager();
        Department1 d1 = em.find(Department1.class, 1);
        assertDepartment1(d1);

        Employee1 e1 = em.find(Employee1.class, 1);
        assertEmployee1(e1);

        Department2 d2 = em.find(Department2.class, 3);
        assertDepartment2(d2);

        Map empMap = d2.getEmpMap();
        Set<EmployeePK2> keys = empMap.keySet();
        for (EmployeePK2 key : keys) {
            Employee2 e2 = em.find(Employee2.class, key);
            assertEmployee2(e2);
        }

        Department3 d3 = em.find(Department3.class, 5);
        assertDepartment3(d3);

        Employee3 e3 = em.find(Employee3.class, 9);
        assertEmployee3(e3);

        em.close();
    }

    public void assertDepartment1(Department1 d) {
        int id = d.getDeptId();
        Map<Integer, Employee1> es = d.getEmpMap();
        Assert.assertEquals(2,es.size());
        Set keys = es.keySet();
        for (Object obj : keys) {
            Integer empId = (Integer) obj;
            Employee1 e = es.get(empId);
            Assert.assertEquals(empId.intValue(), e.getEmpId());
        }
    }

    public void assertDepartment2(Department2 d) {
        int id = d.getDeptId();
        Map<EmployeePK2, Employee2> es = d.getEmpMap();
        Assert.assertEquals(2,es.size());
        Set<EmployeePK2> keys = es.keySet();
        for (EmployeePK2 pk : keys) {
            Employee2 e = es.get(pk);
            Assert.assertEquals(pk, e.getEmpPK());
        }
    }	

    public void assertDepartment3(Department3 d) {
        int id = d.getDeptId();
        Map<EmployeeName3, Employee3> es = d.getEmployees();
        Assert.assertEquals(2,es.size());
        Set<EmployeeName3> keys = es.keySet();
        for (EmployeeName3 key : keys) {
            Employee3 e = es.get(key);
            Assert.assertEquals(key, e.getName());
        }
    }

    public void assertEmployee1(Employee1 e) {
        int id = e.getEmpId();
        Department1 d = e.getDepartment();
        assertDepartment1(d);
    }

    public void assertEmployee2(Employee2 e) {
        EmployeePK2 pk = e.getEmpPK();
        Department2 d = e.getDepartment();
        assertDepartment2(d);
    }

    public void assertEmployee3(Employee3 e) {
        int id = e.getEmpId();
        Department3 d = e.getDepartment();
        assertDepartment3(d);
    }

    public void queryObj() {
        queryDepartment1(emf);
        queryEmployee1(emf);
        queryDepartment2(emf);
        queryEmployee2(emf);
        queryDepartment3(emf);
        queryEmployee3(emf);
    }

    public void queryDepartment1(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        tran.begin();
        Query q = em.createQuery("select d from Department1 d");
        List<Department1> ds = q.getResultList();
        for (Department1 d : ds)
            assertDepartment1(d);

        tran.commit();
        em.close();
    }

    public void queryEmployee1(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        tran.begin();
        Query q = em.createQuery("select e from Employee1 e");
        List<Employee1> es = q.getResultList();
        for (Employee1 e : es)
            assertEmployee1(e);

        tran.commit();
        em.close();
    }

    public void queryDepartment2(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        tran.begin();
        Query q = em.createQuery("select d from Department2 d");
        List<Department2> ds = q.getResultList();
        for (Department2 d : ds)
            assertDepartment2(d);

        tran.commit();
        em.close();
    }

    public void queryEmployee2(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        tran.begin();
        Query q = em.createQuery("select e from Employee2 e");
        List<Employee2> es = q.getResultList();
        for (Employee2 e : es)
            assertEmployee2(e);

        tran.commit();
        em.close();
    }

    public void queryDepartment3(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        tran.begin();
        Query q = em.createQuery("select d from Department3 d");
        List<Department3> ds = q.getResultList();
        for (Department3 d : ds)
            assertDepartment3(d);

        tran.commit();
        em.close();
    }

    public void queryEmployee3(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        tran.begin();
        Query q = em.createQuery("select e from Employee3 e");
        List<Employee3> es = q.getResultList();
        for (Employee3 e : es)
            assertEmployee3(e);

        tran.commit();
        em.close();
    }
}

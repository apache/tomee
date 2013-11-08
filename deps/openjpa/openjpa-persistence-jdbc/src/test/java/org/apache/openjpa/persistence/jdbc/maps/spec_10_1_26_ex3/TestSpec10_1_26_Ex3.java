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
package org.apache.openjpa.persistence.jdbc.maps.spec_10_1_26_ex3;

import java.util.ArrayList;
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

public class TestSpec10_1_26_Ex3 extends SQLListenerTestCase {

    public int numDepartments = 2;
    public int numEmployeesPerDept = 2;
    public List<String> namedQueries = new ArrayList<String>();
    
    public int deptId = 1;
    public int empId = 1;
    public List rsAllDepartments = null;
    
    public void setUp() {
        super.setUp(DROP_TABLES,
            Department.class,
            Employee.class,
            EmployeeName.class);
        createObj(emf);
        rsAllDepartments = getAll(Department.class);
    }

    @AllowFailure
    public void testQueryInMemoryQualifiedId() throws Exception {
        queryQualifiedId(true);
    }
    
    public void testQueryQualifiedId() throws Exception {
        queryQualifiedId(false);
    }

    public void setCandidate(Query q, Class clz) 
        throws Exception {
        org.apache.openjpa.persistence.QueryImpl q1 = 
            (org.apache.openjpa.persistence.QueryImpl) q;
        org.apache.openjpa.kernel.Query q2 = q1.getDelegate();
        org.apache.openjpa.kernel.QueryImpl qi = (QueryImpl) q2;
        if (clz == Department.class)
            qi.setCandidateCollection(rsAllDepartments);
    }

    public void queryQualifiedId(boolean inMemory) throws Exception {
        EntityManager em = emf.createEntityManager();
        String query = "select KEY(e) from Department d, " +
            " in (d.emps) e order by d.deptId, e.empId";
        Query q = em.createQuery(query);
        if (inMemory) 
            setCandidate(q, Department.class);
        List rs = q.getResultList();
        EmployeeName d = (EmployeeName) rs.get(0);

        em.clear();
        query = "select ENTRY(e) from Department d, " +
            " in (d.emps) e order by d.deptId, e.empId";
        q = em.createQuery(query);
        if (inMemory) 
            setCandidate(q, Department.class);
        rs = q.getResultList();
        Map.Entry me = (Map.Entry) rs.get(0);

        assertTrue(d.equals(me.getKey()));
        
        // test ORDER BY qualified path
        query = "select KEY(e), KEY(e).fName from Department d, " +
            " in (d.emps) e order by KEY(e).fName";
        q = em.createQuery(query);
        if (inMemory) 
            setCandidate(q, Department.class);
        rs = q.getResultList();
        d = (EmployeeName) ((Object[]) rs.get(0))[0];
        String fname = (String) ((Object[]) rs.get(0))[1];

        assertEquals(d.getFName(), fname);

        // test GROUP BY qualified path
        sql.clear();
        query = "select COUNT(KEY(e).fName) from Department d " +
            " left join d.emps e GROUP BY KEY(e).fName";
        q = em.createQuery(query);
        if (inMemory) 
            setCandidate(q, Department.class);
        rs = q.getResultList();
        if (!inMemory)
            assertTrue(sql.get(0).toUpperCase().indexOf(" GROUP BY ") != -1);

        query = "select KEY(e) from Department d, " +
            " in (d.emps) e where VALUE(e).department.deptId = 1" +
            " ORDER BY KEY(e).fName";
        q = em.createQuery(query);
        if (inMemory) 
            setCandidate(q, Department.class);
        rs = q.getResultList();
        if (!inMemory)
            assertEquals(((EmployeeName) rs.get(0)).getFName(), "f1");

        query = "select KEY(e) from Department d, " +
            " in (d.emps) e where VALUE(e).name.fName = 'f1'" +
            " ORDER BY KEY(e).fName";
        q = em.createQuery(query);
        if (inMemory) 
            setCandidate(q, Department.class);
        rs = q.getResultList();
        if (!inMemory)
            assertEquals(((EmployeeName) rs.get(0)).getFName(), "f1");

        query = "select KEY(e) from Department d, " +
            " in (d.emps) e where SUBSTRING(VALUE(e).name.fName, 1) = 'f1'" +
            " ORDER BY KEY(e).fName";
        q = em.createQuery(query);
        if (inMemory) 
            setCandidate(q, Department.class);
        rs = q.getResultList();
        if (!inMemory)
            assertEquals(((EmployeeName) rs.get(0)).getFName(), "f1");

        query = "select KEY(e) from Department d, " +
            " in (d.emps) e where LOCATE(VALUE(e).name.fName, 'f1') <> 0" +
            " ORDER BY KEY(e).fName";
        q = em.createQuery(query);
        if (inMemory) 
            setCandidate(q, Department.class);
        rs = q.getResultList();
        if (!inMemory)
            assertEquals(((EmployeeName) rs.get(0)).getFName(), "f1");

        em.close();
    }

    public void testQueryObject() {
        queryObj(emf);
    }

    public void createObj(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        for (int i = 0; i < numDepartments; i++)
            createDepartment(em, deptId++);
        tran.begin();
        em.flush();
        tran.commit();
        em.close();
    }

    public void createDepartment(EntityManager em, int id) {
        Department d = new Department();
        d.setDeptId(id);
        for (int i = 0; i < numEmployeesPerDept; i++) {
            Employee e = createEmployee(em, empId++);
            d.addEmployee(e);
            e.setDepartment(d);
            em.persist(e);
        }
        em.persist(d);
    }

    public Employee createEmployee(EntityManager em, int id) {
        Employee e = new Employee();
        EmployeeName name = new EmployeeName("f" + id, "l" + id);
        e.setEmpId(id);
        e.setName(name);
        return e;
    }

    public void findObj(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        Department d = em.find(Department.class, 1);
        assertDepartment(d);

        //Employee e = em.find(Employee.class, 1);
        //assertEmployee(e);

        updateObj(em, d);
        deleteObj(em, d);
        em.close();
    }

    public void updateObj(EntityManager em, Department d) {
        EntityTransaction tran = em.getTransaction();
        // add an element
        tran.begin();
        Employee e = createEmployee(
                em, numDepartments * numEmployeesPerDept + 1);
        d.addEmployee(e);
        e.setDepartment(d);
        em.persist(d);
        em.persist(e);
        em.flush();
        tran.commit();

        // remove an element
        tran.begin();
        d.removeEmployee(e.getName());
        e.setDepartment(null);
        em.persist(d);
        em.flush();
        tran.commit();
    }

    public void deleteObj(EntityManager em, Department d) {
        EntityTransaction tran = em.getTransaction();
        tran.begin();
        em.remove(d);
        tran.commit();
    }

    public void assertDepartment(Department d) {
        int id = d.getDeptId();
        Map<EmployeeName, Employee> es = d.getEmployees();
        Assert.assertEquals(2,es.size());
        Set keys = es.keySet();
        for (Object obj : keys) {
            EmployeeName empName = (EmployeeName) obj;
            Employee e = (Employee)es.get(empName);
            Assert.assertEquals(empName, e.getName());
        }
    }

    public void queryObj(EntityManagerFactory emf) {
        queryDepartment(emf);
        queryEmployee(emf);
    }

    public void queryDepartment(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        tran.begin();
        Query q = em.createQuery("select d from Department d");
        List<Department> ds = q.getResultList();
        for (Department d : ds){
            assertDepartment(d);
        }
        tran.commit();
        em.close();
    }

    public void queryEmployee(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        tran.begin();
        Query q = em.createQuery("select e from Employee e");
        List<Employee> es = q.getResultList();
        for (Employee e : es){
            assertEmployee(e);
        }
        tran.commit();
        em.close();
    }

    public void assertEmployee(Employee e) {
        int id = e.getEmpId();
        Department d = e.getDepartment();
        assertDepartment(d);
    }
}

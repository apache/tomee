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
package org.apache.openjpa.persistence.enhance.identity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestMapsIdXml extends SingleEMFTestCase {
    public int numEmployees = 4;
    public int numDependentsPerEmployee = 2;
    public int numPersons = 4;

    public Map<Integer, Employee1Xml> emps1xml = new HashMap<Integer, Employee1Xml>();
    public Map<String, Dependent1Xml> deps1xml = new HashMap<String, Dependent1Xml>();
    public int eId1 = 1;
    public int dId1 = 1;

    public void setUp() throws Exception {
        super.setUp(CLEAR_TABLES);
    }
    
    @Override
    protected String getPersistenceUnitName() {
        return "mapsId-pu";
    }
    
    
    /**
     * This is spec 2.4.1.2 Example 1, case(b)
     */
    public void testMapsId1Xml() {
        createObj1Xml();
        findObj1Xml();
        queryObj1Xml();
    }

    public void createObj1Xml() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        for (int i = 0; i < numEmployees; i++)
            createEmployee1Xml(em, eId1++);
        tran.begin();
        em.flush();
        tran.commit();
        em.close();
    }

    public Employee1Xml createEmployee1Xml(EntityManager em, int id) {
        Employee1Xml e = new Employee1Xml();
        e.setEmpId(id);
        e.setName("emp_" + id);
        for (int i = 0; i < numDependentsPerEmployee; i++) {
            Dependent1Xml d = createDependent1Xml(em, dId1++, e);
            e.addDependent(d);
            em.persist(d);
        }
        em.persist(e);
        emps1xml.put(id, e);
        return e;
    }

    public Dependent1Xml createDependent1Xml(EntityManager em, int id, Employee1Xml e) {
        Dependent1Xml d = new Dependent1Xml();
        DependentId1Xml did = new DependentId1Xml();
        did.setName("dep_" + id);
        d.setId(did);
        d.setEmp(e);
        deps1xml.put(did.getName(), d);
        return d;
    }

    public void findObj1Xml() {
        EntityManager em = emf.createEntityManager();
        Employee1Xml e = em.find(Employee1Xml.class, 1);
        List<Dependent1Xml> ds = e.getDependents();
        assertEquals(numDependentsPerEmployee, ds.size());
        Employee1Xml e0 = emps1xml.get(1);
        assertEquals(e0, e);
        em.close();
    }

    public void queryObj1Xml() {
        queryDependent1Xml();
    }

    public void queryDependent1Xml() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        tran.begin();
        String jpql = "select d from Dependent1Xml d where d.id.name = 'dep_1' "
            + "AND d.emp.name = 'emp_1'";
        Query q = em.createQuery(jpql);
        List<Dependent1Xml> ds = q.getResultList();
        for (Dependent1Xml d : ds) {
            assertDependent1Xml(d);
        }
        tran.commit();
        em.close();
    }

    public void assertDependent1Xml(Dependent1Xml d) {
        DependentId1Xml id = d.getId();
        Dependent1Xml d0 = deps1xml.get(id.getName());
        if (d0.id.empPK == 0)
            d0.id.empPK = d0.emp.getEmpId();
        assertEquals(d0, d);
    }
}

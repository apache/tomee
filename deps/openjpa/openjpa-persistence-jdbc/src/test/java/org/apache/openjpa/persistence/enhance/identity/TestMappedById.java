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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.kernel.StateManagerImpl;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;
import org.apache.openjpa.util.ObjectId;

public class TestMappedById extends SingleEMFTestCase {
    public int numEmployees = 4;
    public int numDependentsPerEmployee = 2;
    public int numPersons = 4;

    public Map<Integer, Employee1> emps1 = new HashMap<Integer, Employee1>();
    public Map<String, Dependent1> deps1 = new HashMap<String, Dependent1>();
    public Map<Integer, Employee2> emps2 = new HashMap<Integer, Employee2>();
    public Map<String, Dependent2> deps2 = new HashMap<String, Dependent2>();
    public Map<String, Person1> persons1 = new HashMap<String, Person1>();
    public Map<String, MedicalHistory1> medicals1 = 
        new HashMap<String, MedicalHistory1>();
    public Map<String, Person2> persons2 = new HashMap<String, Person2>();
    public Map<String, MedicalHistory2> medicals2 = 
        new HashMap<String, MedicalHistory2>();
    public Map<String, Person3> persons3 = new HashMap<String, Person3>();
    public Map<String, MedicalHistory3> medicals3 = new HashMap<String, 
        MedicalHistory3>();
    public Map<String, Person4> persons4 = new HashMap<String, Person4>();
    public Map<String, MedicalHistory4> medicals4 =
        new HashMap<String, MedicalHistory4>();

    public Map<Integer, Employee3> emps3 = new HashMap<Integer, Employee3>();
    public Map<Object, Dependent3> depMap3 = 
    	new HashMap<Object, Dependent3>();
    public List dids3 = new ArrayList();
    public List<Dependent3> deps3 = new ArrayList<Dependent3>();
    
    public int eId1 = 1;
    public int dId1 = 1;
    public int eId2 = 1;
    public int dId2 = 1;
    public int eId3 = 1;
    public int eId4 = 1;
    public int dId4 = 1;
    public int dId3 = 1;
    public int pId1 = 1;
    public int mId1 = 1;
    public int pId2 = 1;
    public int mId2 = 1;
    public int pId3 = 1;
    public int mId3 = 1;
    public int pId4 = 1;
    public int mId4 = 1;

    public void setUp() throws Exception {
        super.setUp(DROP_TABLES, Dependent1.class, Employee.class, Employee1.class, 
            DependentId1.class, Dependent2.class, Employee2.class,
            DependentId2.class, EmployeeId2.class, MedicalHistory1.class,
            Person1.class, PersonId1.class, MedicalHistory2.class,
            Person2.class, Person3.class, MedicalHistory3.class, 
            Person4.class, PersonId4.class, MedicalHistory4.class,
            Dependent3.class, Employee3.class, DependentId3.class, 
            Parent3.class, Dependent4.class, Employee4.class, PhoneNumber.class,
            BeneContact.class, BeneContactId.class, Beneficiary.class,
            Dependent5.class, Employee5.class, EmployeeId5.class);
    }

    /**
     * This is spec 2.4.1.2 Example 1, case(b)
     */
    public void testMapsId1() {
        createObj1();
        findObj1();
        queryObj1();
    }

    /**
     * This is spec 2.4.1.2 Example 3, case(b)
     */
    public void testMapsId2() {
        createObj2();
        findObj2();
        queryObj2();
    }
    
    /**
     * This is spec 2.4.1.2 Example 5, case(b)
     */
    public void testMapsId3() {
        createObj3();
        findObj3();
        queryObj3();
    }

    /**
     * This is a variation of spec 2.4.1.2 Example 4, case(b) with generated key
     */
    public void testMapsId4() {
        // Not all databases support GenerationType.IDENTITY column(s)
        if (!((JDBCConfiguration) emf.getConfiguration()).
            getDBDictionaryInstance().supportsAutoAssign) {
            return;
        }
        createObj4();
        queryObj4();
    }

    /**
     * This is a variation of spec 2.4.1.2 Example 1, case(b):
     * two MapsId annotations in Dependent3 and both parent
     * classes use generated key 
     */
    public void testMapsId5() {
        // Not all databases support GenerationType.IDENTITY column(s)
        if (!((JDBCConfiguration) emf.getConfiguration()).
            getDBDictionaryInstance().supportsAutoAssign) {
			return;
        }
        createObj5();
        findObj5();
        queryObj5();
    }

    /**
     * This is spec 2.4.1.2 Example 5, case(a)
     */
    public void testMapsId6() {
        createObj6();
        findObj6();
        queryObj6();
    }

    /**
     * This is spec 2.4.1.2 Example 6, case(a)
     */
    public void testMapsId7() {
        createObj7();
        findObj7();
        queryObj7();
    }

    public void testEnumIdClass() {
        EntityManager em = emf.createEntityManager();
        Employee e = new Employee();
        e.setEmpId(1);
        e.setEmpType(Employee.EmpType.A1);
        PhoneNumber phoneNumber = new PhoneNumber();
        phoneNumber.setPhNumber(1);
        phoneNumber.setEmp(e);
        e.setPhoneNumber(phoneNumber);
        em.persist(phoneNumber);
        em.persist(e);
        EntityTransaction tran = em.getTransaction();
        tran.begin();
        em.flush();
        tran.commit();
        em.clear();
        PhoneNumber p = em.find(PhoneNumber.class, 1);
        Employee emp = p.getEmp();
        assertEquals(1, emp.getEmpId());
        em.close();
    }
    
    public void testEmbeddedIdNestedInIdClass() {
        EntityManager em = emf.createEntityManager();
        EmployeeId5 eId1 = new EmployeeId5("Java", "Duke");
        Employee5 employee1 = new Employee5(eId1);
        Dependent5 dep1 = new Dependent5("1", employee1);

        em.persist(dep1);
        em.persist(employee1);

        em.getTransaction().begin();
        em.flush();
        em.getTransaction().commit();
        em.clear();
        
        DependentId5 depId1 = new DependentId5("1", eId1);
        Dependent5 newDep = em.find(Dependent5.class, depId1);
        assertNotNull(newDep);
        em.getTransaction().begin();
        em.remove(newDep);
        em.getTransaction().commit();
        newDep = em.find(Dependent5.class, depId1);
        assertNull(newDep);        
        em.close();
    }
    
    public void testCountDistinctMultiCols() {
        EntityManager em = emf.createEntityManager(); 

        Employee2 emp1 = new Employee2();
        EmployeeId2 empId1 = new EmployeeId2();
        empId1.setFirstName("James");
        empId1.setLastName("Bond");
        emp1.setEmpId(empId1);
        
        Employee2 emp2 = new Employee2();
        EmployeeId2 empId2 = new EmployeeId2();
        empId2.setFirstName("James");
        empId2.setLastName("Obama");
        emp2.setEmpId(empId2);
        
        Dependent2 dep1 = new Dependent2();
        DependentId2 depId1 = new DependentId2();
        depId1.setEmpPK(empId1);
        depId1.setName("Alan");
        dep1.setId(depId1);
        
        Dependent2 dep2 = new Dependent2();
        DependentId2 depId2 = new DependentId2();
        depId2.setEmpPK(empId2);
        depId2.setName("Darren");
        dep2.setId(depId2);
        
        em.persist(emp1);
        em.persist(emp2);
        em.persist(dep1);
        em.persist(dep2);
        
        em.getTransaction().begin();
        em.flush();        
        em.getTransaction().commit();
        
        String[] jpqls = {
            "SELECT COUNT (DISTINCT d2.emp) FROM Dependent2 d2",
            "select count (DISTINCT d2) from Dependent2 d2",
        };
        
        for (int i = 0; i < jpqls.length; i++) {
            Query q = em.createQuery(jpqls[i]) ;
            Long o = (Long)q.getSingleResult();
            int count = (int)o.longValue();
            assertEquals(2, count);
        }
        em.close();
    }

    public void createObj1() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        for (int i = 0; i < numEmployees; i++)
            createEmployee1(em, eId1++);
        tran.begin();
        em.flush();
        tran.commit();
        em.close();
    }

    public Employee1 createEmployee1(EntityManager em, int id) {
        Employee1 e = new Employee1();
        e.setEmpId(id);
        e.setName("emp_" + id);
        for (int i = 0; i < numDependentsPerEmployee; i++) {
            Dependent1 d = createDependent1(em, dId1++, e);
            e.addDependent(d);
            em.persist(d);
        }
        em.persist(e);
        emps1.put(id, e);
        return e;
    }

    public Dependent1 createDependent1(EntityManager em, int id, Employee1 e) {
        Dependent1 d = new Dependent1();
        DependentId1 did = new DependentId1();
        did.setName("dep_" + id);
        d.setId(did);
        d.setEmp(e);
        deps1.put(did.getName(), d);
        return d;
    }

    public void findObj1() {
        EntityManager em = emf.createEntityManager();
        Employee1 e = em.find(Employee1.class, 1);
        List<Dependent1> ds = e.getDependents();
        assertEquals(numDependentsPerEmployee, ds.size());
        Employee1 e0 = emps1.get(1);
        assertEquals(e0, e);
        em.close();
    }

    public void queryObj1() {
        queryDependent1();
    }

    public void queryDependent1() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        tran.begin();
        String jpql = "select d from Dependent1 d where d.id.name = 'dep_1' "
            + "AND d.emp.name = 'emp_1'";
        Query q = em.createQuery(jpql);
        List<Dependent1> ds = q.getResultList();
        for (Dependent1 d : ds) {
            assertDependent1(d);
        }
        tran.commit();
        em.close();
    }

    public void assertDependent1(Dependent1 d) {
        DependentId1 id = d.getId();
        Dependent1 d0 = deps1.get(id.getName());
        if (d0.id.empPK == 0)
            d0.id.empPK = d0.emp.getEmpId();
        assertEquals(d0, d);
    }
    
    public void createObj2() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        for (int i = 0; i < numEmployees; i++)
            createEmployee2(em, eId2++);
        tran.begin();
        em.flush();
        tran.commit();
        em.close();
    }
    
    public Employee2 createEmployee2(EntityManager em, int id) {
        Employee2 e = new Employee2();
        e.setEmpId(new EmployeeId2("f_" + id, "l_" + id));
        for (int i = 0; i < numDependentsPerEmployee; i++) {
            Dependent2 d = createDependent2(em, dId2++, e);
            e.addDependent(d);
            em.persist(d);
        }
        em.persist(e);
        emps2.put(id, e);
        return e;
    }

    public Dependent2 createDependent2(EntityManager em, int id, Employee2 e) {
        Dependent2 d = new Dependent2();
        DependentId2 did = new DependentId2();
        did.setName("dep_" + id);
        d.setEmp(e);
        d.setId(did);
        em.persist(d);
        deps2.put(did.getName(), d);
        return d;
    }

    public void findObj2() {
        EntityManager em = emf.createEntityManager();
        Employee2 e = em.find(Employee2.class, new EmployeeId2("f_1", "l_1"));
        List<Dependent2> ds = e.getDependents();
        assertEquals(numDependentsPerEmployee, ds.size());
        Employee2 e0 = emps2.get(1);
        assertEquals(e0, e);
        em.close();
    }

    public void queryObj2() {
        queryDependent2();
    }
    public void queryDependent2() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        String jpql = "select d from Dependent2 d where d.id.name = 'dep_1' "
            + "AND d.id.empPK.firstName = 'f_1'";
        Query q = em.createQuery(jpql);
        List<Dependent2> ds = q.getResultList();
        for (Dependent2 d : ds) {
            assertDependent2(d);
        }
        
        jpql = "select d from Dependent2 d where d.id.name = 'dep_1' "
            + "AND d.emp.empId.firstName = 'f_1'";
        q = em.createQuery(jpql);
        ds = q.getResultList();
        for (Dependent2 d : ds) {
            assertDependent2(d);
        }        
        em.close();
    }

    public void assertDependent2(Dependent2 d) {
        DependentId2 did = d.getId();
        Dependent2 d0 = deps2.get(did.getName());
        DependentId2 did0 = d0.getId();
        did0.setEmpPK(d0.getEmp().getEmpId());
        assertEquals(d0, d);
    }
    
    public void createObj3() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        for (int i = 0; i < numPersons; i++)
            createPerson1(em, pId1++);
        tran.begin();
        em.flush();
        tran.commit();
        em.close();
    }

    public Person1 createPerson1(EntityManager em, int id) {
        Person1 p = new Person1();
        PersonId1 pid = new PersonId1();
        pid.setFirstName("f_" + id);
        pid.setLastName("l_" + id);
        p.setId(pid);
        MedicalHistory1 m = createMedicalHistory1(em, mId1++);
        m.setPatient(p);
        p.setMedical(m);
        em.persist(m);
        em.persist(p);
        persons1.put(pid.getFirstName(), p);
        medicals1.put(m.getPatient().getId().getFirstName(), m);
        return p;
    }

    public MedicalHistory1 createMedicalHistory1(EntityManager em, int id) {
        MedicalHistory1 m = new MedicalHistory1();
        m.setName("medical_" + id);
        return m;
    }

    public void findObj3() {
        EntityManager em = emf.createEntityManager();
        PersonId1 pid = new PersonId1();
        pid.setFirstName("f_1");
        pid.setLastName("l_1");
        Person1 p = em.find(Person1.class, pid);
        Person1 p0 = persons1.get(pid.getFirstName());
        assertEquals(p0, p);
        em.close();
    }

    public void queryObj3() {
        queryMedicalHistory1();
    }

    public void queryMedicalHistory1() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        String firstName = "f_1";
        tran.begin();
        String jpql =
            "select m from MedicalHistory1 m where m.patient.id.firstName = '"
            + firstName + "'";
        Query q = em.createQuery(jpql);
        List<MedicalHistory1> ms = q.getResultList();
        for (MedicalHistory1 m : ms) {
            assertMedicalHistory1(m, firstName);
        }
        
        jpql = "select m from MedicalHistory1 m where m.id.firstName = '"
            + firstName + "'";
        q = em.createQuery(jpql);
        ms = q.getResultList();
        for (MedicalHistory1 m : ms) {
            assertMedicalHistory1(m, firstName);
        }
        
        tran.commit();
        em.close();
    }

    public void assertMedicalHistory1(MedicalHistory1 m, String firstName) {
        MedicalHistory1 m0 = medicals1.get(firstName);
        assertEquals(m0, m);
    }
    
    public void createObj4() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        for (int i = 0; i < numPersons; i++)
            createPerson2(em, pId2++);
        tran.begin();
        em.flush();
        tran.commit();
        em.close();
    }

    public Person2 createPerson2(EntityManager em, int id) {
        Person2 p = new Person2();
        p.setName("p_" + id);
        
        MedicalHistory2 m = createMedicalHistory2(em, mId2++);
        m.setPatient(p);  // automatically set the id
        p.setMedical(m);
        em.persist(m);
        medicals2.put(m.getName(), m);

        em.persist(p);
        persons2.put(p.getName(), p);
        return p;
    }

    public MedicalHistory2 createMedicalHistory2(EntityManager em, int id) {
        MedicalHistory2 m = new MedicalHistory2();
        m.setName("medical_" + id);
        return m;
    }

    public void findObj4(long ssn) {
        EntityManager em = emf.createEntityManager();
        Person2 p = em.find(Person2.class, ssn);
        Person2 p1 = p.getMedical().getPatient();
        assertEquals(p1, p);
        em.close();
    }

    public void queryObj4() {
        queryMedicalHistory4();
    }

    public void queryMedicalHistory4() {
        EntityManager em = emf.createEntityManager();
        Map medicals = new HashMap();
        long ssn = 0;
        EntityTransaction tran = em.getTransaction();
        tran.begin();
        String jpql = "select m from MedicalHistory2 m";
        Query q = em.createQuery(jpql);
        List<MedicalHistory2> ms = q.getResultList();
        for (MedicalHistory2 m : ms) {
            ssn = m.getId();
        }
        tran.commit();
        em.close();
        
        em = emf.createEntityManager();
        tran = em.getTransaction();
        tran.begin();
        jpql = "select m from MedicalHistory2 m where m.patient.ssn = " + ssn;
        q = em.createQuery(jpql);
        ms = q.getResultList();
        for (MedicalHistory2 m : ms) {
            assertMedicalHistory2(m);
        }
        tran.commit();
        em.close();
        
        findObj4(ssn);
    }

    public void assertMedicalHistory2(MedicalHistory2 m) {
        String name = m.getName();
        MedicalHistory2 m0 = medicals2.get(name);
        MedicalHistory2 m1 = m.getPatient().getMedical();
        assertEquals(m1, m);
    }
    
    public void createObj5() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        for (int i = 0; i < numEmployees; i++)
            createEmployee3(em, eId3++);
        tran.begin();
        em.flush();
        tran.commit();
        for (Dependent3 d: deps3) {
            ObjectId did = (ObjectId)((StateManagerImpl)((PersistenceCapable)d)
        	        .pcGetStateManager()).getObjectId();
        	dids3.add(did.getId());
        	depMap3.put(did.getId(), d);
        }
        
        em.close();
    }

    public Employee3 createEmployee3(EntityManager em, int id) {
        Employee3 e = new Employee3();
        e.setName("emp_" + id);
        for (int i = 0; i < numDependentsPerEmployee; i++) {
            Dependent3 d = createDependent3(em, dId3++, e);
            e.addDependent(d);
            em.persist(d);
        }
        em.persist(e);
        emps3.put(id, e);
        return e;
    }

    public Dependent3 createDependent3(EntityManager em, int id, Employee3 e) {
        Dependent3 d = new Dependent3();
        DependentId3 did = new DependentId3();
        did.setName("dep_" + id);
        d.setId(did);
        d.setEmp(e);
        deps3.add(d);
        Parent3 p = new Parent3();
        p.setName("p_" + id);
        p.setDependent(d);
        d.setParent(p);
        em.persist(p);
        return d;
    }

    public void findObj5() {
        EntityManager em = emf.createEntityManager();
        Dependent3 d = em.find(Dependent3.class, dids3.get(1));
        Dependent3 d0 = depMap3.get(dids3.get(1));
        assertEquals(d0, d);
        em.close();
    }

    public void queryObj5() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        tran.begin();
        String jpql = "select d from Dependent3 d where d.id.name = 'dep_1' "
            + "AND d.emp.name = 'emp_1'";
        Query q = em.createQuery(jpql);
        List<Dependent3> ds = q.getResultList();
        for (Dependent3 d : ds) {
            assertDependent3(d);
        }
        tran.commit();
        em.close();
    }

    public void assertDependent3(Dependent3 d) {
        DependentId3 id = d.getId();
        Dependent3 d0 = depMap3.get(id);
        if (d0.id.empPK == 0)
            d0.id.empPK = d0.emp.getEmpId();
        assertEquals(d0, d);
    }
    
    public void createObj6() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        for (int i = 0; i < numPersons; i++)
            createPerson3(em, pId3++);
        tran.begin();
        em.flush();
        tran.commit();
        em.close();
    }

    public Person3 createPerson3(EntityManager em, int id) {
        Person3 p = new Person3();
        p.setFirstName("f_" + id);
        p.setLastName("l_" + id);
        MedicalHistory3 m = createMedicalHistory3(em, mId3++);
        m.setPatient(p);
        p.setMedical(m);
        em.persist(m);
        em.persist(p);
        persons3.put(p.getFirstName(), p);
        medicals3.put(m.getPatient().getFirstName(), m);
        return p;
    }

    public MedicalHistory3 createMedicalHistory3(EntityManager em, int id) {
        MedicalHistory3 m = new MedicalHistory3();
        m.setName("medical_" + id);
        return m;
    }

    public void findObj6() {
        EntityManager em = emf.createEntityManager();
        Person3 p = em.find(Person3.class, new PersonId3("f_1", "l_1"));
        Person3 p0 = persons3.get("f_1");
        Person3 p1 = p.getMedical().getPatient();
        assertEquals(p, p1);
        em.clear();
        
        MedicalHistory3 m = em.find(MedicalHistory3.class, new PersonId3("f_1", "l_1"));
        MedicalHistory3 m0 = medicals3.get("f_1");
        assertEquals(m, m0);
        
        em.getTransaction().begin();
        em.remove(m);
        em.getTransaction().commit();
        em.close();
    }

    public void queryObj6() { 
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        tran.begin();
        String firstName = "f_1";
        String jpql =
            "select m from MedicalHistory3 m where m.patient.firstName = '"
            + firstName + "'";
        Query q = em.createQuery(jpql);
        List<MedicalHistory3> ms = q.getResultList();
        for (MedicalHistory3 m : ms) {
            assertMedicalHistory3(m, firstName);
        }
        tran.commit();
        em.close();
    }

    public void assertMedicalHistory3(MedicalHistory3 m, String firstName) {
        MedicalHistory3 m0 = medicals3.get(firstName);
        assertEquals(m0, m);
    }
    
    public void createObj7() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        for (int i = 0; i < numPersons; i++)
            createPerson4(em, pId4++);
        tran.begin();
        em.flush();
        tran.commit();
        em.close();
    }

    public Person4 createPerson4(EntityManager em, int id) {
        Person4 p = new Person4();
        p.setId(new PersonId4("f_" + id, "l_" + id));
        MedicalHistory4 m = createMedicalHistory4(em, mId4++);
        m.setPatient(p);
        p.setMedical(m);
        em.persist(p);
        em.persist(m);
        persons4.put(p.getId().getFirstName(), p);
        medicals4.put(m.getPatient().getId().getFirstName(), m);
        return p;
    }

    public MedicalHistory4 createMedicalHistory4(EntityManager em, int id) {
        MedicalHistory4 m = new MedicalHistory4();
        m.setName("medical_" + id);
        return m;
    }

    public void findObj7() {
        EntityManager em = emf.createEntityManager();
        Person4 p = em.find(Person4.class, new PersonId4("f_1", "l_1"));
        Person4 p0 = persons4.get("f_1");
        Person4 p1 = p.getMedical().getPatient();
        assertEquals(p1, p);
        em.clear();
        
        MedicalHistory4 m = em.find(MedicalHistory4.class, new PersonId4("f_1", "l_1"));
        MedicalHistory4 m0 = medicals4.get("f_1");
        assertEquals(m, m0);
        em.close();
    }

    public void queryObj7() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        tran.begin();
        String firstName = "f_1";
        String jpql =
            "select m from MedicalHistory4 m where m.patient.id.firstName = '"
            + firstName + "'";
        Query q = em.createQuery(jpql);
        List<MedicalHistory4> ms = q.getResultList();
        for (MedicalHistory4 m : ms) {
            assertMedicalHistory4(m, firstName);
        }
        tran.commit();
        em.close();
    }

    public void assertMedicalHistory4(MedicalHistory4 m, String firstName) {
        MedicalHistory4 m0 = medicals4.get(firstName);
        MedicalHistory4 m1 = m.getPatient().getMedical();
        assertEquals(m1, m);
    }
    
    /**
     * Derived Identity with IdClass and generatedKey
     */
    public void testPersistDerivedIdentityUsingIdClassAndGeneratedKey() {
        // Not all databases support GenerationType.IDENTITY column(s)
        if (!((JDBCConfiguration) emf.getConfiguration()).
            getDBDictionaryInstance().supportsAutoAssign) {
			return;
        }
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        for (int i = 0; i < numEmployees; i++)
            persistEmployee4(em, eId4++);
        tran.begin();
        em.flush();
        tran.commit();
        em.close();
    }

    /**
     * Derived Identity with IdClass and generatedKey
     */
    public void testMergeDerivedIdentityUsingIdClassAndGeneratedKey() {
        // Not all databases support GenerationType.IDENTITY column(s)
        if (!((JDBCConfiguration) emf.getConfiguration()).
            getDBDictionaryInstance().supportsAutoAssign) {
			return;
        }
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        for (int i = 0; i < numEmployees; i++)
            mergeEmployee4(em, eId4++);
        tran.begin();
        em.flush();
        tran.commit();
        em.close();
    }

    public Employee4 persistEmployee4(EntityManager em, int id) {
        Employee4 p = new Employee4();
        p.setAge(id);
        for (int i = 0; i < numDependentsPerEmployee; i++) {
            Dependent4 c = persistDependent4(em, dId4++, p);
            p.addChild(c);
        }
        em.persist(p);
        return p;
    }
    
    public Dependent4 persistDependent4(EntityManager em, int id, Employee4 p) {
        Dependent4 c = new Dependent4();
        c.setId(id);
        c.setParent(p);
        em.persist(c);
        return c;
    }
    
    public Employee4 mergeEmployee4(EntityManager em, int id) {
        Employee4 e = new Employee4();
        e.setAge(id);
        e = em.merge(e);
        for (int i = 0; i < numDependentsPerEmployee; i++) {
            Dependent4 d = new Dependent4();
            d.setId(dId4++);
            d.setParent(e);
            // do not need to merge d, as Employee is cascade.All
            d = em.merge(d);
            e.addChild(d);
        }
        return e;
    }
    
    public void testEnumInEmbeddedId() {
        EntityManager em = emf.createEntityManager();
        Beneficiary b = new Beneficiary();
        b.setId("b8");
        List<BeneContact> contacts = new ArrayList<BeneContact>();
        BeneContact c = new BeneContact();
        c.setEmail("email8");
        BeneContactId id = new BeneContactId();
        id.setContactType(BeneContactId.ContactType.HOME);
        c.setBeneficiary(b);
        
        c.setId(id);
        em.persist(c);
        contacts.add(c);
        b.setContacts(contacts);
        em.persist(b);
        em.getTransaction().begin();
        em.flush();
        em.getTransaction().commit();
        em.clear();
        BeneContactId id1 = c.getId();
        BeneContact c1 = em.find(BeneContact.class, id1);
        assertEquals("email8", c1.getEmail());
        em.close();
    }
}

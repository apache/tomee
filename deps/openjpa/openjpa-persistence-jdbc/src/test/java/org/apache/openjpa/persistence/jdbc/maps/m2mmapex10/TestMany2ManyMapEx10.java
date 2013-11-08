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
package org.apache.openjpa.persistence.jdbc.maps.m2mmapex10;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import junit.framework.Assert;

import org.apache.openjpa.kernel.QueryImpl;
import org.apache.openjpa.persistence.ArgumentException;
import org.apache.openjpa.persistence.test.AllowFailure;
import org.apache.openjpa.persistence.test.SQLListenerTestCase;

public class TestMany2ManyMapEx10 extends SQLListenerTestCase {

    public int numEmployees = 2;
    public int numPhoneNumbers = numEmployees + 1;
    public int numEmployeesPerPhoneNumber = 2;
    public int numPhoneNumbersPerEmployee = 2;
    public List<String> namedQueries = new ArrayList<String>();
    public List<EmployeePK> empPKs = new ArrayList<EmployeePK>();
    public List<PhonePK> phonePKs = new ArrayList<PhonePK>();

    public Map<String, Employee> empMap = 
        new HashMap<String, Employee>();
    public Map<PhonePK, PhoneNumber> phoneMap = 
        new HashMap<PhonePK, PhoneNumber>();

    public int empId = 1;
    public int phoneId = 1;
    public int divId = 1;
    public List rsAllPhones = null;
    public List rsAllEmps = null;

    public void setUp() {
        super.setUp(DROP_TABLES,EmployeePK.class, PhonePK.class,
            Employee.class, PhoneNumber.class);
        createObj();
        rsAllPhones = getAll(PhoneNumber.class);
        rsAllEmps = getAll(Employee.class);
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
        if (clz == PhoneNumber.class)
            qi.setCandidateCollection(rsAllPhones);
        else if (clz == Employee.class)
            qi.setCandidateCollection(rsAllEmps);
    }
    
    public void queryQualifiedId(boolean inMemory) throws Exception {
        EntityManager em = emf.createEntityManager();
        EmployeePK ekey = empPKs.get(0);
        String query = "select KEY(e) from PhoneNumber p, " +
            " in (p.emps) e where e.empPK.name = ?1 and e.empPK.bDay = ?2";
        Query q = em.createQuery(query).setParameter(1, ekey.getName()).
            setParameter(2, ekey.getBDay());
        if (inMemory) 
            setCandidate(q, PhoneNumber.class);
        List rs = q.getResultList();
        EmployeePK d = (EmployeePK) rs.get(0);

        query = "select KEY(e) from PhoneNumber p, " +
            " in (p.emps) e where e.empPK = ?1";
        try {
            rs = em.createQuery(query).setParameter(1, ekey).getResultList();
        } catch (Exception e) {
            // as excepted: conditional expressional expression over embeddable
            assertException(e, ArgumentException.class);
        }        
        
        query = "select KEY(p) from Employee e, " +
            " in (e.phones) p";
        q = em.createQuery(query);
        if (inMemory) 
            setCandidate(q, Employee.class);
        rs = q.getResultList();
        PhonePK k = (PhonePK) rs.get(0);

        em.clear();
        query = "select ENTRY(e) from PhoneNumber p, " +
            " in (p.emps) e  where e.empPK.name = ?1 and e.empPK.bDay = ?2";
        q = em.createQuery(query).setParameter(1, ekey.getName()).
            setParameter(2, ekey.getBDay());
        if (inMemory) 
            setCandidate(q, PhoneNumber.class);
        rs = q.getResultList();
        Map.Entry me = (Map.Entry) rs.get(0);

        assertTrue(d.equals(me.getKey()));

        // test navigation thru KEY
        em.clear();
        query = "select KEY(e), KEY(e).name from PhoneNumber p, " +
            " in (p.emps) e";
        q = em.createQuery(query);
        if (inMemory) 
            setCandidate(q, PhoneNumber.class);
        rs = q.getResultList();
        EmployeePK d0 = (EmployeePK) ((Object[]) rs.get(0))[0];
        String name = (String)((Object[]) rs.get(0))[1];
        assertEquals(d0.getName(), name);

        em.clear();
        query = "select KEY(p), KEY(p).phoneNum from Employee e, " +
            " in (e.phones) p";
        q = em.createQuery(query);
        if (inMemory) 
            setCandidate(q, Employee.class);
        rs = q.getResultList();
        k = (PhonePK) ((Object[]) rs.get(0))[0];
        String phoneNum = (String) ((Object[]) rs.get(0))[1];
        assertEquals(k.getPhoneNum(), phoneNum);

        // test ORDER BY qualified path
        query = "select KEY(p), KEY(p).phoneNum as pno from Employee e " +
            " left join e.phones p ORDER BY pno ";
        q = em.createQuery(query);
        if (inMemory) 
            setCandidate(q, Employee.class);
        rs = q.getResultList();

        // test GROUP BY qualified path
        sql.clear();
        query = "select COUNT(KEY(p).phoneNum) from Employee e " +
            " left join e.phones p GROUP BY KEY(p).phoneNum";
        q = em.createQuery(query);
        if (inMemory) 
            setCandidate(q, Employee.class);
        rs = q.getResultList();
        if (!inMemory)
        assertTrue(sql.get(0).toUpperCase().indexOf(" GROUP BY ") != -1);
        
        query = "select LENGTH(KEY(e).name) from PhoneNumber p, " +
            " in (p.emps) e where KEY(e).bDay = CURRENT_TIMESTAMP";
        q = em.createQuery(query);
        if (inMemory) 
            setCandidate(q, Employee.class);
        rs = q.getResultList();
        assertEquals(rs.size(), 0);

        em.close();
    }

    public void testQueryObject() throws Exception {
        queryObj();
        findObj();
    }

    public void createObj() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        for (int i = 0; i < numEmployees; i++) {
            Employee e = createEmployee(em, empId++);
            empMap.put(e.getEmpPK().getName(), e);
        }
        tran.begin();
        em.flush();
        tran.commit();
        em.close();
    }

    public Employee createEmployee(EntityManager em, int id) {
        Employee e = new Employee();
        Date bDay = new Date(System.currentTimeMillis() - 1000000);
        EmployeePK empPK = new EmployeePK("e" + id, bDay);
        empPKs.add(empPK);
        e.setEmpPK(empPK);
        e.setSalary(1000);
        for (int i = 0; i < numPhoneNumbersPerEmployee; i++) { 
            PhoneNumber phoneNumber = new PhoneNumber();
            PhonePK phonePK = new PhonePK("areaCode" + phoneId, "phoneNum" +
                    phoneId);
            phoneNumber.setRoom(phoneId);
            phoneId++;
            phonePKs.add(phonePK);
            phoneNumber.setPhonePK(phonePK);
            phoneNumber.addEmployees(empPK, e);
            e.addPhoneNumber(phonePK, phoneNumber);
            em.persist(phoneNumber);
            phoneMap.put(phoneNumber.getPhonePK(), phoneNumber);
        }
        em.persist(e);
        return e;
    }

    public void findObj() throws Exception {
        EntityManager em = emf.createEntityManager();
        Employee e = em.find(Employee.class, empPKs.get(1));
        assertEmployee(e);

        PhoneNumber p = em.find(PhoneNumber.class, phonePKs.get(1));
        assertPhoneNumber(p);
        em.close();
    }

    public void queryObj() throws Exception {
        queryEmployee();
        queryPhoneNumber();
    }

    public void queryPhoneNumber() throws Exception {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        tran.begin();
        Query q = em.createQuery("select p from PhoneNumber p");
        List<PhoneNumber> ps = q.getResultList();
        for (PhoneNumber p : ps) {
            assertPhoneNumber(p);
        }
        tran.commit();
        em.close();
    }

    public void queryEmployee() throws Exception {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        tran.begin();
        Query q = em.createQuery("select e from Employee e");
        List<Employee> es = q.getResultList();
        for (Employee e : es) {
            assertEmployee(e);
        }
        tran.commit();
        em.close();
    }

    public void assertEmployee(Employee e) throws Exception {
        EmployeePK empPK = e.getEmpPK();
        Employee e0 = empMap.get(empPK.getName());
        Map<PhonePK, PhoneNumber> phones = e.getPhoneNumbers();
        Map<PhonePK, PhoneNumber> phones0 = e0.getPhoneNumbers();
        Assert.assertEquals(phones0.size(), phones.size());
        checkPhoneMap(phones0, phones);
    }

    public void assertPhoneNumber(PhoneNumber p) throws Exception {
        PhonePK phonePK = p.getPhonePK();
        PhoneNumber p0 = phoneMap.get(phonePK);

        Map<EmployeePK, Employee> es = p.getEmployees();
        Map<EmployeePK, Employee> es0 = p0.getEmployees();
        Assert.assertEquals(es0.size(), es.size());
        checkEmpMap(es0, es);
    }

    public void checkPhoneMap(Map<PhonePK, PhoneNumber> es0, 
        Map<PhonePK, PhoneNumber> es) throws Exception {
        Collection<Map.Entry<PhonePK, PhoneNumber>> entrySets0 = es0.entrySet();
        for (Map.Entry<PhonePK, PhoneNumber> entry0 : entrySets0) {
            PhonePK d0 = entry0.getKey();
            PhoneNumber p0 = entry0.getValue();
            PhoneNumber p = es.get(d0);
            if (!p0.equals(p))
                throw new Exception("Assertion failure");
        }
    }

    public void checkEmpMap(Map<EmployeePK, Employee> es0,
        Map<EmployeePK, Employee> es)
        throws Exception {
        Collection<Map.Entry<EmployeePK, Employee>> entrySets0 = es0.entrySet();
        for (Map.Entry<EmployeePK, Employee> entry0 : entrySets0) {
            EmployeePK key0 = entry0.getKey();
            Employee e0 = entry0.getValue();
            //Employee e = es.get(key0);
            Employee e = Employee.findEmpl(es, key0);
            if (!e0.equals(e))
                throw new Exception("Assertion failure");
        }
    } 
}

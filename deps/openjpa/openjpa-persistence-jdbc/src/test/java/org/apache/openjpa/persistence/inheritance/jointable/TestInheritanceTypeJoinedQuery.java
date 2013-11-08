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
package org.apache.openjpa.persistence.inheritance.jointable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.openjpa.persistence.ArgumentException;
import org.apache.openjpa.persistence.test.SQLListenerTestCase;


public class TestInheritanceTypeJoinedQuery  extends SQLListenerTestCase {
    private int numPTEmployees = 1;
    private int numFTEmployees = 2;
    private int numContractors = 3;

    public void setUp() {
        setUp(Contractor.class, Employee.class, Department.class, Person.class,
            FulltimeEmployee.class, ParttimeEmployee.class,     
            CLEAR_TABLES);
        populate();
    }

    public void populate() {
        EntityManager em = emf.createEntityManager();
        
        Department d = new Department("IT");
        for (int i = 0; i < numContractors; i++) {
            Contractor c = new Contractor("ctr" + i);
            c.setDept(d);
            em.persist(c);
        }
        em.persist(d);

        for (int i = 0; i < numFTEmployees; i++) {
            FulltimeEmployee f = new FulltimeEmployee("ftemp" + i);
            f.setDept(d);
            em.persist(f);
        }

        for (int i = 0; i < numPTEmployees; i++) {
            ParttimeEmployee p = new ParttimeEmployee("ptemp" + i);
            p.setDept(d);
            em.persist(p);
        }
        
        em.getTransaction().begin();
        em.getTransaction().commit();
        em.close();
    }

    public void testInheritanceJoinedTypeOperator() {
        EntityManager em = emf.createEntityManager();
        Query q = null;
        String qS = null;
        List rs = null;
        
        qS = "SELECT p FROM Person p where TYPE(p) <> Contractor";
        q = em.createQuery(qS);
        rs = q.getResultList();
        assertEquals(numPTEmployees + numFTEmployees, rs.size());
        for (int i = 0; i < rs.size(); i++){
            Object obj = rs.get(i);
            assertTrue((obj instanceof ParttimeEmployee) || (obj instanceof FulltimeEmployee));
        }
        
        qS = "SELECT p FROM Person p where TYPE(p) = Contractor";
        q = em.createQuery(qS); 
        rs = q.getResultList();
        assertEquals(numContractors, rs.size());
        for (int i = 0; i < rs.size(); i++)
            assertTrue(rs.get(i) instanceof Contractor);
        
        qS = "select p from Person p where TYPE(p) in (?1) order by p.name";
        q = em.createQuery(qS).setParameter(1, Contractor.class);
        rs = q.getResultList();
        assertEquals(numContractors, rs.size());
        for (int i = 0; i < rs.size(); i++)
            assertTrue(rs.get(i) instanceof Contractor);

        qS = "select p from Person p where TYPE(p) in ?1 order by p.name";
        Collection<Class<?>> params = new ArrayList<Class<?>>(2);
        params.add(Contractor.class);
        params.add(Employee.class);
        try {
            q = em.createQuery(qS).setParameter(1, params);
            rs = q.getResultList();
        } catch (ArgumentException e) {
            // as expected
            //System.out.println(e.getMessage());
        }
        
        qS = "SELECT p FROM Person p where TYPE(p) = Contractor AND p.name = 'Name ctr0'";
        q = em.createQuery(qS);
        rs = q.getResultList();
        assertEquals(1, rs.size());
        for (int i = 0; i < rs.size(); i++)
            assertTrue(rs.get(i) instanceof Contractor);

        qS = "select p from Person p where TYPE(p) in (?1, ?2) and p.name = ?3 order by p.name";
        q = em.createQuery(qS);
        q.setParameter(1, Contractor.class);
        q.setParameter(2, FulltimeEmployee.class);
        q.setParameter(3, "Name ctr0");
       
        rs = q.getResultList();
        assertEquals(1, rs.size());
        for (int i = 0; i < rs.size(); i++) {
            Object obj = rs.get(i);
            assertTrue(obj instanceof Contractor || obj instanceof FulltimeEmployee);
        }

        qS = "select p from Person p where TYPE(p) in (?1, ?2) order by p.name";
        q = em.createQuery(qS);
        q.setParameter(1, Contractor.class);
        q.setParameter(2, FulltimeEmployee.class);
        
        rs = q.getResultList();
        assertEquals(numContractors + numFTEmployees, rs.size());
        for (int i = 0; i < rs.size(); i++) {
            Object obj = rs.get(i);
            assertTrue(obj instanceof Contractor || obj instanceof FulltimeEmployee);
        }
        
        qS = "select p from Person p where TYPE(p) not in (Contractor) order by p.name";
        q = em.createQuery(qS);
        rs = q.getResultList();
        assertEquals(numPTEmployees + numFTEmployees, rs.size());
        for (int i = 0; i < rs.size(); i++){
            Object obj = rs.get(i);
            assertTrue((obj instanceof ParttimeEmployee) || (obj instanceof FulltimeEmployee));
        }
        
        qS = "select p from Person p where TYPE(p) not in (?1) order by p.name";
        q = em.createQuery(qS);
        q.setParameter(1, Contractor.class);
        rs = q.getResultList();
        assertEquals(numPTEmployees + numFTEmployees, rs.size());
        for (int i = 0; i < rs.size(); i++){
            Object obj = rs.get(i);
            assertTrue((obj instanceof ParttimeEmployee) || (obj instanceof FulltimeEmployee));
        }

        qS = "select p from Person p where TYPE(p) not in (?1, ?2) order by p.name";
        q = em.createQuery(qS);
        q.setParameter(1, Contractor.class);
        q.setParameter(2, FulltimeEmployee.class);
        rs = q.getResultList();
        assertEquals(numPTEmployees, rs.size());
        for (int i = 0; i < rs.size(); i++){
            Object obj = rs.get(i);
            assertTrue((obj instanceof ParttimeEmployee) || (obj instanceof FulltimeEmployee));
        }
        em.close();
    }

    public void testInheritanceTypeJoinedQuery() {
        EntityManager em = emf.createEntityManager();
        Query q = null;
        String qS = null;
        Department dept = null;
        
        qS = "SELECT c.OID, c.dept FROM Department d, Contractor c where d.OID = c.dept.OID and d.description = 'IT'";
        q = em.createQuery(qS); 
        List<Object[]> lResult = q.getResultList();
        for (Object[] resultElement : lResult) {
            Long oid = (Long)resultElement[0];
            dept = (Department)resultElement[1];
        }
        
        qS = "SELECT c.OID FROM Department d, Contractor c where d.OID = c.dept.OID and d.description = 'IT'";
        q = em.createQuery(qS); 
        for (Object resultElement : q.getResultList()) {
            Long oid = (Long)resultElement;
        }
       
        qS = "SELECT d FROM Department d, Contractor c where d.OID = c.dept.OID and d.description = 'IT'";
        q = em.createQuery(qS); 
        for (Department aResult: (List <Department>) q.getResultList()) {
            assertEquals(dept.getOID(), aResult.getOID());
        }

        qS = "SELECT c FROM Department d, Contractor c  where d.OID = c.dept.OID and d.description = 'IT'";
        q = em.createQuery(qS);             
        for (Contractor aResult: (List <Contractor>) q.getResultList()) {
            //System.out.println(aResult.getDescription() + ", " + aResult.getOID());
            assertEquals(dept.getOID(), aResult.getDept().getOID());
        }
        qS = "SELECT c FROM Contractor c, Department d  where d.OID = c.dept.OID and d.description = 'IT'";
        q = em.createQuery(qS);             
        for (Contractor aResult: (List <Contractor>) q.getResultList()) {
            assertEquals(dept.getOID(), aResult.getDept().getOID());
        }
        
        qS = "SELECT c, c.OID FROM Department d, Contractor c where d.OID = c.dept.OID and d.description = 'IT'";
        q = em.createQuery(qS);             
        List<Object[]> cResult = q.getResultList();
        Contractor contractor = null;
        for (Object[] resultElement : cResult) {
            contractor = (Contractor)resultElement[0];
            Long oid = (Long)resultElement[1];
            assertTrue(contractor.getOID() == oid);
            assertEquals(dept.getOID(), contractor.getDept().getOID());
        }
        
        qS = "SELECT c.OID, c FROM Contractor c, Department d where d.OID = c.dept.OID and d.description = 'IT'";
        q = em.createQuery(qS);             
        List<Object[]> dResult = q.getResultList();
        for (Object[] resultElement : dResult) {
            Long oid = (Long)resultElement[0];
            contractor = (Contractor)resultElement[1];
            assertTrue(contractor.getOID() == oid);
            assertEquals(dept.getOID(), contractor.getDept().getOID());
        }
        
        qS = "SELECT c, c.OID FROM Department d, Contractor c where d.OID = c.dept.OID and d.description = 'IT'";
        q = em.createQuery(qS);             
        List<Object[]> eResult = q.getResultList();
        for (Object[] resultElement : eResult) {
            Long oid = (Long)resultElement[1];
            contractor = (Contractor)resultElement[0];
            assertTrue(contractor.getOID() == oid);
            assertEquals(dept.getOID(), contractor.getDept().getOID());
        }

        qS = "SELECT c.OID, c FROM Department d, Contractor c where d.OID = c.dept.OID and d.description = 'IT'";
        q = em.createQuery(qS);             
        List<Object[]> fResult = q.getResultList();
        for (Object[] resultElement : fResult) {
            Long oid = (Long)resultElement[0];
            Contractor c = (Contractor)resultElement[1];
            assertTrue(oid.longValue() == c.getOID());
            assertEquals(dept.getOID(), c.getDept().getOID());
        }
        
        qS = "SELECT d,c FROM Department d, Contractor c where d.OID = c.dept.OID and d.description = 'IT' " +
                " and c = ?1";
        q = em.createQuery(qS);
        q.setParameter(1, contractor);
        for (Object[] aResult: (List <Object[]>) q.getResultList()) {
            System.out.println(((Department)aResult[0]).getOID() + ", " + ((Contractor)aResult[1]).getOID());
            assertTrue(contractor.equals(aResult[1]));
        }
                
        qS = "SELECT c,d FROM Contractor c, Department d where d.OID = c.dept.OID and d.description = 'IT' " +
                " and c = ?1";
        q = em.createQuery(qS);
        q.setParameter(1, contractor);
        for (Object[] aResult: (List <Object[]>) q.getResultList()) {
            System.out.println(((Contractor)aResult[0]).getOID() + ", " + ((Department)aResult[1]).getOID());
            assertTrue(contractor.equals(aResult[0]));
        }

        qS = "SELECT p FROM Person p ";
        q = em.createQuery(qS);
        List rs = (List<Object>) q.getResultList();
        assertEquals(numPTEmployees + numFTEmployees + numContractors, rs.size());        
        for (Object aResult: rs) {
            assertTrue(aResult instanceof Person);
        }

        em.close();
    }
}


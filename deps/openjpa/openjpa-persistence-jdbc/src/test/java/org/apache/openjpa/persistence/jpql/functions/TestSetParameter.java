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
package org.apache.openjpa.persistence.jpql.functions;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.ArgumentException;
import org.apache.openjpa.persistence.common.apps.Address;
import org.apache.openjpa.persistence.common.apps.CompUser;
import org.apache.openjpa.persistence.common.apps.FemaleUser;
import org.apache.openjpa.persistence.common.apps.MaleUser;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Tests ordering of parameters as set by the parser and the ordering implicitly
 * assumed by the runtime are consistent.
 * 
 * The examples use JPQL queries such that the parser generates parameters  
 * in a different order from their order of appearance in the query string.
 *   
 * @author Pinaki Poddar
 *
 */
public class TestSetParameter extends SingleEMFTestCase {
    private int userid;
    private static String ORIGINAL_NAME = "Shannon";
    private static int ORIGINAL_AGE     = 62;
    private static String UPDATED_NAME  = "Obama";
    private static int UPDATED_AGE      = 29;
    
    public void setUp() {
        super.setUp(CLEAR_TABLES, CompUser.class, MaleUser.class, 
            FemaleUser.class, Address.class);
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        CompUser user = createUser(ORIGINAL_NAME, "PC", ORIGINAL_AGE, false);

        em.persist(user);

        em.getTransaction().commit();
        userid = user.getUserid();
        em.close();
    }

    public void testSetPositionalParameter() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        String query = "UPDATE CompUser e set e.name= ?1, e.age = ?2 "
            + "WHERE e.userid = ?3";
  
        int count = em.createQuery(query).
            setParameter(1, UPDATED_NAME).
            setParameter(2, UPDATED_AGE).
            setParameter(3, userid).
            executeUpdate();
        em.getTransaction().commit();
        assertEquals(1, count);

        em.close();
        verifyUpdate();
    }
    
    public void testSetPositionalParameterInNonIntutiveOrder() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        String query = "UPDATE CompUser e set e.name= ?2, e.age = ?1 "
            + "WHERE e.userid = ?3";
  
        int count = em.createQuery(query).
            setParameter(2, UPDATED_NAME).
            setParameter(1, UPDATED_AGE).
            setParameter(3, userid).
            executeUpdate();
        em.getTransaction().commit();
        assertEquals(1, count);

        em.close();
        verifyUpdate();
    }

    
    public void testSetNamedParameter() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        String query = "UPDATE CompUser e set e.name= :name, e.age = :age "
            + "WHERE e.userid = :id";
  
        int count = em.createQuery(query).
            setParameter("name", UPDATED_NAME).
            setParameter("age", UPDATED_AGE).
            setParameter("id", userid).
            executeUpdate();
        em.getTransaction().commit();
        assertEquals(1, count);

        em.close();
        
        verifyUpdate();
    }
    
    public void testNativeSQL() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        int count = em.createNativeQuery("INSERT INTO Address (id, city,"
          + " country, streetAd, zipcode) VALUES (?,?,?,?,?)")
          .setParameter(1, System.currentTimeMillis()%10000)
          .setParameter(2, "Some City")
          .setParameter(3, "Some Country")
          .setParameter(4, "Some Street")
          .setParameter(5, String.valueOf(System.currentTimeMillis()%10000))
          .executeUpdate();
        em.getTransaction().commit();
        assertEquals(1, count);
    }
    
    public void testMissingFirstPositionalParameter() {
        EntityManager em = emf.createEntityManager();
        String query = "UPDATE CompUser e set e.name= ?2, e.age = ?4 " + "WHERE e.userid = ?3";
        try {
            em.createQuery(query);
            fail("Did not get UserException with invalid JPQL query");
        } catch (ArgumentException ae) {
            // expected
        }
        em.close();
    }   
    
    public void testMixedParameterTypesParameter() {
        EntityManager em = emf.createEntityManager();
        String query = "UPDATE CompUser e set e.name= :name, e.age = ?1 " + "WHERE e.userid = ?3";
        try {
            em.createQuery(query);
            fail("Did not get UserException with invalid JPQL query");
        } catch (ArgumentException ae) {
            // expected
        }
        em.close();
    }
    
    public CompUser createUser(String name, String cName, int age,
        boolean isMale) {
        CompUser user = null;
        Address addr = new Address("43 Sansome", "SF", "United-Kingdom",
                "94104");
        if (isMale) {
            user = new MaleUser();
            user.setName(name);
            user.setComputerName(cName);
            user.setAge(age);
        } else {
            user = new FemaleUser();
            user.setName(name);
            user.setComputerName(cName);
            user.setAge(age);
        }
        user.setAddress(addr);
        return user;
    }
    
    void verifyUpdate() {
        EntityManager em = emf.createEntityManager();
        CompUser user = em.find(CompUser.class, userid);
        assertEquals(UPDATED_NAME, user.getName());
        assertEquals(UPDATED_AGE, user.getAge());
    }
}

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
package org.apache.openjpa.persistence.jpql.expressions;

import java.util.List;
import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.common.apps.*;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;

public class TestEJBQLCondExpression extends AbstractTestCase {

    private int userid1, userid2, userid3, userid4, userid5;

    public TestEJBQLCondExpression(String name) {
        super(name, "jpqlclausescactusapp");
    }

    public void setUp() {
        deleteAll(CompUser.class);
        EntityManager em = currentEntityManager();
        startTx(em);

        Address[] add =
            new Address[]{ new Address("43 Sansome", "SF", "USA", "94104"),
                new Address("24 Mink", "ANTIOCH", "USA", "94513"),
                new Address("23 Ogbete", "CoalCamp", "NIGERIA", "00000"),
                new Address("10 Wilshire", "Worcester", "CANADA", "80080"),
                new Address("23 Bellflower", "Ogui", "NIGERIA", "02000"),
                new Address("24 Bellflower", "Ogui", "NIGERIA", "02000")};


        CompUser user1 = createUser("Seetha", "MAC", add[0], 40, true);
        CompUser user2 = createUser("Shannon", "PC", add[1], 36, false);
        CompUser user3 = createUser("Ugo", "PC", add[2], 19, true);
        CompUser user4 = createUser("Jacob", "LINUX", add[3], 10, true);
        CompUser user5 = createUser("Famzy", "UNIX", add[4], 29, false);
        CompUser user6 = createUser("tes\\ter", "Test", add[5], 10, true);

        em.persist(user1);
        userid1 = user1.getUserid();
        em.persist(user2);
        userid2 = user2.getUserid();
        em.persist(user3);
        userid3 = user3.getUserid();
        em.persist(user4);
        userid4 = user4.getUserid();
        em.persist(user5);
        userid5 = user5.getUserid();
        em.persist(user6);

        endTx(em);
        endEm(em);
    }

    public void testNothing() {
        EntityManager em = currentEntityManager();
        String query = "SELECT o FROM CompUser o";

        List result = em.createQuery(query).getResultList();

        assertNotNull("the list is null", result);
        assertEquals("the size of the list is not 6", 6, result.size());

        endEm(em);
    }

    public void testBetweenExpr() {
        EntityManager em = currentEntityManager();
        String query = "SELECT o.name FROM CompUser o WHERE" +
            " o.age BETWEEN 19 AND 40 AND o.computerName = 'PC'";

        List result = em.createQuery(query).getResultList();

        assertNotNull("the list is null", result);
        assertEquals("they are not equal", 2, result.size());
        assertTrue("result dont contain shannon", result.contains("Shannon"));
        assertTrue("result dont contain ugo", result.contains("Ugo"));

        endEm(em);
    }

    public void testNotBetweenExpr() {
        EntityManager em = currentEntityManager();
        String query = "SELECT o.name FROM CompUser o " +
            "WHERE o.age NOT BETWEEN 19 AND 40 AND o.computerName= 'PC'";

        List result = em.createQuery(query).getResultList();

        assertNotNull("the list is null", result);
        assertEquals("they are not equal", 0, result.size());

        endEm(em);
    }

    public void testInExpr() {
        EntityManager em = currentEntityManager();
        String query =
            "SELECT o.name FROM CompUser o WHERE o.age IN (29, 40, 10)";

        List result = em.createQuery(query).getResultList();

        assertNotNull("the list is null", result);
        assertEquals(4, result.size());
        assertTrue("seetha is not in the list", result.contains("Seetha"));
        assertTrue("jacob is not in the list", result.contains("Jacob"));
        assertTrue("famzy is not in the list", result.contains("Famzy"));

        endEm(em);
    }

    public void testNotInExpr() {
        EntityManager em = currentEntityManager();
        String query =
            "SELECT o.name FROM CompUser o WHERE o.age NOT IN (29, 40, 10)";

        List result = em.createQuery(query).getResultList();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("Ugo"));
        assertTrue(result.contains("Shannon"));

        endEm(em);
    }

    public void testLikeExpr() {
        EntityManager em = currentEntityManager();

        String query = "SELECT o.computerName FROM CompUser o WHERE o.name " +
            "LIKE 'Sha%' AND o.computerName NOT IN ('PC')";

        List result = em.createQuery(query).getResultList();

        assertNotNull(result);
        assertEquals(0, result.size());

        query = "SELECT o.computerName FROM CompUser o WHERE o.name " +
            "LIKE 'Sha%o_' AND o.computerName NOT IN ('UNIX')";

        result = em.createQuery(query).getResultList();

        assertNotNull(result);
        assertEquals(1, result.size());
        
        query = "SELECT o.computerName FROM CompUser o WHERE o.name " +
                "LIKE 'tes\\%'";

        result = em.createQuery(query).getResultList();

        assertNotNull(result);
        assertEquals(1, result.size());
        
        query = "SELECT o.name FROM CompUser o WHERE o.name LIKE '_J%'";

        result = em.createQuery(query).getResultList();

        assertNotNull(result);
        assertEquals(0, result.size());

        query = "SELECT o.name FROM CompUser o WHERE o.name LIKE ?1 ESCAPE '|'";

        result = em.createQuery(query).setParameter(1, "%|_%").getResultList();

        assertNotNull(result);
        assertEquals(0, result.size());

        endEm(em);
    }

    public void testNullExpr() {
        EntityManager em = currentEntityManager();

        String query = "SELECT o.name FROM CompUser o WHERE o.age IS NOT NULL" +
            " AND o.computerName = 'PC' ";

        List result = em.createQuery(query).getResultList();

        assertNotNull("the list is null", result);
        assertEquals("the list size is not 2", 2, result.size());
        assertTrue("the result doesnt contain ugo", result.contains("Ugo"));
        assertTrue("the result doesnt contain shannon",
            result.contains("Shannon"));

        endEm(em);
    }

    public void testNullExpr2() {
        EntityManager em = currentEntityManager();

        String query =
            "SELECT o.name FROM CompUser o WHERE o.address.country IS NULL";

        List result = em.createQuery(query).getResultList();

        assertNotNull("the list is null", result);
        assertEquals("they are not equal", 0, result.size());

        endEm(em);
    }

    public void testIsEmptyExpr() {
        EntityManager em = currentEntityManager();

        String query =
            "SELECT o.name FROM CompUser o WHERE o.nicknames IS NOT EMPTY";

        List result = em.createQuery(query).getResultList();

        assertNotNull("the list is null", result);
        assertEquals("they are not equal", 0, result.size());

        endEm(em);
    }

    public void testIsEmptyExpr2() {
        EntityManager em = currentEntityManager();

        String query =
            "SELECT o.name FROM CompUser o WHERE o.nicknames IS EMPTY";

        List result = em.createQuery(query).getResultList();

        assertNotNull("the list is null", result);
        assertEquals("they are not equal", 6, result.size());

        endEm(em);
    }

    /**
     * TO BE TESTED LATER WITH A DIFF DATABASE
     * public void testMemberOfExpr(){}
     */

    public void testExistExpr() {
        EntityManager em = currentEntityManager();

        String query = "SELECT DISTINCT o.name FROM CompUser o WHERE EXISTS" +
            " (SELECT c FROM Address c WHERE c = o.address )";

        List result = em.createQuery(query).getResultList();

        assertNotNull("the list is null", result);
        assertEquals("they are not equal", 6, result.size());
        assertTrue("Seetha is not list", result.contains("Seetha"));
        assertTrue("Shannon is not list", result.contains("Shannon"));
        assertTrue("jacob is not list", result.contains("Jacob"));
        assertTrue("ugo is not list", result.contains("Ugo"));

        endEm(em);
    }

    public void testNotExistExpr() {
        EntityManager em = currentEntityManager();

        String query =
            "SELECT DISTINCT o.name FROM CompUser o WHERE NOT EXISTS" +
                " (SELECT s FROM CompUser s " +
                "WHERE s.address.country = o.address.country)";

        List result = em.createQuery(query).getResultList();

        assertNotNull("list is null", result);
        assertEquals("they are not equal", 0, result.size());

        endEm(em);
    }

    public void testAnyExpr() {
        EntityManager em = currentEntityManager();

        String query =
            "SELECT o.name FROM CompUser o WHERE o.address.zipcode = ANY (" +
                " SELECT s.computerName FROM CompUser s " +
                "WHERE s.address.country IS NOT NULL )";

        List result = em.createQuery(query).getResultList();

        assertNotNull("list is null", result);
        assertEquals("they are not equal", 0, result.size());

        endEm(em);
    }

    public void testConstructorExpr() {
        EntityManager em = currentEntityManager();

        String query = "SELECT NEW org.apache.openjpa.persistence.common.apps" +
               ".MaleUser(c.name, c.computerName, c.address, c.age, c.userid)" +
               " FROM CompUser c WHERE c.name = 'Seetha'";

        MaleUser male = (MaleUser) em.createQuery(query).getSingleResult();

        assertNotNull("the list is null", male);
        assertEquals("the names dont match", "Seetha", male.getName());
        assertEquals("computer names dont match", "MAC",
            male.getComputerName());
        assertEquals("the ages dont match", 40, male.getAge());

        endEm(em);
    }

    public CompUser createUser(String name, String cName, Address add, int age,
        boolean isMale) {
        CompUser user = null;
        if (isMale) {
            user = new MaleUser();
            user.setName(name);
            user.setComputerName(cName);
            user.setAddress(add);
            user.setAge(age);
            user.setNameAsLob(name);
        } else {
            user = new FemaleUser();
            user.setName(name);
            user.setComputerName(cName);
            user.setAddress(add);
            user.setAge(age);
            user.setNameAsLob(name);
        }
        return user;
	}
}

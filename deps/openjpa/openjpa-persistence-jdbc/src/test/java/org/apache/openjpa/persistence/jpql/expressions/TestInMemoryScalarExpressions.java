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

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.jdbc.sql.SybaseDictionary;
import org.apache.openjpa.kernel.Query;
import org.apache.openjpa.kernel.QueryImpl;
import org.apache.openjpa.persistence.OpenJPAEntityManagerSPI;
import org.apache.openjpa.persistence.common.apps.*;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;

public class TestInMemoryScalarExpressions extends AbstractTestCase {

    private int userid1, userid2, userid3, userid4, userid5, userid6;

    /**
     * Some databases trim the whitespace from a string upon insert. Store Shannon's name for 
     * asserts later in the testcase.
     */
    private String expectedShannonName = "Shannon ";
    
    public TestInMemoryScalarExpressions(String name) {
        super(name, "jpqlclausescactusapp");
    }

    public void setUp() {
        deleteAll(CompUser.class);
        OpenJPAEntityManagerSPI em = (OpenJPAEntityManagerSPI) currentEntityManager();
        startTx(em);

        Address[] add = new Address[]{
            new Address("43 Sansome", "SF", "United-Kingdom", "94104"),
            new Address("24 Mink", "ANTIOCH", "USA", "94513"),
            new Address("23 Ogbete", "CoalCamp", "NIGERIA", "00000"),
            new Address("10 Wilshire", "Worcester", "CANADA", "80080"),
            new Address("23 Bellflower", "Ogui", null, "02000"),
            new Address("22 Montgomery", "SF", null, "50054") };

        CompUser user1 = createUser("Seetha", "MAC", add[0], 36, true);
        CompUser user2 = createUser("Shannon ", "PC", add[1], 36, false);
        CompUser user3 = createUser("Ugo", "PC", add[2], 19, true);
        CompUser user4 = createUser("_Jacob", "LINUX", add[3], 10, true);
        CompUser user5 = createUser("Famzy", "UNIX", add[4], 29, false);
        CompUser user6 = createUser("Shade", "UNIX", add[5], 23, false);

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
        userid6 = user6.getUserid();

        DBDictionary dict = ((JDBCConfiguration) em.getConfiguration()).getDBDictionaryInstance();
        if(dict instanceof SybaseDictionary) { 
            expectedShannonName="Shannon";
        }
        
        endTx(em);
        endEm(em);
    }

    public void testCoalesceExpressions() {
        EntityManager em = currentEntityManager();
        List rsall = em.createQuery("SELECT e from CompUser e")
            .getResultList();

        String query = "SELECT e.name as name, " +
                " COALESCE (e.address.country, 'Unknown')" +
                " FROM CompUser e ORDER BY name";
        org.apache.openjpa.persistence.QueryImpl q1 = 
            (org.apache.openjpa.persistence.QueryImpl) em.createQuery(query);
        Query q2 = q1.getDelegate();
        Query qi = (QueryImpl) q2;
        qi.setCandidateCollection(rsall);
        List rs = q1.getResultList();
        Object[] result = (Object[]) rs.get(0);
        assertEquals("the name is not famzy", "Famzy", result[0]);        
        assertEquals("Unknown", result[1]);

        endEm(em);
    }

    public void testNullIfExpressions() {
        EntityManager em = currentEntityManager();
        List rsall = em.createQuery("SELECT e from CompUser e")
            .getResultList();

        String query = "SELECT e.name,  " +
            " NULLIF (e.address.country, 'USA'), " +
            " e.address.country as res " +
            " FROM CompUser e ORDER BY res";

        org.apache.openjpa.persistence.QueryImpl q1 = 
            (org.apache.openjpa.persistence.QueryImpl) em.createQuery(query);
        Query q2 = q1.getDelegate();
        Query qi = (QueryImpl) q2;
        qi.setCandidateCollection(rsall);
        List rs = q1.getResultList();
        Object[] result = (Object[]) rs.get(2);
        assertEquals("the name is not shannon ", expectedShannonName, result[0]);        
        assertNull("is not null", result[1]);
        
        endEm(em);
    }

    public void testSimpleCaseExpression() {
        EntityManager em = currentEntityManager();
        List rsall = em.createQuery("SELECT e from CompUser e")
            .getResultList();

        String query = "SELECT e.name, " +
            " CASE e.address.country WHEN 'USA' " +
            " THEN 'us' " +
            " ELSE 'non-us'  END as d2, " +
            " e.address.country " +
            " FROM CompUser e order by d2";

        org.apache.openjpa.persistence.QueryImpl q1 = 
            (org.apache.openjpa.persistence.QueryImpl) em.createQuery(query);
        Query q2 = q1.getDelegate();
        Query qi = (QueryImpl) q2;
        qi.setCandidateCollection(rsall);
        List rs = q1.getResultList();
        Object[] result = (Object[]) rs.get(5);
        assertEquals("the name is not shannon ", expectedShannonName, result[0]);        
        assertEquals("is not 'us'", "us", result[1]);
        
        endEm(em);
    }

    public void testGeneralCaseExpression() {
        EntityManager em = currentEntityManager();
        List rsall = em.createQuery("SELECT e from CompUser e")
            .getResultList();

        String query = "SELECT e.name as name, " +
            " CASE WHEN e.age > 30 THEN  30 " +
            " WHEN e.age < 15 THEN  15 " +
            " ELSE 20 " +
            " END, " +
            " e.age " +
            " FROM CompUser e ORDER BY name";
        
        org.apache.openjpa.persistence.QueryImpl q1 = 
            (org.apache.openjpa.persistence.QueryImpl) em.createQuery(query);
        Query q2 = q1.getDelegate();
        Query qi = (QueryImpl) q2;
        qi.setCandidateCollection(rsall);
        List rs = q1.getResultList();
        Object[] result = (Object[]) rs.get(3);
        assertEquals("the name is not shannon ", expectedShannonName, result[0]);
        assertEquals("not 30", "30", result[1].toString());

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
        } else {
            user = new FemaleUser();
            user.setName(name);
            user.setComputerName(cName);
            user.setAddress(add);
            user.setAge(age);
        }
        return user;
    }
}

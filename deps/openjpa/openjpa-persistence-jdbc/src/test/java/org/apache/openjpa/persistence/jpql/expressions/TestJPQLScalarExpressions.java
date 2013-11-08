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

import javax.persistence.Query;
import javax.persistence.EntityManager;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.jdbc.sql.SybaseDictionary;
import org.apache.openjpa.persistence.OpenJPAEntityManagerSPI;
import org.apache.openjpa.persistence.common.apps.*;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;

public class TestJPQLScalarExpressions extends AbstractTestCase {

    private int userid1, userid2, userid3, userid4, userid5, userid6;
    
    /**
     * Some databases trim the whitespace from a string upon insert. Store Shannon's name for 
     * asserts later in the testcase.
     */
    private String expectedShannonName = "Shannon ";
    
    public TestJPQLScalarExpressions(String name) {
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
        CompUser user4 = createUser("Jacob", "LINUX", add[3], 10, true);
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

    @SuppressWarnings("unchecked")
    public void testAggregateResultVariable() {
        EntityManager em = currentEntityManager();
        String querys[] = {
            "SELECT c.name, AVG(c.age) as age, a.city FROM CompUser c left join c.address a " +
            " group by c.name, a.city order by age desc, c.name",
            "SELECT c.name as name, SUM(c.age) as sage FROM CompUser c group by c.name order by sage desc, name",
            "SELECT c.name, AVG(c.age) as age FROM CompUser c group by c.name order by age desc, c.name",
        };
        for (int i = 0; i < querys.length; i++) {
            Query query = em.createQuery(querys[i]);
            query.setFirstResult(1);
            query.setMaxResults(4);
            List<Object[]> rs = query.getResultList();
            Object val = ((Object[]) rs.get(0))[1];
            if (val instanceof Integer)
                assertTrue((Integer) val > 0);
            else if (val instanceof Long)
                assertTrue((Long) val > 0);
        }
    }

    @SuppressWarnings("unchecked")
    public void testMathAndAggregate() {
        EntityManager em = currentEntityManager();
        String query[] = {
            "SELECT SUM(c.age) as age FROM CompUser c",
            "SELECT SUM(c.age) + SUM(c.userid) FROM CompUser c",
            "SELECT SUM(c.age) * SUM(c.userid) FROM CompUser c",
            "SELECT SUM(c.age) - MIN(c.userid) + MAX(c.userid) FROM CompUser c",
        };
        for (int i = 0; i < query.length; i++) {
            List<Long> rs = em.createQuery(query[i]).getResultList();
            assertTrue(rs.get(0) > 0);
        }
        String query2[] = {
                "SELECT SUM(c.age) + SUM(c.userid), MIN(c.age) + MAX(c.age) FROM CompUser c",
                "SELECT SUM(c.age) * SUM(c.userid), AVG(c.age) FROM CompUser c",
                "SELECT SUM(c.age) - MIN(c.userid) + MAX(c.userid), AVG(c.age)/10 FROM CompUser c",
        };
        for (int i = 0; i < query2.length; i++) {
            List<Object[]> rs = (List<Object[]>)em.createQuery(query2[i]).getResultList();
            assertNotNull(rs.get(0)[1]);
        }
        endEm(em);
    }

    @SuppressWarnings("unchecked")
    public void testCoalesceExpressions() {
        EntityManager em = currentEntityManager();
        startTx(em);

        String query = "SELECT e.name, " +
            "COALESCE (e.address.country, 'Unknown')" +
            " FROM CompUser e ORDER BY e.name DESC";
        List rs = em.createQuery(query).getResultList();
        Object[] result = (Object[]) rs.get(rs.size()-1);
        assertEquals("the name is not famzy", "Famzy", result[0]);        
        assertEquals("Unknown", result[1]);

        endTx(em);
        endEm(em);
    }

    @SuppressWarnings("unchecked")
    public void testNullIfExpressions() {
        EntityManager em = currentEntityManager();
        startTx(em);

        String query = "SELECT e.name, " +
            "NULLIF (e.address.country, 'USA')" +
            " FROM CompUser e ORDER BY e.name DESC";

        List rs = em.createQuery(query).getResultList();
        Object[] result = (Object[]) rs.get(1);
        assertEquals("the name is not shannon ", expectedShannonName, result[0]);
        assertNull("is not null", result[1]);
        
        endTx(em);
        endEm(em);
    }

    @SuppressWarnings("unchecked")
    public void testSimpleCaseExpressions() {
        EntityManager em = currentEntityManager();

        CompUser user = em.find(CompUser.class, userid1);
        assertNotNull("user is null", user);
        assertEquals("the name is not seetha", "Seetha", user.getName());
        String query = "SELECT e.name, e.age+1 as cage, " +
            "CASE e.address.country WHEN 'USA'" +
            " THEN 'us' " +
            " ELSE 'non-us'  END as d2," +
            " e.address.country " +
            " FROM CompUser e ORDER BY cage, d2 DESC";
        List rs = em.createQuery(query).getResultList();
        Object[] result = (Object[]) rs.get(rs.size()-1);
        assertEquals("the name is not seetha", "Seetha", result[0]);

        String query2 = "SELECT e.name, e.age+1 as cage, " +
            "CASE e.address.country WHEN 'USA'" +
            " THEN 'United-States' " +
            " ELSE e.address.country  END as d2," +
            " e.address.country " +
            " FROM CompUser e ORDER BY cage, d2 DESC";
        List rs2 = em.createQuery(query2).getResultList();
        Object[] result2 = (Object[]) rs2.get(rs2.size()-1);
        assertEquals("the name is not seetha", "Seetha", result2[0]);

        String query3 = "SELECT e.name, " +
            " CASE TYPE(e) WHEN FemaleUser THEN 'Female' " +
            " ELSE 'Male' " +
            " END as result" +
            " FROM CompUser e WHERE e.name like 'S%' " +
            " ORDER BY e.name DESC";
        List rs3 = em.createQuery(query3).getResultList();
        Object[] result3 = (Object[]) rs3.get(0);
        assertEquals("the result is not female", "Female", result3[1]);
        assertEquals("the name is not shannon", expectedShannonName, result3[0]);
        result3 = (Object[]) rs3.get(2);
        assertEquals("the result is not male", "Male", result3[1]);
        assertEquals("the name is not seetha", "Seetha", result3[0]);

        // boolean literal in case expression
        query = "SELECT e.name, " +
            "CASE e.address.country WHEN 'USA'" +
            " THEN true " +
            " ELSE false  END as b," +
            " e.address.country " +
            " FROM CompUser e order by b";
        rs = em.createQuery(query).getResultList();

        result = (Object[]) rs.get(rs.size()-1);
        
        if (result[1] instanceof String)
            assertEquals(result[1], "true");
        else    
            assertEquals(result[1], 1);
        

        startTx(em);
        String update = "update CompUser c set c.creditRating = " +
            " CASE c.age WHEN 35 THEN " +
            "org.apache.openjpa.persistence.common.apps." +
            "CompUser$CreditRating.POOR" + 
            " WHEN 11 THEN " + 
            "org.apache.openjpa.persistence.common.apps." + 
            "CompUser$CreditRating.GOOD" +
            " ELSE " + 
            "org.apache.openjpa.persistence.common.apps." + 
            "CompUser$CreditRating.EXCELLENT" +
            " END ";
        int updateCount = em.createQuery(update).executeUpdate();
        assertEquals("the result is not 6", 6, updateCount);

        /*
        //Derby fails but DB2 works 
        String update2 = "update CompUser c set c.creditRating = " +
            " (select " +
            " CASE c1.age WHEN 10 THEN " + 
            "org.apache.openjpa.persistence.common.apps." + 
              CompUser$CreditRating.POOR" + 
            " WHEN 19 THEN " + 
            "org.apache.openjpa.persistence.common.apps." + 
            "CompUser$CreditRating.GOOD " +
            " ELSE " + 
            "org.apache.openjpa.persistence.common.apps." + 
            CompUser$CreditRating.EXCELLENT " +
            " END " +
            " from CompUser c1" +
            " where c.userid = c1.userid)";
        updateCount = em.createQuery(update2).executeUpdate();
        assertEquals("the result is not 6", 6, updateCount);
        */
        endTx(em);
        endEm(em);
    }

    @SuppressWarnings("unchecked")
    public void testGeneralCaseExpressions() {
        EntityManager em = currentEntityManager();
        startTx(em);

        CompUser user = em.find(CompUser.class, userid1);
        assertNotNull("user is null", user);
        assertEquals("the name is not seetha", "Seetha", user.getName());

        String query = "SELECT e.name, e.age, " +
            " CASE WHEN e.age > 30 THEN e.age - 1 " +
            " WHEN e.age < 15 THEN e.age + 1 " +
            " ELSE e.age + 0 " +
            " END AS cage " +
            " FROM CompUser e ORDER BY cage";
        List rs = em.createQuery(query).getResultList();

        String update = "UPDATE CompUser e SET e.age = " +
            "CASE WHEN e.age > 30 THEN e.age - 1 " +
            "WHEN e.age < 15 THEN e.age + 1 " +
            "ELSE e.age + 0 " +
            "END";

        int result = em.createQuery(update).executeUpdate();
        assertEquals("the result is not 6", 6, result);
        
        String query2 = "SELECT e.name, e.age+1 as cage, " +
            "CASE WHEN e.address.country = 'USA' " +
            " THEN 'United-States' " +
            " ELSE 'Non United-States'  END as d2," +
            " e.address.country " +
            " FROM CompUser e ORDER BY cage, d2 DESC";
        List rs2 = em.createQuery(query2).getResultList();
        Object[] result2 = (Object[]) rs2.get(rs2.size()-1);
        assertEquals("the name is not seetha", "Seetha", result2[0]);
        assertEquals("the country is not 'Non United-States'", 
            "Non United-States", result2[2]);
        
        String query3 = " select e.name, " +
            "CASE WHEN e.age = 11 THEN " +
            "org.apache.openjpa.persistence.common.apps." +
            "CompUser$CreditRating.POOR" + 
            " WHEN e.age = 35 THEN " + 
            "org.apache.openjpa.persistence.common.apps." +
            "CompUser$CreditRating.GOOD" +
            " ELSE " + 
            "org.apache.openjpa.persistence.common.apps." +
            "CompUser$CreditRating.EXCELLENT" +
            " END FROM CompUser e ORDER BY e.age";
        List rs3 = em.createQuery(query3).getResultList();
        Object[] result3 = (Object[]) rs3.get(0);
        assertEquals("the name is not Jacob", "Jacob", result3[0]);
        assertEquals("the credit rating is not 'POOR'", "POOR", result3[1]);

        String update2 = "update CompUser c set c.creditRating = " +
            " CASE WHEN c.name ='Jacob' THEN " +
            "org.apache.openjpa.persistence.common.apps.CompUser$CreditRating.POOR" + 
            " WHEN c.name = 'Ugo' THEN " + 
            "org.apache.openjpa.persistence.common.apps.CompUser$CreditRating.GOOD " +
            " ELSE " + 
            "org.apache.openjpa.persistence.common.apps.CompUser$CreditRating.EXCELLENT " +
            " END ";
        int updateCount = em.createQuery(update2).executeUpdate();
        assertEquals("the result is not 6", 6, updateCount);
        
        
        String update3 = "update CompUser c set c.creditRating = " +
            " CASE WHEN c.age > 30 THEN " +
            "org.apache.openjpa.persistence.common.apps." +
            "CompUser$CreditRating.POOR" + 
            " WHEN c.age < 15 THEN " + 
            "org.apache.openjpa.persistence.common.apps." +
            "CompUser$CreditRating.GOOD " +
            " ELSE " + 
            "org.apache.openjpa.persistence.common.apps." +
            "CompUser$CreditRating.EXCELLENT " +
            " END "; 
        updateCount = em.createQuery(update3).executeUpdate();
        assertEquals("the result is not 6", 6, updateCount);
        
        String query4 = "select e.name, e.creditRating from CompUser e " + 
            "where e.creditRating = " +
            "(select " +
            "CASE WHEN e1.age = 11 THEN " + 
            "org.apache.openjpa.persistence.common.apps." +
            "CompUser$CreditRating.POOR" + 
            " WHEN e1.age = 35 THEN " + 
            "org.apache.openjpa.persistence.common.apps." +
            "CompUser$CreditRating.GOOD" +
            " ELSE " + 
            "org.apache.openjpa.persistence.common.apps." +
            "CompUser$CreditRating.EXCELLENT" +
            " END " +
            "from CompUser e1" +
            " where e.userid = e1.userid) ORDER BY e.age";
        List rs4 = em.createQuery(query4).getResultList();
        Object[] result4 = (Object[]) rs4.get(0);
        assertEquals("the name is not Ugo", "Ugo", result4[0]);
        assertEquals("the credit rating is not 'EXCELLENT'", "EXCELLENT",
            ((org.apache.openjpa.persistence.common.apps.CompUser.CreditRating)
            result4[1]).name());
        
        String update4 = "update CompUser c set c.creditRating = " +
            " CASE c.age WHEN 35 THEN " +
            "org.apache.openjpa.persistence.common.apps." +
            "CompUser$CreditRating.POOR" + 
            " WHEN 11 THEN " + 
            "org.apache.openjpa.persistence.common.apps." +
            "CompUser$CreditRating.GOOD " +
            " ELSE " + 
            "org.apache.openjpa.persistence.common.apps." +
            "CompUser$CreditRating.EXCELLENT " +
            " END "; 
        result = em.createQuery(update4).executeUpdate();
        assertEquals("the result is not 6", 6, result);

        // Derby fails but DB2 works 
        /*
        String update4 = "update CompUser c set c.creditRating = " +
            " (select " +
            " CASE c1.age WHEN 10 THEN " + 
            "org.apache.openjpa.persistence.common.apps.
            CompUser$CreditRating.POOR" + 
            " WHEN 19 THEN " + 
            "org.apache.openjpa.persistence.common.apps
            .CompUser$CreditRating.GOOD" +
            " ELSE " + 
            "org.apache.openjpa.persistence.common.apps.
            CompUser$CreditRating.EXCELLENT" +
            " END " +
            " from CompUser c1" +
            " where c.userid = c1.userid)";
        updateCount = em.createQuery(update4).executeUpdate();
        assertEquals("the result is not 6", 6, updateCount);
        */
        endTx(em);
        endEm(em);
    }

    @SuppressWarnings("unchecked")
    public void testMathFuncOrderByAlias() {
        EntityManager em = currentEntityManager();

        String query = "SELECT e.age * 2 as cAge FROM CompUser e ORDER BY cAge";

        List result = em.createQuery(query).getResultList();

        assertNotNull(result);
        assertEquals(6, result.size());

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

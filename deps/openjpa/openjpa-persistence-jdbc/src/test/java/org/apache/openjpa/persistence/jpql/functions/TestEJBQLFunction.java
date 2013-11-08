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

import java.util.List;
import javax.persistence.EntityManager;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.jdbc.sql.OracleDictionary;
import org.apache.openjpa.jdbc.sql.SybaseDictionary;
import org.apache.openjpa.persistence.OpenJPAEntityManagerSPI;
import org.apache.openjpa.persistence.common.apps.Address;
import org.apache.openjpa.persistence.common.apps.CompUser;
import org.apache.openjpa.persistence.common.apps.FemaleUser;
import org.apache.openjpa.persistence.common.apps.MaleUser;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;

public class TestEJBQLFunction extends AbstractTestCase {

    private int userid1, userid2, userid3, userid4, userid5, userid6;
    
    /**
     * Some databases trim the whitespace from a string upon insert. Store Shannon's name for 
     * asserts later in the testcase.
     */
    private String expectedShannonName = "Shannon ";
    
    public TestEJBQLFunction(String name) {
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

    public void testConcatSubStringFunc() {
        EntityManager em = currentEntityManager();
        startTx(em);

        CompUser user = em.find(CompUser.class, userid1);
        assertNotNull("user is null", user);
        assertEquals("the name is not seetha", "Seetha", user.getName());

        String query = "UPDATE CompUser e SET e.name = " +
            "CONCAT('Ablahum', SUBSTRING(e.name, LOCATE('e', e.name), 4)) " +
            "WHERE e.name='Seetha'";
        int result = em.createQuery(query).executeUpdate();

        assertEquals("the result is not 1", 1, result);

        user = em.find(CompUser.class, userid1);
        em.refresh(user);

        assertNotNull("the user is null", user);
        assertEquals("the users name is not Ablahumeeth", "Ablahumeeth",
            user.getName());

        query = "UPDATE CompUser e SET e.name = " +
            "CONCAT('XYZ', SUBSTRING(e.name, LOCATE('e', e.name))) " +
            "WHERE e.name='Ablahumeeth'";
        result = em.createQuery(query).executeUpdate();

        assertEquals("the result is not 1", 1, result);

        user = em.find(CompUser.class, userid1);
        em.refresh(user);

        assertNotNull("the user is null", user);
        assertEquals("the users name is not XYZeeth", "XYZeeth",
            user.getName());

        query = "UPDATE CompUser e SET e.name = " +
            "CONCAT('CAD', SUBSTRING(e.name, LOCATE('e', e.name, 5))) " +
            "WHERE e.name='XYZeeth'";
        result = em.createQuery(query).executeUpdate();
    
        assertEquals("the result is not 1", 1, result);

        user = em.find(CompUser.class, userid1);
        em.refresh(user);

        assertNotNull("the user is null", user);
        assertEquals("the users name is not CADeth", "CADeth",
            user.getName());

        endTx(em);
        endEm(em);
    }

    public void testConcatFunc2() {
        EntityManager em = currentEntityManager();
        startTx(em);

        CompUser user = em.find(CompUser.class, userid1);
        assertNotNull("the user is null", user);
        assertEquals("the users name is not seetha", user.getName(), "Seetha");

        String query = "UPDATE CompUser e " +
            "SET e.name = " +
            "CONCAT('', '') WHERE e.name='Seetha'";
        int result = em.createQuery(query).executeUpdate();

        assertEquals(1, result);

        user = em.find(CompUser.class, userid1);
        em.refresh(user);
        assertNotNull(user);
        // Empty strings are stored as null on Oracle so the assertion below
        // must be handled differently on that DB.  The docs indicate that
        // this may not be the case in future releases so either result is
        // allowed.
        // The note in this section of Oracle doc explains the behavior:
        // http://download.oracle.com/docs/cd/B14117_01/server.101/ +
        // b10759/sql_elements005.htm#sthref511
        DBDictionary dict = ((JDBCConfiguration) getEmf().getConfiguration())
            .getDBDictionaryInstance();
        if (dict instanceof OracleDictionary) {
            assertTrue(user.getName() == null ||
                "".equals(user.getName()));
        } else if (dict instanceof SybaseDictionary) {
            assertEquals(" ", user.getName());
        } else {
            assertEquals("", user.getName());
        }

        endTx(em);
        endEm(em);
    }

    public void testTrimFunc3() {
        EntityManager em = currentEntityManager();
        startTx(em);

        CompUser user = em.find(CompUser.class, userid2);
        assertNotNull(user);
        assertEquals(expectedShannonName, user.getName());

        String query = "UPDATE CompUser e SET " +
            "e.name = Trim(e.name) WHERE " +
            "e.name='Shannon '";
        int result = em.createQuery(query).executeUpdate();

        user = em.find(CompUser.class, userid2);
        em.refresh(user);
        assertNotNull(user);
        assertEquals("Shannon", user.getName());

        endTx(em);
        endEm(em);
    }

    public void testLowerFunc() {
        EntityManager em = currentEntityManager();
        startTx(em);

        CompUser user = em.find(CompUser.class, userid3);
        assertNotNull(user);
        assertEquals("Ugo", user.getName());

        String query = "UPDATE CompUser e SET " +
            "e.name = LOWER(e.name) WHERE e.name='Ugo'";

        int result = em.createQuery(query).executeUpdate();

        user = em.find(CompUser.class, userid3);
        em.refresh(user);
        assertNotNull(user);
        assertEquals("ugo", user.getName());

        endTx(em);
        endEm(em);
    }

    public void testLowerClobFunc() {
        OpenJPAEntityManagerSPI em = (OpenJPAEntityManagerSPI) currentEntityManager();
        // some databases do not support case conversion on LOBs,
        // just skip this test case
        DBDictionary dict = ((JDBCConfiguration) em.getConfiguration()).getDBDictionaryInstance();
        if (!dict.supportsCaseConversionForLob) {
            return;
        }
        startTx(em);

        CompUser user = em.find(CompUser.class, userid5);
        assertNotNull(user);
        assertEquals("Famzy", user.getName());

        String query = "UPDATE CompUser e SET e.name = LOWER(e.name) WHERE LOWER(e.nameAsLob)='famzy'";

        int result = em.createQuery(query).executeUpdate();

        user = em.find(CompUser.class, userid5);
        em.refresh(user);
        assertNotNull(user);
        assertEquals("famzy", user.getName());

        endTx(em);
        endEm(em);
    }

    public void testUpperFunc() {
        EntityManager em = currentEntityManager();
        startTx(em);

        CompUser user = em.find(CompUser.class, userid3);
        assertNotNull(user);
        assertEquals("Ugo", user.getName());

        String query = "UPDATE CompUser e SET " +
            "e.name = UPPER(e.name) WHERE e.name='Ugo'";

        int result = em.createQuery(query).executeUpdate();

        user = em.find(CompUser.class, userid3);
        em.refresh(user);
        assertNotNull(user);
        assertEquals("UGO", user.getName());

        endTx(em);
        endEm(em);
    }

    public void testUpperClobFunc() {
        OpenJPAEntityManagerSPI em = (OpenJPAEntityManagerSPI) currentEntityManager();
        // some databases do not support case conversion on LOBs,
        // just skip this test case
        DBDictionary dict = ((JDBCConfiguration) em.getConfiguration()).getDBDictionaryInstance();
        if (!dict.supportsCaseConversionForLob) {
            return;
        }
        startTx(em);

        CompUser user = em.find(CompUser.class, userid5);
        assertNotNull(user);
        assertEquals("Famzy", user.getName());

        String query = "UPDATE CompUser e SET e.name = UPPER(e.name) WHERE UPPER(e.nameAsLob)='FAMZY'";

        int result = em.createQuery(query).executeUpdate();

        user = em.find(CompUser.class, userid5);
        em.refresh(user);
        assertNotNull(user);
        assertEquals("FAMZY", user.getName());

        endTx(em);
        endEm(em);
    }

    public void testLengthFunc() {
        EntityManager em = currentEntityManager();

        String query = "SELECT o.name " +
            "FROM CompUser o " +
            "WHERE LENGTH(o.address.country) = 3";

        List result = em.createQuery(query).getResultList();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(expectedShannonName));

        endEm(em);
    }

    public void testArithmFunc() {
        EntityManager em = currentEntityManager();
        startTx(em);

        CompUser user = em.find(CompUser.class, userid1);

        assertNotNull(user);
        assertEquals("Seetha", user.getName());
        assertEquals(36, user.getAge());

        String query =
            "UPDATE CompUser e SET e.age = ABS(e.age) WHERE e.name='Seetha'";
        int num = em.createQuery(query).executeUpdate();

        assertNotNull(num);
        assertEquals(1, num);

        user = em.find(CompUser.class, userid1);
        em.refresh(user);

        assertEquals(36, user.getAge());

        //----------------------ABS Tested

        query =
            "UPDATE CompUser e SET e.age = SQRT(e.age) WHERE e.name='Seetha'";
        num = em.createQuery(query).executeUpdate();

        assertNotNull(num);
        assertEquals(1, num);

        user = em.find(CompUser.class, userid1);
        em.refresh(user);

        assertEquals(6, user.getAge());

        //-------------------------SQRT Tested

        query =
            "UPDATE CompUser e SET e.age = MOD(e.age, 4) WHERE e.name='Seetha'";
        num = em.createQuery(query).executeUpdate();

        assertNotNull(num);
        assertEquals(1, num);

        user = em.find(CompUser.class, userid1);
        em.refresh(user);

        assertEquals(2, user.getAge());

        //-------------------------MOD Tested

        query = "SELECT e.name FROM CompUser e WHERE SIZE(e.nicknames) = 6";
        List result = em.createQuery(query).getResultList();

        assertNotNull(result);
        assertEquals(0, result.size());

        //------------------------SIZE Tested

        endTx(em);
        endEm(em);
    }

    public void testGroupByHavingClause() {
        EntityManager em = currentEntityManager();

        String query = "SELECT c.name FROM CompUser c GROUP BY c.name "
            + "HAVING c.name LIKE 'S%'";

        List result = em.createQuery(query).getResultList();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains(expectedShannonName));
        assertTrue(result.contains("Shade"));
        assertTrue(result.contains("Seetha"));

        endEm(em);
    }

    public void testOrderByClause() {
        EntityManager em = currentEntityManager();

        String query = "SELECT c.name FROM CompUser c WHERE c.name LIKE 'S%' "
            + "ORDER BY c.name";

        List result = em.createQuery(query).getResultList();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains(expectedShannonName));
        assertTrue(result.contains("Seetha"));
        assertTrue(result.contains("Shade"));

        endEm(em);
    }

    public void testAVGAggregFunc() {
        /**
         * To be Tested: AVG, COUNT, MAX, MIN, SUM
         */

        EntityManager em = currentEntityManager();

        String query = "SELECT AVG(e.age) FROM CompUser e";

        List result = em.createQuery(query).getResultList();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(25));

        endEm(em);
    }

    public void testCOUNTAggregFunc() {
        EntityManager em = currentEntityManager();

        String query = "SELECT COUNT(c.name) FROM CompUser c";

        List result = em.createQuery(query).getResultList();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(6l));

        endEm(em);
    }

    public void testMAXAggregFunc() {
        EntityManager em = currentEntityManager();

        String query = "SELECT DISTINCT MAX(c.age) FROM CompUser c";

        List result = em.createQuery(query).getResultList();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(36));

        endEm(em);
    }

    public void testMINAggregFunc() {
        EntityManager em = currentEntityManager();

        String query = "SELECT DISTINCT MIN(c.age) FROM CompUser c";

        List result = em.createQuery(query).getResultList();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(10));

        endEm(em);
    }

    public void testSUMAggregFunc() {
        EntityManager em = currentEntityManager();

        String query = "SELECT SUM(c.age) FROM CompUser c";

        List result = em.createQuery(query).getResultList();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(153l));

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

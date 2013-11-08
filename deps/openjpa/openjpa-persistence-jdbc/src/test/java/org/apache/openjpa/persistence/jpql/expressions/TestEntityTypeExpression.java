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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.common.apps.*;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;

public class TestEntityTypeExpression extends AbstractTestCase {

    private int userid1, userid2, userid3, userid4, userid5, userid6;

    public TestEntityTypeExpression(String name) {
        super(name, "jpqlclausescactusapp");
    }

    public void setUp() {
        deleteAll(CompUser.class);
        EntityManager em = currentEntityManager();
        startTx(em);

        Address[] add = new Address[]{
            new Address("43 Sansome", "SF", "United-Kingdom", "94104"),
            new Address("24 Mink", "ANTIOCH", "USA", "94513"),
            new Address("23 Ogbete", "CoalCamp", "NIGERIA", "00000"),
            new Address("10 Wilshire", "Worcester", "CANADA", "80080"),
            new Address("23 Bellflower", "Ogui", null, "02000"),
            new Address("22 Montgomery", "SF", null, "50054") };

        CompUser user1 = createUser("Seetha", "MAC", add[0], 36, true);
        CompUser user2 = createUser("Shannon", "PC", add[1], 36, false);
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

        endTx(em);
        endEm(em);
    }

    public void testTypeOnNonPolymorphicEntity() {
        EntityManager em = currentEntityManager();
        String query = "select a from Address a where type(a) = ?1";
        List rs = null;
        try {
            rs =  em.createQuery(query).setParameter(1, Address.class).getResultList();
            System.out.println("rs size="+rs.size());
        } catch(Exception e) {
            // as expected
            //System.out.println(e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public void testTypeExpression() {
        EntityManager em = currentEntityManager();

        String query = null;
        List<CompUser> rs = null;
        CompUser user = null;

        // test collection-valued input parameters in in-expressions
        Collection params = new ArrayList(2);
        params.add(FemaleUser.class);
        params.add(MaleUser.class);

        Collection params2 = new ArrayList(3);
        params2.add(36);
        params2.add(29);
        params2.add(19);
        String param3 = "PC";

        query = "SELECT e FROM CompUser e where TYPE(e) in ?1 and e.age in ?2" +
                " and e.computerName = ?3 ORDER By e.name";
        rs = em.createQuery(query).
            setParameter(1, params).
            setParameter(2, params2).
            setParameter(3, param3).getResultList();
        user = rs.get(0);
        assertEquals("Shannon", user.getName());

        query = "SELECT e FROM CompUser e where TYPE(e) in ?1 and e.age in ?2" +
                " ORDER By e.name";
        rs = em.createQuery(query).
            setParameter(1, params).
            setParameter(2, params2).getResultList();
        user = rs.get(0);
        assertEquals("Famzy", user.getName());

        query = "SELECT e FROM CompUser e where TYPE(e) in :value " +
                "ORDER BY e.age";
        rs = em.createQuery(query).
            setParameter("value", params).getResultList();
        user = rs.get(0);
        assertEquals("Jacob", user.getName());
        
        query = "SELECT TYPE(e) FROM MaleUser e where TYPE(e) = MaleUser";
        rs = em.createQuery(query).getResultList();
        Object type = rs.get(0);
        assertEquals(type, MaleUser.class);
        
        query = "SELECT TYPE(e) FROM CompUser e where TYPE(e) = ?1";
        rs = em.createQuery(query).
            setParameter(1, FemaleUser.class).getResultList();
        type = rs.get(0);
        assertEquals(type, FemaleUser.class);

        query = "SELECT TYPE(e) FROM MaleUser e where TYPE(e) = ?1";
        rs = em.createQuery(query).
            setParameter(1, MaleUser.class).getResultList();
        type = rs.get(0);
        assertEquals(type, MaleUser.class);

        query = "SELECT e, FemaleUser, a FROM Address a, FemaleUser e " +
            " where e.address IS NOT NULL";
        List<Object> rs2 = em.createQuery(query).getResultList();
        type = ((Object[]) rs2.get(0))[1];
        assertEquals(type, FemaleUser.class);
        
        query = "SELECT e FROM CompUser e where TYPE(e) = :type " +
            " ORDER BY e.name";
        rs =  em.createQuery(query).
            setParameter("type", FemaleUser.class).getResultList();
        assertTrue(rs.size()==3);
        user = rs.get(0);
        assertEquals("Famzy", user.getName());
        user = rs.get(1);
        assertEquals("Shade", user.getName());
        user = rs.get(2);
        assertEquals("Shannon", user.getName());

        query = "SELECT e FROM CompUser e where TYPE(e) = ?1 ORDER BY e.name";
        rs = em.createQuery(query).
            setParameter(1, FemaleUser.class).getResultList();
        user = rs.get(0);
        assertEquals("Famzy", user.getName());
        
        query = "SELECT e FROM CompUser e where TYPE(e) in (?1)" +
            " ORDER BY e.name DESC";
        rs = em.createQuery(query).
            setParameter(1, MaleUser.class).getResultList();
        user = rs.get(0);
        assertEquals("Ugo", user.getName());
        
        query = "SELECT e FROM CompUser e where TYPE(e) in (?1, ?2)" +
                " ORDER BY e.name DESC";
        rs = em.createQuery(query).
            setParameter(1, FemaleUser.class).setParameter(2, MaleUser.class).
            getResultList();
        user = rs.get(0);
        assertEquals("Ugo", user.getName());

        query = "select sum(e.age) FROM CompUser e GROUP BY e.age" +
            " HAVING ABS(e.age) = :param";
        Long sum = (Long) em.createQuery(query).
            setParameter("param",  new Double(36)).getSingleResult();
        assertEquals(sum.intValue(), 72);

        String[] queries = {
            "SELECT e FROM CompUser e where TYPE(e) = MaleUser",
            "SELECT e from CompUser e where TYPE(e) in (FemaleUser)",
            "SELECT e from CompUser e where TYPE(e) not in (FemaleUser)",
            "SELECT e from CompUser e where TYPE(e) in (MaleUser, FemaleUser)",
            "SELECT TYPE(e) FROM CompUser e where TYPE(e) = MaleUser",
            "SELECT TYPE(e) FROM CompUser e",
            "SELECT TYPE(a.user) FROM Address a",
            "SELECT MaleUser FROM CompUser e", 
            "SELECT MaleUser FROM Address a",
            "SELECT " +
                " CASE TYPE(e) WHEN FemaleUser THEN 'Female' " +
                " ELSE 'Male' " +
                " END " +
                " FROM CompUser e",
        };
        
        for (int i = 0; i < queries.length; i++) {
            query = queries[i];
            List<Object> rs1 = em.createQuery(query).getResultList();
            Object obj = rs1.get(0);
            obj.toString();
        }

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

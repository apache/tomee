/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
    * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.dynamic;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import jakarta.ejb.EJBException;
import jakarta.ejb.Stateless;
import jakarta.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DynamicUserDaoTest {

    private static UserDao dao;
    private static Util util;

    @BeforeClass
    public static void init() throws Exception {
        final Properties p = new Properties();
        p.put("jdbc/dynamicDB", "new://Resource?type=DataSource");
        p.put("jdbc/dynamicDB.JdbcDriver", "org.hsqldb.jdbcDriver");
        p.put("jdbc/dynamicDB.JdbcUrl", "jdbc:hsqldb:mem:moviedb");
        p.put("jdbc/dynamicDB.UserName", "sa");
        p.put("jdbc/dynamicDB.Password", "");

        final Context context = EJBContainer.createEJBContainer(p).getContext();
        dao = (UserDao) context.lookup("java:global/dynamic-dao-implementation/UserDao");
        util = (Util) context.lookup("java:global/dynamic-dao-implementation/Util");

        util.init(); // init database
    }

    @Test
    public void simple() {
        User user = dao.findById(1);
        assertNotNull(user);
        assertEquals(1, user.getId());
    }

    @Test
    public void findAll() {
        Collection<User> users = dao.findAll();
        assertEquals(10, users.size());
    }

    @Test
    public void pagination() {
        Collection<User> users = dao.findAll(0, 5);
        assertEquals(5, users.size());

        users = dao.findAll(6, 1);
        assertEquals(1, users.size());
        assertEquals(7, users.iterator().next().getId());
    }

    @Test
    public void persist() {
        User u = new User();
        dao.save(u);
        assertNotNull(u.getId());
        util.remove(u);
    }

    @Test
    public void remove() {
        User u = new User();
        dao.save(u);
        assertNotNull(u.getId());
        dao.delete(u);
        try {
            dao.findById(u.getId());
            Assert.fail();
        } catch (EJBException ee) {
            assertTrue(ee.getCause() instanceof NoResultException);
        }
    }

    @Test
    public void merge() {
        User u = new User();
        u.setAge(1);
        dao.save(u);
        assertEquals(1, u.getAge());
        assertNotNull(u.getId());

        u.setAge(2);
        dao.update(u);
        assertEquals(2, u.getAge());

        dao.delete(u);
    }

    @Test
    public void oneCriteria() {
        Collection<User> users = dao.findByName("foo");
        assertEquals(4, users.size());
        for (User user : users) {
            assertEquals("foo", user.getName());
        }
    }

    @Test
    public void twoCriteria() {
        Collection<User> users = dao.findByNameAndAge("bar-1", 1);
        assertEquals(1, users.size());

        User user = users.iterator().next();
        assertEquals("bar-1", user.getName());
        assertEquals(1, user.getAge());
    }

    @Test
    public void query() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", "foo");

        Collection<User> users = dao.namedQuery("dynamic-ejb-impl-test.query", params, 0, 100);
        assertEquals(4, users.size());

        users = dao.namedQuery("dynamic-ejb-impl-test.query", params);
        assertEquals(4, users.size());

        users = dao.namedQuery("dynamic-ejb-impl-test.query", params, 0, 2);
        assertEquals(2, users.size());

        users = dao.namedQuery("dynamic-ejb-impl-test.query", 0, 2, params);
        assertEquals(2, users.size());

        users = dao.namedQuery("dynamic-ejb-impl-test.all");
        assertEquals(10, users.size());

        params.remove("name");
        params.put("age", 1);
        users = dao.query("SELECT u FROM User AS u WHERE u.age = :age", params);
        assertEquals(3, users.size());
    }

    @Stateless
    public static class Util {

        @PersistenceContext
        private EntityManager em;

        public void remove(User o) {
            em.remove(em.find(User.class, o.getId()));
        }

        public void init() {
            for (int i = 0; i < 10; i++) {
                User u = new User();
                u.setAge(i % 4);
                if (i % 3 == 0) {
                    u.setName("foo");
                } else {
                    u.setName("bar-" + i);
                }
                em.persist(u);
            }
        }

    }
}

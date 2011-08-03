/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.util;

import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.junit.Configuration;
import org.apache.openejb.junit.Module;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Singleton;
import javax.ejb.Stateless;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * @author rmannibucau
 */
@RunWith(ApplicationComposer.class)
public class DynamicEJBImplTest {
    private static boolean initDone = false;

    @EJB private UtilBean util;
    @EJB private UserDAO dao;

    @Before public void initDatabaseIfNotDone() {
        if (!initDone) {
            util.init();
            initDone = true;
        }
    }

    @After public void checkAll() {
        Collection<User> users = dao.findAll();
        assertEquals(10, users.size());
    }

    @Test public void simple() {
        User user = dao.findById(1);
        assertNotNull(user);
        assertEquals(1, user.getId());
    }

    @Test public void findAll() {
        Collection<User> users = dao.findAll();
        assertEquals(10, users.size());
    }

    @Test public void pagination() {
        Collection<User> users = dao.findAll(0, 5);
        assertEquals(5, users.size());

        users = dao.findAll(6, 1);
        assertEquals(1, users.size());
        assertEquals(7, users.iterator().next().getId());
    }

    @Test public void persist() {
        User u = new User();
        dao.save(u);
        assertNotNull(u.getId());
        util.remove(u);
    }

    @Test public void remove() {
        User u = new User();
        dao.save(u);
        assertNotNull(u.getId());
        dao.delete(u);
        try {
            dao.findById(u.getId());
            fail();
        } catch (EJBException ee) {
            assertTrue(ee.getCause() instanceof NoResultException);
        }
    }

    @Test public void merge() {
        User u = new User();
        u.setInfo("one");
        dao.save(u);
        assertEquals("one", u.getInfo());
        assertNotNull(u.getId());

        u.setInfo("another one");
        dao.update(u);
        assertEquals("another one", u.getInfo());

        dao.delete(u);
    }

    @Test public void oneCriteria() {
        Collection<User> users = dao.findByName("foo");
        assertEquals(4, users.size());
        for (User user : users) {
            assertEquals("foo", user.getName());
        }
    }

    @Test public void twoCriteria() {
        Collection<User> users = dao.findByNameAndInfo("bar-1", "1");
        assertEquals(1, users.size());

        User user = users.iterator().next();
        assertEquals("bar-1", user.getName());
        assertEquals("1", user.getInfo());
    }

    @Test public void checkInjections() {
        UserDAO injection = util.getDao();
        assertNotNull(injection);
        assertEquals(10, injection.findAll().size());
    }

    @Test public void query() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("name", "foo");

        Collection<User> users = dao.namedQuery("dynamic-ejb-impl-test.query", params, 0, 100);
        assertEquals(4, users.size());

        users = dao.namedQuery("dynamic-ejb-impl-test.query", params);
        assertEquals(4, users.size());

        users = dao.namedQuery("dynamic-ejb-impl-test.query", params, 0, 2);
        assertEquals(2, users.size());

        users = dao.namedQuery("dynamic-ejb-impl-test.query", 0, 2, params);
        assertEquals(2, users.size());
    }

    @Stateless @PersistenceContext(name = "pu") public static interface UserDAO {
        User findById(long id);

        Collection<User> findByName(String name);

        Collection<User> findByNameAndInfo(String name, String info);

        Collection<User> findAll();
        Collection<User> findAll(int first, int max);

        Collection<User> namedQuery(String name, Map<String, ?> params, int first, int max);
        Collection<User> namedQuery(String name, int first, int max, Map<String, ?> params);
        Collection<User> namedQuery(String name, Map<String, ?> params);

        void save(User u);

        void delete(User u);

        User update(User u);
    }

    @NamedQuery(name = "dynamic-ejb-impl-test.query", query = "SELECT u FROM DynamicEJBImplTest$User AS u WHERE u.name LIKE :name")
    @Entity public static class User {
        @Id @GeneratedValue private long id;
        private String name;
        private String info;
        private int age;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public String getInfo() {
            return info;
        }

        public void setInfo(String info) {
            this.info = info;
        }

        @Override public String toString() {
            return "User{age=" + age + ", info='" + info + '\'' + ", name='" + name + '\'' + ", id=" + id + '}';
        }
    }

    @Singleton public static class UtilBean {
        @PersistenceContext private EntityManager em;
        @EJB private UserDAO dao;

        public void init() {
            for (int i = 0; i < 10; i++) {
                User u = new User();
                u.setAge(i * 8);
                if (i % 3 == 0) {
                    u.setName("foo");
                } else {
                    u.setName("bar-" + i);
                }
                u.setInfo(Integer.toString(i % 3));
                em.persist(u);
            }
        }

        public UserDAO getDao() {
            return dao;
        }

        public void remove(User u) {
            em.remove(em.find(u.getClass(), u.getId()));
        }
    }

    @Configuration public Properties config() {
        final Properties p = new Properties();
        p.put("bvalDatabase", "new://Resource?type=DataSource");
        p.put("bvalDatabase.JdbcDriver", "org.hsqldb.jdbcDriver");
        p.put("bvalDatabase.JdbcUrl", "jdbc:hsqldb:mem:bval");
        return p;
    }

    @Module public EjbJar app() throws Exception {
        EjbJar ejbJar = new EjbJar("dynamic");
        ejbJar.addEnterpriseBean(new SingletonBean(UtilBean.class));
        ejbJar.addEnterpriseBean(new StatelessBean(UserDAO.class));
        return ejbJar;
    }

    @Module public Persistence persistence() {
        PersistenceUnit unit = new PersistenceUnit("pu");
        unit.addClass(User.class);
        unit.setProperty("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
        unit.setExcludeUnlistedClasses(true);

        Persistence persistence = new Persistence(unit);
        persistence.setVersion("2.0");
        return persistence;
    }
}

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

import org.apache.openejb.api.Proxy;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.ejb.Singleton;
import jakarta.ejb.Stateless;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(ApplicationComposer.class)
public class DynamicEJBImplTest {

    private static volatile boolean initDone = false;

    @EJB
    private UserDAO dao;
    @EJB
    private UtilBean util;
    @EJB
    private UserDAOChild child;
    @EJB
    private DynamicCustomProxy custom;

    @Test
    public void custom() {
        assertEquals("method = foo", custom.foo());
    }

    @Before
    public void initDatabaseIfNotDone() {
        if (!initDone) {
            util.init();
            initDone = true;
        }
    }

    @After
    public void checkAll() {
        final Collection<User> users = dao.findAll();
        assertEquals(10, users.size());
    }

    @Test
    public void simple() {
        final User user = dao.findById(1);
        assertNotNull(user);
        assertEquals(1, user.getId());
    }

    @Test
    public void findAll() {
        final Collection<User> users = dao.findAll();
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
        final User u = new User();
        dao.save(u);
        assertNotNull(u.getId());
        util.remove(u);
    }

    @Test
    public void remove() {
        final User u = new User();
        dao.save(u);
        assertNotNull(u.getId());
        dao.delete(u);
        java.util.logging.Logger.getLogger(this.getClass().getName()).info("Expecting a SEVERE jakarta.persistence.NoResultException");
        try {
            dao.findById(u.getId());
            fail();
        } catch (final EJBException ee) {
            assertTrue(ee.getCause() instanceof NoResultException);
        }
    }

    @Test
    public void merge() {
        final User u = new User();
        u.setInfo("one");
        dao.save(u);
        assertEquals("one", u.getInfo());
        assertNotNull(u.getId());

        u.setInfo("another one");
        dao.update(u);
        assertEquals("another one", u.getInfo());

        dao.delete(u);
    }

    @Test
    public void oneCriteria() {
        final Collection<User> users = dao.findByName("foo");
        assertEquals(4, users.size());
        for (final User user : users) {
            assertEquals("foo", user.getName());
        }
    }

    @Test
    public void twoCriteria() {
        final Collection<User> users = dao.findByNameAndInfo("bar-1", "1");
        assertEquals(1, users.size());

        final User user = users.iterator().next();
        assertEquals("bar-1", user.getName());
        assertEquals("1", user.getInfo());
    }

    @Test
    public void checkInjections() {
        final UserDAO injection = util.getDao();
        assertNotNull(injection);
        assertEquals(10, injection.findAll().size());
    }

    @Test
    public void query() {
        final Map<String, String> params = new HashMap<>();
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
        params.put("info", "0");
        users = dao.query("SELECT u FROM DynamicEJBImplTest$User AS u WHERE u.info LIKE :info", params);
        assertEquals(4, users.size());
    }

    @Test
    public void inheritance() {
        final Map<String, String> params = new HashMap<>();
        params.put("name", "foo");

        Collection<User> users = child.namedQuery("dynamic-ejb-impl-test.query", params, 0, 100);
        assertEquals(4, users.size());

        users = child.namedQuery("dynamic-ejb-impl-test.query", params);
        assertEquals(4, users.size());

        users = child.namedQuery("dynamic-ejb-impl-test.query", params, 0, 2);
        assertEquals(2, users.size());

        users = child.namedQuery("dynamic-ejb-impl-test.query", 0, 2, params);
        assertEquals(2, users.size());

        users = child.namedQuery("dynamic-ejb-impl-test.all");
        assertEquals(10, users.size());

        params.remove("name");
        params.put("info", "0");
        users = child.query("SELECT u FROM DynamicEJBImplTest$User AS u WHERE u.info LIKE :info", params);
        assertEquals(4, users.size());
    }

    @Stateless
    @PersistenceContext(name = "pu")
    public static interface UserDAO {

        User findById(long id);

        Collection<User> findByName(String name);

        Collection<User> findByNameAndInfo(String name, String info);

        Collection<User> findAll();

        Collection<User> findAll(int first, int max);

        Collection<User> namedQuery(String name, Map<String, ?> params, int first, int max);

        Collection<User> namedQuery(String name, int first, int max, Map<String, ?> params);

        Collection<User> namedQuery(String name, Map<String, ?> params);

        Collection<User> namedQuery(String name);

        Collection<User> query(String value, Map<String, ?> params);

        void save(User u);

        void delete(User u);

        User update(User u);
    }

    @Stateless
    @PersistenceContext(name = "pu")
    public static interface UserDAOChild extends UserDAO {
        // just inherited methods
    }

    @NamedQueries({
        @NamedQuery(name = "dynamic-ejb-impl-test.query", query = "SELECT u FROM DynamicEJBImplTest$User AS u WHERE u.name LIKE :name"),
        @NamedQuery(name = "dynamic-ejb-impl-test.all", query = "SELECT u FROM DynamicEJBImplTest$User AS u")
    })
    @Entity
    public static class User {

        @Id
        @GeneratedValue
        private long id;
        private String name;
        private String info;

        public User() {
        }

        public long getId() {
            return id;
        }

        public void setId(final long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public String getInfo() {
            return info;
        }

        public void setInfo(final String info) {
            this.info = info;
        }

        @Override
        public String toString() {
            return "User{info='" + info + '\'' + ", name='" + name + '\'' + ", id=" + id + '}';
        }
    }

    @Singleton
    public static class UtilBean {

        public UtilBean() {
        }

        @PersistenceContext
        private EntityManager em;
        @EJB
        private UserDAO dao;

        public void init() {
            for (int i = 0; i < 10; i++) {
                final User u = new User();
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

        public void remove(final User u) {
            em.remove(em.find(u.getClass(), u.getId()));
        }
    }

    public static class Handler implements InvocationHandler {

        public Handler() {
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            return "method = " + method.getName();
        }
    }

    @Singleton
    @Proxy(Handler.class)
    public static interface DynamicCustomProxy {

        public String foo();
    }

    @Configuration
    public Properties config() {
        final Properties p = new Properties();
        p.put("dynamicEjbDatabase", "new://Resource?type=DataSource");
        p.put("dynamicEjbDatabase.JdbcDriver", "org.hsqldb.jdbcDriver");
        p.put("dynamicEjbDatabase.JdbcUrl", "jdbc:hsqldb:mem:bval");
        return p;
    }

    @Module
    public EjbJar app() throws Exception {
        final EjbJar ejbJar = new EjbJar("dynamic");
        ejbJar.addEnterpriseBean(new SingletonBean(UtilBean.class));
        ejbJar.addEnterpriseBean(new SingletonBean(DynamicCustomProxy.class));
        ejbJar.addEnterpriseBean(new StatelessBean(UserDAO.class));
        ejbJar.addEnterpriseBean(new StatelessBean(UserDAOChild.class));
        return ejbJar;
    }

    @Module
    public Persistence persistence() {
        final PersistenceUnit unit = new PersistenceUnit("pu");
        unit.addClass(User.class);
        unit.setProperty("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
        unit.setExcludeUnlistedClasses(true);

        final Persistence persistence = new Persistence(unit);
        persistence.setVersion("2.0");
        return persistence;
    }
}

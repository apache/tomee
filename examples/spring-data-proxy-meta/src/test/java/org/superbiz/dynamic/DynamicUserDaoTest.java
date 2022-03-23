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

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import jakarta.ejb.embeddable.EJBContainer;
import jakarta.inject.Inject;
import javax.naming.NamingException;
import java.util.Collection;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DynamicUserDaoTest {

    private static EJBContainer container;
    private static boolean initialized = false;

    @Inject
    private UserSpringDataDao dao;

    @Test
    public void findAll() {
        Collection<User> users = dao.findAll();
        assertEquals(10, users.size());
    }

    @Test
    public void findByName() {
        User user = dao.findByName("bar-1");
        assertNotNull(user);
        assertEquals("bar-1", user.getName());
    }

    @BeforeClass
    public static void start() throws Exception {
        final Properties p = new Properties();

        p.setProperty("openejb.deployments.classpath.include", "spring-data-proxy-meta");
        p.setProperty("openejb.exclude-include.order", "exclude-include");

        p.setProperty("jdbc/DynamicUserDaoTest", "new://Resource?type=DataSource");
        p.setProperty("jdbc/DynamicUserDaoTest.JdbcDriver", "org.hsqldb.jdbcDriver");
        p.setProperty("jdbc/DynamicUserDaoTest.JdbcUrl", "jdbc:hsqldb:mem:moviedb");
        p.setProperty("jdbc/DynamicUserDaoTest.UserName", "sa");
        p.setProperty("jdbc/DynamicUserDaoTest.Password", "");

        container = EJBContainer.createEJBContainer(p);
    }

    @Before
    public void injectAndInit() throws NamingException {
        container.getContext().bind("inject", this);
        if (!initialized) {
            for (int i = 0; i < 10; i++) {
                final User u = new User();
                u.setAge(i % 4);
                if (i % 3 == 0) {
                    u.setName("foo");
                } else {
                    u.setName("bar-" + i);
                }
                dao.save(u);
            }
            initialized = true;
        }
    }

    @AfterClass
    public static void close() {
        container.close();
    }
}

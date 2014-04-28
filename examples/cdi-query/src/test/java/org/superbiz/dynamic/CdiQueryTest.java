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

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.embeddable.EJBContainer;
import javax.inject.Inject;
import javax.naming.NamingException;
import java.util.Collection;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * the UserDao can be injected but doesn't manage transaction so we create
 * a TxProvider bean which provides the tx context.
 * <p/>
 * In this sample it simply delegates but in real life it often aggregates multiple calls.
 */
public class CdiQueryTest {

    private static EJBContainer container;
    private static boolean initialized = false;

    @Inject
    private TxProvider dao;

    @Test
    public void findAll() {
        Collection<User> users = dao.findAll();
        assertEquals(10, users.size());
    }

    @BeforeClass
    public static void start() throws Exception {
        final Properties p = new Properties();

        p.setProperty("jdbc/CdiQueryTest", "new://Resource?type=DataSource");
        p.setProperty("jdbc/CdiQueryTest.JdbcDriver", "org.hsqldb.jdbcDriver");
        p.setProperty("jdbc/CdiQueryTest.JdbcUrl", "jdbc:hsqldb:mem:moviedb");
        p.setProperty("jdbc/CdiQueryTest.UserName", "sa");
        p.setProperty("jdbc/CdiQueryTest.Password", "");

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

    @Singleton
    @Lock(LockType.READ)
    public static class TxProvider { // just here to provide the transactional context
        @Inject
        private UserDao dao;

        public Collection<User> findAll() {
            return dao.findAll();
        }

        public void save(final User u) {
            dao.save(u);
        }
    }
}

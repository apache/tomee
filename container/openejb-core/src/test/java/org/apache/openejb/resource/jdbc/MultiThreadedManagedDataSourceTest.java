/**
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
package org.apache.openejb.resource.jdbc;

import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBContext;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Singleton;
import javax.sql.DataSource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(ApplicationComposer.class)
public class MultiThreadedManagedDataSourceTest {
    private static final String URL = "jdbc:hsqldb:mem:multi-tx-managed;hsqldb.tx=MVCC"; // mvcc otherwise multiple transaction tests will fail
    private static final String USER = "sa";
    private static final String PASSWORD = "";
    private static final String TABLE = "PUBLIC.MULTI_TX_MANAGED_DATASOURCE_TEST";
    private static final int INSERTS_NB = 200;

    @EJB
    private Persister persistManager;

    @BeforeClass
    public static void createTable() throws SQLException, ClassNotFoundException {
        Class.forName("org.hsqldb.jdbcDriver");

        final Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
        final Statement statement = connection.createStatement();
        statement.execute("CREATE TABLE " + TABLE + "(ID INTEGER)");
        statement.close();
        connection.commit();
        connection.close();
    }

    @Configuration
    public Properties config() {
        final Properties p = new Properties();
        p.put("openejb.jdbc.datasource-creator", "dbcp-alternative");

        p.put("managed", "new://Resource?type=DataSource");
        p.put("managed.JdbcDriver", "org.hsqldb.jdbcDriver");
        p.put("managed.JdbcUrl", URL);
        p.put("managed.UserName", USER);
        p.put("managed.Password", PASSWORD);
        p.put("managed.JtaManaged", "true");
        return p;
    }

    @Module
    public EjbJar app() throws Exception {
        return new EjbJar()
            .enterpriseBean(new SingletonBean(Persister.class).localBean());
    }

    @LocalBean
    @Singleton
    public static class Persister {
        private static final AtomicInteger ID = new AtomicInteger(1);

        @Resource(name = "managed")
        private DataSource ds;

        @Resource
        private EJBContext context;

        public int save() throws SQLException {
            final int id = ID.getAndIncrement();
            MultiThreadedManagedDataSourceTest.save(ds, id);
            return id;
        }

        public int saveRollback(final boolean ok) throws SQLException {
            final int id = ID.getAndIncrement();
            MultiThreadedManagedDataSourceTest.save(ds, id);
            if (!ok) {
                context.setRollbackOnly();
            }
            return id;
        }
    }

    @Test
    public void inserts() throws SQLException {
        final int start = count("");
        final AtomicInteger errors = new AtomicInteger(0);
        final AtomicInteger fail = new AtomicInteger(0);
        run(new Runnable() {
            @Override
            public void run() {
                int id = -1;
                try {
                    id = persistManager.save();
                } catch (final SQLException e) {
                    errors.incrementAndGet();
                }
                try {
                    if (!exists(id)) {
                        fail.incrementAndGet();
                    }
                } catch (final SQLException e) {
                    errors.incrementAndGet();
                }
            }
        });
        assertEquals(0, errors.get());
        assertEquals(0, fail.get());
        assertEquals(INSERTS_NB, count("") - start);
    }

    @Test
    public void insertsWithRollback() throws SQLException {
        final int count = count("");
        final AtomicInteger errors = new AtomicInteger(0);
        final AtomicInteger fail = new AtomicInteger(0);
        final AtomicInteger ok = new AtomicInteger(0);
        final List<Exception> ex = new CopyOnWriteArrayList<>();
        run(new Runnable() {
            @Override
            public void run() {
                final boolean rollback = Math.random() > 0.5;
                if (!rollback) {
                    ok.incrementAndGet();
                }
                int id = -1;
                try {
                    id = persistManager.saveRollback(!rollback);
                } catch (final SQLException e) {
                    errors.incrementAndGet();
                    ex.add(e);
                }
                if (!rollback) {
                    try {
                        if (!exists(id)) {
                            fail.incrementAndGet();
                        }
                    } catch (final SQLException e) {
                        errors.incrementAndGet();
                        ex.add(e);
                    }
                }
            }
        });
        for (final Exception e : ex) {
            e.printStackTrace(System.err);
        }
        assertEquals(0, errors.get());
        assertEquals(0, fail.get());
        assertEquals(ok.get(), count("") - count);
    }

    private static boolean exists(final int id) throws SQLException {
        return count(" WHERE ID = " + id) == 1;
    }

    private static int count(final String where) throws SQLException {
        final Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
        final Statement statement = connection.createStatement();
        final ResultSet result = statement.executeQuery("SELECT count(*) AS NB FROM " + TABLE + where);
        try {
            assertTrue(result.next());
            return result.getInt(1);
        } finally {
            statement.close();
            connection.close();
        }
    }

    private static void save(final DataSource ds, final int id) throws SQLException {
        execute(ds, "INSERT INTO " + TABLE + "(ID) VALUES(" + id + ")");
    }

    private static void execute(final DataSource ds, final String sql) throws SQLException {
        final Connection connection = ds.getConnection();
        final Statement statement = connection.createStatement();
        statement.executeUpdate(sql);
        statement.close();
        connection.close();
    }

    private static void execute(final Connection connection, final String sql) throws SQLException {
        final Statement statement = connection.createStatement();
        statement.executeUpdate(sql);
        statement.close();
        connection.close();
    }

    private void run(final Runnable runnable) {
        final ExecutorService es = Executors.newFixedThreadPool(20);
        for (int i = 0; i < INSERTS_NB; i++) {
            es.submit(runnable::run);
        }
        es.shutdown();
        try {
            es.awaitTermination(5, TimeUnit.MINUTES);
        } catch (final InterruptedException e) {
            fail();
        }
    }
}

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
package org.apache.openejb.resource.jdbc;

import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.ContainerProperties;
import org.apache.openejb.testing.SimpleLog;
import org.hsqldb.jdbcDriver;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import javax.sql.DataSource;
import java.io.Flushable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@SimpleLog
@ContainerProperties({
    @ContainerProperties.Property(name = "db", value = "new://Resource?type=DataSource"),
    @ContainerProperties.Property(name = "db.ResetOnError", value = "true"), // = retry(1)
    // @ContainerProperties.Property(name = "db.ResetOnErrorMethods", value = "*"),
    @ContainerProperties.Property(name = "db.JdbcDriver", value = "org.apache.openejb.resource.jdbc.ResettableDataSourceHandlerTest$DsTestDriver"),
})
@Classes(innerClassesAsBean = true)
@RunWith(ApplicationComposer.class)
public class ResettableDataSourceHandlerTest {
    @EJB
    private DsAccessor accessor;

    @Resource
    private DataSource ds;

    @Test
    public void run() throws IOException {
        actualTest(ds); // no tx
        Flushable.class.cast(ds).flush(); // ensure we dont reuse previous checks cached connection
        accessor.doTest(); // tx
    }

    public static void actualTest(final DataSource ds) {
        DsTestDriver.getConnectionCount.set(0);

        DsTestDriver.fail = true; // fail first otherwise we can get a cached connection with our test driver - ie hsqldb
        try {
            useConnection(ds);
        } catch (final SQLException e) {
            // no-op
        }
        assertEquals(0, DsTestDriver.getConnectionCount.get());

        DsTestDriver.fail = false;
        DsTestDriver.resilient = true; // will work since we retry by default
        try {
            useConnection(ds);
        } catch (final SQLException e) {
            // no-op
        }
        assertEquals(1 /* our conn */ + 1 /* the one created to validate the pool */, DsTestDriver.getConnectionCount.get());

        DsTestDriver.resilient = false;
        try {
            useConnection(ds);
            assertEquals(2 /* reuse existing */, DsTestDriver.getConnectionCount.get());
        } catch (final SQLException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    private static void useConnection(final DataSource ds) throws SQLException {
        try (final Connection connection = ds.getConnection()) {
            try (final Statement unused = connection.createStatement()) {
                // just touched a method to force connection init
            }
        }
    }

    @Singleton
    public static class DsAccessor {
        @Resource
        private DataSource ds;

        public void doTest() {
            actualTest(ds);
        }
    }

    public static class DsTestDriver implements Driver {
        private static volatile boolean fail;
        private static boolean resilient;
        private static AtomicInteger getConnectionCount = new AtomicInteger();

        public DsTestDriver() {
            jdbcDriver.class.getName();
        }

        @Override
        public Connection connect(final String url, final Properties info) throws SQLException {
            if (fail) {
                throw new SQLException();
            }
            if (resilient) {
                resilient = false;
                throw new SQLException();
            }

            getConnectionCount.incrementAndGet();
            try {
                return DriverManager.getConnection("jdbc:hsqldb:mem:resettabletest");
            } catch (final SQLException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public boolean acceptsURL(final String url) throws SQLException {
            return true;
        }

        @Override
        public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
            return new DriverPropertyInfo[0];
        }

        @Override
        public int getMajorVersion() {
            return 0;
        }

        @Override
        public int getMinorVersion() {
            return 0;
        }

        @Override
        public boolean jdbcCompliant() {
            return false;
        }

        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            return null;
        }
    }
}

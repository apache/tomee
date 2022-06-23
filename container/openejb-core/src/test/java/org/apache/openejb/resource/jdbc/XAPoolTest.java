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

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.managed.ManagedConnection;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.resource.jdbc.dbcp.ManagedDataSourceWithRecovery;
import org.apache.openejb.resource.jdbc.pool.DefaultDataSourceCreator;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.hsqldb.jdbc.pool.JDBCXAConnectionWrapper;
import org.hsqldb.jdbc.pool.JDBCXADataSource;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(ApplicationComposer.class)
public class XAPoolTest {
    @Resource(name = "xadb")
    private DataSource ds;

    @Module
    public EjbJar mandatory() {
        return new EjbJar();
    }

    @Configuration
    public Properties props() {
        return new PropertiesBuilder()
            .p("openejb.jdbc.datasource-creator", DefaultDataSourceCreator.class.getName())

            .p("txMgr", "new://TransactionManager?type=TransactionManager")
            .p("txMgr.txRecovery", "true")
            .p("txMgr.logFileDir", "target/test/xa/howl_XAPoolTest")

                // real XA datasources
            .p("xa", "new://Resource?class-name=" + JDBCXADataSource.class.getName())
            .p("xa.url", "jdbc:hsqldb:mem:dbcpxa")
            .p("xa.user", "sa")
            .p("xa.password", "")
            .p("xa.SkipImplicitAttributes", "true")
            .p("xa.SkipPropertiesFallback", "true") // otherwise goes to connection properties

            .p("xadb", "new://Resource?type=DataSource")
            .p("xadb.xaDataSource", "xa")
            .p("xadb.JtaManaged", "true")
            .p("xadb.MaxIdle", "25")
            .p("xadb.MaxTotal", "25")
            .p("xadb.InitialSize", "3")

            .build();
    }

    @Test
    public void check() throws SQLException {
        assertNotNull(ds);
        final BasicDataSource tds = ManagedDataSourceWithRecovery.class.cast(ds);

        assertEquals(0, tds.getNumIdle()); // db not yet used so no connection at all (dbcp behavior)

        try (final Connection c = ds.getConnection()) {
            assertNotNull(c);

            final Connection connection = c.getMetaData().getConnection(); // just to do something and force the connection init
            assertThat(connection, instanceOf(ManagedConnection.class));
            assertTrue(connection.toString().contains("URL=jdbc:hsqldb:mem:dbcpxa, HSQL Database Engine Driver"));
        } // here we close the connection so we are back in the initial state

        assertEquals(0, tds.getNumActive());
        assertEquals(3, tds.getNumIdle());

        for (int it = 0; it < 5; it++) { // ensures it always works and not only the first time
            final Collection<Connection> connections = new ArrayList<>(25);
            for (int i = 0; i < 25; i++) {
                final Connection connection = ds.getConnection();
                connections.add(connection);
                connection.getMetaData(); // trigger connection retrieving otherwise nothing is done (pool is not used)
            }
            assertEquals(25, tds.getNumActive());
            assertEquals(0, tds.getNumIdle());
            for (final Connection toClose : connections) {
                toClose.close();
            }
            assertEquals(0, tds.getNumActive());
            assertEquals(25, tds.getNumIdle());
        }
    }
}



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

import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.resource.jdbc.pool.DefaultDataSourceCreator;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testing.SimpleLog;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.openejb.util.reflection.Reflections;
import org.hsqldb.jdbc.pool.JDBCXADataSource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

@SimpleLog
@RunWith(ApplicationComposer.class)
public class DbcpNPEXAConnectionTest {
    @Resource(name = "xadb")
    private DataSource ds;

    @EJB
    private AnEjb ejb;

    @Module
    @Classes(AnEjb.class)
    public EjbJar mandatory() {
        return new EjbJar();
    }

    @Configuration
    public Properties props() {
        return new PropertiesBuilder()
                .p("openejb.jdbc.datasource-creator", DefaultDataSourceCreator.class.getName())

                .p("txMgr", "new://TransactionManager?type=TransactionManager")
                .p("txMgr.txRecovery", "true")
                .p("txMgr.logFileDir", "target/test/xa/DbcpNPEXAConnectionTest")

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
        final Connection con = ejb.newConn();
        con.close(); // no NPE
        Assert.assertTrue("Connection was not closed", con.isClosed());
        final GenericObjectPool<PoolableConnection> pool =  GenericObjectPool.class.cast(Reflections.get(ds, "connectionPool"));
        assertEquals(0, pool.getNumActive());
    }

    @Singleton
    public static class AnEjb {
        @Resource(name = "xadb")
        private DataSource ds;

        public Connection newConn() throws SQLException {
            final Connection con = ds.getConnection();
            con.close(); // first connection is not "shared" so closes correctly
            Assert.assertTrue("Connection was not closed", con.isClosed());
            return use(ds.getConnection()); // this one is shared so delegate will be null and close outside JTA will fail
        }
    }

    private static Connection use(final Connection connection) {
        try {
            connection.getMetaData();
        } catch (final SQLException e) {
            Assert.fail(e.getMessage());
        }
        return connection;
    }
}


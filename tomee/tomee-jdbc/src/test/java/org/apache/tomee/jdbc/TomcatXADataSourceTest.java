/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.jdbc;

import org.apache.openejb.OpenEJB;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.resource.jdbc.managed.local.ManagedDataSource;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.hsqldb.jdbc.pool.JDBCXAConnectionWrapper;
import org.hsqldb.jdbc.pool.JDBCXADataSource;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import javax.sql.ConnectionEventListener;
import javax.sql.DataSource;
import javax.sql.StatementEventListener;
import javax.sql.XAConnection;
import jakarta.transaction.Synchronization;
import javax.transaction.xa.XAResource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(ApplicationComposer.class)
public class TomcatXADataSourceTest {
    @Resource(name = "xadb")
    private DataSource ds;

    @Resource(name = "xadb2")
    private DataSource badDs;

    @EJB
    private TxP tx;

    @Module
    @Classes(TxP.class)
    public EjbJar mandatory() {
        return new EjbJar();
    }

    @Configuration
    public Properties props() {
        return new PropertiesBuilder()
                .p("openejb.jdbc.datasource-creator", TomEEDataSourceCreator.class.getName())

                .p("txMgr", "new://TransactionManager?type=TransactionManager")
                .p("txMgr.txRecovery", "true")
                .p("txMgr.logFileDir", "target/test/xa/howl")

                // real XA datasources
                .p("xa", "new://Resource?class-name=" + JDBCXADataSource.class.getName())
                .p("xa.url", "jdbc:hsqldb:mem:tomcat-xa")
                .p("xa.user", "sa")
                .p("xa.password", "")
                .p("xa.SkipImplicitAttributes", "true")
                .p("xa.SkipPropertiesFallback", "true") // otherwise goes to connection properties

                .p("xadb", "new://Resource?type=DataSource")
                .p("xadb.xaDataSource", "xa")
                .p("xadb.JtaManaged", "true")
                .p("xadb.MaxIdle", "25")
                .p("xadb.MaxActive", "25")
                .p("xadb.InitialSize", "3")

                .p("xa2", "new://Resource?class-name=" + BadDataSource.class.getName())
                .p("xa2.url", "jdbc:hsqldb:mem:tomcat-xa2")
                .p("xa2.user", "sa")
                .p("xa2.password", "")
                .p("xa2.SkipImplicitAttributes", "true")
                .p("xa2.SkipPropertiesFallback", "true") // otherwise goes to connection properties

                .p("xadb2", "new://Resource?type=DataSource")
                .p("xadb2.xaDataSource", "xa2")
                .p("xadb2.JtaManaged", "true")
                .p("xadb2.MaxIdle", "25")
                .p("xadb2.MaxActive", "25")
                .p("xadb2.InitialSize", "3")

                .build();
    }

    @Test
    public void check() throws SQLException {
        assertNotNull(ds);
        final TomEEDataSourceCreator.TomEEDataSource tds = TomEEDataSourceCreator.TomEEDataSource.class.cast(ManagedDataSource.class.cast(ds).getDelegate());

        assertEquals(3, tds.getIdle()); // InitSize

        try (final Connection c = ds.getConnection()) {
            assertNotNull(c);

            final Connection connection = c.getMetaData().getConnection(); // just to do something and force the connection init
            assertThat(connection, instanceOf(JDBCXAConnectionWrapper.class));
        } // here we close the connection so we are back in the initial state

        assertEquals(0, tds.getActive());
        assertEquals(3, tds.getIdle());

        for (int it = 0; it < 5; it++) { // ensures it always works and not only the first time
            final Collection<Connection> connections = new ArrayList<>(25);
            for (int i = 0; i < 25; i++) {
                final Connection connection = ds.getConnection();
                connections.add(connection);
                connection.getMetaData(); // trigger connection retrieving otherwise nothing is done (pool is not used)
            }
            assertEquals(25, tds.getActive());
            assertEquals(0, tds.getIdle());
            for (final Connection toClose : connections) {
                toClose.close();
            }
            assertEquals(0, tds.getActive());
            assertEquals(25, tds.getIdle());
        }

        // in tx - closing in tx
        for (int it = 0; it < 5; it++) { // ensures it always works and not only the first time
            for (int i = 0; i < 25; i++) {
                tx.run(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Connection c = null;
                            for (int i = 0; i < 25; i++) {
                                final Connection connection = ds.getConnection();
                                connection.getMetaData(); // trigger connection retrieving otherwise nothing is done (pool is not used)
                                if (c != null) {
                                    assertEquals(c, connection);
                                } else {
                                    c = connection;
                                }
                            }
                            c.close(); // ensure we handle properly eager close invocations
                        } catch (final SQLException sql) {
                            fail(sql.getMessage());
                        }
                    }
                });
            }
            assertEquals(0, tds.getActive());
            assertEquals(25, tds.getIdle());
        }

        // in tx - not closing
        for (int it = 0; it < 5; it++) { // ensures it always works and not only the first time
            for (int i = 0; i < 25; i++) {
                tx.run(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Connection c = null;
                            for (int i = 0; i < 25; i++) {
                                final Connection connection = ds.getConnection();
                                connection.getMetaData(); // trigger connection retrieving otherwise nothing is done (pool is not used)
                                if (c != null) {
                                    assertEquals(c, connection);
                                } else {
                                    c = connection;
                                }
                            }
                        } catch (final SQLException sql) {
                            fail(sql.getMessage());
                        }
                    }
                });
            }
            assertEquals(0, tds.getActive());
            assertEquals(25, tds.getIdle());
        }

        // in tx - closing after tx
        for (int it = 0; it < 5; it++) { // ensures it always works and not only the first time
            for (int i = 0; i < 25; i++) {
                final AtomicReference<Connection> ref = new AtomicReference<>();
                tx.run(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Connection c = null;
                            for (int i = 0; i < 25; i++) {
                                final Connection connection = ds.getConnection();
                                connection.getMetaData(); // trigger connection retrieving otherwise nothing is done (pool is not used)
                                if (c != null) {
                                    assertEquals(c, connection);
                                } else {
                                    c = connection;
                                    ref.set(c);
                                }
                            }
                        } catch (final SQLException sql) {
                            fail(sql.getMessage());
                        }
                    }
                });
                assertTrue(ref.get().isClosed()); // closed with tx
                ref.get().close();
                assertTrue(ref.get().isClosed());
            }
            assertEquals(0, tds.getActive());
            assertEquals(25, tds.getIdle());
        }

        // in tx - closing in commit
        for (int it = 0; it < 5; it++) { // ensures it always works and not only the first time
            for (int i = 0; i < 25; i++) {
                tx.run(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final Connection ref = ds.getConnection();
                            ref.getMetaData();
                            OpenEJB.getTransactionManager().getTransaction().registerSynchronization(new Synchronization() {
                                @Override
                                public void beforeCompletion() {
                                    // no-op
                                }

                                @Override
                                public void afterCompletion(final int status) { // JPA does it
                                    try {
                                        ref.close();
                                    } catch (final SQLException e) {
                                        fail(e.getMessage());
                                    }
                                }
                            });
                        } catch (final Exception sql) {
                            fail(sql.getMessage());
                        }
                    }
                });
            }
            assertEquals(0, tds.getActive());
            assertEquals(25, tds.getIdle());
        }

        // underlying connection closed when fetch from pool
        for (int it = 0; it < 5; it++) { // ensures it always works and not only the first time
            for (int i = 0; i < 25; i++) {
                tx.run(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final Connection ref = badDs.getConnection();
                            OpenEJB.getTransactionManager().getTransaction().registerSynchronization(new Synchronization() {
                                @Override
                                public void beforeCompletion() {
                                    // no-op
                                }

                                @Override
                                public void afterCompletion(final int status) { // JPA does it
                                    try {
                                        ref.close();
                                    } catch (final SQLException e) {
                                        fail(e.getMessage());
                                    }
                                }
                            });
                            ref.getMetaData();
                        } catch (final Exception sql) {
                            // we expect this
                        }
                    }
                });
            }
            assertEquals(0, tds.getActive());
            assertEquals(25, tds.getIdle());
        }
    }

    @Singleton
    public static class TxP {
        public void run(final Runnable r) {
            r.run();
        }
    }

    public static class BadDataSource extends JDBCXADataSource {
        public BadDataSource() throws SQLException {
            // no-op
        }

        @Override
        public XAConnection getXAConnection() throws SQLException {
            return corrupt(super.getXAConnection());
        }

        @Override
        public XAConnection getXAConnection(final String user, final String pwd) throws SQLException {
            return corrupt(super.getXAConnection());
        }

        // this closes the underlying connection - which should cause enlist to fail
        private XAConnection corrupt(final XAConnection xaConnection) throws SQLException {
            return new XAConnection() {
                private final XAConnection delegate = xaConnection;

                @Override
                public XAResource getXAResource() throws SQLException {
                    return delegate.getXAResource();
                }

                @Override
                public Connection getConnection() throws SQLException {
                    final Connection connection = delegate.getConnection();
                    if (!connection.isClosed()) {
                        connection.close();
                    }
                    return connection;
                }

                @Override
                public void close() throws SQLException {
                    delegate.close();
                }

                @Override
                public void addConnectionEventListener(final ConnectionEventListener listener) {
                    delegate.addConnectionEventListener(listener);
                }

                @Override
                public void removeConnectionEventListener(final ConnectionEventListener listener) {
                    delegate.removeConnectionEventListener(listener);
                }

                @Override
                public void addStatementEventListener(final StatementEventListener listener) {
                    delegate.addStatementEventListener(listener);
                }

                @Override
                public void removeStatementEventListener(final StatementEventListener listener) {
                    delegate.removeStatementEventListener(listener);
                }
            };
        }
    }
}

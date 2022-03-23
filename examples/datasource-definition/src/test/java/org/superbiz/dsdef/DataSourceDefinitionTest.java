/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.dsdef;

import org.apache.openejb.resource.jdbc.dbcp.DbcpManagedDataSource;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import jakarta.ejb.embeddable.EJBContainer;
import jakarta.inject.Inject;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class DataSourceDefinitionTest {

    private static EJBContainer container;

    @Inject
    private Persister persister;

    @BeforeClass
    public static void start() {
        container = EJBContainer.createEJBContainer();
    }

    @Before
    public void inject() throws NamingException {
        container.getContext().bind("inject", this);
    }

    @AfterClass
    public static void stop() {
        container.close();
    }

    @Test
    public void checkDs() throws SQLException {
        final DataSource ds = persister.getDs();
        assertNotNull(ds);
        assertThat(ds, instanceOf(DbcpManagedDataSource.class));

        final DbcpManagedDataSource castedDs = (DbcpManagedDataSource) ds;

        final String driver = castedDs.getDriverClassName();
        assertEquals("org.h2.jdbcx.JdbcDataSource", driver);

        final String user = castedDs.getUserName();
        assertEquals("sa", user);

        final String url = castedDs.getUrl();
        assertEquals("jdbc:h2:mem:persister", url);

        final int initPoolSize = castedDs.getInitialSize();
        assertEquals(1, initPoolSize);

        final int maxIdle = castedDs.getMaxIdle();
        assertEquals(3, maxIdle);

        final Connection connection = ds.getConnection();
        assertNotNull(connection);

        execute(connection, "CREATE TABLE TEST(ID INT PRIMARY KEY, NAME VARCHAR(255))");
        execute(connection, "INSERT INTO TEST(ID, NAME) VALUES(1, 'foo')");
        connection.commit();

        final PreparedStatement statement = ds.getConnection().prepareStatement("SELECT NAME FROM TEST");
        statement.execute();
        final ResultSet set = statement.getResultSet();

        assertTrue(set.next());
        assertEquals("foo", set.getString("NAME"));
    }

    @Test
    public void lookup() throws NamingException {
        final Object o = container.getContext().lookup("java:app/jdbc/persister");
        assertNotNull(o);
        assertEquals(persister.getDs(), o);
    }

    private void execute(final Connection connection, final String sql) throws SQLException {
        connection.prepareStatement(sql).execute();
    }
}

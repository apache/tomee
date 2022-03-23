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
package org.apache.openejb.assembler.classic;

import org.apache.commons.dbcp2.DelegatingConnection;
import org.apache.commons.dbcp2.managed.ManagedConnection;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.resource.jdbc.dbcp.DbcpManagedDataSource;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.testing.Module;
import org.hsqldb.jdbc.JDBCConnection;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import jakarta.annotation.sql.DataSourceDefinition;
import jakarta.annotation.sql.DataSourceDefinitions;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;

@RunWith(ApplicationComposer.class)
public class DataSourceDefinitionJndiTest {

    @EJB
    private DatasourceDefinitionsBean multipleDatasources;

    @Module
    public Class<?>[] app() throws Exception {
        return new Class<?>[]{DatasourceDefinitionsBean.class};
    }


    @DataSourceDefinitions({
        @DataSourceDefinition(
            name = "java:global/foo",
            className = "org.hsqldb.jdbc.JDBCDataSource",
            user = "sa",
            password = "",
            url = "jdbc:hsqldb:mem:dsdjt1"
        ),
        @DataSourceDefinition(
            name = "java:app/foo",
            className = "org.hsqldb.jdbc.JDBCDataSource",
            user = "sa",
            password = "",
            url = "jdbc:hsqldb:mem:dsdjt2"
        )
    })
    @Stateless
    public static class DatasourceDefinitionsBean {
        @Resource(name = "java:app/foo")
        private DataSource app;
        @Resource(name = "java:global/foo")
        private DataSource global;

        public DataSource getApp() {
            return app;
        }

        public DataSource getGlobal() {
            return global;
        }
    }

    @Test
    public void checkInjections() throws Exception {
        final DataSource global = multipleDatasources.getGlobal();
        check(global, "dsdjt1");
        final DataSource app = multipleDatasources.getApp();
        check(app, "dsdjt2");
    }

    @Test
    public void checkGlobal() throws Exception {
        final DataSource ds = (DataSource) SystemInstance.get().getComponent(ContainerSystem.class).getJNDIContext().lookup("java:global/foo");
        check(ds, "dsdjt1");
    }

    private void check(final DataSource ds, final String name) throws SQLException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // the first "cast part" is not important, we just want to check the jdbc url is ok
        assertThat(ds, instanceOf(DbcpManagedDataSource.class));
        final DbcpManagedDataSource dbcp = (DbcpManagedDataSource) ds;
        final Connection connection = dbcp.getConnection();
        assertThat(connection, instanceOf(ManagedConnection.class));
        final ManagedConnection mc = (ManagedConnection) connection;
        final Method getInnermostDelegateInternal = DelegatingConnection.class.getDeclaredMethod("getInnermostDelegateInternal");
        getInnermostDelegateInternal.setAccessible(true);
        final Connection delegate = (Connection) getInnermostDelegateInternal.invoke(mc);
        assertThat(delegate, instanceOf(JDBCConnection.class));
        final Method getURL = JDBCConnection.class.getDeclaredMethod("getURL");
        getURL.setAccessible(true);
        assertEquals("jdbc:hsqldb:mem:" + name, getURL.invoke(delegate));
    }
}

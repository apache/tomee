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
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.resource.jdbc.dbcp.BasicManagedDataSource;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.hsqldb.jdbc.JDBCConnection;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.sql.DataSourceDefinition;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PersistenceUnit;
import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;

@RunWith(ApplicationComposer.class)
public class DataSourceDefinitionGlobalJPATest {

    @EJB
    private EmfHolder holder;

    @Module
    public Class<?>[] app() throws Exception {
        return new Class<?>[]{EmfHolder.class};
    }

    @Configuration // useless but add another datasource so resolution should be more tricky, name before d
    public Properties config() {
        final Properties p = new Properties();
        p.put("cczczcz", "new://Resource?type=DataSource");
        p.put("cczczcz.JdbcDriver", "org.hsqldb.jdbcDriver");
        p.put("cczczcz.JdbcUrl", "jdbc:hsqldb:mem:cczczcz");
        return p;
    }

    @Module
    public Persistence persistence() {
        final org.apache.openejb.jee.jpa.unit.PersistenceUnit unit = new org.apache.openejb.jee.jpa.unit.PersistenceUnit("jpa-global-dsdef-unit");
        unit.addClass(IdEntity.class);
        unit.setProperty("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
        unit.getProperties().setProperty("openjpa.RuntimeUnenhancedClasses", "supported");
        unit.setJtaDataSource("java:app/foo");
        unit.setExcludeUnlistedClasses(true);

        final Persistence persistence = new Persistence(unit);
        persistence.setVersion("2.0");
        return persistence;
    }

    @DataSourceDefinition(
        name = "java:app/foo",
        className = "org.hsqldb.jdbc.JDBCDataSource",
        user = "sa",
        password = "",
        url = "jdbc:hsqldb:mem:dsdjpa"
    )
    @Stateless
    public static class EmfHolder {
        @PersistenceUnit
        private EntityManagerFactory emf;

        public EntityManagerFactory getEmf() {
            return emf;
        }
    }

    @Entity
    public static class IdEntity {
        @Id
        @GeneratedValue
        private long id;

        public long getId() {
            return id;
        }

        public void setId(final long id) {
            this.id = id;
        }
    }

    @Test
    public void check() throws Exception {
        final EntityManagerFactory emf = holder.getEmf();
        assertThat(emf, instanceOf(ReloadableEntityManagerFactory.class));
        final ReloadableEntityManagerFactory remf = (ReloadableEntityManagerFactory) emf;
        final DataSource ds = remf.info().getJtaDataSource();
        check(ds, "dsdjpa");
    }

    private void check(final DataSource ds, final String name) throws SQLException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // the first "cast part" is not important, we just want to check the jdbc url is ok
        assertThat(ds, instanceOf(BasicManagedDataSource.class));
        final BasicManagedDataSource dbcp = (BasicManagedDataSource) ds;
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

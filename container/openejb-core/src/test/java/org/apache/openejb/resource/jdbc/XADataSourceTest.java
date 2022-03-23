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
package org.apache.openejb.resource.jdbc;

import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.loader.Files;
import org.apache.openejb.resource.jdbc.dbcp.DbcpDataSourceCreator;
import org.apache.openejb.resource.jdbc.pool.DataSourceCreator;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.hsqldb.jdbc.pool.JDBCXADataSource;
import org.hsqldb.jdbcDriver;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.ejb.Singleton;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PersistenceContext;
import java.io.File;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@RunWith(ApplicationComposer.class)
public class XADataSourceTest {
    @EJB
    private XAEJB ejb;

    @Test
    public void checkOperationsWork() {
        ejb.doSthg();
        ejb.assertPersisted();
        try {
            ejb.forceRollback();
        } catch (final EJBException ejbEx) {
            assertThat(ejbEx.getCause(), instanceOf(IllegalArgumentException.class));
        }
        ejb.assertPersisted();
    }

    @Configuration
    public java.util.Properties config() {
        final File file = new File("target/test/xa/howl");
        if (file.isDirectory()) {
            Files.delete(file);
        }

        final Properties p = new Properties();
        p.put(DataSourceCreator.class.getName(), DbcpDataSourceCreator.class.getName()); // default dbcp pool supports xaDataSource config, not our proxy layer

        p.put("txMgr", "new://TransactionManager?type=TransactionManager");
        p.put("txMgr.txRecovery", "true");
        p.put("txMgr.logFileDir", "target/test/xa/howl");

        // real XA datasources
        p.put("xa", "new://Resource?class-name=" + JDBCXADataSource.class.getName());
        p.put("xa.url", "jdbc:hsqldb:mem:xa");
        p.put("xa.user", "sa");
        p.put("xa.password", "");
        p.put("xa.SkipImplicitAttributes", "true"); // conflict with connectionProperties

        p.put("xa2", "new://Resource?class-name=" + JDBCXADataSource.class.getName());
        p.put("xa2.url", "jdbc:hsqldb:mem:xa2");
        p.put("xa2.user", "sa");
        p.put("xa2.password", "");
        p.put("xa2.SkipImplicitAttributes", "true");

        // pooled "XA" datasources
        p.put("xadb", "new://Resource?type=DataSource");
        p.put("xadb.XaDataSource", "xa"); // to be xa
        p.put("xadb.JtaManaged", "true");

        p.put("xadb2", "new://Resource?type=DataSource");
        p.put("xadb2.XaDataSource", "xa2"); // to be xa
        p.put("xadb2.JtaManaged", "true");

        // non jta datasources
        p.put("xadbn", "new://Resource?class-name=" + JDBCXADataSource.class.getName());
        p.put("xadbn.JdbcDriver", jdbcDriver.class.getName());
        p.put("xadbn.url", "jdbc:hsqldb:mem:xa");
        p.put("xadbn.user", "sa");
        p.put("xadbn.password", "");

        p.put("xadbn2", "new://Resource?type=DataSource");
        p.put("xadbn2.JdbcDriver", jdbcDriver.class.getName());
        p.put("xadbn2.JdbcUrl", "jdbc:hsqldb:mem:xa2");
        p.put("xadbn2.UserName", "sa");
        p.put("xadbn2.Password", "");
        p.put("xadbn2.JtaManaged", "false");

        return p;
    }

    @Module
    public Persistence pXml() throws Exception {
        final Persistence persistence = new Persistence();
        {
            final PersistenceUnit persistenceUnit = new PersistenceUnit();
            persistenceUnit.setName("xadb");
            persistenceUnit.setJtaDataSource("xadb");
            persistenceUnit.setNonJtaDataSource("xadbn");
            persistenceUnit.getClazz().add(XAEntity.class.getName());
            persistenceUnit.setExcludeUnlistedClasses(true);
            persistenceUnit.getProperties().setProperty("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
            persistence.getPersistenceUnit().add(persistenceUnit);
        }
        {
            final PersistenceUnit persistenceUnit = new PersistenceUnit();
            persistenceUnit.setName("xadb2");
            persistenceUnit.setJtaDataSource("xadb2");
            persistenceUnit.setNonJtaDataSource("xadbn2");
            persistenceUnit.getClazz().add(XAEntity.class.getName());
            persistenceUnit.setExcludeUnlistedClasses(true);
            persistenceUnit.getProperties().setProperty("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
            persistence.getPersistenceUnit().add(persistenceUnit);
        }
        return persistence;
    }

    @Module
    public EjbJar app() throws Exception {
        return new EjbJar().enterpriseBean(new SingletonBean(XAEJB.class).localBean());
    }

    @Singleton
    public static class XAEJB {
        @PersistenceContext(unitName = "xadb")
        private EntityManager em;

        @PersistenceContext(unitName = "xadb2")
        private EntityManager em2;

        public void doSthg() {
            em.persist(new XAEntity());
            em2.persist(new XAEntity());
        }

        public void forceRollback() {
            doSthg();
            throw new IllegalArgumentException(); // force rollback
        }

        public void assertPersisted() {
            assertNotNull(em.createQuery("select m from XADataSourceTest$XAEntity m").getSingleResult());
            assertNotNull(em2.createQuery("select m from XADataSourceTest$XAEntity m").getSingleResult());
        }
    }

    @Entity
    public static class XAEntity {
        @Id
        @GeneratedValue
        private long id;

        public long getId() {
            return id;
        }
    }
}

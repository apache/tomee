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

import org.apache.openejb.OpenEJB;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.ResourceInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.StatelessSessionContainerInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.PersistenceModule;
import org.apache.openejb.config.sys.Resource;
import org.apache.openejb.core.LocalInitialContextFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.jee.jpa.unit.TransactionType;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.resource.jdbc.router.Router;
import org.apache.openejb.spi.ContainerSystem;
import org.junit.After;
import org.junit.Test;

import jakarta.ejb.Local;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Id;
import jakarta.persistence.PersistenceContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.assertEquals;

public class DynamicDataSourceTest {

    @After
    public void tearDown() throws Exception {
        OpenEJB.destroy();
    }

    @Test
    public void route() throws Exception {
        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());

        final ConfigurationFactory config = new ConfigurationFactory();

        final Assembler assembler = new Assembler();
        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        // resources
        for (int i = 1; i <= 3; i++) {
            final String dbName = "database" + i;
            final Resource resourceDs = new Resource(dbName, "DataSource");
            final Properties p = resourceDs.getProperties();
            p.put("JdbcDriver", "org.hsqldb.jdbcDriver");
            p.put("JdbcUrl", "jdbc:hsqldb:mem:db" + i);
            p.put("UserName", "sa");
            p.put("Password", "");
            p.put("JtaManaged", "true");
            assembler.createResource(config.configureService(resourceDs, ResourceInfo.class));
        }
        final Resource resourceRouter = new Resource("My Router", "org.apache.openejb.router.test.DynamicDataSourceTest$DeterminedRouter", "org.router:DeterminedRouter");
        resourceRouter.getProperties().setProperty("DatasourceNames", "database1 database2 database3");
        resourceRouter.getProperties().setProperty("DefaultDataSourceName", "database1");
        assembler.createResource(config.configureService(resourceRouter, ResourceInfo.class));

        final Resource resourceRoutedDs = new Resource("Routed Datasource", "org.apache.openejb.resource.jdbc.Router", "RoutedDataSource");
        resourceRoutedDs.getProperties().setProperty("Router", "My Router");
        assembler.createResource(config.configureService(resourceRoutedDs, ResourceInfo.class));

        // containers
        final StatelessSessionContainerInfo statelessContainerInfo = config.configureService(StatelessSessionContainerInfo.class);
        assembler.createContainer(statelessContainerInfo);

        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean(RoutedEJBBean.class));
        ejbJar.addEnterpriseBean(new StatelessBean(UtilityBean.class));

        final EjbModule ejbModule = new EjbModule(ejbJar);

        // Create an "ear"
        final AppModule appModule = new AppModule(ejbModule.getClassLoader(), "test-dynamic-data-source");
        appModule.getEjbModules().add(ejbModule);

        // Create a persistence-units
        final PersistenceUnit unit = new PersistenceUnit("router");
        unit.addClass(Person.class);
        unit.getProperties().put("openjpa.jdbc.SynchronizeMappings", "buildSchema");
        unit.setTransactionType(TransactionType.JTA);
        unit.setJtaDataSource("Routed Datasource");
        appModule.addPersistenceModule(new PersistenceModule("root", new Persistence(unit)));
        for (int i = 1; i <= 3; i++) {
            final PersistenceUnit u = new PersistenceUnit("db" + i);
            u.addClass(Person.class);
            u.getProperties().put("openjpa.jdbc.SynchronizeMappings", "buildSchema");
            u.setTransactionType(TransactionType.JTA);
            u.setJtaDataSource("database" + i);
            appModule.addPersistenceModule(new PersistenceModule("root", new Persistence(u)));
        }

        assembler.createApplication(config.configureApplication(appModule));

        // context
        final Context ctx = new InitialContext();

        // running persist on all "routed" databases
        final List<String> databases = new ArrayList<>();
        databases.add("database1");
        databases.add("database2");
        databases.add("database3");

        // convinient bean to create tables for each persistence unit
        final Utility utility = (Utility) ctx.lookup("UtilityBeanLocal");
        utility.initDatabase();

        final RoutedEJB ejb = (RoutedEJB) ctx.lookup("RoutedEJBBeanLocal");
        for (int i = 0; i < 18; i++) {
            final String name = "record " + i;
            final String db = databases.get(i % 3);
            ejb.persist(i, name, db);
        }

        // assert database records number using jdbc
        for (int i = 1; i <= 3; i++) {
            final Connection connection = DriverManager.getConnection("jdbc:hsqldb:mem:db" + i, "sa", "");
            final Statement st = connection.createStatement();
            final ResultSet rs = st.executeQuery("select count(*) from \"DynamicDataSourceTest$Person\"");
            rs.next();
            assertEquals(6, rs.getInt(1));
            st.close();
            connection.close();
        }
    }

    @Stateless
    @Local(RoutedEJB.class)
    public static class RoutedEJBBean implements RoutedEJB {
        @PersistenceContext(unitName = "router")
        private EntityManager em;

        @jakarta.annotation.Resource(name = "My Router", type = DeterminedRouter.class)
        private DeterminedRouter router;

        public void persist(final int id, final String name, final String ds) {
            router.setDataSource(ds);
            em.persist(new Person(id, name));
        }

    }

    @Stateless
    @Local(Utility.class)
    public static class UtilityBean implements Utility {

        @PersistenceContext(unitName = "db1")
        private EntityManager em1;
        @PersistenceContext(unitName = "db2")
        private EntityManager em2;
        @PersistenceContext(unitName = "db3")
        private EntityManager em3;

        @TransactionAttribute(TransactionAttributeType.SUPPORTS)
        public void initDatabase() {
            em1.find(Person.class, 0);
            em2.find(Person.class, 0);
            em3.find(Person.class, 0);
        }
    }

    public static interface RoutedEJB {
        void persist(int id, String name, String ds);
    }

    public static interface Utility {
        void initDatabase();
    }

    @Entity
    public static class Person {
        @Id
        private long id;
        private String name;

        public Person() {
            // no-op
        }

        public Person(final int i, final String n) {
            id = i;
            name = n;
        }

        public long getId() {
            return id;
        }

        public void setId(final long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }
    }

    public static class DeterminedRouter implements Router {
        private String dataSourceNames;
        private String defaultDataSourceName;
        private Map<String, DataSource> dataSources = null;
        private final ThreadLocal<DataSource> currentDataSource = new ThreadLocal<>();

        /**
         * @param datasourceList datasource resource name, separator is a space
         */
        public void setDataSourceNames(final String datasourceList) {
            dataSourceNames = datasourceList;
        }

        /**
         * lookup datasource in openejb resources
         */
        private void init() {
            dataSources = new ConcurrentHashMap<>();
            for (final String ds : dataSourceNames.split(" ")) {
                final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);

                Object o = null;
                final Context ctx = containerSystem.getJNDIContext();
                try {
                    o = ctx.lookup("openejb:Resource/" + ds);
                    if (o instanceof DataSource) {
                        dataSources.put(ds, (DataSource) o);
                    }
                } catch (final NamingException e) {
                }
            }
        }

        /**
         * @return the user selected data source if it is set
         * or the default one
         * @throws IllegalArgumentException if the data source is not found
         */
        public DataSource getDataSource() {
            // lazy init of routed datasources
            if (dataSources == null) {
                init();
            }

            // if no datasource is selected use the default one
            if (currentDataSource.get() == null) {
                if (dataSources.containsKey(defaultDataSourceName)) {
                    return dataSources.get(defaultDataSourceName);

                } else {
                    throw new IllegalArgumentException("you have to specify at least one datasource");
                }
            }

            // the developper set the datasource to use
            return currentDataSource.get();
        }

        /**
         * @param datasourceName data source name
         */
        public void setDataSource(final String datasourceName) {
            if (dataSources == null) {
                init();
            }
            if (!dataSources.containsKey(datasourceName)) {
                throw new IllegalArgumentException("data source called " + datasourceName + " can't be found.");
            }
            final DataSource ds = dataSources.get(datasourceName);
            currentDataSource.set(ds);
        }

        /**
         * reset the data source
         */
        public void clear() {
            currentDataSource.remove();
        }

        public void setDefaultDataSourceName(final String name) {
            this.defaultDataSourceName = name;
        }
    }
}

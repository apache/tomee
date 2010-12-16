package org.apache.openejb.router.test;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.ResourceInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.StatelessSessionContainerInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.client.LocalInitialContextFactory;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.PersistenceModule;
import org.apache.openejb.config.sys.Resource;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.jee.jpa.unit.TransactionType;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.resource.jdbc.Router;
import org.apache.openejb.spi.ContainerSystem;
import org.hsqldb.jdbcDriver;
import org.junit.Test;

public class DynamicDataSourceTest {
    @Test
    public void route() throws Exception {
        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());

        ConfigurationFactory config = new ConfigurationFactory();

        Assembler assembler = new Assembler();
        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        // resources
        for (int i = 1; i <= 3; i++) {
            String dbName = "database" + i;
            Resource resourceDs = new Resource(dbName, "DataSource");
            Properties p = resourceDs.getProperties();
            p.put("JdbcDriver", "org.hsqldb.jdbcDriver");
            p.put("JdbcUrl", "jdbc:hsqldb:mem:db" + i);
            p.put("UserName", "sa");
            p.put("Password", "");
            p.put("JtaManaged", "true");
            assembler.createResource(config.configureService(resourceDs, ResourceInfo.class));
        }
        Resource resourceRouter = new Resource("My Router", "org.apache.openejb.router.test.DynamicDataSourceTest$DeterminedRouter", "org.router:DeterminedRouter");
        resourceRouter.getProperties().setProperty("DatasourceNames", "database1 database2 database3");
        resourceRouter.getProperties().setProperty("DefaultDataSourceName", "database1");
        assembler.createResource(config.configureService(resourceRouter, ResourceInfo.class));

        Resource resourceRoutedDs = new Resource("Routed Datasource", "org.apache.openejb.resource.jdbc.Router", "org.apache.openejb.dynamicdatasource:RoutedDataSource");
        resourceRoutedDs.getProperties().setProperty("Router", "My Router");
        assembler.createResource(config.configureService(resourceRoutedDs, ResourceInfo.class));

        // containers
        StatelessSessionContainerInfo statelessContainerInfo = config.configureService(StatelessSessionContainerInfo.class);
        assembler.createContainer(statelessContainerInfo);

        EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean(RoutedEJBBean.class));

        EjbModule ejbModule = new EjbModule(ejbJar);

        // Create an "ear"
        AppModule appModule = new AppModule(ejbModule.getClassLoader(), ejbModule.getJarLocation());
        appModule.getEjbModules().add(ejbModule);

        // Create a persistence-units
        PersistenceUnit unit = new PersistenceUnit("router");
        unit.addClass(Person.class);
        unit.setTransactionType(TransactionType.JTA);
        unit.setJtaDataSource("Routed Datasource");
        appModule.getPersistenceModules().add(new PersistenceModule("root", new Persistence(unit)));

        assembler.createApplication(config.configureApplication(appModule));

        // context
        Context ctx = new InitialContext();

        // creating database
        // openjpa creates it when the entity manager (em) is invoked the first
        // time but we need to create tables before the first use of the em
        // so it is done using jdbc
        Class.forName(jdbcDriver.class.getName());
        for (int i = 1; i <= 3; i++) {
            Connection connection = DriverManager.getConnection(
                    "jdbc:hsqldb:mem:db" + i, "sa", "");
            Statement st = connection.createStatement();
            st.executeUpdate("CREATE TABLE \"DynamicDataSourceTest$Person\" (id BIGINT NOT NULL, name VARCHAR(255), PRIMARY KEY (id))");
            st.close();
            connection.close();
        }

        // running persist on all "routed" databases
        final List<String> databases = new ArrayList<String>();
        databases.add("database1");
        databases.add("database2");
        databases.add("database3");

        RoutedEJB ejb = (RoutedEJB) ctx.lookup("RoutedEJBBeanLocal");
        for (int i = 0; i < 18; i++) {
            String name = "record " + i;
            String db = databases.get(i % 3);
            ejb.persist(i, name, db);
        }

        // assert database record number using jdbc
        for (int i = 1; i <= 3; i++) {
            Connection connection = DriverManager.getConnection(
                    "jdbc:hsqldb:mem:db" + i, "sa", "");
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("select count(*) from \"DynamicDataSourceTest$Person\"");
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

        @javax.annotation.Resource(name = "My Router", type = DeterminedRouter.class)
        private DeterminedRouter router;

        public void persist(int id, String name, String ds) {
            router.setDatasource(ds);
            em.persist(new Person(id, name));
        }
    }

    public static interface RoutedEJB {
        void persist(int id, String name, String ds);
    }

    @Entity
    public static class Person {
        @Id
        private long id;
        private String name;

        public Person() {
            // no-op
        }

        public Person(int i, String n) {
            id = i;
            name = n;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class DeterminedRouter implements Router {
        private String dataSourceNames;
        private String defaultDataSourceName;
        private Map<String, DataSource> dataSources = null;
        private ThreadLocal<DataSource> currentDataSource = new ThreadLocal<DataSource>();

        /**
         * @param datasourceList datasource resource name, separator is a space
         */
        public void setDataSourceNames(String datasourceList) {
            dataSourceNames = datasourceList;
        }

        /**
         * lookup datasource in openejb resources
         */
        private void init() {
            dataSources = new ConcurrentHashMap<String, DataSource>();
            for (String ds : dataSourceNames.split(" ")) {
                ContainerSystem containerSystem = SystemInstance.get()
                        .getComponent(ContainerSystem.class);

                Object o = null;
                Context ctx = containerSystem.getJNDIContext();
                try {
                    o = ctx.lookup("openejb:Resource/" + ds);
                    if (o instanceof DataSource) {
                        dataSources.put(ds, (DataSource) o);
                    } else {
                    }
                } catch (NamingException e) {
                }
            }
        }

        /**
         * @return the user selected data source if it is set
         *         or the default one
         *  @throws IllegalArgumentException if the data source is not found
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
         *
         * @param ds data source name
         */
        public void setDatasource(String datasourceName) {
            if (dataSources == null) {
                init();
            }
            if (!dataSources.containsKey(datasourceName)) {
                throw new IllegalArgumentException("data source called "
                        + datasourceName + " can't be found.");
            }
            DataSource ds = dataSources.get(datasourceName);
            currentDataSource.set(ds);
        }

        /**
         * reset the data source
         */
        public void clear() {
            currentDataSource.remove();
        }

        public void setDefaultDataSourceName(String name) {
            this.defaultDataSourceName = name;
        }
    }
}

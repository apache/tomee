# Dynamic Datasource routing

[Download as zip](dynamic-datasource-routing.zip)

The openejb dynamic datasource api aims to allow to use multiple data sources as one from an application point of view.

It can be useful for technical reasons (load balancing for example) or more generally
functionnal reasons (filtering, aggregation, enriching...). However please note you can choose
only one datasource by transaction. It means the goal of this feature is not to switch more than
once of datasource in a transaction. The following code will not work:

    @Stateless
    public class MyEJB {
        @Resource private MyRouter router;
        @PersistenceContext private EntityManager em;

        public void workWithDataSources() {
            router.setDataSource("ds1");
            em.persist(new MyEntity());

            router.setDataSource("ds2"); // same transaction -> this invocation doesn't work
            em.persist(new MyEntity());
        }
    }

In this example the implementation simply use a datasource from its name and needs to be set before using any JPA
operation in the transaction (to keep the logic simple in the example).

# The implementation of the Router

Our router has two configuration parameters:
* a list of jndi names representing datasources to use
* a default datasource to use

## Java part

The interface Router (org.apache.openejb.resource.jdbc.Router) have only one method to get the datasource and is
simply implemented in [org.superbiz.dynamicdatasourcerouting.DeterminedRouter](src/main/java/org/superbiz/dynamicdatasourcerouting/DeterminedRouter.java.html)
class.

It uses a ThreadLocal to manage the currently used datasource. Keep in mind JPA used more than once the getDatasource() method
for one operation. To change the datasource in one transaction is dangerous and should be avoid.

## Configuration part

To be able to use your router as a resource you need to provide a service configuration. It is done in a file
you can find in META-INF/org.router/ and called service-jar.xml
(for your implementation you can of course change the package name).

It contains the following code:

    <ServiceJar>
      <ServiceProvider id="DeterminedRouter" <!-- the name you want to use -->
          service="Resource"
          type="org.apache.openejb.resource.jdbc.Router"
          class-name="org.superbiz.dynamicdatasourcerouting.DeterminedRouter"> <!-- implementation class -->

        # the parameters

        DataSourceNames
        DefaultDataSourceName
      </ServiceProvider>
    </ServiceJar>

# The test
## Configuration

Using the conf/openejb.xml file the following configuration could have been used:

    <!-- Router and datasource -->
    <Resource id="My Router" type="org.apache.openejb.router.test.DynamicDataSourceTest$DeterminedRouter" provider="org.routertest:DeterminedRouter">
        DatasourceNames = database1 database2 database3
        DefaultDataSourceName = database1
    </Resource>
    <Resource id="Routed Datasource" type="org.apache.openejb.resource.jdbc.Router" provider="org.router:RoutedDataSource">
        Router = My Router
    </Resource>

    <!-- real datasources -->
    <Resource id="database1" type="DataSource">
        JdbcDriver = org.hsqldb.jdbcDriver
        JdbcUrl = jdbc:hsqldb:mem:db1
        UserName = sa
        Password
        JtaManaged = true
    </Resource>
    <Resource id="database2" type="DataSource">
        JdbcDriver = org.hsqldb.jdbcDriver
        JdbcUrl = jdbc:hsqldb:mem:db2
        UserName = sa
        Password
        JtaManaged = true
    </Resource>
    <Resource id="database3" type="DataSource">
        JdbcDriver = org.hsqldb.jdbcDriver
        JdbcUrl = jdbc:hsqldb:mem:db3
        UserName = sa
        Password
        JtaManaged = true
    </Resource>


In test mode and using property style configuration the foolowing configuration is used:

        String[] databases = new String[] { "database1", "database2", "database3" };

        Properties properties = new Properties();
        properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());

        // resources
            // datasources
        for (int i = 1; i <= databases.length; i++) {
            String dbName = databases[i - 1];
            properties.setProperty(dbName, "new://Resource?type=DataSource");
            dbName += ".";
            properties.setProperty(dbName + "JdbcDriver", "org.hsqldb.jdbcDriver");
            properties.setProperty(dbName + "JdbcDriver", "org.hsqldb.jdbcDriver");
            properties.setProperty(dbName + "JdbcUrl", "jdbc:hsqldb:mem:db" + i);
            properties.setProperty(dbName + "UserName", "sa");
            properties.setProperty(dbName + "Password", "");
            properties.setProperty(dbName + "JtaManaged", "true");
        }

            // router
        properties.setProperty("My Router", "new://Resource?provider=org.router:DeterminedRouter&type=" + DeterminedRouter.class.getName());
        properties.setProperty("My Router.DatasourceNames", "database1 database2 database3");
        properties.setProperty("My Router.DefaultDataSourceName", "database1");

            // routed datasource
        properties.setProperty("Routed Datasource", "new://Resource?provider=RoutedDataSource&type=" + Router.class.getName());
        properties.setProperty("Routed Datasource.Router", "My Router");

It is absolutely the same for the application.

## Some hack for OpenJPA

Using more than one datasource behind one EntityManager means the databases are already created. If it is not the case,
the JPA provider has to create the datasource at boot time.

Hibernate do it so if you declare your databases it will work. However with OpenJPA
(the default JPA provider for OpenEJB), the creation is lazy and it happens only once so when you'll switch of database
it will no more work.

Of course OpenEJB provides @Singleton and @Startup features of Java EE 6 and we can do a bean just making a simple find,
even on none existing entities, just to force the database creation:

    @Startup
    @Singleton
    public class BoostrapUtility {
        // inject all real databases

        @PersistenceContext(unitName = "db1")
        private EntityManager em1;

        @PersistenceContext(unitName = "db2")
        private EntityManager em2;

        @PersistenceContext(unitName = "db3")
        private EntityManager em3;

        // force database creation

        @PostConstruct
        @TransactionAttribute(TransactionAttributeType.SUPPORTS)
        public void initDatabase() {
            em1.find(Person.class, 0);
            em2.find(Person.class, 0);
            em3.find(Person.class, 0);
        }
    }

## Using the routed datasource

Now you configured the way you want to route your JPA operation, you registered the resources and you initialized
your databases you can use it and see how it is simple:

    @Stateless
    public class RoutedPersister {
        // injection of the "proxied" datasource
        @PersistenceContext(unitName = "router")
        private EntityManager em;

        // injection of the router you need it to configured the database
        @Resource(name = "My Router", type = DeterminedRouter.class)
        private DeterminedRouter router;

        public void persist(int id, String name, String ds) {
            router.setDataSource(ds); // configuring the database for the current transaction
            em.persist(new Person(id, name)); // will use ds database automatically
        }
    }

index-group=Other Features
type=page
status=published
title=Troubleshooting
~~~~~~

*Help us document this example! Click the blue pencil icon in the upper right to edit this page.*

## Movie

    package org.superbiz.troubleshooting;
    
    import javax.persistence.Entity;
    import javax.persistence.GeneratedValue;
    import javax.persistence.GenerationType;
    import javax.persistence.Id;
    
    @Entity(name = "Movie")
    public class Movie {
    
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private long id;
    
        private String director;
        private String title;
        private int year;
    
        public Movie() {
        }
    
        public Movie(String director, String title, int year) {
            this.director = director;
            this.title = title;
            this.year = year;
        }
    
        public String getDirector() {
            return director;
        }
    
        public void setDirector(String director) {
            this.director = director;
        }
    
        public String getTitle() {
            return title;
        }
    
        public void setTitle(String title) {
            this.title = title;
        }
    
        public int getYear() {
            return year;
        }
    
        public void setYear(int year) {
            this.year = year;
        }
    
    }

## Movies

    package org.superbiz.troubleshooting;
    
    import javax.ejb.Stateless;
    import javax.persistence.EntityManager;
    import javax.persistence.PersistenceContext;
    import javax.persistence.PersistenceContextType;
    import javax.persistence.Query;
    import java.util.List;
    
    //START SNIPPET: code
    @Stateless
    public class Movies {
    
        @PersistenceContext(unitName = "movie-unit", type = PersistenceContextType.TRANSACTION)
        private EntityManager entityManager;
    
        public void addMovie(Movie movie) throws Exception {
            entityManager.persist(movie);
        }
    
        public void deleteMovie(Movie movie) throws Exception {
            entityManager.remove(movie);
        }
    
        public List<Movie> getMovies() throws Exception {
            Query query = entityManager.createQuery("SELECT m from Movie as m");
            return query.getResultList();
        }
    }

## persistence.xml

    <persistence xmlns="http://java.sun.com/xml/ns/persistence" version="1.0">
    
      <persistence-unit name="movie-unit">
        <jta-data-source>movieDatabase</jta-data-source>
        <non-jta-data-source>movieDatabaseUnmanaged</non-jta-data-source>
        <class>org.superbiz.testinjection.MoviesTest.Movie</class>
    
        <properties>
          <property name="openjpa.jdbc.SynchronizeMappings" value="buildSchema(ForeignKeys=true)"/>
        </properties>
      </persistence-unit>
    </persistence>

## MoviesTest

    package org.superbiz.troubleshooting;
    
    import junit.framework.TestCase;
    
    import javax.annotation.Resource;
    import javax.ejb.EJB;
    import javax.ejb.embeddable.EJBContainer;
    import javax.persistence.EntityManager;
    import javax.persistence.PersistenceContext;
    import javax.transaction.UserTransaction;
    import java.util.List;
    import java.util.Properties;
    
    //START SNIPPET: code
    public class MoviesTest extends TestCase {
    
        @EJB
        private Movies movies;
    
        @Resource
        private UserTransaction userTransaction;
    
        @PersistenceContext
        private EntityManager entityManager;
    
        public void setUp() throws Exception {
            Properties p = new Properties();
            p.put("movieDatabase", "new://Resource?type=DataSource");
            p.put("movieDatabase.JdbcDriver", "org.hsqldb.jdbcDriver");
    
            // These two debug levels will get you the basic log information
            // on the deployment of applications. Good first step in troubleshooting.
            p.put("log4j.category.OpenEJB.startup", "debug");
            p.put("log4j.category.OpenEJB.startup.config", "debug");
    
            // This log category is a good way to see what "openejb.foo" options
            // and flags are available and what their default values are
            p.put("log4j.category.OpenEJB.options", "debug");
    
            // This will output the full configuration of all containers
            // resources and other openejb.xml configurable items.  A good
            // way to see what the final configuration looks like after all
            // overriding has been applied.
            p.put("log4j.category.OpenEJB.startup.service", "debug");
    
            // Will output a generated ejb-jar.xml file that represents
            // 100% of the annotations used in the code.  This is a great
            // way to figure out how to do something in xml for overriding
            // or just to "see" all your application meta-data in one place.
            // Look for log lines like this "Dumping Generated ejb-jar.xml to"
            p.put("openejb.descriptors.output", "true");
    
            // Setting the validation output level to verbose results in
            // validation messages that attempt to provide explanations
            // and information on what steps can be taken to remedy failures.
            // A great tool for those learning EJB.
            p.put("openejb.validation.output.level", "verbose");
    
            EJBContainer.createEJBContainer(p).getContext().bind("inject", this);
        }
    
        public void test() throws Exception {
    
            userTransaction.begin();
    
            try {
                entityManager.persist(new Movie("Quentin Tarantino", "Reservoir Dogs", 1992));
                entityManager.persist(new Movie("Joel Coen", "Fargo", 1996));
                entityManager.persist(new Movie("Joel Coen", "The Big Lebowski", 1998));
    
                List<Movie> list = movies.getMovies();
                assertEquals("List.size()", 3, list.size());
            } finally {
                userTransaction.commit();
            }
    
            // Transaction was committed
            List<Movie> list = movies.getMovies();
            assertEquals("List.size()", 3, list.size());
        }
    }

# Running

    
    -------------------------------------------------------
     T E S T S
    -------------------------------------------------------
    Running org.superbiz.troubleshooting.MoviesTest
    2011-10-29 11:50:19,482 - DEBUG - Using default 'openejb.nobanner=true'
    Apache OpenEJB 4.0.0-beta-1    build: 20111002-04:06
    http://tomee.apache.org/
    2011-10-29 11:50:19,482 - INFO  - openejb.home = /Users/dblevins/examples/troubleshooting
    2011-10-29 11:50:19,482 - INFO  - openejb.base = /Users/dblevins/examples/troubleshooting
    2011-10-29 11:50:19,483 - DEBUG - Using default 'openejb.assembler=org.apache.openejb.assembler.classic.Assembler'
    2011-10-29 11:50:19,483 - DEBUG - Instantiating assembler class org.apache.openejb.assembler.classic.Assembler
    2011-10-29 11:50:19,517 - DEBUG - Using default 'openejb.jndiname.failoncollision=true'
    2011-10-29 11:50:19,517 - INFO  - Using 'javax.ejb.embeddable.EJBContainer=true'
    2011-10-29 11:50:19,520 - DEBUG - Using default 'openejb.configurator=org.apache.openejb.config.ConfigurationFactory'
    2011-10-29 11:50:19,588 - DEBUG - Using default 'openejb.validation.skip=false'
    2011-10-29 11:50:19,589 - DEBUG - Using default 'openejb.deploymentId.format={ejbName}'
    2011-10-29 11:50:19,589 - DEBUG - Using default 'openejb.debuggable-vm-hackery=false'
    2011-10-29 11:50:19,589 - DEBUG - Using default 'openejb.webservices.enabled=true'
    2011-10-29 11:50:19,594 - DEBUG - Using default 'openejb.vendor.config=ALL'  Possible values are: geronimo, glassfish, jboss, weblogic or NONE or ALL
    2011-10-29 11:50:19,612 - DEBUG - Using default 'openejb.provider.default=org.apache.openejb.embedded'
    2011-10-29 11:50:19,658 - INFO  - Configuring Service(id=Default Security Service, type=SecurityService, provider-id=Default Security Service)
    2011-10-29 11:50:19,662 - INFO  - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
    2011-10-29 11:50:19,665 - INFO  - Configuring Service(id=movieDatabase, type=Resource, provider-id=Default JDBC Database)
    2011-10-29 11:50:19,665 - DEBUG - Override [JdbcDriver=org.hsqldb.jdbcDriver]
    2011-10-29 11:50:19,666 - DEBUG - Using default 'openejb.deployments.classpath=false'
    2011-10-29 11:50:19,666 - INFO  - Creating TransactionManager(id=Default Transaction Manager)
    2011-10-29 11:50:19,676 - DEBUG - defaultTransactionTimeoutSeconds=600
    2011-10-29 11:50:19,676 - DEBUG - TxRecovery=false
    2011-10-29 11:50:19,676 - DEBUG - bufferSizeKb=32
    2011-10-29 11:50:19,676 - DEBUG - checksumEnabled=true
    2011-10-29 11:50:19,676 - DEBUG - adler32Checksum=true
    2011-10-29 11:50:19,676 - DEBUG - flushSleepTimeMilliseconds=50
    2011-10-29 11:50:19,676 - DEBUG - logFileDir=txlog
    2011-10-29 11:50:19,676 - DEBUG - logFileExt=log
    2011-10-29 11:50:19,676 - DEBUG - logFileName=howl
    2011-10-29 11:50:19,676 - DEBUG - maxBlocksPerFile=-1
    2011-10-29 11:50:19,677 - DEBUG - maxBuffers=0
    2011-10-29 11:50:19,677 - DEBUG - maxLogFiles=2
    2011-10-29 11:50:19,677 - DEBUG - minBuffers=4
    2011-10-29 11:50:19,677 - DEBUG - threadsWaitingForceThreshold=-1
    2011-10-29 11:50:19,724 - DEBUG - createService.success
    2011-10-29 11:50:19,724 - INFO  - Creating SecurityService(id=Default Security Service)
    2011-10-29 11:50:19,724 - DEBUG - DefaultUser=guest
    2011-10-29 11:50:19,750 - DEBUG - createService.success
    2011-10-29 11:50:19,750 - INFO  - Creating Resource(id=movieDatabase)
    2011-10-29 11:50:19,750 - DEBUG - Definition=
    2011-10-29 11:50:19,750 - DEBUG - JtaManaged=true
    2011-10-29 11:50:19,750 - DEBUG - JdbcDriver=org.hsqldb.jdbcDriver
    2011-10-29 11:50:19,750 - DEBUG - JdbcUrl=jdbc:hsqldb:mem:hsqldb
    2011-10-29 11:50:19,750 - DEBUG - UserName=sa
    2011-10-29 11:50:19,750 - DEBUG - Password=
    2011-10-29 11:50:19,750 - DEBUG - PasswordCipher=PlainText
    2011-10-29 11:50:19,750 - DEBUG - ConnectionProperties=
    2011-10-29 11:50:19,750 - DEBUG - DefaultAutoCommit=true
    2011-10-29 11:50:19,750 - DEBUG - InitialSize=0
    2011-10-29 11:50:19,750 - DEBUG - MaxActive=20
    2011-10-29 11:50:19,750 - DEBUG - MaxIdle=20
    2011-10-29 11:50:19,751 - DEBUG - MinIdle=0
    2011-10-29 11:50:19,751 - DEBUG - MaxWait=-1
    2011-10-29 11:50:19,751 - DEBUG - TestOnBorrow=true
    2011-10-29 11:50:19,751 - DEBUG - TestOnReturn=false
    2011-10-29 11:50:19,751 - DEBUG - TestWhileIdle=false
    2011-10-29 11:50:19,751 - DEBUG - TimeBetweenEvictionRunsMillis=-1
    2011-10-29 11:50:19,751 - DEBUG - NumTestsPerEvictionRun=3
    2011-10-29 11:50:19,751 - DEBUG - MinEvictableIdleTimeMillis=1800000
    2011-10-29 11:50:19,751 - DEBUG - PoolPreparedStatements=false
    2011-10-29 11:50:19,751 - DEBUG - MaxOpenPreparedStatements=0
    2011-10-29 11:50:19,751 - DEBUG - AccessToUnderlyingConnectionAllowed=false
    2011-10-29 11:50:19,781 - DEBUG - createService.success
    2011-10-29 11:50:19,783 - DEBUG - Containers        : 0
    2011-10-29 11:50:19,785 - DEBUG - Deployments       : 0
    2011-10-29 11:50:19,785 - DEBUG - SecurityService   : org.apache.openejb.core.security.SecurityServiceImpl
    2011-10-29 11:50:19,786 - DEBUG - TransactionManager: org.apache.geronimo.transaction.manager.GeronimoTransactionManager
    2011-10-29 11:50:19,786 - DEBUG - OpenEJB Container System ready.
    2011-10-29 11:50:19,786 - DEBUG - Using default 'openejb.validation.skip=false'
    2011-10-29 11:50:19,786 - DEBUG - Using default 'openejb.deploymentId.format={ejbName}'
    2011-10-29 11:50:19,786 - DEBUG - Using default 'openejb.debuggable-vm-hackery=false'
    2011-10-29 11:50:19,786 - DEBUG - Using default 'openejb.webservices.enabled=true'
    2011-10-29 11:50:19,786 - DEBUG - Using default 'openejb.vendor.config=ALL'  Possible values are: geronimo, glassfish, jboss, weblogic or NONE or ALL
    2011-10-29 11:50:19,789 - DEBUG - Using default 'openejb.deployments.classpath.include=.*'
    2011-10-29 11:50:19,789 - DEBUG - Using default 'openejb.deployments.classpath.exclude='
    2011-10-29 11:50:19,789 - DEBUG - Using default 'openejb.deployments.classpath.require.descriptor=client'  Possible values are: ejb, client or NONE or ALL
    2011-10-29 11:50:19,789 - DEBUG - Using default 'openejb.deployments.classpath.filter.descriptors=false'
    2011-10-29 11:50:19,789 - DEBUG - Using default 'openejb.deployments.classpath.filter.systemapps=true'
    2011-10-29 11:50:19,828 - DEBUG - Inspecting classpath for applications: 5 urls.
    2011-10-29 11:50:19,846 - INFO  - Found EjbModule in classpath: /Users/dblevins/examples/troubleshooting/target/classes
    2011-10-29 11:50:20,011 - DEBUG - URLs after filtering: 55
    2011-10-29 11:50:20,011 - DEBUG - Annotations path: file:/Users/dblevins/examples/troubleshooting/target/classes/
    2011-10-29 11:50:20,011 - DEBUG - Annotations path: jar:file:/Users/dblevins/.m2/repository/org/apache/maven/surefire/surefire-api/2.7.2/surefire-api-2.7.2.jar!/
    2011-10-29 11:50:20,011 - DEBUG - Annotations path: jar:file:/Users/dblevins/.m2/repository/org/apache/openejb/mbean-annotation-api/4.0.0-beta-1/mbean-annotation-api-4.0.0-beta-1.jar!/
    2011-10-29 11:50:20,011 - DEBUG - Annotations path: jar:file:/Users/dblevins/.m2/repository/org/apache/maven/surefire/surefire-booter/2.7.2/surefire-booter-2.7.2.jar!/
    2011-10-29 11:50:20,011 - DEBUG - Annotations path: file:/Users/dblevins/examples/troubleshooting/target/test-classes/
    2011-10-29 11:50:20,011 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/apache/geronimo/specs/geronimo-jms_1.1_spec/1.1.1/geronimo-jms_1.1_spec-1.1.1.jar!/
    2011-10-29 11:50:20,011 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/apache/bval/bval-core/0.3-incubating/bval-core-0.3-incubating.jar!/
    2011-10-29 11:50:20,011 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/apache/geronimo/specs/geronimo-j2ee-management_1.1_spec/1.0.1/geronimo-j2ee-management_1.1_spec-1.0.1.jar!/
    2011-10-29 11:50:20,011 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/apache/activemq/activemq-core/5.4.2/activemq-core-5.4.2.jar!/
    2011-10-29 11:50:20,012 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/apache/xbean/xbean-bundleutils/3.8/xbean-bundleutils-3.8.jar!/
    2011-10-29 11:50:20,012 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/junit/junit/4.8.1/junit-4.8.1.jar!/
    2011-10-29 11:50:20,012 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/net/sf/scannotation/scannotation/1.0.2/scannotation-1.0.2.jar!/
    2011-10-29 11:50:20,012 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/apache/openejb/javaee-api/6.0-2/javaee-api-6.0-2.jar!/
    2011-10-29 11:50:20,012 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/commons-beanutils/commons-beanutils-core/1.8.3/commons-beanutils-core-1.8.3.jar!/
    2011-10-29 11:50:20,012 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/avalon-framework/avalon-framework/4.1.3/avalon-framework-4.1.3.jar!/
    2011-10-29 11:50:20,012 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/apache/openwebbeans/openwebbeans-web/1.1.1/openwebbeans-web-1.1.1.jar!/
    2011-10-29 11:50:20,012 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/wsdl4j/wsdl4j/1.6.2/wsdl4j-1.6.2.jar!/
    2011-10-29 11:50:20,012 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/logkit/logkit/1.0.1/logkit-1.0.1.jar!/
    2011-10-29 11:50:20,012 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/com/ibm/icu/icu4j/4.0.1/icu4j-4.0.1.jar!/
    2011-10-29 11:50:20,012 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/apache/xbean/xbean-asm-shaded/3.8/xbean-asm-shaded-3.8.jar!/
    2011-10-29 11:50:20,012 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/apache/openwebbeans/openwebbeans-ee-common/1.1.1/openwebbeans-ee-common-1.1.1.jar!/
    2011-10-29 11:50:20,012 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/commons-pool/commons-pool/1.5.6/commons-pool-1.5.6.jar!/
    2011-10-29 11:50:20,012 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/commons-collections/commons-collections/3.2.1/commons-collections-3.2.1.jar!/
    2011-10-29 11:50:20,013 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/commons-logging/commons-logging-api/1.1/commons-logging-api-1.1.jar!/
    2011-10-29 11:50:20,013 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/apache/openwebbeans/openwebbeans-impl/1.1.1/openwebbeans-impl-1.1.1.jar!/
    2011-10-29 11:50:20,013 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/apache/xbean/xbean-finder-shaded/3.8/xbean-finder-shaded-3.8.jar!/
    2011-10-29 11:50:20,013 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/apache/geronimo/specs/geronimo-j2ee-connector_1.6_spec/1.0/geronimo-j2ee-connector_1.6_spec-1.0.jar!/
    2011-10-29 11:50:20,013 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/commons-cli/commons-cli/1.2/commons-cli-1.2.jar!/
    2011-10-29 11:50:20,013 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/apache/activemq/kahadb/5.4.2/kahadb-5.4.2.jar!/
    2011-10-29 11:50:20,013 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/hsqldb/hsqldb/1.8.0.10/hsqldb-1.8.0.10.jar!/
    2011-10-29 11:50:20,013 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/log4j/log4j/1.2.16/log4j-1.2.16.jar!/
    2011-10-29 11:50:20,013 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/apache/geronimo/components/geronimo-connector/3.1.1/geronimo-connector-3.1.1.jar!/
    2011-10-29 11:50:20,013 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/apache/activemq/activemq-ra/5.4.2/activemq-ra-5.4.2.jar!/
    2011-10-29 11:50:20,013 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/net/sourceforge/serp/serp/1.13.1/serp-1.13.1.jar!/
    2011-10-29 11:50:20,013 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/slf4j/slf4j-log4j12/1.6.1/slf4j-log4j12-1.6.1.jar!/
    2011-10-29 11:50:20,013 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/javax/servlet/servlet-api/2.3/servlet-api-2.3.jar!/
    2011-10-29 11:50:20,013 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/apache/activemq/activeio-core/3.1.2/activeio-core-3.1.2.jar!/
    2011-10-29 11:50:20,014 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/quartz-scheduler/quartz/1.8.5/quartz-1.8.5.jar!/
    2011-10-29 11:50:20,014 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/apache/openwebbeans/openwebbeans-ee/1.1.1/openwebbeans-ee-1.1.1.jar!/
    2011-10-29 11:50:20,014 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/slf4j/slf4j-api/1.6.1/slf4j-api-1.6.1.jar!/
    2011-10-29 11:50:20,014 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/apache/openwebbeans/openwebbeans-spi/1.1.1/openwebbeans-spi-1.1.1.jar!/
    2011-10-29 11:50:20,016 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/codehaus/swizzle/swizzle-stream/1.0.2/swizzle-stream-1.0.2.jar!/
    2011-10-29 11:50:20,016 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/apache/openjpa/openjpa/2.1.1/openjpa-2.1.1.jar!/
    2011-10-29 11:50:20,016 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/apache/xbean/xbean-naming/3.8/xbean-naming-3.8.jar!/
    2011-10-29 11:50:20,016 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/apache/geronimo/components/geronimo-transaction/3.1.1/geronimo-transaction-3.1.1.jar!/
    2011-10-29 11:50:20,016 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/commons-lang/commons-lang/2.6/commons-lang-2.6.jar!/
    2011-10-29 11:50:20,016 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/javassist/javassist/3.12.0.GA/javassist-3.12.0.GA.jar!/
    2011-10-29 11:50:20,016 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/objectweb/howl/howl/1.0.1-1/howl-1.0.1-1.jar!/
    2011-10-29 11:50:20,016 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/apache/xbean/xbean-reflect/3.8/xbean-reflect-3.8.jar!/
    2011-10-29 11:50:20,016 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/apache/openwebbeans/openwebbeans-ejb/1.1.1/openwebbeans-ejb-1.1.1.jar!/
    2011-10-29 11:50:20,016 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/commons-logging/commons-logging/1.1/commons-logging-1.1.jar!/
    2011-10-29 11:50:20,016 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/commons-net/commons-net/2.0/commons-net-2.0.jar!/
    2011-10-29 11:50:20,017 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/apache/activemq/protobuf/activemq-protobuf/1.1/activemq-protobuf-1.1.jar!/
    2011-10-29 11:50:20,017 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/commons-dbcp/commons-dbcp/1.4/commons-dbcp-1.4.jar!/
    2011-10-29 11:50:20,017 - DEBUG - Descriptors path: jar:file:/Users/dblevins/.m2/repository/org/apache/geronimo/javamail/geronimo-javamail_1.4_mail/1.8.2/geronimo-javamail_1.4_mail-1.8.2.jar!/
    2011-10-29 11:50:20,017 - DEBUG - Searched 5 classpath urls in 80 milliseconds.  Average 16 milliseconds per url.
    2011-10-29 11:50:20,023 - INFO  - Beginning load: /Users/dblevins/examples/troubleshooting/target/classes
    2011-10-29 11:50:20,028 - DEBUG - Using default 'openejb.tempclassloader.skip=none'  Possible values are: none, annotations, enums or NONE or ALL
    2011-10-29 11:50:20,030 - DEBUG - Using default 'openejb.tempclassloader.skip=none'  Possible values are: none, annotations, enums or NONE or ALL
    2011-10-29 11:50:20,099 - INFO  - Configuring enterprise application: /Users/dblevins/examples/troubleshooting
    2011-10-29 11:50:20,099 - DEBUG - No ejb-jar.xml found assuming annotated beans present: /Users/dblevins/examples/troubleshooting, module: troubleshooting
    2011-10-29 11:50:20,213 - DEBUG - Searching for annotated application exceptions (see OPENEJB-980)
    2011-10-29 11:50:20,214 - DEBUG - Searching for annotated application exceptions (see OPENEJB-980)
    2011-10-29 11:50:20,248 - WARN  - Method 'lookup' is not available for 'javax.annotation.Resource'. Probably using an older Runtime.
    2011-10-29 11:50:20,249 - DEBUG - looking for annotated MBeans in 
    2011-10-29 11:50:20,249 - DEBUG - registered 0 annotated MBeans in 
    2011-10-29 11:50:20,278 - INFO  - Configuring Service(id=Default Stateless Container, type=Container, provider-id=Default Stateless Container)
    2011-10-29 11:50:20,278 - INFO  - Auto-creating a container for bean Movies: Container(type=STATELESS, id=Default Stateless Container)
    2011-10-29 11:50:20,278 - INFO  - Creating Container(id=Default Stateless Container)
    2011-10-29 11:50:20,279 - DEBUG - AccessTimeout=30 seconds
    2011-10-29 11:50:20,279 - DEBUG - MaxSize=10
    2011-10-29 11:50:20,279 - DEBUG - MinSize=0
    2011-10-29 11:50:20,279 - DEBUG - StrictPooling=true
    2011-10-29 11:50:20,279 - DEBUG - MaxAge=0 hours
    2011-10-29 11:50:20,279 - DEBUG - ReplaceAged=true
    2011-10-29 11:50:20,279 - DEBUG - ReplaceFlushed=false
    2011-10-29 11:50:20,279 - DEBUG - MaxAgeOffset=-1
    2011-10-29 11:50:20,279 - DEBUG - IdleTimeout=0 minutes
    2011-10-29 11:50:20,279 - DEBUG - GarbageCollection=false
    2011-10-29 11:50:20,279 - DEBUG - SweepInterval=5 minutes
    2011-10-29 11:50:20,279 - DEBUG - CallbackThreads=5
    2011-10-29 11:50:20,279 - DEBUG - CloseTimeout=5 minutes
    2011-10-29 11:50:20,295 - DEBUG - createService.success
    2011-10-29 11:50:20,296 - INFO  - Configuring Service(id=Default Managed Container, type=Container, provider-id=Default Managed Container)
    2011-10-29 11:50:20,296 - INFO  - Auto-creating a container for bean org.superbiz.troubleshooting.MoviesTest: Container(type=MANAGED, id=Default Managed Container)
    2011-10-29 11:50:20,296 - INFO  - Creating Container(id=Default Managed Container)
    2011-10-29 11:50:20,310 - DEBUG - createService.success
    2011-10-29 11:50:20,310 - INFO  - Configuring PersistenceUnit(name=movie-unit)
    2011-10-29 11:50:20,310 - DEBUG - raw <jta-data-source>movieDatabase</jta-datasource>
    2011-10-29 11:50:20,310 - DEBUG - raw <non-jta-data-source>movieDatabaseUnmanaged</non-jta-datasource>
    2011-10-29 11:50:20,310 - DEBUG - normalized <jta-data-source>movieDatabase</jta-datasource>
    2011-10-29 11:50:20,310 - DEBUG - normalized <non-jta-data-source>movieDatabaseUnmanaged</non-jta-datasource>
    2011-10-29 11:50:20,310 - DEBUG - Available DataSources
    2011-10-29 11:50:20,310 - DEBUG - DataSource(name=movieDatabase, JtaManaged=true)
    2011-10-29 11:50:20,311 - INFO  - Auto-creating a Resource with id 'movieDatabaseNonJta' of type 'DataSource for 'movie-unit'.
    2011-10-29 11:50:20,311 - INFO  - Configuring Service(id=movieDatabaseNonJta, type=Resource, provider-id=movieDatabase)
    2011-10-29 11:50:20,311 - INFO  - Creating Resource(id=movieDatabaseNonJta)
    2011-10-29 11:50:20,311 - DEBUG - Definition=
    2011-10-29 11:50:20,312 - DEBUG - JtaManaged=false
    2011-10-29 11:50:20,312 - DEBUG - JdbcDriver=org.hsqldb.jdbcDriver
    2011-10-29 11:50:20,312 - DEBUG - JdbcUrl=jdbc:hsqldb:mem:hsqldb
    2011-10-29 11:50:20,312 - DEBUG - UserName=sa
    2011-10-29 11:50:20,312 - DEBUG - Password=
    2011-10-29 11:50:20,312 - DEBUG - PasswordCipher=PlainText
    2011-10-29 11:50:20,312 - DEBUG - ConnectionProperties=
    2011-10-29 11:50:20,312 - DEBUG - DefaultAutoCommit=true
    2011-10-29 11:50:20,312 - DEBUG - InitialSize=0
    2011-10-29 11:50:20,312 - DEBUG - MaxActive=20
    2011-10-29 11:50:20,312 - DEBUG - MaxIdle=20
    2011-10-29 11:50:20,312 - DEBUG - MinIdle=0
    2011-10-29 11:50:20,312 - DEBUG - MaxWait=-1
    2011-10-29 11:50:20,312 - DEBUG - TestOnBorrow=true
    2011-10-29 11:50:20,312 - DEBUG - TestOnReturn=false
    2011-10-29 11:50:20,312 - DEBUG - TestWhileIdle=false
    2011-10-29 11:50:20,312 - DEBUG - TimeBetweenEvictionRunsMillis=-1
    2011-10-29 11:50:20,312 - DEBUG - NumTestsPerEvictionRun=3
    2011-10-29 11:50:20,312 - DEBUG - MinEvictableIdleTimeMillis=1800000
    2011-10-29 11:50:20,312 - DEBUG - PoolPreparedStatements=false
    2011-10-29 11:50:20,312 - DEBUG - MaxOpenPreparedStatements=0
    2011-10-29 11:50:20,312 - DEBUG - AccessToUnderlyingConnectionAllowed=false
    2011-10-29 11:50:20,316 - DEBUG - createService.success
    2011-10-29 11:50:20,316 - INFO  - Adjusting PersistenceUnit movie-unit <non-jta-data-source> to Resource ID 'movieDatabaseNonJta' from 'movieDatabaseUnmanaged'
    2011-10-29 11:50:20,317 - INFO  - Using 'openejb.descriptors.output=true'
    2011-10-29 11:50:20,317 - INFO  - Using 'openejb.descriptors.output=true'
    2011-10-29 11:50:20,642 - INFO  - Dumping Generated ejb-jar.xml to: /var/folders/bd/f9ntqy1m8xj_fs006s6crtjh0000gn/T/ejb-jar-4107959830671443055troubleshooting.xml
    2011-10-29 11:50:20,657 - INFO  - Dumping Generated openejb-jar.xml to: /var/folders/bd/f9ntqy1m8xj_fs006s6crtjh0000gn/T/openejb-jar-5369342778223971127troubleshooting.xml
    2011-10-29 11:50:20,657 - INFO  - Using 'openejb.descriptors.output=true'
    2011-10-29 11:50:20,658 - INFO  - Dumping Generated ejb-jar.xml to: /var/folders/bd/f9ntqy1m8xj_fs006s6crtjh0000gn/T/ejb-jar-5569422837673302173EjbModule837053032.xml
    2011-10-29 11:50:20,659 - INFO  - Dumping Generated openejb-jar.xml to: /var/folders/bd/f9ntqy1m8xj_fs006s6crtjh0000gn/T/openejb-jar-560959152015048895EjbModule837053032.xml
    2011-10-29 11:50:20,665 - DEBUG - Adding persistence-unit movie-unit property openjpa.Log=log4j
    2011-10-29 11:50:20,665 - DEBUG - Adjusting PersistenceUnit(name=movie-unit) property to openjpa.RuntimeUnenhancedClasses=supported
    2011-10-29 11:50:20,674 - INFO  - Using 'openejb.validation.output.level=VERBOSE'
    2011-10-29 11:50:20,674 - INFO  - Enterprise application "/Users/dblevins/examples/troubleshooting" loaded.
    2011-10-29 11:50:20,674 - INFO  - Assembling app: /Users/dblevins/examples/troubleshooting
    2011-10-29 11:50:20,678 - DEBUG - Using default 'openejb.tempclassloader.skip=none'  Possible values are: none, annotations, enums or NONE or ALL
    2011-10-29 11:50:20,757 - DEBUG - Using default 'openejb.tempclassloader.skip=none'  Possible values are: none, annotations, enums or NONE or ALL
    2011-10-29 11:50:21,137 - INFO  - PersistenceUnit(name=movie-unit, provider=org.apache.openjpa.persistence.PersistenceProviderImpl) - provider time 407ms
    2011-10-29 11:50:21,138 - DEBUG - openjpa.jdbc.SynchronizeMappings=buildSchema(ForeignKeys=true)
    2011-10-29 11:50:21,138 - DEBUG - openjpa.Log=log4j
    2011-10-29 11:50:21,138 - DEBUG - openjpa.RuntimeUnenhancedClasses=supported
    2011-10-29 11:50:21,262 - DEBUG - Using default 'openejb.jndiname.strategy.class=org.apache.openejb.assembler.classic.JndiBuilder$TemplatedStrategy'
    2011-10-29 11:50:21,262 - DEBUG - Using default 'openejb.jndiname.format={deploymentId}{interfaceType.annotationName}'
    2011-10-29 11:50:21,267 - DEBUG - Using default 'openejb.localcopy=true'
    2011-10-29 11:50:21,270 - DEBUG - bound ejb at name: openejb/Deployment/Movies/org.superbiz.troubleshooting.Movies!LocalBean, ref: org.apache.openejb.core.ivm.naming.BusinessLocalBeanReference@2569a1c5
    2011-10-29 11:50:21,270 - DEBUG - bound ejb at name: openejb/Deployment/Movies/org.superbiz.troubleshooting.Movies!LocalBeanHome, ref: org.apache.openejb.core.ivm.naming.BusinessLocalBeanReference@2569a1c5
    2011-10-29 11:50:21,272 - INFO  - Jndi(name="java:global/troubleshooting/Movies!org.superbiz.troubleshooting.Movies")
    2011-10-29 11:50:21,272 - INFO  - Jndi(name="java:global/troubleshooting/Movies")
    2011-10-29 11:50:21,277 - DEBUG - Using default 'openejb.jndiname.strategy.class=org.apache.openejb.assembler.classic.JndiBuilder$TemplatedStrategy'
    2011-10-29 11:50:21,277 - DEBUG - Using default 'openejb.jndiname.format={deploymentId}{interfaceType.annotationName}'
    2011-10-29 11:50:21,277 - DEBUG - bound ejb at name: openejb/Deployment/org.superbiz.troubleshooting.MoviesTest/org.superbiz.troubleshooting.MoviesTest!LocalBean, ref: org.apache.openejb.core.ivm.naming.BusinessLocalBeanReference@3f78e13f
    2011-10-29 11:50:21,277 - DEBUG - bound ejb at name: openejb/Deployment/org.superbiz.troubleshooting.MoviesTest/org.superbiz.troubleshooting.MoviesTest!LocalBeanHome, ref: org.apache.openejb.core.ivm.naming.BusinessLocalBeanReference@3f78e13f
    2011-10-29 11:50:21,277 - INFO  - Jndi(name="java:global/EjbModule837053032/org.superbiz.troubleshooting.MoviesTest!org.superbiz.troubleshooting.MoviesTest")
    2011-10-29 11:50:21,277 - INFO  - Jndi(name="java:global/EjbModule837053032/org.superbiz.troubleshooting.MoviesTest")
    2011-10-29 11:50:21,291 - DEBUG - CDI Service not installed: org.apache.webbeans.spi.ConversationService
    2011-10-29 11:50:21,399 - INFO  - Created Ejb(deployment-id=Movies, ejb-name=Movies, container=Default Stateless Container)
    2011-10-29 11:50:21,428 - INFO  - Created Ejb(deployment-id=org.superbiz.troubleshooting.MoviesTest, ejb-name=org.superbiz.troubleshooting.MoviesTest, container=Default Managed Container)
    2011-10-29 11:50:21,463 - INFO  - Started Ejb(deployment-id=Movies, ejb-name=Movies, container=Default Stateless Container)
    2011-10-29 11:50:21,463 - INFO  - Started Ejb(deployment-id=org.superbiz.troubleshooting.MoviesTest, ejb-name=org.superbiz.troubleshooting.MoviesTest, container=Default Managed Container)
    2011-10-29 11:50:21,463 - INFO  - Deployed Application(path=/Users/dblevins/examples/troubleshooting)
    2011-10-29 11:50:21,728 - WARN  - The class "org.superbiz.testinjection.MoviesTest.Movie" listed in the openjpa.MetaDataFactory configuration property could not be loaded by sun.misc.Launcher$AppClassLoader@27a8c4e7; ignoring.
    2011-10-29 11:50:21,834 - WARN  - The class "org.superbiz.testinjection.MoviesTest.Movie" listed in the openjpa.MetaDataFactory configuration property could not be loaded by sun.misc.Launcher$AppClassLoader@27a8c4e7; ignoring.
    2011-10-29 11:50:21,846 - WARN  - The class "org.superbiz.testinjection.MoviesTest.Movie" listed in the openjpa.MetaDataFactory configuration property could not be loaded by sun.misc.Launcher$AppClassLoader@27a8c4e7; ignoring.
    Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.642 sec
    
    Results :
    
    Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
    

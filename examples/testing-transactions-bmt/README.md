index-group=Unrevised
type=page
status=published
title=Testing Transactions BMT
~~~~~~

Shows how to begin, commit and rollback transactions using a UserTransaction via a Stateful Bean.

## Movie

    package org.superbiz.injection.tx;

    import javax.persistence.Entity;
    import javax.persistence.GeneratedValue;
    import javax.persistence.Id;

    @Entity
    public class Movie {

        @Id
        @GeneratedValue
        private Long id;
        private String director;
        private String title;
        private int year;

        public Movie(String director, String title, int year) {
            this.director = director;
            this.title = title;
            this.year = year;
        }

        public Movie() {

        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
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

    package org.superbiz.injection.tx;

    import javax.annotation.Resource;
    import javax.ejb.Stateful;
    import javax.ejb.TransactionManagement;
    import javax.ejb.TransactionManagementType;
    import javax.persistence.EntityManager;
    import javax.persistence.PersistenceContext;
    import javax.persistence.PersistenceContextType;
    import javax.persistence.Query;
    import javax.transaction.UserTransaction;

    @Stateful(name = "Movies")
    @TransactionManagement(TransactionManagementType.BEAN)
    public class Movies {

        @PersistenceContext(unitName = "movie-unit", type = PersistenceContextType.TRANSACTION)
        private EntityManager entityManager;

        @Resource
        private UserTransaction userTransaction;

        public void addMovie(Movie movie) throws Exception {
            try {
                userTransaction.begin();
                entityManager.persist(movie);

                //For some dummy reason, this db can have only 5 titles. :O)
                if (countMovies() > 5) {
                    userTransaction.rollback();
                } else {
                    userTransaction.commit();
                }


            } catch (Exception e) {
                e.printStackTrace();
                userTransaction.rollback();
            }
        }

        public Long countMovies() throws Exception {
            Query query = entityManager.createQuery("SELECT COUNT(m) FROM Movie m");
            return Long.class.cast(query.getSingleResult());
        }
    }


## persistence.xml

    <persistence xmlns="http://java.sun.com/xml/ns/persistence" version="1.0">

      <persistence-unit name="movie-unit">
        <jta-data-source>movieDatabase</jta-data-source>
        <non-jta-data-source>movieDatabaseUnmanaged</non-jta-data-source>
        <class>org.superbiz.injection.tx.Movie</class>

        <properties>
          <property name="openjpa.jdbc.SynchronizeMappings" value="buildSchema(ForeignKeys=true)"/>
        </properties>
      </persistence-unit>
    </persistence>

## MoviesTest

    package org.superbiz.injection.tx;

    import org.junit.Assert;
    import org.junit.Test;

    import javax.ejb.EJB;
    import javax.ejb.embeddable.EJBContainer;
    import java.util.Properties;

    public class MoviesTest {

        @EJB
        private Movies movies;

        @Test
        public void testMe() throws Exception {
            final Properties p = new Properties();
            p.put("movieDatabase", "new://Resource?type=DataSource");
            p.put("movieDatabase.JdbcDriver", "org.hsqldb.jdbcDriver");
            p.put("movieDatabase.JdbcUrl", "jdbc:hsqldb:mem:moviedb");

            EJBContainer.createEJBContainer(p).getContext().bind("inject", this);

            movies.addMovie(new Movie("Asif Kapadia", "Senna", 2010));
            movies.addMovie(new Movie("José Padilha", "Tropa de Elite", 2007));
            movies.addMovie(new Movie("Andy Wachowski/Lana Wachowski", "The Matrix", 1999));
            movies.addMovie(new Movie("Quentin Tarantino", "Reservoir Dogs", 1992));
            movies.addMovie(new Movie("Joel Coen", "Fargo", 1996));
            movies.addMovie(new Movie("Joel Coen", "The Big Lebowski", 1998));

            Assert.assertEquals(5L, movies.countMovies().longValue());
        }

    }


# Running

    -------------------------------------------------------
     T E S T S
    -------------------------------------------------------
    Running org.superbiz.injection.tx.MoviesTest
    INFO - ********************************************************************************
    INFO - OpenEJB http://tomee.apache.org/
    INFO - Startup: Sat Jul 21 16:39:28 EDT 2012
    INFO - Copyright 1999-2012 (C) Apache OpenEJB Project, All Rights Reserved.
    INFO - Version: 4.1.0
    INFO - Build date: 20120721
    INFO - Build time: 12:06
    INFO - ********************************************************************************
    INFO - openejb.home = /home/boto/dev/ws/openejb_trunk/openejb/examples/testing-transactions-bmt
    INFO - openejb.base = /home/boto/dev/ws/openejb_trunk/openejb/examples/testing-transactions-bmt
    INFO - Created new singletonService org.apache.openejb.cdi.ThreadSingletonServiceImpl@3f3f210f
    INFO - Succeeded in installing singleton service
    INFO - Using 'javax.ejb.embeddable.EJBContainer=true'
    INFO - Cannot find the configuration file [conf/openejb.xml].  Will attempt to create one for the beans deployed.
    INFO - Configuring Service(id=Default Security Service, type=SecurityService, provider-id=Default Security Service)
    INFO - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
    INFO - Configuring Service(id=movieDatabase, type=Resource, provider-id=Default JDBC Database)
    INFO - Creating TransactionManager(id=Default Transaction Manager)
    INFO - Creating SecurityService(id=Default Security Service)
    INFO - Creating Resource(id=movieDatabase)
    INFO - Beginning load: /home/boto/dev/ws/openejb_trunk/openejb/examples/testing-transactions-bmt/target/classes
    INFO - Configuring enterprise application: /home/boto/dev/ws/openejb_trunk/openejb/examples/testing-transactions-bmt
    WARNING - Method 'lookup' is not available for 'javax.annotation.Resource'. Probably using an older Runtime.
    INFO - Auto-deploying ejb Movies: EjbDeployment(deployment-id=Movies)
    INFO - Configuring Service(id=Default Stateful Container, type=Container, provider-id=Default Stateful Container)
    INFO - Auto-creating a container for bean Movies: Container(type=STATEFUL, id=Default Stateful Container)
    INFO - Creating Container(id=Default Stateful Container)
    INFO - Using directory /tmp for stateful session passivation
    INFO - Configuring Service(id=Default Managed Container, type=Container, provider-id=Default Managed Container)
    INFO - Auto-creating a container for bean org.superbiz.injection.tx.MoviesTest: Container(type=MANAGED, id=Default Managed Container)
    INFO - Creating Container(id=Default Managed Container)
    INFO - Using directory /tmp for stateful session passivation
    INFO - Configuring PersistenceUnit(name=movie-unit)
    INFO - Auto-creating a Resource with id 'movieDatabaseNonJta' of type 'DataSource for 'movie-unit'.
    INFO - Configuring Service(id=movieDatabaseNonJta, type=Resource, provider-id=movieDatabase)
    INFO - Creating Resource(id=movieDatabaseNonJta)
    INFO - Adjusting PersistenceUnit movie-unit <non-jta-data-source> to Resource ID 'movieDatabaseNonJta' from 'movieDatabaseUnmanaged'
    INFO - Enterprise application "/home/boto/dev/ws/openejb_trunk/openejb/examples/testing-transactions-bmt" loaded.
    INFO - Assembling app: /home/boto/dev/ws/openejb_trunk/openejb/examples/testing-transactions-bmt
    SEVERE - JAVA AGENT NOT INSTALLED. The JPA Persistence Provider requested installation of a ClassFileTransformer which requires a JavaAgent.  See http://tomee.apache.org/3.0/javaagent.html
    INFO - PersistenceUnit(name=movie-unit, provider=org.apache.openjpa.persistence.PersistenceProviderImpl) - provider time 399ms
    INFO - Jndi(name="java:global/testing-transactions-bmt/Movies!org.superbiz.injection.tx.Movies")
    INFO - Jndi(name="java:global/testing-transactions-bmt/Movies")
    INFO - Existing thread singleton service in SystemInstance() org.apache.openejb.cdi.ThreadSingletonServiceImpl@3f3f210f
    INFO - OpenWebBeans Container is starting...
    INFO - Adding OpenWebBeansPlugin : [CdiPlugin]
    INFO - All injection points are validated successfully.
    INFO - OpenWebBeans Container has started, it took 157 ms.
    INFO - Created Ejb(deployment-id=Movies, ejb-name=Movies, container=Default Stateful Container)
    INFO - Started Ejb(deployment-id=Movies, ejb-name=Movies, container=Default Stateful Container)
    INFO - Deployed Application(path=/home/boto/dev/ws/openejb_trunk/openejb/examples/testing-transactions-bmt)
    INFO - Started user transaction org.apache.geronimo.transaction.manager.TransactionImpl@709a1411
    21-Jul-2012 4:39:32 PM null openjpa.Runtime
    INFO: Starting OpenJPA 2.2.0
    21-Jul-2012 4:39:32 PM null openjpa.jdbc.JDBC
    INFO: Using dictionary class "org.apache.openjpa.jdbc.sql.HSQLDictionary" (HSQL Database Engine 2.2.8 ,HSQL Database Engine Driver 2.2.8).
    21-Jul-2012 4:39:33 PM null openjpa.Enhance
    INFO: Creating subclass and redefining methods for "[class org.superbiz.injection.tx.Movie]". This means that your application will be less efficient than it would if you ran the OpenJPA enhancer.
    INFO - Committing user transaction org.apache.geronimo.transaction.manager.TransactionImpl@709a1411
    INFO - Started user transaction org.apache.geronimo.transaction.manager.TransactionImpl@2bb64b70
    INFO - Committing user transaction org.apache.geronimo.transaction.manager.TransactionImpl@2bb64b70
    INFO - Started user transaction org.apache.geronimo.transaction.manager.TransactionImpl@627b5c
    INFO - Committing user transaction org.apache.geronimo.transaction.manager.TransactionImpl@627b5c
    INFO - Started user transaction org.apache.geronimo.transaction.manager.TransactionImpl@2f031310
    INFO - Committing user transaction org.apache.geronimo.transaction.manager.TransactionImpl@2f031310
    INFO - Started user transaction org.apache.geronimo.transaction.manager.TransactionImpl@4df2a9da
    INFO - Committing user transaction org.apache.geronimo.transaction.manager.TransactionImpl@4df2a9da
    INFO - Started user transaction org.apache.geronimo.transaction.manager.TransactionImpl@3fa9b4a4
    INFO - Rolling back user transaction org.apache.geronimo.transaction.manager.TransactionImpl@3fa9b4a4
    Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 7.471 sec

    Results :

    Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

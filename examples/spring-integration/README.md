Title: Spring Integration

*Help us document this example! Source available in [svn](http://svn.apache.org/repos/asf/openejb/trunk/openejb/examples/spring-integration) or [git](https://github.com/apache/openejb/tree/trunk/openejb/examples/spring-integration). Open a [JIRA](https://issues.apache.org/jira/browse/TOMEE) with patch or pull request*

## AvailableMovies

    package org.superbiz.spring;
    
    import javax.annotation.PostConstruct;
    import javax.ejb.EJB;
    import java.util.List;
    
    /**
     * This is a simple Spring bean that we use as an easy way
     * to seed the example with a list of persistent Movie objects
     * <p/>
     * The individual Movie objects are constructed by Spring, then
     * passed into the Movies EJB where they are transactionally
     * persisted with the EntityManager.
     */
    public class AvailableMovies {
    
        @EJB(name = "MoviesLocal")
        private Movies moviesEjb;
    
        private List<Movie> movies;
    
        @PostConstruct
        public void construct() throws Exception {
            for (Movie movie : movies) {
                moviesEjb.addMovie(movie);
            }
        }
    
        public List<Movie> getMovies() {
            return movies;
        }
    
        public void setMovies(List<Movie> movies) {
            this.movies = movies;
        }
    
        public void setMoviesEjb(Movies moviesEjb) {
            this.moviesEjb = moviesEjb;
        }
    }

## Cineplex

    package org.superbiz.spring;
    
    import java.util.List;
    
    public interface Cineplex {
    
        public List<Theater> getTheaters();
    }

## CineplexImpl

    package org.superbiz.spring;
    
    import javax.annotation.Resource;
    import javax.ejb.Stateless;
    import java.util.List;
    
    @Stateless
    public class CineplexImpl implements Cineplex {
    
        /**
         * The Theaters Spring bean will be injected
         */
        @Resource
        private Theaters theaters;
    
        public List<Theater> getTheaters() {
            return theaters.getTheaters();
        }
    }

## Movie

    package org.superbiz.spring;
    
    import javax.persistence.Entity;
    import javax.persistence.GeneratedValue;
    import javax.persistence.GenerationType;
    import javax.persistence.Id;
    
    @Entity
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

    package org.superbiz.spring;
    
    import java.util.List;
    
    public interface Movies {
        void addMovie(Movie movie) throws Exception;
    
        void deleteMovie(Movie movie) throws Exception;
    
        List<Movie> getMovies() throws Exception;
    
        Movie getMovieByTitle(String title) throws Exception;
    }

## MoviesImpl

    package org.superbiz.spring;
    
    import javax.ejb.Stateful;
    import javax.persistence.EntityManager;
    import javax.persistence.PersistenceContext;
    import javax.persistence.PersistenceContextType;
    import javax.persistence.Query;
    import java.util.List;

    /**
     * A normal Stateful EJB that uses a JPA EntityManager.
     * <p/>
     * We use this bean to transactionally wrap access to the
     * EntityManager persist, remove, and query methods.
     */
    @Stateful(name = "Movies")
    public class MoviesImpl implements Movies {
    
        @PersistenceContext(unitName = "movie-unit", type = PersistenceContextType.EXTENDED)
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
    
        public Movie getMovieByTitle(String title) throws Exception {
            Query query = entityManager.createQuery("SELECT m from Movie as m where m.title = ?1");
            query.setParameter(1, title);
            return (Movie) query.getSingleResult();
        }
    }

## Theater

    package org.superbiz.spring;
    
    import javax.inject.Inject;
    import javax.inject.Named;
    import java.util.ArrayList;
    import java.util.List;

    /**
     * Spring bean that references the Movies EJB and the Movie JPA bean.
     * <p/>
     * This bean shows that Spring beans can have references to EJBs.
     */
    public class Theater {
    
        /**
         * The Movies @Stateless EJB
         */
        private final Movies movies;
    
        private final List<Movie> nowPlaying = new ArrayList<Movie>();
    
        /**
         * The Movies EJB is passed in on the constructor which
         * guarantees we can use it in the setNowPlaying method.
         *
         * @param movies
         */
        @Inject
        @Named(value = "MoviesLocal")
        public Theater(Movies movies) {
            this.movies = movies;
        }
    
        /**
         * For every title in the list we will use the Movies EJB
         * to lookup the actual Movie JPA object.
         *
         * @param nowPlaying
         * @throws Exception
         */
        public void setNowPlaying(List<String> nowPlaying) throws Exception {
            for (String title : nowPlaying) {
                this.nowPlaying.add(movies.getMovieByTitle(title));
            }
        }
    
        public List<Movie> getMovies() throws Exception {
            return nowPlaying;
        }
    }

## Theaters

    package org.superbiz.spring;
    
    import java.util.List;
    
    /**
     * Injection of collections of Spring beans into an EJB
     * is not yet supported, so this Spring bean exists to
     * wrap the collection as an injectable object.
     */
    public class Theaters {
    
        private List<Theater> theaters;
    
        public List<Theater> getTheaters() {
            return theaters;
        }
    
        public void setTheaters(List<Theater> theaters) {
            this.theaters = theaters;
        }
    }

## persistence.xml

    <persistence xmlns="http://java.sun.com/xml/ns/persistence" version="1.0">
    
      <persistence-unit name="movie-unit">
        <provider>org.hibernate.ejb.HibernatePersistence</provider>
        <jta-data-source>MovieDatabase</jta-data-source>
        <non-jta-data-source>MovieDatabaseUnmanaged</non-jta-data-source>
        <class>org.superbiz.spring.Movie</class>
        <properties>
          <property name="hibernate.hbm2ddl.auto" value="create-drop"/>
        </properties>
      </persistence-unit>
    </persistence>


## movies.xml

    <beans xmlns="http://www.springframework.org/schema/beans"
           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xmlns:context="http://www.springframework.org/schema/context"
           xmlns:tx="http://www.springframework.org/schema/tx"
    
           xsi:schemaLocation="http://www.springframework.org/schema/beans
               http://www.springframework.org/schema/beans/spring-beans-2.5.xsd
               http://www.springframework.org/schema/context
               http://www.springframework.org/schema/context/spring-context-2.5.xsd
               http://www.springframework.org/schema/tx http://www.springframework.org/schema/tx/spring-tx-2.0.xsd">
    

      <context:annotation-config/>
    

      <!--
      Required:  Set up a TransactionManager for use by OpenEJB or Spring.
      The Spring PlatformTransactionManager may be used instead.
    
      In future versions this will not be a required step.
      -->
      <bean name="TransactionManager" class="org.apache.geronimo.transaction.manager.GeronimoTransactionManager"/>
    
    
      <!--
      Required:  Set up a SecurityService for use by OpenEJB.
      In future versions this will not be a required step.
      -->
      <bean name="SecurityService" class="org.apache.openejb.core.security.SecurityServiceImpl"/>
    
    
      <!--
      Loads the EJBs from the classpath just as when embedding OpenEJB via
      the org.apache.openejb.client.LocalInitialContextFactory.  All the discovered
      EJBs are imported into this context and available for injection here.
      -->
      <bean name="classPathApplication" class="org.apache.openejb.spring.ClassPathApplication"/>
    
      <bean name="MovieDatabase" class="org.apache.openejb.spring.Resource">
        <property name="type" value="DataSource"/>
        <property name="properties">
          <props>
            <prop key="JdbcDriver">org.hsqldb.jdbcDriver</prop>
            <prop key="JdbcUrl">jdbc:hsqldb:mem:moviedb</prop>
          </props>
        </property>
      </bean>
    
      <bean name="MovieDatabaseUnmanaged" class="org.apache.openejb.spring.Resource">
        <property name="type" value="DataSource"/>
        <property name="properties">
          <props>
            <prop key="JdbcDriver">org.hsqldb.jdbcDriver</prop>
            <prop key="JdbcUrl">jdbc:hsqldb:mem:moviedb</prop>
            <prop key="JtaManaged">false</prop>
          </props>
        </property>
      </bean>
    
      <bean name="AvailableMovies" class="org.superbiz.spring.AvailableMovies">
        <property name="movies">
          <list>
            <bean class="org.superbiz.spring.Movie">
              <property name="title" value="Fargo"/>
              <property name="director" value="Joel Coen"/>
              <property name="year" value="1996"/>
            </bean>
            <bean class="org.superbiz.spring.Movie">
              <property name="title" value="Reservoir Dogs"/>
              <property name="director" value="Quentin Tarantino"/>
              <property name="year" value="1992"/>
            </bean>
            <bean class="org.superbiz.spring.Movie">
              <property name="title" value="The Big Lebowski"/>
              <property name="director" value="Joel Coen"/>
              <property name="year" value="1998"/>
            </bean>
            <bean class="org.superbiz.spring.Movie">
              <property name="title" value="You, Me and Dupree"/>
              <property name="director" value="Anthony Russo"/>
              <property name="year" value="2006"/>
            </bean>
            <bean class="org.superbiz.spring.Movie">
              <property name="title" value="Wedding Crashers"/>
              <property name="director" value="David Dobkin"/>
              <property name="year" value="2005"/>
            </bean>
            <bean class="org.superbiz.spring.Movie">
              <property name="title" value="Zoolander"/>
              <property name="director" value="Ben Stiller"/>
              <property name="year" value="2001"/>
            </bean>
            <bean class="org.superbiz.spring.Movie">
              <property name="title" value="Shanghai Noon"/>
              <property name="director" value="Tom Dey"/>
              <property name="year" value="2000"/>
            </bean>
          </list>
        </property>
        <!--property name="moviesEjb">
          <ref bean="MoviesLocal" />
        </property-->
      </bean>
    
      <bean name="theater1" class="org.superbiz.spring.Theater">
        <property name="nowPlaying">
          <list>
            <value>Fargo</value>
            <value>Reservoir Dogs</value>
            <value>The Big Lebowski</value>
          </list>
        </property>
      </bean>
    
      <bean name="theater2" class="org.superbiz.spring.Theater">
        <property name="nowPlaying">
          <list>
            <value>You, Me and Dupree</value>
            <value>Wedding Crashers</value>
            <value>Zoolander</value>
            <value>Shanghai Noon</value>
          </list>
        </property>
      </bean>
    
      <bean name="theaters" class="org.superbiz.spring.Theaters">
        <property name="theaters">
          <list>
            <ref bean="theater1"/>
            <ref bean="theater2"/>
          </list>
        </property>
      </bean>

    </beans>


## MoviesTest

    package org.superbiz.spring;
    
    import junit.framework.TestCase;
    import org.springframework.context.support.ClassPathXmlApplicationContext;
    
    import java.util.List;
    
    public class MoviesTest extends TestCase {
    
        public void test() throws Exception {
    
            //Uncomment for debug logging
            //org.apache.log4j.BasicConfigurator.configure();
    
            System.setProperty("openejb.deployments.classpath.include", ".*/spring-integration.*");
    
            ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("movies.xml");
    
            // Can I lookup the Cineplex EJB via the Spring ApplicationContext
            Cineplex cineplex = (Cineplex) context.getBean("CineplexImplLocal");
            assertNotNull(cineplex);
    
            // Does the Cineplex EJB have a reference to the Theaters Spring bean?
            List<Theater> theaters = cineplex.getTheaters();
            assertNotNull(theaters);
    
            assertEquals(2, theaters.size());
    
            Theater theaterOne = theaters.get(0);
            Theater theaterTwo = theaters.get(1);
    
    
            // Were the Theater Spring beans able to use the
            // Movies EJB to get references to the Movie JPA objects?
            List<Movie> theaterOneMovies = theaterOne.getMovies();
            assertNotNull(theaterOneMovies);
    
            List<Movie> theaterTwoMovies = theaterTwo.getMovies();
            assertNotNull(theaterTwoMovies);
    
            // The first Theater should have used the Movies EJB
            // to get a reference to three Movie JPA objects
            assertEquals(3, theaterOneMovies.size());
    
            assertEquals("Fargo", theaterOneMovies.get(0).getTitle());
            assertEquals("Reservoir Dogs", theaterOneMovies.get(1).getTitle());
            assertEquals("The Big Lebowski", theaterOneMovies.get(2).getTitle());
    
            // The second Theater should have used the Movies EJB
            // to get a reference to four Movie JPA objects
    
            assertEquals(4, theaterTwoMovies.size());
    
            assertEquals("You, Me and Dupree", theaterTwoMovies.get(0).getTitle());
            assertEquals("Wedding Crashers", theaterTwoMovies.get(1).getTitle());
            assertEquals("Zoolander", theaterTwoMovies.get(2).getTitle());
            assertEquals("Shanghai Noon", theaterTwoMovies.get(3).getTitle());
        }
    }

# Running

    
    -------------------------------------------------------
     T E S T S
    -------------------------------------------------------
    Running org.superbiz.spring.MoviesTest
    log4j:WARN No appenders could be found for logger (org.springframework.context.support.ClassPathXmlApplicationContext).
    log4j:WARN Please initialize the log4j system properly.
    log4j:WARN See http://logging.apache.org/log4j/1.2/faq.html#noconfig for more info.
    Apache OpenEJB 4.0.0-beta-1    build: 20111002-04:06
    http://openejb.apache.org/
    INFO - openejb.home = /Users/dblevins/examples/spring-integration
    INFO - openejb.base = /Users/dblevins/examples/spring-integration
    INFO - Configuring Service(id=Default JDK 1.3 ProxyFactory, type=ProxyFactory, provider-id=Default JDK 1.3 ProxyFactory)
    INFO - Configuring Service(id=MovieDatabase, type=Resource, provider-id=Default JDBC Database)
    INFO - Configuring Service(id=MovieDatabaseUnmanaged, type=Resource, provider-id=Default JDBC Database)
    INFO - Found EjbModule in classpath: /Users/dblevins/examples/spring-integration/target/classes
    INFO - Beginning load: /Users/dblevins/examples/spring-integration/target/classes
    INFO - Configuring enterprise application: /Users/dblevins/examples/spring-integration/classpath.ear
    WARN - Method 'lookup' is not available for 'javax.annotation.Resource'. Probably using an older Runtime.
    INFO - Configuring Service(id=Default Stateless Container, type=Container, provider-id=Default Stateless Container)
    INFO - Auto-creating a container for bean CineplexImpl: Container(type=STATELESS, id=Default Stateless Container)
    INFO - Auto-linking resource-ref 'java:comp/env/org.superbiz.spring.CineplexImpl/theaters' in bean CineplexImpl to Resource(id=theaters)
    INFO - Configuring Service(id=Default Stateful Container, type=Container, provider-id=Default Stateful Container)
    INFO - Auto-creating a container for bean Movies: Container(type=STATEFUL, id=Default Stateful Container)
    INFO - Configuring PersistenceUnit(name=movie-unit, provider=org.hibernate.ejb.HibernatePersistence)
    INFO - Enterprise application "/Users/dblevins/examples/spring-integration/classpath.ear" loaded.
    INFO - Assembling app: /Users/dblevins/examples/spring-integration/classpath.ear
    INFO - PersistenceUnit(name=movie-unit, provider=org.hibernate.ejb.HibernatePersistence) - provider time 648ms
    INFO - Jndi(name=CineplexImplLocal) --> Ejb(deployment-id=CineplexImpl)
    INFO - Jndi(name=global/classpath.ear/spring-integration/CineplexImpl!org.superbiz.spring.Cineplex) --> Ejb(deployment-id=CineplexImpl)
    INFO - Jndi(name=global/classpath.ear/spring-integration/CineplexImpl) --> Ejb(deployment-id=CineplexImpl)
    INFO - Jndi(name=MoviesLocal) --> Ejb(deployment-id=Movies)
    INFO - Jndi(name=global/classpath.ear/spring-integration/Movies!org.superbiz.spring.Movies) --> Ejb(deployment-id=Movies)
    INFO - Jndi(name=global/classpath.ear/spring-integration/Movies) --> Ejb(deployment-id=Movies)
    INFO - Created Ejb(deployment-id=Movies, ejb-name=Movies, container=Default Stateful Container)
    INFO - Created Ejb(deployment-id=CineplexImpl, ejb-name=CineplexImpl, container=Default Stateless Container)
    INFO - Started Ejb(deployment-id=Movies, ejb-name=Movies, container=Default Stateful Container)
    INFO - Started Ejb(deployment-id=CineplexImpl, ejb-name=CineplexImpl, container=Default Stateless Container)
    INFO - Deployed Application(path=/Users/dblevins/examples/spring-integration/classpath.ear)
    INFO - Exported EJB Movies with interface org.superbiz.spring.Movies to Spring bean MoviesLocal
    INFO - Exported EJB Movies with interface org.superbiz.spring.Movies to Spring bean global/classpath.ear/spring-integration/Movies!org.superbiz.spring.Movies
    INFO - Exported EJB CineplexImpl with interface org.superbiz.spring.Cineplex to Spring bean CineplexImplLocal
    INFO - Exported EJB CineplexImpl with interface org.superbiz.spring.Cineplex to Spring bean global/classpath.ear/spring-integration/CineplexImpl!org.superbiz.spring.Cineplex
    Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.407 sec
    
    Results :
    
    Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
    

index-group=Testing Techniques
type=page
status=published
title=EAR Testing
~~~~~~

The goal of this example is to demonstrate how maven projects might be organized in a more real world style and how testing with OpenEJB can fit into that structure.

This example takes the basic moviefun code we us in many of examples and splits it into two modules:

 - `business-logic`
 - `business-model`

As the names imply, we keep our `@Entity` beans in the `business-model` module and our session beans in the `business-logic` model.  The tests located and run from the business logic module.

    ear-testing
    ear-testing/business-logic
    ear-testing/business-logic/pom.xml
    ear-testing/business-logic/src/main/java/org/superbiz/logic/Movies.java
    ear-testing/business-logic/src/main/java/org/superbiz/logic/MoviesImpl.java
    ear-testing/business-logic/src/main/resources
    ear-testing/business-logic/src/main/resources/META-INF
    ear-testing/business-logic/src/main/resources/META-INF/ejb-jar.xml
    ear-testing/business-logic/src/test/java/org/superbiz/logic/MoviesTest.java
    ear-testing/business-model
    ear-testing/business-model/pom.xml
    ear-testing/business-model/src/main/java/org/superbiz/model/Movie.java
    ear-testing/business-model/src/main/resources/META-INF/persistence.xml
    ear-testing/pom.xml

# Project configuration

The parent pom, trimmed to the minimum, looks like so:

    <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">

      <modelVersion>4.0.0</modelVersion>
      <groupId>org.superbiz</groupId>
      <artifactId>myear</artifactId>
      <version>1.1.0-SNAPSHOT</version>

      <packaging>pom</packaging>

      <modules>
        <module>business-model</module>
        <module>business-logic</module>
      </modules>

      <dependencyManagement>
        <dependencies>
          <dependency>
            <groupId>org.apache.openejb</groupId>
            <artifactId>javaee-api</artifactId>
            <version>6.0-2</version>
          </dependency>
          <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.8.1</version>
          </dependency>
        </dependencies>
      </dependencyManagement>
    </project>

The `business-model/pom.xml` as follows:

    <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
      <parent>
        <groupId>org.superbiz</groupId>
        <artifactId>myear</artifactId>
        <version>1.1.0-SNAPSHOT</version>
      </parent>

      <modelVersion>4.0.0</modelVersion>

      <artifactId>business-model</artifactId>
      <packaging>jar</packaging>

      <dependencies>
        <dependency>
          <groupId>org.apache.openejb</groupId>
          <artifactId>javaee-api</artifactId>
          <scope>provided</scope>
        </dependency>
        <dependency>
          <groupId>junit</groupId>
          <artifactId>junit</artifactId>
          <scope>test</scope>
        </dependency>

      </dependencies>

    </project>

And finally, the `business-logic/pom.xml` which is setup to support embedded testing with OpenEJB:

    <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
      <parent>
        <groupId>org.superbiz</groupId>
        <artifactId>myear</artifactId>
        <version>1.1.0-SNAPSHOT</version>
      </parent>

      <modelVersion>4.0.0</modelVersion>

      <artifactId>business-logic</artifactId>
      <packaging>jar</packaging>

      <dependencies>
        <dependency>
          <groupId>org.superbiz</groupId>
          <artifactId>business-model</artifactId>
          <version>${project.version}</version>
        </dependency>
        <dependency>
          <groupId>org.apache.openejb</groupId>
          <artifactId>javaee-api</artifactId>
          <scope>provided</scope>
        </dependency>
        <dependency>
          <groupId>junit</groupId>
          <artifactId>junit</artifactId>
          <scope>test</scope>
        </dependency>
        <!--
        The <scope>test</scope> guarantees that non of your runtime
        code is dependent on any OpenEJB classes.
        -->
        <dependency>
          <groupId>org.apache.openejb</groupId>
          <artifactId>openejb-core</artifactId>
          <version>7.0.0-SNAPSHOT</version>
          <scope>test</scope>
        </dependency>
      </dependencies>
    </project>

# TestCode

The test code is the same as always:

    public class MoviesTest extends TestCase {

        public void test() throws Exception {
            Properties p = new Properties();
            p.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.core.LocalInitialContextFactory");

            p.put("openejb.deployments.classpath.ear", "true");

            p.put("movieDatabase", "new://Resource?type=DataSource");
            p.put("movieDatabase.JdbcDriver", "org.hsqldb.jdbcDriver");
            p.put("movieDatabase.JdbcUrl", "jdbc:hsqldb:mem:moviedb");

            p.put("movieDatabaseUnmanaged", "new://Resource?type=DataSource");
            p.put("movieDatabaseUnmanaged.JdbcDriver", "org.hsqldb.jdbcDriver");
            p.put("movieDatabaseUnmanaged.JdbcUrl", "jdbc:hsqldb:mem:moviedb");
            p.put("movieDatabaseUnmanaged.JtaManaged", "false");

            Context context = new InitialContext(p);

            Movies movies = (Movies) context.lookup("MoviesLocal");

            movies.addMovie(new Movie("Quentin Tarantino", "Reservoir Dogs", 1992));
            movies.addMovie(new Movie("Joel Coen", "Fargo", 1996));
            movies.addMovie(new Movie("Joel Coen", "The Big Lebowski", 1998));

            List<Movie> list = movies.getMovies();
            assertEquals("List.size()", 3, list.size());

            for (Movie movie : list) {
                movies.deleteMovie(movie);
            }

            assertEquals("Movies.getMovies()", 0, movies.getMovies().size());
        }
    }


# Running


    -------------------------------------------------------
     T E S T S
    -------------------------------------------------------
    Running org.superbiz.logic.MoviesTest
    Apache OpenEJB 7.0.0-SNAPSHOT    build: 20111002-04:06
    http://tomee.apache.org/
    INFO - openejb.home = /Users/dblevins/examples/ear-testing/business-logic
    INFO - openejb.base = /Users/dblevins/examples/ear-testing/business-logic
    INFO - Configuring Service(id=Default Security Service, type=SecurityService, provider-id=Default Security Service)
    INFO - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
    INFO - Configuring Service(id=movieDatabaseUnmanaged, type=Resource, provider-id=Default JDBC Database)
    INFO - Configuring Service(id=movieDatabase, type=Resource, provider-id=Default JDBC Database)
    INFO - Found PersistenceModule in classpath: /Users/dblevins/examples/ear-testing/business-model/target/business-model-1.0.jar
    INFO - Found EjbModule in classpath: /Users/dblevins/examples/ear-testing/business-logic/target/classes
    INFO - Using 'openejb.deployments.classpath.ear=true'
    INFO - Beginning load: /Users/dblevins/examples/ear-testing/business-model/target/business-model-1.0.jar
    INFO - Beginning load: /Users/dblevins/examples/ear-testing/business-logic/target/classes
    INFO - Configuring enterprise application: /Users/dblevins/examples/ear-testing/business-logic/classpath.ear
    INFO - Configuring Service(id=Default Stateful Container, type=Container, provider-id=Default Stateful Container)
    INFO - Auto-creating a container for bean Movies: Container(type=STATEFUL, id=Default Stateful Container)
    INFO - Configuring PersistenceUnit(name=movie-unit)
    INFO - Enterprise application "/Users/dblevins/examples/ear-testing/business-logic/classpath.ear" loaded.
    INFO - Assembling app: /Users/dblevins/examples/ear-testing/business-logic/classpath.ear
    INFO - PersistenceUnit(name=movie-unit, provider=org.apache.openjpa.persistence.PersistenceProviderImpl) - provider time 415ms
    INFO - Jndi(name=MoviesLocal) --> Ejb(deployment-id=Movies)
    INFO - Jndi(name=global/classpath.ear/business-logic/Movies!org.superbiz.logic.Movies) --> Ejb(deployment-id=Movies)
    INFO - Jndi(name=global/classpath.ear/business-logic/Movies) --> Ejb(deployment-id=Movies)
    INFO - Created Ejb(deployment-id=Movies, ejb-name=Movies, container=Default Stateful Container)
    INFO - Started Ejb(deployment-id=Movies, ejb-name=Movies, container=Default Stateful Container)
    INFO - Deployed Application(path=/Users/dblevins/examples/ear-testing/business-logic/classpath.ear)
    Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.393 sec

    Results :

    Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

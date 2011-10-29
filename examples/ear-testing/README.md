[INFO] Scanning for projects...
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Build Order:
[INFO] 
[INFO] OpenEJB :: Examples :: Ear Testing
[INFO] OpenEJB :: Examples :: Ear Testing :: Business Model
[INFO] OpenEJB :: Examples :: Ear Testing :: Business Logic
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building OpenEJB :: Examples :: Ear Testing 1.0
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.4.1:clean (default-clean) @ myear ---
[INFO] 
[INFO] --- maven-install-plugin:2.3.1:install (default-install) @ myear ---
[INFO] Installing /Users/dblevins/examples/ear-testing/pom.xml to /Users/dblevins/.m2/repository/org/superbiz/myear/1.0/myear-1.0.pom
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building OpenEJB :: Examples :: Ear Testing :: Business Model 1.0
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.4.1:clean (default-clean) @ business-model ---
[INFO] Deleting /Users/dblevins/examples/ear-testing/business-model/target
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:resources (default-resources) @ business-model ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Copying 1 resource
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:compile (default-compile) @ business-model ---
[INFO] Compiling 1 source file to /Users/dblevins/examples/ear-testing/business-model/target/classes
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:testResources (default-testResources) @ business-model ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /Users/dblevins/examples/ear-testing/business-model/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ business-model ---
[INFO] Nothing to compile - all classes are up to date
[INFO] 
[INFO] --- maven-surefire-plugin:2.6:test (default-test) @ business-model ---
[INFO] No tests to run.
[INFO] Surefire report directory: /Users/dblevins/examples/ear-testing/business-model/target/surefire-reports

-------------------------------------------------------
 T E S T S
-------------------------------------------------------
There are no tests to run.

Results :

Tests run: 0, Failures: 0, Errors: 0, Skipped: 0

[INFO] 
[INFO] --- maven-jar-plugin:2.3.1:jar (default-jar) @ business-model ---
[INFO] Building jar: /Users/dblevins/examples/ear-testing/business-model/target/business-model-1.0.jar
[INFO] 
[INFO] --- maven-install-plugin:2.3.1:install (default-install) @ business-model ---
[INFO] Installing /Users/dblevins/examples/ear-testing/business-model/target/business-model-1.0.jar to /Users/dblevins/.m2/repository/org/superbiz/business-model/1.0/business-model-1.0.jar
[INFO] Installing /Users/dblevins/examples/ear-testing/business-model/pom.xml to /Users/dblevins/.m2/repository/org/superbiz/business-model/1.0/business-model-1.0.pom
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building OpenEJB :: Examples :: Ear Testing :: Business Logic 1.0
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.4.1:clean (default-clean) @ business-logic ---
[INFO] Deleting /Users/dblevins/examples/ear-testing/business-logic/target
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:resources (default-resources) @ business-logic ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Copying 1 resource
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:compile (default-compile) @ business-logic ---
[INFO] Compiling 2 source files to /Users/dblevins/examples/ear-testing/business-logic/target/classes
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:testResources (default-testResources) @ business-logic ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /Users/dblevins/examples/ear-testing/business-logic/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ business-logic ---
[INFO] Compiling 1 source file to /Users/dblevins/examples/ear-testing/business-logic/target/test-classes
[INFO] 
[INFO] --- maven-surefire-plugin:2.6:test (default-test) @ business-logic ---
[INFO] Surefire report directory: /Users/dblevins/examples/ear-testing/business-logic/target/surefire-reports

-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running org.superbiz.logic.MoviesTest
Apache OpenEJB 4.0.0-beta-1    build: 20111002-04:06
http://openejb.apache.org/
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

[INFO] 
[INFO] --- maven-jar-plugin:2.3.1:jar (default-jar) @ business-logic ---
[INFO] Building jar: /Users/dblevins/examples/ear-testing/business-logic/target/business-logic-1.0.jar
[INFO] 
[INFO] --- maven-install-plugin:2.3.1:install (default-install) @ business-logic ---
[INFO] Installing /Users/dblevins/examples/ear-testing/business-logic/target/business-logic-1.0.jar to /Users/dblevins/.m2/repository/org/superbiz/business-logic/1.0/business-logic-1.0.jar
[INFO] Installing /Users/dblevins/examples/ear-testing/business-logic/pom.xml to /Users/dblevins/.m2/repository/org/superbiz/business-logic/1.0/business-logic-1.0.pom
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary:
[INFO] 
[INFO] OpenEJB :: Examples :: Ear Testing ................ SUCCESS [0.341s]
[INFO] OpenEJB :: Examples :: Ear Testing :: Business Model  SUCCESS [1.419s]
[INFO] OpenEJB :: Examples :: Ear Testing :: Business Logic  SUCCESS [3.691s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 5.573s
[INFO] Finished at: Fri Oct 28 17:07:07 PDT 2011
[INFO] Final Memory: 14M/81M
[INFO] ------------------------------------------------------------------------
    /**
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
    package org.superbiz.logic;
    
    import org.superbiz.model.Movie;
    
    import java.util.List;
    
    /**
     * @version $Revision: 607077 $ $Date: 2007-12-27 06:55:23 -0800 (Thu, 27 Dec 2007) $
     */
    public interface Movies {
        void addMovie(Movie movie) throws Exception;
    
        void deleteMovie(Movie movie) throws Exception;
    
        List<Movie> getMovies() throws Exception;
    }
    /**
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
    package org.superbiz.logic;
    
    //START SNIPPET: code
    
    import org.superbiz.model.Movie;
    
    import javax.ejb.Stateful;
    import javax.persistence.EntityManager;
    import javax.persistence.PersistenceContext;
    import javax.persistence.PersistenceContextType;
    import javax.persistence.Query;
    import java.util.List;
    
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
    
    }
    //END SNIPPET: code
    /**
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
    package org.superbiz.logic;
    
    import junit.framework.TestCase;
    import org.superbiz.model.Movie;
    
    import javax.naming.Context;
    import javax.naming.InitialContext;
    import java.util.List;
    import java.util.Properties;
    
    //START SNIPPET: code
    
    public class MoviesTest extends TestCase {
    
        public void test() throws Exception {
            Properties p = new Properties();
            p.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
    
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
    //END SNIPPET: code
    /**
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
    package org.superbiz.model;
    //START SNIPPET: code
    
    import javax.persistence.Entity;
    
    @Entity
    public class Movie {
    
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
    //END SNIPPET: code

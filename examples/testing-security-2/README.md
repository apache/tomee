[INFO] Scanning for projects...
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building OpenEJB :: Examples :: Testing Security 1.0
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.4.1:clean (default-clean) @ testing-security-2 ---
[INFO] Deleting /Users/dblevins/examples/testing-security-2/target
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:resources (default-resources) @ testing-security-2 ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Copying 1 resource
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:compile (default-compile) @ testing-security-2 ---
[INFO] Compiling 2 source files to /Users/dblevins/examples/testing-security-2/target/classes
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:testResources (default-testResources) @ testing-security-2 ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Copying 2 resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ testing-security-2 ---
[INFO] Compiling 1 source file to /Users/dblevins/examples/testing-security-2/target/test-classes
[INFO] 
[INFO] --- maven-surefire-plugin:2.6:test (default-test) @ testing-security-2 ---
[INFO] Surefire report directory: /Users/dblevins/examples/testing-security-2/target/surefire-reports

-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running org.superbiz.injection.secure.MovieTest
Apache OpenEJB 4.0.0-beta-1    build: 20111002-04:06
http://openejb.apache.org/
INFO - openejb.home = /Users/dblevins/examples/testing-security-2
INFO - openejb.base = /Users/dblevins/examples/testing-security-2
INFO - Using 'javax.ejb.embeddable.EJBContainer=true'
INFO - Configuring Service(id=Default Security Service, type=SecurityService, provider-id=Default Security Service)
INFO - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
INFO - Configuring Service(id=movieDatabase, type=Resource, provider-id=Default JDBC Database)
INFO - Found EjbModule in classpath: /Users/dblevins/examples/testing-security-2/target/classes
INFO - Beginning load: /Users/dblevins/examples/testing-security-2/target/classes
INFO - Configuring enterprise application: /Users/dblevins/examples/testing-security-2
INFO - Configuring Service(id=Default Stateful Container, type=Container, provider-id=Default Stateful Container)
INFO - Auto-creating a container for bean Movies: Container(type=STATEFUL, id=Default Stateful Container)
INFO - Configuring Service(id=Default Managed Container, type=Container, provider-id=Default Managed Container)
INFO - Auto-creating a container for bean org.superbiz.injection.secure.MovieTest: Container(type=MANAGED, id=Default Managed Container)
INFO - Configuring PersistenceUnit(name=movie-unit)
INFO - Auto-creating a Resource with id 'movieDatabaseNonJta' of type 'DataSource for 'movie-unit'.
INFO - Configuring Service(id=movieDatabaseNonJta, type=Resource, provider-id=movieDatabase)
INFO - Adjusting PersistenceUnit movie-unit <non-jta-data-source> to Resource ID 'movieDatabaseNonJta' from 'movieDatabaseUnmanaged'
INFO - Enterprise application "/Users/dblevins/examples/testing-security-2" loaded.
INFO - Assembling app: /Users/dblevins/examples/testing-security-2
INFO - PersistenceUnit(name=movie-unit, provider=org.apache.openjpa.persistence.PersistenceProviderImpl) - provider time 411ms
INFO - Jndi(name="java:global/testing-security-2/Movies!org.superbiz.injection.secure.Movies")
INFO - Jndi(name="java:global/testing-security-2/Movies")
INFO - Jndi(name="java:global/EjbModule236054577/org.superbiz.injection.secure.MovieTest!org.superbiz.injection.secure.MovieTest")
INFO - Jndi(name="java:global/EjbModule236054577/org.superbiz.injection.secure.MovieTest")
INFO - Created Ejb(deployment-id=Movies, ejb-name=Movies, container=Default Stateful Container)
INFO - Created Ejb(deployment-id=org.superbiz.injection.secure.MovieTest, ejb-name=org.superbiz.injection.secure.MovieTest, container=Default Managed Container)
INFO - Started Ejb(deployment-id=Movies, ejb-name=Movies, container=Default Stateful Container)
INFO - Started Ejb(deployment-id=org.superbiz.injection.secure.MovieTest, ejb-name=org.superbiz.injection.secure.MovieTest, container=Default Managed Container)
INFO - Deployed Application(path=/Users/dblevins/examples/testing-security-2)
INFO - Logging in
INFO - Logging out
INFO - EJBContainer already initialized.  Call ejbContainer.close() to allow reinitialization
INFO - Logging in
INFO - Logging out
INFO - EJBContainer already initialized.  Call ejbContainer.close() to allow reinitialization
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.39 sec

Results :

Tests run: 3, Failures: 0, Errors: 0, Skipped: 0

[INFO] 
[INFO] --- maven-jar-plugin:2.3.1:jar (default-jar) @ testing-security-2 ---
[INFO] Building jar: /Users/dblevins/examples/testing-security-2/target/testing-security-2-1.0.jar
[INFO] 
[INFO] --- maven-install-plugin:2.3.1:install (default-install) @ testing-security-2 ---
[INFO] Installing /Users/dblevins/examples/testing-security-2/target/testing-security-2-1.0.jar to /Users/dblevins/.m2/repository/org/superbiz/testing-security-2/1.0/testing-security-2-1.0.jar
[INFO] Installing /Users/dblevins/examples/testing-security-2/pom.xml to /Users/dblevins/.m2/repository/org/superbiz/testing-security-2/1.0/testing-security-2-1.0.pom
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 5.300s
[INFO] Finished at: Fri Oct 28 16:59:09 PDT 2011
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
    package org.superbiz.injection.secure;
    
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
    package org.superbiz.injection.secure;
    
    //START SNIPPET: code
    
    import javax.annotation.security.PermitAll;
    import javax.annotation.security.RolesAllowed;
    import javax.ejb.Stateful;
    import javax.ejb.TransactionAttribute;
    import javax.ejb.TransactionAttributeType;
    import javax.persistence.EntityManager;
    import javax.persistence.PersistenceContext;
    import javax.persistence.PersistenceContextType;
    import javax.persistence.Query;
    import java.util.List;
    
    @Stateful
    public class Movies  {
    
        @PersistenceContext(unitName = "movie-unit", type = PersistenceContextType.EXTENDED)
        private EntityManager entityManager;
    
        @RolesAllowed({"Employee", "Manager"})
        public void addMovie(Movie movie) throws Exception {
            entityManager.persist(movie);
        }
    
        @RolesAllowed({"Manager"})
        public void deleteMovie(Movie movie) throws Exception {
            entityManager.remove(movie);
        }
    
        @PermitAll
        @TransactionAttribute(TransactionAttributeType.SUPPORTS)
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
    package org.superbiz.injection.secure;
    
    import junit.framework.TestCase;
    
    import javax.ejb.EJB;
    import javax.ejb.EJBAccessException;
    import javax.ejb.embeddable.EJBContainer;
    import javax.naming.Context;
    import javax.naming.InitialContext;
    import java.util.List;
    import java.util.Properties;
    
    //START SNIPPET: code
    public class MovieTest extends TestCase {
    
        @EJB
        private Movies movies;
    
        protected void setUp() throws Exception {
    
            // Uncomment this line to set the login/logout functionality on Debug
            //System.setProperty("log4j.category.OpenEJB.security", "debug");
    
            Properties p = new Properties();
            p.put("movieDatabase", "new://Resource?type=DataSource");
            p.put("movieDatabase.JdbcDriver", "org.hsqldb.jdbcDriver");
            p.put("movieDatabase.JdbcUrl", "jdbc:hsqldb:mem:moviedb");
    
            EJBContainer.createEJBContainer(p).getContext().bind("inject", this);
        }
    
        public void testAsManager() throws Exception {
            Properties p = new Properties();
            p.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
            p.put(Context.SECURITY_PRINCIPAL, "jane");
            p.put(Context.SECURITY_CREDENTIALS, "waterfall");
    
            InitialContext context = new InitialContext(p);
    
            try {
                movies.addMovie(new Movie("Quentin Tarantino", "Reservoir Dogs", 1992));
                movies.addMovie(new Movie("Joel Coen", "Fargo", 1996));
                movies.addMovie(new Movie("Joel Coen", "The Big Lebowski", 1998));
    
                List<Movie> list = movies.getMovies();
                assertEquals("List.size()", 3, list.size());
    
                for (Movie movie : list) {
                    movies.deleteMovie(movie);
                }
    
                assertEquals("Movies.getMovies()", 0, movies.getMovies().size());
            } finally {
                context.close();
            }
        }
    
        public void testAsEmployee() throws Exception {
            Properties p = new Properties();
            p.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
            p.put(Context.SECURITY_PRINCIPAL, "joe");
            p.put(Context.SECURITY_CREDENTIALS, "cool");
    
            InitialContext context = new InitialContext(p);
    
            try {
                movies.addMovie(new Movie("Quentin Tarantino", "Reservoir Dogs", 1992));
                movies.addMovie(new Movie("Joel Coen", "Fargo", 1996));
                movies.addMovie(new Movie("Joel Coen", "The Big Lebowski", 1998));
    
                List<Movie> list = movies.getMovies();
                assertEquals("List.size()", 3, list.size());
    
                for (Movie movie : list) {
                    try {
                        movies.deleteMovie(movie);
                        fail("Employees should not be allowed to delete");
                    } catch (EJBAccessException e) {
                        // Good, Employees cannot delete things
                    }
                }
    
                // The list should still be three movies long
                assertEquals("Movies.getMovies()", 3, movies.getMovies().size());
            } finally {
                context.close();
            }
        }
    
        public void testUnauthenticated() throws Exception {
            try {
                movies.addMovie(new Movie("Quentin Tarantino", "Reservoir Dogs", 1992));
                fail("Unauthenticated users should not be able to add movies");
            } catch (EJBAccessException e) {
                // Good, guests cannot add things
            }
    
            try {
                movies.deleteMovie(null);
                fail("Unauthenticated users should not be allowed to delete");
            } catch (EJBAccessException e) {
                // Good, Unauthenticated users cannot delete things
            }
    
            try {
                // Read access should be allowed
    
                List<Movie> list = movies.getMovies();
    
            } catch (EJBAccessException e) {
                fail("Read access should be allowed");
            }
    
        }
    }
    //END SNIPPET: code

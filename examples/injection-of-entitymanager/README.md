[INFO] Scanning for projects...
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building OpenEJB :: Examples :: @PersistenceContext EntityManager Injection 1.0
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.4.1:clean (default-clean) @ injection-of-entitymanager ---
[INFO] Deleting /Users/dblevins/examples/injection-of-entitymanager/target
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:resources (default-resources) @ injection-of-entitymanager ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Copying 1 resource
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:compile (default-compile) @ injection-of-entitymanager ---
[INFO] Compiling 2 source files to /Users/dblevins/examples/injection-of-entitymanager/target/classes
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:testResources (default-testResources) @ injection-of-entitymanager ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /Users/dblevins/examples/injection-of-entitymanager/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ injection-of-entitymanager ---
[INFO] Compiling 1 source file to /Users/dblevins/examples/injection-of-entitymanager/target/test-classes
[INFO] 
[INFO] --- maven-surefire-plugin:2.7.2:test (default-test) @ injection-of-entitymanager ---
[INFO] Surefire report directory: /Users/dblevins/examples/injection-of-entitymanager/target/surefire-reports

-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running org.superbiz.injection.jpa.MoviesTest
Apache OpenEJB 4.0.0-beta-1    build: 20111002-04:06
http://openejb.apache.org/
INFO - openejb.home = /Users/dblevins/examples/injection-of-entitymanager
INFO - openejb.base = /Users/dblevins/examples/injection-of-entitymanager
INFO - Using 'javax.ejb.embeddable.EJBContainer=true'
INFO - Configuring Service(id=Default Security Service, type=SecurityService, provider-id=Default Security Service)
INFO - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
INFO - Configuring Service(id=movieDatabase, type=Resource, provider-id=Default JDBC Database)
INFO - Found EjbModule in classpath: /Users/dblevins/examples/injection-of-entitymanager/target/classes
INFO - Beginning load: /Users/dblevins/examples/injection-of-entitymanager/target/classes
INFO - Configuring enterprise application: /Users/dblevins/examples/injection-of-entitymanager
INFO - Configuring Service(id=Default Stateful Container, type=Container, provider-id=Default Stateful Container)
INFO - Auto-creating a container for bean Movies: Container(type=STATEFUL, id=Default Stateful Container)
INFO - Configuring Service(id=Default Managed Container, type=Container, provider-id=Default Managed Container)
INFO - Auto-creating a container for bean org.superbiz.injection.jpa.MoviesTest: Container(type=MANAGED, id=Default Managed Container)
INFO - Configuring PersistenceUnit(name=movie-unit)
INFO - Auto-creating a Resource with id 'movieDatabaseNonJta' of type 'DataSource for 'movie-unit'.
INFO - Configuring Service(id=movieDatabaseNonJta, type=Resource, provider-id=movieDatabase)
INFO - Adjusting PersistenceUnit movie-unit <non-jta-data-source> to Resource ID 'movieDatabaseNonJta' from 'movieDatabaseUnmanaged'
INFO - Enterprise application "/Users/dblevins/examples/injection-of-entitymanager" loaded.
INFO - Assembling app: /Users/dblevins/examples/injection-of-entitymanager
INFO - PersistenceUnit(name=movie-unit, provider=org.apache.openjpa.persistence.PersistenceProviderImpl) - provider time 413ms
INFO - Jndi(name="java:global/injection-of-entitymanager/Movies!org.superbiz.injection.jpa.Movies")
INFO - Jndi(name="java:global/injection-of-entitymanager/Movies")
INFO - Jndi(name="java:global/EjbModule221571972/org.superbiz.injection.jpa.MoviesTest!org.superbiz.injection.jpa.MoviesTest")
INFO - Jndi(name="java:global/EjbModule221571972/org.superbiz.injection.jpa.MoviesTest")
INFO - Created Ejb(deployment-id=Movies, ejb-name=Movies, container=Default Stateful Container)
INFO - Created Ejb(deployment-id=org.superbiz.injection.jpa.MoviesTest, ejb-name=org.superbiz.injection.jpa.MoviesTest, container=Default Managed Container)
INFO - Started Ejb(deployment-id=Movies, ejb-name=Movies, container=Default Stateful Container)
INFO - Started Ejb(deployment-id=org.superbiz.injection.jpa.MoviesTest, ejb-name=org.superbiz.injection.jpa.MoviesTest, container=Default Managed Container)
INFO - Deployed Application(path=/Users/dblevins/examples/injection-of-entitymanager)
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.211 sec

Results :

Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

[INFO] 
[INFO] --- maven-jar-plugin:2.3.1:jar (default-jar) @ injection-of-entitymanager ---
[INFO] Building jar: /Users/dblevins/examples/injection-of-entitymanager/target/injection-of-entitymanager-1.0.jar
[INFO] 
[INFO] --- maven-install-plugin:2.3.1:install (default-install) @ injection-of-entitymanager ---
[INFO] Installing /Users/dblevins/examples/injection-of-entitymanager/target/injection-of-entitymanager-1.0.jar to /Users/dblevins/.m2/repository/org/superbiz/injection-of-entitymanager/1.0/injection-of-entitymanager-1.0.jar
[INFO] Installing /Users/dblevins/examples/injection-of-entitymanager/pom.xml to /Users/dblevins/.m2/repository/org/superbiz/injection-of-entitymanager/1.0/injection-of-entitymanager-1.0.pom
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 5.094s
[INFO] Finished at: Fri Oct 28 17:10:24 PDT 2011
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
    package org.superbiz.injection.jpa;
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
    package org.superbiz.injection.jpa;
    
    //START SNIPPET: code
    
    import javax.ejb.Stateful;
    import javax.persistence.EntityManager;
    import javax.persistence.PersistenceContext;
    import javax.persistence.PersistenceContextType;
    import javax.persistence.Query;
    import java.util.List;
    
    @Stateful
    public class Movies {
    
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
    package org.superbiz.injection.jpa;
    
    import junit.framework.TestCase;
    
    import javax.ejb.embeddable.EJBContainer;
    import javax.naming.Context;
    import java.util.List;
    import java.util.Properties;
    
    //START SNIPPET: code
    public class MoviesTest extends TestCase {
    
        public void test() throws Exception {
    
            final Properties p = new Properties();
            p.put("movieDatabase", "new://Resource?type=DataSource");
            p.put("movieDatabase.JdbcDriver", "org.hsqldb.jdbcDriver");
            p.put("movieDatabase.JdbcUrl", "jdbc:hsqldb:mem:moviedb");
    
            final Context context = EJBContainer.createEJBContainer(p).getContext();
    
            Movies movies = (Movies) context.lookup("java:global/injection-of-entitymanager/Movies");
    
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

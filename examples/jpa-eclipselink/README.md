[INFO] Scanning for projects...
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building OpenEJB :: Examples :: JPA with EclipseLink 1.0
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.4.1:clean (default-clean) @ jpa-eclipselink ---
[INFO] Deleting /Users/dblevins/examples/jpa-eclipselink/target
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:resources (default-resources) @ jpa-eclipselink ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Copying 1 resource
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:compile (default-compile) @ jpa-eclipselink ---
[INFO] Compiling 2 source files to /Users/dblevins/examples/jpa-eclipselink/target/classes
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:testResources (default-testResources) @ jpa-eclipselink ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /Users/dblevins/examples/jpa-eclipselink/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ jpa-eclipselink ---
[INFO] Compiling 1 source file to /Users/dblevins/examples/jpa-eclipselink/target/test-classes
[INFO] 
[INFO] --- maven-surefire-plugin:2.7.2:test (default-test) @ jpa-eclipselink ---
[INFO] Surefire report directory: /Users/dblevins/examples/jpa-eclipselink/target/surefire-reports

-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running org.superbiz.eclipselink.MoviesTest
Apache OpenEJB 4.0.0-beta-1    build: 20111002-04:06
http://openejb.apache.org/
INFO - openejb.home = /Users/dblevins/examples/jpa-eclipselink
INFO - openejb.base = /Users/dblevins/examples/jpa-eclipselink
INFO - Using 'javax.ejb.embeddable.EJBContainer=true'
INFO - Configuring Service(id=Default Security Service, type=SecurityService, provider-id=Default Security Service)
INFO - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
INFO - Configuring Service(id=movieDatabase, type=Resource, provider-id=Default JDBC Database)
INFO - Found EjbModule in classpath: /Users/dblevins/examples/jpa-eclipselink/target/classes
INFO - Searched 6 classpath urls in 1053 milliseconds.  Average 175 milliseconds per url.
INFO - Beginning load: /Users/dblevins/examples/jpa-eclipselink/target/classes
INFO - Configuring enterprise application: /Users/dblevins/examples/jpa-eclipselink
INFO - Configuring Service(id=Default Stateful Container, type=Container, provider-id=Default Stateful Container)
INFO - Auto-creating a container for bean Movies: Container(type=STATEFUL, id=Default Stateful Container)
INFO - Configuring Service(id=Default Managed Container, type=Container, provider-id=Default Managed Container)
INFO - Auto-creating a container for bean org.superbiz.eclipselink.MoviesTest: Container(type=MANAGED, id=Default Managed Container)
INFO - Configuring PersistenceUnit(name=movie-unit, provider=org.eclipse.persistence.jpa.PersistenceProvider)
INFO - Auto-creating a Resource with id 'movieDatabaseNonJta' of type 'DataSource for 'movie-unit'.
INFO - Configuring Service(id=movieDatabaseNonJta, type=Resource, provider-id=movieDatabase)
INFO - Adjusting PersistenceUnit movie-unit <non-jta-data-source> to Resource ID 'movieDatabaseNonJta' from 'movieDatabaseUnmanaged'
INFO - Enterprise application "/Users/dblevins/examples/jpa-eclipselink" loaded.
INFO - Assembling app: /Users/dblevins/examples/jpa-eclipselink
INFO - PersistenceUnit(name=movie-unit, provider=org.eclipse.persistence.jpa.PersistenceProvider) - provider time 535ms
INFO - Jndi(name="java:global/jpa-eclipselink/Movies!org.superbiz.eclipselink.Movies")
INFO - Jndi(name="java:global/jpa-eclipselink/Movies")
INFO - Jndi(name="java:global/EjbModule900256864/org.superbiz.eclipselink.MoviesTest!org.superbiz.eclipselink.MoviesTest")
INFO - Jndi(name="java:global/EjbModule900256864/org.superbiz.eclipselink.MoviesTest")
INFO - Created Ejb(deployment-id=Movies, ejb-name=Movies, container=Default Stateful Container)
INFO - Created Ejb(deployment-id=org.superbiz.eclipselink.MoviesTest, ejb-name=org.superbiz.eclipselink.MoviesTest, container=Default Managed Container)
INFO - Started Ejb(deployment-id=Movies, ejb-name=Movies, container=Default Stateful Container)
INFO - Started Ejb(deployment-id=org.superbiz.eclipselink.MoviesTest, ejb-name=org.superbiz.eclipselink.MoviesTest, container=Default Managed Container)
INFO - Deployed Application(path=/Users/dblevins/examples/jpa-eclipselink)
[EL Info]: 2011-10-28 17:05:52.461--ServerSession(1920842039)--EclipseLink, version: Eclipse Persistence Services - 2.1.1.v20100817-r8050
[EL Info]: 2011-10-28 17:05:52.624--ServerSession(1920842039)--file:/Users/dblevins/examples/jpa-eclipselink/target/classes/_movie-unit login successful
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 3.428 sec

Results :

Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

[INFO] 
[INFO] --- maven-jar-plugin:2.3.1:jar (default-jar) @ jpa-eclipselink ---
[INFO] Building jar: /Users/dblevins/examples/jpa-eclipselink/target/jpa-eclipselink-1.0.jar
[INFO] 
[INFO] --- maven-install-plugin:2.3.1:install (default-install) @ jpa-eclipselink ---
[INFO] Installing /Users/dblevins/examples/jpa-eclipselink/target/jpa-eclipselink-1.0.jar to /Users/dblevins/.m2/repository/org/superbiz/jpa-eclipselink/1.0/jpa-eclipselink-1.0.jar
[INFO] Installing /Users/dblevins/examples/jpa-eclipselink/pom.xml to /Users/dblevins/.m2/repository/org/superbiz/jpa-eclipselink/1.0/jpa-eclipselink-1.0.pom
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 6.548s
[INFO] Finished at: Fri Oct 28 17:05:53 PDT 2011
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
    package org.superbiz.eclipselink;
    
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
    package org.superbiz.eclipselink;
    
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
    package org.superbiz.eclipselink;
    
    import junit.framework.TestCase;
    
    import javax.ejb.embeddable.EJBContainer;
    import javax.naming.Context;
    import java.util.List;
    import java.util.Properties;
    
    /**
     * @version $Revision: 607077 $ $Date: 2007-12-27 06:55:23 -0800 (Thu, 27 Dec 2007) $
     */
    public class MoviesTest extends TestCase {
    
        public void test() throws Exception {
            Properties p = new Properties();
            p.put("movieDatabase", "new://Resource?type=DataSource");
            p.put("movieDatabase.JdbcDriver", "org.hsqldb.jdbcDriver");
            p.put("movieDatabase.JdbcUrl", "jdbc:hsqldb:mem:moviedb");
    
            final Context context = EJBContainer.createEJBContainer(p).getContext();
    
            Movies movies = (Movies) context.lookup("java:global/jpa-eclipselink/Movies");
    
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

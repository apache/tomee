[INFO] Scanning for projects...
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building OpenEJB :: Examples :: Spring Integration 1.0
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.4.1:clean (default-clean) @ spring-integration ---
[INFO] Deleting /Users/dblevins/examples/spring-integration/target
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:resources (default-resources) @ spring-integration ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Copying 3 resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:compile (default-compile) @ spring-integration ---
[INFO] Compiling 8 source files to /Users/dblevins/examples/spring-integration/target/classes
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:testResources (default-testResources) @ spring-integration ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /Users/dblevins/examples/spring-integration/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ spring-integration ---
[INFO] Compiling 1 source file to /Users/dblevins/examples/spring-integration/target/test-classes
[INFO] 
[INFO] --- maven-surefire-plugin:2.7.2:test (default-test) @ spring-integration ---
[INFO] Surefire report directory: /Users/dblevins/examples/spring-integration/target/surefire-reports

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
INFO - PersistenceUnit(name=movie-unit, provider=org.hibernate.ejb.HibernatePersistence) - provider time 672ms
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
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.429 sec

Results :

Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

[INFO] 
[INFO] --- maven-jar-plugin:2.3.1:jar (default-jar) @ spring-integration ---
[INFO] Building jar: /Users/dblevins/examples/spring-integration/target/spring-integration-1.0.jar
[INFO] 
[INFO] --- maven-install-plugin:2.3.1:install (default-install) @ spring-integration ---
[INFO] Installing /Users/dblevins/examples/spring-integration/target/spring-integration-1.0.jar to /Users/dblevins/.m2/repository/org/superbiz/spring-integration/1.0/spring-integration-1.0.jar
[INFO] Installing /Users/dblevins/examples/spring-integration/pom.xml to /Users/dblevins/.m2/repository/org/superbiz/spring-integration/1.0/spring-integration-1.0.pom
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 5.577s
[INFO] Finished at: Fri Oct 28 17:09:39 PDT 2011
[INFO] Final Memory: 15M/81M
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
    package org.superbiz.spring;
    
    import javax.annotation.PostConstruct;
    import javax.ejb.EJB;
    import java.util.List;
    
    //START SNIPPET: code
    
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
    package org.superbiz.spring;
    
    import java.util.List;
    
    public interface Cineplex {
    
        public List<Theater> getTheaters();
    
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
    package org.superbiz.spring;
    
    import javax.annotation.Resource;
    import javax.ejb.Stateless;
    import java.util.List;
    
    //START SNIPPET: code
    
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
    package org.superbiz.spring;
    
    import java.util.List;
    
    public interface Movies {
        void addMovie(Movie movie) throws Exception;
    
        void deleteMovie(Movie movie) throws Exception;
    
        List<Movie> getMovies() throws Exception;
    
        Movie getMovieByTitle(String title) throws Exception;
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
    package org.superbiz.spring;
    
    import javax.ejb.Stateful;
    import javax.persistence.EntityManager;
    import javax.persistence.PersistenceContext;
    import javax.persistence.PersistenceContextType;
    import javax.persistence.Query;
    import java.util.List;
    
    //START SNIPPET: code
    
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
    package org.superbiz.spring;
    
    import javax.inject.Inject;
    import javax.inject.Named;
    import org.springframework.beans.factory.annotation.Autowired;
    
    import java.util.ArrayList;
    import java.util.List;
    
    //START SNIPPET: code
    
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
        @Inject @Named(value = "MoviesLocal")
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
    package org.superbiz.spring;
    
    import junit.framework.TestCase;
    import org.springframework.context.support.ClassPathXmlApplicationContext;
    
    import java.util.List;
    
    //START SNIPPET: code
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
    //END SNIPPET: code

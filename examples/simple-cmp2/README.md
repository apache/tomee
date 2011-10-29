[INFO] Scanning for projects...
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building OpenEJB :: Examples :: Simple CMP2 Entity 1.0
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.4.1:clean (default-clean) @ simple-cmp2 ---
[INFO] Deleting /Users/dblevins/examples/simple-cmp2/target
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:resources (default-resources) @ simple-cmp2 ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Copying 2 resources
[INFO] 
[INFO] --- maven-dependency-plugin:2.1:copy (copy) @ simple-cmp2 ---
[INFO] Configured Artifact: org.apache.openejb:openejb-javaagent:4.0.0-beta-1:jar
[INFO] Copying openejb-javaagent-4.0.0-beta-1.jar to /Users/dblevins/examples/simple-cmp2/target/openejb-javaagent-4.0.0-beta-1.jar
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:compile (default-compile) @ simple-cmp2 ---
[INFO] Compiling 3 source files to /Users/dblevins/examples/simple-cmp2/target/classes
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:testResources (default-testResources) @ simple-cmp2 ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /Users/dblevins/examples/simple-cmp2/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ simple-cmp2 ---
[INFO] Compiling 1 source file to /Users/dblevins/examples/simple-cmp2/target/test-classes
[INFO] 
[INFO] --- maven-surefire-plugin:2.6:test (default-test) @ simple-cmp2 ---
[INFO] Surefire report directory: /Users/dblevins/examples/simple-cmp2/target/surefire-reports

-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running org.superbiz.cmp2.MoviesTest
Apache OpenEJB 4.0.0-beta-1    build: 20111002-04:06
http://openejb.apache.org/
INFO - openejb.home = /Users/dblevins/examples/simple-cmp2/target
INFO - openejb.base = /Users/dblevins/examples/simple-cmp2/target
INFO - Configuring Service(id=Default Security Service, type=SecurityService, provider-id=Default Security Service)
INFO - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
INFO - Configuring Service(id=movieDatabaseUnmanaged, type=Resource, provider-id=Default JDBC Database)
INFO - Configuring Service(id=movieDatabase, type=Resource, provider-id=Default JDBC Database)
INFO - Found EjbModule in classpath: /Users/dblevins/examples/simple-cmp2/target/classes
INFO - Beginning load: /Users/dblevins/examples/simple-cmp2/target/classes
INFO - Configuring enterprise application: /Users/dblevins/examples/simple-cmp2/target/classpath.ear
INFO - Configuring Service(id=Default CMP Container, type=Container, provider-id=Default CMP Container)
INFO - Auto-creating a container for bean MovieBean: Container(type=CMP_ENTITY, id=Default CMP Container)
INFO - Configuring PersistenceUnit(name=cmp)
INFO - Adjusting PersistenceUnit cmp <jta-data-source> to Resource ID 'movieDatabase' from 'null'
INFO - Adjusting PersistenceUnit cmp <non-jta-data-source> to Resource ID 'movieDatabaseUnmanaged' from 'null'
INFO - Enterprise application "/Users/dblevins/examples/simple-cmp2/target/classpath.ear" loaded.
INFO - Assembling app: /Users/dblevins/examples/simple-cmp2/target/classpath.ear
INFO - PersistenceUnit(name=cmp, provider=org.apache.openjpa.persistence.PersistenceProviderImpl) - provider time 165ms
INFO - Jndi(name=MovieBeanLocalHome) --> Ejb(deployment-id=MovieBean)
INFO - Jndi(name=global/classpath.ear/simple-cmp2/MovieBean!org.superbiz.cmp2.Movies) --> Ejb(deployment-id=MovieBean)
INFO - Jndi(name=global/classpath.ear/simple-cmp2/MovieBean) --> Ejb(deployment-id=MovieBean)
INFO - Created Ejb(deployment-id=MovieBean, ejb-name=MovieBean, container=Default CMP Container)
INFO - Started Ejb(deployment-id=MovieBean, ejb-name=MovieBean, container=Default CMP Container)
INFO - Deployed Application(path=/Users/dblevins/examples/simple-cmp2/target/classpath.ear)
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.91 sec

Results :

Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

[INFO] 
[INFO] --- maven-jar-plugin:2.3.1:jar (default-jar) @ simple-cmp2 ---
[INFO] Building jar: /Users/dblevins/examples/simple-cmp2/target/simple-cmp2-1.0.jar
[INFO] 
[INFO] --- maven-install-plugin:2.3.1:install (default-install) @ simple-cmp2 ---
[INFO] Installing /Users/dblevins/examples/simple-cmp2/target/simple-cmp2-1.0.jar to /Users/dblevins/.m2/repository/org/superbiz/simple-cmp2/1.0/simple-cmp2-1.0.jar
[INFO] Installing /Users/dblevins/examples/simple-cmp2/pom.xml to /Users/dblevins/.m2/repository/org/superbiz/simple-cmp2/1.0/simple-cmp2-1.0.pom
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 6.010s
[INFO] Finished at: Fri Oct 28 17:00:05 PDT 2011
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
    package org.superbiz.cmp2;
    
    /**
     * @version $Revision$ $Date$
     */
    public interface Movie extends javax.ejb.EJBLocalObject {
        java.lang.Integer getId();
    
        void setId(java.lang.Integer id);
    
        String getDirector();
    
        void setDirector(String director);
    
        String getTitle();
    
        void setTitle(String title);
    
        int getYear();
    
        void setYear(int year);
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
    package org.superbiz.cmp2;
    
    import javax.ejb.EntityBean;
    
    public abstract class MovieBean implements EntityBean {
    
        public MovieBean() {
        }
    
        public Integer ejbCreate(String director, String title, int year) {
            this.setDirector(director);
            this.setTitle(title);
            this.setYear(year);
            return null;
        }
    
        public abstract java.lang.Integer getId();
    
        public abstract void setId(java.lang.Integer id);
    
        public abstract String getDirector();
    
        public abstract void setDirector(String director);
    
        public abstract String getTitle();
    
        public abstract void setTitle(String title);
    
        public abstract int getYear();
    
        public abstract void setYear(int year);
    
    
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
    package org.superbiz.cmp2;
    
    import javax.ejb.CreateException;
    import javax.ejb.FinderException;
    import java.util.Collection;
    
    /**
     * @version $Revision$ $Date$
     */
    interface Movies extends javax.ejb.EJBLocalHome {
        Movie create(String director, String title, int year) throws CreateException;
    
        Movie findByPrimaryKey(Integer primarykey) throws FinderException;
    
        Collection<Movie> findAll() throws FinderException;
    
        Collection<Movie> findByDirector(String director) throws FinderException;
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
    package org.superbiz.cmp2;
    
    import junit.framework.TestCase;
    
    import javax.naming.Context;
    import javax.naming.InitialContext;
    import java.util.Collection;
    import java.util.Properties;
    
    /**
     * @version $Revision: 607077 $ $Date: 2007-12-27 06:55:23 -0800 (Thu, 27 Dec 2007) $
     */
    public class MoviesTest extends TestCase {
    
        public void test() throws Exception {
            Properties p = new Properties();
            p.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
            p.put("movieDatabase", "new://Resource?type=DataSource");
            p.put("movieDatabase.JdbcDriver", "org.hsqldb.jdbcDriver");
            p.put("movieDatabase.JdbcUrl", "jdbc:hsqldb:mem:moviedb");
    
            p.put("movieDatabaseUnmanaged", "new://Resource?type=DataSource");
            p.put("movieDatabaseUnmanaged.JdbcDriver", "org.hsqldb.jdbcDriver");
            p.put("movieDatabaseUnmanaged.JdbcUrl", "jdbc:hsqldb:mem:moviedb");
            p.put("movieDatabaseUnmanaged.JtaManaged", "false");
    
            Context context = new InitialContext(p);
    
            Movies movies = (Movies) context.lookup("MovieBeanLocalHome");
    
            movies.create("Quentin Tarantino", "Reservoir Dogs", 1992);
            movies.create("Joel Coen", "Fargo", 1996);
            movies.create("Joel Coen", "The Big Lebowski", 1998);
    
            Collection<Movie> list = movies.findAll();
            assertEquals("Collection.size()", 3, list.size());
    
            for (Movie movie : list) {
                movies.remove(movie.getPrimaryKey());
            }
    
            assertEquals("Movies.findAll()", 0, movies.findAll().size());
        }
    }

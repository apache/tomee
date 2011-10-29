[INFO] Scanning for projects...
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building OpenEJB :: Examples :: @EJB Lookup 1.0
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.4.1:clean (default-clean) @ lookup-of-ejbs ---
[INFO] Deleting /Users/dblevins/examples/lookup-of-ejbs/target
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:resources (default-resources) @ lookup-of-ejbs ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /Users/dblevins/examples/lookup-of-ejbs/src/main/resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:compile (default-compile) @ lookup-of-ejbs ---
[INFO] Compiling 3 source files to /Users/dblevins/examples/lookup-of-ejbs/target/classes
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:testResources (default-testResources) @ lookup-of-ejbs ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /Users/dblevins/examples/lookup-of-ejbs/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ lookup-of-ejbs ---
[INFO] Compiling 1 source file to /Users/dblevins/examples/lookup-of-ejbs/target/test-classes
[INFO] 
[INFO] --- maven-surefire-plugin:2.7.2:test (default-test) @ lookup-of-ejbs ---
[INFO] Surefire report directory: /Users/dblevins/examples/lookup-of-ejbs/target/surefire-reports

-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running org.superbiz.ejblookup.EjbDependencyTest
Apache OpenEJB 4.0.0-beta-1    build: 20111002-04:06
http://openejb.apache.org/
INFO - openejb.home = /Users/dblevins/examples/lookup-of-ejbs
INFO - openejb.base = /Users/dblevins/examples/lookup-of-ejbs
INFO - Using 'javax.ejb.embeddable.EJBContainer=true'
INFO - Configuring Service(id=Default Security Service, type=SecurityService, provider-id=Default Security Service)
INFO - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
INFO - Found EjbModule in classpath: /Users/dblevins/examples/lookup-of-ejbs/target/classes
INFO - Beginning load: /Users/dblevins/examples/lookup-of-ejbs/target/classes
INFO - Configuring enterprise application: /Users/dblevins/examples/lookup-of-ejbs
INFO - Configuring Service(id=Default Stateless Container, type=Container, provider-id=Default Stateless Container)
INFO - Auto-creating a container for bean BlueBean: Container(type=STATELESS, id=Default Stateless Container)
INFO - Configuring Service(id=Default Managed Container, type=Container, provider-id=Default Managed Container)
INFO - Auto-creating a container for bean org.superbiz.ejblookup.EjbDependencyTest: Container(type=MANAGED, id=Default Managed Container)
INFO - Enterprise application "/Users/dblevins/examples/lookup-of-ejbs" loaded.
INFO - Assembling app: /Users/dblevins/examples/lookup-of-ejbs
INFO - Jndi(name="java:global/lookup-of-ejbs/BlueBean!org.superbiz.ejblookup.Friend")
INFO - Jndi(name="java:global/lookup-of-ejbs/BlueBean")
INFO - Jndi(name="java:global/lookup-of-ejbs/RedBean!org.superbiz.ejblookup.Friend")
INFO - Jndi(name="java:global/lookup-of-ejbs/RedBean")
INFO - Jndi(name="java:global/EjbModule236706648/org.superbiz.ejblookup.EjbDependencyTest!org.superbiz.ejblookup.EjbDependencyTest")
INFO - Jndi(name="java:global/EjbModule236706648/org.superbiz.ejblookup.EjbDependencyTest")
INFO - Created Ejb(deployment-id=RedBean, ejb-name=RedBean, container=Default Stateless Container)
INFO - Created Ejb(deployment-id=BlueBean, ejb-name=BlueBean, container=Default Stateless Container)
INFO - Created Ejb(deployment-id=org.superbiz.ejblookup.EjbDependencyTest, ejb-name=org.superbiz.ejblookup.EjbDependencyTest, container=Default Managed Container)
INFO - Started Ejb(deployment-id=RedBean, ejb-name=RedBean, container=Default Stateless Container)
INFO - Started Ejb(deployment-id=BlueBean, ejb-name=BlueBean, container=Default Stateless Container)
INFO - Started Ejb(deployment-id=org.superbiz.ejblookup.EjbDependencyTest, ejb-name=org.superbiz.ejblookup.EjbDependencyTest, container=Default Managed Container)
INFO - Deployed Application(path=/Users/dblevins/examples/lookup-of-ejbs)
INFO - EJBContainer already initialized.  Call ejbContainer.close() to allow reinitialization
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.192 sec

Results :

Tests run: 2, Failures: 0, Errors: 0, Skipped: 0

[INFO] 
[INFO] --- maven-jar-plugin:2.3.1:jar (default-jar) @ lookup-of-ejbs ---
[INFO] Building jar: /Users/dblevins/examples/lookup-of-ejbs/target/lookup-of-ejbs-1.0.jar
[INFO] 
[INFO] --- maven-install-plugin:2.3.1:install (default-install) @ lookup-of-ejbs ---
[INFO] Installing /Users/dblevins/examples/lookup-of-ejbs/target/lookup-of-ejbs-1.0.jar to /Users/dblevins/.m2/repository/org/superbiz/lookup-of-ejbs/1.0/lookup-of-ejbs-1.0.jar
[INFO] Installing /Users/dblevins/examples/lookup-of-ejbs/pom.xml to /Users/dblevins/.m2/repository/org/superbiz/lookup-of-ejbs/1.0/lookup-of-ejbs-1.0.pom
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 4.072s
[INFO] Finished at: Fri Oct 28 17:10:45 PDT 2011
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
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     */
    package org.superbiz.ejblookup;
    
    import javax.ejb.EJB;
    import javax.ejb.EJBException;
    import javax.ejb.Stateless;
    import javax.naming.InitialContext;
    import javax.naming.NamingException;
    
    //START SNIPPET: code
    @Stateless
    @EJB(beanInterface = Friend.class, beanName = "RedBean", name = "myFriend")
    public class BlueBean implements Friend {
    
        public String sayHello() {
            return "Blue says, Hello!";
        }
    
        public String helloFromFriend() {
            try {
                Friend friend = (Friend) new InitialContext().lookup("java:comp/env/myFriend");
                return "My friend " + friend.sayHello();
            } catch (NamingException e) {
                throw new EJBException(e);
            }
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
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     */
    package org.superbiz.ejblookup;
    
    import javax.ejb.Local;
    
    /**
     * This is an EJB 3 local business interface
     * A local business interface may be annotated with the @Local
     * annotation, but it's optional. A business interface which is
     * not annotated with @Local or @Remote is assumed to be Local
     * if the bean does not implement any other interfaces
     */
    //START SNIPPET: code
    @Local
    public interface Friend {
    
        public String sayHello();
    
        public String helloFromFriend();
    
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
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     */
    package org.superbiz.ejblookup;
    
    import javax.ejb.EJB;
    import javax.ejb.EJBException;
    import javax.ejb.Stateless;
    import javax.naming.InitialContext;
    import javax.naming.NamingException;
    
    //START SNIPPET: code
    @Stateless
    @EJB(beanInterface = Friend.class, beanName = "BlueBean", name = "myFriend")
    public class RedBean implements Friend {
    
        public String sayHello() {
            return "Red says, Hello!";
        }
    
        public String helloFromFriend() {
            try {
                Friend friend = (Friend) new InitialContext().lookup("java:comp/env/myFriend");
                return "My friend " + friend.sayHello();
            } catch (NamingException e) {
                throw new EJBException(e);
            }
        }
    }
    //END SNIPPET: code/**
     * Licensed to the Apache Software Foundation (ASF) under one or more
     * contributor license agreements.  See the NOTICE file distributed with
     * this work for additional information regarding copyright ownership.
     * The ASF licenses this file to You under the Apache License, Version 2.0
     * (the "License"); you may not use this file except in compliance with
     * the License.  You may obtain a copy of the License at
     *
     *     http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     */
    package org.superbiz.ejblookup;
    
    import junit.framework.TestCase;
    
    import javax.ejb.embeddable.EJBContainer;
    import javax.naming.Context;
    import javax.naming.InitialContext;
    import java.util.Properties;
    
    //START SNIPPET: code
    public class EjbDependencyTest extends TestCase {
    
        private Context context;
    
        protected void setUp() throws Exception {
            context = EJBContainer.createEJBContainer().getContext();
        }
    
        public void testRed() throws Exception {
    
            Friend red = (Friend) context.lookup("java:global/lookup-of-ejbs/RedBean");
    
            assertNotNull(red);
            assertEquals("Red says, Hello!", red.sayHello());
            assertEquals("My friend Blue says, Hello!", red.helloFromFriend());
    
        }
    
        public void testBlue() throws Exception {
    
            Friend blue = (Friend) context.lookup("java:global/lookup-of-ejbs/BlueBean");
    
            assertNotNull(blue);
            assertEquals("Blue says, Hello!", blue.sayHello());
            assertEquals("My friend Red says, Hello!", blue.helloFromFriend());
    
        }
    
    }
    //END SNIPPET: code

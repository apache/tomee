[INFO] Scanning for projects...
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building OpenEJB :: Examples :: Hello World - Weblogic 1.0
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.4.1:clean (default-clean) @ helloworld-weblogic ---
[INFO] Deleting /Users/dblevins/examples/helloworld-weblogic/target
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:resources (default-resources) @ helloworld-weblogic ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Copying 2 resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:compile (default-compile) @ helloworld-weblogic ---
[INFO] Compiling 3 source files to /Users/dblevins/examples/helloworld-weblogic/target/classes
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:testResources (default-testResources) @ helloworld-weblogic ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /Users/dblevins/examples/helloworld-weblogic/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ helloworld-weblogic ---
[INFO] Compiling 1 source file to /Users/dblevins/examples/helloworld-weblogic/target/test-classes
[INFO] 
[INFO] --- maven-surefire-plugin:2.7.2:test (default-test) @ helloworld-weblogic ---
[INFO] Surefire report directory: /Users/dblevins/examples/helloworld-weblogic/target/surefire-reports

-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running org.superbiz.hello.HelloTest
Apache OpenEJB 4.0.0-beta-1    build: 20111002-04:06
http://openejb.apache.org/
INFO - openejb.home = /Users/dblevins/examples/helloworld-weblogic
INFO - openejb.base = /Users/dblevins/examples/helloworld-weblogic
INFO - Configuring Service(id=Default Security Service, type=SecurityService, provider-id=Default Security Service)
INFO - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
INFO - Found EjbModule in classpath: /Users/dblevins/examples/helloworld-weblogic/target/classes
INFO - Beginning load: /Users/dblevins/examples/helloworld-weblogic/target/classes
INFO - Configuring enterprise application: /Users/dblevins/examples/helloworld-weblogic/classpath.ear
INFO - Configuring Service(id=Default Stateless Container, type=Container, provider-id=Default Stateless Container)
INFO - Auto-creating a container for bean HelloBean: Container(type=STATELESS, id=Default Stateless Container)
INFO - Enterprise application "/Users/dblevins/examples/helloworld-weblogic/classpath.ear" loaded.
INFO - Assembling app: /Users/dblevins/examples/helloworld-weblogic/classpath.ear
INFO - Jndi(name=MyHello) --> Ejb(deployment-id=HelloBean)
INFO - Jndi(name=global/classpath.ear/helloworld-weblogic/HelloBean!org.superbiz.hello.HelloEjbLocalHome) --> Ejb(deployment-id=HelloBean)
INFO - Jndi(name=global/classpath.ear/helloworld-weblogic/HelloBean) --> Ejb(deployment-id=HelloBean)
INFO - Created Ejb(deployment-id=HelloBean, ejb-name=HelloBean, container=Default Stateless Container)
INFO - Started Ejb(deployment-id=HelloBean, ejb-name=HelloBean, container=Default Stateless Container)
INFO - Deployed Application(path=/Users/dblevins/examples/helloworld-weblogic/classpath.ear)
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.397 sec

Results :

Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

[INFO] 
[INFO] --- maven-jar-plugin:2.3.1:jar (default-jar) @ helloworld-weblogic ---
[INFO] Building jar: /Users/dblevins/examples/helloworld-weblogic/target/helloworld-weblogic-1.0.jar
[INFO] 
[INFO] --- maven-install-plugin:2.3.1:install (default-install) @ helloworld-weblogic ---
[INFO] Installing /Users/dblevins/examples/helloworld-weblogic/target/helloworld-weblogic-1.0.jar to /Users/dblevins/.m2/repository/org/superbiz/helloworld-weblogic/1.0/helloworld-weblogic-1.0.jar
[INFO] Installing /Users/dblevins/examples/helloworld-weblogic/pom.xml to /Users/dblevins/.m2/repository/org/superbiz/helloworld-weblogic/1.0/helloworld-weblogic-1.0.pom
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 4.423s
[INFO] Finished at: Fri Oct 28 17:08:51 PDT 2011
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
    package org.superbiz.hello;
    
    import javax.ejb.LocalHome;
    import javax.ejb.Stateless;
    
    /**
     * @version $Revision: 607077 $ $Date: 2007-12-27 06:55:23 -0800 (Thu, 27 Dec 2007) $
     */
    @Stateless
    @LocalHome(HelloEjbLocalHome.class)
    public class HelloBean {
    
        public String sayHello() {
            return "Hello, World!";
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
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     */
    package org.superbiz.hello;
    
    import javax.ejb.EJBLocalObject;
    
    /**
     * @version $Revision: 607077 $ $Date: 2007-12-27 06:55:23 -0800 (Thu, 27 Dec 2007) $
     */
    public interface HelloEjbLocal extends EJBLocalObject {
    
        String sayHello();
    
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
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     */
    package org.superbiz.hello;
    
    import javax.ejb.CreateException;
    import javax.ejb.EJBLocalHome;
    
    /**
     * @version $Revision: 1090810 $ $Date: 2011-04-10 07:49:26 -0700 (Sun, 10 Apr 2011) $
     */
    public interface HelloEjbLocalHome extends EJBLocalHome {
        HelloEjbLocal create() throws CreateException;
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
    package org.superbiz.hello;
    
    import junit.framework.TestCase;
    
    import javax.naming.Context;
    import javax.naming.InitialContext;
    import java.util.Properties;
    
    /**
     * @version $Revision: 1090810 $ $Date: 2011-04-10 07:49:26 -0700 (Sun, 10 Apr 2011) $
     */
    public class HelloTest extends TestCase {
    
        public void test() throws Exception {
            Properties properties = new Properties();
            properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
            InitialContext initialContext = new InitialContext(properties);
    
            HelloEjbLocalHome localHome = (HelloEjbLocalHome) initialContext.lookup("MyHello");
            HelloEjbLocal helloEjb = localHome.create();
    
            String message = helloEjb.sayHello();
    
            assertEquals(message, "Hello, World!");
        }
    }

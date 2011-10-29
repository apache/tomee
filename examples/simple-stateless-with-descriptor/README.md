[INFO] Scanning for projects...
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building OpenEJB :: Examples :: Simple Stateless With Deployment Descriptor 1.0
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.4.1:clean (default-clean) @ simple-stateless-with-descriptor ---
[INFO] Deleting /Users/dblevins/examples/simple-stateless-with-descriptor/target
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:resources (default-resources) @ simple-stateless-with-descriptor ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Copying 1 resource
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:compile (default-compile) @ simple-stateless-with-descriptor ---
[INFO] Compiling 3 source files to /Users/dblevins/examples/simple-stateless-with-descriptor/target/classes
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:testResources (default-testResources) @ simple-stateless-with-descriptor ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /Users/dblevins/examples/simple-stateless-with-descriptor/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ simple-stateless-with-descriptor ---
[INFO] Compiling 1 source file to /Users/dblevins/examples/simple-stateless-with-descriptor/target/test-classes
[INFO] 
[INFO] --- maven-surefire-plugin:2.7.2:test (default-test) @ simple-stateless-with-descriptor ---
[INFO] Surefire report directory: /Users/dblevins/examples/simple-stateless-with-descriptor/target/surefire-reports

-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running org.superbiz.calculator.CalculatorTest
Apache OpenEJB 4.0.0-beta-1    build: 20111002-04:06
http://openejb.apache.org/
INFO - openejb.home = /Users/dblevins/examples/simple-stateless-with-descriptor
INFO - openejb.base = /Users/dblevins/examples/simple-stateless-with-descriptor
INFO - Configuring Service(id=Default Security Service, type=SecurityService, provider-id=Default Security Service)
INFO - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
INFO - Found EjbModule in classpath: /Users/dblevins/examples/simple-stateless-with-descriptor/target/classes
INFO - Beginning load: /Users/dblevins/examples/simple-stateless-with-descriptor/target/classes
INFO - Configuring enterprise application: /Users/dblevins/examples/simple-stateless-with-descriptor/classpath.ear
INFO - Configuring Service(id=Default Stateless Container, type=Container, provider-id=Default Stateless Container)
INFO - Auto-creating a container for bean CalculatorImpl: Container(type=STATELESS, id=Default Stateless Container)
INFO - Enterprise application "/Users/dblevins/examples/simple-stateless-with-descriptor/classpath.ear" loaded.
INFO - Assembling app: /Users/dblevins/examples/simple-stateless-with-descriptor/classpath.ear
INFO - Jndi(name=CalculatorImplLocal) --> Ejb(deployment-id=CalculatorImpl)
INFO - Jndi(name=global/classpath.ear/simple-stateless-with-descriptor/CalculatorImpl!org.superbiz.calculator.CalculatorLocal) --> Ejb(deployment-id=CalculatorImpl)
INFO - Jndi(name=CalculatorImplRemote) --> Ejb(deployment-id=CalculatorImpl)
INFO - Jndi(name=global/classpath.ear/simple-stateless-with-descriptor/CalculatorImpl!org.superbiz.calculator.CalculatorRemote) --> Ejb(deployment-id=CalculatorImpl)
INFO - Jndi(name=global/classpath.ear/simple-stateless-with-descriptor/CalculatorImpl) --> Ejb(deployment-id=CalculatorImpl)
INFO - Created Ejb(deployment-id=CalculatorImpl, ejb-name=CalculatorImpl, container=Default Stateless Container)
INFO - Started Ejb(deployment-id=CalculatorImpl, ejb-name=CalculatorImpl, container=Default Stateless Container)
INFO - Deployed Application(path=/Users/dblevins/examples/simple-stateless-with-descriptor/classpath.ear)
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.578 sec

Results :

Tests run: 2, Failures: 0, Errors: 0, Skipped: 0

[INFO] 
[INFO] --- maven-jar-plugin:2.3.1:jar (default-jar) @ simple-stateless-with-descriptor ---
[INFO] Building jar: /Users/dblevins/examples/simple-stateless-with-descriptor/target/simple-stateless-with-descriptor-1.0.jar
[INFO] 
[INFO] --- maven-install-plugin:2.3.1:install (default-install) @ simple-stateless-with-descriptor ---
[INFO] Installing /Users/dblevins/examples/simple-stateless-with-descriptor/target/simple-stateless-with-descriptor-1.0.jar to /Users/dblevins/.m2/repository/org/superbiz/simple-stateless-with-descriptor/1.0/simple-stateless-with-descriptor-1.0.jar
[INFO] Installing /Users/dblevins/examples/simple-stateless-with-descriptor/pom.xml to /Users/dblevins/.m2/repository/org/superbiz/simple-stateless-with-descriptor/1.0/simple-stateless-with-descriptor-1.0.pom
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 4.718s
[INFO] Finished at: Fri Oct 28 17:03:06 PDT 2011
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
    package org.superbiz.calculator;
    
    /**
     * This is an EJB 3 stateless session bean, configured using an EJB 3
     * deployment descriptor as opposed to using annotations.
     * This EJB has 2 business interfaces: CalculatorRemote, a remote business
     * interface, and CalculatorLocal, a local business interface
     */
    //START SNIPPET: code
    public class CalculatorImpl implements CalculatorRemote, CalculatorLocal {
    
        public int sum(int add1, int add2) {
            return add1 + add2;
        }
    
        public int multiply(int mul1, int mul2) {
            return mul1 * mul2;
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
    package org.superbiz.calculator;
    
    /**
     * This is an EJB 3 local business interface
     * This interface is specified using the business-local tag in the deployment descriptor
     */
    //START SNIPPET: code
    public interface CalculatorLocal {
    
        public int sum(int add1, int add2);
    
        public int multiply(int mul1, int mul2);
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
    package org.superbiz.calculator;
    
    
    /**
     * This is an EJB 3 remote business interface
     * This interface is specified using the business-local tag in the deployment descriptor
     */
    //START SNIPPET: code
    public interface CalculatorRemote {
    
        public int sum(int add1, int add2);
    
        public int multiply(int mul1, int mul2);
    
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
    package org.superbiz.calculator;
    
    import junit.framework.TestCase;
    
    import javax.naming.Context;
    import javax.naming.InitialContext;
    import java.util.Properties;
    
    public class CalculatorTest extends TestCase {
    
        //START SNIPPET: setup
        private InitialContext initialContext;
    
        protected void setUp() throws Exception {
            Properties properties = new Properties();
            properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
    
            initialContext = new InitialContext(properties);
        }
        //END SNIPPET: setup    
    
        /**
         * Lookup the Calculator bean via its remote home interface
         *
         * @throws Exception
         */
        //START SNIPPET: remote
        public void testCalculatorViaRemoteInterface() throws Exception {
            Object object = initialContext.lookup("CalculatorImplRemote");
    
            assertNotNull(object);
            assertTrue(object instanceof CalculatorRemote);
            CalculatorRemote calc = (CalculatorRemote) object;
            assertEquals(10, calc.sum(4, 6));
            assertEquals(12, calc.multiply(3, 4));
        }
        //END SNIPPET: remote
    
        /**
         * Lookup the Calculator bean via its local home interface
         *
         * @throws Exception
         */
        //START SNIPPET: local    
        public void testCalculatorViaLocalInterface() throws Exception {
            Object object = initialContext.lookup("CalculatorImplLocal");
    
            assertNotNull(object);
            assertTrue(object instanceof CalculatorLocal);
            CalculatorLocal calc = (CalculatorLocal) object;
            assertEquals(10, calc.sum(4, 6));
            assertEquals(12, calc.multiply(3, 4));
        }
        //END SNIPPET: local
    
    }

[INFO] Scanning for projects...
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building OpenEJB :: Web Examples :: EJB WebService with Security 1.0
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.4.1:clean (default-clean) @ webservice-security ---
[INFO] Deleting /Users/dblevins/examples/webservice-security/target
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:resources (default-resources) @ webservice-security ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Copying 3 resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:compile (default-compile) @ webservice-security ---
[INFO] Compiling 3 source files to /Users/dblevins/examples/webservice-security/target/classes
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:testResources (default-testResources) @ webservice-security ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Copying 2 resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ webservice-security ---
[INFO] Compiling 1 source file to /Users/dblevins/examples/webservice-security/target/test-classes
[INFO] 
[INFO] --- maven-surefire-plugin:2.7.2:test (default-test) @ webservice-security ---
[INFO] Surefire report directory: /Users/dblevins/examples/webservice-security/target/surefire-reports

-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running org.superbiz.calculator.CalculatorTest
Apache OpenEJB 4.0.0-beta-1    build: 20111002-04:06
http://openejb.apache.org/
INFO - openejb.home = /Users/dblevins/examples/webservice-security
INFO - openejb.base = /Users/dblevins/examples/webservice-security
INFO - Configuring Service(id=Default Security Service, type=SecurityService, provider-id=Default Security Service)
INFO - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
INFO - Found EjbModule in classpath: /Users/dblevins/examples/webservice-security/target/classes
INFO - Beginning load: /Users/dblevins/examples/webservice-security/target/classes
INFO - Configuring enterprise application: /Users/dblevins/examples/webservice-security/classpath.ear
INFO - Configuring Service(id=Default Stateless Container, type=Container, provider-id=Default Stateless Container)
INFO - Auto-creating a container for bean CalculatorImpl: Container(type=STATELESS, id=Default Stateless Container)
INFO - Enterprise application "/Users/dblevins/examples/webservice-security/classpath.ear" loaded.
INFO - Assembling app: /Users/dblevins/examples/webservice-security/classpath.ear
INFO - Jndi(name=CalculatorImplRemote) --> Ejb(deployment-id=CalculatorImpl)
INFO - Jndi(name=global/classpath.ear/webservice-security/CalculatorImpl!org.superbiz.calculator.CalculatorRemote) --> Ejb(deployment-id=CalculatorImpl)
INFO - Jndi(name=global/classpath.ear/webservice-security/CalculatorImpl) --> Ejb(deployment-id=CalculatorImpl)
INFO - Created Ejb(deployment-id=CalculatorImpl, ejb-name=CalculatorImpl, container=Default Stateless Container)
INFO - Started Ejb(deployment-id=CalculatorImpl, ejb-name=CalculatorImpl, container=Default Stateless Container)
INFO - Deployed Application(path=/Users/dblevins/examples/webservice-security/classpath.ear)
INFO - Initializing network services
INFO - Creating ServerService(id=httpejbd)
INFO - Creating ServerService(id=cxf)
INFO - Creating ServerService(id=admin)
INFO - Creating ServerService(id=ejbd)
INFO - Creating ServerService(id=ejbds)
INFO - Initializing network services
  ** Starting Services **
  NAME                 IP              PORT  
  httpejbd             127.0.0.1       4204  
  admin thread         127.0.0.1       4200  
  ejbd                 127.0.0.1       4201  
  ejbd                 127.0.0.1       4203  
-------
Ready!
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 3.415 sec

Results :

Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

[INFO] 
[INFO] --- maven-jar-plugin:2.3.1:jar (default-jar) @ webservice-security ---
[INFO] Building jar: /Users/dblevins/examples/webservice-security/target/webservice-security-1.0.jar
[INFO] 
[INFO] --- maven-install-plugin:2.3.1:install (default-install) @ webservice-security ---
[INFO] Installing /Users/dblevins/examples/webservice-security/target/webservice-security-1.0.jar to /Users/dblevins/.m2/repository/org/superbiz/webservice-security/1.0/webservice-security-1.0.jar
[INFO] Installing /Users/dblevins/examples/webservice-security/pom.xml to /Users/dblevins/.m2/repository/org/superbiz/webservice-security/1.0/webservice-security-1.0.pom
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 7.169s
[INFO] Finished at: Fri Oct 28 17:08:39 PDT 2011
[INFO] Final Memory: 17M/81M
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
    
    import javax.annotation.security.DeclareRoles;
    import javax.annotation.security.RolesAllowed;
    import javax.ejb.Stateless;
    import javax.jws.WebService;
    
    /**
     * This is an EJB 3 style pojo stateless session bean
     * Every stateless session bean implementation must be annotated
     * using the annotation @Stateless
     * This EJB has a single interface: CalculatorWs a webservice interface.
     */
    //START SNIPPET: code
    @DeclareRoles(value = {"Administrator"})
    @Stateless
    @WebService(
            portName = "CalculatorPort",
            serviceName = "CalculatorWsService",
            targetNamespace = "http://superbiz.org/wsdl",
            endpointInterface = "org.superbiz.calculator.CalculatorWs")
    public class CalculatorImpl implements CalculatorWs, CalculatorRemote {
    
        @RolesAllowed(value = {"Administrator"})
        public int sum(int add1, int add2) {
            return add1 + add2;
        }
    
        @RolesAllowed(value = {"Administrator"})
        public int multiply(int mul1, int mul2) {
            return mul1 * mul2;
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
    package org.superbiz.calculator;
    
    import javax.ejb.Remote;
    
    @Remote
    public interface CalculatorRemote {
    
        public int sum(int add1, int add2);
    
        public int multiply(int mul1, int mul2);
    
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
    package org.superbiz.calculator;
    
    import javax.jws.WebService;
    
    //END SNIPPET: code
    
    /**
     * This is an EJB 3 webservice interface
     * A webservice interface must be annotated with the @Local
     * annotation.
     */
    //START SNIPPET: code
    @WebService(targetNamespace = "http://superbiz.org/wsdl")
    public interface CalculatorWs {
    
        public int sum(int add1, int add2);
    
        public int multiply(int mul1, int mul2);
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
    package org.superbiz.calculator;
    
    import junit.framework.TestCase;
    
    import javax.naming.Context;
    import javax.naming.InitialContext;
    import javax.xml.namespace.QName;
    import javax.xml.ws.BindingProvider;
    import javax.xml.ws.Service;
    import java.net.URL;
    import java.util.Properties;
    
    public class CalculatorTest extends TestCase {
    
        //START SNIPPET: setup
        private InitialContext initialContext;
    
        protected void setUp() throws Exception {
            Properties properties = new Properties();
            properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
            properties.setProperty("openejb.embedded.remotable", "true");
    
            initialContext = new InitialContext(properties);
        }
        //END SNIPPET: setup
    
        /**
         * Create a webservice client using wsdl url
         *
         * @throws Exception
         */
        //START SNIPPET: webservice
        public void testCalculatorViaWsInterface() throws Exception {
            URL url = new URL("http://127.0.0.1:4204/CalculatorImpl?wsdl");
            QName calcServiceQName = new QName("http://superbiz.org/wsdl", "CalculatorWsService");
            Service calcService = Service.create(url, calcServiceQName);
            assertNotNull(calcService);
    
            CalculatorWs calc = calcService.getPort(CalculatorWs.class);
            ((BindingProvider) calc).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "jane");
            ((BindingProvider) calc).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "waterfall");
            assertEquals(10, calc.sum(4, 6));
            assertEquals(12, calc.multiply(3, 4));
        }
        //END SNIPPET: webservice
    
    }

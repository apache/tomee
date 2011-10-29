[INFO] Scanning for projects...
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building OpenEJB :: Examples :: Simple Webservice 1.0
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.4.1:clean (default-clean) @ simple-webservice ---
[INFO] Deleting /Users/dblevins/examples/simple-webservice/target
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:resources (default-resources) @ simple-webservice ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Copying 2 resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:compile (default-compile) @ simple-webservice ---
[INFO] Compiling 4 source files to /Users/dblevins/examples/simple-webservice/target/classes
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:testResources (default-testResources) @ simple-webservice ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Copying 1 resource
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ simple-webservice ---
[INFO] Compiling 1 source file to /Users/dblevins/examples/simple-webservice/target/test-classes
[INFO] 
[INFO] --- maven-surefire-plugin:2.7.2:test (default-test) @ simple-webservice ---
[INFO] Surefire report directory: /Users/dblevins/examples/simple-webservice/target/surefire-reports

-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running org.superbiz.calculator.CalculatorTest
Apache OpenEJB 4.0.0-beta-1    build: 20111002-04:06
http://openejb.apache.org/
INFO - openejb.home = /Users/dblevins/examples/simple-webservice
INFO - openejb.base = /Users/dblevins/examples/simple-webservice
INFO - Configuring Service(id=Default Security Service, type=SecurityService, provider-id=Default Security Service)
INFO - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
INFO - Found EjbModule in classpath: /Users/dblevins/examples/simple-webservice/target/test-classes
INFO - Found EjbModule in classpath: /Users/dblevins/examples/simple-webservice/target/classes
INFO - Beginning load: /Users/dblevins/examples/simple-webservice/target/test-classes
INFO - Beginning load: /Users/dblevins/examples/simple-webservice/target/classes
INFO - Configuring enterprise application: /Users/dblevins/examples/simple-webservice/classpath.ear
INFO - Configuring Service(id=Default Stateless Container, type=Container, provider-id=Default Stateless Container)
INFO - Auto-creating a container for bean CalculatorImpl: Container(type=STATELESS, id=Default Stateless Container)
INFO - Enterprise application "/Users/dblevins/examples/simple-webservice/classpath.ear" loaded.
INFO - Assembling app: /Users/dblevins/examples/simple-webservice/classpath.ear
INFO - Jndi(name=CalculatorImplLocal) --> Ejb(deployment-id=CalculatorImpl)
INFO - Jndi(name=global/classpath.ear/simple-webservice/CalculatorImpl!org.superbiz.calculator.CalculatorLocal) --> Ejb(deployment-id=CalculatorImpl)
INFO - Jndi(name=global/classpath.ear/simple-webservice/CalculatorImpl) --> Ejb(deployment-id=CalculatorImpl)
INFO - Created Ejb(deployment-id=CalculatorImpl, ejb-name=CalculatorImpl, container=Default Stateless Container)
INFO - Started Ejb(deployment-id=CalculatorImpl, ejb-name=CalculatorImpl, container=Default Stateless Container)
INFO - LocalClient(class=org.superbiz.calculator.CalculatorTest, module=test-classes) 
INFO - Deployed Application(path=/Users/dblevins/examples/simple-webservice/classpath.ear)
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
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 3.233 sec

Results :

Tests run: 3, Failures: 0, Errors: 0, Skipped: 0

[INFO] 
[INFO] --- maven-jar-plugin:2.3.1:jar (default-jar) @ simple-webservice ---
[INFO] Building jar: /Users/dblevins/examples/simple-webservice/target/simple-webservice-1.0.jar
[INFO] 
[INFO] --- maven-install-plugin:2.3.1:install (default-install) @ simple-webservice ---
[INFO] Installing /Users/dblevins/examples/simple-webservice/target/simple-webservice-1.0.jar to /Users/dblevins/.m2/repository/org/superbiz/simple-webservice/1.0/simple-webservice-1.0.jar
[INFO] Installing /Users/dblevins/examples/simple-webservice/pom.xml to /Users/dblevins/.m2/repository/org/superbiz/simple-webservice/1.0/simple-webservice-1.0.pom
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 6.743s
[INFO] Finished at: Fri Oct 28 16:59:36 PDT 2011
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
    
    import javax.ejb.Stateless;
    import javax.jws.HandlerChain;
    import javax.jws.WebService;
    import javax.xml.ws.Holder;
    import java.util.Date;
    
    /**
     * This is an EJB 3 style pojo stateless session bean
     * Every stateless session bean implementation must be annotated
     * using the annotation @Stateless
     * This EJB has a 2 interfaces:
     * <ul>
     * <li>CalculatorWs a webservice interface</li>
     * <li>CalculatorLocal a local interface</li>
     * </ul>
     */
    //START SNIPPET: code
    @Stateless
    @WebService(
            portName = "CalculatorPort",
            serviceName = "CalculatorWsService",
            targetNamespace = "http://superbiz.org/wsdl",
            endpointInterface = "org.superbiz.calculator.CalculatorWs")
    @HandlerChain(file = "handler.xml")
    public class CalculatorImpl implements CalculatorWs, CalculatorLocal {
    
        public int sum(int add1, int add2) {
            return add1 + add2;
        }
    
        public int multiply(int mul1, int mul2) {
            return mul1 * mul2;
        }
    
        public int factorial(
                int number,
                Holder<String> userId,
                Holder<String> returnCode,
                Holder<Date> datetime) {
    
            if (number == 0) {
                returnCode.value = "Can not calculate factorial for zero.";
                return -1;
            }
    
            returnCode.value = userId.value;
            datetime.value = new Date();
            return (int) factorial(number);
        }
    
        // return n!
        // precondition: n >= 0 and n <= 20
    
        private static long factorial(long n) {
            if (n < 0) throw new RuntimeException("Underflow error in factorial");
            else if (n > 20) throw new RuntimeException("Overflow error in factorial");
            else if (n == 0) return 1;
            else return n * factorial(n - 1);
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
    
    import javax.ejb.Local;
    
    @Local
    public interface CalculatorLocal extends CalculatorWs {
    
    }/**
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
    
    import javax.jws.WebParam;
    import javax.jws.WebService;
    import javax.jws.soap.SOAPBinding;
    import javax.jws.soap.SOAPBinding.ParameterStyle;
    import javax.jws.soap.SOAPBinding.Style;
    import javax.jws.soap.SOAPBinding.Use;
    import javax.xml.ws.Holder;
    import java.util.Date;
    
    //END SNIPPET: code
    
    /**
     * This is an EJB 3 webservice interface
     * A webservice interface must be annotated with the @WebService
     * annotation.
     */
    //START SNIPPET: code
    @WebService(
            name = "CalculatorWs",
            targetNamespace = "http://superbiz.org/wsdl")
    public interface CalculatorWs {
    
        public int sum(int add1, int add2);
    
        public int multiply(int mul1, int mul2);
    
        // because of CXF bug, BARE must be used instead of default WRAPPED
    
        @SOAPBinding(use = Use.LITERAL, parameterStyle = ParameterStyle.BARE, style = Style.DOCUMENT)
        public int factorial(
                int number,
                @WebParam(name = "userid", header = true, mode = WebParam.Mode.IN) Holder<String> userId,
                @WebParam(name = "returncode", header = true, mode = WebParam.Mode.OUT) Holder<String> returnCode,
                @WebParam(name = "datetime", header = true, mode = WebParam.Mode.INOUT) Holder<Date> datetime);
    
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
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.handler;
    
    import javax.xml.namespace.QName;
    import javax.xml.ws.handler.MessageContext;
    import javax.xml.ws.handler.soap.SOAPHandler;
    import javax.xml.ws.handler.soap.SOAPMessageContext;
    import java.util.Collections;
    import java.util.Set;
    
    public class DummyInterceptor implements SOAPHandler<SOAPMessageContext> {
        public DummyInterceptor() {
            super();
        }
    
        public Set<QName> getHeaders() {
            return Collections.emptySet();
        }
    
        public void close(MessageContext mc) {
        }
    
        public boolean handleFault(SOAPMessageContext mc) {
            return true;
        }
    
        public boolean handleMessage(SOAPMessageContext mc) {
            return true;
        }
    }/**
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
    import org.apache.openejb.api.LocalClient;
    
    import javax.naming.Context;
    import javax.naming.InitialContext;
    import javax.xml.namespace.QName;
    import javax.xml.ws.Holder;
    import javax.xml.ws.Service;
    import javax.xml.ws.WebServiceRef;
    import java.net.URL;
    import java.util.Date;
    import java.util.Properties;
    
    @LocalClient
    public class CalculatorTest extends TestCase {
    
        @WebServiceRef(
                wsdlLocation = "http://127.0.0.1:4204/CalculatorImpl?wsdl"
        )
        private CalculatorWs calculatorWs;
    
        //START SNIPPET: setup	
        private InitialContext initialContext;
    
        // date used to invoke a web service with INOUT parameters
        private static final Date date = new Date();
    
        protected void setUp() throws Exception {
            Properties properties = new Properties();
            properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
            properties.setProperty("openejb.embedded.remotable", "true");
    
            initialContext = new InitialContext(properties);
            initialContext.bind("inject", this);
    
        }
        //END SNIPPET: setup    
    
        /**
         * Create a webservice client using wsdl url
         *
         * @throws Exception
         */
        //START SNIPPET: webservice
        public void testCalculatorViaWsInterface() throws Exception {
            Service calcService = Service.create(
                    new URL("http://127.0.0.1:4204/CalculatorImpl?wsdl"),
                    new QName("http://superbiz.org/wsdl", "CalculatorWsService"));
            assertNotNull(calcService);
    
            CalculatorWs calc = calcService.getPort(CalculatorWs.class);
            assertEquals(10, calc.sum(4, 6));
            assertEquals(12, calc.multiply(3, 4));
    
            Holder<String> userIdHolder = new Holder<String>("jane");
            Holder<String> returnCodeHolder = new Holder<String>();
            Holder<Date> datetimeHolder = new Holder<Date>(date);
            assertEquals(6, calc.factorial(3, userIdHolder, returnCodeHolder, datetimeHolder));
            assertEquals(userIdHolder.value, returnCodeHolder.value);
            assertTrue(date.before(datetimeHolder.value));
        }
    
        public void testWebServiceRefInjection() throws Exception {
            assertEquals(10, calculatorWs.sum(4, 6));
            assertEquals(12, calculatorWs.multiply(3, 4));
    
            Holder<String> userIdHolder = new Holder<String>("jane");
            Holder<String> returnCodeHolder = new Holder<String>();
            Holder<Date> datetimeHolder = new Holder<Date>(date);
            assertEquals(6, calculatorWs.factorial(3, userIdHolder, returnCodeHolder, datetimeHolder));
            assertEquals(userIdHolder.value, returnCodeHolder.value);
            assertTrue(date.before(datetimeHolder.value));
        }
    
        public void testCalculatorViaRemoteInterface() throws Exception {
            CalculatorLocal calc = (CalculatorLocal) initialContext.lookup("CalculatorImplLocal");
            assertEquals(10, calc.sum(4, 6));
            assertEquals(12, calc.multiply(3, 4));
    
            Holder<String> userIdHolder = new Holder<String>("jane");
            Holder<String> returnCodeHolder = new Holder<String>();
            Holder<Date> datetimeHolder = new Holder<Date>(date);
            assertEquals(6, calc.factorial(3, userIdHolder, returnCodeHolder, datetimeHolder));
            assertEquals(userIdHolder.value, returnCodeHolder.value);
            assertTrue(date.before(datetimeHolder.value));
        }
        //END SNIPPET: webservice
    
    }

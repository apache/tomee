[INFO] Scanning for projects...
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building OpenEJB :: Web Examples :: EJB WebService 1.0
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.4.1:clean (default-clean) @ ejb-webservice ---
[INFO] Deleting /Users/dblevins/examples/webapps/ejb-webservice/target
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:resources (default-resources) @ ejb-webservice ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Copying 0 resource
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:compile (default-compile) @ ejb-webservice ---
[INFO] Compiling 1 source file to /Users/dblevins/examples/webapps/ejb-webservice/target/classes
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:testResources (default-testResources) @ ejb-webservice ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /Users/dblevins/examples/webapps/ejb-webservice/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ ejb-webservice ---
[INFO] No sources to compile
[INFO] 
[INFO] --- maven-surefire-plugin:2.6:test (default-test) @ ejb-webservice ---
[INFO] No tests to run.
[INFO] Surefire report directory: /Users/dblevins/examples/webapps/ejb-webservice/target/surefire-reports

-------------------------------------------------------
 T E S T S
-------------------------------------------------------
There are no tests to run.

Results :

Tests run: 0, Failures: 0, Errors: 0, Skipped: 0

[INFO] 
[INFO] --- maven-war-plugin:2.1.1:war (default-war) @ ejb-webservice ---
[INFO] Packaging webapp
[INFO] Assembling webapp [ejb-webservice] in [/Users/dblevins/examples/webapps/ejb-webservice/target/ejb-webservice-1.0]
[INFO] Processing war project
[INFO] Copying webapp resources [/Users/dblevins/examples/webapps/ejb-webservice/src/main/webapp]
[INFO] Webapp assembled in [21 msecs]
[INFO] Building war: /Users/dblevins/examples/webapps/ejb-webservice/target/ejb-webservice-1.0.war
[INFO] WEB-INF/web.xml already added, skipping
[INFO] 
[INFO] --- maven-install-plugin:2.3.1:install (default-install) @ ejb-webservice ---
[INFO] Installing /Users/dblevins/examples/webapps/ejb-webservice/target/ejb-webservice-1.0.war to /Users/dblevins/.m2/repository/org/superbiz/ejb-webservice/1.0/ejb-webservice-1.0.war
[INFO] Installing /Users/dblevins/examples/webapps/ejb-webservice/pom.xml to /Users/dblevins/.m2/repository/org/superbiz/ejb-webservice/1.0/ejb-webservice-1.0.pom
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 2.059s
[INFO] Finished at: Fri Oct 28 17:04:56 PDT 2011
[INFO] Final Memory: 10M/81M
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
    package org.superbiz.ws;
    
    import javax.ejb.Stateless;
    import javax.jws.WebService;
    
    @Stateless
    @WebService(portName = "CalculatorPort",
            serviceName = "CalculatorWebService",
            targetNamespace = "http://superbiz.org/wsdl")
    public class Calculator {
        public int sum(int add1, int add2) {
            return add1 + add2;
        }
    
        public int multiply(int mul1, int mul2) {
            return mul1 * mul2;
        }
    
    }
    

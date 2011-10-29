[INFO] Scanning for projects...
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building OpenEJB :: Web Examples :: JSF with MyFaces 1.0
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.4.1:clean (default-clean) @ jsf ---
[INFO] Deleting /Users/dblevins/examples/webapps/jsf/target
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:resources (default-resources) @ jsf ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Copying 0 resource
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:compile (default-compile) @ jsf ---
[INFO] Compiling 3 source files to /Users/dblevins/examples/webapps/jsf/target/classes
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:testResources (default-testResources) @ jsf ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /Users/dblevins/examples/webapps/jsf/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ jsf ---
[INFO] No sources to compile
[INFO] 
[INFO] --- maven-surefire-plugin:2.7.2:test (default-test) @ jsf ---
[INFO] No tests to run.
[INFO] Surefire report directory: /Users/dblevins/examples/webapps/jsf/target/surefire-reports

-------------------------------------------------------
 T E S T S
-------------------------------------------------------
There are no tests to run.

Results :

Tests run: 0, Failures: 0, Errors: 0, Skipped: 0

[INFO] 
[INFO] --- maven-war-plugin:2.1.1:war (default-war) @ jsf ---
[INFO] Packaging webapp
[INFO] Assembling webapp [jsf] in [/Users/dblevins/examples/webapps/jsf/target/jsf]
[INFO] Processing war project
[INFO] Copying webapp resources [/Users/dblevins/examples/webapps/jsf/src/main/webapp]
[INFO] Webapp assembled in [77 msecs]
[INFO] Building war: /Users/dblevins/examples/webapps/jsf/target/jsf.war
[INFO] WEB-INF/web.xml already added, skipping
[INFO] 
[INFO] --- maven-install-plugin:2.3.1:install (default-install) @ jsf ---
[INFO] Installing /Users/dblevins/examples/webapps/jsf/target/jsf.war to /Users/dblevins/.m2/repository/org/superbiz/jsf/jsf/1.0/jsf-1.0.war
[INFO] Installing /Users/dblevins/examples/webapps/jsf/pom.xml to /Users/dblevins/.m2/repository/org/superbiz/jsf/jsf/1.0/jsf-1.0.pom
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 3.068s
[INFO] Finished at: Fri Oct 28 17:03:29 PDT 2011
[INFO] Final Memory: 10M/81M
[INFO] ------------------------------------------------------------------------
    /**
     *
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
    package org.superbiz.jsf;
    
    import javax.ejb.Remote;
    
    @Remote
    public interface Calculator {
        public double add(double x, double y);
    }
    /*
     * Licensed to the Apache Software Foundation (ASF) under one
     * or more contributor license agreements.  See the NOTICE file
     * distributed with this work for additional information
     * regarding copyright ownership.  The ASF licenses this file
     * to you under the Apache License, Version 2.0 (the
     * "License"); you may not use this file except in compliance
     * with the License.  You may obtain a copy of the License at
     *
     *   http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing,
     * software distributed under the License is distributed on an
     * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
     * KIND, either express or implied.  See the License for the
     * specific language governing permissions and limitations
     * under the License.
    */
    package org.superbiz.jsf;
    
    import javax.ejb.EJB;
    
    public class CalculatorBean {
        @EJB
        Calculator calculator;
        private double x;
        private double y;
        private double result;
    
        public double getX() {
            return x;
        }
    
        public void setX(double x) {
            this.x = x;
        }
    
        public double getY() {
            return y;
        }
    
        public void setY(double y) {
            this.y = y;
        }
    
        public double getResult() {
            return result;
        }
    
        public void setResult(double result) {
            this.result = result;
        }
    
        public String add() {
            result = calculator.add(x, y);
            return "success";
        }
    }/**
     *
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
    package org.superbiz.jsf;
    
    import javax.ejb.Stateless;
    
    @Stateless
    public class CalculatorImpl implements Calculator {
    
        public double add(double x, double y) {
            return x + y;
        }
    
    }

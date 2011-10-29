[INFO] Scanning for projects...
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building OpenEJB :: Examples :: @Resource env-entry Injection 1.0
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.4.1:clean (default-clean) @ injection-of-env-entry ---
[INFO] Deleting /Users/dblevins/examples/injection-of-env-entry/target
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:resources (default-resources) @ injection-of-env-entry ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Copying 1 resource
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:compile (default-compile) @ injection-of-env-entry ---
[INFO] Compiling 3 source files to /Users/dblevins/examples/injection-of-env-entry/target/classes
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:testResources (default-testResources) @ injection-of-env-entry ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /Users/dblevins/examples/injection-of-env-entry/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ injection-of-env-entry ---
[INFO] Compiling 1 source file to /Users/dblevins/examples/injection-of-env-entry/target/test-classes
[INFO] 
[INFO] --- maven-surefire-plugin:2.7.2:test (default-test) @ injection-of-env-entry ---
[INFO] Surefire report directory: /Users/dblevins/examples/injection-of-env-entry/target/surefire-reports

-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running org.superbiz.injection.enventry.ConfigurationTest
Apache OpenEJB 4.0.0-beta-1    build: 20111002-04:06
http://openejb.apache.org/
INFO - openejb.home = /Users/dblevins/examples/injection-of-env-entry
INFO - openejb.base = /Users/dblevins/examples/injection-of-env-entry
INFO - Using 'javax.ejb.embeddable.EJBContainer=true'
INFO - Configuring Service(id=Default Security Service, type=SecurityService, provider-id=Default Security Service)
INFO - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
INFO - Found EjbModule in classpath: /Users/dblevins/examples/injection-of-env-entry/target/classes
INFO - Beginning load: /Users/dblevins/examples/injection-of-env-entry/target/classes
INFO - Configuring enterprise application: /Users/dblevins/examples/injection-of-env-entry
WARN - Method 'lookup' is not available for 'javax.annotation.Resource'. Probably using an older Runtime.
INFO - Configuring Service(id=Default Singleton Container, type=Container, provider-id=Default Singleton Container)
INFO - Auto-creating a container for bean Configuration: Container(type=SINGLETON, id=Default Singleton Container)
INFO - Configuring Service(id=Default Managed Container, type=Container, provider-id=Default Managed Container)
INFO - Auto-creating a container for bean org.superbiz.injection.enventry.ConfigurationTest: Container(type=MANAGED, id=Default Managed Container)
INFO - Enterprise application "/Users/dblevins/examples/injection-of-env-entry" loaded.
INFO - Assembling app: /Users/dblevins/examples/injection-of-env-entry
INFO - Jndi(name="java:global/injection-of-env-entry/Configuration!org.superbiz.injection.enventry.Configuration")
INFO - Jndi(name="java:global/injection-of-env-entry/Configuration")
INFO - Jndi(name="java:global/EjbModule1046388151/org.superbiz.injection.enventry.ConfigurationTest!org.superbiz.injection.enventry.ConfigurationTest")
INFO - Jndi(name="java:global/EjbModule1046388151/org.superbiz.injection.enventry.ConfigurationTest")
INFO - Created Ejb(deployment-id=org.superbiz.injection.enventry.ConfigurationTest, ejb-name=org.superbiz.injection.enventry.ConfigurationTest, container=Default Managed Container)
INFO - Created Ejb(deployment-id=Configuration, ejb-name=Configuration, container=Default Singleton Container)
INFO - Started Ejb(deployment-id=org.superbiz.injection.enventry.ConfigurationTest, ejb-name=org.superbiz.injection.enventry.ConfigurationTest, container=Default Managed Container)
INFO - Started Ejb(deployment-id=Configuration, ejb-name=Configuration, container=Default Singleton Container)
INFO - Deployed Application(path=/Users/dblevins/examples/injection-of-env-entry)
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.456 sec

Results :

Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

[INFO] 
[INFO] --- maven-jar-plugin:2.3.1:jar (default-jar) @ injection-of-env-entry ---
[INFO] Building jar: /Users/dblevins/examples/injection-of-env-entry/target/injection-of-env-entry-1.0.jar
[INFO] 
[INFO] --- maven-install-plugin:2.3.1:install (default-install) @ injection-of-env-entry ---
[INFO] Installing /Users/dblevins/examples/injection-of-env-entry/target/injection-of-env-entry-1.0.jar to /Users/dblevins/.m2/repository/org/superbiz/injection-of-env-entry/1.0/injection-of-env-entry-1.0.jar
[INFO] Installing /Users/dblevins/examples/injection-of-env-entry/pom.xml to /Users/dblevins/.m2/repository/org/superbiz/injection-of-env-entry/1.0/injection-of-env-entry-1.0.pom
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 4.491s
[INFO] Finished at: Fri Oct 28 17:02:29 PDT 2011
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
    package org.superbiz.injection.enventry;
    
    import javax.annotation.Resource;
    import javax.ejb.Singleton;
    import java.util.Date;
    
    /**
     * This example demostrates the use of the injection of environment entries
     * using <b>Resource</b> annotation.
     * <p/>
     * "EJB Core Contracts and Requirements" specification section 16.4.1.1.
     *
     * @version $Rev: 1090807 $ $Date: 2011-04-10 07:12:31 -0700 (Sun, 10 Apr 2011) $
     */
    //START SNIPPET: code
    @Singleton
    public class Configuration {
    
        @Resource
        private String color;
    
        @Resource
        private Shape shape;
    
        @Resource
        private Class strategy;
    
        @Resource(name = "date")
        private long date;
    
        public String getColor() {
            return color;
        }
    
        public Shape getShape() {
            return shape;
        }
    
        public Class getStrategy() {
            return strategy;
        }
    
        public Date getDate() {
            return new Date(date);
        }
    }
    //END SNIPPET: code
    /*
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
    package org.superbiz.injection.enventry;
    
    /**
     * @version $Revision$ $Date$
     */
    public enum Shape {
    
        CIRCLE,
        TRIANGLE,
        SQUARE
    
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
    package org.superbiz.injection.enventry;
    
    /**
     * Exists to show that any class object can be injected and does
     * not need to be loaded directly in app code.
     *
     * @version $Revision$ $Date$
     */
    public class Widget {
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
    package org.superbiz.injection.enventry;
    
    import junit.framework.TestCase;
    
    import javax.ejb.embeddable.EJBContainer;
    import javax.naming.Context;
    import java.util.Date;
    
    //START SNIPPET: code
    public class ConfigurationTest extends TestCase {
    
    
        public void test() throws Exception {
            final Context context = EJBContainer.createEJBContainer().getContext();
    
            final Configuration configuration = (Configuration) context.lookup("java:global/injection-of-env-entry/Configuration");
    
            assertEquals("orange", configuration.getColor());
    
            assertEquals(Shape.TRIANGLE, configuration.getShape());
    
            assertEquals(Widget.class, configuration.getStrategy());
    
            assertEquals(new Date(123456789), configuration.getDate());
        }
    }
    //END SNIPPET: code
     
    
    

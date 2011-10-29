[INFO] Scanning for projects...
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building OpenEJB :: Examples :: @Schedule Methods 1.0
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.4.1:clean (default-clean) @ schedule-methods ---
[INFO] Deleting /Users/dblevins/examples/schedule-methods/target
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:resources (default-resources) @ schedule-methods ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /Users/dblevins/examples/schedule-methods/src/main/resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:compile (default-compile) @ schedule-methods ---
[INFO] Compiling 1 source file to /Users/dblevins/examples/schedule-methods/target/classes
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:testResources (default-testResources) @ schedule-methods ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /Users/dblevins/examples/schedule-methods/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ schedule-methods ---
[INFO] Compiling 1 source file to /Users/dblevins/examples/schedule-methods/target/test-classes
[INFO] 
[INFO] --- maven-surefire-plugin:2.7.2:test (default-test) @ schedule-methods ---
[INFO] Surefire report directory: /Users/dblevins/examples/schedule-methods/target/surefire-reports

-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running org.superbiz.corn.FarmerBrownTest
Apache OpenEJB 4.0.0-beta-1    build: 20111002-04:06
http://openejb.apache.org/
INFO - openejb.home = /Users/dblevins/examples/schedule-methods
INFO - openejb.base = /Users/dblevins/examples/schedule-methods
INFO - Using 'javax.ejb.embeddable.EJBContainer=true'
INFO - Configuring Service(id=Default Security Service, type=SecurityService, provider-id=Default Security Service)
INFO - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
INFO - Found EjbModule in classpath: /Users/dblevins/examples/schedule-methods/target/classes
INFO - Beginning load: /Users/dblevins/examples/schedule-methods/target/classes
INFO - Configuring enterprise application: /Users/dblevins/examples/schedule-methods
INFO - Configuring Service(id=Default Singleton Container, type=Container, provider-id=Default Singleton Container)
INFO - Auto-creating a container for bean FarmerBrown: Container(type=SINGLETON, id=Default Singleton Container)
INFO - Configuring Service(id=Default Managed Container, type=Container, provider-id=Default Managed Container)
INFO - Auto-creating a container for bean org.superbiz.corn.FarmerBrownTest: Container(type=MANAGED, id=Default Managed Container)
INFO - Enterprise application "/Users/dblevins/examples/schedule-methods" loaded.
INFO - Assembling app: /Users/dblevins/examples/schedule-methods
INFO - Jndi(name="java:global/schedule-methods/FarmerBrown!org.superbiz.corn.FarmerBrown")
INFO - Jndi(name="java:global/schedule-methods/FarmerBrown")
INFO - Jndi(name="java:global/EjbModule2027722598/org.superbiz.corn.FarmerBrownTest!org.superbiz.corn.FarmerBrownTest")
INFO - Jndi(name="java:global/EjbModule2027722598/org.superbiz.corn.FarmerBrownTest")
INFO - Created Ejb(deployment-id=org.superbiz.corn.FarmerBrownTest, ejb-name=org.superbiz.corn.FarmerBrownTest, container=Default Managed Container)
INFO - Created Ejb(deployment-id=FarmerBrown, ejb-name=FarmerBrown, container=Default Singleton Container)
INFO - Started Ejb(deployment-id=org.superbiz.corn.FarmerBrownTest, ejb-name=org.superbiz.corn.FarmerBrownTest, container=Default Managed Container)
INFO - Started Ejb(deployment-id=FarmerBrown, ejb-name=FarmerBrown, container=Default Singleton Container)
INFO - Deployed Application(path=/Users/dblevins/examples/schedule-methods)
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 6.136 sec

Results :

Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

[INFO] 
[INFO] --- maven-jar-plugin:2.3.1:jar (default-jar) @ schedule-methods ---
[INFO] Building jar: /Users/dblevins/examples/schedule-methods/target/schedule-methods-1.0.jar
[INFO] 
[INFO] --- maven-install-plugin:2.3.1:install (default-install) @ schedule-methods ---
[INFO] Installing /Users/dblevins/examples/schedule-methods/target/schedule-methods-1.0.jar to /Users/dblevins/.m2/repository/org/superbiz/schedule-methods/1.0/schedule-methods-1.0.jar
[INFO] Installing /Users/dblevins/examples/schedule-methods/pom.xml to /Users/dblevins/.m2/repository/org/superbiz/schedule-methods/1.0/schedule-methods-1.0.pom
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 9.109s
[INFO] Finished at: Fri Oct 28 17:01:32 PDT 2011
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
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.corn;
    
    import javax.ejb.Lock;
    import javax.ejb.LockType;
    import javax.ejb.Schedule;
    import javax.ejb.Schedules;
    import javax.ejb.Singleton;
    import java.util.concurrent.atomic.AtomicInteger;
    
    /**
     * This is where we schedule all of Farmer Brown's corn jobs
     *
     * @version $Revision$ $Date$
     */
    @Singleton
    @Lock(LockType.READ) // allows timers to execute in parallel
    public class FarmerBrown {
    
        private final AtomicInteger checks = new AtomicInteger();
    
        @Schedules({
                @Schedule(month = "5", dayOfMonth = "20-Last", minute = "0", hour = "8"),
                @Schedule(month = "6", dayOfMonth = "1-10", minute = "0", hour = "8")
        })
        private void plantTheCorn() {
            // Dig out the planter!!!
        }
    
        @Schedules({
                @Schedule(month = "9", dayOfMonth = "20-Last", minute = "0", hour = "8"),
                @Schedule(month = "10", dayOfMonth = "1-10", minute = "0", hour = "8")
        })
        private void harvestTheCorn() {
            // Dig out the combine!!!
        }
    
        @Schedule(second = "*", minute = "*", hour = "*")
        private void checkOnTheDaughters() {
            checks.incrementAndGet();
        }
    
        public int getChecks() {
            return checks.get();
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
    package org.superbiz.corn;
    
    import junit.framework.TestCase;
    
    import javax.ejb.embeddable.EJBContainer;
    import javax.naming.Context;
    
    import static java.util.concurrent.TimeUnit.SECONDS;
    
    /**
     * @version $Revision$ $Date$
     */
    public class FarmerBrownTest extends TestCase {
    
        public void test() throws Exception {
    
            final Context context = EJBContainer.createEJBContainer().getContext();
    
            final FarmerBrown farmerBrown = (FarmerBrown) context.lookup("java:global/schedule-methods/FarmerBrown");
    
            // Give Farmer brown a chance to do some work
            Thread.sleep(SECONDS.toMillis(5));
    
            assertTrue(farmerBrown.getChecks() > 4);
        }
    }

[INFO] Scanning for projects...
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building OpenEJB :: Examples :: ScheduleExpression 1.0
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.4.1:clean (default-clean) @ schedule-expression ---
[INFO] Deleting /Users/dblevins/examples/schedule-expression/target
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:resources (default-resources) @ schedule-expression ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /Users/dblevins/examples/schedule-expression/src/main/resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:compile (default-compile) @ schedule-expression ---
[INFO] Compiling 1 source file to /Users/dblevins/examples/schedule-expression/target/classes
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:testResources (default-testResources) @ schedule-expression ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /Users/dblevins/examples/schedule-expression/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ schedule-expression ---
[INFO] Compiling 1 source file to /Users/dblevins/examples/schedule-expression/target/test-classes
[INFO] 
[INFO] --- maven-surefire-plugin:2.7.2:test (default-test) @ schedule-expression ---
[INFO] Surefire report directory: /Users/dblevins/examples/schedule-expression/target/surefire-reports

-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running org.superbiz.corn.FarmerBrownTest
Apache OpenEJB 4.0.0-beta-1    build: 20111002-04:06
http://openejb.apache.org/
INFO - openejb.home = /Users/dblevins/examples/schedule-expression
INFO - openejb.base = /Users/dblevins/examples/schedule-expression
INFO - Using 'javax.ejb.embeddable.EJBContainer=true'
INFO - Configuring Service(id=Default Security Service, type=SecurityService, provider-id=Default Security Service)
INFO - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
INFO - Found EjbModule in classpath: /Users/dblevins/examples/schedule-expression/target/classes
INFO - Beginning load: /Users/dblevins/examples/schedule-expression/target/classes
INFO - Configuring enterprise application: /Users/dblevins/examples/schedule-expression
WARN - Method 'lookup' is not available for 'javax.annotation.Resource'. Probably using an older Runtime.
INFO - Configuring Service(id=Default Singleton Container, type=Container, provider-id=Default Singleton Container)
INFO - Auto-creating a container for bean FarmerBrown: Container(type=SINGLETON, id=Default Singleton Container)
INFO - Configuring Service(id=Default Managed Container, type=Container, provider-id=Default Managed Container)
INFO - Auto-creating a container for bean org.superbiz.corn.FarmerBrownTest: Container(type=MANAGED, id=Default Managed Container)
INFO - Enterprise application "/Users/dblevins/examples/schedule-expression" loaded.
INFO - Assembling app: /Users/dblevins/examples/schedule-expression
INFO - Jndi(name="java:global/schedule-expression/FarmerBrown!org.superbiz.corn.FarmerBrown")
INFO - Jndi(name="java:global/schedule-expression/FarmerBrown")
INFO - Jndi(name="java:global/EjbModule181871104/org.superbiz.corn.FarmerBrownTest!org.superbiz.corn.FarmerBrownTest")
INFO - Jndi(name="java:global/EjbModule181871104/org.superbiz.corn.FarmerBrownTest")
INFO - Created Ejb(deployment-id=org.superbiz.corn.FarmerBrownTest, ejb-name=org.superbiz.corn.FarmerBrownTest, container=Default Managed Container)
INFO - Created Ejb(deployment-id=FarmerBrown, ejb-name=FarmerBrown, container=Default Singleton Container)
INFO - Started Ejb(deployment-id=org.superbiz.corn.FarmerBrownTest, ejb-name=org.superbiz.corn.FarmerBrownTest, container=Default Managed Container)
INFO - Started Ejb(deployment-id=FarmerBrown, ejb-name=FarmerBrown, container=Default Singleton Container)
INFO - Deployed Application(path=/Users/dblevins/examples/schedule-expression)
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 6.252 sec

Results :

Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

[INFO] 
[INFO] --- maven-jar-plugin:2.3.1:jar (default-jar) @ schedule-expression ---
[INFO] Building jar: /Users/dblevins/examples/schedule-expression/target/schedule-expression-1.0.jar
[INFO] 
[INFO] --- maven-install-plugin:2.3.1:install (default-install) @ schedule-expression ---
[INFO] Installing /Users/dblevins/examples/schedule-expression/target/schedule-expression-1.0.jar to /Users/dblevins/.m2/repository/org/superbiz/schedule-expression/1.0/schedule-expression-1.0.jar
[INFO] Installing /Users/dblevins/examples/schedule-expression/pom.xml to /Users/dblevins/.m2/repository/org/superbiz/schedule-expression/1.0/schedule-expression-1.0.pom
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 9.008s
[INFO] Finished at: Fri Oct 28 17:00:25 PDT 2011
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
    
    import javax.annotation.PostConstruct;
    import javax.annotation.Resource;
    import javax.ejb.Lock;
    import javax.ejb.LockType;
    import javax.ejb.ScheduleExpression;
    import javax.ejb.Singleton;
    import javax.ejb.Startup;
    import javax.ejb.Timeout;
    import javax.ejb.Timer;
    import javax.ejb.TimerConfig;
    import javax.ejb.TimerService;
    import java.util.concurrent.atomic.AtomicInteger;
    
    /**
     * This is where we schedule all of Farmer Brown's corn jobs
     *
     * @version $Revision$ $Date$
     */
    @Singleton
    @Lock(LockType.READ) // allows timers to execute in parallel
    @Startup
    public class FarmerBrown {
    
        private final AtomicInteger checks = new AtomicInteger();
    
        @Resource
        private TimerService timerService;
    
        @PostConstruct
        private void construct() {
            final TimerConfig plantTheCorn = new TimerConfig("plantTheCorn", false);
            timerService.createCalendarTimer(new ScheduleExpression().month(5).dayOfMonth("20-Last").minute(0).hour(8), plantTheCorn);
            timerService.createCalendarTimer(new ScheduleExpression().month(6).dayOfMonth("1-10").minute(0).hour(8), plantTheCorn);
    
            final TimerConfig harvestTheCorn = new TimerConfig("harvestTheCorn", false);
            timerService.createCalendarTimer(new ScheduleExpression().month(9).dayOfMonth("20-Last").minute(0).hour(8), harvestTheCorn);
            timerService.createCalendarTimer(new ScheduleExpression().month(10).dayOfMonth("1-10").minute(0).hour(8), harvestTheCorn);
    
            final TimerConfig checkOnTheDaughters = new TimerConfig("checkOnTheDaughters", false);
            timerService.createCalendarTimer(new ScheduleExpression().second("*").minute("*").hour("*"), checkOnTheDaughters);
        }
    
        @Timeout
        public void timeout(Timer timer) {
            if ("plantTheCorn".equals(timer.getInfo())) {
                plantTheCorn();
            } else if ("harvestTheCorn".equals(timer.getInfo())) {
                harvestTheCorn();
            } else if ("checkOnTheDaughters".equals(timer.getInfo())) {
                checkOnTheDaughters();
            }
        }
    
        private void plantTheCorn() {
            // Dig out the planter!!!
        }
    
        private void harvestTheCorn() {
            // Dig out the combine!!!
        }
    
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
    
            final FarmerBrown farmerBrown = (FarmerBrown) context.lookup("java:global/schedule-expression/FarmerBrown");
    
            // Give Farmer brown a chance to do some work
            Thread.sleep(SECONDS.toMillis(5));
    
            final int checks = farmerBrown.getChecks();
            assertTrue(checks+"", checks > 4);
        }
    }

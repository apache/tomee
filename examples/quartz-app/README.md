[INFO] Scanning for projects...
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Build Order:
[INFO] 
[INFO] OpenEJB :: Examples :: Quartz Application
[INFO] Quartz Resource Adapter
[INFO] Quartz Beans
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building OpenEJB :: Examples :: Quartz Application 1.0
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.4.1:clean (default-clean) @ quartz-app ---
[INFO] 
[INFO] --- maven-install-plugin:2.3.1:install (default-install) @ quartz-app ---
[INFO] Installing /Users/dblevins/examples/quartz-app/pom.xml to /Users/dblevins/.m2/repository/org/superbiz/quartz/quartz-app/1.0/quartz-app-1.0.pom
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building Quartz Resource Adapter 1.0
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.4.1:clean (default-clean) @ quartz-ra ---
[INFO] Deleting /Users/dblevins/examples/quartz-app/quartz-ra/target
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:resources (default-resources) @ quartz-ra ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Copying 1 resource
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:compile (default-compile) @ quartz-ra ---
[INFO] No sources to compile
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:testResources (default-testResources) @ quartz-ra ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /Users/dblevins/examples/quartz-app/quartz-ra/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ quartz-ra ---
[INFO] No sources to compile
[INFO] 
[INFO] --- maven-surefire-plugin:2.6:test (default-test) @ quartz-ra ---
[INFO] No tests to run.
[INFO] Surefire report directory: /Users/dblevins/examples/quartz-app/quartz-ra/target/surefire-reports

-------------------------------------------------------
 T E S T S
-------------------------------------------------------
There are no tests to run.

Results :

Tests run: 0, Failures: 0, Errors: 0, Skipped: 0

[INFO] 
[INFO] --- maven-jar-plugin:2.3.1:jar (default-jar) @ quartz-ra ---
[INFO] Building jar: /Users/dblevins/examples/quartz-app/quartz-ra/target/quartz-ra-1.0.jar
[INFO] 
[INFO] --- maven-install-plugin:2.3.1:install (default-install) @ quartz-ra ---
[INFO] Installing /Users/dblevins/examples/quartz-app/quartz-ra/target/quartz-ra-1.0.jar to /Users/dblevins/.m2/repository/org/superbiz/quartz/quartz-ra/1.0/quartz-ra-1.0.jar
[INFO] Installing /Users/dblevins/examples/quartz-app/quartz-ra/pom.xml to /Users/dblevins/.m2/repository/org/superbiz/quartz/quartz-ra/1.0/quartz-ra-1.0.pom
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building Quartz Beans 1.0
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.4.1:clean (default-clean) @ quartz-beans ---
[INFO] Deleting /Users/dblevins/examples/quartz-app/quartz-beans/target
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:resources (default-resources) @ quartz-beans ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Copying 1 resource
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:compile (default-compile) @ quartz-beans ---
[INFO] Compiling 3 source files to /Users/dblevins/examples/quartz-app/quartz-beans/target/classes
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:testResources (default-testResources) @ quartz-beans ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /Users/dblevins/examples/quartz-app/quartz-beans/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ quartz-beans ---
[INFO] Compiling 1 source file to /Users/dblevins/examples/quartz-app/quartz-beans/target/test-classes
[INFO] 
[INFO] --- maven-surefire-plugin:2.6:test (default-test) @ quartz-beans ---
[INFO] Surefire report directory: /Users/dblevins/examples/quartz-app/quartz-beans/target/surefire-reports

-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running org.superbiz.quartz.QuartzMdbTest
Apache OpenEJB 4.0.0-beta-1    build: 20111002-04:06
http://openejb.apache.org/
INFO - openejb.home = /Users/dblevins/examples/quartz-app/quartz-beans
INFO - openejb.base = /Users/dblevins/examples/quartz-app/quartz-beans
INFO - Configuring Service(id=Default Security Service, type=SecurityService, provider-id=Default Security Service)
INFO - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
INFO - Found ConnectorModule in classpath: /Users/dblevins/examples/quartz-app/quartz-ra/target/quartz-ra-1.0.jar
INFO - Found EjbModule in classpath: /Users/dblevins/examples/quartz-app/quartz-beans/target/classes
INFO - Beginning load: /Users/dblevins/examples/quartz-app/quartz-ra/target/quartz-ra-1.0.jar
INFO - Extracting jar: /Users/dblevins/examples/quartz-app/quartz-ra/target/quartz-ra-1.0.jar
INFO - Extracted path: /Users/dblevins/examples/quartz-app/quartz-ra/target/quartz-ra-1.0
INFO - Beginning load: /Users/dblevins/examples/quartz-app/quartz-beans/target/classes
INFO - Configuring enterprise application: /Users/dblevins/examples/quartz-app/quartz-beans/classpath.ear
INFO - Configuring Service(id=Default Stateless Container, type=Container, provider-id=Default Stateless Container)
INFO - Auto-creating a container for bean JobBean: Container(type=STATELESS, id=Default Stateless Container)
INFO - Configuring Service(id=QuartzResourceAdapter, type=Resource, provider-id=QuartzResourceAdapter)
INFO - Configuring Service(id=quartz-ra-1.0, type=Container, provider-id=Default MDB Container)
INFO - Enterprise application "/Users/dblevins/examples/quartz-app/quartz-beans/classpath.ear" loaded.
INFO - Assembling app: /Users/dblevins/examples/quartz-app/quartz-beans/classpath.ear
INFO - Jndi(name=JobBeanLocal) --> Ejb(deployment-id=JobBean)
INFO - Jndi(name=global/classpath.ear/quartz-beans/JobBean!org.superbiz.quartz.JobScheduler) --> Ejb(deployment-id=JobBean)
INFO - Jndi(name=global/classpath.ear/quartz-beans/JobBean) --> Ejb(deployment-id=JobBean)
INFO - Created Ejb(deployment-id=JobBean, ejb-name=JobBean, container=Default Stateless Container)
INFO - Created Ejb(deployment-id=QuartzMdb, ejb-name=QuartzMdb, container=quartz-ra-1.0)
Executing Job
INFO - Started Ejb(deployment-id=JobBean, ejb-name=JobBean, container=Default Stateless Container)
INFO - Started Ejb(deployment-id=QuartzMdb, ejb-name=QuartzMdb, container=quartz-ra-1.0)
INFO - Deployed Application(path=/Users/dblevins/examples/quartz-app/quartz-beans/classpath.ear)
This is a simple test job to get: MyJobValue
Scheduled test job should have run at: Fri Oct 28 17:05:12 PDT 2011
Executing Job
Executing Job
Executing Job
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 4.971 sec

Results :

Tests run: 2, Failures: 0, Errors: 0, Skipped: 0

[INFO] 
[INFO] --- maven-jar-plugin:2.3.1:jar (default-jar) @ quartz-beans ---
[INFO] Building jar: /Users/dblevins/examples/quartz-app/quartz-beans/target/quartz-beans-1.0.jar
[INFO] 
[INFO] --- maven-install-plugin:2.3.1:install (default-install) @ quartz-beans ---
[INFO] Installing /Users/dblevins/examples/quartz-app/quartz-beans/target/quartz-beans-1.0.jar to /Users/dblevins/.m2/repository/org/superbiz/quartz/quartz-beans/1.0/quartz-beans-1.0.jar
[INFO] Installing /Users/dblevins/examples/quartz-app/quartz-beans/pom.xml to /Users/dblevins/.m2/repository/org/superbiz/quartz/quartz-beans/1.0/quartz-beans-1.0.pom
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary:
[INFO] 
[INFO] OpenEJB :: Examples :: Quartz Application ......... SUCCESS [0.329s]
[INFO] Quartz Resource Adapter ........................... SUCCESS [1.687s]
[INFO] Quartz Beans ...................................... SUCCESS [6.211s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 8.345s
[INFO] Finished at: Fri Oct 28 17:05:15 PDT 2011
[INFO] Final Memory: 14M/81M
[INFO] ------------------------------------------------------------------------
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
    package org.superbiz.quartz;
    
    import org.apache.openejb.resource.quartz.QuartzResourceAdapter;
    import org.quartz.Job;
    import org.quartz.JobDetail;
    import org.quartz.JobExecutionContext;
    import org.quartz.JobExecutionException;
    import org.quartz.Scheduler;
    import org.quartz.SimpleTrigger;
    
    import javax.ejb.Stateless;
    import javax.naming.InitialContext;
    import java.util.Date;
    
    @Stateless
    public class JobBean implements JobScheduler {
    
        @Override
        public Date createJob() throws Exception {
    
            final QuartzResourceAdapter ra = (QuartzResourceAdapter) new InitialContext().lookup("java:openejb/Resource/QuartzResourceAdapter");
            final Scheduler s = ra.getScheduler();
    
            //Add a job type
            final JobDetail jd = new JobDetail("job1", "group1", JobBean.MyTestJob.class);
            jd.getJobDataMap().put("MyJobKey", "MyJobValue");
    
            //Schedule my 'test' job to run now
            final SimpleTrigger trigger = new SimpleTrigger("trigger1", "group1", new Date());
            return s.scheduleJob(jd, trigger);
        }
    
        public static class MyTestJob implements Job {
    
            @Override
            public void execute(JobExecutionContext context) throws JobExecutionException {
                System.out.println("This is a simple test job to get: " + context.getJobDetail().getJobDataMap().get("MyJobKey"));
            }
        }
    }
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
    package org.superbiz.quartz;
    
    import javax.ejb.Local;
    import java.util.Date;
    
    @Local
    public interface JobScheduler {
    
        Date createJob() throws Exception;
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
    package org.superbiz.quartz;
    
    import org.quartz.Job;
    import org.quartz.JobExecutionContext;
    import org.quartz.JobExecutionException;
    
    import javax.ejb.ActivationConfigProperty;
    import javax.ejb.MessageDriven;
    
    @MessageDriven(activationConfig = {
            @ActivationConfigProperty(propertyName = "cronExpression", propertyValue = "* * * * * ?")})
    public class QuartzMdb implements Job {
    
        @Override
        public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
            System.out.println("Executing Job");
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
    package org.superbiz.quartz;
    
    import org.junit.AfterClass;
    import org.junit.BeforeClass;
    import org.junit.Test;
    
    import javax.naming.Context;
    import javax.naming.InitialContext;
    import java.util.Date;
    import java.util.Properties;
    
    public class QuartzMdbTest {
    
        private static InitialContext initialContext = null;
    
        @BeforeClass
        public static void beforeClass() throws Exception {
    
            if (null == initialContext) {
                Properties properties = new Properties();
                properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
    
                initialContext = new InitialContext(properties);
            }
        }
    
        @AfterClass
        public static void afterClass() throws Exception {
            if (null != initialContext) {
                initialContext.close();
                initialContext = null;
            }
        }
    
        @Test
        public void testLookup() throws Exception {
    
            final JobScheduler jbi = (JobScheduler) initialContext.lookup("JobBeanLocal");
            final Date d = jbi.createJob();
            Thread.sleep(500);
            System.out.println("Scheduled test job should have run at: " + d.toString());
        }
    
        @Test
        public void testMdb() throws Exception {
            // Sleep 3 seconds and give quartz a chance to execute our MDB
            Thread.sleep(3000);
        }
    }

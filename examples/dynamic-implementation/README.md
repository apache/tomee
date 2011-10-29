[INFO] Scanning for projects...
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building OpenEJB :: Examples :: Dynamic Implementation 1.0
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.4.1:clean (default-clean) @ dynamic-implementation ---
[INFO] Deleting /Users/dblevins/examples/dynamic-implementation/target
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:resources (default-resources) @ dynamic-implementation ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /Users/dblevins/examples/dynamic-implementation/src/main/resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:compile (default-compile) @ dynamic-implementation ---
[INFO] Compiling 2 source files to /Users/dblevins/examples/dynamic-implementation/target/classes
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:testResources (default-testResources) @ dynamic-implementation ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /Users/dblevins/examples/dynamic-implementation/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ dynamic-implementation ---
[INFO] Compiling 1 source file to /Users/dblevins/examples/dynamic-implementation/target/test-classes
[INFO] 
[INFO] --- maven-surefire-plugin:2.7.2:test (default-test) @ dynamic-implementation ---
[INFO] Surefire report directory: /Users/dblevins/examples/dynamic-implementation/target/surefire-reports

-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running org.superbiz.dynamic.SocialTest
Apache OpenEJB 4.0.0-beta-1    build: 20111002-04:06
http://openejb.apache.org/
INFO - openejb.home = /Users/dblevins/examples/dynamic-implementation
INFO - openejb.base = /Users/dblevins/examples/dynamic-implementation
INFO - Using 'javax.ejb.embeddable.EJBContainer=true'
INFO - Configuring Service(id=Default Security Service, type=SecurityService, provider-id=Default Security Service)
INFO - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
INFO - Found EjbModule in classpath: /Users/dblevins/examples/dynamic-implementation/target/classes
INFO - Beginning load: /Users/dblevins/examples/dynamic-implementation/target/classes
INFO - Configuring enterprise application: /Users/dblevins/examples/dynamic-implementation
INFO - Configuring Service(id=Default Singleton Container, type=Container, provider-id=Default Singleton Container)
INFO - Auto-creating a container for bean SocialBean: Container(type=SINGLETON, id=Default Singleton Container)
INFO - Configuring Service(id=Default Managed Container, type=Container, provider-id=Default Managed Container)
INFO - Auto-creating a container for bean org.superbiz.dynamic.SocialTest: Container(type=MANAGED, id=Default Managed Container)
INFO - Enterprise application "/Users/dblevins/examples/dynamic-implementation" loaded.
INFO - Assembling app: /Users/dblevins/examples/dynamic-implementation
INFO - Jndi(name="java:global/dynamic-implementation/SocialBean!org.superbiz.dynamic.SocialBean")
INFO - Jndi(name="java:global/dynamic-implementation/SocialBean")
INFO - Jndi(name="java:global/EjbModule853068360/org.superbiz.dynamic.SocialTest!org.superbiz.dynamic.SocialTest")
INFO - Jndi(name="java:global/EjbModule853068360/org.superbiz.dynamic.SocialTest")
INFO - Created Ejb(deployment-id=org.superbiz.dynamic.SocialTest, ejb-name=org.superbiz.dynamic.SocialTest, container=Default Managed Container)
INFO - Created Ejb(deployment-id=SocialBean, ejb-name=SocialBean, container=Default Singleton Container)
INFO - Started Ejb(deployment-id=org.superbiz.dynamic.SocialTest, ejb-name=org.superbiz.dynamic.SocialTest, container=Default Managed Container)
INFO - Started Ejb(deployment-id=SocialBean, ejb-name=SocialBean, container=Default Singleton Container)
INFO - Deployed Application(path=/Users/dblevins/examples/dynamic-implementation)
INFO - Undeploying app: /Users/dblevins/examples/dynamic-implementation
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.012 sec

Results :

Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

[INFO] 
[INFO] --- maven-jar-plugin:2.3.1:jar (default-jar) @ dynamic-implementation ---
[INFO] Building jar: /Users/dblevins/examples/dynamic-implementation/target/dynamic-implementation-1.0.jar
[INFO] 
[INFO] --- maven-install-plugin:2.3.1:install (default-install) @ dynamic-implementation ---
[INFO] Installing /Users/dblevins/examples/dynamic-implementation/target/dynamic-implementation-1.0.jar to /Users/dblevins/.m2/repository/org/superbiz/dynamic-implementation/1.0/dynamic-implementation-1.0.jar
[INFO] Installing /Users/dblevins/examples/dynamic-implementation/pom.xml to /Users/dblevins/.m2/repository/org/superbiz/dynamic-implementation/1.0/dynamic-implementation-1.0.pom
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 3.841s
[INFO] Finished at: Fri Oct 28 17:05:26 PDT 2011
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
    package org.superbiz.dynamic;
    
    import org.apache.openejb.api.Proxy;
    
    import javax.ejb.Singleton;
    import javax.interceptor.Interceptors;
    
    /**
     * @author rmannibucau
     */
    @Singleton
    @Proxy(SocialHandler.class)
    @Interceptors(SocialInterceptor.class)
    public interface SocialBean {
        public String facebookStatus();
        public String twitterStatus();
        public String status();
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
    package org.superbiz.dynamic;
    
    import java.lang.reflect.InvocationHandler;
    import java.lang.reflect.Method;
    
    /**
     * @author rmannibucau
     */
    public class SocialHandler implements InvocationHandler {
        @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String mtd = method.getName();
            if (mtd.toLowerCase().contains("facebook")) {
                return "You think you have a life!";
            } else if (mtd.toLowerCase().contains("twitter")) {
                return "Wow, you eat pop corn!";
            }
            return "Hey, you have no virtual friend!";
        }
    }
    package org.superbiz.dynamic;
    
    import javax.interceptor.AroundInvoke;
    import javax.interceptor.InvocationContext;
    
    /**
     * @author rmannibucau
     */
    public class SocialInterceptor {
        @AroundInvoke public Object around(InvocationContext context) throws Exception {
            String mtd = context.getMethod().getName();
            String address;
            if (mtd.toLowerCase().contains("facebook")) {
                address = "http://www.facebook.com";
            } else if (mtd.toLowerCase().contains("twitter")) {
                address = "http://twitter.com";
            } else {
                address ="no website for you";
            }
    
            System.out.println("go on " + address);
            return context.proceed();
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
    package org.superbiz.dynamic;
    
    import org.junit.AfterClass;
    import org.junit.BeforeClass;
    import org.junit.Test;
    
    import javax.ejb.embeddable.EJBContainer;
    
    import static junit.framework.Assert.assertTrue;
    
    /**
     * @author rmannibucau
     */
    public class SocialTest {
        private static SocialBean social;
        private static EJBContainer container;
    
        @BeforeClass public static void init() throws Exception {
            container = EJBContainer.createEJBContainer();
            social = (SocialBean) container.getContext().lookup("java:global/dynamic-implementation/SocialBean");
        }
    
        @AfterClass public static void close() {
            container.close();
        }
    
        @Test public void simple() {
            assertTrue(social.facebookStatus().contains("think"));
            assertTrue(social.twitterStatus().contains("eat"));
            assertTrue(social.status().contains("virtual"));
        }
    }

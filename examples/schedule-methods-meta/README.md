[INFO] Scanning for projects...
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building OpenEJB :: Examples :: @Schedule Methods (Meta) 1.0
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.4.1:clean (default-clean) @ schedule-methods-meta ---
[INFO] Deleting /Users/dblevins/examples/schedule-methods-meta/target
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:resources (default-resources) @ schedule-methods-meta ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /Users/dblevins/examples/schedule-methods-meta/src/main/resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:compile (default-compile) @ schedule-methods-meta ---
[INFO] Compiling 10 source files to /Users/dblevins/examples/schedule-methods-meta/target/classes
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:testResources (default-testResources) @ schedule-methods-meta ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /Users/dblevins/examples/schedule-methods-meta/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ schedule-methods-meta ---
[INFO] Compiling 1 source file to /Users/dblevins/examples/schedule-methods-meta/target/test-classes
[INFO] 
[INFO] --- maven-surefire-plugin:2.7.2:test (default-test) @ schedule-methods-meta ---
[INFO] Surefire report directory: /Users/dblevins/examples/schedule-methods-meta/target/surefire-reports

-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running org.superbiz.corn.meta.FarmerBrownTest
Apache OpenEJB 4.0.0-beta-1    build: 20111002-04:06
http://openejb.apache.org/
INFO - openejb.home = /Users/dblevins/examples/schedule-methods-meta
INFO - openejb.base = /Users/dblevins/examples/schedule-methods-meta
INFO - Using 'javax.ejb.embeddable.EJBContainer=true'
INFO - Configuring Service(id=Default Security Service, type=SecurityService, provider-id=Default Security Service)
INFO - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
INFO - Found EjbModule in classpath: /Users/dblevins/examples/schedule-methods-meta/target/classes
INFO - Beginning load: /Users/dblevins/examples/schedule-methods-meta/target/classes
INFO - Configuring enterprise application: /Users/dblevins/examples/schedule-methods-meta
INFO - Configuring Service(id=Default Singleton Container, type=Container, provider-id=Default Singleton Container)
INFO - Auto-creating a container for bean FarmerBrown: Container(type=SINGLETON, id=Default Singleton Container)
INFO - Configuring Service(id=Default Managed Container, type=Container, provider-id=Default Managed Container)
INFO - Auto-creating a container for bean org.superbiz.corn.meta.FarmerBrownTest: Container(type=MANAGED, id=Default Managed Container)
INFO - Enterprise application "/Users/dblevins/examples/schedule-methods-meta" loaded.
INFO - Assembling app: /Users/dblevins/examples/schedule-methods-meta
INFO - Jndi(name="java:global/schedule-methods-meta/FarmerBrown!org.superbiz.corn.meta.FarmerBrown")
INFO - Jndi(name="java:global/schedule-methods-meta/FarmerBrown")
INFO - Jndi(name="java:global/EjbModule881708381/org.superbiz.corn.meta.FarmerBrownTest!org.superbiz.corn.meta.FarmerBrownTest")
INFO - Jndi(name="java:global/EjbModule881708381/org.superbiz.corn.meta.FarmerBrownTest")
INFO - Created Ejb(deployment-id=org.superbiz.corn.meta.FarmerBrownTest, ejb-name=org.superbiz.corn.meta.FarmerBrownTest, container=Default Managed Container)
INFO - Created Ejb(deployment-id=FarmerBrown, ejb-name=FarmerBrown, container=Default Singleton Container)
INFO - Started Ejb(deployment-id=org.superbiz.corn.meta.FarmerBrownTest, ejb-name=org.superbiz.corn.meta.FarmerBrownTest, container=Default Managed Container)
INFO - Started Ejb(deployment-id=FarmerBrown, ejb-name=FarmerBrown, container=Default Singleton Container)
INFO - Deployed Application(path=/Users/dblevins/examples/schedule-methods-meta)
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 6.135 sec

Results :

Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

[INFO] 
[INFO] --- maven-jar-plugin:2.3.1:jar (default-jar) @ schedule-methods-meta ---
[INFO] Building jar: /Users/dblevins/examples/schedule-methods-meta/target/schedule-methods-meta-1.0.jar
[INFO] 
[INFO] --- maven-install-plugin:2.3.1:install (default-install) @ schedule-methods-meta ---
[INFO] Installing /Users/dblevins/examples/schedule-methods-meta/target/schedule-methods-meta-1.0.jar to /Users/dblevins/.m2/repository/org/superbiz/schedule-methods-meta/1.0/schedule-methods-meta-1.0.jar
[INFO] Installing /Users/dblevins/examples/schedule-methods-meta/pom.xml to /Users/dblevins/.m2/repository/org/superbiz/schedule-methods-meta/1.0/schedule-methods-meta-1.0.pom
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 9.070s
[INFO] Finished at: Fri Oct 28 17:06:54 PDT 2011
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
    package org.superbiz.corn.meta.api;
    
    import javax.ejb.Schedule;
    import java.lang.annotation.ElementType;
    import java.lang.annotation.Retention;
    import java.lang.annotation.RetentionPolicy;
    import java.lang.annotation.Target;
    
    @Metatype
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    
    public @interface BiAnnually {
        public static interface $ {
    
            @BiAnnually
            @Schedule(second = "0", minute = "0", hour = "0", dayOfMonth = "1", month = "1,6")
            public void method();
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
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.corn.meta.api;
    
    import javax.ejb.Schedule;
    import java.lang.annotation.ElementType;
    import java.lang.annotation.Retention;
    import java.lang.annotation.RetentionPolicy;
    import java.lang.annotation.Target;
    
    @Metatype
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    
    public @interface BiMonthly {
        public static interface $ {
    
            @BiMonthly
            @Schedule(second = "0", minute = "0", hour = "0", dayOfMonth = "1,15")
            public void method();
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
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.corn.meta.api;
    
    import javax.ejb.Schedule;
    import java.lang.annotation.ElementType;
    import java.lang.annotation.Retention;
    import java.lang.annotation.RetentionPolicy;
    import java.lang.annotation.Target;
    
    @Metatype
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    
    public @interface Daily {
        public static interface $ {
    
            @Daily
            @Schedule(second = "0", minute = "0", hour = "0", dayOfMonth = "*")
            public void method();
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
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.corn.meta.api;
    
    import javax.ejb.Schedule;
    import javax.ejb.Schedules;
    import java.lang.annotation.ElementType;
    import java.lang.annotation.Retention;
    import java.lang.annotation.RetentionPolicy;
    import java.lang.annotation.Target;
    
    @Metatype
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    
    public @interface HarvestTime {
        public static interface $ {
    
            @HarvestTime
            @Schedules({
                    @Schedule(month = "9", dayOfMonth = "20-Last", minute = "0", hour = "8"),
                    @Schedule(month = "10", dayOfMonth = "1-10", minute = "0", hour = "8")
            })
            public void method();
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
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.corn.meta.api;
    
    import javax.ejb.Schedule;
    import java.lang.annotation.ElementType;
    import java.lang.annotation.Retention;
    import java.lang.annotation.RetentionPolicy;
    import java.lang.annotation.Target;
    
    @Metatype
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    
    public @interface Hourly {
        public static interface $ {
    
            @Hourly
            @Schedule(second = "0", minute = "0", hour = "*")
            public void method();
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
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.corn.meta.api;
    
    import java.lang.annotation.ElementType;
    import java.lang.annotation.Retention;
    import java.lang.annotation.RetentionPolicy;
    import java.lang.annotation.Target;
    
    @Metatype
    @Target(ElementType.ANNOTATION_TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Metatype {
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
    package org.superbiz.corn.meta.api;
    
    import javax.ejb.Lock;
    import javax.ejb.LockType;
    import javax.ejb.Singleton;
    import javax.ejb.TransactionAttribute;
    import javax.ejb.TransactionAttributeType;
    import java.lang.annotation.ElementType;
    import java.lang.annotation.Retention;
    import java.lang.annotation.RetentionPolicy;
    import java.lang.annotation.Target;
    
    @Metatype
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    
    @Singleton
    @Lock(LockType.READ)
    public @interface Organic {
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
    package org.superbiz.corn.meta.api;
    
    import javax.ejb.Schedule;
    import javax.ejb.Schedules;
    import java.lang.annotation.ElementType;
    import java.lang.annotation.Retention;
    import java.lang.annotation.RetentionPolicy;
    import java.lang.annotation.Target;
    
    @Metatype
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    
    public @interface PlantingTime {
        public static interface $ {
    
            @PlantingTime
            @Schedules({
                    @Schedule(month = "5", dayOfMonth = "20-Last", minute = "0", hour = "8"),
                    @Schedule(month = "6", dayOfMonth = "1-10", minute = "0", hour = "8")
            })
            public void method();
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
    package org.superbiz.corn.meta.api;
    
    import javax.ejb.Schedule;
    import javax.ejb.Schedules;
    import java.lang.annotation.ElementType;
    import java.lang.annotation.Retention;
    import java.lang.annotation.RetentionPolicy;
    import java.lang.annotation.Target;
    
    @Metatype
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    
    public @interface Secondly {
        public static interface $ {
    
            @Secondly
            @Schedule(second = "*", minute = "*", hour = "*")
            public void method();
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
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.corn.meta;
    
    import org.superbiz.corn.meta.api.HarvestTime;
    import org.superbiz.corn.meta.api.Organic;
    import org.superbiz.corn.meta.api.PlantingTime;
    import org.superbiz.corn.meta.api.Secondly;
    
    import java.util.concurrent.atomic.AtomicInteger;
    
    /**
     * This is where we schedule all of Farmer Brown's corn jobs
     *
     * @version $Revision$ $Date$
     */
    @Organic
    public class FarmerBrown {
    
        private final AtomicInteger checks = new AtomicInteger();
    
        @PlantingTime
        private void plantTheCorn() {
            // Dig out the planter!!!
        }
    
        @HarvestTime
        private void harvestTheCorn() {
            // Dig out the combine!!!
        }
    
        @Secondly
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
    package org.superbiz.corn.meta;
    
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
    
            final FarmerBrown farmerBrown = (FarmerBrown) context.lookup("java:global/schedule-methods-meta/FarmerBrown");
    
            // Give Farmer brown a chance to do some work
            Thread.sleep(SECONDS.toMillis(5));
    
            assertTrue(farmerBrown.getChecks() > 4);
        }
    }

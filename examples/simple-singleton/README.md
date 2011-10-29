[INFO] Scanning for projects...
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building OpenEJB :: Examples :: Simple Singleton 1.0
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.4.1:clean (default-clean) @ simple-singleton ---
[INFO] Deleting /Users/dblevins/examples/simple-singleton/target
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:resources (default-resources) @ simple-singleton ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /Users/dblevins/examples/simple-singleton/src/main/resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:compile (default-compile) @ simple-singleton ---
[INFO] Compiling 2 source files to /Users/dblevins/examples/simple-singleton/target/classes
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:testResources (default-testResources) @ simple-singleton ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /Users/dblevins/examples/simple-singleton/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ simple-singleton ---
[INFO] Compiling 2 source files to /Users/dblevins/examples/simple-singleton/target/test-classes
[INFO] 
[INFO] --- maven-surefire-plugin:2.7.2:test (default-test) @ simple-singleton ---
[INFO] Surefire report directory: /Users/dblevins/examples/simple-singleton/target/surefire-reports

-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running org.superbiz.registry.ComponentRegistryTest
Apache OpenEJB 4.0.0-beta-1    build: 20111002-04:06
http://openejb.apache.org/
INFO - openejb.home = /Users/dblevins/examples/simple-singleton
INFO - openejb.base = /Users/dblevins/examples/simple-singleton
INFO - Using 'javax.ejb.embeddable.EJBContainer=true'
INFO - Configuring Service(id=Default Security Service, type=SecurityService, provider-id=Default Security Service)
INFO - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
INFO - Found EjbModule in classpath: /Users/dblevins/examples/simple-singleton/target/classes
INFO - Beginning load: /Users/dblevins/examples/simple-singleton/target/classes
INFO - Configuring enterprise application: /Users/dblevins/examples/simple-singleton
INFO - Configuring Service(id=Default Singleton Container, type=Container, provider-id=Default Singleton Container)
INFO - Auto-creating a container for bean ComponentRegistry: Container(type=SINGLETON, id=Default Singleton Container)
INFO - Configuring Service(id=Default Managed Container, type=Container, provider-id=Default Managed Container)
INFO - Auto-creating a container for bean org.superbiz.registry.ComponentRegistryTest: Container(type=MANAGED, id=Default Managed Container)
INFO - Enterprise application "/Users/dblevins/examples/simple-singleton" loaded.
INFO - Assembling app: /Users/dblevins/examples/simple-singleton
INFO - Jndi(name="java:global/simple-singleton/ComponentRegistry!org.superbiz.registry.ComponentRegistry")
INFO - Jndi(name="java:global/simple-singleton/ComponentRegistry")
INFO - Jndi(name="java:global/simple-singleton/PropertyRegistry!org.superbiz.registry.PropertyRegistry")
INFO - Jndi(name="java:global/simple-singleton/PropertyRegistry")
INFO - Jndi(name="java:global/EjbModule709424757/org.superbiz.registry.ComponentRegistryTest!org.superbiz.registry.ComponentRegistryTest")
INFO - Jndi(name="java:global/EjbModule709424757/org.superbiz.registry.ComponentRegistryTest")
INFO - Created Ejb(deployment-id=org.superbiz.registry.ComponentRegistryTest, ejb-name=org.superbiz.registry.ComponentRegistryTest, container=Default Managed Container)
INFO - Created Ejb(deployment-id=PropertyRegistry, ejb-name=PropertyRegistry, container=Default Singleton Container)
INFO - Created Ejb(deployment-id=ComponentRegistry, ejb-name=ComponentRegistry, container=Default Singleton Container)
INFO - Started Ejb(deployment-id=org.superbiz.registry.ComponentRegistryTest, ejb-name=org.superbiz.registry.ComponentRegistryTest, container=Default Managed Container)
INFO - Started Ejb(deployment-id=PropertyRegistry, ejb-name=PropertyRegistry, container=Default Singleton Container)
INFO - Started Ejb(deployment-id=ComponentRegistry, ejb-name=ComponentRegistry, container=Default Singleton Container)
INFO - Deployed Application(path=/Users/dblevins/examples/simple-singleton)
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.11 sec
Running org.superbiz.registry.PropertiesRegistryTest
INFO - EJBContainer already initialized.  Call ejbContainer.close() to allow reinitialization
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.005 sec

Results :

Tests run: 2, Failures: 0, Errors: 0, Skipped: 0

[INFO] 
[INFO] --- maven-jar-plugin:2.3.1:jar (default-jar) @ simple-singleton ---
[INFO] Building jar: /Users/dblevins/examples/simple-singleton/target/simple-singleton-1.0.jar
[INFO] 
[INFO] --- maven-install-plugin:2.3.1:install (default-install) @ simple-singleton ---
[INFO] Installing /Users/dblevins/examples/simple-singleton/target/simple-singleton-1.0.jar to /Users/dblevins/.m2/repository/org/superbiz/simple-singleton/1.0/simple-singleton-1.0.jar
[INFO] Installing /Users/dblevins/examples/simple-singleton/pom.xml to /Users/dblevins/.m2/repository/org/superbiz/simple-singleton/1.0/simple-singleton-1.0.pom
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 4.013s
[INFO] Finished at: Fri Oct 28 17:10:34 PDT 2011
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
    package org.superbiz.registry;
    
    //START SNIPPET: code
    
    import javax.ejb.Lock;
    import javax.ejb.Singleton;
    import java.util.ArrayList;
    import java.util.Collection;
    import java.util.HashMap;
    import java.util.Map;
    
    import static javax.ejb.LockType.READ;
    import static javax.ejb.LockType.WRITE;
    
    @Singleton
    @Lock(READ)
    public class ComponentRegistry {
    
        private final Map<Class, Object> components = new HashMap<Class, Object>();
    
        public <T> T getComponent(Class<T> type) {
            return (T) components.get(type);
        }
    
        public Collection<?> getComponents() {
            return new ArrayList(components.values());
        }
    
        @Lock(WRITE)
        public <T> T setComponent(Class<T> type, T value) {
            return (T) components.put(type, value);
        }
    
        @Lock(WRITE)
        public <T> T removeComponent(Class<T> type) {
            return (T) components.remove(type);
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
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.registry;
    
    //START SNIPPET: code
    
    import javax.annotation.PostConstruct;
    import javax.annotation.PreDestroy;
    import javax.ejb.ConcurrencyManagement;
    import javax.ejb.Singleton;
    import javax.ejb.Startup;
    import java.util.Properties;
    
    import static javax.ejb.ConcurrencyManagementType.BEAN;
    
    @Singleton
    @Startup
    @ConcurrencyManagement(BEAN)
    public class PropertyRegistry {
    
        // Note the java.util.Properties object is a thread-safe
        // collections that uses synchronization.  If it didn't
        // you would have to use some form of synchronization
        // to ensure the PropertyRegistryBean is thread-safe.
        private final Properties properties = new Properties();
    
        // The @Startup method ensures that this method is
        // called when the application starts up.
        @PostConstruct
        public void applicationStartup() {
            properties.putAll(System.getProperties());
        }
    
        @PreDestroy
        public void applicationShutdown() {
            properties.clear();
        }
    
        public String getProperty(String key) {
            return properties.getProperty(key);
        }
    
        public String setProperty(String key, String value) {
            return (String) properties.setProperty(key, value);
        }
    
        public String removeProperty(String key) {
            return (String) properties.remove(key);
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
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.registry;
    
    import junit.framework.TestCase;
    
    import javax.ejb.embeddable.EJBContainer;
    import javax.naming.Context;
    import java.net.URI;
    import java.util.Date;
    
    //START SNIPPET: code
    public class ComponentRegistryTest extends TestCase {
    
        public void test() throws Exception {
    
            final Context context = EJBContainer.createEJBContainer().getContext();
    
            // Both references below will point to the exact same instance
            ComponentRegistry one = (ComponentRegistry) context.lookup("java:global/simple-singleton/ComponentRegistry");
    
            ComponentRegistry two = (ComponentRegistry) context.lookup("java:global/simple-singleton/ComponentRegistry");
    
    
            // Let's prove both references point to the same instance
    
    
            // Set a URL into 'one' and retrieve it from 'two'
    
            URI expectedUri = new URI("foo://bar/baz");
    
            one.setComponent(URI.class, expectedUri);
    
            URI actualUri = two.getComponent(URI.class);
    
            assertSame(expectedUri, actualUri);
    
    
            // Set a Date into 'two' and retrieve it from 'one'
    
            Date expectedDate = new Date();
    
            two.setComponent(Date.class, expectedDate);
    
            Date actualDate = one.getComponent(Date.class);
    
            assertSame(expectedDate, actualDate);
    
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
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.registry;
    
    import junit.framework.TestCase;
    
    import javax.ejb.embeddable.EJBContainer;
    import javax.naming.Context;
    
    public class PropertiesRegistryTest extends TestCase {
    
        public void test() throws Exception {
    
            Context context = EJBContainer.createEJBContainer().getContext();
    
            PropertyRegistry one = (PropertyRegistry) context.lookup("java:global/simple-singleton/PropertyRegistry");
    
            PropertyRegistry two = (PropertyRegistry) context.lookup("java:global/simple-singleton/PropertyRegistry");
    
    
            one.setProperty("url", "http://superbiz.org");
    
            String url = two.getProperty("url");
    
            assertEquals("http://superbiz.org", url);
    
    
            two.setProperty("version", "1.0.5");
    
            String version = one.getProperty("version");
    
            assertEquals("1.0.5", version);
    
        }
    }

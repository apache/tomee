[INFO] Scanning for projects...
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building OpenEJB :: Examples :: @EJB Injection 1.0
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.4.1:clean (default-clean) @ injection-of-ejbs ---
[INFO] Deleting /Users/dblevins/examples/injection-of-ejbs/target
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:resources (default-resources) @ injection-of-ejbs ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /Users/dblevins/examples/injection-of-ejbs/src/main/resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:compile (default-compile) @ injection-of-ejbs ---
[INFO] Compiling 4 source files to /Users/dblevins/examples/injection-of-ejbs/target/classes
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:testResources (default-testResources) @ injection-of-ejbs ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /Users/dblevins/examples/injection-of-ejbs/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ injection-of-ejbs ---
[INFO] Compiling 1 source file to /Users/dblevins/examples/injection-of-ejbs/target/test-classes
[INFO] 
[INFO] --- maven-surefire-plugin:2.7.2:test (default-test) @ injection-of-ejbs ---
[INFO] Surefire report directory: /Users/dblevins/examples/injection-of-ejbs/target/surefire-reports

-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running org.superbiz.injection.EjbDependencyTest
Apache OpenEJB 4.0.0-beta-1    build: 20111002-04:06
http://openejb.apache.org/
INFO - openejb.home = /Users/dblevins/examples/injection-of-ejbs
INFO - openejb.base = /Users/dblevins/examples/injection-of-ejbs
INFO - Using 'javax.ejb.embeddable.EJBContainer=true'
INFO - Configuring Service(id=Default Security Service, type=SecurityService, provider-id=Default Security Service)
INFO - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
INFO - Found EjbModule in classpath: /Users/dblevins/examples/injection-of-ejbs/target/classes
INFO - Beginning load: /Users/dblevins/examples/injection-of-ejbs/target/classes
INFO - Configuring enterprise application: /Users/dblevins/examples/injection-of-ejbs
INFO - Configuring Service(id=Default Stateless Container, type=Container, provider-id=Default Stateless Container)
INFO - Auto-creating a container for bean DataReader: Container(type=STATELESS, id=Default Stateless Container)
INFO - Configuring Service(id=Default Managed Container, type=Container, provider-id=Default Managed Container)
INFO - Auto-creating a container for bean org.superbiz.injection.EjbDependencyTest: Container(type=MANAGED, id=Default Managed Container)
INFO - Enterprise application "/Users/dblevins/examples/injection-of-ejbs" loaded.
INFO - Assembling app: /Users/dblevins/examples/injection-of-ejbs
INFO - Jndi(name="java:global/injection-of-ejbs/DataReader!org.superbiz.injection.DataReader")
INFO - Jndi(name="java:global/injection-of-ejbs/DataReader")
INFO - Jndi(name="java:global/injection-of-ejbs/DataStore!org.superbiz.injection.DataStore")
INFO - Jndi(name="java:global/injection-of-ejbs/DataStore!org.superbiz.injection.DataStoreLocal")
INFO - Jndi(name="java:global/injection-of-ejbs/DataStore!org.superbiz.injection.DataStoreRemote")
INFO - Jndi(name="java:global/injection-of-ejbs/DataStore")
INFO - Jndi(name="java:global/EjbModule1110828771/org.superbiz.injection.EjbDependencyTest!org.superbiz.injection.EjbDependencyTest")
INFO - Jndi(name="java:global/EjbModule1110828771/org.superbiz.injection.EjbDependencyTest")
INFO - Created Ejb(deployment-id=DataReader, ejb-name=DataReader, container=Default Stateless Container)
INFO - Created Ejb(deployment-id=DataStore, ejb-name=DataStore, container=Default Stateless Container)
INFO - Created Ejb(deployment-id=org.superbiz.injection.EjbDependencyTest, ejb-name=org.superbiz.injection.EjbDependencyTest, container=Default Managed Container)
INFO - Started Ejb(deployment-id=DataReader, ejb-name=DataReader, container=Default Stateless Container)
INFO - Started Ejb(deployment-id=DataStore, ejb-name=DataStore, container=Default Stateless Container)
INFO - Started Ejb(deployment-id=org.superbiz.injection.EjbDependencyTest, ejb-name=org.superbiz.injection.EjbDependencyTest, container=Default Managed Container)
INFO - Deployed Application(path=/Users/dblevins/examples/injection-of-ejbs)
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.146 sec

Results :

Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

[INFO] 
[INFO] --- maven-jar-plugin:2.3.1:jar (default-jar) @ injection-of-ejbs ---
[INFO] Building jar: /Users/dblevins/examples/injection-of-ejbs/target/injection-of-ejbs-1.0.jar
[INFO] 
[INFO] --- maven-install-plugin:2.3.1:install (default-install) @ injection-of-ejbs ---
[INFO] Installing /Users/dblevins/examples/injection-of-ejbs/target/injection-of-ejbs-1.0.jar to /Users/dblevins/.m2/repository/org/superbiz/injection-of-ejbs/1.0/injection-of-ejbs-1.0.jar
[INFO] Installing /Users/dblevins/examples/injection-of-ejbs/pom.xml to /Users/dblevins/.m2/repository/org/superbiz/injection-of-ejbs/1.0/injection-of-ejbs-1.0.pom
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 3.961s
[INFO] Finished at: Fri Oct 28 17:01:42 PDT 2011
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
    package org.superbiz.injection;
    
    import javax.ejb.EJB;
    import javax.ejb.Stateless;
    
    /**
     * This is an EJB 3.1 style pojo stateless session bean
     * Every stateless session bean implementation must be annotated
     * using the annotation @Stateless
     * This EJB has 2 business interfaces: DataReaderRemote, a remote business
     * interface, and DataReaderLocal, a local business interface
     * <p/>
     * The instance variables 'dataStoreRemote' is annotated with the @EJB annotation:
     * this means that the application server, at runtime, will inject in this instance
     * variable a reference to the EJB DataStoreRemote
     * <p/>
     * The instance variables 'dataStoreLocal' is annotated with the @EJB annotation:
     * this means that the application server, at runtime, will inject in this instance
     * variable a reference to the EJB DataStoreLocal
     */
    //START SNIPPET: code
    @Stateless
    public class DataReader {
    
        @EJB
        private DataStoreRemote dataStoreRemote;
        @EJB
        private DataStoreLocal dataStoreLocal;
        @EJB
        private DataStore dataStore;
    
        public String readDataFromLocalStore() {
            return "LOCAL:" + dataStoreLocal.getData();
        }
    
        public String readDataFromLocalBeanStore() {
            return "LOCALBEAN:" + dataStore.getData();
        }
    
        public String readDataFromRemoteStore() {
            return "REMOTE:" + dataStoreRemote.getData();
        }
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
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     */
    package org.superbiz.injection;
    
    import javax.ejb.LocalBean;
    import javax.ejb.Stateless;
    
    /**
     * This is an EJB 3 style pojo stateless session bean
     * Every stateless session bean implementation must be annotated
     * using the annotation @Stateless
     * This EJB has 2 business interfaces: DataStoreRemote, a remote business
     * interface, and DataStoreLocal, a local business interface
     */
    //START SNIPPET: code
    @Stateless
    @LocalBean
    public class DataStore implements DataStoreLocal, DataStoreRemote {
    
        public String getData() {
            return "42";
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
    package org.superbiz.injection;
    
    import javax.ejb.Local;
    
    /**
     * This is an EJB 3 local business interface
     * A local business interface may be annotated with the @Local
     * annotation, but it's optional. A business interface which is
     * not annotated with @Local or @Remote is assumed to be Local
     */
    //START SNIPPET: code
    @Local
    public interface DataStoreLocal {
    
        public String getData();
    
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
    package org.superbiz.injection;
    
    import javax.ejb.Remote;
    
    /**
     * This is an EJB 3 remote business interface
     * A remote business interface must be annotated with the @Remote
     * annotation
     */
    //START SNIPPET: code
    @Remote
    public interface DataStoreRemote {
    
        public String getData();
    
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
    package org.superbiz.injection;
    
    import junit.framework.TestCase;
    
    import javax.ejb.embeddable.EJBContainer;
    import javax.naming.Context;
    
    /**
     * A test case for DataReaderImpl ejb, testing both the remote and local interface
     */
    //START SNIPPET: code
    public class EjbDependencyTest extends TestCase {
    
        public void test() throws Exception {
            final Context context = EJBContainer.createEJBContainer().getContext();
    
            DataReader dataReader = (DataReader) context.lookup("java:global/injection-of-ejbs/DataReader");
    
            assertNotNull(dataReader);
    
            assertEquals("LOCAL:42", dataReader.readDataFromLocalStore());
            assertEquals("REMOTE:42", dataReader.readDataFromRemoteStore());
            assertEquals("LOCALBEAN:42", dataReader.readDataFromLocalBeanStore());
        }
    }
    //END SNIPPET: code

[INFO] Scanning for projects...
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building OpenEJB :: Examples :: CDI-Disposes 1.0
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.4.1:clean (default-clean) @ cdi-produces-disposes ---
[INFO] Deleting /Users/dblevins/examples/cdi-produces-disposes/target
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:resources (default-resources) @ cdi-produces-disposes ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Copying 1 resource
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:compile (default-compile) @ cdi-produces-disposes ---
[INFO] Compiling 7 source files to /Users/dblevins/examples/cdi-produces-disposes/target/classes
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:testResources (default-testResources) @ cdi-produces-disposes ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /Users/dblevins/examples/cdi-produces-disposes/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ cdi-produces-disposes ---
[INFO] Compiling 1 source file to /Users/dblevins/examples/cdi-produces-disposes/target/test-classes
[INFO] 
[INFO] --- maven-surefire-plugin:2.7.2:test (default-test) @ cdi-produces-disposes ---
[INFO] Surefire report directory: /Users/dblevins/examples/cdi-produces-disposes/target/surefire-reports

-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running org.superbiz.cdi.produces.disposes.LoggerTest
Apache OpenEJB 4.0.0-beta-1    build: 20111002-04:06
http://openejb.apache.org/
INFO - openejb.home = /Users/dblevins/examples/cdi-produces-disposes
INFO - openejb.base = /Users/dblevins/examples/cdi-produces-disposes
INFO - Using 'javax.ejb.embeddable.EJBContainer=true'
INFO - Configuring Service(id=Default Security Service, type=SecurityService, provider-id=Default Security Service)
INFO - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
INFO - Found EjbModule in classpath: /Users/dblevins/examples/cdi-produces-disposes/target/classes
INFO - Beginning load: /Users/dblevins/examples/cdi-produces-disposes/target/classes
INFO - Configuring enterprise application: /Users/dblevins/examples/cdi-produces-disposes
INFO - Configuring Service(id=Default Managed Container, type=Container, provider-id=Default Managed Container)
INFO - Auto-creating a container for bean cdi-produces-disposes.Comp: Container(type=MANAGED, id=Default Managed Container)
INFO - Enterprise application "/Users/dblevins/examples/cdi-produces-disposes" loaded.
INFO - Assembling app: /Users/dblevins/examples/cdi-produces-disposes
INFO - Jndi(name="java:global/cdi-produces-disposes/cdi-produces-disposes.Comp!org.apache.openejb.BeanContext$Comp")
INFO - Jndi(name="java:global/cdi-produces-disposes/cdi-produces-disposes.Comp")
INFO - Jndi(name="java:global/EjbModule1847652919/org.superbiz.cdi.produces.disposes.LoggerTest!org.superbiz.cdi.produces.disposes.LoggerTest")
INFO - Jndi(name="java:global/EjbModule1847652919/org.superbiz.cdi.produces.disposes.LoggerTest")
INFO - Created Ejb(deployment-id=cdi-produces-disposes.Comp, ejb-name=cdi-produces-disposes.Comp, container=Default Managed Container)
INFO - Created Ejb(deployment-id=org.superbiz.cdi.produces.disposes.LoggerTest, ejb-name=org.superbiz.cdi.produces.disposes.LoggerTest, container=Default Managed Container)
INFO - Started Ejb(deployment-id=cdi-produces-disposes.Comp, ejb-name=cdi-produces-disposes.Comp, container=Default Managed Container)
INFO - Started Ejb(deployment-id=org.superbiz.cdi.produces.disposes.LoggerTest, ejb-name=org.superbiz.cdi.produces.disposes.LoggerTest, container=Default Managed Container)
INFO - Deployed Application(path=/Users/dblevins/examples/cdi-produces-disposes)
##### Handler: @Produces created DatabaseHandler!, Writing to the database!
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.05 sec

Results :

Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

[INFO] 
[INFO] --- maven-jar-plugin:2.3.1:jar (default-jar) @ cdi-produces-disposes ---
[INFO] Building jar: /Users/dblevins/examples/cdi-produces-disposes/target/cdi-produces-disposes-1.0.jar
[INFO] 
[INFO] --- maven-install-plugin:2.3.1:install (default-install) @ cdi-produces-disposes ---
[INFO] Installing /Users/dblevins/examples/cdi-produces-disposes/target/cdi-produces-disposes-1.0.jar to /Users/dblevins/.m2/repository/org/superbiz/cdi-produces-disposes/1.0/cdi-produces-disposes-1.0.jar
[INFO] Installing /Users/dblevins/examples/cdi-produces-disposes/pom.xml to /Users/dblevins/.m2/repository/org/superbiz/cdi-produces-disposes/1.0/cdi-produces-disposes-1.0.pom
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 4.030s
[INFO] Finished at: Fri Oct 28 17:02:18 PDT 2011
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
    package org.superbiz.cdi.produces.disposes;
    
    public class ConsoleHandler implements LogHandler {
    
        private String name;
    
        public ConsoleHandler (String name) {
            this.name = name;
        }
    
        @Override
        public String getName() {
            return name;
        }
    
        @Override
        public void writeLog(String s) {
            System.out.printf("##### Handler: %s, Writing to the console!\n", getName());
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
    package org.superbiz.cdi.produces.disposes;
    
    public class DatabaseHandler implements LogHandler {
    
        private String name;
    
        public DatabaseHandler (String name) {
            this.name = name;
        }
    
        @Override
        public String getName() {
            return name;
        }
    
        @Override
        public void writeLog(String s) {
            System.out.printf("##### Handler: %s, Writing to the database!\n", getName());
            // Use connection to write log to database
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
    package org.superbiz.cdi.produces.disposes;
    
    public class FileHandler implements LogHandler {
    
        private String name;
    
        public FileHandler (String name) {
            this.name = name;
        }
    
        @Override
        public String getName() {
            return name;
        }
    
        @Override
        public void writeLog(String s) {
            System.out.printf("##### Handler: %s, Writing to the file!\n", getName());
            // Write to log file
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
    package org.superbiz.cdi.produces.disposes;
    
    import javax.enterprise.inject.Disposes;
    import javax.enterprise.inject.Produces;
    
    public class LogFactory {
    
        private int type = 2;
    
        @Produces
        public LogHandler getLogHandler() {
            switch (type) {
                case 1:
                    return new FileHandler("@Produces created FileHandler!");
                case 2:
                    return new DatabaseHandler("@Produces created DatabaseHandler!");
                case 3:
                default:
                    return new ConsoleHandler("@Produces created ConsoleHandler!");
            }
    
        }
    
        public void closeLogHandler (@Disposes LogHandler handler) {
            switch (type) {
                case 1:
                    System.out.println("Closing File handler!");
                    break;
                case 2:
                    System.out.println("Closing DB handler!");
                    break;
                case 3:
                default:
                    System.out.println("Closing Console handler!");
            }
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
    package org.superbiz.cdi.produces.disposes;
    
    public interface Logger {
    
        public void log (String s);
    
        public LogHandler getHandler();
    
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
    package org.superbiz.cdi.produces.disposes;
    
    import javax.inject.Inject;
    import javax.inject.Named;
    
    @Named("logger")
    public class LoggerImpl implements Logger {
    
        @Inject
        private LogHandler handler;
    
        @Override
        public void log(String s) {
            getHandler().writeLog(s);
        }
    
        public LogHandler getHandler() {
            return handler;
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
    package org.superbiz.cdi.produces.disposes;
    
    public interface LogHandler {
    
        public String getName();
    
        public void writeLog(String s);
    
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
    package org.superbiz.cdi.produces.disposes;
    
    import org.junit.After;
    import org.junit.Before;
    import org.junit.Test;
    
    import javax.ejb.embeddable.EJBContainer;
    import javax.inject.Inject;
    import javax.naming.Context;
    
    import static junit.framework.Assert.assertNotNull;
    import static org.junit.Assert.assertFalse;
    import static org.junit.Assert.assertTrue;
    
    public class LoggerTest {
    
        @Inject
        Logger logger;
    
        private Context ctxt;
    
        @Before
        public void setUp() {
            try {
                ctxt = EJBContainer.createEJBContainer().getContext();
                ctxt.bind("inject", this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    
        @After
        public void cleanUp() {
            try {
                ctxt.unbind("inject");
                ctxt.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    
        @Test
        public void testLogHandler() {
            assertNotNull(logger);
            assertFalse("Handler should not be a ConsoleHandler", logger.getHandler() instanceof ConsoleHandler);
            assertFalse("Handler should not be a FileHandler", logger.getHandler() instanceof FileHandler);
            assertTrue("Handler should be a DatabaseHandler", logger.getHandler() instanceof DatabaseHandler);
            logger.log("##### Testing write\n");
            logger = null;
        }
    
    
    }

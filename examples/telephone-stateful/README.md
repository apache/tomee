[INFO] Scanning for projects...
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building OpenEJB :: Examples :: Telephone Stateful Pojo 1.0
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.4.1:clean (default-clean) @ telephone-stateful ---
[INFO] Deleting /Users/dblevins/examples/telephone-stateful/target
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:resources (default-resources) @ telephone-stateful ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Copying 1 resource
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:compile (default-compile) @ telephone-stateful ---
[INFO] Compiling 2 source files to /Users/dblevins/examples/telephone-stateful/target/classes
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:testResources (default-testResources) @ telephone-stateful ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /Users/dblevins/examples/telephone-stateful/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ telephone-stateful ---
[INFO] Compiling 1 source file to /Users/dblevins/examples/telephone-stateful/target/test-classes
[INFO] 
[INFO] --- maven-surefire-plugin:2.7.2:test (default-test) @ telephone-stateful ---
[INFO] Surefire report directory: /Users/dblevins/examples/telephone-stateful/target/surefire-reports

-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running org.superbiz.telephone.TelephoneTest
Apache OpenEJB 4.0.0-beta-1    build: 20111002-04:06
http://openejb.apache.org/
INFO - openejb.home = /Users/dblevins/examples/telephone-stateful
INFO - openejb.base = /Users/dblevins/examples/telephone-stateful
INFO - Configuring Service(id=Default Security Service, type=SecurityService, provider-id=Default Security Service)
INFO - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
INFO - Found EjbModule in classpath: /Users/dblevins/examples/telephone-stateful/target/classes
INFO - Beginning load: /Users/dblevins/examples/telephone-stateful/target/classes
INFO - Configuring enterprise application: /Users/dblevins/examples/telephone-stateful/classpath.ear
INFO - Configuring Service(id=Default Stateful Container, type=Container, provider-id=Default Stateful Container)
INFO - Auto-creating a container for bean TelephoneBean: Container(type=STATEFUL, id=Default Stateful Container)
INFO - Enterprise application "/Users/dblevins/examples/telephone-stateful/classpath.ear" loaded.
INFO - Assembling app: /Users/dblevins/examples/telephone-stateful/classpath.ear
INFO - Jndi(name=TelephoneBeanRemote) --> Ejb(deployment-id=TelephoneBean)
INFO - Jndi(name=global/classpath.ear/telephone-stateful/TelephoneBean!org.superbiz.telephone.Telephone) --> Ejb(deployment-id=TelephoneBean)
INFO - Jndi(name=global/classpath.ear/telephone-stateful/TelephoneBean) --> Ejb(deployment-id=TelephoneBean)
INFO - Created Ejb(deployment-id=TelephoneBean, ejb-name=TelephoneBean, container=Default Stateful Container)
INFO - Started Ejb(deployment-id=TelephoneBean, ejb-name=TelephoneBean, container=Default Stateful Container)
INFO - Deployed Application(path=/Users/dblevins/examples/telephone-stateful/classpath.ear)
INFO - Initializing network services
INFO - Creating ServerService(id=admin)
INFO - Creating ServerService(id=ejbd)
INFO - Creating ServerService(id=ejbds)
INFO - Initializing network services
  ** Starting Services **
  NAME                 IP              PORT  
  admin thread         127.0.0.1       4200  
  ejbd                 127.0.0.1       4201  
  ejbd                 127.0.0.1       4203  
-------
Ready!
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.412 sec

Results :

Tests run: 2, Failures: 0, Errors: 0, Skipped: 0

[INFO] 
[INFO] --- maven-jar-plugin:2.3.1:jar (default-jar) @ telephone-stateful ---
[INFO] Building jar: /Users/dblevins/examples/telephone-stateful/target/telephone-stateful-1.0.jar
[INFO] 
[INFO] --- maven-install-plugin:2.3.1:install (default-install) @ telephone-stateful ---
[INFO] Installing /Users/dblevins/examples/telephone-stateful/target/telephone-stateful-1.0.jar to /Users/dblevins/.m2/repository/org/superbiz/telephone-stateful/1.0/telephone-stateful-1.0.jar
[INFO] Installing /Users/dblevins/examples/telephone-stateful/pom.xml to /Users/dblevins/.m2/repository/org/superbiz/telephone-stateful/1.0/telephone-stateful-1.0.pom
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 4.641s
[INFO] Finished at: Fri Oct 28 17:02:08 PDT 2011
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
    //START SNIPPET: code
    package org.superbiz.telephone;
    
    public interface Telephone {
    
        void speak(String words);
    
        String listen();
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
    //START SNIPPET: code
    package org.superbiz.telephone;
    
    import javax.ejb.Remote;
    import javax.ejb.Stateful;
    import java.util.ArrayList;
    import java.util.List;
    
    @Remote
    @Stateful
    public class TelephoneBean implements Telephone {
    
        private static final String[] answers = {
                "How nice.",
                "Oh, of course.",
                "Interesting.",
                "Really?",
                "No.",
                "Definitely.",
                "I wondered about that.",
                "Good idea.",
                "You don't say!",
        };
    
        private List<String> conversation = new ArrayList<String>();
    
        public void speak(String words) {
            conversation.add(words);
        }
    
        public String listen() {
            if (conversation.size() == 0) {
                return "Nothing has been said";
            }
    
            String lastThingSaid = conversation.get(conversation.size() - 1);
            return answers[Math.abs(lastThingSaid.hashCode()) % answers.length];
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
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.telephone;
    
    import junit.framework.TestCase;
    
    import javax.naming.Context;
    import javax.naming.InitialContext;
    import java.util.Properties;
    
    /**
     * @version $Rev: 1090810 $ $Date: 2011-04-10 07:49:26 -0700 (Sun, 10 Apr 2011) $
     */
    public class TelephoneTest extends TestCase {
    
        //START SNIPPET: setup
    
        protected void setUp() throws Exception {
            Properties properties = new Properties();
            properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
            properties.setProperty("openejb.embedded.remotable", "true");
            // Uncomment these properties to change the defaults
            //properties.setProperty("ejbd.port", "4202");
            //properties.setProperty("ejbd.bind", "localhost");
            //properties.setProperty("ejbd.threads", "200");
            //properties.setProperty("ejbd.disabled", "false");
            //properties.setProperty("ejbd.only_from", "127.0.0.1,192.168.1.1");
    
            new InitialContext(properties);
        }
        //END SNIPPET: setup
    
        /**
         * Lookup the Telephone bean via its remote interface but using the LocalInitialContextFactory
         *
         * @throws Exception
         */
        //START SNIPPET: localcontext
        public void testTalkOverLocalNetwork() throws Exception {
    
            Properties properties = new Properties();
            properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
            InitialContext localContext = new InitialContext(properties);
    
            Telephone telephone = (Telephone) localContext.lookup("TelephoneBeanRemote");
    
            telephone.speak("Did you know I am talking directly through the embedded container?");
    
            assertEquals("Interesting.", telephone.listen());
    
    
            telephone.speak("Yep, I'm using the bean's remote interface but since the ejb container is embedded " +
                    "in the same vm I'm just using the LocalInitialContextFactory.");
    
            assertEquals("Really?", telephone.listen());
    
    
            telephone.speak("Right, you really only have to use the RemoteInitialContextFactory if you're in a different vm.");
    
            assertEquals("Oh, of course.", telephone.listen());
        }
        //END SNIPPET: localcontext
    
        /**
         * Lookup the Telephone bean via its remote interface using the RemoteInitialContextFactory
         *
         * @throws Exception
         */
        //START SNIPPET: remotecontext
        public void testTalkOverRemoteNetwork() throws Exception {
            Properties properties = new Properties();
            properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.RemoteInitialContextFactory");
            properties.setProperty(Context.PROVIDER_URL, "ejbd://localhost:4201");
            InitialContext remoteContext = new InitialContext(properties);
    
            Telephone telephone = (Telephone) remoteContext.lookup("TelephoneBeanRemote");
    
            telephone.speak("Is this a local call?");
    
            assertEquals("No.", telephone.listen());
    
    
            telephone.speak("This would be a lot cooler if I was connecting from another VM then, huh?");
    
            assertEquals("I wondered about that.", telephone.listen());
    
    
            telephone.speak("I suppose I should hangup and call back over the LocalInitialContextFactory.");
    
            assertEquals("Good idea.", telephone.listen());
    
    
            telephone.speak("I'll remember this though in case I ever have to call you accross a network.");
    
            assertEquals("Definitely.", telephone.listen());
        }
        //END SNIPPET: remotecontext
    
    }

[INFO] Scanning for projects...
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building OpenEJB :: Examples :: EJB 2.1 Component Interfaces 1.0
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.4.1:clean (default-clean) @ component-interfaces ---
[INFO] Deleting /Users/dblevins/examples/component-interfaces/target
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:resources (default-resources) @ component-interfaces ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /Users/dblevins/examples/component-interfaces/src/main/resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:compile (default-compile) @ component-interfaces ---
[INFO] Compiling 7 source files to /Users/dblevins/examples/component-interfaces/target/classes
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:testResources (default-testResources) @ component-interfaces ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /Users/dblevins/examples/component-interfaces/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ component-interfaces ---
[INFO] Compiling 1 source file to /Users/dblevins/examples/component-interfaces/target/test-classes
[INFO] 
[INFO] --- maven-surefire-plugin:2.6:test (default-test) @ component-interfaces ---
[INFO] Surefire report directory: /Users/dblevins/examples/component-interfaces/target/surefire-reports

-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running org.superbiz.FriendlyPersonTest
Apache OpenEJB 4.0.0-beta-1    build: 20111002-04:06
http://openejb.apache.org/
INFO - openejb.home = /Users/dblevins/examples/component-interfaces
INFO - openejb.base = /Users/dblevins/examples/component-interfaces
INFO - Using 'javax.ejb.embeddable.EJBContainer=true'
INFO - Configuring Service(id=Default Security Service, type=SecurityService, provider-id=Default Security Service)
INFO - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
INFO - Found EjbModule in classpath: /Users/dblevins/examples/component-interfaces/target/classes
INFO - Beginning load: /Users/dblevins/examples/component-interfaces/target/classes
INFO - Configuring enterprise application: /Users/dblevins/examples/component-interfaces
INFO - Configuring Service(id=Default Stateful Container, type=Container, provider-id=Default Stateful Container)
INFO - Auto-creating a container for bean FriendlyPerson: Container(type=STATEFUL, id=Default Stateful Container)
INFO - Configuring Service(id=Default Managed Container, type=Container, provider-id=Default Managed Container)
INFO - Auto-creating a container for bean org.superbiz.FriendlyPersonTest: Container(type=MANAGED, id=Default Managed Container)
INFO - Enterprise application "/Users/dblevins/examples/component-interfaces" loaded.
INFO - Assembling app: /Users/dblevins/examples/component-interfaces
INFO - Jndi(name="java:global/component-interfaces/FriendlyPerson!org.superbiz.FriendlyPersonLocal")
INFO - Jndi(name="java:global/component-interfaces/FriendlyPerson!org.superbiz.FriendlyPersonRemote")
INFO - Jndi(name="java:global/component-interfaces/FriendlyPerson!org.superbiz.FriendlyPersonEjbLocalHome")
INFO - Jndi(name="java:global/component-interfaces/FriendlyPerson!org.superbiz.FriendlyPersonEjbHome")
INFO - Jndi(name="java:global/component-interfaces/FriendlyPerson")
INFO - Jndi(name="java:global/EjbModule1952576819/org.superbiz.FriendlyPersonTest!org.superbiz.FriendlyPersonTest")
INFO - Jndi(name="java:global/EjbModule1952576819/org.superbiz.FriendlyPersonTest")
INFO - Created Ejb(deployment-id=FriendlyPerson, ejb-name=FriendlyPerson, container=Default Stateful Container)
INFO - Created Ejb(deployment-id=org.superbiz.FriendlyPersonTest, ejb-name=org.superbiz.FriendlyPersonTest, container=Default Managed Container)
INFO - Started Ejb(deployment-id=FriendlyPerson, ejb-name=FriendlyPerson, container=Default Stateful Container)
INFO - Started Ejb(deployment-id=org.superbiz.FriendlyPersonTest, ejb-name=org.superbiz.FriendlyPersonTest, container=Default Managed Container)
INFO - Deployed Application(path=/Users/dblevins/examples/component-interfaces)
INFO - EJBContainer already initialized.  Call ejbContainer.close() to allow reinitialization
INFO - EJBContainer already initialized.  Call ejbContainer.close() to allow reinitialization
INFO - EJBContainer already initialized.  Call ejbContainer.close() to allow reinitialization
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.324 sec

Results :

Tests run: 4, Failures: 0, Errors: 0, Skipped: 0

[INFO] 
[INFO] --- maven-jar-plugin:2.3.1:jar (default-jar) @ component-interfaces ---
[INFO] Building jar: /Users/dblevins/examples/component-interfaces/target/component-interfaces-1.0.jar
[INFO] 
[INFO] --- maven-install-plugin:2.3.1:install (default-install) @ component-interfaces ---
[INFO] Installing /Users/dblevins/examples/component-interfaces/target/component-interfaces-1.0.jar to /Users/dblevins/.m2/repository/org/superbiz/component-interfaces/1.0/component-interfaces-1.0.jar
[INFO] Installing /Users/dblevins/examples/component-interfaces/pom.xml to /Users/dblevins/.m2/repository/org/superbiz/component-interfaces/1.0/component-interfaces-1.0.pom
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 4.240s
[INFO] Finished at: Fri Oct 28 16:59:20 PDT 2011
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
    package org.superbiz;
    
    import javax.ejb.Init;
    import javax.ejb.Local;
    import javax.ejb.LocalHome;
    import javax.ejb.Remote;
    import javax.ejb.RemoteHome;
    import javax.ejb.Remove;
    import javax.ejb.Stateful;
    import java.text.MessageFormat;
    import java.util.HashMap;
    import java.util.Locale;
    import java.util.Properties;
    
    /**
     * This is an EJB 3 style pojo stateful session bean
     * it does not need to implement javax.ejb.SessionBean
     *
     * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
     */
    //START SNIPPET: code
    
    // EJB 3.0 Style business interfaces
    // Each of these interfaces are already annotated in the classes
    // themselves with @Remote and @Local, so annotating them here
    // in the bean class again is not really required.
    @Remote({FriendlyPersonRemote.class})
    @Local({FriendlyPersonLocal.class})
    
    // EJB 2.1 Style component interfaces
    // These interfaces, however, must be annotated here in the bean class.
    // Use of @RemoteHome in the FriendlyPersonEjbHome class itself is not allowed.
    // Use of @LocalHome in the FriendlyPersonEjbLocalHome class itself is also not allowed.
    @RemoteHome(FriendlyPersonEjbHome.class)
    @LocalHome(FriendlyPersonEjbLocalHome.class)
    
    @Stateful
    public class FriendlyPerson implements FriendlyPersonLocal, FriendlyPersonRemote {
    
        private final HashMap<String, MessageFormat> greetings;
        private final Properties languagePreferences;
    
        private String defaultLanguage;
    
        public FriendlyPerson() {
            greetings = new HashMap();
            languagePreferences = new Properties();
            defaultLanguage = Locale.getDefault().getLanguage();
    
            addGreeting("en", "Hello {0}!");
            addGreeting("es", "Hola {0}!");
            addGreeting("fr", "Bonjour {0}!");
            addGreeting("pl", "Witaj {0}!");
        }
    
        /**
         * This method corresponds to the FriendlyPersonEjbHome.create() method
         * and the FriendlyPersonEjbLocalHome.create()
         * <p/>
         * If you do not have an EJBHome or EJBLocalHome interface, this method
         * can be deleted.
         */
        @Init
        public void create() {
        }
    
        /**
         * This method corresponds to the following methods:
         * - EJBObject.remove()
         * - EJBHome.remove(ejbObject)
         * - EJBLocalObject.remove()
         * - EJBLocalHome.remove(ejbObject)
         * <p/>
         * If you do not have an EJBHome or EJBLocalHome interface, this method
         * can be deleted.
         */
        @Remove
        public void remove() {
        }
    
        public String greet(String friend) {
            String language = languagePreferences.getProperty(friend, defaultLanguage);
            return greet(language, friend);
        }
    
        public String greet(String language, String friend) {
            MessageFormat greeting = greetings.get(language);
            if (greeting == null) {
                Locale locale = new Locale(language);
                return "Sorry, I don't speak " + locale.getDisplayLanguage() + ".";
            }
    
            return greeting.format(new Object[]{friend});
        }
    
        public void addGreeting(String language, String message) {
            greetings.put(language, new MessageFormat(message));
        }
    
        public void setLanguagePreferences(String friend, String language) {
            languagePreferences.put(friend, language);
        }
    
        public String getDefaultLanguage() {
            return defaultLanguage;
        }
    
        public void setDefaultLanguage(String defaultLanguage) {
            this.defaultLanguage = defaultLanguage;
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
    package org.superbiz;
    
    //START SNIPPET: code
    
    import javax.ejb.CreateException;
    import javax.ejb.EJBHome;
    import java.rmi.RemoteException;
    
    public interface FriendlyPersonEjbHome extends EJBHome {
        FriendlyPersonEjbObject create() throws CreateException, RemoteException;
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
    package org.superbiz;
    
    //START SNIPPET: code
    
    import javax.ejb.CreateException;
    import javax.ejb.EJBLocalHome;
    import java.rmi.RemoteException;
    
    public interface FriendlyPersonEjbLocalHome extends EJBLocalHome {
        FriendlyPersonEjbLocalObject create() throws CreateException, RemoteException;
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
    package org.superbiz;
    
    import javax.ejb.EJBLocalObject;
    
    public interface FriendlyPersonEjbLocalObject extends EJBLocalObject {
        String greet(String friend);
    
        String greet(String language, String friend);
    
        void addGreeting(String language, String message);
    
        void setLanguagePreferences(String friend, String language);
    
        String getDefaultLanguage();
    
        void setDefaultLanguage(String defaultLanguage);
    
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
    package org.superbiz;
    
    //START SNIPPET: code
    
    import javax.ejb.EJBObject;
    import java.rmi.RemoteException;
    
    public interface FriendlyPersonEjbObject extends EJBObject {
        String greet(String friend) throws RemoteException;
    
        String greet(String language, String friend) throws RemoteException;
    
        void addGreeting(String language, String message) throws RemoteException;
    
        void setLanguagePreferences(String friend, String language) throws RemoteException;
    
        String getDefaultLanguage() throws RemoteException;
    
        void setDefaultLanguage(String defaultLanguage) throws RemoteException;
    
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
    package org.superbiz;
    
    //START SNIPPET: code
    
    import javax.ejb.Local;
    
    @Local
    public interface FriendlyPersonLocal {
        String greet(String friend);
    
        String greet(String language, String friend);
    
        void addGreeting(String language, String message);
    
        void setLanguagePreferences(String friend, String language);
    
        String getDefaultLanguage();
    
        void setDefaultLanguage(String defaultLanguage);
    
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
    package org.superbiz;
    
    import javax.ejb.Remote;
    
    //START SNIPPET: code
    @Remote
    public interface FriendlyPersonRemote {
        String greet(String friend);
    
        String greet(String language, String friend);
    
        void addGreeting(String language, String message);
    
        void setLanguagePreferences(String friend, String language);
    
        String getDefaultLanguage();
    
        void setDefaultLanguage(String defaultLanguage);
    
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
    package org.superbiz;
    
    import junit.framework.TestCase;
    
    import javax.ejb.embeddable.EJBContainer;
    import javax.naming.Context;
    import java.util.Locale;
    
    /**
     * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
     * @version $Rev: 1090810 $ $Date: 2011-04-10 07:49:26 -0700 (Sun, 10 Apr 2011) $
     */
    public class FriendlyPersonTest extends TestCase {
    
        private Context context;
    
        protected void setUp() throws Exception {
            context = EJBContainer.createEJBContainer().getContext();
        }
    
        /**
         * Here we lookup and test the FriendlyPerson bean via its EJB 2.1 EJBHome and EJBObject interfaces
         *
         * @throws Exception
         */
        //START SNIPPET: remotehome
        public void testEjbHomeAndEjbObject() throws Exception {
            Object object = context.lookup("java:global/component-interfaces/FriendlyPerson!org.superbiz.FriendlyPersonEjbHome");
            FriendlyPersonEjbHome home = (FriendlyPersonEjbHome) object;
            FriendlyPersonEjbObject friendlyPerson = home.create();
    
            friendlyPerson.setDefaultLanguage("en");
    
            assertEquals("Hello David!", friendlyPerson.greet("David"));
            assertEquals("Hello Amelia!", friendlyPerson.greet("Amelia"));
    
            friendlyPerson.setLanguagePreferences("Amelia", "es");
    
            assertEquals("Hello David!", friendlyPerson.greet("David"));
            assertEquals("Hola Amelia!", friendlyPerson.greet("Amelia"));
    
            // Amelia took some French, let's see if she remembers
            assertEquals("Bonjour Amelia!", friendlyPerson.greet("fr", "Amelia"));
    
            // Dave should take some Polish and if he had, he could say Hi in Polish
            assertEquals("Witaj Dave!", friendlyPerson.greet("pl", "Dave"));
    
            // Let's see if I speak Portuguese
            assertEquals("Sorry, I don't speak " + new Locale("pt").getDisplayLanguage() + ".", friendlyPerson.greet("pt", "David"));
    
            // Ok, well I've been meaning to learn, so...
            friendlyPerson.addGreeting("pt", "Ola {0}!");
    
            assertEquals("Ola David!", friendlyPerson.greet("pt", "David"));
        }
        //END SNIPPET: remotehome
    
    
        /**
         * Here we lookup and test the FriendlyPerson bean via its EJB 2.1 EJBLocalHome and EJBLocalObject interfaces
         *
         * @throws Exception
         */
        public void testEjbLocalHomeAndEjbLocalObject() throws Exception {
            Object object = context.lookup("java:global/component-interfaces/FriendlyPerson!org.superbiz.FriendlyPersonEjbLocalHome");
            FriendlyPersonEjbLocalHome home = (FriendlyPersonEjbLocalHome) object;
            FriendlyPersonEjbLocalObject friendlyPerson = home.create();
    
            friendlyPerson.setDefaultLanguage("en");
    
            assertEquals("Hello David!", friendlyPerson.greet("David"));
            assertEquals("Hello Amelia!", friendlyPerson.greet("Amelia"));
    
            friendlyPerson.setLanguagePreferences("Amelia", "es");
    
            assertEquals("Hello David!", friendlyPerson.greet("David"));
            assertEquals("Hola Amelia!", friendlyPerson.greet("Amelia"));
    
            // Amelia took some French, let's see if she remembers
            assertEquals("Bonjour Amelia!", friendlyPerson.greet("fr", "Amelia"));
    
            // Dave should take some Polish and if he had, he could say Hi in Polish
            assertEquals("Witaj Dave!", friendlyPerson.greet("pl", "Dave"));
    
            // Let's see if I speak Portuguese
            assertEquals("Sorry, I don't speak " + new Locale("pt").getDisplayLanguage() + ".", friendlyPerson.greet("pt", "David"));
    
            // Ok, well I've been meaning to learn, so...
            friendlyPerson.addGreeting("pt", "Ola {0}!");
    
            assertEquals("Ola David!", friendlyPerson.greet("pt", "David"));
        }
    
        /**
         * Here we lookup and test the FriendlyPerson bean via its EJB 3.0 business remote interface
         *
         * @throws Exception
         */
        //START SNIPPET: remote
        public void testBusinessRemote() throws Exception {
            Object object = context.lookup("java:global/component-interfaces/FriendlyPerson!org.superbiz.FriendlyPersonRemote");
    
            FriendlyPersonRemote friendlyPerson = (FriendlyPersonRemote) object;
    
            friendlyPerson.setDefaultLanguage("en");
    
            assertEquals("Hello David!", friendlyPerson.greet("David"));
            assertEquals("Hello Amelia!", friendlyPerson.greet("Amelia"));
    
            friendlyPerson.setLanguagePreferences("Amelia", "es");
    
            assertEquals("Hello David!", friendlyPerson.greet("David"));
            assertEquals("Hola Amelia!", friendlyPerson.greet("Amelia"));
    
            // Amelia took some French, let's see if she remembers
            assertEquals("Bonjour Amelia!", friendlyPerson.greet("fr", "Amelia"));
    
            // Dave should take some Polish and if he had, he could say Hi in Polish
            assertEquals("Witaj Dave!", friendlyPerson.greet("pl", "Dave"));
    
            // Let's see if I speak Portuguese
            assertEquals("Sorry, I don't speak " + new Locale("pt").getDisplayLanguage() + ".", friendlyPerson.greet("pt", "David"));
    
            // Ok, well I've been meaning to learn, so...
            friendlyPerson.addGreeting("pt", "Ola {0}!");
    
            assertEquals("Ola David!", friendlyPerson.greet("pt", "David"));
        }
        //START SNIPPET: remote
    
        /**
         * Here we lookup and test the FriendlyPerson bean via its EJB 3.0 business local interface
         *
         * @throws Exception
         */
        public void testBusinessLocal() throws Exception {
            Object object = context.lookup("java:global/component-interfaces/FriendlyPerson!org.superbiz.FriendlyPersonLocal");
    
            FriendlyPersonLocal friendlyPerson = (FriendlyPersonLocal) object;
    
            friendlyPerson.setDefaultLanguage("en");
    
            assertEquals("Hello David!", friendlyPerson.greet("David"));
            assertEquals("Hello Amelia!", friendlyPerson.greet("Amelia"));
    
            friendlyPerson.setLanguagePreferences("Amelia", "es");
    
            assertEquals("Hello David!", friendlyPerson.greet("David"));
            assertEquals("Hola Amelia!", friendlyPerson.greet("Amelia"));
    
            // Amelia took some French, let's see if she remembers
            assertEquals("Bonjour Amelia!", friendlyPerson.greet("fr", "Amelia"));
    
            // Dave should take some Polish and if he had, he could say Hi in Polish
            assertEquals("Witaj Dave!", friendlyPerson.greet("pl", "Dave"));
    
            // Let's see if I speak Portuguese
            assertEquals("Sorry, I don't speak " + new Locale("pt").getDisplayLanguage() + ".", friendlyPerson.greet("pt", "David"));
    
            // Ok, well I've been meaning to learn, so...
            friendlyPerson.addGreeting("pt", "Ola {0}!");
    
            assertEquals("Ola David!", friendlyPerson.greet("pt", "David"));
        }
    
    
    }

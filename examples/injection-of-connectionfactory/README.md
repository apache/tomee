[INFO] Scanning for projects...
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building OpenEJB :: Examples :: @Resource javax.jms.ConnectionFactory 1.0
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.4.1:clean (default-clean) @ injection-of-connectionfactory ---
[INFO] Deleting /Users/dblevins/examples/injection-of-connectionfactory/target
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:resources (default-resources) @ injection-of-connectionfactory ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /Users/dblevins/examples/injection-of-connectionfactory/src/main/resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:compile (default-compile) @ injection-of-connectionfactory ---
[INFO] Compiling 1 source file to /Users/dblevins/examples/injection-of-connectionfactory/target/classes
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:testResources (default-testResources) @ injection-of-connectionfactory ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /Users/dblevins/examples/injection-of-connectionfactory/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ injection-of-connectionfactory ---
[INFO] Compiling 1 source file to /Users/dblevins/examples/injection-of-connectionfactory/target/test-classes
[INFO] 
[INFO] --- maven-surefire-plugin:2.7.2:test (default-test) @ injection-of-connectionfactory ---
[INFO] Surefire report directory: /Users/dblevins/examples/injection-of-connectionfactory/target/surefire-reports

-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running org.superbiz.injection.jms.MessagingBeanTest
Apache OpenEJB 4.0.0-beta-1    build: 20111002-04:06
http://openejb.apache.org/
INFO - openejb.home = /Users/dblevins/examples/injection-of-connectionfactory
INFO - openejb.base = /Users/dblevins/examples/injection-of-connectionfactory
INFO - Using 'javax.ejb.embeddable.EJBContainer=true'
INFO - Configuring Service(id=Default Security Service, type=SecurityService, provider-id=Default Security Service)
INFO - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
INFO - Found EjbModule in classpath: /Users/dblevins/examples/injection-of-connectionfactory/target/classes
INFO - Beginning load: /Users/dblevins/examples/injection-of-connectionfactory/target/classes
INFO - Configuring enterprise application: /Users/dblevins/examples/injection-of-connectionfactory
WARN - Method 'lookup' is not available for 'javax.annotation.Resource'. Probably using an older Runtime.
INFO - Configuring Service(id=Default Stateless Container, type=Container, provider-id=Default Stateless Container)
INFO - Auto-creating a container for bean Messages: Container(type=STATELESS, id=Default Stateless Container)
INFO - Configuring Service(id=Default JMS Connection Factory, type=Resource, provider-id=Default JMS Connection Factory)
INFO - Auto-creating a Resource with id 'Default JMS Connection Factory' of type 'javax.jms.ConnectionFactory for 'Messages'.
INFO - Configuring Service(id=Default JMS Resource Adapter, type=Resource, provider-id=Default JMS Resource Adapter)
INFO - Auto-linking resource-ref 'java:comp/env/org.superbiz.injection.jms.Messages/connectionFactory' in bean Messages to Resource(id=Default JMS Connection Factory)
INFO - Configuring Service(id=org.superbiz.injection.jms.Messages/chatQueue, type=Resource, provider-id=Default Queue)
INFO - Auto-creating a Resource with id 'org.superbiz.injection.jms.Messages/chatQueue' of type 'javax.jms.Queue for 'Messages'.
INFO - Auto-linking resource-env-ref 'java:comp/env/org.superbiz.injection.jms.Messages/chatQueue' in bean Messages to Resource(id=org.superbiz.injection.jms.Messages/chatQueue)
INFO - Configuring Service(id=Default Managed Container, type=Container, provider-id=Default Managed Container)
INFO - Auto-creating a container for bean org.superbiz.injection.jms.MessagingBeanTest: Container(type=MANAGED, id=Default Managed Container)
INFO - Enterprise application "/Users/dblevins/examples/injection-of-connectionfactory" loaded.
INFO - Assembling app: /Users/dblevins/examples/injection-of-connectionfactory
INFO - Jndi(name="java:global/injection-of-connectionfactory/Messages!org.superbiz.injection.jms.Messages")
INFO - Jndi(name="java:global/injection-of-connectionfactory/Messages")
INFO - Jndi(name="java:global/EjbModule837053032/org.superbiz.injection.jms.MessagingBeanTest!org.superbiz.injection.jms.MessagingBeanTest")
INFO - Jndi(name="java:global/EjbModule837053032/org.superbiz.injection.jms.MessagingBeanTest")
INFO - Created Ejb(deployment-id=Messages, ejb-name=Messages, container=Default Stateless Container)
INFO - Created Ejb(deployment-id=org.superbiz.injection.jms.MessagingBeanTest, ejb-name=org.superbiz.injection.jms.MessagingBeanTest, container=Default Managed Container)
INFO - Started Ejb(deployment-id=Messages, ejb-name=Messages, container=Default Stateless Container)
INFO - Started Ejb(deployment-id=org.superbiz.injection.jms.MessagingBeanTest, ejb-name=org.superbiz.injection.jms.MessagingBeanTest, container=Default Managed Container)
INFO - Deployed Application(path=/Users/dblevins/examples/injection-of-connectionfactory)
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.575 sec

Results :

Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

[INFO] 
[INFO] --- maven-jar-plugin:2.3.1:jar (default-jar) @ injection-of-connectionfactory ---
[INFO] Building jar: /Users/dblevins/examples/injection-of-connectionfactory/target/injection-of-connectionfactory-1.0.jar
[INFO] 
[INFO] --- maven-install-plugin:2.3.1:install (default-install) @ injection-of-connectionfactory ---
[INFO] Installing /Users/dblevins/examples/injection-of-connectionfactory/target/injection-of-connectionfactory-1.0.jar to /Users/dblevins/.m2/repository/org/superbiz/injection-of-connectionfactory/1.0/injection-of-connectionfactory-1.0.jar
[INFO] Installing /Users/dblevins/examples/injection-of-connectionfactory/pom.xml to /Users/dblevins/.m2/repository/org/superbiz/injection-of-connectionfactory/1.0/injection-of-connectionfactory-1.0.pom
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 4.738s
[INFO] Finished at: Fri Oct 28 17:05:38 PDT 2011
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
    package org.superbiz.injection.jms;
    
    import javax.annotation.Resource;
    import javax.ejb.Stateless;
    import javax.jms.Connection;
    import javax.jms.ConnectionFactory;
    import javax.jms.DeliveryMode;
    import javax.jms.JMSException;
    import javax.jms.MessageConsumer;
    import javax.jms.MessageProducer;
    import javax.jms.Queue;
    import javax.jms.Session;
    import javax.jms.TextMessage;
    
    @Stateless
    public class Messages {
    
        @Resource
        private ConnectionFactory connectionFactory;
    
        @Resource
        private Queue chatQueue;
    
    
        public void sendMessage(String text) throws JMSException {
    
            Connection connection = null;
            Session session = null;
    
            try {
                connection = connectionFactory.createConnection();
                connection.start();
    
                // Create a Session
                session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    
                // Create a MessageProducer from the Session to the Topic or Queue
                MessageProducer producer = session.createProducer(chatQueue);
                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
    
                // Create a message
                TextMessage message = session.createTextMessage(text);
    
                // Tell the producer to send the message
                producer.send(message);
            } finally {
                // Clean up
                if (session != null) session.close();
                if (connection != null) connection.close();
            }
        }
    
        public String receiveMessage() throws JMSException {
    
            Connection connection = null;
            Session session = null;
            MessageConsumer consumer = null;
            try {
                connection = connectionFactory.createConnection();
                connection.start();
    
                // Create a Session
                session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    
                // Create a MessageConsumer from the Session to the Topic or Queue
                consumer = session.createConsumer(chatQueue);
    
                // Wait for a message
                TextMessage message = (TextMessage) consumer.receive(1000);
    
                return message.getText();
            } finally {
                if (consumer != null) consumer.close();
                if (session != null) session.close();
                if (connection != null) connection.close();
            }
    
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
    //START SNIPPET: code
    package org.superbiz.injection.jms;
    
    import junit.framework.TestCase;
    
    import javax.ejb.embeddable.EJBContainer;
    import javax.naming.Context;
    
    public class MessagingBeanTest extends TestCase {
    
        public void test() throws Exception {
    
            final Context context = EJBContainer.createEJBContainer().getContext();
    
            Messages messages = (Messages) context.lookup("java:global/injection-of-connectionfactory/Messages");
    
            messages.sendMessage("Hello World!");
            messages.sendMessage("How are you?");
            messages.sendMessage("Still spinning?");
    
            assertEquals(messages.receiveMessage(), "Hello World!");
            assertEquals(messages.receiveMessage(), "How are you?");
            assertEquals(messages.receiveMessage(), "Still spinning?");
        }
    }
    //END SNIPPET: code

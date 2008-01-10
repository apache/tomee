/**
 *
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
package org.apache.openejb.core.mdb;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.work.WorkManager;

import junit.framework.TestCase;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.geronimo.connector.GeronimoBootstrapContext;
import org.apache.geronimo.connector.work.GeronimoWorkManager;
import org.apache.geronimo.transaction.manager.GeronimoTransactionManager;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.resource.activemq.ActiveMQResourceAdapter;

public class JmsProxyTest extends TestCase {
    private static final String REQUEST_QUEUE_NAME = "request";
    private ConnectionFactory connectionFactory;
    private ActiveMQResourceAdapter ra;

    protected void setUp() throws Exception {
        super.setUp();

        // create a transaction manager
        GeronimoTransactionManager transactionManager = new GeronimoTransactionManager();

        // create the ActiveMQ resource adapter instance
        ra = new ActiveMQResourceAdapter();

        // initialize properties
        ra.setServerUrl("tcp://localhost:61616");
        ra.setBrokerXmlConfig("broker:(tcp://localhost:61616)?useJmx=false");

        // create a thead pool for ActiveMQ
        Executor threadPool = Executors.newFixedThreadPool(30);

        // create a work manager which ActiveMQ uses to dispatch message delivery jobs
        WorkManager workManager = new GeronimoWorkManager(threadPool, threadPool, threadPool, transactionManager);

        // wrap the work mananger and transaction manager in a bootstrap context (connector spec thing)
        BootstrapContext bootstrapContext = new GeronimoBootstrapContext(workManager, transactionManager);

        // start the resource adapter
        try {
            ra.start(bootstrapContext);
        } catch (ResourceAdapterInternalException e) {
            throw new OpenEJBException(e);
        }
        // Create a ConnectionFactory
        connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
    }

    protected void tearDown() throws Exception {
        connectionFactory = null;
        if (ra != null) {
            ra.stop();
            ra = null;
        }
        super.tearDown();
    }

    public void testProxy() throws Exception {
        // create reciever object
        JmsProxyTest.TestObject testObject = new JmsProxyTest.TestObject("foo");
        MdbInvoker mdbInvoker = new MdbInvoker(connectionFactory, testObject);

        // Create a Session
        Connection connection = connectionFactory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        // Create the request Queue
        Destination requestQueue = session.createQueue(REQUEST_QUEUE_NAME);
        MessageConsumer consumer = session.createConsumer(requestQueue);
        consumer.setMessageListener(mdbInvoker);

        // create in invoker
        JmsProxyTest.TestInterface testInterface = MdbProxy.newProxyInstance(JmsProxyTest.TestInterface.class, connectionFactory, REQUEST_QUEUE_NAME);
        assertEquals("foobar", testInterface.echo("bar"));
        assertEquals("foobar", testInterface.echo("bar"));
        assertEquals("foobar", testInterface.echo("bar"));
        assertEquals("foobar", testInterface.echo("bar"));
        assertEquals("foobar", testInterface.echo("bar"));
    }

    public static interface TestInterface {
        String echo(String msg);
    }

    public static class TestObject implements JmsProxyTest.TestInterface {
        private final String prefix;


        public TestObject(String prefix) {
            this.prefix = prefix;
        }

        public String echo(String msg) {
            return prefix + msg;
        }
    }
}

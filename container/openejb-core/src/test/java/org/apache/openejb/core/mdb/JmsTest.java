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

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.work.WorkManager;

import junit.framework.TestCase;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ra.ActiveMQResourceAdapter;
import org.apache.geronimo.connector.GeronimoBootstrapContext;
import org.apache.geronimo.connector.work.GeronimoWorkManager;
import org.apache.geronimo.transaction.manager.GeronimoTransactionManager;
import org.apache.openejb.OpenEJBException;

public class JmsTest extends TestCase {
    private ConnectionFactory connectionFactory;
    private static final String REQUEST_QUEUE_NAME = "request";
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
        // Create a Session
        Connection connection = connectionFactory.createConnection();
        try {
            connection.start();

            Destination requestQueue = createListener(connection);

            createSender(connection, requestQueue);
        } finally {
            MdbUtil.close(connection);
        }
    }

    @SuppressWarnings("unchecked")
    private void createSender(Connection connection, Destination requestQueue) throws JMSException {
        Session session = null;
        MessageProducer producer = null;
        MessageConsumer consumer = null;
        try {
            // create request
            Map<String, Object> request = new TreeMap<String, Object>();
            request.put("args", new Object[]{"cheese"});

            // create a new temp response queue
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination responseQueue = session.createTemporaryQueue();

            // Create a request messages
            ObjectMessage requestMessage = session.createObjectMessage();
            requestMessage.setJMSReplyTo(responseQueue);
            requestMessage.setObject((Serializable) request);

            // Send the request message
            producer = session.createProducer(requestQueue);
            producer.send(requestMessage);

            // wait for the response message
            consumer = session.createConsumer(responseQueue);
            Message message = consumer.receive(1000);

            // verify message
            assertNotNull("Did not get a response message", message);
            assertTrue("Response message is not an ObjectMessage", message instanceof ObjectMessage);
            ObjectMessage responseMessage = (ObjectMessage) message;
            Serializable object = responseMessage.getObject();
            assertNotNull("Response ObjectMessage contains a null object");
            assertTrue("Response ObjectMessage does not contain an instance of Map", object instanceof Map);
            Map<String, String> response = (Map<String, String>) object;

            // process results
            String returnValue = (String) response.get("return");
            assertEquals("test-cheese", returnValue);
        } finally {
            MdbUtil.close(consumer);
            MdbUtil.close(producer);
            MdbUtil.close(session);
        }
    }

    
    private Destination createListener(Connection connection) throws JMSException {
        final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        // Create the request Queue
        Destination requestQueue = session.createQueue(REQUEST_QUEUE_NAME);
        MessageConsumer consumer = session.createConsumer(requestQueue);
        consumer.setMessageListener(new MessageListener() {
            @SuppressWarnings("unchecked")
            public void onMessage(Message message) {
                // if we got a dummy (non ObjectMessage) return
                if (!(message instanceof ObjectMessage)) return;

                MessageProducer producer = null;
                try {
                    // process request
                    ObjectMessage requestMessage = (ObjectMessage) message;
                    Map<String, Object[]> request = (Map<String, Object[]>) requestMessage.getObject();
                    Object[] args = (Object[]) request.get("args");
                    String returnValue = "test-" + args[0];

                    // create response map
                    Map<String, Object> response = new TreeMap<String, Object>();
                    response.put("return", returnValue);

                    // create response message
                    ObjectMessage responseMessage = session.createObjectMessage();
                    responseMessage.setJMSCorrelationID(requestMessage.getJMSCorrelationID());
                    responseMessage.setObject((Serializable) response);

                    // send response message
                    producer = session.createProducer(requestMessage.getJMSReplyTo());
                    producer.send(responseMessage);

                } catch (Throwable e) {
                    e.printStackTrace();
                } finally {
                    MdbUtil.close(producer);
                }
            }
        });
        return requestQueue;
    }
}

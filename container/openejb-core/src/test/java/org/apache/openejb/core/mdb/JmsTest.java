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

import junit.framework.TestCase;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.geronimo.connector.GeronimoBootstrapContext;
import org.apache.geronimo.connector.work.GeronimoWorkManager;
import org.apache.geronimo.connector.work.TransactionContextHandler;
import org.apache.geronimo.connector.work.WorkContextHandler;
import org.apache.geronimo.transaction.manager.GeronimoTransactionManager;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.resource.activemq.ActiveMQResourceAdapter;
import org.apache.openejb.util.Duration;
import org.apache.openejb.util.NetworkUtil;

import jakarta.jms.Connection;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageListener;
import jakarta.jms.MessageProducer;
import jakarta.jms.ObjectMessage;
import jakarta.jms.Session;
import jakarta.resource.spi.BootstrapContext;
import jakarta.resource.spi.ResourceAdapterInternalException;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class JmsTest extends TestCase {
    protected static final String REQUEST_QUEUE_NAME = "request";
    protected ActiveMQConnectionFactory connectionFactory;
    protected ActiveMQResourceAdapter ra;
    protected String brokerAddress = NetworkUtil.getLocalAddress("tcp://", "?jms.watchTopicAdvisories=false");
    protected String brokerXmlConfig = "broker:(" + brokerAddress + ")?useJmx=false&persistent=false&cacheTempDestinations=true";

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // create a transaction manager
        final GeronimoTransactionManager transactionManager = new GeronimoTransactionManager();

        // create the ActiveMQ resource adapter instance
        ra = new ActiveMQResourceAdapter();

        // initialize properties
        ra.setServerUrl(brokerAddress);
        ra.setBrokerXmlConfig(brokerXmlConfig);
        ra.setStartupTimeout(new Duration(10, TimeUnit.SECONDS));

        // create a thead pool for ActiveMQ
        final Executor threadPool = Executors.newFixedThreadPool(30);

        // create a work manager which ActiveMQ uses to dispatch message delivery jobs
        final TransactionContextHandler txWorkContextHandler = new TransactionContextHandler(transactionManager);
        final GeronimoWorkManager workManager = new GeronimoWorkManager(threadPool, threadPool, threadPool, Collections.<WorkContextHandler>singletonList(txWorkContextHandler));

        // wrap the work mananger and transaction manager in a bootstrap context (connector spec thing)
        final BootstrapContext bootstrapContext = new GeronimoBootstrapContext(workManager, transactionManager, transactionManager);

        // Create a ConnectionFactory
        connectionFactory = new ActiveMQConnectionFactory(brokerAddress);
        ra.setConnectionFactory(connectionFactory);

        // start the resource adapter
        try {
            ra.start(bootstrapContext);
        } catch (final ResourceAdapterInternalException e) {
            throw new OpenEJBException(e);
        }
    }

    @Override
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
        final Connection connection = connectionFactory.createConnection();
        try {
            connection.start();

            final Destination requestQueue = createListener(connection);

            createSender(connection, requestQueue);
        } finally {
            MdbUtil.close(connection);
        }
    }

    @SuppressWarnings("unchecked")
    private synchronized void createSender(final Connection connection, final Destination requestQueue) throws JMSException {
        Session session = null;
        MessageProducer producer = null;
        MessageConsumer consumer = null;
        try {
            // create request
            final Map<String, Object> request = new TreeMap<>();
            request.put("args", new Object[]{"cheese"});

            // create a new temp response queue
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            final Destination responseQueue = session.createTemporaryQueue();

            // Create a request messages
            final ObjectMessage requestMessage = session.createObjectMessage();
            requestMessage.setJMSReplyTo(responseQueue);
            requestMessage.setObject((Serializable) request);

            // Send the request message
            producer = session.createProducer(requestQueue);
            producer.send(requestMessage);

            // wait for the response message
            consumer = session.createConsumer(responseQueue);
            final Message message = consumer.receive(30000);

            // verify message
            assertNotNull("Did not get a response message", message);
            assertTrue("Response message is not an ObjectMessage", message instanceof ObjectMessage);
            final ObjectMessage responseMessage = (ObjectMessage) message;
            final Serializable object = responseMessage.getObject();
            assertNotNull("Response ObjectMessage contains a null object");
            assertTrue("Response ObjectMessage does not contain an instance of Map", object instanceof Map);
            final Map<String, String> response = (Map<String, String>) object;

            // process results
            final String returnValue = response.get("return");
            assertEquals("test-cheese", returnValue);
        } finally {
            MdbUtil.close(consumer);
            MdbUtil.close(producer);
            MdbUtil.close(session);
        }
    }


    private Destination createListener(final Connection connection) throws JMSException {
        final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        // Create the request Queue
        final Destination requestQueue = session.createQueue(REQUEST_QUEUE_NAME);
        final MessageConsumer consumer = session.createConsumer(requestQueue);
        consumer.setMessageListener(new MessageListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void onMessage(final Message message) {
                // if we got a dummy (non ObjectMessage) return
                if (!(message instanceof ObjectMessage)) return;

                MessageProducer producer = null;
                try {
                    // process request
                    final ObjectMessage requestMessage = (ObjectMessage) message;
                    final Map<String, Object[]> request = (Map<String, Object[]>) requestMessage.getObject();
                    final Object[] args = request.get("args");
                    final String returnValue = "test-" + args[0];

                    // create response map
                    final Map<String, Object> response = new TreeMap<>();
                    response.put("return", returnValue);

                    // create response message
                    final ObjectMessage responseMessage = session.createObjectMessage();
                    responseMessage.setJMSCorrelationID(requestMessage.getJMSCorrelationID());
                    responseMessage.setObject((Serializable) response);

                    // send response message
                    producer = session.createProducer(requestMessage.getJMSReplyTo());
                    producer.send(responseMessage);

                } catch (final Throwable e) {
                    e.printStackTrace();
                } finally {
                    MdbUtil.close(producer);
                }
            }
        });
        return requestQueue;
    }
}

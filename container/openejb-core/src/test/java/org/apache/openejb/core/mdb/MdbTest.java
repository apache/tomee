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

import edu.emory.mathcs.backport.java.util.concurrent.Executor;
import edu.emory.mathcs.backport.java.util.concurrent.Executors;
import junit.framework.TestCase;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ra.ActiveMQActivationSpec;
import org.apache.activemq.ra.ActiveMQResourceAdapter;
import org.apache.geronimo.connector.GeronimoBootstrapContext;
import org.apache.geronimo.connector.work.GeronimoWorkManager;
import org.apache.geronimo.transaction.jta11.GeronimoTransactionManagerJTA11;
import org.apache.openejb.OpenEJBException;

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
import javax.resource.ResourceException;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.UnavailableException;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.work.WorkManager;
import javax.transaction.xa.XAResource;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.TreeMap;

public class MdbTest extends TestCase {
    private static final String REQUEST_QUEUE_NAME = "request";
    private ConnectionFactory connectionFactory;
    private ActiveMQResourceAdapter ra;

    protected void setUp() throws Exception {
        super.setUp();

        // create a transaction manager
        GeronimoTransactionManagerJTA11 transactionManager = new GeronimoTransactionManagerJTA11();

        // create the ActiveMQ resource adapter instance
        ra = new ActiveMQResourceAdapter();

        // initialize properties
        ra.setServerUrl("tcp://localhost:61616");
        ra.setBrokerXmlConfig("broker:(tcp://localhost:61616)");

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
        createListener();
        createSender();
    }

    private void createSender() throws JMSException {
        Connection connection = null;
        Session session = null;
        MessageProducer producer = null;
        MessageConsumer consumer = null;
        try {
            connection = connectionFactory.createConnection();
            connection.start();

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
            producer = session.createProducer(session.createQueue(REQUEST_QUEUE_NAME));
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
            Map response = (Map) object;

            // process results
            String returnValue = (String) response.get("return");
            assertEquals("test-cheese", returnValue);
        } finally {
            MdbUtil.close(consumer);
            MdbUtil.close(producer);
            MdbUtil.close(session);
            MdbUtil.close(connection);
        }
    }

    private void createListener() throws Exception {
        // create the activation spec
        ActiveMQActivationSpec activationSpec = new ActiveMQActivationSpec();
        activationSpec.setDestinationType("javax.jms.Queue");
        activationSpec.setDestination(REQUEST_QUEUE_NAME);

        // validate the activation spec
        activationSpec.validate();

        // set the resource adapter into the activation spec
        activationSpec.setResourceAdapter(ra);

        // create the message endpoint
        MessageEndpointFactory endpointFactory = new JmsEndpointFactory();

        // activate the endpoint
        ra.endpointActivation(endpointFactory, activationSpec);
    }

    public static class JmsEndpointFactory implements MessageEndpointFactory {
        private final ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");

        public MessageEndpoint createEndpoint(XAResource xaResource) throws UnavailableException {
            try {
                return new JmsEndpoint(connectionFactory);
            } catch (JMSException e) {
                e.printStackTrace();
                throw new UnavailableException(e);
            }
        }

        public boolean isDeliveryTransacted(Method method) throws NoSuchMethodException {
            return false;
        }
    }

    public static class JmsEndpoint implements MessageEndpoint, MessageListener {
        private final Session session;

        public JmsEndpoint(ConnectionFactory connectionFactory) throws JMSException {
            Connection connection = connectionFactory.createConnection();
            connection.start();

            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        }

        public void onMessage(Message message) {
            // if we got a dummy (non ObjectMessage) return
            if (!(message instanceof ObjectMessage)) return;

            MessageProducer producer = null;
            try {
                // process request
                ObjectMessage requestMessage = (ObjectMessage) message;
                Map request = (Map) requestMessage.getObject();
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

        public void beforeDelivery(Method method) throws NoSuchMethodException, ResourceException {
        }

        public void afterDelivery() throws ResourceException {
        }

        public void release() {
        }
    }
}

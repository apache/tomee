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

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ra.ActiveMQActivationSpec;

import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageListener;
import jakarta.jms.MessageProducer;
import jakarta.jms.ObjectMessage;
import jakarta.jms.Session;
import jakarta.resource.ResourceException;
import jakarta.resource.spi.UnavailableException;
import jakarta.resource.spi.endpoint.MessageEndpoint;
import jakarta.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.TreeMap;

public class MdbTest extends JmsTest {

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
            producer = session.createProducer(session.createQueue(REQUEST_QUEUE_NAME));
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
            final String returnValue = (String) response.get("return");
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
        final ActiveMQActivationSpec activationSpec = new ActiveMQActivationSpec();
        activationSpec.setDestinationType("jakarta.jms.Queue");
        activationSpec.setDestination(REQUEST_QUEUE_NAME);

        // validate the activation spec
        activationSpec.validate();

        // set the resource adapter into the activation spec
        activationSpec.setResourceAdapter(ra);

        // create the message endpoint
        final MessageEndpointFactory endpointFactory = new JmsEndpointFactory();

        // activate the endpoint
        ra.endpointActivation(endpointFactory, activationSpec);
    }

    public class JmsEndpointFactory implements MessageEndpointFactory {
        private final ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerAddress);

        public MessageEndpoint createEndpoint(final XAResource xaResource) throws UnavailableException {
            try {
                return new JmsEndpoint(connectionFactory);
            } catch (final JMSException e) {
                e.printStackTrace();
                throw new UnavailableException(e);
            }
        }

        public MessageEndpoint createEndpoint(final XAResource xaResource, final long timeout) throws UnavailableException {
            return createEndpoint(xaResource);
        }

        public boolean isDeliveryTransacted(final Method method) throws NoSuchMethodException {
            return false;
        }
    }

    public static class JmsEndpoint implements MessageEndpoint, MessageListener {
        private final Session session;

        public JmsEndpoint(final ConnectionFactory connectionFactory) throws JMSException {
            final Connection connection = connectionFactory.createConnection();
            connection.start();

            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        }

        public void onMessage(final Message message) {
            // if we got a dummy (non ObjectMessage) return
            if (!(message instanceof ObjectMessage)) return;

            MessageProducer producer = null;
            try {
                // process request
                final ObjectMessage requestMessage = (ObjectMessage) message;
                final Map<String, Object[]> request = (Map<String, Object[]>) requestMessage.getObject();
                final Object[] args = (Object[]) request.get("args");
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

        public void beforeDelivery(final Method method) throws NoSuchMethodException, ResourceException {
        }

        public void afterDelivery() throws ResourceException {
        }

        public void release() {
        }
    }
}

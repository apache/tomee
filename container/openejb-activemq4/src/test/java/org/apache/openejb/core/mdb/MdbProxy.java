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

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.ObjectMessage;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.Map;
import java.util.TreeMap;
import java.io.Serializable;

public class MdbProxy {
    @SuppressWarnings({"unchecked"})
    public static <T> T newProxyInstance(Class<T> type, ConnectionFactory connectionFactory, String requestQueueName) throws JMSException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) classLoader = type.getClassLoader();
        if (classLoader == null) classLoader = ClassLoader.getSystemClassLoader();

        InvocationHandler invocationHandler = new MdbInvocationHandler(connectionFactory, requestQueueName);
        Object proxy = Proxy.newProxyInstance(classLoader, new Class[]{type}, invocationHandler);
        return (T) proxy;
    }

    public static void destroyProxy(Object proxy) {
        InvocationHandler handler = Proxy.getInvocationHandler(proxy);
        if (handler instanceof MdbProxy) {
            MdbInvocationHandler mdbInvocationHandler = (MdbInvocationHandler) handler;
            mdbInvocationHandler.destroy();
        }
    }

    private static class MdbInvocationHandler implements InvocationHandler {
        private static final int MAX_RESPONSE_WAIT = 1000;
        private Connection connection;
        private Session session;
        private MessageProducer producer;

        public MdbInvocationHandler(ConnectionFactory connectionFactory, String requestQueueName) throws JMSException {
            // open a connection
            connection = connectionFactory.createConnection();
            connection.start();

            // create a session
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // create the request queue
            Destination requestQueue = session.createQueue(requestQueueName);

            // create a producer which is used to send requests
            producer = session.createProducer(requestQueue);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        }

        public synchronized void destroy() {
            MdbUtil.close(producer);
            producer = null;
            MdbUtil.close(session);
            session = null;
            MdbUtil.close(connection);
            connection = null;
        }

        private synchronized Session getSession() {
            return session;
        }

        public synchronized MessageProducer getProducer() {
            return producer;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Session session = getSession();
            if (session == null) throw new IllegalStateException("Proxy has been destroyed");

            // create request
            Map<String,Object> request = new TreeMap<String,Object>();
            String signature = MdbUtil.getSignature(method);
            request.put("method", signature);
            request.put("args", args);

            // create a new temp response queue and consumer
            // this is very inefficient, but eliminates a whole class of errors
            Destination responseQueue = session.createTemporaryQueue();

            // Create a messages
            ObjectMessage reqMessage = session.createObjectMessage();
            reqMessage.setJMSReplyTo(responseQueue);
            String correlationId = UUID.randomUUID().toString();
            reqMessage.setJMSCorrelationID(correlationId);
            reqMessage.setObject((Serializable) request);

            // Send the request
            getProducer().send(reqMessage);

            // Wait for a message
            // Again this is quite inefficient
            MessageConsumer consumer = session.createConsumer(responseQueue);
            try {
                // wait for the message
                Message message = consumer.receive(MAX_RESPONSE_WAIT);

                // verify message
                if (message == null) throw new NullPointerException("message is null");
                if (!correlationId.equals(message.getJMSCorrelationID())) {
                    throw new IllegalStateException("Recieved a response message with the wrong correlation id");
                }
                if (!(message instanceof ObjectMessage)) throw new IllegalArgumentException("Expected a ObjectMessage response but got a " + message.getClass().getName());
                ObjectMessage resMessage = (ObjectMessage) message;
                Serializable object = resMessage.getObject();
                if (object == null) throw new NullPointerException("object in ObjectMessage is null");
                if (!(object instanceof Map)) {
                    if (message instanceof ObjectMessage) throw new IllegalArgumentException("Expected a Map contained in the ObjectMessage response but got a " + object.getClass().getName());
                }
                Map response = (Map) object;

                // process results
                boolean exception = response.containsKey("exception");
                Object returnValue = response.get("return");
                if (exception) {
                    throw (Throwable) returnValue;
                }
                return returnValue;
            } finally {
                MdbUtil.close(consumer);
            }
        }
    }

    private MdbProxy() {
    }
}

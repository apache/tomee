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
package org.apache.openejb.test.mdb;

import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.DeliveryMode;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.MessageProducer;
import jakarta.jms.ObjectMessage;
import jakarta.jms.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.TreeMap;

public class MdbInvoker implements MessageListener {

    private final Map<String, Method> signatures = new TreeMap<String, Method>();
    private final ConnectionFactory connectionFactory;
    private final Object target;

    private Connection connection;
    private Session session;
    private MessageProducer replyProducer = null;

    public MdbInvoker(final ConnectionFactory connectionFactory, final Object target) throws JMSException {

        this.connectionFactory = connectionFactory;
        this.target = target;

        for (final Method method : target.getClass().getMethods()) {
            final String signature = MdbUtil.getSignature(method);
            signatures.put(signature, method);
        }
    }

    @Override
    protected void finalize() throws Throwable {

        try {
            this.destroy();
        } finally {
            super.finalize();
        }
    }

    public synchronized void destroy() {
        MdbUtil.close(replyProducer);
        MdbUtil.close(session);
        session = null;
        MdbUtil.close(connection);
        connection = null;
    }

    private synchronized Session getSession() throws JMSException {

        this.connection = this.connectionFactory.createConnection();
        this.connection.start();

        boolean isBeanManagedTransaction = false;

        try {
            new InitialContext().lookup("java:comp/UserTransaction");
            isBeanManagedTransaction = true;
        } catch (final NamingException e) {
            //Ignore - Not transacted
        }

        if (isBeanManagedTransaction) {
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        } else {
            session = connection.createSession(true, Session.SESSION_TRANSACTED);
        }

        this.replyProducer = this.session.createProducer(null);
        this.replyProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

        return session;
    }

    @Override
    public void onMessage(final Message message) {

        try {

            if (!(message instanceof ObjectMessage)) {
                return;
            }

            final Session session = getSession();
            if (session == null) throw new IllegalStateException("Invoker has been destroyed");

            if (message == null) throw new NullPointerException("request message is null");
            if (!(message instanceof ObjectMessage))
                throw new IllegalArgumentException("Expected a ObjectMessage request but got a " + message.getClass().getName());
            final Serializable object = ((ObjectMessage) message).getObject();
            if (object == null) throw new NullPointerException("object in ObjectMessage is null");
            if (!(object instanceof Map)) {
                if (message instanceof ObjectMessage)
                    throw new IllegalArgumentException("Expected a Map contained in the ObjectMessage request but got a " + object.getClass().getName());
            }
            final Map request = (Map) object;

            final String signature = (String) request.get("method");
            if (signature == null) throw new NullPointerException("method property is null");
            final Method method = signatures.get(signature);
            if (method == null)
                throw new IllegalArgumentException("no such method " + signature + "; known methods are " + signatures.keySet());
            final Object[] args = (Object[]) request.get("args");

            boolean exception = false;
            Object result;
            try {
                result = method.invoke(target, args);
            } catch (final IllegalAccessException e) {
                result = e;
                exception = true;
            } catch (final InvocationTargetException e) {
                result = e.getCause();
                if (result == null) result = e;
                exception = true;
            } catch (final Exception e) {
                result = e.getCause();
                if (result == null) result = e;
                exception = true;
            }

            try {
                // create response
                final Map<String, Object> response = new TreeMap<String, Object>();
                if (exception) {
                    response.put("exception", "true");
                }
                response.put("return", result);

                // create response message
                final ObjectMessage resMessage = session.createObjectMessage();
                resMessage.setJMSCorrelationID(message.getJMSCorrelationID());
                resMessage.setObject((Serializable) response);

                // send response message
                replyProducer.send(message.getJMSReplyTo(), resMessage);

            } catch (final Exception e) {
                e.printStackTrace();
            }
        } catch (final Throwable e) {
            e.printStackTrace();
        } finally {
            this.destroy();
        }
    }
}

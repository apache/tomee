/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.core.mdb;

import javax.jms.*;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;
import java.lang.IllegalStateException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MdbInvoker implements MessageListener {
    private final ReentrantLock lock = new ReentrantLock();
    private final Map<String, Method> signatures = new TreeMap<String, Method>();
    private final Object target;
    private Connection connection;
    private Session session;
    private final ConnectionFactory connectionFactory;

    public MdbInvoker(final ConnectionFactory connectionFactory, final Object target) throws JMSException {
        this.target = target;
        this.connectionFactory = connectionFactory;
        for (final Method method : target.getClass().getMethods()) {
            final String signature = MdbUtil.getSignature(method);
            signatures.put(signature, method);
        }
    }

    public void destroy() {

        lock.lock();

        try {
            MdbUtil.close(session);
            session = null;
            MdbUtil.close(connection);
            connection = null;
        } finally {
            lock.unlock();
        }
    }

    private Session getSession() throws JMSException {
        lock.lock();

        try {
            connection = connectionFactory.createConnection();
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            return session;
        } finally {
            lock.unlock();
        }
    }

    public void onMessage(final Message message) {
        try {
            if (message == null) throw new NullPointerException("request message is null");

            if (!ObjectMessage.class.isInstance(message))
                throw new IllegalArgumentException("Expected a ObjectMessage request but got a " + message.getClass().getName());

            final Session session = getSession();
            if (session == null) throw new IllegalStateException("Invoker has been destroyed");

            final ObjectMessage objectMessage = (ObjectMessage) message;
            final Serializable object = objectMessage.getObject();

            if (object == null) throw new NullPointerException("object in ObjectMessage is null");

            if (!(object instanceof Map)) {
                throw new IllegalArgumentException("Expected a Map contained in the ObjectMessage request but got a " + object.getClass().getName());
            }

            final Map request = (Map) object;
            final String signature = (String) request.get("method");
            final Method method = signatures.get(signature);
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
            }

            MessageProducer producer = null;

            try {
                // create response
                final Map<String, Object> response = new TreeMap<String, Object>();
                if (exception) {
                    response.put("exception", "true");
                }
                response.put("return", result);

                // create response message
                final ObjectMessage resMessage = session.createObjectMessage();
                resMessage.setJMSCorrelationID(objectMessage.getJMSCorrelationID());
                resMessage.setObject((Serializable) response);

                // send response message
                producer = session.createProducer(objectMessage.getJMSReplyTo());
                producer.send(resMessage);
            } catch (final Exception e) {
                e.printStackTrace();
            } finally {
                MdbUtil.close(producer);
                destroy();
            }
        } catch (final Exception e) {
            Logger.getLogger(MdbInvoker.class.getName()).log(Level.WARNING, "MdbInvoker.onMessage: " + message, e);
        }
    }
}

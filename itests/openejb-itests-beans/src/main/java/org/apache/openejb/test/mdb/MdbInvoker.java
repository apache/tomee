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

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.TreeMap;

public class MdbInvoker implements MessageListener {
    private final Map<String, Method> signatures = new TreeMap<String, Method>();
    private final Object target;
    private Connection connection;
    private Session session;
    private ConnectionFactory connectionFactory;

    public MdbInvoker(ConnectionFactory connectionFactory, Object target) throws JMSException {
        this.target = target;
        this.connectionFactory = connectionFactory;
        for (Method method : target.getClass().getMethods()) {
            String signature = MdbUtil.getSignature(method);
            signatures.put(signature, method);
        }
    }

    public synchronized void destroy() {
        MdbUtil.close(session);
        session = null;
        MdbUtil.close(connection);
        connection = null;
    }

    private synchronized Session getSession() throws JMSException{
        connection = connectionFactory.createConnection();
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        return session;
    }

    public void onMessage(Message message) {
        if (!(message instanceof ObjectMessage)) return;

        try {
            Session session = getSession();
            if (session == null) throw new IllegalStateException("Invoker has been destroyed");

            if (message == null) throw new NullPointerException("request message is null");
            if (!(message instanceof ObjectMessage)) throw new IllegalArgumentException("Expected a ObjectMessage request but got a " + message.getClass().getName());
            ObjectMessage responseMessage = (ObjectMessage) message;
            Serializable object = responseMessage.getObject();
            if (object == null) throw new NullPointerException("object in ObjectMessage is null");
            if (!(object instanceof Map)) {
                if (message instanceof ObjectMessage) throw new IllegalArgumentException("Expected a Map contained in the ObjectMessage request but got a " + object.getClass().getName());
            }
            Map request = (Map) object;

            String signature = (String) request.get("method");
            if (signature == null) throw new NullPointerException("method property is null");
            Method method = signatures.get(signature);
            if (method == null) throw new IllegalArgumentException("no such method " + signature + "; known methods are " + signatures.keySet());
            Object[] args = (Object[]) request.get("args");

            boolean exception = false;
            Object result = null;
            try {
                result = method.invoke(target, args);
            } catch (IllegalAccessException e) {
                result = e;
                exception = true;
            } catch (InvocationTargetException e) {
                result = e.getCause();
                if (result == null) result = e;
                exception = true;
            }

            MessageProducer producer = null;
            try {
                // create response
                Map<String, Object> response = new TreeMap<String, Object>();
                if (exception) {
                    response.put("exception", "true");
                }
                response.put("return", result);

                // create response message
                ObjectMessage resMessage = session.createObjectMessage();
                resMessage.setJMSCorrelationID(responseMessage.getJMSCorrelationID());
                resMessage.setObject((Serializable) response);

                // send response message
                producer = session.createProducer(responseMessage.getJMSReplyTo());
                producer.send(resMessage);
//                System.out.println("\n" +
//                        "***************************************\n" +
//                        "Sent response message: " + responseMessage + "\n" +
//                        "         response map: " + response + "\n" +
//                        "             to queue: " + message.getJMSReplyTo() + "\n" +
//                        "***************************************\n\n");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                MdbUtil.close(producer);
                destroy();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.rest.service;


import jakarta.annotation.Resource;
import jakarta.ejb.Singleton;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import jakarta.jms.Topic;

import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;

@Singleton
@TransactionManagement(TransactionManagementType.CONTAINER)
public class Producer {

    @Resource(name = "event")
    private Topic event;

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void sendMessage(final String message, final String broker) {
        try {
            final InitialContext context = new InitialContext();
            final NamingEnumeration<NameClassPair> list = context.list("openejb:Resource");

            while (list.hasMoreElements()) {
                final NameClassPair nameClassPair = list.nextElement();
                final String name = nameClassPair.getName();
                if (name.endsWith("ConnectionFactory") && name.substring(0, name.length() - 17).toLowerCase().equals(broker.toLowerCase())) {
                    ConnectionFactory cf = null;
                    Connection connection = null;
                    Session session = null;
                    MessageProducer producer = null;

                    try {
                        cf = (ConnectionFactory) context.lookup("openejb:Resource/" + name);
                        connection = cf.createConnection();
                        session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
                        producer = session.createProducer(event);
                        final Message msg = session.createTextMessage(message);
                        producer.send(msg);
                    } finally {
                        try {
                            if (producer != null) producer.close();
                        } catch (Exception e) {
                            // ignore
                        }

                        try {
                            if (session != null) session.close();
                        } catch (JMSException e) {
                            // ignore
                        }

                        try {
                            if (connection != null) connection.close();
                        } catch (JMSException e) {
                            // ignore
                        }
                    }

                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}

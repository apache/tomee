/**
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
package org.superbiz.moviefun;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

@Stateless
public class NotifierImpl implements Notifier {

    @Resource
    private ConnectionFactory connectionFactory;

    @Resource(name = "notifications")
    private Topic notificationsTopic;

    public void notify(String message) {
        try {
            Connection connection = null;
            Session session = null;

            try {
                connection = connectionFactory.createConnection();
                connection.start();

                // Create a Session
                session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                // Create a MessageProducer from the Session to the Topic or Queue
                MessageProducer producer = session.createProducer(notificationsTopic);
                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

                // Create a message
                TextMessage textMessage = session.createTextMessage(message);

                // Tell the producer to send the message
                producer.send(textMessage);
            } finally {
                // Clean up
                if (session != null) session.close();
                if (connection != null) connection.close();
            }
        } catch (JMSException e) {
            throw new IllegalStateException(e);
        }

    }

}

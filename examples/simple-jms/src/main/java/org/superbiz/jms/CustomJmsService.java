/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.jms;

import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageProducer;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Stateless
@Path("message")
public class CustomJmsService {

    @Resource(name = "messageQueue")
    private Queue messageQueue;

    @Resource
    private ConnectionFactory connectionFactory;

    @POST
    public void sendMessage(final String message) {
        sendMessage(messageQueue, message);
    }

    @GET
    public String receiveMessage() throws JMSException {
        final TextMessage textMessage = receiveMessage(messageQueue, 1000);
        if (textMessage == null) {
            return null;
        }

        return textMessage.getText();
    }

    private void sendMessage(final Queue queue, final String message) {
        try (final Connection connection = connectionFactory.createConnection();
             final Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
             final MessageProducer producer = session.createProducer(queue)) {

            connection.start();

            final Message jmsMessage = session.createTextMessage(message);

            // This enqueues messages successfully with both 8.0.0-M3 and 8.0.0
            producer.send(jmsMessage);
        } catch (final Exception e) {
            throw new RuntimeException("Caught exception from JMS when sending a message", e);
        }
    }

    private TextMessage receiveMessage(final Queue queue, final long receiveTimeoutMillis) {
        try (final Connection connection = connectionFactory.createConnection();
             final Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
             final MessageConsumer messageConsumer = session.createConsumer(queue)) {

            connection.start();

            final Message jmsMessage = messageConsumer.receive(receiveTimeoutMillis);

            if (jmsMessage == null) {
                return null;
            }

            return (TextMessage) jmsMessage;
        } catch (final Exception e) {
            throw new RuntimeException("Caught exception from JMS when receiving a message", e);
        }
    }
}


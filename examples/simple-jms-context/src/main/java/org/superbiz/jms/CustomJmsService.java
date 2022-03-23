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
import jakarta.inject.Inject;
import jakarta.jms.*;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Stateless
@Path("message")
public class CustomJmsService {

    @Resource
    private ConnectionFactory cf;

    @Inject
    private JMSContext jmsContext;

    @Resource(name = "messageQueue")
    private Queue messageQueue;

    @POST
    public void sendMessage(final String message) {
        sendMessage(messageQueue, message);
    }

    @GET
    public String receiveMessage() throws JMSException {
        final Message message = jmsContext.createConsumer(messageQueue).receive(1000);
        if (! (message instanceof TextMessage)) {
            return null;
        }

        return ((TextMessage) message).getText();
    }

    private void sendMessage(final Queue queue, final String message) {
        jmsContext.createProducer().send(messageQueue, jmsContext.createTextMessage(message));
    }
}


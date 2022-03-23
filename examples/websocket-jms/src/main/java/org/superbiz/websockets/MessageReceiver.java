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
 *
 */
package org.superbiz.websockets;

import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.TextMessage;
import java.util.logging.Logger;

@MessageDriven( activationConfig = {
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "messageReceived"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Topic")
})
public class MessageReceiver implements MessageListener {

    private static Logger LOG = Logger.getLogger(MessageReceiver.class.getName());

    @Inject
    private Event<MessageReceivedEvent> event;

    @Override
    public void onMessage(final Message message) {
        if (message instanceof TextMessage) {
            try {
                event.fire(new MessageReceivedEvent(TextMessage.class.cast(message).getText()));
            } catch (JMSException e) {
                LOG.warning("Error processing JMS message: " + e.getMessage());
            }
        }
    }
}

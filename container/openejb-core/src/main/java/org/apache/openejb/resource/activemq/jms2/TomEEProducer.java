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
package org.apache.openejb.resource.activemq.jms2;

import org.apache.activemq.ActiveMQMessageProducer;
import org.apache.activemq.ActiveMQSession;
import org.apache.activemq.AsyncCallback;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ProducerId;

import jakarta.jms.CompletionListener;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.Message;

public class TomEEProducer extends ActiveMQMessageProducer {
    private final ActiveMQDestination destination;
    private long deliveryDelay;

    public TomEEProducer(final ActiveMQSession session, final ProducerId producerId,
                         final ActiveMQDestination destination, final int sendTimeout) throws JMSException {
        super(session, producerId, destination, sendTimeout);
        this.destination = destination;
    }

    @Override
    public void send(final Message message, final CompletionListener completionListener) throws JMSException {
        super.send(destination, message, new ProducerAsyncCallback(message, completionListener));
    }

    @Override
    public void send(final Destination destination, final Message message, final CompletionListener completionListener) throws JMSException {
        super.send(destination, message, new ProducerAsyncCallback(message, completionListener));
    }

    @Override
    public void send(final Message message, final int deliveryMode, final int priority,
                     final long timeToLive, final CompletionListener completionListener) throws JMSException {
        super.send(destination, message, deliveryMode, priority, timeToLive, new ProducerAsyncCallback(message, completionListener));
    }

    @Override
    public void send(final Destination destination, final Message message,
                     final int deliveryMode, final int priority, final long timeToLive,
                     final CompletionListener completionListener) throws JMSException {
        super.send(destination, message, deliveryMode, priority, timeToLive, new ProducerAsyncCallback(message, completionListener));
    }

    @Override
    public long getDeliveryDelay() throws JMSException {
        return deliveryDelay;
    }

    @Override
    public void setDeliveryDelay(final long deliveryDelay) throws JMSException {
        this.deliveryDelay = deliveryDelay;
    }

    private static final class ProducerAsyncCallback implements AsyncCallback {
        private final Message message;
        private final CompletionListener completionListener;

        private ProducerAsyncCallback(final Message message, final CompletionListener completionListener) {
            this.message = message;
            this.completionListener = completionListener;
        }

        @Override
        public void onSuccess() {
            completionListener.onCompletion(message);
        }

        @Override
        public void onException(final JMSException exception) {
            completionListener.onException(message, exception);
        }
    }
}

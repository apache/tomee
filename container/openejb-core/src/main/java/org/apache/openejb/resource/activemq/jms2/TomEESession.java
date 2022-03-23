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

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQMessageTransformation;
import org.apache.activemq.ActiveMQSession;
import org.apache.activemq.CustomDestination;
import org.apache.activemq.command.SessionId;

import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageProducer;
import jakarta.jms.Topic;

// Note: not shared in the code
public class TomEESession extends ActiveMQSession {
    public TomEESession(final ActiveMQConnection connection, final SessionId sessionId,
                        final int acknowledgeMode, final boolean asyncDispatch, final boolean sessionAsyncDispatch) throws JMSException {
        super(connection, sessionId, acknowledgeMode, asyncDispatch, sessionAsyncDispatch);
    }

    @Override
    public MessageProducer createProducer(final Destination destination) throws JMSException {
        checkClosed();
        if (CustomDestination.class.isInstance(destination)) {
            return CustomDestination.class.cast(destination).createProducer(this);
        }
        return new TomEEProducer(this, getNextProducerId(), ActiveMQMessageTransformation.transformDestination(destination), connection.getSendTimeout());
    }

    @Override
    public MessageConsumer createDurableConsumer(final Topic topic, final String name) throws JMSException {
        return createDurableSubscriber(topic, name);
    }

    @Override
    public MessageConsumer createDurableConsumer(final Topic topic, final String name, final String messageSelector, final boolean noLocal) throws JMSException {
        return createDurableSubscriber(topic, name, messageSelector, noLocal);
    }

    @Override
    public MessageConsumer createSharedConsumer(final Topic topic, final String sharedSubscriptionName) throws JMSException {
        return createConsumer(topic);
    }

    @Override
    public MessageConsumer createSharedConsumer(final Topic topic, final String sharedSubscriptionName, final String messageSelector) throws JMSException {
        return createConsumer(topic, messageSelector);
    }

    @Override
    public MessageConsumer createSharedDurableConsumer(final Topic topic, final String name) throws JMSException {
        return createDurableSubscriber(topic, name);
    }

    @Override
    public MessageConsumer createSharedDurableConsumer(final Topic topic, final String name, final String messageSelector) throws JMSException {
        return createDurableSubscriber(topic, name, messageSelector, false);
    }
}

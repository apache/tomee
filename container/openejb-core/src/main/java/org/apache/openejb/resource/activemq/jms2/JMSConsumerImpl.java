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

import jakarta.jms.JMSConsumer;
import jakarta.jms.JMSException;
import jakarta.jms.JMSRuntimeException;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageListener;

import static org.apache.openejb.resource.activemq.jms2.JMS2.toRuntimeException;

public class JMSConsumerImpl implements JMSConsumer {
    private final JMSContextImpl context;
    private final MessageConsumer consumer;

    public JMSConsumerImpl(final JMSContextImpl jmsContext, final MessageConsumer consumer) {
        this.context = jmsContext;
        this.consumer = consumer;
    }

    @Override
    public String getMessageSelector() {
        try {
            return consumer.getMessageSelector();
        } catch (JMSException e) {
            throw toRuntimeException(e);
        }
    }

    @Override
    public MessageListener getMessageListener() throws JMSRuntimeException {
        try {
            return consumer.getMessageListener();
        } catch (JMSException e) {
            throw toRuntimeException(e);
        }
    }

    @Override
    public void setMessageListener(final MessageListener listener) throws JMSRuntimeException {
        try {
            consumer.setMessageListener(new ContextUpdaterMessageListenerWrapper(context, listener));
        } catch (JMSException e) {
            throw toRuntimeException(e);
        }
    }

    @Override
    public Message receive() {
        try {
            return context.setLastMessage(wrap(consumer.receive()));
        } catch (JMSException e) {
            throw toRuntimeException(e);
        }
    }

    @Override
    public Message receive(final long timeout) {
        try {
            return context.setLastMessage(wrap(consumer.receive(timeout)));
        } catch (JMSException e) {
            throw toRuntimeException(e);
        }
    }

    @Override
    public Message receiveNoWait() {
        try {
            return context.setLastMessage(wrap(consumer.receiveNoWait()));
        } catch (JMSException e) {
            throw toRuntimeException(e);
        }
    }

    @Override
    public void close() {
        try {
            consumer.close();
        } catch (JMSException e) {
            throw toRuntimeException(e);
        }
    }

    @Override
    public <T> T receiveBody(final Class<T> c) {
        try {
            final Message message = wrap(consumer.receive());
            context.setLastMessage(message);
            return message == null ? null : message.getBody(c);
        } catch (JMSException e) {
            throw toRuntimeException(e);
        }
    }

    @Override
    public <T> T receiveBody(final Class<T> c, final long timeout) {
        try {
            final Message message = wrap(consumer.receive(timeout));
            context.setLastMessage(message);
            return message == null ? null : message.getBody(c);
        } catch (final JMSException e) {
            throw toRuntimeException(e);
        }
    }

    @Override
    public <T> T receiveBodyNoWait(final Class<T> c) {
        try {
            final Message message = wrap(consumer.receiveNoWait());
            context.setLastMessage(message);
            return message == null ? null : message.getBody(c);
        } catch (JMSException e) {
            throw toRuntimeException(e);
        }
    }

    private static Message wrap(final Message message) {
        if (message == null) {
            return null;
        }
        final Message wrapped = JMS2.wrap(message);
        try {
            wrapped.setJMSDeliveryTime(System.currentTimeMillis());
        } catch (final JMSException e) {
            // no-op: TODO: investigate if an issue or not in this context
        }
        return wrapped;
    }

    private static final class ContextUpdaterMessageListenerWrapper implements MessageListener {
        private final JMSContextImpl context;
        private final MessageListener wrapped;

        private ContextUpdaterMessageListenerWrapper(final JMSContextImpl context, MessageListener wrapped) {
            this.context = context;
            this.wrapped = wrapped;
        }

        @Override
        public void onMessage(final Message message) {
            final Message wrappedMessage = wrap(message);
            context.setLastMessage(wrappedMessage);
            wrapped.onMessage(wrappedMessage);
        }
    }
}

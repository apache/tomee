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

import jakarta.jms.BytesMessage;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.ConnectionMetaData;
import jakarta.jms.Destination;
import jakarta.jms.ExceptionListener;
import jakarta.jms.IllegalStateRuntimeException;
import jakarta.jms.JMSConsumer;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.JMSProducer;
import jakarta.jms.JMSRuntimeException;
import jakarta.jms.MapMessage;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageProducer;
import jakarta.jms.ObjectMessage;
import jakarta.jms.Queue;
import jakarta.jms.QueueBrowser;
import jakarta.jms.Session;
import jakarta.jms.StreamMessage;
import jakarta.jms.TemporaryQueue;
import jakarta.jms.TemporaryTopic;
import jakarta.jms.TextMessage;
import jakarta.jms.Topic;
import jakarta.jms.XAConnection;

import java.io.Serializable;

import static org.apache.openejb.resource.activemq.jms2.JMS2.toRuntimeException;
import static org.apache.openejb.resource.activemq.jms2.JMS2.wrap;

public class JMSContextImpl implements JMSContext {
    private final int sessionMode;
    private final String username;
    private final String password;
    private final ConnectionFactory factory;
    private Session session;
    private boolean autoStart = true;
    private MessageProducer innerProducer;
    private boolean xa;
    private boolean closed;
    private Connection connection;
    private volatile Message lastMessagesWaitingAck;

    public JMSContextImpl(final ConnectionFactory factory, final int sessionMode, final String user, final String pwd,
                          final boolean xa) {
        this.factory = factory;
        this.sessionMode = sessionMode;
        this.username = user;
        this.password = pwd;
        this.xa = xa;
    }

    Message setLastMessage(final Message lastMessageReceived) {
        if (sessionMode == CLIENT_ACKNOWLEDGE) {
            lastMessagesWaitingAck = lastMessageReceived;
        }
        return lastMessageReceived;
    }

    protected Connection connection() {
        if (connection == null) {
            try {
                connection = username != null ? factory.createConnection(username, password) : factory.createConnection();
            } catch (final JMSException e) {
                throw toRuntimeException(e);
            }
        }
        return connection;
    }

    protected Session session() {
        if (session == null) {
            synchronized (this) {
                if (closed) {
                    throw new IllegalStateRuntimeException("Context is closed");
                }
                if (session == null) {
                    try {
                        Connection connection = connection();
                        if (xa) {
                            session = XAConnection.class.cast(connection).createXASession();
                        } else {
                            session = connection.createSession(sessionMode);
                        }
                    } catch (final JMSException e) {
                        throw toRuntimeException(e);
                    }
                }
            }
        }
        return session;
    }

    private synchronized void checkAutoStart() throws JMSException {
        if (closed) {
            throw new IllegalStateRuntimeException("Context is closed");
        }
        if (autoStart) {
            connection.start();
        }
    }

    private synchronized MessageProducer getInnerProducer() throws JMSException {
        if (innerProducer == null) {
            innerProducer = session().createProducer(null);
        }
        return innerProducer;
    }

    @Override
    public void acknowledge() {
        session();
        try {
            if (lastMessagesWaitingAck != null) {
                lastMessagesWaitingAck.acknowledge();
            }
        } catch (final JMSException e) {
            throw toRuntimeException(e);
        }
    }

    @Override
    public void close() {
        try {
            synchronized (this) {
                if (session != null) {
                    session.close();
                }
                if (connection != null) {
                    connection.close();
                }
                closed = true;
            }
        } catch (final JMSException e) {
            throw toRuntimeException(e);
        }
    }

    @Override
    public void commit() {
        try {
            session().commit();
        } catch (final JMSException e) {
            throw toRuntimeException(e);
        }
    }

    @Override
    public QueueBrowser createBrowser(final Queue queue) {
        try {
            final QueueBrowser browser = session().createBrowser(queue);
            checkAutoStart();
            return browser;
        } catch (final JMSException e) {
            throw toRuntimeException(e);
        }
    }

    @Override
    public QueueBrowser createBrowser(final Queue queue, final String messageSelector) {
        try {
            final QueueBrowser browser = session().createBrowser(queue, messageSelector);
            checkAutoStart();
            return browser;
        } catch (final JMSException e) {
            throw toRuntimeException(e);
        }
    }

    @Override
    public BytesMessage createBytesMessage() {
        try {
            return wrap(session().createBytesMessage());
        } catch (final JMSException e) {
            throw toRuntimeException(e);
        }
    }

    @Override
    public JMSConsumer createConsumer(final Destination destination) {
        try {
            final JMSConsumerImpl consumer = new JMSConsumerImpl(this, session().createConsumer(destination));
            checkAutoStart();
            return consumer;
        } catch (final JMSException e) {
            throw toRuntimeException(e);
        }
    }

    @Override
    public JMSConsumer createConsumer(final Destination destination, final String messageSelector) {
        try {
            final JMSConsumerImpl consumer = new JMSConsumerImpl(this, session().createConsumer(destination, messageSelector));
            checkAutoStart();
            return consumer;
        } catch (final JMSException e) {
            throw toRuntimeException(e);
        }
    }

    @Override
    public JMSConsumer createConsumer(Destination destination, String messageSelector, boolean noLocal) {
        try {
            final JMSConsumerImpl consumer = new JMSConsumerImpl(this, session().createConsumer(destination, messageSelector, noLocal));
            checkAutoStart();
            return consumer;
        } catch (final JMSException e) {
            throw toRuntimeException(e);
        }
    }

    @Override
    public JMSContext createContext(final int sessionMode) {
        if (xa) {
            throw new JMSRuntimeException("Illegal call to createContext");
        }
        return factory.createContext(sessionMode);
    }

    @Override
    public JMSConsumer createDurableConsumer(final Topic topic, final String name) {
        try {
            // JMS 2 only: final JMSConsumerImpl consumer = new JMSConsumerImpl(this, session().createDurableConsumer(topic, name));
            final MessageConsumer delegate = session().createDurableSubscriber(topic, name);
            checkAutoStart();
            return new JMSConsumerImpl(this, delegate);
        } catch (final JMSException e) {
            throw toRuntimeException(e);
        }
    }

    @Override
    public JMSConsumer createDurableConsumer(final Topic topic, final String name, final String messageSelector, final boolean noLocal) {
        try {
            // JMS 2 only: final JMSConsumerImpl consumer = new JMSConsumerImpl(this, session().createDurableConsumer(topic, name, messageSelector, noLocal));
            final MessageConsumer delegate = session().createDurableSubscriber(topic, name, messageSelector, noLocal);
            checkAutoStart();
            return new JMSConsumerImpl(this, delegate);
        } catch (final JMSException e) {
            throw toRuntimeException(e);
        }
    }

    @Override
    public MapMessage createMapMessage() {
        try {
            return wrap(session().createMapMessage());
        } catch (final JMSException e) {
            throw toRuntimeException(e);
        }
    }

    @Override
    public Message createMessage() {
        try {
            return wrap(session().createMessage());
        } catch (final JMSException e) {
            throw toRuntimeException(e);
        }
    }

    @Override
    public ObjectMessage createObjectMessage() {
        try {
            return wrap(session().createObjectMessage());
        } catch (final JMSException e) {
            throw toRuntimeException(e);
        }
    }

    @Override
    public ObjectMessage createObjectMessage(final Serializable object) {
        try {
            return wrap(session().createObjectMessage(object));
        } catch (final JMSException e) {
            throw toRuntimeException(e);
        }
    }

    @Override
    public JMSProducer createProducer() {
        try {
            return new JMSProducerImpl(this, getInnerProducer());
        } catch (final JMSException e) {
            throw toRuntimeException(e);
        }
    }

    @Override
    public Queue createQueue(final String queueName) {
        try {
            return session().createQueue(queueName);
        } catch (final JMSException e) {
            throw toRuntimeException(e);
        }
    }

    @Override
    public JMSConsumer createSharedConsumer(final Topic topic, final String sharedSubscriptionName) {
        try {
            final JMSConsumerImpl consumer = new JMSConsumerImpl(this, session().createSharedConsumer(topic, sharedSubscriptionName));
            checkAutoStart();
            return consumer;
        } catch (final JMSException e) {
            throw toRuntimeException(e);
        }
    }

    @Override
    public JMSConsumer createSharedConsumer(final Topic topic, final String sharedSubscriptionName, final String messageSelector) {
        try {
            final JMSConsumerImpl consumer = new JMSConsumerImpl(this, session().createSharedConsumer(topic, sharedSubscriptionName, messageSelector));
            checkAutoStart();
            return consumer;
        } catch (final JMSException e) {
            throw toRuntimeException(e);
        }
    }

    @Override
    public JMSConsumer createSharedDurableConsumer(final Topic topic, final String name) {
        try {
            final JMSConsumerImpl consumer = new JMSConsumerImpl(this, session().createSharedDurableConsumer(topic, name));
            checkAutoStart();
            return consumer;
        } catch (final JMSException e) {
            throw toRuntimeException(e);
        }
    }

    @Override
    public JMSConsumer createSharedDurableConsumer(final Topic topic, final String name, final String messageSelector) {
        try {
            final JMSConsumerImpl consumer = new JMSConsumerImpl(this, session().createSharedDurableConsumer(topic, name, messageSelector));
            checkAutoStart();
            return consumer;
        } catch (final JMSException e) {
            throw toRuntimeException(e);
        }
    }

    @Override
    public StreamMessage createStreamMessage() {
        try {
            return wrap(session().createStreamMessage());
        } catch (final JMSException e) {
            throw toRuntimeException(e);
        }
    }

    @Override
    public TemporaryQueue createTemporaryQueue() {
        try {
            return session().createTemporaryQueue();
        } catch (final JMSException e) {
            throw toRuntimeException(e);
        }
    }

    @Override
    public TemporaryTopic createTemporaryTopic() {
        try {
            return session().createTemporaryTopic();
        } catch (final JMSException e) {
            throw toRuntimeException(e);
        }
    }

    @Override
    public TextMessage createTextMessage() {
        try {
            return wrap(session().createTextMessage());
        } catch (final JMSException e) {
            throw toRuntimeException(e);
        }
    }

    @Override
    public TextMessage createTextMessage(final String text) {
        try {
            return wrap(session().createTextMessage(text));
        } catch (final JMSException e) {
            throw toRuntimeException(e);
        }
    }

    @Override
    public Topic createTopic(final String topicName) {
        try {
            return session().createTopic(topicName);
        } catch (final JMSException e) {
            throw toRuntimeException(e);
        }
    }

    @Override
    public boolean getAutoStart() {
        return autoStart;
    }

    @Override
    public String getClientID() {
        try {
            return connection().getClientID();
        } catch (final JMSException e) {
            throw toRuntimeException(e);
        }
    }

    @Override
    public ExceptionListener getExceptionListener() {
        try {
            return connection().getExceptionListener();
        } catch (final JMSException e) {
            throw toRuntimeException(e);
        }
    }

    @Override
    public ConnectionMetaData getMetaData() {
        try {
            return connection().getMetaData();
        } catch (final JMSException e) {
            throw toRuntimeException(e);
        }
    }

    @Override
    public int getSessionMode() {
        return sessionMode;
    }

    @Override
    public boolean getTransacted() {
        try {
            return session().getTransacted();
        } catch (final JMSException e) {
            throw toRuntimeException(e);
        }
    }

    @Override
    public void recover() {
        try {
            session().recover();
        } catch (final JMSException e) {
            throw toRuntimeException(e);
        }
    }

    @Override
    public void rollback() {
        session();
        try {
            session().rollback();
        } catch (final JMSException e) {
            throw toRuntimeException(e);
        }
    }

    @Override
    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }

    @Override
    public void setClientID(final String clientID) {
        if (xa) {
            throw new JMSRuntimeException("Illegal call to setClientID");
        }
        try {
            connection().setClientID(clientID);
        } catch (final JMSException e) {
            throw toRuntimeException(e);
        }
    }

    @Override
    public void setExceptionListener(ExceptionListener listener) {
        if (xa) {
            throw new JMSRuntimeException("Illegal call to setExceptionListener");
        }
        try {
            connection().setExceptionListener(listener);
        } catch (final JMSException e) {
            throw toRuntimeException(e);
        }
    }

    @Override
    public void start() {
        try {
            connection().start();
        } catch (final JMSException e) {
            throw toRuntimeException(e);
        }
    }

    @Override
    public void stop() {
        if (xa) {
            throw new JMSRuntimeException("Illegal call to stop");
        }
        try { // TODO: ref counting
            connection().stop();
        } catch (final JMSException e) {
            throw toRuntimeException(e);
        }
    }

    @Override
    public void unsubscribe(final String name) {
        try {
            session().unsubscribe(name);
        } catch (final JMSException e) {
            throw toRuntimeException(e);
        }
    }
}

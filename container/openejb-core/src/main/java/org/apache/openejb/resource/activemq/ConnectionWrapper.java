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
package org.apache.openejb.resource.activemq;

import javax.jms.Connection;
import javax.jms.ConnectionConsumer;
import javax.jms.ConnectionMetaData;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.ServerSessionPool;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicSession;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectionWrapper implements Connection, TopicConnection, QueueConnection {

    private final ReentrantLock lock = new ReentrantLock();
    private final ArrayList<SessionWrapper> sessions = new ArrayList<SessionWrapper>();

    private final String name;
    private final Connection con;

    public ConnectionWrapper(final String name, final Connection con) {
        this.name = name;
        this.con = con;
    }

    @Override
    public Session createSession(final boolean transacted, final int acknowledgeMode) throws JMSException {
        return this.getSession(con.createSession(transacted, acknowledgeMode));
    }

    private Session getSession(final Session session) {
        lock.lock();
        try {
            final SessionWrapper wrapper = new SessionWrapper(this, session);
            sessions.add(wrapper);
            return wrapper;
        } finally {
            lock.unlock();
        }
    }

    protected void remove(final SessionWrapper wrapper) {
        lock.lock();
        try {
            sessions.remove(wrapper);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String getClientID() throws JMSException {
        return con.getClientID();
    }

    @Override
    public TopicSession createTopicSession(final boolean transacted, final int acknowledgeMode) throws JMSException {
        return TopicSession.class.cast(this.getSession(TopicConnection.class.cast(this.con).createTopicSession(transacted, acknowledgeMode)));
    }

    @Override
    public ConnectionConsumer createConnectionConsumer(final Topic topic, final String messageSelector, final ServerSessionPool sessionPool, final int maxMessages) throws JMSException {
        return TopicConnection.class.cast(this.con).createConnectionConsumer(topic, messageSelector, sessionPool, maxMessages);
    }

    @Override
    public QueueSession createQueueSession(final boolean transacted, final int acknowledgeMode) throws JMSException {
        return QueueSession.class.cast(this.getSession(QueueConnection.class.cast(this.con).createQueueSession(transacted, acknowledgeMode)));
    }

    @Override
    public ConnectionConsumer createConnectionConsumer(final Queue queue, final String messageSelector, final ServerSessionPool sessionPool, final int maxMessages) throws JMSException {
        return QueueConnection.class.cast(this.con).createConnectionConsumer(queue, messageSelector, sessionPool, maxMessages);
    }

    @Override
    public ConnectionConsumer createDurableConnectionConsumer(final Topic topic, final String subscriptionName, final String messageSelector, final ServerSessionPool sessionPool, final int maxMessages) throws JMSException {
        return con.createDurableConnectionConsumer(topic, subscriptionName, messageSelector, sessionPool, maxMessages);
    }

    @Override
    public ExceptionListener getExceptionListener() throws JMSException {
        return con.getExceptionListener();
    }

    @Override
    public void setClientID(final String clientID) throws JMSException {
        con.setClientID(clientID);
    }

    @Override
    public ConnectionConsumer createConnectionConsumer(final Destination destination, final String messageSelector, final ServerSessionPool sessionPool, final int maxMessages) throws JMSException {
        return con.createConnectionConsumer(destination, messageSelector, sessionPool, maxMessages);
    }

    @Override
    public ConnectionMetaData getMetaData() throws JMSException {
        return con.getMetaData();
    }

    @Override
    public void close() throws JMSException {
        lock.lock();
        try {
            final Iterator<SessionWrapper> iterator = sessions.iterator();

            while (iterator.hasNext()) {
                final SessionWrapper next = iterator.next();
                iterator.remove();
                try {
                    next.close();
                } catch (final Exception e) {
                    //no-op
                } finally {
                    Logger.getLogger(ConnectionFactoryWrapper.class.getName()).log(Level.SEVERE, "Closed a JMS session. You have an application that fails to close a session "
                            + "created by this injection path: " + this.name);
                }
            }

            try {
                con.close();
            } finally {
                ConnectionFactoryWrapper.remove(this);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void stop() throws JMSException {
        con.stop();
    }

    @Override
    public void setExceptionListener(final ExceptionListener listener) throws JMSException {
        con.setExceptionListener(listener);
    }

    @Override
    public void start() throws JMSException {
        con.start();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ConnectionWrapper that = (ConnectionWrapper) o;

        return con.equals(that.con);

    }

    @Override
    public int hashCode() {
        return con.hashCode();
    }


    public String getName() {
        return this.name;
    }
}

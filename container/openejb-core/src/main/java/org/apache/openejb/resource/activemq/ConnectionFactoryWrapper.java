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
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectionFactoryWrapper implements ConnectionFactory, TopicConnectionFactory, QueueConnectionFactory {

    private static final ReentrantLock lock = new ReentrantLock();
    private static final ArrayList<ConnectionWrapper> connections = new ArrayList<ConnectionWrapper>();

    private final org.apache.activemq.ra.ActiveMQConnectionFactory factory;
    private final String name;

    public ConnectionFactoryWrapper(final String name, final Object factory) {
        this.name = name;
        this.factory = org.apache.activemq.ra.ActiveMQConnectionFactory.class.cast(factory);
    }

    @Override
    public Connection createConnection() throws JMSException {
        return getConnection(this.name, this.factory.createConnection());
    }

    @Override
    public Connection createConnection(final String userName, final String password) throws JMSException {
        return getConnection(this.name, this.factory.createConnection(userName, password));
    }

    private static Connection getConnection(final String name, final Connection connection) {
        lock.lock();
        try {
            final ConnectionWrapper wrapper = new ConnectionWrapper(name, connection);
            connections.add(wrapper);
            return wrapper;
        } finally {
            lock.unlock();
        }
    }

    protected static void remove(final ConnectionWrapper connectionWrapper) {
        lock.lock();
        try {
            connections.remove(connectionWrapper);
        } finally {
            lock.unlock();
        }
    }

    public static void closeConnections() {
        lock.lock();
        try {
            final Iterator<ConnectionWrapper> iterator = connections.iterator();

            ConnectionWrapper next;
            while (iterator.hasNext()) {
                next = iterator.next();
                iterator.remove();
                try {
                    next.close();
                } catch (final Exception e) {
                    //no-op
                } finally {
                    Logger.getLogger(ConnectionFactoryWrapper.class.getName()).log(Level.SEVERE, "Closed a JMS connection. You have an application that fails to close a connection "
                            + "created by this injection path: " + next.getName());
                }
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public QueueConnection createQueueConnection() throws JMSException {
        return QueueConnection.class.cast(getConnection(this.name, this.factory.createQueueConnection()));
    }

    @Override
    public QueueConnection createQueueConnection(final String userName, final String password) throws JMSException {
        return QueueConnection.class.cast(getConnection(this.name, this.factory.createQueueConnection(userName, password)));
    }

    @Override
    public TopicConnection createTopicConnection() throws JMSException {
        return TopicConnection.class.cast(getConnection(this.name, this.factory.createTopicConnection()));
    }

    @Override
    public TopicConnection createTopicConnection(final String userName, final String password) throws JMSException {
        return TopicConnection.class.cast(getConnection(this.name, this.factory.createTopicConnection(userName, password)));
    }
}

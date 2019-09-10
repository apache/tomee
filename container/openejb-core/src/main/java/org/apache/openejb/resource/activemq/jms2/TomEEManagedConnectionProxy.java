/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.resource.activemq.jms2;

import org.apache.activemq.ra.ActiveMQManagedConnection;
import org.apache.activemq.ra.ManagedConnectionProxy;
import org.apache.openejb.OpenEJB;

import javax.jms.Connection;
import javax.jms.ConnectionConsumer;
import javax.jms.ExceptionListener;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.ServerSessionPool;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.XAConnection;
import javax.jms.XASession;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.TransactionSupport.TransactionSupportLevel;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

public class TomEEManagedConnectionProxy extends ManagedConnectionProxy
    // cause org.apache.openejb.resource.AutoConnectionTracker.proxyConnection() just uses getInterfaces()
    implements Connection, QueueConnection, TopicConnection, ExceptionListener, XAConnection {

    private volatile ActiveMQManagedConnection connection;

    public TomEEManagedConnectionProxy(final ActiveMQManagedConnection managedConnection, final ConnectionRequestInfo info) {
        super(managedConnection, info);
        connection = managedConnection;
    }

    @Override
    public void cleanup() {
        super.cleanup();
        connection = null;
    }

    @Override
    public Session createSession(final int acknowledgeMode) throws JMSException {
        // For the next three methods, we ignore the requested session mode per the
        // spec:
        // https://docs.oracle.com/javaee/7/api/javax/jms/Connection.html#createSession-int-
        //
        // But we also allow the user to override this behavior. If they set
        // transactionSupport on the connection factory
        // we will not return to them a xa session, even though the underlying physical
        // connection may support XA.

        int mode;
        boolean xa;
        TransactionSupportLevel transactionSupportLevel;
        if (connection instanceof TomEEManagedConnection) {
            transactionSupportLevel = ((TomEEManagedConnection) connection).getTransactionSupportLevel();
        } else {
            transactionSupportLevel = TransactionSupportLevel.XATransaction;
        }
        switch (transactionSupportLevel) {
            case XATransaction:
                if (JMS2.inTx()) {
                    mode = -1;
                    xa = true;
                    break;
                }
            case NoTransaction:
                mode = acknowledgeMode;
                xa = false;
                break;
            case LocalTransaction:
                mode = JMSContext.SESSION_TRANSACTED;
                xa = false;
                break;
            default:
                throw new IllegalStateException("transactionSupportLevel mode not supported:" + transactionSupportLevel);
        }
        if (xa) {
            return createXASession();
        } else {
            return connection.getPhysicalConnection().createSession(mode);
        }
    }

    @Override
    public Session createSession(boolean transacted, int acknowledgeMode) throws JMSException {
        int mode;
        boolean xa;
        TransactionSupportLevel transactionSupportLevel;
        if (connection instanceof TomEEManagedConnection) {
            transactionSupportLevel = ((TomEEManagedConnection) connection).getTransactionSupportLevel();
        } else if (!transacted) {
            transactionSupportLevel = TransactionSupportLevel.NoTransaction;
        } else {
            transactionSupportLevel = TransactionSupportLevel.XATransaction;
        }
        switch (transactionSupportLevel) {
            case XATransaction:
                if (JMS2.inTx()) {
                    mode = -1;
                    xa = true;
                    break;
                }
            case NoTransaction:
                mode = acknowledgeMode;
                xa = false;
                break;
            case LocalTransaction:
                mode = JMSContext.SESSION_TRANSACTED;
                xa = false;
                break;
            default:
                throw new IllegalStateException("transactionSupportLevel mode not supported:" + transactionSupportLevel);
        }
        if (xa) {
            return createXASession();
        } else {
            return connection.getPhysicalConnection().createSession(mode);
        }
    }

    @Override
    public Session createSession() throws JMSException {
        int mode;
        boolean xa;
        TransactionSupportLevel transactionSupportLevel;
        if (connection instanceof TomEEManagedConnection) {
            transactionSupportLevel = ((TomEEManagedConnection) connection).getTransactionSupportLevel();
        } else {
            transactionSupportLevel = TransactionSupportLevel.XATransaction;
        }
        switch (transactionSupportLevel) {
            case XATransaction:
                if (JMS2.inTx()) {
                    mode = -1;
                    xa = true;
                    break;
                }
            case NoTransaction:
                mode = JMSContext.AUTO_ACKNOWLEDGE;
                xa = false;
                break;
            case LocalTransaction:
                mode = JMSContext.SESSION_TRANSACTED;
                xa = false;
                break;
            default:
                throw new IllegalStateException("transactionSupportLevel mode not supported:" + transactionSupportLevel);
        }
        if (xa) {
            return createXASession();
        } else {
            return connection.getPhysicalConnection().createSession(mode);
        }
    }

    @Override
    public ConnectionConsumer createSharedDurableConnectionConsumer(final Topic topic, final String subscriptionName,
                                                                    final String messageSelector, final ServerSessionPool sessionPool,
                                                                    final int maxMessages) throws JMSException {
        return connection.getPhysicalConnection().createSharedDurableConnectionConsumer(topic, subscriptionName, messageSelector, sessionPool, maxMessages);
    }

    @Override
    public ConnectionConsumer createSharedConnectionConsumer(final Topic topic, final String subscriptionName, final String messageSelector,
                                                             final ServerSessionPool sessionPool, final int maxMessages) throws JMSException {
        return connection.getPhysicalConnection().createSharedConnectionConsumer(topic, subscriptionName, messageSelector, sessionPool, maxMessages);
    }

    @Override
    public XASession createXASession() throws JMSException {
        XASession session = ((XAConnection) connection.getPhysicalConnection()).createXASession();
        try {
            OpenEJB.getTransactionManager().getTransaction().enlistResource(session.getXAResource());
        } catch (IllegalStateException | SystemException | RollbackException e) {
            throw new RuntimeException(e);
        }
        return session;
    }
}

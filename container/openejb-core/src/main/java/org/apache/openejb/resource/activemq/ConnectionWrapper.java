/**
 * Tomitribe Confidential
 * <p/>
 * Copyright(c) Tomitribe Corporation. 2014
 * <p/>
 * The source code for this program is not published or otherwise divested
 * of its trade secrets, irrespective of what has been deposited with the
 * U.S. Copyright Office.
 * <p/>
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
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectionWrapper implements Connection, TopicConnection, QueueConnection {

    private final ArrayList<SessionWrapper> sessions = new ArrayList<SessionWrapper>();

    private final Connection con;

    public ConnectionWrapper(final Connection con) {
        this.con = con;
    }

    @Override
    public Session createSession(final boolean transacted, final int acknowledgeMode) throws JMSException {
        return getSession(con.createSession(transacted, acknowledgeMode));
    }

    private Session getSession(final Session session) {
        final SessionWrapper wrapper = new SessionWrapper(this, session);
        sessions.add(wrapper);
        return wrapper;
    }

    protected void remove(final SessionWrapper wrapper) {
        sessions.remove(wrapper);
    }

    @Override
    public String getClientID() throws JMSException {
        return con.getClientID();
    }

    @Override
    public TopicSession createTopicSession(final boolean transacted, final int acknowledgeMode) throws JMSException {
        return TopicConnection.class.cast(this.con).createTopicSession(transacted, acknowledgeMode);
    }

    @Override
    public ConnectionConsumer createConnectionConsumer(final Topic topic, final String messageSelector, final ServerSessionPool sessionPool, final int maxMessages) throws JMSException {
        return TopicConnection.class.cast(this.con).createConnectionConsumer(topic, messageSelector, sessionPool, maxMessages);
    }

    @Override
    public QueueSession createQueueSession(final boolean transacted, final int acknowledgeMode) throws JMSException {
        return QueueConnection.class.cast(this.con).createQueueSession(transacted, acknowledgeMode);
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

        final Iterator<SessionWrapper> iterator = sessions.iterator();

        while (iterator.hasNext()) {
            final SessionWrapper next = iterator.next();
            iterator.remove();
            try {
                next.close();
            } catch (final Exception e) {
                //no-op
            } finally {
                Logger.getLogger(ConnectionFactoryWrapper.class.getName()).log(Level.SEVERE, "Closed a JMS session. You have an application that fails to close this session");
            }
        }

        try {
            con.close();
        } finally {
            ConnectionFactoryWrapper.remove(this);
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


}

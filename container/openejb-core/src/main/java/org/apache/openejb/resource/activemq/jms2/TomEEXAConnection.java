package org.apache.openejb.resource.activemq.jms2;

import org.apache.activemq.ActiveMQXAConnection;
import org.apache.activemq.management.JMSStatsImpl;
import org.apache.activemq.transport.Transport;
import org.apache.activemq.util.IdGenerator;

import javax.jms.ConnectionConsumer;
import javax.jms.JMSException;
import javax.jms.ServerSessionPool;
import javax.jms.Session;
import javax.jms.Topic;

public class TomEEXAConnection extends ActiveMQXAConnection {
    protected TomEEXAConnection(final Transport transport, final IdGenerator clientIdGenerator,
                                final IdGenerator connectionIdGenerator, final JMSStatsImpl factoryStats) throws Exception {
        super(transport, clientIdGenerator, connectionIdGenerator, factoryStats);
    }

    @Override
    public Session createSession(final int sessionMode) throws JMSException {
        return super.createSession(sessionMode == Session.SESSION_TRANSACTED, sessionMode);
    }

    @Override
    public Session createSession() throws JMSException {
        return createSession(Session.AUTO_ACKNOWLEDGE);
    }

    @Override
    public ConnectionConsumer createSharedDurableConnectionConsumer(final Topic topic, final String subscriptionName, final String messageSelector,
                                                                    final ServerSessionPool sessionPool, final int maxMessages) throws JMSException {
        throw new IllegalStateException("Not allowed in a RA");
    }

    @Override
    public ConnectionConsumer createSharedConnectionConsumer(final Topic topic, final String subscriptionName, final String messageSelector,
                                                             final ServerSessionPool sessionPool, final int maxMessages) throws JMSException {
        throw new IllegalStateException("Not allowed in a RA");
    }
}

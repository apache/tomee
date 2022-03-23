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

import org.apache.activemq.ActiveMQXAConnection;
import org.apache.activemq.management.JMSStatsImpl;
import org.apache.activemq.transport.Transport;
import org.apache.activemq.util.IdGenerator;

import jakarta.jms.ConnectionConsumer;
import jakarta.jms.JMSException;
import jakarta.jms.ServerSessionPool;
import jakarta.jms.Session;
import jakarta.jms.Topic;

public class TomEEXAConnection extends ActiveMQXAConnection {
    protected TomEEXAConnection(final Transport transport, final IdGenerator clientIdGenerator,
                                final IdGenerator connectionIdGenerator, final JMSStatsImpl factoryStats) throws Exception {
        super(transport, clientIdGenerator, connectionIdGenerator, factoryStats);
    }

    @Override
    public Session createSession(final boolean transacted, final int acknowledgeMode) throws JMSException {
        checkClosedOrFailed();
        ensureConnectionInfoSent();
        return new TomEEXASession(this, getNextSessionId(), getXaAckMode() > 0 ? getXaAckMode() : Session.SESSION_TRANSACTED, isDispatchAsync());
    }

    @Override
    public Session createSession(final int sessionMode) throws JMSException {
        return createSession(sessionMode == Session.SESSION_TRANSACTED, sessionMode);
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

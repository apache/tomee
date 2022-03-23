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

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ra.ActiveMQConnectionRequestInfo;
import org.apache.activemq.ra.ActiveMQManagedConnectionFactory;
import org.apache.activemq.ra.MessageActivationSpec;
import org.apache.activemq.ra.SimpleConnectionManager;

import java.util.Locale;

import jakarta.jms.JMSException;
import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionManager;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.TransactionSupport.TransactionSupportLevel;
import javax.security.auth.Subject;

public class TomEEManagedConnectionFactory extends ActiveMQManagedConnectionFactory {
    private static final long serialVersionUID = 1L;
    private TransactionSupportLevel transactionSupportLevel;

    @Override
    public Object createConnectionFactory(final ConnectionManager manager) throws ResourceException {
        TomEERAConnectionFactory factory = new TomEERAConnectionFactory(this, manager, getInfo());
        factory.setTransactionSupport(transactionSupportLevel);
        return factory;
    }

    @Override
    public Object createConnectionFactory() throws ResourceException {
        return createConnectionFactory(new SimpleConnectionManager());
    }

    @Override
    protected ActiveMQConnectionFactory createConnectionFactory(final ActiveMQConnectionRequestInfo connectionRequestInfo, final MessageActivationSpec activationSpec) {
        final TomEEConnectionFactory connectionFactory = new TomEEConnectionFactory(transactionSupportLevel);
        connectionRequestInfo.configure(connectionFactory, activationSpec);
        return connectionFactory;
    }

    @Override
    public ManagedConnection createManagedConnection(final Subject subject, final ConnectionRequestInfo connectionRequestInfo) throws ResourceException {
        final ActiveMQConnectionRequestInfo amqInfo;
        if (ActiveMQConnectionRequestInfo.class.isInstance(connectionRequestInfo)) {
            amqInfo = ActiveMQConnectionRequestInfo.class.cast(connectionRequestInfo);
        } else {
            amqInfo = getInfo();
        }
        try {
            return new TomEEManagedConnection(subject, makeConnection(amqInfo), amqInfo, transactionSupportLevel);
        } catch (final JMSException e) {
            throw new ResourceException("Could not create connection.", e);
        }
    }

    @Override
    public boolean equals(final Object object) {
        return !(object == null || !getClass().isInstance(object))
            && ((ActiveMQManagedConnectionFactory) object).getInfo().equals(getInfo());
    }

    public String getTransactionSupport() {
        switch (transactionSupportLevel) {
            case XATransaction:
                return "xa";
            case LocalTransaction:
                return "local";
            case NoTransaction:
                return "none";
            default:
                return null;
        }
    }

    public void setTransactionSupport(String transactionSupport) {
        if (transactionSupport == null) {
            throw new IllegalArgumentException("transactionSupport cannot be not null");
        } else {
            switch (transactionSupport.toLowerCase(Locale.ENGLISH)) {
                case "xa":
                    transactionSupportLevel = TransactionSupportLevel.XATransaction;
                    break;
                case "local":
                    transactionSupportLevel = TransactionSupportLevel.LocalTransaction;
                    break;
                case "none":
                    transactionSupportLevel = TransactionSupportLevel.NoTransaction;
                    break;
                default:
                    throw new IllegalArgumentException("transactionSupport must be xa, local, or none:" + transactionSupport);
            }
        }
    }
}

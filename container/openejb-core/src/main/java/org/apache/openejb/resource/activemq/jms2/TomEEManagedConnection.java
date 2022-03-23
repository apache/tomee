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

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ra.ActiveMQConnectionRequestInfo;
import org.apache.activemq.ra.ActiveMQManagedConnection;
import org.apache.activemq.ra.ManagedConnectionProxy;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.TransactionSupport.TransactionSupportLevel;
import javax.security.auth.Subject;
import java.lang.reflect.Field;
import java.util.Collection;

public class TomEEManagedConnection extends ActiveMQManagedConnection {
    private static final Field PROXY_CONNECTIONS_FIELD;
    private TransactionSupportLevel transactionSupportLevel;

    static {
        try {
            PROXY_CONNECTIONS_FIELD = ActiveMQManagedConnection.class.getDeclaredField("proxyConnections");
        } catch (final NoSuchFieldException e) {
            throw new IllegalStateException("Incompatible AMQ", e);
        }
        PROXY_CONNECTIONS_FIELD.setAccessible(true);
    }

    private final Collection<ManagedConnectionProxy> proxyConnections;

    @SuppressWarnings("unchecked")
    public TomEEManagedConnection(final Subject subject, final ActiveMQConnection physicalConnection,
                                  final ActiveMQConnectionRequestInfo info, TransactionSupportLevel transactionSupportLevel) throws ResourceException {
        super(subject, physicalConnection, info);
        try {
            proxyConnections = Collection.class.cast(PROXY_CONNECTIONS_FIELD.get(this));
        } catch (final IllegalAccessException e) {
            throw new IllegalStateException("Incompatible AMQ", e);
        }
        this.transactionSupportLevel = transactionSupportLevel;
    }

    @Override
    public Object getConnection(final Subject subject, final ConnectionRequestInfo info) throws ResourceException {
        final ManagedConnectionProxy proxy = new TomEEManagedConnectionProxy(this, info);
        proxyConnections.add(proxy);
        return proxy;
    }

    public TransactionSupportLevel getTransactionSupportLevel() {
        return transactionSupportLevel;
    }
}

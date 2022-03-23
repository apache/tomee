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
package org.apache.openejb.resource;

import org.apache.geronimo.connector.outbound.GenericConnectionManager;
import org.apache.geronimo.transaction.manager.TransactionManagerImpl;
import org.apache.openejb.util.Duration;
import org.junit.Test;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionEventListener;
import jakarta.resource.spi.ConnectionManager;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.LocalTransaction;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ManagedConnectionFactory;
import jakarta.resource.spi.ManagedConnectionMetaData;
import jakarta.resource.spi.ValidatingManagedConnectionFactory;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertTrue;

public class GeronimoConnectionManagerFactoryTest {
    @Test // ensure we don't have an exception TOMEE-1806
    public void eviction() throws Exception {
        final MyMcf mcf = new MyMcf();

        final GeronimoConnectionManagerFactory factory = new GeronimoConnectionManagerFactory();
        factory.setValidationInterval(new Duration("1 second"));
        factory.setTransactionSupport("local");
        factory.setMcf(mcf);
        factory.setPooling(true);
        factory.setPartitionStrategy("none");
        factory.setTransactionManager(new TransactionManagerImpl());
        factory.setPoolMinSize(1);
        factory.setAllConnectionsEqual(true);
        final GenericConnectionManager mgr = factory.create();
        mgr.doStart();
        try {
            mgr.allocateConnection(mcf, new ConnectionRequestInfo() { // just to use it
            });
            sleep(2500);
            assertTrue(mcf.evicted.get());
            assertTrue(mcf.destroyed.get());
        } finally {
            mgr.doStop();
        }
    }

    @Test
    public void evictionNotAllEquals() throws Exception {
        final MyMcf mcf = new MyMcf();

        final GeronimoConnectionManagerFactory factory = new GeronimoConnectionManagerFactory();
        factory.setValidationInterval(new Duration("1 second"));
        factory.setTransactionSupport("local");
        factory.setMcf(mcf);
        factory.setPooling(true);
        factory.setPartitionStrategy("none");
        factory.setTransactionManager(new TransactionManagerImpl());
        factory.setPoolMinSize(1);
        factory.setAllConnectionsEqual(false);
        final GenericConnectionManager mgr = factory.create();
        mgr.doStart();
        try {
            mgr.allocateConnection(mcf, new ConnectionRequestInfo() { // just to use it
            });
            sleep(2500);
            assertTrue(mcf.evicted.get());
            assertTrue(mcf.destroyed.get());
        } finally {
            mgr.doStop();
        }
    }

    public static class MyMcf implements ManagedConnectionFactory, ValidatingManagedConnectionFactory {
        private final Set<ManagedConnection> connections = new HashSet<>();
        private final AtomicBoolean evicted = new AtomicBoolean(false);
        private final AtomicBoolean destroyed = new AtomicBoolean(false);

        @Override
        public Object createConnectionFactory(final ConnectionManager cxManager) throws ResourceException {
            return null;
        }

        @Override
        public Object createConnectionFactory() throws ResourceException {
            return null;
        }

        @Override
        public ManagedConnection createManagedConnection(final Subject subject, final ConnectionRequestInfo cxRequestInfo) throws ResourceException {
            return new ManagedConnection() {
                @Override
                public Object getConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
                    connections.add(this);
                    return this;
                }

                @Override
                public void destroy() throws ResourceException {
                    connections.remove(this);
                    destroyed.set(true);
                }

                @Override
                public void cleanup() throws ResourceException {
                    // no-op
                }

                @Override
                public void associateConnection(Object connection) throws ResourceException {
                    // no-op
                }

                @Override
                public void addConnectionEventListener(ConnectionEventListener listener) {
                    // no-op
                }

                @Override
                public void removeConnectionEventListener(ConnectionEventListener listener) {
                    // no-op
                }

                @Override
                public XAResource getXAResource() throws ResourceException {
                    return null;
                }

                @Override
                public LocalTransaction getLocalTransaction() throws ResourceException {
                    return new LocalTransaction() {
                        @Override
                        public void begin() throws ResourceException {

                        }

                        @Override
                        public void commit() throws ResourceException {

                        }

                        @Override
                        public void rollback() throws ResourceException {

                        }
                    };
                }

                @Override
                public ManagedConnectionMetaData getMetaData() throws ResourceException {
                    return null;
                }

                @Override
                public void setLogWriter(PrintWriter out) throws ResourceException {
                    // no-op
                }

                @Override
                public PrintWriter getLogWriter() throws ResourceException {
                    return null;
                }
            };
        }

        @Override
        public ManagedConnection matchManagedConnections(final Set connectionSet, final Subject subject,
                                                         final ConnectionRequestInfo cxRequestInfo) throws ResourceException {
            return null;
        }

        @Override
        public void setLogWriter(PrintWriter out) throws ResourceException {
            // no-op
        }

        @Override
        public PrintWriter getLogWriter() throws ResourceException {
            return null;
        }

        @Override
        public Set getInvalidConnections(final Set connectionSet) throws ResourceException {
            evicted.set(true);
            return connections;
        }
    }
}

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
package org.apache.openejb.config;

import org.apache.openejb.jee.ConfigProperty;
import org.apache.openejb.jee.ConnectionDefinition;
import org.apache.openejb.jee.Connector;
import org.apache.openejb.jee.OutboundResourceAdapter;
import org.apache.openejb.jee.ResourceAdapter;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testing.SimpleLog;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import javax.naming.NamingException;
import jakarta.resource.ResourceException;
import jakarta.resource.cci.ConnectionFactory;
import jakarta.resource.spi.ActivationSpec;
import jakarta.resource.spi.BootstrapContext;
import jakarta.resource.spi.ConnectionEventListener;
import jakarta.resource.spi.ConnectionManager;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.LocalTransaction;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ManagedConnectionFactory;
import jakarta.resource.spi.ManagedConnectionMetaData;
import jakarta.resource.spi.ResourceAdapterInternalException;
import jakarta.resource.spi.ValidatingManagedConnectionFactory;
import jakarta.resource.spi.endpoint.MessageEndpointFactory;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@SimpleLog
@RunWith(ApplicationComposer.class)
public class GetterConnectorTest {
    @Module
    public Connector connector() {
        final ConfigProperty configProperty = new ConfigProperty();
        configProperty.setConfigPropertyName("conf");
        configProperty.setConfigPropertyType(String.class.getName());
        configProperty.setConfigPropertyValue("GetterConnectorTest");

        final ConnectionDefinition connectionDefinition = new ConnectionDefinition();
        connectionDefinition.setConnectionFactoryImplClass(MyMcf.class.getName());
        connectionDefinition.setConnectionInterface(ConnectionFactory.class.getName());

        final OutboundResourceAdapter out = new OutboundResourceAdapter();
        out.getConnectionDefinition().add(connectionDefinition);

        final ResourceAdapter ra = new ResourceAdapter();
        ra.setResourceAdapterClass(MyRa.class.getName());
        ra.getConfigProperty().add(configProperty);

        final Connector connector = new Connector();
        connector.setVersion("1.7");
        connector.setResourceAdapter(ra);
        return connector;
    }

    @Test
    public void run() throws NamingException {
        // https://issues.apache.org/jira/browse/TOMEE-1817 is a NPE so if started we are good
        final MyRa ra = MyRa.class.cast(SystemInstance.get().getComponent(ContainerSystem.class).getJNDIContext().lookup("openejb:Resource/connectorRA"));
        assertNotNull(ra);
        assertEquals("GetterConnectorTest", ra.getConf());
    }

    public static class MyRa implements jakarta.resource.spi.ResourceAdapter {
        @jakarta.resource.spi.ConfigProperty
        private String conf;

        private String TOMEE1817;

        public String getTOMEE1817() { // getter without setter
            return TOMEE1817;
        }

        public String getConf() {
            return conf;
        }

        public void setConf(final String conf) {
            this.conf = conf;
        }

        @Override
        public void start(final BootstrapContext ctx) throws ResourceAdapterInternalException {
            // no-op
        }

        @Override
        public void stop() {
            // no-op
        }

        @Override
        public void endpointActivation(final MessageEndpointFactory endpointFactory, final ActivationSpec spec) throws ResourceException {
            // no-op
        }

        @Override
        public void endpointDeactivation(final MessageEndpointFactory endpointFactory, final ActivationSpec spec) {
            // no-op
        }

        @Override
        public XAResource[] getXAResources(final ActivationSpec[] specs) throws ResourceException {
            return new XAResource[0];
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

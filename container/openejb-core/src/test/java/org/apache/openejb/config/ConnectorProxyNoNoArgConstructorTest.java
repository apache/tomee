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

import javax.naming.NamingException;
import javax.naming.Reference;
import jakarta.resource.ResourceException;
import jakarta.resource.cci.Connection;
import jakarta.resource.cci.ConnectionFactory;
import jakarta.resource.cci.ConnectionMetaData;
import jakarta.resource.cci.ConnectionSpec;
import jakarta.resource.cci.Interaction;
import jakarta.resource.cci.RecordFactory;
import jakarta.resource.cci.ResourceAdapterMetaData;
import jakarta.resource.cci.ResultSetInfo;
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
import jakarta.resource.spi.endpoint.MessageEndpointFactory;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;
import java.io.PrintWriter;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

// dev note: the RA impl is very suspicious but it tests the fact we proxy the connection
@SimpleLog
@RunWith(ApplicationComposer.class)
public class ConnectorProxyNoNoArgConstructorTest {
    @Module
    public Connector connector() {
        final ConnectionDefinition connectionDefinition = new ConnectionDefinition();
        connectionDefinition.setId("cf");
        connectionDefinition.setConnectionImplClass(MyCon.class.getName());
        connectionDefinition.setConnectionInterface(MyConAPI.class.getName());
        connectionDefinition.setConnectionFactoryImplClass(MyMcf.class.getName());
        connectionDefinition.setConnectionFactoryInterface(ConnectionFactory.class.getName());
        connectionDefinition.setManagedConnectionFactoryClass(MyMcf.class.getName());

        final OutboundResourceAdapter out = new OutboundResourceAdapter();
        out.getConnectionDefinition().add(connectionDefinition);

        final ResourceAdapter ra = new ResourceAdapter();
        ra.setResourceAdapterClass(MyRa.class.getName());
        ra.setOutboundResourceAdapter(out);

        final Connector connector = new Connector();
        connector.setVersion("1.7");
        connector.setResourceAdapter(ra);
        return connector;
    }

    @Test
    public void run() throws NamingException, ResourceException {
        final MyCf jndi = MyCf.class.cast(SystemInstance.get().getComponent(ContainerSystem.class).getJNDIContext().lookup("openejb:Resource/cf"));
        assertNotNull(jndi);

        final Connection connection = jndi.getConnection();
        assertTrue(MyConAPI.class.isInstance(connection));
        assertTrue(MyCon.class.isInstance(connection));
    }

    public static class MyRa implements jakarta.resource.spi.ResourceAdapter {
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

    public static class MyCf implements ConnectionFactory {
        private final ConnectionManager mgr;
        private final ManagedConnectionFactory mcf;

        public MyCf(final MyMcf myMcf, final ConnectionManager cxManager) {
            this.mcf = myMcf;
            this.mgr = cxManager;
        }

        @Override
        public Connection getConnection() throws ResourceException {
            return MyConAPI.class/*impl, this is what we want to test*/.cast(mgr.allocateConnection(mcf, new ConnectionRequestInfo() {
            }));
        }

        @Override
        public Connection getConnection(ConnectionSpec properties) throws ResourceException {
            return getConnection();
        }

        @Override
        public RecordFactory getRecordFactory() throws ResourceException {
            return null;
        }

        @Override
        public ResourceAdapterMetaData getMetaData() throws ResourceException {
            return null;
        }

        @Override
        public void setReference(Reference reference) {

        }

        @Override
        public Reference getReference() throws NamingException {
            return null;
        }
    }

    public static class MyMcf implements ManagedConnectionFactory {
        @Override
        public Object createConnectionFactory(final ConnectionManager cxManager) throws ResourceException {
            return new MyCf(this, cxManager);
        }

        @Override
        public Object createConnectionFactory() throws ResourceException {
            return new MyCf(this, null);
        }

        @Override
        public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
            return new MyMC();
        }

        @Override
        public ManagedConnection matchManagedConnections(Set connectionSet, Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
            return null;
        }

        @Override
        public void setLogWriter(PrintWriter out) throws ResourceException {

        }

        @Override
        public PrintWriter getLogWriter() throws ResourceException {
            return null;
        }
    }

    public interface MyConAPI extends Connection {
    }

    public static class MyCon implements MyConAPI {
        public MyCon(final String noNoArgConstructor) {
            // no-op
        }

        public String specific() {
            return "yes";
        }

        @Override
        public Interaction createInteraction() throws ResourceException {
            return null;
        }

        @Override
        public jakarta.resource.cci.LocalTransaction getLocalTransaction() throws ResourceException {
            return null;
        }

        @Override
        public ConnectionMetaData getMetaData() throws ResourceException {
            return null;
        }

        @Override
        public ResultSetInfo getResultSetInfo() throws ResourceException {
            return null;
        }

        @Override
        public void close() throws ResourceException {

        }
    }

    public static class MyMC implements ManagedConnection {
        @Override
        public Object getConnection(final Subject subject, final ConnectionRequestInfo cxRequestInfo) throws ResourceException {
            return new MyCon("-");
        }

        @Override
        public void destroy() throws ResourceException {

        }

        @Override
        public void cleanup() throws ResourceException {

        }

        @Override
        public void associateConnection(Object connection) throws ResourceException {

        }

        @Override
        public void addConnectionEventListener(ConnectionEventListener listener) {

        }

        @Override
        public void removeConnectionEventListener(ConnectionEventListener listener) {

        }

        @Override
        public XAResource getXAResource() throws ResourceException {
            return null;
        }

        @Override
        public LocalTransaction getLocalTransaction() throws ResourceException {
            return null;
        }

        @Override
        public ManagedConnectionMetaData getMetaData() throws ResourceException {
            return null;
        }

        @Override
        public void setLogWriter(PrintWriter out) throws ResourceException {

        }

        @Override
        public PrintWriter getLogWriter() throws ResourceException {
            return null;
        }
    }
}
